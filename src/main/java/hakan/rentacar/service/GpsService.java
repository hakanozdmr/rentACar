package hakan.rentacar.service;

import hakan.rentacar.entities.dtos.CarDto;
import hakan.rentacar.entities.dtos.GpsLocationDto;

import java.util.List;

public interface GpsService {
    
    /**
     * Araç konumunu günceller
     */
    CarDto updateCarLocation(Long carId, Double latitude, Double longitude);
    
    /**
     * Araç konumunu getirir
     */
    GpsLocationDto getCarLocation(Long carId);
    
    /**
     * Belirli yarıçap içindeki araçları getirir
     */
    List<CarDto> getCarsInRadius(Double centerLat, Double centerLng, Double radiusKm);
    
    /**
     * Araçların son konumlarını getirir (son X saat içinde)
     */
    List<GpsLocationDto> getRecentCarLocations(int hoursAgo);
    
    /**
     * İki nokta arasındaki mesafeyi hesaplar (km)
     */
    Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2);
    
    /**
     * Araç takip geçmişini getirir
     */
    List<GpsLocationDto> getCarLocationHistory(Long carId, int days);
}



