import React, {useEffect} from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import {search} from "backend/search";
import {useNavigate, useParams} from "react-router-dom";
import {getMovie} from "../backend/getMovie";
import {login} from "../backend/idm";
import {insertCart} from "../backend/insertCart";


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
const MovieDetail = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const [ movie, setMovie ] = React.useState("");
    const {register, getValues, handleSubmit} = useForm();
    const navigate = useNavigate();

    const addCart = () => {
        const quantity = getValues("quantity");

        const payLoad = {
            movieId: movieId,
            quantity: quantity,
        }

        insertCart(payLoad, accessToken)
            .then(response => console.log("Cart Insert"))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }

    let {movieId} = useParams();

    useEffect(() =>
            getMovie(movieId, accessToken)
                .then(response => setMovie(response.data))
        , []);

    return (
        <StyledDiv>
            {movie !== "" &&
                <StyledDiv>
                    <h1>{movie.movie.title} ({movie.movie.year})</h1>
                    <img style = {{height: "500px", objectFit: "contain"}} src = {"https://image.tmdb.org/t/p/original" + movie.movie.posterPath}/>

                    <h2>MOVIE INFO</h2>
                    <div>{movie.movie.overview}</div>
                    <div>Director: {movie.movie.director}</div>
                    <div>Rating: {movie.movie.rating}/10</div>

                    <div>Genre: {movie.genres.map(genre => {
                        return (
                            genre.name
                        )
                    }).join(", ")}</div>

                    <div>Cast: {movie.persons.map(person => {
                        return (
                            person.name
                        )
                    }).join(", ")}</div>

                    <div># of Vote: {movie.movie.numVotes}</div>
                    <div>Budget: ${movie.movie.budget}</div>
                    <div>Revenue: ${movie.movie.revenue}</div>

                    <select {...register("quantity")} name="quantity">
                        <option value="1">Qty:1</option>
                        <option value="2">Qty:2</option>
                        <option value="3">Qty:3</option>
                        <option value="4">Qty:4</option>
                        <option value="5">Qty:5</option>
                        <option value="6">Qty:6</option>
                        <option value="7">Qty:7</option>
                        <option value="8">Qty:8</option>
                        <option value="9">Qty:9</option>
                        <option value="10">Qty:10</option>
                    </select>

                    <button onClick={addCart}>Add to Cart</button>

                </StyledDiv>}
        </StyledDiv>
    );
}

export default MovieDetail;
