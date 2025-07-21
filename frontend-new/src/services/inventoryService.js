import axios from 'axios';
import { API_BASE_URL, INVENTORY_SERVICE_PORT } from '../config';

// Correctly construct the URL with port and appropriate API path
const INVENTORY_API_BASE_URL = `${API_BASE_URL}:${INVENTORY_SERVICE_PORT}/api`;

// Create axios instance with auth token interceptor
const inventoryApi = axios.create({
  baseURL: INVENTORY_API_BASE_URL,
});

// Add auth token to requests
inventoryApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Brand APIs
const getBrands = async () => {
  try {
    const response = await inventoryApi.get('/brands');
    return response.data;
  } catch (error) {
    console.error('Error fetching brands:', error);
    throw error;
  }
};

const createBrand = async (brandData) => {
  try {
    const response = await inventoryApi.post('/brands', brandData);
    return response.data;
  } catch (error) {
    console.error('Error creating brand:', error);
    throw error;
  }
};

const updateBrand = async (id, brandData) => {
  try {
    const response = await inventoryApi.put(`/brands/${id}`, brandData);
    return response.data;
  } catch (error) {
    console.error(`Error updating brand ${id}:`, error);
    throw error;
  }
};

const deleteBrand = async (id) => {
  try {
    await inventoryApi.delete(`/brands/${id}`);
  } catch (error) {
    console.error(`Error deleting brand ${id}:`, error);
    throw error;
  }
};

// Category APIs
const getCategories = async () => {
  try {
    const response = await inventoryApi.get('/categories');
    return response.data;
  } catch (error) {
    console.error('Error fetching categories:', error);
    throw error;
  }
};

const getTopLevelCategories = async () => {
  try {
    const response = await inventoryApi.get('/categories/top-level');
    return response.data;
  } catch (error) {
    console.error('Error fetching top-level categories:', error);
    throw error;
  }
};

const getSubcategories = async (parentId) => {
  try {
    const response = await inventoryApi.get(`/categories/${parentId}/subcategories`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching subcategories for ${parentId}:`, error);
    throw error;
  }
};

const createCategory = async (categoryData) => {
  try {
    const response = await inventoryApi.post('/categories', categoryData);
    return response.data;
  } catch (error) {
    console.error('Error creating category:', error);
    throw error;
  }
};

const updateCategory = async (id, categoryData) => {
  try {
    const response = await inventoryApi.put(`/categories/${id}`, categoryData);
    return response.data;
  } catch (error) {
    console.error(`Error updating category ${id}:`, error);
    throw error;
  }
};

const deleteCategory = async (id) => {
  try {
    await inventoryApi.delete(`/categories/${id}`);
  } catch (error) {
    console.error(`Error deleting category ${id}:`, error);
    throw error;
  }
};

// Location APIs
const getLocations = async () => {
  try {
    const response = await inventoryApi.get('/locations');
    return response.data;
  } catch (error) {
    console.error('Error fetching locations:', error);
    throw error;
  }
};

const createLocation = async (locationData) => {
  try {
    const response = await inventoryApi.post('/locations', locationData);
    return response.data;
  } catch (error) {
    console.error('Error creating location:', error);
    throw error;
  }
};

const updateLocation = async (id, locationData) => {
  try {
    const response = await inventoryApi.put(`/locations/${id}`, locationData);
    return response.data;
  } catch (error) {
    console.error(`Error updating location ${id}:`, error);
    throw error;
  }
};

const deleteLocation = async (id) => {
  try {
    await inventoryApi.delete(`/locations/${id}`);
  } catch (error) {
    console.error(`Error deleting location ${id}:`, error);
    throw error;
  }
};

// Item APIs
const getItems = async () => {
  try {
    const response = await inventoryApi.get('/items');
    return response.data;
  } catch (error) {
    console.error('Error fetching items:', error);
    throw error;
  }
};

const getItemById = async (id) => {
  try {
    const response = await inventoryApi.get(`/items/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching item ${id}:`, error);
    throw error;
  }
};

const createItem = async (itemData) => {
  try {
    const response = await inventoryApi.post('/items', itemData);
    return response.data;
  } catch (error) {
    console.error('Error creating item:', error);
    throw error;
  }
};

const updateItem = async (id, itemData) => {
  try {
    const response = await inventoryApi.put(`/items/${id}`, itemData);
    return response.data;
  } catch (error) {
    console.error(`Error updating item ${id}:`, error);
    throw error;
  }
};

const deleteItem = async (id) => {
  try {
    await inventoryApi.delete(`/items/${id}`);
  } catch (error) {
    console.error(`Error deleting item ${id}:`, error);
    throw error;
  }
};

// Item Variant APIs
const getItemVariants = async (itemId) => {
  try {
    const response = await inventoryApi.get(`/items/${itemId}/variants`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching variants for item ${itemId}:`, error);
    throw error;
  }
};

