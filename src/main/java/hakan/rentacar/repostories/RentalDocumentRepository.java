package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.RentalDocument;
import hakan.rentacar.entities.concretes.RentalDocument.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RentalDocumentRepository extends JpaRepository<RentalDocument, Long> {
    
    List<RentalDocument> findByRentalId(Long rentalId);
    
    List<RentalDocument> findByRentalIdAndDocumentType(Long rentalId, DocumentType documentType);
    
    List<RentalDocument> findByDocumentType(DocumentType documentType);
    
    List<RentalDocument> findByIsVerifiedTrue();
    
    List<RentalDocument> findByIsVerifiedFalse();
    
    @Query("SELECT rd FROM RentalDocument rd WHERE rd.rental.id = :rentalId ORDER BY rd.uploadedAt DESC")
    List<RentalDocument> findByRentalIdOrderByUploadedAtDesc(@Param("rentalId") Long rentalId);
    
    @Query("SELECT rd FROM RentalDocument rd WHERE rd.uploadedAt BETWEEN :startDate AND :endDate")
    List<RentalDocument> findDocumentsUploadedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(rd) FROM RentalDocument rd WHERE rd.documentType = :documentType AND rd.isVerified = :isVerified")
    Long countByDocumentTypeAndIsVerified(@Param("documentType") DocumentType documentType, @Param("isVerified") Boolean isVerified);
}


