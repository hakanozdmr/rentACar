import React, { useState } from 'react';
import {
  Box,
  Grid,
  Paper,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Button,
  Tabs,
  Tab,
} from '@mui/material';
import {
  BarChart,
  PieChart,
  TrendingUp,
  AttachMoney,
  DirectionsCar,
  People,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import { analyticsApi, DashboardStats, RevenueReport, CarAnalytics, CustomerSegment, TrendAnalysis } from '../services/api';
import dayjs from 'dayjs';
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

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`analytics-tabpanel-${index}`}
      aria-labelledby={`analytics-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const AnalyticsPage: React.FC = () => {
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [startYear, setStartYear] = useState(new Date().getFullYear() - 5);
  const [endYear, setEndYear] = useState(new Date().getFullYear());
  const [tabValue, setTabValue] = useState(0);

  // Analytics data queries
  const { data: dashboardStats, isLoading: statsLoading } = useQuery<DashboardStats>(
    'dashboardStats', 
    () => analyticsApi.getDashboardStats().then(res => res.data)
  );

  const { data: monthlyRevenue = [], isLoading: monthlyLoading } = useQuery<RevenueReport[]>(
    ['monthlyRevenue', selectedYear],
    () => analyticsApi.getMonthlyRevenue(selectedYear).then(res => res.data)
  );

  const { data: yearlyRevenue = [], isLoading: yearlyLoading } = useQuery<RevenueReport[]>(
    ['yearlyRevenue', startYear, endYear],
    () => analyticsApi.getYearlyRevenue(startYear, endYear).then(res => res.data)
  );

  const { data: topCars = [], isLoading: topCarsLoading } = useQuery<CarAnalytics[]>(
    'topCars',
    () => analyticsApi.getTopRevenueCars(10).then(res => res.data)
  );

  const { data: mostRentedCars = [], isLoading: mostRentedLoading } = useQuery<CarAnalytics[]>(
    'mostRentedCars',
    () => analyticsApi.getMostRentedCars(10).then(res => res.data)
  );

  const { data: customerSegments = [], isLoading: segmentsLoading } = useQuery<CustomerSegment[]>(
    'customerSegments',
    () => analyticsApi.getCustomerSegmentation().then(res => res.data)
  );

  const { data: revenueTrend = [], isLoading: trendLoading } = useQuery<TrendAnalysis[]>(
    'revenueTrend',
    () => analyticsApi.getRevenueTrend(24).then(res => res.data)
  );

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  // Chart data configurations
  const monthlyRevenueChartData = {
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

  const yearlyRevenueChartData = {
    labels: yearlyRevenue.map(item => dayjs(item.date).format('YYYY')),
    datasets: [
      {
        label: 'Yıllık Gelir (TL)',
        data: yearlyRevenue.map(item => item.totalRevenue),
        backgroundColor: 'rgba(75, 192, 192, 0.5)',
        borderColor: 'rgba(75, 192, 192, 1)',
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
          'rgba(201, 203, 207, 0.5)',
          'rgba(54, 162, 235, 0.5)',
        ],
        borderColor: [
          'rgba(255, 99, 132, 1)',
          'rgba(75, 192, 192, 1)',
          'rgba(255, 205, 86, 1)',
          'rgba(201, 203, 207, 1)',
          'rgba(54, 162, 235, 1)',
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

  const statsCards = [
    {
      title: 'Toplam Gelir',
      value: statsLoading ? '...' : `₺${dashboardStats?.totalRevenue?.toLocaleString() || 0}`,
      icon: <AttachMoney />,
      color: '#4caf50',
    },
    {
      title: 'Aylık Ortalama Gelir',
      value: statsLoading ? '...' : `₺${dashboardStats?.monthlyRevenue?.toLocaleString() || 0}`,
      icon: <TrendingUp />,
      color: '#2196f3',
    },
    {
      title: 'Bugünün Geliri',
      value: statsLoading ? '...' : `₺${dashboardStats?.todayRevenue?.toLocaleString() || 0}`,
      icon: <BarChart />,
      color: '#ff9800',
    },
    {
      title: 'Aktif Kiralama',
      value: statsLoading ? '...' : dashboardStats?.activeRentals || 0,
      icon: <DirectionsCar />,
      color: '#9c27b0',
    },
  ];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Analytics & Raporlar
      </Typography>
      <Typography variant="subtitle1" color="textSecondary" sx={{ mb: 3 }}>
        Detaylı analiz ve raporlama araçları
      </Typography>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {statsCards.map((stat, index) => (
          <Grid item xs={12} sm={6} md={3} key={index}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" justifyContent="space-between">
                  <Box>
                    <Typography color="textSecondary" gutterBottom variant="body2">
                      {stat.title}
                    </Typography>
                    <Typography variant="h6" component="div">
                      {stat.value}
                    </Typography>
                  </Box>
                  <Box sx={{ color: stat.color }}>
                    {stat.icon}
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Yıl</InputLabel>
              <Select
                value={selectedYear}
                label="Yıl"
                onChange={(e) => setSelectedYear(Number(e.target.value))}
              >
                {Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - i).map(year => (
                  <MenuItem key={year} value={year}>{year}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Başlangıç Yılı</InputLabel>
              <Select
                value={startYear}
                label="Başlangıç Yılı"
                onChange={(e) => setStartYear(Number(e.target.value))}
              >
                {Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - i).map(year => (
                  <MenuItem key={year} value={year}>{year}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Bitiş Yılı</InputLabel>
              <Select
                value={endYear}
                label="Bitiş Yılı"
                onChange={(e) => setEndYear(Number(e.target.value))}
              >
                {Array.from({ length: 10 }, (_, i) => new Date().getFullYear() - i).map(year => (
                  <MenuItem key={year} value={year}>{year}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="analytics tabs">
          <Tab label="Gelir Analizi" />
          <Tab label="Araç Analizi" />
          <Tab label="Müşteri Analizi" />
          <Tab label="Trend Analizi" />
        </Tabs>
      </Box>

      {/* Revenue Analysis Tab */}
      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Aylık Gelir ({selectedYear})
              </Typography>
              {monthlyLoading ? (
                <Box display="flex" justifyContent="center" p={3}>
                  <CircularProgress />
                </Box>
              ) : (
                <Box height={300}>
                  <Bar data={monthlyRevenueChartData} options={chartOptions} />
                </Box>
              )}
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Yıllık Gelir ({startYear} - {endYear})
              </Typography>
              {yearlyLoading ? (
                <Box display="flex" justifyContent="center" p={3}>
                  <CircularProgress />
                </Box>
              ) : (
                <Box height={300}>
                  <Bar data={yearlyRevenueChartData} options={chartOptions} />
                </Box>
              )}
            </Paper>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Car Analysis Tab */}
      <TabPanel value={tabValue} index={1}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                En Yüksek Gelir Getiren Araçlar
              </Typography>
              {topCarsLoading ? (
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
                            {car.totalRentals}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                En Çok Kiralanan Araçlar
              </Typography>
              {mostRentedLoading ? (
                <Box display="flex" justifyContent="center" p={3}>
                  <CircularProgress />
                </Box>
              ) : (
                <TableContainer>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Araç</TableCell>
                        <TableCell align="right">Kiralama</TableCell>
                        <TableCell align="right">Gelir</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {mostRentedCars.map((car) => (
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
                              {car.totalRentals}
                            </Typography>
                          </TableCell>
                          <TableCell align="right">
                            ₺{car.totalRevenue.toLocaleString()}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Paper>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Customer Analysis Tab */}
      <TabPanel value={tabValue} index={2}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
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
                        <TableCell align="right">Ort. Gelir</TableCell>
                        <TableCell align="right">Toplam Gelir</TableCell>
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
                                segment.segmentName === 'REGULAR' ? 'primary' : 
                                segment.segmentName === 'FREQUENT' ? 'success' : 'default'
                              }
                              size="small"
                            />
                          </TableCell>
                          <TableCell align="right">
                            {segment.customerCount}
                          </TableCell>
                          <TableCell align="right">
                            ₺{segment.averageRevenuePerCustomer.toLocaleString()}
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
        </Grid>
      </TabPanel>

      {/* Trend Analysis Tab */}
      <TabPanel value={tabValue} index={3}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                24 Aylık Gelir Trend Analizi
              </Typography>
              {trendLoading ? (
                <Box display="flex" justifyContent="center" p={3}>
                  <CircularProgress />
                </Box>
              ) : (
                <Box height={500}>
                  <Line data={trendChartData} options={trendChartOptions} />
                </Box>
              )}
            </Paper>
          </Grid>
        </Grid>
      </TabPanel>
    </Box>
  );
};

export default AnalyticsPage;
