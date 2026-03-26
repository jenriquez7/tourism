package com.tourism.test.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.tourism.controller.CategoryController;
import com.tourism.dto.request.PageableRequest;
import com.tourism.dto.response.StandardResponseDto;
import com.tourism.model.Category;
import com.tourism.service.CategoryService;

import jakarta.servlet.http.HttpServletRequest;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

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
      List<Category> categories = List.of(category, otherCategory);
      Page<Category> page = new PageImpl<>(categories);

      when(service.findAll(any(PageableRequest.class))).thenReturn(Either.right(page));

      PageableRequest pageableRequest = new PageableRequest(0, 10, new String[] { "id" }, Sort.Direction.ASC);
      ResponseEntity<StandardResponseDto<Page<Category>>> response = controller.findAll(request, pageableRequest);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      StandardResponseDto<Page<Category>> body = response.getBody();
      assertNotNull(body);
      Object[] data = body.getData();
      assertNotNull(data);
      assertEquals(1, data.length);
      assertInstanceOf(Page.class, data[0]);

      Page<Category> resultPage = (Page<Category>) data[0];
      assertEquals(2, resultPage.getTotalElements());
      assertEquals(2, resultPage.getContent().size());

      List<Category> resultCategories = resultPage.getContent();
      assertEquals(category.getId(), resultCategories.get(0).getId());
      assertEquals(category.getName(), resultCategories.get(0).getName());
      assertEquals(otherCategory.getId(), resultCategories.get(1).getId());
      assertEquals(otherCategory.getName(), resultCategories.get(1).getName());

      verify(service, times(1)).findAll(argThat(
            req -> req.getPage() == 0 && req.getSize() == 10 && Arrays.equals(req.getSort(), new String[] { "id" })
                  && req.getSortType() == Sort.Direction.ASC));
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

      Category resultCategory = (Category) data[0];
      assertEquals(category.getId(), resultCategory.getId());
      assertEquals(category.getName(), resultCategory.getName());

      verify(service, times(1)).update(any(Category.class));
   }

   @Test
   @DisplayName("Get Category By Id")
   void getById() {
      when(service.getById(category.getId())).thenReturn(Either.right(category));

      ResponseEntity<StandardResponseDto<Category>> response = controller.getById(request, category.getId());

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

      verify(service, times(1)).getById(category.getId());
   }

   @Test
   @DisplayName("Find Category By Name")
   void findByName() {
      List<Category> categories = List.of(category, otherCategory);
      Page<Category> page = new PageImpl<>(categories);

      PageableRequest pageableRequest = new PageableRequest(0, 10, new String[] { "id" }, Sort.Direction.ASC);

      when(service.findByName(eq("Playa"), any(PageableRequest.class))).thenReturn(Either.right(page));

      ResponseEntity<StandardResponseDto<Page<Category>>> response = controller.findByName(request, "Playa", pageableRequest);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      StandardResponseDto<Page<Category>> body = response.getBody();
      assertNotNull(body);
      Object[] data = body.getData();
      assertNotNull(data);
      assertEquals(1, data.length);
      assertInstanceOf(Page.class, data[0]);

      Page<Category> resultPage = (Page<Category>) data[0];
      assertEquals(2, resultPage.getTotalElements());
      assertEquals(2, resultPage.getContent().size());

      List<Category> resultCategories = resultPage.getContent();
      assertEquals(category.getId(), resultCategories.get(0).getId());
      assertEquals(category.getName(), resultCategories.get(0).getName());
      assertEquals(otherCategory.getId(), resultCategories.get(1).getId());
      assertEquals(otherCategory.getName(), resultCategories.get(1).getName());

      verify(service, times(1)).findByName(eq("Playa"), argThat(
            req -> req.getPage() == 0 && req.getSize() == 10 && Arrays.equals(req.getSort(), new String[] { "id" })
                  && req.getSortType() == Sort.Direction.ASC));
   }

   @Test
   @DisplayName("Delete Category")
   void delete() {
      when(service.delete(category.getId())).thenReturn(Either.right(category));

      ResponseEntity<StandardResponseDto<Category>> response = controller.delete(request, category.getId());

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

      verify(service, times(1)).delete(category.getId());
   }

}
