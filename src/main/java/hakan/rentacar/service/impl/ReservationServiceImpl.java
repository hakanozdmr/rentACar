package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.*;
import hakan.rentacar.entities.dtos.ReservationDto;
import hakan.rentacar.entities.dtos.NotificationDto;
import hakan.rentacar.repostories.*;
import hakan.rentacar.service.ReservationService;
import hakan.rentacar.service.NotificationService;
import hakan.rentacar.service.EmailService;
import hakan.rentacar.audit.Auditable;
import hakan.rentacar.entities.concretes.AuditLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    @Lazy
    private ReservationServiceImpl self;

    @Override
    public List<ReservationDto> getAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationDto getById(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        return mapToDto(reservation);
    }

    @Override
    public List<ReservationDto> getByCustomerId(Long customerId) {
        List<Reservation> reservations = reservationRepository.findByCustomerIdOrderByCreatedDateDesc(customerId);
        return reservations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getByCarId(Long carId) {
        List<Reservation> reservations = reservationRepository.findByCarIdOrderByCreatedDateDesc(carId);
        return reservations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDto> getPendingReservations() {
        List<Reservation> reservations = reservationRepository.findByStatusOrderByCreatedDateDesc(Reservation.ReservationStatus.PENDING);
        return reservations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(entity = "Reservation", action = AuditLog.ActionType.CREATE, description = "Create new reservation")
    public ReservationDto create(ReservationDto reservationDto) {
        // Check if car is available
        if (!isCarAvailable(reservationDto.getCarId(), reservationDto.getStartDate(), reservationDto.getEndDate())) {
            throw new RuntimeException("Car is not available for the selected dates");
        }

        Reservation reservation = mapToEntity(reservationDto);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);

        // Calculate total amount
        Car car = carRepository.findById(reservationDto.getCarId()).orElseThrow();
        long daysCount = ChronoUnit.DAYS.between(reservationDto.getStartDate(), reservationDto.getEndDate());
        BigDecimal dailyPrice = BigDecimal.valueOf(car.getDailyPrice());
        reservation.setTotalAmount(dailyPrice.multiply(BigDecimal.valueOf(daysCount + 1)));

        Reservation savedReservation = reservationRepository.save(reservation);
        System.out.println("=== CREATED RESERVATION ID: " + savedReservation.getId() + " ===");
        
        // Send notification and email after successful creation
        System.out.println("Sending initial notification for new reservation...");
        try {
            // Create in-app notification directly
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setCustomerId(reservationDto.getCustomerId());
            notificationDto.setTitle("Yeni Rezervasyon");
            notificationDto.setMessage(String.format("Araç kiralama talebiniz alındı. Rezervasyon No: %d", savedReservation.getId()));
            notificationDto.setType(Notification.NotificationType.RESERVATION_REMINDER);
            notificationDto.setStatus(Notification.NotificationStatus.SENT);
            notificationDto.setChannel(Notification.NotificationChannel.IN_APP);
            
            System.out.println("Creating notification for customer ID: " + reservationDto.getCustomerId());
            notificationService.create(notificationDto);
            System.out.println("Notification created successfully");

            // Send email notification
            Customer customer = customerRepository.findById(reservationDto.getCustomerId()).orElse(null);
            if (customer != null) {
                System.out.println("Sending email to: " + customer.getEmail());
                emailService.sendReservationNotification(customer, savedReservation);
            }
        } catch (Exception e) {
            // Log error but don't fail the main transaction
            System.err.println("Error sending notifications for reservation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mapToDto(savedReservation);
    }

    @Override
    @Transactional
    @Auditable(entity = "Reservation", action = AuditLog.ActionType.UPDATE, description = "Update reservation")
    public ReservationDto update(ReservationDto reservationDto) {
        Reservation existingReservation = reservationRepository.findById(reservationDto.getId()).orElseThrow();
        
        existingReservation.setStartDate(reservationDto.getStartDate());
        existingReservation.setEndDate(reservationDto.getEndDate());
        existingReservation.setSpecialRequests(reservationDto.getSpecialRequests());
        existingReservation.setNote(reservationDto.getNote());

        Reservation savedReservation = reservationRepository.save(existingReservation);
        return mapToDto(savedReservation);
    }

    @Override
    @Auditable(entity = "Reservation", action = AuditLog.ActionType.DELETE, description = "Delete reservation")
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional
    @Auditable(entity = "Reservation", action = AuditLog.ActionType.UPDATE, description = "Confirm reservation")
    public ReservationDto confirmReservation(Long reservationId) {
        System.out.println("=== CONFIRMING RESERVATION ID: " + reservationId + " ===");
        
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));
        
        System.out.println("Found reservation: " + reservation.getId() + ", Status: " + reservation.getStatus());
        
        reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(LocalDateTime.now());
        
        Reservation savedReservation = reservationRepository.save(reservation);
        System.out.println("Reservation status updated to: " + savedReservation.getStatus());
        
        // Create Rental record when reservation is confirmed
        System.out.println("Creating Rental record...");
        System.out.println("Car ID: " + reservation.getCar().getId());
        System.out.println("Customer ID: " + reservation.getCustomer().getId());
        System.out.println("Start Date: " + reservation.getStartDate());
        System.out.println("End Date: " + reservation.getEndDate());
        
        Rental rental = Rental.builder()
                .start(reservation.getStartDate())
                .end(reservation.getEndDate())
                .car(reservation.getCar())
                .customer(reservation.getCustomer())
                .note("Rezervasyon onayından oluşturuldu - Rezervasyon #" + reservation.getId())
                .extraCosts(0)
                // Copy additional fields from Reservation
                .status(Rental.RentalStatus.CONFIRMED)
                .totalAmount(reservation.getTotalAmount())
                .specialRequests(reservation.getSpecialRequests())
                .confirmedAt(reservation.getConfirmedAt())
                .build();
        
        Rental savedRental = rentalRepository.save(rental);
        System.out.println("Rental created with ID: " + savedRental.getId());
        
        // Update car state to Rented (2)
        Car car = reservation.getCar();
        System.out.println("Car current state: " + car.getState());
        car.setState(2); // 2 = Rented
        Car savedCar = carRepository.save(car);
        System.out.println("Car state updated to: " + savedCar.getState());
        
        // Send confirmation notification and email
        System.out.println("Sending notifications...");
        try {
            // Create in-app notification directly
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setCustomerId(reservation.getCustomer().getId());
            notificationDto.setTitle("Rezervasyon Onaylandı");
            notificationDto.setMessage(String.format("Rezervasyonunuz onaylandı! Rezervasyon No: %d", savedReservation.getId()));
            notificationDto.setType(Notification.NotificationType.RESERVATION_CONFIRMED);
            notificationDto.setStatus(Notification.NotificationStatus.SENT);
            notificationDto.setChannel(Notification.NotificationChannel.IN_APP);
            
            System.out.println("Creating confirmation notification for customer ID: " + reservation.getCustomer().getId());
            notificationService.create(notificationDto);
            System.out.println("Notification created successfully");

            // Send email confirmation
            Customer customer = reservation.getCustomer();
            if (customer != null) {
                System.out.println("Sending confirmation email to: " + customer.getEmail());
                emailService.sendReservationConfirmation(customer, savedReservation);
            }
        } catch (Exception e) {
            System.err.println("Error sending confirmation notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== RESERVATION CONFIRMATION COMPLETED ===");
        return mapToDto(savedReservation);
    }

    @Override
    @Transactional
    public ReservationDto cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();
        
        // Check if reservation was previously confirmed before cancelling
        boolean wasConfirmed = reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED || reservation.getConfirmedAt() != null;
        
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        
        Reservation savedReservation = reservationRepository.save(reservation);
        
        // If reservation was previously confirmed, update car state back to available
        if (wasConfirmed) {
            // Find and delete related rental records for this reservation
            List<Rental> relatedRentals = rentalRepository.findByCarId(reservation.getCar().getId())
                .stream()
                .filter(rental -> rental.getCustomer().getId().equals(reservation.getCustomer().getId()) &&
                        rental.getStart().equals(reservation.getStartDate()) &&
                        rental.getEnd().equals(reservation.getEndDate()))
                .collect(java.util.stream.Collectors.toList());
            
            if (!relatedRentals.isEmpty()) {
                // Delete the rental record since reservation is cancelled
                rentalRepository.deleteAll(relatedRentals);
            }
            
            // Update car state back to Available (1)
            Car car = reservation.getCar();
            car.setState(1); // 1 = Available
            carRepository.save(car);
        }
        
        // Send cancellation notification and email
        try {
            // Create in-app notification directly
            NotificationDto notificationDto = new NotificationDto();
            notificationDto.setCustomerId(reservation.getCustomer().getId());
            notificationDto.setTitle("Rezervasyon İptal Edildi");
            notificationDto.setMessage(String.format("Rezervasyonunuz iptal edildi. Rezervasyon No: %d", savedReservation.getId()));
            notificationDto.setType(Notification.NotificationType.RESERVATION_CANCELLED);
            notificationDto.setStatus(Notification.NotificationStatus.SENT);
            notificationDto.setChannel(Notification.NotificationChannel.IN_APP);
            
            System.out.println("Creating cancellation notification for customer ID: " + reservation.getCustomer().getId());
            notificationService.create(notificationDto);

            // Send email cancellation notice
            Customer customer = reservation.getCustomer();
            if (customer != null) {
                emailService.sendReservationCancellation(customer, savedReservation);
            }
        } catch (Exception e) {
            System.err.println("Error sending cancellation notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mapToDto(savedReservation);
    }

    @Override
    public List<ReservationDto> getAvailableCars(LocalDate startDate, LocalDate endDate) {
        // This would require a more complex query to find cars not booked in the date range
        // For now, return all cars (simplified implementation)
        List<Car> allCars = carRepository.findAll();
        return allCars.stream()
                .filter(car -> isCarAvailable(car.getId(), startDate, endDate))
                .map(car -> {
                    ReservationDto dto = new ReservationDto();
                    dto.setCarId(car.getId());
                    dto.setCarPlate(car.getPlate());
                    dto.setStartDate(startDate);
                    dto.setEndDate(endDate);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate) {
        // Check reservations
        List<Reservation> conflictingReservations = reservationRepository.findByCarIdAndDateRange(carId, startDate, endDate);
        if (!conflictingReservations.isEmpty()) {
            return false;
        }

        // Check existing rentals
        List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(carId, startDate, endDate);
        return conflictingRentals.isEmpty();
    }

    private ReservationDto mapToDto(Reservation reservation) {
        try {
            ReservationDto dto = modelMapper.map(reservation, ReservationDto.class);
            
            // Set customer name manually
            if (reservation.getCustomer() != null) {
                dto.setCustomerName(reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName());
            }
            
            // Set car information manually
            if (reservation.getCar() != null) {
                dto.setCarPlate(reservation.getCar().getPlate());
                if (reservation.getCar().getModel() != null) {
                    dto.setCarBrandName(reservation.getCar().getModel().getBrand().getName());
                    dto.setCarModelName(reservation.getCar().getModel().getName());
                }
            }
            
            // Calculate days count
            if (reservation.getStartDate() != null && reservation.getEndDate() != null) {
                dto.setDaysCount(ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()));
            }
            
            return dto;
        } catch (Exception e) {
            // Fallback to manual mapping if ModelMapper fails
            return mapToDtoManually(reservation);
        }
    }
    
    private ReservationDto mapToDtoManually(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setId(reservation.getId());
        dto.setCustomerId(reservation.getCustomer() != null ? reservation.getCustomer().getId() : null);
        dto.setCarId(reservation.getCar() != null ? reservation.getCar().getId() : null);
        dto.setStartDate(reservation.getStartDate());
        dto.setEndDate(reservation.getEndDate());
        dto.setStatus(reservation.getStatus());
        dto.setTotalAmount(reservation.getTotalAmount());
        dto.setSpecialRequests(reservation.getSpecialRequests());
        dto.setNote(reservation.getNote());
        dto.setConfirmedAt(reservation.getConfirmedAt());
        dto.setCancelledAt(reservation.getCancelledAt());
        
        // Set customer name
        if (reservation.getCustomer() != null) {
            dto.setCustomerName(reservation.getCustomer().getFirstName() + " " + reservation.getCustomer().getLastName());
        }
        
        // Set car information
        if (reservation.getCar() != null) {
            dto.setCarPlate(reservation.getCar().getPlate());
            if (reservation.getCar().getModel() != null) {
                dto.setCarBrandName(reservation.getCar().getModel().getBrand().getName());
                dto.setCarModelName(reservation.getCar().getModel().getName());
            }
        }
        
        // Calculate days count
        if (reservation.getStartDate() != null && reservation.getEndDate() != null) {
            dto.setDaysCount(ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()));
        }
        
        return dto;
    }

    private Reservation mapToEntity(ReservationDto dto) {
        Reservation reservation = modelMapper.map(dto, Reservation.class);
        
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow();
            reservation.setCustomer(customer);
        }
        
        if (dto.getCarId() != null) {
            Car car = carRepository.findById(dto.getCarId()).orElseThrow();
            reservation.setCar(car);
        }
        
        return reservation;
    }
}
