package edu.praktikum.diploma.clients;

import edu.praktikum.diploma.models.Order;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderClient {
    private static final String API_ORDERS = "api/orders";

    public Response createWithAuth(Order order, String token) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .and()
                .body(order)
                .when()
                .post(API_ORDERS);
    }

    public Response createWithoutAuth(Order order) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(order)
                .when()
                .post(API_ORDERS);
    }

    public Response getWithAuth(String token) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .when()
                .get(API_ORDERS);
    }

    public Response getWithoutAuth() {
        return given()
                .header("Content-type", "application/json")
                .when()
                .get(API_ORDERS);
    }
}
