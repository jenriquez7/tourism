package com.tourism.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Admin;
import com.tourism.model.Category;
import com.tourism.model.LodgingOwner;
import com.tourism.model.Tourist;
import com.tourism.model.TouristType;
import com.tourism.repository.AdminRepository;
import com.tourism.repository.CategoryRepository;
import com.tourism.repository.LodgingOwnerRepository;
import com.tourism.repository.TouristRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

   private final AdminRepository adminRepository;

   private final TouristRepository touristRepository;

   private final LodgingOwnerRepository ownerRepository;

   private final CategoryRepository categoryRepository;

   private final PasswordEncryptionService encryptionService;

   @Value("${admin.email}")
   private String adminEmail;

   @Value("${admin.password}")
   private String adminPassword;

   @Value("${tourist.email}")
   private String touristEmail;

   @Value("${tourist.password}")
   private String touristPassword;

   @Value("${owner.email}")
   private String ownerEmail;

   @Value("${owner.password}")
   private String ownerPassword;

   @Override
   public void run(String... args) {
      log.info("*** DataInitializer starting ***");
      if (adminRepository.findByEmailStartingWithIgnoreCase(adminEmail, null).isEmpty()) {
         Admin admin = new Admin(adminEmail, encryptionService.encryptPassword(adminPassword), true);
         adminRepository.save(admin);
         log.info("*** Admin created ***");
      }
      if (touristRepository.findByEmailStartingWithIgnoreCase(touristEmail, null).isEmpty()) {
         Tourist tourist = new Tourist(touristEmail, encryptionService.encryptPassword(touristPassword), "Turista", "Test", TouristType.STANDARD,
               true);
         touristRepository.save(tourist);
         log.info("*** tourist created ***");
      }
      if (ownerRepository.findByEmailStartingWithIgnoreCase(ownerEmail, null).isEmpty()) {
         LodgingOwner owner = new LodgingOwner(ownerEmail, encryptionService.encryptPassword(ownerPassword), "Lodging", "Owner", true);
         ownerRepository.save(owner);
         log.info("*** owner created ***");
      }
      if (categoryRepository.findByNameStartingWithIgnoreCaseOrderByNameAsc("Ciudad", Pageable.unpaged()).isEmpty()) {
         List<Category> categories = new ArrayList<>(
               List.of(new Category(1, "Ciudad", true), new Category(2, "Playa", true), new Category(3, "Sierras", true),
                     new Category(4, "Termas", true), new Category(5, "Campo", true), new Category(6, "Pueblo", true),
                     new Category(7, "Colonial", true)));
         categoryRepository.saveAll(categories);
         log.info("*** Categories created ***");
      }
      log.info("*** DataInitializer ended ***");
   }

}
