import React, { useState } from 'react';
import {
  AppBar,
  Box,
  CssBaseline,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  useTheme,
  useMediaQuery,
  Button,
  Menu,
  MenuItem,
  Avatar,
  Badge,
  Popover,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  DirectionsCar as CarIcon,
  People as CustomerIcon,
  BookOnline as RentalIcon,
  ConfirmationNumber as ReservationIcon,
  Speed as BrandIcon,
  Category as ModelIcon,
  AccountCircle,
  Logout,
  Analytics as AnalyticsIcon,
  Settings as SettingsIcon,
  Person as PersonIcon,
  Notifications as NotificationsIcon,
  Payment as PaymentIcon,
  Receipt as InvoiceIcon,
  Assessment as ReportsIcon,
  History as AuditIcon,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { customerPortalApi, Notification } from '../services/api';

const drawerWidth = 240;

interface LayoutProps {
  children: React.ReactNode;
}

const adminMenuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
  { text: 'Markalar', icon: <BrandIcon />, path: '/brands' },
  { text: 'Modeller', icon: <ModelIcon />, path: '/models' },
  { text: 'Araçlar', icon: <CarIcon />, path: '/cars' },
  { text: 'Müşteriler', icon: <CustomerIcon />, path: '/customers' },
  { text: 'Kiralama', icon: <RentalIcon />, path: '/rentals' },
  { text: 'Rezervasyonlar', icon: <ReservationIcon />, path: '/reservations' },
  { text: 'Ödemeler', icon: <PaymentIcon />, path: '/payments' },
  { text: 'Faturalar', icon: <InvoiceIcon />, path: '/invoices' },
  { text: 'Mali Raporlar', icon: <ReportsIcon />, path: '/financial-reports' },
  { text: 'Audit Loglar', icon: <AuditIcon />, path: '/audit-logs' },
  { text: 'Analytics', icon: <AnalyticsIcon />, path: '/analytics' },
  { text: 'Ayarlar', icon: <SettingsIcon />, path: '/settings' },
];

const customerMenuItems = [
  { text: 'Ana Sayfa', icon: <DashboardIcon />, path: '/' },
  { text: 'Müşteri Portalı', icon: <PersonIcon />, path: '/customer-portal' },
];

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [notificationAnchorEl, setNotificationAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { user, logout, isAuthenticated, hasRole } = useAuth();
  const queryClient = useQueryClient();

  // Notifications query - only for authenticated users with USER role
  const { data: unreadNotificationCount = 0 } = useQuery(
    ['unreadNotificationCount'],
    () => customerPortalApi.getUnreadNotificationCount().then(res => res.data),
    {
      enabled: isAuthenticated && hasRole('USER') && !hasRole('ADMIN'),
      refetchInterval: 10000, // Her 10 saniyede bir güncelle
      refetchIntervalInBackground: true
    }
  );

  const { data: notifications = [] } = useQuery(
    ['notifications'],
    () => customerPortalApi.getNotifications().then(res => res.data),
    {
      enabled: isAuthenticated && hasRole('USER') && !hasRole('ADMIN') && Boolean(notificationAnchorEl),
      refetchInterval: 5000 // Popover açıkken daha sık güncelle
    }
  );

  // Mark notification as read mutation
  const markAsReadMutation = useMutation(
    (id: number) => customerPortalApi.markNotificationAsRead(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(['notifications']);
        queryClient.invalidateQueries(['unreadNotificationCount']);
      }
    }
  );

  // Menu items based on user role
  const menuItems = hasRole('ADMIN') ? adminMenuItems : customerMenuItems;

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleNavigation = (path: string) => {
    navigate(path);
    if (isMobile) {
      setMobileOpen(false);
    }
  };

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationClick = (event: React.MouseEvent<HTMLElement>) => {
    setNotificationAnchorEl(event.currentTarget);
  };

  const handleNotificationClose = () => {
    setNotificationAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleClose();
  };

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap component="div" sx={{ color: 'primary.main', fontWeight: 'bold' }}>
          RentACar
        </Typography>
      </Toolbar>
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => handleNavigation(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar
        position="fixed"
        sx={{
          width: { md: `calc(100% - ${drawerWidth}px)` },
          ml: { md: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { md: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Araç Kiralama Yönetim Sistemi
          </Typography>
          
          {isAuthenticated && user && (
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              <Typography variant="body2" sx={{ mr: 2 }}>
                Merhaba, {user.firstName} {user.lastName}
              </Typography>
              
              {/* Notification Icon - only for USER role */}
              {hasRole('USER') && !hasRole('ADMIN') && (
                <IconButton
                  size="large"
                  aria-label="notifications"
                  onClick={handleNotificationClick}
                  color="inherit"
                  sx={{ mr: 1 }}
                >
                  <Badge badgeContent={unreadNotificationCount} color="error">
                    <NotificationsIcon />
                  </Badge>
                </IconButton>
              )}
              
              <IconButton
                size="large"
                aria-label="account of current user"
                aria-controls="menu-appbar"
                aria-haspopup="true"
                onClick={handleMenu}
                color="inherit"
              >
                <Avatar sx={{ width: 32, height: 32 }}>
                  {user.firstName.charAt(0)}{user.lastName.charAt(0)}
                </Avatar>
              </IconButton>
              <Menu
                id="menu-appbar"
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <MenuItem onClick={handleClose}>
                  <AccountCircle sx={{ mr: 1 }} />
                  Profil
                </MenuItem>
                <MenuItem onClick={handleLogout}>
                  <Logout sx={{ mr: 1 }} />
                  Çıkış
                </MenuItem>
              </Menu>
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {/* Notification Popover */}
      {isAuthenticated && hasRole('USER') && !hasRole('ADMIN') && (
        <Popover
          open={Boolean(notificationAnchorEl)}
          anchorEl={notificationAnchorEl}
          onClose={handleNotificationClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'right',
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
          }}
        >
          <Box sx={{ width: 350, maxHeight: 400, overflow: 'auto', p: 1 }}>
            <Typography variant="h6" sx={{ p: 2, borderBottom: '1px solid #eee' }}>
              Bildirimler
            </Typography>
            {notifications.length === 0 ? (
              <Typography variant="body2" color="text.secondary" sx={{ p: 2, textAlign: 'center' }}>
                Henüz bildiriminiz bulunmuyor.
              </Typography>
            ) : (
              notifications.slice(0, 5).map((notification: Notification) => (
                <Box
                  key={notification.id}
                  sx={{
                    p: 2,
                    borderBottom: '1px solid #f0f0f0',
                    bgcolor: notification.readAt ? 'transparent' : 'action.hover',
                    cursor: 'pointer'
                  }}
                  onClick={() => {
                    if (!notification.readAt && notification.id) {
                      markAsReadMutation.mutate(notification.id);
                    }
                  }}
                >
                  <Typography variant="subtitle2" gutterBottom>
                    {notification.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" noWrap>
                    {notification.message}
                  </Typography>
                </Box>
              ))
            )}
            {notifications.length > 5 && (
              <Box sx={{ p: 2, textAlign: 'center', borderTop: '1px solid #eee' }}>
                <Button 
                  size="small" 
                  onClick={() => {
                    handleNotificationClose();
                    navigate('/customer-portal');
                  }}
                >
                  Tümünü Gör
                </Button>
              </Box>
            )}
          </Box>
        </Popover>
      )}
      
      <Box
        component="nav"
        sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { md: `calc(100% - ${drawerWidth}px)` },
          mt: 8,
        }}
      >
        {children}
      </Box>
    </Box>
  );
};

export default Layout;

