package com.tourism.service;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.model.Tourist;
import io.vavr.control.Either;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface TouristService {

    Either<ErrorDto[], TouristResponseDTO> create(TouristRequestDTO user);

    Either<ErrorDto[], Page<TouristResponseDTO>> findAll(PageableRequest paging);

    Either<ErrorDto[], Tourist> delete(UUID id);

    Either<ErrorDto[], TouristResponseDTO> getById(UUID id);

    Either<ErrorDto[], Page<TouristResponseDTO>> findByEmail(String email, PageableRequest paging);

    Either<ErrorDto[], Page<TouristResponseDTO>> findByLastName(String email, PageableRequest paging);
}
