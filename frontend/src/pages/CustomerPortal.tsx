import React, { useState } from 'react';
import {
  Box,
  Paper,
  Tabs,
  Tab,
  Typography,
  AppBar,
  Toolbar,
  Badge,
  IconButton,
} from '@mui/material';
import {
  CalendarToday,
  Star,
  Notifications,
  Person,
  History,
  Add
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import CustomerProfile from '../components/CustomerProfile';
import ReservationManagement from '../components/ReservationManagement';
import NotificationCenter from '../components/NotificationCenter';
import RatingManagement from '../components/RatingManagement';
import RentalManagement from '../components/RentalManagement';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`customer-tabpanel-${index}`}
      aria-labelledby={`customer-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

const CustomerPortal: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [currentTab, setCurrentTab] = useState(0);
  const [unreadCount, setUnreadCount] = useState(0);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const handleNewRental = () => {
    navigate('/customer-rental');
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static" elevation={0} sx={{ bgcolor: 'background.paper', color: 'text.primary' }}>
        <Toolbar>
          <Typography variant="h5" component="h1" sx={{ flexGrow: 1 }}>
            Müşteri Portalı
          </Typography>
          <Typography variant="body1" sx={{ mr: 2 }}>
            Hoş geldin, {user?.firstName} {user?.lastName}
          </Typography>
        </Toolbar>
      </AppBar>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Tabs 
          value={currentTab} 
          onChange={handleTabChange} 
          aria-label="customer portal tabs"
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab 
            icon={<Person />} 
            label="Profil" 
            id="customer-tab-0"
            aria-controls="customer-tabpanel-0"
          />
          <Tab 
            icon={<CalendarToday />} 
            label="Rezervasyonlarım" 
            id="customer-tab-1"
            aria-controls="customer-tabpanel-1"
          />
          <Tab 
            icon={<History />} 
            label="Geçmiş Kiralamalar" 
            id="customer-tab-2"
            aria-controls="customer-tabpanel-2"
          />
          <Tab 
            icon={<Star />} 
            label="Değerlendirmelerim" 
            id="customer-tab-3"
            aria-controls="customer-tabpanel-3"
          />
          <Tab 
            icon={
              <Badge badgeContent={unreadCount} color="error">
                <Notifications />
              </Badge>
            } 
            label="Bildirimler" 
            id="customer-tab-4"
            aria-controls="customer-tabpanel-4"
          />
          <Tab 
            icon={<Add />}
            label="Yeni Kiralama"
            onClick={handleNewRental}
            sx={{ color: 'primary.main', fontWeight: 'bold' }}
          />
        </Tabs>
      </Box>

      <TabPanel value={currentTab} index={0}>
        <CustomerProfile />
      </TabPanel>
      
      <TabPanel value={currentTab} index={1}>
        <ReservationManagement onUnreadCountChange={setUnreadCount} />
      </TabPanel>
      
      <TabPanel value={currentTab} index={2}>
        <RentalManagement />
      </TabPanel>
      
      <TabPanel value={currentTab} index={3}>
        <RatingManagement />
      </TabPanel>
      
      <TabPanel value={currentTab} index={4}>
        <NotificationCenter onUnreadCountChange={setUnreadCount} />
      </TabPanel>
    </Box>
  );
};

export default CustomerPortal;

