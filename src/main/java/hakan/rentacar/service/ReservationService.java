package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.ReservationDto;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    List<ReservationDto> getAll();
    ReservationDto getById(Long id);
    List<ReservationDto> getByCustomerId(Long customerId);
    List<ReservationDto> getByCarId(Long carId);
    ReservationDto create(ReservationDto reservationDto);
    ReservationDto update(ReservationDto reservationDto);
    void delete(Long id);
    ReservationDto confirmReservation(Long reservationId);
    ReservationDto cancelReservation(Long reservationId);
    List<ReservationDto> getPendingReservations();
    List<ReservationDto> getAvailableCars(LocalDate startDate, LocalDate endDate);
    boolean isCarAvailable(Long carId, LocalDate startDate, LocalDate endDate);
}
