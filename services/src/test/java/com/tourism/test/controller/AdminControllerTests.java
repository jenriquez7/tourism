package com.tourism.test.controller;

import com.tourism.controller.AdminController;
import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.AdminResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.service.AdminService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AdminControllerTests {

    @Mock
    HttpServletRequest request;

    @Mock
    private AdminService service;

    @InjectMocks
    private AdminController controller;

    private AuthUserDto authUserDto;

    private AdminResponseDTO admin;
    private AdminResponseDTO otherAdmin;

    @BeforeEach
    void setUp() {
        authUserDto = new AuthUserDto("admin@email.com", "12345678");
        admin = new AdminResponseDTO(UUID.randomUUID(), "admin@email.com");
        otherAdmin = new AdminResponseDTO(UUID.randomUUID(), "admin2@email.com");
    }

    @Test
    @DisplayName("Create Admin")
    void create() {
        when(service.create(any(AuthUserDto.class))).thenReturn(Either.right(admin));

        ResponseEntity<StandardResponseDto<AdminResponseDTO>> response = controller.create(request, authUserDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StandardResponseDto<AdminResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);

        assertInstanceOf(AdminResponseDTO.class, data[0]);
        AdminResponseDTO resultAdmin = (AdminResponseDTO) data[0];
        assertEquals(admin.getEmail(), resultAdmin.getEmail());
        assertEquals(admin.getId(), resultAdmin.getId());

        verify(service, times(1)).create(any(AuthUserDto.class));
    }

    @Test
    @DisplayName("Find All Admin")
    void findAll() {
        List<AdminResponseDTO> adminList = Arrays.asList(admin, otherAdmin);
        Page<AdminResponseDTO> adminPage = new PageImpl<>(adminList);

        when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(adminPage));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);

        ResponseEntity<StandardResponseDto<Page<AdminResponseDTO>>> response = controller.findAll(request, pageableRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<AdminResponseDTO>> body = response.getBody();
        assertNotNull(body);
        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<AdminResponseDTO> resultPage = (Page<AdminResponseDTO>) data[0];
        assertEquals(2, resultPage.getTotalElements());
        assertEquals(2, resultPage.getContent().size());

        List<AdminResponseDTO> resultAdmins = resultPage.getContent();
        assertEquals(admin.getId(), resultAdmins.get(0).getId());
        assertEquals(admin.getEmail(), resultAdmins.get(0).getEmail());
        assertEquals(otherAdmin.getId(), resultAdmins.get(1).getId());
        assertEquals(otherAdmin.getEmail(), resultAdmins.get(1).getEmail());

        verify(service, times(1)).findAll(argThat(req ->
                req.getPage() == 0 &&
                req.getSize() == 10 &&
                Arrays.equals(req.getSort(), new String[]{"id"}) &&
                req.getSortType() == Sort.Direction.ASC
        ));
    }

    @Test
    @DisplayName("Get Admin By Id")
    void getById() {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(Either.right(admin));

        ResponseEntity<StandardResponseDto<AdminResponseDTO>> response = controller.getById(request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<AdminResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(AdminResponseDTO.class, data[0]);

        AdminResponseDTO resultAdmin = (AdminResponseDTO) data[0];
        assertEquals(admin.getId(), resultAdmin.getId());
        assertEquals(admin.getEmail(), resultAdmin.getEmail());

        verify(service, times(1)).getById(id);
    }

    @Test
    @DisplayName("Delete Admin")
    void delete() {
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(Either.right(admin));

        ResponseEntity<StandardResponseDto<AdminResponseDTO>> response = controller.delete(request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<AdminResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(AdminResponseDTO.class, data[0]);

        AdminResponseDTO resultAdmin = (AdminResponseDTO) data[0];
        assertEquals(admin.getId(), resultAdmin.getId());
        assertEquals(admin.getEmail(), resultAdmin.getEmail());

        verify(service, times(1)).delete(id);
    }

    @Test
    @DisplayName("Get Admin By Email")
    void getByEmail() {
        String email = "admin@email.com";
        List<AdminResponseDTO> adminList = Collections.singletonList(admin);
        Page<AdminResponseDTO> adminPage = new PageImpl<>(adminList);

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);

        when(service.findByEmail(eq(email), any(PageableRequest.class))).thenReturn(Either.right(adminPage));

        ResponseEntity<StandardResponseDto<Page<AdminResponseDTO>>> response =
                controller.getByEmail(request, email, pageableRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<AdminResponseDTO>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<AdminResponseDTO> resultPage = (Page<AdminResponseDTO>) data[0];
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        AdminResponseDTO resultAdmin = resultPage.getContent().getFirst();
        assertEquals(admin.getId(), resultAdmin.getId());
        assertEquals(admin.getEmail(), resultAdmin.getEmail());

        verify(service, times(1)).findByEmail(eq(email), argThat(req ->
                req.getPage() == 0 &&
                req.getSize() == 10 &&
                Arrays.equals(req.getSort(), new String[]{"id"}) &&
                req.getSortType() == Sort.Direction.ASC
        ));
    }
}
