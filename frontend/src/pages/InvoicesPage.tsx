import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Grid,
  Card,
  CardContent,
  CircularProgress,
  Alert,
  Tooltip,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  Receipt,
  AttachMoney,
  CheckCircle,
  Pending,
  Warning,
  Search,
  Send,
  Print,
  Download,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { invoicesApi, Invoice, InvoiceSummary } from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs from 'dayjs';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const InvoicesPage: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [editingInvoice, setEditingInvoice] = useState<Invoice | null>(null);
  const [viewingInvoice, setViewingInvoice] = useState<Invoice | null>(null);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState<Partial<Invoice>>({
    rentalId: 0,
    customerId: 0,
    invoiceNumber: '',
    subtotal: 0,
    taxRate: 0.18,
    taxAmount: 0,
    totalAmount: 0,
    status: 'PENDING',
    dueDate: dayjs().add(30, 'days').format('YYYY-MM-DD'),
    issueDate: dayjs().format('YYYY-MM-DD'),
    paymentTerms: '30 gün vadeli',
    notes: '',
  });

  // Fetch invoices
  const { data: invoices = [], isLoading: invoicesLoading } = useQuery(
    ['invoices', filterStatus],
    () => {
      switch (filterStatus) {
        case 'PENDING':
          return invoicesApi.getPending().then(res => res.data);
        case 'PAID':
          return invoicesApi.getPaid().then(res => res.data);
        case 'OVERDUE':
          return invoicesApi.getOverdue().then(res => res.data);
        default:
          return invoicesApi.getAll().then(res => res.data);
      }
    }
  );

  // Fetch summary
  const { data: summary } = useQuery(
    ['invoices-summary'],
    () => invoicesApi.getSummary().then(res => res.data)
  );

  // Mutations
  const createInvoiceMutation = useMutation(
    (invoice: Invoice) => invoicesApi.create(invoice),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['invoices']);
        queryClient.invalidateQueries(['invoices-summary']);
        showSuccess('Fatura başarıyla oluşturuldu');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Fatura oluşturulurken hata oluştu');
      },
    }
  );

  const updateInvoiceMutation = useMutation(
    (invoice: Invoice) => invoicesApi.update(invoice),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['invoices']);
        queryClient.invalidateQueries(['invoices-summary']);
        showSuccess('Fatura başarıyla güncellendi');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Fatura güncellenirken hata oluştu');
      },
    }
  );

  const markPaidMutation = useMutation(
    (id: number) => invoicesApi.markAsPaid(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['invoices']);
        queryClient.invalidateQueries(['invoices-summary']);
        showSuccess('Fatura ödendi olarak işaretlendi');
      },
      onError: (error: any) => {
        showError(error.message || 'Fatura işaretlenirken hata oluştu');
      },
    }
  );

  const markSentMutation = useMutation(
    (id: number) => invoicesApi.markAsSent(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['invoices']);
        queryClient.invalidateQueries(['invoices-summary']);
        showSuccess('Fatura gönderildi olarak işaretlendi');
      },
      onError: (error: any) => {
        showError(error.message || 'Fatura işaretlenirken hata oluştu');
      },
    }
  );

  const deleteInvoiceMutation = useMutation(
    (id: number) => invoicesApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['invoices']);
        queryClient.invalidateQueries(['invoices-summary']);
        showSuccess('Fatura silindi');
      },
      onError: (error: any) => {
        showError(error.message || 'Fatura silinirken hata oluştu');
      },
    }
  );

  const handleOpenDialog = (invoice?: Invoice) => {
    if (invoice) {
      setEditingInvoice(invoice);
      setFormData({
        ...invoice,
        dueDate: invoice.dueDate ? dayjs(invoice.dueDate).format('YYYY-MM-DD') : '',
        issueDate: invoice.issueDate ? dayjs(invoice.issueDate).format('YYYY-MM-DD') : '',
      });
    } else {
      setEditingInvoice(null);
      const invoiceNumber = `INV-${dayjs().format('YYYYMMDD')}-${Math.floor(Math.random() * 1000).toString().padStart(3, '0')}`;
      setFormData({
        rentalId: 0,
        customerId: 0,
        invoiceNumber,
        subtotal: 0,
        taxRate: 0.18,
        taxAmount: 0,
        totalAmount: 0,
        status: 'PENDING',
        dueDate: dayjs().add(30, 'days').format('YYYY-MM-DD'),
        issueDate: dayjs().format('YYYY-MM-DD'),
        paymentTerms: '30 gün vadeli',
        notes: '',
      });
    }
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingInvoice(null);
    setFormData({
      rentalId: 0,
      customerId: 0,
      invoiceNumber: '',
      subtotal: 0,
      taxRate: 0.18,
      taxAmount: 0,
      totalAmount: 0,
      status: 'PENDING',
      dueDate: dayjs().add(30, 'days').format('YYYY-MM-DD'),
      issueDate: dayjs().format('YYYY-MM-DD'),
      paymentTerms: '30 gün vadeli',
      notes: '',
    });
  };

  const handleViewInvoice = (invoice: Invoice) => {
    setViewingInvoice(invoice);
    setOpenViewDialog(true);
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setViewingInvoice(null);
  };

  const handlePrintInvoice = (invoice?: Invoice) => {
    const invoiceToPrint = invoice || viewingInvoice;
    if (!invoiceToPrint) return;

    const printWindow = window.open('', '_blank');
    if (printWindow) {
      printWindow.document.write(`
        <html>
          <head>
            <title>Fatura - ${invoiceToPrint.invoiceNumber}</title>
            <style>
              body { font-family: Arial, sans-serif; margin: 20px; }
              .header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #333; padding-bottom: 20px; }
              .company-info { margin-bottom: 30px; }
              .invoice-details { display: flex; justify-content: space-between; margin-bottom: 30px; }
              .customer-info, .invoice-info { width: 45%; }
              .items-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
              .items-table th, .items-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
              .items-table th { background-color: #f2f2f2; }
              .totals { margin-top: 30px; }
              .total-row { display: flex; justify-content: space-between; margin: 10px 0; }
              .final-total { font-weight: bold; font-size: 1.2em; border-top: 2px solid #333; padding-top: 10px; }
            </style>
          </head>
          <body>
            <div class="header">
              <h1>FATURA</h1>
              <h2>Fatura No: ${invoiceToPrint.invoiceNumber}</h2>
            </div>
            
            <div class="invoice-details">
              <div class="customer-info">
                <h3>Fatura Edilen:</h3>
                <p><strong>${invoiceToPrint.customerName || 'Müşteri Adı'}</strong></p>
                <p>Araç: ${invoiceToPrint.carBrandName || ''} ${invoiceToPrint.carModelName || ''} (${invoiceToPrint.carPlate || ''})</p>
              </div>
              <div class="invoice-info">
                <h3>Fatura Bilgileri:</h3>
                <p><strong>Kesim Tarihi:</strong> ${invoiceToPrint.issueDate ? dayjs(invoiceToPrint.issueDate).format('DD.MM.YYYY') : '-'}</p>
                <p><strong>Vade Tarihi:</strong> ${invoiceToPrint.dueDate ? dayjs(invoiceToPrint.dueDate).format('DD.MM.YYYY') : '-'}</p>
                <p><strong>Durum:</strong> ${invoiceToPrint.statusDisplayName || invoiceToPrint.status}</p>
              </div>
            </div>

            <table class="items-table">
              <thead>
                <tr>
                  <th>Açıklama</th>
                  <th>Miktar</th>
                  <th>Birim Fiyat</th>
                  <th>Tutar</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Araç Kiralama Hizmeti</td>
                  <td>1</td>
                  <td>${formatCurrency(invoiceToPrint.subtotal || 0)}</td>
                  <td>${formatCurrency(invoiceToPrint.subtotal || 0)}</td>
                </tr>
              </tbody>
            </table>

            <div class="totals">
              <div class="total-row">
                <span>Ara Toplam:</span>
                <span>${formatCurrency(invoiceToPrint.subtotal || 0)}</span>
              </div>
              <div class="total-row">
                <span>KDV (${((invoiceToPrint.taxRate || 0) * 100).toFixed(1)}%):</span>
                <span>${formatCurrency(invoiceToPrint.taxAmount || 0)}</span>
              </div>
              <div class="total-row final-total">
                <span>TOPLAM:</span>
                <span>${formatCurrency(invoiceToPrint.totalAmount || 0)}</span>
              </div>
            </div>

            ${invoiceToPrint.notes ? `
            <div style="margin-top: 30px;">
              <h3>Notlar:</h3>
              <p>${invoiceToPrint.notes}</p>
            </div>
            ` : ''}

            ${invoiceToPrint.paymentTerms ? `
            <div style="margin-top: 30px;">
              <p><strong>Ödeme Koşulları:</strong> ${invoiceToPrint.paymentTerms}</p>
            </div>
            ` : ''}
          </body>
        </html>
      `);
      printWindow.document.close();
      printWindow.print();
    }
  };

  const handleDownloadInvoice = async (invoice?: Invoice) => {
    const invoiceToDownload = invoice || viewingInvoice;
    if (!invoiceToDownload) return;

    try {
      // Create PDF with jsPDF
      const pdf = new jsPDF('p', 'mm', 'a4');
      
      // PDF styling
      pdf.setFont('helvetica');
      
      // Header
      pdf.setFontSize(20);
      pdf.setFont('helvetica', 'bold');
      pdf.text('FATURA', 105, 30, { align: 'center' });
      
      pdf.setFontSize(16);
      pdf.text(`Fatura No: ${invoiceToDownload.invoiceNumber}`, 105, 40, { align: 'center' });
      
      // Company info (you can customize this)
      pdf.setFontSize(10);
      pdf.setFont('helvetica', 'normal');
      pdf.text('Araç Kiralama Şirketi', 20, 55);
      pdf.text('Adres: Şirket Adresi', 20, 60);
      pdf.text('Tel: +90 (123) 456 7890', 20, 65);
      
      // Invoice details
      pdf.setFont('helvetica', 'bold');
      pdf.text('Fatura Bilgileri:', 120, 55);
      pdf.setFont('helvetica', 'normal');
      pdf.text(`Kesim Tarihi: ${invoiceToDownload.issueDate ? dayjs(invoiceToDownload.issueDate).format('DD.MM.YYYY') : '-'}`, 120, 60);
      pdf.text(`Vade Tarihi: ${invoiceToDownload.dueDate ? dayjs(invoiceToDownload.dueDate).format('DD.MM.YYYY') : '-'}`, 120, 65);
      pdf.text(`Durum: ${invoiceToDownload.statusDisplayName || invoiceToDownload.status}`, 120, 70);
      
      // Customer info
      pdf.setFont('helvetica', 'bold');
      pdf.text('Fatura Edilen:', 20, 85);
      pdf.setFont('helvetica', 'normal');
      pdf.text(invoiceToDownload.customerName || 'Müşteri Adı', 20, 90);
      pdf.text(`Araç: ${invoiceToDownload.carBrandName || ''} ${invoiceToDownload.carModelName || ''} (${invoiceToDownload.carPlate || ''})`, 20, 95);
      
      // Table header
      pdf.setFont('helvetica', 'bold');
      pdf.setFontSize(12);
      pdf.text('Açıklama', 20, 115);
      pdf.text('Miktar', 100, 115);
      pdf.text('Birim Fiyat', 130, 115);
      pdf.text('Tutar', 160, 115);
      
      // Table line
      pdf.line(20, 117, 190, 117);
      
      // Table content
      pdf.setFont('helvetica', 'normal');
      pdf.setFontSize(10);
      pdf.text('Araç Kiralama Hizmeti', 20, 125);
      pdf.text('1', 100, 125);
      pdf.text(formatCurrency(invoiceToDownload.subtotal || 0), 130, 125);
      pdf.text(formatCurrency(invoiceToDownload.subtotal || 0), 160, 125);
      
      // Totals
      pdf.setFont('helvetica', 'bold');
      pdf.text('Ara Toplam:', 130, 140);
      pdf.text(formatCurrency(invoiceToDownload.subtotal || 0), 160, 140);
      
      pdf.text(`KDV (${((invoiceToDownload.taxRate || 0) * 100).toFixed(1)}%):`, 130, 150);
      pdf.text(formatCurrency(invoiceToDownload.taxAmount || 0), 160, 150);
      
      pdf.setFontSize(12);
      pdf.text('TOPLAM:', 130, 165);
      pdf.text(formatCurrency(invoiceToDownload.totalAmount || 0), 160, 165);
      
      // Bottom line
      pdf.line(130, 167, 190, 167);
      
      // Notes
      if (invoiceToDownload.notes) {
        pdf.setFont('helvetica', 'bold');
        pdf.text('Notlar:', 20, 185);
        pdf.setFont('helvetica', 'normal');
        const notes = pdf.splitTextToSize(invoiceToDownload.notes, 170);
        pdf.text(notes, 20, 190);
      }
      
      // Payment terms
      if (invoiceToDownload.paymentTerms) {
        pdf.setFont('helvetica', 'bold');
        pdf.text('Ödeme Koşulları:', 20, 220);
        pdf.setFont('helvetica', 'normal');
        pdf.text(invoiceToDownload.paymentTerms, 20, 225);
      }
      
      // Download PDF
      const fileName = `fatura-${invoiceToDownload.invoiceNumber}.pdf`;
      pdf.save(fileName);
    } catch (error) {
      console.error('PDF oluşturma hatası:', error);
      // Fallback to text download
      const invoiceContent = `
FATURA
Fatura No: ${invoiceToDownload.invoiceNumber}

Fatura Edilen:
${invoiceToDownload.customerName || 'Müşteri Adı'}
Araç: ${invoiceToDownload.carBrandName || ''} ${invoiceToDownload.carModelName || ''} (${invoiceToDownload.carPlate || ''})

Fatura Bilgileri:
Kesim Tarihi: ${invoiceToDownload.issueDate ? dayjs(invoiceToDownload.issueDate).format('DD.MM.YYYY') : '-'}
Vade Tarihi: ${invoiceToDownload.dueDate ? dayjs(invoiceToDownload.dueDate).format('DD.MM.YYYY') : '-'}
Durum: ${invoiceToDownload.statusDisplayName || invoiceToDownload.status}

Açıklama: Araç Kiralama Hizmeti
Miktar: 1
Birim Fiyat: ${formatCurrency(invoiceToDownload.subtotal || 0)}
Tutar: ${formatCurrency(invoiceToDownload.subtotal || 0)}

Ara Toplam: ${formatCurrency(invoiceToDownload.subtotal || 0)}
KDV (${((invoiceToDownload.taxRate || 0) * 100).toFixed(1)}%): ${formatCurrency(invoiceToDownload.taxAmount || 0)}
TOPLAM: ${formatCurrency(invoiceToDownload.totalAmount || 0)}

${invoiceToDownload.notes ? `Notlar: ${invoiceToDownload.notes}` : ''}
${invoiceToDownload.paymentTerms ? `Ödeme Koşulları: ${invoiceToDownload.paymentTerms}` : ''}
      `.trim();

      const blob = new Blob([invoiceContent], { type: 'text/plain;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `fatura-${invoiceToDownload.invoiceNumber}.txt`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    }
  };

  const calculateTax = () => {
    if (formData.subtotal && formData.taxRate) {
      const taxAmount = formData.subtotal * formData.taxRate;
      const totalAmount = formData.subtotal + taxAmount;
      setFormData({
        ...formData,
        taxAmount: Math.round(taxAmount * 100) / 100,
        totalAmount: Math.round(totalAmount * 100) / 100,
      });
    }
  };

  React.useEffect(() => {
    calculateTax();
  }, [formData.subtotal, formData.taxRate]);

  const handleSubmit = () => {
    if (!formData.rentalId || !formData.customerId || !formData.invoiceNumber || !formData.totalAmount) {
      showError('Lütfen tüm zorunlu alanları doldurun');
      return;
    }

    const invoiceData: Invoice = {
      ...formData,
      rentalId: Number(formData.rentalId),
      customerId: Number(formData.customerId),
      subtotal: Number(formData.subtotal),
      taxRate: Number(formData.taxRate),
      taxAmount: Number(formData.taxAmount),
      totalAmount: Number(formData.totalAmount),
      dueDate: formData.dueDate ? dayjs(formData.dueDate).toISOString() : '',
      issueDate: formData.issueDate ? dayjs(formData.issueDate).toISOString() : '',
    } as Invoice;

    if (editingInvoice) {
      updateInvoiceMutation.mutate({ ...invoiceData, id: editingInvoice.id });
    } else {
      createInvoiceMutation.mutate(invoiceData);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'SENT':
        return 'info';
      case 'OVERDUE':
        return 'error';
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PAID':
        return <CheckCircle />;
      case 'PENDING':
        return <Pending />;
      case 'OVERDUE':
      case 'CANCELLED':
        return <Warning />;
      default:
        return <Receipt />;
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY',
    }).format(amount);
  };

  const isOverdue = (dueDate: string) => {
    return dayjs(dueDate).isBefore(dayjs(), 'day');
  };

  // Filter invoices based on search term
  const filteredInvoices = invoices.filter((invoice) => {
    const searchLower = searchTerm.toLowerCase();
    return (
      invoice.invoiceNumber.toLowerCase().includes(searchLower) ||
      invoice.customerName?.toLowerCase().includes(searchLower) ||
      invoice.carPlate?.toLowerCase().includes(searchLower) ||
      invoice.id?.toString().includes(searchTerm)
    );
  });

  return (
    <Box sx={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', minHeight: '100vh', p: 0 }}>
      {/* Header */}
      <Box sx={{ background: 'white', p: 3, mb: 3, boxShadow: '0 4px 20px rgba(0,0,0,0.1)' }}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h4" component="h1" sx={{ 
            background: 'linear-gradient(45deg, #667eea 30%, #764ba2 90%)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            fontWeight: 'bold'
          }}>
            📄 Fatura Yönetimi
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => handleOpenDialog()}
            sx={{
              background: 'linear-gradient(45deg, #667eea 30%, #764ba2 90%)',
              '&:hover': {
                background: 'linear-gradient(45deg, #5a6fd8 30%, #6a4190 90%)',
              }
            }}
          >
            Yeni Fatura
          </Button>
        </Box>
      </Box>

      {/* Summary Cards */}
      {summary && (
        <Grid container spacing={3} mb={3} sx={{ mx: 2 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ 
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white',
              transition: 'transform 0.3s ease, box-shadow 0.3s ease',
              '&:hover': {
                transform: 'translateY(-5px)',
                boxShadow: '0 12px 40px rgba(102, 126, 234, 0.4)'
              }
            }}>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Receipt sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                  <Box>
                    <Typography variant="body2" sx={{ opacity: 0.9 }}>
                      Toplam Tutar
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                      {formatCurrency(summary.totalAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ 
              background: 'linear-gradient(135deg, #56ab2f 0%, #a8e6cf 100%)',
              color: 'white',
              transition: 'transform 0.3s ease, box-shadow 0.3s ease',
              '&:hover': {
                transform: 'translateY(-5px)',
                boxShadow: '0 12px 40px rgba(86, 171, 47, 0.4)'
              }
            }}>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <CheckCircle sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                  <Box>
                    <Typography variant="body2" sx={{ opacity: 0.9 }}>
                      Ödenen
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                      {formatCurrency(summary.paidAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ 
              background: 'linear-gradient(135deg, #f39c12 0%, #f7dc6f 100%)',
              color: 'white',
              transition: 'transform 0.3s ease, box-shadow 0.3s ease',
              '&:hover': {
                transform: 'translateY(-5px)',
                boxShadow: '0 12px 40px rgba(243, 156, 18, 0.4)'
              }
            }}>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Pending sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                  <Box>
                    <Typography variant="body2" sx={{ opacity: 0.9 }}>
                      Bekleyen
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                      {formatCurrency(summary.pendingAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ 
              background: 'linear-gradient(135deg, #e74c3c 0%, #f1948a 100%)',
              color: 'white',
              transition: 'transform 0.3s ease, box-shadow 0.3s ease',
              '&:hover': {
                transform: 'translateY(-5px)',
                boxShadow: '0 12px 40px rgba(231, 76, 60, 0.4)'
              }
            }}>
              <CardContent>
                <Box display="flex" alignItems="center">
                  <Warning sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                  <Box>
                    <Typography variant="body2" sx={{ opacity: 0.9 }}>
                      Vadesi Geçen
                    </Typography>
                    <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                      {formatCurrency(summary.overdueAmount)}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {/* Filters */}
      <Paper sx={{ 
        p: 2, 
        mb: 3,
        mx: 2,
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
        borderRadius: 3,
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)'
      }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={4}>
            <TextField
              fullWidth
              label="Ara"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
            />
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth>
              <InputLabel>Durum</InputLabel>
              <Select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                label="Durum"
              >
                <MenuItem value="ALL">Tümü</MenuItem>
                <MenuItem value="PENDING">Bekleyen</MenuItem>
                <MenuItem value="SENT">Gönderilen</MenuItem>
                <MenuItem value="PAID">Ödenen</MenuItem>
                <MenuItem value="OVERDUE">Vadesi Geçen</MenuItem>
                <MenuItem value="CANCELLED">İptal Edilen</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Invoices Table */}
      <Paper sx={{ 
        mx: 2,
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
        borderRadius: 3,
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)'
      }}>
        {invoicesLoading ? (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Fatura No</TableCell>
                  <TableCell>Müşteri</TableCell>
                  <TableCell>Araç</TableCell>
                  <TableCell>Tutar</TableCell>
                  <TableCell>Durum</TableCell>
                  <TableCell>Kesim Tarihi</TableCell>
                  <TableCell>Vade Tarihi</TableCell>
                  <TableCell>İşlemler</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredInvoices.map((invoice) => (
                  <TableRow key={invoice.id}>
                    <TableCell>
                      <Typography variant="body2" fontWeight="bold">
                        {invoice.invoiceNumber}
                      </Typography>
                    </TableCell>
                    <TableCell>{invoice.customerName || '-'}</TableCell>
                    <TableCell>
                      {invoice.carBrandName && invoice.carModelName && invoice.carPlate
                        ? `${invoice.carBrandName} ${invoice.carModelName} (${invoice.carPlate})`
                        : invoice.carPlate || '-'}
                    </TableCell>
                    <TableCell>{formatCurrency(invoice.totalAmount)}</TableCell>
                    <TableCell>
                      <Chip
                        icon={getStatusIcon(invoice.status)}
                        label={invoice.statusDisplayName || invoice.status}
                        color={getStatusColor(invoice.status) as any}
                        size="small"
                      />
                      {invoice.dueDate && isOverdue(invoice.dueDate) && invoice.status !== 'PAID' && (
                        <Chip
                          label="Vadesi Geçti"
                          color="error"
                          size="small"
                          sx={{ ml: 1 }}
                        />
                      )}
                    </TableCell>
                    <TableCell>
                      {invoice.issueDate ? dayjs(invoice.issueDate).format('DD.MM.YYYY') : '-'}
                    </TableCell>
                    <TableCell>
                      {invoice.dueDate ? dayjs(invoice.dueDate).format('DD.MM.YYYY') : '-'}
                    </TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        <Tooltip title="Görüntüle">
                          <IconButton 
                            size="small"
                            onClick={() => handleViewInvoice(invoice)}
                          >
                            <Receipt />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Düzenle">
                          <IconButton
                            size="small"
                            onClick={() => handleOpenDialog(invoice)}
                          >
                            <Edit />
                          </IconButton>
                        </Tooltip>
                        {invoice.status === 'PENDING' && (
                          <Tooltip title="Gönderildi Olarak İşaretle">
                            <IconButton
                              size="small"
                              color="info"
                              onClick={() => invoice.id && markSentMutation.mutate(invoice.id)}
                            >
                              <Send />
                            </IconButton>
                          </Tooltip>
                        )}
                        {invoice.status !== 'PAID' && (
                          <Tooltip title="Ödendi Olarak İşaretle">
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => invoice.id && markPaidMutation.mutate(invoice.id)}
                            >
                              <CheckCircle />
                            </IconButton>
                          </Tooltip>
                        )}
                        <Tooltip title="Yazdır">
                          <IconButton 
                            size="small"
                            onClick={() => handlePrintInvoice(invoice)}
                          >
                            <Print />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="İndir">
                          <IconButton 
                            size="small"
                            onClick={() => handleDownloadInvoice(invoice)}
                          >
                            <Download />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Sil">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => invoice.id && deleteInvoiceMutation.mutate(invoice.id)}
                          >
                            <Delete />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Paper>

      {/* Invoice Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingInvoice ? 'Fatura Düzenle' : 'Yeni Fatura'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Kiralama ID"
                type="number"
                value={formData.rentalId || ''}
                onChange={(e) => setFormData({ ...formData, rentalId: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Müşteri ID"
                type="number"
                value={formData.customerId || ''}
                onChange={(e) => setFormData({ ...formData, customerId: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Fatura Numarası"
                value={formData.invoiceNumber || ''}
                onChange={(e) => setFormData({ ...formData, invoiceNumber: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Kesim Tarihi"
                type="date"
                value={formData.issueDate || ''}
                onChange={(e) => setFormData({ ...formData, issueDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Vade Tarihi"
                type="date"
                value={formData.dueDate || ''}
                onChange={(e) => setFormData({ ...formData, dueDate: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Ara Toplam"
                type="number"
                value={formData.subtotal || ''}
                onChange={(e) => setFormData({ ...formData, subtotal: Number(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="KDV Oranı"
                type="number"
                value={formData.taxRate || ''}
                onChange={(e) => setFormData({ ...formData, taxRate: Number(e.target.value) })}
                inputProps={{ step: "0.01" }}
                InputProps={{ endAdornment: <Typography>%</Typography> }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="KDV Tutarı"
                type="number"
                value={formData.taxAmount || ''}
                InputProps={{ readOnly: true }}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Toplam Tutar"
                type="number"
                value={formData.totalAmount || ''}
                InputProps={{ readOnly: true }}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Durum</InputLabel>
                <Select
                  value={formData.status || 'PENDING'}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  label="Durum"
                >
                  <MenuItem value="PENDING">Bekleyen</MenuItem>
                  <MenuItem value="SENT">Gönderildi</MenuItem>
                  <MenuItem value="PAID">Ödendi</MenuItem>
                  <MenuItem value="OVERDUE">Vadesi Geçti</MenuItem>
                  <MenuItem value="CANCELLED">İptal Edildi</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Ödeme Koşulları"
                value={formData.paymentTerms || ''}
                onChange={(e) => setFormData({ ...formData, paymentTerms: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Notlar"
                multiline
                rows={3}
                value={formData.notes || ''}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>İptal</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={createInvoiceMutation.isLoading || updateInvoiceMutation.isLoading}
          >
            {(createInvoiceMutation.isLoading || updateInvoiceMutation.isLoading) ? (
              <CircularProgress size={20} />
            ) : editingInvoice ? (
              'Güncelle'
            ) : (
              'Oluştur'
            )}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Invoice View Dialog */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="lg" fullWidth>
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">
              Fatura Detayı - {viewingInvoice?.invoiceNumber}
            </Typography>
            <Box>
              <Button
                startIcon={<Print />}
                onClick={() => handlePrintInvoice()}
                sx={{ mr: 1 }}
              >
                Yazdır
              </Button>
              <Button
                startIcon={<Download />}
                onClick={() => handleDownloadInvoice()}
                variant="contained"
              >
                İndir
              </Button>
            </Box>
          </Box>
        </DialogTitle>
        <DialogContent>
          {viewingInvoice && (
            <Box>
              <Grid container spacing={3} sx={{ mt: 1 }}>
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Fatura Bilgileri
                    </Typography>
                    <Box>
                      <Typography><strong>Fatura No:</strong> {viewingInvoice.invoiceNumber}</Typography>
                      <Typography><strong>Kesim Tarihi:</strong> {viewingInvoice.issueDate ? dayjs(viewingInvoice.issueDate).format('DD.MM.YYYY') : '-'}</Typography>
                      <Typography><strong>Vade Tarihi:</strong> {viewingInvoice.dueDate ? dayjs(viewingInvoice.dueDate).format('DD.MM.YYYY') : '-'}</Typography>
                      <Typography><strong>Durum:</strong> 
                        <Chip
                          label={viewingInvoice.statusDisplayName || viewingInvoice.status}
                          color={getStatusColor(viewingInvoice.status) as any}
                          size="small"
                          sx={{ ml: 1 }}
                        />
                      </Typography>
                      {viewingInvoice.paymentTerms && (
                        <Typography><strong>Ödeme Koşulları:</strong> {viewingInvoice.paymentTerms}</Typography>
                      )}
                    </Box>
                  </Paper>
                </Grid>
                <Grid item xs={12} md={6}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Müşteri Bilgileri
                    </Typography>
                    <Box>
                      <Typography><strong>Müşteri:</strong> {viewingInvoice.customerName || '-'}</Typography>
                      <Typography><strong>Araç:</strong> {viewingInvoice.carBrandName && viewingInvoice.carModelName && viewingInvoice.carPlate
                        ? `${viewingInvoice.carBrandName} ${viewingInvoice.carModelName} (${viewingInvoice.carPlate})`
                        : viewingInvoice.carPlate || '-'}</Typography>
                    </Box>
                  </Paper>
                </Grid>
                <Grid item xs={12}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      Fatura Kalemleri
                    </Typography>
                    <TableContainer>
                      <Table>
                        <TableHead>
                          <TableRow>
                            <TableCell>Açıklama</TableCell>
                            <TableCell align="right">Miktar</TableCell>
                            <TableCell align="right">Birim Fiyat</TableCell>
                            <TableCell align="right">Tutar</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          <TableRow>
                            <TableCell>Araç Kiralama Hizmeti</TableCell>
                            <TableCell align="right">1</TableCell>
                            <TableCell align="right">{formatCurrency(viewingInvoice.subtotal || 0)}</TableCell>
                            <TableCell align="right">{formatCurrency(viewingInvoice.subtotal || 0)}</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell colSpan={3}><strong>Ara Toplam</strong></TableCell>
                            <TableCell align="right"><strong>{formatCurrency(viewingInvoice.subtotal || 0)}</strong></TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell colSpan={3}>
                              <strong>KDV ({((viewingInvoice.taxRate || 0) * 100).toFixed(1)}%)</strong>
                            </TableCell>
                            <TableCell align="right"><strong>{formatCurrency(viewingInvoice.taxAmount || 0)}</strong></TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell colSpan={3}><strong>TOPLAM</strong></TableCell>
                            <TableCell align="right">
                              <strong style={{ fontSize: '1.2em' }}>
                                {formatCurrency(viewingInvoice.totalAmount || 0)}
                              </strong>
                            </TableCell>
                          </TableRow>
                        </TableBody>
                      </Table>
                    </TableContainer>
                  </Paper>
                </Grid>
                {viewingInvoice.notes && (
                  <Grid item xs={12}>
                    <Paper sx={{ p: 2 }}>
                      <Typography variant="h6" gutterBottom>
                        Notlar
                      </Typography>
                      <Typography>{viewingInvoice.notes}</Typography>
                    </Paper>
                  </Grid>
                )}
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseViewDialog}>Kapat</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default InvoicesPage;
