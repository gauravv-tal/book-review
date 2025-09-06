import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import StarRating from '../components/StarRating';

const MyReviews = () => {
  const { apiCall, isAuthenticated } = useAuth();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isAuthenticated) {
      fetchMyReviews();
    } else {
      setLoading(false);
      setError('Please log in to view your reviews');
    }
  }, [isAuthenticated]);

  const fetchMyReviews = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await apiCall('/reviews/my');
      if (!response.ok) {
        throw new Error(`Failed to fetch reviews: ${response.status}`);
      }
      const data = await response.json();
      setReviews(data);
    } catch (e) {
      setError(e.message || 'Failed to load reviews');
      console.error('Error loading reviews:', e);
    } finally {
      setLoading(false);
    }
  };

  if (!isAuthenticated) {
    return (
      <div style={{ padding: 20, maxWidth: 1000, margin: '0 auto', textAlign: 'center' }}>
        <h2>Please log in to view your reviews</h2>
        <p>You need to be logged in to see your reviews.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: 20, maxWidth: 1000, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 16 }}>My Reviews</h2>

      {loading && <div>Loading your reviews...</div>}

      {error && <div style={{ color: 'red', marginBottom: 16 }}>{error}</div>}

      {!loading && !error && reviews.length === 0 && (
        <div style={{ textAlign: 'center', color: '#666', marginTop: 40 }}>
          <div style={{ fontSize: '48px', marginBottom: 16 }}>📝</div>
          <h3>No reviews yet</h3>
          <p>You haven't written any reviews yet. Start exploring books and share your thoughts!</p>
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
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 16 }}>
          {reviews.map(review => (
            <div key={review.id} style={{ border: '1px solid #eee', borderRadius: 10, overflow: 'hidden', background: 'white', display: 'flex', flexDirection: 'column' }}>
              <Link to={`/books/${review.book.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                {review.book.coverUrl ? (
                  <img src={review.book.coverUrl} alt={review.book.title} style={{ width: '100%', height: 200, objectFit: 'cover', background: '#fafafa' }} />
                ) : (
                  <div style={{ width: '100%', height: 200, background: '#f2f2f2', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#888' }}>No Cover</div>
                )}
              </Link>

              <div style={{ padding: 16 }}>
                <Link to={`/books/${review.book.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                  <div style={{ fontWeight: 600, marginBottom: 6 }}>{review.book.title}</div>
                  <div style={{ fontSize: 13, color: '#555', marginBottom: 8 }}>{review.book.author}{review.book.year ? ` • ${review.book.year}` : ''}</div>
                </Link>

                <div style={{ marginBottom: 12 }}>
                  <StarRating rating={review.rating} disabled={true} />
                  <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
                    Reviewed on {new Date(review.createdAt).toLocaleDateString()}
                  </div>
                </div>

                {review.text && (
                  <div style={{
                    padding: '12px',
                    backgroundColor: '#f9f9f9',
                    borderRadius: '6px',
                    borderLeft: '3px solid #4CAF50',
                    fontSize: '14px',
                    lineHeight: '1.5',
                    color: '#333'
                  }}>
                    {review.text}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyReviews;
