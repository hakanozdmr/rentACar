package hakan.rentacar.entities.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Kullanıcı giriş isteği")
public class LoginRequest {
    
    @NotBlank
    @Schema(description = "Kullanıcı adı", example = "admin")
    private String username;
    
    @NotBlank
    @Schema(description = "Şifre", example = "password123")
    private String password;
}
