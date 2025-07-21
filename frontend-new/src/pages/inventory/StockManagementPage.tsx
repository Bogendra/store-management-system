import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Grid,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  SelectChangeEvent,
  MenuItem,
  InputAdornment
} from '@mui/material';
import {
  Refresh as RefreshIcon,
  Edit as EditIcon,
  Warning as WarningIcon
} from '@mui/icons-material';
import AdminNavigation from '../../components/AdminNavigation';
import inventoryService from '../../services/inventoryService';
import { useSnackbar } from 'notistack';

interface Location {
  id: number;
  name: string;
  type: string;
}

interface ItemVariant {
  id: number;
  sku: string;
  variantName: string;
  itemName: string;
}

interface InventoryLevel {
  id: number;
  locationId: number;
  locationName: string;
  itemVariantId: number;
  itemVariantSku: string;
  itemVariantName: string;
  itemName: string;
  quantityOnHand: number;
  quantityReserved: number;
  quantityAvailable: number;
  reorderPoint: number;
  reorderQuantity: number;
  lowStock: boolean;
}

const StockManagementPage: React.FC = () => {
  const [inventoryLevels, setInventoryLevels] = useState<InventoryLevel[]>([]);
  const [locations, setLocations] = useState<Location[]>([]);
  const [itemVariants, setItemVariants] = useState<ItemVariant[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedLocation, setSelectedLocation] = useState<string>('');
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [adjustDialogOpen, setAdjustDialogOpen] = useState(false);
  const [reorderDialogOpen, setReorderDialogOpen] = useState(false);
  const [currentLevel, setCurrentLevel] = useState<InventoryLevel | null>(null);
  const { enqueueSnackbar } = useSnackbar();

  // Form fields
  const [adjustmentFields, setAdjustmentFields] = useState({
    quantity: '',
    transactionType: 'ADJUSTMENT',
    notes: ''
  });

  const [reorderFields, setReorderFields] = useState({
    reorderPoint: '',
    reorderQuantity: ''
  });

  useEffect(() => {
    const fetchLocations = async () => {
      try {
        const locationsData = await inventoryService.getLocations();
        setLocations(locationsData);
        
        // Select the first location by default if available
        if (locationsData.length > 0) {
          setSelectedLocation(locationsData[0].id.toString());
          await fetchInventoryForLocation(locationsData[0].id);
        }
      } catch (error) {
        console.error('Error fetching locations:', error);
        enqueueSnackbar('Failed to load locations', { variant: 'error' });
      }
    };

    fetchLocations();
  }, [enqueueSnackbar]);

  const fetchInventoryForLocation = async (locationId: number) => {
    setLoading(true);
    try {
      const levels = await inventoryService.getInventoryLevels(locationId);
      setInventoryLevels(levels);
    } catch (error) {
      console.error('Error fetching inventory levels:', error);
      enqueueSnackbar('Failed to load inventory data', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleLocationChange = async (event: SelectChangeEvent<string>) => {
    const locationId = event.target.value as string;
    setSelectedLocation(locationId);
    await fetchInventoryForLocation(Number(locationId));
  };

  const handleRefresh = async () => {
    if (selectedLocation) {
      await fetchInventoryForLocation(Number(selectedLocation));
    }
  };

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleOpenAdjustDialog = (level: InventoryLevel) => {
    setCurrentLevel(level);
    setAdjustmentFields({
      quantity: '',
      transactionType: 'ADJUSTMENT',
      notes: ''
    });
    setAdjustDialogOpen(true);
  };

  const handleCloseAdjustDialog = () => {
    setAdjustDialogOpen(false);
    setCurrentLevel(null);
  };

  const handleOpenReorderDialog = (level: InventoryLevel) => {
    setCurrentLevel(level);
    setReorderFields({
      reorderPoint: level.reorderPoint?.toString() || '',
      reorderQuantity: level.reorderQuantity?.toString() || ''
    });
    setReorderDialogOpen(true);
  };

  const handleCloseReorderDialog = () => {
    setReorderDialogOpen(false);
    setCurrentLevel(null);
  };

  const handleAdjustmentInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setAdjustmentFields({
      ...adjustmentFields,
      [name as string]: value,
    });
  };
  
  // Separate handler for select fields
  const handleSelectChange = (e: SelectChangeEvent<string>) => {
    const { name, value } = e.target;
    setAdjustmentFields({
      ...adjustmentFields,
      [name as string]: value,
    });
  };

  const handleReorderInputChange = (e: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = e.target;
    setReorderFields({
      ...reorderFields,
      [name as string]: value,
    });
  };

  const handleSaveAdjustment = async () => {
    try {
      if (!currentLevel) return;

      const { quantity, transactionType, notes } = adjustmentFields;
      const parsedQuantity = parseFloat(quantity);

      if (isNaN(parsedQuantity)) {
        enqueueSnackbar('Please enter a valid quantity', { variant: 'error' });
        return;
      }

      await inventoryService.updateInventoryQuantity(
        currentLevel.locationId,
        currentLevel.itemVariantId,
        parsedQuantity,
        transactionType,
        notes
      );

      enqueueSnackbar('Inventory adjusted successfully', { variant: 'success' });
      handleCloseAdjustDialog();
      handleRefresh();
    } catch (error) {
      console.error('Error adjusting inventory:', error);
      enqueueSnackbar('Failed to adjust inventory', { variant: 'error' });
    }
  };

  const handleSaveReorderPoint = async () => {
    try {
      if (!currentLevel) return;

      const { reorderPoint, reorderQuantity } = reorderFields;
      const parsedReorderPoint = parseFloat(reorderPoint);
      const parsedReorderQuantity = parseFloat(reorderQuantity);

      if (isNaN(parsedReorderPoint) || isNaN(parsedReorderQuantity)) {
        enqueueSnackbar('Please enter valid values', { variant: 'error' });
        return;
      }

      await inventoryService.setReorderPoint(
        currentLevel.locationId,
        currentLevel.itemVariantId,
        parsedReorderPoint,
        parsedReorderQuantity
      );

      enqueueSnackbar('Reorder settings updated successfully', { variant: 'success' });
      handleCloseReorderDialog();
      handleRefresh();
    } catch (error) {
      console.error('Error setting reorder point:', error);
      enqueueSnackbar('Failed to update reorder settings', { variant: 'error' });
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <AdminNavigation
        title="Stock Management"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Stock Management', path: '/admin/inventory/stock' }
        ]}
      />

      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">Inventory Levels</Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <FormControl variant="outlined" sx={{ minWidth: 200 }}>
              <InputLabel id="location-select-label">Location</InputLabel>
              <Select
                labelId="location-select-label"
                value={selectedLocation}
                onChange={handleLocationChange}
                label="Location"
              >
                {locations.map((location) => (
                  <MenuItem key={location.id} value={location.id.toString()}>
                    {location.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={handleRefresh}
            >
              Refresh
            </Button>
          </Box>
        </Box>

        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
            <CircularProgress />
          </Box>
        ) : (
          <Box>
            <TableContainer>
              <Table sx={{ minWidth: 650 }}>
                <TableHead>
                  <TableRow>
                    <TableCell>Item</TableCell>
                    <TableCell>SKU</TableCell>
                    <TableCell>Variant</TableCell>
                    <TableCell>On Hand</TableCell>
                    <TableCell>Reserved</TableCell>
                    <TableCell>Available</TableCell>
                    <TableCell>Reorder Point</TableCell>
                    <TableCell>Reorder Qty</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {inventoryLevels
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((level) => (
                      <TableRow 
                        key={level.id}
                        sx={level.lowStock ? { backgroundColor: 'rgba(255, 0, 0, 0.05)' } : {}}
                      >
                        <TableCell>{level.itemName}</TableCell>
                        <TableCell>{level.itemVariantSku}</TableCell>
                        <TableCell>{level.itemVariantName || 'Default'}</TableCell>
                        <TableCell>{level.quantityOnHand}</TableCell>
                        <TableCell>{level.quantityReserved}</TableCell>
                        <TableCell>
                          {level.quantityAvailable}
                          {level.lowStock && (
                            <WarningIcon 
                              fontSize="small" 
                              color="warning" 
                              sx={{ ml: 1, verticalAlign: 'middle' }}
                            />
                          )}
                        </TableCell>
                        <TableCell>{level.reorderPoint || '-'}</TableCell>
                        <TableCell>{level.reorderQuantity || '-'}</TableCell>
                        <TableCell>
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={() => handleOpenAdjustDialog(level)}
                            sx={{ mr: 1 }}
                          >
                            Adjust
                          </Button>
                          <Button
                            size="small"
                            variant="outlined"
                            color="secondary"
                            onClick={() => handleOpenReorderDialog(level)}
                          >
                            Reorder
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={inventoryLevels.length}
              page={page}
              onPageChange={handleChangePage}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={handleChangeRowsPerPage}
            />
          </Box>
        )}
      </Paper>

      {/* Adjustment Dialog */}
      <Dialog open={adjustDialogOpen} onClose={handleCloseAdjustDialog}>
        <DialogTitle>Adjust Inventory</DialogTitle>
        <DialogContent>
          {currentLevel && (
            <Box sx={{ my: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Item: {currentLevel.itemName}
              </Typography>
              <Typography variant="subtitle2" gutterBottom>
                SKU: {currentLevel.itemVariantSku}
              </Typography>
              <Typography variant="subtitle2" gutterBottom>
                Current Quantity: {currentLevel.quantityOnHand}
              </Typography>
            </Box>
          )}
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                name="quantity"
                label="Adjustment Quantity"
                value={adjustmentFields.quantity}
                onChange={handleAdjustmentInputChange}
                fullWidth
                required
                type="number"
                helperText="Use positive values to add, negative to subtract"
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel id="transaction-type-label">Transaction Type</InputLabel>
                <Select
                  labelId="transaction-type-label"
                  name="transactionType"
                  value={adjustmentFields.transactionType}
                  onChange={handleSelectChange}
                  label="Transaction Type"
                >
                  <MenuItem value="ADJUSTMENT">Adjustment</MenuItem>
                  <MenuItem value="PURCHASE">Purchase</MenuItem>
                  <MenuItem value="SALE">Sale</MenuItem>
                  <MenuItem value="RETURN">Return</MenuItem>
                  <MenuItem value="COUNT">Inventory Count</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="notes"
                label="Notes"
                value={adjustmentFields.notes}
                onChange={handleAdjustmentInputChange}
                fullWidth
                multiline
                rows={2}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseAdjustDialog}>Cancel</Button>
          <Button onClick={handleSaveAdjustment} variant="contained" color="primary">
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Reorder Dialog */}
      <Dialog open={reorderDialogOpen} onClose={handleCloseReorderDialog}>
        <DialogTitle>Set Reorder Points</DialogTitle>
        <DialogContent>
          {currentLevel && (
            <Box sx={{ my: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Item: {currentLevel.itemName}
              </Typography>
              <Typography variant="subtitle2" gutterBottom>
                SKU: {currentLevel.itemVariantSku}
              </Typography>
            </Box>
          )}
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                name="reorderPoint"
                label="Reorder Point"
                value={reorderFields.reorderPoint}
                onChange={handleReorderInputChange}
                fullWidth
                required
                type="number"
                helperText="Stock level that triggers reordering"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="reorderQuantity"
                label="Reorder Quantity"
                value={reorderFields.reorderQuantity}
                onChange={handleReorderInputChange}
                fullWidth
                required
                type="number"
                helperText="Quantity to reorder when stock is low"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseReorderDialog}>Cancel</Button>
          <Button onClick={handleSaveReorderPoint} variant="contained" color="primary">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default StockManagementPage;
