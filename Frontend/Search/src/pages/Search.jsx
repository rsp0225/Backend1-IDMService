import React from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import {search} from "backend/search";


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
const Search = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const {register, getValues, setValue, handleSubmit} = useForm();

    const [ movies, setMovies ] = React.useState([]);

    const prevPage = () =>{
        const page = getValues("page");
        if(page !== 1){
            setValue("page", page-1);
            submitLogin();
        }
    }

    const nextPage = () =>{
        const page = parseInt(getValues("page"));
        setValue("page", page+1);
        submitLogin();
    }

    const update = (movies) => {
        setMovies(movies);
    };

    const submitLogin = () => {
        const title = getValues("title");
        const year = getValues("year");
        const director = getValues("director");
        const genre = getValues("genre");
        const limit = getValues("limit");
        const page = getValues("page");
        const orderBy = getValues("orderBy");
        const direction = getValues("direction");

        //title, year, director, genre, limit, page, orderBy, direction
        const payLoad = {};

        if(title !== ""){
            payLoad.title = title;
        }

        if(year !== ""){
            payLoad.year = year;
            //parseInt()
        }

        if(director !== ""){
            payLoad.director = director;
        }

        if(genre !== ""){
            payLoad.genre = genre;
        }

        if(limit !== "10" && limit !== ""){
            payLoad.limit = limit;
        }
        else{
            payLoad.limit = "10";
        }

        if(page !== "" && page !== "1"){
            payLoad.page = page;
        }

        if(orderBy !== "title" && orderBy !== ""){
            payLoad.orderBy = orderBy;
        }

        if(direction !== "ASC" && direction !== ""){
            payLoad.direction = direction;
        }
        
        search(payLoad, accessToken)
            .then(response => update(response.data.movies))
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
    }

    return (
        <StyledDiv>
            <h1>Search</h1>
            <input placeholder="Title" {...register("title")}/>
            <input placeholder= "Year"{...register("year")} />
            <input placeholder="Director" {...register("director")} />
            <input placeholder="Genre" {...register("genre")} />

            <select {...register("limit")}>
                <option value="10">10</option>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
            </select>

            <input type="number" min = "1" {...register("page")} />

            <select {...register("OrderBy")}>
                <option value="title">Title</option>
                <option value="year">Year</option>
                <option value="rating">Rating</option>
            </select>


            <select {...register("direction")}>
                <option value="asc">Ascending</option>
                <option value="desc">Descending</option>
            </select>

            <button onClick={handleSubmit(submitLogin)}>Search</button>

            <table>
                <tr>
                    <th> Title </th>
                    <th> Year </th>
                    <th> Director </th>
                </tr>

                {movies.map(movie => {
                    return (
                        <tr key={movie.id}>
                            <StyledTd> {movie.title} </StyledTd>
                            <StyledTd> {movie.year} </StyledTd>
                            <StyledTd> {movie.director} </StyledTd>
                        </tr>
                    )
                })}
            </table>

            <button onClick={prevPage}>Previous Page</button>
            <button onClick={nextPage}>Next Page</button>

        </StyledDiv>
    );
}

export default Search;
