package com.github.klefstad_teaching.cs122b.idm.component;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.idm.config.IDMServiceConfig;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.RefreshToken;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.User;
import com.github.klefstad_teaching.cs122b.idm.repo.entity.type.TokenStatus;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.transform.Result;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class IDMJwtManager
{
    private final JWTManager jwtManager;

    @Autowired
    public IDMJwtManager(IDMServiceConfig serviceConfig)
    {
        this.jwtManager =
            new JWTManager.Builder()
                .keyFileName(serviceConfig.keyFileName())
                .accessTokenExpire(serviceConfig.accessTokenExpire())
                .maxRefreshTokenLifeTime(serviceConfig.maxRefreshTokenLifeTime())
                .refreshTokenExpire(serviceConfig.refreshTokenExpire())
                .build();
    }

    private SignedJWT buildAndSignJWT(JWTClaimsSet claimsSet)
        throws JOSEException
    {
        //A2's Creating a JWSHeader and Creating a SignedJWT and signing it code
        JWSHeader header =
                new JWSHeader.Builder(JWTManager.JWS_ALGORITHM)
                        .keyID(jwtManager.getEcKey().getKeyID())
                        .type(JWTManager.JWS_TYPE)
                        .build();

        SignedJWT signedJWT = new SignedJWT(header, claimsSet);
        signedJWT.sign(jwtManager.getSigner());
        return signedJWT;
    }

    private void verifyJWT(SignedJWT jwt)
        throws JOSEException, BadJOSEException
    {
        jwt.verify(jwtManager.getVerifier());
        jwtManager.getJwtProcessor().process(jwt, null);
    }

    public String buildAccessToken(User user) throws JOSEException {
        //A2's Creating a JWTClaimsSet
        JWTClaimsSet claimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getEmail())
                        .expirationTime(Date.from(Instant.now().plus(jwtManager.getAccessTokenExpire())))
                        .claim(JWTManager.CLAIM_ID, user.getId())    // we set claims like values in a map
                        .claim(JWTManager.CLAIM_ROLES, user.getRoles())
                        .issueTime(Date.from(Instant.now()))
                        .build();
        return buildAndSignJWT(claimsSet).serialize();
    }

    public void verifyAccessToken(String jws) throws ParseException, BadJOSEException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(jws);

        // Do logic to check if expired manually
        if(signedJWT.getJWTClaimsSet().getExpirationTime().before(Date.from(Instant.now()))){
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_EXPIRED);
        }

        try {
            verifyJWT(signedJWT);

        } catch (IllegalStateException | JOSEException | BadJOSEException e) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
            // If the verify function throws an error that we know the
            // token can not be trusted and the request should not be continued
        }
    }

    public RefreshToken buildRefreshToken(User user)
    {
        RefreshToken refreshToken =
                new RefreshToken()
                        .setToken(UUID.randomUUID().toString())
                        .setTokenStatus(TokenStatus.ACTIVE)
                        .setUserId(user.getId())
                        .setExpireTime(Instant.now().plus(jwtManager.getRefreshTokenExpire()))
                        .setMaxLifeTime(Instant.now().plus(jwtManager.getMaxRefreshTokenLifeTime()));
        return refreshToken;
    }

    public boolean hasExpired(RefreshToken refreshToken)
    {
        return false;
    }

    public boolean needsRefresh(RefreshToken refreshToken)
    {
        return false;
    }

    public void updateRefreshTokenExpireTime(RefreshToken refreshToken)
    {
        refreshToken.setExpireTime(Instant.now().plus(jwtManager.getRefreshTokenExpire()));
        //check if Java can return reference or be able to set variable
    }

    private UUID generateUUID()
    {
        return UUID.randomUUID();
    }
}
