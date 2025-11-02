import axios, { 
  InternalAxiosRequestConfig, 
  AxiosResponse, 
  AxiosError 
} from 'axios';

const API_BASE_URL = (process as any).env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Simple token validation utility
const isTokenValid = (token: string): boolean => {
  if (!token) return false;
  
  try {
    // JWT token has 3 parts separated by dots
    const parts = token.split('.');
    if (parts.length !== 3) return false;
    
    // Decode payload (second part)
    const payload = JSON.parse(atob(parts[1]));
    
    // Check if token is expired
    if (payload.exp && payload.exp * 1000 < Date.now()) {
      console.warn('Token is expired');
      return false;
    }
    
    return true;
  } catch (error) {
    console.warn('Invalid token format:', error);
    return false;
  }
};

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token) {
      // Check if token is valid before sending request
      if (isTokenValid(token)) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log('Valid token sent for request:', config.url, 'Authorization:', config.headers.Authorization);
      } else {
        console.warn('Invalid or expired token, clearing localStorage');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.dispatchEvent(new CustomEvent('auth-logout'));
        return Promise.reject(new Error('Token is invalid or expired'));
      }
    } else {
      console.warn('No token found for request:', config.url);
    }
    console.log('Final headers for request:', config.url, config.headers);
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Handle unauthorized - clear token but don't redirect immediately
      // Let the components handle the redirect through AuthContext
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // Dispatch a custom event to notify components about logout
      window.dispatchEvent(new CustomEvent('auth-logout'));
    }
    
    // Backend'den gelen error mesajını yakala ve frontend'e aktar
    if (error.response?.data) {
      const errorData = error.response.data as any;
      let errorMessage = 'Bir hata oluştu';
      
      // Spring Boot error response formatını kontrol et
      if (errorData.message) {
        errorMessage = errorData.message;
      } else if (typeof errorData === 'string') {
        errorMessage = errorData;
      }
      
      // Error'u frontend'e daha anlaşılır şekilde aktar
      const enhancedError = new Error(errorMessage);
      (enhancedError as any).response = error.response;
      (enhancedError as any).isAxiosError = true;
      
      return Promise.reject(enhancedError);
    }
    
    return Promise.reject(error);
  }
);

export interface Brand {
  id?: number;
  name: string;
}

export interface Model {
  id?: number;
  name: string;
  brandId: number;
  brandName?: string;
}

export interface Car {
  id?: number;
  plate: string;
  dailyPrice: number;
  modelYear: number;
  state: number;
  modelId: number;
  modelName?: string;
  brandName?: string;
  // Gelişmiş özellikler
  mileage?: number;
  fuelType?: string;
  transmission?: string;
  segment?: string;
  color?: string;
  features?: string[];
  images?: string[];
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  maintenanceHistory?: MaintenanceRecord[];
  insuranceExpiryDate?: string;
  insuranceCompany?: string;
  gpsLatitude?: number;
  gpsLongitude?: number;
  lastLocationUpdate?: string;
  // Rating bilgileri
  averageRating?: number;
  ratingCount?: number;
  ratings?: ReservationRating[];
}

export interface MaintenanceRecord {
  id?: number;
  carId: number;
  maintenanceDate: string;
  type: string;
  description: string;
  cost: number;
  mileage: number;
  serviceProvider: string;
}

export interface GpsLocation {
  carId: number;
  plate: string;
  brandName: string;
  modelName: string;
  latitude: number;
  longitude: number;
  lastUpdate: string;
  address?: string;
  speed?: number;
  isOnline?: boolean;
  batteryLevel?: number;
}

export interface CarFeature {
  id: string;
  name: string;
  category: string;
}

export interface Customer {
  id?: number;
  firstName: string;
  lastName: string;
  street: string;
  zipcode: number;
  city: string;
  phone: string;
  email: string;
  dateOfBirth: string;
  idNumber: string;
  driverLicenseNumber: string;
}

export interface Rental {
  id?: number;
  start: string;
  end: string;
  carId: number;
  customerId: number;
  extraCosts?: number;
  note?: string;
  carPlate?: string;
  carBrandName?: string;
  carModelName?: string;
  customerFirstName?: string;
  customerLastName?: string;
  dailyPrice?: number;
  totalPrice?: number;
  canRate?: boolean;
  isRated?: boolean;
  // Additional fields from Reservation
  status?: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'EXPIRED';
  totalAmount?: number;
  specialRequests?: string;
  confirmedAt?: string;
  cancelledAt?: string;
}

