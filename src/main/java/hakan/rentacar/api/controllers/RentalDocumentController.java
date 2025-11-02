package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.RentalDocument;
import hakan.rentacar.entities.dtos.RentalDocumentDto;
import hakan.rentacar.service.RentalDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rental-documents")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Rental Documents", description = "Kiralama belgeleri yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class RentalDocumentController {

    private final RentalDocumentService rentalDocumentService;

    public RentalDocumentController(RentalDocumentService rentalDocumentService) {
        this.rentalDocumentService = rentalDocumentService;
    }

    @GetMapping()
    @Operation(summary = "Tüm belgeleri getir")
    public ResponseEntity<List<RentalDocumentDto>> getAll() {
        return new ResponseEntity<>(rentalDocumentService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "ID ile belge getir")
    public ResponseEntity<RentalDocumentDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(rentalDocumentService.getById(id), HttpStatus.OK);
    }

    @GetMapping("/rental/{rentalId}")
    @Operation(summary = "Kiralama ID ile belgeleri getir")
    public ResponseEntity<List<RentalDocumentDto>> getByRentalId(@PathVariable("rentalId") Long rentalId) {
        return new ResponseEntity<>(rentalDocumentService.getByRentalId(rentalId), HttpStatus.OK);
    }

    @GetMapping("/rental/{rentalId}/type/{documentType}")
    @Operation(summary = "Kiralama ve tip ile belgeleri getir")
    public ResponseEntity<List<RentalDocumentDto>> getByRentalIdAndType(
            @PathVariable("rentalId") Long rentalId,
            @PathVariable("documentType") RentalDocument.DocumentType documentType) {
        return new ResponseEntity<>(rentalDocumentService.getByRentalIdAndType(rentalId, documentType), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Yeni belge oluştur")
    public RentalDocumentDto add(@RequestBody @Valid RentalDocumentDto documentDto) {
        rentalDocumentService.add(documentDto);
        return documentDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Belge güncelle")
    public RentalDocumentDto update(@RequestBody @Valid RentalDocumentDto documentDto) {
        rentalDocumentService.update(documentDto);
        return documentDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Belge sil")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        rentalDocumentService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/verify")
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Belgeyi doğrula")
    public RentalDocumentDto verify(
            @PathVariable("id") Long id,
            @RequestParam("verifiedBy") String verifiedBy) {
        return rentalDocumentService.verify(id, verifiedBy);
    }

    @PostMapping("/upload")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Dosya yükle")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = rentalDocumentService.uploadFile(file.getBytes(), file.getOriginalFilename(), file.getContentType());
            return new ResponseEntity<>(filePath, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


