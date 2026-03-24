package com.tourism.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
import com.tourism.dto.request.LodgingOwnerRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.LodgingOwner;
import com.tourism.model.User;
import com.tourism.service.LodgingOwnerService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import com.tourism.util.helpers.AuthenticationHelper;

import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "Lodging Owner", description = "Lodging Owner API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.LODGING_OWNER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class LodgingOwnerController {

   private final LodgingOwnerService touristService;

   private final JwtTokenProvider jwtTokenProvider;

   @Operation(summary = "Create a lodging owner", operationId = "create")
   @CommonApiResponses
   @PermitAll
   @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> create(HttpServletRequest request,
         @RequestBody @Valid LodgingOwnerRequestDTO touristDto) {
      return ResponseEntityUtil.buildObject(request, touristService.create(touristDto));
   }

   @Operation(summary = "Get all lodging owners", operationId = "findAll")
   @CommonApiResponses
   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   public ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> findAll(HttpServletRequest request,
         @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, touristService.findAll(paging));
   }

   @Operation(summary = "delete a lodging owner by admin by id", operationId = "delete")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @DeleteMapping("/{id}")
   public ResponseEntity<StandardResponseDto<LodgingOwner>> deleteByAdmin(HttpServletRequest request, @PathVariable("id") UUID id) {
      return ResponseEntityUtil.buildObject(request, touristService.delete(id));
   }

   @Operation(summary = "delete a lodging owner by token", operationId = "delete")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.LODGING_OWNER_ROLE)
   @DeleteMapping()
   public ResponseEntity<StandardResponseDto<LodgingOwner>> delete(HttpServletRequest request) {
      User user = jwtTokenProvider.getUserFromToken(request);
      if (user != null) {
         return ResponseEntityUtil.buildObject(request, touristService.delete(user.getId()));
      } else {
         return ResponseEntityUtil.buildObject(request,
               Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to delete tourist. Not logged") }));
      }
   }

   @Operation(summary = "Get a lodging owner by id", operationId = "getById")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
      return ResponseEntityUtil.buildObject(request, touristService.getById(id));
   }

   @Operation(summary = "lodging owner profile", operationId = "profile")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.LODGING_OWNER_ROLE)
   @GetMapping("/profile")
   public ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> profile(HttpServletRequest request) {
      User user = jwtTokenProvider.getUserFromToken(request);
      if (user != null) {
         return ResponseEntityUtil.buildObject(request, touristService.getById(user.getId()));
      } else {
         return ResponseEntityUtil.buildObject(request,
               Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to delete tourist. Not logged") }));
      }
   }

   @Operation(summary = "Get a lodging owner by email", operationId = "findByEmail")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @GetMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> findByEmail(HttpServletRequest request,
         @RequestParam("email") String email, @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, touristService.findByEmail(email, paging));
   }

   @Operation(summary = "Get a lodging owner by last name", operationId = "findByLastName")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @GetMapping(value = "/lastName", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> findByLastName(HttpServletRequest request,
         @RequestParam("lastName") String lastName, @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, touristService.findByLastName(lastName, paging));
   }

}
