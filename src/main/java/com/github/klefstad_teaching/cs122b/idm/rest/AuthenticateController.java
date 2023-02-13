package com.github.klefstad_teaching.cs122b.idm.rest;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.component.IDMAuthenticationManager;
import com.github.klefstad_teaching.cs122b.idm.component.IDMJwtManager;
import com.github.klefstad_teaching.cs122b.idm.model.request.AuthenticateRequest;
import com.github.klefstad_teaching.cs122b.idm.model.request.LoginRequest;
import com.github.klefstad_teaching.cs122b.idm.model.response.AuthenticateResponse;
import com.github.klefstad_teaching.cs122b.idm.model.response.LoginResponse;
import com.github.klefstad_teaching.cs122b.idm.util.Validate;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
public class AuthenticateController
{
    private final IDMAuthenticationManager authManager;
    private final IDMJwtManager            jwtManager;
    private final Validate                 validate;

    @Autowired
    public AuthenticateController(IDMAuthenticationManager authManager,
                         IDMJwtManager jwtManager,
                         Validate validate)
    {
        this.authManager = authManager;
        this.jwtManager = jwtManager;
        this.validate = validate;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) throws BadJOSEException, ParseException, JOSEException {
        AuthenticateResponse result = new AuthenticateResponse();
        jwtManager.verifyAccessToken(request.getAccessToken());
        result.setResult(IDMResults.ACCESS_TOKEN_IS_VALID);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}



