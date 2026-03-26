package com.tourism.service.impl;

import java.util.NoSuchElementException;

import io.vavr.control.Either;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Category;
import com.tourism.repository.CategoryRepository;
import com.tourism.service.CategoryService;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

   private final CategoryRepository repository;

   private final PageService pageService;

   @Override
   public Either<ErrorDto[], Category> create(Category category) {
      try {
         return Either.right(repository.save(category));
      } catch (DataIntegrityViolationException e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.CONFLICT, MessageConstants.GENERIC_ERROR, e.getMessage()) });
      } catch (Exception e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CATEGORY_NOT_CREATED, e.getMessage()) });
      }
   }

   @Override
   public Either<ErrorDto[], Category> update(Category category) {
      try {
         if (category.getId() != null) {
            return Either.right(repository.save(category));
         } else {
            return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.NULL_ID) });
         }
      } catch (DataIntegrityViolationException e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.CONFLICT, MessageConstants.ERROR_CATEGORY_NOT_UPDATED, e.getMessage()) });
      } catch (Exception e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CATEGORY_NOT_UPDATED, e.getMessage()) });
      }
   }

   @Override
   public Either<ErrorDto[], Page<Category>> findAll(PageableRequest paging) {
      try {
         Pageable pageable = pageService.createSortedPageable(paging);
         Page<Category> categories = repository.findAllByOrderByNameAsc(pageable);
         return Either.right(categories);
      } catch (Exception e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.GENERIC_ERROR) });
      }
   }

   @Override
   public Either<ErrorDto[], Category> delete(Integer id) {
      try {
         Either<ErrorDto[], Category> category = this.getById(id);
         repository.delete(category.get());
         return Either.right(null);
      } catch (NoSuchElementException | InvalidDataAccessApiUsageException e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.BAD_REQUEST, MessageConstants.ERROR_CATEGORY_NOT_FOUND) });
      } catch (Exception e) {
         log.error(e.getMessage());
         return Either.left(
               new ErrorDto[] { ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_DELETING_CATEGORY, e.getMessage()) });
      }
   }

   @Override
   public Either<ErrorDto[], Category> getById(Integer id) {
      try {
         return Either.right(repository.findById(id).orElse(null));
      } catch (InvalidDataAccessApiUsageException e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.NULL_ID) });
      } catch (Exception e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_CATEGORY, e.getMessage()) });
      }
   }

   @Override
   public Either<ErrorDto[], Page<Category>> findByName(String name, PageableRequest paging) {
      try {
         Pageable pageable = pageService.createSortedPageable(paging);
         return Either.right(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name, pageable));
      } catch (InvalidDataAccessApiUsageException e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.NOT_FOUND, MessageConstants.NULL_NAME) });
      } catch (Exception e) {
         log.error(e.getMessage());
         return Either.left(new ErrorDto[] { ErrorDto.of(HttpStatus.INTERNAL_SERVER_ERROR, MessageConstants.ERROR_GET_CATEGORY, e.getMessage()) });
      }
   }

}
