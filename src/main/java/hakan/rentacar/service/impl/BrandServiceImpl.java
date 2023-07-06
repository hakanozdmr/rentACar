package hakan.rentacar.service.impl;

import hakan.rentacar.business.rules.BrandBusinessRules;
import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.dtos.BrandDto;
import hakan.rentacar.repostories.BrandRepostory;
import hakan.rentacar.service.BrandService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    private BrandRepostory brandRepostory;
    @Autowired
    private BrandBusinessRules brandBusinessRules;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public BrandServiceImpl(BrandRepostory brandRepostory, ModelMapper modelMapper) {
        this.brandRepostory = brandRepostory;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<BrandDto> getAll() {
       /* List<BrandDto> listDto = new ArrayList<>();
        Iterable<Brand> entityList = brandRepostory.findAll();
        for (Brand brand : entityList) {
            BrandDto brandDto  = EntityToDto(brand);//model
            listDto.add(brandDto);
        }*/
        Sort sort = Sort.by(Sort.Direction.ASC,"id");
        List<Brand> brands = brandRepostory.findAll(sort);
        List<BrandDto> listDto = brands.stream().map(brand -> EntityToDto(brand)).collect(Collectors.toList());
        return  listDto;
    }

    @Override
    public BrandDto getById(Long id) {
        Brand brand = brandRepostory.findById(id).orElseThrow();
        BrandDto brandDto = EntityToDto(brand);
        return brandDto;
    }

    @Override
    public BrandDto add(BrandDto brandDto) {
        this.brandBusinessRules.checkIfBrandNameExists(brandDto.getName());
        Brand brand = DtoToEntity(brandDto);//ModelMapper
        brandRepostory.save(brand);
        return brandDto;
    }

    @Override
    public BrandDto update(BrandDto brandDto) {
        Brand brand = DtoToEntity(brandDto);//ModelMapper
        brandRepostory.save(brand);
        return brandDto;
    }

    @Override
    public BrandDto delete(Long id) {
        Brand brand = brandRepostory.findById(id).get();
        BrandDto brandDto = EntityToDto(brand);
        brandRepostory.deleteById(id);
        return brandDto;
    }

    ////////////////////////////////////
    //Model Mapper Entity ==> Dto
    @Override
    public BrandDto EntityToDto(Brand brand) {
        BrandDto brandDto = modelMapper.map(brand, BrandDto.class);
        return brandDto;
    }

    //Model Mapper Dto  ==> Entity
    @Override
    public Brand DtoToEntity(BrandDto brandDto) {
        Brand brand = modelMapper.map(brandDto, Brand.class);
        return brand;
    }
}
