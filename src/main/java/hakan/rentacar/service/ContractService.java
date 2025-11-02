package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Contract;
import hakan.rentacar.entities.dtos.ContractDto;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {

    List<ContractDto> getAll();

    ContractDto getById(Long id);

    ContractDto getByContractNumber(String contractNumber);

    List<ContractDto> getByRentalId(Long rentalId);

    List<ContractDto> getByCustomerId(Long customerId);

    List<ContractDto> getByStatus(Contract.ContractStatus status);

    ContractDto add(ContractDto contractDto);

    ContractDto update(ContractDto contractDto);

    void delete(Long id);

    ContractDto signContract(Long id, String customerSignature, String companySignature);

    ContractDto verifyESignature(Long id, String eSignatureHash);

    void markAsExpired();

    // Template-based contract generation
    ContractDto generateFromTemplate(Long rentalId, Long templateId);

    // Model mapper
    ContractDto EntityToDto(Contract contract);
    Contract DtoToEntity(ContractDto contractDto);
}


