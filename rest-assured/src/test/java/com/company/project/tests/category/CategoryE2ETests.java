package com.company.project.tests.category;

import com.company.project.base.BaseTest;
import com.company.project.model.request.CategoryRequest;
import com.company.project.model.response.CategoryResponse;
import io.restassured.response.Response;
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
@DisplayName("Category API Hierarchical E2E Tests")
public class CategoryE2ETests extends BaseTest {

    @Test
    @Story("Parent-Child Category Lifecycle (CRUD)")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Tests the full lifecycle of nested categories, verifies DB persistence, updates details, and performs teardown in correct constraint order.")
    @DisplayName("Should complete successful Parent-Child category lifecycle flow")
    public void successfulCategoryE2EFlow() {

        // ========== STEP 1: CREATE Root (Parent) Category (TC-CAT-01) ==========
        Allure.step("STEP 1: Creating Root Category 'Literature & Fiction' (TC-CAT-01)");
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
        Allure.step("Verifying Root Category ID " + rootCategoryId + " in database");
        LoggerUtil.info("Verifying Root Category ID {} in database", rootCategoryId);

        List<Map<String, Object>> rootRows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + rootCategoryId);
        assertFalse(rootRows.isEmpty(), "Root category should exist in DB after creation");
        assertEquals("Literature & Fiction", DBUtils.getString(rootRows, "name"), "Root name in DB should match");
        assertNull(DBUtils.getLong(rootRows, "parent_id"), "Root parent_id should be null in DB");

        LoggerUtil.info("Root Category verification passed");
        Allure.addAttachment("Root Category DB Row", rootRows.toString());

        // ========== STEP 2: CREATE Child Category (TC-CAT-02) ==========
        Allure.step("STEP 2: Creating Child Category 'Turkish Literature TOP 50' (TC-CAT-02)");
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
        Allure.step("Verifying Child Category ID " + childCategoryId + " in database");
        LoggerUtil.info("Verifying Child Category ID {} in database", childCategoryId);

        List<Map<String, Object>> childRows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + childCategoryId);
        assertFalse(childRows.isEmpty(), "Child category should exist in DB after creation");
        assertEquals("Turkish Literature TOP 50", DBUtils.getString(childRows, "name"), "Child name in DB should match");
        assertEquals(rootCategoryId, DBUtils.getLong(childRows, "parent_id"), "Child parent_id in DB should match rootCategoryId");

        LoggerUtil.info("Child Category verification passed");
        Allure.addAttachment("Child Category DB Row", childRows.toString());

        // ========== STEP 3: READ Child Category by ID (TC-CAT-06) ==========
        Allure.step("STEP 3: Reading Child Category by ID (TC-CAT-06)");
        LoggerUtil.info("STEP 3: Reading Child Category by ID {}", childCategoryId);

        Response getChildResponse = categoryAPI.getCategoryById(childCategoryId);
        assertEquals(200, getChildResponse.getStatusCode(), "Expected 200 OK on fetching child category");

        CategoryResponse retrievedChild = getChildResponse.as(CategoryResponse.class);
        assertEquals(childCategoryId, retrievedChild.getId(), "Retrieved ID should match child ID");
        assertEquals(rootCategoryId, retrievedChild.getParentId(), "Retrieved parentId should match root ID");

        // ========== STEP 4: LIST All Categories (TC-CAT-07) ==========
        Allure.step("STEP 4: Listing All Categories (TC-CAT-07)");
        LoggerUtil.info("STEP 4: Listing all categories");

        Response listResponse = categoryAPI.getAllCategories();
        assertEquals(200, listResponse.getStatusCode(), "Expected 200 OK on listing categories");

        List<?> categories = listResponse.as(List.class);
        assertFalse(categories.isEmpty(), "Category list should not be empty after insertions");

        // ========== STEP 5: UPDATE Root Category Name (TC-CAT-10) ==========
        Allure.step("STEP 5: Updating Root Category Name (TC-CAT-10)");
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
        Allure.step("Verifying updated Root Category in database");
        LoggerUtil.info("Verifying updated Root Category ID {} in database", rootCategoryId);

        List<Map<String, Object>> updatedRootRows = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + rootCategoryId);
        assertFalse(updatedRootRows.isEmpty(), "Root category should exist in DB after update");
        assertEquals("Literature", DBUtils.getString(updatedRootRows, "name"), "Root name in DB should be updated to Literature");

        LoggerUtil.info("Root Category update verification passed");

        // ========== STEP 6: DELETE Categories in Strict Order (TC-CAT-11) ==========
        Allure.step("STEP 6: Deleting Child Category FIRST (TC-CAT-11)");
        LoggerUtil.info("STEP 6: Deleting Child Category ID {}", childCategoryId);

        // 1. Delete Child Category FIRST to prevent foreign key / constraint violations
        Response deleteChildResponse = categoryAPI.deleteCategory(childCategoryId);
        assertTrue(
                deleteChildResponse.getStatusCode() == 200 || deleteChildResponse.getStatusCode() == 204,
                "Expected 200 or 204 on child category deletion"
        );

        // 2. Delete Root Category SECOND now that it has no dependents
        Allure.step("Deleting Root Category SECOND");
        LoggerUtil.info("Deleting Root Category ID {}", rootCategoryId);

        Response deleteRootResponse = categoryAPI.deleteCategory(rootCategoryId);
        assertTrue(
                deleteRootResponse.getStatusCode() == 200 || deleteRootResponse.getStatusCode() == 204,
                "Expected 200 or 204 on root category deletion"
        );

        // DB verification after STEP 6 (Delete both)
        Allure.step("Verifying deletion of both categories in database");
        LoggerUtil.info("Verifying both categories have been deleted from database - Root ID: {}, Child ID: {}", rootCategoryId, childCategoryId);

        List<Map<String, Object>> rootAfterDelete = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + rootCategoryId);
        List<Map<String, Object>> childAfterDelete = DBUtils.getQueryResultList("SELECT * FROM category WHERE id = " + childCategoryId);
        assertTrue(rootAfterDelete.isEmpty() && childAfterDelete.isEmpty(), "Both categories should be removed from DB after deletion");

        LoggerUtil.info("Deletion verification passed");
    }
}