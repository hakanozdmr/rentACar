import React, { useState } from 'react';
import {
  Paper,
  Typography,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
  Alert,
  Button,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Rating,
  FormControlLabel,
  Switch,
} from '@mui/material';
import { Star, StarBorder } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { customerPortalApi, Rental, ReservationRating } from '../services/api';
import dayjs from 'dayjs';
import { useSnackbar } from '../contexts/SnackbarContext';

const RentalManagement: React.FC = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useSnackbar();
  const [ratingModalOpen, setRatingModalOpen] = useState(false);
  const [selectedRental, setSelectedRental] = useState<Rental | null>(null);
  const [rating, setRating] = useState<number>(0);
  const [comment, setComment] = useState<string>('');
  const [isPublic, setIsPublic] = useState<boolean>(true);

  const { data: rentals = [], isLoading, error } = useQuery(
    ['myRentals'],
    () => customerPortalApi.getMyRentals().then(res => res.data)
  );

  const ratingMutation = useMutation(
    (ratingData: ReservationRating) => customerPortalApi.rateReservation(ratingData),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['myRentals']);
        showSuccess('Değerlendirmeniz başarıyla kaydedildi!');
        setRatingModalOpen(false);
        resetRatingForm();
      },
      onError: () => {
        showError('Değerlendirme kaydedilirken bir hata oluştu.');
      }
    }
  );

  const getStatusColor = (rental: Rental) => {
    // Use status from rental if available, otherwise use date-based logic
    if (rental.status) {
      switch (rental.status) {
        case 'COMPLETED':
          return 'success';
        case 'CONFIRMED':
          return 'primary';
        case 'PENDING':
          return 'warning';
        case 'CANCELLED':
          return 'error';
        case 'EXPIRED':
          return 'default';
        default:
          break;
      }
    }
    
    // Fallback to date-based logic
    const endDate = dayjs(rental.end);
    const now = dayjs();
    
    if (endDate.isBefore(now)) {
      return 'success'; // Tamamlandı
    } else {
      return 'primary'; // Aktif
    }
  };

  const getStatusText = (rental: Rental) => {
    // Use status from rental if available
    if (rental.status) {
      switch (rental.status) {
        case 'PENDING':
          return 'Beklemede';
        case 'CONFIRMED':
          return 'Onaylandı';
        case 'CANCELLED':
          return 'İptal Edildi';
        case 'COMPLETED':
          return 'Tamamlandı';
        case 'EXPIRED':
          return 'Süresi Doldu';
        default:
          break;
      }
    }
    
    // Fallback to date-based logic
    const endDate = dayjs(rental.end);
    const now = dayjs();
    
    if (endDate.isBefore(now)) {
      return 'Tamamlandı';
    } else {
      return 'Aktif';
    }
  };

  const resetRatingForm = () => {
    setRating(0);
    setComment('');
    setIsPublic(true);
  };

  const handleRateClick = (rental: Rental) => {
    setSelectedRental(rental);
    resetRatingForm();
    setRatingModalOpen(true);
  };

  const handleRatingSubmit = () => {
    if (!selectedRental) {
      showError('Kiralama bulunamadı!');
      return;
    }
    
    if (!rating || rating === 0) {
      showError('Lütfen en az 1 yıldız verin!');
      return;
    }

    // Use rental ID directly (after table merge)
    const rentalId = selectedRental.id;
    
    if (!rentalId) {
      console.error('Rental ID not found for rental:', selectedRental);
      showError('Kiralama bilgisi bulunamadı! Lütfen sistem yöneticisi ile iletişime geçin.');
      return;
    }

    const ratingData: ReservationRating = {
      rentalId: rentalId, // Use rentalId instead of reservationId
      customerId: selectedRental.customerId,
      rating: rating,
      comment: comment.trim() || undefined,
      carRating: true,
      isPublic: isPublic,
    };

    ratingMutation.mutate(ratingData);
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="200px">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error">
        Geçmiş kiralama bilgileri yüklenirken bir hata oluştu.
      </Alert>
    );
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h6" gutterBottom>
        Geçmiş Kiralamalarım
      </Typography>
      
      {rentals.length === 0 ? (
        <Box display="flex" flexDirection="column" alignItems="center" py={4}>
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Henüz kiralama yapmamışsınız
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Rezervasyon yaparak kiralama işleminizi başlatabilirsiniz.
          </Typography>
        </Box>
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
                <TableCell>Not</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rentals.map((rental: Rental) => (
                <TableRow key={rental.id}>
                  <TableCell>
                    <Box>
                      <Typography variant="body2" fontWeight="medium">
                        {rental.carBrandName && rental.carModelName ? 
                          `${rental.carBrandName} ${rental.carModelName}` : 
                          rental.carPlate || '-'
                        }
                      </Typography>
                      {rental.carPlate && (rental.carBrandName || rental.carModelName) && (
                        <Typography variant="caption" color="text.secondary">
                          {rental.carPlate}
                        </Typography>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {dayjs(rental.start).format('DD.MM.YYYY')}
                  </TableCell>
                  <TableCell>
                    {dayjs(rental.end).format('DD.MM.YYYY')}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusText(rental)}
                      color={getStatusColor(rental) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {rental.totalPrice ? 
                      `₺${rental.totalPrice.toLocaleString()}` : 
                      rental.dailyPrice ? 
                        `₺${rental.dailyPrice} × ${dayjs(rental.end).diff(dayjs(rental.start), 'day') + 1} gün` :
                        '-'
                    }
                  </TableCell>
                  <TableCell>
                    {rental.note && rental.note.length > 50 ? 
                      `${rental.note.substring(0, 50)}...` : 
                      rental.note || '-'
                    }
                  </TableCell>
                  <TableCell>
                    {rental.canRate ? (
                      <Button
                        variant="outlined"
                        size="small"
                        color="warning"
                        startIcon={<StarBorder />}
                        onClick={() => handleRateClick(rental)}
                      >
                        Değerlendir
                      </Button>
                    ) : rental.isRated ? (
                      <Box display="flex" alignItems="center" gap={1}>
                        <Star sx={{ color: 'orange' }} />
                        <Typography variant="body2" color="text.secondary">
                          Değerlendirildi
                        </Typography>
                      </Box>
                    ) : (
                      <Typography variant="body2" color="text.secondary">
                        Henüz değerlendirme yapılamaz
                      </Typography>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Rating Modal */}
      <Dialog
        open={ratingModalOpen}
        onClose={() => {
          setRatingModalOpen(false);
          setSelectedRental(null);
        }}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {selectedRental && `${selectedRental.carBrandName} ${selectedRental.carModelName}`} Değerlendirmesi
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Kiralama Tarihi: {selectedRental && selectedRental.start && selectedRental.end ? 
                `${dayjs(selectedRental.start).format('DD.MM.YYYY')} - ${dayjs(selectedRental.end).format('DD.MM.YYYY')}` : 
                'Tarih bilgisi bulunamadı'
              }
            </Typography>
            
            <Box sx={{ mt: 3 }}>
              <Typography variant="subtitle1" gutterBottom>
                Genel Deneyiminiz:
              </Typography>
              <Rating
                value={rating}
                onChange={(event, newValue) => {
                  const ratingValue = newValue !== null ? newValue : 0;
                  setRating(ratingValue);
                }}
                size="large"
              />
            </Box>

            <Box sx={{ mt: 3 }}>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Yorumunuz (İsteğe bağlı)"
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                placeholder="Deneyiminizi paylaşın..."
                variant="outlined"
              />
            </Box>

            <Box sx={{ mt: 3 }}>
              <FormControlLabel
                control={
                  <Switch
                    checked={isPublic}
                    onChange={(e) => setIsPublic(e.target.checked)}
                  />
                }
                label="Değerlendirmemi herkese açık yap"
              />
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setRatingModalOpen(false);
            setSelectedRental(null);
          }}>
            İptal
          </Button>
          <Button 
            onClick={handleRatingSubmit}
            variant="contained"
            disabled={!rating || rating < 1 || ratingMutation.isLoading}
            startIcon={ratingMutation.isLoading ? <CircularProgress size={20} /> : <Star />}
          >
            {ratingMutation.isLoading ? 'Gönderiliyor...' : 'Değerlendir'}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default RentalManagement;
