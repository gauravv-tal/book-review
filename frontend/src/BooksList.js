import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from './AuthContext';

const TextInput = ({ label, name, value, onChange, placeholder }) => (
  <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
    <label htmlFor={name} style={{ fontSize: 12, color: '#555' }}>{label}</label>
    <input
      id={name}
      name={name}
      type="text"
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      style={{ padding: 8, borderRadius: 6, border: '1px solid #ddd' }}
    />
  </div>
);

const BooksList = () => {
  const { apiCall } = useAuth();

  // Filters and pagination state
  const [filters, setFilters] = useState({ title: '', author: '', genre: '', year: '' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);

  // Data state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [pageData, setPageData] = useState({ content: [], totalPages: 0, totalElements: 0, number: 0 });

  const params = useMemo(() => {
    const p = new URLSearchParams();
    if (filters.title) p.set('title', filters.title);
    if (filters.author) p.set('author', filters.author);
    if (filters.genre) p.set('genre', filters.genre);
    if (filters.year) p.set('year', filters.year);
    p.set('page', page);
    p.set('size', size);
    return p.toString();
  }, [filters, page, size]);

  const fetchBooks = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await apiCall(`/books?${params}`);
      if (!res.ok) throw new Error(`Failed: ${res.status}`);
      const data = await res.json();
      setPageData(data);
    } catch (e) {
      setError(e.message || 'Failed to load books');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBooks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params]);

  const onFieldChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const onSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchBooks();
  };

  const clearFilters = () => {
    setFilters({ title: '', author: '', genre: '', year: '' });
    setPage(0);
  };

  const canPrev = page > 0;
  const canNext = pageData && page < (pageData.totalPages - 1);

  return (
    <div style={{ padding: 20, maxWidth: 1000, margin: '0 auto' }}>
      <h2 style={{ marginBottom: 16 }}>Books</h2>

      {/* Search / Filters */}
      <form onSubmit={onSearch} style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, alignItems: 'end', marginBottom: 16 }}>
        <TextInput label="Title" name="title" value={filters.title} onChange={onFieldChange} placeholder="e.g. ring" />
        <TextInput label="Author" name="author" value={filters.author} onChange={onFieldChange} placeholder="e.g. tolkien" />
        <TextInput label="Genre" name="genre" value={filters.genre} onChange={onFieldChange} placeholder="e.g. fantasy" />
        <TextInput label="Year" name="year" value={filters.year} onChange={onFieldChange} placeholder="e.g. 1954" />
        <div style={{ gridColumn: 'span 4', display: 'flex', gap: 8 }}>
          <button type="submit" style={{ padding: '8px 12px', borderRadius: 6, border: 'none', background: '#667eea', color: 'white' }}>Search</button>
          <button type="button" onClick={clearFilters} style={{ padding: '8px 12px', borderRadius: 6, border: '1px solid #ddd', background: 'white' }}>Clear</button>
          <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ color: '#666', fontSize: 12 }}>Page size</span>
            <select value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}>
              {[6,12,20,40].map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
        </div>
      </form>

      {/* Status */}
      {loading && <div>Loading...</div>}
      {error && <div style={{ color: 'red' }}>{error}</div>}

      {/* Grid */}
      {!loading && pageData?.content?.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 16 }}>
          {pageData.content.map(b => (
            <Link key={b.id} to={`/books/${b.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
              <div style={{ border: '1px solid #eee', borderRadius: 10, overflow: 'hidden', background: 'white', display: 'flex', flexDirection: 'column' }}>
                {b.coverUrl ? (
                  <img src={b.coverUrl} alt={b.title} style={{ width: '100%', height: 220, objectFit: 'cover', background: '#fafafa' }} />
                ) : (
                  <div style={{ width: '100%', height: 220, background: '#f2f2f2', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#888' }}>No Cover</div>
                )}
                <div style={{ padding: 12 }}>
                  <div style={{ fontWeight: 600, marginBottom: 6 }}>{b.title}</div>
                  <div style={{ fontSize: 13, color: '#555' }}>{b.author}{b.year ? ` • ${b.year}` : ''}</div>
                  {b.genres && <div style={{ marginTop: 8, fontSize: 12, color: '#777' }}>{b.genres}</div>}
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && pageData?.totalPages > 0 && (
        <div style={{ display: 'flex', gap: 8, alignItems: 'center', justifyContent: 'center', marginTop: 16 }}>
          <button disabled={!canPrev} onClick={() => setPage(p => Math.max(0, p - 1))} style={{ padding: '6px 10px' }}>Prev</button>
          <span style={{ fontSize: 13, color: '#555' }}>Page {page + 1} of {pageData.totalPages}</span>
          <button disabled={!canNext} onClick={() => setPage(p => p + 1)} style={{ padding: '6px 10px' }}>Next</button>
        </div>
      )}

      {/* Empty state */}
      {!loading && !error && pageData?.content?.length === 0 && (
        <div style={{ textAlign: 'center', color: '#666', marginTop: 24 }}>No books found.</div>
      )}
    </div>
  );
};

export default BooksList;
