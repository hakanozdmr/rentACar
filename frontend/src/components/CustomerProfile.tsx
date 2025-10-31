import React, { useState, useEffect } from 'react';
import {
  Paper,
  Typography,
  Box,
  Grid,
  TextField,
  Button,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { customerPortalApi, Customer } from '../services/api';

const CustomerProfile: React.FC = () => {
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<Partial<Customer>>({});
  const queryClient = useQueryClient();

  const { data: profile, isLoading, error } = useQuery(
    ['customerProfile'],
    () => customerPortalApi.getProfile().then(res => res.data)
  );

  const updateProfileMutation = useMutation(
    (data: Customer) => customerPortalApi.updateProfile(data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['customerProfile']);
        setIsEditing(false);
      }
    }
  );

  useEffect(() => {
    if (profile) {
      setFormData(profile);
    }
  }, [profile]);

  const handleInputChange = (field: keyof Customer) => (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: event.target.value
    }));
  };

  const handleSave = () => {
    if (formData && profile) {
      const updatedProfile = { ...profile, ...formData };
      updateProfileMutation.mutate(updatedProfile);
    }
  };

  if (isLoading) return <CircularProgress />;
  if (error) return <Alert severity="error">Profil bilgileri yüklenirken hata oluştu</Alert>;

  return (
    <Paper sx={{ p: 3 }}>
      <>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">Profil Bilgilerim</Typography>
        <Button
          variant={isEditing ? "contained" : "outlined"}
          onClick={isEditing ? handleSave : () => setIsEditing(true)}
          disabled={updateProfileMutation.isLoading}
        >
          {updateProfileMutation.isLoading ? (
            <CircularProgress size={20} />
          ) : isEditing ? (
            'Kaydet'
          ) : (
            'Düzenle'
          )}
        </Button>
      </Box>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Ad"
            value={formData.firstName || ''}
            onChange={handleInputChange('firstName')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Soyad"
            value={formData.lastName || ''}
            onChange={handleInputChange('lastName')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="E-posta"
            type="email"
            value={formData.email || ''}
            onChange={handleInputChange('email')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={6}>
          <TextField
            fullWidth
            label="Telefon"
            value={formData.phone || ''}
            onChange={handleInputChange('phone')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={8}>
          <TextField
            fullWidth
            label="Adres"
            value={formData.street || ''}
            onChange={handleInputChange('street')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Posta Kodu"
            value={formData.zipcode || ''}
            onChange={handleInputChange('zipcode')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Şehir"
            value={formData.city || ''}
            onChange={handleInputChange('city')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="Doğum Tarihi"
            type="date"
            value={formData.dateOfBirth ? formData.dateOfBirth.split('T')[0] : ''}
            onChange={(e) => setFormData(prev => ({ ...prev, dateOfBirth: e.target.value }))}
            disabled={!isEditing}
            InputLabelProps={{ shrink: true }}
          />
        </Grid>
        <Grid item xs={12} sm={4}>
          <TextField
            fullWidth
            label="T.C. Kimlik No"
            value={formData.idNumber || ''}
            onChange={handleInputChange('idNumber')}
            disabled={!isEditing}
          />
        </Grid>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Ehliyet No"
            value={formData.driverLicenseNumber || ''}
            onChange={handleInputChange('driverLicenseNumber')}
            disabled={!isEditing}
          />
        </Grid>
      </Grid>

      {updateProfileMutation.error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          Profil güncellenirken hata oluştu
        </Alert>
      )}
      </>
    </Paper>
  );
};

export default CustomerProfile;
