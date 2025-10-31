package hakan.rentacar.config;

import hakan.rentacar.entities.concretes.Role;
import hakan.rentacar.entities.concretes.User;
import hakan.rentacar.repostories.RoleRepository;
import hakan.rentacar.repostories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Admin kullanıcısının şifresini runtime'da doğru hash ile güncelle
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        if (adminUser != null) {
            // "123456" şifresi için doğru hash
            String correctHash = passwordEncoder.encode("123456");
            adminUser.setPassword(correctHash);
            userRepository.save(adminUser);
            System.out.println("Admin password updated with hash: " + correctHash);
        }
    }
}

