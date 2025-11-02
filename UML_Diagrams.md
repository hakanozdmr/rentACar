# RentACar System - UML Diagrams

## 1. Class Diagram (Domain Model)

### Core Entities
```
@startuml Core Entities
package "Domain Model" {
    abstract class BaseEntity {
        + Long id
        + String createdBy
        + Date createdDate
        + String updateBy
        + Date updateDate
    }

    class Brand {
        + String name
    }

    class Model {
        + String name
        - Brand brand
    }

    class Car {
        + String plate
        + Double dailyPrice
        + Integer modelYear
        + Integer state
        + Long mileage
        + String fuelType
        + String transmission
        + String segment
        + String color
        + List<String> features
        + List<String> images
        + LocalDate lastMaintenanceDate
        + LocalDate nextMaintenanceDate
        + LocalDate insuranceExpiryDate
        + String insuranceCompany
        + Double gpsLatitude
        + Double gpsLongitude
        + LocalDateTime lastLocationUpdate
    }

    class Customer {
        + String firstName
        + String lastName
        + String street
        + Integer zipcode
        + String city
        + String phone
        + String email
        + LocalDate dateOfBirth
        + String idNumber
        + String driverLicenseNumber
    }

    class User {
        + String username
        + String email
        + String password
        + String firstName
        + String lastName
        + Boolean enabled
        + Set<Role> roles
    }

    class Role {
        + String name
    }

    class Reservation {
        + LocalDate startDate
        + LocalDate endDate
        + ReservationStatus status
        + BigDecimal totalAmount
        + String specialRequests
        + String note
        + LocalDateTime confirmedAt
        + LocalDateTime cancelledAt
    }

    class Rental {
        + LocalDate start
        + LocalDate end
        + Integer extraCosts
        + String note
        + RentalStatus status
        + BigDecimal totalAmount
        + String specialRequests
        + LocalDateTime confirmedAt
        + LocalDateTime cancelledAt
    }

    class Payment {
        + BigDecimal amount
        + PaymentMethod method
        + PaymentStatus status
        + LocalDateTime paidAt
        + LocalDateTime dueDate
        + String transactionId
        + String paymentReference
        + String notes
    }

    class Invoice {
        + String invoiceNumber
        + LocalDate issueDate
        + LocalDate dueDate
        + InvoiceStatus status
        + BigDecimal subtotal
        + BigDecimal taxAmount
        + BigDecimal totalAmount
        + BigDecimal taxRate
        + String notes
        + String paymentTerms
    }

    class Notification {
        + String title
        + String message
        + NotificationType type
        + NotificationStatus status
        + NotificationChannel channel
        + LocalDateTime sentAt
        + LocalDateTime readAt
    }

    class ReservationRating {
        + Integer rating
        + String comment
        + LocalDateTime ratingDate
    }

    class MaintenanceRecord {
        + LocalDate maintenanceDate
        + String maintenanceType
        + String description
        + BigDecimal cost
        + String serviceProvider
        + LocalDate nextMaintenanceDate
    }

    class GeneralLedger {
        + String accountCode
        + String accountName
        + AccountType accountType
        + BigDecimal debitAmount
        + BigDecimal creditAmount
        + TransactionType transactionType
        + String description
        + LocalDateTime transactionDate
        + String reference
    }

    class TaxCalculation {
        + TaxType taxType
        + BigDecimal taxRate
        + BigDecimal taxableAmount
        + BigDecimal taxAmount
        + LocalDate calculationDate
        + LocalDate dueDate
        + String description
    }

    enum ReservationStatus {
        PENDING
        CONFIRMED
        CANCELLED
        COMPLETED
        EXPIRED
    }

    enum RentalStatus {
        PENDING
        CONFIRMED
        CANCELLED
        COMPLETED
        EXPIRED
    }

    enum PaymentMethod {
        CREDIT_CARD
        BANK_TRANSFER
        CASH
        CHECK
        DIGITAL_WALLET
        ONLINE_BANKING
    }

    enum PaymentStatus {
        PENDING
        COMPLETED
        FAILED
        CANCELLED
        REFUNDED
        PARTIAL
    }

    enum InvoiceStatus {
        PENDING
        SENT
        PAID
        OVERDUE
        CANCELLED
    }

    enum NotificationType {
        RESERVATION_REMINDER
        PAYMENT_REMINDER
        MAINTENANCE_REMINDER
        RATING_REQUEST
        SYSTEM_NOTIFICATION
    }

    enum NotificationStatus {
        PENDING
        SENT
        DELIVERED
        READ
        FAILED
    }

    enum NotificationChannel {
        EMAIL
        SMS
        IN_APP
        PUSH
    }

    enum AccountType {
        ASSET
        LIABILITY
        EQUITY
        REVENUE
        EXPENSE
    }

    enum TransactionType {
        DEBIT
        CREDIT
    }

    enum TaxType {
        VAT
        CORPORATE_TAX
        WITHHOLDING_TAX
        STAMP_DUTY
        MUNICIPAL_TAX
    }
}

' Relationships
BaseEntity <|-- Brand
BaseEntity <|-- Model
BaseEntity <|-- Car
BaseEntity <|-- Customer
BaseEntity <|-- User
BaseEntity <|-- Role
BaseEntity <|-- Reservation
BaseEntity <|-- Rental
BaseEntity <|-- Payment
BaseEntity <|-- Invoice
BaseEntity <|-- Notification
BaseEntity <|-- ReservationRating
BaseEntity <|-- MaintenanceRecord
BaseEntity <|-- GeneralLedger
BaseEntity <|-- TaxCalculation

Brand ||--o{ Model : "has"
Model ||--o{ Car : "has"
Car ||--o{ Rental : "rented in"
Customer ||--o{ Rental : "makes"
User ||--o| Customer : "associated with"

Reservation }o--|| Customer : "made by"
Reservation }o--|| Car : "for"

Rental ||--o{ Payment : "paid by"
Rental }o--|| Customer : "by"
Rental }o--|| Car : "of"

Invoice }o--|| Rental : "for"

ReservationRating }o--|| Rental : "rates"
ReservationRating }o--|| Customer : "rated by"
ReservationRating }o--|| Car : "about"

Car ||--o{ MaintenanceRecord : "maintained"

ReservationStatus --o Reservation
RentalStatus --o Rental
PaymentMethod --o Payment
PaymentStatus --o Payment
InvoiceStatus --o Invoice
NotificationType --o Notification
NotificationStatus --o Notification
NotificationChannel --o Notification
AccountType --o GeneralLedger
TransactionType --o GeneralLedger
TaxType --o TaxCalculation

@enduml
```

