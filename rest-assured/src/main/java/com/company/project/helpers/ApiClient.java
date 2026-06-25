package com.company.project.helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.Objects;

/**
 * Centralized HTTP client for tests. Wraps Rest-Assured and provides reusable request methods.
 * Service layer classes should only use this client and must not call Rest-Assured directly.
 */
public class ApiClient {

    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        // Optionally set global base URI for Rest-Assured
        RestAssured.baseURI = this.baseUrl;
    }

    public Response postRequest(String endpoint, Object body, Map<String, String> headers) {
        RequestSpecification req = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (headers != null && !headers.isEmpty()) {
            req.headers(headers);
        }

        if (body != null) {
            req.body(body);
        }

        return req.when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    public Response getRequest(String endpoint, Map<String, String> queryParams, Map<String, String> headers) {
        RequestSpecification req = RestAssured.given()
                .accept(ContentType.JSON);

        if (headers != null && !headers.isEmpty()) {
            req.headers(headers);
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            req.queryParams(queryParams);
        }

        return req.when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    public Response putRequest(String endpoint, Object body, Map<String, String> headers) {
        RequestSpecification req = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (headers != null && !headers.isEmpty()) {
            req.headers(headers);
        }

        if (body != null) {
            req.body(body);
        }

        return req.when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }

    public Response deleteRequest(String endpoint, Map<String, String> headers) {
        RequestSpecification req = RestAssured.given()
                .accept(ContentType.JSON);

        if (headers != null && !headers.isEmpty()) {
            req.headers(headers);
        }

        return req.when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }
}

