package com.company.project.base;

import com.company.project.api.CategoryAPI;
import com.company.project.helpers.ApiClient;

import org.junit.jupiter.api.BeforeEach;

public class BaseTest {

	protected ApiClient apiClient;
	protected CategoryAPI categoryAPI;

	private static final String BASE_URL = "http://localhost:8081";


	@BeforeEach
	public void setUp() {
		apiClient = new ApiClient(BASE_URL);

		categoryAPI = new CategoryAPI(apiClient);
	}
}

