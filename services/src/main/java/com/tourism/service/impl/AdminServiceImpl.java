package com.tourism.service.impl;

import com.tourism.dto.request.PageableRequest;
import com.tourism.model.Role;
import com.tourism.dto.request.AuthUserDto;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.AdminResponseDTO;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Admin;
import com.tourism.model.User;
import com.tourism.repository.AdminRepository;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.AdminService;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.UserValidation;
import io.vavr.control.Either;
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
public class AdminServiceImpl implements AdminService {

    private final AdminRepository repository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncryptionService encryptionService;
    private final UserValidation userValidation;
    private final PageService pageService;

    @Autowired
    public AdminServiceImpl(AdminRepository repository, UserRepository userRepository, RefreshTokenRepository tokenRepository,
                            PasswordEncryptionService encryptionService, UserValidation userValidation, PageService pageService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encryptionService = encryptionService;
        this.userValidation = userValidation;
        this.pageService = pageService;
    }

    @Override
    public Either<ErrorDto[], AdminResponseDTO> create(AuthUserDto userDto) {
        try {
            Either<ErrorDto[], Boolean> validation = userValidation.validateEmailAndPassword(userDto.getEmail(), userDto.getPassword());
            if (validation.isRight()) {
                Admin admin = repository.save(new Admin(userDto.getEmail(), encryptionService.encryptPassword(userDto.getPassword()), Role.ADMIN, true));
                return Either.right(admin.getId() != null ? AdminResponseDTO.adminToResponseDto(admin) : null);
            } else {
                return Either.left(validation.getLeft());
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.ERROR_ADMIN_NOT_CREATED, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CREATE_ADMIN, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<AdminResponseDTO>> findAll(PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Admin> adminsPage = repository.findAll(pageable);
            return Either.right(adminsPage.map(AdminResponseDTO::adminToResponseDto));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_ADMINS, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], AdminResponseDTO> delete(UUID id) {
        try {
            if (repository.count() > 1) {
                User user = userRepository.findById(id).orElse(null);
                Admin admin = repository.findById(id).orElse(null);
                tokenRepository.deleteByUser(user);
                repository.delete(Objects.requireNonNull(admin));
                return Either.right(null);
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CANNOT_DELETE_LAST_ADMIN)});
            }
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_ADMIN_NOT_FOUND)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_ADMINS, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], AdminResponseDTO> getById(UUID id) {
        try {
            return Either.right(AdminResponseDTO.adminToResponseDto(Objects.requireNonNull(repository.findById(id).orElse(null))));
        } catch (InvalidDataAccessApiUsageException e) {
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_ADMINS, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<AdminResponseDTO>> findByEmail(String email, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Admin> adminsPage = repository.findByEmailStartingWithIgnoreCase(email, pageable);
            return Either.right(adminsPage.map(AdminResponseDTO::adminToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_EMAIL)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_ADMINS, e.getMessage())});
        }
    }
}
