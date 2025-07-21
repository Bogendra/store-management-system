import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import { AuthProvider, useAuth } from './context/AuthContext';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import UserManagementPage from './pages/UserManagementPage';
import RoleManagementPage from './pages/RoleManagementPage';
import ItemsManagementPage from './pages/inventory/ItemsManagementPage';
import StockManagementPage from './pages/inventory/StockManagementPage';
import BrandsAndCategoriesPage from './pages/inventory/BrandsAndCategoriesPage';

// Create theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
});

// Protected route component
interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: string[];
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRoles }) => {
  const { isAuthenticated, userInfo } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    // Redirect to login page if not authenticated
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Check for required roles if specified
  if (requiredRoles && requiredRoles.length > 0 && userInfo) {
    const hasRequiredRole = userInfo.roleNames.some(role => requiredRoles.includes(role));
    if (!hasRequiredRole) {
      // Redirect to dashboard if user doesn't have the required roles
      return <Navigate to="/dashboard" replace />;
    }
  }

  return <>{children}</>;
};

// App component
const AppContent: React.FC = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      
      {/* Protected routes */}
      <Route 
        path="/dashboard" 
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/admin/users" 
        element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN', 'ADMIN']}>
            <UserManagementPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/admin/roles" 
        element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN', 'ADMIN']}>
            <RoleManagementPage />
          </ProtectedRoute>
        } 
      />
      
      {/* Inventory Management Routes */}
      <Route 
        path="/admin/inventory/items" 
        element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN', 'ADMIN']}>
            <ItemsManagementPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/admin/inventory/stock" 
        element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN', 'ADMIN']}>
            <StockManagementPage />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/admin/inventory/brands" 
        element={
          <ProtectedRoute requiredRoles={['SUPER_ADMIN', 'ADMIN']}>
            <BrandsAndCategoriesPage />
          </ProtectedRoute>
        } 
      />
      
      {/* Default redirect */}
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
};

// Root App component with providers
const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <AppContent />
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App;
