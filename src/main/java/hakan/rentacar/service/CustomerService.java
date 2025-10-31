package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.dtos.CustomerDto;

import java.util.List;

public interface CustomerService {

    List<CustomerDto> getAll();

    CustomerDto getById(Long id);
    
    CustomerDto add(CustomerDto customerDto);

    CustomerDto update(CustomerDto customerDto);

    CustomerDto delete(Long id);
    
    CustomerDto getByIdNumber(String idNumber);
    
    CustomerDto getByDriverLicenseNumber(String driverLicenseNumber);
    
    CustomerDto getByEmail(String email);
    
    List<CustomerDto> getByCity(String city);
    
    List<CustomerDto> findByNameContaining(String name);

    // Model mapper
    CustomerDto EntityToDto(Customer customer);
    Customer DtoToEntity(CustomerDto customerDto);
}


