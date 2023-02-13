package com.github.klefstad_teaching.cs122b.billing.rest;

import com.github.klefstad_teaching.cs122b.billing.Request.CartInsertRequest;
import com.github.klefstad_teaching.cs122b.billing.Request.OrderCompleteRequest;
import com.github.klefstad_teaching.cs122b.billing.Response.*;
import com.github.klefstad_teaching.cs122b.billing.entity.Item;
import com.github.klefstad_teaching.cs122b.billing.entity.Sale;
import com.github.klefstad_teaching.cs122b.billing.repo.BillingRepo;
import com.github.klefstad_teaching.cs122b.billing.util.Validate;
import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.BillingResults;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.nimbusds.jwt.SignedJWT;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Types;
import java.text.ParseException;
import java.util.List;

@RestController
public class OrderController
{
    private final BillingRepo repo;
    private final Validate    validate;

    @Autowired
    public OrderController(BillingRepo repo,Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @GetMapping("/order/payment")
    public ResponseEntity<OrderPaymentResponse> orderPayment(@AuthenticationPrincipal SignedJWT correctUser) throws StripeException {
        Integer userId;
        List<String> roles;

        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
            roles = correctUser.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        OrderPaymentResponse response = new OrderPaymentResponse();
        Item[] items = repo.retrieveItem(userId, roles);

        if (items.length == 0) {
            throw new ResultError(BillingResults.CART_EMPTY);
        }

        BigDecimal total = new BigDecimal(0);
        String description = "";

        for (int i = 0; i < items.length; i++) {
            total = total.add(items[i].getUnitPrice().multiply(new BigDecimal(items[i].getQuantity())));
            if(i < items.length-1){
                description += items[i].getMovieTitle() + ", ";
            }
            else {
                description += items[i].getMovieTitle();
            }
        }

        // Stripe takes amount in total cents
        Long amountInTotalCents = total.multiply(new BigDecimal(100)).longValue();
        String newUserId = Long.toString(userId);

        PaymentIntentCreateParams paymentIntentCreateParams =
                PaymentIntentCreateParams
                        .builder()
                        .setCurrency("USD") // This will always be the same for our project
                        .setDescription(description)
                        .setAmount(amountInTotalCents)
                        // We use MetaData to keep track of the user that should pay for the order
                        .putMetadata("userId", newUserId)
                        .setAutomaticPaymentMethods(
                                // This will tell stripe to generate the payment methods automatically
                                // This will always be the same for our project
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();

        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
        } catch (StripeException e) {
            throw new ResultError(BillingResults.STRIPE_ERROR);
        }

        String paymentIntentId = paymentIntent.getId();
        String clientSecret = paymentIntent.getClientSecret();

        response.setResult(BillingResults.ORDER_PAYMENT_INTENT_CREATED);
        response.setPaymentIntentId(paymentIntentId);
        response.setClientSecret(clientSecret);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/order/complete")
    public ResponseEntity<OrderCompleteResponse> orderComplete(@AuthenticationPrincipal SignedJWT correctUser, @RequestBody OrderCompleteRequest request)
    {
        Integer userId;
        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        OrderCompleteResponse response = new OrderCompleteResponse();

        PaymentIntent retrievedPaymentIntent;
        try{
            retrievedPaymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
        } catch (StripeException e){
            throw new ResultError(BillingResults.STRIPE_ERROR);
        }

        String status = retrievedPaymentIntent.getStatus();
        if(!status.equals("succeeded")){
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_NOT_SUCCEEDED);
        }

        if(!retrievedPaymentIntent.getMetadata().get("userId").equals(Integer.toString(userId))){
            throw new ResultError(BillingResults.ORDER_CANNOT_COMPLETE_WRONG_USER);
        }

        BigDecimal amount = new BigDecimal(retrievedPaymentIntent.getAmount()).divide(new BigDecimal(100)).setScale(2);
        repo.completeOrder(userId, amount);
        Integer sale_id = repo.findId(userId);
        repo.populateItem(sale_id, userId);
        repo.clearItem(userId);

        response.setResult(BillingResults.ORDER_COMPLETED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/order/list")
    public ResponseEntity<OrderListResponse> orderList(@AuthenticationPrincipal SignedJWT correctUser)
    {
        Integer userId;
        List<String> roles;

        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
            roles = correctUser.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        OrderListResponse response = new OrderListResponse();
        Sale[] sales = repo.listOrder(userId, roles);

        if (sales.length == 0) {
            throw new ResultError(BillingResults.ORDER_LIST_NO_SALES_FOUND);
        }

        response.setResult(BillingResults.ORDER_LIST_FOUND_SALES);
        response.setSales(sales);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/order/detail/{saleId}")
    public ResponseEntity<OrderDetailResponse> orderDetail(@PathVariable Long saleId, @AuthenticationPrincipal SignedJWT correctUser)
    {
        Integer userId;
        List<String> roles;

        try {
            userId = correctUser.getJWTClaimsSet().getIntegerClaim(JWTManager.CLAIM_ID);
            roles = correctUser.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        } catch (ParseException exc) {
            throw new ResultError(IDMResults.ACCESS_TOKEN_IS_INVALID);
        }

        OrderDetailResponse response = new OrderDetailResponse();
        Item[] items = repo.retrieveDetail(saleId, userId, roles);

        if(items.length == 0){
            throw new ResultError(BillingResults.ORDER_DETAIL_NOT_FOUND);
        }

        BigDecimal total = new BigDecimal(0);
        for(int i = 0; i < items.length; i++){
            total = total.add(items[i].getUnitPrice().multiply(new BigDecimal(items[i].getQuantity())));
        }

        response.setResult(BillingResults.ORDER_DETAIL_FOUND);
        response.setTotal(total);
        response.setItems(items);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
