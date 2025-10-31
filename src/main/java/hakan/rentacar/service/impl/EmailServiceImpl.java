package hakan.rentacar.service.impl;

import hakan.rentacar.entities.concretes.Customer;
import hakan.rentacar.entities.concretes.Reservation;
import hakan.rentacar.entities.concretes.Rental;
import hakan.rentacar.entities.concretes.Payment;
import hakan.rentacar.entities.concretes.Invoice;
import hakan.rentacar.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0,00 ₺";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
        return formatter.format(amount).replace("₺", "TL");
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return date.format(formatter);
    }

    private String createEmailTemplate(String title, String customerName, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="tr">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f4f4f4;
                    }
                    .container {
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 600;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        margin-bottom: 20px;
                        color: #2c3e50;
                    }
                    .message {
                        font-size: 16px;
                        margin-bottom: 30px;
                        line-height: 1.8;
                    }
                    .info-box {
                        background-color: #f8f9fa;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 0 5px 5px 0;
                    }
                    .info-row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 10px;
                        padding: 5px 0;
                        border-bottom: 1px solid #e9ecef;
                    }
                    .info-row:last-child {
                        border-bottom: none;
                        margin-bottom: 0;
                    }
                    .info-label {
                        font-weight: 600;
                        color: #495057;
                    }
                    .info-value {
                        color: #6c757d;
                    }
                    .footer {
                        background-color: #2c3e50;
                        color: white;
                        padding: 20px 30px;
                        text-align: center;
                    }
                    .footer p {
                        margin: 0;
                        font-size: 14px;
                    }
                    .logo {
                        font-size: 28px;
                        font-weight: bold;
                        margin-bottom: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🚗 Rent A Car</div>
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <div class="greeting">Sayın %s,</div>
                        <div class="message">
                            %s
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>Rent A Car</strong></p>
                        <p>Müşteri memnuniyeti bizim önceliğimizdir</p>
                    </div>
                </div>
            </body>
            </html>
            """, title, title, customerName, content);
    }

    @Override
    public void sendReservationConfirmation(Customer customer, Reservation reservation) {
        String subject = "Rezervasyon Onayı - " + reservation.getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Başlangıç Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Bitiş Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Toplam Tutar:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p>Rezervasyonunuz başarıyla onaylanmıştır. Belirtilen tarihlerde araç teslimini gerçekleştirebilirsiniz.</p>
            <p><strong>Dikkat:</strong> Araç teslimi sırasında ehliyet ve kimlik belgenizi yanınızda bulundurmayı unutmayın.</p>
            """,
            reservation.getCar().getModel().getBrand().getName(),
            reservation.getCar().getModel().getName(),
            reservation.getCar().getPlate(),
            formatDate(reservation.getStartDate()),
            formatDate(reservation.getEndDate()),
            formatCurrency(reservation.getTotalAmount())
        );

        sendHtmlEmail(customer.getEmail(), subject, "Rezervasyon Onayı", customerName, htmlContent);
    }

    public void sendReservationNotification(Customer customer, Reservation reservation) {
        String subject = "📝 Rezervasyonunuz Alındı - " + reservation.getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Başlangıç Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Bitiş Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Toplam Tutar:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p>Rezervasyon talebiniz başarıyla alınmıştır. En kısa sürede rezervasyonunuz onaylanacak ve size bilgi verilecektir.</p>
            <p><strong>Not:</strong> Rezervasyon onayından sonra araç teslimi bilgileri size iletilecektir.</p>
            """,
            reservation.getCar().getModel().getBrand().getName(),
            reservation.getCar().getModel().getName(),
            reservation.getCar().getPlate(),
            formatDate(reservation.getStartDate()),
            formatDate(reservation.getEndDate()),
            formatCurrency(reservation.getTotalAmount())
        );

        sendHtmlEmail(customer.getEmail(), subject, "Rezervasyon Talebi Alındı", customerName, htmlContent);
    }

    @Override
    public void sendReservationReminder(Customer customer, Reservation reservation) {
        String subject = "🔔 Rezervasyon Hatırlatması - " + reservation.getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Başlangıç Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Bitiş Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>⚠️ Hatırlatma:</strong> Yarın rezervasyonunuz var! Lütfen belirtilen tarihte ofisimize gelmeyi unutmayın.</p>
            <p>Araç teslimi için <strong>ehliyet</strong>, <strong>kimlik belgesi</strong> ve <strong>kredi kartı</strong> (depozito için) yanınızda bulundurmanız gerekmektedir.</p>
            <p>Herhangi bir sorunuz olması durumunda lütfen bizimle iletişime geçin.</p>
            """,
            reservation.getCar().getModel().getBrand().getName(),
            reservation.getCar().getModel().getName(),
            reservation.getCar().getPlate(),
            formatDate(reservation.getStartDate()),
            formatDate(reservation.getEndDate())
        );

        sendHtmlEmail(customer.getEmail(), subject, "Rezervasyon Hatırlatması", customerName, htmlContent);
    }

    @Override
    public void sendReservationCancellation(Customer customer, Reservation reservation) {
        String subject = "Rezervasyon İptali - " + reservation.getCar().getPlate();
        String content = String.format(
            "Sayın %s %s,\n\n" +
            "Rezervasyonunuz iptal edilmiştir.\n\n" +
            "Araç: %s %s (%s)\n" +
            "Başlangıç Tarihi: %s\n" +
            "Bitiş Tarihi: %s\n\n" +
            "İyi günler dileriz.\n" +
            "Rent A Car Ekibi",
            customer.getFirstName(),
            customer.getLastName(),
            reservation.getCar().getModel().getBrand().getName(),
            reservation.getCar().getModel().getName(),
            reservation.getCar().getPlate(),
            reservation.getStartDate(),
            reservation.getEndDate()
        );

        sendCustomEmail(customer.getEmail(), subject, content);
    }

    @Override
    public void sendCarDeliveryNotification(Customer customer, Rental rental) {
        String subject = "Araç Teslim - " + rental.getCar().getPlate();
        String content = String.format(
            "Sayın %s %s,\n\n" +
            "Aracınız teslim edilmiştir.\n\n" +
            "Araç: %s %s (%s)\n" +
            "Kiralama Başlangıcı: %s\n" +
            "Kiralama Bitişi: %s\n\n" +
            "Güvenli sürüşler dileriz.\n" +
            "Rent A Car Ekibi",
            customer.getFirstName(),
            customer.getLastName(),
            rental.getCar().getModel().getBrand().getName(),
            rental.getCar().getModel().getName(),
            rental.getCar().getPlate(),
            rental.getStart(),
            rental.getEnd()
        );

        sendCustomEmail(customer.getEmail(), subject, content);
    }

    @Override
    public void sendCarPickupNotification(Customer customer, Rental rental) {
        String subject = "🚗 Araç Teslim Alma Hatırlatması - " + rental.getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Kiralama Başlangıcı:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Teslim Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>⚠️ Önemli Hatırlatma:</strong> Kiralama süreniz yarın sona eriyor. Aracı zamanında teslim etmeniz gerekmektedir.</p>
            <p><strong>Araç teslimi için:</strong></p>
            <ul>
                <li>Aracı temiz durumda teslim edin</li>
                <li>Yakıt seviyesini kontrol edin</li>
                <li>Anahtar ve belgeleri eksiksiz getirin</li>
            </ul>
            <p>Gecikme durumunda ek ücret uygulanabilir. Lütfen zamanında teslimi gerçekleştirin.</p>
            """,
            rental.getCar().getModel().getBrand().getName(),
            rental.getCar().getModel().getName(),
            rental.getCar().getPlate(),
            formatDate(rental.getStart()),
            formatDate(rental.getEnd())
        );

        sendHtmlEmail(customer.getEmail(), subject, "Araç Teslim Alma Hatırlatması", customerName, htmlContent);
    }

    @Override
    public void sendPaymentReminder(Customer customer, Rental rental) {
        String subject = "⏰ Ödeme Hatırlatması - " + rental.getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        // Calculate total price
        long days = java.time.temporal.ChronoUnit.DAYS.between(rental.getStart(), rental.getEnd()) + 1;
        double totalPrice = rental.getCar().getDailyPrice() * days + (rental.getExtraCosts() != null ? rental.getExtraCosts() : 0);
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Kiralama Başlangıcı:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Kiralama Bitişi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Toplam Ödeme:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>⚠️ Önemli:</strong> Kiralama süreniz yakında sona erecek. Ödemenizi zamanında tamamlamanız önemlidir.</p>
            <p>Araç teslimi sırasında ödemenizi nakit, kredi kartı veya banka kartı ile gerçekleştirebilirsiniz.</p>
            <p>Herhangi bir sorunuz olması durumunda 7/24 müşteri hizmetlerimizden yardım alabilirsiniz.</p>
            """,
            rental.getCar().getModel().getBrand().getName(),
            rental.getCar().getModel().getName(),
            rental.getCar().getPlate(),
            formatDate(rental.getStart()),
            formatDate(rental.getEnd()),
            formatCurrency(BigDecimal.valueOf(totalPrice))
        );

        sendHtmlEmail(customer.getEmail(), subject, "Ödeme Hatırlatması", customerName, htmlContent);
    }

    @Override
    public void sendRatingRequest(Customer customer, Rental rental) {
        String subject = "⭐ Kiralama Değerlendirmesi - " + rental.getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Kiralama Tarihi:</span>
                    <span class="info-value">%s - %s</span>
                </div>
            </div>
            <p><strong>🎉 Kiralama tamamlandı!</strong> Deneyiminizi bizimle paylaşmak ister misiniz?</p>
            <p>Size sunulan hizmeti değerlendirmeniz, hizmet kalitemizi artırmamıza yardımcı olur.</p>
            <p><strong>💡 Nasıl değerlendirme yapabilirsiniz?</strong></p>
            <ul>
                <li>Müşteri portalına giriş yapın</li>
                <li>"Geçmiş Kiralamalar" bölümüne gidin</li>
                <li>Değerlendirme yapmak istediğiniz kiralama için "Değerlendir" butonuna tıklayın</li>
                <li>1-5 yıldız arasında puan verin ve görüşlerinizi yazın</li>
            </ul>
            <p>Görüşleriniz bizim için çok değerlidir!</p>
            """,
            rental.getCar().getModel().getBrand().getName(),
            rental.getCar().getModel().getName(),
            rental.getCar().getPlate(),
            formatDate(rental.getStart()),
            formatDate(rental.getEnd())
        );

        sendHtmlEmail(customer.getEmail(), subject, "Kiralama Değerlendirmesi", customerName, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String emailTitle, String customerName, String htmlContent) {
        System.out.println("=== HTML EMAIL SERVICE CALLED ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        
        try {
            if (mailSender == null) {
                System.err.println("ERROR: mailSender is NULL!");
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("noreply@rentacar.com");
            helper.setText(createEmailTemplate(emailTitle, customerName, htmlContent), true);
            
            System.out.println("Attempting to send HTML email...");
            mailSender.send(message);
            System.out.println("HTML email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Error sending HTML email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error sending HTML email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendCustomEmail(String to, String subject, String content) {
        System.out.println("=== EMAIL SERVICE CALLED ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Content length: " + (content != null ? content.length() : 0));
        
        try {
            if (mailSender == null) {
                System.err.println("ERROR: mailSender is NULL!");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom("noreply@rentacar.com");

            System.out.println("Attempting to send email...");
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            
            // Check if it's an SSL/TLS related error
            if (e.getMessage() != null && e.getMessage().contains("Server is not trusted")) {
                System.err.println("SSL/TLS certificate validation failed. This might be due to:");
                System.err.println("1. Invalid SSL certificate");
                System.err.println("2. Network firewall blocking the connection");
                System.err.println("3. Email server configuration issue");
                System.err.println("Email will be skipped, but notification was still created.");
            } else if (e.getMessage() != null && e.getMessage().contains("Could not convert socket to TLS")) {
                System.err.println("TLS connection failed. Please check email server configuration.");
            } else {
                System.err.println("General email sending error occurred.");
            }
            
            // Don't re-throw the exception to prevent breaking the main transaction
            // Just log the error and continue
        }
    }

    @Override
    public void sendPaymentConfirmation(Customer customer, Payment payment) {
        String subject = "✅ Ödeme Onayı - " + payment.getRental().getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Ödeme Tutarı:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Ödeme Yöntemi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">İşlem Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                %s
            </div>
            <p><strong>✅ Ödemeniz başarıyla alınmıştır!</strong></p>
            <p>Rent A Car olarak hizmetinizden memnun kaldığınızı umuyoruz. Güvenli yolculuklar dileriz!</p>
            """,
            payment.getRental().getCar().getModel().getBrand().getName(),
            payment.getRental().getCar().getModel().getName(),
            payment.getRental().getCar().getPlate(),
            formatCurrency(payment.getAmount()),
            payment.getMethod().getDisplayName(),
            payment.getPaidAt() != null ? formatDateTime(payment.getPaidAt()) : "Belirtilmemiş",
            payment.getTransactionId() != null ? 
                String.format("<div class=\"info-row\"><span class=\"info-label\">İşlem No:</span><span class=\"info-value\">%s</span></div>", payment.getTransactionId()) : ""
        );

        sendHtmlEmail(customer.getEmail(), subject, "Ödeme Onayı", customerName, htmlContent);
    }

    @Override
    public void sendPaymentReminder(Customer customer, Payment payment) {
        String subject = "⏰ Ödeme Hatırlatması - " + payment.getRental().getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String dueDateInfo = "";
        if (payment.getDueDate() != null) {
            long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), payment.getDueDate());
            if (daysUntilDue > 0) {
                dueDateInfo = String.format("Bu ödeme %d gün sonra vadesi geliyor.", daysUntilDue);
            } else if (daysUntilDue == 0) {
                dueDateInfo = "Bu ödeme bugün vadesi geliyor!";
            } else {
                dueDateInfo = String.format("Bu ödeme %d gün önce vadesi geçti!", Math.abs(daysUntilDue));
            }
        }
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Ödeme Tutarı:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Vade Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Durum:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>⏰ Ödeme Hatırlatması</strong></p>
            <p>%s</p>
            <p>Ödemenizi zamanında tamamlamanız gerekmektedir. Geç ödemeler ek ücret gerektirebilir.</p>
            <p>Ödeme seçenekleriniz: Nakit, Kredi Kartı, Havale</p>
            """,
            payment.getRental().getCar().getModel().getBrand().getName(),
            payment.getRental().getCar().getModel().getName(),
            payment.getRental().getCar().getPlate(),
            formatCurrency(payment.getAmount()),
            payment.getDueDate() != null ? formatDateTime(payment.getDueDate()) : "Belirtilmemiş",
            payment.getStatus().getDisplayName(),
            dueDateInfo
        );

        sendHtmlEmail(customer.getEmail(), subject, "Ödeme Hatırlatması", customerName, htmlContent);
    }

    @Override
    public void sendPaymentDueNotification(Customer customer, Payment payment) {
        String subject = "🚨 Vadesi Gelen Ödeme - " + payment.getRental().getCar().getPlate();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Ödeme Tutarı:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Vade Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>🚨 Önemli: Ödeme Vadesi!</strong></p>
            <p>Bu ödeme bugün vadesi geliyor. Lütfen en kısa sürede ödemenizi tamamlayın.</p>
            <p>Vadesi geçen ödemeler için gecikme faizi uygulanabilir.</p>
            """,
            payment.getRental().getCar().getModel().getBrand().getName(),
            payment.getRental().getCar().getModel().getName(),
            payment.getRental().getCar().getPlate(),
            formatCurrency(payment.getAmount()),
            payment.getDueDate() != null ? formatDateTime(payment.getDueDate()) : "Belirtilmemiş"
        );

        sendHtmlEmail(customer.getEmail(), subject, "Vadesi Gelen Ödeme", customerName, htmlContent);
    }

    @Override
    public void sendInvoiceNotification(Customer customer, Invoice invoice) {
        String subject = "📄 Fatura - " + invoice.getInvoiceNumber();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Fatura No:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Kiralama Tarihleri:</span>
                    <span class="info-value">%s - %s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Fatura Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Vade Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Ara Toplam:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">KDV (%%18):</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Genel Toplam:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>📄 Faturanız Eklendi</strong></p>
            <p>Rent A Car hizmeti için faturanız hazırlanmıştır. Vade tarihinde ödemenizi tamamlamanız gerekmektedir.</p>
            <p>Ödeme seçenekleriniz: Nakit, Kredi Kartı, Havale</p>
            """,
            invoice.getInvoiceNumber(),
            invoice.getRental().getCar().getModel().getBrand().getName(),
            invoice.getRental().getCar().getModel().getName(),
            invoice.getRental().getCar().getPlate(),
            formatDate(invoice.getRental().getStart()),
            formatDate(invoice.getRental().getEnd()),
            formatDate(invoice.getIssueDate().toLocalDate()),
            formatDate(invoice.getDueDate().toLocalDate()),
            formatCurrency(invoice.getSubtotal()),
            formatCurrency(invoice.getTaxAmount()),
            formatCurrency(invoice.getTotalAmount())
        );

        sendHtmlEmail(customer.getEmail(), subject, "Fatura Bildirimi", customerName, htmlContent);
    }

    @Override
    public void sendOverdueInvoiceNotification(Customer customer, Invoice invoice) {
        String subject = "🚨 Vadesi Geçen Fatura - " + invoice.getInvoiceNumber();
        String customerName = customer.getFirstName() + " " + customer.getLastName();
        
        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(invoice.getDueDate(), LocalDateTime.now());
        
        String htmlContent = String.format("""
            <div class="info-box">
                <div class="info-row">
                    <span class="info-label">Fatura No:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Araç:</span>
                    <span class="info-value">%s %s (%s)</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Vade Tarihi:</span>
                    <span class="info-value">%s</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Gecikme Süresi:</span>
                    <span class="info-value">%d gün</span>
                </div>
                <div class="info-row">
                    <span class="info-label">Toplam Tutar:</span>
                    <span class="info-value">%s</span>
                </div>
            </div>
            <p><strong>🚨 Önemli: Vadesi Geçen Fatura!</strong></p>
            <p>Faturanız %d gün önce vadesi geçti. Lütfen en kısa sürede ödemenizi tamamlayın.</p>
            <p>Vadesi geçen ödemeler için gecikme faizi uygulanabilir.</p>
            <p>Ödeme yapmak için bizimle iletişime geçebilirsiniz.</p>
            """,
            invoice.getInvoiceNumber(),
            invoice.getRental().getCar().getModel().getBrand().getName(),
            invoice.getRental().getCar().getModel().getName(),
            invoice.getRental().getCar().getPlate(),
            formatDate(invoice.getDueDate().toLocalDate()),
            daysOverdue,
            formatCurrency(invoice.getTotalAmount()),
            daysOverdue
        );

        sendHtmlEmail(customer.getEmail(), subject, "Vadesi Geçen Fatura", customerName, htmlContent);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }
}
