package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Car;
import hakan.rentacar.entities.concretes.MaintenanceRecord;
import hakan.rentacar.entities.dtos.CarDto;
import hakan.rentacar.entities.dtos.MaintenanceRecordDto;

import java.util.List;

public interface CarService {

    List<CarDto> getAll();

    CarDto getById(Long id);
    
    CarDto add(CarDto carDto);

    CarDto update(CarDto carDto);

    CarDto delete(Long id);
    
    List<CarDto> getByModelId(Long modelId);
    
    List<CarDto> getByState(Integer state);
    
    List<CarDto> getByBrandId(Long brandId);
    
    List<CarDto> getByModelYear(Integer year);
    
    // Gelişmiş filtreleme metodları
    List<CarDto> getBySegment(String segment);
    List<CarDto> getByTransmission(String transmission);
    List<CarDto> getByFuelType(String fuelType);
    List<CarDto> getByStateAndSegment(Integer state, String segment);
    List<CarDto> searchByPlateOrModelOrBrand(String searchTerm);
    List<CarDto> getByFeature(String feature);
    List<CarDto> getCarsWithUpcomingMaintenance();
    List<CarDto> getCarsWithExpiringInsurance();
    
    // Bakım kayıtları
    List<MaintenanceRecordDto> getMaintenanceHistory(Long carId);
    MaintenanceRecordDto addMaintenanceRecord(MaintenanceRecordDto maintenanceRecordDto);
    MaintenanceRecordDto updateMaintenanceRecord(MaintenanceRecordDto maintenanceRecordDto);
    void deleteMaintenanceRecord(Long maintenanceRecordId);
    
    // GPS güncelleme
    CarDto updateLocation(Long carId, Double latitude, Double longitude);

    // Model mapper
    CarDto EntityToDto(Car car);
    Car DtoToEntity(CarDto carDto);
}

