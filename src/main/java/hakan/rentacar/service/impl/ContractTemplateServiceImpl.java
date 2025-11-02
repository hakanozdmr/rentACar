package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.ContractTemplate;
import hakan.rentacar.entities.dtos.ContractTemplateDto;
import hakan.rentacar.repostories.ContractTemplateRepository;
import hakan.rentacar.service.ContractTemplateService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ContractTemplateServiceImpl implements ContractTemplateService {

    @Autowired
    private ContractTemplateRepository contractTemplateRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ContractTemplateDto> getAll() {
        List<ContractTemplate> templates = contractTemplateRepository.findAll();
        return templates.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ContractTemplateDto getById(Long id) {
        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found"));
        return EntityToDto(template);
    }

    @Override
    public ContractTemplateDto getByTemplateKey(String templateKey) {
        ContractTemplate template = contractTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new NoSuchElementException("Template not found"));
        return EntityToDto(template);
    }

    @Override
    public ContractTemplateDto getDefaultTemplate() {
        ContractTemplate template = contractTemplateRepository.findDefaultActiveTemplate()
                .orElseThrow(() -> new NoSuchElementException("No default template found"));
        return EntityToDto(template);
    }

    @Override
    public List<ContractTemplateDto> getActiveTemplates() {
        List<ContractTemplate> templates = contractTemplateRepository.findByIsActiveTrue();
        return templates.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContractTemplateDto add(ContractTemplateDto templateDto) {
        ContractTemplate template = DtoToEntity(templateDto);
        template.setIsActive(true);
        template.setUsageCount(0L);
        ContractTemplate savedTemplate = contractTemplateRepository.save(template);
        return EntityToDto(savedTemplate);
    }

    @Override
    @Transactional
    public ContractTemplateDto update(ContractTemplateDto templateDto) {
        ContractTemplate existingTemplate = contractTemplateRepository.findById(templateDto.getId())
                .orElseThrow(() -> new NoSuchElementException("Template not found"));

        existingTemplate.setName(templateDto.getName());
        existingTemplate.setDescription(templateDto.getDescription());
        existingTemplate.setContent(templateDto.getContent());
        existingTemplate.setVariables(templateDto.getVariables());
        
        ContractTemplate savedTemplate = contractTemplateRepository.save(existingTemplate);
        return EntityToDto(savedTemplate);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        contractTemplateRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ContractTemplateDto setAsDefault(Long id) {
        // Remove default flag from all templates
        List<ContractTemplate> allTemplates = contractTemplateRepository.findAll();
        for (ContractTemplate template : allTemplates) {
            template.setIsDefault(false);
        }
        contractTemplateRepository.saveAll(allTemplates);

        // Set this template as default
        ContractTemplate template = contractTemplateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found"));
        template.setIsDefault(true);
        template.setLastUsedAt(LocalDateTime.now());
        
        ContractTemplate savedTemplate = contractTemplateRepository.save(template);
        return EntityToDto(savedTemplate);
    }

    @Override
    public String replaceVariables(String content, Map<String, String> variables) {
        if (content == null || variables == null) return content;
        
        String result = content;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }
        return result;
    }

    @Override
    public ContractTemplateDto EntityToDto(ContractTemplate template) {
        if (template == null) return null;
        return modelMapper.map(template, ContractTemplateDto.class);
    }

    @Override
    public ContractTemplate DtoToEntity(ContractTemplateDto templateDto) {
        if (templateDto == null) return null;
        return modelMapper.map(templateDto, ContractTemplate.class);
    }
}


