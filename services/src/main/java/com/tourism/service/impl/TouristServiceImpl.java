package com.tourism.service.impl;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.request.TouristRequestDTO;
import com.tourism.dto.response.ErrorDto;
import com.tourism.dto.response.TouristResponseDTO;
import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Role;
import com.tourism.model.Tourist;
import com.tourism.model.TouristType;
import com.tourism.model.User;
import com.tourism.repository.RefreshTokenRepository;
import com.tourism.repository.TouristRepository;
import com.tourism.repository.UserRepository;
import com.tourism.service.TouristService;
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

import java.util.*;

@Slf4j
@Service
public class TouristServiceImpl implements TouristService {

    private final TouristRepository repository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;
    private final PasswordEncryptionService encryptionService;
    private final UserValidation userValidation;
    private final PageService pageService;

    @Autowired
    public TouristServiceImpl(TouristRepository repository, UserRepository userRepository, RefreshTokenRepository tokenRepository,
                              PasswordEncryptionService encryptionService, UserValidation userValidation, PageService pageService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.encryptionService = encryptionService;
        this.userValidation = userValidation;
        this.pageService = pageService;
    }

    @Override
    public Either<ErrorDto[], TouristResponseDTO> create(TouristRequestDTO userDto) {
        try {
            Either<ErrorDto[], Boolean> validation = userValidation.validateEmailAndPassword(userDto.getEmail(), userDto.getPassword());
            if (validation.isRight()) {
                Tourist tourist = repository.save(new Tourist(
                        userDto.getEmail(),
                        encryptionService.encryptPassword(userDto.getPassword()),
                        userDto.getFirstName(),
                        userDto.getLastName(),
                        Role.TOURIST,
                        TouristType.STANDARD,
                        true
                ));
                return Either.right(tourist.getId() != null ? TouristResponseDTO.touristToResponseDto(tourist) : null);
            } else {
                return Either.left(validation.getLeft());
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.ERROR_TOURIST_NOT_CREATED)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_TOURIST_NOT_CREATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<TouristResponseDTO>> findAll(PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Tourist> touristsPage = repository.findAll(pageable);
            return Either.right(touristsPage.map(TouristResponseDTO::touristToResponseDto));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURISTS, e.getMessage())});
        }
    }

    @Override
    @Transactional
    public Either<ErrorDto[], Tourist> delete(UUID id) {
        try {
            User user = userRepository.findById(id).orElse(null);
            Tourist tourist = repository.findById(id).orElse(null);
            tokenRepository.deleteByUser(user);
            repository.delete(Objects.requireNonNull(tourist));
            return Either.right(null);
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.NULL_ID)});
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.ERROR_DELETING_TOURIST)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURIST, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], TouristResponseDTO> getById(UUID id) {
        try {
            return Either.right(TouristResponseDTO.touristToResponseDto(Objects.requireNonNull(repository.findById(id).orElse(null))));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURIST, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<TouristResponseDTO>> findByEmail(String email, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Tourist> tourists = repository.findByEmailStartingWithIgnoreCase(email, pageable);
            return Either.right(tourists.map(TouristResponseDTO::touristToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_EMAIL)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURISTS, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Page<TouristResponseDTO>> findByLastName(String lastName, PageableRequest paging) {
        try {
            Pageable pageable = pageService.createSortedPageable(paging);
            Page<Tourist> tourists = repository.findByLastNameStartingWithIgnoreCase(lastName, pageable);
            return Either.right(tourists.map(TouristResponseDTO::touristToResponseDto));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_LAST_NAME)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_TOURIST, e.getMessage())});
        }
    }

}
