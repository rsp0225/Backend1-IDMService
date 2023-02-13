import Config from "backend/config.json";
import Axios from "axios";


/**
 * We use axios to create REST calls to our backend
 *
 * We have provided the login rest call for your
 * reference to build other rest calls with.
 *
 * This is an async function. Which means calling this function requires that
 * you "chain" it with a .then() function call.
 * <br>
 * What this means is when the function is called it will essentially do it "in
 * another thread" and when the action is done being executed it will do
 * whatever the logic in your ".then()" function you chained to it
 * @example
 * login(request)
 * .then(response => alert(JSON.stringify(response.data, null, 2)));
 */
export async function orderPayment(accessToken) {
    const options = {
        method: "GET", // Method type ("POST", "GET", "DELETE", ect)
        baseURL: Config.billing.baseUrl, // Base URL (localhost:8083 for example)
        url: Config.billing.orderPayment,
        headers: {
            Authorization: "Bearer " + accessToken
        }
    }

    return Axios.request(options);
}