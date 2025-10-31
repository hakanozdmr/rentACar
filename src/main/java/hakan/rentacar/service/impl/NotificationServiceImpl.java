package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.*;
import hakan.rentacar.entities.dtos.NotificationDto;
import hakan.rentacar.repostories.*;
import hakan.rentacar.service.EmailService;
import hakan.rentacar.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<NotificationDto> getByCustomerId(Long customerId) {
        List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedDateDesc(customerId);
        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getUnreadByCustomerId(Long customerId) {
        List<Notification> notifications = notificationRepository.findUnreadByCustomerId(customerId);
        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationDto create(NotificationDto notificationDto) {
        System.out.println("=== NOTIFICATION SERVICE CREATE CALLED ===");
        System.out.println("Customer ID: " + notificationDto.getCustomerId());
        System.out.println("Title: " + notificationDto.getTitle());
        System.out.println("Message: " + notificationDto.getMessage());
        System.out.println("Channel: " + notificationDto.getChannel());
        System.out.println("Type: " + notificationDto.getType());
        
        Notification notification = mapToEntity(notificationDto);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        
        System.out.println("Mapped to entity - Customer: " + (notification.getCustomer() != null ? notification.getCustomer().getId() : "NULL"));

        // Try to send immediately based on channel
        try {
            System.out.println("Processing channel: " + notification.getChannel());
            switch (notification.getChannel()) {
                case EMAIL:
                    System.out.println("Sending EMAIL notification");
                    emailService.sendCustomEmail(
                        notification.getCustomer().getEmail(),
                        notification.getTitle(),
                        notification.getMessage()
                    );
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    System.out.println("Email notification sent successfully");
                    break;
                case SMS:
                    System.out.println("Sending SMS notification");
                    // SMS implementation would go here
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    break;
                case IN_APP:
                    System.out.println("Creating IN_APP notification");
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    System.out.println("IN_APP notification status set to SENT");
                    break;
                default:
                    System.out.println("Unknown channel, setting to PENDING");
                    notification.setStatus(Notification.NotificationStatus.PENDING);
            }
        } catch (Exception e) {
            System.err.println("Error processing notification: " + e.getMessage());
            e.printStackTrace();
            notification.setStatus(Notification.NotificationStatus.FAILED);
        }

        System.out.println("Saving notification to database...");
        Notification savedNotification = notificationRepository.save(notification);
        System.out.println("Notification saved with ID: " + savedNotification.getId());
        
        try {
            NotificationDto result = mapToDto(savedNotification);
            System.out.println("Notification created successfully - returning DTO");
            return result;
        } catch (Exception e) {
            System.err.println("Error mapping notification to DTO: " + e.getMessage());
            e.printStackTrace();
            // Return a minimal DTO to avoid breaking the transaction
            NotificationDto fallbackDto = new NotificationDto();
            fallbackDto.setId(savedNotification.getId());
            fallbackDto.setTitle(savedNotification.getTitle());
            fallbackDto.setMessage(savedNotification.getMessage());
            return fallbackDto;
        }
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setReadAt(LocalDateTime.now());
        notification.setStatus(Notification.NotificationStatus.READ);

        Notification savedNotification = notificationRepository.save(notification);
        return mapToDto(savedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long customerId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByCustomerId(customerId);
        LocalDateTime now = LocalDateTime.now();
        
        for (Notification notification : unreadNotifications) {
            notification.setReadAt(now);
            notification.setStatus(Notification.NotificationStatus.READ);
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    public Long getUnreadCount(Long customerId) {
        return notificationRepository.countUnreadByCustomerId(customerId);
    }

    @Override
    public void sendReservationReminder(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        Customer customer = reservation.getCustomer();

        // Send the actual email using the email service
        try {
            emailService.sendReservationReminder(customer, reservation);
        } catch (Exception e) {
            System.err.println("Error sending reservation reminder email: " + e.getMessage());
        }

        // Also create in-app notification
        NotificationDto notificationDto = NotificationDto.builder()
            .customerId(customer.getId())
            .title("Rezervasyon Hatırlatması")
            .message(String.format("Yarın rezervasyonunuz var. Araç: %s", reservation.getCar().getPlate()))
            .type(Notification.NotificationType.RESERVATION_REMINDER)
            .channel(Notification.NotificationChannel.IN_APP)
            .relatedEntityType("RESERVATION")
            .relatedEntityId(reservationId)
            .build();

        create(notificationDto);
    }

    @Override
    public void sendCarDeliveryNotification(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        Customer customer = rental.getCustomer();

        // Send the actual email using the email service
        try {
            emailService.sendCarDeliveryNotification(customer, rental);
        } catch (Exception e) {
            System.err.println("Error sending car delivery email: " + e.getMessage());
        }

        // Also create in-app notification
        NotificationDto notificationDto = NotificationDto.builder()
            .customerId(customer.getId())
            .title("Araç Teslim Edildi")
            .message(String.format("Aracınız teslim edilmiştir. Plaka: %s", rental.getCar().getPlate()))
            .type(Notification.NotificationType.CAR_DELIVERY)
            .channel(Notification.NotificationChannel.IN_APP)
            .relatedEntityType("RENTAL")
            .relatedEntityId(rentalId)
            .build();

        create(notificationDto);
    }

    @Override
    public void sendCarPickupNotification(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        Customer customer = rental.getCustomer();

        // Send the actual email using the email service
        try {
            emailService.sendCarPickupNotification(customer, rental);
        } catch (Exception e) {
            System.err.println("Error sending car pickup email: " + e.getMessage());
        }

        // Also create in-app notification
        NotificationDto notificationDto = NotificationDto.builder()
            .customerId(customer.getId())
            .title("Araç Teslim Alma Hatırlatması")
            .message(String.format("Aracınızı teslim alma zamanı yaklaştı. Plaka: %s", rental.getCar().getPlate()))
            .type(Notification.NotificationType.CAR_PICKUP)
            .channel(Notification.NotificationChannel.IN_APP)
            .relatedEntityType("RENTAL")
            .relatedEntityId(rentalId)
            .build();

        create(notificationDto);
    }

    @Override
    public void sendPaymentReminder(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        Customer customer = rental.getCustomer();

        // Send the actual email using the email service
        try {
            emailService.sendPaymentReminder(customer, rental);
        } catch (Exception e) {
            System.err.println("Error sending payment reminder email: " + e.getMessage());
        }

        // Also create in-app notification
        NotificationDto notificationDto = NotificationDto.builder()
            .customerId(customer.getId())
            .title("Ödeme Hatırlatması")
            .message(String.format("Ödeme tarihi yaklaştı. Araç: %s", rental.getCar().getPlate()))
            .type(Notification.NotificationType.PAYMENT_DUE)
            .channel(Notification.NotificationChannel.IN_APP)
            .relatedEntityType("RENTAL")
            .relatedEntityId(rentalId)
            .build();

        create(notificationDto);
    }

    @Override
    public void sendRatingRequest(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElseThrow();
        Customer customer = rental.getCustomer();

        // Send the actual email using the email service
        try {
            emailService.sendRatingRequest(customer, rental);
        } catch (Exception e) {
            System.err.println("Error sending rating request email: " + e.getMessage());
        }

        // Also create in-app notification
        NotificationDto notificationDto = NotificationDto.builder()
            .customerId(customer.getId())
            .title("Kiralama Değerlendirmesi")
            .message(String.format("Kiralama deneyiminizi değerlendirmek ister misiniz? Araç: %s", rental.getCar().getPlate()))
            .type(Notification.NotificationType.RESERVATION_REMINDER)
            .channel(Notification.NotificationChannel.IN_APP)
            .relatedEntityType("RENTAL")
            .relatedEntityId(rentalId)
            .build();

        create(notificationDto);
    }

    @Override
    @Transactional
    public void processPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications(Notification.NotificationStatus.PENDING);
        
        for (Notification notification : pendingNotifications) {
            try {
                switch (notification.getChannel()) {
                    case EMAIL:
                        emailService.sendCustomEmail(
                            notification.getCustomer().getEmail(),
                            notification.getTitle(),
                            notification.getMessage()
                        );
                        notification.setStatus(Notification.NotificationStatus.SENT);
                        notification.setSentAt(LocalDateTime.now());
                        break;
                    case SMS:
                        // SMS implementation
                        notification.setStatus(Notification.NotificationStatus.SENT);
                        notification.setSentAt(LocalDateTime.now());
                        break;
                }
            } catch (Exception e) {
                notification.setStatus(Notification.NotificationStatus.FAILED);
            }
        }
        
        notificationRepository.saveAll(pendingNotifications);
    }

    private NotificationDto mapToDto(Notification notification) {
        try {
            NotificationDto dto = modelMapper.map(notification, NotificationDto.class);
            
            if (notification.getCustomer() != null) {
                dto.setCustomerId(notification.getCustomer().getId());
                dto.setCustomerName(notification.getCustomer().getFirstName() + " " + notification.getCustomer().getLastName());
            }
            
            // Ensure createdAt is properly set if null
            if (dto.getCreatedAt() == null && notification.getCreatedDate() != null) {
                try {
                    dto.setCreatedAt(notification.getCreatedDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime());
                } catch (Exception e) {
                    System.err.println("Error converting createdDate to LocalDateTime in ModelMapper path: " + e.getMessage());
                    dto.setCreatedAt(java.time.LocalDateTime.now());
                }
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("ModelMapper error, using manual mapping: " + e.getMessage());
            return mapToDtoManually(notification);
        }
    }
    
    private NotificationDto mapToDtoManually(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setStatus(notification.getStatus());
        dto.setChannel(notification.getChannel());
        dto.setSentAt(notification.getSentAt());
        dto.setReadAt(notification.getReadAt());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        // BaseEntity uses Date, convert to LocalDateTime if needed
        try {
            if (notification.getCreatedDate() != null) {
                dto.setCreatedAt(notification.getCreatedDate().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
            }
        } catch (Exception e) {
            System.err.println("Error converting createdDate to LocalDateTime: " + e.getMessage());
            // Set current time as fallback
            dto.setCreatedAt(java.time.LocalDateTime.now());
        }
        
        if (notification.getCustomer() != null) {
            dto.setCustomerId(notification.getCustomer().getId());
            dto.setCustomerName(notification.getCustomer().getFirstName() + " " + notification.getCustomer().getLastName());
        }
        
        return dto;
    }

    private Notification mapToEntity(NotificationDto dto) {
        try {
            Notification notification = modelMapper.map(dto, Notification.class);
            
            if (dto.getCustomerId() != null) {
                Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow();
                notification.setCustomer(customer);
            }
            
            return notification;
        } catch (Exception e) {
            System.err.println("ModelMapper error in mapToEntity, using manual mapping: " + e.getMessage());
            return mapToEntityManually(dto);
        }
    }
    
    private Notification mapToEntityManually(NotificationDto dto) {
        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setStatus(dto.getStatus());
        notification.setChannel(dto.getChannel());
        notification.setSentAt(dto.getSentAt());
        notification.setReadAt(dto.getReadAt());
        notification.setRelatedEntityType(dto.getRelatedEntityType());
        notification.setRelatedEntityId(dto.getRelatedEntityId());
        // Don't set createdAt as it's managed by BaseEntity
        
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow();
            notification.setCustomer(customer);
        }
        
        return notification;
    }
}
