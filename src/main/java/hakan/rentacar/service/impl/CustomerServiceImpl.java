package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.dtos.CustomerDto;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.service.CustomerService;
import hakan.rentacar.audit.Auditable;
import hakan.rentacar.entities.concretes.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<CustomerDto> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Customer> customers = customerRepository.findAll(sort);
        return customers.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public CustomerDto getById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        return EntityToDto(customer);
    }

    @Override
    @Auditable(entity = "Customer", action = AuditLog.ActionType.CREATE, description = "Create new customer")
    public CustomerDto add(CustomerDto customerDto) {
        // Validate age (must be at least 18)
        if (Period.between(customerDto.getDateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new RuntimeException("Customer must be at least 18 years old");
        }
        
        // Check if ID number already exists
        if (customerRepository.existsByIdNumber(customerDto.getIdNumber())) {
            throw new RuntimeException("Customer with ID number " + customerDto.getIdNumber() + " already exists");
        }
        
        // Check if driver license already exists
        if (customerRepository.existsByDriverLicenseNumber(customerDto.getDriverLicenseNumber())) {
            throw new RuntimeException("Customer with driver license " + customerDto.getDriverLicenseNumber() + " already exists");
        }
        
        // Check if email already exists
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new RuntimeException("Customer with email " + customerDto.getEmail() + " already exists");
        }
        
        Customer customer = DtoToEntity(customerDto);
        customerRepository.save(customer);
        return customerDto;
    }

    @Override
    @Auditable(entity = "Customer", action = AuditLog.ActionType.UPDATE, description = "Update customer information")
    public CustomerDto update(CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(customerDto.getId()).orElseThrow();
        
        // Validate age (must be at least 18)
        if (Period.between(customerDto.getDateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new RuntimeException("Customer must be at least 18 years old");
        }
        
        // Check if ID number is being changed and new ID already exists
        if (!existingCustomer.getIdNumber().equals(customerDto.getIdNumber()) && 
            customerRepository.existsByIdNumber(customerDto.getIdNumber())) {
            throw new RuntimeException("Customer with ID number " + customerDto.getIdNumber() + " already exists");
        }
        
        // Check if driver license is being changed and new license already exists
        if (!existingCustomer.getDriverLicenseNumber().equals(customerDto.getDriverLicenseNumber()) && 
            customerRepository.existsByDriverLicenseNumber(customerDto.getDriverLicenseNumber())) {
            throw new RuntimeException("Customer with driver license " + customerDto.getDriverLicenseNumber() + " already exists");
        }
        
        // Check if email is being changed and new email already exists
        if (!existingCustomer.getEmail().equals(customerDto.getEmail()) && 
            customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new RuntimeException("Customer with email " + customerDto.getEmail() + " already exists");
        }
        
        Customer customer = DtoToEntity(customerDto);
        customerRepository.save(customer);
        return customerDto;
    }

    @Override
    @Auditable(entity = "Customer", action = AuditLog.ActionType.DELETE, description = "Delete customer")
    public CustomerDto delete(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow();
        CustomerDto customerDto = EntityToDto(customer);
        customerRepository.deleteById(id);
        return customerDto;
    }

    @Override
    public CustomerDto getByIdNumber(String idNumber) {
        Customer customer = customerRepository.findByIdNumber(idNumber).orElseThrow();
        return EntityToDto(customer);
    }

    @Override
    public CustomerDto getByDriverLicenseNumber(String driverLicenseNumber) {
        Customer customer = customerRepository.findByDriverLicenseNumber(driverLicenseNumber).orElseThrow();
        return EntityToDto(customer);
    }

    @Override
    public CustomerDto getByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow();
        return EntityToDto(customer);
    }

    @Override
    public List<CustomerDto> getByCity(String city) {
        List<Customer> customers = customerRepository.findByCity(city);
        return customers.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<CustomerDto> findByNameContaining(String name) {
        List<Customer> customers = customerRepository.findByNameContaining(name);
        return customers.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public CustomerDto EntityToDto(Customer customer) {
        return modelMapper.map(customer, CustomerDto.class);
    }

    @Override
    public Customer DtoToEntity(CustomerDto customerDto) {
        return modelMapper.map(customerDto, Customer.class);
    }
}
