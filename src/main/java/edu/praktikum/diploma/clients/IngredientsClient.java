package edu.praktikum.diploma.clients;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class IngredientsClient {
    private static final String API_INGREDIENTS = "api/ingredients";

    public Response get() {
        return given()
                .header("Content-type", "application/json")
                .get(API_INGREDIENTS);
    }
}
