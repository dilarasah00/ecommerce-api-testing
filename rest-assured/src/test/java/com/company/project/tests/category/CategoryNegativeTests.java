package com.company.project.tests.category;

import com.company.project.base.BaseTest;
import com.company.project.model.request.CategoryRequest;
import io.restassured.response.Response;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Category Negative Test Cases")
public class CategoryNegativeTests extends BaseTest {

    // Helper method to generate a unique random ID for each test execution
    private Long generateNonExistingId() {
        return ThreadLocalRandom.current().nextLong(900000L, 999999L);
    }

    @Test
    @DisplayName("TC-CAT-03: Create category with non-existing parent ID should return 404")
    public void createCategory_withNonExistingParentId_shouldReturn404() {
        // Arrange
        Long nonExistingParentId = generateNonExistingId();
        CategoryRequest request = CategoryRequest.builder()
                .name("manga")
                .parentId(nonExistingParentId)
                .build();

        // Act
        Response response = categoryAPI.createCategory(request);

        // Assert
        assertEquals(404, response.getStatusCode(), "Expected 404 Not Found for non-existing parent ID");
    }

    @Test
    @DisplayName("TC-CAT-08: Get category with non-existing ID should return 404")
    public void getCategory_withNonExistingId_shouldReturn404() {
        // Arrange
        Long nonExistingId = generateNonExistingId();

        // Act
        Response response = categoryAPI.getCategoryById(nonExistingId);

        // Assert
        assertEquals(404, response.getStatusCode(), "Expected 404 Not Found for non-existing category ID");
    }

    @Test
    @DisplayName("TC-CAT-12: Delete category with non-existing ID should return 404")
    public void deleteCategory_withNonExistingId_shouldReturn404() {
        // Arrange
        Long nonExistingId = generateNonExistingId();

        // Act
        Response response = categoryAPI.deleteCategory(nonExistingId);

        // Assert
        assertEquals(404, response.getStatusCode(), "Expected 404 Not Found when deleting non-existing category");
    }
}