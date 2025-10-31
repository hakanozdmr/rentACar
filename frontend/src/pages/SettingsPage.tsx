import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Card,
  CardContent,
  CardHeader,
  Switch,
  FormControlLabel,
  Button,
  Grid,
  Divider,
  Alert,
  TextField,
} from '@mui/material';
import {
  Settings as SettingsIcon,
  Security as SecurityIcon,
  Notifications as NotificationsIcon,
  Palette as PaletteIcon,
  Save as SaveIcon,
} from '@mui/icons-material';

interface AppSettings {
  darkMode: boolean;
  notifications: boolean;
  autoRefresh: boolean;
  refreshInterval: number;
}

const SettingsPage: React.FC = () => {
  const [darkMode, setDarkMode] = useState(false);
  const [notifications, setNotifications] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [refreshInterval, setRefreshInterval] = useState(30);
  const [saveMessage, setSaveMessage] = useState<string>('');

  // localStorage'dan ayarları yükle
  useEffect(() => {
    const savedSettings = localStorage.getItem('appSettings');
    if (savedSettings) {
      try {
        const settings: AppSettings = JSON.parse(savedSettings);
        setDarkMode(settings.darkMode);
        setNotifications(settings.notifications);
        setAutoRefresh(settings.autoRefresh);
        setRefreshInterval(settings.refreshInterval);
      } catch (error) {
        console.error('Error loading settings:', error);
      }
    }
  }, []);

  // Tema değişikliğini uygula
  useEffect(() => {
    const root = document.documentElement;
    if (darkMode) {
      root.classList.add('dark-theme');
    } else {
      root.classList.remove('dark-theme');
    }
  }, [darkMode]);

  const handleSaveSettings = () => {
    const settings: AppSettings = {
      darkMode,
      notifications,
      autoRefresh,
      refreshInterval,
    };
    
    try {
      localStorage.setItem('appSettings', JSON.stringify(settings));
      
      // Bildirim ayarını uygula
      if (notifications && 'Notification' in window) {
        if (Notification.permission === 'default') {
          Notification.requestPermission();
        }
      }
      
      setSaveMessage('Ayarlar başarıyla kaydedildi!');
      setTimeout(() => setSaveMessage(''), 3000);
    } catch (error) {
      setSaveMessage('Ayarlar kaydedilirken hata oluştu!');
      setTimeout(() => setSaveMessage(''), 3000);
    }
  };

  // Otomatik yenileme interval'ını ayarla (demo için)
  useEffect(() => {
    let intervalId: NodeJS.Timeout;
    
    if (autoRefresh && refreshInterval > 0) {
      intervalId = setInterval(() => {
        // Burada veri yenileme işlemi yapılabilir
        console.log('Auto refresh triggered');
      }, refreshInterval * 1000);
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [autoRefresh, refreshInterval]);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        <SettingsIcon sx={{ mr: 2, verticalAlign: 'middle' }} />
        Sistem Ayarları
      </Typography>
      <Typography variant="subtitle1" color="textSecondary" sx={{ mb: 3 }}>
        Uygulama tercihlerinizi yönetin
      </Typography>

      {saveMessage && (
        <Alert 
          severity="success" 
          sx={{ mb: 3 }}
          onClose={() => setSaveMessage('')}
        >
          {saveMessage}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Tema Ayarları */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader
              avatar={<PaletteIcon />}
              title="Görünüm Ayarları"
              subheader="Tema ve görsel tercihleri"
            />
            <CardContent>
              <FormControlLabel
                control={
                  <Switch
                    checked={darkMode}
                    onChange={(e) => setDarkMode(e.target.checked)}
                  />
                }
                label="Karanlık Tema"
              />
              <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
                Uygulamanın görünümünü karanlık tema ile değiştirin
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Bildirim Ayarları */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader
              avatar={<NotificationsIcon />}
              title="Bildirim Ayarları"
              subheader="Uygulama bildirimleri"
            />
            <CardContent>
              <FormControlLabel
                control={
                  <Switch
                    checked={notifications}
                    onChange={(e) => setNotifications(e.target.checked)}
                  />
                }
                label="Push Bildirimleri"
              />
              <Typography variant="body2" color="textSecondary" sx={{ mt: 1, mb: 2 }}>
                Sistem bildirimlerini aktif edin
              </Typography>
              
              <Button
                variant="outlined"
                size="small"
                disabled={!notifications}
                onClick={() => {
                  if ('Notification' in window && Notification.permission === 'granted') {
                    new Notification('Test Bildirimi', {
                      body: 'Bildirim sistemi çalışıyor!',
                      icon: '/favicon.ico'
                    });
                  } else if (notifications) {
                    Notification.requestPermission().then(permission => {
                      if (permission === 'granted') {
                        new Notification('Test Bildirimi', {
                          body: 'Bildirim sistemi çalışıyor!',
                          icon: '/favicon.ico'
                        });
                      }
                    });
                  }
                }}
              >
                Bildirim Test Et
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* Performans Ayarları */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader
              avatar={<SecurityIcon />}
              title="Performans Ayarları"
              subheader="Veri yenileme ve performans"
            />
            <CardContent>
              <FormControlLabel
                control={
                  <Switch
                    checked={autoRefresh}
                    onChange={(e) => setAutoRefresh(e.target.checked)}
                  />
                }
                label="Otomatik Yenileme"
              />
              <Typography variant="body2" color="textSecondary" sx={{ mt: 1, mb: 2 }}>
                Verilerin otomatik olarak yenilenmesini sağlar
              </Typography>
              
              <TextField
                fullWidth
                size="small"
                label="Yenileme Aralığı (saniye)"
                type="number"
                value={refreshInterval}
                onChange={(e) => setRefreshInterval(Number(e.target.value))}
                disabled={!autoRefresh}
                helperText="Otomatik yenileme aktifken veriler bu aralıkta güncellenir"
              />
            </CardContent>
          </Card>
        </Grid>

        {/* Sistem Bilgileri */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardHeader
              title="Sistem Bilgileri"
              subheader="Uygulama versiyonu ve durumu"
            />
            <CardContent>
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary">
                  Versiyon: 1.0.0
                </Typography>
              </Box>
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary">
                  Son Güncelleme: {new Date().toLocaleDateString('tr-TR')}
                </Typography>
              </Box>
              <Box mb={2}>
                <Typography variant="body2" color="textSecondary">
                  Backend Durumu: Çevrimiçi
                </Typography>
              </Box>
              <Divider sx={{ my: 2 }} />
              <Alert severity="info" sx={{ mt: 2 }}>
                Bu uygulama React, TypeScript ve Material-UI kullanılarak geliştirilmiştir.
              </Alert>
            </CardContent>
          </Card>
        </Grid>

        {/* Kaydet Butonu */}
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Box display="flex" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="h6">
                  Değişiklikleri Kaydet
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  Yaptığınız değişiklikleri kalıcı olarak kaydedin
                </Typography>
              </Box>
              <Button
                variant="contained"
                startIcon={<SaveIcon />}
                onClick={handleSaveSettings}
                size="large"
              >
                Ayarları Kaydet
              </Button>
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SettingsPage;
