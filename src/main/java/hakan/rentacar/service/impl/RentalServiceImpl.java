package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Car;
import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.dtos.RentalDto;
import hakan.rentacar.repostories.CarRepository;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.repostories.ReservationRatingRepository;
import hakan.rentacar.service.RentalService;
import hakan.rentacar.service.ReservationRatingService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private ReservationRatingRepository reservationRatingRepository;

    @Autowired
    public RentalServiceImpl(RentalRepository rentalRepository, 
                           CarRepository carRepository, 
                           CustomerRepository customerRepository) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<RentalDto> getAll() {
        List<Rental> rentals = rentalRepository.findAll();
        return rentals.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public RentalDto getById(Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        return EntityToDto(rental);
    }

    @Override
    public RentalDto add(RentalDto rentalDto) {
        // Validate dates
        if (rentalDto.getStart().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
        
        if (rentalDto.getEnd().isBefore(rentalDto.getStart())) {
            throw new RuntimeException("End date must be after start date");
        }
        
        // Check if car exists and is available
        Car car = carRepository.findById(rentalDto.getCarId()).orElseThrow(
            () -> new RuntimeException("Car not found")
        );
        
        // Check if car is available for the requested dates (date-based check only)
        if (!isCarAvailable(rentalDto.getCarId(), rentalDto.getStart(), rentalDto.getEnd())) {
            throw new RuntimeException("Car is not available for the requested dates");
        }
        
        // Check if customer exists
        Customer customer = customerRepository.findById(rentalDto.getCustomerId()).orElseThrow(
            () -> new RuntimeException("Customer not found")
        );
        
        Rental rental = DtoToEntity(rentalDto);
        Rental savedRental = rentalRepository.save(rental);
        rentalDto.setId(savedRental.getId());
        return rentalDto;
    }

    @Override
    public RentalDto update(RentalDto rentalDto) {
        Rental existingRental = rentalRepository.findById(rentalDto.getId()).orElseThrow();
        
        // Validate dates
        if (rentalDto.getStart().isBefore(LocalDate.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
        
        if (rentalDto.getEnd().isBefore(rentalDto.getStart())) {
            throw new RuntimeException("End date must be after start date");
        }
        
        // Check if the updated dates conflict with other rentals (excluding current rental)
        List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(
            rentalDto.getCarId(), rentalDto.getStart(), rentalDto.getEnd()
        );
        conflictingRentals.removeIf(r -> r.getId().equals(rentalDto.getId()));
        
        if (!conflictingRentals.isEmpty()) {
            throw new RuntimeException("Car is not available for the requested dates");
        }
        
        Rental rental = DtoToEntity(rentalDto);
        rentalRepository.save(rental);
        return rentalDto;
    }

    @Override
    public RentalDto delete(Long id) {
        Rental rental = rentalRepository.findById(id).orElseThrow();
        RentalDto rentalDto = EntityToDto(rental);
        rentalRepository.deleteById(id);
        return rentalDto;
    }

    @Override
    public List<RentalDto> getByCarId(Long carId) {
        List<Rental> rentals = rentalRepository.findByCarId(carId);
        return rentals.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<RentalDto> getByCustomerId(Long customerId) {
        List<Rental> rentals = rentalRepository.findByCustomerIdOrderByStartDesc(customerId);
        return rentals.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<RentalDto> findByDate(LocalDate date) {
        List<Rental> rentals = rentalRepository.findByDate(date);
        return rentals.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<RentalDto> findActiveRentals(LocalDate date) {
        List<Rental> rentals = rentalRepository.findActiveRentals(date);
        return rentals.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<RentalDto> findRentalsBetweenDates(LocalDate startDate, LocalDate endDate) {
        List<Rental> rentals = rentalRepository.findRentalsBetweenDates(startDate, endDate);
        return rentals.stream().map(this::EntityToDto).collect(Collectors.toList());
    }

    @Override
    public boolean isCarAvailable(Long carId, LocalDate start, LocalDate end) {
        List<Rental> conflictingRentals = rentalRepository.findConflictingRentals(carId, start, end);
        return conflictingRentals.isEmpty();
    }

    @Override
    public RentalDto EntityToDto(Rental rental) {
        RentalDto rentalDto = modelMapper.map(rental, RentalDto.class);
        rentalDto.setCarId(rental.getCar().getId());
        rentalDto.setCustomerId(rental.getCustomer().getId());
        rentalDto.setCarPlate(rental.getCar().getPlate());
        rentalDto.setCarBrandName(rental.getCar().getModel() != null && rental.getCar().getModel().getBrand() != null ? 
            rental.getCar().getModel().getBrand().getName() : null);
        rentalDto.setCarModelName(rental.getCar().getModel() != null ? 
            rental.getCar().getModel().getName() : null);
        rentalDto.setCustomerFirstName(rental.getCustomer().getFirstName());
        rentalDto.setCustomerLastName(rental.getCustomer().getLastName());
        rentalDto.setDailyPrice(rental.getCar().getDailyPrice());
        
        // Map new fields from Reservation
        rentalDto.setStatus(rental.getStatus());
        rentalDto.setTotalAmount(rental.getTotalAmount());
        rentalDto.setSpecialRequests(rental.getSpecialRequests());
        rentalDto.setConfirmedAt(rental.getConfirmedAt());
        rentalDto.setCancelledAt(rental.getCancelledAt());
        
        // Calculate total price (use totalAmount if available, otherwise calculate)
        if (rental.getTotalAmount() != null) {
            rentalDto.setTotalPrice(rental.getTotalAmount().doubleValue());
        } else {
            long days = ChronoUnit.DAYS.between(rental.getStart(), rental.getEnd()) + 1;
            double totalPrice = rental.getCar().getDailyPrice() * days + (rental.getExtraCosts() != null ? rental.getExtraCosts() : 0);
            rentalDto.setTotalPrice(totalPrice);
        }
        
        
        // Check if customer can rate this rental (rental ended and not rated yet)
        LocalDate today = LocalDate.now();
        boolean rentalEnded = rental.getEnd().isBefore(today);
        Boolean hasRated = reservationRatingRepository.hasCustomerRatedRental(rental.getCustomer().getId(), rental.getCar().getId());
        
        rentalDto.setCanRate(rentalEnded && (hasRated == null || !hasRated));
        rentalDto.setIsRated(hasRated != null && hasRated);
        
        return rentalDto;
    }

    @Override
    public Rental DtoToEntity(RentalDto rentalDto) {
        Rental rental = modelMapper.map(rentalDto, Rental.class);
        
        // Set the car and customer
        if (rentalDto.getCarId() != null) {
            Car car = carRepository.findById(rentalDto.getCarId()).orElseThrow();
            rental.setCar(car);
        }
        
        if (rentalDto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(rentalDto.getCustomerId()).orElseThrow();
            rental.setCustomer(customer);
        }
        
        // Set default status if not provided
        if (rental.getStatus() == null) {
            rental.setStatus(Rental.RentalStatus.CONFIRMED);
        }
        
        return rental;
    }
}


