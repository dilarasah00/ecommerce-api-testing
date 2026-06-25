package com.company.project.api;

import com.company.project.helpers.ApiClient;
import com.company.project.model.request.CategoryRequest;
import io.restassured.response.Response;

import java.util.Objects;

/**
 * Service layer for Category endpoints. Uses only ApiClient for HTTP calls (no direct Rest-Assured usage).
 * Returns raw Response objects to allow test layer to handle assertions and error cases.
 */
public class CategoryAPI {

	private final ApiClient apiClient;
	private static final String BASE_PATH = "/api/categories";

	public CategoryAPI(ApiClient apiClient) {
		this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
	}

	public Response createCategory(CategoryRequest request) {
		return apiClient.postRequest(BASE_PATH, request, null);
	}

	public Response getCategoryById(Long id) {
		String path = String.format("%s/%d", BASE_PATH, id);
		return apiClient.getRequest(path, null, null);
	}

	public Response getAllCategories() {
		return apiClient.getRequest(BASE_PATH, null, null);
	}

	public Response updateCategory(Long id, CategoryRequest request) {
		String path = String.format("%s/%d", BASE_PATH, id);
		return apiClient.putRequest(path, request, null);
	}

	public Response deleteCategory(Long id) {
		String path = String.format("%s/%d", BASE_PATH, id);
		return apiClient.deleteRequest(path, null);
	}
}