export interface User {
  id?: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password?: string;
  enabled?: boolean;
  roleNames?: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

// Analytics Interfaces
export interface RevenueReport {
  date: string;
  period: string;
  totalRevenue: number;
  totalRentals: number;
  averageRevenuePerRental?: number;
  dailyRate?: number;
  carBrand: string;
  carModel: string;
}

export interface CarAnalytics {
  carId: number;
  plate: string;
  brandName: string;
  modelName: string;
  totalRentals: number;
  totalRevenue: number;
  averageRating?: number;
  totalRentalDays: number;
  utilizationRate: number;
  averageDailyRevenue: number;
}

export interface AvailableCar {
  carId: number;
  carPlate: string;
  carBrandName: string;
  carModelName: string;
}

export interface CustomerSegment {
  segmentName: string;
  customerCount: number;
  totalRevenue: number;
  averageRevenuePerCustomer: number;
  averageRentalsPerCustomer?: number;
  totalRentals: number;
}

export interface DashboardStats {
  totalRevenue: number;
  monthlyRevenue: number;
  todayRevenue: number;
  totalActiveCars: number;
  totalRentals: number;
  activeRentals: number;
  totalCustomers: number;
  averageRentalDuration: number;
  monthlyRevenueData: RevenueReport[];
  topPerformingCars: CarAnalytics[];
  customerSegments: CustomerSegment[];
}

export interface TrendAnalysis {
  date: string;
  revenue: number;
  rentalCount: number;
  growthRate: number;
  trend: string;
}

// Brands API
export const brandsApi = {
  getAll: () => api.get<Brand[]>('/brands'),
  getById: (id: number) => api.get<Brand>(`/brands/${id}`),
  create: (brand: Brand) => api.post<Brand>('/brands', brand),
  update: (brand: Brand) => api.put<Brand>('/brands', brand),
  delete: (id: number) => api.delete(`/brands/${id}`),
};

// Models API
export const modelsApi = {
  getAll: () => api.get<Model[]>('/models'),
  getById: (id: number) => api.get<Model>(`/models/${id}`),
  create: (model: Model) => api.post<Model>('/models', model),
  update: (model: Model) => api.put<Model>('/models', model),
  delete: (id: number) => api.delete(`/models/${id}`),
  getByBrandId: (brandId: number) => api.get<Model[]>(`/models/brand/${brandId}`),
};

// Cars API
export const carsApi = {
  getAll: () => api.get<Car[]>('/cars'),
  getById: (id: number) => api.get<Car>(`/cars/${id}`),
  create: (car: Car) => api.post<Car>('/cars', car),
  update: (car: Car) => api.put<Car>('/cars', car),
  delete: (id: number) => api.delete(`/cars/${id}`),
  getByModelId: (modelId: number) => api.get<Car[]>(`/cars/model/${modelId}`),
  getByState: (state: number) => api.get<Car[]>(`/cars/state/${state}`),
  getByBrandId: (brandId: number) => api.get<Car[]>(`/cars/brand/${brandId}`),
  getByModelYear: (year: number) => api.get<Car[]>(`/cars/year/${year}`),
  // Gelişmiş filtreleme
  getBySegment: (segment: string) => api.get<Car[]>(`/cars/segment/${segment}`),
  getByTransmission: (transmission: string) => api.get<Car[]>(`/cars/transmission/${transmission}`),
  getByFuelType: (fuelType: string) => api.get<Car[]>(`/cars/fuel-type/${fuelType}`),
  getByStateAndSegment: (state: number, segment: string) => api.get<Car[]>(`/cars/state/${state}/segment/${segment}`),
  search: (searchTerm: string) => api.get<Car[]>(`/cars/search?q=${encodeURIComponent(searchTerm)}`),
  getByFeature: (feature: string) => api.get<Car[]>(`/cars/feature/${feature}`),
  getCarsWithUpcomingMaintenance: () => api.get<Car[]>('/cars/maintenance/upcoming'),
  getCarsWithExpiringInsurance: () => api.get<Car[]>('/cars/insurance/expiring'),
  // Bakım kayıtları
  getMaintenanceHistory: (carId: number) => api.get<MaintenanceRecord[]>(`/cars/${carId}/maintenance`),
  addMaintenanceRecord: (carId: number, maintenanceRecord: MaintenanceRecord) => 
    api.post<MaintenanceRecord>(`/cars/${carId}/maintenance`, maintenanceRecord),
  updateMaintenanceRecord: (maintenanceRecordId: number, maintenanceRecord: MaintenanceRecord) => 
    api.put<MaintenanceRecord>(`/cars/maintenance/${maintenanceRecordId}`, maintenanceRecord),
  deleteMaintenanceRecord: (maintenanceRecordId: number) => 
    api.delete(`/cars/maintenance/${maintenanceRecordId}`),
  // GPS güncelleme
  updateLocation: (carId: number, latitude: number, longitude: number) => 
    api.put<Car>(`/cars/${carId}/location?latitude=${latitude}&longitude=${longitude}`),
  // GPS konum bilgisi
  getLocation: (carId: number) => api.get<GpsLocation>(`/cars/${carId}/location`),
  // Yarıçap içindeki araçlar
  getCarsInRadius: (latitude: number, longitude: number, radius: number) =>
    api.get<Car[]>(`/cars/location/nearby?latitude=${latitude}&longitude=${longitude}&radius=${radius}`),
  // Son aktif araçlar
  getRecentCarLocations: (hours: number = 24) => 
    api.get<GpsLocation[]>(`/cars/location/recent?hours=${hours}`),
  // Araç konum geçmişi
  getLocationHistory: (carId: number, days: number = 7) =>
    api.get<GpsLocation[]>(`/cars/${carId}/location/history?days=${days}`),
  // Mesafe hesaplama
  calculateDistance: (lat1: number, lng1: number, lat2: number, lng2: number) =>
    api.get<number>(`/cars/location/distance?lat1=${lat1}&lng1=${lng1}&lat2=${lat2}&lng2=${lng2}`),
};

// Customers API
export const customersApi = {
  getAll: () => api.get<Customer[]>('/customers'),
  getById: (id: number) => api.get<Customer>(`/customers/${id}`),
  create: (customer: Customer) => api.post<Customer>('/customers', customer),
  update: (customer: Customer) => api.put<Customer>('/customers', customer),
  delete: (id: number) => api.delete(`/customers/${id}`),
  getByIdNumber: (idNumber: string) => api.get<Customer>(`/customers/id-number/${idNumber}`),
  getByDriverLicense: (licenseNumber: string) => api.get<Customer>(`/customers/driver-license/${licenseNumber}`),
  getByEmail: (email: string) => api.get<Customer>(`/customers/email/${email}`),
  getByCity: (city: string) => api.get<Customer[]>(`/customers/city/${city}`),
  searchByName: (name: string) => api.get<Customer[]>(`/customers/search?name=${name}`),
};

// Rentals API
export const rentalsApi = {
  getAll: () => api.get<Rental[]>('/rentals'),
  getById: (id: number) => api.get<Rental>(`/rentals/${id}`),
  create: (rental: Rental) => api.post<Rental>('/rentals', rental),
  update: (rental: Rental) => api.put<Rental>('/rentals', rental),
  delete: (id: number) => api.delete(`/rentals/${id}`),
  getByCarId: (carId: number) => api.get<Rental[]>(`/rentals/car/${carId}`),
  getByCustomerId: (customerId: number) => api.get<Rental[]>(`/rentals/customer/${customerId}`),
  findByDate: (date: string) => api.get<Rental[]>(`/rentals/date/${date}`),
  findActiveRentals: (date: string) => api.get<Rental[]>(`/rentals/active/${date}`),
  findBetweenDates: (startDate: string, endDate: string) => 
    api.get<Rental[]>(`/rentals/between?startDate=${startDate}&endDate=${endDate}`),
  checkAvailability: (carId: number, start: string, end: string) =>
    api.get<boolean>(`/rentals/availability/${carId}?start=${start}&end=${end}`),
};

// Auth API
export const authApi = {
  login: (loginRequest: LoginRequest) => api.post<LoginResponse>('/auth/login', loginRequest),
  register: (user: User) => api.post<User>('/auth/register', user),
};

// Analytics API
export const analyticsApi = {
  // Revenue Reports
  getMonthlyRevenue: (year: number) => api.get<RevenueReport[]>(`/analytics/revenue/monthly?year=${year}`),
  getYearlyRevenue: (startYear: number, endYear: number) => 
    api.get<RevenueReport[]>(`/analytics/revenue/yearly?startYear=${startYear}&endYear=${endYear}`),
  getDailyRevenue: (startDate: string, endDate: string) => 
    api.get<RevenueReport[]>(`/analytics/revenue/daily?startDate=${startDate}&endDate=${endDate}`),
  
  // Car Analytics
  getMostRentedCars: (limit: number = 10) => api.get<CarAnalytics[]>(`/analytics/cars/most-rented?limit=${limit}`),
  getTopRevenueCars: (limit: number = 10) => api.get<CarAnalytics[]>(`/analytics/cars/top-revenue?limit=${limit}`),
  getCarUtilizationStats: () => api.get<CarAnalytics[]>('/analytics/cars/utilization'),
  
  // Customer Segmentation
  getCustomerSegmentation: () => api.get<CustomerSegment[]>('/analytics/customers/segmentation'),
  
  // Dashboard
  getDashboardStats: () => api.get<DashboardStats>('/analytics/dashboard'),
  
  // Trend Analysis
  getRevenueTrend: (months: number = 12) => api.get<TrendAnalysis[]>(`/analytics/trends/revenue?months=${months}`),
  
  // Real-time Statistics
  getTodayRevenue: () => api.get<string>('/analytics/realtime/today-revenue'),
  getActiveRentalsCount: () => api.get<number>('/analytics/realtime/active-rentals'),
  getAverageRentalDuration: () => api.get<string>('/analytics/realtime/avg-rental-duration'),
};

// Customer Portal API
export interface Reservation {
  id?: number;
  customerId: number;
  customerName?: string;
  carId: number;
  carPlate?: string;
  carBrandName?: string;
  carModelName?: string;
  startDate: string;
  endDate: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'EXPIRED';
  totalAmount?: number;
  specialRequests?: string;
  note?: string;
  confirmedAt?: string;
  cancelledAt?: string;
  daysCount?: number;
}

export interface ReservationRating {
  id?: number;
  rentalId: number; // Direct rental rating after table merge
  customerId: number;
  customerName?: string;
  rating: number;
  comment?: string;
  carRating?: boolean;
  isPublic?: boolean;
  createdAt?: string;
  // Rental information for display
  carBrandName?: string;
  carModelName?: string;
  carPlate?: string;
}

export interface Notification {
  id?: number;
  customerId: number;
  customerName?: string;
  title: string;
  message: string;
  type: string;
  status: string;
  channel: string;
  sentAt?: string;
  readAt?: string;
  relatedEntityType?: string;
  relatedEntityId?: number;
  createdAt?: string;
}

// ==============================================
// FINANCIAL MANAGEMENT INTERFACES
// ==============================================

export interface Payment {
  id?: number;
  rentalId: number;
  customerId: number;
  customerName?: string;
  customerEmail?: string;
  carPlate?: string;
  carBrandName?: string;
  carModelName?: string;
  amount: number;
  method: 'CREDIT_CARD' | 'BANK_TRANSFER' | 'CASH' | 'CHECK' | 'DIGITAL_WALLET' | 'ONLINE_BANKING';
  methodDisplayName?: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REFUNDED' | 'PARTIAL';
  statusDisplayName?: string;
  dueDate?: string;
  paidAt?: string;
  transactionId?: string;
  paymentReference?: string;
  notes?: string;
  isOverdue?: boolean;
  daysUntilDue?: number;
  createdAt?: string;
}

export interface Invoice {
  id?: number;
  rentalId: number;
  customerId: number;
  customerName?: string;
  customerEmail?: string;
  customerAddress?: string;
  carPlate?: string;
  carBrandName?: string;
  carModelName?: string;
  rentalStartDate?: string;
  rentalEndDate?: string;
  rentalDays?: number;
  invoiceNumber: string;
  issueDate: string;
  dueDate: string;
  subtotal: number;
  taxRate?: number;
  taxAmount?: number;
  totalAmount: number;
  status: 'PENDING' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  statusDisplayName?: string;
  paidAt?: string;
  paymentTerms?: string;
  notes?: string;
  referenceNumber?: string;
  isOverdue?: boolean;
  daysUntilDue?: number;
  daysOverdue?: number;
  createdAt?: string;
}

export interface GeneralLedger {
  id?: number;
  transactionType: 'REVENUE' | 'EXPENSE' | 'ASSET' | 'LIABILITY' | 'EQUITY' | 'TRANSFER';
  transactionTypeDisplayName?: string;
  accountType: 'CASH_ASSET' | 'BANK_ASSET' | 'ACCOUNTS_RECEIVABLE' | 'INVENTORY_ASSET' | 'FIXED_ASSET' | 
              'ACCOUNTS_PAYABLE' | 'TAX_PAYABLE' | 'SHORT_TERM_DEBT' | 'LONG_TERM_DEBT' |
              'CAPITAL' | 'RETAINED_EARNINGS' | 'RENTAL_REVENUE' | 'OTHER_REVENUE' |
              'OPERATING_EXPENSE' | 'MAINTENANCE_EXPENSE' | 'ADMINISTRATIVE_EXPENSE' | 
              'TAX_EXPENSE' | 'FUEL_EXPENSE' | 'INSURANCE_EXPENSE';
  accountTypeDisplayName?: string;
  accountCode: string;
  accountName: string;
  transactionDate: string;
  description?: string;
  debitAmount: number;
  creditAmount: number;
  referenceId?: number;
  referenceType?: string;
  documentNumber?: string;
  reconciled: boolean;
  reconciledAt?: string;
  balance?: number;
  relatedEntityInfo?: string;
  createdAt?: string;
}

export interface TaxCalculation {
  id?: number;
  invoiceId?: number;
  paymentId?: number;
  taxType: 'VAT' | 'CORPORATE_TAX' | 'WITHHOLDING_TAX' | 'STAMP_TAX' | 'MUNICIPAL_TAX';
  taxTypeDisplayName?: string;
  taxableAmount: number;
  taxRate: number;
  taxAmount: number;
  calculationDate: string;
  calculationDetails?: string;
  isReported: boolean;
  reportedAt?: string;
  taxPeriod?: string;
  taxPercentage?: number;
  relatedEntityInfo?: string;
  createdAt?: string;
}

export interface AuditLog {
  id?: number;
  entityName: string;
  entityId?: number;
  actionType: 'CREATE' | 'UPDATE' | 'DELETE' | 'READ' | 'LOGIN' | 'LOGOUT' | 'ACCESS_DENIED' | 'EXPORT' | 'IMPORT' | 'BACKUP' | 'RESTORE' | 'BULK_UPDATE' | 'BULK_DELETE';
  actionTypeDisplayName?: string;
  userId?: number;
  username?: string;
  userEmail?: string;
  ipAddress?: string;
  userAgent?: string;
  requestMethod?: string;
  requestUrl?: string;
  oldValues?: string;
  newValues?: string;
  changedFields?: string;
  sessionId?: string;
  operationResult?: string;
  errorMessage?: string;
  executionTimeMs?: number;
  timestamp: string;
  additionalInfo?: string;
  createdBy?: string;
  createdDate?: string;
  updateBy?: string;
  updateDate?: string;
}

export interface AuditLogFilters {
  entityName?: string;
  actionType?: string;
  userId?: number;
  operationResult?: string;
  startDate?: string;
  endDate?: string;
}

export interface AuditLogPageResponse {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Financial Reports
export interface FinancialSummary {
  totalRevenue: number;
  totalInvoiced: number;
  totalPaid: number;
  pendingPayments: number;
  overduePayments: number;
  pendingInvoices: number;
  overdueInvoices: number;
  totalTaxLiability: number;
  netProfit: number;
}

export interface PaymentSummary {
  totalAmount: number;
  count: number;
  pendingAmount: number;
  completedAmount: number;
  overdueAmount: number;
  byMethod: { [key: string]: { amount: number; count: number } };
}

export interface InvoiceSummary {
  totalAmount: number;
  count: number;
  pendingAmount: number;
  paidAmount: number;
  overdueAmount: number;
  byStatus: { [key: string]: { amount: number; count: number } };
}

export const customerPortalApi = {
  // Profile
  getProfile: () => api.get<Customer>('/customer-portal/profile'),
  updateProfile: (customer: Customer) => api.put<Customer>('/customer-portal/profile', customer),
  
  // Reservations
  getMyReservations: () => api.get<Reservation[]>('/customer-portal/reservations'),
  createReservation: (reservation: Reservation) => api.post<Reservation>('/customer-portal/reservations', reservation),
  cancelReservation: (id: number) => api.post<Reservation>(`/customer-portal/reservations/${id}/cancel`),
  getAvailableCars: (startDate: string, endDate: string) => 
    api.get<AvailableCar[]>(`/customer-portal/available-cars?startDate=${startDate}&endDate=${endDate}`),
  
  // Ratings
  rateReservation: (rating: ReservationRating) => api.post<ReservationRating>('/customer-portal/ratings', rating),
  getMyRatings: () => api.get<ReservationRating[]>('/customer-portal/ratings'),
  getCarRatings: (carId: number) => api.get<ReservationRating[]>(`/customer-portal/cars/${carId}/ratings`),
  
  // Notifications
  getNotifications: () => api.get<Notification[]>('/customer-portal/notifications'),
  getUnreadNotifications: () => api.get<Notification[]>('/customer-portal/notifications/unread'),
  markNotificationAsRead: (id: number) => api.post<Notification>(`/customer-portal/notifications/${id}/read`),
  markAllNotificationsAsRead: () => api.post('/customer-portal/notifications/read-all'),
  getUnreadNotificationCount: () => api.get<number>('/customer-portal/notifications/unread-count'),
  
  // Rentals
  getMyRentals: () => api.get<Rental[]>('/customer-portal/rentals'),
};

export const reservationsApi = {
  getAll: () => api.get<Reservation[]>('/reservations'),
  getById: (id: number) => api.get<Reservation>(`/reservations/${id}`),
  getByCustomerId: (customerId: number) => api.get<Reservation[]>(`/reservations/customer/${customerId}`),
  getByCarId: (carId: number) => api.get<Reservation[]>(`/reservations/car/${carId}`),
  getPending: () => api.get<Reservation[]>('/reservations/pending'),
  create: (reservation: Reservation) => api.post<Reservation>('/reservations', reservation),
  update: (id: number, reservation: Reservation) => api.put<Reservation>(`/reservations/${id}`, reservation),
  delete: (id: number) => api.delete(`/reservations/${id}`),
  confirm: (id: number) => api.post<Reservation>(`/reservations/${id}/confirm`),
  cancel: (id: number) => api.post<Reservation>(`/reservations/${id}/cancel`),
};

// ==============================================
// FINANCIAL MANAGEMENT APIs
// ==============================================

// Payments API
export const paymentsApi = {
  getAll: () => api.get<Payment[]>('/payments'),
  getById: (id: number) => api.get<Payment>(`/payments/${id}`),
  getByRentalId: (rentalId: number) => api.get<Payment[]>(`/payments/rental/${rentalId}`),
  getByCustomerId: (customerId: number) => api.get<Payment[]>(`/payments/customer/${customerId}`),
  getPending: () => api.get<Payment[]>('/payments/pending'),
  getOverdue: () => api.get<Payment[]>('/payments/overdue'),
  getCompleted: () => api.get<Payment[]>('/payments/completed'),
  create: (payment: Payment) => api.post<Payment>('/payments', payment),
  update: (payment: Payment) => api.put<Payment>('/payments', payment),
  delete: (id: number) => api.delete(`/payments/${id}`),
  markAsCompleted: (id: number) => api.post<Payment>(`/payments/${id}/complete`),
  getSummary: () => api.get<PaymentSummary>('/payments/summary'),
  getTotalRevenue: (startDate: string, endDate: string) => 
    api.get<number>(`/payments/revenue?startDate=${startDate}&endDate=${endDate}`),
};

// Invoices API
export const invoicesApi = {
  getAll: () => api.get<Invoice[]>('/invoices'),
  getById: (id: number) => api.get<Invoice>(`/invoices/${id}`),
  getByRentalId: (rentalId: number) => api.get<Invoice[]>(`/invoices/rental/${rentalId}`),
  getByCustomerId: (customerId: number) => api.get<Invoice[]>(`/invoices/customer/${customerId}`),
  getPending: () => api.get<Invoice[]>('/invoices/pending'),
  getOverdue: () => api.get<Invoice[]>('/invoices/overdue'),
  getPaid: () => api.get<Invoice[]>('/invoices/paid'),
  getByNumber: (invoiceNumber: string) => api.get<Invoice>(`/invoices/number/${invoiceNumber}`),
  create: (invoice: Invoice) => api.post<Invoice>('/invoices', invoice),
  update: (invoice: Invoice) => api.put<Invoice>('/invoices', invoice),
  delete: (id: number) => api.delete(`/invoices/${id}`),
  markAsPaid: (id: number) => api.put<Invoice>(`/invoices/${id}/mark-paid`),
  markAsSent: (id: number) => api.put<Invoice>(`/invoices/${id}/mark-sent`),
  getSummary: () => api.get<InvoiceSummary>('/invoices/summary'),
  getTotalInvoicedAmount: (startDate: string, endDate: string) => 
    api.get<number>(`/invoices/total-amount?startDate=${startDate}&endDate=${endDate}`),
};

// General Ledger API
export const generalLedgerApi = {
  getAll: () => api.get<GeneralLedger[]>('/general-ledger'),
  getById: (id: number) => api.get<GeneralLedger>(`/general-ledger/${id}`),
  getByAccountType: (accountType: string) => api.get<GeneralLedger[]>(`/general-ledger/account-type/${accountType}`),
  getByTransactionType: (transactionType: string) => api.get<GeneralLedger[]>(`/general-ledger/transaction-type/${transactionType}`),
  getByDateRange: (startDate: string, endDate: string) => 
    api.get<GeneralLedger[]>(`/general-ledger/date-range?startDate=${startDate}&endDate=${endDate}`),
  getUnreconciled: () => api.get<GeneralLedger[]>('/general-ledger/unreconciled'),
  getTrialBalance: () => api.get<GeneralLedger[]>('/general-ledger/trial-balance'),
  getIncomeStatement: (startDate: string, endDate: string) => 
    api.get<any>(`/general-ledger/income-statement?startDate=${startDate}&endDate=${endDate}`),
  getBalanceSheet: (date: string) => 
    api.get<any>(`/general-ledger/balance-sheet?date=${date}`),
  getCashFlowStatement: (startDate: string, endDate: string) => 
    api.get<any>(`/general-ledger/cash-flow?startDate=${startDate}&endDate=${endDate}`),
  create: (entry: GeneralLedger) => api.post<GeneralLedger>('/general-ledger', entry),
  update: (entry: GeneralLedger) => api.put<GeneralLedger>('/general-ledger', entry),
  delete: (id: number) => api.delete(`/general-ledger/${id}`),
  markAsReconciled: (id: number) => api.post<GeneralLedger>(`/general-ledger/${id}/reconcile`),
  closePeriod: (periodEnd: string) => api.post('/general-ledger/close-period', { periodEnd }),
};

// Tax Calculations API
export const taxCalculationsApi = {
  getAll: () => api.get<TaxCalculation[]>('/tax-calculations'),
  getById: (id: number) => api.get<TaxCalculation>(`/tax-calculations/${id}`),
  getByInvoiceId: (invoiceId: number) => api.get<TaxCalculation[]>(`/tax-calculations/invoice/${invoiceId}`),
  getByPaymentId: (paymentId: number) => api.get<TaxCalculation[]>(`/tax-calculations/payment/${paymentId}`),
  getByTaxType: (taxType: string) => api.get<TaxCalculation[]>(`/tax-calculations/tax-type/${taxType}`),
  getByPeriod: (period: string) => api.get<TaxCalculation[]>(`/tax-calculations/period/${period}`),
  getUnreported: () => api.get<TaxCalculation[]>('/tax-calculations/unreported'),
  calculateVAT: (amount: number, rate?: number) => 
    api.post<TaxCalculation>('/tax-calculations/vat', { amount, rate }),
  calculateCorporateTax: (amount: number, rate?: number) => 
    api.post<TaxCalculation>('/tax-calculations/corporate-tax', { amount, rate }),
  create: (calculation: TaxCalculation) => api.post<TaxCalculation>('/tax-calculations', calculation),
  update: (calculation: TaxCalculation) => api.put<TaxCalculation>('/tax-calculations', calculation),
  delete: (id: number) => api.delete(`/tax-calculations/${id}`),
  markAsReported: (id: number) => api.post<TaxCalculation>(`/tax-calculations/${id}/mark-reported`),
  getTaxReport: (startDate: string, endDate: string) => 
    api.get<any>(`/tax-calculations/report?startDate=${startDate}&endDate=${endDate}`),
};

// Financial Reports API
export const financialReportsApi = {
  getDashboardSummary: (startDate: string, endDate: string) => 
    api.get<FinancialSummary>(`/financial-reports/dashboard-summary?startDate=${startDate}&endDate=${endDate}`),
  getTrialBalance: () => api.get<any[]>('/financial-reports/trial-balance'),
  getIncomeStatement: (startDate: string, endDate: string) => 
    api.get<any>('/financial-reports/income-statement', { params: { startDate, endDate } }),
  getBalanceSheet: (date: string) => 
    api.get<any>('/financial-reports/balance-sheet', { params: { date } }),
  getCashFlowStatement: (startDate: string, endDate: string) => 
    api.get<any>('/financial-reports/cash-flow', { params: { startDate, endDate } }),
  getTaxReport: (startDate: string, endDate: string) => 
    api.get<any>('/financial-reports/tax-report', { params: { startDate, endDate } }),
};

// Audit Logs API
export const auditLogsApi = {
  getAll: (page: number = 0, size: number = 25, filters?: AuditLogFilters) => 
    api.get<AuditLogPageResponse>('/audit-logs/filtered', { 
      params: { page, size, ...filters } 
    }),
  getByEntityName: (entityName: string) => 
    api.get<AuditLog[]>(`/audit-logs/entity/${entityName}`),
  getByEntityNameAndId: (entityName: string, entityId: number) => 
    api.get<AuditLog[]>(`/audit-logs/entity/${entityName}/${entityId}`),
  getByUserId: (userId: number) => 
    api.get<AuditLog[]>(`/audit-logs/user/${userId}`),
  getByUsername: (username: string) => 
    api.get<AuditLog[]>(`/audit-logs/username/${username}`),
  getByActionType: (actionType: string) => 
    api.get<AuditLog[]>(`/audit-logs/action/${actionType}`),
  getByDateRange: (startDate: string, endDate: string) => 
    api.get<AuditLog[]>('/audit-logs/date-range', { 
      params: { startDate, endDate } 
    }),
  getRecent: (limit: number = 50) => 
    api.get<AuditLog[]>(`/audit-logs/recent?limit=${limit}`),
  getFailedOperations: () => 
    api.get<AuditLog[]>('/audit-logs/failed'),
  getStatistics: () => 
    api.get<any>('/audit-logs/statistics'),
  cleanupOldLogs: (daysToKeep: number = 365) => 
    api.delete(`/audit-logs/cleanup?daysToKeep=${daysToKeep}`),
};

// ==============================================
// CONTRACT MANAGEMENT INTERFACES & API
// ==============================================

export interface Contract {
  id?: number;
  rentalId: number;
  customerId: number;
  templateId?: number;
  contractNumber: string;
  signedDate: string;
  signedAt?: string;
  status: 'DRAFT' | 'PENDING_SIGNATURE' | 'SIGNED' | 'EXPIRED' | 'CANCELLED' | 'VERIFIED';
  terms?: string;
  conditions?: string;
  customerSignature?: string;
  companySignature?: string;
  signedBy?: string;
  eSignatureVerifiedAt?: string;
  eSignatureHash?: string;
  pdfPath?: string;
  witnessName?: string;
  expiryDate?: string;
  notes?: string;
  customerName?: string;
  rentalInfo?: string;
  templateName?: string;
}

export interface ContractTemplate {
  id?: number;
  name: string;
  description?: string;
  templateKey: string;
  content: string;
  isActive: boolean;
  isDefault: boolean;
  version?: number;
  variables?: string;
  lastUsedAt?: string;
  usageCount?: number;
}

export interface RentalDocument {
  id?: number;
  rentalId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  filePath: string;
  documentType: 'DELIVERY_PHOTO' | 'PICKUP_PHOTO' | 'DAMAGE_REPORT' | 'CONTRACT' | 'ID_CARD' | 'DRIVER_LICENSE' | 'INSURANCE' | 'CONDITION_CHECK' | 'SIGNATURE' | 'OTHER';
  description?: string;
  uploadedAt?: string;
  uploadedBy?: string;
  thumbnailPath?: string;
  isVerified: boolean;
  verifiedAt?: string;
  verifiedBy?: string;
  metadata?: string;
  rentalInfo?: string;
}

export interface VehicleConditionCheck {
  id?: number;
  rentalId: number;
  carId: number;
  checkType: 'TESLIM' | 'TESLIM_ALMA';
  mileageAtCheck: number;
  fuelLevel: number;
  bodyHasDamage: boolean;
  bodyDamageDescription?: string;
  interiorHasDamage: boolean;
  interiorDamageDescription?: string;
  windowsHaveDamage: boolean;
  windowsDamageDescription?: string;
  tiresHaveDamage: boolean;
  tiresDamageDescription?: string;
  hasScratches: boolean;
  scratchesDescription?: string;
  damageCost?: number;
  performedBy: string;
  performedAt: string;
  customerNote?: string;
  staffNote?: string;
  isConfirmed: boolean;
  confirmedAt?: string;
  needsMaintenance: boolean;
  maintenanceNote?: string;
  rentalInfo?: string;
  carPlate?: string;
}

// Contracts API
export const contractsApi = {
  getAll: () => api.get<Contract[]>('/contracts'),
  getById: (id: number) => api.get<Contract>(`/contracts/${id}`),
  getByContractNumber: (contractNumber: string) => api.get<Contract>(`/contracts/number/${contractNumber}`),
  getByRentalId: (rentalId: number) => api.get<Contract[]>(`/contracts/rental/${rentalId}`),
  getByCustomerId: (customerId: number) => api.get<Contract[]>(`/contracts/customer/${customerId}`),
  getByStatus: (status: string) => api.get<Contract[]>(`/contracts/status/${status}`),
  create: (contract: Contract) => api.post<Contract>('/contracts', contract),
  update: (contract: Contract) => api.put<Contract>('/contracts', contract),
  delete: (id: number) => api.delete(`/contracts/${id}`),
  signContract: (id: number, customerSignature: string, companySignature: string) => 
    api.post<Contract>(`/contracts/${id}/sign?customerSignature=${encodeURIComponent(customerSignature)}&companySignature=${encodeURIComponent(companySignature)}`),
  verifyESignature: (id: number, eSignatureHash: string) => 
    api.post<Contract>(`/contracts/${id}/verify?eSignatureHash=${eSignatureHash}`),
  markAsExpired: () => api.post('/contracts/expire'),
};

// Contract Templates API
export const contractTemplatesApi = {
  getAll: () => api.get<ContractTemplate[]>('/contract-templates'),
  getActive: () => api.get<ContractTemplate[]>('/contract-templates/active'),
  getDefault: () => api.get<ContractTemplate>('/contract-templates/default'),
  getById: (id: number) => api.get<ContractTemplate>(`/contract-templates/${id}`),
  getByTemplateKey: (templateKey: string) => api.get<ContractTemplate>(`/contract-templates/key/${templateKey}`),
  create: (template: ContractTemplate) => api.post<ContractTemplate>('/contract-templates', template),
  update: (template: ContractTemplate) => api.put<ContractTemplate>('/contract-templates', template),
  delete: (id: number) => api.delete(`/contract-templates/${id}`),
  setAsDefault: (id: number) => api.post<ContractTemplate>(`/contract-templates/${id}/set-default`),
  replaceVariables: (content: string, variables: Record<string, string>) => 
    api.post<string>('/contract-templates/replace-variables', variables, {
      params: { content }
    }),
};

// Rental Documents API
export const rentalDocumentsApi = {
  getAll: () => api.get<RentalDocument[]>('/rental-documents'),
  getById: (id: number) => api.get<RentalDocument>(`/rental-documents/${id}`),
  getByRentalId: (rentalId: number) => api.get<RentalDocument[]>(`/rental-documents/rental/${rentalId}`),
  getByRentalIdAndType: (rentalId: number, documentType: string) => 
    api.get<RentalDocument[]>(`/rental-documents/rental/${rentalId}/type/${documentType}`),
  create: (document: RentalDocument) => api.post<RentalDocument>('/rental-documents', document),
  update: (document: RentalDocument) => api.put<RentalDocument>('/rental-documents', document),
  delete: (id: number) => api.delete(`/rental-documents/${id}`),
  verify: (id: number, verifiedBy: string) => 
    api.post<RentalDocument>(`/rental-documents/${id}/verify?verifiedBy=${encodeURIComponent(verifiedBy)}`),
  uploadFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<string>('/rental-documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

// Vehicle Condition Checks API
export const vehicleConditionChecksApi = {
  getAll: () => api.get<VehicleConditionCheck[]>('/vehicle-condition-checks'),
  getById: (id: number) => api.get<VehicleConditionCheck>(`/vehicle-condition-checks/${id}`),
  getByRentalId: (rentalId: number) => api.get<VehicleConditionCheck[]>(`/vehicle-condition-checks/rental/${rentalId}`),
  getByCarId: (carId: number) => api.get<VehicleConditionCheck[]>(`/vehicle-condition-checks/car/${carId}`),
  getByCheckType: (checkType: string) => api.get<VehicleConditionCheck[]>(`/vehicle-condition-checks/type/${checkType}`),
  create: (check: VehicleConditionCheck) => api.post<VehicleConditionCheck>('/vehicle-condition-checks', check),
  update: (check: VehicleConditionCheck) => api.put<VehicleConditionCheck>('/vehicle-condition-checks', check),
  delete: (id: number) => api.delete(`/vehicle-condition-checks/${id}`),
  getLatestDeliveryCheck: (rentalId: number) => 
    api.get<VehicleConditionCheck>(`/vehicle-condition-checks/rental/${rentalId}/latest-delivery`),
  getLatestPickupCheck: (rentalId: number) => 
    api.get<VehicleConditionCheck>(`/vehicle-condition-checks/rental/${rentalId}/latest-pickup`),
  confirmByCustomer: (id: number) => api.post<VehicleConditionCheck>(`/vehicle-condition-checks/${id}/confirm`),
  compareDeliveryAndPickup: (rentalId: number) => 
    api.get<VehicleConditionCheck>(`/vehicle-condition-checks/rental/${rentalId}/compare`),
};

export default api;
