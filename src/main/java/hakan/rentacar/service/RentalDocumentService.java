package hakan.rentacar.service;

import hakan.rentacar.entities.concretes.RentalDocument;
import hakan.rentacar.entities.dtos.RentalDocumentDto;

import java.util.List;

public interface RentalDocumentService {

    List<RentalDocumentDto> getAll();

    RentalDocumentDto getById(Long id);

    List<RentalDocumentDto> getByRentalId(Long rentalId);

    List<RentalDocumentDto> getByRentalIdAndType(Long rentalId, RentalDocument.DocumentType documentType);

    RentalDocumentDto add(RentalDocumentDto documentDto);

    RentalDocumentDto update(RentalDocumentDto documentDto);

    void delete(Long id);

    RentalDocumentDto verify(Long id, String verifiedBy);

    // File upload helpers
    String uploadFile(byte[] fileData, String fileName, String fileType);

    void deleteFile(String filePath);

    // Model mapper
    RentalDocumentDto EntityToDto(RentalDocument document);
    RentalDocument DtoToEntity(RentalDocumentDto documentDto);
}


