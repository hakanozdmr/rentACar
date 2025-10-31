package hakan.rentacar.api.controllers;

import hakan.rentacar.entities.concretes.AuditLog;
import hakan.rentacar.entities.dtos.AuditLogDto;
import hakan.rentacar.entities.dtos.AuditStatisticsDto;
import hakan.rentacar.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Tag(name = "Audit Logs", description = "Sistem denetim kayıtları yönetimi")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Tüm audit logları listele", 
               description = "Sayfalama desteği ile tüm audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogDto>> getAllAuditLogs(
            @Parameter(description = "Sayfalama parametreleri") Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs(pageable));
    }

    @GetMapping("/entity/{entityName}")
    @Operation(summary = "Varlık türüne göre audit logları", 
               description = "Belirtilen varlık türü için audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getByEntityName(
            @Parameter(description = "Varlık türü (Car, Customer, Reservation vb.)") 
            @PathVariable String entityName) {
        return ResponseEntity.ok(auditLogService.getByEntityName(entityName));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @Operation(summary = "Varlık geçmişi", 
               description = "Belirli bir varlığın tüm değişiklik geçmişini listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getEntityHistory(
            @Parameter(description = "Varlık türü") @PathVariable String entityName,
            @Parameter(description = "Varlık ID'si") @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getEntityHistory(entityName, entityId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcı aktiviteleri", 
               description = "Belirtilen kullanıcının aktivite geçmişini listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getByUserId(
            @Parameter(description = "Kullanıcı ID'si") @PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getByUserId(userId));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Kullanıcı adına göre aktiviteler", 
               description = "Belirtilen kullanıcı adının aktivite geçmişini listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getByUsername(
            @Parameter(description = "Kullanıcı adı") @PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getByUsername(username));
    }

    @GetMapping("/action/{actionType}")
    @Operation(summary = "İşlem türüne göre audit logları", 
               description = "Belirtilen işlem türü için audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getByActionType(
            @Parameter(description = "İşlem türü") @PathVariable AuditLog.ActionType actionType) {
        return ResponseEntity.ok(auditLogService.getByActionType(actionType));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Tarih aralığına göre audit logları", 
               description = "Belirtilen tarih aralığındaki audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getByDateRange(
            @Parameter(description = "Başlangıç tarihi") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Bitiş tarihi") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditLogService.getByDateRange(startDate, endDate));
    }

    @GetMapping("/filtered")
    @Operation(summary = "Filtrelenmiş audit logları", 
               description = "Çoklu filtreleme ile audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogsWithFilters(
            @Parameter(description = "Varlık türü") @RequestParam(required = false) String entityName,
            @Parameter(description = "İşlem türü") @RequestParam(required = false) AuditLog.ActionType actionType,
            @Parameter(description = "Kullanıcı ID'si") @RequestParam(required = false) Long userId,
            @Parameter(description = "Başlangıç tarihi") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Bitiş tarihi") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Sayfalama parametreleri") Pageable pageable) {
        // Ensure proper sorting by timestamp desc
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                                    Sort.by(Sort.Direction.DESC, "timestamp"));
        }
        
        return ResponseEntity.ok(auditLogService.getAuditLogsWithFilters(
                entityName, actionType, userId, startDate, endDate, pageable));
    }

    @GetMapping("/recent")
    @Operation(summary = "Son audit logları", 
               description = "En son gerçekleşen audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getRecentAuditLogs(
            @Parameter(description = "Kayıt sayısı limiti") @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditLogService.getRecentAuditLogs(limit));
    }

    @GetMapping("/failed")
    @Operation(summary = "Başarısız işlemler", 
               description = "Başarısız olan işlemlerin audit kayıtlarını listeler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getFailedOperations() {
        return ResponseEntity.ok(auditLogService.getFailedOperations());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Audit istatistikleri", 
               description = "Sistem audit istatistiklerini getirir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditStatisticsDto> getAuditStatistics() {
        return ResponseEntity.ok(auditLogService.getAuditStatistics());
    }

    @DeleteMapping("/cleanup")
    @Operation(summary = "Eski audit loglarını temizle", 
               description = "Belirtilen günden eski audit kayıtlarını siler")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cleanupOldAuditLogs(
            @Parameter(description = "Saklanacak gün sayısı") @RequestParam(defaultValue = "365") int daysToKeep) {
        auditLogService.cleanupOldAuditLogs(daysToKeep);
        return ResponseEntity.ok().build();
    }
}
