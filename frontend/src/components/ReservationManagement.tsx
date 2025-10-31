import React, { useState } from 'react';
import {
  Paper,
  Typography,
  Box,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Add, Cancel } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { customerPortalApi, Reservation as ReservationType, AvailableCar } from '../services/api';
import dayjs from 'dayjs';

interface ReservationManagementProps {
  onUnreadCountChange: (count: number) => void;
}

const ReservationManagement: React.FC<ReservationManagementProps> = ({ onUnreadCountChange }) => {
  const [openNewReservation, setOpenNewReservation] = useState(false);
  const [formData, setFormData] = useState<Partial<ReservationType>>({
    startDate: '',
    endDate: '',
    carId: 0,
    specialRequests: '',
    note: ''
  });
  const [selectedCar, setSelectedCar] = useState<any>(null);
  const queryClient = useQueryClient();

  const { data: reservations, isLoading } = useQuery(
    ['myReservations'],
    () => customerPortalApi.getMyReservations().then(res => res.data)
  );

  const { data: availableCars, isLoading: isLoadingAvailableCars } = useQuery(
    ['availableCars', formData.startDate, formData.endDate],
    () => customerPortalApi.getAvailableCars(
      formData.startDate || '', 
      formData.endDate || ''
    ).then(res => res.data),
    {
      enabled: !!(formData.startDate && formData.endDate)
    }
  );

  const createReservationMutation = useMutation(
    (reservation: ReservationType) => customerPortalApi.createReservation(reservation),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['myReservations']);
        setOpenNewReservation(false);
        setFormData({
          startDate: '',
          endDate: '',
          carId: 0,
          specialRequests: '',
          note: ''
        });
      }
    }
  );

  const cancelReservationMutation = useMutation(
    (id: number) => customerPortalApi.cancelReservation(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['myReservations']);
      }
    }
  );

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'PENDING': return 'warning';
      case 'CANCELLED': return 'error';
      case 'COMPLETED': return 'info';
      default: return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'CONFIRMED': return 'Onaylandı';
      case 'PENDING': return 'Beklemede';
      case 'CANCELLED': return 'İptal Edildi';
      case 'COMPLETED': return 'Tamamlandı';
      case 'EXPIRED': return 'Süresi Doldu';
      default: return status;
    }
  };

  const handleCreateReservation = () => {
    if (formData.carId && formData.startDate && formData.endDate) {
      createReservationMutation.mutate(formData as ReservationType);
    }
  };

  const handleCancelReservation = (id: number) => {
    if (window.confirm('Bu rezervasyonu iptal etmek istediğinizden emin misiniz?')) {
      cancelReservationMutation.mutate(id);
    }
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Rezervasyonlarım</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => setOpenNewReservation(true)}
        >
          Yeni Rezervasyon
        </Button>
      </Box>

      {isLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Araç</TableCell>
                <TableCell>Başlangıç</TableCell>
                <TableCell>Bitiş</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>Tutar</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {reservations?.map((reservation: ReservationType) => (
                <TableRow key={reservation.id}>
                  <TableCell>
                    {reservation.carBrandName} {reservation.carModelName} ({reservation.carPlate})
                  </TableCell>
                  <TableCell>
                    {dayjs(reservation.startDate).format('DD.MM.YYYY')}
                  </TableCell>
                  <TableCell>
                    {dayjs(reservation.endDate).format('DD.MM.YYYY')}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusText(reservation.status)}
                      color={getStatusColor(reservation.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {reservation.totalAmount ? `₺${reservation.totalAmount.toLocaleString()}` : '-'}
                  </TableCell>
                  <TableCell>
                    {reservation.status === 'PENDING' && (
                      <IconButton
                        size="small"
                        onClick={() => handleCancelReservation(reservation.id!)}
                        color="error"
                      >
                        <Cancel />
                      </IconButton>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Yeni Rezervasyon Dialog */}
      <Dialog 
        open={openNewReservation} 
        onClose={() => setOpenNewReservation(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Yeni Rezervasyon Oluştur</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Başlangıç Tarihi"
                type="date"
                value={formData.startDate}
                onChange={(e) => setFormData(prev => ({ 
                  ...prev, 
                  startDate: e.target.value,
                  carId: 0 // Tarih değiştiğinde seçili araç ID'sini temizle
                }))}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: dayjs().format('YYYY-MM-DD') }}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Bitiş Tarihi"
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData(prev => ({ 
                  ...prev, 
                  endDate: e.target.value,
                  carId: 0 // Tarih değiştiğinde seçili araç ID'sini temizle
                }))}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: formData.startDate || dayjs().format('YYYY-MM-DD') }}
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>
                  {!formData.startDate || !formData.endDate 
                    ? 'Önce tarih seçin'
                    : 'Araç Seçin'
                  }
                </InputLabel>
                <Select
                  value={formData.carId || ''}
                  onChange={(e) => setFormData(prev => ({ ...prev, carId: Number(e.target.value) }))}
                  label={!formData.startDate || !formData.endDate ? 'Önce tarih seçin' : 'Araç Seçin'}
                  disabled={!formData.startDate || !formData.endDate || isLoadingAvailableCars || (!availableCars || availableCars.length === 0)}
                >
                  {isLoadingAvailableCars ? (
                    <MenuItem disabled>
                      <Box display="flex" alignItems="center" gap={1}>
                        <CircularProgress size={16} />
                        <Typography variant="body2">Müsait araçlar yükleniyor...</Typography>
                      </Box>
                    </MenuItem>
                  ) : availableCars && availableCars.length > 0 ? (
                    availableCars.map((car: AvailableCar) => (
                      <MenuItem key={car.carId} value={car.carId}>
                        {car.carBrandName} {car.carModelName} ({car.carPlate})
                      </MenuItem>
                    ))
                  ) : formData.startDate && formData.endDate ? (
                    <MenuItem disabled>
                      <Typography variant="body2" color="text.secondary">
                        Bu tarih aralığında müsait araç bulunmamaktadır
                      </Typography>
                    </MenuItem>
                  ) : null}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Özel İstekler"
                multiline
                rows={3}
                value={formData.specialRequests || ''}
                onChange={(e) => setFormData(prev => ({ ...prev, specialRequests: e.target.value }))}
                placeholder="Özel isteklerinizi buraya yazabilirsiniz..."
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Not"
                multiline
                rows={2}
                value={formData.note || ''}
                onChange={(e) => setFormData(prev => ({ ...prev, note: e.target.value }))}
                placeholder="Ek notlarınız..."
              />
            </Grid>
          </Grid>
          
          {/* Tarih seçimi ve araç durumu bilgileri */}
          {formData.startDate && formData.endDate && (
            <Box sx={{ mt: 2 }}>
              {isLoadingAvailableCars ? (
                <Alert severity="info" sx={{ display: 'flex', alignItems: 'center' }}>
                  <CircularProgress size={16} sx={{ mr: 1 }} />
                  {dayjs(formData.startDate).format('DD.MM.YYYY')} - {dayjs(formData.endDate).format('DD.MM.YYYY')} tarihleri arasında müsait araçlar kontrol ediliyor...
                </Alert>
              ) : availableCars && availableCars.length > 0 ? (
                <Alert severity="success">
                  {dayjs(formData.startDate).format('DD.MM.YYYY')} - {dayjs(formData.endDate).format('DD.MM.YYYY')} tarihleri arasında {availableCars.length} adet müsait araç bulundu.
                </Alert>
              ) : (
                <Alert severity="warning">
                  {dayjs(formData.startDate).format('DD.MM.YYYY')} - {dayjs(formData.endDate).format('DD.MM.YYYY')} tarihleri arasında uygun araç bulunamadı. Lütfen farklı tarihler deneyin.
                </Alert>
              )}
            </Box>
          )}
          
          {(!formData.startDate || !formData.endDate) && (
            <Box sx={{ mt: 2 }}>
              <Alert severity="info">
                Rezervasyon yapabilmek için önce başlangıç ve bitiş tarihlerini seçin.
              </Alert>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenNewReservation(false)}>İptal</Button>
          <Button
            onClick={handleCreateReservation}
            variant="contained"
            disabled={!formData.carId || !formData.startDate || !formData.endDate || createReservationMutation.isLoading}
          >
            {createReservationMutation.isLoading ? <CircularProgress size={20} /> : 'Rezervasyon Oluştur'}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default ReservationManagement;
