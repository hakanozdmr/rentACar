import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
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
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Tooltip,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Description,
  CheckCircle,
  Pending,
  Cancel,
  Verified,
  Visibility,
  Search,
  Email,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  contractsApi, 
  rentalsApi, 
  customersApi, 
  contractTemplatesApi,
  Contract,
  Rental,
  Customer,
  ContractTemplate 
} from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs from 'dayjs';

const ContractManagementPage: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [editingContract, setEditingContract] = useState<Contract | null>(null);
  const [viewingContract, setViewingContract] = useState<Contract | null>(null);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string>('all');
  const [tabValue, setTabValue] = useState(0);
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState<Partial<Contract>>({
    rentalId: 0,
    customerId: 0,
    templateId: 0,
    contractNumber: '',
    signedDate: dayjs().format('YYYY-MM-DD'),
    status: 'DRAFT',
    terms: '',
    conditions: '',
    notes: '',
  });

  // Fetch contracts
  const { data: contracts = [], isLoading: contractsLoading } = useQuery(
    ['contracts', filterStatus],
    () => {
      if (filterStatus === 'all') {
        return contractsApi.getAll().then(res => res.data);
      }
      return contractsApi.getByStatus(filterStatus.toUpperCase() as any).then(res => res.data);
    }
  );

  // Fetch data
  const { data: rentals = [] } = useQuery(
    'rentals',
    () => rentalsApi.getAll().then(res => res.data)
  );

  const { data: customers = [] } = useQuery(
    'customers',
    () => customersApi.getAll().then(res => res.data)
  );

  const { data: templates = [] } = useQuery(
    'contract-templates',
    () => contractTemplatesApi.getAll().then(res => res.data)
  );

  // Mutations
  const createMutation = useMutation(
    (contract: Contract) => contractsApi.create(contract),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['contracts']);
        showSuccess('Sözleşme başarıyla oluşturuldu');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Sözleşme oluşturulurken hata oluştu');
      },
    }
  );

  const updateMutation = useMutation(
    (contract: Contract) => contractsApi.update(contract),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['contracts']);
        showSuccess('Sözleşme başarıyla güncellendi');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Sözleşme güncellenirken hata oluştu');
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => contractsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['contracts']);
        showSuccess('Sözleşme başarıyla silindi');
      },
      onError: (error: any) => {
        showError(error.message || 'Sözleşme silinirken hata oluştu');
      },
    }
  );

  const signMutation = useMutation(
    ({ id, customerSignature, companySignature }: { id: number, customerSignature: string, companySignature: string }) =>
      contractsApi.signContract(id, customerSignature, companySignature),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['contracts']);
        showSuccess('Sözleşme başarıyla imzalandı');
      },
      onError: (error: any) => {
        showError(error.message || 'Sözleşme imzalanırken hata oluştu');
      },
    }
  );

  // Handlers
  const handleOpenDialog = () => {
    setFormData({
      rentalId: 0,
      customerId: 0,
      templateId: 0,
      contractNumber: '',
      signedDate: dayjs().format('YYYY-MM-DD'),
      status: 'DRAFT',
      terms: '',
      conditions: '',
      notes: '',
    });
    setEditingContract(null);
    setOpenDialog(true);
  };

  const handleEditContract = (contract: Contract) => {
    setFormData(contract);
    setEditingContract(contract);
    setOpenDialog(true);
  };

  const handleViewContract = (contract: Contract) => {
    setViewingContract(contract);
    setOpenViewDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingContract(null);
    setFormData({
      rentalId: 0,
      customerId: 0,
      templateId: 0,
      contractNumber: '',
      signedDate: dayjs().format('YYYY-MM-DD'),
      status: 'DRAFT',
      terms: '',
      conditions: '',
      notes: '',
    });
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setViewingContract(null);
  };

  const handleSubmit = () => {
    if (editingContract) {
      updateMutation.mutate(formData as Contract);
    } else {
      createMutation.mutate(formData as Contract);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Bu sözleşmeyi silmek istediğinizden emin misiniz?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleSign = (contract: Contract) => {
    // In a real application, you would get signatures from user input
    signMutation.mutate({
      id: contract.id!,
      customerSignature: 'customer-signature-base64',
      companySignature: 'company-signature-base64',
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'default';
      case 'PENDING_SIGNATURE':
        return 'warning';
      case 'SIGNED':
        return 'success';
      case 'VERIFIED':
        return 'info';
      case 'EXPIRED':
        return 'error';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'DRAFT':
        return 'Taslak';
      case 'PENDING_SIGNATURE':
        return 'İmza Bekliyor';
      case 'SIGNED':
        return 'İmzalandı';
      case 'VERIFIED':
        return 'Doğrulandı';
      case 'EXPIRED':
        return 'Süresi Doldu';
      case 'CANCELLED':
        return 'İptal Edildi';
      default:
        return status;
    }
  };

  // Statistics
  const stats = {
    total: contracts.length,
    signed: contracts.filter(c => c.status === 'SIGNED').length,
    pending: contracts.filter(c => c.status === 'PENDING_SIGNATURE').length,
    expired: contracts.filter(c => c.status === 'EXPIRED').length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Sözleşme Yönetimi
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenDialog}
        >
          Yeni Sözleşme
        </Button>
      </Box>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Toplam Sözleşme
              </Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                İmzalanan
              </Typography>
              <Typography variant="h4" color="success.main">
                {stats.signed}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                İmza Bekliyor
              </Typography>
              <Typography variant="h4" color="warning.main">
                {stats.pending}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Süresi Dolan
              </Typography>
              <Typography variant="h4" color="error.main">
                {stats.expired}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(_, newValue) => {
            setTabValue(newValue);
            const statusMap = ['all', 'PENDING_SIGNATURE', 'SIGNED', 'EXPIRED'];
            setFilterStatus(statusMap[newValue]);
          }}
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab label="Tümü" />
          <Tab label="İmza Bekliyor" />
          <Tab label="İmzalanan" />
          <Tab label="Süresi Dolan" />
        </Tabs>
      </Paper>

      {/* Table */}
      {contractsLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Sözleşme No</TableCell>
                <TableCell>Müşteri</TableCell>
                <TableCell>Kiralama</TableCell>
                <TableCell>İmza Tarihi</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {contracts.map((contract) => (
                <TableRow key={contract.id}>
                  <TableCell>
                    <Typography variant="body2" fontWeight="bold">
                      {contract.contractNumber}
                    </Typography>
                  </TableCell>
                  <TableCell>{contract.customerName || 'N/A'}</TableCell>
                  <TableCell>{contract.rentalInfo || `#${contract.rentalId}`}</TableCell>
                  <TableCell>{dayjs(contract.signedDate).format('DD.MM.YYYY')}</TableCell>
                  <TableCell>
                    <Chip
                      label={getStatusText(contract.status)}
                      color={getStatusColor(contract.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Tooltip title="Görüntüle">
                      <IconButton size="small" onClick={() => handleViewContract(contract)}>
                        <Visibility />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Düzenle">
                      <IconButton size="small" onClick={() => handleEditContract(contract)}>
                        <Edit />
                      </IconButton>
                    </Tooltip>
                    {contract.status === 'PENDING_SIGNATURE' && (
                      <Tooltip title="İmzala">
                        <IconButton size="small" onClick={() => handleSign(contract)}>
                          <CheckCircle />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Sil">
                      <IconButton size="small" onClick={() => handleDelete(contract.id!)}>
                        <Delete />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingContract ? 'Sözleşme Düzenle' : 'Yeni Sözleşme'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>Kiralama</InputLabel>
                <Select
                  value={formData.rentalId}
                  label="Kiralama"
                  onChange={(e) => setFormData({ ...formData, rentalId: Number(e.target.value) })}
                >
                  {rentals.map((rental) => (
                    <MenuItem key={rental.id} value={rental.id}>
                      #{rental.id} - {rental.carPlate}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>Müşteri</InputLabel>
                <Select
                  value={formData.customerId}
                  label="Müşteri"
                  onChange={(e) => setFormData({ ...formData, customerId: Number(e.target.value) })}
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
              <FormControl fullWidth>
                <InputLabel>Şablon</InputLabel>
                <Select
                  value={formData.templateId}
                  label="Şablon"
                  onChange={(e) => setFormData({ ...formData, templateId: Number(e.target.value) })}
                >
                  {templates.map((template) => (
                    <MenuItem key={template.id} value={template.id}>
                      {template.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Sözleşme No"
                value={formData.contractNumber}
                onChange={(e) => setFormData({ ...formData, contractNumber: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="date"
                label="İmza Tarihi"
                value={formData.signedDate}
                onChange={(e) => setFormData({ ...formData, signedDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Durum</InputLabel>
                <Select
                  value={formData.status}
                  label="Durum"
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                >
                  <MenuItem value="DRAFT">Taslak</MenuItem>
                  <MenuItem value="PENDING_SIGNATURE">İmza Bekliyor</MenuItem>
                  <MenuItem value="SIGNED">İmzalandı</MenuItem>
                  <MenuItem value="VERIFIED">Doğrulandı</MenuItem>
                  <MenuItem value="EXPIRED">Süresi Doldu</MenuItem>
                  <MenuItem value="CANCELLED">İptal Edildi</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Şartlar"
                value={formData.terms}
                onChange={(e) => setFormData({ ...formData, terms: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Özel Koşullar"
                value={formData.conditions}
                onChange={(e) => setFormData({ ...formData, conditions: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Notlar"
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>İptal</Button>
          <Button onClick={handleSubmit} variant="contained" disabled={createMutation.isLoading}>
            {createMutation.isLoading ? <CircularProgress size={24} /> : 'Kaydet'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* View Dialog */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="md" fullWidth>
        <DialogTitle>Sözleşme Detayları</DialogTitle>
        <DialogContent>
          {viewingContract && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Sözleşme No
                  </Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {viewingContract.contractNumber}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Durum
                  </Typography>
                  <Chip
                    label={getStatusText(viewingContract.status)}
                    color={getStatusColor(viewingContract.status) as any}
                    size="small"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    İmza Tarihi
                  </Typography>
                  <Typography variant="body1">
                    {dayjs(viewingContract.signedDate).format('DD.MM.YYYY')}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Müşteri
                  </Typography>
                  <Typography variant="body1">
                    {viewingContract.customerName || 'N/A'}
                  </Typography>
                </Grid>
                {viewingContract.terms && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="textSecondary">
                      Şartlar
                    </Typography>
                    <Typography variant="body1">
                      {viewingContract.terms}
                    </Typography>
                  </Grid>
                )}
                {viewingContract.conditions && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="textSecondary">
                      Özel Koşullar
                    </Typography>
                    <Typography variant="body1">
                      {viewingContract.conditions}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseViewDialog}>Kapat</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ContractManagementPage;


