package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.configuration.annotation.RequiresRoles;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristicPlaceRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.Region;
import com.tourism.model.TouristicPlace;
import com.tourism.model.User;
import com.tourism.service.TouristicPlaceService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Touristic Place", description = "Touristic API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.TOURISTIC_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class TouristicPlaceController {

    private final TouristicPlaceService placeService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public TouristicPlaceController(TouristicPlaceService placeService, JwtTokenProvider jwtTokenProvider) {
        this.placeService = placeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Operation(summary = "Create a touristic place", operationId = "create")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN, Role.LODGING_OWNER})
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> create(HttpServletRequest request, @RequestBody @Valid TouristicPlaceRequestDTO touristicPlaceDto) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, placeService.create(touristicPlaceDto, user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    new ErrorDto(HttpStatus.BAD_REQUEST, "Error to create touristic place. User Not logged")}));
        }
    }


    @Operation(summary = "update a touristic place", operationId = "update")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN, Role.LODGING_OWNER})
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> update(HttpServletRequest request, @RequestBody TouristicPlaceRequestDTO touristicPlaceDto) {
        return ResponseEntityUtil.buildObject(request, placeService.update(touristicPlaceDto));
    }


    @Operation(summary = "Get all touristic places", operationId = "findAll")
    @CommonApiResponses
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> findAll(HttpServletRequest request,
                                                                                        @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, placeService.findAll(paging));
    }


    @Operation(summary = "delete a touristic place", operationId = "delete")
    @CommonApiResponses
    @RequiresRoles({Role.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<TouristicPlace>> delete(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, placeService.delete(id));
    }


    @Operation(summary = "Get a touristic place by id", operationId = "getById")
    @CommonApiResponses
    @RequiresRoles({Role.EVERY_ROL})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, placeService.getById(id));
    }


    @Operation(summary = "Find a touristic place by name", operationId = "findByName")
    @CommonApiResponses
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/name", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> findByName(HttpServletRequest request,
                                                                                           @RequestParam("name") String name,
                                                                                           @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, placeService.findByName(name, paging));
    }


    @Operation(summary = "Find a touristic place by region", operationId = "findByRegion")
    @CommonApiResponses
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(value = "/region", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> findByRegion(HttpServletRequest request,
                                                                                             @RequestParam("region") Region region,
                                                                                             @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, placeService.findByRegion(region, paging));
    }

}