## 2. Service Layer Architecture

```
@startuml Service Layer
package "Service Layer" {
    interface AuthService
    interface BrandService
    interface CarService
    interface CustomerService
    interface RentalService
    interface ReservationService
    interface PaymentService
    interface InvoiceService
    interface NotificationService
    interface EmailService
    interface AnalyticsService
    interface GeneralLedgerService
    interface TaxCalculationService

    class AuthServiceImpl implements AuthService
    class BrandServiceImpl implements BrandService
    class CarServiceImpl implements CarService
    class CustomerServiceImpl implements CustomerService
    class RentalServiceImpl implements RentalService
    class ReservationServiceImpl implements ReservationService
    class PaymentServiceImpl implements PaymentService
    class InvoiceServiceImpl implements InvoiceService
    class NotificationServiceImpl implements NotificationService
    class EmailServiceImpl implements EmailService
    class AnalyticsServiceImpl implements AnalyticsService
    class GeneralLedgerServiceImpl implements GeneralLedgerService
    class TaxCalculationServiceImpl implements TaxCalculationService
}

package "Controller Layer" {
    class AuthController
    class BrandsController
    class CarsController
    class CustomersController
    class RentalsController
    class ReservationsController
    class PaymentsController
    class InvoicesController
    class CustomerPortalController
    class FinancialReportController
    class AnalyticsController
    class NotificationController
}

package "Repository Layer" {
    interface BrandRepository
    interface CarRepository
    interface CustomerRepository
    interface RentalRepository
    interface ReservationRepository
    interface PaymentRepository
    interface InvoiceRepository
    interface UserRepository
    interface NotificationRepository
}

' Dependencies
AuthController --> AuthService
BrandsController --> BrandService
CarsController --> CarService
CustomersController --> CustomerService
RentalsController --> RentalService
ReservationsController --> ReservationService
PaymentsController --> PaymentService
InvoicesController --> InvoiceService
CustomerPortalController --> ReservationService
CustomerPortalController --> CarService
CustomerPortalController --> RentalService
FinancialReportController --> PaymentService
FinancialReportController --> InvoiceService
FinancialReportController --> GeneralLedgerService

AuthService --> UserRepository
BrandService --> BrandRepository
CarService --> CarRepository
CustomerService --> CustomerRepository
RentalService --> RentalRepository
ReservationService --> ReservationRepository
PaymentService --> PaymentRepository
InvoiceService --> InvoiceRepository

@enduml
```

