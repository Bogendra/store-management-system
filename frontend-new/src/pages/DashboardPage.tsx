import React, { useEffect, useState } from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Button,
  Avatar,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  CircularProgress,
  Grid,
  Card,
  CardContent
} from '@mui/material';
import { 
  Person as PersonIcon,
  Business as BusinessIcon,
  Security as SecurityIcon,
  Logout as LogoutIcon,
  PeopleAlt as PeopleIcon,
  AdminPanelSettings as AdminIcon,
  Inventory as InventoryIcon,
  ShoppingCart as OrdersIcon,
  Inventory2 as Inventory2Icon,
  Warehouse as WarehouseIcon,
  BrandingWatermark as BrandingWatermarkIcon
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const DashboardPage: React.FC = () => {
  const { logout, userInfo } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(!userInfo);

  useEffect(() => {
    setLoading(!userInfo);
  }, [userInfo]);

  // Check if user has admin privileges
  const isAdmin = userInfo?.roleNames?.some(role => 
    ['SUPER_ADMIN', 'ADMIN'].includes(role)
  ) || false;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <Container sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Avatar sx={{ mr: 2, bgcolor: 'primary.main' }}>
              {userInfo?.username.charAt(0).toUpperCase()}
            </Avatar>
            <Box>
              <Typography variant="h5" component="h1">
                Welcome, {userInfo?.username}!
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {userInfo?.email}
              </Typography>
            </Box>
          </Box>
          <Button 
            variant="outlined" 
            startIcon={<LogoutIcon />}
            onClick={handleLogout}
          >
            Logout
          </Button>
        </Box>

        <Divider sx={{ my: 2 }} />

        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              User Information
            </Typography>
            <List dense>
              <ListItem>
                <ListItemIcon>
                  <PersonIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText 
                  primary="Username" 
                  secondary={userInfo?.username} 
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <BusinessIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText 
                  primary="Organization" 
                  secondary={userInfo?.tenantName} 
                />
              </ListItem>
              <ListItem>
                <ListItemIcon>
                  <SecurityIcon fontSize="small" />
                </ListItemIcon>
                <ListItemText 
                  primary="Roles" 
                  secondary={userInfo?.roleNames.join(', ')} 
                />
              </ListItem>
            </List>
          </Paper>
          
          <Paper sx={{ p: 2, flex: 1 }}>
            <Typography variant="h6" gutterBottom>
              Quick Links
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {/* Admin Section - Only show for users with admin roles */}
              {isAdmin && (
                <Grid item xs={12} md={6}>
                  <Card>
                    <CardContent>
                      <Typography variant="h5" gutterBottom>
                        Admin Controls
                      </Typography>
                      <List>
                        <ListItem
                          button
                          component={RouterLink}
                          to="/admin/users"
                          sx={{ borderRadius: 1 }}
                        >
                          <ListItemIcon>
                            <PeopleIcon />
                          </ListItemIcon>
                          <ListItemText primary="User Management" />
                        </ListItem>
                        
                        <ListItem
                          button
                          component={RouterLink}
                          to="/admin/roles"
                          sx={{ borderRadius: 1 }}
                        >
                          <ListItemIcon>
                            <SecurityIcon />
                          </ListItemIcon>
                          <ListItemText primary="Role Management" />
                        </ListItem>

                        <Divider sx={{ my: 1 }} />
                        <Typography variant="subtitle1" sx={{ mt: 1, mb: 1, fontWeight: 'medium' }}>
                          Inventory Management
                        </Typography>
                        
                        <ListItem
                          button
                          component={RouterLink}
                          to="/admin/inventory/items"
                          sx={{ borderRadius: 1 }}
                        >
                          <ListItemIcon>
                            <Inventory2Icon />
                          </ListItemIcon>
                          <ListItemText primary="Inventory Items" />
                        </ListItem>

                        <ListItem
                          button
                          component={RouterLink}
                          to="/admin/inventory/stock"
                          sx={{ borderRadius: 1 }}
                        >
                          <ListItemIcon>
                            <WarehouseIcon />
                          </ListItemIcon>
                          <ListItemText primary="Stock Management" />
                        </ListItem>

                        <ListItem
                          button
                          component={RouterLink}
                          to="/admin/inventory/brands"
                          sx={{ borderRadius: 1 }}
                        >
                          <ListItemIcon>
                            <BrandingWatermarkIcon />
                          </ListItemIcon>
                          <ListItemText primary="Brands & Categories" />
                        </ListItem>
                      </List>
                    </CardContent>
                  </Card>
                </Grid>
              )}
              
              <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 1, mb: 1 }}>
                Store Operations
              </Typography>
              <Button 
                variant="contained" 
                fullWidth
                startIcon={<InventoryIcon />}
              >
                View Inventory
              </Button>
              <Button 
                variant="contained" 
                fullWidth
                startIcon={<OrdersIcon />}
              >
                Manage Orders
              </Button>
            </Box>
          </Paper>
        </Box>
      </Paper>

      <Typography variant="body2" color="text.secondary" align="center" sx={{ pt: 4 }}>
        Store Management System — {new Date().getFullYear()}
      </Typography>
    </Container>
  );
};

export default DashboardPage;
