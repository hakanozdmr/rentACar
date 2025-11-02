package hakan.rentacar.bean;

import hakan.rentacar.entities.concretes.Reservation;
import hakan.rentacar.entities.concretes.Notification;
import hakan.rentacar.entities.concretes.ReservationRating;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;
import hakan.rentacar.entities.concretes.GeneralLedger;
import hakan.rentacar.entities.concretes.TaxCalculation;
import hakan.rentacar.entities.concretes.AuditLog;
import hakan.rentacar.entities.concretes.Contract;
import hakan.rentacar.entities.concretes.ContractTemplate;
import hakan.rentacar.entities.concretes.VehicleConditionCheck;
import hakan.rentacar.entities.concretes.RentalDocument;
import hakan.rentacar.entities.dtos.ReservationDto;
import hakan.rentacar.entities.dtos.NotificationDto;
import hakan.rentacar.entities.dtos.ReservationRatingDto;
import hakan.rentacar.entities.dtos.PaymentDto;
import hakan.rentacar.entities.dtos.InvoiceDto;
import hakan.rentacar.entities.dtos.GeneralLedgerDto;
import hakan.rentacar.entities.dtos.TaxCalculationDto;
import hakan.rentacar.entities.dtos.AuditLogDto;
import hakan.rentacar.entities.dtos.ContractDto;
import hakan.rentacar.entities.dtos.ContractTemplateDto;
import hakan.rentacar.entities.dtos.VehicleConditionCheckDto;
import hakan.rentacar.entities.dtos.RentalDocumentDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperBean {
    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        
        // Configure Reservation to ReservationDto mapping
        modelMapper.addMappings(new PropertyMap<Reservation, ReservationDto>() {
            @Override
            protected void configure() {
                // Skip customerName auto-mapping to avoid conflicts
                skip(destination.getCustomerName());
                // Skip car-related fields that are manually set
                skip(destination.getCarPlate());
                skip(destination.getCarBrandName());
                skip(destination.getCarModelName());
            }
        });
        
        // Configure Notification to NotificationDto mapping
        modelMapper.addMappings(new PropertyMap<Notification, NotificationDto>() {
            @Override
            protected void configure() {
                // Skip customerName auto-mapping to avoid conflicts with firstName/lastName
                skip(destination.getCustomerName());
                // Skip customerId as it's manually set from customer.id
                skip(destination.getCustomerId());
            }
        });
        
        // Configure ReservationRating to ReservationRatingDto mapping
        modelMapper.addMappings(new PropertyMap<ReservationRating, ReservationRatingDto>() {
            @Override
            protected void configure() {
                // Skip customerName auto-mapping to avoid conflicts with firstName/lastName
                skip(destination.getCustomerName());
                // Skip car-related fields that are manually set from rental
                skip(destination.getCarBrandName());
                skip(destination.getCarModelName());
                skip(destination.getCarPlate());
            }
        });
        
        // Configure Payment to PaymentDto mapping
        modelMapper.addMappings(new PropertyMap<Payment, PaymentDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set from relationships
                skip(destination.getCustomerId());
                skip(destination.getRentalId());
                skip(destination.getCustomerName());
                skip(destination.getCustomerEmail());
                skip(destination.getCarPlate());
                skip(destination.getCarBrandName());
                skip(destination.getCarModelName());
                skip(destination.getMethodDisplayName());
                skip(destination.getStatusDisplayName());
                skip(destination.getIsOverdue());
                skip(destination.getDaysUntilDue());
            }
        });
        
        // Configure Invoice to InvoiceDto mapping
        modelMapper.addMappings(new PropertyMap<Invoice, InvoiceDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set from relationships
                skip(destination.getCustomerId());
                skip(destination.getRentalId());
                skip(destination.getCustomerName());
                skip(destination.getCustomerEmail());
                skip(destination.getCustomerAddress());
                skip(destination.getCarPlate());
                skip(destination.getCarBrandName());
                skip(destination.getCarModelName());
                skip(destination.getRentalStartDate());
                skip(destination.getRentalEndDate());
                skip(destination.getRentalDays());
                skip(destination.getStatusDisplayName());
                skip(destination.getIsOverdue());
                skip(destination.getDaysUntilDue());
                skip(destination.getDaysOverdue());
            }
        });
        
        // Configure GeneralLedger to GeneralLedgerDto mapping
        modelMapper.addMappings(new PropertyMap<GeneralLedger, GeneralLedgerDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set
                skip(destination.getTransactionTypeDisplayName());
                skip(destination.getAccountTypeDisplayName());
                skip(destination.getBalance());
                skip(destination.getRelatedEntityInfo());
            }
        });
        
        // Configure TaxCalculation to TaxCalculationDto mapping
        modelMapper.addMappings(new PropertyMap<TaxCalculation, TaxCalculationDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set
                skip(destination.getInvoiceId());
                skip(destination.getPaymentId());
                skip(destination.getTaxTypeDisplayName());
                skip(destination.getTaxPercentage());
                skip(destination.getRelatedEntityInfo());
            }
        });
        
        // Configure AuditLog to AuditLogDto mapping
        modelMapper.addMappings(new PropertyMap<AuditLog, AuditLogDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set
                skip(destination.getActionTypeDisplayName());
            }
        });
        
        // Configure Contract to ContractDto mapping
        modelMapper.addMappings(new PropertyMap<Contract, ContractDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set from relationships
                skip(destination.getCustomerName());
                skip(destination.getRentalInfo());
                skip(destination.getTemplateName());
            }
        });
        
        // Configure VehicleConditionCheck to VehicleConditionCheckDto mapping
        modelMapper.addMappings(new PropertyMap<VehicleConditionCheck, VehicleConditionCheckDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set from relationships
                skip(destination.getRentalInfo());
                skip(destination.getCarPlate());
            }
        });
        
        // Configure RentalDocument to RentalDocumentDto mapping
        modelMapper.addMappings(new PropertyMap<RentalDocument, RentalDocumentDto>() {
            @Override
            protected void configure() {
                // Skip fields that are manually set from relationships
                skip(destination.getRentalInfo());
            }
        });
        
        return modelMapper;
    }
}