## 3. Component/System Architecture

```
@startuml System Architecture
package "Frontend (React)" {
    [React App]
    [Material-UI Components]
    [React Query]
    [Context Providers]
}

package "Backend (Spring Boot)" {
    package "Presentation Layer" {
        [REST Controllers]
        [Authentication Filter]
        [Rate Limiting Filter]
        [Request Logging Filter]
        [CORS Configuration]
    }

    package "Business Layer" {
        [Service Interfaces]
        [Service Implementations]
        [Business Rules]
        [Email Service]
        [Notification Service]
    }

    package "Data Access Layer" {
        [Repository Interfaces]
        [JPA Entities]
        [DTO Mappers]
    }

    package "Security Layer" {
        [JWT Utils]
        [User Details Service]
        [Security Configuration]
        [Password Encoder]
    }

    package "Configuration Layer" {
        [Database Config]
        [Model Mapper Config]
        [OpenAPI Config]
        [Audit Config]
    }
}

package "Database Layer" {
    database PostgreSQL {
        [brands]
        [models]
        [cars]
        [customers]
        [users]
        [reservations]
        [rentals]
        [payments]
        [invoices]
        [notifications]
        [reservation_ratings]
        [maintenance_records]
        [general_ledger]
        [tax_calculations]
    }
}

package "External Services" {
    [Email Service (SMTP)]
    [Google Maps API]
    [PDF Generation]
}

' Connections
[React App] --> [REST Controllers] : HTTP/REST
[REST Controllers] --> [Service Interfaces] : Dependency Injection
[Service Implementations] --> [Repository Interfaces] : Data Access
[Repository Interfaces] --> [JPA Entities] : ORM Mapping
[JPA Entities] --> [PostgreSQL] : SQL/JDBC

[Email Service] --> [Email Service (SMTP)] : SMTP
[React App] --> [Google Maps API] : API Call

[Authentication Filter] --> [JWT Utils]
[Security Configuration] --> [User Details Service]
[Service Implementations] --> [Email Service]
[Service Implementations] --> [Notification Service]

@enduml
```

## 4. Sequence Diagram - Reservation Flow

