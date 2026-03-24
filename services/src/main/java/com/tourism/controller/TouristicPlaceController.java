package com.tourism.controller;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristicPlaceRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Region;
import com.tourism.model.TouristicPlace;
import com.tourism.model.User;
import com.tourism.service.TouristicPlaceService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import com.tourism.util.helpers.AuthenticationHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Tag(name = "Touristic Place", description = "Touristic API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH
      + EndpointConstants.TOURISTIC_PATH, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class TouristicPlaceController {

   private final TouristicPlaceService placeService;

   private final JwtTokenProvider jwtTokenProvider;

   @Operation(summary = "Create a touristic place", operationId = "create")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE + " or " + AuthenticationHelper.LODGING_OWNER_ROLE)
   @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> create(HttpServletRequest request,
         @RequestBody @Valid TouristicPlaceRequestDTO touristicPlaceDto) {
      User user = jwtTokenProvider.getUserFromToken(request);
      if (user != null) {
         return ResponseEntityUtil.buildObject(request, placeService.create(touristicPlaceDto, user.getId()));
      } else {
         return ResponseEntityUtil.buildObject(request,
               Either.left(new ErrorDto[] { new ErrorDto(HttpStatus.BAD_REQUEST, "Error to create touristic place. User Not logged", null) }));
      }
   }

   @Operation(summary = "update a touristic place", operationId = "update")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE + " or " + AuthenticationHelper.LODGING_OWNER_ROLE)
   @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> update(HttpServletRequest request,
         @RequestBody TouristicPlaceRequestDTO touristicPlaceDto) {
      return ResponseEntityUtil.buildObject(request, placeService.update(touristicPlaceDto));
   }

   @Operation(summary = "Get all touristic places", operationId = "findAll")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
   @SecurityRequirement(name = "bearerAuth")
   @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> findAll(HttpServletRequest request,
         @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, placeService.findAll(paging));
   }

   @Operation(summary = "delete a touristic place", operationId = "delete")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
   @DeleteMapping("/{id}")
   public ResponseEntity<StandardResponseDto<TouristicPlace>> delete(HttpServletRequest request, @PathVariable("id") UUID id) {
      return ResponseEntityUtil.buildObject(request, placeService.delete(id));
   }

   @Operation(summary = "Get a touristic place by id", operationId = "getById")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
   @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
      return ResponseEntityUtil.buildObject(request, placeService.getById(id));
   }

   @Operation(summary = "Find a touristic place by name", operationId = "findByName")
   @CommonApiResponses
   @SecurityRequirement(name = "bearerAuth")
   @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
   @GetMapping(value = "/name", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> findByName(HttpServletRequest request,
         @RequestParam("name") String name, @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, placeService.findByName(name, paging));
   }

   @Operation(summary = "Find a touristic place by region", operationId = "findByRegion")
   @CommonApiResponses
   @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
   @SecurityRequirement(name = "bearerAuth")
   @GetMapping(value = "/region", produces = MediaType.APPLICATION_JSON_VALUE)
   public ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> findByRegion(HttpServletRequest request,
         @RequestParam("region") Region region, @Valid @ModelAttribute PageableRequest paging) {
      return ResponseEntityUtil.buildObject(request, placeService.findByRegion(region, paging));
   }

}
