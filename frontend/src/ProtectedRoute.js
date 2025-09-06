import React from 'react';
import { useAuth } from './AuthContext';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh'
      }}>
        <div>Loading...</div>
      </div>
    );
  }

  if (!user?.authenticated) {
    return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        textAlign: 'center'
      }}>
        <h2>Access Denied</h2>
        <p>Please log in to access this page.</p>
        <a href="/login" style={{
          color: '#667eea',
          textDecoration: 'none',
          fontWeight: '500'
        }}>
          Go to Login
        </a>
      </div>
    );
  }

  return children;
};

export default ProtectedRoute;
