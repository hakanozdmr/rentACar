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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Rating,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Add, Edit, Delete } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { customerPortalApi, ReservationRating, Rental } from '../services/api';

const RatingManagement: React.FC = () => {
  const [openRatingModal, setOpenRatingModal] = useState(false);
  const [editingRating, setEditingRating] = useState<ReservationRating | null>(null);
  const [formData, setFormData] = useState<Partial<ReservationRating>>({
    rating: 5,
    comment: '',
    carRating: true,
    isPublic: true
  });
  const [selectedRentalId, setSelectedRentalId] = useState<number | ''>('');
  const queryClient = useQueryClient();

  const { data: ratings, isLoading: ratingsLoading } = useQuery(
    ['myRatings'],
    () => customerPortalApi.getMyRatings().then(res => res.data)
  );

  const { data: rentals } = useQuery(
    ['myRentals'],
    () => customerPortalApi.getMyRentals().then(res => res.data)
  );

  const createRatingMutation = useMutation(
    (rating: ReservationRating) => customerPortalApi.rateReservation(rating),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['myRatings']);
        handleCloseModal();
      }
    }
  );

  const updateRatingMutation = useMutation(
    (rating: ReservationRating) => customerPortalApi.rateReservation(rating),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['myRatings']);
        handleCloseModal();
      }
    }
  );

  const handleOpenModal = (rating?: ReservationRating) => {
    if (rating) {
      setEditingRating(rating);
      setFormData({
        rating: rating.rating,
        comment: rating.comment || '',
        carRating: rating.carRating,
        isPublic: rating.isPublic
      });
      setSelectedRentalId(rating.rentalId);
    } else {
      setEditingRating(null);
      setFormData({
        rating: 5,
        comment: '',
        carRating: true,
        isPublic: true
      });
      setSelectedRentalId('');
    }
    setOpenRatingModal(true);
  };

  const handleCloseModal = () => {
    setOpenRatingModal(false);
    setEditingRating(null);
    setFormData({
      rating: 5,
      comment: '',
      carRating: true,
      isPublic: true
    });
    setSelectedRentalId('');
  };

  const handleSubmit = () => {
    if (!selectedRentalId) return;

    const ratingData: ReservationRating = {
      ...formData,
      rentalId: Number(selectedRentalId),
      customerId: 0, // Bu backend'de set edilecek
    } as ReservationRating;

    if (editingRating) {
      ratingData.id = editingRating.id;
      updateRatingMutation.mutate(ratingData);
    } else {
      createRatingMutation.mutate(ratingData);
    }
  };

  const getRentalInfo = (rating: ReservationRating) => {
    // Use car info directly from rating if available, otherwise fallback to rental lookup
    if (rating.carBrandName && rating.carModelName && rating.carPlate) {
      return `${rating.carBrandName} ${rating.carModelName} (${rating.carPlate})`;
    }
    
    const rental = rentals?.find((r: Rental) => r.id === rating.rentalId);
    return rental ? `${rental.carBrandName} ${rental.carModelName} (${rental.carPlate})` : 'Bilinmiyor';
  };

  const getCompletedRentals = () => {
    return rentals?.filter((r: Rental) => (r.status === 'COMPLETED' || r.status === 'CONFIRMED') && 
      !ratings?.some((rate: ReservationRating) => rate.rentalId === r.id)) || [];
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Değerlendirmelerim</Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => handleOpenModal()}
          disabled={getCompletedRentals().length === 0}
        >
          Yeni Değerlendirme
        </Button>
      </Box>

      {ratingsLoading ? (
        <CircularProgress />
      ) : ratings && ratings.length > 0 ? (
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Kiralama</TableCell>
                <TableCell>Puan</TableCell>
                <TableCell>Değerlendirme Türü</TableCell>
                <TableCell>Yorum</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ratings.map((rating: ReservationRating) => (
                <TableRow key={rating.id}>
                  <TableCell>
                    {getRentalInfo(rating)}
                  </TableCell>
                  <TableCell>
                    <Rating value={rating.rating} readOnly size="small" />
                  </TableCell>
                  <TableCell>
                    {rating.carRating ? 'Araç Değerlendirmesi' : 'Hizmet Değerlendirmesi'}
                  </TableCell>
                  <TableCell>
                    {rating.comment || '-'}
                  </TableCell>
                  <TableCell>
                    {rating.isPublic ? 'Herkese Açık' : 'Gizli'}
                  </TableCell>
                  <TableCell>
                    <Button
                      size="small"
                      startIcon={<Edit />}
                      onClick={() => handleOpenModal(rating)}
                    >
                      Düzenle
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        <Alert severity="info">
          Henüz değerlendirmeniz bulunmuyor.
        </Alert>
      )}

      {/* Rating Modal */}
      <Dialog open={openRatingModal} onClose={handleCloseModal} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingRating ? 'Değerlendirmeyi Düzenle' : 'Yeni Değerlendirme'}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <FormControl fullWidth>
              <InputLabel>Kiralama</InputLabel>
              <Select
                value={selectedRentalId}
                onChange={(e) => setSelectedRentalId(Number(e.target.value))}
                label="Kiralama"
                disabled={!!editingRating}
              >
                {getCompletedRentals().map((rental: Rental) => (
                  <MenuItem key={rental.id} value={rental.id}>
                    {rental.carBrandName} {rental.carModelName} ({rental.carPlate})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Box>
              <Typography component="legend">Puan</Typography>
              <Rating
                value={formData.rating}
                onChange={(event, newValue) => {
                  setFormData(prev => ({ ...prev, rating: newValue || 5 }));
                }}
                size="large"
              />
            </Box>

            <TextField
              fullWidth
              label="Yorum"
              multiline
              rows={4}
              value={formData.comment}
              onChange={(e) => setFormData(prev => ({ ...prev, comment: e.target.value }))}
              placeholder="Deneyiminiz hakkında yorum yapın..."
            />

            <FormControlLabel
              control={
                <Switch
                  checked={formData.carRating}
                  onChange={(e) => setFormData(prev => ({ ...prev, carRating: e.target.checked }))}
                />
              }
              label="Araç Değerlendirmesi"
            />

            <FormControlLabel
              control={
                <Switch
                  checked={formData.isPublic}
                  onChange={(e) => setFormData(prev => ({ ...prev, isPublic: e.target.checked }))}
                />
              }
              label="Herkese Açık"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseModal}>İptal</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={!selectedRentalId || createRatingMutation.isLoading || updateRatingMutation.isLoading}
          >
            {(createRatingMutation.isLoading || updateRatingMutation.isLoading) ? (
              <CircularProgress size={20} />
            ) : editingRating ? (
              'Güncelle'
            ) : (
              'Değerlendir'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default RatingManagement;
