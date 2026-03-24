package com.tourism.test.service;

import com.tourism.dto.mappers.TouristMapper;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.*;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.TouristRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.impl.TouristServiceImpl;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.UserValidation;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TouristServiceTests {

    @Mock
    private TouristRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository tokenRepository;

    @Mock
    private PasswordEncryptionService encryptionService;

    @Mock
    private UserValidation userValidation;

    @Mock
    private PageService pageService;

    @Mock
    private TouristMapper mapper;

    @InjectMocks
    private TouristServiceImpl service;

    private TouristRequestDTO validRequestDTO;
    private Tourist tourist;
    private PageableRequest pageableRequest;
    private Pageable pageable;
    private TouristResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        validRequestDTO = new TouristRequestDTO("Turista", "Verano","tverano@email.com", "validPassword123");
        tourist = new Tourist("tverano@email.com", "validPassword123", "Turista", "Verano", Role.TOURIST, TouristType.STANDARD, true);
        pageableRequest = new PageableRequest(0, 10, new String[]{"email"}, Sort.Direction.ASC);
        pageable = mock(Pageable.class);
        responseDTO = new TouristResponseDTO(UUID.randomUUID(), tourist.getEmail(), tourist.getFirstName(), tourist.getLastName());
    }

    @Test
    @DisplayName("Create Tourist - Success")
    void createSuccess() {
        when(mapper.modelToResponseDto(any(Tourist.class))).thenReturn(responseDTO);
        when(userValidation.validateEmailAndPassword(validRequestDTO.getEmail(), validRequestDTO.getPassword())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(validRequestDTO.getPassword())).thenReturn("encryptedPassword");
        when(repository.save(any(Tourist.class))).thenAnswer(invocation -> {
            tourist = invocation.getArgument(0);
            tourist.setId(responseDTO.id());
            return tourist;
        });

        Either<ErrorDto[], TouristResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isRight());
        TouristResponseDTO responseDTO = result.get();
        assertNotNull(responseDTO);
        assertEquals(validRequestDTO.getEmail(), responseDTO.email());
        assertEquals(validRequestDTO.getFirstName(), responseDTO.firstName());
        assertEquals(validRequestDTO.getLastName(), responseDTO.lastName());
    }

    @Test
    @DisplayName("Create Tourist - Validation Failure")
    void createTouristValidationFailure() {
        ErrorDto[] validationErrors = {new ErrorDto(HttpStatus.BAD_REQUEST, "Validation error", "Invalid email or password")};
        when(userValidation.validateEmailAndPassword(anyString(), anyString())).thenReturn(Either.left(validationErrors));

        Either<ErrorDto[], TouristResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals("Validation error", errors[0].message());
    }

    @Test
    @DisplayName("Create Tourist - Data Integrity Violation")
    void createTouristDataIntegrityViolation() {
        when(userValidation.validateEmailAndPassword(anyString(), anyString())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(anyString())).thenReturn("encryptedPassword");
        when(repository.save(any(Tourist.class))).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        Either<ErrorDto[], TouristResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.CONFLICT, errors[0].code());
        assertEquals(MessageConstants.ERROR_TOURIST_NOT_CREATED, errors[0].message());
    }

    @Test
    @DisplayName("Create Tourist - Generic Exception")
    void createLodgingOwnerGenericException() {
        when(userValidation.validateEmailAndPassword(anyString(), anyString())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(anyString())).thenReturn("encryptedPassword");
        when(repository.save(any(Tourist.class))).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], TouristResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.ERROR_TOURIST_NOT_CREATED, errors[0].message());
    }

    @Test
    @DisplayName("Find All Tourist - Success")
    void findAllSuccess() {
        List<Tourist> tourists = Arrays.asList(
                tourist,
                new Tourist("tinvierno@email.com", "validPassword123", "Turista", "Invierno", Role.TOURIST, TouristType.STANDARD, true)
        );
        Page<Tourist> page = new PageImpl<>(tourists);
        TouristResponseDTO dto1 = new TouristResponseDTO(UUID.randomUUID(),tourists.getFirst().getEmail(), tourists.getFirst().getFirstName(), tourists.getFirst().getLastName());
        TouristResponseDTO dto2 = new TouristResponseDTO(UUID.randomUUID(),tourists.getLast().getEmail(), tourists.getLast().getFirstName(), tourists.getLast().getLastName());

        when(mapper.modelToResponseDto(tourists.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(tourists.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(page);
        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<TouristResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("tverano@email.com", resultPage.getContent().get(0).email());
        assertEquals("tinvierno@email.com", resultPage.getContent().get(1).email());
    }

    @Test
    @DisplayName("Find All Tourists - Empty Page")
    void findAllEmptyPage() {
        Page<Tourist> page = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(page);

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<TouristResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find All Tourists - Exception Thrown")
    void findAllException() {
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURISTS, errors[0].message());
        assertEquals("Database error", errors[0].detail());
    }

    @Test
    @DisplayName("Delete Tourist - Success")
    void deleteTouristSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User();

        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(repository.findById(id)).thenReturn(Optional.of(tourist));

        Either<ErrorDto[], Tourist> result = service.delete(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(tokenRepository).deleteByUser(user);
        verify(repository).delete(tourist);
    }

    @Test
    @DisplayName("Delete Tourist - Invalid Id")
    void deleteTouristInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], Tourist> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_DELETING_TOURIST, errors[0].message());
    }

    @Test
    @DisplayName("Delete Tourist - Unexpected Exception")
    void deleteTouristUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Tourist> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURIST, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }

    @Test
    @DisplayName("Get Tourist By Id - Success")
    void getByIdSuccess() {
        when(repository.findById(responseDTO.id())).thenReturn(Optional.of(tourist));
        when(mapper.modelToResponseDto(any(Tourist.class))).thenReturn(responseDTO);

        Either<ErrorDto[], TouristResponseDTO> result = service.getById(responseDTO.id());

        assertTrue(result.isRight());
        TouristResponseDTO adminResponseDTO = result.get();
        assertNotNull(adminResponseDTO);
        assertEquals(responseDTO.id(), adminResponseDTO.id());
        assertEquals(responseDTO.email(), adminResponseDTO.email());
    }

    @Test
    @DisplayName("Get Tourist By Id - Not Found")
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Either<ErrorDto[], TouristResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURIST, errors[0].message());
    }

    @Test
    @DisplayName("Get Tourist By Id - Invalid Id")
    void getByIdInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], TouristResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_ID, errors[0].message());
    }

    @Test
    @DisplayName("Get Tourist By ID - Unexpected Exception")
    void getByIdUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], TouristResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURIST, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }

    @Test
    @DisplayName("Find Tourist By Email - Success")
    void findByEmailSuccess() {
        String email = "owner";
        List<Tourist> tourists = Arrays.asList(
                tourist,
                new Tourist("tinvierno@email.com", "validPassword123", "Turista", "Invierno", Role.TOURIST, TouristType.STANDARD, true)
        );
        Page<Tourist> page = new PageImpl<>(tourists);
        TouristResponseDTO dto1 = new TouristResponseDTO(UUID.randomUUID(),tourists.getFirst().getEmail(), tourists.getFirst().getFirstName(), tourists.getFirst().getLastName());
        TouristResponseDTO dto2 = new TouristResponseDTO(UUID.randomUUID(),tourists.getLast().getEmail(), tourists.getFirst().getFirstName(), tourists.getFirst().getLastName());

        when(mapper.modelToResponseDto(tourists.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(tourists.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable)).thenReturn(page);

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isRight());
        Page<TouristResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals(dto1.email(), resultPage.getContent().get(0).email());
        assertEquals(dto2.email(), resultPage.getContent().get(1).email());
    }

    @Test
    @DisplayName("Find Tourist By Email - Empty Result")
    void findByEmailEmptyResult() {
        String email = "nonexistent";
        Page<Tourist> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isRight());
        Page<TouristResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find Tourist By Email - Invalid Data Access")
    void findByEmailInvalidDataAccess() {
        String email = "tverano";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable))
                .thenThrow(new InvalidDataAccessApiUsageException("Invalid data access"));

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_EMAIL, errors[0].message());
    }

    @Test
    @DisplayName("Find Tourist By Email - Unexpected Exception")
    void findByEmailUnexpectedException() {
        String email = "tverano";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURISTS, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }

    @Test
    @DisplayName("Find Tourist By Last Name - Success")
    void findByLastNameSuccess() {
        String lastName = "verano";
        List<Tourist> tourists = Arrays.asList(
                tourist,
                new Tourist("verano@email.com", "validPassword123", "Turista", "Verano Soleado", Role.TOURIST, TouristType.STANDARD, true)
        );
        Page<Tourist> page = new PageImpl<>(tourists);
        TouristResponseDTO dto1 = new TouristResponseDTO(UUID.randomUUID(),tourists.getFirst().getEmail(), tourists.getFirst().getFirstName(), tourists.getFirst().getLastName());
        TouristResponseDTO dto2 = new TouristResponseDTO(UUID.randomUUID(),tourists.getLast().getEmail(), tourists.getFirst().getFirstName(), tourists.getFirst().getLastName());

        when(mapper.modelToResponseDto(tourists.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(tourists.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable)).thenReturn(page);

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isRight());
        Page<TouristResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals(dto1.lastName(), resultPage.getContent().get(0).lastName());
        assertEquals(dto2.lastName(), resultPage.getContent().get(1).lastName());
    }

    @Test
    @DisplayName("Find Tourist By Last Name - Empty Result")
    void findByLastNameEmptyResult() {
        String lastName = "nonexistent";
        Page<Tourist> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isRight());
        Page<TouristResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find Tourist By Last Name - Invalid Data Access")
    void findByLastNameInvalidDataAccess() {
        String lastName = "verano";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable))
                .thenThrow(new InvalidDataAccessApiUsageException("Invalid data access"));

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_LAST_NAME, errors[0].message());
    }

    @Test
    @DisplayName("Find Tourist By Last Name - Unexpected Exception")
    void findByLastNameUnexpectedException() {
        String lastName = "owner";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<TouristResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURIST, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }
}
