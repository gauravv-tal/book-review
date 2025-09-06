import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import StarRating from '../components/StarRating';

const MyProfile = () => {
  const { apiCall, isAuthenticated } = useAuth();
  const [favourites, setFavourites] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      fetchProfileData();
    } else {
      setLoading(false);
      setError('Please log in to view your profile');
    }
  }, [isAuthenticated]);

  const fetchProfileData = async () => {
    setLoading(true);
    setError('');
    try {
      // Fetch both favourites and reviews in parallel
      const [favouritesResponse, reviewsResponse] = await Promise.all([
        apiCall('/favourites/my'),
        apiCall('/reviews/my')
      ]);

      if (!favouritesResponse.ok) {
        throw new Error(`Failed to fetch favourites: ${favouritesResponse.status}`);
      }
      if (!reviewsResponse.ok) {
        throw new Error(`Failed to fetch reviews: ${reviewsResponse.status}`);
      }

      const favouritesData = await favouritesResponse.json();
      const reviewsData = await reviewsResponse.json();

      setFavourites(favouritesData);
      setReviews(reviewsData);
    } catch (e) {
      setError(e.message || 'Failed to load profile data');
      console.error('Error loading profile data:', e);
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
        <h2>Please log in to view your profile</h2>
        <p>You need to be logged in to see your favourites and reviews.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: 20, maxWidth: 1000, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 16 }}>My Profile</h2>

      {loading && <div>Loading your profile...</div>}

      {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}

      {/* Favourites Section */}
      <div style={{ marginBottom: 40 }}>
        <h3 style={{ marginBottom: 16, color: '#333', borderBottom: '2px solid #667eea', paddingBottom: '8px' }}>
          My Favourites ({favourites.length})
        </h3>

        {!loading && favourites.length === 0 && (
          <div style={{ textAlign: 'center', color: '#666', marginTop: 20, marginBottom: 20 }}>
            <div style={{ fontSize: '48px', marginBottom: 16 }}>🤍</div>
            <h4>No favourites yet</h4>
            <p>You haven't favourited any books yet.</p>
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

      {/* Reviews Section */}
      <div>
        <h3 style={{ marginBottom: 16, color: '#333', borderBottom: '2px solid #667eea', paddingBottom: '8px' }}>
          My Reviews ({reviews.length})
        </h3>

        {!loading && reviews.length === 0 && (
          <div style={{ textAlign: 'center', color: '#666', marginTop: 20 }}>
            <div style={{ fontSize: '48px', marginBottom: 16 }}>📝</div>
            <h4>No reviews yet</h4>
            <p>You haven't written any reviews yet.</p>
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

        {!loading && reviews.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {reviews
              .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
              .map(review => (
                <div key={review.id} style={{
                  border: '1px solid #eee',
                  borderRadius: '8px',
                  padding: '16px',
                  background: 'white',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.1)'
                }}>
                  <div style={{ marginBottom: '12px' }}>
                    <Link
                      to={`/books/${review.book.id}`}
                      style={{
                        fontSize: '16px',
                        fontWeight: '600',
                        color: '#667eea',
                        textDecoration: 'none'
                      }}
                    >
                      {review.book.title}
                    </Link>
                    <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                      {review.book.author}{review.book.year ? ` • ${review.book.year}` : ''}
                    </div>
                  </div>

                  <div style={{ marginBottom: '12px' }}>
                    <StarRating rating={review.rating} disabled={true} />
                    <div style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>
                      Reviewed on {new Date(review.createdAt).toLocaleDateString()}
                    </div>
                  </div>

                  {review.text && (
                    <div style={{
                      fontSize: '14px',
                      lineHeight: '1.5',
                      color: '#333',
                      padding: '12px',
                      backgroundColor: '#f9f9f9',
                      borderRadius: '6px',
                      borderLeft: '3px solid #4CAF50'
                    }}>
                      {review.text}
                    </div>
                  )}
                </div>
              ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyProfile;
