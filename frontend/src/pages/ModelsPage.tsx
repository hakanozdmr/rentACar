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
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { modelsApi, brandsApi, Model, Brand } from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import { useConfirmDialog } from '../hooks/useConfirmDialog';

const ModelsPage: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [editingModel, setEditingModel] = useState<Model | null>(null);
  const [formData, setFormData] = useState<Model>({ name: '', brandId: 0 });
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useSnackbar();
  const { showConfirm, ConfirmDialogComponent } = useConfirmDialog();

  const { data: models = [], isLoading, error } = useQuery(
    'models',
    () => modelsApi.getAll().then(res => res.data)
  );

  const { data: brands = [] } = useQuery(
    'brands',
    () => brandsApi.getAll().then(res => res.data)
  );

  const createMutation = useMutation(
    (model: Model) => modelsApi.create(model),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('models');
        handleClose();
        showSuccess('Model başarıyla oluşturuldu!');
      },
      onError: (error: any) => {
        showError('Model oluşturulurken hata oluştu');
        console.error('Create model error:', error);
      },
    }
  );

  const updateMutation = useMutation(
    (model: Model) => modelsApi.update(model),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('models');
        handleClose();
        showSuccess('Model başarıyla güncellendi!');
      },
      onError: (error: any) => {
        showError('Model güncellenirken hata oluştu');
        console.error('Update model error:', error);
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => modelsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('models');
        showSuccess('Model başarıyla silindi!');
      },
      onError: (error: any) => {
        showError('Model silinirken hata oluştu');
        console.error('Delete model error:', error);
      },
    }
  );

  const handleOpen = (model?: Model) => {
    if (model) {
      setEditingModel(model);
      setFormData(model);
    } else {
      setEditingModel(null);
      setFormData({ name: '', brandId: 0 });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditingModel(null);
    setFormData({ name: '', brandId: 0 });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.brandId) {
      alert('Lütfen bir marka seçin');
      return;
    }
    
    if (editingModel) {
      updateMutation.mutate({ ...formData, id: editingModel.id });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number, modelName: string, brandName?: string) => {
    showConfirm(
      {
        title: 'Modeli Sil',
        message: `"${modelName}"${brandName ? ` (${brandName})` : ''} modelini silmek istediğinizden emin misiniz? Bu işlem geri alınamaz ve bu modele ait araçlar etkilenebilir.`,
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
          Model Yönetimi
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpen()}
        >
          Yeni Model
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Modeller yüklenirken hata oluştu.
        </Alert>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Model Adı</TableCell>
              <TableCell>Marka</TableCell>
              <TableCell align="center">İşlemler</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {models.map((model) => (
              <TableRow key={model.id}>
                <TableCell>{model.id}</TableCell>
                <TableCell>{model.name}</TableCell>
                <TableCell>{model.brandName || 'Bilinmiyor'}</TableCell>
                <TableCell align="center">
                  <IconButton
                    color="primary"
                    onClick={() => handleOpen(model)}
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton
                    color="error"
                    onClick={() => handleDelete(model.id!, model.name, model.brandName)}
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
          {editingModel ? 'Model Düzenle' : 'Yeni Model'}
        </DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              label="Model Adı"
              fullWidth
              variant="outlined"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
              sx={{ mb: 2 }}
            />
            <FormControl fullWidth>
              <InputLabel>Marka</InputLabel>
              <Select
                value={formData.brandId}
                label="Marka"
                onChange={(e) => setFormData({ ...formData, brandId: e.target.value as number })}
                required
              >
                {brands.map((brand) => (
                  <MenuItem key={brand.id} value={brand.id}>
                    {brand.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
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
                editingModel ? 'Güncelle' : 'Kaydet'
              )}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      
      <ConfirmDialogComponent />
    </>
  );
};

export default ModelsPage;
