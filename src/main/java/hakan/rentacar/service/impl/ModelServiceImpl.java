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

    private final ModelRepostory modelRepostory;
    private final BrandRepostory brandRepository;

    @Autowired
    private ModelMapper modelMapper;

    public ModelServiceImpl(ModelRepostory modelRepostory, BrandRepostory brandRepository) {
        this.modelRepostory = modelRepostory;
        this.brandRepository = brandRepository;
    }

    @Override
    public List<ModelDto> getAll() {
        Sort sort = Sort.by(Sort.Direction.ASC,"id");
        List<Model> models = modelRepostory.findAll(sort);
        List<ModelDto> listDto = models.stream().map(model -> EntityToDto(model)).collect(Collectors.toList());
        return  listDto;
    }

    @Override
    public ModelDto getById(Long id) {
        Model model = modelRepostory.findById(id).orElseThrow();
        return EntityToDto(model);
    }

    @Override
    public ModelDto add(ModelDto modelDto) {
        // Check if model name already exists for this brand
        if (modelRepostory.existsByNameAndBrandId(modelDto.getName(), modelDto.getBrandId())) {
            throw new RuntimeException("Model with name " + modelDto.getName() + " already exists for this brand");
        }
        
        // Check if brand exists
        Brand brand = brandRepository.findById(modelDto.getBrandId()).orElseThrow(
            () -> new RuntimeException("Brand not found")
        );
        
        Model model = DtoToEntity(modelDto);
        modelRepostory.save(model);
        return modelDto;
    }

    @Override
    public ModelDto update(ModelDto modelDto) {
        Model existingModel = modelRepostory.findById(modelDto.getId()).orElseThrow();
        
        // Check if model name is being changed and new name already exists for this brand
        if (!existingModel.getName().equals(modelDto.getName()) && 
            modelRepostory.existsByNameAndBrandId(modelDto.getName(), modelDto.getBrandId())) {
            throw new RuntimeException("Model with name " + modelDto.getName() + " already exists for this brand");
        }
        
        // Check if brand exists
        Brand brand = brandRepository.findById(modelDto.getBrandId()).orElseThrow(
            () -> new RuntimeException("Brand not found")
        );
        
        Model model = DtoToEntity(modelDto);
        modelRepostory.save(model);
        return modelDto;
    }

    @Override
    public ModelDto delete(Long id) {
        Model model = modelRepostory.findById(id).orElseThrow();
        ModelDto modelDto = EntityToDto(model);
        modelRepostory.deleteById(id);
        return modelDto;
    }

    @Override
    public List<ModelDto> getByBrandId(Long brandId) {
        List<Model> models = modelRepostory.findByBrandId(brandId);
        return models.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public ModelDto EntityToDto(Model model) {
        ModelDto modelDto = modelMapper.map(model, ModelDto.class);
        if (model.getBrand() != null) {
            modelDto.setBrandId(model.getBrand().getId());
            modelDto.setBrandName(model.getBrand().getName());
        }
        return modelDto;
    }

    @Override
    public Model DtoToEntity(ModelDto modelDto) {
        Model model = modelMapper.map(modelDto, Model.class);
        
        // Set the brand
        if (modelDto.getBrandId() != null) {
            Brand brand = brandRepository.findById(modelDto.getBrandId()).orElseThrow();
            model.setBrand(brand);
        }
        
        return model;
    }
}
