package hakan.rentacar.service.impl;

import hakan.rentacar.entities.dtos.*;
import hakan.rentacar.repostories.CarRepository;
import hakan.rentacar.repostories.CustomerRepository;
import hakan.rentacar.repostories.RentalRepository;
import hakan.rentacar.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<RevenueReportDto> getMonthlyRevenueReport(int year) {
        List<Object[]> results = rentalRepository.getMonthlyRevenueReport(year);
        
        // Group by month to avoid duplicate months
        Map<LocalDate, RevenueReportDto> monthlyTotals = new HashMap<>();
        
        for (Object[] result : results) {
            LocalDate date = null;
            if (result[0] != null) {
                if (result[0] instanceof Timestamp) {
                    date = ((Timestamp) result[0]).toLocalDateTime().toLocalDate();
                } else if (result[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) result[0]).toLocalDate();
                } else if (result[0] instanceof LocalDate) {
                    date = (LocalDate) result[0];
                } else if (result[0] instanceof java.util.Date) {
                    date = ((java.util.Date) result[0]).toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                }
            }
            
            if (date != null) {
                RevenueReportDto existing = monthlyTotals.get(date);
                
                BigDecimal revenue = BigDecimal.ZERO;
                if (result[2] != null) {
                    if (result[2] instanceof BigDecimal) {
                        revenue = (BigDecimal) result[2];
                    } else if (result[2] instanceof Number) {
                        revenue = BigDecimal.valueOf(((Number) result[2]).doubleValue());
                    }
                }
                
                Long rentals = ((Number) result[3]).longValue();
                
                if (existing == null) {
                    monthlyTotals.put(date, RevenueReportDto.builder()
                            .date(date)
                            .period("MONTHLY")
                            .totalRevenue(revenue)
                            .totalRentals(rentals)
                            .build());
                } else {
                    existing.setTotalRevenue(existing.getTotalRevenue().add(revenue));
                    existing.setTotalRentals(existing.getTotalRentals() + rentals);
                }
            }
        }
        
        return monthlyTotals.values().stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueReportDto> getYearlyRevenueReport(int startYear, int endYear) {
        List<Object[]> results = rentalRepository.getYearlyRevenueReport(startYear, endYear);
        return results.stream()
                .map(this::mapToRevenueReport)
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueReportDto> getDailyRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = rentalRepository.getDailyRevenueReport(startDate, endDate);
        return results.stream()
                .map(this::mapToRevenueReport)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarAnalyticsDto> getMostRentedCars(int limit) {
        List<Object[]> results = rentalRepository.getMostRentedCars(limit);
        return results.stream()
                .map(this::mapToCarAnalytics)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarAnalyticsDto> getTopRevenueCars(int limit) {
        List<Object[]> results = rentalRepository.getTopRevenueCars(limit);
        return results.stream()
                .map(this::mapToCarAnalytics)
                .collect(Collectors.toList());
    }

    @Override
    public List<CarAnalyticsDto> getCarUtilizationStats() {
        List<Object[]> results = rentalRepository.getCarUtilizationStats();
        return results.stream()
                .map(this::mapToCarAnalytics)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerSegmentDto> getCustomerSegmentation() {
        List<Object[]> results = rentalRepository.getCustomerSegmentation();
        return results.stream()
                .map(this::mapToCustomerSegment)
                .collect(Collectors.toList());
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        BigDecimal totalRevenue = rentalRepository.getTotalRevenue();
        BigDecimal monthlyRevenue = rentalRepository.getMonthlyRevenue();
        BigDecimal todayRevenue = getTodayRevenue();
        
        Long totalActiveCars = carRepository.count();
        Long totalRentals = rentalRepository.count();
        Long activeRentals = getActiveRentalsCount();
        Long totalCustomers = customerRepository.count();
        
        BigDecimal averageRentalDuration = getAverageRentalDuration();
        
        List<RevenueReportDto> monthlyRevenueData = getMonthlyRevenueReport(LocalDate.now().getYear());
        List<CarAnalyticsDto> topPerformingCars = getTopRevenueCars(5);
        List<CustomerSegmentDto> customerSegments = getCustomerSegmentation();
        
        return DashboardStatsDto.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .totalActiveCars(totalActiveCars)
                .totalRentals(totalRentals)
                .activeRentals(activeRentals)
                .totalCustomers(totalCustomers)
                .averageRentalDuration(averageRentalDuration)
                .monthlyRevenueData(monthlyRevenueData)
                .topPerformingCars(topPerformingCars)
                .customerSegments(customerSegments)
                .build();
    }

    @Override
    public List<TrendAnalysisDto> getRevenueTrend(int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        List<Object[]> results = rentalRepository.getRevenueTrend(startDate);
        
        List<TrendAnalysisDto> trends = new ArrayList<>();
        BigDecimal previousRevenue = BigDecimal.ZERO;
        
        for (Object[] result : results) {
            LocalDate date = null;
            if (result[0] != null) {
                if (result[0] instanceof Timestamp) {
                    date = ((Timestamp) result[0]).toLocalDateTime().toLocalDate();
                } else if (result[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) result[0]).toLocalDate();
                } else if (result[0] instanceof LocalDate) {
                    date = (LocalDate) result[0];
                } else if (result[0] instanceof java.util.Date) {
                    date = ((java.util.Date) result[0]).toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                }
            }

            BigDecimal revenue = BigDecimal.ZERO;
            if (result[1] != null) {
                if (result[1] instanceof BigDecimal) {
                    revenue = (BigDecimal) result[1];
                } else if (result[1] instanceof Number) {
                    revenue = BigDecimal.valueOf(((Number) result[1]).doubleValue());
                }
            }
            
            Long rentalCount = ((Number) result[2]).longValue();
            
            BigDecimal growthRate = BigDecimal.ZERO;
            String trend = "STABLE";
            
            if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                growthRate = revenue.subtract(previousRevenue)
                        .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                
                if (growthRate.compareTo(BigDecimal.valueOf(5)) > 0) {
                    trend = "UP";
                } else if (growthRate.compareTo(BigDecimal.valueOf(-5)) < 0) {
                    trend = "DOWN";
                }
            }
            
            // Only add if date is valid
            if (date != null) {
                trends.add(TrendAnalysisDto.builder()
                        .date(date)
                        .revenue(revenue)
                        .rentalCount(rentalCount)
                        .growthRate(growthRate)
                        .trend(trend)
                        .build());
            }
            
            previousRevenue = revenue;
        }
        
        return trends;
    }

    @Override
    public BigDecimal getTodayRevenue() {
        return rentalRepository.getRevenueByDate(LocalDate.now());
    }

    @Override
    public Long getActiveRentalsCount() {
        return rentalRepository.getActiveRentalsCountQuery();
    }

    @Override
    public BigDecimal getAverageRentalDuration() {
        List<Object[]> results = rentalRepository.getAverageRentalDuration();
        if (!results.isEmpty()) {
            Object result = results.get(0)[0];
            if (result instanceof Number) {
                return BigDecimal.valueOf(((Number) result).doubleValue());
            }
        }
        return BigDecimal.ZERO;
    }

    // Helper mapping methods
    private RevenueReportDto mapToRevenueReport(Object[] result) {
        LocalDate date = null;
        if (result[0] != null) {
            if (result[0] instanceof Timestamp) {
                date = ((Timestamp) result[0]).toLocalDateTime().toLocalDate();
            } else if (result[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) result[0]).toLocalDate();
            } else if (result[0] instanceof LocalDate) {
                date = (LocalDate) result[0];
            } else if (result[0] instanceof java.util.Date) {
                date = ((java.util.Date) result[0]).toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            }
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (result[2] != null) {
            if (result[2] instanceof BigDecimal) {
                totalRevenue = (BigDecimal) result[2];
            } else if (result[2] instanceof Number) {
                totalRevenue = BigDecimal.valueOf(((Number) result[2]).doubleValue());
            }
        }

        return RevenueReportDto.builder()
                .date(date)
                .period((String) result[1])
                .totalRevenue(totalRevenue)
                .totalRentals(((Number) result[3]).longValue())
                .carBrand((String) result[4])
                .carModel((String) result[5])
                .build();
    }

    private CarAnalyticsDto mapToCarAnalytics(Object[] result) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (result[5] != null) {
            if (result[5] instanceof BigDecimal) {
                totalRevenue = (BigDecimal) result[5];
            } else if (result[5] instanceof Number) {
                totalRevenue = BigDecimal.valueOf(((Number) result[5]).doubleValue());
            }
        }

        BigDecimal utilizationRate = BigDecimal.ZERO;
        if (result[7] != null) {
            if (result[7] instanceof BigDecimal) {
                utilizationRate = (BigDecimal) result[7];
            } else if (result[7] instanceof Number) {
                utilizationRate = BigDecimal.valueOf(((Number) result[7]).doubleValue());
            }
        }

        BigDecimal averageDailyRevenue = BigDecimal.ZERO;
        if (result[8] != null) {
            if (result[8] instanceof BigDecimal) {
                averageDailyRevenue = (BigDecimal) result[8];
            } else if (result[8] instanceof Number) {
                averageDailyRevenue = BigDecimal.valueOf(((Number) result[8]).doubleValue());
            }
        }

        return CarAnalyticsDto.builder()
                .carId(((Number) result[0]).longValue())
                .plate((String) result[1])
                .brandName((String) result[2])
                .modelName((String) result[3])
                .totalRentals(((Number) result[4]).longValue())
                .totalRevenue(totalRevenue)
                .totalRentalDays(((Number) result[6]).longValue())
                .utilizationRate(utilizationRate)
                .averageDailyRevenue(averageDailyRevenue)
                .build();
    }

    private CustomerSegmentDto mapToCustomerSegment(Object[] result) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        if (result[2] != null) {
            if (result[2] instanceof BigDecimal) {
                totalRevenue = (BigDecimal) result[2];
            } else if (result[2] instanceof Number) {
                totalRevenue = BigDecimal.valueOf(((Number) result[2]).doubleValue());
            }
        }

        BigDecimal averageRevenuePerCustomer = BigDecimal.ZERO;
        if (result[3] != null) {
            if (result[3] instanceof BigDecimal) {
                averageRevenuePerCustomer = (BigDecimal) result[3];
            } else if (result[3] instanceof Number) {
                averageRevenuePerCustomer = BigDecimal.valueOf(((Number) result[3]).doubleValue());
            }
        }

        return CustomerSegmentDto.builder()
                .segmentName((String) result[0])
                .customerCount(((Number) result[1]).longValue())
                .totalRevenue(totalRevenue)
                .averageRevenuePerCustomer(averageRevenuePerCustomer)
                .totalRentals(((Number) result[4]).longValue())
                .build();
    }
}
