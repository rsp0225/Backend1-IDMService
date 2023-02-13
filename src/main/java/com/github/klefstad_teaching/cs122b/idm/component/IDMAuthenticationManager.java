package com.github.klefstad_teaching.cs122b.idm.component;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.repo.IDMRepo;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.UserStatus;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Base64;
import java.util.List;

@Component
public class IDMAuthenticationManager
{
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String       HASH_FUNCTION = "PBKDF2WithHmacSHA512";

    private static final int ITERATIONS     = 10000;
    private static final int KEY_BIT_LENGTH = 512;

    private static final int SALT_BYTE_LENGTH = 4;

    public final IDMRepo repo;

    @Autowired
    public IDMAuthenticationManager(IDMRepo repo)
    {
        this.repo = repo;
    }

    private static byte[] hashPassword(final char[] password, String salt)
    {
        return hashPassword(password, Base64.getDecoder().decode(salt));
    }

    private static byte[] hashPassword(final char[] password, final byte[] salt)
    {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_FUNCTION);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BIT_LENGTH);

            SecretKey key = skf.generateSecret(spec);

            return key.getEncoded();

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] genSalt()
    {
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    public User selectAndAuthenticateUser(String email, char[] password)
    {
        //A3's Select Queries
        String sql =
                "SELECT id, email, user_status_id, salt, hashed_password " +
                "FROM idm.user " +
                "WHERE email = :email;";

        MapSqlParameterSource source =
                new MapSqlParameterSource() //For ever ':var' we list a value and `Type` for value
                        .addValue("email", email, Types.VARCHAR); // Notice the lack of ':'  in the string here

        List<User> users =
                this.repo.getTemplate().query(
                        sql,
                        source,
                        // For every row this lambda will be called to turn it into a Object (in this case `User`)
                        (rs, rowNum) ->
                                new User()
                                        .setId(rs.getInt("id"))
                                        .setEmail(rs.getString("email"))
                                        .setUserStatus(UserStatus.fromId(rs.getInt("user_status_id")))
                                        .setSalt(rs.getString("salt"))
                                        .setHashedPassword(rs.getString("hashed_password"))
                );

        if(users.size() == 0){
            //return null;
            throw new ResultError(IDMResults.USER_NOT_FOUND);
        }
        else {
            byte[] encodedPasswordUser = hashPassword(password, users.get(0).getSalt());
            String encodedPasswordDB = users.get(0).getHashedPassword();
            String base64EncodedCorrectPass = Base64.getEncoder().encodeToString(encodedPasswordUser);

            if(base64EncodedCorrectPass.equals(encodedPasswordDB) == true){
                return users.get(0);
            }
            else{
                throw new ResultError(IDMResults.INVALID_CREDENTIALS);
            }
        }
    }

    public void createAndInsertUser(String email, char[] password)
    {
        byte[] salt = genSalt();
        byte[] hashedPassword = hashPassword(password, salt);
        String base64EncodedHashedPassword = Base64.getEncoder().encodeToString(hashedPassword);
        String base64EncodedHashedSalt = Base64.getEncoder().encodeToString(salt);

        String sql = "INSERT INTO idm.user (email, user_status_id, salt, hashed_password)" +
                "VALUES (:email, :user_status_id, :salt, :hashed_password);";

        repo.getTemplate().update(
                sql,
                new MapSqlParameterSource()
                        .addValue("email", email, Types.VARCHAR)
                        .addValue("user_status_id", UserStatus.ACTIVE.id(), Types.INTEGER)
                        .addValue("salt", base64EncodedHashedSalt, Types.NCHAR)
                        .addValue("hashed_password", base64EncodedHashedPassword, Types.NCHAR)
        );
    }

    public void insertRefreshToken(RefreshToken refreshToken)
    {
        String sql = "INSERT INTO idm.refresh_token (token, user_id, token_status_id, expire_time, max_life_time)" +
                "VALUES (:token, :user_id, :token_status_id, :expire_time, :max_life_time);";

        repo.getTemplate().update(
                sql,
                new MapSqlParameterSource()
                        .addValue("token", refreshToken.getToken(), Types.NCHAR)
                        .addValue("user_id", refreshToken.getUserId(), Types.INTEGER)
                        .addValue("token_status_id", refreshToken.getTokenStatus().id(), Types.INTEGER)
                        .addValue("expire_time", Timestamp.from(refreshToken.getExpireTime()), Types.TIMESTAMP)
                        .addValue("max_life_time", Timestamp.from(refreshToken.getMaxLifeTime()), Types.TIMESTAMP)
        );
    }

    public RefreshToken verifyRefreshToken(String token)
    {
        String sql =
                "SELECT id, token, user_id, token_status_id, expire_time, max_life_time " +
                "FROM idm.refresh_token " +
                "WHERE token = :token;";

        MapSqlParameterSource source =
                new MapSqlParameterSource()
                        .addValue("token", token, Types.NCHAR);

        List<RefreshToken> refreshTokens =
                this.repo.getTemplate().query(
                        sql,
                        source,
                        // For every row this lambda will be called to turn it into a Object (in this case `Student`)
                        (rs, rowNum) ->
                                new RefreshToken()
                                        .setId(rs.getInt("id"))
                                        .setToken(rs.getString("token"))
                                        .setUserId(rs.getInt("user_id"))
                                        .setTokenStatus(TokenStatus.fromId(rs.getInt("token_status_id")))
                                        .setExpireTime(rs.getTimestamp("expire_time").toInstant())
                                        .setMaxLifeTime(rs.getTimestamp("max_life_time").toInstant())
                );

        if(refreshTokens.size() == 0){
            throw new ResultError(IDMResults.REFRESH_TOKEN_NOT_FOUND);
        }
        return refreshTokens.get(0);
    }

    public void updateRefreshTokenExpireTime(RefreshToken token)
    {
        String sql = "UPDATE idm.refresh_token " +
                "SET expire_time = :expire_time " +
                "WHERE id = :id";

        repo.getTemplate().update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", token.getId(), Types.INTEGER)
                        .addValue("expire_time", Timestamp.from(token.getExpireTime()), Types.TIMESTAMP)
        );
    }

    public void expireRefreshToken(RefreshToken token)
    {
        String sql = "UPDATE idm.refresh_token " +
                "SET token_status_id = :token_status_id " +
                "WHERE id = :id";

        repo.getTemplate().update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", token.getId(), Types.INTEGER)
                        .addValue("token_status_id", TokenStatus.EXPIRED.id(), Types.INTEGER)
        );
    }

    public void revokeRefreshToken(RefreshToken token)
    {
        String sql = "UPDATE idm.refresh_token " +
                "SET token_status_id = :token_status_id " +
                "WHERE id = :id";

        repo.getTemplate().update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", token.getId(), Types.INTEGER)
                        .addValue("token_status_id", TokenStatus.REVOKED.id(), Types.INTEGER)
        );
    }

    public User getUserFromRefreshToken(RefreshToken refreshToken)
    {
        String sql =
                "SELECT id, email, user_status_id, salt, hashed_password " +
                        "FROM idm.user " +
                        "WHERE id = :id;";

        MapSqlParameterSource source =
                new MapSqlParameterSource() //For ever ':var' we list a value and `Type` for value
                        .addValue("id", refreshToken.getUserId(), Types.INTEGER); // Notice the lack of ':'  in the string here

        List<User> users =
                this.repo.getTemplate().query(
                        sql,
                        source,
                        // For every row this lambda will be called to turn it into a Object (in this case `User`)
                        (rs, rowNum) ->
                                new User()
                                        .setId(rs.getInt("id"))
                                        .setEmail(rs.getString("email"))
                                        .setUserStatus(UserStatus.fromId(rs.getInt("user_status_id")))
                                        .setSalt(rs.getString("salt"))
                                        .setHashedPassword(rs.getString("hashed_password"))
                );
        return users.get(0);
    }
}
