package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.ContractTemplateDto;

import java.util.List;

public interface ContractTemplateService {

    List<ContractTemplateDto> getAll();

    ContractTemplateDto getById(Long id);

    ContractTemplateDto getByTemplateKey(String templateKey);

    ContractTemplateDto getDefaultTemplate();

    List<ContractTemplateDto> getActiveTemplates();

    ContractTemplateDto add(ContractTemplateDto templateDto);

    ContractTemplateDto update(ContractTemplateDto templateDto);

    void delete(Long id);

    ContractTemplateDto setAsDefault(Long id);

    String replaceVariables(String content, java.util.Map<String, String> variables);

    // Model mapper
    ContractTemplateDto EntityToDto(hakan.rentacar.entities.concretes.ContractTemplate template);
    hakan.rentacar.entities.concretes.ContractTemplate DtoToEntity(ContractTemplateDto templateDto);
}


