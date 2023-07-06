package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.concretes.Model;
import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.entities.dtos.ModelDto;
import hakan.rentacar.repostories.BrandRepostory;
import hakan.rentacar.repostories.ModelRepostory;
import hakan.rentacar.service.ModelService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModelServiceImpl implements ModelService {

    private ModelRepostory modelRepostory;

    @Autowired
    private ModelMapper modelMapper;

    public ModelServiceImpl(ModelRepostory modelRepostory, ModelMapper modelMapper) {
        this.modelRepostory = modelRepostory;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ModelDto> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC,"id");
        List<Model> models = modelRepostory.findAll(sort);
        List<ModelDto> listDto = models.stream().map(model -> EntityToDto(model)).collect(Collectors.toList());
        return  listDto;
    }
    @Override
    public ModelDto add(ModelDto modelDto) {
        Model model = DtoToEntity(modelDto);//ModelMapper
        modelRepostory.save(model);
        return modelDto;
    }

    @Override
    public ModelDto EntityToDto(Model model) {
        ModelDto modelDto = modelMapper.map(model, ModelDto.class);
        return modelDto;
    }

    @Override
    public Model DtoToEntity(ModelDto modelDto) {
        Model model = modelMapper.map(modelDto, Model.class);
        return model;
    }
}
