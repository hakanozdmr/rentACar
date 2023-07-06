package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.Brand;
import hakan.rentacar.entities.dtos.BrandDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BrandService{

    List<BrandDto> getAll();

    BrandDto getById(Long id);
    BrandDto add(BrandDto brandDto);

    BrandDto update(BrandDto brandDto);

    BrandDto delete(Long id);


    //model mapper
    public BrandDto EntityToDto(Brand brand);
    public Brand DtoToEntity(BrandDto brandDto);
}
