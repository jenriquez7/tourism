package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.model.Category;
import com.tourism.service.CategoryService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import com.tourism.util.helpers.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Tag(name = "Category", description = "Category API")
@RequestMapping(EndpointConstants.ROOT_PATH + EndpointConstants.CATEGORY_PATH)
@Validated
@PermitAll
public class CategoryController {

    private final CategoryService service;

    @Autowired
    public CategoryController(CategoryService service) {
        this.service = service;
    }


    @Operation(summary = "EndPoint for create a category", operationId = "createCategory")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
    @PostMapping
    public ResponseEntity<StandardResponseDto<Category>> create(HttpServletRequest request, @RequestBody Category category) {
        return ResponseEntityUtil.buildObject(request, service.create(category));
    }


    @Operation(summary = "Endpoint returns every categories", operationId = "findAllCategories")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
    @GetMapping
    public ResponseEntity<StandardResponseDto<Category>> findAll(HttpServletRequest request) {
        return ResponseEntityUtil.buildArray(request, service.findAll());
    }



    @Operation(summary = "Endpoint to update an specific category", operationId = "updateCategory")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
    @PutMapping
    public ResponseEntity<StandardResponseDto<Category>> update(HttpServletRequest request, @RequestBody Category category) {
        return ResponseEntityUtil.buildObject(request, service.update(category));
    }


    @Operation(summary = "Endpoint returns a category by id", operationId = "getCategoryById")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Category>> getById(HttpServletRequest request, @PathVariable("id") Integer id) {
        return ResponseEntityUtil.buildObject(request, service.getById(id));
    }


    @Operation(summary = "Endpoint returns a category by name", operationId = "getCategoryByName")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
    @GetMapping("/findByName")
    public ResponseEntity<StandardResponseDto<Category>> findByName(HttpServletRequest request, @RequestParam("name") String name) {
        return ResponseEntityUtil.buildArray(request, service.findByName(name));
    }


    @Operation(summary = "Endpoint to delete a category", operationId = "deleteCategory")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Category>> delete(HttpServletRequest request, @PathVariable("id") Integer id) {
        return ResponseEntityUtil.buildObject(request, service.delete(id));
    }

}
