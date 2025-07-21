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
  Tabs,
  Tab,
  Divider
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import AdminNavigation from '../../components/AdminNavigation';
import inventoryService from '../../services/inventoryService';
import { useSnackbar } from 'notistack';

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
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

interface Brand {
  id: number;
  name: string;
  description?: string;
}

interface Category {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  parentName?: string;
  children?: Category[];
}

const BrandsAndCategoriesPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [brands, setBrands] = useState<Brand[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [parentCategories, setParentCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [brandDialogOpen, setBrandDialogOpen] = useState(false);
  const [categoryDialogOpen, setCategoryDialogOpen] = useState(false);
  const [currentBrand, setCurrentBrand] = useState<Brand | null>(null);
  const [currentCategory, setCurrentCategory] = useState<Category | null>(null);
  const [brandsPage, setBrandsPage] = useState(0);
  const [categoriesPage, setCategoriesPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const { enqueueSnackbar } = useSnackbar();

  // Form fields
  const [brandFields, setBrandFields] = useState({
    name: '',
    description: ''
  });

  const [categoryFields, setCategoryFields] = useState({
    name: '',
    description: '',
    parentId: ''
  });

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        console.log('Fetching brands and categories data...');
        const brandsPromise = inventoryService.getBrands();
        const categoriesPromise = inventoryService.getCategories();
        const topLevelCategoriesPromise = inventoryService.getTopLevelCategories();

        const [brandsData, categoriesData, topLevelCategories] = await Promise.all([
          brandsPromise,
          categoriesPromise,
          topLevelCategoriesPromise
        ]);
        
        console.log('Brands data received:', brandsData);
        console.log('Categories data received:', categoriesData);
        
        setBrands(brandsData);
        setCategories(categoriesData);
        setParentCategories(categoriesData);
      } catch (error) {
        console.error('Error fetching data:', error);
        enqueueSnackbar('Failed to load brands and categories data', { variant: 'error' });
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [enqueueSnackbar]);

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleBrandsPageChange = (_: unknown, newPage: number) => {
    setBrandsPage(newPage);
  };

  const handleCategoriesPageChange = (_: unknown, newPage: number) => {
    setCategoriesPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newRowsPerPage = parseInt(event.target.value, 10);
    setRowsPerPage(newRowsPerPage);
    setBrandsPage(0);
    setCategoriesPage(0);
  };

  const handleOpenBrandDialog = (brand: Brand | null = null) => {
    if (brand) {
      setBrandFields({
        name: brand.name,
        description: brand.description || ''
      });
      setCurrentBrand(brand);
    } else {
      setBrandFields({
        name: '',
        description: ''
      });
      setCurrentBrand(null);
    }
    setBrandDialogOpen(true);
  };

  const handleCloseBrandDialog = () => {
    setBrandDialogOpen(false);
    setCurrentBrand(null);
  };

  const handleOpenCategoryDialog = (category: Category | null = null) => {
    if (category) {
      setCategoryFields({
        name: category.name,
        description: category.description || '',
        parentId: category.parentId ? category.parentId.toString() : ''
      });
      setCurrentCategory(category);
    } else {
      setCategoryFields({
        name: '',
        description: '',
        parentId: ''
      });
      setCurrentCategory(null);
    }
    setCategoryDialogOpen(true);
  };

  const handleCloseCategoryDialog = () => {
    setCategoryDialogOpen(false);
    setCurrentCategory(null);
  };

  const handleBrandInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setBrandFields({
      ...brandFields,
      [name]: value
    });
  };

  const handleCategoryInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCategoryFields({
      ...categoryFields,
      [name as string]: value
    });
  };
  
  const handleCategorySelectChange = (e: SelectChangeEvent<string>) => {
    const { name, value } = e.target;
    setCategoryFields({
      ...categoryFields,
      [name as string]: value
    });
  };

  const handleSaveBrand = async () => {
    // Validate
    if (!brandFields.name) {
      enqueueSnackbar('Brand name is required', { variant: 'error' });
      return;
    }

    try {
      console.log('Saving brand with data:', brandFields);
      const brandData = { ...brandFields };

      let savedBrand: any;
      if (currentBrand) {
        console.log(`Updating brand with ID: ${currentBrand.id}`);
        savedBrand = await inventoryService.updateBrand(currentBrand.id, brandData);
        console.log('Brand updated response:', savedBrand);
        enqueueSnackbar('Brand updated successfully', { variant: 'success' });
      } else {
        console.log('Creating new brand');
        savedBrand = await inventoryService.createBrand(brandData);
        console.log('Brand creation response:', savedBrand);
        enqueueSnackbar('Brand created successfully', { variant: 'success' });
      }

      // Close dialog
      setBrandDialogOpen(false);
      
      // Update brands list
      if (currentBrand) {
        setBrands(brands.map(brand => (brand.id === currentBrand.id ? savedBrand : brand)));
      } else {
        setBrands([...brands, savedBrand]);
      }
      
      // Refresh data from server
      console.log('Refreshing brands data after save');
      const refreshedBrands = await inventoryService.getBrands();
      console.log('Refreshed brands data:', refreshedBrands);
      setBrands(refreshedBrands);
      
    } catch (error) {
      console.error('Error saving brand:', error);
      enqueueSnackbar('Error saving brand', { variant: 'error' });
    }
  };

  const handleSaveCategory = async () => {
    try {
      const categoryData = {
        ...categoryFields,
        parentId: categoryFields.parentId ? parseInt(categoryFields.parentId) : undefined
      };

      let savedCategory;
      if (currentCategory) {
        savedCategory = await inventoryService.updateCategory(currentCategory.id, categoryData);
        enqueueSnackbar('Category updated successfully', { variant: 'success' });
      } else {
        savedCategory = await inventoryService.createCategory(categoryData);
        enqueueSnackbar('Category created successfully', { variant: 'success' });
      }

      // Fetch updated categories to reflect parent-child relationships correctly
      const updatedCategories = await inventoryService.getCategories();
      setCategories(updatedCategories);
      setParentCategories(updatedCategories);

      handleCloseCategoryDialog();
    } catch (error) {
      console.error('Error saving category:', error);
      enqueueSnackbar('Failed to save category', { variant: 'error' });
    }
  };

  const handleDeleteBrand = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this brand?')) {
      try {
        await inventoryService.deleteBrand(id);
        setBrands(brands.filter(brand => brand.id !== id));
        enqueueSnackbar('Brand deleted successfully', { variant: 'success' });
      } catch (error) {
        console.error('Error deleting brand:', error);
        enqueueSnackbar('Failed to delete brand', { variant: 'error' });
      }
    }
  };

  const handleDeleteCategory = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        await inventoryService.deleteCategory(id);
        setCategories(categories.filter(category => category.id !== id));
        enqueueSnackbar('Category deleted successfully', { variant: 'success' });
      } catch (error) {
        console.error('Error deleting category:', error);
        enqueueSnackbar('Failed to delete category', { variant: 'error' });
      }
    }
  };

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <AdminNavigation
        title="Brands & Categories"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Brands & Categories', path: '/admin/inventory/brands' }
        ]}
      />

      <Paper sx={{ p: 2 }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="brands and categories tabs">
          <Tab label="Brands" />
          <Tab label="Categories" />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Brands</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleOpenBrandDialog()}
            >
              Add Brand
            </Button>
          </Box>

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table sx={{ minWidth: 650 }}>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {brands
                      .slice(brandsPage * rowsPerPage, brandsPage * rowsPerPage + rowsPerPage)
                      .map((brand) => (
                        <TableRow key={brand.id}>
                          <TableCell>{brand.name}</TableCell>
                          <TableCell>{brand.description || '-'}</TableCell>
                          <TableCell>
                            <IconButton color="primary" onClick={() => handleOpenBrandDialog(brand)}>
                              <EditIcon />
                            </IconButton>
                            <IconButton color="error" onClick={() => handleDeleteBrand(brand.id)}>
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
                count={brands.length}
                page={brandsPage}
                onPageChange={handleBrandsPageChange}
                rowsPerPage={rowsPerPage}
                onRowsPerPageChange={handleChangeRowsPerPage}
              />
            </>
          )}
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Categories</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleOpenCategoryDialog()}
            >
              Add Category
            </Button>
          </Box>

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <>
              <TableContainer>
                <Table sx={{ minWidth: 650 }}>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Parent Category</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {categories
                      .slice(categoriesPage * rowsPerPage, categoriesPage * rowsPerPage + rowsPerPage)
                      .map((category) => (
                        <TableRow key={category.id}>
                          <TableCell>{category.name}</TableCell>
                          <TableCell>{category.parentName || '-'}</TableCell>
                          <TableCell>{category.description || '-'}</TableCell>
                          <TableCell>
                            <IconButton color="primary" onClick={() => handleOpenCategoryDialog(category)}>
                              <EditIcon />
                            </IconButton>
                            <IconButton color="error" onClick={() => handleDeleteCategory(category.id)}>
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
                count={categories.length}
                page={categoriesPage}
                onPageChange={handleCategoriesPageChange}
                rowsPerPage={rowsPerPage}
                onRowsPerPageChange={handleChangeRowsPerPage}
              />
            </>
          )}
        </TabPanel>
      </Paper>

      {/* Brand Dialog */}
      <Dialog open={brandDialogOpen} onClose={handleCloseBrandDialog}>
        <DialogTitle>{currentBrand ? 'Edit Brand' : 'Add New Brand'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                name="name"
                label="Brand Name"
                value={brandFields.name}
                onChange={handleBrandInputChange}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="description"
                label="Description"
                value={brandFields.description}
                onChange={handleBrandInputChange}
                fullWidth
                multiline
                rows={3}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseBrandDialog}>Cancel</Button>
          <Button onClick={handleSaveBrand} variant="contained" color="primary">
            Save
          </Button>
        </DialogActions>
      </Dialog>

      {/* Category Dialog */}
      <Dialog open={categoryDialogOpen} onClose={handleCloseCategoryDialog}>
        <DialogTitle>{currentCategory ? 'Edit Category' : 'Add New Category'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                name="name"
                label="Category Name"
                value={categoryFields.name}
                onChange={handleCategoryInputChange}
                fullWidth
                required
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel id="parent-category-label">Parent Category</InputLabel>
                <Select
                  labelId="parent-category-label"
                  name="parentId"
                  value={categoryFields.parentId}
                  onChange={handleCategorySelectChange}
                  label="Parent Category"
                >
                  <MenuItem value="">
                    <em>None (Top Level)</em>
                  </MenuItem>
                  {parentCategories
                    .filter(cat => !currentCategory || cat.id !== currentCategory.id)
                    .map((category) => (
                      <MenuItem key={category.id} value={category.id.toString()}>
                        {category.name}
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                name="description"
                label="Description"
                value={categoryFields.description}
                onChange={handleCategoryInputChange}
                fullWidth
                multiline
                rows={3}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseCategoryDialog}>Cancel</Button>
          <Button onClick={handleSaveCategory} variant="contained" color="primary">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default BrandsAndCategoriesPage;
