package com.taskmanagement.config;

import com.taskmanagement.entity.Institute;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.UserRole;
import com.taskmanagement.repository.InstituteRepository;
import com.taskmanagement.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDefaultAdmin(UserRepository users, InstituteRepository institutes, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default institute if it doesn't exist
            Institute defaultInstitute;
            if (institutes.count() == 0) {
                defaultInstitute = new Institute();
                defaultInstitute.setName("Default Institute");
                defaultInstitute.setStatus(Institute.InstituteStatus.ACTIVE);
                defaultInstitute = institutes.save(defaultInstitute);
            } else {
                defaultInstitute = institutes.findAll().get(0);
            }
            
            // Fix all users with null institute_id
            final Institute institute = defaultInstitute;
            users.findAll().forEach(user -> {
                if (user.getInstitute() == null) {
                    user.setInstitute(institute);
                    users.save(user);
                }
            });
            
            // Create admin user if doesn't exist
            String email = "admin@taskmanagement.com";
            if (!users.existsByEmail(email)) {
                User admin = new User();
                admin.setEmail(email);
                admin.setFullName("System Administrator");
                admin.setRole(UserRole.ADMIN);
                admin.setIsEnabled(true);
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setInstitute(defaultInstitute);
                users.save(admin);
            }
        };
    }
}
