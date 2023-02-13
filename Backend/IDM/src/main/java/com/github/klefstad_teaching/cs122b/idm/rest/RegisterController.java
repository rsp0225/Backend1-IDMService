package com.github.klefstad_teaching.cs122b.idm.rest;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BasicResults;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.idm.component.IDMAuthenticationManager;
import com.github.klefstad_teaching.cs122b.idm.component.IDMJwtManager;
import com.github.klefstad_teaching.cs122b.idm.model.request.RegisterRequest;
import com.github.klefstad_teaching.cs122b.idm.model.response.RegisterResponse;
import com.github.klefstad_teaching.cs122b.idm.util.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController
{
    private final IDMAuthenticationManager authManager;
    private final IDMJwtManager            jwtManager;
    private final Validate                 validate;

    @Autowired
    public RegisterController(IDMAuthenticationManager authManager,
                              IDMJwtManager jwtManager,
                              Validate validate)
    {
        this.authManager = authManager;
        this.jwtManager = jwtManager;
        this.validate = validate;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request)
    {
        if(request.getEmail().length() < 6 || request.getEmail().length() > 32){
            throw new ResultError(IDMResults.EMAIL_ADDRESS_HAS_INVALID_LENGTH);
        }

        else if(request.getPassword().length < 10 || request.getPassword().length > 20){
            throw new ResultError(IDMResults.PASSWORD_DOES_NOT_MEET_LENGTH_REQUIREMENTS);
        }

        else if(! request.getEmail().matches("^([a-zA-Z0-9]+)@([a-zA-Z0-9]+)\\.([a-zA-Z0-9]+)$")){
            throw new ResultError(IDMResults.EMAIL_ADDRESS_HAS_INVALID_FORMAT);
        }

        else if(!(new String(request.getPassword()).matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{10,20}$"))){
            throw new ResultError((IDMResults.PASSWORD_DOES_NOT_MEET_CHARACTER_REQUIREMENT));
        }

        try{
            authManager.createAndInsertUser(request.getEmail(), request.getPassword());
        }
        catch(DuplicateKeyException k){
            throw new ResultError(IDMResults.USER_ALREADY_EXISTS);
        }

        RegisterResponse result = new RegisterResponse();
        result.setResult(IDMResults.USER_REGISTERED_SUCCESSFULLY);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
