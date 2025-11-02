package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.concretes.RentalDocument;
import hakan.rentacar.entities.dtos.RentalDocumentDto;
import hakan.rentacar.repostories.RentalDocumentRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.service.RentalDocumentService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RentalDocumentServiceImpl implements RentalDocumentService {

    @Autowired
    private RentalDocumentRepository rentalDocumentRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static final String UPLOAD_DIR = "uploads/documents/";

    @Override
    public List<RentalDocumentDto> getAll() {
        List<RentalDocument> documents = rentalDocumentRepository.findAll();
        return documents.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RentalDocumentDto getById(Long id) {
        RentalDocument document = rentalDocumentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
        return EntityToDto(document);
    }

    @Override
    public List<RentalDocumentDto> getByRentalId(Long rentalId) {
        List<RentalDocument> documents = rentalDocumentRepository.findByRentalId(rentalId);
        return documents.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RentalDocumentDto> getByRentalIdAndType(Long rentalId, RentalDocument.DocumentType documentType) {
        List<RentalDocument> documents = rentalDocumentRepository.findByRentalIdAndDocumentType(rentalId, documentType);
        return documents.stream()
                .map(this::EntityToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RentalDocumentDto add(RentalDocumentDto documentDto) {
        // Validate rental exists
        Rental rental = rentalRepository.findById(documentDto.getRentalId())
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        RentalDocument document = DtoToEntity(documentDto);
        document.setUploadedAt(LocalDateTime.now());

        RentalDocument savedDocument = rentalDocumentRepository.save(document);
        return EntityToDto(savedDocument);
    }

    @Override
    @Transactional
    public RentalDocumentDto update(RentalDocumentDto documentDto) {
        RentalDocument existingDocument = rentalDocumentRepository.findById(documentDto.getId())
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        existingDocument.setDescription(documentDto.getDescription());
        existingDocument.setMetadata(documentDto.getMetadata());

        RentalDocument savedDocument = rentalDocumentRepository.save(existingDocument);
        return EntityToDto(savedDocument);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RentalDocument document = rentalDocumentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
        
        // Delete physical file
        if (document.getFilePath() != null) {
            deleteFile(document.getFilePath());
        }
        
        rentalDocumentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RentalDocumentDto verify(Long id, String verifiedBy) {
        RentalDocument document = rentalDocumentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));

        document.setIsVerified(true);
        document.setVerifiedAt(LocalDateTime.now());
        document.setVerifiedBy(verifiedBy);

        RentalDocument savedDocument = rentalDocumentRepository.save(document);
        return EntityToDto(savedDocument);
    }

    @Override
    public String uploadFile(byte[] fileData, String fileName, String fileType) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path filePath = uploadPath.resolve(uniqueFileName);

            // Save file
            Files.write(filePath, fileData);

            return filePath.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public RentalDocumentDto EntityToDto(RentalDocument document) {
        if (document == null) return null;
        return modelMapper.map(document, RentalDocumentDto.class);
    }

    @Override
    public RentalDocument DtoToEntity(RentalDocumentDto documentDto) {
        if (documentDto == null) return null;
        return modelMapper.map(documentDto, RentalDocument.class);
    }
}


