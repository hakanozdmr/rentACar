package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.concretes.Model;
import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.entities.dtos.ModelDto;

import java.util.List;

public interface ModelService {
    List<ModelDto> getAll();

    ModelDto getById(Long id);

    ModelDto add(ModelDto modelDto);

    ModelDto update(ModelDto modelDto);

    ModelDto delete(Long id);

    List<ModelDto> getByBrandId(Long brandId);

    //model mapper
    public ModelDto EntityToDto(Model model);
    public Model DtoToEntity(ModelDto modelDto);
}
