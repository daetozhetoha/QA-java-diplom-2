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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static edu.praktikum.diploma.generators.UserGenerator.faker;
import static edu.praktikum.diploma.generators.UserGenerator.randomUser;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderCreationTest extends Url {
    private OrderClient orderClient;
    private IngredientsClient ingredientsClient;
    private UserClient userClient;
    private User user;
    private String accessToken;
    private String bunId;
    private String fillingId;
    private String sauceId;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        orderClient = new OrderClient();
        ingredientsClient = new IngredientsClient();
        userClient = new UserClient();
        user = randomUser();
        userClient.create(user);
    }

    @Test
    @DisplayName("Check order creation with only bun for authorized user")
    @Description("Checking status code and body response for order creation with only bun for authorized user")
    public void createOrderWithOnlyBunWithAuthReturnsOk200AndOrderNumber() {
        userLogin();
        Response response = createOrderWithIngredientTypeWithAuth("bun");
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with only bun for unauthorized user")
    @Description("Checking status code and body response for order creation with only bun for unauthorized user")
    public void createOrderWithOnlyBunWithoutAuthReturnsOk200AndOrderNumber() {
        Response response = createOrderWithIngredientTypeWithoutAuth("bun");
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with only filling for authorized user")
    @Description("Checking status code and body response for order creation with only filling for authorized user")
    public void createOrderWithOnlyFillingWithAuthReturnsOk200AndOrderNumber() {
        userLogin();
        Response response = createOrderWithIngredientTypeWithAuth("main");
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with only filling for unauthorized user")
    @Description("Checking status code and body response for order creation with only filling for unauthorized user")
    public void createOrderWithOnlyFillingWithoutAuthReturnsOk200AndOrderNumber() {
        Response response = createOrderWithIngredientTypeWithoutAuth("main");
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with only sauce for authorized user")
    @Description("Checking status code and body response for order creation with only sauce for authorized user")
    public void createOrderWithOnlySauceWithAuthReturnsOk200AndOrderNumber() {
        userLogin();
        Response response = createOrderWithIngredientTypeWithAuth("sauce");
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with only sauce for unauthorized user")
    @Description("Checking status code and body response for order creation with only sauce for unauthorized user")
    public void createOrderWithOnlySauceWithoutAuthReturnsOk200AndOrderNumber() {
        Response response = createOrderWithIngredientTypeWithoutAuth("sauce");
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with several ingredients for authorized user")
    @Description("Checking status code and body response for order creation with several ingredients for authorized user")
    public void createOrderWithSeveralIngredientsWithAuthReturnsOk200AndOrderNumber() {
        userLogin();
        bunId = getIngredientIdByType("bun");
        fillingId = getIngredientIdByType("main");
        sauceId = getIngredientIdByType("sauce");
        Response response = createOrderWithSeveralIngredientsWithAuth(bunId, fillingId, sauceId);
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with several ingredients for unauthorized user")
    @Description("Checking status code and body response for order creation with several ingredients for unauthorized user")
    public void createOrderWithSeveralIngredientsWithoutAuthReturnsOk200AndOrderNumber() {
        bunId = getIngredientIdByType("bun");
        fillingId = getIngredientIdByType("main");
        sauceId = getIngredientIdByType("sauce");
        Response response = createOrderWithSeveralIngredientsWithoutAuth(bunId, fillingId, sauceId);
        compareStatus200WithResponse(response);
        compareParameterSuccessFor200WithResponse(response);
        compareParameterOrderNumberFor200WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation without ingredients for authorized user")
    @Description("Checking status code and body response for order creation without ingredients for authorized user")
    public void createOrderWithoutIngredientsWithAuthReturnsError400() {
        userLogin();
        Response response = createOrderWithoutIngredientsWithAuth();
        compareStatus400WithResponse(response);
        compareParameterMessageFor400WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation without ingredients for unauthorized user")
    @Description("Checking status code and body response for order creation without ingredients for unauthorized user")
    public void createOrderWithoutIngredientsWithoutAuthReturnsError400() {
        Response response = createOrderWithoutIngredientsWithoutAuth();
        compareStatus400WithResponse(response);
        compareParameterMessageFor400WithResponse(response);
    }

    @Test
    @DisplayName("Check order creation with incorrect ingredient")
    @Description("Checking status code and body response for order creation with incorrect ingredient")
    public void createOrderWithoutIngredientsWithoutAuthReturnsError500() {
        Response response = createOrderWithIncorrectIngredientId();
        compareStatus500WithResponse(response);
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

    @Step("Create order with exact ingredient type: {type} for authorized user")
    public Response createOrderWithIngredientTypeWithAuth(String type) {
        switch (type) {
            case "bun":
                bunId = getIngredientIdByType(type);
                break;
            case "main":
                fillingId = getIngredientIdByType(type);
                break;
            case "sauce":
                sauceId = getIngredientIdByType(type);
                break;
        }

        String[] ingredientsIds;

        if (type.equals("bun")) {
            ingredientsIds = new String[]{bunId};
        } else if (type.equals("main")) {
            ingredientsIds = new String[]{fillingId};
        } else if (type.equals("sauce")) {
            ingredientsIds = new String[]{sauceId};
        } else {
            ingredientsIds = new String[0];
        }
        Order order = new Order().setIngredients(ingredientsIds);
        return orderClient.createWithAuth(order, accessToken);
    }

    @Step("Create order with exact ingredient type: {type} for unauthorized user")
    public Response createOrderWithIngredientTypeWithoutAuth(String type) {
        switch (type) {
            case "bun":
                bunId = getIngredientIdByType(type);
                break;
            case "main":
                fillingId = getIngredientIdByType(type);
                break;
            case "sauce":
                sauceId = getIngredientIdByType(type);
                break;
        }

        String[] ingredientsIds;

        if (type.equals("bun")) {
            ingredientsIds = new String[]{bunId};
        } else if (type.equals("main")) {
            ingredientsIds = new String[]{fillingId};
        } else if (type.equals("sauce")) {
            ingredientsIds = new String[]{sauceId};
        } else {
            ingredientsIds = new String[0];
        }
        Order order = new Order().setIngredients(ingredientsIds);
        return orderClient.createWithoutAuth(order);
    }


    @Step("Create order with several ingredients: bun={bun}, main={main}, sauce={sauce} for authorized user")
    public Response createOrderWithSeveralIngredientsWithAuth(String bun, String main, String sauce) {
        Order order = new Order()
                .setIngredients(new String[]{bun, main, sauce});
        return orderClient.createWithAuth(order, accessToken);
    }

    @Step("Create order with several ingredients: bun={bun}, main={main}, sauce={sauce} for unauthorized user")
    public Response createOrderWithSeveralIngredientsWithoutAuth(String bun, String main, String sauce) {
        Order order = new Order()
                .setIngredients(new String[]{bun, main, sauce});
        return orderClient.createWithoutAuth(order);
    }


    @Step("Send POST request to api/orders to create order without ingredients for authorized user")
    public Response createOrderWithoutIngredientsWithAuth() {
        Order order = new Order().setIngredients(new String[]{});
        Response response = orderClient.createWithAuth(order, accessToken);
        return response;
    }

    @Step("Send POST request to api/orders to create order without ingredients for unauthorized user")
    public Response createOrderWithoutIngredientsWithoutAuth() {
        Order order = new Order().setIngredients(new String[]{});
        Response response = orderClient.createWithoutAuth(order);
        return response;
    }

    @Step("Compare status code 200 with response")
    public void compareStatus200WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_OK);
    }

    @Step("Compare body parameter 'success' for 200 status with response")
    public void compareParameterSuccessFor200WithResponse(Response response) {
        response.then().assertThat().body("success", equalTo(true));
    }

    @Step("Compare body parameter 'order.number' for 200 status with response")
    public void compareParameterOrderNumberFor200WithResponse(Response response) {
        response.then().assertThat().body("order.number", notNullValue());
    }

    @Step("Compare status code 400 with response")
    public void compareStatus400WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_BAD_REQUEST);
    }

    @Step("Compare body parameter 'message' for 400 status with response")
    public void compareParameterMessageFor400WithResponse(Response response) {
        response.then().assertThat().body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Send POST request to api/orders to create order with incorrect ingredient id")
    public Response createOrderWithIncorrectIngredientId() {
        String incorrectIngredient = faker.bothify("##?#?#?##?#?#####?????#?");
        Order order = new Order().setIngredients(new String[]{incorrectIngredient});
        Response response = orderClient.createWithoutAuth(order);
        return response;
    }

    @Step("Compare status code 500 with response")
    public void compareStatus500WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @After
    public void tearTest() {
        Response loginResponse = userClient.login(UserCreds.credsFromUser(user));
        accessToken = loginResponse.jsonPath().getString("accessToken");
        if (accessToken != null && !accessToken.isEmpty()) {
            userClient.delete(accessToken);
        }
    }

}
