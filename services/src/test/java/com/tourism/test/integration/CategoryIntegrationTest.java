package com.tourism.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.model.Category;
import com.tourism.repository.CategoryRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CategoryIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private CategoryRepository categoryRepository;

   @Test
   @DisplayName("create Category successfully")
   @WithMockUser(roles = "ADMIN")
   void createCategoryIntegrationTest() throws Exception {
      Category category = new Category();
      category.setName("Test Category");
      String url = "/v1/category";

      mockMvc
            .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category)))
            .andExpect(status().isOk());

      Optional<Category> saved = categoryRepository
            .findByNameStartingWithIgnoreCaseOrderByNameAsc("Test Category", Pageable.unpaged())
            .getContent()
            .stream()
            .filter(c -> c.getName().equals("Test Category"))
            .findFirst();
      assertTrue(saved.isPresent());
   }

   @Test
   @DisplayName("create Category wrong role")
   @WithMockUser(roles = "TOURIST")
   void createCategoryForbiddenTest() throws Exception {
      Category category = new Category();
      category.setName("Fail Category");
      String url = "/v1/category";

      mockMvc
            .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category)))
            .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("findAll Categories successfully")
   @WithMockUser(roles = "ADMIN")
   void findAllCategoriesIntegrationTest() throws Exception {
      categoryRepository.save(new Category(1, "Category A", true));
      String url = "/v1/category";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].content").exists());
   }

   @Test
   @DisplayName("findAll Categories with tourist role")
   @WithMockUser(roles = "TOURIST")
   void findAllCategoriesWithTouristRoleTest() throws Exception {
      String url = "/v1/category";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
   }

   @Test
   @DisplayName("update Category successfully")
   @WithMockUser(roles = "ADMIN")
   void updateCategoryIntegrationTest() throws Exception {
      Category category = categoryRepository.save(new Category(1, "Original Name", true));
      category.setName("Updated Name");
      String url = "/v1/category";

      mockMvc.perform(put(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category))).andExpect(status().isOk());

      Optional<Category> updated = categoryRepository.findById(category.getId());
      assertTrue(updated.isPresent());
      assertEquals("Updated Name", updated.get().getName());
   }

   @Test
   @DisplayName("update Category wrong role")
   @WithMockUser(roles = "TOURIST")
   void updateCategoryForbiddenTest() throws Exception {
      Category category = new Category();
      category.setId(1);
      category.setName("Test");
      String url = "/v1/category";

      mockMvc
            .perform(put(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category)))
            .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("update Category null id")
   @WithMockUser(roles = "ADMIN")
   void updateCategoryNullIdTest() throws Exception {
      Category category = new Category();
      category.setName("No Id Category");
      String url = "/v1/category";

      mockMvc
            .perform(put(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category)))
            .andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("getById Category successfully")
   @WithMockUser(roles = "ADMIN")
   void getByIdCategoryIntegrationTest() throws Exception {
      Category category = categoryRepository.save(new Category(1, "ById Category", true));
      String url = "/v1/category/" + category.getId();

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("ById Category"));
   }

   @Test
   @DisplayName("getById Category with tourist role")
   @WithMockUser(roles = "TOURIST")
   void getByIdCategoryWithTouristRoleTest() throws Exception {
      Category category = categoryRepository.save(new Category(1, "Tourist Access", true));
      String url = "/v1/category/" + category.getId();

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
   }

   @Test
   @DisplayName("findByName Category successfully")
   @WithMockUser(roles = "ADMIN")
   void findByNameCategoryIntegrationTest() throws Exception {
      categoryRepository.save(new Category(1, "Searchable Name", true));
      String url = "/v1/category/findByName?name=Searchable";

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].content[0].name").value("Searchable Name"));
   }

   @Test
   @DisplayName("findByName Category with lodging owner role")
   @WithMockUser(roles = "LODGING_OWNER")
   void findByNameCategoryWithLodgingOwnerRoleTest() throws Exception {
      String url = "/v1/category/findByName?name=test";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
   }

   @Test
   @DisplayName("delete Category successfully")
   @WithMockUser(roles = "ADMIN")
   void deleteCategoryIntegrationTest() throws Exception {
      Category category = categoryRepository.save(new Category(1, "To Delete", true));
      String url = "/v1/category/" + category.getId();

      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

      assertTrue(categoryRepository.findById(category.getId()).isEmpty());
   }

   @Test
   @DisplayName("delete Category not found")
   @WithMockUser(roles = "ADMIN")
   void deleteCategoryNotFoundTest() throws Exception {
      String url = "/v1/category/999999";

      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("delete Category wrong role")
   @WithMockUser(roles = "TOURIST")
   void deleteCategoryForbiddenTest() throws Exception {
      String url = "/v1/category/1";

      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

}
