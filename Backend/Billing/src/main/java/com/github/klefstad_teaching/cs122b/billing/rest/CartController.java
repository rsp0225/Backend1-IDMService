package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.Request.CartInsertRequest;
import com.github.klefstad_teaching.cs122b.billing.Request.CartUpdateRequest;
import com.github.klefstad_teaching.cs122b.billing.Response.*;
import com.github.klefstad_teaching.cs122b.billing.entity.Item;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BasicResults;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

@RestController
public class CartController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public CartController(BillingRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @PostMapping("/cart/insert")
    public ResponseEntity<CartInsertResponse> cartInsert(@AuthenticationPrincipal SignedJWT correctUser, @RequestBody CartInsertRequest request)
    {
        Integer userId;
        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        CartInsertResponse response = new CartInsertResponse();

        if(request.getQuantity() == 0 || request.getQuantity() < 0){
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        }

        if(request.getQuantity() > 10){
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }

        try{
            repo.insertItem(userId, request);
        }
        catch (DuplicateKeyException e){
            throw new ResultError(BillingResults.CART_ITEM_EXISTS);
        }

        response.setResult(BillingResults.CART_ITEM_INSERTED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cart/update")
    public ResponseEntity<CartUpdateResponse> cartUpdate(@AuthenticationPrincipal SignedJWT correctUser, @RequestBody CartUpdateRequest request)
    {
        Integer userId;
        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        CartUpdateResponse response = new CartUpdateResponse();

        if(request.getQuantity() == 0 || request.getQuantity() < 0){
            throw new ResultError(BillingResults.INVALID_QUANTITY);
        }

        if(request.getQuantity() > 10){
            throw new ResultError(BillingResults.MAX_QUANTITY);
        }


        if(repo.updateItem(userId, request) == 0){
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }

        response.setResult(BillingResults.CART_ITEM_UPDATED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/cart/delete/{movieId}")
    public ResponseEntity<CartDeleteResponse> cartDelete(@AuthenticationPrincipal SignedJWT correctUser, @PathVariable String movieId)
    {
        Integer userId;
        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        CartDeleteResponse response = new CartDeleteResponse();

        if(repo.deleteItem(userId, movieId) == 0){
            throw new ResultError(BillingResults.CART_ITEM_DOES_NOT_EXIST);
        }

        response.setResult(BillingResults.CART_ITEM_DELETED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/cart/retrieve")
    public ResponseEntity<CartRetrieveResponse> cartRetrieve(@AuthenticationPrincipal SignedJWT correctUser)
    {
        Integer userId;
        List<String> roles;

        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
            roles = correctUser.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        CartRetrieveResponse response = new CartRetrieveResponse();
        Item[] items = repo.retrieveItem(userId, roles);

        if(items.length == 0){
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        BigDecimal total = new BigDecimal(0);
        for(int i = 0; i < items.length; i++){
            total = total.add(items[i].getUnitPrice().multiply(new BigDecimal(items[i].getQuantity())));
        }

        response.setResult(BillingResults.CART_RETRIEVED);
        response.setTotal(total);
        response.setItems(items);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cart/clear")
    public ResponseEntity<CartClearResponse> cartClear(@AuthenticationPrincipal SignedJWT correctUser)
    {
        Integer userId;
        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        CartClearResponse response = new CartClearResponse();
        if(repo.clearItem(userId) == 0){
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        response.setResult(BillingResults.CART_CLEARED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
