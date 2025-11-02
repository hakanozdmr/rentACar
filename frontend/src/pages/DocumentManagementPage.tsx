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
  Tabs,
  Tab,
  Input,
} from '@mui/material';
import {
  Add,
  Edit,
  Delete,
  CloudUpload,
  CheckCircle,
  Cancel,
  Visibility,
  Verified,
  PictureAsPdf,
  Image,
  Description,
  Search,
  Pending,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { 
  rentalDocumentsApi, 
  rentalsApi,
  RentalDocument,
  Rental 
} from '../services/api';
import { useSnackbar } from '../contexts/SnackbarContext';
import dayjs from 'dayjs';

const DocumentManagementPage: React.FC = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [editingDocument, setEditingDocument] = useState<RentalDocument | null>(null);
  const [viewingDocument, setViewingDocument] = useState<RentalDocument | null>(null);
  const [openViewDialog, setOpenViewDialog] = useState(false);
  const [filterType, setFilterType] = useState<string>('all');
  const [tabValue, setTabValue] = useState(0);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const { showSuccess, showError } = useSnackbar();
  const queryClient = useQueryClient();

  // Form state
  const [formData, setFormData] = useState<Partial<RentalDocument>>({
    rentalId: 0,
    fileName: '',
    fileType: '',
    fileSize: 0,
    filePath: '',
    documentType: 'OTHER',
    description: '',
    isVerified: false,
  });

  // Fetch documents
  const { data: documents = [], isLoading: documentsLoading } = useQuery(
    ['rental-documents', filterType],
    () => {
      if (filterType === 'all') {
        return rentalDocumentsApi.getAll().then(res => res.data);
      }
      return rentalDocumentsApi.getAll().then(res => 
        res.data.filter(doc => doc.documentType === filterType)
      );
    }
  );

  // Fetch rentals
  const { data: rentals = [] } = useQuery(
    'rentals',
    () => rentalsApi.getAll().then(res => res.data)
  );

  // Mutations
  const createMutation = useMutation(
    (document: RentalDocument) => rentalDocumentsApi.create(document),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['rental-documents']);
        showSuccess('Belge başarıyla eklendi');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Belge eklenirken hata oluştu');
      },
    }
  );

  const updateMutation = useMutation(
    (document: RentalDocument) => rentalDocumentsApi.update(document),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['rental-documents']);
        showSuccess('Belge başarıyla güncellendi');
        handleCloseDialog();
      },
      onError: (error: any) => {
        showError(error.message || 'Belge güncellenirken hata oluştu');
      },
    }
  );

  const deleteMutation = useMutation(
    (id: number) => rentalDocumentsApi.delete(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['rental-documents']);
        showSuccess('Belge başarıyla silindi');
      },
      onError: (error: any) => {
        showError(error.message || 'Belge silinirken hata oluştu');
      },
    }
  );

  const verifyMutation = useMutation(
    ({ id, verifiedBy }: { id: number, verifiedBy: string }) =>
      rentalDocumentsApi.verify(id, verifiedBy),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['rental-documents']);
        showSuccess('Belge başarıyla doğrulandı');
      },
      onError: (error: any) => {
        showError(error.message || 'Belge doğrulanırken hata oluştu');
      },
    }
  );

  const uploadMutation = useMutation(
    (file: File) => rentalDocumentsApi.uploadFile(file),
    {
      onSuccess: (data) => {
        showSuccess('Dosya başarıyla yüklendi');
        setUploadFile(null);
        queryClient.invalidateQueries(['rental-documents']);
      },
      onError: (error: any) => {
        showError(error.message || 'Dosya yüklenirken hata oluştu');
      },
    }
  );

  // Handlers
  const handleOpenDialog = () => {
    setFormData({
      rentalId: 0,
      fileName: '',
      fileType: '',
      fileSize: 0,
      filePath: '',
      documentType: 'OTHER',
      description: '',
      isVerified: false,
    });
    setEditingDocument(null);
    setOpenDialog(true);
  };

  const handleEditDocument = (document: RentalDocument) => {
    setFormData(document);
    setEditingDocument(document);
    setOpenDialog(true);
  };

  const handleViewDocument = (document: RentalDocument) => {
    setViewingDocument(document);
    setOpenViewDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setEditingDocument(null);
    setFormData({
      rentalId: 0,
      fileName: '',
      fileType: '',
      fileSize: 0,
      filePath: '',
      documentType: 'OTHER',
      description: '',
      isVerified: false,
    });
  };

  const handleCloseViewDialog = () => {
    setOpenViewDialog(false);
    setViewingDocument(null);
  };

  const handleSubmit = () => {
    if (editingDocument) {
      updateMutation.mutate(formData as RentalDocument);
    } else {
      createMutation.mutate(formData as RentalDocument);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Bu belgeyi silmek istediğinizden emin misiniz?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleVerify = (document: RentalDocument) => {
    verifyMutation.mutate({
      id: document.id!,
      verifiedBy: 'admin' // In real app, get from auth context
    });
  };

  const handleFileUpload = () => {
    if (!uploadFile) return;
    uploadMutation.mutate(uploadFile);
    setUploadDialogOpen(false);
    setUploadFile(null);
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'DELIVERY_PHOTO':
      case 'PICKUP_PHOTO':
        return 'primary';
      case 'DAMAGE_REPORT':
        return 'error';
      case 'CONTRACT':
        return 'success';
      case 'ID_CARD':
      case 'DRIVER_LICENSE':
        return 'warning';
      default:
        return 'default';
    }
  };

  const getTypeText = (type: string) => {
    const typeMap: Record<string, string> = {
      'DELIVERY_PHOTO': 'Teslim Fotoğrafı',
      'PICKUP_PHOTO': 'Teslim Alma Fotoğrafı',
      'DAMAGE_REPORT': 'Hasar Raporu',
      'CONTRACT': 'Sözleşme',
      'ID_CARD': 'Kimlik',
      'DRIVER_LICENSE': 'Ehliyet',
      'INSURANCE': 'Sigorta',
      'CONDITION_CHECK': 'Durum Kontrol',
      'SIGNATURE': 'İmza',
      'OTHER': 'Diğer',
    };
    return typeMap[type] || type;
  };

  const documentTypeGroups = {
    all: 'Tümü',
    DELIVERY_PHOTO: 'Fotoğraflar',
    DAMAGE_REPORT: 'Raporlar',
    CONTRACT: 'Sözleşmeler',
    ID_CARD: 'Kimlikler',
  };

  const typeTabs = Object.entries(documentTypeGroups);

  // Statistics
  const stats = {
    total: documents.length,
    verified: documents.filter(d => d.isVerified).length,
    pending: documents.filter(d => !d.isVerified).length,
    photos: documents.filter(d => d.documentType === 'DELIVERY_PHOTO' || d.documentType === 'PICKUP_PHOTO').length,
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Belge Yönetimi
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<CloudUpload />}
            onClick={() => setUploadDialogOpen(true)}
          >
            Dosya Yükle
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={handleOpenDialog}
          >
            Yeni Belge
          </Button>
        </Box>
      </Box>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Toplam Belge
              </Typography>
              <Typography variant="h4">{stats.total}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Doğrulanan
              </Typography>
              <Typography variant="h4" color="success.main">
                {stats.verified}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Bekleyen
              </Typography>
              <Typography variant="h4" color="warning.main">
                {stats.pending}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Fotoğraf
              </Typography>
              <Typography variant="h4" color="info.main">
                {stats.photos}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Paper sx={{ mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(_, newValue) => {
            setTabValue(newValue);
            const typeMap = ['all', 'DELIVERY_PHOTO', 'DAMAGE_REPORT', 'CONTRACT', 'ID_CARD'];
            setFilterType(typeMap[newValue]);
          }}
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          {typeTabs.map(([key, label]) => (
            <Tab key={key} label={label} />
          ))}
        </Tabs>
      </Paper>

      {/* Table */}
      {documentsLoading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Dosya Adı</TableCell>
                <TableCell>Tip</TableCell>
                <TableCell>Kiralama</TableCell>
                <TableCell>Boyut</TableCell>
                <TableCell>Yüklenme Tarihi</TableCell>
                <TableCell>Durum</TableCell>
                <TableCell>İşlemler</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {documents.map((document) => (
                <TableRow key={document.id}>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      {document.fileType?.includes('image') ? <Image /> : <Description />}
                      <Typography variant="body2">
                        {document.fileName}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={getTypeText(document.documentType)}
                      color={getTypeColor(document.documentType) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {document.rentalInfo || `#${document.rentalId}`}
                  </TableCell>
                  <TableCell>
                    {(document.fileSize / 1024).toFixed(2)} KB
                  </TableCell>
                  <TableCell>
                    {document.uploadedAt ? dayjs(document.uploadedAt).format('DD.MM.YYYY HH:mm') : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={document.isVerified ? 'Doğrulandı' : 'Bekliyor'}
                      color={document.isVerified ? 'success' : 'warning'}
                      size="small"
                      icon={document.isVerified ? <CheckCircle /> : <Pending />}
                    />
                  </TableCell>
                  <TableCell>
                    <Tooltip title="Görüntüle">
                      <IconButton size="small" onClick={() => handleViewDocument(document)}>
                        <Visibility />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Düzenle">
                      <IconButton size="small" onClick={() => handleEditDocument(document)}>
                        <Edit />
                      </IconButton>
                    </Tooltip>
                    {!document.isVerified && (
                      <Tooltip title="Doğrula">
                        <IconButton size="small" onClick={() => handleVerify(document)}>
                          <Verified />
                        </IconButton>
                      </Tooltip>
                    )}
                    <Tooltip title="Sil">
                      <IconButton size="small" onClick={() => handleDelete(document.id!)}>
                        <Delete />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {/* Create/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>
          {editingDocument ? 'Belge Düzenle' : 'Yeni Belge'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>Kiralama</InputLabel>
                <Select
                  value={formData.rentalId}
                  label="Kiralama"
                  onChange={(e) => setFormData({ ...formData, rentalId: Number(e.target.value) })}
                >
                  {rentals.map((rental) => (
                    <MenuItem key={rental.id} value={rental.id}>
                      #{rental.id} - {rental.carPlate}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth required>
                <InputLabel>Belge Tipi</InputLabel>
                <Select
                  value={formData.documentType}
                  label="Belge Tipi"
                  onChange={(e) => setFormData({ ...formData, documentType: e.target.value as any })}
                >
                  <MenuItem value="DELIVERY_PHOTO">Teslim Fotoğrafı</MenuItem>
                  <MenuItem value="PICKUP_PHOTO">Teslim Alma Fotoğrafı</MenuItem>
                  <MenuItem value="DAMAGE_REPORT">Hasar Raporu</MenuItem>
                  <MenuItem value="CONTRACT">Sözleşme</MenuItem>
                  <MenuItem value="ID_CARD">Kimlik Fotokopisi</MenuItem>
                  <MenuItem value="DRIVER_LICENSE">Ehliyet Fotokopisi</MenuItem>
                  <MenuItem value="INSURANCE">Sigorta Belgesi</MenuItem>
                  <MenuItem value="CONDITION_CHECK">Durum Kontrol Formu</MenuItem>
                  <MenuItem value="SIGNATURE">İmza</MenuItem>
                  <MenuItem value="OTHER">Diğer</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Dosya Adı"
                value={formData.fileName}
                onChange={(e) => setFormData({ ...formData, fileName: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Dosya Tipi"
                value={formData.fileType}
                onChange={(e) => setFormData({ ...formData, fileType: e.target.value })}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                type="number"
                label="Dosya Boyutu (Byte)"
                value={formData.fileSize}
                onChange={(e) => setFormData({ ...formData, fileSize: Number(e.target.value) })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Açıklama"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>İptal</Button>
          <Button onClick={handleSubmit} variant="contained" disabled={createMutation.isLoading}>
            {createMutation.isLoading ? <CircularProgress size={24} /> : 'Kaydet'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Upload Dialog */}
      <Dialog open={uploadDialogOpen} onClose={() => setUploadDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Dosya Yükle</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Input
              type="file"
              fullWidth
              onChange={(e) => {
                const file = (e.target as HTMLInputElement).files?.[0];
                if (file) {
                  setUploadFile(file);
                }
              }}
            />
            {uploadFile && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="body2">
                  Seçilen: {uploadFile.name}
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Boyut: {(uploadFile.size / 1024).toFixed(2)} KB
                </Typography>
              </Box>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUploadDialogOpen(false)}>İptal</Button>
          <Button 
            onClick={handleFileUpload} 
            variant="contained" 
            disabled={!uploadFile || uploadMutation.isLoading}
          >
            {uploadMutation.isLoading ? <CircularProgress size={24} /> : 'Yükle'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* View Dialog */}
      <Dialog open={openViewDialog} onClose={handleCloseViewDialog} maxWidth="md" fullWidth>
        <DialogTitle>Belge Detayları</DialogTitle>
        <DialogContent>
          {viewingDocument && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Dosya Adı
                  </Typography>
                  <Typography variant="body1" fontWeight="bold">
                    {viewingDocument.fileName}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Durum
                  </Typography>
                  <Chip
                    label={viewingDocument.isVerified ? 'Doğrulandı' : 'Bekliyor'}
                    color={viewingDocument.isVerified ? 'success' : 'warning'}
                    size="small"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Belge Tipi
                  </Typography>
                  <Typography variant="body1">
                    {getTypeText(viewingDocument.documentType)}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="textSecondary">
                    Dosya Boyutu
                  </Typography>
                  <Typography variant="body1">
                    {(viewingDocument.fileSize / 1024).toFixed(2)} KB
                  </Typography>
                </Grid>
                {viewingDocument.description && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="textSecondary">
                      Açıklama
                    </Typography>
                    <Typography variant="body1">
                      {viewingDocument.description}
                    </Typography>
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

export default DocumentManagementPage;

