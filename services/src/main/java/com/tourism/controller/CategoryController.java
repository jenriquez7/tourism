package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.configuration.annotation.RequiresRoles;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.model.Role;
import com.tourism.model.Category;
import com.tourism.service.CategoryService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @RequiresRoles({Role.ADMIN})
    @PostMapping
    public ResponseEntity<StandardResponseDto<Category>> create(HttpServletRequest request, @RequestBody Category category) {
        return ResponseEntityUtil.buildObject(request, service.create(category));
    }


    @Operation(summary = "Endpoint returns every categories", operationId = "findAllCategories")
    @CommonApiResponses
    @PermitAll
    @GetMapping
    public ResponseEntity<StandardResponseDto<Category>> findAll(HttpServletRequest request) {
        return ResponseEntityUtil.buildArray(request, service.findAll());
    }



    @Operation(summary = "Endpoint to update an specific category", operationId = "updateCategory")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @PutMapping
    public ResponseEntity<StandardResponseDto<Category>> update(HttpServletRequest request, @RequestBody Category category) {
        return ResponseEntityUtil.buildObject(request, service.update(category));
    }


    @Operation(summary = "Endpoint returns a category by id", operationId = "getCategoryById")
    @CommonApiResponses
    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Category>> getById(HttpServletRequest request, @PathVariable("id") Integer id) {
        return ResponseEntityUtil.buildObject(request, service.getById(id));
    }


    @Operation(summary = "Endpoint returns a category by name", operationId = "getCategoryByName")
    @CommonApiResponses
    @PermitAll
    @GetMapping("/findByName")
    public ResponseEntity<StandardResponseDto<Category>> findByName(HttpServletRequest request, @RequestParam("name") String name) {
        return ResponseEntityUtil.buildArray(request, service.findByName(name));
    }


    @Operation(summary = "Endpoint to delete a category", operationId = "deleteCategory")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Category>> delete(HttpServletRequest request, @PathVariable("id") Integer id) {
        return ResponseEntityUtil.buildObject(request, service.delete(id));
    }

}
