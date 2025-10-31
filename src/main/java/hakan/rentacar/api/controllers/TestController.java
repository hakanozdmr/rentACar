package hakan.rentacar.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import hakan.rentacar.entities.concretes.User;
import hakan.rentacar.repostories.UserRepository;
import hakan.rentacar.service.EncryptionService;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Test", description = "Test ve geliştirme endpoint'leri")
public class TestController {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Autowired
    private EncryptionService encryptionService;
    
    @GetMapping("/hash-password/{password}")
    public String hashPassword(@PathVariable String password) {
        String hashed = passwordEncoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashed);
        return hashed;
    }
    
    @GetMapping("/verify-admin")
    public String verifyAdmin() {
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin != null) {
            boolean matches = passwordEncoder.matches("123456", admin.getPassword());
            return "Admin user found. Password matches: " + matches;
        }
        return "Admin user not found";
    }
    
    @GetMapping("/jwt-key-info")
    public String jwtKeyInfo() {
        int keyLength = jwtSecret.getBytes().length;
        boolean isLongEnough = keyLength >= 64;
        return String.format("JWT Secret Key Length: %d bytes (%d bits). HS512 Compatible: %s", 
                           keyLength, keyLength * 8, isLongEnough);
    }
    
    @GetMapping("/encrypt/{text}")
    public String encryptText(@PathVariable String text) {
        try {
            return encryptionService.encrypt(text);
        } catch (Exception e) {
            return "Encryption failed: " + e.getMessage();
        }
    }
    
    @GetMapping("/hash/{text}")
    public String hashText(@PathVariable String text) {
        return encryptionService.hash(text);
    }
}
