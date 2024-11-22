package com.tourism.configuration;

import com.tourism.infrastructure.PasswordEncryptionService;
import com.tourism.model.Admin;
import com.tourism.model.Category;
import com.tourism.model.Role;
import com.tourism.repository.AdminRepository;
import com.tourism.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncryptionService encryptionService;

    @Value("${admin.user}")
    private String user;
    @Value("${admin.password}")
    private String password;

    @Autowired
    public DataInitializer(AdminRepository adminRepository, CategoryRepository categoryRepository, PasswordEncryptionService encryptionService) {
        this.adminRepository = adminRepository;
        this.categoryRepository = categoryRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    public void run(String... args) {
        log.info("*** DataInitializer starting ***");
        if (adminRepository.findByEmailStartingWithIgnoreCase(user, null).isEmpty()) {
            Admin admin = new Admin(user, encryptionService.encryptPassword(password), Role.ADMIN,true);
            adminRepository.save(admin);
            log.info("*** Admin created ***");
        }
        if (Arrays.stream(categoryRepository.findByNameStartingWithIgnoreCaseOrderByNameAsc("Ciudad")).findAny().isEmpty()) {
            List<Category> categories = new ArrayList<>(
                    List.of(
                            new Category(1, "Ciudad", true),
                            new Category(2, "Playa", true),
                            new Category(3, "Sierras", true),
                            new Category(4, "Termas", true),
                            new Category(5, "Campo", true),
                            new Category(6, "Pueblo", true),
                            new Category(7, "Colonial", true))
            );
            categoryRepository.saveAll(categories);
            log.info("*** Categories created ***");
        }
        log.info("*** DataInitializer ended ***");
    }
}
