package com.tourism.service;

import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.AdminResponseDTO;
import io.vavr.control.Either;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AdminService {

    Either<ErrorDto[], AdminResponseDTO> create(AuthUserDto user);

    Either<ErrorDto[], Page<AdminResponseDTO>> findAll(PageableRequest paging);

    Either<ErrorDto[], AdminResponseDTO> delete(UUID id);

    Either<ErrorDto[], AdminResponseDTO> getById(UUID id);

    Either<ErrorDto[], Page<AdminResponseDTO>> findByEmail(String email, PageableRequest paging);
}
