package com.tourism.service;

import com.tourism.dto.request.LodgingRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingResponseDTO;
import com.tourism.model.Lodging;
import io.vavr.control.Either;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LodgingService {

    Either<ErrorDto[], LodgingResponseDTO> create(LodgingRequestDTO lodging, UUID ownerId);
    Either<ErrorDto[], LodgingResponseDTO> update(Lodging lodging, UUID ownerId);
    Either<ErrorDto[], Page<LodgingResponseDTO>> findAll(PageableRequest paging);
    Either<ErrorDto[], Lodging> delete(UUID id);
    Either<ErrorDto[], LodgingResponseDTO> getById(UUID id);
    Either<ErrorDto[], Page<LodgingResponseDTO>> findLodgingsByTouristicPlace(UUID id, PageableRequest paging);
}
