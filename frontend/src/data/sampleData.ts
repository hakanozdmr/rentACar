// Frontend test için örnek veriler
export const sampleBrands = [
  { id: 1, name: 'Toyota' },
  { id: 2, name: 'Ford' },
  { id: 3, name: 'BMW' },
  { id: 4, name: 'Mercedes-Benz' },
  { id: 5, name: 'Audi' },
  { id: 6, name: 'Volkswagen' },
  { id: 7, name: 'Honda' },
  { id: 8, name: 'Hyundai' },
  { id: 9, name: 'Renault' },
  { id: 10, name: 'Peugeot' }
];

export const sampleModels = [
  { id: 1, name: 'Corolla', brandId: 1, brandName: 'Toyota' },
  { id: 2, name: 'Camry', brandId: 1, brandName: 'Toyota' },
  { id: 3, name: 'RAV4', brandId: 1, brandName: 'Toyota' },
  { id: 4, name: 'Focus', brandId: 2, brandName: 'Ford' },
  { id: 5, name: 'Mustang', brandId: 2, brandName: 'Ford' },
  { id: 6, name: 'Explorer', brandId: 2, brandName: 'Ford' },
  { id: 7, name: '3 Series', brandId: 3, brandName: 'BMW' },
  { id: 8, name: '5 Series', brandId: 3, brandName: 'BMW' },
  { id: 9, name: 'X3', brandId: 3, brandName: 'BMW' },
  { id: 10, name: 'C-Class', brandId: 4, brandName: 'Mercedes-Benz' }
];

export const sampleCars = [
  {
    id: 1,
    plate: '34ABC123',
    dailyPrice: 450.00,
    modelYear: 2023,
    state: 1,
    modelId: 1,
    modelName: 'Corolla',
    brandName: 'Toyota'
  },
  {
    id: 2,
    plate: '06DEF456',
    dailyPrice: 550.00,
    modelYear: 2023,
    state: 1,
    modelId: 2,
    modelName: 'Camry',
    brandName: 'Toyota'
  },
  {
    id: 3,
    plate: '35GHI789',
    dailyPrice: 600.00,
    modelYear: 2023,
    state: 1,
    modelId: 3,
    modelName: 'RAV4',
    brandName: 'Toyota'
  },
  {
    id: 4,
    plate: '16JKL012',
    dailyPrice: 400.00,
    modelYear: 2022,
    state: 2,
    modelId: 4,
    modelName: 'Focus',
    brandName: 'Ford'
  },
  {
    id: 5,
    plate: '01MNO345',
    dailyPrice: 800.00,
    modelYear: 2023,
    state: 1,
    modelId: 5,
    modelName: 'Mustang',
    brandName: 'Ford'
  }
];

export const sampleCustomers = [
  {
    id: 1,
    firstName: 'Ahmet',
    lastName: 'Yılmaz',
    street: 'Atatürk Caddesi No:45 Daire:12',
    zipcode: 34000,
    city: 'İstanbul',
    phone: '+90 532 123 4567',
    email: 'ahmet.yilmaz@email.com',
    dateOfBirth: '1985-03-15',
    idNumber: '12345678901',
    driverLicenseNumber: 'A1234567'
  },
  {
    id: 2,
    firstName: 'Ayşe',
    lastName: 'Kaya',
    street: 'Cumhuriyet Bulvarı No:78',
    zipcode: 35200,
    city: 'İzmir',
    phone: '+90 555 987 6543',
    email: 'ayse.kaya@email.com',
    dateOfBirth: '1990-07-22',
    idNumber: '23456789012',
    driverLicenseNumber: 'B2345678'
  },
  {
    id: 3,
    firstName: 'Mehmet',
    lastName: 'Demir',
    street: 'Millet Caddesi No:123',
    zipcode: 35000,
    city: 'İzmir',
    phone: '+90 533 456 7890',
    email: 'mehmet.demir@email.com',
    dateOfBirth: '1982-11-08',
    idNumber: '34567890123',
    driverLicenseNumber: 'C3456789'
  },
  {
    id: 4,
    firstName: 'Fatma',
    lastName: 'Özkan',
    street: 'Şehitler Caddesi No:67',
    zipcode: 16100,
    city: 'Bursa',
    phone: '+90 534 567 8901',
    email: 'fatma.ozkan@email.com',
    dateOfBirth: '1988-05-30',
    idNumber: '45678901234',
    driverLicenseNumber: 'D4567890'
  },
  {
    id: 5,
    firstName: 'Ali',
    lastName: 'Şahin',
    street: 'Gazi Mustafa Kemal Bulvarı No:89',
    zipcode: 42000,
    city: 'Konya',
    phone: '+90 535 678 9012',
    email: 'ali.sahin@email.com',
    dateOfBirth: '1983-09-14',
    idNumber: '56789012345',
    driverLicenseNumber: 'E5678901'
  }
];

export const sampleRentals = [
  {
    id: 1,
    start: '2024-01-15',
    end: '2024-01-20',
    carId: 4,
    customerId: 1,
    extraCosts: 150,
    note: 'Havalimanı teslim',
    carPlate: '16JKL012',
    customerFirstName: 'Ahmet',
    customerLastName: 'Yılmaz',
    dailyPrice: 400.00,
    totalPrice: 2150.00
  },
  {
    id: 2,
    start: '2024-01-18',
    end: '2024-01-25',
    carId: 9,
    customerId: 2,
    extraCosts: 0,
    note: null,
    carPlate: '35YZA567',
    customerFirstName: 'Ayşe',
    customerLastName: 'Kaya',
    dailyPrice: 900.00,
    totalPrice: 6300.00
  },
  {
    id: 3,
    start: '2024-02-01',
    end: '2024-02-03',
    carId: 19,
    customerId: 3,
    extraCosts: 75,
    note: 'Şehir merkezi teslim',
    carPlate: '34CDE567',
    customerFirstName: 'Mehmet',
    customerLastName: 'Demir',
    dailyPrice: 450.00,
    totalPrice: 975.00
  }
];

// Test için kullanılabilecek yardımcı fonksiyonlar
export const getAvailableCars = () => sampleCars.filter(car => car.state === 1);
export const getRentedCars = () => sampleCars.filter(car => car.state === 2);
export const getMaintenanceCars = () => sampleCars.filter(car => car.state === 3);

export const getActiveRentals = () => {
  const today = new Date().toISOString().split('T')[0];
  return sampleRentals.filter(rental => 
    rental.start <= today && rental.end >= today
  );
};

export const getCustomerByEmail = (email: string) => 
  sampleCustomers.find(customer => customer.email === email);

export const getCarByPlate = (plate: string) => 
  sampleCars.find(car => car.plate === plate);

export default {
  sampleBrands,
  sampleModels,
  sampleCars,
  sampleCustomers,
  sampleRentals,
  getAvailableCars,
  getRentedCars,
  getMaintenanceCars,
  getActiveRentals,
  getCustomerByEmail,
  getCarByPlate
};


