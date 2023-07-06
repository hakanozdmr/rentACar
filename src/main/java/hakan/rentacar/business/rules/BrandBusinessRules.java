package hakan.rentacar.business.rules;

import hakan.rentacar.exceptions.BusinessException;
import hakan.rentacar.repostories.BrandRepostory;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@Service
public class BrandBusinessRules {

    @Autowired
    private BrandRepostory brandRepostory;

    public void checkIfBrandNameExists(String name){
        if(this.brandRepostory.existsByName(name)){
            throw new BusinessException("Brand name already exists");
        }
    }
}
