import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import FavouriteToggle from '../components/FavouriteToggle';

const MyFavourites = () => {
  const { apiCall, isAuthenticated } = useAuth();
  const [favourites, setFavourites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      fetchFavourites();
    } else {
      setLoading(false);
      setError('Please log in to view your favourites');
    }
  }, [isAuthenticated]);

  const fetchFavourites = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await apiCall('/favourites/my');
      if (!response.ok) {
        throw new Error(`Failed to fetch favourites: ${response.status}`);
      }
      const data = await response.json();
      setFavourites(data);
    } catch (e) {
      setError(e.message || 'Failed to load favourites');
      console.error('Error loading favourites:', e);
    } finally {
      setLoading(false);
    }
  };

  const removeFavourite = async (bookId) => {
    try {
      const response = await apiCall(`/favourites/book/${bookId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        // Remove the favourite from the local state
        setFavourites(prev => prev.filter(fav => fav.book.id !== bookId));
      } else {
        console.error('Failed to remove favourite');
        alert('Failed to remove favourite. Please try again.');
      }
    } catch (error) {
      console.error('Error removing favourite:', error);
      alert('Error removing favourite. Please try again.');
    }
  };

  if (!isAuthenticated) {
    return (
      <div style={{ padding: 20, maxWidth: 1000, margin: '0 auto', textAlign: 'center' }}>
        <h2>Please log in to view your favourites</h2>
        <p>You need to be logged in to see your favourited books.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: 20, maxWidth: 1000, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 16 }}>My Favourites</h2>

      {loading && <div>Loading your favourites...</div>}

      {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}

      {!loading && !error && favourites.length === 0 && (
        <div style={{ textAlign: 'center', color: '#666', marginTop: 40 }}>
          <div style={{ fontSize: '48px', marginBottom: 16 }}>🤍</div>
          <h3>No favourites yet</h3>
          <p>Start exploring books and add them to your favourites!</p>
          <Link
            to="/books"
            style={{
              display: 'inline-block',
              padding: '10px 20px',
              background: '#4CAF50',
              color: 'white',
              textDecoration: 'none',
              borderRadius: '4px',
              marginTop: 16
            }}
          >
            Browse Books
          </Link>
        </div>
      )}

      {!loading && favourites.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 16 }}>
          {favourites.map(favourite => (
            <div key={favourite.id} style={{ display: 'flex', flexDirection: 'column', gap: '8px', height: '100%' }}>
              <Link to={`/books/${favourite.book.id}`} style={{ textDecoration: 'none', color: 'inherit', flex: 1 }}>
                <div style={{ border: '1px solid #eee', borderRadius: 10, overflow: 'hidden', background: 'white', display: 'flex', flexDirection: 'column', height: '100%' }}>
                  {favourite.book.coverUrl ? (
                    <img src={favourite.book.coverUrl} alt={favourite.book.title} style={{ width: '100%', height: 220, objectFit: 'cover', background: '#fafafa' }} />
                  ) : (
                    <div style={{ width: '100%', height: 220, background: '#f2f2f2', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#888' }}>No Cover</div>
                  )}
                  <div style={{ padding: 12, flex: 1, display: 'flex', flexDirection: 'column' }}>
                    <div style={{ fontWeight: 600, marginBottom: 6 }}>{favourite.book.title}</div>
                    <div style={{ fontSize: 13, color: '#555', marginBottom: 8 }}>{favourite.book.author}{favourite.book.year ? ` • ${favourite.book.year}` : ''}</div>
                    {favourite.book.genres && <div style={{ fontSize: 12, color: '#777', marginBottom: 8 }}>{favourite.book.genres}</div>}
                    <div style={{ marginTop: 'auto', fontSize: 12, color: '#999' }}>
                      Added {new Date(favourite.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>
              </Link>
              
              {/* Remove button below the card */}
              <button
                onClick={(e) => {
                  e.preventDefault();
                  removeFavourite(favourite.book.id);
                }}
                style={{
                  width: '100%',
                  padding: '8px 12px',
                  background: '#f5f5f5',
                  color: '#666',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: '500',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => {
                  e.target.style.backgroundColor = '#e0e0e0';
                  e.target.style.color = '#333';
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = '#f5f5f5';
                  e.target.style.color = '#666';
                }}
                title="Remove from favourites"
              >
                <span style={{ fontSize: '16px' }}>✕</span>
                Remove
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyFavourites;
