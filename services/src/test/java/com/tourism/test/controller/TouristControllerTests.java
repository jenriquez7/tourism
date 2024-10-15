package com.tourism.test.controller;

import com.tourism.controller.TouristController;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristRequestDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.Tourist;
import com.tourism.model.TouristType;
import com.tourism.model.User;
import com.tourism.service.TouristService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TouristControllerTests {

    @Mock
    private TouristService service;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TouristController controller;

    private TouristRequestDTO touristRequestDTO;
    private TouristResponseDTO touristResponseDTO;
    private Tourist tourist;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        touristRequestDTO = new TouristRequestDTO("Turista", "Verano", "tverano@email.com", "12345678");
        touristResponseDTO = new TouristResponseDTO(userId, "tverano@email.com", "Turista", "Verano");
        tourist = new Tourist("tverano@email.com", "12345678", "Turista", "Verano", Role.TOURIST, TouristType.STANDARD, true);
    }

    @Test
    @DisplayName("Create Tourist")
    void create() {
        when(service.create(any(TouristRequestDTO.class))).thenReturn(Either.right(touristResponseDTO));
        ResponseEntity<StandardResponseDto<TouristResponseDTO>> response = controller.create(request, touristRequestDTO);

        verifyTouristResponseDto(response);
        verify(service, times(1)).create(any(TouristRequestDTO.class));
    }

    @Test
    @DisplayName("Find All Tourists")
    void findAll() {
        List<TouristResponseDTO> tourists = Collections.singletonList(touristResponseDTO);
        Page<TouristResponseDTO> page = new PageImpl<>(tourists);

        when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(page));

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> response = controller.findAll(request, pageableRequest);

        verifyPageTouristResponseDto(response);

        verify(service, times(1)).findAll(pageableRequest);
    }

    @Test
    @DisplayName("Delete Tourist By Admin")
    void deleteByAdmin() {
        when(service.delete(userId)).thenReturn(Either.right(tourist));

        ResponseEntity<StandardResponseDto<Tourist>> response = controller.deleteByAdmin(request, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Tourist> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Tourist.class, data[0]);
        assertEquals(tourist, data[0]);
        verify(service, times(1)).delete(userId);
    }

    @Test
    @DisplayName("Delete Tourist By Token")
    void delete() {
        User user = new User(userId, "tverano@email.com", Role.TOURIST);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.delete(user.getId())).thenReturn(Either.right(tourist));

        ResponseEntity<StandardResponseDto<Tourist>> response = controller.delete(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Tourist> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Tourist.class, data[0]);
        assertEquals(tourist, data[0]);
        verify(service, times(1)).delete(user.getId());
    }

    @Test
    @DisplayName("Get Tourist By Id")
    void getById() {
        when(service.getById(userId)).thenReturn(Either.right(touristResponseDTO));
        ResponseEntity<StandardResponseDto<TouristResponseDTO>> response = controller.getById(request, userId);
        verifyTouristResponseDto(response);
        verify(service, times(1)).getById(userId);
    }

    @Test
    @DisplayName("Get Tourist Profile")
    void profile() {
        User user = new User(userId, "tverano@email.com", Role.TOURIST);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.getById(any(UUID.class))).thenReturn(Either.right(touristResponseDTO));
        ResponseEntity<StandardResponseDto<TouristResponseDTO>> response = controller.profile(request);
        verifyTouristResponseDto(response);
        verify(service, times(1)).getById(userId);
    }

    @Test
    @DisplayName("Get Tourist By Email")
    void findByEmail() {
        String email = "tverano@email.com";
        List<TouristResponseDTO> tourists = Collections.singletonList(touristResponseDTO);
        Page<TouristResponseDTO> page = new PageImpl<>(tourists);

        when(service.findByEmail(anyString(), any(PageableRequest.class))).thenReturn(Either.right(page));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> response = controller.findByEmail(request, email, pageableRequest);

        verifyPageTouristResponseDto(response);

        verify(service, times(1)).findByEmail(eq(email), argThat(req ->
                req.getPage() == 0 &&
                        req.getSize() == 10 &&
                        Arrays.equals(req.getSort(), new String[]{"id"}) &&
                        req.getSortType() == Sort.Direction.ASC
        ));
    }

    @Test
    @DisplayName("Find Tourist By Last Name")
    void findByLastName() {
        String lastName = "Verano";
        List<TouristResponseDTO> tourists = Collections.singletonList(touristResponseDTO);
        Page<TouristResponseDTO> page = new PageImpl<>(tourists);

        when(service.findByLastName(anyString(), any(PageableRequest.class))).thenReturn(Either.right(page));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> response = controller.findByLastName(request, lastName, pageableRequest);

        verifyPageTouristResponseDto(response);

        verify(service, times(1)).findByLastName(eq(lastName), argThat(req ->
                req.getPage() == 0 &&
                req.getSize() == 10 &&
                Arrays.equals(req.getSort(), new String[]{"id"}) &&
                req.getSortType() == Sort.Direction.ASC
        ));
    }


    private void verifyPageTouristResponseDto(ResponseEntity<StandardResponseDto<Page<TouristResponseDTO>>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<TouristResponseDTO>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<TouristResponseDTO> resultPage = (Page<TouristResponseDTO>) data[0];
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        TouristResponseDTO resultTourist = resultPage.getContent().getFirst();
        assertEquals(touristResponseDTO.id(), resultTourist.id());
        assertEquals(touristResponseDTO.email(), resultTourist.email());
    }

    private void verifyTouristResponseDto(ResponseEntity<StandardResponseDto<TouristResponseDTO>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<TouristResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(TouristResponseDTO.class, data[0]);

        TouristResponseDTO resultTourist = (TouristResponseDTO) data[0];
        assertEquals(touristResponseDTO.id(), resultTourist.id());
        assertEquals(touristResponseDTO.email(), resultTourist.email());
    }
}