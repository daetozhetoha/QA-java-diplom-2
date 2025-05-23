package edu.praktikum.diploma;

import edu.praktikum.diploma.base.Url;
import edu.praktikum.diploma.clients.UserClient;
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

import static edu.praktikum.diploma.generators.UserGenerator.faker;
import static edu.praktikum.diploma.generators.UserGenerator.randomUser;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserRegistrationTest extends Url {
    private UserClient userClient;
    private String accessToken;
    private User user;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        userClient = new UserClient();
        user = randomUser();
    }

    @Test
    @DisplayName("Check unique user registration")
    @Description("Checking status code and body response for unique user registration")
    public void registerUniqueUserReturns200AndToken() {
        Response response = sendPostRequestAuthRegister();
        compareStatus200WithResponse(response);
        compareParameterAccessTokenWithResponse(response);
    }

    @Test
    @DisplayName("Check existing user registration")
    @Description("Checking status code and body response for register user existing in database")
    public void registerExistingUserReturnsError403() {
        createUser();
        Response response = sendPostRequestAuthRegister();
        compareStatus403WithResponse(response);
        compareParameterMessageFor403ExistingUserWithResponse(response);
    }

    @Test
    @DisplayName("Check user registration without email")
    @Description("Checking status code and body response for register user without email")
    public void registerUserWithoutEmailReturnsError403() {
        Response response = (sendPostRequestV1CouriersWithoutEmail());
        compareStatus403WithResponse(response);
        compareParameterMessageFor403RequiredFieldWithResponse(response);
    }

    @Test
    @DisplayName("Check user registration without password")
    @Description("Checking status code and body response for register user without password")
    public void registerUserWithoutPasswordReturnsError403() {
        Response response = (sendPostRequestV1CouriersWithoutPassword());
        compareStatus403WithResponse(response);
        compareParameterMessageFor403RequiredFieldWithResponse(response);
    }

    @Test
    @DisplayName("Check user registration without name")
    @Description("Checking status code and body response for register user without name")
    public void registerUserWithoutNameReturnsError403() {
        Response response = (sendPostRequestV1CouriersWithoutName());
        compareStatus403WithResponse(response);
        compareParameterMessageFor403RequiredFieldWithResponse(response);
    }

    @Step("Send POST request to api/auth/register with valid creds")
    public Response sendPostRequestAuthRegister() {
        Response response = userClient.create(user);
        return response;
    }

    @Step("Compare status code 200 with response")
    public void compareStatus200WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_OK);
    }

    @Step("Compare body parameter 'accessToken' for 200 status with response")
    public void compareParameterAccessTokenWithResponse(Response response) {
        response.then().assertThat().body("accessToken", notNullValue());
    }

    @Step("Create user")
    public void createUser() {
        userClient.create(user);
    }

    @Step("Compare status code 403 with response")
    public void compareStatus403WithResponse(Response duplicateResponse) {
        duplicateResponse.then().assertThat().statusCode(SC_FORBIDDEN);
    }

    @Step("Compare body parameter 'message' for 403 status (existing user) with response")
    public void compareParameterMessageFor403ExistingUserWithResponse(Response response) {
        response.then().assertThat().body("message", equalTo("User already exists"));
    }

    @Step("Compare body parameter 'message' for 403 status (required field absence) with response")
    public void compareParameterMessageFor403RequiredFieldWithResponse(Response response) {
        response.then().assertThat().body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Send POST request to api/auth/register without firstname")
    public Response sendPostRequestV1CouriersWithoutEmail() {
        User userWithoutEmail = new User()
                .setPassword(faker.internet().password(12, 20))
                .setName(faker.name().username());
        Response response = userClient.create(userWithoutEmail);
        return response;
    }

    @Step("Send POST request to api/auth/register without password")
    public Response sendPostRequestV1CouriersWithoutPassword() {
        User userWithoutPassword = new User()
                .setEmail(faker.internet().safeEmailAddress())
                .setName(faker.name().username());
        Response response = userClient.create(userWithoutPassword);
        return response;
    }

    @Step("Send POST request to api/auth/register without name")
    public Response sendPostRequestV1CouriersWithoutName() {
        User userWithoutName = new User()
                .setEmail(faker.internet().safeEmailAddress())
                .setPassword(faker.internet().password(12, 20));
        Response response = userClient.create(userWithoutName);
        return response;
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
