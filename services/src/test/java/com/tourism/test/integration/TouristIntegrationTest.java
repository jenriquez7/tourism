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
import com.tourism.dto.request.TouristRequestDTO;
import com.tourism.model.Tourist;
import com.tourism.model.TouristType;
import com.tourism.repository.TouristRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TouristIntegrationTest {

   @Autowired
   private MockMvc mockMvc;

   @Autowired
   private ObjectMapper objectMapper;

   @Autowired
   private TouristRepository touristRepository;

   @Test
   @DisplayName("create Tourist successfully")
   void createTouristIntegrationTest() throws Exception {
      TouristRequestDTO requestDto = TouristRequestDTO
            .builder()
            .firstName("John")
            .lastName("Doe")
            .email("tourist_test@email.com")
            .password("Abcd1234!")
            .type(TouristType.STANDARD)
            .build();
      String url = "/v1/tourist";

      mockMvc
            .perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk());

      assertTrue(touristRepository
            .findByEmailStartingWithIgnoreCase("tourist_test@email.com", Pageable.unpaged())
            .getContent()
            .stream()
            .anyMatch(t -> t.getEmail().equals("tourist_test@email.com")));
   }

   @Test
   @DisplayName("findAll Tourists successfully")
   @WithMockUser(roles = "ADMIN")
   void findAllTouristsIntegrationTest() throws Exception {
      String url = "/v1/tourist";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.data").exists());
   }

   @Test
   @DisplayName("findAll Tourists wrong role")
   @WithMockUser(roles = "TOURIST")
   void findAllTouristsForbiddenTest() throws Exception {
      String url = "/v1/tourist";
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("getById Tourist successfully")
   @WithMockUser(roles = "ADMIN")
   void getByIdTouristIntegrationTest() throws Exception {
      Tourist tourist = touristRepository.save(new Tourist("tourist_by_id@test.com", "password", "John", "Doe", TouristType.STANDARD, true));
      String url = "/v1/tourist/" + tourist.getId();

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].email").value("tourist_by_id@test.com"));
   }

   @Test
   @DisplayName("getById Tourist not found")
   @WithMockUser(roles = "ADMIN")
   void getByIdTouristNotFoundTest() throws Exception {
      String url = "/v1/tourist/" + UUID.randomUUID();

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
   }

   @Test
   @DisplayName("getById Tourist wrong role")
   @WithMockUser(roles = "TOURIST")
   void getByIdTouristForbiddenTest() throws Exception {
      String url = "/v1/tourist/" + UUID.randomUUID();
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("delete Tourist by admin successfully")
   @WithMockUser(roles = "ADMIN")
   void deleteTouristByAdminIntegrationTest() throws Exception {
      Tourist touristToDelete = touristRepository.save(
            new Tourist("tourist_delete_admin@test.com", "Abcd1234!", "John", "Doe", TouristType.STANDARD, true));
      String url = "/v1/tourist/" + touristToDelete.getId();

      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

      assertTrue(touristRepository.findById(touristToDelete.getId()).isEmpty());
   }

   @Test
   @DisplayName("delete Tourist by admin not found")
   @WithMockUser(roles = "ADMIN")
   void deleteTouristByAdminNotFoundTest() throws Exception {
      String url = "/v1/tourist/" + UUID.randomUUID();

      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
   }

   @Test
   @DisplayName("delete Tourist by admin wrong role")
   @WithMockUser(roles = "TOURIST")
   void deleteTouristByAdminForbiddenTest() throws Exception {
      String url = "/v1/tourist/" + UUID.randomUUID();
      mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("profile Tourist successfully")
   @WithMockUser(roles = "TOURIST")
   void profileTouristIntegrationTest() throws Exception {
      String url = "/v1/tourist/profile";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
   }

   @Test
   @DisplayName("profile Tourist wrong role")
   @WithMockUser(roles = "ADMIN")
   void profileTouristForbiddenTest() throws Exception {
      String url = "/v1/tourist/profile";
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("findByEmail Tourist successfully")
   @WithMockUser(roles = "ADMIN")
   void findByEmailTouristIntegrationTest() throws Exception {
      touristRepository.save(new Tourist("tourist_email@test.com", "Abcd1234!", "John", "Doe", TouristType.STANDARD, true));
      String url = "/v1/tourist/email?email=tourist_email";

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].content[0].email").value("tourist_email@test.com"));
   }

   @Test
   @DisplayName("findByEmail Tourist not found")
   @WithMockUser(roles = "ADMIN")
   void findByEmailTouristNotFoundTest() throws Exception {
      String url = "/v1/tourist/email?email=nonexistent_email";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].content").isEmpty());
   }

   @Test
   @DisplayName("findByEmail Tourist wrong role")
   @WithMockUser(roles = "TOURIST")
   void findByEmailTouristForbiddenTest() throws Exception {
      String url = "/v1/tourist/email?email=test";
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

   @Test
   @DisplayName("findByLastName Tourist successfully")
   @WithMockUser(roles = "ADMIN")
   void findByLastNameTouristIntegrationTest() throws Exception {
      touristRepository.save(new Tourist("tourist_lastname@test.com", "Abcd1234!", "John", "Smith", TouristType.STANDARD, true));
      String url = "/v1/tourist/lastName?lastName=Smith";

      mockMvc
            .perform(get(url).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].content[0].lastName").value("Smith"));
   }

   @Test
   @DisplayName("findByLastName Tourist not found")
   @WithMockUser(roles = "ADMIN")
   void findByLastNameTouristNotFoundTest() throws Exception {
      String url = "/v1/tourist/lastName?lastName=nonexistent";

      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].content").isEmpty());
   }

   @Test
   @DisplayName("findByLastName Tourist wrong role")
   @WithMockUser(roles = "TOURIST")
   void findByLastNameTouristForbiddenTest() throws Exception {
      String url = "/v1/tourist/lastName?lastName=test";
      mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
   }

}
