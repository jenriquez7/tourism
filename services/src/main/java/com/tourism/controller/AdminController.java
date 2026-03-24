package com.tourism.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.AdminResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.service.AdminService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import com.tourism.util.helpers.AuthenticationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "Admin", description = "Admin API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.ADMIN_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class AdminController {

   private final AdminService adminService;

   @Operation(summary = "Create an admin", operationId = "create")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<AdminResponseDTO>> create(HttpServletRequest request, @RequestBody @Valid AuthUserDto authUserDto) {
      return ResponseEntityUtil.buildObject(request, adminService.create(authUserDto));
   }

   @Operation(summary = "Get all admins", operationId = "findAll")
   @CommonApiResponses
   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   @SecurityRequirement(name = "bearerAuth")
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   public ResponseEntity<StandardResponseDto<Page<AdminResponseDTO>>> findAll(HttpServletRequest request,
         @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, adminService.findAll(paging));
   }

   @Operation(summary = "Get an admin by id", operationId = "getById")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<AdminResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
      return ResponseEntityUtil.buildObject(request, adminService.getById(id));
   }

   @Operation(summary = "delete an admin by id", operationId = "delete")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @DeleteMapping("/{id}")
   public ResponseEntity<StandardResponseDto<AdminResponseDTO>> delete(HttpServletRequest request, @PathVariable("id") UUID id) {
      return ResponseEntityUtil.buildObject(request, adminService.delete(id));
   }

   @Operation(summary = "Get an admin by email", operationId = "findByEmail")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @GetMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<Page<AdminResponseDTO>>> getByEmail(HttpServletRequest request, @RequestParam("email") String email,
         @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, adminService.findByEmail(email, paging));
   }

}
