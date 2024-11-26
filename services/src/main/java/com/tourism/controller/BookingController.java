package com.tourism.controller;

import com.tourism.configuration.annotation.CommonApiResponses;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Booking;
import com.tourism.model.BookingState;
import com.tourism.model.User;
import com.tourism.service.BookingService;
import com.tourism.util.EndpointConstants;
import com.tourism.util.ResponseEntityUtil;
import com.tourism.util.helpers.AuthenticationHelper;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Booking Controller", description = "Booking API")
@Slf4j
@RequestMapping(path = EndpointConstants.ROOT_PATH + EndpointConstants.BOOKING_PATH,
        produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class BookingController {

    private final BookingService service;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public BookingController(BookingService service, JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Operation(summary = "Create a booking", operationId = "create")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.TOURIST_ROLE)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<String>> create(HttpServletRequest request, @RequestBody @Valid BookingRequestDTO booking) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, service.create(booking, user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to create booking. User Not logged", null)}));
        }
    }


    @Operation(summary = "update a booking", operationId = "update")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.TOURIST_ROLE)
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<String>> update(HttpServletRequest request, @RequestBody BookingUpdateRequestDTO bookingDto) {
        User user = jwtTokenProvider.getUserFromToken(request);
        if (user != null) {
            return ResponseEntityUtil.buildObject(request, service.update(bookingDto, user.getId()));
        } else {
            return ResponseEntityUtil.buildObject(request, Either.left(new ErrorDto[]{
                    ErrorDto.of(HttpStatus.BAD_REQUEST, "Error to update booking. User Not logged", null)}));
        }
    }


    @Operation(summary = "Find all bookings", operationId = "findAll")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.ADMIN_ROLE)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<Page<BookingResponseDTO>>> findAll(HttpServletRequest request,
                                                                      @Valid @ModelAttribute PageableRequest paging) {
        return ResponseEntityUtil.buildObject(request, service.findAll(paging));
    }


    @Operation(summary = "delete a touristic place", operationId = "delete")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.ADMIN_ROLE + " or " + AuthenticationHelper.LODGING_OWNER_ROLE)
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponseDto<Booking>> delete(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, service.delete(id));
    }


    @Operation(summary = "Get a touristic place by id", operationId = "getById")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.EVERY_ROLE)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<BookingResponseDTO>> getById(HttpServletRequest request, @PathVariable("id") UUID id) {
        return ResponseEntityUtil.buildObject(request, service.getById(id));
    }


    @Operation(summary = "Lodging owner accept the booking. Is waiting for the payment", operationId = "pending")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.LODGING_OWNER_ROLE)
    @PutMapping(value = "/pending/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<BookingResponseDTO>> pendingBooking(HttpServletRequest request,
                                                                                  @PathVariable("id") UUID bookingId) {
        User user = jwtTokenProvider.getUserFromToken(request);
        return ResponseEntityUtil.buildObject(request, service.changeState(bookingId, BookingState.PENDING, user.getId()));
    }

    @Operation(summary = "Booking payment. Booking is accepted", operationId = "payment")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.TOURIST_ROLE)
    @PutMapping(value = "/accepted/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<BookingResponseDTO>> bookingPayment(HttpServletRequest request,
                                                                                  @PathVariable("id") UUID bookingId) {
        User user = jwtTokenProvider.getUserFromToken(request);
        return ResponseEntityUtil.buildObject(request, service.changeState(bookingId, BookingState.ACCEPTED, user.getId()));
    }

    @Operation(summary = "Reject the booking by lodging owner", operationId = "reject")
    @CommonApiResponses
    @PreAuthorize(AuthenticationHelper.LODGING_OWNER_ROLE)
    @PutMapping(value = "/reject/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponseDto<BookingResponseDTO>> rejectBooking(HttpServletRequest request,
                                                                                 @PathVariable("id") UUID bookingId) {
        User user = jwtTokenProvider.getUserFromToken(request);
        return ResponseEntityUtil.buildObject(request, service.changeState(bookingId, BookingState.REJECTED, user.getId()));
    }
}
