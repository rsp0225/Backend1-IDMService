package com.github.klefstad_teaching.cs122b.gateway.repo;

import com.github.klefstad_teaching.cs122b.gateway.Request.GatewayRequestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Component
public class GatewayRepo
{
    private NamedParameterJdbcTemplate template;

    @Autowired
    public GatewayRepo(NamedParameterJdbcTemplate template)
    {
        this.template= template;
    }

    public Mono<int[]> insertRequests(List<GatewayRequestObject> requests)
    {
        //Activity 6's stream example
        MapSqlParameterSource[] arrayOfSources = requests.stream()
                .map(
                        request -> new MapSqlParameterSource()
                                .addValue("ip_address", request.getIp_address(), Types.VARCHAR)
                                .addValue("call_time", request.getCall_time(), Types.TIMESTAMP)
                                .addValue("path", request.getPath(), Types.VARCHAR)
                )
                .toArray(MapSqlParameterSource[]::new);

        return Mono.fromCallable(() -> this.template.batchUpdate(
                "INSERT INTO gateway.request (ip_address, call_time, path) " +
                        "VALUES (:ip_address, :call_time, :path); ",
                arrayOfSources
        ));
    }
}
