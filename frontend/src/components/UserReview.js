import React from 'react';
import StarRating from './StarRating';

const UserReview = ({ review, onEdit, onDelete, isSubmitting }) => {
  return (
    <div style={{ 
      margin: '20px 0', 
      padding: '16px', 
      border: '1px solid #e0e0e0', 
      borderRadius: '8px',
      backgroundColor: '#f8f9fa'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
        <div>
          <strong>Your Review</strong>
          <div style={{ marginTop: '4px' }}>
            <StarRating rating={review.rating} disabled={true} />
            <span style={{ marginLeft: '8px', color: '#666', fontSize: '14px' }}>
              {new Date(review.createdAt).toLocaleDateString()}
            </span>
          </div>
        </div>
        <div>
          <button 
            onClick={onEdit}
            disabled={isSubmitting}
            style={{
              marginRight: '8px',
              padding: '4px 8px',
              background: '#f0f0f0',
              border: '1px solid #ddd',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px'
            }}
          >
            Edit
          </button>
          <button 
            onClick={onDelete}
            disabled={isSubmitting}
            style={{
              padding: '4px 8px',
              background: '#fff0f0',
              border: '1px solid #ffdddd',
              color: '#cc0000',
              borderRadius: '4px',
              cursor: 'pointer',
              fontSize: '12px'
            }}
          >
            Delete
          </button>
        </div>
      </div>
      {review.text && (
        <div style={{ 
          marginTop: '12px', 
          padding: '12px', 
          backgroundColor: 'white', 
          borderRadius: '4px',
          borderLeft: '3px solid #4CAF50'
        }}>
          {review.text}
        </div>
      )}
    </div>
  );
};

export default UserReview;
