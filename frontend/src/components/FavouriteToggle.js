import React, { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';

const FavouriteToggle = ({ bookId, size = 24, className = '' }) => {
  const { apiCall, isAuthenticated } = useAuth();
  const [isFavourite, setIsFavourite] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    console.log('FavouriteToggle - Component mounted with:', { bookId, isAuthenticated });
    if (isAuthenticated && bookId) {
      checkFavouriteStatus();
    }
  }, [bookId, isAuthenticated]);

  const checkFavouriteStatus = async () => {
    console.log('FavouriteToggle - Checking favourite status for bookId:', bookId);
    try {
      const response = await apiCall(`/favourites/book/${bookId}/check`);
      console.log('FavouriteToggle - Check response:', response.status, response.ok);
      if (response.ok) {
        const isFav = await response.json();
        console.log('FavouriteToggle - Is favourite:', isFav);
        setIsFavourite(isFav);
      }
    } catch (error) {
      console.error('Error checking favourite status:', error);
    }
  };

  const toggleFavourite = async () => {
    console.log('FavouriteToggle - Toggle clicked for bookId:', bookId);
    if (!isAuthenticated) {
      alert('Please log in to add favourites');
      return;
    }

    setLoading(true);
    try {
      console.log('FavouriteToggle - Making API call to toggle favourite');
      const response = await apiCall(`/favourites/book/${bookId}/toggle`, {
        method: 'PUT'
      });
      console.log('FavouriteToggle - Toggle response:', response.status, response.ok);

      if (response.ok) {
        setIsFavourite(!isFavourite);
        console.log('FavouriteToggle - Successfully toggled favourite');
      } else {
        console.error('Failed to toggle favourite');
      }
    } catch (error) {
      console.error('Error toggling favourite:', error);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return null; // Don't show toggle for unauthenticated users
  }

  return (
    <button
      onClick={toggleFavourite}
      disabled={loading}
      className={`favourite-toggle ${className}`}
      style={{
        background: 'none',
        border: 'none',
        cursor: loading ? 'not-allowed' : 'pointer',
        padding: '4px',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        opacity: loading ? 0.6 : 1,
        transition: 'all 0.2s'
      }}
      title={isFavourite ? 'Remove from favourites' : 'Add to favourites'}
    >
      {isFavourite ? (
        <span
          style={{
            fontSize: `${size}px`,
            color: '#ff4081',
            filter: 'drop-shadow(0 0 2px rgba(255, 64, 129, 0.3))'
          }}
        >
          ❤️
        </span>
      ) : (
        <span
          style={{
            fontSize: `${size}px`,
            color: '#666',
            transition: 'color 0.2s'
          }}
        >
          🤍
        </span>
      )}
    </button>
  );
};

export default FavouriteToggle;
