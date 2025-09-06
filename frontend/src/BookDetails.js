import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from './AuthContext';
import styled from 'styled-components';
import StarRating from './components/StarRating';
import ReviewsSection from './components/ReviewsSection';
import FavouriteToggle from './components/FavouriteToggle';

const Row = ({ label, value }) => (
  <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
    <div style={{ width: 100, color: '#666' }}>{label}</div>
    <div style={{ flex: 1 }}>{value || '-'}</div>
  </div>
);

const BookDetails = () => {
  const { id } = useParams();
  const { apiCall, isAuthenticated } = useAuth();
  const [book, setBook] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [userReview, setUserReview] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadBook = useCallback(async () => {
    try {
      const res = await apiCall(`/books/${id}`);
      if (res.status === 404) {
        setError('Book not found');
        setBook(null);
      } else if (!res.ok) {
        throw new Error(`Failed to load book: ${res.status}`);
      } else {
        const data = await res.json();
        setBook(data);
      }
    } catch (e) {
      setError(e.message);
    }
  }, [id, apiCall]);

  const loadReviews = useCallback(async () => {
    if (!id) return;
    
    try {
      // Load all reviews for the book
      const response = await apiCall(`/reviews/book/${id}`);
      if (response.status === 404) {
        setReviews([]);
      } else if (!response.ok) {
        throw new Error(`Failed to fetch reviews: ${response.status}`);
      } else {
        const allReviews = await response.json();
        setReviews(Array.isArray(allReviews) ? allReviews : []);
      }
      
      // Load current user's review if authenticated
      if (isAuthenticated) {
        try {
          const userResponse = await apiCall(`/reviews/book/${id}/my`);
          if (userResponse.status === 404) {
            console.log('No user review found for book', id);
            setUserReview(null);
          } else if (!userResponse.ok) {
            throw new Error(`Failed to fetch user review: ${userResponse.status}`);
          } else {
            const userReview = await userResponse.json();
            console.log('Loaded user review:', userReview);
            setUserReview(userReview);
          }
        } catch (e) {
          console.error('Error loading user review:', e);
          setUserReview(null);
        }
      } else {
        console.log('Not authenticated, skipping user review load');
        setUserReview(null);
      }
    } catch (e) {
      console.error('Error loading reviews:', e);
      setReviews([]);
    }
  }, [id, isAuthenticated, apiCall]);

  useEffect(() => {
    let isMounted = true;
    
    const loadData = async () => {
      if (!isMounted) return;
      
      setLoading(true);
      setError('');
      try {
        await Promise.all([
          loadBook(),
          loadReviews()
        ]);
      } catch (e) {
        if (isMounted) {
          setError(e.message || 'An error occurred');
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };
    
    loadData();
    
    return () => {
      isMounted = false;
    };
  }, [loadBook, loadReviews]);

  const handleSubmitReview = async ({ text, rating }) => {
    if (!isAuthenticated) return;
    
    setIsSubmitting(true);
    try {
      console.log('Submitting review:', { text, rating, bookId: id });
      const response = await apiCall(`/reviews/book/${id}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text, rating })
      });
      
      if (!response.ok) {
        throw new Error(`Failed to save review: ${response.status}`);
      }
      
      const review = await response.json();
      console.log('Review saved successfully:', review);
      setUserReview(review);
      await loadReviews(); // Refresh all reviews to update averages
    } catch (e) {
      setError('Failed to save review');
      console.error('Error saving review:', e);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteReview = async () => {
    if (!userReview || !window.confirm('Are you sure you want to delete your review?')) return;
    
    setIsSubmitting(true);
    try {
      const response = await apiCall(`/reviews/${userReview.id}`, {
        method: 'DELETE'
      });
      
      if (!response.ok) {
        throw new Error(`Failed to delete review: ${response.status}`);
      }
      
      setUserReview(null);
      await loadReviews(); // Refresh all reviews
    } catch (e) {
      setError('Failed to delete review');
      console.error(e);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) return <div style={{ padding: 20 }}>Loading...</div>;
  if (error) return <div style={{ padding: 20, color: 'red' }}>{error}</div>;
  if (!book) return <div style={{ padding: 20 }}>No data.</div>;
  
  const averageRating = book.avgRating ? parseFloat(book.avgRating).toFixed(1) : 'Not rated yet';
  const reviewCount = book.reviewCount || 0;

  return (
    <div style={{ padding: 20, maxWidth: 900, margin: '0 auto' }}>
      <Link to="/" style={{ color: '#667eea', textDecoration: 'none' }}>← Back to list</Link>

      <div style={{ display: 'grid', gridTemplateColumns: '260px 1fr', gap: 20, marginTop: 16 }}>
        <div>
          {book.coverUrl ? (
            <img 
              src={book.coverUrl} 
              alt={book.title} 
              style={{ 
                width: '100%', 
                borderRadius: 10, 
                background: '#f5f5f5',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
              }} 
            />
          ) : (
            <div style={{ 
              width: '100%', 
              height: 360, 
              background: '#f2f2f2', 
              borderRadius: 10, 
              display: 'flex', 
              alignItems: 'center', 
              justifyContent: 'center', 
              color: '#888',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
            }}>
              No Cover
            </div>
          )}
          
          <div style={{ marginTop: '20px', textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold' }}>{averageRating}</div>
            <StarRating rating={parseFloat(averageRating) || 0} disabled={false} />
            <div style={{ color: '#666', fontSize: '14px', marginTop: '4px' }}>
              {reviewCount} review{reviewCount !== 1 ? 's' : ''}
            </div>
          </div>
        </div>
        
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
            <h1 style={{ marginTop: 0, marginBottom: 0 }}>{book.title}</h1>
            {console.log('BookDetails - id:', id, 'bookId:', parseInt(id), 'isAuthenticated:', isAuthenticated)}
            <FavouriteToggle bookId={parseInt(id)} size={32} />
          </div>
          <Row label="Author" value={book.author} />
          <Row label="Year" value={book.year} />
          <Row label="Genres" value={book.genres} />
          
          <div style={{ margin: '24px 0' }}>
            <h3>Description</h3>
            <div style={{ 
              whiteSpace: 'pre-wrap', 
              lineHeight: 1.6,
              color: '#333',
              backgroundColor: '#f9f9f9',
              padding: '16px',
              borderRadius: '8px',
              borderLeft: '4px solid #4CAF50'
            }}>
              {book.description || 'No description available.'}
            </div>
          </div>
          
          {/* Reviews Section */}
          <ReviewsSection
            isAuthenticated={isAuthenticated}
            userReview={userReview}
            reviews={reviews}
            isSubmitting={isSubmitting}
            onSubmitReview={handleSubmitReview}
            onDeleteReview={handleDeleteReview}
          />
            {reviews.length === 0 && (
              <div style={{ color: '#666', textAlign: 'center', padding: '20px' }}>
                No reviews yet. Be the first to review!
              </div>
            )}
        </div>
      </div>
    </div>
  );
};

export default BookDetails;
