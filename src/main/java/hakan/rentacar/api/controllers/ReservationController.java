package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.ReservationDto;
import hakan.rentacar.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Reservations", description = "Rezervasyon yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getAll() {
        List<ReservationDto> reservations = reservationService.getAll();
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getById(@PathVariable Long id) {
        ReservationDto reservation = reservationService.getById(id);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ReservationDto>> getByCustomerId(@PathVariable Long customerId) {
        List<ReservationDto> reservations = reservationService.getByCustomerId(customerId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/car/{carId}")
    public ResponseEntity<List<ReservationDto>> getByCarId(@PathVariable Long carId) {
        List<ReservationDto> reservations = reservationService.getByCarId(carId);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ReservationDto>> getPendingReservations() {
        List<ReservationDto> reservations = reservationService.getPendingReservations();
        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<ReservationDto> create(@Valid @RequestBody ReservationDto reservationDto) {
        ReservationDto createdReservation = reservationService.create(reservationDto);
        return ResponseEntity.ok(createdReservation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDto> update(@PathVariable Long id, @Valid @RequestBody ReservationDto reservationDto) {
        reservationDto.setId(id);
        ReservationDto updatedReservation = reservationService.update(reservationDto);
        return ResponseEntity.ok(updatedReservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirm")
    @Operation(
        summary = "Rezervasyon onayla", 
        description = "Bekleyen rezervasyonu onaylar ve kiralama kaydı oluşturur"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Rezervasyon başarıyla onaylandı",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ReservationDto.class),
            examples = @ExampleObject(
                name = "Onaylanan Rezervasyon",
                value = """
                {
                  "id": 1,
                  "customerId": 5,
                  "customerName": "John Doe",
                  "carId": 3,
                  "carPlate": "34ABC123",
                  "carBrandName": "Toyota",
                  "carModelName": "Corolla",
                  "startDate": "2024-01-15",
                  "endDate": "2024-01-20",
                  "status": "CONFIRMED",
                  "totalAmount": 750.00,
                  "specialRequests": "Klima açık olmasını istiyorum",
                  "confirmedAt": "2024-01-10T14:30:00"
                }
                """
            )
        )
    )
    public ResponseEntity<ReservationDto> confirmReservation(
        @Parameter(description = "Onaylanacak rezervasyon ID", example = "1")
        @PathVariable Long id
    ) {
        ReservationDto confirmedReservation = reservationService.confirmReservation(id);
        return ResponseEntity.ok(confirmedReservation);
    }

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Rezervasyon iptal et", 
        description = "Rezervasyonu iptal eder ve ilgili kiralama kayıtlarını temizler"
    )
    @ApiResponse(
        responseCode = "200", 
        description = "Rezervasyon başarıyla iptal edildi"
    )
    public ResponseEntity<ReservationDto> cancelReservation(
        @Parameter(description = "İptal edilecek rezervasyon ID", example = "1")
        @PathVariable Long id
    ) {
        ReservationDto cancelledReservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(cancelledReservation);
    }

    @GetMapping("/available-cars")
    public ResponseEntity<List<ReservationDto>> getAvailableCars(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        List<ReservationDto> availableCars = reservationService.getAvailableCars(
            java.time.LocalDate.parse(startDate),
            java.time.LocalDate.parse(endDate)
        );
        return ResponseEntity.ok(availableCars);
    }
}
