package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.concretes.Model;
import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.entities.dtos.ModelDto;

import java.util.List;

public interface ModelService {
    List<ModelDto> getAll();

    ModelDto add(ModelDto modelDto);

    //model mapper
    public ModelDto EntityToDto(Model model);
    public Model DtoToEntity(ModelDto modelDto);

}
