package com.tourism.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.tourism.dto.mappers.BookingMapper;
import com.tourism.dto.request.BookingMessage;
import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Booking;
import com.tourism.model.BookingDate;
import com.tourism.model.BookingState;
import com.tourism.model.Lodging;
import com.tourism.model.LodgingOwner;
import com.tourism.model.Tourist;
import com.tourism.model.TouristType;
import com.tourism.model.TouristicPlace;
import com.tourism.repository.BookingDateRepository;
import com.tourism.repository.BookingRepository;
import com.tourism.repository.LodgingRepository;
import com.tourism.repository.TouristRepository;
import com.tourism.service.BookingSendingQueueService;
import com.tourism.service.impl.BookingServiceImpl;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.helpers.PricingService;
import com.tourism.util.validations.BookingValidation;
import com.tourism.util.validations.DateValidation;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

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
   private BookingMapper mapper;

   @Mock
   private BookingSendingQueueService queueService;

   @Mock
   private ApplicationEventPublisher eventPublisher;

   @InjectMocks
   private BookingServiceImpl bookingService;

   private BookingRequestDTO requestDto;

   private Tourist tourist;

   private Lodging lodging;

   private BookingUpdateRequestDTO updateDto;

   private Booking existingBooking;

   private List<LocalDate> mockDates;

   private PageableRequest pageableRequest;

   private Pageable pageable;

   private Page<Booking> bookingPage;

   private BookingResponseDTO responseDTO;

   private BookingMessage bookingMessage;

   @BeforeEach
   void setUp() {
      String key = "key";
      LocalDate checkIn = LocalDate.now();
      LocalDate checkOut = LocalDate.now().plusDays(3L);
      tourist = new Tourist("tverano@email.com", "12345678", "Turista", "Verano", TouristType.STANDARD, true);
      LodgingOwner owner = new LodgingOwner("owner@email.com", "validPassword123", "Owner", "Hotel", true);
      lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Parada 5, playa mansa", "+5984422112233", 50, 25.0, 5, new TouristicPlace(), owner,
            true, true);
      tourist.setId(UUID.randomUUID());
      lodging.setId(UUID.randomUUID());
      requestDto = new BookingRequestDTO(checkIn, checkOut, lodging.getId(), 2, 1, 1);
      updateDto = new BookingUpdateRequestDTO(UUID.randomUUID(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(4));
      existingBooking = new Booking(checkIn, checkOut, 100.0, lodging, tourist, BookingState.CREATED, 2, 1, 1, false, key);
      existingBooking.setId(updateDto.bookingId());
      pageableRequest = new PageableRequest(0, 10, new String[] { "email" }, Sort.Direction.ASC);
      pageable = mock(Pageable.class);
      bookingPage = new PageImpl<>(Arrays.asList(new Booking(), new Booking()));
      responseDTO = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(),
            requestDto.checkIn(), requestDto.checkOut(), existingBooking.getTotalPrice(), lodging.getPhone(), lodging.getInformation(),
            existingBooking.getState());
      mockDates = Arrays.asList(requestDto.checkIn(), requestDto.checkOut().plusDays(1));
      bookingMessage = new BookingMessage(requestDto, tourist.getId(), key);
   }

   @Test
   @DisplayName("Create Booking - Success")
   void createBookingSuccess() {
      BookingResponseDTO dto = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(),
            requestDto.checkIn(), requestDto.checkOut(), existingBooking.getTotalPrice(), lodging.getPhone(), lodging.getInformation(),
            existingBooking.getState());
      when(dateValidation.datesBetweenDates(requestDto.checkIn(), requestDto.checkOut())).thenReturn(mockDates);
      when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
      when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
      when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
      when(mapper.modelToResponseDTO(any(Booking.class))).thenReturn(dto);
      doNothing().when(queueService).sendMessage(any(BookingRequestDTO.class), any(UUID.class), any(String.class));
      when(repository.save(any())).thenAnswer(b -> {
         Booking savedBooking = b.getArgument(0);
         savedBooking.setId(UUID.randomUUID());
         return savedBooking;
      });

      Either<ErrorDto[], String> result = bookingService.create(requestDto, tourist.getId());

      assertTrue(result.isRight());
      String response = result.get();
      assertNotNull(response);
      assertEquals(MessageConstants.BOOKING_IS_BEING_PROCESSED, response);
   }

   @Test
   @DisplayName("Create Booking - Fails")
   void createBookingValidationFails() {
      when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
      when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
      when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(
            Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, "Validation failed") }));

      Either<ErrorDto[], String> result = bookingService.create(requestDto, tourist.getId());

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

      Either<ErrorDto[], String> result = bookingService.create(requestDto, tourist.getId());

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

      Either<ErrorDto[], String> result = bookingService.create(requestDto, tourist.getId());

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertTrue(errors[0].message().contains(MessageConstants.ERROR_BOOKING_NOT_CREATED));
   }

   @Test
   @DisplayName("Create Booking - Success")
   void processBookingSuccess() {
      when(touristRepository.findById(any())).thenReturn(Optional.of(tourist));
      when(lodgingRepository.findById(any())).thenReturn(Optional.of(lodging));
      when(bookingValidation.validLodgingCapacityVsBookings(anyInt(), anyInt(), anyInt(), any(), any(), any())).thenReturn(true);

      when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
      when(pricingService.calculateBookingPrice(any(), any(), any(), anyInt(), anyInt(), anyInt())).thenReturn(100.0);
      when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

      bookingService.processBooking(bookingMessage);

      verify(repository).save(argThat(booking -> booking.getState() == BookingState.PENDING_PAYMENT && booking.getTourist().equals(tourist) && booking
            .getLodging()
            .equals(lodging)));
   }

   @Test
   @DisplayName("Create Booking - Unavailable")
   void processBookingUnavailable() {
      when(touristRepository.findById(any())).thenReturn(Optional.of(tourist));
      when(lodgingRepository.findById(any())).thenReturn(Optional.of(lodging));
      when(bookingValidation.validLodgingCapacityVsBookings(anyInt(), anyInt(), anyInt(), any(), any(), any())).thenReturn(false);
      when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

      bookingService.processBooking(bookingMessage);

      verify(repository).save(argThat(booking -> booking.getState() == BookingState.UNAVAILABLE && booking.getTourist().equals(tourist) && booking
            .getLodging()
            .equals(lodging)));
      verify(dateRepository, never()).save(any(BookingDate.class));
   }

   @Test
   @DisplayName("Create Booking - Tourist Not Found")
   void processBookingTouristNotFound() {
      when(touristRepository.findById(any())).thenReturn(Optional.empty());
      assertThrows(NullPointerException.class, () -> bookingService.processBooking(bookingMessage));
   }

   @Test
   @DisplayName("Update Booking - Success")
   void updateBookingSuccess() {
      BookingResponseDTO dto = new BookingResponseDTO(existingBooking.getId(), lodging.getName(), tourist.getFirstName(), tourist.getLastName(),
            requestDto.checkIn(), requestDto.checkOut(), existingBooking.getTotalPrice(), lodging.getPhone(), lodging.getInformation(),
            BookingState.CREATED);
      when(dateValidation.datesBetweenDates(requestDto.checkIn(), requestDto.checkOut())).thenReturn(mockDates);
      doNothing().when(dateRepository).deleteByBooking(existingBooking);
      when(touristRepository.findById(tourist.getId())).thenReturn(Optional.of(tourist));
      when(repository.findById(updateDto.bookingId())).thenReturn(Optional.of(existingBooking));
      when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
      when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(Either.right(true));
      when(mapper.modelToResponseDTO(any(Booking.class))).thenReturn(dto);
      doNothing().when(queueService).sendMessage(any(BookingRequestDTO.class), any(UUID.class), any(String.class));

      Either<ErrorDto[], String> result = bookingService.update(updateDto, tourist.getId());

      assertTrue(result.isRight());
      String response = result.get();
      assertNotNull(response);
      assertEquals(MessageConstants.BOOKING_IS_BEING_PROCESSED, response);
   }

   @Test
   @DisplayName("Update Booking - Booking Not Found")
   void updateBookingNotFound() {
      when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
      when(repository.findById(updateDto.bookingId())).thenReturn(Optional.empty());

      Either<ErrorDto[], String> result = bookingService.update(updateDto, tourist.getId());

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
      when(repository.findById(updateDto.bookingId())).thenReturn(Optional.of(existingBooking));
      when(lodgingRepository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
      when(bookingValidation.validateBooking(any(), any(), any())).thenReturn(
            Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, "Validation failed") }));

      Either<ErrorDto[], String> result = bookingService.update(updateDto, tourist.getId());

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals("Validation failed", errors[0].message());
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
      when(mapper.modelToResponseDTO(any(Booking.class))).thenReturn(responseDTO);

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
      when(mapper.modelToResponseDTO(any(Booking.class))).thenReturn(responseDTO);

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
            requestDto.checkIn(), requestDto.checkOut(), existingBooking.getTotalPrice(), lodging.getPhone(), lodging.getInformation(),
            BookingState.ACCEPTED);
      UUID bookingId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      when(repository.findById(bookingId)).thenReturn(Optional.of(existingBooking));
      when(bookingValidation.validChangeState(existingBooking, BookingState.ACCEPTED, userId)).thenReturn(Either.right(true));
      when(repository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
      when(pricingService.calculateBookingPrice(eq(TouristType.STANDARD), any(), any(), anyInt(), anyInt(), anyInt())).thenReturn(100.0);
      when(dateValidation.datesBetweenDates(any(), any())).thenReturn(mockDates);
      when(mapper.modelToResponseDTO(any(Booking.class))).thenReturn(dto);

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
      when(bookingValidation.validChangeState(existingBooking, BookingState.ACCEPTED, userId)).thenReturn(
            Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, "Invalid state change") }));

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
   @DisplayName("Change Booking State - Should Not Expire Non Eligible Bookings")
   void updateToExpiredBookingsShouldNotExpireNonEligibleBookings() {
      LocalDate tomorrow = LocalDate.now().plusDays(1);
      List<BookingState> states = Arrays.asList(BookingState.CREATED, BookingState.PENDING_PAYMENT);

      Booking booking1 = new Booking(LocalDate.now().plusDays(2), LocalDate.now().plusDays(4), 100.0, lodging, tourist, BookingState.CREATED, 2, 1, 1,
            false, "key");
      Booking booking2 = new Booking(LocalDate.now().plusDays(3), LocalDate.now().plusDays(5), 100.0, lodging, tourist, BookingState.PENDING_PAYMENT,
            2, 1, 1, false, "key2");

      bookingService.updateToExpiredBookings();

      verify(repository).expireBookingsAutomatic(tomorrow, states);
      assertEquals(BookingState.CREATED, booking1.getState());
      assertEquals(BookingState.PENDING_PAYMENT, booking2.getState());
   }

   @Test
   @DisplayName("Change Booking State - Should Handle Empty List")
   void updateToExpiredBookings_ShouldHandleEmptyList() {
      LocalDate tomorrow = LocalDate.now().plusDays(1);
      List<BookingState> states = Arrays.asList(BookingState.CREATED, BookingState.PENDING_PAYMENT);

      bookingService.updateToExpiredBookings();

      verify(repository).expireBookingsAutomatic(tomorrow, states);
   }

}
