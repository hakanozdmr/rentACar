package hakan.rentacar.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatisticsDto {
    
    private long totalAuditLogs;
    private long totalUsers;
    private long totalEntities;
    private Map<String, Long> actionTypeCounts;
    private Map<String, Long> entityTypeCounts;
    private Map<String, Long> userActivityCounts;
    private long failedOperations;
    private long successfulOperations;
    private String mostActiveUser;
    private String mostModifiedEntity;
    private String lastAuditTime;
}


