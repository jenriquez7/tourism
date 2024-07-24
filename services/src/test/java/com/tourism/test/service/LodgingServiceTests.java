package com.tourism.test.service;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.model.Role;
import com.tourism.model.*;
import com.tourism.repository.BookingDateRepository;
import com.tourism.repository.LodgingOwnerRepository;
import com.tourism.repository.LodgingRepository;
import com.tourism.repository.TouristicPlaceRepository;
import com.tourism.service.impl.LodgingServiceImpl;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.BookingValidation;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LodgingServiceTests {

    @Mock
    private LodgingRepository repository;

    @Mock
    private TouristicPlaceRepository placeRepository;

    @Mock
    private LodgingOwnerRepository ownerRepository;

    @Mock
    private BookingValidation bookingValidation;

    @Mock
    private BookingDateRepository bookingDateRepository;

    @Mock
    private PageService pageService;

    @InjectMocks
    private LodgingServiceImpl service;

    private Lodging lodging;
    private TouristicPlace place;
    private LodgingOwner owner;
    private PageableRequest pageableRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        owner = new LodgingOwner("owner@email.com", "validPassword123", "Owner", "Hotel", Role.LODGING_OWNER, true);
        place = new TouristicPlace("Punta del Este", "Hermoso lugar", Region.EAST, new User(), true);
        place.setCategories(new ArrayList<>());
        lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Calle falsa 123", "+59899123456", 20, 25.0, 4, place, owner, true);
        pageableRequest = new PageableRequest(0, 10, new String[]{"email"}, Sort.Direction.ASC);
        pageable = mock(Pageable.class);
    }

    @Test
    @DisplayName("Create Lodging - Success")
    void createLodgingSuccess() {
        UUID ownerId = UUID.randomUUID();
        when(placeRepository.findById(place.getId())).thenReturn(Optional.of(place));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(repository.save(any(Lodging.class))).thenReturn(lodging);

        Either<ErrorDto[], LodgingResponseDTO> result = service.create(lodging, ownerId);

        assertTrue(result.isRight());
        LodgingResponseDTO responseDTO = result.get();
        assertEquals(lodging.getName(), responseDTO.getName());
        assertEquals(lodging.getDescription(), responseDTO.getDescription());
        assertEquals(lodging.getInformation(), responseDTO.getInformation());
        assertEquals(lodging.getPhone(), responseDTO.getPhone());
        assertEquals(lodging.getCapacity(), responseDTO.getCapacity());
        assertEquals(lodging.getNightPrice(), responseDTO.getNightPrice());
        assertEquals(lodging.getStars(), responseDTO.getStars());

        verify(placeRepository).findById(place.getId());
        verify(ownerRepository).findById(ownerId);
        verify(repository).save(any(Lodging.class));
    }

    @Test
    @DisplayName("Create Lodging - TouristicPlace Not Found")
    void createLodgingTouristicPlaceNotFound() {
        UUID ownerId = UUID.randomUUID();
        when(placeRepository.findById(place.getId())).thenReturn(Optional.empty());

        Either<ErrorDto[], LodgingResponseDTO> result = service.create(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_NOT_CREATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Create Lodging - LodgingOwner Not Found")
    void createLodgingOwnerNotFound() {
        UUID ownerId = UUID.randomUUID();
        when(placeRepository.findById(place.getId())).thenReturn(Optional.of(place));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.empty());

        Either<ErrorDto[], LodgingResponseDTO> result = service.create(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_NOT_CREATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Create Lodging - DataIntegrityViolationException")
    void createLodgingDataIntegrityViolation() {
        UUID ownerId = UUID.randomUUID();
        when(placeRepository.findById(place.getId())).thenReturn(Optional.of(place));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(repository.save(any(Lodging.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        Either<ErrorDto[], LodgingResponseDTO> result = service.create(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_NOT_CREATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Update Lodging - Success")
    void updateLodgingSuccess() {
        UUID ownerId = UUID.randomUUID();
        lodging.setId(UUID.randomUUID());

        when(repository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingDateRepository.findLastBookingDateByLodgingAndState(lodging, BookingState.ACCEPTED)).thenReturn(null);
        when(placeRepository.findById(place.getId())).thenReturn(Optional.of(place));
        when(repository.save(any(Lodging.class))).thenReturn(lodging);

        Either<ErrorDto[], LodgingResponseDTO> result = service.update(lodging, ownerId);

        assertTrue(result.isRight());
        LodgingResponseDTO responseDTO = result.get();
        assertEquals(lodging.getName(), responseDTO.getName());

        verify(repository).findById(lodging.getId());
        verify(ownerRepository).findById(ownerId);
        verify(bookingDateRepository).findLastBookingDateByLodgingAndState(lodging, BookingState.ACCEPTED);
        verify(placeRepository).findById(place.getId());
        verify(repository).save(any(Lodging.class));
    }

    @Test
    @DisplayName("Update Lodging - Lodging Not Found")
    void updateLodgingNotFound() {
        UUID ownerId = UUID.randomUUID();
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Either<ErrorDto[], LodgingResponseDTO> result = service.update(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_LODGING_OWNER, errors[0].getMessage());
    }

    @Test
    @DisplayName("Update Lodging - Owner Mismatch")
    void updateLodgingOwnerMismatch() {
        UUID ownerId = UUID.randomUUID();
        lodging.setId(UUID.randomUUID());
        lodging.setLodgingOwner(new LodgingOwner());

        when(repository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        Either<ErrorDto[], LodgingResponseDTO> result = service.update(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_LODGING_OWNER, errors[0].getMessage());
    }

    @Test
    @DisplayName("Update Lodging - Invalid Capacity")
    void updateLodgingInvalidCapacity() {
        UUID ownerId = UUID.randomUUID();
        lodging.setId(UUID.randomUUID());
        lodging.setLodgingOwner(owner);

        when(repository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingDateRepository.findLastBookingDateByLodgingAndState(lodging, BookingState.ACCEPTED)).thenReturn(LocalDate.now().plusDays(30));
        when(bookingValidation.invalidLodgingCapacityVsBookings(anyInt(), anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class), any(Lodging.class))).thenReturn(true);

        Either<ErrorDto[], LodgingResponseDTO> result = service.update(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_FULL_CAPACITY, errors[0].getMessage());
    }

    @Test
    @DisplayName("Update Lodging - DataIntegrityViolationException")
    void updateLodgingDataIntegrityViolation() {
        UUID ownerId = UUID.randomUUID();
        lodging.setId(UUID.randomUUID());
        lodging.setLodgingOwner(owner);

        when(repository.findById(lodging.getId())).thenReturn(Optional.of(lodging));
        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingDateRepository.findLastBookingDateByLodgingAndState(lodging, BookingState.ACCEPTED)).thenReturn(null);
        when(placeRepository.findById(place.getId())).thenReturn(Optional.of(place));
        when(repository.save(any(Lodging.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        Either<ErrorDto[], LodgingResponseDTO> result = service.update(lodging, ownerId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_NOT_UPDATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find All Lodgings - Success")
    void findAllLodgingsSuccess() {
        List<Lodging> lodgingList = Arrays.asList(lodging, new Lodging("Hotel 2", "Descripción 2", "Dirección 2", "+59899123457", 30, 35.0, 3, place, owner, true));
        Page<Lodging> lodgingPage = new PageImpl<>(lodgingList, pageable, lodgingList.size());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(lodgingPage);

        Either<ErrorDto[], Page<LodgingResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingResponseDTO> responsePage = result.get();
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals(2, responsePage.getContent().size());
        assertEquals(lodging.getName(), responsePage.getContent().getFirst().getName());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Find All Lodgings - Empty Page")
    void findAllLodgingsEmptyPage() {
        Page<Lodging> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<LodgingResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingResponseDTO> responsePage = result.get();
        assertEquals(0, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertTrue(responsePage.getContent().isEmpty());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Find All Lodgings - Exception")
    void findAllLodgingsException() {
        when(pageService.createSortedPageable(pageableRequest)).thenThrow(new RuntimeException("Error creating pageable"));

        Either<ErrorDto[], Page<LodgingResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_LODGINGS, errors[0].getMessage());
        assertEquals("Error creating pageable", errors[0].getDetail());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Delete Lodging - Success")
    void deleteLodgingSuccess() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(lodging));
        doNothing().when(repository).delete(lodging);

        Either<ErrorDto[], Lodging> result = service.delete(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(repository).findById(id);
        verify(repository).delete(lodging);
    }


    @Test
    @DisplayName("Delete Lodging - InvalidDataAccessApiUsageException")
    void deleteLodgingInvalidDataAccessApiUsageException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(lodging));
        doThrow(new InvalidDataAccessApiUsageException("Invalid data access")).when(repository).delete(lodging);

        Either<ErrorDto[], Lodging> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_DELETING_LODGING, errors[0].getMessage());
        verify(repository).findById(id);
        verify(repository).delete(lodging);
    }

    @Test
    @DisplayName("Get Lodging By Id - Success")
    void getLodgingByIdSuccess() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(lodging));

        Either<ErrorDto[], LodgingResponseDTO> result = service.getById(id);

        assertTrue(result.isRight());
        LodgingResponseDTO responseDTO = result.get();
        assertEquals(lodging.getName(), responseDTO.getName());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Get Lodging By Id - Not Found")
    void getLodgingByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Either<ErrorDto[], LodgingResponseDTO> result = service.getById(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Get Lodging By Id - InvalidDataAccessApiUsageException")
    void getLodgingByIdInvalidDataAccessApiUsageException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid data access"));

        Either<ErrorDto[], LodgingResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.NULL_ID, errors[0].getMessage());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Find Lodgings By Touristic Place - Success")
    void findLodgingsByTouristicPlaceSuccess() {
        UUID placeId = UUID.randomUUID();
        List<Lodging> lodgingList = Arrays.asList(lodging, new Lodging("Hotel 2", "Descripción 2", "Dirección 2", "+59899123457", 30, 35.0, 3, place, owner, true));
        Page<Lodging> lodgingPage = new PageImpl<>(lodgingList, pageable, lodgingList.size());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));
        when(repository.findByTouristicPlace(place, pageable)).thenReturn(lodgingPage);

        Either<ErrorDto[], Page<LodgingResponseDTO>> result = service.findLodgingsByTouristicPlace(placeId, pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingResponseDTO> responsePage = result.get();
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals(2, responsePage.getContent().size());
        assertEquals(lodging.getName(), responsePage.getContent().getFirst().getName());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(placeRepository).findById(placeId);
        verify(repository).findByTouristicPlace(place, pageable);
    }

    @Test
    @DisplayName("Find Lodgings By Touristic Place - Place Not Found")
    void findLodgingsByTouristicPlacePlaceNotFound() {
        UUID placeId = UUID.randomUUID();

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(placeRepository.findById(placeId)).thenReturn(Optional.empty());

        Either<ErrorDto[], Page<LodgingResponseDTO>> result = service.findLodgingsByTouristicPlace(placeId, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_LODGINGS, errors[0].getMessage());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(placeRepository).findById(placeId);
    }
}
