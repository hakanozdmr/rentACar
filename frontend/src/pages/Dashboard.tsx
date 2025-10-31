import React, { useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  Card,
  CardContent,
  CardActions,
  CardMedia,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Slider,
  Collapse,
  IconButton,
} from '@mui/material';
import {
  DirectionsCar,
  People,
  BookOnline,
  TrendingUp,
  AttachMoney,
  Dashboard as DashboardIcon,
  FilterList,
  ExpandMore,
  LocalGasStation,
  Settings,
  Category,
  Star,
  Speed,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { carsApi, customersApi, rentalsApi, brandsApi, analyticsApi, DashboardStats, RevenueReport, CarAnalytics, CustomerSegment, TrendAnalysis, Car, customerPortalApi, Reservation, AvailableCar } from '../services/api';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs from 'dayjs';
import isSameOrBefore from 'dayjs/plugin/isSameOrBefore';
import isSameOrAfter from 'dayjs/plugin/isSameOrAfter';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  PointElement,
  LineElement,
} from 'chart.js';
import { Bar, Pie, Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  PointElement,
  LineElement
);

dayjs.extend(isSameOrBefore);
dayjs.extend(isSameOrAfter);

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useSnackbar();
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  
  // Reservation modal states
  const [openReservationModal, setOpenReservationModal] = useState(false);
  const [selectedCarForReservation, setSelectedCarForReservation] = useState<Car | null>(null);
  const [reservationData, setReservationData] = useState<Partial<Reservation>>({
    startDate: '',
    endDate: '',
    specialRequests: '',
    note: ''
  });
  const [formErrors, setFormErrors] = useState<{[key: string]: string}>({});
  
  // Date range for available cars
  const [dateRange, setDateRange] = useState({
    startDate: dayjs().format('YYYY-MM-DD'),
    endDate: dayjs().add(7, 'day').format('YYYY-MM-DD')
  });

  // Filter states for cars
  const [filters, setFilters] = useState({
    brand: '',
    fuelType: '',
    transmission: '',
    segment: '',
    minPrice: '',
    maxPrice: '',
    sortBy: 'dailyPrice',
    sortOrder: 'asc' as 'asc' | 'desc'
  });

  const { data: cars = [] } = useQuery('cars', () => carsApi.getAll().then(res => res.data));
  const { data: customers = [] } = useQuery('customers', () => customersApi.getAll().then(res => res.data));
  const { data: rentals = [] } = useQuery('rentals', () => rentalsApi.getAll().then(res => res.data));
  const { data: brands = [] } = useQuery('brands', () => brandsApi.getAll().then(res => res.data));
  
  // Analytics data
  const { data: dashboardStats, isLoading: statsLoading } = useQuery<DashboardStats>(
    'dashboardStats', 
    () => analyticsApi.getDashboardStats().then(res => res.data)
  );
  
  const { data: monthlyRevenue = [], isLoading: revenueLoading } = useQuery<RevenueReport[]>(
    ['monthlyRevenue', selectedYear],
    () => analyticsApi.getMonthlyRevenue(selectedYear).then(res => res.data)
  );
  
  const { data: topCars = [], isLoading: carsLoading } = useQuery<CarAnalytics[]>(
    'topCars',
    () => analyticsApi.getTopRevenueCars(5).then(res => res.data)
  );
  
  const { data: customerSegments = [], isLoading: segmentsLoading } = useQuery<CustomerSegment[]>(
    'customerSegments',
    () => analyticsApi.getCustomerSegmentation().then(res => res.data)
  );
  
  const { data: revenueTrend = [], isLoading: trendLoading } = useQuery<TrendAnalysis[]>(
    'revenueTrend',
    () => analyticsApi.getRevenueTrend(12).then(res => res.data)
  );

  // Available cars based on date range
  const { data: dateBasedAvailableCars = [], isLoading: availableCarsLoading } = useQuery<AvailableCar[]>(
    ['availableCars', dateRange.startDate, dateRange.endDate],
    () => customerPortalApi.getAvailableCars(dateRange.startDate, dateRange.endDate).then(res => res.data),
    {
      enabled: !!(dateRange.startDate && dateRange.endDate)
    }
  );

  const availableCars = cars.filter(car => car.state === 1).length;
  const rentedCars = cars.filter(car => car.state === 2).length;
  const maintenanceCars = cars.filter(car => car.state === 3).length;
  const activeRentals = rentals.filter(rental => 
    dayjs(rental.start).isSameOrBefore(dayjs()) && 
    dayjs(rental.end).isSameOrAfter(dayjs())
  ).length;

  // Chart data configurations
  const revenueChartData = {
    labels: monthlyRevenue.map(item => dayjs(item.date).format('MMM')),
    datasets: [
      {
        label: 'Aylık Gelir (TL)',
        data: monthlyRevenue.map(item => item.totalRevenue),
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1,
      },
    ],
  };

  const customerSegmentData = {
    labels: customerSegments.map(item => item.segmentName),
    datasets: [
      {
        label: 'Müşteri Sayısı',
        data: customerSegments.map(item => item.customerCount),
        backgroundColor: [
          'rgba(255, 99, 132, 0.5)',
          'rgba(75, 192, 192, 0.5)',
          'rgba(255, 205, 86, 0.5)',
        ],
        borderColor: [
          'rgba(255, 99, 132, 1)',
          'rgba(75, 192, 192, 1)',
          'rgba(255, 205, 86, 1)',
        ],
        borderWidth: 1,
      },
    ],
  };

  const trendChartData = {
    labels: revenueTrend.map(item => dayjs(item.date).format('MMM YYYY')),
    datasets: [
      {
        label: 'Gelir Trendi (TL)',
        data: revenueTrend.map(item => item.revenue),
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: 'rgba(75, 192, 192, 1)',
        borderWidth: 2,
        tension: 0.1,
        fill: true,
      },
      {
        label: 'Kiralama Sayısı',
        data: revenueTrend.map(item => item.rentalCount),
        backgroundColor: 'rgba(255, 206, 86, 0.2)',
        borderColor: 'rgba(255, 206, 86, 1)',
        borderWidth: 2,
        tension: 0.1,
        yAxisID: 'y1',
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: true,
        text: 'Analytics Dashboard',
      },
    },
  };

  const trendChartOptions = {
    responsive: true,
    interaction: {
      mode: 'index' as const,
      intersect: false,
    },
    plugins: {
      legend: {
        position: 'top' as const,
      },
      title: {
        display: true,
        text: 'Gelir Trend Analizi',
      },
    },
    scales: {
      y: {
        type: 'linear' as const,
        display: true,
        position: 'left' as const,
        title: {
          display: true,
          text: 'Gelir (TL)',
        },
      },
      y1: {
        type: 'linear' as const,
        display: true,
        position: 'right' as const,
        title: {
          display: true,
          text: 'Kiralama Sayısı',
        },
        grid: {
          drawOnChartArea: false,
        },
      },
    },
  };

  const stats = [
    {
      title: 'Toplam Gelir',
      value: statsLoading ? '...' : `₺${dashboardStats?.totalRevenue?.toLocaleString() || 0}`,
      icon: <AttachMoney />,
      color: '#4caf50',
      path: '/analytics',
    },
    {
      title: 'Aylık Gelir',
      value: statsLoading ? '...' : `₺${dashboardStats?.monthlyRevenue?.toLocaleString() || 0}`,
      icon: <TrendingUp />,
      color: '#2196f3',
      path: '/analytics',
    },
    {
      title: 'Bugünkü Gelir',
      value: statsLoading ? '...' : `₺${dashboardStats?.todayRevenue?.toLocaleString() || 0}`,
      icon: <DashboardIcon />,
      color: '#ff9800',
      path: '/analytics',
    },
    {
      title: 'Aktif Kiralama',
      value: statsLoading ? '...' : dashboardStats?.activeRentals || activeRentals,
      icon: <BookOnline />,
      color: '#f44336',
      path: '/rentals',
    },
  ];

  // Reservation mutation
  const createReservationMutation = useMutation(
    (reservation: Reservation) => customerPortalApi.createReservation(reservation),
    {
      onSuccess: (data) => {
        queryClient.invalidateQueries(['myReservations']);
        queryClient.invalidateQueries(['unreadNotificationCount']); // Refresh notification count
        setOpenReservationModal(false);
        setSelectedCarForReservation(null);
        setReservationData({
          startDate: '',
          endDate: '',
          specialRequests: '',
          note: ''
        });
        setFormErrors({});
        
        // Show success message
        showSuccess(`Rezervasyonunuz başarıyla alınmıştır! Rezervasyon No: ${data?.data?.id || ''}`);
      },
      onError: (error) => {
        console.error('Rezervasyon oluşturulurken hata:', error);
        setFormErrors({ submit: 'Rezervasyon oluşturulurken bir hata oluştu. Lütfen tekrar deneyiniz.' });
        showError('Rezervasyon oluşturulurken bir hata oluştu');
      }
    }
  );

  // Handler functions
  const handleReservationClick = (car: Car) => {
    setSelectedCarForReservation(car);
    // Pre-fill reservation dates with the selected date range
    setReservationData(prev => ({
      ...prev,
      startDate: dateRange.startDate,
      endDate: dateRange.endDate
    }));
    setOpenReservationModal(true);
  };

  const validateForm = () => {
    const errors: {[key: string]: string} = {};
    
    if (!reservationData.startDate) {
      errors.startDate = 'Başlangıç tarihi gereklidir';
    } else if (dayjs(reservationData.startDate).isBefore(dayjs(), 'day')) {
      errors.startDate = 'Başlangıç tarihi bugünden önce olamaz';
    }
    
    if (!reservationData.endDate) {
      errors.endDate = 'Bitiş tarihi gereklidir';
    }
    
    if (reservationData.startDate && reservationData.endDate) {
      if (dayjs(reservationData.endDate).isBefore(dayjs(reservationData.startDate))) {
        errors.endDate = 'Bitiş tarihi başlangıç tarihinden önce olamaz';
      }
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleReservationSubmit = () => {
    if (!validateForm() || !selectedCarForReservation) {
      return;
    }

    const reservation: Reservation = {
      ...reservationData,
      customerId: 0, // Will be set by backend
      carId: selectedCarForReservation.id!,
      startDate: reservationData.startDate!,
      endDate: reservationData.endDate!,
      status: 'PENDING' as const,
      specialRequests: reservationData.specialRequests || '',
      note: reservationData.note || ''
    } as Reservation;

    createReservationMutation.mutate(reservation);
  };

  const handleReservationCancel = () => {
    setOpenReservationModal(false);
    setSelectedCarForReservation(null);
    setReservationData({
      startDate: '',
      endDate: '',
      specialRequests: '',
      note: ''
    });
    setFormErrors({});
  };

  // User Dashboard Component
  const renderUserDashboard = () => {
    // Get full car details for available cars based on date range
    let availableCarsList = dateBasedAvailableCars.map(availableCar => {
      const fullCarDetails = cars.find(car => car.id === availableCar.carId);
      return fullCarDetails || {
        id: availableCar.carId,
        plate: availableCar.carPlate,
        brandName: availableCar.carBrandName,
        modelName: availableCar.carModelName,
        dailyPrice: 0,
        modelYear: 0,
        state: 1
      } as Car;
    }).filter(Boolean);

    // Get unique values for filter options (before filtering)
    const allAvailableCars = [...availableCarsList];
    const uniqueBrands = Array.from(new Set(allAvailableCars.map(car => car.brandName).filter(Boolean)));
    const uniqueFuelTypes = Array.from(new Set(allAvailableCars.map(car => car.fuelType).filter(Boolean)));
    const uniqueTransmissions = Array.from(new Set(allAvailableCars.map(car => car.transmission).filter(Boolean)));
    const uniqueSegments = Array.from(new Set(allAvailableCars.map(car => car.segment).filter(Boolean)));

    // Apply filters
    availableCarsList = availableCarsList.filter(car => {
      if (filters.brand && car.brandName !== filters.brand) return false;
      if (filters.fuelType && car.fuelType !== filters.fuelType) return false;
      if (filters.transmission && car.transmission !== filters.transmission) return false;
      if (filters.segment && car.segment !== filters.segment) return false;
      if (filters.minPrice && car.dailyPrice < Number(filters.minPrice)) return false;
      if (filters.maxPrice && car.dailyPrice > Number(filters.maxPrice)) return false;
      return true;
    });

    // Apply sorting
    availableCarsList.sort((a, b) => {
      let aValue: any = a[filters.sortBy as keyof Car];
      let bValue: any = b[filters.sortBy as keyof Car];
      
      if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = (bValue || '').toLowerCase();
      }
      
      if (filters.sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    // Calculate price range for filtered results
    const minPrice = availableCarsList.length > 0 ? Math.min(...availableCarsList.map(car => car.dailyPrice)) : 0;
    const maxPrice = availableCarsList.length > 0 ? Math.max(...availableCarsList.map(car => car.dailyPrice)) : 0;
    
    return (
      <Box>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Typography variant="h4" component="h1">
            Müsait Araçlar
          </Typography>
        </Box>

        {/* Date Range Selector */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Tarih Aralığı Seçin
          </Typography>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Başlangıç Tarihi"
                type="date"
                value={dateRange.startDate}
                onChange={(e) => setDateRange(prev => ({ 
                  ...prev, 
                  startDate: e.target.value 
                }))}
                InputLabelProps={{ shrink: true }}
                inputProps={{ min: dayjs().format('YYYY-MM-DD') }}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              <TextField
                fullWidth
                label="Bitiş Tarihi"
                type="date"
                value={dateRange.endDate}
                onChange={(e) => setDateRange(prev => ({ 
                  ...prev, 
                  endDate: e.target.value 
                }))}
                InputLabelProps={{ shrink: true }}
                inputProps={{ 
                  min: dateRange.startDate || dayjs().format('YYYY-MM-DD')
                }}
              />
            </Grid>
            <Grid item xs={12} sm={4}>
              {availableCarsLoading ? (
                <Box display="flex" alignItems="center" gap={1}>
                  <CircularProgress size={20} />
                  <Typography variant="body2">Yükleniyor...</Typography>
                </Box>
              ) : (
                <Alert severity="info" sx={{ mb: 0 }}>
                  {dayjs(dateRange.startDate).format('DD.MM.YYYY')} - {dayjs(dateRange.endDate).format('DD.MM.YYYY')}
                  <br />
                  {dateBasedAvailableCars.length} araç müsait
                </Alert>
              )}
            </Grid>
          </Grid>
        </Paper>

        {/* Filters Section */}
        {dateRange.startDate && dateRange.endDate && dateBasedAvailableCars.length > 0 && (
          <Paper sx={{ p: 2, mb: 3 }}>
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMore />}>
                <Box display="flex" alignItems="center" gap={1}>
                  <FilterList />
                  <Typography variant="h6">Filtreler ve Sıralama</Typography>
                </Box>
              </AccordionSummary>
              <AccordionDetails>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6} md={3}>
                    <FormControl fullWidth size="small">
                      <InputLabel>Marka</InputLabel>
                      <Select
                        value={filters.brand}
                        onChange={(e) => setFilters(prev => ({ ...prev, brand: e.target.value }))}
                        label="Marka"
                      >
                        <MenuItem value="">Tümü</MenuItem>
                        {uniqueBrands.map(brand => (
                          <MenuItem key={brand} value={brand}>{brand}</MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <FormControl fullWidth size="small">
                      <InputLabel>Yakıt Tipi</InputLabel>
                      <Select
                        value={filters.fuelType}
                        onChange={(e) => setFilters(prev => ({ ...prev, fuelType: e.target.value }))}
                        label="Yakıt Tipi"
                      >
                        <MenuItem value="">Tümü</MenuItem>
                        {uniqueFuelTypes.map(fuel => (
                          <MenuItem key={fuel} value={fuel}>{fuel}</MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <FormControl fullWidth size="small">
                      <InputLabel>Vites</InputLabel>
                      <Select
                        value={filters.transmission}
                        onChange={(e) => setFilters(prev => ({ ...prev, transmission: e.target.value }))}
                        label="Vites"
                      >
                        <MenuItem value="">Tümü</MenuItem>
                        {uniqueTransmissions.map(transmission => (
                          <MenuItem key={transmission} value={transmission}>{transmission}</MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <FormControl fullWidth size="small">
                      <InputLabel>Segment</InputLabel>
                      <Select
                        value={filters.segment}
                        onChange={(e) => setFilters(prev => ({ ...prev, segment: e.target.value }))}
                        label="Segment"
                      >
                        <MenuItem value="">Tümü</MenuItem>
                        {uniqueSegments.map(segment => (
                          <MenuItem key={segment} value={segment}>{segment}</MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Min Fiyat (₺)"
                      type="number"
                      value={filters.minPrice}
                      onChange={(e) => setFilters(prev => ({ ...prev, minPrice: e.target.value }))}
                    />
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <TextField
                      fullWidth
                      size="small"
                      label="Max Fiyat (₺)"
                      type="number"
                      value={filters.maxPrice}
                      onChange={(e) => setFilters(prev => ({ ...prev, maxPrice: e.target.value }))}
                    />
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <FormControl fullWidth size="small">
                      <InputLabel>Sırala</InputLabel>
                      <Select
                        value={filters.sortBy}
                        onChange={(e) => setFilters(prev => ({ ...prev, sortBy: e.target.value }))}
                        label="Sırala"
                      >
                        <MenuItem value="dailyPrice">Fiyat</MenuItem>
                        <MenuItem value="modelYear">Model Yılı</MenuItem>
                        <MenuItem value="brandName">Marka</MenuItem>
                        <MenuItem value="modelName">Model</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                  
                  <Grid item xs={12} sm={6} md={3}>
                    <FormControl fullWidth size="small">
                      <InputLabel>Sıralama</InputLabel>
                      <Select
                        value={filters.sortOrder}
                        onChange={(e) => setFilters(prev => ({ ...prev, sortOrder: e.target.value as 'asc' | 'desc' }))}
                        label="Sıralama"
                      >
                        <MenuItem value="asc">Artan</MenuItem>
                        <MenuItem value="desc">Azalan</MenuItem>
                      </Select>
                    </FormControl>
                  </Grid>
                </Grid>
                
                {/* Active Filters */}
                {(filters.brand || filters.fuelType || filters.transmission || filters.segment || filters.minPrice || filters.maxPrice) && (
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Aktif Filtreler:
                    </Typography>
                    <Box display="flex" flexWrap="wrap" gap={1}>
                      {filters.brand && (
                        <Chip 
                          label={`Marka: ${filters.brand}`} 
                          size="small" 
                          onDelete={() => setFilters(prev => ({ ...prev, brand: '' }))}
                        />
                      )}
                      {filters.fuelType && (
                        <Chip 
                          label={`Yakıt: ${filters.fuelType}`} 
                          size="small" 
                          onDelete={() => setFilters(prev => ({ ...prev, fuelType: '' }))}
                        />
                      )}
                      {filters.transmission && (
                        <Chip 
                          label={`Vites: ${filters.transmission}`} 
                          size="small" 
                          onDelete={() => setFilters(prev => ({ ...prev, transmission: '' }))}
                        />
                      )}
                      {filters.segment && (
                        <Chip 
                          label={`Segment: ${filters.segment}`} 
                          size="small" 
                          onDelete={() => setFilters(prev => ({ ...prev, segment: '' }))}
                        />
                      )}
                      {filters.minPrice && (
                        <Chip 
                          label={`Min: ₺${filters.minPrice}`} 
                          size="small" 
                          onDelete={() => setFilters(prev => ({ ...prev, minPrice: '' }))}
                        />
                      )}
                      {filters.maxPrice && (
                        <Chip 
                          label={`Max: ₺${filters.maxPrice}`} 
                          size="small" 
                          onDelete={() => setFilters(prev => ({ ...prev, maxPrice: '' }))}
                        />
                      )}
                    </Box>
                  </Box>
                )}
                
                <Box display="flex" justifyContent="space-between" alignItems="center" mt={2}>
                  <Typography variant="body2" color="text.secondary">
                    {availableCarsList.length} araç gösteriliyor
                    {dateBasedAvailableCars.length !== availableCarsList.length && (
                      <span> (toplam {dateBasedAvailableCars.length} araçtan)</span>
                    )}
                  </Typography>
                  <Button 
                    size="small" 
                    variant="outlined"
                    onClick={() => setFilters({
                      brand: '',
                      fuelType: '',
                      transmission: '',
                      segment: '',
                      minPrice: '',
                      maxPrice: '',
                      sortBy: 'dailyPrice',
                      sortOrder: 'asc'
                    })}
                  >
                    Filtreleri Temizle
                  </Button>
                </Box>
              </AccordionDetails>
            </Accordion>
          </Paper>
        )}
        
        {dateRange.startDate && dateRange.endDate ? (
          <Grid container spacing={3}>
            {availableCarsList.map((car: Car) => (
            <Grid item xs={12} sm={6} md={4} key={car.id}>
              <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                <CardMedia
                  component="img"
                  height="200"
                  image={car.images && car.images.length > 0 ? car.images[0] : "https://via.placeholder.com/400x200?text=Car+Image"}
                  alt={`${car.brandName} ${car.modelName}`}
                />
                <CardContent sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" component="h2" gutterBottom>
                    {car.brandName} {car.modelName}
                  </Typography>
                  
                  {/* Basic Info */}
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Plaka: {car.plate}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    Model Yılı: {car.modelYear}
                  </Typography>
                  
                  {/* Price */}
                  <Typography variant="h6" color="primary" gutterBottom sx={{ mt: 1 }}>
                    ₺{car.dailyPrice}/gün
                  </Typography>

                  {/* Car Features */}
                  <Box sx={{ mt: 2, mb: 2 }}>
                    <Grid container spacing={1}>
                      {car.fuelType && (
                        <Grid item>
                          <Chip 
                            icon={<LocalGasStation />} 
                            label={car.fuelType} 
                            size="small" 
                            variant="outlined" 
                          />
                        </Grid>
                      )}
                      {car.transmission && (
                        <Grid item>
                          <Chip 
                            icon={<Settings />} 
                            label={car.transmission} 
                            size="small" 
                            variant="outlined" 
                          />
                        </Grid>
                      )}
                      {car.segment && (
                        <Grid item>
                          <Chip 
                            icon={<Category />} 
                            label={car.segment} 
                            size="small" 
                            variant="outlined" 
                          />
                        </Grid>
                      )}
                    </Grid>
                  </Box>

                  {/* Additional Details */}
                  <Box>
                    {car.mileage && (
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        <Speed sx={{ fontSize: 16, mr: 0.5, verticalAlign: 'middle' }} />
                        {car.mileage.toLocaleString()} km
                      </Typography>
                    )}
                    {car.color && (
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Renk: {car.color}
                      </Typography>
                    )}
                    <Box display="flex" alignItems="center" gap={0.5} mt={1}>
                      <Star sx={{ fontSize: 16, color: car.averageRating && car.ratingCount && car.ratingCount > 0 ? 'orange' : 'grey' }} />
                      <Typography variant="body2" color="text.secondary">
                        {car.averageRating && car.ratingCount && car.ratingCount > 0 ? (
                          `${car.averageRating.toFixed(1)} (${car.ratingCount} değerlendirme)`
                        ) : (
                          'Henüz değerlendirme almadı'
                        )}
                      </Typography>
                    </Box>
                  </Box>

                  {/* Features */}
                  {car.features && car.features.length > 0 && (
                    <Box sx={{ mt: 2 }}>
                      <Typography variant="caption" color="text.secondary" display="block" gutterBottom>
                        Özellikler:
                      </Typography>
                      <Box display="flex" flexWrap="wrap" gap={0.5}>
                        {car.features.slice(0, 3).map((feature, index) => (
                          <Chip 
                            key={index}
                            label={feature} 
                            size="small" 
                            variant="filled"
                            color="secondary"
                          />
                        ))}
                        {car.features.length > 3 && (
                          <Chip 
                            label={`+${car.features.length - 3} daha`} 
                            size="small" 
                            variant="outlined"
                          />
                        )}
                      </Box>
                    </Box>
                  )}

                  {/* Availability Chip */}
                  <Box display="flex" alignItems="center" gap={1} mt={2}>
                    <Chip 
                      label={`${dayjs(dateRange.startDate).format('DD.MM')} - ${dayjs(dateRange.endDate).format('DD.MM')} arası müsait`} 
                      color="success" 
                      size="small"
                    />
                  </Box>
                </CardContent>
                <CardActions>
                  <Button 
                    size="small" 
                    variant="contained" 
                    fullWidth
                    onClick={() => handleReservationClick(car)}
                  >
                    Rezervasyon Yap
                  </Button>
                </CardActions>
              </Card>
            </Grid>
            ))}
          </Grid>
        ) : (
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="h6" color="text.secondary">
              Lütfen yukarıdan tarih aralığı seçin.
            </Typography>
          </Paper>
        )}
        
        {availableCarsList.length === 0 && !availableCarsLoading && dateRange.startDate && dateRange.endDate && (
          <Paper sx={{ p: 4, textAlign: 'center', mt: 2 }}>
            <Typography variant="h6" color="text.secondary">
              {dayjs(dateRange.startDate).format('DD.MM.YYYY')} - {dayjs(dateRange.endDate).format('DD.MM.YYYY')} tarihleri arasında müsait araç bulunmamaktadır.
            </Typography>
          </Paper>
        )}
        
        {availableCarsLoading && (
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <CircularProgress />
            <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
              Müsait araçlar kontrol ediliyor...
            </Typography>
          </Paper>
        )}

        {/* Reservation Modal */}
        <Dialog 
          open={openReservationModal} 
          onClose={handleReservationCancel}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            Rezervasyon Yap - {selectedCarForReservation?.brandName} {selectedCarForReservation?.modelName}
          </DialogTitle>
          <DialogContent>
            <Box sx={{ pt: 2 }}>
              {selectedCarForReservation && (
                <Box sx={{ mb: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                  <Typography variant="h6" gutterBottom>
                    {selectedCarForReservation.brandName} {selectedCarForReservation.modelName}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Plaka: {selectedCarForReservation.plate} | Günlük Fiyat: ₺{selectedCarForReservation.dailyPrice}
                  </Typography>
                </Box>
              )}
              
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextField
                  label="Başlangıç Tarihi"
                  type="date"
                  value={reservationData.startDate}
                  onChange={(e) => {
                    setReservationData({...reservationData, startDate: e.target.value});
                    if (formErrors.startDate) {
                      setFormErrors({...formErrors, startDate: ''});
                    }
                  }}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                  required
                  error={!!formErrors.startDate}
                  helperText={formErrors.startDate}
                  inputProps={{ min: dayjs().format('YYYY-MM-DD') }}
                />
                
                <TextField
                  label="Bitiş Tarihi"
                  type="date"
                  value={reservationData.endDate}
                  onChange={(e) => {
                    setReservationData({...reservationData, endDate: e.target.value});
                    if (formErrors.endDate) {
                      setFormErrors({...formErrors, endDate: ''});
                    }
                  }}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                  required
                  error={!!formErrors.endDate}
                  helperText={formErrors.endDate}
                  inputProps={{ 
                    min: reservationData.startDate ? dayjs(reservationData.startDate).format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD')
                  }}
                />
                
                <TextField
                  label="Özel İstekler"
                  multiline
                  rows={3}
                  value={reservationData.specialRequests || ''}
                  onChange={(e) => setReservationData({...reservationData, specialRequests: e.target.value})}
                  fullWidth
                />
                
                <TextField
                  label="Not"
                  multiline
                  rows={2}
                  value={reservationData.note || ''}
                  onChange={(e) => setReservationData({...reservationData, note: e.target.value})}
                  fullWidth
                />
              </Box>
              
              {(createReservationMutation.isError || formErrors.submit) && (
                <Alert severity="error" sx={{ mt: 2 }}>
                  {formErrors.submit || 'Rezervasyon oluşturulurken hata oluştu'}
                </Alert>
              )}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleReservationCancel}>
              İptal
            </Button>
            <Button 
              onClick={handleReservationSubmit}
              variant="contained"
              disabled={
                createReservationMutation.isLoading || 
                !reservationData.startDate || 
                !reservationData.endDate
              }
            >
              {createReservationMutation.isLoading ? <CircularProgress size={20} /> : 'Rezervasyon Yap'}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    );
  };

  // If user has USER role, show user dashboard
  if (hasRole('USER') && !hasRole('ADMIN')) {
    return renderUserDashboard();
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          Analytics Dashboard
        </Typography>
        
        <FormControl size="small" sx={{ minWidth: 120 }}>
          <InputLabel>Yıl</InputLabel>
          <Select
            value={selectedYear}
            label="Yıl"
            onChange={(e) => setSelectedYear(Number(e.target.value))}
          >
            {[new Date().getFullYear(), new Date().getFullYear() - 1, new Date().getFullYear() - 2].map(year => (
              <MenuItem key={year} value={year}>{year}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>
      
      {/* Key Metrics */}
      <Grid container spacing={3}>
        {stats.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" justifyContent="space-between">
                  <Box>
                    <Typography color="textSecondary" gutterBottom variant="h6">
                      {stat.title}
                    </Typography>
                    <Typography variant="h4" component="div" style={{ color: stat.color }}>
                      {stat.value}
                    </Typography>
                  </Box>
                  <Box style={{ color: stat.color, fontSize: 40 }}>
                    {stat.icon}
                  </Box>
                </Box>
              </CardContent>
              <CardActions>
                <Button size="small" onClick={() => navigate(stat.path)}>
                  Detayları Gör
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Charts and Analytics */}
      <Grid container spacing={3} sx={{ mt: 2 }}>
        {/* Revenue Chart */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Aylık Gelir Trendi ({selectedYear})
            </Typography>
            {revenueLoading ? (
              <Box display="flex" justifyContent="center" p={3}>
                <CircularProgress />
              </Box>
            ) : (
              <Box height={300}>
                <Bar data={revenueChartData} options={chartOptions} />
              </Box>
            )}
          </Paper>
        </Grid>

        {/* Customer Segments */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Müşteri Segmentasyonu
            </Typography>
            {segmentsLoading ? (
              <Box display="flex" justifyContent="center" p={3}>
                <CircularProgress />
              </Box>
            ) : (
              <Box height={300}>
                <Pie data={customerSegmentData} options={chartOptions} />
              </Box>
            )}
          </Paper>
        </Grid>

        {/* Top Performing Cars */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              En Yüksek Gelir Getiren Araçlar
            </Typography>
            {carsLoading ? (
              <Box display="flex" justifyContent="center" p={3}>
                <CircularProgress />
              </Box>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Araç</TableCell>
                      <TableCell align="right">Gelir</TableCell>
                      <TableCell align="right">Kiralama</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {topCars.map((car) => (
                      <TableRow key={car.carId}>
                        <TableCell>
                          {car.brandName} {car.modelName}
                          <br />
                          <Typography variant="caption" color="textSecondary">
                            {car.plate}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight="bold">
                            ₺{car.totalRevenue.toLocaleString()}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Chip 
                            label={car.totalRentals} 
                            size="small" 
                            color="primary" 
                          />
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>
        </Grid>

        {/* Customer Segments Table */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Segment Detayları
            </Typography>
            {segmentsLoading ? (
              <Box display="flex" justifyContent="center" p={3}>
                <CircularProgress />
              </Box>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Segment</TableCell>
                      <TableCell align="right">Müşteri</TableCell>
                      <TableCell align="right">Gelir</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {customerSegments.map((segment) => (
                      <TableRow key={segment.segmentName}>
                        <TableCell>
                          <Chip 
                            label={segment.segmentName} 
                            color={
                              segment.segmentName === 'VIP' ? 'error' : 
                              segment.segmentName === 'REGULAR' ? 'primary' : 'default'
                            }
                            size="small"
                          />
                        </TableCell>
                        <TableCell align="right">
                          {segment.customerCount}
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body2" fontWeight="bold">
                            ₺{segment.totalRevenue.toLocaleString()}
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>
        </Grid>

        {/* Revenue Trend Analysis */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              12 Aylık Gelir Trend Analizi
            </Typography>
            {trendLoading ? (
              <Box display="flex" justifyContent="center" p={3}>
                <CircularProgress />
              </Box>
            ) : (
              <Box height={400}>
                <Line data={trendChartData} options={trendChartOptions} />
              </Box>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
