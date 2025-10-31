import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Chip,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { rentalsApi, carsApi, customersApi, Rental, Car, Customer } from '../services/api';
import dayjs from 'dayjs';
import isBetween from 'dayjs/plugin/isBetween';

dayjs.extend(isBetween);

const RentalsPage: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [viewingRental, setViewingRental] = useState<Rental | null>(null);
  const [editingRental, setEditingRental] = useState<Rental | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'completed' | 'upcoming'>('all');
  const [formData, setFormData] = useState<Partial<Rental>>({
    start: '',
    end: '',
    carId: 0,
    customerId: 0,
    extraCosts: 0,
    note: '',
  });
  const queryClient = useQueryClient();

  const { data: rentals = [], isLoading, error } = useQuery(
    'rentals',
    () => rentalsApi.getAll().then(res => res.data)
  );

  const { data: cars = [] } = useQuery(
    'cars',
    () => carsApi.getAll().then(res => res.data)
  );

  const { data: customers = [] } = useQuery(
    'customers',
    () => customersApi.getAll().then(res => res.data)
  );

  const createMutation = useMutation(
    (rental: Rental) => rentalsApi.create(rental as Rental),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('rentals');
        queryClient.invalidateQueries('cars');
        setErrorMessage(''); // Clear error message on success
        handleClose();
      },
      onError: (error: any) => {
        console.error('Create rental error:', error);
        setErrorMessage(error.message || 'Kiralama oluşturulurken hata oluştu');
      },
    }
  );

  const updateMutation = useMutation(
    (rental: Rental) => rentalsApi.update(rental),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('rentals');
        queryClient.invalidateQueries('cars');
        setErrorMessage(''); // Clear error message on success
        handleClose();
      },
      onError: (error: any) => {
        console.error('Update rental error:', error);
        setErrorMessage(error.message || 'Kiralama güncellenirken hata oluştu');
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => rentalsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('rentals');
        queryClient.invalidateQueries('cars');
        setErrorMessage(''); // Clear error message on success
      },
      onError: (error: any) => {
        console.error('Delete rental error:', error);
        setErrorMessage(error.message || 'Kiralama silinirken hata oluştu');
      },
    }
  );

  const availableCars = cars.filter(car => car.state === 1);

  const handleOpen = (rental?: Rental, isView = false) => {
    setErrorMessage(''); // Clear error message when opening dialog
    if (rental) {
      if (isView) {
        setViewingRental(rental);
        setEditingRental(null);
      } else {
        setEditingRental(rental);
        setViewingRental(null);
        setFormData({
          ...rental,
          start: rental.start,
          end: rental.end,
        });
      }
    } else {
      setViewingRental(null);
      setEditingRental(null);
      setFormData({
        start: '',
        end: '',
        carId: 0,
        customerId: 0,
        extraCosts: 0,
        note: '',
      });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setViewingRental(null);
    setEditingRental(null);
    setErrorMessage(''); // Clear error message when closing dialog
    setFormData({
      start: '',
      end: '',
      carId: 0,
      customerId: 0,
      extraCosts: 0,
      note: '',
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.carId || !formData.customerId) {
      alert('Lütfen araç ve müşteri seçin');
      return;
    }

    const rentalData: Rental = {
      ...formData,
      carId: formData.carId!,
      customerId: formData.customerId!,
    } as Rental;

    if (editingRental) {
      updateMutation.mutate({ ...rentalData, id: editingRental.id });
    } else {
      createMutation.mutate(rentalData);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Bu kiralamayı silmek istediğinizden emin misiniz?')) {
      deleteMutation.mutate(id);
    }
  };

  // Rental durumunu belirleme fonksiyonu
  const getRentalStatus = (rental: Rental) => {
    const now = dayjs();
    const startDate = dayjs(rental.start);
    const endDate = dayjs(rental.end);

    if (now.isBefore(startDate)) {
      return 'upcoming';
    } else if (now.isAfter(endDate)) {
      return 'completed';
    } else {
      return 'active';
    }
  };

  // Durum etiketini ve rengini döndürme fonksiyonu
  const getStatusChip = (rental: Rental) => {
    const status = getRentalStatus(rental);
    const statusMap = {
      'active': { label: 'Aktif', color: 'success' as const },
      'completed': { label: 'Tamamlandı', color: 'default' as const },
      'upcoming': { label: 'Yaklaşan', color: 'warning' as const },
    };
    
    const { label, color } = statusMap[status];
    return <Chip label={label} color={color} size="small" />;
  };

  // Filtrelenmiş kiralamalar
  const filteredRentals = rentals.filter(rental => {
    if (statusFilter === 'all') return true;
    return getRentalStatus(rental) === statusFilter;
  });

  const calculateTotalPrice = () => {
    if (!formData.start || !formData.end || !formData.carId) return 0;
    
    const selectedCar = cars.find(car => car.id === formData.carId);
    if (!selectedCar) return 0;

    const startDate = dayjs(formData.start);
    const endDate = dayjs(formData.end);
    const days = endDate.diff(startDate, 'day') + 1;
    
    return (selectedCar.dailyPrice * days) + (formData.extraCosts || 0);
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h4" component="h1">
            Kiralama Yönetimi
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpen()}
          >
            Yeni Kiralama
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            Kiralamalar yüklenirken hata oluştu.
          </Alert>
        )}

        {/* Durum Filtresi */}
        <Box mb={3}>
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Durum Filtresi</InputLabel>
            <Select
              value={statusFilter}
              label="Durum Filtresi"
              onChange={(e) => setStatusFilter(e.target.value as 'all' | 'active' | 'completed' | 'upcoming')}
            >
              <MenuItem value="all">Tümü</MenuItem>
              <MenuItem value="active">Aktif</MenuItem>
              <MenuItem value="completed">Tamamlandı</MenuItem>
              <MenuItem value="upcoming">Yaklaşan</MenuItem>
            </Select>
          </FormControl>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Müşteri</TableCell>
                <TableCell>Araç</TableCell>
                <TableCell>Başlangıç</TableCell>
                <TableCell>Bitiş</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>Tutar</TableCell>
                <TableCell align="center">İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredRentals.map((rental) => {
                return (
                  <TableRow key={rental.id}>
                    <TableCell>{rental.id}</TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {rental.customerFirstName} {rental.customerLastName}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="bold">
                        {rental.carPlate}
                      </Typography>
                    </TableCell>
                    <TableCell>{dayjs(rental.start).format('DD.MM.YYYY')}</TableCell>
                    <TableCell>{dayjs(rental.end).format('DD.MM.YYYY')}</TableCell>
                    <TableCell>
                      {getStatusChip(rental)}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" fontWeight="bold">
                        {rental.totalPrice?.toLocaleString('tr-TR', {
                          style: 'currency',
                          currency: 'TRY'
                        })}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <IconButton
                        color="info"
                        onClick={() => handleOpen(rental, true)}
                        size="small"
                      >
                        <ViewIcon />
                      </IconButton>
                      <IconButton
                        color="primary"
                        onClick={() => handleOpen(rental)}
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton
                        color="error"
                        onClick={() => handleDelete(rental.id!)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>

        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
          <DialogTitle>
            {viewingRental ? 'Kiralama Detayı' : editingRental ? 'Kiralama Düzenle' : 'Yeni Kiralama'}
          </DialogTitle>
          
          {viewingRental ? (
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="textSecondary">Müşteri:</Typography>
                  <Typography>{viewingRental.customerFirstName} {viewingRental.customerLastName}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="textSecondary">Araç:</Typography>
                  <Typography>{viewingRental.carPlate}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="textSecondary">Başlangıç:</Typography>
                  <Typography>{dayjs(viewingRental.start).format('DD.MM.YYYY')}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="textSecondary">Bitiş:</Typography>
                  <Typography>{dayjs(viewingRental.end).format('DD.MM.YYYY')}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="textSecondary">Günlük Fiyat:</Typography>
                  <Typography>
                    {viewingRental.dailyPrice?.toLocaleString('tr-TR', {
                      style: 'currency',
                      currency: 'TRY'
                    })}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="textSecondary">Toplam Tutar:</Typography>
                  <Typography fontWeight="bold">
                    {viewingRental.totalPrice?.toLocaleString('tr-TR', {
                      style: 'currency',
                      currency: 'TRY'
                    })}
                  </Typography>
                </Grid>
                {viewingRental.note && (
                  <Grid item xs={12}>
                    <Typography variant="subtitle2" color="textSecondary">Not:</Typography>
                    <Typography>{viewingRental.note}</Typography>
                  </Grid>
                )}
              </Grid>
            </DialogContent>
          ) : (
            <form onSubmit={handleSubmit}>
              <DialogContent>
                {errorMessage && (
                  <Alert severity="error" sx={{ mb: 2 }}>
                    {errorMessage}
                  </Alert>
                )}
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <FormControl fullWidth required>
                      <InputLabel>Müşteri</InputLabel>
                      <Select
                        value={formData.customerId}
                        label="Müşteri"
                        onChange={(e) => setFormData({ ...formData, customerId: e.target.value as number })}
                      >
                        {customers.map((customer) => (
                          <MenuItem key={customer.id} value={customer.id}>
                            {customer.firstName} {customer.lastName}
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <FormControl fullWidth required>
                      <InputLabel>Araç</InputLabel>
                      <Select
                        value={formData.carId}
                        label="Araç"
                        onChange={(e) => setFormData({ ...formData, carId: e.target.value as number })}
                      >
                        {availableCars.map((car) => (
                          <MenuItem key={car.id} value={car.id}>
                            {car.plate} ({car.brandName} {car.modelName})
                          </MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DatePicker
                      label="Başlangıç Tarihi"
                      value={formData.start ? dayjs(formData.start) : null}
                      onChange={(date) => setFormData({ 
                        ...formData, 
                        start: date?.format('YYYY-MM-DD') || '' 
                      })}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          required: true,
                        },
                      }}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <DatePicker
                      label="Bitiş Tarihi"
                      value={formData.end ? dayjs(formData.end) : null}
                      onChange={(date) => setFormData({ 
                        ...formData, 
                        end: date?.format('YYYY-MM-DD') || '' 
                      })}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          required: true,
                        },
                      }}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <TextField
                      label="Ek Maliyet"
                      type="number"
                      fullWidth
                      value={formData.extraCosts}
                      onChange={(e) => setFormData({ ...formData, extraCosts: Number(e.target.value) })}
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Box display="flex" alignItems="center" height="56px">
                      <Typography variant="h6" color="primary">
                        Toplam: {calculateTotalPrice().toLocaleString('tr-TR', {
                          style: 'currency',
                          currency: 'TRY'
                        })}
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      label="Not"
                      multiline
                      rows={3}
                      fullWidth
                      value={formData.note}
                      onChange={(e) => setFormData({ ...formData, note: e.target.value })}
                    />
                  </Grid>
                </Grid>
              </DialogContent>
              <DialogActions>
                <Button onClick={handleClose}>İptal</Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={createMutation.isLoading || updateMutation.isLoading}
                >
                  {createMutation.isLoading || updateMutation.isLoading ? (
                    <CircularProgress size={20} />
                  ) : (
                    editingRental ? 'Güncelle' : 'Kaydet'
                  )}
                </Button>
              </DialogActions>
            </form>
          )}
          
          {viewingRental && (
            <DialogActions>
              <Button onClick={handleClose}>Kapat</Button>
            </DialogActions>
          )}
        </Dialog>
      </>
    </LocalizationProvider>
  );
};

export default RentalsPage;
