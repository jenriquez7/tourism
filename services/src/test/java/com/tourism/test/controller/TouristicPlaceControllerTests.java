package com.tourism.test.controller;

import com.tourism.controller.TouristicPlaceController;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristicPlaceRequestDTO;
import com.tourism.dto.response.CategoryDTO;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.infrastructure.JwtTokenProvider;
import com.tourism.model.Role;
import com.tourism.model.*;
import com.tourism.service.TouristicPlaceService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TouristicPlaceControllerTests {

    @Mock
    private TouristicPlaceService service;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TouristicPlaceController controller;

    private TouristicPlace place;
    private TouristicPlaceRequestDTO placeRequestDTO;
    private TouristicPlaceResponseDTO placeResponseDTO;

    @BeforeEach
    void setUp() {
        UUID placeId = UUID.randomUUID();
        List<Category> categories = new ArrayList<>();
        List<CategoryDTO> categoriesDto = new ArrayList<>();

        Category category = new Category(1, "Playa", true);
        Category otherCategory = new Category(2, "Ciudad", true);
        categories.add(category);
        categories.add(otherCategory);

        CategoryDTO categoryDto = new CategoryDTO(1, "Playa");
        CategoryDTO otherCategoryDto = new CategoryDTO(2, "Ciudad");
        categoriesDto.add(categoryDto);
        categoriesDto.add(otherCategoryDto);

        placeRequestDTO = new TouristicPlaceRequestDTO(placeId, "Punta del Este", "Hermoso lugar", Region.EAST, categories, true);
        placeResponseDTO = new TouristicPlaceResponseDTO(placeId, "Punta del Este", "Hermoso lugar", Region.EAST, categoriesDto, true);
        place = new TouristicPlace("Punta del Este", "Hermoso lugar", Region.EAST, new User(), true);
    }

    @Test
    @DisplayName("Create Touristic Place")
    void create() {
        User user = new User(UUID.randomUUID(), "admin@email.com", Role.ADMIN);
        when(jwtTokenProvider.getUserFromToken(request)).thenReturn(user);
        when(service.create(placeRequestDTO, user.getId())).thenReturn(Either.right(placeResponseDTO));
        ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> response = controller.create(request, placeRequestDTO);

        verifyTouristicPlaceResponseDto(response);
        verify(service, times(1)).create(placeRequestDTO, user.getId());
    }

    @Test
    @DisplayName("Update Touristic Place")
    void update() {
        when(service.update(placeRequestDTO)).thenReturn(Either.right(placeResponseDTO));
        ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> response = controller.update(request, placeRequestDTO);

        verifyTouristicPlaceResponseDto(response);
        verify(service, times(1)).update(placeRequestDTO);
    }

    @Test
    @DisplayName("Find All Touristic Places")
    void findAll() {
        List<TouristicPlaceResponseDTO> places = Collections.singletonList(placeResponseDTO);
        Page<TouristicPlaceResponseDTO> page = new PageImpl<>(places);

        when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(page));

        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> response = controller.findAll(request, pageableRequest);

        verifyPageTouristicPlaceResponseDto(response);
        verify(service, times(1)).findAll(pageableRequest);
    }

    @Test
    @DisplayName("Delete Touristic Place")
    void delete() {
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(Either.right(place));

        ResponseEntity<StandardResponseDto<TouristicPlace>> response = controller.delete(request, id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<TouristicPlace> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(TouristicPlace.class, data[0]);
        assertEquals(place, data[0]);
        verify(service, times(1)).delete(id);
    }

    @Test
    @DisplayName("Get Touristic Place By Id")
    void getById() {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(Either.right(placeResponseDTO));
        ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> response = controller.getById(request, id);
        verifyTouristicPlaceResponseDto(response);
        verify(service, times(1)).getById(id);
    }

    @Test
    @DisplayName("Find Tourist Place By Name")
    void findByEmail() {
        String name = "Punta";
        List<TouristicPlaceResponseDTO> tourists = Collections.singletonList(placeResponseDTO);
        Page<TouristicPlaceResponseDTO> page = new PageImpl<>(tourists);

        when(service.findByName(anyString(), any(PageableRequest.class))).thenReturn(Either.right(page));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> response = controller.findByName(request, name, pageableRequest);

        verifyPageTouristicPlaceResponseDto(response);

        verify(service, times(1)).findByName(eq(name), argThat(req ->
                req.getPage() == 0 &&
                req.getSize() == 10 &&
                Arrays.equals(req.getSort(), new String[]{"id"}) &&
                req.getSortType() == Sort.Direction.ASC
        ));
    }

    @Test
    @DisplayName("Find Tourist Place By Region")
    void findByRegion() {
        Region region = Region.EAST;
        List<TouristicPlaceResponseDTO> tourists = Collections.singletonList(placeResponseDTO);
        Page<TouristicPlaceResponseDTO> page = new PageImpl<>(tourists);

        when(service.findByRegion(any(Region.class), any(PageableRequest.class))).thenReturn(Either.right(page));
        PageableRequest pageableRequest = new PageableRequest(0, 10, new String[]{"id"}, Sort.Direction.ASC);
        ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> response = controller.findByRegion(request, region, pageableRequest);

        verifyPageTouristicPlaceResponseDto(response);

        verify(service, times(1)).findByRegion(eq(region), argThat(req ->
                req.getPage() == 0 &&
                req.getSize() == 10 &&
                Arrays.equals(req.getSort(), new String[]{"id"}) &&
                req.getSortType() == Sort.Direction.ASC
        ));
    }

    private void verifyTouristicPlaceResponseDto(ResponseEntity<StandardResponseDto<TouristicPlaceResponseDTO>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<TouristicPlaceResponseDTO> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(TouristicPlaceResponseDTO.class, data[0]);

        TouristicPlaceResponseDTO responseDto = (TouristicPlaceResponseDTO) data[0];
        assertEquals(placeResponseDTO.getId(), responseDto.getId());
        assertEquals(placeResponseDTO.getName(), responseDto.getName());
        assertEquals(placeResponseDTO.getRegion(), responseDto.getRegion());
        assertEquals(placeResponseDTO.getDescription(), responseDto.getDescription());
    }

    private void verifyPageTouristicPlaceResponseDto(ResponseEntity<StandardResponseDto<Page<TouristicPlaceResponseDTO>>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Page<TouristicPlaceResponseDTO>> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Page.class, data[0]);

        Page<TouristicPlaceResponseDTO> resultPage = (Page<TouristicPlaceResponseDTO>) data[0];
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(1, resultPage.getContent().size());

        TouristicPlaceResponseDTO resultPlace = resultPage.getContent().getFirst();
        assertEquals(placeResponseDTO.getId(), resultPlace.getId());
        assertEquals(placeResponseDTO.getName(), resultPlace.getName());
        assertEquals(placeResponseDTO.getDescription(), resultPlace.getDescription());
        assertEquals(placeResponseDTO.getRegion(), resultPlace.getRegion());
    }
}
