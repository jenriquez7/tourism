package com.tourism.test.service;

import com.tourism.dto.response.ErrorDto;
import com.tourism.model.Category;
import com.tourism.repository.CategoryRepository;
import com.tourism.service.impl.CategoryServiceImpl;
import com.tourism.util.MessageConstants;
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
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {

    @Mock
    private CategoryRepository repository;

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
        assertEquals(HttpStatus.CONFLICT, errors[0].getCode());
        assertEquals(MessageConstants.GENERIC_ERROR, errors[0].getMessage());
    }

    @Test
    @DisplayName("Create Category - Generic Exception")
    void createCategoryGenericException() {
        when(repository.save(any(Category.class))).thenThrow(new RuntimeException("Error"));

        Either<ErrorDto[], Category> result = categoryService.create(category);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_CREATED, errors[0].getMessage());
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
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.NULL_ID, errors[0].getMessage());
    }

    @Test
    @DisplayName("Update Category - Data Integrity Violation Exception")
    void updateCategoryDataIntegrityViolationException() {
        when(repository.save(any(Category.class))).thenThrow(new DataIntegrityViolationException("Error"));

        Either<ErrorDto[], Category> result = categoryService.update(category);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.CONFLICT, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_UPDATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Update Category - Generic Exception")
    void updateCategoryGenericException() {
        when(repository.save(any(Category.class))).thenThrow(new RuntimeException("Error"));

        Either<ErrorDto[], Category> result = categoryService.update(category);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_UPDATED, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find All Categories - Success")
    void findAllSuccess() {
        Category otherCategory = new Category();
        otherCategory.setName("Ciudad");
        Category[] categories = {category, otherCategory};

        when(repository.findAllByOrderByNameAsc()).thenReturn(categories);

        Either<ErrorDto[], Category[]> result = categoryService.findAll();

        assertTrue(result.isRight());
        assertArrayEquals(categories, result.get());
        assertTrue(Arrays.stream(result.get()).anyMatch(c -> c.equals(category)));
    }

    @Test
    @DisplayName("Find All Categories - Empty List")
    void findAllEmptyList() {
        Category[] emptyCategories = {};

        when(repository.findAllByOrderByNameAsc()).thenReturn(emptyCategories);

        Either<ErrorDto[], Category[]> result = categoryService.findAll();

        assertTrue(result.isRight());
        assertArrayEquals(emptyCategories, result.get());
    }

    @Test
    @DisplayName("Find All Categories - Exception")
    void findAllException() {
        when(repository.findAllByOrderByNameAsc()).thenThrow(new RuntimeException("Database error"));

        Either<ErrorDto[], Category[]> result = categoryService.findAll();

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.GENERIC_ERROR, errors[0].getMessage());
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
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_FOUND, errors[0].getMessage());
    }

    @Test
    @DisplayName("Delete Category - No Such Element Exception")
    void deleteNoSuchElementException() {
        when(categoryService.getById(category.getId())).thenThrow(new NoSuchElementException());

        Either<ErrorDto[], Category> result = categoryService.delete(category.getId());

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.BAD_REQUEST, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_CATEGORY_NOT_FOUND, errors[0].getMessage());
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_DELETING_CATEGORY, errors[0].getMessage());
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
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.NULL_ID, errors[0].getMessage());
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_CATEGORY, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find Categories By Name - Success")
    void findByNameSuccess() {
        String name = "Test";
        Category[] expectedCategories = {
                new Category(1, "Test Category 1", true),
                new Category(2, "Test Category 2", true)
        };
        when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name)).thenReturn(expectedCategories);

        Either<ErrorDto[], Category[]> result = categoryService.findByName(name);

        assertTrue(result.isRight());
        assertArrayEquals(expectedCategories, result.get());
    }

    @Test
    @DisplayName("Find Categories By Name - Empty Result")
    void findByNameEmptyResult() {
        String name = "Nonexistent";
        when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name)).thenReturn(new Category[0]);

        Either<ErrorDto[], Category[]> result = categoryService.findByName(name);

        assertTrue(result.isRight());
        assertArrayEquals(new Category[0], result.get());
    }

    @Test
    @DisplayName("Find Categories By Name - InvalidDataAccessApiUsageException")
    void findByNameInvalidDataAccessApiUsageException() {
        when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(null)).thenThrow(new InvalidDataAccessApiUsageException("Invalid name"));

        Either<ErrorDto[], Category[]> result = categoryService.findByName(null);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.NOT_FOUND, errors[0].getCode());
        assertEquals(MessageConstants.NULL_NAME, errors[0].getMessage());
    }

    @Test
    @DisplayName("Find Categories By Name - Generic Exception")
    void findByNameGenericException() {
        String name = "Test";
        when(repository.findByNameStartingWithIgnoreCaseOrderByNameAsc(name)).thenThrow(new RuntimeException("Database error"));

        Either<ErrorDto[], Category[]> result = categoryService.findByName(name);

        assertTrue(result.isLeft());
        ErrorDto[] errors = result.getLeft();
        assertEquals(1, errors.length);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errors[0].getCode());
        assertEquals(MessageConstants.ERROR_GET_CATEGORY, errors[0].getMessage());
    }
}
