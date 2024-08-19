package com.tourism.service.impl;

import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Category;
import com.tourism.repository.CategoryRepository;
import com.tourism.service.CategoryService;
import com.tourism.util.MessageConstants;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Either<ErrorDto[], Category> create(Category category) {
        try {
            return Either.right(repository.save(category));
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.GENERIC_ERROR, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CATEGORY_NOT_CREATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Category> update(Category category) {
        try {
            if (category.getId() != null) {
                return Either.right(repository.save(category));
            } else {
                return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.NULL_ID)});
            }
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.CONFLICT, MessageConstants.ERROR_CATEGORY_NOT_UPDATED, e.getMessage())});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CATEGORY_NOT_UPDATED, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Category[]> findAll() {
        try {
            return Either.right(repository.findAllByOrderByNameAsc());
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.GENERIC_ERROR)});
        }
    }

    @Override
    public Either<ErrorDto[], Category> delete(Integer id) {
        try {
            Either<ErrorDto[], Category> category =  this.getById(id);
            repository.delete(category.get());
            return Either.right(null);
        } catch (NoSuchElementException | InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CATEGORY_NOT_FOUND)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_DELETING_CATEGORY, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Category> getById(Integer id) {
        try {
            return Either.right(repository.findById(id).orElse(null));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_CATEGORY, e.getMessage())});
        }
    }

    @Override
    public Either<ErrorDto[], Category[]> findByName(String name) {
        try {
            return Either.right(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name));
        } catch (InvalidDataAccessApiUsageException e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.NOT_FOUND, MessageConstants.NULL_NAME)});
        } catch (Exception e) {
            log.error(e.getMessage());
            return Either.left(new ErrorDto[]{new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_CATEGORY, e.getMessage())});
        }
    }
}
