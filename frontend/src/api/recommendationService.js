import { useAuth } from '../AuthContext';
import { useCallback } from 'react';

const useRecommendationService = () => {
  const { apiCall, user } = useAuth();

  const getTopRated = useCallback(async () => {
    const res = await apiCall('/recommendations/top-rated');
    if (!res.ok) throw new Error('Failed to load top rated');
    return await res.json();
  }, [apiCall]);

  const getAiRecommendations = useCallback(async () => {
    if (!user?.authenticated) throw new Error('Not authenticated');
    const res = await apiCall('/recommendations/ai');
    if (!res.ok) throw new Error('Failed to load AI recommendations');
    return await res.json();
  }, [apiCall, user?.authenticated]);

  return { getTopRated, getAiRecommendations };
};

export default useRecommendationService;
