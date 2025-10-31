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
  FormControlLabel,
  Switch,
  Tooltip,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Payment as PaymentIcon,
  AttachMoney,
  CheckCircle,
  Pending,
  Warning,
  FilterList,
  Search,
  Receipt,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { paymentsApi, Payment, PaymentSummary } from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs from 'dayjs';

const PaymentsPage: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [editingPayment, setEditingPayment] = useState<Payment | null>(null);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState<Partial<Payment>>({
    rentalId: 0,
    customerId: 0,
    amount: 0,
    method: 'CREDIT_CARD',
    status: 'PENDING',
    dueDate: dayjs().add(7, 'days').format('YYYY-MM-DD'),
    notes: '',
  });

  // Fetch payments
  const { data: payments = [], isLoading: paymentsLoading } = useQuery(
    ['payments', filterStatus],
    () => {
      switch (filterStatus) {
        case 'PENDING':
          return paymentsApi.getPending().then(res => res.data);
        case 'COMPLETED':
          return paymentsApi.getCompleted().then(res => res.data);
        case 'OVERDUE':
          return paymentsApi.getOverdue().then(res => res.data);
        default:
          return paymentsApi.getAll().then(res => res.data);
      }
    }
  );

  // Fetch summary
  const { data: summary } = useQuery(
    ['payments-summary'],
    () => paymentsApi.getSummary().then(res => res.data)
  );

  // Mutations
  const createPaymentMutation = useMutation(
    (payment: Payment) => paymentsApi.create(payment),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['payments']);
        queryClient.invalidateQueries(['payments-summary']);
        showSuccess('Ödeme başarıyla oluşturuldu');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Ödeme oluşturulurken hata oluştu');
      },
    }
  );

  const updatePaymentMutation = useMutation(
    (payment: Payment) => paymentsApi.update(payment),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['payments']);
        queryClient.invalidateQueries(['payments-summary']);
        showSuccess('Ödeme başarıyla güncellendi');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Ödeme güncellenirken hata oluştu');
      },
    }
  );

  const markCompletedMutation = useMutation(
    (id: number) => paymentsApi.markAsCompleted(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['payments']);
        queryClient.invalidateQueries(['payments-summary']);
        showSuccess('Ödeme tamamlandı olarak işaretlendi');
      },
      onError: (error: any) => {
        showError(error.message || 'Ödeme işaretlenirken hata oluştu');
      },
    }
  );

  const deletePaymentMutation = useMutation(
    (id: number) => paymentsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['payments']);
        queryClient.invalidateQueries(['payments-summary']);
        showSuccess('Ödeme silindi');
      },
      onError: (error: any) => {
        showError(error.message || 'Ödeme silinirken hata oluştu');
      },
    }
  );

  const handleOpenDialog = (payment?: Payment) => {
    if (payment) {
      setEditingPayment(payment);
      setFormData({
        ...payment,
        dueDate: payment.dueDate ? dayjs(payment.dueDate).format('YYYY-MM-DD') : '',
      });
    } else {
      setEditingPayment(null);
      setFormData({
        rentalId: 0,
        customerId: 0,
        amount: 0,
        method: 'CREDIT_CARD',
        status: 'PENDING',
        dueDate: dayjs().add(7, 'days').format('YYYY-MM-DD'),
        notes: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingPayment(null);
    setFormData({
      rentalId: 0,
      customerId: 0,
      amount: 0,
      method: 'CREDIT_CARD',
      status: 'PENDING',
      dueDate: dayjs().add(7, 'days').format('YYYY-MM-DD'),
      notes: '',
    });
  };

  const handleSubmit = () => {
    if (!formData.rentalId || !formData.customerId || !formData.amount) {
      showError('Lütfen tüm zorunlu alanları doldurun');
      return;
    }

    const paymentData: Payment = {
      ...formData,
      rentalId: Number(formData.rentalId),
      customerId: Number(formData.customerId),
      amount: Number(formData.amount),
      dueDate: formData.dueDate ? dayjs(formData.dueDate).toISOString() : undefined,
    } as Payment;

    if (editingPayment) {
      updatePaymentMutation.mutate({ ...paymentData, id: editingPayment.id });
    } else {
      createPaymentMutation.mutate(paymentData);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'FAILED':
      case 'CANCELLED':
        return 'error';
      case 'OVERDUE':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle />;
      case 'PENDING':
        return <Pending />;
      case 'FAILED':
      case 'CANCELLED':
        return <Warning />;
      default:
        return <PaymentIcon />;
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY',
    }).format(amount);
  };

  // Filter payments based on search term
  const filteredPayments = payments.filter((payment) => {
    const searchLower = searchTerm.toLowerCase();
    return (
      payment.customerName?.toLowerCase().includes(searchLower) ||
      payment.carPlate?.toLowerCase().includes(searchLower) ||
      payment.transactionId?.toLowerCase().includes(searchLower) ||
      payment.id?.toString().includes(searchTerm)
    );
  });

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          Ödeme Yönetimi
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => handleOpenDialog()}
        >
          Yeni Ödeme
        </Button>
      </Box>

      {/* Summary Cards */}
      {summary && (
        <Grid container spacing={3} mb={3}>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <AttachMoney color="primary" sx={{ mr: 1 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      Toplam Tutar
                    </Typography>
                    <Typography variant="h6">
                      {formatCurrency(summary.totalAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <CheckCircle color="success" sx={{ mr: 1 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      Tamamlanan
                    </Typography>
                    <Typography variant="h6">
                      {formatCurrency(summary.completedAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Pending color="warning" sx={{ mr: 1 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      Bekleyen
                    </Typography>
                    <Typography variant="h6">
                      {formatCurrency(summary.pendingAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Warning color="error" sx={{ mr: 1 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      Vadesi Geçen
                    </Typography>
                    <Typography variant="h6">
                      {formatCurrency(summary.overdueAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={4}>
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
          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth>
              <InputLabel>Durum</InputLabel>
              <Select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                label="Durum"
              >
                <MenuItem value="ALL">Tümü</MenuItem>
                <MenuItem value="PENDING">Bekleyen</MenuItem>
                <MenuItem value="COMPLETED">Tamamlanan</MenuItem>
                <MenuItem value="OVERDUE">Vadesi Geçen</MenuItem>
                <MenuItem value="FAILED">Başarısız</MenuItem>
                <MenuItem value="CANCELLED">İptal Edilen</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Payments Table */}
      <Paper>
        {paymentsLoading ? (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Müşteri</TableCell>
                  <TableCell>Araç</TableCell>
                  <TableCell>Tutar</TableCell>
                  <TableCell>Yöntem</TableCell>
                  <TableCell>Durum</TableCell>
                  <TableCell>Vade Tarihi</TableCell>
                  <TableCell>İşlem Tarihi</TableCell>
                  <TableCell>İşlemler</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredPayments.map((payment) => (
                  <TableRow key={payment.id}>
                    <TableCell>{payment.id}</TableCell>
                    <TableCell>{payment.customerName || '-'}</TableCell>
                    <TableCell>
                      {payment.carBrandName && payment.carModelName && payment.carPlate
                        ? `${payment.carBrandName} ${payment.carModelName} (${payment.carPlate})`
                        : payment.carPlate || '-'}
                    </TableCell>
                    <TableCell>{formatCurrency(payment.amount)}</TableCell>
                    <TableCell>{payment.methodDisplayName || payment.method}</TableCell>
                    <TableCell>
                      <Chip
                        icon={getStatusIcon(payment.status)}
                        label={payment.statusDisplayName || payment.status}
                        color={getStatusColor(payment.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {payment.dueDate ? dayjs(payment.dueDate).format('DD.MM.YYYY') : '-'}
                    </TableCell>
                    <TableCell>
                      {payment.paidAt ? dayjs(payment.paidAt).format('DD.MM.YYYY HH:mm') : '-'}
                    </TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        <Tooltip title="Düzenle">
                          <IconButton
                            size="small"
                            onClick={() => handleOpenDialog(payment)}
                          >
                            <Edit />
                          </IconButton>
                        </Tooltip>
                        {payment.status === 'PENDING' && (
                          <Tooltip title="Tamamlandı Olarak İşaretle">
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => payment.id && markCompletedMutation.mutate(payment.id)}
                            >
                              <CheckCircle />
                            </IconButton>
                          </Tooltip>
                        )}
                        <Tooltip title="Sil">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => payment.id && deletePaymentMutation.mutate(payment.id)}
                          >
                            <Delete />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      {/* Payment Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingPayment ? 'Ödeme Düzenle' : 'Yeni Ödeme'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Kiralama ID"
                type="number"
                value={formData.rentalId || ''}
                onChange={(e) => setFormData({ ...formData, rentalId: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Müşteri ID"
                type="number"
                value={formData.customerId || ''}
                onChange={(e) => setFormData({ ...formData, customerId: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Tutar"
                type="number"
                value={formData.amount || ''}
                onChange={(e) => setFormData({ ...formData, amount: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Ödeme Yöntemi</InputLabel>
                <Select
                  value={formData.method || 'CREDIT_CARD'}
                  onChange={(e) => setFormData({ ...formData, method: e.target.value as any })}
                  label="Ödeme Yöntemi"
                >
                  <MenuItem value="CREDIT_CARD">Kredi Kartı</MenuItem>
                  <MenuItem value="BANK_TRANSFER">Banka Havalesi</MenuItem>
                  <MenuItem value="CASH">Nakit</MenuItem>
                  <MenuItem value="CHECK">Çek</MenuItem>
                  <MenuItem value="DIGITAL_WALLET">Dijital Cüzdan</MenuItem>
                  <MenuItem value="ONLINE_BANKING">Online Banking</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Durum</InputLabel>
                <Select
                  value={formData.status || 'PENDING'}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  label="Durum"
                >
                  <MenuItem value="PENDING">Beklemede</MenuItem>
                  <MenuItem value="COMPLETED">Tamamlandı</MenuItem>
                  <MenuItem value="FAILED">Başarısız</MenuItem>
                  <MenuItem value="CANCELLED">İptal Edildi</MenuItem>
                  <MenuItem value="REFUNDED">İade Edildi</MenuItem>
                  <MenuItem value="PARTIAL">Kısmi Ödeme</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Vade Tarihi"
                type="date"
                value={formData.dueDate || ''}
                onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="İşlem ID"
                value={formData.transactionId || ''}
                onChange={(e) => setFormData({ ...formData, transactionId: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Notlar"
                multiline
                rows={3}
                value={formData.notes || ''}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>İptal</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={createPaymentMutation.isLoading || updatePaymentMutation.isLoading}
          >
            {(createPaymentMutation.isLoading || updatePaymentMutation.isLoading) ? (
              <CircularProgress size={20} />
            ) : editingPayment ? (
              'Güncelle'
            ) : (
              'Oluştur'
            )}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PaymentsPage;
