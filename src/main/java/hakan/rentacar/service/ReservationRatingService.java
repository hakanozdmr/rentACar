package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.ReservationRatingDto;

import java.util.List;

public interface ReservationRatingService {
    List<ReservationRatingDto> getByCustomerId(Long customerId);
    List<ReservationRatingDto> getPublicCarRatings();
    List<ReservationRatingDto> getPublicCarRatingsByCarId(Long carId);
    ReservationRatingDto create(ReservationRatingDto ratingDto);
    ReservationRatingDto update(ReservationRatingDto ratingDto);
    void delete(Long ratingId);
    Double getAverageCarRating(Long carId);
    Long getCarRatingCount(Long carId);
    boolean hasCustomerRatedReservation(Long customerId, Long rentalId);
}

