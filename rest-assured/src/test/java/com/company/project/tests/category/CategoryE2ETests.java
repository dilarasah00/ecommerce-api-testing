package com.company.project.tests.category;

import com.company.project.base.BaseTest;
import com.company.project.model.request.CategoryRequest;
import com.company.project.model.response.CategoryResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End (E2E) test class for Category API.
 * Tests the complete positive CRUD flow maintaining Parent-Child relationships
 * and validating strict deletion order (Child first, then Parent).
 */
@DisplayName("Category API Hierarchical E2E Tests")
public class CategoryE2ETests extends BaseTest {

    @Test
    @DisplayName("Should complete successful Parent-Child category lifecycle flow")
    public void successfulCategoryE2EFlow() {

        // ========== STEP 1: CREATE Root (Parent) Category (TC-CAT-01) ==========
        CategoryRequest rootRequest = CategoryRequest.builder()
                .name("Literature & Fiction")
                .parentId(null)
                .build();

        Response rootCreateResponse = categoryAPI.createCategory(rootRequest);
        assertEquals(201, rootCreateResponse.getStatusCode(), "Expected 201 Created for root category");

        CategoryResponse rootCategory = rootCreateResponse.as(CategoryResponse.class);
        assertNotNull(rootCategory.getId(), "Root category ID should not be null");
        Long rootCategoryId = rootCategory.getId();

        // ========== STEP 2: CREATE Child Category (TC-CAT-02) ==========
        CategoryRequest childRequest = CategoryRequest.builder()
                .name("Turkish Literature TOP 50")
                .parentId(rootCategoryId) // Linking to the created root category
                .build();

        Response childCreateResponse = categoryAPI.createCategory(childRequest);
        assertEquals(201, childCreateResponse.getStatusCode(), "Expected 201 Created for child category");

        CategoryResponse childCategory = childCreateResponse.as(CategoryResponse.class);
        assertNotNull(childCategory.getId(), "Child category ID should not be null");
        assertEquals(rootCategoryId, childCategory.getParentId(), "Child's parentId should match root category ID");
        Long childCategoryId = childCategory.getId();

        // ========== STEP 3: READ Child Category by ID (TC-CAT-06) ==========
        Response getChildResponse = categoryAPI.getCategoryById(childCategoryId);
        assertEquals(200, getChildResponse.getStatusCode(), "Expected 200 OK on fetching child category");

        CategoryResponse retrievedChild = getChildResponse.as(CategoryResponse.class);
        assertEquals(childCategoryId, retrievedChild.getId(), "Retrieved ID should match child ID");
        assertEquals(rootCategoryId, retrievedChild.getParentId(), "Retrieved parentId should match root ID");

        // ========== STEP 4: LIST All Categories (TC-CAT-07) ==========
        Response listResponse = categoryAPI.getAllCategories();
        assertEquals(200, listResponse.getStatusCode(), "Expected 200 OK on listing categories");

        // Assuming the response returns a list or array of categories
        List<?> categories = listResponse.as(List.class);
        assertFalse(categories.isEmpty(), "Category list should not be empty after insertions");

        // ========== STEP 5: UPDATE Root Category Name (TC-CAT-10) ==========
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Literature")
                .parentId(null)
                .build();

        Response updateResponse = categoryAPI.updateCategory(rootCategoryId, updateRequest);
        assertEquals(200, updateResponse.getStatusCode(), "Expected 200 OK on root category update");

        CategoryResponse updatedRoot = updateResponse.as(CategoryResponse.class);
        assertEquals("Literature", updatedRoot.getName(), "Root category name should be updated");

        // ========== STEP 6: DELETE Categories in Strict Order (TC-CAT-11) ==========
        // 1. Delete Child Category FIRST to prevent foreign key / constraint violations
        Response deleteChildResponse = categoryAPI.deleteCategory(childCategoryId);
        assertTrue(
                deleteChildResponse.getStatusCode() == 200 || deleteChildResponse.getStatusCode() == 204,
                "Expected 200 or 204 on child category deletion"
        );

        // 2. Delete Root Category SECOND now that it has no dependents
        Response deleteRootResponse = categoryAPI.deleteCategory(rootCategoryId);
        assertTrue(
                deleteRootResponse.getStatusCode() == 200 || deleteRootResponse.getStatusCode() == 204,
                "Expected 200 or 204 on root category deletion"
        );
    }
}