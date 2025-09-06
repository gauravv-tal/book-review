import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import useRecommendationService from '../api/recommendationService';

const Section = ({ title, children }) => (
  <div style={{ marginBottom: 32 }}>
    <h3 style={{ marginBottom: 12, color: '#333', borderBottom: '2px solid #667eea', paddingBottom: 8 }}>
      {title}
    </h3>
    {children}
  </div>
);

const BookList = ({ books }) => {
  if (!books || books.length === 0) return (
    <div style={{ color: '#666' }}>No books to show.</div>
  );
  return (
    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
      {books.map(b => (
        <li key={b.id} style={{
          padding: '10px 12px',
          border: '1px solid #eee',
          borderRadius: 8,
          marginBottom: 10,
          background: 'white',
          boxShadow: '0 1px 2px rgba(0,0,0,0.04)'
        }}>
          <div style={{ fontWeight: 600 }}>
            <Link to={`/books/${b.id}`} style={{ color: '#667eea', textDecoration: 'none' }}>
              {b.title}
            </Link>
          </div>
          <div style={{ fontSize: 13, color: '#555' }}>
            {b.author}{b.year ? ` • ${b.year}` : ''}
          </div>
        </li>
      ))}
    </ul>
  );
};

// Renders AI DTOs: { title, author } and opens Google search
const AiList = ({ items }) => {
  if (!items || items.length === 0) return (
    <div style={{ color: '#666' }}>No recommendations yet.</div>
  );
  return (
    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
      {items.map((it, idx) => (
        <li key={`${it.title}-${it.author || ''}-${idx}`} style={{
          padding: '10px 12px',
          border: '1px solid #eee',
          borderRadius: 8,
          marginBottom: 10,
          background: 'white',
          boxShadow: '0 1px 2px rgba(0,0,0,0.04)'
        }}>
          <div style={{ fontWeight: 600 }}>
            {(() => {
              const q = encodeURIComponent(`${it.title} ${it.author || ''}`.trim());
              const href = `https://www.google.com/search?q=${q}`;
              return (
                <a href={href} target="_blank" rel="noreferrer" style={{ color: '#667eea', textDecoration: 'none' }}>
                  {it.title}
                </a>
              );
            })()}
          </div>
          <div style={{ fontSize: 13, color: '#555' }}>
            {it.author}
          </div>
        </li>
      ))}
    </ul>
  );
};

const Recommendations = () => {
  const { user } = useAuth();
  const { getTopRated, getAiRecommendations } = useRecommendationService();

  const [topRated, setTopRated] = useState([]);
  const [aiRecs, setAiRecs] = useState([]);
  const [loadingTop, setLoadingTop] = useState(true);
  const [loadingAi, setLoadingAi] = useState(false);
  const [errorTop, setErrorTop] = useState('');
  const [errorAi, setErrorAi] = useState('');

  useEffect(() => {
    const load = async () => {
      setLoadingTop(true);
      try {
        const data = await getTopRated();
        setTopRated(data);
      } catch (e) {
        setErrorTop(e.message || 'Failed to load top rated');
      } finally {
        setLoadingTop(false);
      }
    };
    load();
  }, [getTopRated]);

  useEffect(() => {
    const loadAi = async () => {
      if (!user?.authenticated) return;
      setLoadingAi(true);
      try {
        const data = await getAiRecommendations();
        setAiRecs(data);
      } catch (e) {
        setErrorAi(e.message || 'Failed to load AI recommendations');
      } finally {
        setLoadingAi(false);
      }
    };
    loadAi();
  }, [user, getAiRecommendations]);

  return (
    <div style={{ padding: 20, maxWidth: 900, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 20 }}>Recommendations</h2>

      <Section title="Top Rated">
        {loadingTop && <div>Loading...</div>}
        {errorTop && <div style={{ color: 'crimson' }}>{errorTop}</div>}
        {!loadingTop && !errorTop && <BookList books={topRated} />}
      </Section>

      <Section title="AI Recommendations">
        {!user?.authenticated && (
          <div style={{ color: '#666' }}>
            Login to get AI-based recommendations based on your favourites.
          </div>
        )}
        {user?.authenticated && (
          <>
            {loadingAi && <div>Loading...</div>}
            {errorAi && <div style={{ color: 'crimson' }}>{errorAi}</div>}
            {!loadingAi && !errorAi && <AiList items={aiRecs} />}
          </>
        )}
      </Section>
    </div>
  );
};

export default Recommendations;
