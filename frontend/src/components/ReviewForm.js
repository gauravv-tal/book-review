import React, { useState, useEffect } from 'react';
import StarRating from './StarRating';

const ReviewForm = ({ initialText = '', initialRating, onSubmit, onCancel, isSubmitting }) => {
  const [text, setText] = useState(initialText);
  const [rating, setRating] = useState(initialRating);
  const [error, setError] = useState('');
  
  // Reset form when initial values change (e.g., when switching between new/edit mode)
  useEffect(() => {
    setText(initialText);
    setRating(initialRating);
  }, [initialText, initialRating]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!rating) {
      setError('Please select a rating');
      return;
    }
    onSubmit({ text, rating });
  };
  
  const handleRatingChange = (newRating) => {
    setRating(newRating);
    setError(''); // Clear any previous error when user selects a rating
  };

  return (
    <div style={{ marginTop: '20px', padding: '20px', border: '1px solid #eee', borderRadius: '8px' }}>
      <h3>{initialText ? 'Edit Your Review' : 'Write a Review'}</h3>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '16px' }}>
          <div style={{ marginBottom: '8px' }}>Your Rating:</div>
          <StarRating 
            rating={rating}
            onRatingChange={handleRatingChange}
            disabled={isSubmitting}
          />
          {error && <div style={{ color: 'red', marginTop: '8px' }}>{error}</div>}
        </div>
        <div style={{ marginBottom: '16px' }}>
          <div style={{ marginBottom: '8px' }}>Your Review (optional):</div>
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Share your thoughts about this book..."
            style={{ 
              width: '100%', 
              minHeight: '120px', 
              padding: '12px', 
              borderRadius: '4px', 
              border: '1px solid #ddd',
              fontSize: '14px',
              lineHeight: '1.5',
              resize: 'vertical'
            }}
          />
        </div>
        <div>
          <button 
            type="submit" 
            disabled={isSubmitting}
            style={{ 
              marginRight: '10px', 
              padding: '8px 16px', 
              background: '#4CAF50', 
              color: 'white', 
              border: 'none', 
              borderRadius: '4px', 
              cursor: 'pointer' 
            }}
          >
            {isSubmitting ? 'Saving...' : 'Submit Review'}
          </button>
          {onCancel && (
            <button 
              type="button" 
              onClick={onCancel}
              disabled={isSubmitting}
              style={{ 
                padding: '8px 16px', 
                background: '#f0f0f0', 
                border: '1px solid #ddd', 
                borderRadius: '4px', 
                cursor: 'pointer' 
              }}
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default ReviewForm;
