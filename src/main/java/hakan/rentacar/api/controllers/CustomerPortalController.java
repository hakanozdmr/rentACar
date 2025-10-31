package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.User;
import hakan.rentacar.entities.dtos.*;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.repostories.UserRepository;
import hakan.rentacar.security.JwtUtils;
import hakan.rentacar.service.NotificationService;
import hakan.rentacar.service.ReservationRatingService;
import hakan.rentacar.service.ReservationService;
import hakan.rentacar.service.CustomerService;
import hakan.rentacar.service.CarService;
import hakan.rentacar.service.RentalService;
import hakan.rentacar.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer-portal")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Customer Portal", description = "Müşteri portal işlemleri")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerPortalController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReservationRatingService reservationRatingService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CarService carService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private PaymentService paymentService;

    // Get current customer info
    @GetMapping("/profile")
    public ResponseEntity<CustomerDto> getProfile() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        CustomerDto customerDto = new CustomerDto();
        customerDto.setId(customer.getId());
        customerDto.setFirstName(customer.getFirstName());
        customerDto.setLastName(customer.getLastName());
        customerDto.setEmail(customer.getEmail());
        customerDto.setPhone(customer.getPhone());
        customerDto.setCity(customer.getCity());
        customerDto.setStreet(customer.getStreet());
        customerDto.setZipcode(customer.getZipcode());
        customerDto.setDateOfBirth(customer.getDateOfBirth());
        customerDto.setIdNumber(customer.getIdNumber());
        customerDto.setDriverLicenseNumber(customer.getDriverLicenseNumber());

        return ResponseEntity.ok(customerDto);
    }

    // Update customer profile
    @PutMapping("/profile")
    public ResponseEntity<CustomerDto> updateProfile(@Valid @RequestBody CustomerDto customerDto) {
        Customer currentCustomer = getCurrentCustomer();
        if (currentCustomer == null) {
            return ResponseEntity.notFound().build();
        }

        // Only update allowed fields
        customerDto.setId(currentCustomer.getId());
        customerDto.setEmail(currentCustomer.getEmail()); // Email cannot be changed
        customerDto.setDateOfBirth(currentCustomer.getDateOfBirth()); // Date of birth cannot be changed

        CustomerDto updatedCustomerDto = customerService.update(customerDto);
        return ResponseEntity.ok(updatedCustomerDto);
    }

    // Get customer's reservations
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDto>> getMyReservations() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        List<ReservationDto> reservations = reservationService.getByCustomerId(customer.getId());
        return ResponseEntity.ok(reservations);
    }

    // Create new reservation
    @PostMapping("/reservations")
    public ResponseEntity<ReservationDto> createReservation(@RequestBody ReservationDto reservationDto) {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        // Set customer ID before validation
        reservationDto.setCustomerId(customer.getId());
        
        // Validate the DTO manually after setting customerId
        if (reservationDto.getCarId() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        if (reservationDto.getStartDate() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        if (reservationDto.getEndDate() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        
        // Validate date range
        if (reservationDto.getStartDate().isAfter(reservationDto.getEndDate())) {
            return ResponseEntity.badRequest().body(null);
        }
        
        try {
            ReservationDto createdReservation = reservationService.create(reservationDto);
            return ResponseEntity.ok(createdReservation);
        } catch (Exception e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Cancel reservation
    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable Long id) {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership
        ReservationDto reservation = reservationService.getById(id);
        if (!reservation.getCustomerId().equals(customer.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ReservationDto cancelledReservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(cancelledReservation);
    }

    // Get available cars for date range
    @GetMapping("/available-cars")
    public ResponseEntity<List<AvailableCarDto>> getAvailableCars(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        
        // Get all cars regardless of state and filter by actual date availability
        List<CarDto> allCars = carService.getAll();
        
        // Filter cars that are actually available for the date range (date-based check only)
        List<AvailableCarDto> result = allCars.stream()
            .filter(car -> reservationService.isCarAvailable(car.getId(), start, end))
            .map(car -> new AvailableCarDto(
                car.getId(),
                car.getPlate(),
                car.getBrandName(),
                car.getModelName()
            ))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(result);
    }

    // Rate a reservation
    @PostMapping("/ratings")
    public ResponseEntity<ReservationRatingDto> rateReservation(@Valid @RequestBody ReservationRatingDto ratingDto) {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        ratingDto.setCustomerId(customer.getId());
        ReservationRatingDto createdRating = reservationRatingService.create(ratingDto);
        return ResponseEntity.ok(createdRating);
    }

    // Get customer's ratings
    @GetMapping("/ratings")
    public ResponseEntity<List<ReservationRatingDto>> getMyRatings() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        List<ReservationRatingDto> ratings = reservationRatingService.getByCustomerId(customer.getId());
        return ResponseEntity.ok(ratings);
    }

    // Get notifications
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        List<NotificationDto> notifications = notificationService.getByCustomerId(customer.getId());
        return ResponseEntity.ok(notifications);
    }

    // Get unread notifications
    @GetMapping("/notifications/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        List<NotificationDto> notifications = notificationService.getUnreadByCustomerId(customer.getId());
        return ResponseEntity.ok(notifications);
    }

    // Mark notification as read
    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationDto> markNotificationAsRead(@PathVariable Long id) {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    // Mark all notifications as read
    @PostMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        notificationService.markAllAsRead(customer.getId());
        return ResponseEntity.ok().build();
    }

    // Get unread notification count
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        Long count = notificationService.getUnreadCount(customer.getId());
        return ResponseEntity.ok(count);
    }

    // Get public car ratings for a specific car
    @GetMapping("/cars/{carId}/ratings")
    public ResponseEntity<List<ReservationRatingDto>> getCarRatings(@PathVariable Long carId) {
        List<ReservationRatingDto> ratings = reservationRatingService.getPublicCarRatingsByCarId(carId);
        return ResponseEntity.ok(ratings);
    }

    // Get customer's rental history
    @GetMapping("/rentals")
    public ResponseEntity<List<RentalDto>> getMyRentals() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        List<RentalDto> rentals = rentalService.getByCustomerId(customer.getId());
        return ResponseEntity.ok(rentals);
    }

    private Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return null;
        }

        User user = userOpt.get();
        return customerRepository.findByUserId(user.getId()).orElse(null);
    }

    // Payment endpoints for customer portal
    @GetMapping("/payments")
    @Operation(summary = "Müşteri ödemeleri", description = "Mevcut müşterinin ödeme geçmişini getirir")
    public ResponseEntity<List<PaymentDto>> getMyPayments() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<PaymentDto> payments = paymentService.getByCustomerId(customer.getId());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/pending")
    @Operation(summary = "Bekleyen ödemeler", description = "Müşterinin bekleyen ödemelerini getirir")
    public ResponseEntity<List<PaymentDto>> getMyPendingPayments() {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<PaymentDto> allPayments = paymentService.getByCustomerId(customer.getId());
        List<PaymentDto> pendingPayments = allPayments.stream()
                .filter(payment -> payment.getStatus().name().equals("PENDING"))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(pendingPayments);
    }

    @GetMapping("/rental/{rentalId}/payments")
    @Operation(summary = "Kiralama ödemeleri", description = "Belirli kiralama için ödemeleri getirir")
    public ResponseEntity<List<PaymentDto>> getRentalPayments(@PathVariable Long rentalId) {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify that this rental belongs to the current customer
        List<PaymentDto> allPayments = paymentService.getByCustomerId(customer.getId());
        List<PaymentDto> rentalPayments = allPayments.stream()
                .filter(payment -> payment.getRentalId().equals(rentalId))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(rentalPayments);
    }

    @GetMapping("/rental/{rentalId}/balance")
    @Operation(summary = "Kiralama bakiye", description = "Belirli kiralama için kalan bakiye bilgisini getirir")
    public ResponseEntity<PaymentBalanceResponse> getRentalBalance(@PathVariable Long rentalId) {
        Customer customer = getCurrentCustomer();
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify that this rental belongs to the current customer
        List<PaymentDto> customerPayments = paymentService.getByCustomerId(customer.getId());
        boolean rentalOwnedByCustomer = customerPayments.stream()
                .anyMatch(payment -> payment.getRentalId().equals(rentalId));
        
        if (!rentalOwnedByCustomer) {
            return ResponseEntity.notFound().build();
        }
        
        BigDecimal totalPaid = paymentService.getTotalPaidForRental(rentalId);
        BigDecimal remainingBalance = paymentService.getRemainingBalanceForRental(rentalId);
        
        PaymentBalanceResponse response = new PaymentBalanceResponse();
        response.setRentalId(rentalId);
        response.setTotalPaid(totalPaid);
        response.setRemainingBalance(remainingBalance);
        response.setIsFullyPaid(remainingBalance.compareTo(java.math.BigDecimal.ZERO) <= 0);
        
        return ResponseEntity.ok(response);
    }

    // Response class for balance information
    public static class PaymentBalanceResponse {
        private Long rentalId;
        private java.math.BigDecimal totalPaid;
        private java.math.BigDecimal remainingBalance;
        private Boolean isFullyPaid;

        public Long getRentalId() {
            return rentalId;
        }

        public void setRentalId(Long rentalId) {
            this.rentalId = rentalId;
        }

        public java.math.BigDecimal getTotalPaid() {
            return totalPaid;
        }

        public void setTotalPaid(java.math.BigDecimal totalPaid) {
            this.totalPaid = totalPaid;
        }

        public java.math.BigDecimal getRemainingBalance() {
            return remainingBalance;
        }

        public void setRemainingBalance(java.math.BigDecimal remainingBalance) {
            this.remainingBalance = remainingBalance;
        }

        public Boolean getIsFullyPaid() {
            return isFullyPaid;
        }

        public void setIsFullyPaid(Boolean isFullyPaid) {
            this.isFullyPaid = isFullyPaid;
        }
    }
}
