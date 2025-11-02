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
  Chip,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  List,
  ListItem,
  ListItemText,
  Avatar,
  Tooltip,
  Switch,
  FormControlLabel,
  Checkbox,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  FilterList as FilterIcon,
  PhotoCamera as PhotoIcon,
  LocationOn as LocationIcon,
  Refresh as RefreshIcon,
  Build as MaintenanceIcon,
  Security as InsuranceIcon,
  ExpandMore as ExpandMoreIcon,
  DirectionsCar as CarIcon,
  KeyboardArrowRight as ArrowRightIcon,
  CheckCircle as CheckIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { carsApi, modelsApi, brandsApi, customerPortalApi, Car, Model, Brand, MaintenanceRecord, CarFeature, ReservationRating } from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import { useConfirmDialog } from '../hooks/useConfirmDialog';
import GoogleMap from '../components/GoogleMap';
import dayjs from 'dayjs';

const CarsPage: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [editingCar, setEditingCar] = useState<Car | null>(null);
  const [viewMode, setViewMode] = useState<'table' | 'cards'>('table');
  const [activeTab, setActiveTab] = useState(0);
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);
  const [maintenanceModalOpen, setMaintenanceModalOpen] = useState(false);
  const [insuranceModalOpen, setInsuranceModalOpen] = useState(false);
  const [modalSelectedCar, setModalSelectedCar] = useState<Car | null>(null);
  const [formData, setFormData] = useState<Car>({
    plate: '',
    dailyPrice: 0,
    modelYear: new Date().getFullYear(),
    state: 1,
    modelId: 0,
    mileage: 0,
    fuelType: 'Benzin',
    transmission: 'Manuel',
    segment: 'Ekonomi',
    color: '',
    features: [],
    images: [],
  });
  const [stateFilter, setStateFilter] = useState<number | 'all'>('all');
  const [segmentFilter, setSegmentFilter] = useState<string>('all');
  const [transmissionFilter, setTransmissionFilter] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useSnackbar();
  const { showConfirm, ConfirmDialogComponent } = useConfirmDialog();

  // Bakım kaydı form data
  const [maintenanceFormData, setMaintenanceFormData] = useState<MaintenanceRecord>({
    carId: 0,
    maintenanceDate: dayjs().format('YYYY-MM-DD'),
    type: '',
    description: '',
    cost: 0,
    mileage: 0,
    serviceProvider: '',
  });

  // Sigorta bilgisi form data
  const [insuranceFormData, setInsuranceFormData] = useState({
    insuranceCompany: '',
    insuranceExpiryDate: dayjs().add(1, 'year').format('YYYY-MM-DD'),
  });

  // Araç segmentleri
  const carSegments = [
    { value: 'Ekonomi', label: 'Ekonomi' },
    { value: 'Komfort', label: 'Komfort' },
    { value: 'Lüks', label: 'Lüks' },
    { value: 'SUV', label: 'SUV' },
    { value: 'Van', label: 'Van' },
    { value: 'Pick-up', label: 'Pick-up' },
  ];

  // Yakıt türleri
  const fuelTypes = ['Benzin', 'Dizel', 'Hibrit', 'Elektrik'];

  // Şanzıman türleri
  const transmissionTypes = ['Manuel', 'Otomatik', 'Yarı Otomatik'];

  // Araç özellikleri
  const availableFeatures: CarFeature[] = [
    { id: 'ac', name: 'Klima', category: 'Konfor' },
    { id: 'abs', name: 'ABS', category: 'Güvenlik' },
    { id: 'airbag', name: 'Hava Yastığı', category: 'Güvenlik' },
    { id: 'bluetooth', name: 'Bluetooth', category: 'Teknoloji' },
    { id: 'gps', name: 'GPS', category: 'Teknoloji' },
    { id: 'sunroof', name: 'Açılır Tavan', category: 'Konfor' },
    { id: 'leather', name: 'Deri Döşeme', category: 'Konfor' },
    { id: 'usb', name: 'USB Portu', category: 'Teknoloji' },
  ];

  const { data: cars = [], isLoading, error } = useQuery(
    'cars',
    () => carsApi.getAll().then(res => res.data)
  );

  const { data: models = [] } = useQuery(
    'models',
    () => modelsApi.getAll().then(res => res.data)
  );

  const { data: brands = [] } = useQuery(
    'brands',
    () => brandsApi.getAll().then(res => res.data)
  );

  const createMutation = useMutation(
    (car: Car) => carsApi.create(car),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('cars');
        handleClose();
        showSuccess('Araç başarıyla oluşturuldu!');
      },
      onError: (error: any) => {
        showError('Araç oluşturulurken hata oluştu');
        console.error('Create car error:', error);
      },
    }
  );

  const updateMutation = useMutation(
    (car: Car) => carsApi.update(car),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('cars');
        handleClose();
        showSuccess('Araç başarıyla güncellendi!');
      },
      onError: (error: any) => {
        showError('Araç güncellenirken hata oluştu');
        console.error('Update car error:', error);
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => carsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries('cars');
        showSuccess('Araç başarıyla silindi!');
      },
      onError: (error: any) => {
        showError('Araç silinirken hata oluştu');
        console.error('Delete car error:', error);
      },
    }
  );

  // Bakım kaydı ekleme mutation
  const addMaintenanceMutation = useMutation(
    (maintenanceRecord: MaintenanceRecord) => carsApi.addMaintenanceRecord(maintenanceRecord.carId, maintenanceRecord),
    {
      onSuccess: async (data, variables) => {
        queryClient.invalidateQueries('cars');
        
        // Seçili aracın güncel bakım geçmişini getir
        if (modalSelectedCar) {
          try {
            const updatedCarResponse = await carsApi.getById(modalSelectedCar.id!);
            const updatedCar = updatedCarResponse.data;
            
            // Eğer araç detay modal'ı açıksa, güncel veriyi set et
            if (selectedCar && selectedCar.id === modalSelectedCar.id) {
              setSelectedCar(updatedCar);
            }
          } catch (error) {
            console.error('Error fetching updated car data:', error);
          }
        }
        
        setMaintenanceModalOpen(false);
        setModalSelectedCar(null);
        setMaintenanceFormData({
          carId: 0,
          maintenanceDate: dayjs().format('YYYY-MM-DD'),
          type: '',
          description: '',
          cost: 0,
          mileage: 0,
          serviceProvider: '',
        });
        showSuccess('Bakım kaydı başarıyla eklendi!');
      },
      onError: (error: any) => {
        showError('Bakım kaydı eklenirken hata oluştu');
        console.error('Add maintenance error:', error);
      },
    }
  );

  // Sigorta bilgisi güncelleme mutation
  const updateInsuranceMutation = useMutation(
    (car: Car) => carsApi.update(car),
    {
      onSuccess: async (data, variables) => {
        queryClient.invalidateQueries('cars');
        
        // Seçili aracın güncel bilgilerini getir
        if (modalSelectedCar) {
          try {
            const updatedCarResponse = await carsApi.getById(modalSelectedCar.id!);
            const updatedCar = updatedCarResponse.data;
            
            // Eğer araç detay modal'ı açıksa, güncel veriyi set et
            if (selectedCar && selectedCar.id === modalSelectedCar.id) {
              setSelectedCar(updatedCar);
            }
          } catch (error) {
            console.error('Error fetching updated car data:', error);
          }
        }
        
        setInsuranceModalOpen(false);
        setModalSelectedCar(null);
        showSuccess('Sigorta bilgileri başarıyla güncellendi!');
      },
      onError: (error: any) => {
        showError('Sigorta bilgileri güncellenirken hata oluştu');
        console.error('Update insurance error:', error);
      },
    }
  );

  // GPS konum yenileme mutation
  const refreshLocationMutation = useMutation(
    (carId: number) => carsApi.getLocation(carId),
    {
      onSuccess: (response, carId) => {
        queryClient.invalidateQueries('cars');
        
        // Update the selected car's location if it's the same car
        if (selectedCar && selectedCar.id === carId) {
          const locationData = response.data;
          setSelectedCar(prev => prev ? {
            ...prev,
            gpsLatitude: locationData.latitude,
            gpsLongitude: locationData.longitude,
            lastLocationUpdate: locationData.lastUpdate
          } : null);
        }
        
        showSuccess('GPS konumu başarıyla güncellendi!');
      },
      onError: (error: any) => {
        showError('GPS konumu güncellenirken hata oluştu');
        console.error('Refresh location error:', error);
      },
    }
  );

  const handleRefreshLocation = (carId: number) => {
    refreshLocationMutation.mutate(carId);
  };

  const handleOpen = (car?: Car) => {
    if (car) {
      setEditingCar(car);
      setFormData({
        ...car,
        features: car.features || [],
        images: car.images || [],
      });
    } else {
      setEditingCar(null);
      setFormData({
        plate: '',
        dailyPrice: 0,
        modelYear: new Date().getFullYear(),
        state: 1,
        modelId: 0,
        mileage: 0,
        fuelType: 'Benzin',
        transmission: 'Manuel',
        segment: 'Ekonomi',
        color: '',
        features: [],
        images: [],
      });
    }
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
    setEditingCar(null);
    setFormData({
      plate: '',
      dailyPrice: 0,
      modelYear: new Date().getFullYear(),
      state: 1,
      modelId: 0,
      mileage: 0,
      fuelType: 'Benzin',
      transmission: 'Manuel',
      segment: 'Ekonomi',
      color: '',
      features: [],
      images: [],
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.modelId) {
      alert('Lütfen bir model seçin');
      return;
    }
    
    if (editingCar) {
      updateMutation.mutate({ ...formData, id: editingCar.id });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number, carPlate: string, carModel?: string) => {
    showConfirm(
      {
        title: 'Aracı Sil',
        message: `"${carPlate}"${carModel ? ` (${carModel})` : ''} plakalı aracı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz ve araçla ilgili tüm kayıtlar silinecektir.`,
        confirmText: 'Sil',
        cancelText: 'İptal',
        severity: 'error',
      },
      () => {
        deleteMutation.mutate(id);
      }
    );
  };

  // Bakım kaydı ekleme modal'ını açma
  const handleOpenMaintenanceModal = (car: Car) => {
    setModalSelectedCar(car);
    setMaintenanceFormData({
      carId: car.id!,
      maintenanceDate: dayjs().format('YYYY-MM-DD'),
      type: '',
      description: '',
      cost: 0,
      mileage: car.mileage || 0,
      serviceProvider: '',
    });
    setMaintenanceModalOpen(true);
  };

  // Sigorta bilgisi güncelleme modal'ını açma
  const handleOpenInsuranceModal = (car: Car) => {
    setModalSelectedCar(car);
    setInsuranceFormData({
      insuranceCompany: car.insuranceCompany || '',
      insuranceExpiryDate: car.insuranceExpiryDate || dayjs().add(1, 'year').format('YYYY-MM-DD'),
    });
    setInsuranceModalOpen(true);
  };

  // Bakım kaydı ekleme
  const handleSubmitMaintenance = (e: React.FormEvent) => {
    e.preventDefault();
    addMaintenanceMutation.mutate(maintenanceFormData);
  };

  // Sigorta bilgisi güncelleme
  const handleSubmitInsurance = (e: React.FormEvent) => {
    e.preventDefault();
    if (modalSelectedCar) {
      const updatedCar = {
        ...modalSelectedCar,
        insuranceCompany: insuranceFormData.insuranceCompany,
        insuranceExpiryDate: insuranceFormData.insuranceExpiryDate,
      };
      updateInsuranceMutation.mutate(updatedCar);
    }
  };

  const filteredCars = cars.filter(car => {
    const matchesState = stateFilter === 'all' || car.state === stateFilter;
    const matchesSegment = segmentFilter === 'all' || car.segment === segmentFilter;
    const matchesTransmission = transmissionFilter === 'all' || car.transmission === transmissionFilter;
    const matchesSearch = searchTerm === '' || 
      car.plate.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (car.brandName && car.brandName.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (car.modelName && car.modelName.toLowerCase().includes(searchTerm.toLowerCase()));
    return matchesState && matchesSegment && matchesTransmission && matchesSearch;
  });

  // Utility fonksiyonları
  const getStateLabel = (state: number) => {
    const states = { 1: 'Müsait', 2: 'Kiralandı', 3: 'Bakımda' };
    return states[state as keyof typeof states] || 'Bilinmiyor';
  };

  const getStateColor = (state: number): 'success' | 'error' | 'warning' | 'default' => {
    const colors: { [key: number]: 'success' | 'error' | 'warning' | 'default' } = { 
      1: 'success', 
      2: 'error', 
      3: 'warning' 
    };
    return colors[state] || 'default';
  };

  const isMaintenanceDue = (car: Car) => {
    if (!car.nextMaintenanceDate) return false;
    return dayjs(car.nextMaintenanceDate).isBefore(dayjs().add(7, 'days'));
  };

  const isInsuranceExpiring = (car: Car) => {
    if (!car.insuranceExpiryDate) return false;
    return dayjs(car.insuranceExpiryDate).isBefore(dayjs().add(30, 'days'));
  };

  const getStateChip = (state: number) => {
    const label = getStateLabel(state);
    const color = getStateColor(state);
    return <Chip label={label} color={color} size="small" />;
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
          Araç Yönetimi
        </Typography>
        <Box display="flex" gap={2}>
          <FormControlLabel
            control={
              <Switch
                checked={viewMode === 'cards'}
                onChange={(e) => setViewMode(e.target.checked ? 'cards' : 'table')}
              />
            }
            label="Kart Görünümü"
          />
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpen()}
          >
            Yeni Araç
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          Araçlar yüklenirken hata oluştu.
        </Alert>
      )}

      <Box mb={3}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={3}>
            <TextField
              fullWidth
              size="small"
              label="Ara (Plaka, Marka, Model)"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              variant="outlined"
            />
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Durum</InputLabel>
              <Select
                value={stateFilter}
                label="Durum"
                onChange={(e) => setStateFilter(e.target.value as number | 'all')}
              >
                <MenuItem value="all">Tümü</MenuItem>
                <MenuItem value={1}>Müsait</MenuItem>
                <MenuItem value={2}>Kiralandı</MenuItem>
                <MenuItem value={3}>Bakımda</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Segment</InputLabel>
              <Select
                value={segmentFilter}
                label="Segment"
                onChange={(e) => setSegmentFilter(e.target.value)}
              >
                <MenuItem value="all">Tümü</MenuItem>
                {carSegments.map((segment) => (
                  <MenuItem key={segment.value} value={segment.value}>
                    {segment.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={6} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Şanzıman</InputLabel>
              <Select
                value={transmissionFilter}
                label="Şanzıman"
                onChange={(e) => setTransmissionFilter(e.target.value)}
              >
                <MenuItem value="all">Tümü</MenuItem>
                {transmissionTypes.map((type) => (
                  <MenuItem key={type} value={type}>
                    {type}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Box>

      {viewMode === 'table' ? (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Plaka</TableCell>
                <TableCell>Model</TableCell>
                <TableCell>Marka</TableCell>
                <TableCell>Yıl</TableCell>
                <TableCell>KM</TableCell>
                <TableCell>Segment</TableCell>
                <TableCell>Günlük Fiyat</TableCell>
                <TableCell>Değerlendirme</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell align="center">İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredCars.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={10} align="center" sx={{ py: 8 }}>
                    <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
                      <Typography variant="h6" color="text.secondary">
                        {stateFilter !== 'all' || segmentFilter !== 'all' || transmissionFilter !== 'all' || searchTerm
                          ? 'Arama kriterlerinize uygun araç bulunamadı'
                          : 'Henüz araç kaydı bulunmamaktadır'}
                      </Typography>
                      {!(stateFilter !== 'all' || segmentFilter !== 'all' || transmissionFilter !== 'all' || searchTerm) && (
                        <Button
                          variant="contained"
                          color="primary"
                          startIcon={<AddIcon />}
                          onClick={() => setOpen(true)}
                        >
                          Yeni Araç Ekle
                        </Button>
                      )}
                    </Box>
                  </TableCell>
                </TableRow>
              ) : (
                filteredCars.map((car) => (
                  <TableRow key={car.id}>
                  <TableCell>{car.id}</TableCell>
                  <TableCell>
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="body2" fontWeight="bold">
                        {car.plate}
                      </Typography>
                      {isMaintenanceDue(car) && (
                        <Tooltip title="Bakım tarihi yaklaştı">
                          <WarningIcon color="warning" fontSize="small" />
                        </Tooltip>
                      )}
                      {isInsuranceExpiring(car) && (
                        <Tooltip title="Sigorta süresi dolmak üzere">
                          <ErrorIcon color="error" fontSize="small" />
                        </Tooltip>
                      )}
                    </Box>
                  </TableCell>
                  <TableCell>{car.modelName}</TableCell>
                  <TableCell>{car.brandName}</TableCell>
                  <TableCell>{car.modelYear}</TableCell>
                  <TableCell>
                    {car.mileage ? `${car.mileage.toLocaleString()} km` : '-'}
                  </TableCell>
                  <TableCell>
                    {car.segment ? (
                      <Chip label={car.segment} size="small" variant="outlined" />
                    ) : '-'}
                  </TableCell>
                  <TableCell>
                    {car.dailyPrice?.toLocaleString('tr-TR', {
                      style: 'currency',
                      currency: 'TRY'
                    })}
                  </TableCell>
                  <TableCell>
                    {car.averageRating && car.averageRating > 0 ? (
                      <Box display="flex" alignItems="center" gap={0.5}>
                        <Box display="flex" gap={0.2}>
                          {[1, 2, 3, 4, 5].map((star) => (
                            <StarIcon
                              key={star}
                              sx={{ 
                                color: star <= Math.round(car.averageRating!) ? 'orange' : 'grey.300',
                                fontSize: 14 
                              }}
                            />
                          ))}
                        </Box>
                        <Typography variant="body2">
                          {car.averageRating.toFixed(1)}
                        </Typography>
                      </Box>
                    ) : (
                      <Typography variant="body2" color="text.secondary">
                        Değerlendirme yok
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell>{getStateChip(car.state)}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      color="info"
                      onClick={() => setSelectedCar(car)}
                      size="small"
                    >
                      <ArrowRightIcon />
                    </IconButton>
                    <IconButton
                      color="primary"
                      onClick={() => handleOpen(car)}
                      size="small"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(car.id!, car.plate, car.modelName)}
                      size="small"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      ) : (
        <Grid container spacing={3}>
          {filteredCars.length === 0 ? (
            <Grid item xs={12}>
              <Box display="flex" flexDirection="column" alignItems="center" gap={2} sx={{ py: 8 }}>
                <Typography variant="h6" color="text.secondary">
                  {stateFilter !== 'all' || segmentFilter !== 'all' || transmissionFilter !== 'all' || searchTerm
                    ? 'Arama kriterlerinize uygun araç bulunamadı'
                    : 'Henüz araç kaydı bulunmamaktadır'}
                </Typography>
                {!(stateFilter !== 'all' || segmentFilter !== 'all' || transmissionFilter !== 'all' || searchTerm) && (
                  <Button
                    variant="contained"
                    color="primary"
                    startIcon={<AddIcon />}
                    onClick={() => setOpen(true)}
                  >
                    Yeni Araç Ekle
                  </Button>
                )}
              </Box>
            </Grid>
          ) : (
            filteredCars.map((car) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={car.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                {car.images && car.images.length > 0 ? (
                  <CardMedia
                    component="img"
                    height="200"
                    image={car.images[0]}
                    alt={car.plate}
                  />
                ) : (
                  <Box
                    height={200}
                    display="flex"
                    alignItems="center"
                    justifyContent="center"
                    bgcolor="grey.100"
                  >
                    <CarIcon sx={{ fontSize: 80, color: 'grey.400' }} />
                  </Box>
                )}
                <CardContent sx={{ flexGrow: 1 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="h6" component="div" fontWeight="bold">
                      {car.plate}
                    </Typography>
                    {getStateChip(car.state)}
                  </Box>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    {car.brandName} {car.modelName} ({car.modelYear})
                  </Typography>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="body2">
                      {car.segment || 'Segment'} • {car.fuelType || 'Yakıt'}
                    </Typography>
                    {car.mileage && (
                      <Typography variant="body2" color="text.secondary">
                        {car.mileage.toLocaleString()} km
                      </Typography>
                    )}
                  </Box>
                  
                  {isMaintenanceDue(car) && (
                    <Alert severity="warning" sx={{ mb: 1, fontSize: '0.75rem' }}>
                      Bakım tarihi yaklaştı
                    </Alert>
                  )}
                  {isInsuranceExpiring(car) && (
                    <Alert severity="error" sx={{ mb: 1, fontSize: '0.75rem' }}>
                      Sigorta süresi dolmak üzere
                    </Alert>
                  )}
                  
                  {/* Rating Display */}
                  {car.averageRating !== undefined && car.averageRating > 0 && (
                    <Box display="flex" alignItems="center" gap={1} mb={1}>
                      <Box display="flex" gap={0.2}>
                        {[1, 2, 3, 4, 5].map((star) => (
                          <StarIcon
                            key={star}
                            sx={{ 
                              color: star <= Math.round(car.averageRating!) ? 'orange' : 'grey.300',
                              fontSize: 16 
                            }}
                          />
                        ))}
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {car.averageRating.toFixed(1)} ({car.ratingCount || 0})
                      </Typography>
                    </Box>
                  )}
                  
                  <Typography variant="h6" color="primary" fontWeight="bold">
                    {car.dailyPrice?.toLocaleString('tr-TR', {
                      style: 'currency',
                      currency: 'TRY'
                    })}/gün
                  </Typography>
                </CardContent>
                <Box display="flex" justifyContent="space-between" p={2}>
                  <Button
                    size="small"
                    onClick={() => setSelectedCar(car)}
                    startIcon={<ArrowRightIcon />}
                  >
                    Detay
                  </Button>
                  <Box>
                    <IconButton
                      color="primary"
                      onClick={() => handleOpen(car)}
                      size="small"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      color="error"
                      onClick={() => handleDelete(car.id!, car.plate, car.modelName)}
                      size="small"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Box>
                </Box>
              </Card>
            </Grid>
          ))
          )}
        </Grid>
      )}

      <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingCar ? 'Araç Düzenle' : 'Yeni Araç'}
        </DialogTitle>
        <form onSubmit={handleSubmit}>
          <DialogContent>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <TextField
                  autoFocus
                  margin="dense"
                  label="Plaka"
                  fullWidth
                  variant="outlined"
                  value={formData.plate}
                  onChange={(e) => setFormData({ ...formData, plate: e.target.value.toUpperCase() })}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="dense"
                  label="Günlük Fiyat"
                  type="number"
                  fullWidth
                  variant="outlined"
                  value={formData.dailyPrice}
                  onChange={(e) => setFormData({ ...formData, dailyPrice: Number(e.target.value) })}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="dense"
                  label="Model Yılı"
                  type="number"
                  fullWidth
                  variant="outlined"
                  value={formData.modelYear}
                  onChange={(e) => setFormData({ ...formData, modelYear: Number(e.target.value) })}
                  required
                  inputProps={{ min: 2000, max: new Date().getFullYear() + 1 }}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth margin="dense">
                  <InputLabel>Durum</InputLabel>
                  <Select
                    value={formData.state}
                    label="Durum"
                    onChange={(e) => setFormData({ ...formData, state: e.target.value as number })}
                    required
                  >
                    <MenuItem value={1}>Müsait</MenuItem>
                    <MenuItem value={2}>Kiralandı</MenuItem>
                    <MenuItem value={3}>Bakımda</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="dense"
                  label="KM (Kilometre)"
                  type="number"
                  fullWidth
                  variant="outlined"
                  value={formData.mileage || ''}
                  onChange={(e) => setFormData({ ...formData, mileage: Number(e.target.value) })}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth margin="dense">
                  <InputLabel>Yakıt Türü</InputLabel>
                  <Select
                    value={formData.fuelType || 'Benzin'}
                    label="Yakıt Türü"
                    onChange={(e) => setFormData({ ...formData, fuelType: e.target.value })}
                  >
                    {fuelTypes.map((fuel) => (
                      <MenuItem key={fuel} value={fuel}>
                        {fuel}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth margin="dense">
                  <InputLabel>Şanzıman</InputLabel>
                  <Select
                    value={formData.transmission || 'Manuel'}
                    label="Şanzıman"
                    onChange={(e) => setFormData({ ...formData, transmission: e.target.value })}
                  >
                    {transmissionTypes.map((transmission) => (
                      <MenuItem key={transmission} value={transmission}>
                        {transmission}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth margin="dense">
                  <InputLabel>Segment</InputLabel>
                  <Select
                    value={formData.segment || 'Ekonomi'}
                    label="Segment"
                    onChange={(e) => setFormData({ ...formData, segment: e.target.value })}
                  >
                    {carSegments.map((segment) => (
                      <MenuItem key={segment.value} value={segment.value}>
                        {segment.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  margin="dense"
                  label="Renk"
                  fullWidth
                  variant="outlined"
                  value={formData.color || ''}
                  onChange={(e) => setFormData({ ...formData, color: e.target.value })}
                />
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth margin="dense">
                  <InputLabel>Model</InputLabel>
                  <Select
                    value={formData.modelId}
                    label="Model"
                    onChange={(e) => setFormData({ ...formData, modelId: e.target.value as number })}
                    required
                  >
                    {models.map((model) => (
                      <MenuItem key={model.id} value={model.id}>
                        {model.name} ({model.brandName})
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Araç Özellikleri
                </Typography>
                <Box display="flex" flexWrap="wrap" gap={1}>
                  {availableFeatures.map((feature) => (
                    <FormControlLabel
                      key={feature.id}
                      control={
                        <Checkbox
                          checked={formData.features?.includes(feature.id) || false}
                          onChange={(e) => {
                            const features = formData.features || [];
                            if (e.target.checked) {
                              setFormData({ ...formData, features: [...features, feature.id] });
                            } else {
                              setFormData({ ...formData, features: features.filter(f => f !== feature.id) });
                            }
                          }}
                        />
                      }
                      label={feature.name}
                    />
                  ))}
                </Box>
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
                editingCar ? 'Güncelle' : 'Kaydet'
              )}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Araç Detay Modal */}
      <Dialog open={!!selectedCar} onClose={() => setSelectedCar(null)} maxWidth="lg" fullWidth>
        <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <CarIcon />
          {selectedCar?.brandName} {selectedCar?.modelName} - {selectedCar?.plate}
        </DialogTitle>
        {selectedCar && (
          <DialogContent>
            <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
              <Tab label="Genel Bilgiler" />
              <Tab label="Bakım Geçmişi" />
              <Tab label="Sigorta Bilgileri" />
              <Tab label="GPS Konumu" />
              <Tab label="Değerlendirmeler" />
              <Tab label="Görseller" />
            </Tabs>

            <Box sx={{ mt: 3 }}>
              {activeTab === 0 && (
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="h6" gutterBottom>
                        Araç Bilgileri
                      </Typography>
                      <List>
                        <ListItem>
                          <ListItemText
                            primary="Plaka"
                            secondary={<Typography variant="h6">{selectedCar.plate}</Typography>}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Model & Marka"
                            secondary={`${selectedCar.brandName} ${selectedCar.modelName} (${selectedCar.modelYear})`}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Günlük Fiyat"
                            secondary={selectedCar.dailyPrice?.toLocaleString('tr-TR', {
                              style: 'currency',
                              currency: 'TRY'
                            })}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Durum"
                            secondary={getStateChip(selectedCar.state)}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="KM"
                            secondary={selectedCar.mileage ? `${selectedCar.mileage.toLocaleString()} km` : 'Belirtilmemiş'}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Yakıt Türü"
                            secondary={selectedCar.fuelType || 'Belirtilmemiş'}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Şanzıman"
                            secondary={selectedCar.transmission || 'Belirtilmemiş'}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Segment"
                            secondary={selectedCar.segment || 'Belirtilmemiş'}
                          />
                        </ListItem>
                        <ListItem>
                          <ListItemText
                            primary="Renk"
                            secondary={selectedCar.color || 'Belirtilmemiş'}
                          />
                        </ListItem>
                      </List>
                    </Paper>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="h6" gutterBottom>
                        Araç Özellikleri
                      </Typography>
                      {selectedCar.features && selectedCar.features.length > 0 ? (
                        <Box display="flex" flexWrap="wrap" gap={1}>
                          {selectedCar.features.map((featureId) => {
                            const feature = availableFeatures.find(f => f.id === featureId);
                            return feature ? (
                              <Chip key={featureId} label={feature.name} variant="outlined" />
                            ) : null;
                          })}
                        </Box>
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          Özellik bilgisi bulunmamaktadır.
                        </Typography>
                      )}
                    </Paper>
                  </Grid>
                </Grid>
              )}

              {activeTab === 1 && (
                <Paper sx={{ p: 2 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6">Bakım Geçmişi</Typography>
                    <Box display="flex" gap={2} alignItems="center">
                      {isMaintenanceDue(selectedCar) && (
                        <Alert severity="warning" sx={{ maxWidth: 300 }}>
                          <Typography variant="body2">
                            Sonraki bakım: {selectedCar.nextMaintenanceDate ? dayjs(selectedCar.nextMaintenanceDate).format('DD.MM.YYYY') : 'Belirtilmemiş'}
                          </Typography>
                        </Alert>
                      )}
                      <Button
                        variant="contained"
                        startIcon={<AddIcon />}
                        onClick={() => selectedCar && handleOpenMaintenanceModal(selectedCar)}
                        size="small"
                      >
                        Yeni Bakım Ekle
                      </Button>
                    </Box>
                  </Box>
                  
                  {selectedCar.maintenanceHistory && selectedCar.maintenanceHistory.length > 0 ? (
                    <List>
                      {selectedCar.maintenanceHistory.map((record, index) => (
                        <ListItem key={index}>
                          <ListItemText
                            primary={`${record.type} - ${dayjs(record.maintenanceDate).format('DD.MM.YYYY')}`}
                            secondary={
                              <Box>
                                <Typography variant="body2">{record.description}</Typography>
                                <Box display="flex" gap={2} mt={1}>
                                  <Chip size="small" label={`${record.cost.toLocaleString('tr-TR')} TL`} />
                                  <Chip size="small" label={`${record.mileage.toLocaleString()} km`} />
                                  <Typography variant="caption" color="text.secondary">
                                    {record.serviceProvider}
                                  </Typography>
                                </Box>
                              </Box>
                            }
                          />
                        </ListItem>
                      ))}
                    </List>
                  ) : (
                    <Typography variant="body2" color="text.secondary">
                      Bakım geçmişi bulunmamaktadır.
                    </Typography>
                  )}
                </Paper>
              )}

              {activeTab === 2 && (
                <Paper sx={{ p: 2 }}>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6">Sigorta Bilgileri</Typography>
                    <Box display="flex" gap={2} alignItems="center">
                      {isInsuranceExpiring(selectedCar) && (
                        <Alert severity="error" sx={{ maxWidth: 300 }}>
                          <Typography variant="body2">
                            Sigorta süresi: {selectedCar.insuranceExpiryDate ? dayjs(selectedCar.insuranceExpiryDate).format('DD.MM.YYYY') : 'Belirtilmemiş'}
                          </Typography>
                        </Alert>
                      )}
                      <Button
                        variant="outlined"
                        startIcon={<EditIcon />}
                        onClick={() => selectedCar && handleOpenInsuranceModal(selectedCar)}
                        size="small"
                      >
                        Sigorta Güncelle
                      </Button>
                    </Box>
                  </Box>
                  
                  <List>
                    <ListItem>
                      <ListItemText
                        primary="Sigorta Şirketi"
                        secondary={selectedCar.insuranceCompany || 'Belirtilmemiş'}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemText
                        primary="Son Geçerlilik Tarihi"
                        secondary={selectedCar.insuranceExpiryDate ? dayjs(selectedCar.insuranceExpiryDate).format('DD.MM.YYYY') : 'Belirtilmemiş'}
                      />
                    </ListItem>
                  </List>
                </Paper>
              )}

              {activeTab === 3 && (
                <Paper sx={{ p: 2 }}>
                  <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
                    <Box display="flex" alignItems="center" gap={1}>
                      <LocationIcon color="primary" />
                      <Typography variant="h6">GPS Konumu</Typography>
                    </Box>
                    {selectedCar.gpsLatitude && selectedCar.gpsLongitude && selectedCar.id && (
                      <Button
                        variant="outlined"
                        size="small"
                        startIcon={<RefreshIcon />}
                        onClick={() => handleRefreshLocation(selectedCar.id!)}
                        disabled={refreshLocationMutation.isLoading}
                        sx={{ minWidth: '120px' }}
                      >
                        {refreshLocationMutation.isLoading ? (
                          <CircularProgress size={16} />
                        ) : (
                          'Yenile'
                        )}
                      </Button>
                    )}
                  </Box>
                  {selectedCar.gpsLatitude && selectedCar.gpsLongitude ? (
                    <Box>
                      <Grid container spacing={2}>
                        <Grid item xs={12} md={4}>
                          <Box>
                            <Typography variant="subtitle2" gutterBottom>
                              Konum Bilgileri
                            </Typography>
                            <Box sx={{ mb: 2 }}>
                              <Typography variant="body2" color="text.secondary">
                                Enlem
                              </Typography>
                              <Typography variant="body1" fontWeight="medium">
                                {selectedCar.gpsLatitude.toFixed(6)}
                              </Typography>
                            </Box>
                            <Box sx={{ mb: 2 }}>
                              <Typography variant="body2" color="text.secondary">
                                Boylam
                              </Typography>
                              <Typography variant="body1" fontWeight="medium">
                                {selectedCar.gpsLongitude.toFixed(6)}
                              </Typography>
                            </Box>
                            <Box sx={{ mb: 2 }}>
                              <Typography variant="body2" color="text.secondary">
                                Son Güncelleme
                              </Typography>
                              <Typography variant="body1" fontWeight="medium">
                                {selectedCar.lastLocationUpdate ? dayjs(selectedCar.lastLocationUpdate).format('DD.MM.YYYY HH:mm') : 'Bilinmiyor'}
                              </Typography>
                            </Box>
                          </Box>
                        </Grid>
                        <Grid item xs={12} md={8}>
                          <Box sx={{ height: '400px', borderRadius: 1, overflow: 'hidden' }}>
                            <GoogleMap
                              center={{
                                lat: selectedCar.gpsLatitude,
                                lng: selectedCar.gpsLongitude
                              }}
                              zoom={15}
                              height="400px"
                              carInfo={{
                                plate: selectedCar.plate,
                                brandName: selectedCar.brandName,
                                modelName: selectedCar.modelName,
                                lastUpdate: selectedCar.lastLocationUpdate
                              }}
                            />
                          </Box>
                        </Grid>
                      </Grid>
                    </Box>
                  ) : (
                    <Box display="flex" flexDirection="column" alignItems="center" py={4}>
                      <LocationIcon sx={{ fontSize: 64, color: 'grey.400', mb: 2 }} />
                      <Typography variant="h6" color="text.secondary" gutterBottom>
                        GPS Konumu Bulunamadı
                      </Typography>
                      <Typography variant="body2" color="text.secondary" textAlign="center">
                        Bu araç için henüz GPS konum bilgisi bulunmamaktadır.
                        <br />
                        Konum bilgisi cihazdan geldiğinde haritada görüntülenecektir.
                      </Typography>
                    </Box>
                  )}
                </Paper>
              )}

              {activeTab === 4 && (
                <Paper sx={{ p: 2 }}>
                  <Box display="flex" alignItems="center" gap={1} mb={2}>
                    <StarIcon color="primary" />
                    <Typography variant="h6">Değerlendirmeler</Typography>
                  </Box>
                  {selectedCar.id && (
                    <RatingDisplay carId={selectedCar.id} />
                  )}
                </Paper>
              )}

              {activeTab === 5 && (
                <Paper sx={{ p: 2 }}>
                  <Typography variant="h6" gutterBottom>Araç Görselleri</Typography>
                  {selectedCar.images && selectedCar.images.length > 0 ? (
                    <Grid container spacing={2}>
                      {selectedCar.images.map((image, index) => (
                        <Grid item xs={12} sm={6} md={4} key={index}>
                          <Card>
                            <CardMedia
                              component="img"
                              height="200"
                              image={image}
                              alt={`${selectedCar.plate} - ${index + 1}`}
                              sx={{ objectFit: 'cover' }}
                            />
                          </Card>
                        </Grid>
                      ))}
                    </Grid>
                  ) : (
                    <Box display="flex" flexDirection="column" alignItems="center" py={4}>
                      <PhotoIcon sx={{ fontSize: 64, color: 'grey.400', mb: 2 }} />
                      <Typography variant="body2" color="text.secondary">
                        Bu araç için görsel bulunmamaktadır.
                      </Typography>
                    </Box>
                  )}
                </Paper>
              )}
            </Box>
          </DialogContent>
        )}
        <DialogActions>
          <Button onClick={() => setSelectedCar(null)}>Kapat</Button>
          {selectedCar && (
            <Button
              variant="contained"
              onClick={() => {
                setSelectedCar(null);
                handleOpen(selectedCar);
              }}
            >
              Düzenle
            </Button>
          )}
        </DialogActions>
      </Dialog>

      {/* Bakım Kaydı Ekleme Modal */}
      <Dialog 
        open={maintenanceModalOpen} 
        onClose={() => {
          setMaintenanceModalOpen(false);
          setModalSelectedCar(null);
        }} 
        maxWidth="md" 
        fullWidth
      >
        <DialogTitle>Yeni Bakım Kaydı Ekle</DialogTitle>
        <form onSubmit={handleSubmitMaintenance}>
          <DialogContent>
            <Box display="flex" flexDirection="column" gap={2} pt={2}>
              <TextField
                label="Bakım Tarihi"
                type="date"
                value={maintenanceFormData.maintenanceDate}
                onChange={(e) => setMaintenanceFormData({ ...maintenanceFormData, maintenanceDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
              
              <FormControl required>
                <InputLabel>Bakım Türü</InputLabel>
                <Select
                  value={maintenanceFormData.type}
                  onChange={(e) => setMaintenanceFormData({ ...maintenanceFormData, type: e.target.value })}
                  label="Bakım Türü"
                >
                  <MenuItem value="Periyodik">Periyodik Bakım</MenuItem>
                  <MenuItem value="Fren">Fren Bakımı</MenuItem>
                  <MenuItem value="Motor">Motor Bakımı</MenuItem>
                  <MenuItem value="Lastik">Lastik Bakımı</MenuItem>
                  <MenuItem value="Klima">Klima Bakımı</MenuItem>
                  <MenuItem value="Elektrik">Elektrik Bakımı</MenuItem>
                  <MenuItem value="Diğer">Diğer</MenuItem>
                </Select>
              </FormControl>

              <TextField
                label="Açıklama"
                multiline
                rows={3}
                value={maintenanceFormData.description}
                onChange={(e) => setMaintenanceFormData({ ...maintenanceFormData, description: e.target.value })}
                required
              />

              <TextField
                label="Maliyet (TL)"
                type="number"
                value={maintenanceFormData.cost}
                onChange={(e) => setMaintenanceFormData({ ...maintenanceFormData, cost: Number(e.target.value) })}
                required
                inputProps={{ min: 0 }}
              />

              <TextField
                label="Kilometre"
                type="number"
                value={maintenanceFormData.mileage}
                onChange={(e) => setMaintenanceFormData({ ...maintenanceFormData, mileage: Number(e.target.value) })}
                required
                inputProps={{ min: 0 }}
              />

              <TextField
                label="Servis Sağlayıcı"
                value={maintenanceFormData.serviceProvider}
                onChange={(e) => setMaintenanceFormData({ ...maintenanceFormData, serviceProvider: e.target.value })}
                required
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => {
              setMaintenanceModalOpen(false);
              setModalSelectedCar(null);
            }}>İptal</Button>
            <Button type="submit" variant="contained" disabled={addMaintenanceMutation.isLoading}>
              {addMaintenanceMutation.isLoading ? <CircularProgress size={24} /> : 'Kaydet'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      {/* Sigorta Bilgisi Güncelleme Modal */}
      <Dialog 
        open={insuranceModalOpen} 
        onClose={() => {
          setInsuranceModalOpen(false);
          setModalSelectedCar(null);
        }} 
        maxWidth="sm" 
        fullWidth
      >
        <DialogTitle>Sigorta Bilgilerini Güncelle</DialogTitle>
        <form onSubmit={handleSubmitInsurance}>
          <DialogContent>
            <Box display="flex" flexDirection="column" gap={2} pt={2}>
              <TextField
                label="Sigorta Şirketi"
                value={insuranceFormData.insuranceCompany}
                onChange={(e) => setInsuranceFormData({ ...insuranceFormData, insuranceCompany: e.target.value })}
                required
                fullWidth
              />

              <TextField
                label="Sigorta Bitiş Tarihi"
                type="date"
                value={insuranceFormData.insuranceExpiryDate}
                onChange={(e) => setInsuranceFormData({ ...insuranceFormData, insuranceExpiryDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
                fullWidth
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => {
              setInsuranceModalOpen(false);
              setModalSelectedCar(null);
            }}>İptal</Button>
            <Button type="submit" variant="contained" disabled={updateInsuranceMutation.isLoading}>
              {updateInsuranceMutation.isLoading ? <CircularProgress size={24} /> : 'Güncelle'}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      
      <ConfirmDialogComponent />
    </>
  );
};

// Rating Display Component
const RatingDisplay: React.FC<{ carId: number }> = ({ carId }) => {
  const { data: ratings = [], isLoading } = useQuery(
    ['carRatings', carId],
    () => customerPortalApi.getCarRatings(carId).then(res => res.data),
    { enabled: !!carId }
  );

  const { data: car, isLoading: carLoading } = useQuery(
    ['car', carId],
    () => carsApi.getById(carId).then(res => res.data),
    { enabled: !!carId }
  );

  if (isLoading || carLoading) {
    return (
      <Box display="flex" justifyContent="center" py={4}>
        <CircularProgress />
      </Box>
    );
  }

  const averageRating = car?.averageRating || 0;
  const ratingCount = car?.ratingCount || 0;

  return (
    <Box>
      {/* Rating Summary */}
      <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)' }}>
        <Grid container spacing={3} alignItems="center">
          <Grid item xs={12} md={4}>
            <Box display="flex" alignItems="center" justifyContent="center">
              <Box textAlign="center">
                <Typography variant="h2" fontWeight="bold" color="primary">
                  {averageRating > 0 ? averageRating.toFixed(1) : '0.0'}
                </Typography>
                <Box display="flex" justifyContent="center" gap={0.5} mb={1}>
                  {[1, 2, 3, 4, 5].map((star) => (
                    <StarIcon
                      key={star}
                      sx={{ 
                        color: star <= Math.round(averageRating) ? 'orange' : 'grey.300',
                        fontSize: 24 
                      }}
                    />
                  ))}
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {ratingCount} değerlendirme
                </Typography>
              </Box>
            </Box>
          </Grid>
          <Grid item xs={12} md={8}>
            <Typography variant="h6" gutterBottom>
              Müşteri Değerlendirmeleri
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Bu araç için toplamda {ratingCount} müşteri değerlendirmesi bulunmaktadır.
              {averageRating > 0 ? (
                ` Ortalama puan ${averageRating.toFixed(1)} olarak hesaplanmıştır.`
              ) : (
                ' Henüz değerlendirme yapılmamıştır.'
              )}
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* Individual Ratings */}
      {ratings.length > 0 ? (
        <Box>
          <Typography variant="h6" gutterBottom>
            Müşteri Yorumları
          </Typography>
          {ratings.map((rating) => (
            <Paper key={rating.id} sx={{ p: 2, mb: 2 }}>
              <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
                <Box>
                  <Typography variant="subtitle1" fontWeight="medium">
                    {rating.customerName || 'Anonim'}
                  </Typography>
                  <Box display="flex" gap={0.5}>
                    {[1, 2, 3, 4, 5].map((star) => (
                      <StarIcon
                        key={star}
                        sx={{ 
                          color: star <= rating.rating ? 'orange' : 'grey.300',
                          fontSize: 16 
                        }}
                      />
                    ))}
                    <Typography variant="body2" color="text.secondary" ml={1}>
                      {rating.rating}/5
                    </Typography>
                  </Box>
                </Box>
                <Typography variant="caption" color="text.secondary">
                  {rating.createdAt ? dayjs(rating.createdAt).format('DD.MM.YYYY') : ''}
                </Typography>
              </Box>
              {rating.comment && (
                <Typography variant="body2" sx={{ mt: 1, fontStyle: 'italic' }}>
                  "{rating.comment}"
                </Typography>
              )}
            </Paper>
          ))}
        </Box>
      ) : (
        <Box display="flex" flexDirection="column" alignItems="center" py={4}>
          <StarBorderIcon sx={{ fontSize: 64, color: 'grey.400', mb: 2 }} />
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Henüz değerlendirme yapılmamış
          </Typography>
          <Typography variant="body2" color="text.secondary" textAlign="center">
            Bu araç için henüz müşteri değerlendirmesi bulunmamaktadır.
          </Typography>
        </Box>
      )}
    </Box>
  );
};

export default CarsPage;
