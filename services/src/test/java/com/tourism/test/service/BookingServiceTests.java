package com.tourism.test.service;

import com.tourism.dto.mappers.BookingMapper;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Role;
import com.tourism.model.*;
import com.tourism.repository.BookingDateRepository;
import com.tourism.repository.BookingRepository;
import com.tourism.repository.LodgingRepository;
import com.tourism.repository.TouristRepository;
import com.tourism.service.impl.BookingServiceImpl;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.BookingValidation;
import com.tourism.util.validations.DateValidation;
import com.tourism.util.helpers.PricingService;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    @Mock
    private BookingRepository repository;
    @Mock
    private TouristRepository touristRepository;
    @Mock
    private LodgingRepository lodgingRepository;
    @Mock
    private BookingValidation bookingValidation;
    @Mock
    private PricingService pricingService;
    @Mock
    private PageService pageService;
    @Mock
    private BookingDateRepository dateRepository;
    @Mock
    private DateValidation dateValidation;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequestDTO bookingDto;
    private Tourist tourist;
    private Lodging lodging;

    private BookingUpdateRequestDTO updateDto;
    private Booking existingBooking;
    List<LocalDate> mockDates;

    private PageableRequest pageableRequest;
    private Pageable pageable;
    private Page<Booking> bookingPage;
    private BookingResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(3L);
        tourist = new Tourist("tverano@email.com", "12345678", "Turista", "Verano", Role.TOURIST, TouristType.STANDARD, true);
        lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Parada 5, playa mansa", "+5984422112233", 50, 25.0, 5, new TouristicPlace(), new LodgingOwner(), true);
        tourist.setId(UUID.randomUUID());
        lodging.setId(UUID.randomUUID());
        bookingDto = new BookingRequestDTO(checkIn, checkOut, lodging, 2, 1, 1);
        updateDto = new BookingUpdateRequestDTO(UUID.randomUUID(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
        existingBooking = new Booking(checkIn, checkOut, 100.0, lodging, tourist, BookingState.CREATED, 2, 1, 1, false);
        existingBooking.setId(updateDto.getBookingId());
        pageableRequest = new PageableRequest(0, 10, new String[]{"email"}, Sort.Direction.ASC);
        pageable = mock(Pageable.class);
        bookingPage = new PageImpl<>(Arrays.asList(new Booking(), new Booking()));
        responseDTO = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(), bookingDto.getCheckIn(), bookingDto.getCheckOut(), existingBooking.getTotalPrice(), lodging.getPhone(), lodging.getInformation(), existingBooking.getState());
        mockDates = Arrays.asList(
                bookingDto.getCheckIn(),
                bookingDto.getCheckIn().plusDays(1)
        );
    }

    @Test
    @DisplayName("Create Booking - Success")
    void createBookingSuccess() {
        BookingResponseDTO dto = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(),
                bookingDto.getCheckIn(), bookingDto.getCheckOut(), existingBooking.getTotalPrice(), lodging.getPhone(),
                lodging.getInformation(), existingBooking.getState());
        when(dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut())).thenReturn(mockDates);
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
        when(bookingMapper.modelToResponseDTO(any(Booking.class))).thenReturn(dto);
        when(repository.save(any())).thenAnswer(b -> {
            Booking savedBooking = b.getArgument(0);
            savedBooking.setId(UUID.randomUUID());
            return savedBooking;
        });



        Either<ErrorDto[], BookingResponseDTO> result = bookingService.create(bookingDto, tourist.getId());

        assertTrue(result.isRight());
        BookingResponseDTO response = result.get();
        assertNotNull(response);
        assertEquals(lodging.getName(), response.lodgingName());
        assertEquals(tourist.getFirstName(), response.firstName());
        assertEquals(tourist.getLastName(), response.lastName());
        assertEquals(dto.checkIn(), response.checkIn());
        assertEquals(dto.checkOut(), response.checkOut());
        assertEquals(BookingState.CREATED, response.state());
    }

    @Test
    @DisplayName("Create Booking - Fails")
    void createBookingValidationFails() {
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, "Validation failed")}));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.create(bookingDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals("Validation failed", errors[0].message());
    }

    @Test
    @DisplayName("Create Booking - Tourist Not Found")
    void createBookingTouristNotFound() {
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.empty());

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.create(bookingDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertTrue(errors[0].message().contains(MessageConstants.ERROR_BOOKING_NOT_CREATED));
    }

    @Test
    @DisplayName("Create Booking - Lodging Not Found")
    void createBookingLodgingNotFound() {
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.empty());

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.create(bookingDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertTrue(errors[0].message().contains(MessageConstants.ERROR_BOOKING_NOT_CREATED));
    }

    @Test
    @DisplayName("Create Booking - DataIntegrityViolation")
    void createBookingDataIntegrityViolation() {
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("Integrity violation"));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.create(bookingDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_NOT_CREATED, errors[0].message());
    }

    @Test
    @DisplayName("Update Booking - Success")
    void updateBookingSuccess() {
        BookingResponseDTO dto = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(),
                bookingDto.getCheckIn(), bookingDto.getCheckOut(), existingBooking.getTotalPrice(), lodging.getPhone(),
                lodging.getInformation(), BookingState.CREATED);
        when(dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut())).thenReturn(mockDates);
        doNothing().when(dateRepository).deleteByBooking(existingBooking);
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(repository.findById(updateDto.getBookingId())).thenReturn(Optional.of(existingBooking));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
        when(bookingMapper.modelToResponseDTO(any(Booking.class))).thenReturn(dto);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.update(updateDto, tourist.getId());

        assertTrue(result.isRight());
        BookingResponseDTO response = result.get();
        assertNotNull(response);
        assertEquals(dto.checkIn(), response.checkIn());
        assertEquals(dto.checkOut(), response.checkOut());
        assertEquals(BookingState.CREATED, response.state());
    }

    @Test
    @DisplayName("Update Booking - Booking Not Found")
    void updateBookingNotFound() {
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
        when(repository.findById(updateDto.getBookingId())).thenReturn(Optional.empty());

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.update(updateDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_NOT_FOUND, errors[0].message());
    }

    @Test
    @DisplayName("Update Booking - Validation Fails")
    void updateBookingValidationFails() {
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(repository.findById(updateDto.getBookingId())).thenReturn(Optional.of(existingBooking));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, "Validation failed")}));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.update(updateDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals("Validation failed", errors[0].message());
    }

    @Test
    @DisplayName("Update Booking - Data Integrity Violation")
    void updateBookingDataIntegrityViolation() {
        when(dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut())).thenReturn(mockDates);
        doNothing().when(dateRepository).deleteByBooking(existingBooking);
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(repository.findById(updateDto.getBookingId())).thenReturn(Optional.of(existingBooking));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.update(updateDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.CONFLICT, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_NOT_UPDATED, errors[0].message());
    }

    @Test
    @DisplayName("Update Booking - Generic Exception")
    void updateBookingGenericException() {
        when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
        when(repository.findById(updateDto.getBookingId())).thenReturn(Optional.of(existingBooking));
        when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
        when(repository.save(any())).thenThrow(new RuntimeException("Generic error"));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.update(updateDto, tourist.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_NOT_UPDATED, errors[0].message());
    }

    @Test
    @DisplayName("Find All Bookings - Success")
    void findAllBookingsSuccess() {
        List<Booking> mockBookings = Arrays.asList(existingBooking, existingBooking);
        bookingPage = new PageImpl<>(mockBookings);

        when(pricingService.calculateBookingPrice(eq(TouristType.STANDARD), any(), any(), anyInt(), anyInt(), anyInt())).thenReturn(100.0);

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
        when(repository.findAll(pageable)).thenReturn(bookingPage);
        when(bookingMapper.modelToResponseDTO(any(Booking.class))).thenReturn(responseDTO);

        Either<ErrorDto[], Page<BookingResponseDTO>> result = bookingService.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<BookingResponseDTO> responsePage = result.get();
        assertNotNull(responsePage);
        assertEquals(2, responsePage.getContent().size());
        assertEquals(bookingPage.getTotalElements(), responsePage.getTotalElements());
        assertEquals(bookingPage.getTotalPages(), responsePage.getTotalPages());

        BookingResponseDTO responseDTO = responsePage.getContent().getFirst();
        assertEquals(lodging.getName(), responseDTO.lodgingName());
        assertEquals(tourist.getFirstName(), responseDTO.firstName());
        assertEquals(tourist.getLastName(), responseDTO.lastName());
        assertEquals(100.0, responseDTO.totalPrice());
        assertEquals(BookingState.CREATED, responseDTO.state());
    }

    @Test
    @DisplayName("Find All Bookings - Empty Page")
    void findAllBookingsEmptyPage() {
        Page<Booking> emptyPage = new PageImpl<>(Collections.emptyList());
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<BookingResponseDTO>> result = bookingService.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<BookingResponseDTO> responsePage = result.get();
        assertNotNull(responsePage);
        assertTrue(responsePage.getContent().isEmpty());
        assertEquals(0, responsePage.getTotalElements());
    }

    @Test
    @DisplayName("Find All Bookings - Exception Thrown")
    void findAllBookingsException() {
        when(pageService.createSortedPageable(pageableRequest)).thenThrow(new RuntimeException("Test exception"));

        Either<ErrorDto[], Page<BookingResponseDTO>> result = bookingService.findAll(pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.GENERIC_ERROR, errors[0].message());
        assertEquals("Test exception", errors[0].detail());
    }

    @Test
    @DisplayName("Delete Booking - Success")
    void deleteBookingSuccess() {
        UUID bookingId = UUID.randomUUID();
        Booking mockBooking = new Booking();
        when(repository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        doNothing().when(repository).delete(mockBooking);

        Either<ErrorDto[], Booking> result = bookingService.delete(bookingId);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(repository).delete(mockBooking);
    }

    @Test
    @DisplayName("Delete Booking - Invalid Data Access")
    void deleteBookingInvalidDataAccess() {
        UUID bookingId = UUID.randomUUID();
        Booking mockBooking = new Booking();
        when(repository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        doThrow(new InvalidDataAccessApiUsageException("Invalid data access")).when(repository).delete(mockBooking);

        Either<ErrorDto[], Booking> result = bookingService.delete(bookingId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_NOT_FOUND, errors[0].message());
    }

    @Test
    @DisplayName("Delete Booking - Generic Exception")
    void deleteBookingGenericException() {
        UUID bookingId = UUID.randomUUID();
        Booking mockBooking = new Booking();
        when(repository.findById(bookingId)).thenReturn(Optional.of(mockBooking));
        doThrow(new RuntimeException("Generic error")).when(repository).delete(mockBooking);

        Either<ErrorDto[], Booking> result = bookingService.delete(bookingId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_DELETING_BOOKING, errors[0].message());
    }

    @Test
    @DisplayName("Get Booking By Id - Success")
    void getBookingByIdSuccess() {
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
        when(repository.findById(existingBooking.getId())).thenReturn(Optional.of(existingBooking));
        when(pricingService.calculateBookingPrice(eq(TouristType.STANDARD), any(), any(), anyInt(), anyInt(), anyInt())).thenReturn(100.0);
        when(bookingMapper.modelToResponseDTO(any(Booking.class))).thenReturn(responseDTO);

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.getById(existingBooking.getId());

        assertTrue(result.isRight());
        BookingResponseDTO responseDTO = result.get();
        assertNotNull(responseDTO);
        assertEquals(existingBooking.getId(), responseDTO.id());
        assertEquals(lodging.getName(), responseDTO.lodgingName());
        assertEquals(tourist.getFirstName(), responseDTO.firstName());
        assertEquals(tourist.getLastName(), responseDTO.lastName());
        assertEquals(100.0, responseDTO.totalPrice());
        assertEquals(BookingState.CREATED, responseDTO.state());
    }

    @Test
    @DisplayName("Get Booking By Id - Booking Not Found")
    void getBookingByIdNotFound() {
        UUID bookingId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.getById(bookingId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_ID, errors[0].message());
    }

    @Test
    @DisplayName("Get Booking By Id - Exception Thrown")
    void getBookingByIdException() {
        UUID bookingId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenThrow(new RuntimeException("Test exception"));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.getById(bookingId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_BOOKING, errors[0].message());
        assertEquals("Test exception", errors[0].detail());
    }

    @Test
    @DisplayName("Change Booking State - Success")
    void changeBookingStateSuccess() {
        BookingResponseDTO dto = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(),
                bookingDto.getCheckIn(), bookingDto.getCheckOut(), existingBooking.getTotalPrice(), lodging.getPhone(),
                lodging.getInformation(), BookingState.ACCEPTED);
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(bookingValidation.validChangeState(existingBooking, BookingState.ACCEPTED, userId)).thenReturn(Either.right(true));
        when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pricingService.calculateBookingPrice(eq(TouristType.STANDARD), any(), any(), anyInt(), anyInt(), anyInt())).thenReturn(100.0);
        when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
        when(bookingMapper.modelToResponseDTO(any(Booking.class))).thenReturn(dto);

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.changeState(bookingId, BookingState.ACCEPTED, userId);

        assertTrue(result.isRight());
        BookingResponseDTO responseDTO = result.get();
        assertNotNull(responseDTO);
        assertEquals(BookingState.ACCEPTED, responseDTO.state());
        verify(repository).save(existingBooking);
    }

    @Test
    @DisplayName("Change Booking State - Booking Not Found")
    void changeBookingStateNotFound() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.empty());

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.changeState(bookingId, BookingState.ACCEPTED, userId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_NOT_FOUND, errors[0].message());
    }

    @Test
    @DisplayName("Change Booking State - Invalid State Change")
    void changeBookingStateInvalid() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
        when(bookingValidation.validChangeState(existingBooking, BookingState.ACCEPTED, userId)).thenReturn(Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, "Invalid state change")}));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.changeState(bookingId, BookingState.ACCEPTED, userId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.ERROR_INVALID_BOOKING_CHANGE_STATE, errors[0].message());
    }

    @Test
    @DisplayName("Change Booking State - Exception Thrown")
    void changeBookingStateException() {
        UUID bookingId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(bookingId)).thenThrow(new RuntimeException("Test exception"));

        Either<ErrorDto[], BookingResponseDTO> result = bookingService.changeState(bookingId, BookingState.ACCEPTED, userId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_BOOKING_CHANGE_STATE, errors[0].message());
        assertEquals("Test exception", errors[0].detail());
    }

    @Test
    @DisplayName("Change Booking State - Should Expire Eligible Bookings")
    void updateToExpiredBookingsShouldExpireEligibleBookings() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<BookingState> states = Arrays.asList(BookingState.CREATED, BookingState.PENDING);

        Booking booking1 = new Booking(LocalDate.now(), LocalDate.now().plusDays(2), 100.0, lodging, tourist, BookingState.CREATED, 2, 1, 1, false);
        Booking booking2 = new Booking(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), 100.0, lodging, tourist, BookingState.PENDING, 2, 1, 1, false);
        List<Booking> bookingsToExpire = Arrays.asList(booking1, booking2);

        when(repository.findByCheckInLessThanAndStateIn(tomorrow, states)).thenReturn(bookingsToExpire);

        bookingService.updateToExpiredBookings();

        verify(repository).findByCheckInLessThanAndStateIn(tomorrow, states);
        verify(repository).saveAll(bookingsToExpire);
        assertEquals(BookingState.EXPIRED, booking1.getState());
        assertEquals(BookingState.EXPIRED, booking2.getState());
    }

    @Test
    @DisplayName("Change Booking State - Should Not Expire Non Eligible Bookings")
    void updateToExpiredBookingsShouldNotExpireNonEligibleBookings() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<BookingState> states = Arrays.asList(BookingState.CREATED, BookingState.PENDING);

        Booking booking1 = new Booking(LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 100.0, lodging, tourist, BookingState.CREATED, 2, 1, 1, false);
        Booking booking2 = new Booking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5), 100.0, lodging, tourist, BookingState.PENDING, 2, 1, 1, false);

        when(repository.findByCheckInLessThanAndStateIn(tomorrow, states)).thenReturn(new ArrayList<>());

        bookingService.updateToExpiredBookings();

        verify(repository).findByCheckInLessThanAndStateIn(tomorrow, states);
        verify(repository).saveAll(new ArrayList<>());
        assertEquals(BookingState.CREATED, booking1.getState());
        assertEquals(BookingState.PENDING, booking2.getState());
    }

    @Test
    @DisplayName("Change Booking State - Should Handle Empty List")
    void updateToExpiredBookings_ShouldHandleEmptyList() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<BookingState> states = Arrays.asList(BookingState.CREATED, BookingState.PENDING);

        when(repository.findByCheckInLessThanAndStateIn(tomorrow, states)).thenReturn(new ArrayList<>());

        bookingService.updateToExpiredBookings();

        verify(repository).findByCheckInLessThanAndStateIn(tomorrow, states);
        verify(repository).saveAll(new ArrayList<>());
    }
}
