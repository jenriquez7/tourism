package com.tourism.test.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.tourism.dto.request.AuthUserDto;
import com.tourism.model.Admin;
import com.tourism.repository.AdminRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AdminIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private AdminRepository adminRepository;

   @Test
   @DisplayName("create Admin successfully")
   @WithMockUser(roles = "ADMIN")
   void createAdminIntegrationTest() throws Exception {
      AuthUserDto requestDto = new AuthUserDto("admin_test@email.com", "Abcd1234!");
      String url = "/v1/admin";

      mockMvc
            .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk());

      assertTrue(adminRepository
            .findByEmailStartingWithIgnoreCase("admin_test@email.com", Pageable.unpaged())
            .getContent()
            .stream()
            .anyMatch(a -> a.getEmail().equals("admin_test@email.com")));
   }

   @Test
   @DisplayName("create Admin wrong role")
   @WithMockUser(roles = "TOURIST")
   void createAdminForbiddenTest() throws Exception {
      AuthUserDto requestDto = new AuthUserDto("fail@email.com", "password123");
      String url = "/v1/admin";
      mockMvc
            .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("findAll Admins successfully")
   @WithMockUser(roles = "ADMIN")
   void findAllAdminsIntegrationTest() throws Exception {
      String url = "/v1/admin";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.data").exists());
   }

   @Test
   @DisplayName("findAll Admins wrong role")
   @WithMockUser(roles = "TOURIST")
   void findAllAdminsForbiddenTest() throws Exception {
      String url = "/v1/admin";
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("getById Admin successfully")
   @WithMockUser(roles = "ADMIN")
   void getByIdAdminIntegrationTest() throws Exception {
      Admin admin = adminRepository.save(new Admin("admin_by_id@test.com", "password", true));
      String url = "/v1/admin/" + admin.getId();

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].email").value("admin_by_id@test.com"));
   }

   @Test
   @DisplayName("getById Admin not found")
   @WithMockUser(roles = "ADMIN")
   void getByIdAdminNotFoundTest() throws Exception {
      String url = "/v1/admin/" + UUID.randomUUID();

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
   }

   @Test
   @DisplayName("getById Admin wrong role")
   @WithMockUser(roles = "TOURIST")
   void getByIdAdminForbiddenTest() throws Exception {
      String url = "/v1/admin/" + UUID.randomUUID();
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("delete Admin successfully")
   @WithMockUser(roles = "ADMIN")
   void deleteAdminIntegrationTest() throws Exception {
      adminRepository.save(new Admin("admin_to_delete@test.com", "password", true));
      Admin adminToDelete = adminRepository.save(new Admin("admin_delete@test.com", "password", true));
      String url = "/v1/admin/" + adminToDelete.getId();

      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

      assertTrue(adminRepository.findById(adminToDelete.getId()).isEmpty());
   }

   @Test
   @DisplayName("delete Admin cannot delete last admin")
   @WithMockUser(roles = "ADMIN")
   void deleteAdminCannotDeleteLastTest() throws Exception {
      long count = adminRepository.count();
      if (count == 1) {
         Admin singleAdmin = adminRepository.findAll().getFirst();
         String url = "/v1/admin/" + singleAdmin.getId();

         mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
      }
   }

   @Test
   @DisplayName("delete Admin wrong role")
   @WithMockUser(roles = "TOURIST")
   void deleteAdminForbiddenTest() throws Exception {
      String url = "/v1/admin/" + UUID.randomUUID();
      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("findByEmail Admin successfully")
   @WithMockUser(roles = "ADMIN")
   void findByEmailAdminIntegrationTest() throws Exception {
      adminRepository.save(new Admin("admin_email@test.com", "password", true));
      String url = "/v1/admin/email?email=admin_email";

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].content[0].email").value("admin_email@test.com"));
   }

   @Test
   @DisplayName("findByEmail Admin not found")
   @WithMockUser(roles = "ADMIN")
   void findByEmailAdminNotFoundTest() throws Exception {
      String url = "/v1/admin/email?email=nonexistent_email";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].content").isEmpty());
   }

   @Test
   @DisplayName("findByEmail Admin wrong role")
   @WithMockUser(roles = "TOURIST")
   void findByEmailAdminForbiddenTest() throws Exception {
      String url = "/v1/admin/email?email=test";
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

}
