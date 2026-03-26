package com.tourism.test.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import io.vavr.control.Either;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Category;
import com.tourism.repository.CategoryRepository;
import com.tourism.service.impl.CategoryServiceImpl;
import com.tourism.util.MessageConstants;
import com.tourism.util.PageService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

   @Mock
   private CategoryRepository repository;

   @Mock
   private PageService pageService;

   @InjectMocks
   private CategoryServiceImpl categoryService;

   private Category category;

   @BeforeEach
   void setUp() {
      category = new Category(1, "Playa", true);
   }

   @Test
   @DisplayName("Create Category - Success")
   void createCategorySuccess() {
      when(repository.save(any(Category.class))).thenReturn(category);

      Either<ErrorDto[], Category> result = categoryService.create(category);

      assertTrue(result.isRight());
      assertEquals(category, result.get());
   }

   @Test
   @DisplayName("Create Category - Data Integrity Violation Exception")
   void createCategoryDataIntegrityViolationException() {
      when(repository.save(any(Category.class))).thenThrow(new DataIntegrityViolationException("Error"));

      Either<ErrorDto[], Category> result = categoryService.create(category);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.CONFLICT, errors[0].code());
      assertEquals(MessageConstants.GENERIC_ERROR, errors[0].message());
   }

   @Test
   @DisplayName("Create Category - Generic Exception")
   void createCategoryGenericException() {
      when(repository.save(any(Category.class))).thenThrow(new RuntimeException("Error"));

      Either<ErrorDto[], Category> result = categoryService.create(category);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals(MessageConstants.ERROR_CATEGORY_NOT_CREATED, errors[0].message());
   }

   @Test
   @DisplayName("Update Category - Success")
   void updateCategorySuccess() {
      when(repository.save(any(Category.class))).thenReturn(category);

      Either<ErrorDto[], Category> result = categoryService.update(category);

      assertTrue(result.isRight());
      assertEquals(category, result.get());
   }

   @Test
   @DisplayName("Update Category - Null Id")
   void updateCategoryNullId() {
      category.setId(null);
      Either<ErrorDto[], Category> result = categoryService.update(category);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals(MessageConstants.NULL_ID, errors[0].message());
   }

   @Test
   @DisplayName("Update Category - Data Integrity Violation Exception")
   void updateCategoryDataIntegrityViolationException() {
      when(repository.save(any(Category.class))).thenThrow(new DataIntegrityViolationException("Error"));

      Either<ErrorDto[], Category> result = categoryService.update(category);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.CONFLICT, errors[0].code());
      assertEquals(MessageConstants.ERROR_CATEGORY_NOT_UPDATED, errors[0].message());
   }

   @Test
   @DisplayName("Update Category - Generic Exception")
   void updateCategoryGenericException() {
      when(repository.save(any(Category.class))).thenThrow(new RuntimeException("Error"));

      Either<ErrorDto[], Category> result = categoryService.update(category);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals(MessageConstants.ERROR_CATEGORY_NOT_UPDATED, errors[0].message());
   }

   @Test
   @DisplayName("Find All Categories - Success")
   void findAllSuccess() {
      Category otherCategory = new Category();
      otherCategory.setName("Ciudad");
      List<Category> categories = List.of(category, otherCategory);
      Page<Category> page = new PageImpl<>(categories);
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findAllByOrderByNameAsc(Pageable.unpaged())).thenReturn(page);

      Either<ErrorDto[], Page<Category>> result = categoryService.findAll(paging);

      assertTrue(result.isRight());
      assertEquals(2, result.get().getTotalElements());
      assertEquals(category, result.get().getContent().get(0));
   }

   @Test
   @DisplayName("Find All Categories - Empty List")
   void findAllEmptyList() {
      Page<Category> emptyPage = new PageImpl<>(Collections.emptyList());
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findAllByOrderByNameAsc(Pageable.unpaged())).thenReturn(emptyPage);

      Either<ErrorDto[], Page<Category>> result = categoryService.findAll(paging);

      assertTrue(result.isRight());
      assertEquals(0, result.get().getTotalElements());
   }

   @Test
   @DisplayName("Find All Categories - Exception")
   void findAllException() {
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findAllByOrderByNameAsc(Pageable.unpaged())).thenThrow(new RuntimeException("Database error"));

      Either<ErrorDto[], Page<Category>> result = categoryService.findAll(paging);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals(MessageConstants.GENERIC_ERROR, errors[0].message());
   }

   @Test
   @DisplayName("Delete Category - Success")
   void deleteSuccess() {
      when(repository.findById(category.getId())).thenReturn(Optional.of(category));
      doNothing().when(repository).delete(category);

      Either<ErrorDto[], Category> result = categoryService.delete(category.getId());

      assertTrue(result.isRight());
      assertNull(result.get());
      verify(repository, times(1)).delete(category);
   }

   @Test
   @DisplayName("Delete Category - Invalid Data Access Api Usage Exception")
   void deleteCategoryNotFound() {
      when(repository.findById(category.getId())).thenThrow(new InvalidDataAccessApiUsageException("Error"));

      Either<ErrorDto[], Category> result = categoryService.delete(category.getId());

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals(MessageConstants.ERROR_CATEGORY_NOT_FOUND, errors[0].message());
   }

   @Test
   @DisplayName("Delete Category - No Such Element Exception")
   void deleteNoSuchElementException() {
      when(categoryService.getById(category.getId())).thenThrow(new NoSuchElementException());

      Either<ErrorDto[], Category> result = categoryService.delete(category.getId());

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.BAD_REQUEST, errors[0].code());
      assertEquals(MessageConstants.ERROR_CATEGORY_NOT_FOUND, errors[0].message());
   }

   @Test
   @DisplayName("Delete Category - Generic Exception")
   void deleteGenericException() {
      when(repository.findById(category.getId())).thenReturn(Optional.of(category));
      doThrow(new RuntimeException("Error")).when(repository).delete(category);

      Either<ErrorDto[], Category> result = categoryService.delete(category.getId());

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
      assertEquals(MessageConstants.ERROR_DELETING_CATEGORY, errors[0].message());
   }

   @Test
   @DisplayName("Get Category By ID - Success")
   void getByIdSuccess() {
      when(repository.findById(1)).thenReturn(Optional.of(category));

      Either<ErrorDto[], Category> result = categoryService.getById(1);

      assertTrue(result.isRight());
      assertEquals(category, result.get());
      assertEquals(category.getName(), result.get().getName());
   }

   @Test
   @DisplayName("Get Category By ID - Not Found")
   void getByIdNotFound() {
      when(repository.findById(2)).thenReturn(Optional.empty());

      Either<ErrorDto[], Category> result = categoryService.getById(2);

      assertTrue(result.isRight());
      assertNull(result.get());
   }

   @Test
   @DisplayName("Get Category By ID - InvalidDataAccessApiUsageException")
   void getByIdInvalidDataAccessApiUsageException() {
      when(repository.findById(null)).thenThrow(new InvalidDataAccessApiUsageException("Invalid Id"));

      Either<ErrorDto[], Category> result = categoryService.getById(null);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
      assertEquals(MessageConstants.NULL_ID, errors[0].message());
   }

   @Test
   @DisplayName("Get Category By ID - Generic Exception")
   void getByIdGenericException() {
      Integer id = 1;
      when(repository.findById(id)).thenThrow(new RuntimeException("Database error"));

      Either<ErrorDto[], Category> result = categoryService.getById(id);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
      assertEquals(MessageConstants.ERROR_GET_CATEGORY, errors[0].message());
   }

   @Test
   @DisplayName("Find Categories By Name - Success")
   void findByNameSuccess() {
      String name = "Test";
      List<Category> categories = List.of(new Category(1, "Test Category 1", true), new Category(2, "Test Category 2", true));
      Page<Category> page = new PageImpl<>(categories);
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name, Pageable.unpaged())).thenReturn(page);

      Either<ErrorDto[], Page<Category>> result = categoryService.findByName(name, paging);

      assertTrue(result.isRight());
      assertEquals(2, result.get().getTotalElements());
      assertEquals("Test Category 1", result.get().getContent().get(0).getName());
   }

   @Test
   @DisplayName("Find Categories By Name - Empty Result")
   void findByNameEmptyResult() {
      String name = "Nonexistent";
      Page<Category> emptyPage = new PageImpl<>(Collections.emptyList());
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name, Pageable.unpaged())).thenReturn(emptyPage);

      Either<ErrorDto[], Page<Category>> result = categoryService.findByName(name, paging);

      assertTrue(result.isRight());
      assertEquals(0, result.get().getTotalElements());
   }

   @Test
   @DisplayName("Find Categories By Name - InvalidDataAccessApiUsageException")
   void findByNameInvalidDataAccessApiUsageException() {
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(null, Pageable.unpaged())).thenThrow(new InvalidDataAccessApiUsageException("Invalid name"));

      Either<ErrorDto[], Page<Category>> result = categoryService.findByName(null, paging);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.NOT_FOUND, errors[0].code());
      assertEquals(MessageConstants.NULL_NAME, errors[0].message());
   }

   @Test
   @DisplayName("Find Categories By Name - Generic Exception")
   void findByNameGenericException() {
      String name = "Test";
      PageableRequest paging = new PageableRequest(0, 10, new String[] { "name" }, Sort.Direction.ASC);

      when(pageService.createSortedPageable(paging)).thenReturn(Pageable.unpaged());
      when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name, Pageable.unpaged())).thenThrow(new RuntimeException("Database error"));

      Either<ErrorDto[], Page<Category>> result = categoryService.findByName(name, paging);

      assertTrue(result.isLeft());
      ErrorDto[] errors = result.getLeft();
      assertEquals(1, errors.length);
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].code());
      assertEquals(MessageConstants.ERROR_GET_CATEGORY, errors[0].message());
   }

}
