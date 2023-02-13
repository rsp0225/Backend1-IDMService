import React, {useEffect, useState} from "react";
import styled from "styled-components";
import {orderPayment} from "../backend/orderPayment";
import {orderComplete} from "../backend/orderComplete";
import {useSearchParams} from "react-router-dom";
import {useUser} from "../hook/User";

const StyledDiv = styled.div` 
`

const StyledH1 = styled.h1`
`

const CompletePage = () => {
    const [message, setMessage] = useState("");
    const [searchParams, setSearchParams] = useSearchParams();
    const {
                accessToken, setAccessToken,
                refreshToken, setRefreshToken
            } = useUser();
    const paymentIntent = searchParams.get("payment_intent");

    useEffect(() => {
        orderComplete(paymentIntent, accessToken)
            .then(response => setMessage(response.data.result.message));
    }, []);

    return (
        <div>
            <h3>Order completed</h3>
        </div>
    );
}

export default CompletePage;
