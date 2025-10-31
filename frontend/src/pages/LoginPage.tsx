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
import { authApi, LoginRequest } from '../services/api';
import { useAuth } from '../contexts/AuthContext';

interface LoginFormData {
  username: string;
  password: string;
}

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState<string>('');
  
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginFormData>();

  const loginMutation = useMutation(
    (data: LoginRequest) => authApi.login(data).then(res => res.data),
    {
      onSuccess: (response) => {
        const userData = {
          id: response.id,
          username: response.username,
          email: response.email,
          firstName: response.firstName,
          lastName: response.lastName,
          roles: response.roles
        };
        login(userData, response.token);
        navigate('/');
      },
      onError: (error: any) => {
        setError(error.message || 'Giriş yapılırken hata oluştu');
      }
    }
  );

  const onSubmit = (data: LoginFormData) => {
    setError('');
    loginMutation.mutate(data);
  };

  return (
    <Container component="main" maxWidth="xs">
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
              RentACar
            </Typography>
            <Typography component="h2" variant="h6" align="center" color="text.secondary" gutterBottom>
              Sisteme Giriş
            </Typography>
            
            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 1 }}>
              <TextField
                margin="normal"
                required
                fullWidth
                id="username"
                label="Kullanıcı Adı"
                autoComplete="username"
                autoFocus
                {...register('username', { required: 'Kullanıcı adı gerekli' })}
                error={!!errors.username}
                helperText={errors.username?.message}
              />
              
              <TextField
                margin="normal"
                required
                fullWidth
                label="Şifre"
                type="password"
                id="password"
                autoComplete="current-password"
                {...register('password', { required: 'Şifre gerekli' })}
                error={!!errors.password}
                helperText={errors.password?.message}
              />
              
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loginMutation.isLoading}
              >
                {loginMutation.isLoading ? (
                  <CircularProgress size={24} />
                ) : (
                  'Giriş Yap'
                )}
              </Button>
            </Box>

            <Box sx={{ mt: 2, textAlign: 'center' }}>
              <Link
                component="button"
                variant="body2"
                onClick={() => navigate('/register')}
                type="button"
                sx={{ mb: 2, display: 'block' }}
              >
                Hesabınız yok mu? Kayıt olun
              </Link>
              <Typography variant="body2" color="text.secondary">
                Demo hesap: <strong>admin</strong> / <strong>password</strong>
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                Veya test için: <strong>admin</strong> / <strong>123456</strong>
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  );
};

export default LoginPage;
