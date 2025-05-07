package com.bootcamp.ecommerce_rohit.config;
import com.bootcamp.ecommerce_rohit.entities.Role;
import com.bootcamp.ecommerce_rohit.entities.User;
import com.bootcamp.ecommerce_rohit.repositories.RoleRepository;
import com.bootcamp.ecommerce_rohit.repositories.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;


    public AdminBootstrap(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    private String email = "rohit.gupta11@tothenew.com";
    private String password = "hello@WOrld";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        User existingAdmin = userRepository.findByEmail(email);


        if (existingAdmin == null) {
            User admin = new User();
            admin.setFirstName("Rohit");
            admin.setLastName("Gupta");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setIsLocked(false);
            admin.setIsActive(true);
            Role r =roleRepository.findByAuthority("admin");
            admin.setRole(r);
            admin.setPasswordUpdateDate(LocalDateTime.now());
            admin.setFailLoginAttemptsCount(0);
            userRepository.save(admin);
        }
    }
}