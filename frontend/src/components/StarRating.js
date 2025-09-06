import React, { useState, useEffect } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

const StarContainer = styled.div`
  display: inline-block;
`;

// Removed the styled component since we're using inline styles

const StarRating = ({ rating = 0, onRatingChange, disabled = false }) => {
  const [hoverRating, setHoverRating] = useState(0);
  const [localRating, setLocalRating] = useState(rating);
  
  // Sync local state with prop changes
  useEffect(() => {
    setLocalRating(rating);
  }, [rating]);
  
  const handleClick = (value) => {
    if (disabled || !onRatingChange) return;
    
    const newRating = value === localRating ? 0 : value;
    console.log('Star clicked - New rating:', newRating);
    
    setLocalRating(newRating);
    onRatingChange(newRating);
  };
  
  const handleKeyDown = (e, value) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      handleClick(value);
    }
  };

  // Show hover rating when hovering, otherwise show selected rating
  const displayRating = hoverRating || localRating || 0;
  
  return (
    <div 
      style={{ 
        display: 'inline-block',
        pointerEvents: disabled ? 'none' : 'auto',
        opacity: disabled ? 0.7 : 1
      }}
    >
      {[1, 2, 3, 4, 5].map((value) => (
        <span
          key={value}
          role="button"
          aria-label={disabled ? `Rating: ${value} out of 5` : `Rate ${value} out of 5`}
          tabIndex={disabled ? -1 : 0}
          onClick={() => handleClick(value)}
          onKeyDown={(e) => handleKeyDown(e, value)}
          onMouseEnter={() => !disabled && setHoverRating(value)}
          onMouseLeave={() => !disabled && setHoverRating(0)}
          style={{
            display: 'inline-block',
            cursor: disabled ? 'default' : 'pointer',
            fontSize: '24px',
            color: value <= displayRating ? '#ffc107' : '#e4e5e9',
            padding: '4px',
            userSelect: 'none',
            transition: 'all 0.2s',
            ...(value <= displayRating ? { transform: 'scale(1.1)' } : {})
          }}
        >
          {value <= displayRating ? '★' : '☆'}
        </span>
      ))}
    </div>
  );
};

StarRating.propTypes = {
  rating: PropTypes.number,
  onRatingChange: PropTypes.func,
  disabled: PropTypes.bool
};

export default StarRating;
