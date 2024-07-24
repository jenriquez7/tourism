package com.tourism.service;

import com.tourism.dto.request.LodgingOwnerRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.model.LodgingOwner;
import io.vavr.control.Either;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LodgingOwnerService {

    Either<ErrorDto[], LodgingOwnerResponseDTO> create(LodgingOwnerRequestDTO user);

    Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> findAll(PageableRequest paging);

    Either<ErrorDto[], LodgingOwner> delete(UUID id);

    Either<ErrorDto[], LodgingOwnerResponseDTO> getById(UUID id);

    Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> findByEmail(String email, PageableRequest paging);

    Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> findByLastName(String email, PageableRequest paging);
}
