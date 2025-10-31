package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.RentalDto;
import hakan.rentacar.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Rentals", description = "Kiralama işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
public class RentalsController {

    private final RentalService rentalService;

    public RentalsController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping()
    public ResponseEntity<List<RentalDto>> getAll() {
        return new ResponseEntity<>(rentalService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(rentalService.getById(id), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public RentalDto add(@RequestBody @Valid RentalDto rentalDto) {
        rentalService.add(rentalDto);
        return rentalDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public RentalDto update(@RequestBody @Valid RentalDto rentalDto) {
        rentalService.update(rentalDto);
        return rentalDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<RentalDto> delete(@PathVariable("id") Long id) {
        return new ResponseEntity<>(rentalService.delete(id), HttpStatus.OK);
    }

    @GetMapping("/car/{carId}")
    public ResponseEntity<List<RentalDto>> getByCarId(@PathVariable("carId") Long carId) {
        return new ResponseEntity<>(rentalService.getByCarId(carId), HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RentalDto>> getByCustomerId(@PathVariable("customerId") Long customerId) {
        return new ResponseEntity<>(rentalService.getByCustomerId(customerId), HttpStatus.OK);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<RentalDto>> findByDate(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(rentalService.findByDate(date), HttpStatus.OK);
    }

    @GetMapping("/active/{date}")
    public ResponseEntity<List<RentalDto>> findActiveRentals(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return new ResponseEntity<>(rentalService.findActiveRentals(date), HttpStatus.OK);
    }

    @GetMapping("/between")
    public ResponseEntity<List<RentalDto>> findRentalsBetweenDates(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return new ResponseEntity<>(rentalService.findRentalsBetweenDates(startDate, endDate), HttpStatus.OK);
    }

    @GetMapping("/availability/{carId}")
    public ResponseEntity<Boolean> isCarAvailable(
            @PathVariable("carId") Long carId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return new ResponseEntity<>(rentalService.isCarAvailable(carId, start, end), HttpStatus.OK);
    }
}

