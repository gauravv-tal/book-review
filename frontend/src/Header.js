import React from 'react';
import { useAuth } from './AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import './Header.css';

const Header = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-left">
          <h1 className="logo">
            <Link to="/">📚 Book Review</Link>
          </h1>
        </div>

        <nav className="header-nav">
          {user?.authenticated ? (
            <div className="nav-authenticated">
              <Link to="/" className="nav-link">Books</Link>
              <Link to="/recommendations" className="nav-link">Recommendations</Link>
              <Link to="/my-profile" className="nav-link">My Profile</Link>
              <span className="welcome-text">
                Welcome, {user.email || 'User'}!
              </span>
              <button onClick={handleLogout} className="logout-button">
                Logout
              </button>
            </div>
          ) : (
            <div className="nav-unauthenticated">
              <Link to="/login" className="nav-link">Login</Link>
              <Link to="/signup" className="nav-link nav-link-primary">Sign Up</Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;
