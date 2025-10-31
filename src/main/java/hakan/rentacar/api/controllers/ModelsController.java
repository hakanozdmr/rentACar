package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.ModelDto;
import hakan.rentacar.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/models")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Models", description = "Araç model yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class ModelsController {

    private final ModelService modelService;

    public ModelsController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping()
    public ResponseEntity<List<ModelDto>> getAll() {
        return new ResponseEntity<>(modelService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModelDto> getById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(modelService.getById(id), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public ModelDto add(@RequestBody @Valid ModelDto modelDto) {
        modelService.add(modelDto);
        return modelDto;
    }

    @PutMapping()
    @ResponseStatus(code = HttpStatus.OK)
    public ModelDto update(@RequestBody @Valid ModelDto modelDto) {
        modelService.update(modelDto);
        return modelDto;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public ResponseEntity<ModelDto> delete(@PathVariable("id") Long id) {
        return new ResponseEntity<>(modelService.delete(id), HttpStatus.OK);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<ModelDto>> getByBrandId(@PathVariable("brandId") Long brandId) {
        return new ResponseEntity<>(modelService.getByBrandId(brandId), HttpStatus.OK);
    }
}
