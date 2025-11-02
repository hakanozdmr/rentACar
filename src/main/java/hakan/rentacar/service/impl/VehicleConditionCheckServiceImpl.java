package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Car;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.concretes.VehicleConditionCheck;
import hakan.rentacar.entities.dtos.VehicleConditionCheckDto;
import hakan.rentacar.repostories.CarRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.repostories.VehicleConditionCheckRepository;
import hakan.rentacar.service.VehicleConditionCheckService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleConditionCheckServiceImpl implements VehicleConditionCheckService {

    @Autowired
    private VehicleConditionCheckRepository vehicleConditionCheckRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<VehicleConditionCheckDto> getAll() {
        List<VehicleConditionCheck> checks = vehicleConditionCheckRepository.findAll();
        return checks.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleConditionCheckDto getById(Long id) {
        VehicleConditionCheck check = vehicleConditionCheckRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Condition check not found"));
        return EntityToDto(check);
    }

    @Override
    public List<VehicleConditionCheckDto> getByRentalId(Long rentalId) {
        List<VehicleConditionCheck> checks = vehicleConditionCheckRepository.findByRentalId(rentalId);
        return checks.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleConditionCheckDto> getByCarId(Long carId) {
        List<VehicleConditionCheck> checks = vehicleConditionCheckRepository.findByCarId(carId);
        return checks.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleConditionCheckDto> getByCheckType(VehicleConditionCheck.CheckType checkType) {
        List<VehicleConditionCheck> checks = vehicleConditionCheckRepository.findByCheckType(checkType);
        return checks.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleConditionCheckDto add(VehicleConditionCheckDto checkDto) {
        // Validate rental exists
        Rental rental = rentalRepository.findById(checkDto.getRentalId())
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // Validate car exists
        Car car = carRepository.findById(checkDto.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found"));

        VehicleConditionCheck check = DtoToEntity(checkDto);
        VehicleConditionCheck savedCheck = vehicleConditionCheckRepository.save(check);
        return EntityToDto(savedCheck);
    }

    @Override
    @Transactional
    public VehicleConditionCheckDto update(VehicleConditionCheckDto checkDto) {
        VehicleConditionCheck existingCheck = vehicleConditionCheckRepository.findById(checkDto.getId())
                .orElseThrow(() -> new NoSuchElementException("Condition check not found"));

        existingCheck.setCustomerNote(checkDto.getCustomerNote());
        existingCheck.setStaffNote(checkDto.getStaffNote());
        existingCheck.setDamageCost(checkDto.getDamageCost());

        VehicleConditionCheck savedCheck = vehicleConditionCheckRepository.save(existingCheck);
        return EntityToDto(savedCheck);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        vehicleConditionCheckRepository.deleteById(id);
    }

    @Override
    public Optional<VehicleConditionCheckDto> getLatestDeliveryCheck(Long rentalId) {
        return vehicleConditionCheckRepository
                .findLatestByRentalIdAndCheckType(rentalId, VehicleConditionCheck.CheckType.TESLIM)
                .map(this::EntityToDto);
    }

    @Override
    public Optional<VehicleConditionCheckDto> getLatestPickupCheck(Long rentalId) {
        return vehicleConditionCheckRepository
                .findLatestByRentalIdAndCheckType(rentalId, VehicleConditionCheck.CheckType.TESLIM_ALMA)
                .map(this::EntityToDto);
    }

    @Override
    @Transactional
    public VehicleConditionCheckDto confirmByCustomer(Long id) {
        VehicleConditionCheck check = vehicleConditionCheckRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Condition check not found"));

        check.setIsConfirmed(true);
        check.setConfirmedAt(java.time.LocalDateTime.now());

        VehicleConditionCheck savedCheck = vehicleConditionCheckRepository.save(check);
        return EntityToDto(savedCheck);
    }

    @Override
    public VehicleConditionCheckDto compareDeliveryAndPickup(Long rentalId) {
        Optional<VehicleConditionCheck> deliveryCheck = vehicleConditionCheckRepository
                .findLatestByRentalIdAndCheckType(rentalId, VehicleConditionCheck.CheckType.TESLIM);
        Optional<VehicleConditionCheck> pickupCheck = vehicleConditionCheckRepository
                .findLatestByRentalIdAndCheckType(rentalId, VehicleConditionCheck.CheckType.TESLIM_ALMA);

        if (deliveryCheck.isEmpty() || pickupCheck.isEmpty()) {
            throw new RuntimeException("Both delivery and pickup checks are required");
        }

        VehicleConditionCheck delivery = deliveryCheck.get();
        VehicleConditionCheck pickup = pickupCheck.get();

        VehicleConditionCheckDto comparisonDto = VehicleConditionCheckDto.builder()
                .id(pickup.getId())
                .rentalId(rentalId)
                .carId(pickup.getCar().getId())
                .checkType(VehicleConditionCheck.CheckType.TESLIM_ALMA)
                .build();

        // Compare fuel levels
        int fuelDifference = pickup.getFuelLevel() - delivery.getFuelLevel();
        if (fuelDifference < 0) {
            comparisonDto.setStaffNote("Yakıt farkı: " + Math.abs(fuelDifference) + "% eksik");
        }

        // Compare damages
        if (pickup.getBodyHasDamage() && !delivery.getBodyHasDamage()) {
            comparisonDto.setBodyHasDamage(true);
            comparisonDto.setBodyDamageDescription("Teslim sırasında hasar tespit edildi");
        }

        // Calculate damage cost if there are differences
        if (fuelDifference < 0 || 
            (pickup.getBodyHasDamage() && !delivery.getBodyHasDamage()) ||
            (pickup.getInteriorHasDamage() && !delivery.getInteriorHasDamage())) {
            comparisonDto.setDamageCost(BigDecimal.ZERO); // Can be calculated based on company policy
        }

        return comparisonDto;
    }

    @Override
    public VehicleConditionCheckDto EntityToDto(VehicleConditionCheck check) {
        if (check == null) return null;
        return modelMapper.map(check, VehicleConditionCheckDto.class);
    }

    @Override
    public VehicleConditionCheck DtoToEntity(VehicleConditionCheckDto checkDto) {
        if (checkDto == null) return null;
        return modelMapper.map(checkDto, VehicleConditionCheck.class);
    }
}

