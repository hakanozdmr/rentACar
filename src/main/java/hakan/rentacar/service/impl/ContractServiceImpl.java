package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Contract;
import hakan.rentacar.entities.concretes.ContractTemplate;
import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.dtos.ContractDto;
import hakan.rentacar.repostories.ContractRepository;
import hakan.rentacar.repostories.ContractTemplateRepository;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.service.ContractService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ContractTemplateRepository contractTemplateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ContractDto> getAll() {
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContractDto getById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contract not found"));
        return EntityToDto(contract);
    }

    @Override
    public ContractDto getByContractNumber(String contractNumber) {
        Contract contract = contractRepository.findByContractNumber(contractNumber)
                .orElseThrow(() -> new NoSuchElementException("Contract not found"));
        return EntityToDto(contract);
    }

    @Override
    public List<ContractDto> getByRentalId(Long rentalId) {
        List<Contract> contracts = contractRepository.findByRentalId(rentalId);
        return contracts.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContractDto> getByCustomerId(Long customerId) {
        List<Contract> contracts = contractRepository.findByCustomerId(customerId);
        return contracts.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContractDto> getByStatus(Contract.ContractStatus status) {
        List<Contract> contracts = contractRepository.findByStatus(status);
        return contracts.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractDto add(ContractDto contractDto) {
        // Convert Dto to Entity (DtoToEntity already loads and sets relationships)
        Contract contract = DtoToEntity(contractDto);
        
        // Override auto-set values
        contract.setStatus(Contract.ContractStatus.DRAFT);
        contract.setSignedDate(LocalDate.now());

        // Generate contract number if not provided
        if (contract.getContractNumber() == null || contract.getContractNumber().isEmpty()) {
            contract.setContractNumber(generateContractNumber());
        }

        Contract savedContract = contractRepository.save(contract);
        contractDto.setId(savedContract.getId());
        return contractDto;
    }

    @Override
    @Transactional
    public ContractDto update(ContractDto contractDto) {
        Contract existingContract = contractRepository.findById(contractDto.getId())
                .orElseThrow(() -> new NoSuchElementException("Contract not found"));

        existingContract.setTerms(contractDto.getTerms());
        existingContract.setConditions(contractDto.getConditions());
        existingContract.setNotes(contractDto.getNotes());
        existingContract.setExpiryDate(contractDto.getExpiryDate());

        Contract savedContract = contractRepository.save(existingContract);
        return EntityToDto(savedContract);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ContractDto signContract(Long id, String customerSignature, String companySignature) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contract not found"));

        contract.setCustomerSignature(customerSignature);
        contract.setCompanySignature(companySignature);
        contract.setSignedAt(LocalDateTime.now());
        contract.setStatus(Contract.ContractStatus.SIGNED);

        Contract savedContract = contractRepository.save(contract);
        return EntityToDto(savedContract);
    }

    @Override
    @Transactional
    public ContractDto verifyESignature(Long id, String eSignatureHash) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contract not found"));

        contract.setESignatureHash(eSignatureHash);
        contract.setESignatureVerifiedAt(LocalDateTime.now());
        contract.setStatus(Contract.ContractStatus.VERIFIED);

        Contract savedContract = contractRepository.save(contract);
        return EntityToDto(savedContract);
    }

    @Override
    @Transactional
    public void markAsExpired() {
        LocalDate today = LocalDate.now();
        List<Contract.ContractStatus> excludedStatuses = List.of(
                Contract.ContractStatus.CANCELLED,
                Contract.ContractStatus.EXPIRED
        );
        List<Contract> expiredContracts = contractRepository.findExpiredContracts(today, excludedStatuses);
        
        for (Contract contract : expiredContracts) {
            contract.setStatus(Contract.ContractStatus.EXPIRED);
        }
        contractRepository.saveAll(expiredContracts);
    }

    @Override
    @Transactional
    public ContractDto generateFromTemplate(Long rentalId, Long templateId) {
        // This will be implemented with template processing
        throw new UnsupportedOperationException("Template generation not yet implemented");
    }

    @Override
    public ContractDto EntityToDto(Contract contract) {
        if (contract == null) return null;
        return modelMapper.map(contract, ContractDto.class);
    }

    @Override
    public Contract DtoToEntity(ContractDto contractDto) {
        if (contractDto == null) return null;
        Contract contract = modelMapper.map(contractDto, Contract.class);
        
        // Set the rental
        if (contractDto.getRentalId() != null) {
            Rental rental = rentalRepository.findById(contractDto.getRentalId()).orElseThrow();
            contract.setRental(rental);
        }
        
        // Set the customer
        if (contractDto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(contractDto.getCustomerId()).orElseThrow();
            contract.setCustomer(customer);
        }
        
        // Set the template
        if (contractDto.getTemplateId() != null) {
            ContractTemplate template = contractTemplateRepository.findById(contractDto.getTemplateId()).orElseThrow();
            contract.setTemplate(template);
        }
        
        return contract;
    }

    private String generateContractNumber() {
        // Format: KIR-YYYY-MMDDNNN
        LocalDate today = LocalDate.now();
        String prefix = "KIR-" + today.getYear() + "-";
        long count = contractRepository.count();
        return prefix + String.format("%03d", count + 1);
    }
}

