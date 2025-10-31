package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Role;
import hakan.rentacar.entities.concretes.User;
import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.dtos.LoginRequest;
import hakan.rentacar.entities.dtos.LoginResponse;
import hakan.rentacar.entities.dtos.UserDto;
import hakan.rentacar.repostories.RoleRepository;
import hakan.rentacar.repostories.UserRepository;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.security.JwtUtils;
import hakan.rentacar.security.UserDetailsImpl;
import hakan.rentacar.service.AuthService;
import hakan.rentacar.audit.Auditable;
import hakan.rentacar.entities.concretes.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    @Auditable(entity = "User", action = AuditLog.ActionType.LOGIN, description = "User login")
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            // Önce kullanıcının var olup olmadığını kontrol et
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found: " + loginRequest.getUsername()));

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtUtils.generateToken(userDetails);
            
            return LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(userDetails.getAuthorities().stream()
                            .map(authority -> authority.getAuthority().substring(5)) // "ROLE_" prefix'ini kaldır
                            .collect(java.util.stream.Collectors.toSet()))
                    .build();
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new RuntimeException("Invalid username or password: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }
    
    @Override
    @Auditable(entity = "User", action = AuditLog.ActionType.CREATE, description = "User registration")
    public UserDto register(UserDto userDto) {
        System.out.println("Starting user registration for: " + userDto.getUsername());
        
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEnabled(true);
        
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        // Create Customer record for USER role
        try {
            if (savedUser.getRoles().stream().anyMatch(role -> "USER".equals(role.getName()))) {
                Customer customer = new Customer();
                customer.setFirstName(savedUser.getFirstName());
                customer.setLastName(savedUser.getLastName());
                customer.setEmail(savedUser.getEmail());
                customer.setUser(savedUser); // Link customer to user
                // Set default values for required fields
                customer.setStreet("Adres bilgisi güncellenecek");
                customer.setCity("Şehir bilgisi güncellenecek");
                customer.setZipcode(34000); // Valid zipcode that satisfies @Min(10000) constraint
                customer.setDateOfBirth(LocalDate.now().minusYears(25)); // Default age 25
                // Generate unique values to avoid constraint violations
                String uniqueId = String.valueOf(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999));
                customer.setIdNumber(uniqueId);
                customer.setDriverLicenseNumber("DL" + uniqueId);
                customer.setPhone("+905000000000"); // Default Turkish phone number format
                
                Customer savedCustomer = customerRepository.save(customer);
                System.out.println("Customer record created with ID: " + savedCustomer.getId());
            }
        } catch (Exception e) {
            // Log the error but don't fail the user registration
            System.err.println("Error creating customer record: " + e.getMessage());
            e.printStackTrace();
            // Continue with user registration even if customer creation fails
        }
        
        UserDto response = modelMapper.map(savedUser, UserDto.class);
        response.setPassword(null); // Don't return password in response
        response.setRoleNames(savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet()));
        
        System.out.println("User registration completed successfully for: " + savedUser.getUsername());
        return response;
    }
    
    @Override
    public String generateToken(String username) {
        // Implementation for generating token
        return null;
    }
}
