package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Brands", description = "Araç markası yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class BrandsController {

    private final BrandService brandService;


    public BrandsController(BrandService brandService) {
        this.brandService = brandService;
    }
    @GetMapping()
    @Operation(summary = "Tüm markaları listele", description = "Sistemde bulunan tüm araç markalarını getirir")
    @ApiResponse(
        responseCode = "200", 
        description = "Başarılı",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BrandDto[].class),
            examples = @ExampleObject(
                name = "Marka Listesi",
                value = """
                [
                  {
                    "id": 1,
                    "name": "Toyota"
                  },
                  {
                    "id": 2,
                    "name": "Honda"
                  },
                  {
                    "id": 3,
                    "name": "BMW"
                  }
                ]
                """
            )
        )
    )
    public ResponseEntity<List<BrandDto>> findAllBrands(){
        return new ResponseEntity<>(brandService.getAll(), HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "ID ile marka getir", description = "Belirtilen ID'ye sahip markayı getirir")
    @ApiResponse(
        responseCode = "200", 
        description = "Marka bulundu",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BrandDto.class),
            examples = @ExampleObject(
                name = "Marka Detayı",
                value = """
                {
                  "id": 1,
                  "name": "Toyota"
                }
                """
            )
        )
    )
    @ApiResponse(responseCode = "404", description = "Marka bulunamadı")
    public ResponseEntity<BrandDto> findById(
        @Parameter(description = "Marka ID", example = "1")
        @PathVariable("id") Long id
    ){
        return new ResponseEntity<>(brandService.getById(id), HttpStatus.OK);
    }
    
    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Yeni marka ekle", description = "Sisteme yeni bir araç markası ekler")
    @ApiResponse(
        responseCode = "201", 
        description = "Marka başarıyla oluşturuldu",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BrandDto.class),
            examples = @ExampleObject(
                name = "Oluşturulan Marka",
                value = """
                {
                  "id": 4,
                  "name": "Mercedes"
                }
                """
            )
        )
    )
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya validation hatası")
    public BrandDto add(
        @Parameter(description = "Oluşturulacak marka bilgileri", required = true)
        @RequestBody @Valid BrandDto brandDto
    ){
        brandService.add(brandDto);
        return brandDto;
    }
    
    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Marka güncelle", description = "Mevcut bir markayı günceller")
    @ApiResponse(
        responseCode = "200", 
        description = "Marka başarıyla güncellendi",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BrandDto.class)
        )
    )
    @ApiResponse(responseCode = "404", description = "Marka bulunamadı")
    @ApiResponse(responseCode = "400", description = "Geçersiz istek")
    public BrandDto update(
        @Parameter(description = "Güncellenecek marka bilgileri", required = true)
        @RequestBody BrandDto brandDto
    ){
        brandService.update(brandDto);
        return brandDto;
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.GONE)
    @Operation(summary = "Marka sil", description = "Belirtilen ID'ye sahip markayı siler")
    @ApiResponse(
        responseCode = "200", 
        description = "Marka başarıyla silindi",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BrandDto.class)
        )
    )
    @ApiResponse(responseCode = "404", description = "Marka bulunamadı")
    public ResponseEntity<BrandDto> delete(
        @Parameter(description = "Silinecek marka ID", example = "1")
        @PathVariable("id") Long id
    ){
        BrandDto result = brandService.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
