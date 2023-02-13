import styled from "styled-components";
import { loadStripe } from "@stripe/stripe-js";
import { Elements } from "@stripe/react-stripe-js";

import CheckoutForm from "./CheckoutForm";
import "./CheckoutPageApp.css";
import {orderPayment} from "../backend/orderPayment";
import {useUser} from "../hook/User";
import React, { useState, useEffect } from "react";


const StyledDiv = styled.div` 
`
const StyledH1 = styled.h1`
`
// Make sure to call loadStripe outside of a component’s render to avoid
// recreating the Stripe object on every render.
// This is your test publishable API key.
const stripePromise = loadStripe("pk_test_51KxkKxGQBzsoDFTV2QC9Eg1ievHwva23P1sixvHjI2iY1mdTOBnxaLZcH1vZf51QIXekmj8KvvuvfioMpz8bQHcD00WHtNCqYM");

export default function CheckoutPage() {
    const [clientSecret, setClientSecret] = useState("");
    const {accessToken} = useUser();

    useEffect(() => {
        orderPayment(accessToken)
            .then(response => setClientSecret(response.data.clientSecret));
    }, []);

    const appearance = {
        theme: 'stripe',
    };
    const options = {
        clientSecret,
        appearance,
    };

    return (
        <div className="App">
            {clientSecret && (
                <Elements options={options} stripe={stripePromise}>
                    <CheckoutForm />
                </Elements>
            )}
        </div>
    );
}


