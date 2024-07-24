package com.tourism.test.service;

import com.tourism.dto.request.LodgingOwnerRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Role;
import com.tourism.model.LodgingOwner;
import com.tourism.model.User;
import com.tourism.repository.LodgingOwnerRepository;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.impl.LodgingOwnerServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LodgingOwnerServiceTests {

    @Mock
    private LodgingOwnerRepository repository;

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

    @InjectMocks
    private LodgingOwnerServiceImpl service;

    private LodgingOwnerRequestDTO validRequestDTO;
    private LodgingOwner owner;
    private PageableRequest pageableRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        validRequestDTO = new LodgingOwnerRequestDTO("Owner", "Hotel","owner@email.com", "validPassword123");
        owner = new LodgingOwner("owner@email.com", "validPassword123", "Owner", "Hotel", Role.LODGING_OWNER, true);
        pageableRequest = new PageableRequest(0, 10, new String[]{"email"}, Sort.Direction.ASC);
        pageable = mock(Pageable.class);
    }

    @Test
    @DisplayName("Create Lodging Owner - Success")
    void createLodgingOwnerSuccess() {
        UUID id = UUID.randomUUID();
        when(userValidation.validateEmailAndPassword(validRequestDTO.getEmail(), validRequestDTO.getPassword())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(validRequestDTO.getPassword())).thenReturn("encryptedPassword");
        when(repository.save(any(LodgingOwner.class))).thenAnswer(invocation -> {
            owner = invocation.getArgument(0);
            owner.setId(id);
            return owner;
        });

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isRight());
        LodgingOwnerResponseDTO responseDTO = result.get();
        assertNotNull(responseDTO);
        assertEquals(id, responseDTO.getId());
        assertEquals(validRequestDTO.getEmail(), responseDTO.getEmail());
        assertEquals(validRequestDTO.getFirstName(), responseDTO.getFirstName());
        assertEquals(validRequestDTO.getLastName(), responseDTO.getLastName());
    }

    @Test
    @DisplayName("Create Lodging Owner - Validation Failure")
    void createLodgingOwnerValidationFailure() {
        ErrorDto[] validationErrors = {new ErrorDto(HttpStatus.BAD_REQUEST, "Validation error", "Invalid email or password")};
        when(userValidation.validateEmailAndPassword(anyString(), anyString())).thenReturn(Either.left(validationErrors));

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals("Validation error", errors[0].getMessage());
    }

    @Test
    @DisplayName("Create Lodging Owner - Data Integrity Violation")
    void createLodgingOwnerDataIntegrityViolation() {
        when(userValidation.validateEmailAndPassword(anyString(), anyString())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(anyString())).thenReturn("encryptedPassword");
        when(repository.save(any(LodgingOwner.class))).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.CONFLICT, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_OWNER_NOT_CREATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Create Lodging Owner - Generic Exception")
    void createLodgingOwnerGenericException() {
        when(userValidation.validateEmailAndPassword(anyString(), anyString())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(anyString())).thenReturn("encryptedPassword");
        when(repository.save(any(LodgingOwner.class))).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.create(validRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_LODGING_OWNER_NOT_CREATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find All Lodging Owners - Success")
    void findAllSuccess() {
        List<LodgingOwner> owners = Arrays.asList(
                owner,
                new LodgingOwner("owner2@email.com", "validPassword123", "Owner2", "Hotel2", Role.LODGING_OWNER, true)
        );
        Page<LodgingOwner> page = new PageImpl<>(owners);

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(page);
        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingOwnerResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("owner@email.com", resultPage.getContent().get(0).getEmail());
        assertEquals("owner2@email.com", resultPage.getContent().get(1).getEmail());
    }

    @Test
    @DisplayName("Find All Lodging Owners - Empty Page")
    void findAllEmptyPage() {
        Page<LodgingOwner> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingOwnerResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find All Lodging Owners - Exception Thrown")
    void findAllException() {
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.GENERIC_ERROR, errors[0].getMessage());
        assertEquals("Database error", errors[0].getDetail());
    }

    @Test
    @DisplayName("Delete Lodging Owner - Success")
    void deleteLodgingOwnerSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User();

        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(repository.findById(id)).thenReturn(Optional.of(owner));

        Either<ErrorDto[], LodgingOwner> result = service.delete(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(tokenRepository).deleteByUser(user);
        verify(repository).delete(owner);
    }

    @Test
    @DisplayName("Delete Lodging Owner - Invalid Id")
    void deleteLodgingOwnerInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], LodgingOwner> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_DELETING_LODGING_OWNER, errors[0].getMessage());
    }

    @Test
    @DisplayName("Delete Lodging Owner - Unexpected Exception")
    void deleteLodgingOwnerUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], LodgingOwner> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_DELETING_LODGING_OWNER, errors[0].getMessage());
        assertEquals("Unexpected error", errors[0].getDetail());
    }

    @Test
    @DisplayName("Get Lodging Owner By Id - Success")
    void getByIdSuccess() {
        UUID id = UUID.randomUUID();
        owner.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(owner));

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.getById(id);

        assertTrue(result.isRight());
        LodgingOwnerResponseDTO adminResponseDTO = result.get();
        assertNotNull(adminResponseDTO);
        assertEquals(id, adminResponseDTO.getId());
        assertEquals("owner@email.com", adminResponseDTO.getEmail());
    }

    @Test
    @DisplayName("Get Lodging Owner By Id - Not Found")
    void getByIdLodgingOwnerNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_LODGING_OWNER, errors[0].getMessage());
    }

    @Test
    @DisplayName("Get Lodging Owner By Id - Invalid Id")
    void getByIdInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.NULL_ID, errors[0].getMessage());
    }

    @Test
    @DisplayName("Get Lodging Owner By ID - Unexpected Exception")
    void getByIdUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], LodgingOwnerResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_LODGING_OWNER, errors[0].getMessage());
        assertEquals("Unexpected error", errors[0].getDetail());
    }

    @Test
    @DisplayName("Find Lodging Owners By Email - Success")
    void findByEmailSuccess() {
        String email = "owner";
        List<LodgingOwner> owners = Arrays.asList(
                owner,
                new LodgingOwner("owner2@email.com", "validPassword123", "Owner2", "Hotel2", Role.LODGING_OWNER, true)
        );
        Page<LodgingOwner> page = new PageImpl<>(owners);

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable)).thenReturn(page);

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingOwnerResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("owner@email.com", resultPage.getContent().get(0).getEmail());
        assertEquals("owner2@email.com", resultPage.getContent().get(1).getEmail());
    }

    @Test
    @DisplayName("Find Lodging Owners By Email - Empty Result")
    void findByEmailEmptyResult() {
        String email = "nonexistent";
        Page<LodgingOwner> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingOwnerResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find Lodging Owners By Email - Invalid Data Access")
    void findByEmailInvalidDataAccess() {
        String email = "owner";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable))
                .thenThrow(new InvalidDataAccessApiUsageException("Invalid data access"));

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.NULL_EMAIL, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find Lodging Owners By Email - Unexpected Exception")
    void findByEmailUnexpectedException() {
        String email = "owner";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_LODGING_OWNER, errors[0].getMessage());
        assertEquals("Unexpected error", errors[0].getDetail());
    }

    @Test
    @DisplayName("Find Lodging Owners By Last Name - Success")
    void findByLastNameSuccess() {
        String lastName = "Hotel";
        List<LodgingOwner> owners = Arrays.asList(
                owner,
                new LodgingOwner("owner2@email.com", "validPassword123", "Owner2", "Hotel2", Role.LODGING_OWNER, true)
        );
        Page<LodgingOwner> page = new PageImpl<>(owners);

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable)).thenReturn(page);

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingOwnerResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals("Hotel", resultPage.getContent().get(0).getLastName());
        assertEquals("Hotel2", resultPage.getContent().get(1).getLastName());
    }

    @Test
    @DisplayName("Find Lodging Owners By Last Name - Empty Result")
    void findByLastNameEmptyResult() {
        String email = "nonexistent";
        Page<LodgingOwner> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(email, pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByLastName(email, pageableRequest);

        assertTrue(result.isRight());
        Page<LodgingOwnerResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find Lodging Owners By Last Name - Invalid Data Access")
    void findByLastNameInvalidDataAccess() {
        String lastName = "owner";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable))
                .thenThrow(new InvalidDataAccessApiUsageException("Invalid data access"));

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.NULL_LAST_NAME, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find Lodging Owners By Last Name - Unexpected Exception")
    void findByLastNameUnexpectedException() {
        String lastName = "owner";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByLastNameStartingWithIgnoreCase(lastName, pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> result = service.findByLastName(lastName, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_LODGING_OWNER, errors[0].getMessage());
        assertEquals("Unexpected error", errors[0].getDetail());
    }
}
