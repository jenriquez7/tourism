package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.configuration.annotation.RequiresRoles;
import com.tourism.dto.request.LodgingOwnerRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.LodgingOwner;
import com.tourism.model.User;
import com.tourism.service.LodgingOwnerService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Lodging Owner", description = "Lodging Owner API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.LODGING_OWNER_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class LodgingOwnerController {

    private final LodgingOwnerService touristService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public LodgingOwnerController(LodgingOwnerService touristService, JwtTokenProvider jwtTokenProvider) {
        this.touristService = touristService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Operation(summary = "Create a lodging owner", operationId = "create")
    @CommonApiResponses
    @PermitAll
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> create(HttpServletRequest request, @RequestBody @Valid LodgingOwnerRequestDTO touristDto) {
        return ResponseEntityUtil.buildObject(request, touristService.create(touristDto));
    }


    @Operation(summary = "Get all lodging owners", operationId = "findAll")
    @CommonApiResponses
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresRoles({Role.ADMIN})
    public ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> findAll(HttpServletRequest request,
                                                                                       @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, touristService.findAll(paging));
    }


    @Operation(summary = "delete a lodging owner by admin by id", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<LodgingOwner>> deleteByAdmin(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, touristService.delete(id));
    }


    @Operation(summary = "delete a lodging owner by token", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.LODGING_OWNER})
    @DeleteMapping()
    public ResponseEntity<StandardResponseDto<LodgingOwner>> delete(HttpServletRequest request) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, touristService.delete(user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to delete tourist. Not logged")}));
        }
    }


    @Operation(summary = "Get a lodging owner by id", operationId = "getById")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, touristService.getById(id));
    }


    @Operation(summary = "lodging owner profile", operationId = "profile")
    @CommonApiResponses
    @RequiresRoles({Role.LODGING_OWNER})
    @GetMapping("/profile")
    public ResponseEntity<StandardResponseDto<LodgingOwnerResponseDTO>> profile(HttpServletRequest request) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, touristService.getById(user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to delete tourist. Not logged")}));
        }
    }


    @Operation(summary = "Get a lodging owner by email", operationId = "findByEmail")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> findByEmail(HttpServletRequest request,
                                                                                          @RequestParam("email") String email,
                                                                                          @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, touristService.findByEmail(email, paging));
    }


    @Operation(summary = "Get a lodging owner by last name", operationId = "findByLastName")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/lastName", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<LodgingOwnerResponseDTO>>> findByLastName(HttpServletRequest request,
                                                                                             @RequestParam("lastName") String lastName,
                                                                                             @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, touristService.findByLastName(lastName, paging));
    }
}
