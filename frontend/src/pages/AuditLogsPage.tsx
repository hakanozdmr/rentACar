import React, { useState } from 'react';
import {
  Box,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Chip,
  Grid,
  Card,
  CardContent,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Search,
  FilterList,
  Visibility,
  GetApp,
  History,
  Security,
  BugReport,
  CheckCircle,
  Cancel,
  Error,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import dayjs from 'dayjs';
import { auditLogsApi, AuditLog } from '../services/api';

const AuditLogsPage: React.FC = () => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [searchTerm, setSearchTerm] = useState('');
  const [entityNameFilter, setEntityNameFilter] = useState('');
  const [actionTypeFilter, setActionTypeFilter] = useState('');
  const [operationResultFilter, setOperationResultFilter] = useState('');
  const [selectedLog, setSelectedLog] = useState<AuditLog | null>(null);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);

  // Fetch audit logs
  const { data: auditLogsResponse, isLoading, error } = useQuery(
    ['audit-logs', page, rowsPerPage, entityNameFilter, actionTypeFilter, operationResultFilter],
    () => auditLogsApi.getAll(page, rowsPerPage, {
      entityName: entityNameFilter || undefined,
      actionType: actionTypeFilter || undefined,
      operationResult: operationResultFilter || undefined,
    }).then(res => res.data),
    {
      keepPreviousData: true,
    }
  );

  const auditLogs = auditLogsResponse?.content || [];

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleViewDetails = (log: AuditLog) => {
    setSelectedLog(log);
    setDetailDialogOpen(true);
  };

  const handleCloseDetailDialog = () => {
    setDetailDialogOpen(false);
    setSelectedLog(null);
  };

  const getStatusColor = (result?: string) => {
    switch (result) {
      case 'SUCCESS':
        return 'success';
      case 'FAILURE':
      case 'ERROR':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (result?: string) => {
    switch (result) {
      case 'SUCCESS':
        return <CheckCircle />;
      case 'FAILURE':
      case 'ERROR':
        return <Error />;
      default:
        return <Cancel />;
    }
  };

  const getActionTypeColor = (actionType: string) => {
    switch (actionType) {
      case 'CREATE':
        return 'success';
      case 'UPDATE':
        return 'warning';
      case 'DELETE':
        return 'error';
      case 'READ':
        return 'info';
      default:
        return 'default';
    }
  };

  const formatJson = (jsonString?: string) => {
    if (!jsonString) return '';
    try {
      return JSON.stringify(JSON.parse(jsonString), null, 2);
    } catch {
      return jsonString;
    }
  };

  if (error) {
    const errorMessage = (error as any)?.message || (error as any)?.error || 'Bilinmeyen hata oluştu';
    
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Audit logları yüklenirken hata oluştu: {errorMessage}
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', minHeight: '100vh', p: 0 }}>
      {/* Header */}
      <Box sx={{ background: 'white', p: 3, mb: 3, boxShadow: '0 4px 20px rgba(0,0,0,0.1)' }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ 
          background: 'linear-gradient(45deg, #667eea 30%, #764ba2 90%)',
          backgroundClip: 'text',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          fontWeight: 'bold',
          textAlign: 'center'
        }}>
          🔍 Sistem Denetim Kayıtları
        </Typography>
      </Box>

      {/* Filters */}
      <Paper sx={{ 
        p: 2, 
        mb: 3,
        mx: 2,
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
        borderRadius: 3,
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)'
      }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              fullWidth
              label="Ara"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <FormControl fullWidth>
              <InputLabel>Varlık Türü</InputLabel>
              <Select
                value={entityNameFilter}
                onChange={(e) => setEntityNameFilter(e.target.value)}
                label="Varlık Türü"
              >
                <MenuItem value="">Tümü</MenuItem>
                <MenuItem value="Car">Araç</MenuItem>
                <MenuItem value="Customer">Müşteri</MenuItem>
                <MenuItem value="Reservation">Rezervasyon</MenuItem>
                <MenuItem value="Rental">Kiralama</MenuItem>
                <MenuItem value="Payment">Ödeme</MenuItem>
                <MenuItem value="Invoice">Fatura</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <FormControl fullWidth>
              <InputLabel>İşlem Türü</InputLabel>
              <Select
                value={actionTypeFilter}
                onChange={(e) => setActionTypeFilter(e.target.value)}
                label="İşlem Türü"
              >
                <MenuItem value="">Tümü</MenuItem>
                <MenuItem value="CREATE">Oluştur</MenuItem>
                <MenuItem value="UPDATE">Güncelle</MenuItem>
                <MenuItem value="DELETE">Sil</MenuItem>
                <MenuItem value="READ">Oku</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <FormControl fullWidth>
              <InputLabel>Sonuç</InputLabel>
              <Select
                value={operationResultFilter}
                onChange={(e) => setOperationResultFilter(e.target.value)}
                label="Sonuç"
              >
                <MenuItem value="">Tümü</MenuItem>
                <MenuItem value="SUCCESS">Başarılı</MenuItem>
                <MenuItem value="FAILURE">Başarısız</MenuItem>
                <MenuItem value="ERROR">Hata</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Audit Logs Table */}
      <Paper sx={{ 
        mx: 2,
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
        borderRadius: 3,
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)'
      }}>
        {isLoading ? (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Tarih</TableCell>
                    <TableCell>Varlık</TableCell>
                    <TableCell>İşlem</TableCell>
                    <TableCell>Kullanıcı</TableCell>
                    <TableCell>IP Adresi</TableCell>
                    <TableCell>Sonuç</TableCell>
                    <TableCell>Süre (ms)</TableCell>
                    <TableCell>İşlemler</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {auditLogs.map((log) => (
                    <TableRow key={log.id} hover>
                      <TableCell>
                        {dayjs(log.timestamp).format('DD.MM.YYYY HH:mm:ss')}
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {log.entityName}
                          {log.entityId && ` (ID: ${log.entityId})`}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={log.actionTypeDisplayName}
                          color={getActionTypeColor(log.actionType) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {log.username || 'Sistem'}
                        </Typography>
                        {log.userEmail && (
                          <Typography variant="caption" color="textSecondary">
                            {log.userEmail}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {log.ipAddress || '-'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          icon={getStatusIcon(log.operationResult)}
                          label={log.operationResult || 'UNKNOWN'}
                          color={getStatusColor(log.operationResult) as any}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {log.executionTimeMs ? `${log.executionTimeMs}ms` : '-'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Tooltip title="Detayları Görüntüle">
                          <IconButton
                            size="small"
                            onClick={() => handleViewDetails(log)}
                          >
                            <Visibility />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              rowsPerPageOptions={[10, 25, 50, 100]}
              component="div"
              count={auditLogsResponse?.totalElements || 0}
              rowsPerPage={rowsPerPage}
              page={page}
              onPageChange={handleChangePage}
              onRowsPerPageChange={handleChangeRowsPerPage}
              labelRowsPerPage="Satır sayısı:"
            />
          </>
        )}
      </Paper>

      {/* Detail Dialog */}
      <Dialog
        open={detailDialogOpen}
        onClose={handleCloseDetailDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <History />
            <Typography variant="h6">Audit Log Detayları</Typography>
          </Box>
        </DialogTitle>
        <DialogContent>
          {selectedLog && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="textSecondary">
                    Varlık Türü:
                  </Typography>
                  <Typography variant="body1">{selectedLog.entityName}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="textSecondary">
                    Varlık ID:
                  </Typography>
                  <Typography variant="body1">{selectedLog.entityId || '-'}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="textSecondary">
                    İşlem Türü:
                  </Typography>
                  <Chip
                    label={selectedLog.actionTypeDisplayName}
                    color={getActionTypeColor(selectedLog.actionType) as any}
                    size="small"
                  />
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="textSecondary">
                    Kullanıcı:
                  </Typography>
                  <Typography variant="body1">
                    {selectedLog.username || 'Sistem'}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="textSecondary">
                    IP Adresi:
                  </Typography>
                  <Typography variant="body1">{selectedLog.ipAddress || '-'}</Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="subtitle2" color="textSecondary">
                    Tarih:
                  </Typography>
                  <Typography variant="body1">
                    {dayjs(selectedLog.timestamp).format('DD.MM.YYYY HH:mm:ss')}
                  </Typography>
                </Grid>
                {selectedLog.requestUrl && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="textSecondary">
                      Request URL:
                    </Typography>
                    <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>
                      {selectedLog.requestMethod} {selectedLog.requestUrl}
                    </Typography>
                  </Grid>
                )}
                {selectedLog.oldValues && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="textSecondary">
                      Eski Değerler:
                    </Typography>
                    <Paper sx={{ p: 1, bgcolor: 'grey.50', mt: 1 }}>
                      <pre style={{ margin: 0, fontSize: '12px' }}>
                        {formatJson(selectedLog.oldValues)}
                      </pre>
                    </Paper>
                  </Grid>
                )}
                {selectedLog.newValues && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="textSecondary">
                      Yeni Değerler:
                    </Typography>
                    <Paper sx={{ p: 1, bgcolor: 'grey.50', mt: 1 }}>
                      <pre style={{ margin: 0, fontSize: '12px' }}>
                        {formatJson(selectedLog.newValues)}
                      </pre>
                    </Paper>
                  </Grid>
                )}
                {selectedLog.errorMessage && (
                  <Grid item xs={12}>
                    <Alert severity="error">
                      <Typography variant="subtitle2">Hata Mesajı:</Typography>
                      <Typography variant="body2">{selectedLog.errorMessage}</Typography>
                    </Alert>
                  </Grid>
                )}
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDetailDialog}>Kapat</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AuditLogsPage;
