package com.github.klefstad_teaching.cs122b.billing.repo;

import com.github.klefstad_teaching.cs122b.billing.Request.CartInsertRequest;
import com.github.klefstad_teaching.cs122b.billing.Request.CartUpdateRequest;
import com.github.klefstad_teaching.cs122b.billing.entity.Item;
import com.github.klefstad_teaching.cs122b.billing.entity.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.List;

@Component
public class BillingRepo
{
    private NamedParameterJdbcTemplate template;

    @Autowired
    public BillingRepo(NamedParameterJdbcTemplate template)
    {
        this.template = template;
    }

    public void insertItem(Integer userId, CartInsertRequest request)
    {

        String sql = "INSERT INTO billing.cart (user_id, movie_id, quantity) " +
                "VALUES (:user_id, :movie_id, :quantity); ";

        this.template.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("user_id", userId, Types.INTEGER)
                        .addValue("movie_id", request.getMovieId(), Types.INTEGER)
                        .addValue("quantity", request.getQuantity(), Types.INTEGER)
        );
    }

    public int updateItem(Integer userId, CartUpdateRequest request)
    {
        String sql = "UPDATE billing.cart " +
                    "SET quantity = :quantity " +
                    "WHERE movie_id = :movie_id AND user_id =:user_id";

        return
            this.template.update(
                    sql,
                    new MapSqlParameterSource()
                            .addValue("user_id", userId, Types.INTEGER)
                            .addValue("movie_id", request.getMovieId(), Types.INTEGER)
                            .addValue("quantity", request.getQuantity(), Types.INTEGER)
            );
    }

    public int deleteItem(Integer userId, String movieId)
    {
        String sql = "DELETE FROM billing.cart " +
                    "WHERE movie_id = :movie_id AND user_id = :user_id";

        return
            this.template.update(
                    sql,
                    new MapSqlParameterSource()
                            .addValue("user_id", userId, Types.INTEGER)
                            .addValue("movie_id", Integer.valueOf(movieId), Types.INTEGER)
            );
    }

    public Item[] retrieveItem(Integer userId, List<String> roles)
    {
        //unitPrice, quantity, movieId, movieTitle, backdropPath, posterPath
        String sql = "SELECT mp.unit_price, c.quantity, c.movie_id, m.title, m.backdrop_path, m.poster_path, mp.premium_discount " +
                    "FROM billing.cart c " +
                    "JOIN billing.movie_price mp ON c.movie_id = mp.movie_id " +
                    "JOIN movies.movie m ON c.movie_id = m.id " +
                    "WHERE c.user_id = :user_id";

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("user_id", userId, Types.INTEGER);

        List<Item> items =
                this.template.query(
                        sql,
                        source,

                        (rs, rowNum) ->
                                new Item()
                                        .setUnitPrice(roles.contains("PREMIUM") ? rs.getBigDecimal("unit_price").multiply(BigDecimal.valueOf(1 - rs.getInt("premium_discount")/100.0)).setScale(2, BigDecimal.ROUND_DOWN) : rs.getBigDecimal("unit_price").setScale(2))
                                        .setQuantity(rs.getInt("quantity"))
                                        .setMovieId(rs.getLong("movie_id"))
                                        .setMovieTitle(rs.getString("title"))
                                        .setBackdropPath(rs.getString("backdrop_path"))
                                        .setPosterPath(rs.getString("poster_path"))
                );

        return items.toArray(new Item[0]);
    }

    public int clearItem(Integer userId)
    {
        String sql = "DELETE FROM billing.cart " +
                "WHERE user_id = :user_id";

        return
                this.template.update(
                        sql,
                        new MapSqlParameterSource()
                                .addValue("user_id", userId, Types.INTEGER)
                );
    }

    public void completeOrder(Integer userId, BigDecimal amount)
    {
        String sql = "INSERT INTO billing.sale(user_id, total, order_date) " +
                "VALUES (:user_id, :total, :order_date); ";

        this.template.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("user_id", userId, Types.INTEGER)
                        .addValue("total", amount, Types.DECIMAL)
                        .addValue("order_date", Timestamp.from(Instant.now()), Types.TIMESTAMP)
        );
    }

    public int findId(Integer userId)
    {
        String sql = "SELECT MAX(id) " +
                    "FROM Billing.sale " +
                    "WHERE user_id = :user_id; ";

        return
                this.template.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("user_id", userId, Types.INTEGER),
                        (rs, rowNum) -> rs.getInt(1)
        );
    }

    public void populateItem(Integer sale_id, Integer userId)
    {
        String sql = "Insert INTO billing.sale_item(sale_id, movie_id, quantity) " +
                    "SELECT :sale_id AS sale_id, movie_id, quantity " +
                    "FROM billing.cart " +
                    "WHERE user_id = :user_id; ";

        this.template.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("sale_id", sale_id, Types.INTEGER)
                        .addValue("user_id", userId, Types.INTEGER)
        );
    }

    public Sale[] listOrder(Integer userId, List<String> roles)
    {
        //saleId, total, orderDate
        String sql = "SELECT s.id, s.total, s.order_date " +
                "FROM billing.sale s " +
                "WHERE s.user_id = :user_id " +
                "ORDER BY s.order_date DESC " +
                "LIMIT 5; ";

        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("user_id", userId, Types.INTEGER);

        List<Sale> sales =
                this.template.query(
                        sql,
                        source,
                        (rs, rowNum) ->
                                new Sale()
                                        .setSaleId(rs.getLong("id"))
                                        .setTotal(rs.getBigDecimal("total"))
                                        .setOrderDate(rs.getTimestamp("order_date").toInstant())
                );

        return sales.toArray(new Sale[0]);
    }

    public Item[] retrieveDetail(Long saleId, Integer userId, List<String> roles)
    {
        //unitPrice, quantity, movieId, movieTitle, backdropPath, posterPath
        String sql = "SELECT mp.unit_price, si.quantity, si.movie_id, m.title, m.backdrop_path, m.poster_path, mp.premium_discount " +
                    "FROM billing.sale_item si " +
                    "JOIN billing.movie_price mp ON si.movie_id = mp.movie_id " +
                    "JOIN movies.movie m ON si.movie_id = m.id " +
                    "JOIN billing.sale s ON si.sale_id = s.id " +
                    "WHERE si.sale_id = :sale_id AND s.user_id = :user_id";


        MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue("sale_id", saleId, Types.INTEGER);
        source.addValue("user_id", userId, Types.INTEGER);

        List<Item> items =
                this.template.query(
                        sql,
                        source,

                        (rs, rowNum) ->
                                new Item()
                                        .setUnitPrice(roles.contains("PREMIUM") ? rs.getBigDecimal("unit_price").multiply(BigDecimal.valueOf(1 - rs.getInt("premium_discount")/100.0)).setScale(2, BigDecimal.ROUND_DOWN) : rs.getBigDecimal("unit_price").setScale(2))
                                        .setQuantity(rs.getInt("quantity"))
                                        .setMovieId(rs.getLong("movie_id"))
                                        .setMovieTitle(rs.getString("title"))
                                        .setBackdropPath(rs.getString("backdrop_path"))
                                        .setPosterPath(rs.getString("poster_path"))
                );

        return items.toArray(new Item[0]);
    }

}
