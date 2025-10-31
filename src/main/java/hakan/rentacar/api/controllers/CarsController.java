package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.CarDto;
import hakan.rentacar.entities.dtos.GpsLocationDto;
import hakan.rentacar.entities.dtos.MaintenanceRecordDto;
import hakan.rentacar.service.CarService;
import hakan.rentacar.service.GpsService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Cars", description = "Araç yönetimi ve işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
public class CarsController {

    private final CarService carService;
    private final GpsService gpsService;

    public CarsController(CarService carService, GpsService gpsService) {
        this.carService = carService;
        this.gpsService = gpsService;
    }

    @GetMapping()
    public ResponseEntity<List<CarDto>> getAll() {
        return new ResponseEntity<>(carService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(carService.getById(id), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public CarDto add(@RequestBody @Valid CarDto carDto) {
        carService.add(carDto);
        return carDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public CarDto update(@RequestBody @Valid CarDto carDto) {
        carService.update(carDto);
        return carDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<CarDto> delete(@PathVariable("id") Long id) {
        return new ResponseEntity<>(carService.delete(id), HttpStatus.OK);
    }

    @GetMapping("/model/{modelId}")
    public ResponseEntity<List<CarDto>> getByModelId(@PathVariable("modelId") Long modelId) {
        return new ResponseEntity<>(carService.getByModelId(modelId), HttpStatus.OK);
    }

    @GetMapping("/state/{state}")
    public ResponseEntity<List<CarDto>> getByState(@PathVariable("state") Integer state) {
        return new ResponseEntity<>(carService.getByState(state), HttpStatus.OK);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<CarDto>> getByBrandId(@PathVariable("brandId") Long brandId) {
        return new ResponseEntity<>(carService.getByBrandId(brandId), HttpStatus.OK);
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<CarDto>> getByModelYear(@PathVariable("year") Integer year) {
        return new ResponseEntity<>(carService.getByModelYear(year), HttpStatus.OK);
    }

    // Gelişmiş filtreleme endpoint'leri
    @GetMapping("/segment/{segment}")
    public ResponseEntity<List<CarDto>> getBySegment(@PathVariable("segment") String segment) {
        return new ResponseEntity<>(carService.getBySegment(segment), HttpStatus.OK);
    }

    @GetMapping("/transmission/{transmission}")
    public ResponseEntity<List<CarDto>> getByTransmission(@PathVariable("transmission") String transmission) {
        return new ResponseEntity<>(carService.getByTransmission(transmission), HttpStatus.OK);
    }

    @GetMapping("/fuel-type/{fuelType}")
    public ResponseEntity<List<CarDto>> getByFuelType(@PathVariable("fuelType") String fuelType) {
        return new ResponseEntity<>(carService.getByFuelType(fuelType), HttpStatus.OK);
    }

    @GetMapping("/state/{state}/segment/{segment}")
    public ResponseEntity<List<CarDto>> getByStateAndSegment(
            @PathVariable("state") Integer state,
            @PathVariable("segment") String segment) {
        return new ResponseEntity<>(carService.getByStateAndSegment(state, segment), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CarDto>> searchCars(@RequestParam("q") String searchTerm) {
        return new ResponseEntity<>(carService.searchByPlateOrModelOrBrand(searchTerm), HttpStatus.OK);
    }

    @GetMapping("/feature/{feature}")
    public ResponseEntity<List<CarDto>> getByFeature(@PathVariable("feature") String feature) {
        return new ResponseEntity<>(carService.getByFeature(feature), HttpStatus.OK);
    }

    @GetMapping("/maintenance/upcoming")
    public ResponseEntity<List<CarDto>> getCarsWithUpcomingMaintenance() {
        return new ResponseEntity<>(carService.getCarsWithUpcomingMaintenance(), HttpStatus.OK);
    }

    @GetMapping("/insurance/expiring")
    public ResponseEntity<List<CarDto>> getCarsWithExpiringInsurance() {
        return new ResponseEntity<>(carService.getCarsWithExpiringInsurance(), HttpStatus.OK);
    }

    // Bakım kayıtları endpoint'leri
    @GetMapping("/{carId}/maintenance")
    public ResponseEntity<List<MaintenanceRecordDto>> getMaintenanceHistory(@PathVariable("carId") Long carId) {
        return new ResponseEntity<>(carService.getMaintenanceHistory(carId), HttpStatus.OK);
    }

    @PostMapping("/{carId}/maintenance")
    public ResponseEntity<MaintenanceRecordDto> addMaintenanceRecord(
            @PathVariable("carId") Long carId,
            @RequestBody @Valid MaintenanceRecordDto maintenanceRecordDto) {
        maintenanceRecordDto.setCarId(carId);
        return new ResponseEntity<>(carService.addMaintenanceRecord(maintenanceRecordDto), HttpStatus.CREATED);
    }

    @PutMapping("/maintenance/{maintenanceRecordId}")
    public ResponseEntity<MaintenanceRecordDto> updateMaintenanceRecord(
            @PathVariable("maintenanceRecordId") Long maintenanceRecordId,
            @RequestBody @Valid MaintenanceRecordDto maintenanceRecordDto) {
        maintenanceRecordDto.setId(maintenanceRecordId);
        return new ResponseEntity<>(carService.updateMaintenanceRecord(maintenanceRecordDto), HttpStatus.OK);
    }

    @DeleteMapping("/maintenance/{maintenanceRecordId}")
    public ResponseEntity<Void> deleteMaintenanceRecord(@PathVariable("maintenanceRecordId") Long maintenanceRecordId) {
        carService.deleteMaintenanceRecord(maintenanceRecordId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // GPS güncelleme endpoint'i
    @PutMapping("/{carId}/location")
    public ResponseEntity<CarDto> updateLocation(
            @PathVariable("carId") Long carId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude) {
        return new ResponseEntity<>(gpsService.updateCarLocation(carId, latitude, longitude), HttpStatus.OK);
    }

    // GPS konum bilgisi getirme
    @GetMapping("/{carId}/location")
    public ResponseEntity<GpsLocationDto> getCarLocation(@PathVariable("carId") Long carId) {
        return new ResponseEntity<>(gpsService.getCarLocation(carId), HttpStatus.OK);
    }

    // Yarıçap içindeki araçları getirme
    @GetMapping("/location/nearby")
    public ResponseEntity<List<CarDto>> getCarsInRadius(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("radius") Double radiusKm) {
        return new ResponseEntity<>(gpsService.getCarsInRadius(latitude, longitude, radiusKm), HttpStatus.OK);
    }

    // Son aktif araçları getirme
    @GetMapping("/location/recent")
    public ResponseEntity<List<GpsLocationDto>> getRecentCarLocations(
            @RequestParam(value = "hours", defaultValue = "24") int hoursAgo) {
        return new ResponseEntity<>(gpsService.getRecentCarLocations(hoursAgo), HttpStatus.OK);
    }

    // Araç konum geçmişi
    @GetMapping("/{carId}/location/history")
    public ResponseEntity<List<GpsLocationDto>> getCarLocationHistory(
            @PathVariable("carId") Long carId,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        return new ResponseEntity<>(gpsService.getCarLocationHistory(carId, days), HttpStatus.OK);
    }

    // Mesafe hesaplama
    @GetMapping("/location/distance")
    public ResponseEntity<Double> calculateDistance(
            @RequestParam("lat1") Double lat1,
            @RequestParam("lng1") Double lng1,
            @RequestParam("lat2") Double lat2,
            @RequestParam("lng2") Double lng2) {
        return new ResponseEntity<>(gpsService.calculateDistance(lat1, lng1, lat2, lng2), HttpStatus.OK);
    }
}

