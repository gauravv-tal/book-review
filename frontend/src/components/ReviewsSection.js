import React, { useState, useEffect } from 'react';
import StarRating from './StarRating';
import ReviewForm from './ReviewForm';
import UserReview from './UserReview';

const ReviewsSection = ({
  isAuthenticated,
  userReview,
  reviews,
  isSubmitting,
  onSubmitReview,
  onDeleteReview,
}) => {
  // Handle undefined isAuthenticated by treating it as false
  const isAuth = Boolean(isAuthenticated);
  
  const [isWritingReview, setIsWritingReview] = useState(isAuth && !userReview);
  const [isEditingReview, setIsEditingReview] = useState(false);

  // Auto-open write form when authenticated and no user review; close when review appears
  useEffect(() => {
    console.log('ReviewsSection - Auth state:', { isAuthenticated, isAuth, userReview, hasUserReview: !!userReview });
    if (isAuth && !userReview) {
      setIsWritingReview(true);
      setIsEditingReview(false);
    } else {
      setIsWritingReview(false);
    }
  }, [isAuth, userReview]);

  console.log('ReviewsSection render:', { 
    isAuthenticated, 
    userReview: !!userReview, 
    isWritingReview, 
    shouldShowButton: isAuthenticated && !userReview 
  });

  return (
    <div style={{ margin: '32px 0' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <h2 style={{ margin: 0 }}>Reviews</h2>
        <div>
          {isAuth && !userReview && (
            isWritingReview ? (
              <button
                onClick={() => setIsWritingReview(false)}
                style={{
                  padding: '8px 16px',
                  background: '#f0f0f0',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                Cancel
              </button>
            ) : (
              <button 
                onClick={() => setIsWritingReview(true)}
                style={{
                  padding: '8px 16px',
                  background: '#4CAF50',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                Write a Review
              </button>
            )
          )}
        </div>
      </div>

      {(isWritingReview || isEditingReview) && (
        <ReviewForm 
          initialText={isEditingReview ? userReview?.text : ''}
          initialRating={isEditingReview ? userReview?.rating : undefined}
          onSubmit={async (payload) => {
            await onSubmitReview(payload);
            setIsWritingReview(false);
            setIsEditingReview(false);
          }}
          onCancel={() => {
            setIsWritingReview(false);
            setIsEditingReview(false);
          }}
          isSubmitting={isSubmitting}
        />
      )}

      {userReview && !isEditingReview && (
        <UserReview 
          review={userReview}
          onEdit={() => setIsEditingReview(true)}
          onDelete={onDeleteReview}
          isSubmitting={isSubmitting}
        />
      )}

      {/* Other Reviews */}
      {reviews
        .filter(r => !userReview || r.id !== userReview.id)
        .map(review => (
          <div 
            key={review.id} 
            style={{ 
              margin: '16px 0', 
              padding: '16px', 
              border: '1px solid #e0e0e0', 
              borderRadius: '8px' 
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <div>
                <StarRating rating={review.rating} disabled={true} />
                <span style={{ marginLeft: '8px', color: '#666', fontSize: '14px' }}>
                  {review.userName} • {new Date(review.createdAt).toLocaleDateString()}
                </span>
              </div>
            </div>
            {review.text && (
              <div style={{ marginTop: '8px', color: '#333' }}>
                {review.text}
              </div>
            )}
          </div>
        ))}

      {/* Empty state when no reviews at all */}
      {(!reviews || reviews.length === 0) && (
        <div style={{ color: '#666', textAlign: 'center', padding: '20px' }}>
          No reviews yet. Be the first to review!
        </div>
      )}
    </div>
  );
};

export default ReviewsSection;
