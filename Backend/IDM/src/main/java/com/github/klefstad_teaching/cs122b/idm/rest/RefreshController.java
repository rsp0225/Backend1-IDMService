package com.github.klefstad_teaching.cs122b.idm.rest;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.component.IDMAuthenticationManager;
import com.github.klefstad_teaching.cs122b.idm.component.IDMJwtManager;
import com.github.klefstad_teaching.cs122b.idm.model.request.RefreshRequest;
import com.github.klefstad_teaching.cs122b.idm.model.response.RefreshResponse;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.github.klefstad_teaching.cs122b.idm.util.Validate;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
public class RefreshController
{
    private final IDMAuthenticationManager authManager;
    private final IDMJwtManager            jwtManager;
    private final Validate                 validate;

    @Autowired
    public RefreshController(IDMAuthenticationManager authManager,
                         IDMJwtManager jwtManager,
                         Validate validate)
    {
        this.authManager = authManager;
        this.jwtManager = jwtManager;
        this.validate = validate;
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) throws JOSEException {
        String givenRefreshToken = request.getRefreshToken();

        if(givenRefreshToken.length() != 36){
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_LENGTH);
        }

        try{
            UUID uuid = UUID.fromString(givenRefreshToken);
        }
        catch (IllegalArgumentException e){
            throw new ResultError(IDMResults.REFRESH_TOKEN_HAS_INVALID_FORMAT);
        }

        RefreshToken refreshToken = authManager.verifyRefreshToken(givenRefreshToken);

        if(refreshToken.getTokenStatus().equals(TokenStatus.EXPIRED)){
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_EXPIRED);
        }

        if(refreshToken.getTokenStatus().equals(TokenStatus.REVOKED)){
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_REVOKED);
        }

        if(refreshToken.getExpireTime().isBefore(Instant.now()) ||
                refreshToken.getMaxLifeTime().isBefore(Instant.now())){
            authManager.expireRefreshToken(refreshToken);
            throw new ResultError(IDMResults.REFRESH_TOKEN_IS_EXPIRED);
        }

        jwtManager.updateRefreshTokenExpireTime(refreshToken);

        if(refreshToken.getExpireTime().isAfter(refreshToken.getMaxLifeTime())){
            authManager.revokeRefreshToken(refreshToken);
            User user = authManager.getUserFromRefreshToken(refreshToken);
            String accessToken = jwtManager.buildAccessToken(user);
            RefreshToken new_refreshToken = jwtManager.buildRefreshToken(user);
            authManager.insertRefreshToken(new_refreshToken);

            RefreshResponse result = new RefreshResponse();
            result.setResult(IDMResults.RENEWED_FROM_REFRESH_TOKEN);
            result.setRefreshToken(new_refreshToken.getToken());
            result.setAccessToken(accessToken);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(result);
        }

        else{
            authManager.updateRefreshTokenExpireTime(refreshToken);
            User user = authManager.getUserFromRefreshToken(refreshToken);
            String accessToken = jwtManager.buildAccessToken(user);

            RefreshResponse result = new RefreshResponse();
            result.setResult(IDMResults.RENEWED_FROM_REFRESH_TOKEN);
            result.setRefreshToken(refreshToken.getToken());
            result.setAccessToken(accessToken);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(result);
        }
    }
}


