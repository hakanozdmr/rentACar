import React, { useState, useRef } from 'react';
import {
  Box,
  Stepper,
  Step,
  StepLabel,
  Typography,
  Paper,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
} from '@mui/material';
import {
  CheckCircle,
  DirectionsCar,
  Description,
  AttachMoney,
  Assignment,
  LocalGasStation,
  Photo,
} from '@mui/icons-material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  rentalsApi, 
  carsApi, 
  customersApi, 
  contractsApi,
  contractTemplatesApi,
  paymentsApi,
  invoicesApi,
  vehicleConditionChecksApi,
  rentalDocumentsApi,
  Car,
  Customer,
  ContractTemplate,
  Contract,
  Payment,
  Invoice,
  VehicleConditionCheck,
  RentalDocument,
  Rental,
} from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs, { Dayjs } from 'dayjs';
import isBetween from 'dayjs/plugin/isBetween';

dayjs.extend(isBetween);

const steps = [
  { label: 'Araç Seçimi' },
  { label: 'Sözleşme Şablonu' },
  { label: 'Sözleşme' },
  { label: 'Ödeme' },
  { label: 'Teslim' },
];

const RentalWorkflowPage: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [selectedRentalId, setSelectedRentalId] = useState<number | null>(null);
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Step 1: Car Selection
  const [selectedCar, setSelectedCar] = useState<Car | null>(null);
  const [startDate, setStartDate] = useState<Dayjs | null>(dayjs());
  const [endDate, setEndDate] = useState<Dayjs | null>(dayjs().add(3, 'day'));

  // Step 2: Customer & Details
  const [selectedCustomer, setSelectedCustomer] = useState<number>(0);
  const [specialRequests, setSpecialRequests] = useState('');
  const [dailyPrice, setDailyPrice] = useState(0);
  const [totalAmount, setTotalAmount] = useState(0);

  // Step 3: Contract
  const [selectedTemplate, setSelectedTemplate] = useState<number | null>(null);
  const [contract, setContract] = useState<Contract | null>(null);
  const [contractSigned, setContractSigned] = useState(false);

  // Step 4: Payment
  const [paymentMethod, setPaymentMethod] = useState<'CREDIT_CARD' | 'CASH' | 'BANK_TRANSFER'>('CREDIT_CARD');
  const [paymentCompleted, setPaymentCompleted] = useState(false);

  // Step 5: Delivery
  const [deliveryCheck, setDeliveryCheck] = useState<VehicleConditionCheck | null>(null);
  const [deliveryPhotos, setDeliveryPhotos] = useState<RentalDocument[]>([]);

  // Refs to avoid stale closures in mutation callbacks
  const selectedTemplateRef = useRef(selectedTemplate);
  const selectedCustomerRef = useRef(selectedCustomer);
  const totalAmountRef = useRef(totalAmount);
  const paymentMethodRef = useRef(paymentMethod);
  const selectedRentalIdRef = useRef(selectedRentalId);
  const selectedCarRef = useRef(selectedCar);

  // Update refs when state changes
  React.useEffect(() => {
    selectedTemplateRef.current = selectedTemplate;
  }, [selectedTemplate]);

  React.useEffect(() => {
    selectedCustomerRef.current = selectedCustomer;
  }, [selectedCustomer]);

  React.useEffect(() => {
    totalAmountRef.current = totalAmount;
  }, [totalAmount]);

  React.useEffect(() => {
    paymentMethodRef.current = paymentMethod;
  }, [paymentMethod]);

  React.useEffect(() => {
    selectedRentalIdRef.current = selectedRentalId;
  }, [selectedRentalId]);

  React.useEffect(() => {
    selectedCarRef.current = selectedCar;
  }, [selectedCar]);

  // Queries
  const { data: availableCars = [], isLoading: isLoadingCars } = useQuery(
    ['availableCars', startDate, endDate],
    () => {
      if (!startDate || !endDate) return Promise.resolve([]);
      return carsApi.getAll().then(res => {
        const cars = res.data.filter(car => car.state === 1); // Available
        return cars;
      });
    },
    { enabled: !!startDate && !!endDate }
  );

  const { data: customers = [] } = useQuery(
    'customers',
    () => customersApi.getAll().then(res => res.data)
  );

  const { data: templates = [] } = useQuery(
    'contract-templates',
    () => contractTemplatesApi.getAll().then(res => res.data)
  );

  const { data: rental, refetch: refetchRental } = useQuery(
    ['rental', selectedRentalId],
    () => rentalsApi.getById(selectedRentalId!),
    { enabled: !!selectedRentalId }
  );

  // Handlers
  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
  };

  // Mutations
  const createRentalMutation = useMutation(
    (rental: Partial<Rental>) => rentalsApi.create(rental as Rental),
    {
      onSuccess: (data) => {
        setSelectedRentalId(data.data.id || null);
        // After rental created, create contract
        if (selectedTemplateRef.current) {
          const contractData = {
            rentalId: data.data.id!,
            customerId: selectedCustomerRef.current,
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
        // After contract created, sign it
        signContractMutation.mutate({
          id: data.data.id!,
          customerSignature: 'customer-signature-' + Date.now(),
          companySignature: 'company-signature-' + Date.now(),
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
        setContractSigned(true);
        // After signing, create payment
        createPaymentMutation.mutate({
          rentalId: selectedRentalIdRef.current!,
          customerId: selectedCustomerRef.current,
          amount: totalAmountRef.current,
          method: paymentMethodRef.current,
          status: 'COMPLETED',
        } as Payment);
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
        setPaymentCompleted(true);
        // After payment, create delivery check
        createDeliveryCheckMutation.mutate({
          rentalId: selectedRentalIdRef.current!,
          carId: selectedCarRef.current!.id,
          checkType: 'TESLIM',
          mileageAtCheck: selectedCarRef.current!.mileage || 0,
          fuelLevel: 100,
          bodyHasDamage: false,
          interiorHasDamage: false,
          windowsHaveDamage: false,
          tiresHaveDamage: false,
          hasScratches: false,
          performedBy: 'Admin',
          performedAt: dayjs().format('YYYY-MM-DDTHH:mm'),
          isConfirmed: false,
          needsMaintenance: false,
        } as VehicleConditionCheck);
      },
      onError: (error: any) => {
        showError(error.message || 'Ödeme kaydedilirken hata oluştu');
      },
    }
  );

  const createDeliveryCheckMutation = useMutation(
    (check: Partial<VehicleConditionCheck>) => vehicleConditionChecksApi.create(check as VehicleConditionCheck),
    {
      onSuccess: () => {
        showSuccess('Kiralama işlemi başarıyla tamamlandı!');
        setTimeout(() => {
          window.location.href = '/rentals';
        }, 2000);
      },
      onError: (error: any) => {
        showError(error.message || 'Teslim kontrolü kaydedilirken hata oluştu');
      },
    }
  );

  const handleStep1Complete = () => {
    if (selectedCar && startDate && endDate && selectedCustomer) {
      const days = endDate.diff(startDate, 'day') + 1;
      setDailyPrice(selectedCar.dailyPrice);
      setTotalAmount(selectedCar.dailyPrice * days);
      // Just move to next step, don't create rental yet
      handleNext();
    } else {
      showError('Lütfen tüm alanları doldurun');
    }
  };

  const handleStep2Complete = () => {
    if (selectedTemplate) {
      // Just move to next step
      handleNext();
    } else {
      showError('Lütfen sözleşme şablonu seçin');
    }
  };

  const handleStep3Complete = () => {
    // Just move to next step
    handleNext();
  };

  const handleStep4Complete = () => {
    // Just move to next step
    handleNext();
  };

  const handleFinish = () => {
    if (selectedCar && startDate && endDate && selectedCustomer && selectedTemplate) {
      const days = endDate.diff(startDate, 'day') + 1;
      
      // NOW create all records in sequence
      createRentalMutation.mutate({
        carId: selectedCar.id,
        customerId: selectedCustomer,
        start: startDate.format('YYYY-MM-DD'),
        end: endDate.format('YYYY-MM-DD'),
        totalAmount: selectedCar.dailyPrice * days,
        specialRequests: specialRequests,
        status: 'CONFIRMED',
      });
      
      // After rental is created, create delivery check
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
                    minDate={dayjs()}
                    slotProps={{ textField: { fullWidth: true } }}
                  />
                </LocalizationProvider>
              </Grid>
              <Grid item xs={12} sm={6}>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DatePicker
                    label="Bitiş Tarihi"
                    value={endDate}
                    onChange={(newValue) => setEndDate(newValue)}
                    minDate={startDate || dayjs()}
                    slotProps={{ textField: { fullWidth: true } }}
                  />
                </LocalizationProvider>
              </Grid>
              <Grid item xs={12} sm={6}>
                {isLoadingCars ? (
                  <CircularProgress />
                ) : (
                  <FormControl fullWidth required>
                    <InputLabel>Araç Seçin</InputLabel>
                    <Select
                      value={selectedCar?.id || ''}
                      label="Araç Seçin"
                      onChange={(e) => {
                        const car = availableCars.find(c => c.id === e.target.value);
                        setSelectedCar(car || null);
                      }}
                    >
                      {availableCars.map((car) => (
                        <MenuItem key={car.id} value={car.id}>
                          {car.brandName} {car.modelName} - {car.plate} - {car.dailyPrice} TL/gün
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
              </Grid>
              <Grid item xs={12} sm={6}>
                <FormControl fullWidth required>
                  <InputLabel>Müşteri Seçin</InputLabel>
                  <Select
                    value={selectedCustomer}
                    label="Müşteri Seçin"
                    onChange={(e) => setSelectedCustomer(Number(e.target.value))}
                  >
                    {customers.map((customer) => (
                      <MenuItem key={customer.id} value={customer.id}>
                        {customer.firstName} {customer.lastName} - {customer.email}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              {selectedCar && (
                <Grid item xs={12}>
                  <Alert severity="info">
                    <strong>{selectedCar.brandName} {selectedCar.modelName}</strong><br />
                    Plaka: {selectedCar.plate} | Günlük: {selectedCar.dailyPrice} TL
                  </Alert>
                </Grid>
              )}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={2}
                  label="Özel İstekler (Opsiyonel)"
                  value={specialRequests}
                  onChange={(e) => setSpecialRequests(e.target.value)}
                  placeholder="Varsa özel isteklerinizi belirtin..."
                />
              </Grid>
            </Grid>
          </Box>
        );

      case 1:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Sözleşme Şablonu
            </Typography>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Alert severity="info">
                  Lütfen sözleşme şablonu seçin ve bilgilerinizi kontrol edin.
                </Alert>
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth required>
                  <InputLabel>Sözleşme Şablonu Seçin</InputLabel>
                  <Select
                    value={selectedTemplate || ''}
                    label="Sözleşme Şablonu Seçin"
                    onChange={(e) => setSelectedTemplate(Number(e.target.value))}
                  >
                    {templates.map((template) => (
                      <MenuItem key={template.id} value={template.id}>
                        {template.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <Paper sx={{ p: 2, bgcolor: 'grey.50' }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Özet
                  </Typography>
                  <List dense>
                    <ListItem>
                      <ListItemIcon><DirectionsCar /></ListItemIcon>
                      <ListItemText
                        primary="Araç"
                        secondary={selectedCar ? `${selectedCar.brandName} ${selectedCar.modelName} (${selectedCar.plate})` : 'Seçilmedi'}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemText
                        primary="Süre"
                        secondary={`${calculateDays()} gün`}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><AttachMoney /></ListItemIcon>
                      <ListItemText
                        primary="Günlük Fiyat"
                        secondary={`${dailyPrice} TL`}
                      />
                    </ListItem>
                    <ListItem>
                      <ListItemIcon><AttachMoney /></ListItemIcon>
                      <ListItemText
                        primary="Toplam Tutar"
                        secondary={
                          <Typography component="span" variant="h6" color="primary">
                            {totalAmount} TL
                          </Typography>
                        }
                      />
                    </ListItem>
                  </List>
                </Paper>
              </Grid>
            </Grid>
          </Box>
        );

      case 2:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Sözleşme Önizleme
            </Typography>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Alert severity="info">
                  Seçilen şablon ile sözleşme oluşturulacak.
                </Alert>
              </Grid>
              {selectedTemplate && templates.find(t => t.id === selectedTemplate) && (
                <Grid item xs={12}>
                  <Paper sx={{ p: 2, bgcolor: 'grey.50' }}>
                    <Typography variant="subtitle1" gutterBottom>
                      Şablon: {templates.find(t => t.id === selectedTemplate)?.name}
                    </Typography>
                    <Typography variant="body2">
                      Sözleşme işlem tamamlandığında oluşturulacak.
                    </Typography>
                  </Paper>
                </Grid>
              )}
            </Grid>
          </Box>
        );

      case 3:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Ödeme İşlemi
            </Typography>
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
              <Grid item xs={12}>
                <Paper sx={{ p: 2, bgcolor: 'primary.light' }}>
                  <Typography variant="h5" color="primary.contrastText">
                    Ödenecek Tutar: {totalAmount} TL
                  </Typography>
                </Paper>
              </Grid>
              <Grid item xs={12}>
                <Alert severity="info">
                  Ödeme işlem tamamlandığında kaydedilecek.
                </Alert>
              </Grid>
            </Grid>
          </Box>
        );

      case 4:
        return (
          <Box sx={{ mt: 3 }}>
            <Typography variant="h6" gutterBottom>
              Teslim İşlemi
            </Typography>
            <Alert severity="warning" sx={{ mb: 2 }}>
              "Tamamla" butonuna basarak tüm kayıtları oluşturun.
            </Alert>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="subtitle1" gutterBottom>
                      Teslim Kontrolü Bilgileri
                    </Typography>
                    <List dense>
                      <ListItem>
                        <ListItemIcon><DirectionsCar /></ListItemIcon>
                        <ListItemText
                          primary="Kilometre"
                          secondary={`${selectedCar?.mileage?.toLocaleString('tr-TR') || '0'} km`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon><LocalGasStation /></ListItemIcon>
                        <ListItemText
                          primary="Yakıt Seviyesi"
                          secondary="100%"
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemIcon><Photo /></ListItemIcon>
                        <ListItemText
                          primary="Fotoğraf"
                          secondary="Durum kontrolü kaydı oluşturulacak"
                        />
                      </ListItem>
                    </List>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12}>
                <Alert severity="info">
                  Tüm kayıtlar işlem tamamlandığında otomatik oluşturulacak: Kiralama, Sözleşme, Ödeme, Teslim Kontrolü
                </Alert>
              </Grid>
            </Grid>
          </Box>
        );

      default:
        return 'Bilinmeyen adım';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Yeni Kiralama İşlemi
      </Typography>
      
      <Paper sx={{ p: 3, mt: 3 }}>
        <Stepper activeStep={activeStep} alternativeLabel>
          {steps.map((step, index) => (
            <Step key={index} completed={activeStep > index}>
              <StepLabel>
                {step.label}
              </StepLabel>
            </Step>
          ))}
        </Stepper>

        <Divider sx={{ my: 3 }} />

        {renderStepContent(activeStep)}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button
            disabled={activeStep === 0}
            onClick={handleBack}
          >
            Geri
          </Button>
          {activeStep === steps.length - 1 ? (
            <Button
              variant="contained"
              onClick={handleFinish}
              disabled={createDeliveryCheckMutation.isLoading}
            >
              {createDeliveryCheckMutation.isLoading ? (
                <CircularProgress size={24} />
              ) : (
                'Tamamla'
              )}
            </Button>
          ) : (
            <Button
              variant="contained"
              onClick={() => {
                if (activeStep === 0) handleStep1Complete();
                else if (activeStep === 1) handleStep2Complete();
                else if (activeStep === 2) handleStep3Complete();
                else handleNext();
              }}
              disabled={
                (activeStep === 0 && (!selectedCar || !startDate || !endDate || !selectedCustomer)) ||
                (activeStep === 1 && (!selectedTemplate)) ||
                createRentalMutation.isLoading ||
                createContractMutation.isLoading ||
                createPaymentMutation.isLoading
              }
            >
              {createRentalMutation.isLoading || createContractMutation.isLoading || createPaymentMutation.isLoading ? (
                <CircularProgress size={24} />
              ) : (
                'Devam Et'
              )}
            </Button>
          )}
        </Box>
      </Paper>
    </Box>
  );
};

export default RentalWorkflowPage;

