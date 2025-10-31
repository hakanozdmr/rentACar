package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    boolean existsByIdNumber(String idNumber);
    
    boolean existsByDriverLicenseNumber(String driverLicenseNumber);
    
    boolean existsByEmail(String email);
    
    Optional<Customer> findByIdNumber(String idNumber);
    
    Optional<Customer> findByDriverLicenseNumber(String driverLicenseNumber);
    
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.city = :city")
    List<Customer> findByCity(@Param("city") String city);
    
    @Query("SELECT c FROM Customer c WHERE c.firstName LIKE CONCAT('%', :name, '%') OR c.lastName LIKE CONCAT('%', :name, '%')")
    List<Customer> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Customer c WHERE c.user.id = :userId")
    Optional<Customer> findByUserId(@Param("userId") Long userId);
}
