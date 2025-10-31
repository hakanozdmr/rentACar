package hakan.rentacar.api.controllers;

import hakan.rentacar.service.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Monitoring", description = "Sistem izleme ve sağlık kontrolü")
public class MonitoringController {

    @Autowired
    private EncryptionService encryptionService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        return ResponseEntity.ok(health);
    }

    @GetMapping("/encryption/test")
    public ResponseEntity<Map<String, String>> testEncryption(@RequestParam String text) {
        Map<String, String> result = new HashMap<>();
        try {
            String encrypted = encryptionService.encrypt(text);
            String decrypted = encryptionService.decrypt(encrypted);
            String hashed = encryptionService.hash(text);
            
            result.put("original", text);
            result.put("encrypted", encrypted);
            result.put("decrypted", decrypted);
            result.put("hashed", hashed);
            result.put("status", "success");
            
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rate-limit-status")
    public ResponseEntity<Map<String, String>> rateLimitStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("rateLimitEnabled", "true");
        status.put("defaultLimit", "100 requests/minute");
        status.put("authLimit", "5 requests/minute");
        status.put("description", "Rate limiting is active for API protection");
        
        return ResponseEntity.ok(status);
    }
}
