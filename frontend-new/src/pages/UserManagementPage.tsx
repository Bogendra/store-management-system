import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import AdminNavigation from '../components/AdminNavigation';
import {
  Container,
  Typography,
  Box,
  Paper,
  Button,
  TextField,
  Grid,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  CircularProgress,
  Alert
} from '@mui/material';
import { 
  Add as AddIcon,
  PersonAdd as PersonAddIcon,
} from '@mui/icons-material';
import { useForm, SubmitHandler } from 'react-hook-form';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

// API base URL
const API_BASE_URL = 'http://localhost:8081';

interface UserData {
  id: number;
  username: string;
  email: string;
  tenantName: string;
  roleNames: string[];
  enabled: boolean;
}

interface CreateUserFormData {
  username: string;
  password: string;
  email: string;
  tenantId: number;
  role: string;
}

interface Tenant {
  id: number;
  name: string;
}

interface Role {
  id: number;
  name: string;
}

const UserManagementPage: React.FC = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [users, setUsers] = useState<UserData[]>([]);
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  
  // For development/testing until the endpoints are ready
  const mockTenants: Tenant[] = [
    { id: 1, name: 'Sample Brand' },
    { id: 2, name: 'Store One' },
    { id: 3, name: 'Store Two' },
  ];
  
  const mockRoles: Role[] = [
    { id: 1, name: 'SUPER_ADMIN' },
    { id: 2, name: 'STORE_MANAGER' },
    { id: 3, name: 'STORE_STAFF' },
  ];

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateUserFormData>();

  // Fetch users, roles and tenants
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      
      try {
        // Fetch users
        const usersResponse = await axios.get(`${API_BASE_URL}/api/users/all`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setUsers(usersResponse.data);
        
        try {
          // Fetch roles
          const rolesResponse = await axios.get(`${API_BASE_URL}/api/admin/roles`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          if (Array.isArray(rolesResponse.data)) {
            setRoles(rolesResponse.data);
          } else {
            console.warn('Roles API did not return an array, using mock data');
            setRoles(mockRoles);
          }
        } catch (err) {
          console.warn('Error fetching roles, using mock data');
          setRoles(mockRoles);
        }
        
        try {
          // Fetch tenants
          const tenantsResponse = await axios.get(`${API_BASE_URL}/api/admin/tenants`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          if (Array.isArray(tenantsResponse.data)) {
            setTenants(tenantsResponse.data);
          } else {
            console.warn('Tenants API did not return an array, using mock data');
            setTenants(mockTenants);
          }
        } catch (err) {
          console.warn('Error fetching tenants, using mock data');
          setTenants(mockTenants);
        }
      } catch (err: any) {
        console.error('Error fetching data:', err);
        setError(err.response?.data?.message || 'Error loading data. Please try again later.');
        
        // If unauthorized, redirect to login
        if (err.response?.status === 401 || err.response?.status === 403) {
          navigate('/login');
        }
      } finally {
        setLoading(false);
      }
    };
    
    fetchData();
  }, [token, navigate]);

  const handleCreateUser: SubmitHandler<CreateUserFormData> = async (data) => {
    setCreateError(null);
    
    try {
      await axios.post(`${API_BASE_URL}/api/admin/users`, data, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      // Close dialog and refresh user list
      setCreateDialogOpen(false);
      reset();
      
      // Refresh users
      const response = await axios.get(`${API_BASE_URL}/api/users/all`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setUsers(response.data);
    } catch (err: any) {
      console.error('Error creating user:', err);
      setCreateError(err.response?.data?.message || 'Failed to create user. Please try again.');
    }
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
        <AdminNavigation title="User Management" currentPath="/admin/users" />
        
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            onClick={() => setCreateDialogOpen(true)}
          >
            Create User
          </Button>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <TableContainer component={Paper} sx={{ mt: 3 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Username</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Tenant</TableCell>
                <TableCell>Roles</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    <Typography variant="body1" sx={{ py: 2 }}>
                      No users found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                users.map(user => (
                  <TableRow key={user.id}>
                    <TableCell>{user.username}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.tenantName}</TableCell>
                    <TableCell>
                      {user.roleNames.map(role => (
                        <Chip 
                          key={role} 
                          label={role} 
                          size="small" 
                          variant="outlined"
                          color="primary"
                          sx={{ mr: 0.5, mb: 0.5 }} 
                        />
                      ))}
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={user.enabled ? "Active" : "Inactive"} 
                        color={user.enabled ? "success" : "error"} 
                        size="small" 
                      />
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Create User Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <PersonAddIcon sx={{ mr: 1 }} />
            Create New User
          </Box>
        </DialogTitle>
        <DialogContent>
          {createError && <Alert severity="error" sx={{ mb: 2 }}>{createError}</Alert>}
          
          <Box component="form" noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              fullWidth
              id="username"
              label="Username"
              autoFocus
              {...register('username', { 
                required: 'Username is required',
                minLength: { value: 4, message: 'Username must be at least 4 characters' }
              })}
              error={!!errors.username}
              helperText={errors.username?.message}
            />
            
            <TextField
              margin="normal"
              fullWidth
              id="email"
              label="Email Address"
              {...register('email', { 
                required: 'Email is required',
                pattern: { 
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i, 
                  message: 'Invalid email address' 
                }
              })}
              error={!!errors.email}
              helperText={errors.email?.message}
            />
            
            <TextField
              margin="normal"
              fullWidth
              label="Password"
              type="password"
              id="password"
              {...register('password', { 
                required: 'Password is required',
                minLength: { value: 8, message: 'Password must be at least 8 characters' }
              })}
              error={!!errors.password}
              helperText={errors.password?.message}
            />
            
            <TextField
              margin="normal"
              fullWidth
              select
              id="tenantId"
              label="Organization"
              {...register('tenantId', { required: 'Please select an organization' })}
              error={!!errors.tenantId}
              helperText={errors.tenantId?.message}
            >
              {tenants.map(tenant => (
                <MenuItem key={tenant.id} value={tenant.id}>
                  {tenant.name}
                </MenuItem>
              ))}
            </TextField>
            
            <TextField
              margin="normal"
              fullWidth
              select
              id="role"
              label="Role"
              {...register('role', { required: 'Please select a role' })}
              error={!!errors.role}
              helperText={errors.role?.message}
            >
              {roles.map(role => (
                <MenuItem key={role.name} value={role.name}>
                  {role.name.replace('_', ' ')}
                </MenuItem>
              ))}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button 
            onClick={handleSubmit(handleCreateUser)} 
            variant="contained"
          >
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default UserManagementPage;
