package com.tourism.test.service;

import com.tourism.dto.mappers.AdminMapper;
import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.AdminResponseDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Role;
import com.tourism.model.Admin;
import com.tourism.model.User;
import com.tourism.repository.AdminRepository;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.impl.AdminServiceImpl;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AdminServiceTests {

    @InjectMocks
    private AdminServiceImpl service;

    @Mock
    private AdminRepository repository;

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
    private AdminMapper mapper;

    private AuthUserDto authUserDto;
    private Admin admin;
    private PageableRequest pageableRequest;
    private Pageable pageable;
    private AdminResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        authUserDto = new AuthUserDto("admin@email.com", "12345678");
        admin = new Admin("admin@email.com", "12345678", Role.ADMIN, true);
        pageableRequest = new PageableRequest(0, 10, new String[]{"email"}, Sort.Direction.ASC);
        pageable = mock(Pageable.class);
        responseDTO = new AdminResponseDTO(UUID.randomUUID(),authUserDto.getEmail());
    }

    @Test
    @DisplayName("Create Admin - Success")
    void createSuccess() {
        String encryptedPassword = "encryptedPassword";
        when(mapper.modelToResponseDto(any(Admin.class))).thenReturn(responseDTO);
        when(userValidation.validateEmailAndPassword(authUserDto.getEmail(), authUserDto.getPassword())).thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(authUserDto.getPassword())).thenReturn(encryptedPassword);
        when(repository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin savedAdmin = invocation.getArgument(0);
            savedAdmin.setId(responseDTO.id());
            return savedAdmin;
        });

        Either<ErrorDto[], AdminResponseDTO> response = service.create(authUserDto);

        assertTrue(response.isRight());
        AdminResponseDTO adminResponse = response.get();
        assertEquals(responseDTO.email(), adminResponse.email());
        assertEquals(responseDTO.id(), adminResponse.id());

        verify(repository).save(argThat(a ->
                a.getEmail().equals(authUserDto.getEmail()) &&
                        a.getPassword().equals(encryptedPassword) &&
                        a.getRole() == Role.ADMIN &&
                        a.getEnabled()
        ));
    }

    @Test
    @DisplayName("Create Admin - Validation Fails")
    void createValidationFails() {
        ErrorDto[] errors = {new ErrorDto(HttpStatus.BAD_REQUEST, "Invalid email or password", "Validation failed")};
        when(userValidation.validateEmailAndPassword(authUserDto.getEmail(), authUserDto.getPassword()))
                .thenReturn(Either.left(errors));

        Either<ErrorDto[], AdminResponseDTO> response = service.create(authUserDto);

        assertTrue(response.isLeft());
        assertArrayEquals(errors, response.getLeft());
        verify(repository, never()).save(any(Admin.class));
    }

    @Test
    @DisplayName("Create Admin - DataIntegrityViolationException")
    void createDataIntegrityViolationException() {
        when(userValidation.validateEmailAndPassword(authUserDto.getEmail(), authUserDto.getPassword()))
                .thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(authUserDto.getPassword()))
                .thenReturn("encryptedPassword");
        when(repository.save(any(Admin.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        Either<ErrorDto[], AdminResponseDTO> response = service.create(authUserDto);

        assertTrue(response.isLeft());
        ErrorDto[] errors = response.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.CONFLICT, errors[0].code());
        assertEquals(MessageConstants.ERROR_ADMIN_NOT_CREATED, errors[0].message());
    }

    @Test
    @DisplayName("Create Admin - General Exception")
    void createGeneralException() {
        when(userValidation.validateEmailAndPassword(authUserDto.getEmail(), authUserDto.getPassword()))
                .thenReturn(Either.right(true));
        when(encryptionService.encryptPassword(authUserDto.getPassword()))
                .thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], AdminResponseDTO> response = service.create(authUserDto);

        assertTrue(response.isLeft());
        ErrorDto[] errors = response.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.ERROR_CREATE_ADMIN, errors[0].message());
    }

    @Test
    @DisplayName("Find All Admins - Success")
    void findAllSuccess() {
        List<Admin> admins = Arrays.asList(
                new Admin("admin1@example.com", "password", Role.ADMIN, true),
                new Admin("admin2@example.com", "password", Role.ADMIN, true)
        );
        Page<Admin> page = new PageImpl<>(admins);
        AdminResponseDTO dto1 = new AdminResponseDTO(UUID.randomUUID(),admins.getFirst().getEmail());
        AdminResponseDTO dto2 = new AdminResponseDTO(UUID.randomUUID(),admins.getLast().getEmail());
        when(mapper.modelToResponseDto(admins.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(admins.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(page);
        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<AdminResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals(dto1.email(), resultPage.getContent().get(0).email());
        assertEquals(dto2.email(), resultPage.getContent().get(1).email());
    }

    @Test
    @DisplayName("Find All Admins - Empty Page")
    void findAllEmptyPage() {
        Page<Admin> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<AdminResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find All Admins - Exception Thrown")
    void findAllException() {
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_ADMINS, errors[0].message());
        assertEquals("Database error", errors[0].detail());
    }

    @Test
    @DisplayName("Delete Admin - Success")
    void deleteAdminSuccess() {
        UUID id = UUID.randomUUID();
        User user = new User();

        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(repository.findById(id)).thenReturn(Optional.of(admin));

        Either<ErrorDto[], AdminResponseDTO> result = service.delete(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(tokenRepository).deleteByUser(user);
        verify(repository).delete(admin);
    }

    @Test
    @DisplayName("Delete Admin - Cannot Delete Last Admin")
    void deleteLastAdmin() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(1L);

        Either<ErrorDto[], AdminResponseDTO> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.ERROR_CANNOT_DELETE_LAST_ADMIN, errors[0].message());
    }

    @Test
    @DisplayName("Delete Admin - Invalid ID")
    void deleteAdminInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], AdminResponseDTO> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_ADMIN_NOT_FOUND, errors[0].message());
    }

    @Test
    @DisplayName("Delete Admin - Unexpected Exception")
    void deleteAdminUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], AdminResponseDTO> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_ADMINS, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }

    @Test
    @DisplayName("Get Admin By Id - Success")
    void getByIdSuccess() {
        when(repository.findById(responseDTO.id())).thenReturn(Optional.of(admin));
        when(mapper.modelToResponseDto(any(Admin.class))).thenReturn(responseDTO);

        Either<ErrorDto[], AdminResponseDTO> result = service.getById(responseDTO.id());

        assertTrue(result.isRight());
        AdminResponseDTO adminResponseDTO = result.get();
        assertNotNull(adminResponseDTO);
        assertEquals(responseDTO.id(), adminResponseDTO.id());
        assertEquals("admin@email.com", adminResponseDTO.email());
    }

    @Test
    @DisplayName("Get Admin By Id - Admin Not Found")
    void getByIdAdminNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Either<ErrorDto[], AdminResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_ADMINS, errors[0].message());
    }

    @Test
    @DisplayName("Get Admin By Id - Invalid Id")
    void getByIdInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], AdminResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_ID, errors[0].message());
    }

    @Test
    @DisplayName("Get Admin By ID - Unexpected Exception")
    void getByIdUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], AdminResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_ADMINS, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }

    @Test
    @DisplayName("Find Admins By Email - Success")
    void findByEmailSuccess() {
        String email = "admin";
        List<Admin> admins = Arrays.asList(
                new Admin("admin1@example.com", "password", Role.ADMIN, true),
                new Admin("admin2@example.com", "password", Role.ADMIN, true)
        );
        Page<Admin> adminPage = new PageImpl<>(admins);
        AdminResponseDTO dto1 = new AdminResponseDTO(UUID.randomUUID(),admins.getFirst().getEmail());
        AdminResponseDTO dto2 = new AdminResponseDTO(UUID.randomUUID(),admins.getLast().getEmail());

        when(mapper.modelToResponseDto(admins.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(admins.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable)).thenReturn(adminPage);

        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isRight());
        Page<AdminResponseDTO> resultPage = result.get();
        assertEquals(2, resultPage.getContent().size());
        assertEquals(dto1.email(), resultPage.getContent().get(0).email());
        assertEquals(dto2.email(), resultPage.getContent().get(1).email());
    }

    @Test
    @DisplayName("Find Admins By Email - Empty Result")
    void findByEmailEmptyResult() {
        String email = "nonexistent";
        Page<Admin> emptyPage = new PageImpl<>(List.of());

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable)).thenReturn(emptyPage);

        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isRight());
        Page<AdminResponseDTO> resultPage = result.get();
        assertTrue(resultPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("Find Admins By Email - Invalid Data Access")
    void findByEmailInvalidDataAccess() {
        String email = "admin";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable))
                .thenThrow(new InvalidDataAccessApiUsageException("Invalid data access"));

        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_EMAIL, errors[0].message());
    }

    @Test
    @DisplayName("Find Admins By Email - Unexpected Exception")
    void findByEmailUnexpectedException() {
        String email = "admin";

        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByEmailStartingWithIgnoreCase(email, pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<AdminResponseDTO>> result = service.findByEmail(email, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_ADMINS, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());
    }
}
