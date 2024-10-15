package com.tourism.service.impl;

import com.tourism.dto.mappers.LodgingMapper;
import com.tourism.dto.mappers.TouristicPlaceMapper;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.model.*;
import com.tourism.repository.*;
import com.tourism.service.LodgingService;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.BookingValidation;
import io.vavr.control.Either;
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
public class LodgingServiceImpl implements LodgingService {

    private final LodgingRepository repository;
    private final TouristicPlaceRepository placeRepository;
    private final LodgingOwnerRepository ownerRepository;
    private final BookingValidation bookingValidation;
    private final BookingDateRepository bookingDateRepository;
    private final PageService pageService;
    private final LodgingMapper mapper;

    @Autowired
    public LodgingServiceImpl(LodgingRepository repository,
                              TouristicPlaceRepository placeRepository,
                              LodgingOwnerRepository ownerRepository,
                              BookingValidation bookingValidation,
                              BookingDateRepository bookingDateRepository,
                              PageService pageService,
                              LodgingMapper mapper) {
        this.repository = repository;
        this.placeRepository = placeRepository;
        this.ownerRepository = ownerRepository;
        this.bookingValidation = bookingValidation;
        this.bookingDateRepository = bookingDateRepository;
        this.pageService = pageService;
        this.mapper = mapper;
    }


    @Override
    public Either<ErrorDto[], LodgingResponseDTO> create(Lodging lodging, UUID ownerId) {
        try {
            TouristicPlace place = placeRepository.findById(lodging.getTouristicPlace().getId()).orElse(null);
            LodgingOwner lodgingOwner = ownerRepository.findById(ownerId).orElse(null);
            lodging.setTouristicPlace(Objects.requireNonNull(place));
            lodging.setLodgingOwner(Objects.requireNonNull(lodgingOwner));
            lodging.setEnabled(true);
            return Either.right(mapper.modelToResponseDto(repository.save(lodging)));
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_LODGING_NOT_CREATED, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_LODGING_NOT_CREATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], LodgingResponseDTO> update(Lodging lodgingDTO, UUID ownerId) {
        try {
            Lodging lodging = repository.findById(lodgingDTO.getId()).orElse(null);
            LodgingOwner lodgingOwner = ownerRepository.findById(ownerId).orElse(null);

            if (lodging != null && lodging.getLodgingOwner().equals(lodgingOwner)) {
                LocalDate lastBookingDate = bookingDateRepository.findLastBookingDateByLodgingAndState(lodging, BookingState.ACCEPTED);
                if (lastBookingDate != null && bookingValidation.invalidLodgingCapacityVsBookings(0, 0, 0, LocalDate.now(), lastBookingDate, lodgingDTO)) {
                    return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_FULL_CAPACITY)});
                }

                TouristicPlace place = placeRepository.findById(lodging.getTouristicPlace().getId()).orElse(null);
                lodging.updateLodgingFromDTO(lodgingDTO, place);
                return Either.right(mapper.modelToResponseDto(repository.save(lodging)));
            } else {
                return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_LODGING_LODGING_OWNER)});
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_LODGING_NOT_UPDATED, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_LODGING_NOT_UPDATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<LodgingResponseDTO>> findAll(PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Lodging> lodgings = repository.findAll(pageable);
            return Either.right(lodgings.map(mapper::modelToResponseDto));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_LODGINGS, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Lodging> delete(UUID id) {
        try {
            Lodging lodging = repository.findById(id).orElse(null);
            repository.delete(Objects.requireNonNull(lodging));
            return Either.right(null);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.ERROR_DELETING_TOURISTIC_PLACE, "Touristic Place not found")});
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.ERROR_DELETING_LODGING, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_DELETING_LODGING, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], LodgingResponseDTO> getById(UUID id) {
        try {
            Lodging lodging = repository.findById(id).orElse(null);
            return Either.right(lodging != null ? mapper.modelToResponseDto(lodging) : null);
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_LODGINGS, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<LodgingResponseDTO>> findLodgingsByTouristicPlace(UUID id, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            TouristicPlace place = placeRepository.findById(id).orElse(null);
            Page<Lodging> lodgings = repository.findByTouristicPlace(place, pageable);
            return Either.right(lodgings.map(mapper::modelToResponseDto));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_GET_LODGINGS, e.getMessage())});
        }
    }
}
