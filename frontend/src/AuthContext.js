import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Load JWT token from localStorage on mount
  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      // You might want to validate the token here or set user state
      setUser({ authenticated: true });
    }
    setLoading(false);
  }, []);

  // Helper function to make authenticated API calls
  const apiCall = async (url, options = {}) => {
    const token = localStorage.getItem('jwtToken');
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      // Token expired or invalid
      logout();
      throw new Error('Authentication failed');
    }

    return response;
  };

  const signup = async (name, email, password) => {
    try {
      const response = await fetch('/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error || 'Signup failed');
      }
      // For signup, do not auto-login. Let the UI redirect to Login page.
      // We still consume the response body to avoid stream locking errors.
      try { await response.json(); } catch (_) { /* ignore non-JSON */ }
      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  };

  const login = async (email, password) => {
    try {
      const response = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        let errJson = {};
        try { errJson = await response.json(); } catch (_) { /* ignore */ }
        const baseMsg = errJson.error || 'Login failed';
        if (response.status === 401) {
          return { success: false, error: 'Authentication failed', code: 'AUTH_FAILED' };
        }
        return { success: false, error: baseMsg };
      }

      const data = await response.json();
      const token = data.token;

      if (token) {
        localStorage.setItem('jwtToken', token);
        setUser({ authenticated: true, email });
        return { success: true };
      }
    } catch (error) {
      return { success: false, error: error.message || 'Login failed' };
    }
  };

  const logout = async () => {
    try {
      await fetch('/auth/logout', { method: 'POST' });
    } catch (error) {
      console.warn('Logout request failed:', error);
    } finally {
      localStorage.removeItem('jwtToken');
      setUser(null);
    }
  };

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    signup,
    login,
    logout,
    apiCall,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
