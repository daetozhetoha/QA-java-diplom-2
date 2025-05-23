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

import static edu.praktikum.diploma.generators.UserGenerator.randomUser;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class UserLoginTest extends Url {
    private UserClient userClient;
    private String accessToken;
    private User user;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        userClient = new UserClient();
        user = randomUser();
        userClient.create(user);
    }

    @Test
    @DisplayName("Check user login with valid credentials")
    @Description("Checking status code and body response for login with valid email and password")
    public void existingUserLoginReturnsOk200AndToken() {
        Response loginResponse = sendPostRequestAuthLogin();
        compareStatus200WithResponse(loginResponse);
        compareParameterAccessTokenFor200WithResponse(loginResponse);
    }

    @Test
    @DisplayName("Check user login with wrong email")
    @Description("Checking status code and body response for login with wrong email")
    public void loginUserWithWrongEmailReturnsError401() {
        Response loginResponse = sendPostRequestV1CourierLoginWithWrongEmail();
        compareStatus401WithResponse(loginResponse);
        compareParameterMessageFor401WithResponse(loginResponse);
    }

    @Test
    @DisplayName("Check user login with wrong password")
    @Description("Checking status code and body response for login with wrong password")
    public void loginUserWithWrongPasswordReturnsError401() {
        Response loginResponse = sendPostRequestV1CourierLoginWithWrongPassword();
        compareStatus401WithResponse(loginResponse);
        compareParameterMessageFor401WithResponse(loginResponse);
    }


    @Step("Send POST request to api/auth/login with valid creds")
    public Response sendPostRequestAuthLogin() {
        Response loginResponse = userClient.login(UserCreds.credsFromUser(user));
        return loginResponse;
    }

    @Step("Compare status code 200 with response")
    public void compareStatus200WithResponse(Response loginResponse) {
        loginResponse.then().assertThat().statusCode(SC_OK);
    }

    @Step("Compare body parameter 'accessToken' for 200 status with response")
    public void compareParameterAccessTokenFor200WithResponse(Response response) {
        response.then().assertThat().body("accessToken", notNullValue());
    }

    @Step("Send POST request to api/v1/courier/login with wrong email")
    public Response sendPostRequestV1CourierLoginWithWrongEmail() {
        Response wrongEmailResponse = userClient.login(UserCreds.credsWithWrongEmail(user));
        return wrongEmailResponse;
    }

    @Step("Send POST request to api/v1/courier/login with wrong password")
    public Response sendPostRequestV1CourierLoginWithWrongPassword() {
        Response wrongPassResponse = userClient.login(UserCreds.credsWithWrongPassword(user));
        return wrongPassResponse;
    }

    @Step("Compare status code 401 with response")
    public void compareStatus401WithResponse(Response error404Response) {
        error404Response.then().assertThat().statusCode(SC_UNAUTHORIZED);
    }

    @Step("Compare body parameter 'message' for 401 status with response")
    public void compareParameterMessageFor401WithResponse(Response error401Response) {
        error401Response.then().assertThat().body("message", equalTo("email or password are incorrect"));
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
