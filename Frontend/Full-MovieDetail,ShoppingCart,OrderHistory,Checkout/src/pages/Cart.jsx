import React, {useEffect} from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import {search} from "backend/search";
import {Link, useNavigate} from "react-router-dom";
import {login} from "../backend/idm";
import {insertCart} from "../backend/insertCart";
import {retrieveCart} from "../backend/retrieveCart";
import {getMovie} from "../backend/getMovie";
import {deleteCart} from "../backend/deleteCart";
import {updateCart} from "../backend/updateCart";
import { Text, TextInput, View } from 'react-native';


const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`
const StyledTd = styled.td`
  border: 1px solid black;
  padding: 10px;
`

const StyledH1 = styled.h1`
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
`
/**
 * useUser():
 * <br>
 * This is a hook we will use to keep track of our accessToken and
 * refreshToken given to use when the user calls "login".
 * <br>
 * For now, it is not being used, but we recommend setting the two tokens
 * here to the tokens you get when the user completes the login call (once
 * you are in the .then() function after calling login)
 * <br>
 * These have logic inside them to make sure the accessToken and
 * refreshToken are saved into the local storage of the web browser
 * allowing you to keep values alive even when the user leaves the website
 * <br>
 * <br>
 * useForm()
 * <br>
 * This is a library that helps us with gathering input values from our
 * users.
 * <br>
 * Whenever we make a html component that takes a value (<input>, <select>,
 * ect) we call this function in this way:
 * <pre>
 *     {...register("email")}
 * </pre>
 * Notice that we have "{}" with a function call that has "..." before it.
 * This is just a way to take all the stuff that is returned by register
 * and <i>distribute</i> it as attributes for our components. Do not worry
 * too much about the specifics of it, if you would like you can read up
 * more about it on "react-hook-form"'s documentation:
 * <br>
 * <a href="https://react-hook-form.com/">React Hook Form</a>.
 * <br>
 * Their documentation is very detailed and goes into all of these functions
 * with great examples. But to keep things simple: Whenever we have a html with
 * input we will use that function with the name associated with that input,
 * and when we want to get the value in that input we call:
 * <pre>
 * getValue("email")
 * </pre>
 * <br>
 * To Execute some function when the user asks we use:
 * <pre>
 *     handleSubmit(ourFunctionToExecute)
 * </pre>
 * This wraps our function and does some "pre-checks" before (This is useful if
 * you want to do some input validation, more of that in their documentation)
 */
const Cart = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const {register, getValues, setValue, handleSubmit} = useForm();
    const [ cartItems, setCartItems ] = React.useState([]);
    const [ total, setTotal ] = React.useState("");

    const deleteMovie = (movieId) => {
        const payLoad = {
            movieId: movieId,
        }

        deleteCart(payLoad, accessToken)
            .then(() =>
                {
                    retrieveCart(accessToken)
                        .then(response => {setCartItems(response.data.items); setTotal(response.data.total)})
                })
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }

    const navigate = useNavigate();
    const checkOut = () => {
        navigate("/checkOut")
    }

    const cartUpdate = (movieId, quantity) => {
        //const quantity = getValues("quantity" + index);
        if(quantity == "")
            return;

        const payLoad = {
            movieId: movieId,
            quantity: quantity
        }

        updateCart(payLoad, accessToken)
            .then(() => retrieveCart(accessToken)
                                .then(response => {setCartItems(response.data.items); setTotal(response.data.total)}))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }


    useEffect(() =>
            retrieveCart(accessToken)
                .then(response => {setCartItems(response.data.items); setTotal(response.data.total)})
        , []);

    return (cartItems !== undefined &&
        <StyledDiv>
            <h1>My Cart</h1>

            <table>
                <tr>
                    <th> Movie </th>
                    <th> Quantity </th>
                    <th> Unit Price </th>
                    <th> Total Price </th>
                </tr>

                {cartItems.map((cartItem, index) => {
                        return (
                            <tr key={cartItem.id}>
                                <StyledTd> {cartItem.movieTitle} </StyledTd>
                                <StyledTd> <TextInput onChangeText={quantity => cartUpdate(cartItem.movieId, quantity)}  defaultValue={cartItem.quantity} /> </StyledTd>
                                <StyledTd> ${(cartItem.unitPrice).toFixed(2)} </StyledTd>
                                <StyledTd> ${(cartItem.quantity * cartItem.unitPrice).toFixed(2)} </StyledTd>
                                <StyledTd> <button onClick={() => deleteMovie(cartItem.movieId)}>Delete</button> </StyledTd>
                            </tr>
                        )
                    })}
            </table>

            <h3>Total Cart Price: ${total !== "" ? total.toFixed(2) : total}</h3>
            <button onClick={checkOut}>Checkout</button>
        </StyledDiv>
    );
}

export default Cart;
