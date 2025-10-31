import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Container,
  Alert,
  CircularProgress,
  Link
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { useMutation } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { authApi, User } from '../services/api';

interface RegisterFormData {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  confirmPassword: string;
}

const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState<string>('');
  
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors }
  } = useForm<RegisterFormData>();

  const password = watch('password');

  const registerMutation = useMutation(
    (data: RegisterFormData) => {
      const { confirmPassword, ...userData } = data;
      return authApi.register(userData as User).then(res => res.data);
    },
    {
      onSuccess: (response) => {
        setSuccess('Hesabınız başarıyla oluşturuldu! Giriş yapabilirsiniz.');
        setError('');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      },
      onError: (error: any) => {
        console.error('Register error:', error);
        let errorMessage = 'Kayıt olurken bir hata oluştu';
        
        if (error.response) {
          // Server responded with error status
          if (error.response.status === 404) {
            errorMessage = 'Kayıt servisi bulunamadı. Lütfen daha sonra tekrar deneyin.';
          } else if (error.response.status === 400) {
            errorMessage = error.response.data || 'Geçersiz bilgiler. Lütfen kontrol edin.';
          } else {
            errorMessage = error.response.data || `Sunucu hatası (${error.response.status})`;
          }
        } else if (error.request) {
          // Request was made but no response received
          errorMessage = 'Sunucuya ulaşılamıyor. İnternet bağlantınızı kontrol edin.';
        }
        
        setError(errorMessage);
        setSuccess('');
      }
    }
  );

  const onSubmit = (data: RegisterFormData) => {
    if (data.password !== data.confirmPassword) {
      setError('Şifreler eşleşmiyor');
      return;
    }
    
    setError('');
    registerMutation.mutate(data);
  };

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Card sx={{ width: '100%' }}>
          <CardContent sx={{ p: 4 }}>
            <Typography component="h1" variant="h4" align="center" gutterBottom>
              Kayıt Ol
            </Typography>

            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            {success && (
              <Alert severity="success" sx={{ mb: 2 }}>
                {success}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
              <TextField
                margin="normal"
                required
                fullWidth
                id="username"
                label="Kullanıcı Adı"
                autoComplete="username"
                autoFocus
                {...register('username', {
                  required: 'Kullanıcı adı gerekli',
                  minLength: {
                    value: 3,
                    message: 'En az 3 karakter olmalı'
                  }
                })}
                error={!!errors.username}
                helperText={errors.username?.message}
              />

              <TextField
                margin="normal"
                required
                fullWidth
                id="email"
                label="E-posta Adresi"
                type="email"
                autoComplete="email"
                {...register('email', {
                  required: 'E-posta gerekli',
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: 'Geçerli bir e-posta adresi girin'
                  }
                })}
                error={!!errors.email}
                helperText={errors.email?.message}
              />

              <TextField
                margin="normal"
                required
                fullWidth
                id="firstName"
                label="Ad"
                autoComplete="given-name"
                {...register('firstName', {
                  required: 'Ad gerekli',
                  minLength: {
                    value: 2,
                    message: 'En az 2 karakter olmalı'
                  }
                })}
                error={!!errors.firstName}
                helperText={errors.firstName?.message}
              />

              <TextField
                margin="normal"
                required
                fullWidth
                id="lastName"
                label="Soyad"
                autoComplete="family-name"
                {...register('lastName', {
                  required: 'Soyad gerekli',
                  minLength: {
                    value: 2,
                    message: 'En az 2 karakter olmalı'
                  }
                })}
                error={!!errors.lastName}
                helperText={errors.lastName?.message}
              />

              <TextField
                margin="normal"
                required
                fullWidth
                label="Şifre"
                type="password"
                id="password"
                autoComplete="new-password"
                {...register('password', {
                  required: 'Şifre gerekli',
                  minLength: {
                    value: 6,
                    message: 'En az 6 karakter olmalı'
                  }
                })}
                error={!!errors.password}
                helperText={errors.password?.message}
              />

              <TextField
                margin="normal"
                required
                fullWidth
                label="Şifre Tekrar"
                type="password"
                id="confirmPassword"
                autoComplete="new-password"
                {...register('confirmPassword', {
                  required: 'Şifre tekrarı gerekli',
                  validate: value => value === password || 'Şifreler eşleşmiyor'
                })}
                error={!!errors.confirmPassword}
                helperText={errors.confirmPassword?.message}
              />

              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={registerMutation.isLoading}
              >
                {registerMutation.isLoading ? (
                  <CircularProgress size={24} />
                ) : (
                  'Kayıt Ol'
                )}
              </Button>

              <Box textAlign="center">
                <Link
                  component="button"
                  variant="body2"
                  onClick={() => navigate('/login')}
                  type="button"
                >
                  Zaten hesabınız var mı? Giriş yapın
                </Link>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default RegisterPage;
