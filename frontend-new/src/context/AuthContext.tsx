import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import authApi, { UserInfo } from '../api/auth';

// Define the context type
interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  userInfo: UserInfo | null;
  login: (newToken: string) => void;
  logout: () => void;
  setCurrentUserInfo: (user: UserInfo) => void;
}

// Create the context with default values
const AuthContext = createContext<AuthContextType>({
  token: null,
  isAuthenticated: false,
  userInfo: null,
  login: () => {},
  logout: () => {},
  setCurrentUserInfo: () => {}
});

// Create the context provider component
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const isAuthenticated = !!token;
  
  // Load user profile if token exists
  useEffect(() => {
    const fetchUserProfile = async () => {
      if (token) {
        try {
          const userData = await authApi.getCurrentUser(token);
          setUserInfo(userData);
        } catch (error) {
          console.error('Error fetching user info:', error);
          // If token is invalid, log out
          logout();
        }
      }
    };
    
    fetchUserProfile();
  }, [token]);
  
  // Save token to localStorage when it changes
  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  }, [token]);
  
  // Login function
  const login = (newToken: string) => {
    setToken(newToken);
  };
  
  // Logout function
  const logout = () => {
    setToken(null);
    setUserInfo(null);
    localStorage.removeItem('token');
  };
  
  // Set user info
  const setCurrentUserInfo = (user: UserInfo) => {
    setUserInfo(user);
  };
  
  // Create the context value
  const contextValue = {
    token,
    isAuthenticated,
    userInfo,
    login,
    logout,
    setCurrentUserInfo
  };
  
  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook for using the auth context
export const useAuth = () => useContext(AuthContext);

export default AuthContext;
