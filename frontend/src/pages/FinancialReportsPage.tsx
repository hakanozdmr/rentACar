import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  Chip,
  Divider,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  TrendingUp,
  AttachMoney,
  Receipt,
  Payment,
  Assessment,
  ExpandMore,
  AccountBalance,
  ShowChart,
  Print,
  Download,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import { financialReportsApi, FinancialSummary } from '../services/api';
import dayjs from 'dayjs';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const FinancialReportsPage: React.FC = () => {
  const [startDate, setStartDate] = useState(dayjs().subtract(1, 'month').format('YYYY-MM-DD'));
  const [endDate, setEndDate] = useState(dayjs().format('YYYY-MM-DD'));
  const [reportDate, setReportDate] = useState(dayjs().format('YYYY-MM-DD'));
  const [selectedReport, setSelectedReport] = useState<string>('');

  // Fetch dashboard summary
  const { data: summary, isLoading: summaryLoading } = useQuery(
    ['financial-summary', startDate, endDate],
    () => financialReportsApi.getDashboardSummary(startDate, endDate).then(res => res.data),
    {
      enabled: Boolean(startDate && endDate),
    }
  );

  // Fetch trial balance
  const { data: trialBalance, isLoading: trialBalanceLoading } = useQuery(
    ['trial-balance'],
    () => financialReportsApi.getTrialBalance().then(res => res.data)
  );

  // Fetch income statement
  const { data: incomeStatement, isLoading: incomeStatementLoading } = useQuery(
    ['income-statement', startDate, endDate],
    () => financialReportsApi.getIncomeStatement(startDate, endDate).then(res => res.data),
    {
      enabled: Boolean(startDate && endDate && selectedReport === 'income-statement'),
    }
  );

  // Fetch balance sheet
  const { data: balanceSheet, isLoading: balanceSheetLoading } = useQuery(
    ['balance-sheet', reportDate],
    () => financialReportsApi.getBalanceSheet(reportDate).then(res => res.data),
    {
      enabled: Boolean(reportDate && selectedReport === 'balance-sheet'),
    }
  );

  // Fetch cash flow statement
  const { data: cashFlowStatement, isLoading: cashFlowStatementLoading } = useQuery(
    ['cash-flow-statement', startDate, endDate],
    () => financialReportsApi.getCashFlowStatement(startDate, endDate).then(res => res.data),
    {
      enabled: Boolean(startDate && endDate && selectedReport === 'cash-flow-statement'),
    }
  );

  // Fetch tax report
  const { data: taxReport, isLoading: taxReportLoading } = useQuery(
    ['tax-report', startDate, endDate],
    () => financialReportsApi.getTaxReport(startDate, endDate).then(res => res.data),
    {
      enabled: Boolean(startDate && endDate && selectedReport === 'tax-report'),
    }
  );

  const formatCurrency = (amount: number | null | undefined) => {
    if (amount === null || amount === undefined || isNaN(amount)) {
      return '₺0,00';
    }
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY',
    }).format(amount);
  };

  const handlePrintReport = (reportType: string) => {
    const reportContent = document.getElementById(`report-${reportType}`);
    if (reportContent) {
      const printWindow = window.open('', '_blank');
      if (printWindow) {
        printWindow.document.write(`
          <html>
            <head>
              <title>${reportType} Raporu</title>
              <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                table { width: 100%; border-collapse: collapse; }
                th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                th { background-color: #f2f2f2; }
                .header { text-align: center; margin-bottom: 20px; }
                .summary { margin: 20px 0; }
              </style>
            </head>
            <body>
              ${reportContent.innerHTML}
            </body>
          </html>
        `);
        printWindow.document.close();
        printWindow.print();
      }
    }
  };

  const handleDownloadReport = async (reportType: string) => {
    const reportContent = document.getElementById(`report-${reportType}`);
    if (reportContent) {
      try {
        // Create canvas from HTML content
        const canvas = await html2canvas(reportContent, {
          scale: 2,
          useCORS: true,
          backgroundColor: '#ffffff',
          logging: false,
        });
        
        // Create PDF
        const imgData = canvas.toDataURL('image/png');
        const pdf = new jsPDF('p', 'mm', 'a4');
        
        // Get dimensions
        const imgWidth = 210; // A4 width in mm
        const pageHeight = 295; // A4 height in mm
        const imgHeight = (canvas.height * imgWidth) / canvas.width;
        let heightLeft = imgHeight;
        
        let position = 0;
        
        // Add header
        pdf.setFontSize(18);
        pdf.setFont('helvetica', 'bold');
        pdf.text(getReportTitle(reportType), 105, 15, { align: 'center' });
        
        // Add date range if applicable
        if (startDate && endDate) {
          pdf.setFontSize(10);
          pdf.setFont('helvetica', 'normal');
          pdf.text(`Tarih Aralığı: ${dayjs(startDate).format('DD.MM.YYYY')} - ${dayjs(endDate).format('DD.MM.YYYY')}`, 105, 22, { align: 'center' });
        }
        
        // Add image
        pdf.addImage(imgData, 'PNG', 0, 30, imgWidth, imgHeight);
        
        // Add new pages if needed
        heightLeft -= pageHeight - 30;
        while (heightLeft >= 0) {
          position = heightLeft - imgHeight + 30;
          pdf.addPage();
          pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
          heightLeft -= pageHeight;
        }
        
        // Download PDF
        const fileName = `${reportType}-raporu-${dayjs().format('YYYY-MM-DD')}.pdf`;
        pdf.save(fileName);
      } catch (error) {
        console.error('PDF oluşturma hatası:', error);
        // Fallback to text download
        const content = reportContent.innerText;
        const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${reportType}-raporu-${dayjs().format('YYYY-MM-DD')}.txt`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      }
    }
  };

  const getReportTitle = (reportType: string) => {
    const titles: { [key: string]: string } = {
      'trial-balance': 'Mizan Raporu',
      'income-statement': 'Gelir Tablosu',
      'balance-sheet': 'Bilanço',
      'cash-flow-statement': 'Nakit Akış Tablosu',
      'tax-report': 'Vergi Raporu'
    };
    return titles[reportType] || 'Rapor';
  };

  return (
    <Box sx={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', minHeight: '100vh', p: 0 }}>
      {/* Header */}
      <Box sx={{ background: 'white', p: 3, mb: 3, boxShadow: '0 4px 20px rgba(0,0,0,0.1)' }}>
        <Typography variant="h4" component="h1" gutterBottom sx={{ 
          background: 'linear-gradient(45deg, #667eea 30%, #764ba2 90%)',
          backgroundClip: 'text',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          fontWeight: 'bold',
          textAlign: 'center'
        }}>
          📊 Finansal Raporlar
        </Typography>
      </Box>

      {/* Date Range Selector */}
      <Paper sx={{ 
        p: 3, 
        mb: 3, 
        mx: 2,
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
        borderRadius: 3,
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)'
      }}>
        <Typography variant="h6" gutterBottom sx={{ 
          color: '#333',
          fontWeight: 'bold',
          display: 'flex',
          alignItems: 'center',
          gap: 1
        }}>
          📅 Tarih Aralığı ve Rapor Seçimi
        </Typography>
        <Grid container spacing={3} alignItems="center">
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Başlangıç Tarihi"
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              label="Bitiş Tarihi"
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControl fullWidth>
              <InputLabel>Rapor Türü</InputLabel>
              <Select
                value={selectedReport}
                onChange={(e) => setSelectedReport(e.target.value)}
                label="Rapor Türü"
              >
                <MenuItem value="">Rapor Seçin</MenuItem>
                <MenuItem value="income-statement">Gelir Tablosu</MenuItem>
                <MenuItem value="balance-sheet">Bilanço</MenuItem>
                <MenuItem value="cash-flow-statement">Nakit Akış Tablosu</MenuItem>
                <MenuItem value="tax-report">Vergi Raporu</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Paper>

      {/* Financial Summary Dashboard */}
      <Paper sx={{ 
        p: 3, 
        mb: 3,
        mx: 2,
        boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
        borderRadius: 3,
        background: 'rgba(255, 255, 255, 0.95)',
        backdropFilter: 'blur(10px)'
      }}>
        <Typography variant="h6" gutterBottom sx={{ 
          color: '#333',
          fontWeight: 'bold',
          display: 'flex',
          alignItems: 'center',
          gap: 1
        }}>
          💰 Finansal Özet
        </Typography>
        {summaryLoading ? (
          <Box display="flex" justifyContent="center" p={4}>
            <CircularProgress />
          </Box>
        ) : summary ? (
          <Grid container spacing={3}>
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
                    <TrendingUp sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                    <Box>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>
                        Toplam Gelir
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                        {formatCurrency(summary.totalRevenue || 0)}
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
                    <Receipt sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                    <Box>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>
                        Toplam Faturalanan
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                        {formatCurrency(summary.totalInvoiced || 0)}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Card sx={{ 
                background: 'linear-gradient(135deg, #3498db 0%, #85c1e9 100%)',
                color: 'white',
                transition: 'transform 0.3s ease, box-shadow 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-5px)',
                  boxShadow: '0 12px 40px rgba(52, 152, 219, 0.4)'
                }
              }}>
                <CardContent>
                  <Box display="flex" alignItems="center">
                    <Payment sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                    <Box>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>
                        Toplam Ödenen
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                        {formatCurrency(summary.totalPaid || 0)}
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
                    <AttachMoney sx={{ mr: 2, fontSize: 40, opacity: 0.9 }} />
                    <Box>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>
                        Net Kar
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                        {formatCurrency(summary.netProfit || 0)}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            
            {/* Additional Summary Cards */}
            <Grid item xs={12} sm={6} md={4}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Bekleyen Ödemeler
                  </Typography>
                  <Typography variant="h6" color="warning.main">
                    {formatCurrency(summary.pendingPayments || 0)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Vadesi Geçen Ödemeler
                  </Typography>
                  <Typography variant="h6" color="error.main">
                    {formatCurrency(summary.overduePayments || 0)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} sm={6} md={4}>
              <Card>
                <CardContent>
                  <Typography color="textSecondary" gutterBottom>
                    Vergi Yükümlülüğü
                  </Typography>
                  <Typography variant="h6" color="info.main">
                    {formatCurrency(summary.totalTaxLiability || 0)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        ) : (
          <Alert severity="info">Özet verileri yüklenemedi</Alert>
        )}
      </Paper>

      {/* Trial Balance */}
      <Accordion>
        <AccordionSummary expandIcon={<ExpandMore />}>
          <Typography variant="h6">
            <AccountBalance sx={{ mr: 1, verticalAlign: 'middle' }} />
            Mizan Raporu
          </Typography>
        </AccordionSummary>
        <AccordionDetails>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
            <Typography variant="subtitle1">Tüm Hesaplar Mizanı</Typography>
            <Box>
              <Button
                startIcon={<Print />}
                onClick={() => handlePrintReport('trial-balance')}
                sx={{ mr: 1 }}
              >
                Yazdır
              </Button>
              <Button
                startIcon={<Download />}
                onClick={() => handleDownloadReport('trial-balance')}
              >
                İndir
              </Button>
            </Box>
          </Box>
          {trialBalanceLoading ? (
            <Box display="flex" justifyContent="center" p={4}>
              <CircularProgress />
            </Box>
          ) : trialBalance ? (
            <Box id="report-trial-balance">
              <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Hesap Kodu</TableCell>
                    <TableCell>Hesap Adı</TableCell>
                    <TableCell align="right">Borç</TableCell>
                    <TableCell align="right">Alacak</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {trialBalance.map((entry: any, index: number) => (
                    <TableRow key={index}>
                      <TableCell>{entry.accountCode}</TableCell>
                      <TableCell>{entry.accountName}</TableCell>
                      <TableCell align="right">
                        {entry.debitAmount > 0 ? formatCurrency(entry.debitAmount) : '-'}
                      </TableCell>
                      <TableCell align="right">
                        {entry.creditAmount > 0 ? formatCurrency(entry.creditAmount) : '-'}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
            </Box>
          ) : (
            <Alert severity="info">Mizan verileri yüklenemedi</Alert>
          )}
        </AccordionDetails>
      </Accordion>

      {/* Income Statement */}
      {selectedReport === 'income-statement' && (
        <Accordion expanded>
          <AccordionSummary expandIcon={<ExpandMore />}>
            <Typography variant="h6">
              <ShowChart sx={{ mr: 1, verticalAlign: 'middle' }} />
              Gelir Tablosu ({startDate} - {endDate})
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="subtitle1">Gelir ve Gider Tablosu</Typography>
              <Box>
                <Button
                  startIcon={<Print />}
                  onClick={() => handlePrintReport('income-statement')}
                  sx={{ mr: 1 }}
                >
                  Yazdır
                </Button>
                <Button
                  startIcon={<Download />}
                  onClick={() => handleDownloadReport('income-statement')}
                >
                  İndir
                </Button>
              </Box>
            </Box>
            {incomeStatementLoading ? (
              <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
              </Box>
            ) : incomeStatement ? (
              <Box id="report-income-statement">
                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Typography variant="h6" gutterBottom>
                      Gelirler
                    </Typography>
                    {incomeStatement.revenues?.map((item: any, index: number) => (
                      <Box key={index} display="flex" justifyContent="space-between" mb={1}>
                        <Typography>{item?.name || 'Gelir'}</Typography>
                        <Typography>{formatCurrency(item?.amount || 0)}</Typography>
                      </Box>
                    ))}
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Typography variant="h6" gutterBottom>
                      Giderler
                    </Typography>
                    {incomeStatement.expenses?.map((item: any, index: number) => (
                      <Box key={index} display="flex" justifyContent="space-between" mb={1}>
                        <Typography>{item?.name || 'Gider'}</Typography>
                        <Typography>{formatCurrency(item?.amount || 0)}</Typography>
                      </Box>
                    ))}
                  </Grid>
                </Grid>
                <Divider sx={{ my: 2 }} />
                <Box display="flex" justifyContent="space-between">
                  <Typography variant="h6">Net Kar/Zarar:</Typography>
                  <Typography variant="h6" color={(incomeStatement.netProfit || 0) >= 0 ? 'success.main' : 'error.main'}>
                    {formatCurrency(incomeStatement.netProfit || 0)}
                  </Typography>
                </Box>
              </Box>
            ) : (
              <Alert severity="info">Gelir tablosu verileri yüklenemedi</Alert>
            )}
          </AccordionDetails>
        </Accordion>
      )}

      {/* Balance Sheet */}
      {selectedReport === 'balance-sheet' && (
        <Accordion expanded>
          <AccordionSummary expandIcon={<ExpandMore />}>
            <Typography variant="h6">
              <AccountBalance sx={{ mr: 1, verticalAlign: 'middle' }} />
              Bilanço ({reportDate})
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="subtitle1">Tarihi Bilanço</Typography>
              <Box>
                <TextField
                  label="Bilanço Tarihi"
                  type="date"
                  value={reportDate}
                  onChange={(e) => setReportDate(e.target.value)}
                  size="small"
                  sx={{ mr: 2 }}
                />
                <Button
                  startIcon={<Print />}
                  onClick={() => handlePrintReport('balance-sheet')}
                  sx={{ mr: 1 }}
                >
                  Yazdır
                </Button>
                <Button
                  startIcon={<Download />}
                  onClick={() => handleDownloadReport('balance-sheet')}
                >
                  İndir
                </Button>
              </Box>
            </Box>
            {balanceSheetLoading ? (
              <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
              </Box>
            ) : balanceSheet ? (
              <Box id="report-balance-sheet">
                <Grid container spacing={3}>
                  <Grid item xs={12} md={4}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Toplam Aktifler
                        </Typography>
                        <Typography variant="h6" color="primary.main">
                          {formatCurrency(balanceSheet.totalAssets || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Toplam Yükümlülükler
                        </Typography>
                        <Typography variant="h6" color="error.main">
                          {formatCurrency(balanceSheet.totalLiabilities || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={4}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Toplam Öz Kaynaklar
                        </Typography>
                        <Typography variant="h6" color="success.main">
                          {formatCurrency(balanceSheet.totalEquity || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </Box>
            ) : (
              <Alert severity="info">Bilanço verileri yüklenemedi</Alert>
            )}
          </AccordionDetails>
        </Accordion>
      )}

      {/* Cash Flow Statement */}
      {selectedReport === 'cash-flow-statement' && (
        <Accordion expanded>
          <AccordionSummary expandIcon={<ExpandMore />}>
            <Typography variant="h6">
              <TrendingUp sx={{ mr: 1, verticalAlign: 'middle' }} />
              Nakit Akış Tablosu ({startDate} - {endDate})
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="subtitle1">Nakit Akış Raporu</Typography>
              <Box>
                <Button
                  startIcon={<Print />}
                  onClick={() => handlePrintReport('cash-flow-statement')}
                  sx={{ mr: 1 }}
                >
                  Yazdır
                </Button>
                <Button
                  startIcon={<Download />}
                  onClick={() => handleDownloadReport('cash-flow-statement')}
                >
                  İndir
                </Button>
              </Box>
            </Box>
            {cashFlowStatementLoading ? (
              <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
              </Box>
            ) : cashFlowStatement ? (
              <Box id="report-cash-flow-statement">
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Nakit Girişleri
                        </Typography>
                        <Typography variant="h6" color="success.main">
                          {formatCurrency(cashFlowStatement.cashInflows || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Nakit Çıkışları
                        </Typography>
                        <Typography variant="h6" color="error.main">
                          {formatCurrency(cashFlowStatement.cashOutflows || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Net Nakit Akışı
                        </Typography>
                        <Typography variant="h5" color={(cashFlowStatement.netCashFlow || 0) >= 0 ? 'success.main' : 'error.main'}>
                          {formatCurrency(cashFlowStatement.netCashFlow || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </Box>
            ) : (
              <Alert severity="info">Nakit akış tablosu verileri yüklenemedi</Alert>
            )}
          </AccordionDetails>
        </Accordion>
      )}

      {/* Tax Report */}
      {selectedReport === 'tax-report' && (
        <Accordion expanded>
          <AccordionSummary expandIcon={<ExpandMore />}>
            <Typography variant="h6">
              <Assessment sx={{ mr: 1, verticalAlign: 'middle' }} />
              Vergi Raporu ({startDate} - {endDate})
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
              <Typography variant="subtitle1">Vergi Raporu</Typography>
              <Box>
                <Button
                  startIcon={<Print />}
                  onClick={() => handlePrintReport('tax-report')}
                  sx={{ mr: 1 }}
                >
                  Yazdır
                </Button>
                <Button
                  startIcon={<Download />}
                  onClick={() => handleDownloadReport('tax-report')}
                >
                  İndir
                </Button>
              </Box>
            </Box>
            {taxReportLoading ? (
              <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
              </Box>
            ) : taxReport ? (
              <Box id="report-tax-report">
                <Grid container spacing={3}>
                  <Grid item xs={12} md={6}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          Toplam Vergi Tutarı
                        </Typography>
                        <Typography variant="h6" color="error.main">
                          {formatCurrency(taxReport.totalTaxAmount || 0)}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                  <Grid item xs={12} md={6}>
                    <Card>
                      <CardContent>
                        <Typography color="textSecondary" gutterBottom>
                          KDV Oranı
                        </Typography>
                        <Typography variant="h6">
                          %{taxReport.vatRate || 0}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                </Grid>
              </Box>
            ) : (
              <Alert severity="info">Vergi raporu verileri yüklenemedi</Alert>
            )}
          </AccordionDetails>
        </Accordion>
      )}
    </Box>
  );
};

export default FinancialReportsPage;
