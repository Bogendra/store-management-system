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
  MenuItem,
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
  SelectChangeEvent
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import AdminNavigation from '../../components/AdminNavigation';
import inventoryService from '../../services/inventoryService';
import { useSnackbar } from 'notistack';

interface Category {
  id: number;
  name: string;
}

interface Brand {
  id: number;
  name: string;
}

interface ItemVariant {
  id: number;
  sku: string;
  variantName: string;
}

interface Item {
  id: number;
  itemCode: string;
  name: string;
  description: string;
  upcCode?: string;
  categoryId?: number;
  categoryName?: string;
  brandId?: number;
  brandName?: string;
  variants?: ItemVariant[];
}

const ItemsManagementPage: React.FC = () => {
  const [items, setItems] = useState<Item[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [currentItem, setCurrentItem] = useState<Item | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [variantDialogOpen, setVariantDialogOpen] = useState(false);
  const [currentVariant, setCurrentVariant] = useState<ItemVariant | null>(null);
  const { enqueueSnackbar } = useSnackbar();

  // Form fields
  const [formFields, setFormFields] = useState({
    itemCode: '',
    name: '',
    description: '',
    upcCode: '',
    categoryId: '',
    brandId: '',
  });

  // Variant form fields
  const [variantFields, setVariantFields] = useState({
    sku: '',
    variantName: ''
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [itemsData, categoriesData, brandsData] = await Promise.all([
          inventoryService.getItems(),
          inventoryService.getCategories(),
          inventoryService.getBrands()
        ]);
        
        setItems(itemsData);
        setCategories(categoriesData);
        setBrands(brandsData);
      } catch (error) {
        console.error('Error fetching data:', error);
        enqueueSnackbar('Failed to load inventory data', { variant: 'error' });
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [enqueueSnackbar]);

  const handleChangePage = (_: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const handleOpenDialog = (item: Item | null = null) => {
    if (item) {
      setFormFields({
        itemCode: item.itemCode || '',
        name: item.name || '',
        description: item.description || '',
        upcCode: item.upcCode || '',
        categoryId: item.categoryId?.toString() || '',
        brandId: item.brandId?.toString() || '',
      });
      setCurrentItem(item);
    } else {
      setFormFields({
        itemCode: '',
        name: '',
        description: '',
        upcCode: '',
        categoryId: '',
        brandId: '',
      });
      setCurrentItem(null);
    }
    setDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setDialogOpen(false);
    setCurrentItem(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormFields({
      ...formFields,
      [name as string]: value,
    });
  };
  
  // Separate handler for select fields
  const handleSelectChange = (e: SelectChangeEvent<string>) => {
    const { name, value } = e.target;
    setFormFields({
      ...formFields,
      [name as string]: value,
    });
  };

  const handleVariantChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setVariantFields({
      ...variantFields,
      [name]: value,
    });
  };

  const handleOpenVariantDialog = (itemId: number, variant: ItemVariant | null = null) => {
    if (variant) {
      setVariantFields({
        sku: variant.sku,
        variantName: variant.variantName
      });
      setCurrentVariant(variant);
    } else {
      setVariantFields({
        sku: '',
        variantName: ''
      });
      setCurrentVariant(null);
    }
    setVariantDialogOpen(true);
  };

  const handleCloseVariantDialog = () => {
    setVariantDialogOpen(false);
    setCurrentVariant(null);
  };

  const handleSaveItem = async () => {
    try {
      const itemData = {
        ...formFields,
        categoryId: formFields.categoryId ? parseInt(formFields.categoryId) : undefined,
        brandId: formFields.brandId ? parseInt(formFields.brandId) : undefined,
      };

      let savedItem: any;
      if (currentItem) {
        savedItem = await inventoryService.updateItem(currentItem.id, itemData);
        enqueueSnackbar('Item updated successfully', { variant: 'success' });
      } else {
        savedItem = await inventoryService.createItem(itemData);
        enqueueSnackbar('Item created successfully', { variant: 'success' });
      }

      // Update the items list
      if (currentItem) {
        setItems(items.map(item => (item.id === currentItem.id ? savedItem : item)));
      } else {
        setItems([...items, savedItem]);
      }

      handleCloseDialog();
    } catch (error) {
      console.error('Error saving item:', error);
      enqueueSnackbar('Failed to save item', { variant: 'error' });
    }
  };

  const handleSaveVariant = async () => {
    try {
      if (!currentItem) return;

      if (currentVariant) {
        await inventoryService.updateItemVariant(currentVariant.id, variantFields);
        enqueueSnackbar('Variant updated successfully', { variant: 'success' });
      } else {
        await inventoryService.createItemVariant(currentItem.id, variantFields);
        enqueueSnackbar('Variant created successfully', { variant: 'success' });
      }

      // Refresh the item to get updated variants
      const updatedItem = await inventoryService.getItemById(currentItem.id);
      setItems(items.map(item => (item.id === currentItem.id ? updatedItem : item)));

      handleCloseVariantDialog();
    } catch (error) {
      console.error('Error saving variant:', error);
      enqueueSnackbar('Failed to save variant', { variant: 'error' });
    }
  };

  const handleDeleteItem = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this item?')) {
      try {
        await inventoryService.deleteItem(id);
        setItems(items.filter(item => item.id !== id));
        enqueueSnackbar('Item deleted successfully', { variant: 'success' });
      } catch (error) {
        console.error('Error deleting item:', error);
        enqueueSnackbar('Failed to delete item', { variant: 'error' });
      }
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <AdminNavigation
        title="Inventory Items Management"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Inventory Items', path: '/admin/inventory/items' }
        ]}
      />

      <Paper sx={{ p: 2, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">Inventory Items</Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Add Item
          </Button>
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
                    <TableCell>Item Code</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell>Brand</TableCell>
                    <TableCell>Variants</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {items
                    .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                    .map((item) => (
                      <TableRow key={item.id}>
                        <TableCell>{item.itemCode}</TableCell>
                        <TableCell>{item.name}</TableCell>
                        <TableCell>{item.description}</TableCell>
                        <TableCell>{item.categoryName || '-'}</TableCell>
                        <TableCell>{item.brandName || '-'}</TableCell>
                        <TableCell>
                          {item.variants?.length || 0} variants
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={() => {
                              setCurrentItem(item);
                              handleOpenVariantDialog(item.id);
                            }}
                            sx={{ ml: 1 }}
                          >
                            Add
                          </Button>
                        </TableCell>
                        <TableCell>
                          <IconButton color="primary" onClick={() => handleOpenDialog(item)}>
                            <EditIcon />
                          </IconButton>
                          <IconButton color="error" onClick={() => handleDeleteItem(item.id)}>
                            <DeleteIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={items.length}
              page={page}
              onPageChange={handleChangePage}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={handleChangeRowsPerPage}
            />
          </Box>
        )}
      </Paper>

      {/* Item Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="md" fullWidth>
        <DialogTitle>{currentItem ? 'Edit Item' : 'Add New Item'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <TextField
                name="itemCode"
                label="Item Code"
                value={formFields.itemCode}
                onChange={handleInputChange}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                name="upcCode"
                label="UPC Code"
                value={formFields.upcCode}
                onChange={handleInputChange}
                fullWidth
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="name"
                label="Item Name"
                value={formFields.name}
                onChange={handleInputChange}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="description"
                label="Description"
                value={formFields.description}
                onChange={handleInputChange}
                fullWidth
                multiline
                rows={3}
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel id="category-select-label">Category</InputLabel>
                <Select
                  labelId="category-select-label"
                  name="categoryId"
                  value={formFields.categoryId}
                  onChange={handleSelectChange}
                  label="Category"
                >
                  <MenuItem value="">
                    <em>None</em>
                  </MenuItem>
                  {categories.map((category) => (
                    <MenuItem key={category.id} value={category.id.toString()}>
                      {category.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel id="brand-select-label">Brand</InputLabel>
                <Select
                  labelId="brand-select-label"
                  name="brandId"
                  value={formFields.brandId}
                  onChange={handleSelectChange}
                  label="Brand"
                >
                  <MenuItem value="">
                    <em>None</em>
                  </MenuItem>
                  {brands.map((brand) => (
                    <MenuItem key={brand.id} value={brand.id.toString()}>
                      {brand.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSaveItem} variant="contained" color="primary">
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Variant Dialog */}
      <Dialog open={variantDialogOpen} onClose={handleCloseVariantDialog}>
        <DialogTitle>{currentVariant ? 'Edit Variant' : 'Add New Variant'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                name="sku"
                label="SKU"
                value={variantFields.sku}
                onChange={handleVariantChange}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="variantName"
                label="Variant Name"
                value={variantFields.variantName}
                onChange={handleVariantChange}
                fullWidth
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseVariantDialog}>Cancel</Button>
          <Button onClick={handleSaveVariant} variant="contained" color="primary">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default ItemsManagementPage;
