package com.tourism.service.impl;

import com.tourism.dto.request.BookingRequestDTO;
import com.tourism.dto.request.BookingUpdateRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.BookingResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.*;
import com.tourism.observer.BookingObserver;
import com.tourism.repository.*;
import com.tourism.service.BookingService;
import com.tourism.util.validations.DateValidation;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.BookingValidation;
import com.tourism.util.helpers.PricingService;
import io.vavr.control.Either;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository repository;
    private final TouristRepository touristRepository;
    private final LodgingRepository lodgingRepository;
    private final BookingValidation bookingValidation;
    private final DateValidation dateValidation;
    private final BookingDateRepository dateRepository;
    private final PageService pageService;
    private final PricingService pricingService;

    private final List<BookingObserver> observers = new ArrayList<>();


    @Autowired
    public BookingServiceImpl(BookingRepository repository, TouristRepository touristRepository,
                              LodgingRepository lodgingRepository, BookingValidation bookingValidation,
                              DateValidation dateValidation, BookingDateRepository dateRepository, PageService pageService,
                              PricingService pricingService) {
        this.repository = repository;
        this.touristRepository = touristRepository;
        this.lodgingRepository = lodgingRepository;
        this.bookingValidation = bookingValidation;
        this.dateValidation = dateValidation;
        this.dateRepository = dateRepository;
        this.pageService = pageService;
        this.pricingService = pricingService;
    }

    @Override
    @Transactional
    public Either<ErrorDto[], BookingResponseDTO> create(BookingRequestDTO bookingDto, UUID touristId) {
        try {
            bookingDto.setCheckOut(bookingDto.getCheckOut().minusDays(1));
            Tourist tourist = touristRepository.findById(touristId).orElse(null);
            Lodging lodging = lodgingRepository.findById(bookingDto.getLodging().getId()).orElse(null);
            Either<ErrorDto[], Boolean> validation = bookingValidation.validateBooking(bookingDto, tourist, lodging);

            if (validation.isRight()) {
                Either<ErrorDto[], Booking> booking = this.createBooking(bookingDto, lodging, tourist);
                if (booking.isRight()) {
                    notifyObservers(booking.get(), BookingState.CREATED);
                } else {
                    return Either.left(validation.getLeft());
                }
                return Either.right(BookingResponseDTO.bookingToResponseDTO(booking.get()));
            } else {
                return Either.left(validation.getLeft());
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_BOOKING_NOT_CREATED, e.getMessage())});
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.ERROR_BOOKING_DATES, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_BOOKING_NOT_CREATED, e.getMessage())});
        }
    }

    @Override
    @Transactional
    public Either<ErrorDto[], BookingResponseDTO> update(BookingUpdateRequestDTO bookingDto, UUID touristId) {
        try {
            bookingDto.setCheckOut(bookingDto.getCheckOut().minusDays(1));
            Tourist tourist = touristRepository.findById(touristId).orElse(null);
            Booking booking = repository.findById(bookingDto.getBookingId()).orElse(null);
            if (booking != null) {
                Lodging lodging = lodgingRepository.findById(booking.getLodging().getId()).orElse(null);
                BookingRequestDTO bookingRequest = bookingDto.transformRequestDTO(lodging, booking);
                Either<ErrorDto[], Boolean> validation = bookingValidation.validateBooking(bookingRequest, tourist, lodging);
                if (validation.isRight()) {
                    dateRepository.deleteByBooking(booking);
                    booking.setCheckIn(bookingDto.getCheckIn());
                    booking.setCheckOut(bookingDto.getCheckOut());
                    booking.setState(BookingState.CREATED);
                    repository.save(booking);

                    notifyObservers(booking, BookingState.CREATED);

                    List<LocalDate> bookingDays = dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut());
                    createBookingDates(bookingRequest, booking.getLodging(), bookingDays, booking);
                    return Either.right(BookingResponseDTO.bookingToResponseDTO(booking));
                } else {
                    return Either.left(validation.getLeft());
                }
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_BOOKING_NOT_FOUND)});
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.ERROR_BOOKING_NOT_UPDATED, e.getMessage())});
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.ERROR_BOOKING_DATES, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_BOOKING_NOT_UPDATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<BookingResponseDTO>> findAll(PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Booking> bookings = repository.findAll(pageable);
            return Either.right(bookings.map(BookingResponseDTO::bookingToResponseDTO));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.GENERIC_ERROR, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Booking> delete(UUID id) {
        try {
            Booking booking = repository.findById(id).orElse(null);
            repository.delete(Objects.requireNonNull(booking));
            return Either.right(null);
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_BOOKING_NOT_FOUND)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_DELETING_BOOKING, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], BookingResponseDTO> getById(UUID id) {
        try {
            Booking booking = repository.findById(id).orElse(null);
            if (booking != null) {
                return Either.right(BookingResponseDTO.bookingToResponseDTO(booking));
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID)});
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_BOOKING, e.getMessage())});
        }
    }

    @Override
    @Transactional
    public Either<ErrorDto[], BookingResponseDTO> changeState(UUID bookingId, BookingState newState, UUID userId) {
        try {
            Booking booking = repository.findById(bookingId).orElse(null);
            if (booking != null) {
                if (bookingValidation.validChangeState(booking, newState, userId).isRight()) {
                    notifyObservers(booking, newState);
                    if (newState.equals(BookingState.ACCEPTED)) {
                        booking.setHasPaid(true);
                    }
                    booking.setState(newState);
                    repository.save(booking);
                } else {
                    return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_INVALID_BOOKING_CHANGE_STATE)});
                }

                return Either.right(BookingResponseDTO.bookingToResponseDTO(booking));
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_BOOKING_NOT_FOUND)});
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_BOOKING_CHANGE_STATE, e.getMessage())});
        }
    }

    @Override
    @Transactional
    public void updateToExpiredBookings() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<BookingState> states = Arrays.asList(BookingState.CREATED, BookingState.PENDING);
        List<Booking> bookingsToExpire = repository.findByCheckInLessThanAndStateIn(tomorrow, states);

        for (Booking booking : bookingsToExpire) {
            booking.setState(BookingState.EXPIRED);
        }

        repository.saveAll(bookingsToExpire);
    }

    @Override
    public void addObserver(BookingObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(BookingObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Booking booking, BookingState state) {
        for (BookingObserver observer : observers) {
            observer.notifyStatusChange(booking, state);
        }
    }

    private Either<ErrorDto[], Booking> createBooking(BookingRequestDTO bookingDto, Lodging lodging, Tourist tourist) {
        List<LocalDate> bookingDays = dateValidation.datesBetweenDates(bookingDto.getCheckIn(), bookingDto.getCheckOut());
        Double bookingPrice = pricingService.calculateBookingPrice(tourist.getType(), lodging, bookingDays, bookingDto.getAdults(), bookingDto.getChildren(), bookingDto.getBabies());

        Booking booking = new Booking(
                bookingDto.getCheckIn(),
                bookingDto.getCheckOut(),
                bookingPrice,
                lodging,
                tourist,
                BookingState.CREATED,
                bookingDto.getAdults(),
                bookingDto.getChildren(),
                bookingDto.getBabies(),
                false
        );
        repository.save(booking);

        createBookingDates(bookingDto, lodging, bookingDays, booking);
        return Either.right(booking);
    }

    private void createBookingDates(BookingRequestDTO bookingDto, Lodging lodging, List<LocalDate> bookingDays, Booking booking) {
        for (LocalDate date : bookingDays) {
            BookingDate bookingDate = new BookingDate(
                    booking,
                    date,
                    pricingService.calculateBookingPrice(booking.getTourist().getType(), lodging, Collections.singletonList(date),
                            bookingDto.getAdults(), bookingDto.getChildren(), bookingDto.getBabies())
            );
            dateRepository.save(bookingDate);
        }
    }

}
