import React from 'react';
import {
  Paper,
  Typography,
  Box,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  IconButton,
  Button,
  Chip,
  Alert,
  CircularProgress,
} from '@mui/material';
import {
  Notifications,
  CheckCircle,
  Email,
  Sms,
  PushPin,
  MoreVert
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { customerPortalApi, Notification } from '../services/api';
import dayjs from 'dayjs';

interface NotificationCenterProps {
  onUnreadCountChange: (count: number) => void;
}

const NotificationCenter: React.FC<NotificationCenterProps> = ({ onUnreadCountChange }) => {
  const queryClient = useQueryClient();

  const { data: notifications, isLoading } = useQuery(
    ['notifications'],
    () => customerPortalApi.getNotifications().then(res => res.data),
    {
      refetchInterval: 10000, // Her 10 saniyede bir güncelle
      refetchIntervalInBackground: true
    }
  );

  const { data: unreadCount } = useQuery(
    ['unreadNotificationCount'],
    () => customerPortalApi.getUnreadNotificationCount().then(res => res.data),
    {
      refetchInterval: 10000, // Her 10 saniyede bir güncelle
      refetchIntervalInBackground: true
    }
  );

  const markAsReadMutation = useMutation(
    (id: number) => customerPortalApi.markNotificationAsRead(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['notifications']);
        queryClient.invalidateQueries(['unreadNotificationCount']);
      }
    }
  );

  const markAllAsReadMutation = useMutation(
    () => customerPortalApi.markAllNotificationsAsRead(),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['notifications']);
        queryClient.invalidateQueries(['unreadNotificationCount']);
        if (onUnreadCountChange) {
          onUnreadCountChange(0);
        }
      }
    }
  );

  React.useEffect(() => {
    if (unreadCount !== undefined && onUnreadCountChange) {
      onUnreadCountChange(unreadCount);
    }
  }, [unreadCount, onUnreadCountChange]);

  const getNotificationIcon = (channel: string) => {
    switch (channel) {
      case 'EMAIL': return <Email />;
      case 'SMS': return <Sms />;
      case 'PUSH': return <PushPin />;
      default: return <Notifications />;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'RESERVATION_CONFIRMED': return 'success';
      case 'RESERVATION_REMINDER': return 'warning';
      case 'RESERVATION_CANCELLED': return 'error';
      case 'CAR_DELIVERY': return 'info';
      case 'CAR_PICKUP': return 'warning';
      default: return 'default';
    }
  };

  const getTypeText = (type: string) => {
    switch (type) {
      case 'RESERVATION_CONFIRMED': return 'Rezervasyon Onayı';
      case 'RESERVATION_REMINDER': return 'Rezervasyon Hatırlatması';
      case 'RESERVATION_CANCELLED': return 'Rezervasyon İptali';
      case 'CAR_DELIVERY': return 'Araç Teslim';
      case 'CAR_PICKUP': return 'Araç Teslim Alma';
      case 'PAYMENT_DUE': return 'Ödeme Hatırlatması';
      case 'RENTAL_EXPIRED': return 'Kiralama Süresi';
      default: return type;
    }
  };

  const handleMarkAsRead = (id: number) => {
    markAsReadMutation.mutate(id);
  };

  const handleMarkAllAsRead = () => {
    markAllAsReadMutation.mutate();
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h6">
          Bildirimler 
          {unreadCount !== undefined && unreadCount > 0 && (
            <Chip 
              label={unreadCount} 
              color="error" 
              size="small" 
              sx={{ ml: 2 }}
            />
          )}
        </Typography>
        
        {unreadCount !== undefined && unreadCount > 0 && (
          <Button
            variant="outlined"
            size="small"
            onClick={handleMarkAllAsRead}
            disabled={markAllAsReadMutation.isLoading}
          >
            {markAllAsReadMutation.isLoading ? <CircularProgress size={16} /> : 'Tümünü Okundu İşaretle'}
          </Button>
        )}
      </Box>

      {isLoading ? (
        <CircularProgress />
      ) : notifications && notifications.length > 0 ? (
        <List>
          {notifications.map((notification: Notification) => (
            <ListItem
              key={notification.id}
              sx={{
                bgcolor: notification.readAt ? 'transparent' : 'action.hover',
                borderLeft: notification.readAt ? 'none' : '4px solid',
                borderLeftColor: 'primary.main',
                borderRadius: 1,
                mb: 1
              }}
            >
              <ListItemIcon>
                {getNotificationIcon(notification.channel)}
              </ListItemIcon>
              
              <ListItemText
                primary={
                  <Box display="flex" alignItems="center" gap={1}>
                    <Typography variant="subtitle1">
                      {notification.title}
                    </Typography>
                    <Chip
                      label={getTypeText(notification.type)}
                      color={getTypeColor(notification.type) as any}
                      size="small"
                    />
                  </Box>
                }
                secondary={
                  <Box>
                    <Typography variant="body2" color="text.secondary">
                      {notification.message}
                    </Typography>
                    <Typography variant="caption" color="text.disabled">
                      {notification.createdAt ? 
                        (dayjs(notification.createdAt).isValid() ? 
                          dayjs(notification.createdAt).format('DD.MM.YYYY HH:mm') : 
                          'Tarih bilgisi yok') : 
                        'Tarih bilgisi yok'}
                    </Typography>
                  </Box>
                }
              />
              
              {!notification.readAt && (
                <IconButton
                  size="small"
                  onClick={() => handleMarkAsRead(notification.id!)}
                  disabled={markAsReadMutation.isLoading}
                >
                  <CheckCircle color="primary" />
                </IconButton>
              )}
            </ListItem>
          ))}
        </List>
      ) : (
        <Alert severity="info">
          Henüz bildiriminiz bulunmuyor.
        </Alert>
      )}
    </Paper>
  );
};

export default NotificationCenter;
