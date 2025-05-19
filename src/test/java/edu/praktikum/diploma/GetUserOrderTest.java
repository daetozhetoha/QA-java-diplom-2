package edu.praktikum.diploma;

import edu.praktikum.diploma.base.Url;
import edu.praktikum.diploma.clients.IngredientsClient;
import edu.praktikum.diploma.clients.OrderClient;
import edu.praktikum.diploma.clients.UserClient;
import edu.praktikum.diploma.models.Order;
import edu.praktikum.diploma.models.User;
import edu.praktikum.diploma.models.UserCreds;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static edu.praktikum.diploma.generators.UserGenerator.randomUser;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isA;

public class GetUserOrderTest extends Url {
    private UserClient userClient;
    private User user;
    private OrderClient orderClient;
    private IngredientsClient ingredientsClient;
    private String accessToken;
    private String bunId;
    private String fillingId;
    private String sauceId;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        userClient = new UserClient();
        user = randomUser();
        userClient.create(user);
        orderClient = new OrderClient();
        ingredientsClient = new IngredientsClient();
        userLogin();
        bunId = getIngredientIdByType("bun");
        fillingId = getIngredientIdByType("main");
        sauceId = getIngredientIdByType("sauce");
        createOrderWithSeveralIngredientsWithAuth(bunId, fillingId, sauceId);
        createOrderWithSeveralIngredientsWithAuth(bunId, fillingId, sauceId);
        createOrderWithSeveralIngredientsWithAuth(bunId, fillingId, sauceId);
    }

    @Test
    @DisplayName("Check getting list of orders for authorized user")
    @Description("Checking status code and body response structure for authorized user")
    public void getUsersOrdersWithAuthReturnsListOfOrders() {
        Response response = sendGetRequestOrdersWithAuth();
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        checkThatOrdersParameterIsArray(response);
        checkThatOrdersSizeMoreThan2(response);
        checkThatIdInFirstOrderInfoNotNull(response);
        checkThatIdInSecondOrderInfoNotNull(response);
        checkThatIdInThirdOrderInfoNotNull(response);
    }

    @Test
    @DisplayName("Check getting list of orders for unauthorized user")
    @Description("Checking status code and body response for unauthorized user")
    public void getUsersOrdersWithoutAuthReturnsError401() {
        Response response = sendGetRequestOrdersWithoutAuth();
        compareStatus401WithResponse(response);
        compareParameterMessageFor401WithResponse(response);
    }

    @Step("Get accessToken")
    public void userLogin() {
        Response loginResponse = userClient.login(UserCreds.credsFromUser(user));
        accessToken = loginResponse.jsonPath().getString("accessToken");
    }

    @Step("Get ingredient id for exact type")
    public String getIngredientIdByType(String type) {
        Response response = ingredientsClient.get();
        List<String> ids = response.jsonPath().getList("data.findAll { it.type == '" + type + "'}._id");
        return ids.get(0);
    }

    @Step("Create order with several ingredients: bun={bun}, main={main}, sauce={sauce} for authorized user")
    public Response createOrderWithSeveralIngredientsWithAuth(String bun, String main, String sauce) {
        Order order = new Order()
                .setIngredients(new String[]{bun, main, sauce});
        return orderClient.createWithAuth(order, accessToken);
    }

    @Step("Compare status code 200 with response")
    public void compareStatus200WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_OK);
    }

    @Step("Compare body parameter 'success' for 200 status with response")
    public void compareParameterSuccessFor200WithResponse(Response response) {
        response.then().assertThat().body("success", equalTo(true));
    }

    @Step("Check that 'orders' is an array")
    public void checkThatOrdersParameterIsArray(Response response) {
        response.then().assertThat().body("orders", isA(List.class));
    }

    @Step("Check that 'orders' size > 2")
    public void checkThatOrdersSizeMoreThan2(Response response) {
        response.then().assertThat().body("orders.size()", greaterThan(2));
    }

    @Step("Check that first id in orders list not null")
    public void checkThatIdInFirstOrderInfoNotNull(Response response) {
        response.then().assertThat().body("orders._id[0]", notNullValue());
    }
    @Step("Check that second id in orders list not null")
    public void checkThatIdInSecondOrderInfoNotNull(Response response) {
        response.then().assertThat().body("orders._id[1]", notNullValue());
    }

    @Step("Check that third id in orders list not null")
    public void checkThatIdInThirdOrderInfoNotNull(Response response) {
        response.then().assertThat().body("orders._id[2]", notNullValue());
    }

    @Step("Send GET request to api/orders with auth")
    public Response sendGetRequestOrdersWithAuth() {
        Response response = orderClient.getWithAuth(accessToken);
        return response;
    }

    @Step("Send GET request to api/orders without auth")
    public Response sendGetRequestOrdersWithoutAuth() {
        Response response = orderClient.getWithoutAuth();
        return response;
    }

    @Step("Compare status code 401 with response")
    public void compareStatus401WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_UNAUTHORIZED);
    }

    @Step("Compare body parameter 'message' for 401 status with response")
    public void compareParameterMessageFor401WithResponse(Response response) {
        response.then().assertThat().body("message", equalTo("You should be authorised"));
    }
}
