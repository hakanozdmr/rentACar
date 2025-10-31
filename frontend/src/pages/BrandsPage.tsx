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
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { brandsApi, Brand } from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import { useConfirmDialog } from '../hooks/useConfirmDialog';

const BrandsPage: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [editingBrand, setEditingBrand] = useState<Brand | null>(null);
  const [formData, setFormData] = useState<Brand>({ name: '' });
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useSnackbar();
  const { showConfirm, ConfirmDialogComponent } = useConfirmDialog();

  const { data: brands = [], isLoading, error } = useQuery(
    'brands',
    () => brandsApi.getAll().then(res => res.data)
  );

  const createMutation = useMutation(
    (brand: Brand) => brandsApi.create(brand),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('brands');
        handleClose();
        showSuccess('Marka başarıyla oluşturuldu!');
      },
      onError: (error: any) => {
        showError('Marka oluşturulurken hata oluştu');
        console.error('Create brand error:', error);
      },
    }
  );

  const updateMutation = useMutation(
    (brand: Brand) => brandsApi.update(brand),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('brands');
        handleClose();
        showSuccess('Marka başarıyla güncellendi!');
      },
      onError: (error: any) => {
        showError('Marka güncellenirken hata oluştu');
        console.error('Update brand error:', error);
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => brandsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('brands');
        showSuccess('Marka başarıyla silindi!');
      },
      onError: (error: any) => {
        showError('Marka silinirken hata oluştu');
        console.error('Delete brand error:', error);
      },
    }
  );

  const handleOpen = (brand?: Brand) => {
    if (brand) {
      setEditingBrand(brand);
      setFormData(brand);
    } else {
      setEditingBrand(null);
      setFormData({ name: '' });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditingBrand(null);
    setFormData({ name: '' });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingBrand) {
      updateMutation.mutate({ ...formData, id: editingBrand.id });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number, brandName: string) => {
    showConfirm(
      {
        title: 'Markayı Sil',
        message: `"${brandName}" markasını silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.`,
        confirmText: 'Sil',
        cancelText: 'İptal',
        severity: 'error',
      },
      () => {
        deleteMutation.mutate(id);
      }
    );
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          Marka Yönetimi
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpen()}
        >
          Yeni Marka
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Markalar yüklenirken hata oluştu.
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Marka Adı</TableCell>
              <TableCell align="center">İşlemler</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {brands.map((brand) => (
              <TableRow key={brand.id}>
                <TableCell>{brand.id}</TableCell>
                <TableCell>{brand.name}</TableCell>
                <TableCell align="center">
                  <IconButton
                    color="primary"
                    onClick={() => handleOpen(brand)}
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton
                    color="error"
                    onClick={() => handleDelete(brand.id!, brand.name)}
                  >
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
        <DialogTitle>
          {editingBrand ? 'Marka Düzenle' : 'Yeni Marka'}
        </DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              label="Marka Adı"
              fullWidth
              variant="outlined"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
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
                editingBrand ? 'Güncelle' : 'Kaydet'
              )}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      
      <ConfirmDialogComponent />
    </>
  );
};

export default BrandsPage;
