package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Contract;
import hakan.rentacar.entities.concretes.Contract.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    List<Contract> findByRentalId(Long rentalId);
    
    List<Contract> findByCustomerId(Long customerId);
    
    List<Contract> findByStatus(ContractStatus status);
    
    Optional<Contract> findByContractNumber(String contractNumber);
    
    List<Contract> findByStatusAndSignedDateBetween(ContractStatus status, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT c FROM Contract c WHERE c.rental.id = :rentalId AND c.status = :status ORDER BY c.signedDate DESC")
    List<Contract> findByRentalIdAndStatus(@Param("rentalId") Long rentalId, @Param("status") ContractStatus status);
    
    @Query("SELECT c FROM Contract c WHERE c.expiryDate < :date AND c.status NOT IN (:excludedStatuses)")
    List<Contract> findExpiredContracts(@Param("date") LocalDate date, @Param("excludedStatuses") List<ContractStatus> excludedStatuses);
    
    @Query("SELECT COUNT(c) FROM Contract c WHERE c.status = :status")
    Long countByStatus(@Param("status") ContractStatus status);
    
    @Query("SELECT c FROM Contract c WHERE c.customer.id = :customerId ORDER BY c.signedDate DESC")
    List<Contract> findByCustomerIdOrderBySignedDateDesc(@Param("customerId") Long customerId);
}


