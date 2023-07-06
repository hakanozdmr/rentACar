package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
public class BrandsController {

    private final BrandService brandService;


    public BrandsController(BrandService brandService) {
        this.brandService = brandService;
    }
    @GetMapping()
    public ResponseEntity<?> findAllUsers(){
        return new ResponseEntity<>(brandService.getAll(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable("id") Long id){
        return new ResponseEntity<>(brandService.getById(id), HttpStatus.OK);
    }
    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public BrandDto add(@RequestBody @Valid BrandDto brandDto){
        brandService.add(brandDto);
        return brandDto;
    }
    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public BrandDto update(@RequestBody BrandDto brandDto){
        brandService.update(brandDto);
        return brandDto;
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.GONE)
    public ResponseEntity<?> delete(@PathVariable("id") Long id){
        return new ResponseEntity<>(brandService.delete(id), HttpStatus.OK);
    }
}
