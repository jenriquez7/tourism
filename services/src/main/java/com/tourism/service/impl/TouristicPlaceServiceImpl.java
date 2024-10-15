package com.tourism.service.impl;

import com.tourism.dto.mappers.TouristicPlaceMapper;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristicPlaceRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.model.*;
import com.tourism.repository.CategoryRepository;
import com.tourism.repository.TouristicPlaceRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.TouristicPlaceService;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import io.vavr.control.Either;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class TouristicPlaceServiceImpl implements TouristicPlaceService {

    private final TouristicPlaceRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PageService pageService;
    private final TouristicPlaceMapper mapper;

    @Autowired
    public TouristicPlaceServiceImpl(TouristicPlaceRepository repository, UserRepository userRepository,
                                     CategoryRepository categoryRepository, PageService pageService,
                                     TouristicPlaceMapper mapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.pageService = pageService;
        this.mapper = mapper;
    }


    @Override
    @Transactional
    public Either<ErrorDto[], TouristicPlaceResponseDTO> create(TouristicPlaceRequestDTO touristicPlaceDto, UUID userId) {
        try {
            TouristicPlace place = new TouristicPlace(
                    touristicPlaceDto.getName(),
                    touristicPlaceDto.getDescription(),
                    touristicPlaceDto.getRegion(),
                    Objects.requireNonNull(userRepository.findById(userId).orElse(null)),
                    true
            );

            List<TouristicPlaceCategory> categories = transformCategoriesToTouristicPlaceCategory(touristicPlaceDto, place);
            place.setCategories(categories);

            return Either.right(mapper.modelToResponseDto(repository.save(place)));
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_CREATE_TOURISTIC_PLACE, e.getMessage())});
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_CATEGORY_NOT_FOUND, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CREATE_TOURISTIC_PLACE, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], TouristicPlaceResponseDTO> update(TouristicPlaceRequestDTO placeDTO) {
        try {
            TouristicPlace place = repository.findById(placeDTO.getId()).orElse(null);
            if (place != null) {
                place.setName(placeDTO.getName());
                place.setDescription(placeDTO.getDescription());
                place.setRegion(placeDTO.getRegion());
                place.setCategories(this.transformCategoriesToTouristicPlaceCategory(placeDTO, place));
                place.setEnabled(placeDTO.getEnabled());
                return Either.right(mapper.modelToResponseDto(repository.save(place)));
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, "Error to delete touristic place", null)});
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_ACCEPTABLE, MessageConstants.ERROR_UPDATE_TOURISTIC_PLACE, e.getMessage())});
        } catch (EntityNotFoundException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_CATEGORY_NOT_FOUND, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_UPDATE_TOURISTIC_PLACE, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> findAll(PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<TouristicPlace> places = repository.findAll(pageable);
            return Either.right(places.map(mapper::modelToResponseDto));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURISTIC_PLACE, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], TouristicPlace> delete(UUID id) {
        try {
            TouristicPlace place = repository.findById(id).orElse(null);
            repository.delete(Objects.requireNonNull(place));
            return Either.right(null);
        } catch (InvalidDataAccessApiUsageException | NoSuchElementException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_DELETING_TOURISTIC_PLACE, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_DELETING_TOURISTIC_PLACE, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], TouristicPlaceResponseDTO> getById(UUID id) {
        try {
            TouristicPlace place = repository.findById(id).orElse(null);
            return Either.right(place != null ? mapper.modelToResponseDto(place) : null);
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID, null)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURISTIC_PLACE, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> findByName(String email, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<TouristicPlace> places = repository.findByNameStartingWithIgnoreCase(email, pageable);
            return Either.right(places.map(mapper::modelToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID, null)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURISTIC_PLACE, e.getMessage())});
        }

    }

    @Override
    public Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> findByRegion(Region region, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<TouristicPlace> places = repository.findByRegion(region, pageable);
            return Either.right(places.map(mapper::modelToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID, null)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURISTIC_PLACE, e.getMessage())});
        }
    }

    private List<TouristicPlaceCategory> transformCategoriesToTouristicPlaceCategory(TouristicPlaceRequestDTO touristicPlaceDto, TouristicPlace place) {
        List<TouristicPlaceCategory> categories = new ArrayList<>();
        for (Category categoryDto : touristicPlaceDto.getCategories()) {

            Category category = categoryRepository.findById(categoryDto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryDto.getId()));

            TouristicPlaceCategoryId id = new TouristicPlaceCategoryId(place.getId(), categoryDto.getId());
            TouristicPlaceCategory touristicPlaceCategory = new TouristicPlaceCategory();
            touristicPlaceCategory.setId(id);
            touristicPlaceCategory.setTouristicPlace(place);
            touristicPlaceCategory.setCategory(category);
            categories.add(touristicPlaceCategory);
        }
        return categories;
    }
}