```
@startuml Reservation Flow
actor Customer
participant "React Frontend" as Frontend
participant "ReservationController" as Controller
participant "ReservationService" as Service
participant "ReservationRepository" as Repository
participant "EmailService" as Email
participant "NotificationService" as Notification
participant "Database" as DB

Customer -> Frontend: Create reservation request
Frontend -> Controller: POST /api/reservations
Controller -> Service: create(reservationDto)

Service -> Service: validateCarAvailability()
Service -> Repository: findConflictingReservations()
Repository -> DB: SELECT * FROM reservations WHERE...
DB --> Repository: conflicting reservations
Repository --> Service: result

alt Car is available
    Service -> Service: calculateTotalAmount()
    Service -> Repository: save(reservation)
    Repository -> DB: INSERT INTO reservations
    DB --> Repository: saved reservation
    Repository --> Service: saved reservation
    
    Service -> Notification: createNotification()
    Notification -> DB: INSERT INTO notifications
    
    Service -> Email: sendReservationNotification()
    Email -> Email: Send SMTP email
    
    Service --> Controller: ReservationDto
    Controller --> Frontend: HTTP 200 + DTO
    Frontend --> Customer: Success message
else Car not available
    Service --> Controller: RuntimeException
    Controller --> Frontend: HTTP 400 + Error
    Frontend --> Customer: Error message
end

@enduml
```

## 5. Security Architecture

```
@startuml Security Flow
actor User
participant "Browser" as Browser
participant "AuthController" as Auth
participant "JwtUtils" as JWT
participant "UserDetailsService" as UDS
participant "SecurityFilterChain" as Security
participant "ApiController" as API

User -> Browser: Login request
Browser -> Auth: POST /api/auth/login
Auth -> UDS: loadUserByUsername()
UDS -> Database: SELECT * FROM users WHERE username=?
Database --> UDS: User details
UDS --> Auth: UserDetails object
Auth -> Auth: authenticate()
Auth -> JWT: generateJwtToken()
JWT --> Auth: JWT token
Auth --> Browser: JWT token
Browser -> Browser: Store JWT

later...
User -> Browser: API request
Browser -> Security: Request + JWT Header
Security -> JWT: parseJwt(request)
JWT --> Security: Extracted JWT
Security -> JWT: validateToken()
JWT -> UDS: loadUserByUsername()
UDS --> JWT: UserDetails
JWT --> Security: Validation result

alt Valid token
    Security -> Security: Set authentication in context
    Security -> API: Forward request
    API --> Browser: Response
    Browser --> User: Data
else Invalid token
    Security --> Browser: 401 Unauthorized
    Browser --> User: Login required
end

@enduml
```

## 6. Database ERD (Simplified)

