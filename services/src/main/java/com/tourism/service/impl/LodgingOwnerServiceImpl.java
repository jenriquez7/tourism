package com.tourism.service.impl;

import com.tourism.dto.mappers.LodgingOwnerMapper;
import com.tourism.dto.request.LodgingOwnerRequestDTO;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.LodgingOwnerResponseDTO;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Role;
import com.tourism.model.LodgingOwner;
import com.tourism.model.User;
import com.tourism.repository.LodgingOwnerRepository;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.LodgingOwnerService;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;
import com.tourism.util.validations.UserValidation;
import io.vavr.control.Either;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class LodgingOwnerServiceImpl implements LodgingOwnerService {

    private final LodgingOwnerRepository repository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncryptionService encryptionService;
    private final UserValidation userValidation;
    private final PageService pageService;
    private final LodgingOwnerMapper mapper;

    @Autowired
    public LodgingOwnerServiceImpl(LodgingOwnerRepository repository, UserRepository userRepository, RefreshTokenRepository tokenRepository,
                                   PasswordEncryptionService encryptionService, UserValidation userValidation, PageService pageService,
                                   LodgingOwnerMapper mapper) {
        this.repository = repository;
        this.userValidation = userValidation;
        this.tokenRepository = tokenRepository;
        this.encryptionService = encryptionService;
        this.userRepository = userRepository;
        this.pageService = pageService;
        this.mapper = mapper;
    }

    @Override
    public Either<ErrorDto[], LodgingOwnerResponseDTO> create(LodgingOwnerRequestDTO userDto) {
        try {
            Either<ErrorDto[], Boolean> validation = userValidation.validateEmailAndPassword(userDto.getEmail(), userDto.getPassword());
            if (validation.isRight()) {
                LodgingOwner lodgingOwner = repository.save(new LodgingOwner(
                        userDto.getEmail(),
                        encryptionService.encryptPassword(userDto.getPassword()),
                        userDto.getFirstName(),
                        userDto.getLastName(),
                        Role.LODGING_OWNER,
                        true));
                return Either.right(lodgingOwner.getId() != null ? mapper.modelToResponseDto(lodgingOwner) : null);
            } else {
                return Either.left(validation.getLeft());
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.CONFLICT, MessageConstants.ERROR_LODGING_OWNER_NOT_CREATED, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_LODGING_OWNER_NOT_CREATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> findAll(PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<LodgingOwner> owners = repository.findAll(pageable);
            return Either.right(owners.map(mapper::modelToResponseDto));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.GENERIC_ERROR, e.getMessage())});
        }
    }

    @Override
    @Transactional
    public Either<ErrorDto[], LodgingOwner> delete(UUID id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            LodgingOwner lodgingOwner = repository.findById(id).orElse(null);
            tokenRepository.deleteByUser(Objects.requireNonNull(user));
            repository.delete(Objects.requireNonNull(lodgingOwner));
            return Either.right(null);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.NULL_ID)});
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.ERROR_DELETING_LODGING_OWNER)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_DELETING_LODGING_OWNER, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], LodgingOwnerResponseDTO> getById(UUID id) {
        try {
            return Either.right(mapper.modelToResponseDto(Objects.requireNonNull(repository.findById(id).orElse(null))));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_LODGING_OWNER, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> findByEmail(String email, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<LodgingOwner> owners = repository.findByEmailStartingWithIgnoreCase(email, pageable);
            return Either.right(owners.map(mapper::modelToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.NULL_EMAIL)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_LODGING_OWNER, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<LodgingOwnerResponseDTO>> findByLastName(String lastName, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<LodgingOwner> owners = repository.findByLastNameStartingWithIgnoreCase(lastName, pageable);
            return Either.right(owners.map(mapper::modelToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.NULL_LAST_NAME)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_LODGING_OWNER, e.getMessage())});
        }
    }
}
