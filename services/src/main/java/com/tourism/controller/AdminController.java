package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.dto.request.PageableRequest;
import com.tourism.model.Role;
import com.tourism.configuration.annotation.RequiresRoles;
import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.AdminResponseDTO;
import com.tourism.service.AdminService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Admin", description = "Admin API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.ADMIN_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController (AdminService adminService) {
        this.adminService = adminService;
    }


    @Operation(summary = "Create an admin", operationId = "create")
    @CommonApiResponses
    //@RequiresRoles({Role.ADMIN})
    @PermitAll
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<AdminResponseDTO>> create(HttpServletRequest request, @RequestBody @Valid AuthUserDto authUserDto) {
        return ResponseEntityUtil.buildObject(request, adminService.create(authUserDto));
    }


    @Operation(summary = "Get all admins", operationId = "findAll")
    @CommonApiResponses
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @RequiresRoles({Role.ADMIN})
    public ResponseEntity<StandardResponseDto<Page<AdminResponseDTO>>> findAll(HttpServletRequest request,
                                                                               @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, adminService.findAll(paging));
    }


    @Operation(summary = "Get an admin by id", operationId = "getById")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<AdminResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, adminService.getById(id));
    }


    @Operation(summary = "delete an admin by id", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<AdminResponseDTO>> delete(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, adminService.delete(id));
    }



    @Operation(summary = "Get an admin by email", operationId = "findByEmail")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<AdminResponseDTO>>> getByEmail(HttpServletRequest request,
                                                                                  @RequestParam("email") String email,
                                                                                  @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, adminService.findByEmail(email, paging));
    }
}
