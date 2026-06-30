package com.company.project.tests.category;

import com.company.project.base.BaseTest;
import com.company.project.model.request.CategoryRequest;
import com.company.project.model.response.CategoryResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.company.project.utilities.DBUtils;
import com.company.project.utilities.LoggerUtil;

import java.util.List;
import java.util.Map;

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
        LoggerUtil.info("STEP 1: Creating Root Category 'Literature & Fiction'");
        CategoryRequest rootRequest = CategoryRequest.builder()
                .name("Literature & Fiction")
                .parentId(null)
                .build();

        Response rootCreateResponse = categoryAPI.createCategory(rootRequest);
        assertEquals(201, rootCreateResponse.getStatusCode(), "Expected 201 Created for root category");

        CategoryResponse rootCategory = rootCreateResponse.as(CategoryResponse.class);
        assertNotNull(rootCategory.getId(), "Root category ID should not be null");
        Long rootCategoryId = rootCategory.getId();

        // DB verification after STEP 1 (Root Create)
        LoggerUtil.info("Verifying Root Category ID {} in database", rootCategoryId);
        List<Map<String, Object>> rootRows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + rootCategoryId);
        assertFalse(rootRows.isEmpty(), "Root category should exist in DB after creation");
        Object rootNameVal = rootRows.get(0).get("name");
        if (rootNameVal == null) rootNameVal = rootRows.get(0).get("NAME");
        assertEquals("Literature & Fiction", String.valueOf(rootNameVal), "Root name in DB should match");
        Object rootParentVal = rootRows.get(0).get("parent_id");
        if (rootParentVal == null) rootParentVal = rootRows.get(0).get("PARENT_ID");
        assertNull(rootParentVal, "Root parent_id should be null in DB");
        LoggerUtil.info("Root Category verification passed");

        // ========== STEP 2: CREATE Child Category (TC-CAT-02) ==========
        LoggerUtil.info("STEP 2: Creating Child Category 'Turkish Literature TOP 50' with parent ID {}", rootCategoryId);
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

        // DB verification after STEP 2 (Child Create)
        LoggerUtil.info("Verifying Child Category ID {} in database", childCategoryId);
        List<Map<String, Object>> childRows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + childCategoryId);
        assertFalse(childRows.isEmpty(), "Child category should exist in DB after creation");
        Object childNameVal = childRows.get(0).get("name");
        if (childNameVal == null) childNameVal = childRows.get(0).get("NAME");
        assertEquals("Turkish Literature TOP 50", String.valueOf(childNameVal), "Child name in DB should match");
        Object childParentVal = childRows.get(0).get("parent_id");
        if (childParentVal == null) childParentVal = childRows.get(0).get("PARENT_ID");
        Long parentIdFromDb = null;
        if (childParentVal instanceof Number) parentIdFromDb = ((Number) childParentVal).longValue();
        else if (childParentVal != null) parentIdFromDb = Long.parseLong(childParentVal.toString());
        assertEquals(rootCategoryId, parentIdFromDb, "Child parent_id in DB should match rootCategoryId");
        LoggerUtil.info("Child Category verification passed");

        // ========== STEP 3: READ Child Category by ID (TC-CAT-06) ==========
        LoggerUtil.info("STEP 3: Reading Child Category by ID {}", childCategoryId);
        Response getChildResponse = categoryAPI.getCategoryById(childCategoryId);
        assertEquals(200, getChildResponse.getStatusCode(), "Expected 200 OK on fetching child category");

        CategoryResponse retrievedChild = getChildResponse.as(CategoryResponse.class);
        assertEquals(childCategoryId, retrievedChild.getId(), "Retrieved ID should match child ID");
        assertEquals(rootCategoryId, retrievedChild.getParentId(), "Retrieved parentId should match root ID");

        // ========== STEP 4: LIST All Categories (TC-CAT-07) ==========
        LoggerUtil.info("STEP 4: Listing all categories");
        Response listResponse = categoryAPI.getAllCategories();
        assertEquals(200, listResponse.getStatusCode(), "Expected 200 OK on listing categories");

        // Assuming the response returns a list or array of categories
        List<?> categories = listResponse.as(List.class);
        assertFalse(categories.isEmpty(), "Category list should not be empty after insertions");

        // ========== STEP 5: UPDATE Root Category Name (TC-CAT-10) ==========
        LoggerUtil.info("STEP 5: Updating Root Category ID {} name to 'Literature'", rootCategoryId);
        CategoryRequest updateRequest = CategoryRequest.builder()
                .name("Literature")
                .parentId(null)
                .build();

        Response updateResponse = categoryAPI.updateCategory(rootCategoryId, updateRequest);
        assertEquals(200, updateResponse.getStatusCode(), "Expected 200 OK on root category update");

        CategoryResponse updatedRoot = updateResponse.as(CategoryResponse.class);
        assertEquals("Literature", updatedRoot.getName(), "Root category name should be updated");

        // DB verification after STEP 5 (Update Root)
        LoggerUtil.info("Verifying updated Root Category ID {} in database", rootCategoryId);
        List<Map<String, Object>> updatedRootRows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + rootCategoryId);
        assertFalse(updatedRootRows.isEmpty(), "Root category should exist in DB after update");
        Object updatedNameVal = updatedRootRows.get(0).get("name");
        if (updatedNameVal == null) updatedNameVal = updatedRootRows.get(0).get("NAME");
        assertEquals("Literature", String.valueOf(updatedNameVal), "Root name in DB should be updated to Literature");
        LoggerUtil.info("Root Category update verification passed");

        // ========== STEP 6: DELETE Categories in Strict Order (TC-CAT-11) ==========
        LoggerUtil.info("STEP 6: Deleting Child Category ID {}", childCategoryId);
        // 1. Delete Child Category FIRST to prevent foreign key / constraint violations
        Response deleteChildResponse = categoryAPI.deleteCategory(childCategoryId);
        assertTrue(
                deleteChildResponse.getStatusCode() == 200 || deleteChildResponse.getStatusCode() == 204,
                "Expected 200 or 204 on child category deletion"
        );

        // 2. Delete Root Category SECOND now that it has no dependents
        LoggerUtil.info("Deleting Root Category ID {}", rootCategoryId);
        Response deleteRootResponse = categoryAPI.deleteCategory(rootCategoryId);
        assertTrue(
                deleteRootResponse.getStatusCode() == 200 || deleteRootResponse.getStatusCode() == 204,
                "Expected 200 or 204 on root category deletion"
        );

        // DB verification after STEP 6 (Delete both)
        LoggerUtil.info("Verifying both categories have been deleted from database - Root ID: {}, Child ID: {}", rootCategoryId, childCategoryId);
        List<Map<String, Object>> rootAfterDelete = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + rootCategoryId);
        List<Map<String, Object>> childAfterDelete = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + childCategoryId);
        assertTrue(rootAfterDelete.isEmpty() && childAfterDelete.isEmpty(), "Both categories should be removed from DB after deletion");
        LoggerUtil.info("Deletion verification passed");
    }

    @AfterEach
    public void tearDown() {
        DBUtils.destroy();
    }
}