package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.LoginRequest;
import hakan.rentacar.entities.dtos.LoginResponse;
import hakan.rentacar.entities.dtos.UserDto;
import hakan.rentacar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Authentication", description = "Kimlik doğrulama işlemleri")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    @Operation(
        summary = "Kullanıcı girişi", 
        description = "Kullanıcı adı ve şifre ile giriş yapar ve JWT token döner"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Başarılı giriş",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(
                    name = "Başarılı Giriş",
                    value = """
                    {
                      "token": "eyJhbGciOiJIUzUxMiJ9...",
                      "type": "Bearer",
                      "id": 1,
                      "username": "admin",
                      "email": "admin@rentacar.com",
                      "firstName": "Admin",
                      "lastName": "User",
                      "roles": ["ADMIN"]
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    })
    public ResponseEntity<LoginResponse> login(
        @Parameter(description = "Giriş bilgileri", required = true)
        @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Authentication endpoint'inin çalışıp çalışmadığını test eder")
    @ApiResponse(responseCode = "200", description = "Endpoint çalışıyor")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth endpoint is working");
    }
    
    @PostMapping("/register")
    @Operation(
        summary = "Kullanıcı kaydı", 
        description = "Yeni kullanıcı kaydı oluşturur ve otomatik olarak Customer profili oluşturur"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Başarılı kayıt",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class),
                examples = @ExampleObject(
                    name = "Başarılı Kayıt",
                    value = """
                    {
                      "id": 2,
                      "username": "john_doe",
                      "email": "john@example.com",
                      "firstName": "John",
                      "lastName": "Doe",
                      "roles": ["USER"]
                    }
                    """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek veya kullanıcı adı mevcut"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> register(
        @Parameter(description = "Kullanıcı kayıt bilgileri", required = true)
        @Valid @RequestBody UserDto userDto
    ) {
        try {
            System.out.println("Register request received for: " + userDto.getUsername());
            UserDto response = authService.register(userDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Register error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected register error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }
}