```
@startuml Database ERD
!define TABLE entity
!define PRIMARY_KEY #FFAAAA
!define FOREIGN_KEY #AAFFAA
!define COLUMN #FFFFFF

entity "brands" as brands {
    * id : BIGINT <<PK>>
    --
    name : VARCHAR(255)
    created_by : VARCHAR(255)
    created_date : TIMESTAMP
    update_by : VARCHAR(255)
    update_date : TIMESTAMP
}

entity "models" as models {
    * id : BIGINT <<PK>>
    * brand_id : BIGINT <<FK>>
    --
    name : VARCHAR(255)
}

entity "cars" as cars {
    * id : BIGINT <<PK>>
    * model_id : BIGINT <<FK>>
    --
    plate : VARCHAR(20) <<UNIQUE>>
    daily_price : DECIMAL(10,2)
    model_year : INTEGER
    state : INTEGER
    mileage : BIGINT
    fuel_type : VARCHAR(20)
    transmission : VARCHAR(20)
    segment : VARCHAR(20)
    color : VARCHAR(50)
    gps_latitude : DOUBLE
    gps_longitude : DOUBLE
}

entity "customers" as customers {
    * id : BIGINT <<PK>>
    user_id : BIGINT <<FK>>
    --
    first_name : VARCHAR(255)
    last_name : VARCHAR(255)
    street : VARCHAR(255)
    zipcode : INTEGER
    city : VARCHAR(255)
    phone : VARCHAR(255)
    email : VARCHAR(255)
    date_of_birth : DATE
    id_number : VARCHAR(255) <<UNIQUE>>
    driver_license_number : VARCHAR(255) <<UNIQUE>>
}

entity "users" as users {
    * id : BIGINT <<PK>>
    --
    username : VARCHAR(50) <<UNIQUE>>
    email : VARCHAR(100) <<UNIQUE>>
    password : VARCHAR(100)
    first_name : VARCHAR(50)
    last_name : VARCHAR(50)
    enabled : BOOLEAN
}

entity "reservations" as reservations {
    * id : BIGINT <<PK>>
    * customer_id : BIGINT <<FK>>
    * car_id : BIGINT <<FK>>
    --
    start_date : DATE
    end_date : DATE
    status : VARCHAR(50)
    total_amount : DECIMAL(10,2)
    special_requests : VARCHAR(500)
    note : VARCHAR(500)
}

entity "rentals" as rentals {
    * id : BIGINT <<PK>>
    * customer_id : BIGINT <<FK>>
    * car_id : BIGINT <<FK>>
    --
    start : DATE
    end : DATE
    status : VARCHAR(50)
    total_amount : DECIMAL(10,2)
    extra_costs : INTEGER
    note : VARCHAR(500)
}

entity "payments" as payments {
    * id : BIGINT <<PK>>
    * rental_id : BIGINT <<FK>>
    * customer_id : BIGINT <<FK>>
    --
    amount : DECIMAL(10,2)
    method : VARCHAR(50)
    status : VARCHAR(50)
    paid_at : TIMESTAMP
    due_date : TIMESTAMP
    transaction_id : VARCHAR(100)
}

entity "invoices" as invoices {
    * id : BIGINT <<PK>>
    rental_id : BIGINT <<FK>>
    --
    invoice_number : VARCHAR(100) <<UNIQUE>>
    issue_date : DATE
    due_date : DATE
    status : VARCHAR(50)
    subtotal : DECIMAL(10,2)
    tax_amount : DECIMAL(10,2)
    total_amount : DECIMAL(10,2)
}

brands ||--o{ models : "has"
models ||--o{ cars : "has"
users ||--o| customers : "associated with"
customers ||--o{ reservations : "makes"
cars ||--o{ reservations : "reserved for"
customers ||--o{ rentals : "rents"
cars ||--o{ rentals : "rented car"
rentals ||--o{ payments : "payment for"
rentals ||--o{ invoices : "invoice for"

@enduml
```

## 7. Package Structure

```
@startuml Package Structure
package "hakan.rentacar" {
    package "api.controllers" {
        [AuthController]
        [BrandsController]
        [CarsController]
        [CustomersController]
        [RentalsController]
        [ReservationsController]
        [PaymentsController]
        [InvoicesController]
        [CustomerPortalController]
        [FinancialReportController]
        [AnalyticsController]
        [NotificationController]
    }

    package "service" {
        interface "Service Interfaces"
        package "impl" {
            [Service Implementations]
        }
    }

    package "entities" {
        package "concretes" {
            [JPA Entities]
        }
        package "dtos" {
            [Data Transfer Objects]
        }
    }

    package "repostories" {
        [JPA Repositories]
    }

    package "security" {
        [JWT Utils]
        [Auth Filter]
        [User Details Service]
    }

    package "config" {
        [Security Config]
        [Database Config]
        [CORS Config]
        [OpenAPI Config]
        [Model Mapper Config]
    }

    package "filters" {
        [Rate Limiting Filter]
        [Request Logging Filter]
    }

    package "exceptions" {
        [Business Exception]
        [Problem Details]
        [Validation Problem Details]
    }

    package "bean" {
        [Model Mapper Bean]
        [Auditor Aware Bean]
    }

    package "audit" {
        [Auditor Aware Impl]
    }
}

@enduml
```

Bu UML diagramları sistemin tüm önemli bileşenlerini, ilişkilerini ve mimarisini göstermektedir. Her diagram farklı bir perspektiften sistemi analiz etmek için kullanılabilir.




