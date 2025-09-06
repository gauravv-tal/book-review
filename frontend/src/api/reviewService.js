import { useAuth } from '../AuthContext';

const useReviewService = () => {
  const { apiCall } = useAuth();

  const getBookReviews = async (bookId) => {
    const response = await apiCall(`/reviews/book/${bookId}`);
    if (!response.ok) {
      throw new Error('Failed to fetch reviews');
    }
    return await response.json();
  };

  const getUserReview = async (bookId) => {
    const response = await apiCall(`/reviews/book/${bookId}/my`);
    if (response.status === 404) return null;
    if (!response.ok) {
      throw new Error('Failed to fetch your review');
    }
    return await response.json();
  };

  const createOrUpdateReview = async (bookId, text, rating) => {
    const response = await apiCall(`/reviews/book/${bookId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ text, rating })
    });
    if (!response.ok) {
      throw new Error('Failed to save review');
    }
    return await response.json();
  };

  const deleteReview = async (reviewId) => {
    const response = await apiCall(`/reviews/${reviewId}`, {
      method: 'DELETE'
    });
    if (!response.ok) {
      throw new Error('Failed to delete review');
    }
  };

  return {
    getBookReviews,
    getUserReview,
    createOrUpdateReview,
    deleteReview
  };
};

export default useReviewService;
