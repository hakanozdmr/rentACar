import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Alert,
  CircularProgress,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Check as CheckIcon,
  Cancel as CancelIcon,
  Visibility as VisibilityIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { reservationsApi, Reservation, carsApi, customersApi, Car, Customer } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useSnackbar } from '../contexts/SnackbarContext';

const ReservationsPage: React.FC = () => {
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogType, setDialogType] = useState<'view' | 'confirm' | 'cancel'>('view');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { showSuccess, showError } = useSnackbar();

  const { data: pendingReservations = [], isLoading: isLoadingPending, error: errorPending } = useQuery(
    'pendingReservations',
    () => reservationsApi.getPending().then(res => res.data),
    {
      enabled: isAuthenticated,
      refetchInterval: 30000, // Refresh every 30 seconds
      retry: 1,
      onError: (error: any) => {
        console.error('Error fetching pending reservations:', error);
        if (error.response?.status === 401) {
          console.error('Unauthorized access - token might be invalid');
        }
      }
    }
  );

  const { data: allReservations = [], isLoading, error: errorAll } = useQuery(
    'allReservations',
    () => reservationsApi.getAll().then(res => res.data),
    {
      enabled: isAuthenticated,
      retry: 1,
      onError: (error: any) => {
        console.error('Error fetching all reservations:', error);
        if (error.response?.status === 401) {
          console.error('Unauthorized access - token might be invalid');
        }
      }
    }
  );

  const { data: cars = [] } = useQuery(
    'cars',
    () => carsApi.getAll().then(res => res.data),
    {
      enabled: isAuthenticated,
      retry: 1,
      onError: (error: any) => {
        console.error('Error fetching cars:', error);
      }
    }
  );

  const { data: customers = [] } = useQuery(
    'customers',
    () => customersApi.getAll().then(res => res.data),
    {
      enabled: isAuthenticated,
      retry: 1,
      onError: (error: any) => {
        console.error('Error fetching customers:', error);
      }
    }
  );

  const confirmMutation = useMutation(
    (id: number) => reservationsApi.confirm(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('pendingReservations');
        queryClient.invalidateQueries('allReservations');
        queryClient.invalidateQueries('cars'); // Araba durumu değiştiği için
        setDialogOpen(false);
        setErrorMessage('');
        showSuccess('Rezervasyon başarıyla onaylandı!');
        console.log('Reservation confirmed successfully, invalidating queries...');
      },
      onError: (error: any) => {
        console.error('Confirm reservation error:', error);
        setErrorMessage('Rezervasyon onaylanırken hata oluştu: ' + (error.response?.data?.message || error.message));
        showError('Rezervasyon onaylanırken hata oluştu');
      },
    }
  );

  const cancelMutation = useMutation(
    (id: number) => reservationsApi.cancel(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('pendingReservations');
        queryClient.invalidateQueries('allReservations');
        queryClient.invalidateQueries('cars'); // Araba durumu değiştiği için
        setDialogOpen(false);
        setErrorMessage('');
        showSuccess('Rezervasyon başarıyla iptal edildi!');
        console.log('Reservation cancelled successfully, invalidating queries...');
      },
      onError: (error: any) => {
        console.error('Cancel reservation error:', error);
        setErrorMessage('Rezervasyon iptal edilirken hata oluştu: ' + (error.response?.data?.message || error.message));
        showError('Rezervasyon iptal edilirken hata oluştu');
      },
    }
  );

  const handleOpenDialog = (reservation: Reservation, type: 'view' | 'confirm' | 'cancel') => {
    setErrorMessage(''); // Clear any previous error messages
    setSelectedReservation(reservation);
    setDialogType(type);
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setSelectedReservation(null);
    setErrorMessage(''); // Clear error messages when closing dialog
  };

  const handleConfirm = () => {
    if (selectedReservation?.id) {
      confirmMutation.mutate(selectedReservation.id);
    }
  };

  const handleCancel = () => {
    if (selectedReservation?.id) {
      cancelMutation.mutate(selectedReservation.id);
    }
  };

  const getCarInfo = (carId: number) => {
    const car = cars.find((c: Car) => c.id === carId);
    return car ? `${car.brandName} ${car.modelName} (${car.plate})` : 'Bilinmeyen Araç';
  };

  const getCustomerInfo = (customerId: number) => {
    const customer = customers.find((c: Customer) => c.id === customerId);
    return customer ? `${customer.firstName} ${customer.lastName}` : 'Bilinmeyen Müşteri';
  };

  const getStatusChip = (status: string) => {
    const statusMap: { [key: string]: { label: string; color: 'default' | 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' } } = {
      'PENDING': { label: 'Beklemede', color: 'warning' },
      'CONFIRMED': { label: 'Onaylandı', color: 'success' },
      'CANCELLED': { label: 'İptal Edildi', color: 'error' },
      'COMPLETED': { label: 'Tamamlandı', color: 'info' },
      'EXPIRED': { label: 'Süresi Doldu', color: 'default' },
    };
    
    const statusInfo = statusMap[status] || { label: status, color: 'default' };
    return <Chip label={statusInfo.label} color={statusInfo.color} size="small" />;
  };

  // Check authentication first
  if (!isAuthenticated) {
    return (
      <Box p={3}>
        <Alert severity="error">
          Bu sayfaya erişim için giriş yapmanız gerekiyor.
        </Alert>
      </Box>
    );
  }

  if (isLoadingPending || isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (errorPending || errorAll) {
    const error = errorPending || errorAll;
    const isAuthError = error?.response?.status === 401;
    
    return (
      <Box p={3}>
        <Alert severity="error">
          {isAuthError 
            ? "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın." 
            : "Rezervasyonlar yüklenirken hata oluştu"}
        </Alert>
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Rezervasyon Yönetimi
      </Typography>

      {errorMessage && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErrorMessage('')}>
          {errorMessage}
        </Alert>
      )}

      {/* Pending Reservations Section */}
      <Paper sx={{ mb: 3, p: 2 }}>
        <Typography variant="h6" gutterBottom color="warning.main">
          ⏳ Onay Bekleyen Rezervasyonlar ({pendingReservations.length})
        </Typography>
        
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Rezervasyon No</TableCell>
                <TableCell>Müşteri</TableCell>
                <TableCell>Araç</TableCell>
                <TableCell>Başlangıç</TableCell>
                <TableCell>Bitiş</TableCell>
                <TableCell>Toplam Tutar</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pendingReservations.map((reservation: Reservation) => (
                <TableRow key={reservation.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                  <TableCell>#{reservation.id}</TableCell>
                  <TableCell>{reservation.customerName || getCustomerInfo(reservation.customerId)}</TableCell>
                  <TableCell>{getCarInfo(reservation.carId)}</TableCell>
                  <TableCell>{new Date(reservation.startDate).toLocaleDateString('tr-TR')}</TableCell>
                  <TableCell>{new Date(reservation.endDate).toLocaleDateString('tr-TR')}</TableCell>
                  <TableCell>₺{reservation.totalAmount}</TableCell>
                  <TableCell>{getStatusChip(reservation.status || 'PENDING')}</TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      color="primary"
                      onClick={() => handleOpenDialog(reservation, 'view')}
                      title="Görüntüle"
                    >
                      <VisibilityIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      color="success"
                      onClick={() => handleOpenDialog(reservation, 'confirm')}
                      title="Onayla"
                      disabled={confirmMutation.isLoading}
                    >
                      <CheckIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleOpenDialog(reservation, 'cancel')}
                      title="İptal Et"
                      disabled={cancelMutation.isLoading}
                    >
                      <CancelIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {pendingReservations.length === 0 && (
          <Box textAlign="center" py={4}>
            <Typography color="text.secondary">
              Onay bekleyen rezervasyon bulunmuyor.
            </Typography>
          </Box>
        )}
      </Paper>

      {/* All Reservations Section */}
      <Paper sx={{ p: 2 }}>
        <Typography variant="h6" gutterBottom>
          Tüm Rezervasyonlar
        </Typography>
        
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Rezervasyon No</TableCell>
                <TableCell>Müşteri</TableCell>
                <TableCell>Araç</TableCell>
                <TableCell>Başlangıç</TableCell>
                <TableCell>Bitiş</TableCell>
                <TableCell>Toplam Tutar</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {allReservations
                .filter((r: Reservation) => r.status !== 'PENDING')
                .slice(0, 10)
                .map((reservation: Reservation) => (
                <TableRow key={reservation.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                  <TableCell>#{reservation.id}</TableCell>
                  <TableCell>{reservation.customerName || getCustomerInfo(reservation.customerId)}</TableCell>
                  <TableCell>{getCarInfo(reservation.carId)}</TableCell>
                  <TableCell>{new Date(reservation.startDate).toLocaleDateString('tr-TR')}</TableCell>
                  <TableCell>{new Date(reservation.endDate).toLocaleDateString('tr-TR')}</TableCell>
                  <TableCell>₺{reservation.totalAmount}</TableCell>
                  <TableCell>{getStatusChip(reservation.status || 'PENDING')}</TableCell>
                  <TableCell>
                    <IconButton
                      size="small"
                      color="primary"
                      onClick={() => handleOpenDialog(reservation, 'view')}
                      title="Görüntüle"
                    >
                      <VisibilityIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Dialog for viewing/confirming/cancelling reservations */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {dialogType === 'view' && 'Rezervasyon Detayları'}
          {dialogType === 'confirm' && 'Rezervasyon Onayla'}
          {dialogType === 'cancel' && 'Rezervasyon İptal Et'}
        </DialogTitle>
        <DialogContent>
          {selectedReservation && (
            <Box>
              <Typography variant="body1" gutterBottom>
                <strong>Rezervasyon No:</strong> #{selectedReservation.id}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Müşteri:</strong> {selectedReservation.customerName || getCustomerInfo(selectedReservation.customerId)}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Araç:</strong> {getCarInfo(selectedReservation.carId)}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Başlangıç:</strong> {new Date(selectedReservation.startDate).toLocaleDateString('tr-TR')}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Bitiş:</strong> {new Date(selectedReservation.endDate).toLocaleDateString('tr-TR')}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Gün Sayısı:</strong> {selectedReservation.daysCount}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Toplam Tutar:</strong> ₺{selectedReservation.totalAmount}
              </Typography>
              <Typography variant="body1" gutterBottom>
                <strong>Durum:</strong> {getStatusChip(selectedReservation.status || 'PENDING')}
              </Typography>
              {selectedReservation.specialRequests && (
                <Typography variant="body1" gutterBottom>
                  <strong>Özel İstekler:</strong> {selectedReservation.specialRequests}
                </Typography>
              )}
              {selectedReservation.note && (
                <Typography variant="body1" gutterBottom>
                  <strong>Not:</strong> {selectedReservation.note}
                </Typography>
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>
            {dialogType === 'view' ? 'Kapat' : 'İptal'}
          </Button>
          {dialogType === 'confirm' && (
            <Button
              onClick={handleConfirm}
              color="success"
              variant="contained"
              disabled={confirmMutation.isLoading}
            >
              {confirmMutation.isLoading ? <CircularProgress size={20} /> : 'Onayla'}
            </Button>
          )}
          {dialogType === 'cancel' && (
            <Button
              onClick={handleCancel}
              color="error"
              variant="contained"
              disabled={cancelMutation.isLoading}
            >
              {cancelMutation.isLoading ? <CircularProgress size={20} /> : 'İptal Et'}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ReservationsPage;
