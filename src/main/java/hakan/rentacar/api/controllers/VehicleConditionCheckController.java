package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.VehicleConditionCheck;
import hakan.rentacar.entities.dtos.VehicleConditionCheckDto;
import hakan.rentacar.service.VehicleConditionCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicle-condition-checks")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Vehicle Condition Checks", description = "Araç durum kontrolü işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
public class VehicleConditionCheckController {

    private final VehicleConditionCheckService vehicleConditionCheckService;

    public VehicleConditionCheckController(VehicleConditionCheckService vehicleConditionCheckService) {
        this.vehicleConditionCheckService = vehicleConditionCheckService;
    }

    @GetMapping()
    @Operation(summary = "Tüm kontrolleri getir")
    public ResponseEntity<List<VehicleConditionCheckDto>> getAll() {
        return new ResponseEntity<>(vehicleConditionCheckService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile kontrol getir")
    public ResponseEntity<VehicleConditionCheckDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(vehicleConditionCheckService.getById(id), HttpStatus.OK);
    }

    @GetMapping("/rental/{rentalId}")
    @Operation(summary = "Kiralama ID ile kontrolleri getir")
    public ResponseEntity<List<VehicleConditionCheckDto>> getByRentalId(@PathVariable("rentalId") Long rentalId) {
        return new ResponseEntity<>(vehicleConditionCheckService.getByRentalId(rentalId), HttpStatus.OK);
    }

    @GetMapping("/car/{carId}")
    @Operation(summary = "Araç ID ile kontrolleri getir")
    public ResponseEntity<List<VehicleConditionCheckDto>> getByCarId(@PathVariable("carId") Long carId) {
        return new ResponseEntity<>(vehicleConditionCheckService.getByCarId(carId), HttpStatus.OK);
    }

    @GetMapping("/type/{checkType}")
    @Operation(summary = "Kontrol tipi ile kontrolleri getir")
    public ResponseEntity<List<VehicleConditionCheckDto>> getByCheckType(@PathVariable("checkType") VehicleConditionCheck.CheckType checkType) {
        return new ResponseEntity<>(vehicleConditionCheckService.getByCheckType(checkType), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Yeni kontrol oluştur")
    public VehicleConditionCheckDto add(@RequestBody @Valid VehicleConditionCheckDto checkDto) {
        vehicleConditionCheckService.add(checkDto);
        return checkDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Kontrol güncelle")
    public VehicleConditionCheckDto update(@RequestBody @Valid VehicleConditionCheckDto checkDto) {
        vehicleConditionCheckService.update(checkDto);
        return checkDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Kontrol sil")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        vehicleConditionCheckService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/rental/{rentalId}/latest-delivery")
    @Operation(summary = "Son teslim kontrolünü getir")
    public ResponseEntity<VehicleConditionCheckDto> getLatestDeliveryCheck(@PathVariable("rentalId") Long rentalId) {
        Optional<VehicleConditionCheckDto> check = vehicleConditionCheckService.getLatestDeliveryCheck(rentalId);
        return check.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/rental/{rentalId}/latest-pickup")
    @Operation(summary = "Son teslim alma kontrolünü getir")
    public ResponseEntity<VehicleConditionCheckDto> getLatestPickupCheck(@PathVariable("rentalId") Long rentalId) {
        Optional<VehicleConditionCheckDto> check = vehicleConditionCheckService.getLatestPickupCheck(rentalId);
        return check.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/{id}/confirm")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Müşteri onayla")
    public VehicleConditionCheckDto confirmByCustomer(@PathVariable("id") Long id) {
        return vehicleConditionCheckService.confirmByCustomer(id);
    }

    @GetMapping("/rental/{rentalId}/compare")
    @Operation(summary = "Teslim ve teslim alma kontrollerini karşılaştır")
    public ResponseEntity<VehicleConditionCheckDto> compareDeliveryAndPickup(@PathVariable("rentalId") Long rentalId) {
        return new ResponseEntity<>(vehicleConditionCheckService.compareDeliveryAndPickup(rentalId), HttpStatus.OK);
    }
}


