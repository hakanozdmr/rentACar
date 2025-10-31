package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Car;
import hakan.rentacar.entities.concretes.Model;
import hakan.rentacar.entities.concretes.MaintenanceRecord;
import hakan.rentacar.entities.dtos.CarDto;
import hakan.rentacar.entities.dtos.MaintenanceRecordDto;
import hakan.rentacar.entities.dtos.ReservationRatingDto;
import hakan.rentacar.repostories.CarRepository;
import hakan.rentacar.repostories.ModelRepostory;
import hakan.rentacar.repostories.MaintenanceRecordRepository;
import hakan.rentacar.service.CarService;
import hakan.rentacar.service.ReservationRatingService;
import hakan.rentacar.audit.Auditable;
import hakan.rentacar.entities.concretes.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final ModelRepostory modelRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private ReservationRatingService reservationRatingService;

    @Autowired
    public CarServiceImpl(CarRepository carRepository, ModelRepostory modelRepository, MaintenanceRecordRepository maintenanceRecordRepository) {
        this.carRepository = carRepository;
        this.modelRepository = modelRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
    }

    @Override
    public List<CarDto> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Car> cars = carRepository.findAll(sort);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public CarDto getById(Long id) {
        Car car = carRepository.findByIdWithMaintenanceHistory(id).orElseThrow();
        return EntityToDto(car);
    }

    @Override
    @Auditable(entity = "Car", action = AuditLog.ActionType.CREATE, description = "Create new car")
    public CarDto add(CarDto carDto) {
        // Check if plate already exists
        if (carRepository.existsByPlate(carDto.getPlate())) {
            throw new RuntimeException("Car with plate " + carDto.getPlate() + " already exists");
        }
        
        Car car = DtoToEntity(carDto);
        carRepository.save(car);
        return carDto;
    }

    @Override
    @Auditable(entity = "Car", action = AuditLog.ActionType.UPDATE, description = "Update car information")
    public CarDto update(CarDto carDto) {
        Car existingCar = carRepository.findById(carDto.getId()).orElseThrow();
        
        // Check if plate is being changed and new plate already exists
        if (!existingCar.getPlate().equals(carDto.getPlate()) && 
            carRepository.existsByPlate(carDto.getPlate())) {
            throw new RuntimeException("Car with plate " + carDto.getPlate() + " already exists");
        }
        
        Car car = DtoToEntity(carDto);
        carRepository.save(car);
        return carDto;
    }

    @Override
    @Auditable(entity = "Car", action = AuditLog.ActionType.DELETE, description = "Delete car")
    public CarDto delete(Long id) {
        Car car = carRepository.findById(id).orElseThrow();
        CarDto carDto = EntityToDto(car);
        carRepository.deleteById(id);
        return carDto;
    }

    @Override
    public List<CarDto> getByModelId(Long modelId) {
        List<Car> cars = carRepository.findByModelId(modelId);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByState(Integer state) {
        List<Car> cars = carRepository.findByState(state);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByBrandId(Long brandId) {
        List<Car> cars = carRepository.findByBrandId(brandId);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByModelYear(Integer year) {
        List<Car> cars = carRepository.findByModelYear(year);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public CarDto EntityToDto(Car car) {
        CarDto carDto = modelMapper.map(car, CarDto.class);
        carDto.setModelId(car.getModel().getId());
        if (car.getModel() != null && car.getModel().getBrand() != null) {
            carDto.setBrandName(car.getModel().getBrand().getName());
            carDto.setModelName(car.getModel().getName());
        }
        
        // Bakım geçmişini DTO'ya map et
        if (car.getMaintenanceHistory() != null && !car.getMaintenanceHistory().isEmpty()) {
            List<MaintenanceRecordDto> maintenanceHistory = car.getMaintenanceHistory()
                .stream()
                .map(this::mapToMaintenanceRecordDto)
                .collect(Collectors.toList());
            carDto.setMaintenanceHistory(maintenanceHistory);
        }
        
        // Rating bilgilerini ekle
        if (car.getId() != null) {
            double averageRating = reservationRatingService.getAverageCarRating(car.getId());
            long ratingCount = reservationRatingService.getCarRatingCount(car.getId());
            
            carDto.setAverageRating(averageRating > 0 ? averageRating : 0.0);
            carDto.setRatingCount(ratingCount);
        }
        
        return carDto;
    }

    @Override
    public Car DtoToEntity(CarDto carDto) {
        Car car = modelMapper.map(carDto, Car.class);
        
        // Set the model
        if (carDto.getModelId() != null) {
            Model model = modelRepository.findById(carDto.getModelId()).orElseThrow();
            car.setModel(model);
        }
        
        return car;
    }

    // Gelişmiş filtreleme metodları
    @Override
    public List<CarDto> getBySegment(String segment) {
        List<Car> cars = carRepository.findBySegment(segment);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByTransmission(String transmission) {
        List<Car> cars = carRepository.findByTransmission(transmission);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByFuelType(String fuelType) {
        List<Car> cars = carRepository.findByFuelType(fuelType);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByStateAndSegment(Integer state, String segment) {
        List<Car> cars = carRepository.findByStateAndSegment(state, segment);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> searchByPlateOrModelOrBrand(String searchTerm) {
        List<Car> cars = carRepository.findByPlateOrModelNameOrBrandName(searchTerm, searchTerm);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getByFeature(String feature) {
        List<Car> cars = carRepository.findByFeature(feature);
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getCarsWithUpcomingMaintenance() {
        List<Car> cars = carRepository.findCarsWithUpcomingMaintenance();
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getCarsWithExpiringInsurance() {
        List<Car> cars = carRepository.findCarsWithExpiringInsurance();
        return cars.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    // Bakım kayıtları
    @Override
    public List<MaintenanceRecordDto> getMaintenanceHistory(Long carId) {
        List<MaintenanceRecord> records = maintenanceRecordRepository.findByCarIdOrderByMaintenanceDateDesc(carId);
        return records.stream().map(this::mapToMaintenanceRecordDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaintenanceRecordDto addMaintenanceRecord(MaintenanceRecordDto maintenanceRecordDto) {
        MaintenanceRecord record = mapToMaintenanceRecordEntity(maintenanceRecordDto);
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        
        // Update car's last maintenance date
        Car car = carRepository.findById(maintenanceRecordDto.getCarId()).orElseThrow();
        car.setLastMaintenanceDate(maintenanceRecordDto.getMaintenanceDate());
        car.setMileage(maintenanceRecordDto.getMileage());
        carRepository.save(car);
        
        return mapToMaintenanceRecordDto(saved);
    }

    @Override
    @Transactional
    public MaintenanceRecordDto updateMaintenanceRecord(MaintenanceRecordDto maintenanceRecordDto) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(maintenanceRecordDto.getId()).orElseThrow();
        record.setMaintenanceDate(maintenanceRecordDto.getMaintenanceDate());
        record.setType(maintenanceRecordDto.getType());
        record.setDescription(maintenanceRecordDto.getDescription());
        record.setCost(maintenanceRecordDto.getCost());
        record.setMileage(maintenanceRecordDto.getMileage());
        record.setServiceProvider(maintenanceRecordDto.getServiceProvider());
        
        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        return mapToMaintenanceRecordDto(saved);
    }

    @Override
    @Transactional
    public void deleteMaintenanceRecord(Long maintenanceRecordId) {
        maintenanceRecordRepository.deleteById(maintenanceRecordId);
    }

    @Override
    @Transactional
    public CarDto updateLocation(Long carId, Double latitude, Double longitude) {
        Car car = carRepository.findById(carId).orElseThrow();
        car.setGpsLatitude(latitude);
        car.setGpsLongitude(longitude);
        car.setLastLocationUpdate(LocalDateTime.now());
        
        Car saved = carRepository.save(car);
        return EntityToDto(saved);
    }

    private MaintenanceRecordDto mapToMaintenanceRecordDto(MaintenanceRecord record) {
        MaintenanceRecordDto dto = new MaintenanceRecordDto();
        dto.setId(record.getId());
        dto.setCarId(record.getCar().getId());
        dto.setMaintenanceDate(record.getMaintenanceDate());
        dto.setType(record.getType());
        dto.setDescription(record.getDescription());
        dto.setCost(record.getCost());
        dto.setMileage(record.getMileage());
        dto.setServiceProvider(record.getServiceProvider());
        return dto;
    }

    private MaintenanceRecord mapToMaintenanceRecordEntity(MaintenanceRecordDto dto) {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setId(dto.getId());
        record.setMaintenanceDate(dto.getMaintenanceDate());
        record.setType(dto.getType());
        record.setDescription(dto.getDescription());
        record.setCost(dto.getCost());
        record.setMileage(dto.getMileage());
        record.setServiceProvider(dto.getServiceProvider());
        
        Car car = carRepository.findById(dto.getCarId()).orElseThrow();
        record.setCar(car);
        
        return record;
    }
}

