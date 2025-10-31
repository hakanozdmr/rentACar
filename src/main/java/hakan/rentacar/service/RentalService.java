package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.dtos.RentalDto;

import java.time.LocalDate;
import java.util.List;

public interface RentalService {

    List<RentalDto> getAll();

    RentalDto getById(Long id);
    
    RentalDto add(RentalDto rentalDto);

    RentalDto update(RentalDto rentalDto);

    RentalDto delete(Long id);
    
    List<RentalDto> getByCarId(Long carId);
    
    List<RentalDto> getByCustomerId(Long customerId);
    
    List<RentalDto> findByDate(LocalDate date);
    
    List<RentalDto> findActiveRentals(LocalDate date);
    
    List<RentalDto> findRentalsBetweenDates(LocalDate startDate, LocalDate endDate);
    
    boolean isCarAvailable(Long carId, LocalDate start, LocalDate end);

    // Model mapper
    RentalDto EntityToDto(Rental rental);
    Rental DtoToEntity(RentalDto rentalDto);
}


