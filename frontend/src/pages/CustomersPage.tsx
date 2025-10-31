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
  Grid,
  InputAdornment,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { customersApi, Customer } from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import { useConfirmDialog } from '../hooks/useConfirmDialog';
import dayjs from 'dayjs';

interface CustomerFormData {
  firstName: string;
  lastName: string;
  street: string;
  zipcode: string;
  city: string;
  phone: string;
  email: string;
  dateOfBirth: string;
  idNumber: string;
  driverLicenseNumber: string;
}

const CustomersPage: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState<CustomerFormData>({
    firstName: '',
    lastName: '',
    street: '',
    zipcode: '',
    city: '',
    phone: '',
    email: '',
    dateOfBirth: '',
    idNumber: '',
    driverLicenseNumber: '',
  });
  const [searchTerm, setSearchTerm] = useState('');
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useSnackbar();
  const { showConfirm, ConfirmDialogComponent } = useConfirmDialog();

  const { data: customers = [], isLoading, error } = useQuery(
    'customers',
    () => customersApi.getAll().then(res => res.data)
  );

  const createMutation = useMutation(
    (customer: Customer) => customersApi.create(customer as Customer),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('customers');
        handleClose();
        showSuccess('Müşteri başarıyla oluşturuldu!');
      },
      onError: (error: any) => {
        showError('Müşteri oluşturulurken hata oluştu');
        console.error('Create customer error:', error);
      },
    }
  );

  const updateMutation = useMutation(
    (customer: Customer) => customersApi.update(customer),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('customers');
        handleClose();
        showSuccess('Müşteri başarıyla güncellendi!');
      },
      onError: (error: any) => {
        showError('Müşteri güncellenirken hata oluştu');
        console.error('Update customer error:', error);
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => customersApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('customers');
        showSuccess('Müşteri başarıyla silindi!');
      },
      onError: (error: any) => {
        showError('Müşteri silinirken hata oluştu');
        console.error('Delete customer error:', error);
      },
    }
  );

  const handleOpen = (customer?: Customer) => {
    if (customer) {
      setEditingCustomer(customer);
      setFormData({
        firstName: customer.firstName || '',
        lastName: customer.lastName || '',
        street: customer.street || '',
        zipcode: customer.zipcode?.toString() || '',
        city: customer.city || '',
        phone: customer.phone || '',
        email: customer.email || '',
        dateOfBirth: customer.dateOfBirth || '',
        idNumber: customer.idNumber || '',
        driverLicenseNumber: customer.driverLicenseNumber || '',
      });
    } else {
      setEditingCustomer(null);
      setFormData({
        firstName: '',
        lastName: '',
        street: '',
        zipcode: '',
        city: '',
        phone: '',
        email: '',
        dateOfBirth: '',
        idNumber: '',
        driverLicenseNumber: '',
      });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditingCustomer(null);
    setFormData({
      firstName: '',
      lastName: '',
      street: '',
      zipcode: '',
      city: '',
      phone: '',
      email: '',
      dateOfBirth: '',
      idNumber: '',
      driverLicenseNumber: '',
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    const customerData: Customer = {
      ...formData,
      zipcode: Number(formData.zipcode),
    } as Customer;

    if (editingCustomer) {
      updateMutation.mutate({ ...customerData, id: editingCustomer.id });
    } else {
      createMutation.mutate(customerData);
    }
  };

  const handleDelete = (id: number, firstName: string, lastName: string, email: string) => {
    showConfirm(
      {
        title: 'Müşteriyi Sil',
        message: `"${firstName} ${lastName}" (${email}) müşterisini silmek istediğinizden emin misiniz? Bu işlem geri alınamaz ve müşteriyle ilgili tüm kayıtlar silinecektir.`,
        confirmText: 'Sil',
        cancelText: 'İptal',
        severity: 'error',
      },
      () => {
        deleteMutation.mutate(id);
      }
    );
  };

  const filteredCustomers = customers.filter(customer =>
    customer.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    customer.idNumber.includes(searchTerm)
  );

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
            Müşteri Yönetimi
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpen()}
          >
            Yeni Müşteri
          </Button>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            Müşteriler yüklenirken hata oluştu.
          </Alert>
        )}

        <Box mb={3}>
          <TextField
            placeholder="Müşteri ara..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ minWidth: 300 }}
          />
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Ad Soyad</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Telefon</TableCell>
                <TableCell>Şehir</TableCell>
                <TableCell>TC No</TableCell>
                <TableCell align="center">İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredCustomers.map((customer) => (
                <TableRow key={customer.id}>
                  <TableCell>{customer.id}</TableCell>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      {customer.firstName} {customer.lastName}
                    </Typography>
                  </TableCell>
                  <TableCell>{customer.email}</TableCell>
                  <TableCell>{customer.phone}</TableCell>
                  <TableCell>{customer.city}</TableCell>
                  <TableCell>{customer.idNumber}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      color="primary"
                      onClick={() => handleOpen(customer)}
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(customer.id!, customer.firstName, customer.lastName, customer.email)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
          <DialogTitle>
            {editingCustomer ? 'Müşteri Düzenle' : 'Yeni Müşteri'}
          </DialogTitle>
          <form onSubmit={handleSubmit}>
            <DialogContent>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    autoFocus
                    margin="dense"
                    label="Ad"
                    fullWidth
                    variant="outlined"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    margin="dense"
                    label="Soyad"
                    fullWidth
                    variant="outlined"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    margin="dense"
                    label="Email"
                    type="email"
                    fullWidth
                    variant="outlined"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    margin="dense"
                    label="Telefon"
                    fullWidth
                    variant="outlined"
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    margin="dense"
                    label="TC Kimlik No"
                    fullWidth
                    variant="outlined"
                    value={formData.idNumber}
                    onChange={(e) => setFormData({ ...formData, idNumber: e.target.value })}
                    required
                    inputProps={{ maxLength: 11 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    margin="dense"
                    label="Ehliyet No"
                    fullWidth
                    variant="outlined"
                    value={formData.driverLicenseNumber}
                    onChange={(e) => setFormData({ ...formData, driverLicenseNumber: e.target.value })}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <DatePicker
                    label="Doğum Tarihi"
                    value={formData.dateOfBirth ? dayjs(formData.dateOfBirth) : null}
                    onChange={(date) => setFormData({ 
                      ...formData, 
                      dateOfBirth: date?.format('YYYY-MM-DD') || '' 
                    })}
                    slotProps={{
                      textField: {
                        fullWidth: true,
                        margin: 'dense' as const,
                        required: true,
                      },
                    }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    margin="dense"
                    label="Adres"
                    fullWidth
                    variant="outlined"
                    value={formData.street}
                    onChange={(e) => setFormData({ ...formData, street: e.target.value })}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={3}>
                  <TextField
                    margin="dense"
                    label="Posta Kodu"
                    type="number"
                    fullWidth
                    variant="outlined"
                    value={formData.zipcode}
                    onChange={(e) => setFormData({ ...formData, zipcode: e.target.value })}
                    required
                    inputProps={{ min: 10000, max: 99999 }}
                  />
                </Grid>
                <Grid item xs={12} sm={3}>
                  <TextField
                    margin="dense"
                    label="Şehir"
                    fullWidth
                    variant="outlined"
                    value={formData.city}
                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                    required
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
                  editingCustomer ? 'Güncelle' : 'Kaydet'
                )}
              </Button>
            </DialogActions>
          </form>
        </Dialog>
        
        <ConfirmDialogComponent />
      </>
    </LocalizationProvider>
  );
};

export default CustomersPage;
