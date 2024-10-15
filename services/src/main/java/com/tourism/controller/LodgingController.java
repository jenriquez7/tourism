package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.configuration.annotation.RequiresRoles;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.Lodging;
import com.tourism.model.User;
import com.tourism.service.LodgingService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;
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
@Tag(name = "Lodging Controller", description = "Lodging API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.LODGING_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class LodgingController {

    private final LodgingService service;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public LodgingController(LodgingService service, JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Operation(summary = "Create a lodging", operationId = "create")
    @CommonApiResponses
    @RequiresRoles({Role.LODGING_OWNER})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<LodgingResponseDTO>> create(HttpServletRequest request,
                                                                          @RequestBody @Valid Lodging touristicPlaceDto) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, service.create(touristicPlaceDto, user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to create lodging. User Not logged")}));
        }
    }


    @Operation(summary = "update a lodging", operationId = "update")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN, Role.LODGING_OWNER})
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<LodgingResponseDTO>> update(HttpServletRequest request, @RequestBody Lodging touristicPlaceDto) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, service.update(touristicPlaceDto, user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to update lodging. User Not logged")}));
        }
    }


    @Operation(summary = "Get all lodgings", operationId = "findAll")
    @CommonApiResponses
    @RequiresRoles({Role.EVERY_ROL})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<LodgingResponseDTO>>> findAll(HttpServletRequest request,
                                                                                 @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, service.findAll(paging));
    }


    @Operation(summary = "delete a touristic place", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN, Role.LODGING_OWNER})
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Lodging>> delete(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, service.delete(id));
    }


    @Operation(summary = "Get a lodging by id", operationId = "getById")
    @CommonApiResponses
    @RequiresRoles({Role.EVERY_ROL})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<LodgingResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, service.getById(id));
    }


    @Operation(summary = "Get all lodgings by touristic places", operationId = "findAll")
    @CommonApiResponses
    @RequiresRoles({Role.EVERY_ROL})
    @GetMapping(value = "/touristic_place/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<LodgingResponseDTO>>> findLodgingsByTouristicPlace(HttpServletRequest request,
                                                                                                      @PathVariable("id") UUID id,
                                                                                                      @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, service.findLodgingsByTouristicPlace(id, paging));
    }
}
