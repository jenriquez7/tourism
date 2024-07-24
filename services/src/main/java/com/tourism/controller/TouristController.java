package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.configuration.annotation.RequiresRoles;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.Tourist;
import com.tourism.model.User;
import com.tourism.service.TouristService;
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
@Tag(name = "Tourist", description = "Tourist API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.TOURIST_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TouristController {

    private final TouristService touristService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public TouristController(TouristService touristService, JwtTokenProvider jwtTokenProvider) {
        this.touristService = touristService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Operation(summary = "Create an admin", operationId = "create")
    @CommonApiResponses
    @PermitAll
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<TouristResponseDTO>> create(HttpServletRequest request, @RequestBody @Valid TouristRequestDTO touristDto) {
        return ResponseEntityUtil.buildObject(request, touristService.create(touristDto));
    }


    @Operation(summary = "Get all tourists", operationId = "findAll")
    @CommonApiResponses
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @RequiresRoles({Role.ADMIN})
    public ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> findAll(HttpServletRequest request,
                                                                                 @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, touristService.findAll(paging));
    }


    @Operation(summary = "delete an tourist by admin by id", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Tourist>> deleteByAdmin(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, touristService.delete(id));
    }


    @Operation(summary = "delete a tourist by token", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.TOURIST})
    @DeleteMapping()
    public ResponseEntity<StandardResponseDto<Tourist>> delete(HttpServletRequest request) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, touristService.delete(user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    new ErrorDto(HttpStatus.BAD_REQUEST, "Error to delete tourist. Not logged")}));
        }
    }


    @Operation(summary = "Get an admin by id", operationId = "getById")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<TouristResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, touristService.getById(id));
    }


    @Operation(summary = "delete a tourist by token", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.TOURIST})
    @GetMapping("/profile")
    public ResponseEntity<StandardResponseDto<TouristResponseDTO>> profile(HttpServletRequest request) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, touristService.getById(user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    new ErrorDto(HttpStatus.BAD_REQUEST, "Error to delete tourist. Not logged")}));
        }
    }


    @Operation(summary = "Get a tourist by email", operationId = "findByEmail")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> findByEmail(HttpServletRequest request,
                                                                                     @RequestParam("email") String email,
                                                                                     @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, touristService.findByEmail(email, paging));
    }


    @Operation(summary = "Get a tourist by last name", operationId = "findByLastName")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @GetMapping(value = "/lastName", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> findByLastName(HttpServletRequest request,
                                                                                        @RequestParam("lastName") String lastName,
                                                                                        @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, touristService.findByLastName(lastName, paging));
    }
}
