package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.Contract;
import hakan.rentacar.entities.dtos.ContractDto;
import hakan.rentacar.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Contracts", description = "Sözleşme yönetimi işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping()
    @Operation(summary = "Tüm sözleşmeleri getir")
    public ResponseEntity<List<ContractDto>> getAll() {
        return new ResponseEntity<>(contractService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile sözleşme getir")
    public ResponseEntity<ContractDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(contractService.getById(id), HttpStatus.OK);
    }

    @GetMapping("/number/{contractNumber}")
    @Operation(summary = "Sözleşme numarası ile getir")
    public ResponseEntity<ContractDto> getByContractNumber(@PathVariable("contractNumber") String contractNumber) {
        return new ResponseEntity<>(contractService.getByContractNumber(contractNumber), HttpStatus.OK);
    }

    @GetMapping("/rental/{rentalId}")
    @Operation(summary = "Kiralama ID ile sözleşmeleri getir")
    public ResponseEntity<List<ContractDto>> getByRentalId(@PathVariable("rentalId") Long rentalId) {
        return new ResponseEntity<>(contractService.getByRentalId(rentalId), HttpStatus.OK);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Müşteri ID ile sözleşmeleri getir")
    public ResponseEntity<List<ContractDto>> getByCustomerId(@PathVariable("customerId") Long customerId) {
        return new ResponseEntity<>(contractService.getByCustomerId(customerId), HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Durum ile sözleşmeleri getir")
    public ResponseEntity<List<ContractDto>> getByStatus(@PathVariable("status") Contract.ContractStatus status) {
        return new ResponseEntity<>(contractService.getByStatus(status), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Yeni sözleşme oluştur")
    public ContractDto add(@RequestBody @Valid ContractDto contractDto) {
        contractService.add(contractDto);
        return contractDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Sözleşme güncelle")
    public ContractDto update(@RequestBody @Valid ContractDto contractDto) {
        contractService.update(contractDto);
        return contractDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Sözleşme sil")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        contractService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/sign")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Sözleşmeyi imzala")
    public ContractDto signContract(
            @PathVariable("id") Long id,
            @RequestParam("customerSignature") String customerSignature,
            @RequestParam("companySignature") String companySignature) {
        return contractService.signContract(id, customerSignature, companySignature);
    }

    @PostMapping("/{id}/verify")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "E-imzayı doğrula")
    public ContractDto verifyESignature(
            @PathVariable("id") Long id,
            @RequestParam("eSignatureHash") String eSignatureHash) {
        return contractService.verifyESignature(id, eSignatureHash);
    }

    @PostMapping("/expire")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Süresi dolan sözleşmeleri işaretle")
    public ResponseEntity<Void> markAsExpired() {
        contractService.markAsExpired();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}


