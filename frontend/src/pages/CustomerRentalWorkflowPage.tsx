import React, { useState, useRef } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  Typography,
  Paper,
  Button,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Divider,
} from '@mui/material';
import {
  DirectionsCar,
  Description,
  CheckCircle,
  LocalGasStation,
  AttachMoney,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { useAuth } from '../contexts/AuthContext';
import { useSnackbar } from '../contexts/SnackbarContext';
import {
  carsApi,
  contractsApi,
  contractTemplatesApi,
  paymentsApi,
  rentalsApi,
  Car,
  ContractTemplate,
  Contract,
  Payment,
} from '../services/api';
import dayjs, { Dayjs } from 'dayjs';
import isBetween from 'dayjs/plugin/isBetween';

dayjs.extend(isBetween);

const steps = [
  { label: 'Araç ve Tarih Seçimi' },
  { label: 'Sözleşme Şablonu' },
  { label: 'Sözleşme Onayı' },
  { label: 'Ödeme Bilgileri' },
  { label: 'Tamamlandı' },
];

const CustomerRentalWorkflowPage: React.FC = () => {
  const { user } = useAuth();
  const [activeStep, setActiveStep] = useState(0);
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Step 0: Car & Date Selection
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);
  const [startDate, setStartDate] = useState<Dayjs | null>(dayjs());
  const [endDate, setEndDate] = useState<Dayjs | null>(dayjs().add(3, 'day'));

  // Step 1: Contract Template
  const [selectedTemplate, setSelectedTemplate] = useState<number | null>(null);

  // Step 2: Contract Review
  const [contract, setContract] = useState<Contract | null>(null);

  // Step 3: Payment
  const [paymentMethod, setPaymentMethod] = useState<'CREDIT_CARD' | 'CASH' | 'BANK_TRANSFER'>('CREDIT_CARD');
  const [completedRentalId, setCompletedRentalId] = useState<number | null>(null);
  const [cardNumber, setCardNumber] = useState('');
  const [cardHolder, setCardHolder] = useState('');
  const [cardExpiry, setCardExpiry] = useState('');
  const [cardCVV, setCardCVV] = useState('');

  // Refs to avoid stale closures
  const selectedTemplateRef = useRef(selectedTemplate);
  const selectedCarRef = useRef(selectedCar);
  const paymentMethodRef = useRef(paymentMethod);
  const contractRef = useRef(contract);
  const rentalIdRef = useRef<number | null>(null);

  React.useEffect(() => {
    selectedTemplateRef.current = selectedTemplate;
  }, [selectedTemplate]);

  React.useEffect(() => {
    selectedCarRef.current = selectedCar;
  }, [selectedCar]);

  React.useEffect(() => {
    paymentMethodRef.current = paymentMethod;
  }, [paymentMethod]);

  React.useEffect(() => {
    contractRef.current = contract;
  }, [contract]);

  // Queries
  const { data: availableCars = [], isLoading: isLoadingCars } = useQuery(
    ['availableCars', startDate, endDate],
    () => {
      if (!startDate || !endDate) return Promise.resolve([]);
      return carsApi.getAll().then(res => {
        const cars = res.data.filter(car => car.state === 1);
        return cars;
      });
    },
    { enabled: !!startDate && !!endDate }
  );

  const { data: templates = [] } = useQuery(
    'contract-templates',
    () => contractTemplatesApi.getAll().then(res => res.data)
  );

  // Mutations
  const createRentalMutation = useMutation(
    (rental: Partial<any>) => rentalsApi.create(rental as any),
    {
      onSuccess: (data) => {
        const newRentalId = data.data.id!;
        rentalIdRef.current = newRentalId;
        if (selectedTemplateRef.current) {
          const contractData = {
            rentalId: newRentalId,
            customerId: user?.id!,
            templateId: selectedTemplateRef.current,
            signedDate: dayjs().format('YYYY-MM-DD'),
            status: 'DRAFT' as const,
          };
          createContractMutation.mutate(contractData as Contract);
        }
      },
      onError: (error: any) => {
        showError(error.message || 'Kiralama oluşturulurken hata oluştu');
      },
    }
  );

  const createContractMutation = useMutation(
    (contract: Contract) => contractsApi.create(contract),
    {
      onSuccess: (data) => {
        setContract(data.data);
        signContractMutation.mutate({
          id: data.data.id!,
          customerSignature: user?.email || 'customer-signature',
          companySignature: 'company-signature',
        });
      },
      onError: (error: any) => {
        showError(error.message || 'Sözleşme oluşturulurken hata oluştu');
      },
    }
  );

  const signContractMutation = useMutation(
    ({ id, customerSignature, companySignature }: { id: number, customerSignature: string, companySignature: string }) =>
      contractsApi.signContract(id, customerSignature, companySignature),
    {
      onSuccess: () => {
        const rentalId = rentalIdRef.current;
        if (rentalId && selectedCarRef.current) {
          const days = endDate!.diff(startDate!, 'day') + 1;
          createPaymentMutation.mutate({
            rentalId: rentalId,
            customerId: user?.id!,
            amount: selectedCarRef.current.dailyPrice * days,
            method: paymentMethodRef.current,
            status: 'COMPLETED',
          } as Payment);
        }
      },
      onError: (error: any) => {
        showError(error.message || 'Sözleşme imzalanırken hata oluştu');
      },
    }
  );

  const createPaymentMutation = useMutation(
    (payment: Partial<Payment>) => paymentsApi.create(payment as Payment),
    {
      onSuccess: () => {
        const finalRentalId = rentalIdRef.current;
        setCompletedRentalId(finalRentalId);
        setActiveStep(4);
        showSuccess('Kiralama başarıyla tamamlandı!');
        queryClient.invalidateQueries(['myRentals']);
      },
      onError: (error: any) => {
        showError(error.message || 'Ödeme kaydedilirken hata oluştu');
      },
    }
  );

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  const handleStep0Complete = () => {
    if (selectedCar && startDate && endDate) {
      handleNext();
    } else {
      showError('Lütfen araç ve tarih seçin');
    }
  };

  const handleStep1Complete = () => {
    if (selectedTemplate) {
      handleNext();
    } else {
      showError('Lütfen sözleşme şablonu seçin');
    }
  };

  const handleStep2Complete = () => {
    handleNext();
  };

  const handleStep3Complete = () => {
    // Validate payment method specific fields
    if (paymentMethod === 'CREDIT_CARD') {
      if (!cardNumber || cardNumber.length !== 16) {
        showError('Lütfen geçerli bir kart numarası girin');
        return;
      }
      if (!cardHolder || cardHolder.length < 3) {
        showError('Lütfen kart üzerindeki ismi girin');
        return;
      }
      if (!cardExpiry || cardExpiry.length !== 4) {
        showError('Lütfen son kullanma tarihini girin');
        return;
      }
      if (!cardCVV || cardCVV.length !== 3) {
        showError('Lütfen CVV kodunu girin');
        return;
      }
    }

    if (selectedCar && startDate && endDate) {
      const days = endDate.diff(startDate, 'day') + 1;
      
      createRentalMutation.mutate({
        carId: selectedCar.id,
        customerId: user?.id,
        start: startDate.format('YYYY-MM-DD'),
        end: endDate.format('YYYY-MM-DD'),
        totalAmount: selectedCar.dailyPrice * days,
        status: 'CONFIRMED',
      });
    }
  };

  const calculateDays = () => {
    if (startDate && endDate) {
      return endDate.diff(startDate, 'day') + 1;
    }
    return 0;
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 0:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Araç ve Tarih Seçimi
            </Typography>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DatePicker
                    label="Başlangıç Tarihi"
                    value={startDate}
                    onChange={(newValue) => setStartDate(newValue)}
                    disablePast
                    sx={{ width: '100%' }}
                  />
                </LocalizationProvider>
              </Grid>
              <Grid item xs={12} sm={6}>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DatePicker
                    label="Bitiş Tarihi"
                    value={endDate}
                    onChange={(newValue) => setEndDate(newValue)}
                    minDate={startDate}
                    sx={{ width: '100%' }}
                  />
                </LocalizationProvider>
              </Grid>
              <Grid item xs={12}>
                {isLoadingCars ? (
                  <CircularProgress />
                ) : availableCars.length === 0 ? (
                  <Alert severity="info">Seçilen tarih aralığında uygun araç bulunamadı.</Alert>
                ) : (
                  <TableContainer>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>Marka/Model</TableCell>
                          <TableCell>Yıl</TableCell>
                          <TableCell>Segment</TableCell>
                          <TableCell>Günlük Fiyat</TableCell>
                          <TableCell>İşlem</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {availableCars.map((car: Car) => (
                          <TableRow
                            key={car.id}
                            onClick={() => setSelectedCar(car)}
                            sx={{
                              cursor: 'pointer',
                              bgcolor: selectedCar?.id === car.id ? 'action.selected' : 'transparent',
                              '&:hover': { bgcolor: 'action.hover' }
                            }}
                          >
                            <TableCell>{car.brandName} {car.modelName}</TableCell>
                            <TableCell>{car.modelYear}</TableCell>
                            <TableCell>{car.segment}</TableCell>
                            <TableCell>{car.dailyPrice?.toLocaleString('tr-TR')} TL</TableCell>
                            <TableCell>
                              {selectedCar?.id === car.id && (
                                <CheckCircle color="primary" />
                              )}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </Grid>
            </Grid>
          </Box>
        );

      case 1:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Sözleşme Şablonu Seçin
            </Typography>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              {templates.map((template: ContractTemplate) => (
                <Grid item xs={12} sm={6} key={template.id}>
                  <Card
                    onClick={() => setSelectedTemplate(template.id!)}
                    sx={{
                      cursor: 'pointer',
                      border: selectedTemplate === template.id ? 2 : 1,
                      borderColor: selectedTemplate === template.id ? 'primary.main' : 'divider',
                      bgcolor: selectedTemplate === template.id ? 'action.selected' : 'background.paper',
                      '&:hover': { bgcolor: 'action.hover' }
                    }}
                  >
                    <CardContent>
                      <Typography variant="h6">{template.name}</Typography>
                      {template.description && (
                        <Typography variant="body2" color="text.secondary">
                          {template.description}
                        </Typography>
                      )}
                      {selectedTemplate === template.id && (
                        <CheckCircle color="primary" sx={{ mt: 1 }} />
                      )}
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Box>
        );

      case 2:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Sözleşme Özeti
            </Typography>
            {selectedCar && (
              <Card sx={{ mt: 2 }}>
                <CardContent>
                  <List>
                    <ListItem>
                      <ListItemIcon><DirectionsCar color="primary" /></ListItemIcon>
                      <ListItemText
                        primary="Araç"
                        secondary={`${selectedCar.brandName} ${selectedCar.modelName} - ${selectedCar.plate}`}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><Description color="primary" /></ListItemIcon>
                      <ListItemText
                        primary="Kiralama Süresi"
                        secondary={`${calculateDays()} gün`}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><AttachMoney color="primary" /></ListItemIcon>
                      <ListItemText
                        primary="Toplam Tutar"
                        secondary={`${(selectedCar.dailyPrice * calculateDays()).toLocaleString('tr-TR')} TL`}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><LocalGasStation color="primary" /></ListItemIcon>
                      <ListItemText
                        primary="Yakıt Seviyesi"
                        secondary="Dolu teslim alınacak"
                      />
                    </ListItem>
                  </List>
                </CardContent>
              </Card>
            )}
          </Box>
        );

      case 3:
        const totalPrice = selectedCar ? selectedCar.dailyPrice * calculateDays() : 0;
        const dailyPrice = selectedCar?.dailyPrice || 0;
        
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Ödeme Bilgileri
            </Typography>
            
            {/* Ödeme Yöntemi Seçimi */}
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <FormControl fullWidth required>
                  <InputLabel>Ödeme Yöntemi</InputLabel>
                  <Select
                    value={paymentMethod}
                    label="Ödeme Yöntemi"
                    onChange={(e) => setPaymentMethod(e.target.value as any)}
                  >
                    <MenuItem value="CREDIT_CARD">Kredi Kartı</MenuItem>
                    <MenuItem value="CASH">Nakit</MenuItem>
                    <MenuItem value="BANK_TRANSFER">Banka Havalesi</MenuItem>
                  </Select>
                </FormControl>
              </Grid>

              {/* Kredi Kartı Formu */}
              {paymentMethod === 'CREDIT_CARD' && (
                <>
                  <Grid item xs={12}>
                    <Divider sx={{ my: 2 }}>
                      <Chip label="Kredi Kartı Bilgileri" />
                    </Divider>
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      required
                      label="Kart Numarası"
                      placeholder="1234 5678 9012 3456"
                      value={cardNumber}
                      onChange={(e) => {
                        const value = e.target.value.replace(/\s/g, '');
                        if (value.length <= 16 && /^\d*$/.test(value)) {
                          setCardNumber(value);
                        }
                      }}
                      inputProps={{ maxLength: 16 }}
                      helperText="16 haneli kart numarası"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      required
                      label="Kart Üzerindeki İsim"
                      placeholder="JOHN DOE"
                      value={cardHolder}
                      onChange={(e) => setCardHolder(e.target.value.toUpperCase())}
                      helperText="Kart üzerinde yazan isim"
                    />
                  </Grid>
                  <Grid item xs={8}>
                    <TextField
                      fullWidth
                      required
                      label="Son Kullanma Tarihi"
                      placeholder="MM/YY"
                      value={cardExpiry}
                      onChange={(e) => {
                        const value = e.target.value.replace(/\D/g, '');
                        if (value.length <= 4) {
                          setCardExpiry(value);
                        }
                      }}
                      inputProps={{ maxLength: 4 }}
                      helperText="Ay/Yıl (Örn: 12/25)"
                    />
                  </Grid>
                  <Grid item xs={4}>
                    <TextField
                      fullWidth
                      required
                      label="CVV"
                      placeholder="123"
                      type="password"
                      value={cardCVV}
                      onChange={(e) => {
                        const value = e.target.value.replace(/\D/g, '');
                        if (value.length <= 3) {
                          setCardCVV(value);
                        }
                      }}
                      inputProps={{ maxLength: 3 }}
                      helperText="Güvenlik kodu"
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <Alert severity="info" icon={<AttachMoney />}>
                      <Typography variant="body2">
                        <strong>Güvenli Ödeme:</strong> Kart bilgileriniz şifrelenir ve güvenli bir şekilde işlenir.
                      </Typography>
                    </Alert>
                  </Grid>
                </>
              )}

              {/* Nakit veya Havale Bilgisi */}
              {(paymentMethod === 'CASH' || paymentMethod === 'BANK_TRANSFER') && (
                <Grid item xs={12}>
                  <Alert severity="warning">
                    <Typography variant="body2">
                      {paymentMethod === 'CASH' 
                        ? 'Nakit ödeme ile teslim sırasında ödeme yapabilirsiniz.'
                        : 'Banka havalesi ile ödeme yapmak için iletişime geçiniz.'}
                    </Typography>
                  </Alert>
                </Grid>
              )}

              {/* Özet Bilgiler */}
              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
              </Grid>
              <Grid item xs={12}>
                <Card sx={{ bgcolor: 'primary.dark', color: 'primary.contrastText' }}>
                  <CardContent>
                    <Typography variant="h5" gutterBottom>
                      Ödeme Özeti
                    </Typography>
                    <List dense sx={{ color: 'primary.contrastText' }}>
                      <ListItem>
                        <ListItemText
                          primary="Araç"
                          secondary={`${selectedCar?.brandName} ${selectedCar?.modelName}`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Günlük Fiyat"
                          secondary={`${dailyPrice.toLocaleString('tr-TR')} TL`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Kiralama Süresi"
                          secondary={`${calculateDays()} gün`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary={
                            <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                              TOPLAM TUTAR
                            </Typography>
                          }
                          secondary={
                            <Typography variant="h5" sx={{ fontWeight: 'bold', mt: 1 }}>
                              {totalPrice.toLocaleString('tr-TR')} TL
                            </Typography>
                          }
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12}>
                <Alert severity="info">
                  Ödeme yapıldıktan sonra kiralama kaydı otomatik olarak oluşturulacaktır.
                </Alert>
              </Grid>
            </Grid>
          </Box>
        );

      case 4:
        return (
          <Box sx={{ mt: 3, textAlign: 'center' }}>
            <CheckCircle sx={{ fontSize: 80, color: 'success.main', mb: 2 }} />
            <Typography variant="h5" gutterBottom>
              Kiralama Başarıyla Tamamlandı!
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mt: 2 }}>
              Kiralama numaranız: {completedRentalId}
            </Typography>
            <Button
              variant="contained"
              color="primary"
              sx={{ mt: 3 }}
              onClick={() => window.location.href = '/customer-portal'}
            >
              Portala Dön
            </Button>
          </Box>
        );

      default:
        return <div>Bilinmeyen adım</div>;
    }
  };

  return (
    <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto', p: 3 }}>
      <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
        Yeni Kiralama
      </Typography>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Stepper activeStep={activeStep} alternativeLabel>
          {steps.map((step) => (
            <Step key={step.label}>
              <StepLabel>{step.label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        <Box sx={{ mt: 4, minHeight: 300 }}>
          {renderStepContent(activeStep)}
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button
            disabled={activeStep === 0}
            onClick={handleBack}
          >
            Geri
          </Button>

          {activeStep === steps.length - 1 ? null : (
            <Button
              variant="contained"
              onClick={activeStep === 3 ? handleStep3Complete : activeStep === 2 ? handleStep2Complete : activeStep === 1 ? handleStep1Complete : handleStep0Complete}
              disabled={
                (activeStep === 0 && (!selectedCar || !startDate || !endDate)) ||
                (activeStep === 1 && !selectedTemplate) ||
                createRentalMutation.isLoading ||
                createContractMutation.isLoading ||
                createPaymentMutation.isLoading
              }
            >
              {activeStep === 3 ? 'Kiralama Oluştur ve Öde' : 'Devam Et'}
              {(createRentalMutation.isLoading || createContractMutation.isLoading || createPaymentMutation.isLoading) && (
                <CircularProgress size={20} sx={{ ml: 1 }} />
              )}
            </Button>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default CustomerRentalWorkflowPage;

