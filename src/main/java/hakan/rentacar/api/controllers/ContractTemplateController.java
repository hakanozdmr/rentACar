package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.ContractTemplateDto;
import hakan.rentacar.service.ContractTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract-templates")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Contract Templates", description = "Sözleşme şablonları yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractTemplateController {

    private final ContractTemplateService contractTemplateService;

    public ContractTemplateController(ContractTemplateService contractTemplateService) {
        this.contractTemplateService = contractTemplateService;
    }

    @GetMapping()
    @Operation(summary = "Tüm şablonları getir")
    public ResponseEntity<List<ContractTemplateDto>> getAll() {
        return new ResponseEntity<>(contractTemplateService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/active")
    @Operation(summary = "Aktif şablonları getir")
    public ResponseEntity<List<ContractTemplateDto>> getActiveTemplates() {
        return new ResponseEntity<>(contractTemplateService.getActiveTemplates(), HttpStatus.OK);
    }

    @GetMapping("/default")
    @Operation(summary = "Varsayılan şablonu getir")
    public ResponseEntity<ContractTemplateDto> getDefaultTemplate() {
        return new ResponseEntity<>(contractTemplateService.getDefaultTemplate(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile şablon getir")
    public ResponseEntity<ContractTemplateDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(contractTemplateService.getById(id), HttpStatus.OK);
    }

    @GetMapping("/key/{templateKey}")
    @Operation(summary = "Şablon anahtarı ile getir")
    public ResponseEntity<ContractTemplateDto> getByTemplateKey(@PathVariable("templateKey") String templateKey) {
        return new ResponseEntity<>(contractTemplateService.getByTemplateKey(templateKey), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Yeni şablon oluştur")
    public ContractTemplateDto add(@RequestBody @Valid ContractTemplateDto templateDto) {
        contractTemplateService.add(templateDto);
        return templateDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Şablon güncelle")
    public ContractTemplateDto update(@RequestBody @Valid ContractTemplateDto templateDto) {
        contractTemplateService.update(templateDto);
        return templateDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Şablon sil")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        contractTemplateService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/set-default")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Şablonu varsayılan yap")
    public ContractTemplateDto setAsDefault(@PathVariable("id") Long id) {
        return contractTemplateService.setAsDefault(id);
    }

    @PostMapping("/replace-variables")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Şablon değişkenlerini değiştir")
    public ResponseEntity<String> replaceVariables(
            @RequestParam("content") String content,
            @RequestBody Map<String, String> variables) {
        String result = contractTemplateService.replaceVariables(content, variables);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}


