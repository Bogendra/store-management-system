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
  Alert,
  List,
  ListItem,
  ListItemText,
  Checkbox,
  FormControlLabel
} from '@mui/material';
import { 
  Add as AddIcon,
  Security as SecurityIcon
} from '@mui/icons-material';
import { useForm, SubmitHandler } from 'react-hook-form';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

// API base URL
const API_BASE_URL = 'http://localhost:8081';

interface Role {
  id: number;
  name: string;
  description: string;
  privileges: Privilege[];
}

interface Privilege {
  id: number;
  name: string;
  description: string;
}

interface CreateRoleForm {
  name: string;
  description: string;
  privilegeIds: number[];
}

const RoleManagementPage: React.FC = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [roles, setRoles] = useState<Role[]>([]);
  const [privileges, setPrivileges] = useState<Privilege[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [selectedPrivileges, setSelectedPrivileges] = useState<number[]>([]);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<CreateRoleForm>();

  // Mock data for development
  const mockRoles: Role[] = [
    { 
      id: 1, 
      name: 'SUPER_ADMIN', 
      description: 'Has full access to all system features',
      privileges: [
        { id: 1, name: 'USER_CREATE', description: 'Can create users' },
        { id: 2, name: 'USER_VIEW_ALL', description: 'Can view all users' },
        { id: 3, name: 'ROLE_VIEW_ALL', description: 'Can view all roles' }
      ]
    },
    { 
      id: 2, 
      name: 'STORE_MANAGER', 
      description: 'Can manage store operations and staff',
      privileges: [
        { id: 1, name: 'USER_CREATE', description: 'Can create users' },
        { id: 2, name: 'USER_VIEW_ALL', description: 'Can view all users' }
      ]
    },
    { 
      id: 3, 
      name: 'STORE_STAFF', 
      description: 'Regular staff member with limited access',
      privileges: []
    }
  ];

  const mockPrivileges: Privilege[] = [
    { id: 1, name: 'USER_CREATE', description: 'Can create users' },
    { id: 2, name: 'USER_VIEW_ALL', description: 'Can view all users' },
    { id: 3, name: 'ROLE_VIEW_ALL', description: 'Can view all roles' },
    { id: 4, name: 'ROLE_CREATE', description: 'Can create roles' },
    { id: 5, name: 'TENANT_VIEW_ALL', description: 'Can view all tenants' },
    { id: 6, name: 'TENANT_CREATE', description: 'Can create tenants' }
  ];

  // Fetch roles and privileges
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      
      try {
        // Try to fetch roles from API
        try {
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
        
        // Try to fetch privileges from API
        try {
          const privilegesResponse = await axios.get(`${API_BASE_URL}/api/admin/privileges`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          if (Array.isArray(privilegesResponse.data)) {
            setPrivileges(privilegesResponse.data);
          } else {
            console.warn('Privileges API did not return an array, using mock data');
            setPrivileges(mockPrivileges);
          }
        } catch (err) {
          console.warn('Error fetching privileges, using mock data');
          setPrivileges(mockPrivileges);
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

  const togglePrivilege = (privilegeId: number) => {
    setSelectedPrivileges(prevSelected => 
      prevSelected.includes(privilegeId)
        ? prevSelected.filter(id => id !== privilegeId)
        : [...prevSelected, privilegeId]
    );
  };

  const handleCreateRole: SubmitHandler<CreateRoleForm> = async (data) => {
    setCreateError(null);
    
    try {
      // Add selected privileges to the form data
      const roleData = {
        ...data,
        privilegeIds: selectedPrivileges
      };
      
      // Make API call to create role
      await axios.post(`${API_BASE_URL}/api/admin/roles`, roleData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      // Close dialog and refresh roles
      setCreateDialogOpen(false);
      reset();
      setSelectedPrivileges([]);
      
      // Try to refresh roles from API
      try {
        const response = await axios.get(`${API_BASE_URL}/api/admin/roles`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        if (Array.isArray(response.data)) {
          setRoles(response.data);
        }
      } catch (err) {
        // If API fails, update mock data (for development)
        const newRole: Role = {
          id: mockRoles.length + 1,
          name: data.name,
          description: data.description,
          privileges: mockPrivileges.filter(priv => selectedPrivileges.includes(priv.id))
        };
        setRoles([...mockRoles, newRole]);
      }
    } catch (err: any) {
      console.error('Error creating role:', err);
      setCreateError(err.response?.data?.message || 'Failed to create role. Please try again.');
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
        <AdminNavigation title="Role Management" currentPath="/admin/roles" />
        
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            onClick={() => setCreateDialogOpen(true)}
          >
            Create Role
          </Button>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

        <TableContainer component={Paper} sx={{ mt: 3 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Privileges</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {roles.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} align="center">
                    <Typography variant="body1" sx={{ py: 2 }}>
                      No roles found
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                roles.map(role => (
                  <TableRow key={role.id}>
                    <TableCell>{role.name}</TableCell>
                    <TableCell>{role.description}</TableCell>
                    <TableCell>
                      {role.privileges?.map(privilege => (
                        <Chip 
                          key={privilege.id} 
                          label={privilege.name} 
                          size="small" 
                          variant="outlined"
                          color="primary"
                          sx={{ mr: 0.5, mb: 0.5 }} 
                        />
                      ))}
                      {!role.privileges?.length && (
                        <Typography variant="caption" color="text.secondary">
                          No privileges assigned
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Button size="small" variant="outlined">
                        Edit
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* Create Role Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <SecurityIcon sx={{ mr: 1 }} />
            Create New Role
          </Box>
        </DialogTitle>
        <DialogContent>
          {createError && <Alert severity="error" sx={{ mb: 2 }}>{createError}</Alert>}
          
          <Box component="form" noValidate sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              fullWidth
              id="name"
              label="Role Name"
              autoFocus
              {...register('name', { 
                required: 'Role name is required',
                pattern: {
                  value: /^[A-Z0-9_]+$/,
                  message: 'Role name must be uppercase with underscores only (e.g., STORE_MANAGER)'
                }
              })}
              error={!!errors.name}
              helperText={errors.name?.message || 'Use uppercase letters with underscores (e.g., STORE_MANAGER)'}
            />
            
            <TextField
              margin="normal"
              fullWidth
              id="description"
              label="Description"
              multiline
              rows={2}
              {...register('description', { required: 'Description is required' })}
              error={!!errors.description}
              helperText={errors.description?.message}
            />
            
            <Typography variant="subtitle1" sx={{ mt: 2, mb: 1 }}>
              Assign Privileges
            </Typography>
            
            <Grid container spacing={2}>
              {privileges.map(privilege => (
                <Grid item xs={12} sm={6} key={privilege.id}>
                  <FormControlLabel
                    control={
                      <Checkbox 
                        checked={selectedPrivileges.includes(privilege.id)}
                        onChange={() => togglePrivilege(privilege.id)}
                      />
                    }
                    label={
                      <Box>
                        <Typography variant="body2">{privilege.name}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {privilege.description}
                        </Typography>
                      </Box>
                    }
                  />
                </Grid>
              ))}
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button 
            onClick={handleSubmit(handleCreateRole)} 
            variant="contained"
          >
            Create Role
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default RoleManagementPage;
