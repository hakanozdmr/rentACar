package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.entities.dtos.ModelDto;
import hakan.rentacar.service.BrandService;
import hakan.rentacar.service.ModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/models")
public class ModelsController {

    private final ModelService modelService;

    public ModelsController(ModelService modelService) {
        this.modelService = modelService;
    }
    @GetMapping()
    public ResponseEntity<?> getAll(){
        return new ResponseEntity<>(modelService.getAll(), HttpStatus.OK);
    }

    @PostMapping()
    @ResponseStatus(code = HttpStatus.CREATED)
    public ModelDto add(@RequestBody ModelDto modelDto){
        modelService.add(modelDto);
        return modelDto;
    }
}
