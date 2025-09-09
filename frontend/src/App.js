import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import { useAuth } from './AuthContext';
import Header from './Header';
import Login from './Login';
import Signup from './Signup';
import ProtectedRoute from './ProtectedRoute';
import BooksList from './BooksList';
import BookDetails from './BookDetails';
import MyProfile from './components/MyProfile';
import Recommendations from './components/Recommendations';
import './App.css';

// Dashboard/Home component for authenticated users
const Dashboard = () => {
  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h2>Welcome to Book Review Platform!</h2>
      <p>You are successfully logged in. This is a protected page.</p>
      <div style={{
        background: '#f8f9fa',
        padding: '20px',
        borderRadius: '8px',
        marginTop: '20px'
      }}>
        <h3>What's Next?</h3>
        <ul>
          <li>Browse and review books</li>
          <li>Manage your favorite books</li>
          <li>Share reviews with other readers</li>
        </ul>
      </div>
    </div>
  );
};

// Home component for unauthenticated users
const Home = () => {
  return (
    <div style={{
      padding: '40px 20px',
      textAlign: 'center',
      maxWidth: '800px',
      margin: '0 auto'
    }}>
      <h1>Welcome to Book Review Platform</h1>
      <p style={{ fontSize: '18px', color: '#666', margin: '20px 0' }}>
        Discover, review, and share your thoughts on books with fellow readers.
      </p>
      <div style={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        padding: '30px',
        borderRadius: '12px',
        margin: '30px 0'
      }}>
        <h2>Join Our Community</h2>
        <p>Create an account to start reviewing books and connecting with other readers.</p>
        <div style={{ marginTop: '20px' }}>
        </div>
      </div>
    </div>
  );
};

// Route element that shows Home for guests, BooksList for authenticated users
const HomeOrBooks = () => {
  const { user } = useAuth();
  return user?.authenticated ? <BooksList /> : <Home />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Header />
          <main style={{ minHeight: 'calc(100vh - 70px)' }}>
            <Routes>
              <Route path="/" element={<HomeOrBooks />} />
              <Route path="/login" element={<Login />} />
              <Route path="/signup" element={<Signup />} />
              <Route path="/recommendations" element={<Recommendations />} />
              <Route
                path="/dashboard"
                element={
                  <ProtectedRoute>
                    <Dashboard />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/books/:id"
                element={
                  <ProtectedRoute>
                    <BookDetails />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/my-profile"
                element={
                  <ProtectedRoute>
                    <MyProfile />
                  </ProtectedRoute>
                }
              />
              {/* Catch all route - redirect to home */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
