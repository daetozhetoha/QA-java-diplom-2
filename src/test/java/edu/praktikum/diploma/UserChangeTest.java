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

import java.util.HashMap;
import java.util.Map;

import static edu.praktikum.diploma.generators.UserGenerator.faker;
import static edu.praktikum.diploma.generators.UserGenerator.randomUser;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UserChangeTest extends Url {
    private UserClient userClient;
    private String accessToken;
    private User user;
    private String newEmail;
    private String newName;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;
        userClient = new UserClient();
        user = randomUser();
        userClient.create(user);
    }

    @Test
    @DisplayName("Check that email for authorized user has changed")
    @Description("Checking status code and body response authorized user email changing")
    public void changeEmailForAuthorizedUserReturnsOk200() {
        userLogin();
        Response response = sendPostRequestAuthUserToChangeEmailWithAuth();
        compareStatus200WithResponse(response);
        checkThatEmailIsChanged(response, newEmail);
    }

    @Test
    @DisplayName("Check that name for authorized user has changed")
    @Description("Checking status code and body response authorized user name changing")
    public void changeNameForAuthorizedUserReturnsOk200() {
        userLogin();
        Response response = sendPostRequestAuthUserToChangeNameWithAuth();
        compareStatus200WithResponse(response);
        checkThatNameIsChanged(response, newName);
    }

    @Test
    @DisplayName("Check that email for unauthorized user has not changed")
    @Description("Checking status code and body response unauthorized user email changing")
    public void changeEmailForAuthorizedUserReturnsError401() {
        Response response = sendPostRequestAuthUserToChangeEmailWithoutAuth();
        compareStatus401WithResponse(response);
        compareParameterMessageFor401WithResponse(response);
    }

    @Test
    @DisplayName("Check that name for unauthorized user has not changed")
    @Description("Checking status code and body response unauthorized user name changing")
    public void changeNameForAuthorizedUserReturnsError401() {
        Response response = sendPostRequestAuthUserToChangeNameWithoutAuth();
        compareStatus401WithResponse(response);
        compareParameterMessageFor401WithResponse(response);
    }

    @Step("Get accessToken")
    public void userLogin() {
        Response loginResponse = userClient.login(UserCreds.credsFromUser(user));
        accessToken = loginResponse.jsonPath().getString("accessToken");
    }

    @Step("Send PATCH request to api/auth/user to change email for authorized user")
    public Response sendPostRequestAuthUserToChangeEmailWithAuth() {
        this.newEmail = faker.internet().safeEmailAddress();
        Map<String, Object> body = new HashMap<>();
        body.put("email", newEmail);
        return userClient.patchWithAuth(accessToken, body);
    }

    @Step("Send PATCH request to api/auth/user to change name for authorized user")
    public Response sendPostRequestAuthUserToChangeNameWithAuth() {
        this.newName = faker.name().username();
        Map<String, Object> body = new HashMap<>();
        body.put("name", newName);
        return userClient.patchWithAuth(accessToken, body);
    }

    @Step("Send PATCH request to api/auth/user to change email for unauthorized user")
    public Response sendPostRequestAuthUserToChangeEmailWithoutAuth() {
        this.newEmail = faker.internet().safeEmailAddress();
        Map<String, Object> body = new HashMap<>();
        body.put("email", newEmail);
        return userClient.patchWithoutAuth(body);
    }

    @Step("Send PATCH request to api/auth/user to change name for authorized user")
    public Response sendPostRequestAuthUserToChangeNameWithoutAuth() {
        this.newName = faker.name().username();
        Map<String, Object> body = new HashMap<>();
        body.put("name", newName);
        return userClient.patchWithoutAuth(body);
    }

    @Step("Compare status code 200 with response")
    public void compareStatus200WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_OK);
    }

    @Step("Check that user.email has changed after patch request")
    public void checkThatEmailIsChanged(Response response, String expectedEmail) {
        String actualEmail = response.jsonPath().getString("user.email");
        assertThat("Email не изменился", actualEmail, equalTo(expectedEmail));
    }

    @Step("Check that user.name has changed after patch request")
    public void checkThatNameIsChanged(Response response, String expectedName) {
        String actualName = response.jsonPath().getString("user.name");
        assertThat("Name не изменился", actualName, equalTo(expectedName));
    }

    @Step("Compare status code 401 with response")
    public void compareStatus401WithResponse(Response response) {
        response.then().assertThat().statusCode(SC_UNAUTHORIZED);
    }

    @Step("Compare body parameter 'message' for 401 status with response")
    public void compareParameterMessageFor401WithResponse(Response response) {
        response.then().assertThat().body("message", equalTo("You should be authorised"));
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
