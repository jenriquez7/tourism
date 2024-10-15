package com.tourism.test.controller;

import com.tourism.controller.BookingController;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.*;
import com.tourism.service.BookingService;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookingControllerTests {

    @Mock
    private BookingService service;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private BookingController controller;

    private BookingRequestDTO requestDTO;
    private BookingResponseDTO responseDTO;
    private BookingUpdateRequestDTO updateRequestDTO;

    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        checkIn = LocalDate.now();
        checkOut = LocalDate.now().plusDays(3L);
        UUID bookingId = UUID.randomUUID();
        Lodging lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Parada 5, playa mansa", "+5984422112233", 50, 25.0, 5, new TouristicPlace(), new LodgingOwner(), true);
        requestDTO = new BookingRequestDTO(checkIn, checkOut, lodging, 2, 1, 1);
        responseDTO = new BookingResponseDTO(bookingId, lodging.getName(), "Turista", "Verano", checkIn, checkOut, 150.0, lodging.getPhone(), lodging.getInformation(), BookingState.CREATED);
        updateRequestDTO = new BookingUpdateRequestDTO(bookingId, checkIn.plusDays(5L), checkOut.plusDays(7L));
    }

    @Test
    @DisplayName("Create Booking")
    void create() {
        User user = new User(UUID.randomUUID(), "tverano@email.com", Role.TOURIST);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.create(requestDTO, user.getId())).thenReturn(Either.right(responseDTO));
        ResponseEntity<StandardResponseDto<BookingResponseDTO>> response = controller.create(request, requestDTO);

        verifyBookingResponseDto(response);
        verify(service, times(1)).create(requestDTO, user.getId());
    }

    @Test
    @DisplayName("Update Booking")
    void update() {
        BookingResponseDTO updated = new BookingResponseDTO(responseDTO.id(), responseDTO.lodgingName(), responseDTO.firstName(),
                responseDTO.lastName(), checkIn.plusDays(5L), checkOut.plusDays(7L), responseDTO.totalPrice(),
                responseDTO.lodgingPhone(), responseDTO.lodgingInformation(), responseDTO.state());

        User user = new User(UUID.randomUUID(), "tverano@email.com", Role.TOURIST);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.update(updateRequestDTO, user.getId())).thenReturn(Either.right(updated));

        ResponseEntity<StandardResponseDto<BookingResponseDTO>> response = controller.update(request, updateRequestDTO);

        verifyBookingResponseDto(response);
        verify(service, times(1)).update(updateRequestDTO, user.getId());
    }

    @Test
    @DisplayName("Find All Bookings")
    void findAll() {
        List<BookingResponseDTO> places = Collections.singletonList(responseDTO);
        Page<BookingResponseDTO> page = new PageImpl<>(places);

        when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(page));

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<BookingResponseDTO>>> response = controller.findAll(request, pageableRequest);

        verifyPageBookingResponseDto(response);
        verify(service, times(1)).findAll(pageableRequest);
    }

    @Test
    @DisplayName("Delete Booking")
    void delete() {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(Either.right(booking));

        ResponseEntity<StandardResponseDto<Booking>> response = controller.delete(request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Booking> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Booking.class, data[0]);
        assertEquals(booking, data[0]);
        verify(service, times(1)).delete(id);
    }

    @Test
    @DisplayName("Get Booking By Id")
    void getById() {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(Either.right(responseDTO));
        ResponseEntity<StandardResponseDto<BookingResponseDTO>> response = controller.getById(request, id);
        verifyBookingResponseDto(response);
        verify(service, times(1)).getById(id);
    }

    @Test
    @DisplayName("Update to Pending Booking")
    void pendingBooking() {
        User user = new User(UUID.randomUUID(), "owner@email.com", Role.LODGING_OWNER);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        UUID bookingId = UUID.randomUUID();

        when(service.changeState(bookingId, BookingState.PENDING, user.getId())).thenReturn(Either.right(responseDTO));
        ResponseEntity<StandardResponseDto<BookingResponseDTO>> response = controller.pendingBooking(request, bookingId);

        verifyBookingResponseDto(response);
        verify(service, times(1)).changeState(bookingId, BookingState.PENDING, user.getId());
    }

    @Test
    @DisplayName("Update to paid booking")
    void bookingPayment() {
        User user = new User(UUID.randomUUID(), "tverano@email.com", Role.TOURIST);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        UUID bookingId = UUID.randomUUID();

        when(service.changeState(bookingId, BookingState.ACCEPTED, user.getId())).thenReturn(Either.right(responseDTO));
        ResponseEntity<StandardResponseDto<BookingResponseDTO>> response = controller.bookingPayment(request, bookingId);

        verifyBookingResponseDto(response);
        verify(service, times(1)).changeState(bookingId, BookingState.ACCEPTED, user.getId());
    }

    @Test
    @DisplayName("Update to rejected booking")
    void rejectBooking() {
        User user = new User(UUID.randomUUID(), "owner@email.com", Role.LODGING_OWNER);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        UUID bookingId = UUID.randomUUID();

        when(service.changeState(bookingId, BookingState.REJECTED, user.getId())).thenReturn(Either.right(responseDTO));
        ResponseEntity<StandardResponseDto<BookingResponseDTO>> response = controller.rejectBooking(request, bookingId);

        verifyBookingResponseDto(response);
        verify(service, times(1)).changeState(bookingId, BookingState.REJECTED, user.getId());
    }


    private void verifyBookingResponseDto(ResponseEntity<StandardResponseDto<BookingResponseDTO>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<BookingResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(BookingResponseDTO.class, data[0]);

        BookingResponseDTO responseDto = (BookingResponseDTO) data[0];
        assertEquals(responseDTO.id(), responseDto.id());
        assertEquals(responseDTO.lodgingPhone(), responseDto.lodgingPhone());
        assertEquals(responseDTO.firstName(), responseDto.firstName());
    }

    private void verifyPageBookingResponseDto(ResponseEntity<StandardResponseDto<Page<BookingResponseDTO>>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<BookingResponseDTO>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<BookingResponseDTO> resultPage = (Page<BookingResponseDTO>) data[0];
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        BookingResponseDTO responseDto = resultPage.getContent().getFirst();
        assertEquals(responseDTO.id(), responseDto.id());
        assertEquals(responseDTO.checkIn(), responseDto.checkIn());
        assertEquals(responseDTO.lodgingPhone(), responseDto.lodgingPhone());
        assertEquals(responseDTO.firstName(), responseDto.firstName());
    }
}
