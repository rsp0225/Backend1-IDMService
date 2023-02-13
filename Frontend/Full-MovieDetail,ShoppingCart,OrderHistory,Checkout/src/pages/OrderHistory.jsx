import React, {useEffect} from "react";
import styled from "styled-components";
import {useUser} from "../hook/User";
import {useForm} from "react-hook-form";
import {orderList} from "../backend/orderList";
import {TextInput} from "react-native";
import {retrieveCart} from "../backend/retrieveCart";

const StyledDiv = styled.div` 
`

const StyledH1 = styled.h1`


`
const StyledTd = styled.td`
  border: 1px solid black;
  padding: 10px;
`

const OrderHistory = () => {
    const {
        accessToken, setAccessToken,
        refreshToken, setRefreshToken
    } = useUser();

    const [ orderItems, setOrderItems ] = React.useState([]);


    useEffect(() =>
        orderList(accessToken)
            .then(response => {setOrderItems(response.data.sales)})
            .catch(error => alert(JSON.stringify(error.response.data, null, 2)))
        , []);

    return (
        <StyledDiv>
            <h1>My Order History</h1>

            <table>
                <tr>
                    <th> Date </th>
                    <th> Price </th>
                </tr>

                {orderItems !== undefined &&
                    orderItems.map((orderItem, index) => {
                        return (
                            <tr key={orderItem.saleId}>
                                <StyledTd> {new Date(orderItem.orderDate).toLocaleDateString()} </StyledTd>
                                <StyledTd> {orderItem.total}  </StyledTd>
                            </tr>
                        )
                })}
            </table>
        </StyledDiv>
    );
}

export default OrderHistory;
