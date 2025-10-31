import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';
import { QueryClient, QueryClientProvider } from 'react-query';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import BrandsPage from './pages/BrandsPage';
import ModelsPage from './pages/ModelsPage';
import CarsPage from './pages/CarsPage';
import CustomersPage from './pages/CustomersPage';
import RentalsPage from './pages/RentalsPage';
import ReservationsPage from './pages/ReservationsPage';
import PaymentsPage from './pages/PaymentsPage';
import InvoicesPage from './pages/InvoicesPage';
import FinancialReportsPage from './pages/FinancialReportsPage';
import AuditLogsPage from './pages/AuditLogsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import AnalyticsPage from './pages/AnalyticsPage';
import SettingsPage from './pages/SettingsPage';
import CustomerPortal from './pages/CustomerPortal';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { SnackbarProvider } from './contexts/SnackbarContext';

const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 500,
    },
  },
});

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

// Protected Route Component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
};

// App Routes Component
const AppRoutes: React.FC = () => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Box sx={{ display: 'flex' }}>
      <Layout>
        <Routes>
          <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/brands" element={<ProtectedRoute><BrandsPage /></ProtectedRoute>} />
          <Route path="/models" element={<ProtectedRoute><ModelsPage /></ProtectedRoute>} />
          <Route path="/cars" element={<ProtectedRoute><CarsPage /></ProtectedRoute>} />
          <Route path="/customers" element={<ProtectedRoute><CustomersPage /></ProtectedRoute>} />
          <Route path="/rentals" element={<ProtectedRoute><RentalsPage /></ProtectedRoute>} />
          <Route path="/reservations" element={<ProtectedRoute><ReservationsPage /></ProtectedRoute>} />
          <Route path="/payments" element={<ProtectedRoute><PaymentsPage /></ProtectedRoute>} />
          <Route path="/invoices" element={<ProtectedRoute><InvoicesPage /></ProtectedRoute>} />
          <Route path="/financial-reports" element={<ProtectedRoute><FinancialReportsPage /></ProtectedRoute>} />
          <Route path="/audit-logs" element={<ProtectedRoute><AuditLogsPage /></ProtectedRoute>} />
          <Route path="/analytics" element={<ProtectedRoute><AnalyticsPage /></ProtectedRoute>} />
          <Route path="/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
          <Route path="/customer-portal" element={<ProtectedRoute><CustomerPortal /></ProtectedRoute>} />
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Layout>
    </Box>
  );
};

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <Router>
          <AuthProvider>
            <SnackbarProvider>
              <AppRoutes />
            </SnackbarProvider>
          </AuthProvider>
        </Router>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;

