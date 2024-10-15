package com.tourism.test.service;

import com.tourism.dto.mappers.TouristMapper;
import com.tourism.dto.mappers.TouristicPlaceMapper;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristicPlaceRequestDTO;
import com.tourism.dto.response.CategoryDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.model.*;
import com.tourism.repository.CategoryRepository;
import com.tourism.repository.TouristicPlaceRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.impl.TouristicPlaceServiceImpl;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
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
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TouristicPlaceServiceTests {

    @Mock
    private TouristicPlaceRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PageService pageService;

    @Mock
    private TouristicPlaceMapper mapper;

    @InjectMocks
    private TouristicPlaceServiceImpl service;

    private TouristicPlace place;
    private TouristicPlaceRequestDTO placeRequestDTO;
    private TouristicPlaceResponseDTO placeResponseDTO;
    private Category category;
    private Category otherCategory;
    private PageableRequest pageableRequest;
    private Pageable pageable;
    private CategoryDTO categoryDto;
    private CategoryDTO otherCategoryDto;

    @BeforeEach
    void setUp() {
        UUID placeId = UUID.randomUUID();
        List<Category> categories = new ArrayList<>();
        List<CategoryDTO> categoriesDto = new ArrayList<>();

        category = new Category(1, "Playa", true);
        otherCategory = new Category(2, "Ciudad", true);
        categories.add(category);
        categories.add(otherCategory);

        categoryDto = new CategoryDTO(1, "Playa");
        otherCategoryDto = new CategoryDTO(2, "Ciudad");
        categoriesDto.add(categoryDto);
        categoriesDto.add(otherCategoryDto);

        placeRequestDTO = new TouristicPlaceRequestDTO(placeId, "Punta del Este", "Hermoso lugar", Region.EAST, categories, true);
        placeResponseDTO = new TouristicPlaceResponseDTO(placeId, "Punta del Este", "Hermoso lugar", Region.EAST, categoriesDto, true);
        place = new TouristicPlace("Punta del Este", "Hermoso lugar", Region.EAST, new User(), true);
        pageableRequest = new PageableRequest(0, 10, new String[]{"email"}, Sort.Direction.ASC);
        pageable = mock(Pageable.class);
    }

    @Test
    @DisplayName("Create Touristic Place - Success")
    void createSuccess() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        TouristicPlace savedPlace = new TouristicPlace(place.getName(), place.getDescription(), place.getRegion(), user, true);

        List<TouristicPlaceCategory> touristicPlaceCategories = Arrays.asList(
                new TouristicPlaceCategory(savedPlace, category),
                new TouristicPlaceCategory(savedPlace, otherCategory)
        );
        savedPlace.setCategories(touristicPlaceCategories);

        when(mapper.modelToResponseDto(any(TouristicPlace.class))).thenReturn(placeResponseDTO);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(otherCategory));
        when(repository.save(any(TouristicPlace.class))).thenReturn(savedPlace);

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.create(placeRequestDTO, userId);

        assertTrue(result.isRight());
        TouristicPlaceResponseDTO resultDto = result.get();
        assertEquals(placeResponseDTO.id(), resultDto.id());
        assertEquals(placeResponseDTO.name(), resultDto.name());
        assertEquals(placeResponseDTO.description(), resultDto.description());
        assertEquals(placeResponseDTO.region(), resultDto.region());
        assertEquals(2, resultDto.categoryDTOs().size());
        assertTrue(resultDto.categoryDTOs().stream().anyMatch(dto -> dto.name().equals("Playa")));
        assertTrue(resultDto.categoryDTOs().stream().anyMatch(dto -> dto.name().equals("Ciudad")));
        assertEquals(placeResponseDTO.enabled(), resultDto.enabled());

        verify(userRepository).findById(userId);
        verify(categoryRepository, times(2)).findById(anyInt());
        verify(repository).save(any(TouristicPlace.class));
    }

    @Test
    @DisplayName("Create Touristic Place - User Not Found")
    void createUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.create(placeRequestDTO, userId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
        assertEquals(MessageConstants.ERROR_CREATE_TOURISTIC_PLACE, errors[0].message());

        verify(userRepository).findById(userId);
        verify(repository, never()).save(any(TouristicPlace.class));
    }

    @Test
    @DisplayName("Create Touristic Place - Category Not Found")
    void createCategoryNotFound() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.create(placeRequestDTO, userId);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_FOUND, errors[0].message());

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(1);
        verify(repository, never()).save(any(TouristicPlace.class));
    }

    @Test
    @DisplayName("Update Touristic Place - Success")
    void updateSuccess() {
        UUID placeId = placeRequestDTO.getId();
        when(mapper.modelToResponseDto(any(TouristicPlace.class))).thenReturn(placeResponseDTO);
        when(repository.findById(placeId)).thenReturn(Optional.of(place));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(2)).thenReturn(Optional.of(otherCategory));
        when(repository.save(any(TouristicPlace.class))).thenReturn(place);

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.update(placeRequestDTO);

        assertTrue(result.isRight());
        TouristicPlaceResponseDTO responseDTO = result.get();
        assertEquals(placeResponseDTO.name(), responseDTO.name());
        assertEquals(placeResponseDTO.description(), responseDTO.description());
        assertEquals(placeResponseDTO.region(), responseDTO.region());
        assertEquals(placeResponseDTO.categoryDTOs().size(), responseDTO.categoryDTOs().size());

        verify(repository).findById(placeId);
        verify(categoryRepository, times(2)).findById(anyInt());
        verify(repository).save(any(TouristicPlace.class));
    }

    @Test
    @DisplayName("Update Touristic Place - Not Found")
    void updateNotFound() {
        UUID placeId = placeRequestDTO.getId();
        when(repository.findById(placeId)).thenReturn(Optional.empty());

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.update(placeRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals("Error to delete touristic place", errors[0].message());

        verify(repository).findById(placeId);
        verify(repository, never()).save(any(TouristicPlace.class));
    }

    @Test
    @DisplayName("Update Touristic Place - Category Not Found")
    void updateCategoryNotFound() {
        UUID placeId = placeRequestDTO.getId();
        when(repository.findById(placeId)).thenReturn(Optional.of(place));
        when(categoryRepository.findById(1)).thenReturn(Optional.empty());

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.update(placeRequestDTO);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_FOUND, errors[0].message());

        verify(repository).findById(placeId);
        verify(categoryRepository).findById(1);
        verify(repository, never()).save(any(TouristicPlace.class));
    }

    @Test
    @DisplayName("Find All Touristic Places - Success")
    void findAllSuccess() {
        List<TouristicPlace> placeList = Arrays.asList(place, new TouristicPlace("Montevideo", "Capital city", Region.SOUTH, new User(), true));
        Page<TouristicPlace> page = new PageImpl<>(placeList, pageable, placeList.size());
        TouristicPlaceResponseDTO dto1 = placeResponseDTO;
        TouristicPlaceResponseDTO dto2 = new TouristicPlaceResponseDTO(UUID.randomUUID(),placeList.getLast().getName(), placeList.getLast().getDescription(), placeList.getLast().getRegion(), List.of(categoryDto, otherCategoryDto), placeList.getLast().getEnabled());

        when(mapper.modelToResponseDto(placeList.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(placeList.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findAll(pageable)).thenReturn(page);

        List<TouristicPlaceCategory> touristicPlaceCategories = Arrays.asList(
                new TouristicPlaceCategory(placeList.getFirst(), category),
                new TouristicPlaceCategory(placeList.getFirst(), otherCategory)
        );
        List<TouristicPlaceCategory> touristicPlaceCategories2 = Arrays.asList(
                new TouristicPlaceCategory(placeList.getLast(), category),
                new TouristicPlaceCategory(placeList.getLast(), otherCategory)
        );
        placeList.getFirst().setCategories(touristicPlaceCategories);
        placeList.getLast().setCategories(touristicPlaceCategories2);

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isRight());
        Page<TouristicPlaceResponseDTO> responsePage = result.get();
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals(2, responsePage.getContent().size());
        assertEquals(place.getName(), responsePage.getContent().getFirst().name());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Find All Touristic Places - Exception")
    void findAllException() {
        when(pageService.createSortedPageable(pageableRequest)).thenThrow(new RuntimeException("Error creating pageable"));

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findAll(pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURISTIC_PLACE, errors[0].message());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Delete Touristic Place - Success")
    void deleteSuccess() {
        UUID id = UUID.randomUUID();
        TouristicPlace touristicPlace = new TouristicPlace();
        when(repository.findById(id)).thenReturn(Optional.of(touristicPlace));
        doNothing().when(repository).delete(touristicPlace);

        Either<ErrorDto[], TouristicPlace> result = service.delete(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(repository).findById(id);
        verify(repository).delete(touristicPlace);
    }

    @Test
    @DisplayName("Delete Touristic Place - Invalid Id")
    void deleteInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

        Either<ErrorDto[], TouristicPlace> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.ERROR_DELETING_TOURISTIC_PLACE, errors[0].message());
        assertEquals("Invalid Id", errors[0].detail());
    }

    @Test
    @DisplayName("Delete Touristic Place - Unexpected Exception")
    void deleteUnexpectedException() {
        UUID id = UUID.randomUUID();
        when(repository.count()).thenReturn(2L);
        when(userRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], TouristicPlace> result = service.delete(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_DELETING_TOURISTIC_PLACE, errors[0].message());
    }

    @Test
    @DisplayName("Get Touristic Place By Id - Success")
    void getByIdSuccess() {
        when(mapper.modelToResponseDto(any(TouristicPlace.class))).thenReturn(placeResponseDTO);
        when(repository.findById(placeResponseDTO.id())).thenReturn(Optional.of(place));

        List<TouristicPlaceCategory> touristicPlaceCategories = Arrays.asList(
                new TouristicPlaceCategory(place, category),
                new TouristicPlaceCategory(place, otherCategory)
        );

        place.setCategories(touristicPlaceCategories);

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.getById(placeResponseDTO.id());

        assertTrue(result.isRight());
        TouristicPlaceResponseDTO responseDTO = result.get();
        assertEquals(placeResponseDTO.id(), responseDTO.id());
        assertEquals(placeResponseDTO.name(), responseDTO.name());
        assertEquals(placeResponseDTO.description(), responseDTO.description());
        assertEquals(placeResponseDTO.region(), responseDTO.region());
        assertEquals(placeResponseDTO.enabled(), responseDTO.enabled());
        verify(repository).findById(placeResponseDTO.id());
    }

    @Test
    @DisplayName("Get Touristic Place By Id - Not Found")
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.getById(id);

        assertTrue(result.isRight());
        assertNull(result.get());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Get Touristic Place By Id - Invalid Id")
    void getByIdInvalidId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenThrow(new InvalidDataAccessApiUsageException("Invalid id"));

        Either<ErrorDto[], TouristicPlaceResponseDTO> result = service.getById(id);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_ID, errors[0].message());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Find Touristic Places by Name - Success")
    void findByNameSuccess() {
        String name = "Beach";
        List<TouristicPlace> placeList = Arrays.asList(
                new TouristicPlace("Beach Resort", "Beautiful beach resort", Region.EAST, new User(), true),
                new TouristicPlace("Beach Hotel", "Luxurious beach hotel", Region.SOUTH, new User(), true)
        );
        Page<TouristicPlace> placePage = new PageImpl<>(placeList, pageable, placeList.size());
        TouristicPlaceResponseDTO dto1 = new TouristicPlaceResponseDTO(UUID.randomUUID(),placeList.getFirst().getName(), placeList.getFirst().getDescription(), placeList.getFirst().getRegion(), List.of(categoryDto, otherCategoryDto), placeList.getFirst().getEnabled());;
        TouristicPlaceResponseDTO dto2 = new TouristicPlaceResponseDTO(UUID.randomUUID(),placeList.getLast().getName(), placeList.getLast().getDescription(), placeList.getLast().getRegion(), List.of(categoryDto, otherCategoryDto), placeList.getLast().getEnabled());

        List<TouristicPlaceCategory> touristicPlaceCategories = Arrays.asList(
                new TouristicPlaceCategory(placeList.getFirst(), category),
                new TouristicPlaceCategory(placeList.getFirst(), otherCategory)
        );
        List<TouristicPlaceCategory> touristicPlaceCategories2 = Arrays.asList(
                new TouristicPlaceCategory(placeList.getLast(), category),
                new TouristicPlaceCategory(placeList.getLast(), otherCategory)
        );
        placeList.getFirst().setCategories(touristicPlaceCategories);
        placeList.getLast().setCategories(touristicPlaceCategories2);

        when(mapper.modelToResponseDto(placeList.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(placeList.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByNameStartingWithIgnoreCase(name, pageable)).thenReturn(placePage);

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findByName(name, pageableRequest);

        assertTrue(result.isRight());
        Page<TouristicPlaceResponseDTO> responsePage = result.get();
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals(2, responsePage.getContent().size());
        assertTrue(responsePage.getContent().get(0).name().startsWith("Beach"));
        assertTrue(responsePage.getContent().get(1).name().startsWith("Beach"));

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findByNameStartingWithIgnoreCase(name, pageable);
    }

    @Test
    @DisplayName("Find Touristic Places by Name - Invalid Pageable")
    void findByNameInvalidPageable() {
        String name = "Beach";
        when(pageService.createSortedPageable(pageableRequest)).thenThrow(new InvalidDataAccessApiUsageException("Invalid pageable"));

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findByName(name, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_ID, errors[0].message());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository, never()).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Find Touristic Places by Region - Success")
    void findByRegionSuccess() {
        Region region = Region.EAST;
        List<TouristicPlace> placeList = Arrays.asList(
                new TouristicPlace("East Resort", "Beautiful east resort", Region.EAST, new User(), true),
                new TouristicPlace("East Hotel", "Luxurious east hotel", Region.EAST, new User(), true)
        );
        Page<TouristicPlace> placePage = new PageImpl<>(placeList, pageable, placeList.size());
        TouristicPlaceResponseDTO dto1 = placeResponseDTO;
        TouristicPlaceResponseDTO dto2 = new TouristicPlaceResponseDTO(UUID.randomUUID(),placeList.getLast().getName(), placeList.getLast().getDescription(), placeList.getLast().getRegion(), List.of(categoryDto, otherCategoryDto), placeList.getLast().getEnabled());


        List<TouristicPlaceCategory> touristicPlaceCategories = Arrays.asList(
                new TouristicPlaceCategory(placeList.getFirst(), category),
                new TouristicPlaceCategory(placeList.getFirst(), otherCategory)
        );
        List<TouristicPlaceCategory> touristicPlaceCategories2 = Arrays.asList(
                new TouristicPlaceCategory(placeList.getLast(), category),
                new TouristicPlaceCategory(placeList.getLast(), otherCategory)
        );
        placeList.getFirst().setCategories(touristicPlaceCategories);
        placeList.getLast().setCategories(touristicPlaceCategories2);

        when(mapper.modelToResponseDto(placeList.getFirst())).thenReturn(dto1);
        when(mapper.modelToResponseDto(placeList.getLast())).thenReturn(dto2);
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(pageable);
        when(repository.findByRegion(region, pageable)).thenReturn(placePage);

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findByRegion(region, pageableRequest);

        assertTrue(result.isRight());
        Page<TouristicPlaceResponseDTO> responsePage = result.get();
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(1, responsePage.getTotalPages());
        assertEquals(2, responsePage.getContent().size());
        assertEquals(Region.EAST, responsePage.getContent().get(0).region());
        assertEquals(Region.EAST, responsePage.getContent().get(1).region());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findByRegion(region, pageable);
    }

    @Test
    @DisplayName("Find Touristic Places by Region - Invalid Pageable")
    void findByRegionInvalidPageable() {
        Region region = Region.EAST;
        when(pageService.createSortedPageable(pageableRequest)).thenThrow(new InvalidDataAccessApiUsageException("Invalid pageable"));

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findByRegion(region, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
        assertEquals(MessageConstants.NULL_ID, errors[0].message());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository, never()).findByRegion(any(Region.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Find Touristic Places by Name - Unexpected Exception")
    void findByNameUnexpectedException() {
        String name = "Beach";
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(mock(Pageable.class));
        when(repository.findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class))).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findByName(name, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURISTIC_PLACE, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findByNameStartingWithIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Find Touristic Places by Region - Unexpected Exception")
    void findByRegionUnexpectedException() {
        Region region = Region.EAST;
        when(pageService.createSortedPageable(pageableRequest)).thenReturn(mock(Pageable.class));
        when(repository.findByRegion(any(Region.class), any(Pageable.class))).thenThrow(new RuntimeException("Unexpected error"));

        Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> result = service.findByRegion(region, pageableRequest);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
        assertEquals(MessageConstants.ERROR_GET_TOURISTIC_PLACE, errors[0].message());
        assertEquals("Unexpected error", errors[0].detail());

        verify(pageService).createSortedPageable(pageableRequest);
        verify(repository).findByRegion(any(Region.class), any(Pageable.class));
    }
}
