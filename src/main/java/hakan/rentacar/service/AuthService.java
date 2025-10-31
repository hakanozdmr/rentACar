package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.LoginRequest;
import hakan.rentacar.entities.dtos.LoginResponse;
import hakan.rentacar.entities.dtos.UserDto;

public interface AuthService {
    
    LoginResponse login(LoginRequest loginRequest);
    
    UserDto register(UserDto userDto);
    
    String generateToken(String username);
}

