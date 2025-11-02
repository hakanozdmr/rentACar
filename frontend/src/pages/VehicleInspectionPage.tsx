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
  Checkbox,
  FormControlLabel,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  CheckCircle,
  LocalGasStation,
  Speed as SpeedIcon,
  Build,
  Compare,
  Visibility,
  Search,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  vehicleConditionChecksApi,
  rentalsApi,
  carsApi,
  VehicleConditionCheck,
  Rental,
  Car 
} from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs from 'dayjs';

const VehicleInspectionPage: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [comparisonDialog, setComparisonDialog] = useState(false);
  const [editingCheck, setEditingCheck] = useState<VehicleConditionCheck | null>(null);
  const [viewingCheck, setViewingCheck] = useState<VehicleConditionCheck | null>(null);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [filterType, setFilterType] = useState<string>('all');
  const [tabValue, setTabValue] = useState(0);
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState<Partial<VehicleConditionCheck>>({
    rentalId: 0,
    carId: 0,
    checkType: 'TESLIM',
    mileageAtCheck: 0,
    fuelLevel: 0,
    bodyHasDamage: false,
    bodyDamageDescription: '',
    interiorHasDamage: false,
    interiorDamageDescription: '',
    windowsHaveDamage: false,
    windowsDamageDescription: '',
    tiresHaveDamage: false,
    tiresDamageDescription: '',
    hasScratches: false,
    scratchesDescription: '',
    damageCost: 0,
    performedBy: '',
    performedAt: dayjs().format('YYYY-MM-DDTHH:mm'),
    customerNote: '',
    staffNote: '',
    isConfirmed: false,
    needsMaintenance: false,
    maintenanceNote: '',
  });

  // Fetch checks
  const { data: checks = [], isLoading: checksLoading } = useQuery(
    ['vehicle-condition-checks', filterType],
    () => {
      if (filterType === 'all') {
        return vehicleConditionChecksApi.getAll().then(res => res.data);
      }
      return vehicleConditionChecksApi.getByCheckType(filterType).then(res => res.data);
    }
  );

  // Fetch data
  const { data: rentals = [] } = useQuery(
    'rentals',
    () => rentalsApi.getAll().then(res => res.data)
  );

  const { data: cars = [] } = useQuery(
    'cars',
    () => carsApi.getAll().then(res => res.data)
  );

  // Mutations
  const createMutation = useMutation(
    (check: VehicleConditionCheck) => vehicleConditionChecksApi.create(check),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['vehicle-condition-checks']);
        showSuccess('Kontrol başarıyla oluşturuldu');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Kontrol oluşturulurken hata oluştu');
      },
    }
  );

  const updateMutation = useMutation(
    (check: VehicleConditionCheck) => vehicleConditionChecksApi.update(check),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['vehicle-condition-checks']);
        showSuccess('Kontrol başarıyla güncellendi');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Kontrol güncellenirken hata oluştu');
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => vehicleConditionChecksApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['vehicle-condition-checks']);
        showSuccess('Kontrol başarıyla silindi');
      },
      onError: (error: any) => {
        showError(error.message || 'Kontrol silinirken hata oluştu');
      },
    }
  );

  const confirmMutation = useMutation(
    (id: number) => vehicleConditionChecksApi.confirmByCustomer(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['vehicle-condition-checks']);
        showSuccess('Kontrol müşteri tarafından onaylandı');
      },
      onError: (error: any) => {
        showError(error.message || 'Kontrol onaylanırken hata oluştu');
      },
    }
  );

  // Comparison query
  const [comparisonData, setComparisonData] = useState<VehicleConditionCheck | null>(null);
  const [selectedRentalId, setSelectedRentalId] = useState<number | null>(null);
  
  const { data: comparisonResult, refetch: refetchComparison } = useQuery(
    ['comparison', selectedRentalId],
    () => vehicleConditionChecksApi.compareDeliveryAndPickup(selectedRentalId!),
    {
      enabled: false,
    }
  );

  // Handlers
  const handleOpenDialog = () => {
    setFormData({
      rentalId: 0,
      carId: 0,
      checkType: 'TESLIM',
      mileageAtCheck: 0,
      fuelLevel: 0,
      bodyHasDamage: false,
      bodyDamageDescription: '',
      interiorHasDamage: false,
      interiorDamageDescription: '',
      windowsHaveDamage: false,
      windowsDamageDescription: '',
      tiresHaveDamage: false,
      tiresDamageDescription: '',
      hasScratches: false,
      scratchesDescription: '',
      damageCost: 0,
      performedBy: '',
      performedAt: dayjs().format('YYYY-MM-DDTHH:mm'),
      customerNote: '',
      staffNote: '',
      isConfirmed: false,
      needsMaintenance: false,
      maintenanceNote: '',
    });
    setEditingCheck(null);
    setOpenDialog(true);
  };

  const handleEditCheck = (check: VehicleConditionCheck) => {
    setFormData(check);
    setEditingCheck(check);
    setOpenDialog(true);
  };

  const handleViewCheck = (check: VehicleConditionCheck) => {
    setViewingCheck(check);
    setOpenViewDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingCheck(null);
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setViewingCheck(null);
  };

  const handleSubmit = () => {
    if (editingCheck) {
      updateMutation.mutate(formData as VehicleConditionCheck);
    } else {
      createMutation.mutate(formData as VehicleConditionCheck);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Bu kontrolü silmek istediğinizden emin misiniz?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleConfirm = (check: VehicleConditionCheck) => {
    confirmMutation.mutate(check.id!);
  };

  const handleCompare = () => {
    if (selectedRentalId) {
      refetchComparison();
      setComparisonDialog(true);
    }
  };

  const getTypeColor = (type: string) => {
    return type === 'TESLIM' ? 'primary' : 'success';
  };

  const getTypeText = (type: string) => {
    return type === 'TESLIM' ? 'Teslim' : 'Teslim Alma';
  };

  // Statistics
  const stats = {
    total: checks.length,
    deliveries: checks.filter(c => c.checkType === 'TESLIM').length,
    pickups: checks.filter(c => c.checkType === 'TESLIM_ALMA').length,
    damages: checks.filter(c => c.bodyHasDamage || c.interiorHasDamage || c.hasScratches).length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Araç Durum Kontrolü
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<Compare />}
            onClick={() => setComparisonDialog(true)}
          >
            Karşılaştır
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={handleOpenDialog}
          >
            Yeni Kontrol
          </Button>
        </Box>
      </Box>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Toplam Kontrol
              </Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Teslim
              </Typography>
              <Typography variant="h4" color="primary.main">
                {stats.deliveries}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Teslim Alma
              </Typography>
              <Typography variant="h4" color="success.main">
                {stats.pickups}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Hasar Tespit Edilen
              </Typography>
              <Typography variant="h4" color="error.main">
                {stats.damages}
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
            const typeMap = ['all', 'TESLIM', 'TESLIM_ALMA'];
            setFilterType(typeMap[newValue]);
          }}
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab label="Tümü" />
          <Tab label="Teslim" />
          <Tab label="Teslim Alma" />
        </Tabs>
      </Paper>

      {/* Table */}
      {checksLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Kiralama</TableCell>
                <TableCell>Araç</TableCell>
                <TableCell>Tip</TableCell>
                <TableCell>Kilometre</TableCell>
                <TableCell>Yakıt</TableCell>
                <TableCell>Hasar</TableCell>
                <TableCell>Kontrol Eden</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {checks.map((check) => (
                <TableRow key={check.id}>
                  <TableCell>
                    {check.rentalInfo || `#${check.rentalId}`}
                  </TableCell>
                  <TableCell>{check.carPlate || `#${check.carId}`}</TableCell>
                  <TableCell>
                    <Chip
                      label={getTypeText(check.checkType)}
                      color={getTypeColor(check.checkType) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <SpeedIcon fontSize="small" />
                      {check.mileageAtCheck.toLocaleString('tr-TR')} km
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <LocalGasStation fontSize="small" />
                      {check.fuelLevel}%
                    </Box>
                  </TableCell>
                  <TableCell>
                    {(check.bodyHasDamage || check.interiorHasDamage || check.hasScratches) ? (
                      <Chip label="Var" color="error" size="small" />
                    ) : (
                      <Chip label="Yok" color="success" size="small" />
                    )}
                  </TableCell>
                  <TableCell>{check.performedBy}</TableCell>
                  <TableCell>
                    <Tooltip title="Görüntüle">
                      <IconButton size="small" onClick={() => handleViewCheck(check)}>
                        <Visibility />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Düzenle">
                      <IconButton size="small" onClick={() => handleEditCheck(check)}>
                        <Edit />
                      </IconButton>
                    </Tooltip>
                    {!check.isConfirmed && (
                      <Tooltip title="Onayla">
                        <IconButton size="small" onClick={() => handleConfirm(check)}>
                          <CheckCircle />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Sil">
                      <IconButton size="small" onClick={() => handleDelete(check.id!)}>
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
          {editingCheck ? 'Kontrol Düzenle' : 'Yeni Kontrol'}
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
                <InputLabel>Araç</InputLabel>
                <Select
                  value={formData.carId}
                  label="Araç"
                  onChange={(e) => setFormData({ ...formData, carId: Number(e.target.value) })}
                >
                  {cars.map((car) => (
                    <MenuItem key={car.id} value={car.id}>
                      {car.plate}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>Kontrol Tipi</InputLabel>
                <Select
                  value={formData.checkType}
                  label="Kontrol Tipi"
                  onChange={(e) => setFormData({ ...formData, checkType: e.target.value as any })}
                >
                  <MenuItem value="TESLIM">Teslim</MenuItem>
                  <MenuItem value="TESLIM_ALMA">Teslim Alma</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="Kilometre"
                value={formData.mileageAtCheck}
                onChange={(e) => setFormData({ ...formData, mileageAtCheck: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="Yakıt Seviyesi (%)"
                value={formData.fuelLevel}
                onChange={(e) => setFormData({ ...formData, fuelLevel: Number(e.target.value) })}
                inputProps={{ min: 0, max: 100 }}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Kontrol Eden"
                value={formData.performedBy}
                onChange={(e) => setFormData({ ...formData, performedBy: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                type="datetime-local"
                label="Kontrol Zamanı"
                value={formData.performedAt}
                onChange={(e) => setFormData({ ...formData, performedAt: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Hasar Durumu
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.bodyHasDamage}
                    onChange={(e) => setFormData({ ...formData, bodyHasDamage: e.target.checked })}
                  />
                }
                label="Gövde Hasarı"
              />
            </Grid>
            {formData.bodyHasDamage && (
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Gövde Hasarı Açıklaması"
                  value={formData.bodyDamageDescription}
                  onChange={(e) => setFormData({ ...formData, bodyDamageDescription: e.target.value })}
                />
              </Grid>
            )}

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.interiorHasDamage}
                    onChange={(e) => setFormData({ ...formData, interiorHasDamage: e.target.checked })}
                  />
                }
                label="İç Mekan Hasarı"
              />
            </Grid>
            {formData.interiorHasDamage && (
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="İç Mekan Hasarı Açıklaması"
                  value={formData.interiorDamageDescription}
                  onChange={(e) => setFormData({ ...formData, interiorDamageDescription: e.target.value })}
                />
              </Grid>
            )}

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.hasScratches}
                    onChange={(e) => setFormData({ ...formData, hasScratches: e.target.checked })}
                  />
                }
                label="Çizik"
              />
            </Grid>
            {formData.hasScratches && (
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Çizik Açıklaması"
                  value={formData.scratchesDescription}
                  onChange={(e) => setFormData({ ...formData, scratchesDescription: e.target.value })}
                />
              </Grid>
            )}

            {formData.damageCost && formData.damageCost > 0 && (
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  type="number"
                  label="Hasar Maliyeti (TL)"
                  value={formData.damageCost}
                  onChange={(e) => setFormData({ ...formData, damageCost: Number(e.target.value) })}
                />
              </Grid>
            )}

            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Müşteri Notu"
                value={formData.customerNote}
                onChange={(e) => setFormData({ ...formData, customerNote: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={2}
                label="Personel Notu"
                value={formData.staffNote}
                onChange={(e) => setFormData({ ...formData, staffNote: e.target.value })}
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

      {/* Comparison Dialog */}
      <Dialog open={comparisonDialog} onClose={() => setComparisonDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>Kontrolleri Karşılaştır</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 2 }}>
            <Grid item xs={12}>
              <FormControl fullWidth required>
                <InputLabel>Kiralama Seçin</InputLabel>
                <Select
                  value={selectedRentalId || ''}
                  label="Kiralama Seçin"
                  onChange={(e) => setSelectedRentalId(Number(e.target.value))}
                >
                  {rentals.map((rental) => (
                    <MenuItem key={rental.id} value={rental.id}>
                      #{rental.id} - {rental.carPlate}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            {comparisonResult && comparisonResult.data && (
              <Grid item xs={12}>
                <Alert severity="info" sx={{ mt: 2 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    Karşılaştırma Sonuçları
                  </Typography>
                  <Typography variant="body2">
                    {comparisonResult.data.staffNote || 'Fark bulunamadı'}
                  </Typography>
                  {comparisonResult.data.damageCost && comparisonResult.data.damageCost > 0 && (
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      Önerilen Hasar Maliyeti: {comparisonResult.data.damageCost} TL
                    </Typography>
                  )}
                </Alert>
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setComparisonDialog(false)}>Kapat</Button>
          <Button onClick={handleCompare} variant="contained" disabled={!selectedRentalId}>
            Karşılaştır
          </Button>
        </DialogActions>
      </Dialog>

      {/* View Dialog */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="md" fullWidth>
        <DialogTitle>Kontrol Detayları</DialogTitle>
        <DialogContent>
          {viewingCheck && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Kontrol Tipi
                  </Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {getTypeText(viewingCheck.checkType)}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Durum
                  </Typography>
                  <Chip
                    label={viewingCheck.isConfirmed ? 'Onaylandı' : 'Bekliyor'}
                    color={viewingCheck.isConfirmed ? 'success' : 'warning'}
                    size="small"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Kilometre
                  </Typography>
                  <Typography variant="body1">
                    {viewingCheck.mileageAtCheck.toLocaleString('tr-TR')} km
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Yakıt Seviyesi
                  </Typography>
                  <Typography variant="body1">
                    {viewingCheck.fuelLevel}%
                  </Typography>
                </Grid>
                {(viewingCheck.bodyHasDamage || viewingCheck.interiorHasDamage || viewingCheck.hasScratches) && (
                  <Grid item xs={12}>
                    <Alert severity="error">
                      <Typography variant="subtitle2" gutterBottom>
                        Hasar Tespit Edildi
                      </Typography>
                      {viewingCheck.bodyDamageDescription && (
                        <Typography variant="body2">
                          Gövde: {viewingCheck.bodyDamageDescription}
                        </Typography>
                      )}
                      {viewingCheck.interiorDamageDescription && (
                        <Typography variant="body2">
                          İç Mekan: {viewingCheck.interiorDamageDescription}
                        </Typography>
                      )}
                      {viewingCheck.scratchesDescription && (
                        <Typography variant="body2">
                          Çizik: {viewingCheck.scratchesDescription}
                        </Typography>
                      )}
                      {viewingCheck.damageCost && viewingCheck.damageCost > 0 && (
                        <Typography variant="body2" fontWeight="bold" sx={{ mt: 1 }}>
                          Tahmini Maliyet: {viewingCheck.damageCost} TL
                        </Typography>
                      )}
                    </Alert>
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

export default VehicleInspectionPage;


