package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Car;
import hakan.rentacar.entities.concretes.GpsLocationHistory;
import hakan.rentacar.entities.dtos.CarDto;
import hakan.rentacar.entities.dtos.GpsLocationDto;
import hakan.rentacar.repostories.CarRepository;
import hakan.rentacar.repostories.GpsLocationHistoryRepository;
import hakan.rentacar.service.CarService;
import hakan.rentacar.service.GpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GpsServiceImpl implements GpsService {

    private final CarRepository carRepository;
    private final GpsLocationHistoryRepository gpsLocationHistoryRepository;
    private final CarService carService;

    @Autowired
    public GpsServiceImpl(CarRepository carRepository, 
                         GpsLocationHistoryRepository gpsLocationHistoryRepository,
                         CarService carService) {
        this.carRepository = carRepository;
        this.gpsLocationHistoryRepository = gpsLocationHistoryRepository;
        this.carService = carService;
    }

    @Override
    @Transactional
    public CarDto updateCarLocation(Long carId, Double latitude, Double longitude) {
        Car car = carRepository.findById(carId).orElseThrow();
        
        // Araç konumunu güncelle
        car.setGpsLatitude(latitude);
        car.setGpsLongitude(longitude);
        car.setLastLocationUpdate(LocalDateTime.now());
        
        Car savedCar = carRepository.save(car);
        
        // Konum geçmişine ekle
        GpsLocationHistory locationHistory = GpsLocationHistory.builder()
            .car(car)
            .latitude(BigDecimal.valueOf(latitude))
            .longitude(BigDecimal.valueOf(longitude))
            .recordedAt(LocalDateTime.now())
            .isOnline(true)
            .source("GPS_DEVICE")
            .build();
        
        gpsLocationHistoryRepository.save(locationHistory);
        
        return carService.EntityToDto(savedCar);
    }

    @Override
    public GpsLocationDto getCarLocation(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow();
        
        return GpsLocationDto.builder()
            .carId(car.getId())
            .plate(car.getPlate())
            .brandName(car.getModel() != null && car.getModel().getBrand() != null ? 
                      car.getModel().getBrand().getName() : "Unknown")
            .modelName(car.getModel() != null ? car.getModel().getName() : "Unknown")
            .latitude(car.getGpsLatitude())
            .longitude(car.getGpsLongitude())
            .lastUpdate(car.getLastLocationUpdate() != null ? 
                       car.getLastLocationUpdate() : 
                       LocalDateTime.now().minusDays(1))
            .build();
    }

    @Override
    public List<CarDto> getCarsInRadius(Double centerLat, Double centerLng, Double radiusKm) {
        LocalDateTime since = LocalDateTime.now().minusHours(24); // Son 24 saat
        
        List<GpsLocationHistory> locations = gpsLocationHistoryRepository.findCarsWithinRadius(
            centerLat, centerLng, radiusKm, since);
        
        return locations.stream()
            .map(location -> {
                Car car = location.getCar();
                return carService.EntityToDto(car);
            })
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    public List<GpsLocationDto> getRecentCarLocations(int hoursAgo) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        
        List<GpsLocationHistory> recentLocations = gpsLocationHistoryRepository.findRecentlyActiveCars(since);
        
        return recentLocations.stream()
            .map(this::mapToGpsLocationDto)
            .collect(Collectors.toList());
    }

    @Override
    public Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                  Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                  Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public List<GpsLocationDto> getCarLocationHistory(Long carId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        List<GpsLocationHistory> history = gpsLocationHistoryRepository
            .findByCarIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                carId, startDate, LocalDateTime.now());
        
        return history.stream()
            .map(this::mapToGpsLocationDto)
            .collect(Collectors.toList());
    }

    private GpsLocationDto mapToGpsLocationDto(GpsLocationHistory history) {
        Car car = history.getCar();
        
        return GpsLocationDto.builder()
            .carId(car.getId())
            .plate(car.getPlate())
            .brandName(car.getModel() != null && car.getModel().getBrand() != null ? 
                      car.getModel().getBrand().getName() : "Unknown")
            .modelName(car.getModel() != null ? car.getModel().getName() : "Unknown")
            .latitude(history.getLatitude() != null ? history.getLatitude().doubleValue() : null)
            .longitude(history.getLongitude() != null ? history.getLongitude().doubleValue() : null)
            .lastUpdate(history.getRecordedAt())
            .address(history.getAddress())
            .speed(history.getSpeed())
            .isOnline(history.getIsOnline())
            .batteryLevel(history.getBatteryLevel())
            .build();
    }
}
