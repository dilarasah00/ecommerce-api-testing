package com.company.project.helpers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;
import java.util.Objects;

public class ApiClient {

    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
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

        return req.post(endpoint);
    }

    public Response getRequest(String endpoint, Map<String, String> queryParams, Map<String, String> headers) {

        System.out.println("BEFORE REQUEST");

        Response res = RestAssured.given()
                .log().all()
                .get(endpoint)
                .andReturn();

        System.out.println("AFTER REQUEST");

        return res;
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

        return req.put(endpoint);
    }

    public Response deleteRequest(String endpoint, Map<String, String> headers) {
        RequestSpecification req = RestAssured.given()
                .accept(ContentType.JSON);

        if (headers != null && !headers.isEmpty()) {
            req.headers(headers);
        }

        return req.delete(endpoint);
    }
}