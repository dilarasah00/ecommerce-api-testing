package com.company.project.base;

import com.company.project.api.CategoryAPI;
import com.company.project.helpers.ApiClient;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for all API test classes.
 * Initializes ApiClient and service layer instances for use in test methods.
 */
public class BaseTest {

	protected ApiClient apiClient;
	protected CategoryAPI categoryAPI;

	private static final String BASE_URL = "http://localhost:8081";

	@BeforeEach
	public void setUp() {
		// Initialize ApiClient with base URL
		apiClient = new ApiClient(BASE_URL);

		// Initialize CategoryAPI with ApiClient
		categoryAPI = new CategoryAPI(apiClient);
	}
}

