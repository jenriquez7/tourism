package com.tourism.test.controller;

import com.tourism.controller.CategoryController;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.model.Category;
import com.tourism.service.CategoryService;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CategoryControllerTests {

    @Mock
    HttpServletRequest request;

    @Mock
    private CategoryService service;

    @InjectMocks
    private CategoryController controller;

    private Category category;
    private Category otherCategory;

    @BeforeEach
    void setUp() {
        category = new Category(1, "Playa", true);
        otherCategory = new Category(2, "Ciudad", true);
    }


    @Test
    @DisplayName("Create Category")
    void create() {
        when(service.create(any(Category.class))).thenReturn(Either.right(category));

        ResponseEntity<StandardResponseDto<Category>> response = controller.create(request, category);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StandardResponseDto<Category> body = response.getBody();
        assertNotNull(body);

        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);

        assertInstanceOf(Category.class, data[0]);
        Category resultCategory = (Category) data[0];
        assertEquals(category.getId(), resultCategory.getId());
        assertEquals(category.getName(), resultCategory.getName());
        assertEquals(category.getEnabled(), resultCategory.getEnabled());

        verify(service, times(1)).create(any(Category.class));
    }

    @Test
    @DisplayName("Find All Categories")
    void findAll() {
        Category[] categories = {category, otherCategory};
        when(service.findAll()).thenReturn(Either.right(categories));

        ResponseEntity<StandardResponseDto<Category>> response = controller.findAll(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Category> body = response.getBody();
        assertNotNull(body);
        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(categories.length, data.length);
        for (int i = 0; i < data.length; i++) {
            assertInstanceOf(Category.class, data[i]);
            assertEquals(categories[i], data[i]);
        }
        verify(service, times(1)).findAll();
    }

    @Test
    @DisplayName("Update Category")
    void update() {
        when(service.update(any(Category.class))).thenReturn(Either.right(category));

        ResponseEntity<StandardResponseDto<Category>> response = controller.update(request, category);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Category> body = response.getBody();
        assertNotNull(body);
        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Category.class, data[0]);
        assertEquals(category, data[0]);
        verify(service, times(1)).update(any(Category.class));
    }

    @Test
    @DisplayName("Get Category By Id")
    void getById() {
        when(service.getById(anyInt())).thenReturn(Either.right(category));

        ResponseEntity<StandardResponseDto<Category>> response = controller.getById(request, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Category> body = response.getBody();
        assertNotNull(body);
        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Category.class, data[0]);
        assertEquals(category, data[0]);
        verify(service, times(1)).getById(1);
    }

    @Test
    @DisplayName("Find Category By Name")
    void findByName() {
        Category[] categories = {category, otherCategory};
        when(service.findByName(anyString())).thenReturn(Either.right(categories));

        ResponseEntity<StandardResponseDto<Category>> response = controller.findByName(request, "Playa");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Category> body = response.getBody();
        assertNotNull(body);
        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(categories.length, data.length);
        assertInstanceOf(Category.class, data[0]);
        assertEquals(category, data[0]);
        verify(service, times(1)).findByName("Playa");
    }

    @Test
    @DisplayName("Delete Category")
    void delete() {
        when(service.delete(anyInt())).thenReturn(Either.right(category));

        ResponseEntity<StandardResponseDto<Category>> response = controller.delete(request, 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        StandardResponseDto<Category> body = response.getBody();
        assertNotNull(body);
        Object[] data = body.getData();
        assertNotNull(data);
        assertEquals(1, data.length);
        assertInstanceOf(Category.class, data[0]);
        assertEquals(category, data[0]);
        verify(service, times(1)).delete(1);
    }
}
