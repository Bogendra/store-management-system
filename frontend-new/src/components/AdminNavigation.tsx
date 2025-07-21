import React from 'react';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { Box, Button, Breadcrumbs, Link, Typography } from '@mui/material';
import { Home as HomeIcon, ArrowBack as BackIcon } from '@mui/icons-material';

interface AdminNavigationProps {
  title: string;
  currentPath?: string;
  breadcrumbs?: Array<{
    label: string;
    path: string;
  }>;
}

const AdminNavigation: React.FC<AdminNavigationProps> = ({ title, currentPath, breadcrumbs }) => {
  const navigate = useNavigate();
  
  const handleBack = () => {
    navigate(-1); // Go back to the previous page
  };
  
  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h5" component="h1">
          {title}
        </Typography>
        <Box>
          <Button 
            variant="outlined" 
            startIcon={<BackIcon />}
            onClick={handleBack}
            sx={{ mr: 1 }}
          >
            Back
          </Button>
          <Button 
            variant="outlined" 
            component={RouterLink}
            to="/dashboard"
            startIcon={<HomeIcon />}
          >
            Dashboard
          </Button>
        </Box>
      </Box>
      
      <Breadcrumbs aria-label="breadcrumb">
        <Link
          component={RouterLink}
          to="/dashboard"
          underline="hover"
          color="inherit"
          sx={{ display: 'flex', alignItems: 'center' }}
        >
          <HomeIcon sx={{ mr: 0.5 }} fontSize="inherit" />
          Dashboard
        </Link>
        
        {breadcrumbs ? (
          // Render custom breadcrumbs if provided
          breadcrumbs.map((crumb, index) => (
            index === breadcrumbs.length - 1 ? (
              <Typography key={crumb.path} color="text.primary">{crumb.label}</Typography>
            ) : (
              <Link
                key={crumb.path}
                component={RouterLink}
                to={crumb.path}
                underline="hover"
                color="inherit"
              >
                {crumb.label}
              </Link>
            )
          ))
        ) : (
          // Default behavior
          <Typography color="text.primary">{title}</Typography>
        )}
      </Breadcrumbs>
    </Box>
  );
};

export default AdminNavigation;
