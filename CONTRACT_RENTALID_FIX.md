# Contract `rentalId` Null Hatası - Düzeltme Özeti

## Sorun
Workflow'dan sözleşme oluşturulurken backend'e `rentalId: null` gönderiliyordu:

```json
{
  "rentalId": null,
  "customerId": 5,
  "templateId": 1,
  "signedDate": "2025-11-01",
  "status": "DRAFT"
}
```

## Kök Neden
React'in state güncellemeleri asenkron olduğu için, zincirleme mutation'larda closure'lar eski state değerlerine sahip oluyordu:

1. **`createRentalMutation` başarılı** → `setSelectedRentalId(data.data.id)` çağrılıyor
2. **Ancak** mutation'ın `onSuccess` callback'i tanımlandığı anda capture edilen eski `selectedRentalId` değerini (`null`) kullanıyor
3. Bu yüzden `createContractMutation` `rentalId: null` ile tetikleniyordu

## Çözüm

### 1. Backend: ModelMapper Configuration
`ContractServiceImpl.java`'da `DtoToEntity` metodunu, ID'leri entity'lere manuel map edecek şekilde güncelledik:

```java
@Override
public Contract DtoToEntity(ContractDto contractDto) {
    if (contractDto == null) return null;
    Contract contract = modelMapper.map(contractDto, Contract.class);
    
    // Set the rental
    if (contractDto.getRentalId() != null) {
        Rental rental = rentalRepository.findById(contractDto.getRentalId()).orElseThrow();
        contract.setRental(rental);
    }
    
    // Set the customer
    if (contractDto.getCustomerId() != null) {
        Customer customer = customerRepository.findById(contractDto.getCustomerId()).orElseThrow();
        contract.setCustomer(customer);
    }
    
    // Set the template
    if (contractDto.getTemplateId() != null) {
        ContractTemplate template = contractTemplateRepository.findById(contractDto.getTemplateId()).orElseThrow();
        contract.setTemplate(template);
    }
    
    return contract;
}
```

### 2. Frontend: useRef ile Stale Closure Problemini Çözme
State'leri `useRef` ile wrap edip, zincirleme mutation'larda ref'leri kullanarak güncel değerlere erişim sağladık:

```typescript
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

// ... diğer useEffect'ler

// Mutation'larda ref kullanımı
const createRentalMutation = useMutation(
  (rental: Partial<Rental>) => rentalsApi.create(rental as Rental),
  {
    onSuccess: (data) => {
      setSelectedRentalId(data.data.id || null);
      // After rental created, create contract
      if (selectedTemplateRef.current) {
        createContractMutation.mutate({
          rentalId: data.data.id!,
          customerId: selectedCustomerRef.current,
          templateId: selectedTemplateRef.current,
          // ...
        } as Contract);
      }
    },
  }
);
```

## Değişiklikler

### Backend
- ✅ `src/main/java/hakan/rentacar/service/impl/ContractServiceImpl.java`: `DtoToEntity` metodunu güncelledik
- ✅ `src/main/java/hakan/rentacar/service/impl/ContractServiceImpl.java`: `add` metodunu basitleştirdik (artık `DtoToEntity` tüm ilişkileri set ediyor)

### Frontend
- ✅ `frontend/src/pages/RentalWorkflowPage.tsx`: `useRef` import'unu ekledik
- ✅ `frontend/src/pages/RentalWorkflowPage.tsx`: State'leri ref'lerle wrap ettik
- ✅ `frontend/src/pages/RentalWorkflowPage.tsx`: Her state değişiminde ref'i güncelleyen useEffect'ler ekledik
- ✅ `frontend/src/pages/RentalWorkflowPage.tsx`: Zincirleme mutation'larda ref'leri kullandık

## Test
1. Workflow'da yeni kiralama oluştur
2. Tüm adımları tamamla (Araç Seçimi → Sözleşme → Ödeme → Teslim)
3. Sözleşme oluşturulurken `rentalId` artık null değil, doğru rental ID'si gönderilmeli

## Sonuç
Workflow artık sırayla şu şekilde çalışıyor:
1. ✅ Rental oluşturuluyor (`createRentalMutation`)
2. ✅ Rental ID'si doğru şekilde capture ediliyor
3. ✅ Contract oluşturuluyor (`createContractMutation` - `rentalId` artık null değil)
4. ✅ Contract imzalanıyor (`signContractMutation`)
5. ✅ Payment oluşturuluyor (`createPaymentMutation`)
6. ✅ Delivery check oluşturuluyor (`createDeliveryCheckMutation`)

Her mutation, bir önceki mutation'ın başarılı sonucundan gelen güncel değerleri kullanıyor.


