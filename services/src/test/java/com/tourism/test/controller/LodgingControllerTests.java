package com.tourism.test.controller;

import com.tourism.controller.LodgingController;
import com.tourism.dto.request.LodgingRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.*;
import com.tourism.service.LodgingService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LodgingControllerTests {

    @Mock
    private LodgingService service;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LodgingController controller;

    private Lodging lodging;
    private LodgingRequestDTO requestDto;
    private LodgingResponseDTO lodgingResponseDTO;

    @BeforeEach
    void setUp() {
        TouristicPlaceResponseDTO tpResponseDto = new TouristicPlaceResponseDTO(UUID.randomUUID(), "Punta del Este", "Hermoso lugar", Region.EAST, new ArrayList<>(), true);;
        lodging = new Lodging("Hotel Test", "Un hotel de pruebas", "Parada 5, playa mansa", "+5984422112233", 50, 25.0, 5, new TouristicPlace(), new LodgingOwner(), true);
        lodgingResponseDTO = new LodgingResponseDTO(UUID.randomUUID(),"Hotel Test", "Un hotel de pruebas", "Parada 5, playa mansa", "+5984422112233", 50, 25.0, 5, tpResponseDto, true);
        requestDto = new LodgingRequestDTO("Hotel Test", "Un hotel de pruebas", "Calle falsa 123", "+59899123456", 4, 20, 25.0, UUID.randomUUID());
    }

    @Test
    @DisplayName("Create Lodging")
    void create() {
        User user = new User(UUID.randomUUID(), "admin@email.com", Role.ADMIN);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.create(requestDto, user.getId())).thenReturn(Either.right(lodgingResponseDTO));
        ResponseEntity<StandardResponseDto<LodgingResponseDTO>> response = controller.create(request, requestDto);

        verifyLodgingResponseDto(response);
        verify(service, times(1)).create(requestDto, user.getId());
    }

    @Test
    @DisplayName("Update Lodging")
    void update() {
        User user = new User(UUID.randomUUID(), "admin@email.com", Role.ADMIN);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.update(lodging, user.getId())).thenReturn(Either.right(lodgingResponseDTO));
        ResponseEntity<StandardResponseDto<LodgingResponseDTO>> response = controller.update(request, lodging);

        verifyLodgingResponseDto(response);
        verify(service, times(1)).update(lodging, user.getId());
    }

    @Test
    @DisplayName("Find All Lodgings")
    void findAll() {
        List<LodgingResponseDTO> places = Collections.singletonList(lodgingResponseDTO);
        Page<LodgingResponseDTO> page = new PageImpl<>(places);

        when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(page));

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<LodgingResponseDTO>>> response = controller.findAll(request, pageableRequest);

        verifyPageLodgingResponseDto(response);
        verify(service, times(1)).findAll(pageableRequest);
    }

    @Test
    @DisplayName("Delete Lodging")
    void delete() {
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(Either.right(lodging));

        ResponseEntity<StandardResponseDto<Lodging>> response = controller.delete(request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Lodging> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Lodging.class, data[0]);
        assertEquals(lodging, data[0]);
        verify(service, times(1)).delete(id);
    }

    @Test
    @DisplayName("Get Lodging By Id")
    void getById() {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(Either.right(lodgingResponseDTO));
        ResponseEntity<StandardResponseDto<LodgingResponseDTO>> response = controller.getById(request, id);
        verifyLodgingResponseDto(response);
        verify(service, times(1)).getById(id);
    }

    @Test
    @DisplayName("Get Lodging By Touristic Place")
    void findByTouristicPlace() {
        UUID touristicPlaceId = UUID.randomUUID();
        List<LodgingResponseDTO> places = Collections.singletonList(lodgingResponseDTO);
        Page<LodgingResponseDTO> page = new PageImpl<>(places);

        when(service.findLodgingsByTouristicPlace(any(UUID.class), any(PageableRequest.class))).thenReturn(Either.right(page));

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<LodgingResponseDTO>>> response = controller.findLodgingsByTouristicPlace(request, touristicPlaceId, pageableRequest);

        verifyPageLodgingResponseDto(response);
        verify(service, times(1)).findLodgingsByTouristicPlace(touristicPlaceId, pageableRequest);
    }


    private void verifyLodgingResponseDto(ResponseEntity<StandardResponseDto<LodgingResponseDTO>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<LodgingResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(LodgingResponseDTO.class, data[0]);

        LodgingResponseDTO responseDto = (LodgingResponseDTO) data[0];
        assertEquals(lodgingResponseDTO.id(), responseDto.id());
        assertEquals(lodgingResponseDTO.name(), responseDto.name());
        assertEquals(lodgingResponseDTO.stars(), responseDto.stars());
        assertEquals(lodgingResponseDTO.description(), responseDto.description());
    }

    private void verifyPageLodgingResponseDto(ResponseEntity<StandardResponseDto<Page<LodgingResponseDTO>>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<LodgingResponseDTO>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<LodgingResponseDTO> resultPage = (Page<LodgingResponseDTO>) data[0];
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        LodgingResponseDTO responseDto = resultPage.getContent().getFirst();
        assertEquals(lodgingResponseDTO.id(), responseDto.id());
        assertEquals(lodgingResponseDTO.name(), responseDto.name());
        assertEquals(lodgingResponseDTO.description(), responseDto.description());
        assertEquals(lodgingResponseDTO.stars(), responseDto.stars());
    }
}
