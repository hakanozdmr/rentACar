package hakan.rentacar.repostories;

import hakan.rentacar.entities.concretes.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    List<Rental> findByCarId(Long carId);
    
    List<Rental> findByCustomerId(Long customerId);
    
    List<Rental> findByCustomerIdOrderByStartDesc(Long customerId);
    
    @Query("SELECT r FROM Rental r WHERE r.car.id = :carId AND " +
           "((r.start <= :start AND r.end >= :start) OR " +
           "(r.start <= :end AND r.end >= :end) OR " +
           "(r.start >= :start AND r.end <= :end))")
    List<Rental> findConflictingRentals(@Param("carId") Long carId, 
                                       @Param("start") LocalDate start, 
                                       @Param("end") LocalDate end);
    
    @Query("SELECT r FROM Rental r WHERE r.start = :date OR r.end = :date")
    List<Rental> findByDate(@Param("date") LocalDate date);
    
    @Query("SELECT r FROM Rental r WHERE r.start <= :date AND r.end >= :date")
    List<Rental> findActiveRentals(@Param("date") LocalDate date);
    
    @Query("SELECT r FROM Rental r WHERE r.start BETWEEN :startDate AND :endDate OR r.end BETWEEN :startDate AND :endDate")
    List<Rental> findRentalsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Analytics Queries
    @Query(value = """
        SELECT 
            CAST(DATE_TRUNC('month', r.start) AS DATE) as date,
            'MONTHLY' as period,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as totalRevenue,
            COUNT(r.id) as totalRentals,
            b.name as carBrand,
            m.name as carModel
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        JOIN models m ON c.model_id = m.id
        JOIN brands b ON m.brand_id = b.id
        WHERE EXTRACT(year FROM r.start) = :year
        GROUP BY DATE_TRUNC('month', r.start), b.name, m.name
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getMonthlyRevenueReport(@Param("year") int year);
    
    @Query(value = """
        SELECT 
            CAST(DATE_TRUNC('year', r.start) AS DATE) as date,
            'YEARLY' as period,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as totalRevenue,
            COUNT(r.id) as totalRentals,
            b.name as carBrand,
            m.name as carModel
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        JOIN models m ON c.model_id = m.id
        JOIN brands b ON m.brand_id = b.id
        WHERE EXTRACT(year FROM r.start) BETWEEN :startYear AND :endYear
        GROUP BY DATE_TRUNC('year', r.start), b.name, m.name
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getYearlyRevenueReport(@Param("startYear") int startYear, @Param("endYear") int endYear);
    
    @Query(value = """
        SELECT 
            r.start as date,
            'DAILY' as period,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as totalRevenue,
            COUNT(r.id) as totalRentals,
            b.name as carBrand,
            m.name as carModel
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        JOIN models m ON c.model_id = m.id
        JOIN brands b ON m.brand_id = b.id
        WHERE r.start BETWEEN :startDate AND :endDate
        GROUP BY r.start, b.name, m.name
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getDailyRevenueReport(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query(value = """
        SELECT 
            r.car_id as carId,
            c.plate as plate,
            b.name as brandName,
            m.name as modelName,
            COUNT(r.id) as totalRentals,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as totalRevenue,
            SUM((r."end" - r.start) + 1) as totalRentalDays,
            0 as utilizationRate,
            AVG(c.daily_price) as averageDailyRevenue
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        JOIN models m ON c.model_id = m.id
        JOIN brands b ON m.brand_id = b.id
        GROUP BY r.car_id, c.plate, b.name, m.name
        ORDER BY COUNT(r.id) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getMostRentedCars(@Param("limit") int limit);
    
    @Query(value = """
        SELECT 
            r.car_id as carId,
            c.plate as plate,
            b.name as brandName,
            m.name as modelName,
            COUNT(r.id) as totalRentals,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as totalRevenue,
            SUM((r."end" - r.start) + 1) as totalRentalDays,
            0 as utilizationRate,
            AVG(c.daily_price) as averageDailyRevenue
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        JOIN models m ON c.model_id = m.id
        JOIN brands b ON m.brand_id = b.id
        GROUP BY r.car_id, c.plate, b.name, m.name
        ORDER BY totalRevenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> getTopRevenueCars(@Param("limit") int limit);
    
    @Query(value = """
        SELECT 
            r.car_id as carId,
            c.plate as plate,
            b.name as brandName,
            m.name as modelName,
            COUNT(r.id) as totalRentals,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as totalRevenue,
            SUM((r."end" - r.start) + 1) as totalRentalDays,
            CASE 
                WHEN COUNT(r.id) > 10 THEN 85.0
                WHEN COUNT(r.id) > 5 THEN 65.0
                WHEN COUNT(r.id) > 2 THEN 45.0
                ELSE 25.0
            END as utilizationRate,
            AVG(c.daily_price) as averageDailyRevenue
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        JOIN models m ON c.model_id = m.id
        JOIN brands b ON m.brand_id = b.id
        GROUP BY r.car_id, c.plate, b.name, m.name
        ORDER BY utilizationRate DESC
        """, nativeQuery = true)
    List<Object[]> getCarUtilizationStats();
    
    @Query(value = """
        WITH customer_segments AS (
            SELECT 
                cu.id,
                cu.first_name,
                cu.last_name,
                COUNT(r.id) as rental_count,
                SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as total_revenue,
                CASE 
                    WHEN COUNT(r.id) >= 10 AND COALESCE(SUM(c.daily_price * ((r."end" - r.start) + 1)) + SUM(COALESCE(r.extra_costs, 0)), 0) > 10000 THEN 'VIP'
                    WHEN COUNT(r.id) >= 5 THEN 'REGULAR'
                    WHEN COUNT(r.id) >= 2 THEN 'FREQUENT'
                    WHEN COUNT(r.id) >= 1 THEN 'OCCASIONAL'
                    ELSE 'NEW'
                END as segment
            FROM customers cu
            LEFT JOIN rentals r ON cu.id = r.customer_id
            LEFT JOIN cars c ON r.car_id = c.id
            GROUP BY cu.id, cu.first_name, cu.last_name
        )
        SELECT 
            segment as segmentName,
            COUNT(id) as customerCount,
            COALESCE(SUM(total_revenue), 0) as totalRevenue,
            CASE 
                WHEN COUNT(id) > 0 THEN COALESCE(SUM(total_revenue), 0) / COUNT(id)
                ELSE 0
            END as averageRevenuePerCustomer,
            SUM(rental_count) as totalRentals
        FROM customer_segments
        GROUP BY segment
        ORDER BY totalRevenue DESC
        """, nativeQuery = true)
    List<Object[]> getCustomerSegmentation();
    
    @Query(value = """
        SELECT COALESCE(SUM(c.daily_price * ((r."end" - r.start) + 1)) + SUM(COALESCE(r.extra_costs, 0)), 0)
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        """, nativeQuery = true)
    BigDecimal getTotalRevenue();
    
    @Query(value = """
        SELECT COALESCE(SUM(c.daily_price * ((r."end" - r.start) + 1)) + SUM(COALESCE(r.extra_costs, 0)), 0) 
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        WHERE EXTRACT(year FROM r.start) = EXTRACT(year FROM CURRENT_DATE) 
        AND EXTRACT(month FROM r.start) = EXTRACT(month FROM CURRENT_DATE)
        """, nativeQuery = true)
    BigDecimal getMonthlyRevenue();
    
    @Query(value = """
        SELECT COUNT(*) 
        FROM rentals r 
        WHERE r.start <= CURRENT_DATE AND r."end" >= CURRENT_DATE
        """, nativeQuery = true)
    Long getActiveRentalsCountQuery();
    
    @Query(value = """
        SELECT AVG((r."end" - r.start) + 1) 
        FROM rentals r
        """, nativeQuery = true)
    List<Object[]> getAverageRentalDuration();
    
    @Query(value = """
        SELECT COALESCE(SUM(c.daily_price * ((r."end" - r.start) + 1)) + SUM(COALESCE(r.extra_costs, 0)), 0) 
        FROM rentals r 
        JOIN cars c ON r.car_id = c.id
        WHERE r.start = :date OR r."end" = :date
        """, nativeQuery = true)
    BigDecimal getRevenueByDate(@Param("date") LocalDate date);
    
    @Query(value = """
        SELECT 
            CAST(DATE_TRUNC('month', r.start) AS DATE) as date,
            SUM(c.daily_price * ((r."end" - r.start) + 1)) + COALESCE(SUM(r.extra_costs), 0) as revenue,
            COUNT(r.id) as rentalCount
        FROM rentals r
        JOIN cars c ON r.car_id = c.id
        WHERE r.start >= :startDate
        GROUP BY DATE_TRUNC('month', r.start)
        ORDER BY date
        """, nativeQuery = true)
    List<Object[]> getRevenueTrend(@Param("startDate") LocalDate startDate);
    
    // Find active rentals ending tomorrow (for pickup reminders)
    @Query("SELECT r FROM Rental r WHERE r.end = :tomorrow")
    List<Rental> findRentalsEndingTomorrow(@Param("tomorrow") LocalDate tomorrow);
    
    // Find active rentals ending in 2 days (for payment reminders)
    @Query("SELECT r FROM Rental r WHERE r.end = :twoDaysFromNow")
    List<Rental> findRentalsEndingInTwoDays(@Param("twoDaysFromNow") LocalDate twoDaysFromNow);
    
    // Find rentals that ended yesterday (for rating requests)
    @Query("SELECT r FROM Rental r WHERE r.end = :yesterday")
    List<Rental> findRentalsEndedYesterday(@Param("yesterday") LocalDate yesterday);
}