const createItemVariant = async (itemId, variantData) => {
  try {
    const response = await inventoryApi.post(`/items/${itemId}/variants`, variantData);
    return response.data;
  } catch (error) {
    console.error(`Error creating variant for item ${itemId}:`, error);
    throw error;
  }
};

const updateItemVariant = async (variantId, variantData) => {
  try {
    const response = await inventoryApi.put(`/items/variants/${variantId}`, variantData);
    return response.data;
  } catch (error) {
    console.error(`Error updating variant ${variantId}:`, error);
    throw error;
  }
};

const deleteItemVariant = async (variantId) => {
  try {
    await inventoryApi.delete(`/items/variants/${variantId}`);
  } catch (error) {
    console.error(`Error deleting variant ${variantId}:`, error);
    throw error;
  }
};

// Inventory Level APIs
const getInventoryLevels = async (locationId) => {
  try {
    const response = await inventoryApi.get(`/inventory/levels/location/${locationId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching inventory levels for location ${locationId}:`, error);
    throw error;
  }
};

const getInventoryLevelsByVariant = async (variantId) => {
  try {
    const response = await inventoryApi.get(`/inventory/levels/variant/${variantId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching inventory levels for variant ${variantId}:`, error);
    throw error;
  }
};

const getLowStockItems = async () => {
  try {
    const response = await inventoryApi.get('/inventory/levels/low-stock');
    return response.data;
  } catch (error) {
    console.error('Error fetching low stock items:', error);
    throw error;
  }
};

const updateInventoryQuantity = async (locationId, itemVariantId, quantity, transactionType, notes) => {
  try {
    const response = await inventoryApi.post('/inventory/update', null, {
      params: {
        locationId,
        itemVariantId,
        quantity,
        transactionType,
        notes
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error updating inventory quantity:', error);
    throw error;
  }
};

const setReorderPoint = async (locationId, itemVariantId, reorderPoint, reorderQuantity) => {
  try {
    const response = await inventoryApi.post('/inventory/reorder-point', null, {
      params: {
        locationId,
        itemVariantId,
        reorderPoint,
        reorderQuantity
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error setting reorder point:', error);
    throw error;
  }
};

// Export all API functions
const inventoryService = {
  // Brand APIs
  getBrands,
  createBrand,
  updateBrand,
  deleteBrand,
  
  // Category APIs
  getCategories,
  getTopLevelCategories,
  getSubcategories,
  createCategory,
  updateCategory,
  deleteCategory,
  
  // Location APIs
  getLocations,
  createLocation,
  updateLocation,
  deleteLocation,
  
  // Item APIs
  getItems,
  getItemById,
  createItem,
  updateItem,
  deleteItem,
  
  // Item Variant APIs
  getItemVariants,
  createItemVariant,
  updateItemVariant,
  deleteItemVariant,
  
  // Inventory Level APIs
  getInventoryLevels,
  getInventoryLevelsByVariant,
  getLowStockItems,
  updateInventoryQuantity,
  setReorderPoint
};

export default inventoryService;
