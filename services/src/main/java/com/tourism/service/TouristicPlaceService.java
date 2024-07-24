package com.tourism.service;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristicPlaceRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.TouristicPlaceResponseDTO;
import com.tourism.model.Region;
import com.tourism.model.TouristicPlace;
import io.vavr.control.Either;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TouristicPlaceService {

    Either<ErrorDto[], TouristicPlaceResponseDTO> create(TouristicPlaceRequestDTO place, UUID userId);
    Either<ErrorDto[], TouristicPlaceResponseDTO> update(TouristicPlaceRequestDTO place);
    Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> findAll(PageableRequest paging);
    Either<ErrorDto[], TouristicPlace> delete(UUID id);
    Either<ErrorDto[], TouristicPlaceResponseDTO> getById(UUID id);
    Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> findByName(String name, PageableRequest paging);
    Either<ErrorDto[], Page<TouristicPlaceResponseDTO>> findByRegion(Region region, PageableRequest paging);
}
