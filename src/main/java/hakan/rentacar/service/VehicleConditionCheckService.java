package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.VehicleConditionCheck;
import hakan.rentacar.entities.dtos.VehicleConditionCheckDto;

import java.util.List;
import java.util.Optional;

public interface VehicleConditionCheckService {

    List<VehicleConditionCheckDto> getAll();

    VehicleConditionCheckDto getById(Long id);

    List<VehicleConditionCheckDto> getByRentalId(Long rentalId);

    List<VehicleConditionCheckDto> getByCarId(Long carId);

    List<VehicleConditionCheckDto> getByCheckType(VehicleConditionCheck.CheckType checkType);

    VehicleConditionCheckDto add(VehicleConditionCheckDto checkDto);

    VehicleConditionCheckDto update(VehicleConditionCheckDto checkDto);

    void delete(Long id);

    Optional<VehicleConditionCheckDto> getLatestDeliveryCheck(Long rentalId);

    Optional<VehicleConditionCheckDto> getLatestPickupCheck(Long rentalId);

    VehicleConditionCheckDto confirmByCustomer(Long id);

    VehicleConditionCheckDto compareDeliveryAndPickup(Long rentalId);

    // Model mapper
    VehicleConditionCheckDto EntityToDto(VehicleConditionCheck check);
    VehicleConditionCheck DtoToEntity(VehicleConditionCheckDto checkDto);
}


