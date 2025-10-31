package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.ReservationRating;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.dtos.ReservationRatingDto;
import hakan.rentacar.repostories.*;
import hakan.rentacar.service.ReservationRatingService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationRatingServiceImpl implements ReservationRatingService {

    @Autowired
    private ReservationRatingRepository reservationRatingRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ReservationRatingDto> getByCustomerId(Long customerId) {
        List<ReservationRating> ratings = reservationRatingRepository.findByCustomerIdOrderByCreatedDateDesc(customerId);
        return ratings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationRatingDto> getPublicCarRatings() {
        List<ReservationRating> ratings = reservationRatingRepository.findPublicCarRatings();
        return ratings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationRatingDto> getPublicCarRatingsByCarId(Long carId) {
        List<ReservationRating> ratings = reservationRatingRepository.findPublicCarRatingsByCarId(carId);
        return ratings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationRatingDto create(ReservationRatingDto ratingDto) {
        ReservationRating rating = mapToEntity(ratingDto);
        ReservationRating savedRating = reservationRatingRepository.save(rating);
        return mapToDto(savedRating);
    }

    @Override
    @Transactional
    public ReservationRatingDto update(ReservationRatingDto ratingDto) {
        ReservationRating existingRating = reservationRatingRepository.findById(ratingDto.getId()).orElseThrow();
        
        existingRating.setRating(ratingDto.getRating());
        existingRating.setComment(ratingDto.getComment());
        existingRating.setCarRating(ratingDto.getCarRating());
        existingRating.setIsPublic(ratingDto.getIsPublic());

        ReservationRating savedRating = reservationRatingRepository.save(existingRating);
        return mapToDto(savedRating);
    }

    @Override
    public void delete(Long ratingId) {
        reservationRatingRepository.deleteById(ratingId);
    }

    @Override
    public Double getAverageCarRating(Long carId) {
        Double average = reservationRatingRepository.getAverageCarRating(carId);
        return average != null ? average : 0.0;
    }

    @Override
    public Long getCarRatingCount(Long carId) {
        return reservationRatingRepository.getCarRatingCount(carId);
    }

    @Override
    public boolean hasCustomerRatedReservation(Long customerId, Long rentalId) {
        return reservationRatingRepository.findByRentalId(rentalId).isPresent();
    }

    private ReservationRatingDto mapToDto(ReservationRating rating) {
        ReservationRatingDto dto = modelMapper.map(rating, ReservationRatingDto.class);
        
        if (rating.getCustomer() != null) {
            dto.setCustomerName(rating.getCustomer().getFirstName() + " " + rating.getCustomer().getLastName());
        }
        
        // Set rental information for display
        if (rating.getRental() != null) {
            if (rating.getRental().getCar() != null) {
                dto.setCarPlate(rating.getRental().getCar().getPlate());
                if (rating.getRental().getCar().getModel() != null) {
                    dto.setCarModelName(rating.getRental().getCar().getModel().getName());
                    if (rating.getRental().getCar().getModel().getBrand() != null) {
                        dto.setCarBrandName(rating.getRental().getCar().getModel().getBrand().getName());
                    }
                }
            }
        }
        
        return dto;
    }

    private ReservationRating mapToEntity(ReservationRatingDto dto) {
        ReservationRating rating = modelMapper.map(dto, ReservationRating.class);
        
        // Set rental relationship
        if (dto.getRentalId() != null) {
            Rental rental = rentalRepository.findById(dto.getRentalId()).orElseThrow();
            rating.setRental(rental);
        }
        
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId()).orElseThrow();
            rating.setCustomer(customer);
        }
        
        return rating;
    }
}

