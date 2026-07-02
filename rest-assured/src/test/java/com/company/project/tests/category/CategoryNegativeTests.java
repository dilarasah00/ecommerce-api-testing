package com.company.project.tests.category;

import com.company.project.base.BaseTest;
import com.company.project.model.request.CategoryRequest;
import io.restassured.response.Response;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.company.project.utilities.DBUtils;
import com.company.project.utilities.LoggerUtil;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import io.qameta.allure.*;

@Epic("E-Commerce Platform")
@Feature("Category Management")
@DisplayName("Category Negative Test Cases")
public class CategoryNegativeTests extends BaseTest {

    // Helper method to generate a unique random ID for each test execution
    private Long generateNonExistingId() {
        return ThreadLocalRandom.current().nextLong(900000L, 999999L);
    }

    @Test
    @Story("Create Category - Negative Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies that creating a category with a parent ID that does not exist in the database fails gracefully with a 404 Not Found status code.")
    @DisplayName("TC-CAT-03: Create category with non-existing parent ID should return 404")
    public void createCategory_withNonExistingParentId_shouldReturn404() {
        // Arrange
        Long nonExistingParentId = generateNonExistingId();
        LoggerUtil.info("TC-CAT-03: Attempting to create category with non-existing parent ID: {}", nonExistingParentId);
        CategoryRequest request = CategoryRequest.builder()
                .name("manga")
                .parentId(nonExistingParentId)
                .build();

        // Act
        Response response = categoryAPI.createCategory(request);

        // Assert
        assertEquals(404, response.getStatusCode(), "Expected 404 Not Found for non-existing parent ID");
        LoggerUtil.info("API returned 404 as expected for invalid parent ID");

        // DB verification: ensure no category was created with the invalid parent ID
        Allure.step("Verifying DB state: No data persistence for invalid parent ID " + nonExistingParentId);
        LoggerUtil.info("Verifying that no category was created in DB for invalid parent ID: {}", nonExistingParentId);
        List<Map<String, Object>> created = DBUtils.getQueryResultList("SELECT * FROM category WHERE name = 'manga' AND parent_id = " + nonExistingParentId);
        assertTrue(created.isEmpty(), "No category should be created in DB for invalid parent id");
        LoggerUtil.info("DB verification passed - no invalid category created");
    }


    @Test
    @Story("Read Category - Negative Scenarios")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifies that requesting a category details with a non-existing ID returns a 404 Not Found error and database contains no such record.")
    @DisplayName("TC-CAT-08: Get category with non-existing ID should return 404")
    public void getCategory_withNonExistingId_shouldReturn404() {
        // Arrange
        Long nonExistingId = generateNonExistingId();
        LoggerUtil.info("TC-CAT-08: Attempting to get category with non-existing ID: {}", nonExistingId);

        // Act
        Response response = categoryAPI.getCategoryById(nonExistingId);

        // Assert
        assertEquals(404, response.getStatusCode(), "Expected 404 Not Found for non-existing category ID");
        LoggerUtil.info("API returned 404 as expected for non-existing category ID");

        // DB verification: ensure the ID truly does not exist in DB
        Allure.step("Verifying DB state: Checking that ID " + nonExistingId + " is strictly absent");
        LoggerUtil.info("Verifying that ID {} does not exist in DB", nonExistingId);
        List<Map<String, Object>> rows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + nonExistingId);
        assertTrue(rows.isEmpty(), "No category should exist in DB with the non-existing id");
        LoggerUtil.info("DB verification passed - no category found with ID {}", nonExistingId);
    }

    @Test
    @Story("Delete Category - Negative Scenarios")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifies that attempting to delete a category that does not exist returns a 404 Not Found response and has no side effects on the database.")
    @DisplayName("TC-CAT-12: Delete category with non-existing ID should return 404")
    public void deleteCategory_withNonExistingId_shouldReturn404() {
        // Arrange
        Long nonExistingId = generateNonExistingId();
        LoggerUtil.info("TC-CAT-12: Attempting to delete category with non-existing ID: {}", nonExistingId);

        // Act
        Response response = categoryAPI.deleteCategory(nonExistingId);

        // Assert
        assertEquals(404, response.getStatusCode(), "Expected 404 Not Found when deleting non-existing category");
        LoggerUtil.info("API returned 404 as expected for non-existing category deletion");

        // DB verification: ensure the ID truly does not exist in DB after delete attempt
        Allure.step("Verifying DB state after deletion attempt for ID " + nonExistingId);
        LoggerUtil.info("Verifying that ID {} does not exist in DB after delete attempt", nonExistingId);
        List<Map<String, Object>> rows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + nonExistingId);
        assertTrue(rows.isEmpty(), "No category should exist in DB with the non-existing id after delete attempt");
        LoggerUtil.info("DB verification passed - no category found after delete attempt with ID {}", nonExistingId);
    }
}