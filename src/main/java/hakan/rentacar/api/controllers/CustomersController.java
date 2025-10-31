package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.CustomerDto;
import hakan.rentacar.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Customers", description = "Müşteri yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomersController {

    private final CustomerService customerService;

    public CustomersController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping()
    public ResponseEntity<List<CustomerDto>> getAll() {
        return new ResponseEntity<>(customerService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(customerService.getById(id), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public CustomerDto add(@RequestBody @Valid CustomerDto customerDto) {
        customerService.add(customerDto);
        return customerDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public CustomerDto update(@RequestBody @Valid CustomerDto customerDto) {
        customerService.update(customerDto);
        return customerDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<CustomerDto> delete(@PathVariable("id") Long id) {
        return new ResponseEntity<>(customerService.delete(id), HttpStatus.OK);
    }

    @GetMapping("/id-number/{idNumber}")
    public ResponseEntity<CustomerDto> getByIdNumber(@PathVariable("idNumber") String idNumber) {
        return new ResponseEntity<>(customerService.getByIdNumber(idNumber), HttpStatus.OK);
    }

    @GetMapping("/driver-license/{driverLicenseNumber}")
    public ResponseEntity<CustomerDto> getByDriverLicenseNumber(@PathVariable("driverLicenseNumber") String driverLicenseNumber) {
        return new ResponseEntity<>(customerService.getByDriverLicenseNumber(driverLicenseNumber), HttpStatus.OK);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto> getByEmail(@PathVariable("email") String email) {
        return new ResponseEntity<>(customerService.getByEmail(email), HttpStatus.OK);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<CustomerDto>> getByCity(@PathVariable("city") String city) {
        return new ResponseEntity<>(customerService.getByCity(city), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerDto>> searchByName(@RequestParam("name") String name) {
        return new ResponseEntity<>(customerService.findByNameContaining(name), HttpStatus.OK);
    }
}

