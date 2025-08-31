import csv, requests

API_KEY = ''
GENRES = ["kids", "technology", "self help", "current affairs", "history"]
RESULTS_PER_GENRE = 40
ITEMS_PER_PAGE = 40
BASE_URL = "https://www.googleapis.com/books/v1/volumes"

def fetch_books_for_genre(genre):
    books = []
    for start in range(0, RESULTS_PER_GENRE, ITEMS_PER_PAGE):
        params = {
            'q': f"subject:{genre}",
            'printType': 'books',
            'langRestrict': 'en',
            'startIndex': start,
            'maxResults': ITEMS_PER_PAGE,
            'key': API_KEY
        }
        resp = requests.get(BASE_URL, params=params)
        data = resp.json()
        items = data.get('items', [])
        for item in items:
            info = item.get('volumeInfo', {})
            books.append({
                'title': info.get('title', ''),
                'author': ', '.join(info.get('authors', [])),
                'description': info.get('description', ''),
                'cover_url': info.get('imageLinks', {}).get('thumbnail', ''),
                'genres': ', '.join(info.get('categories', [])),
                'year': info.get('publishedDate', '')[:4]
            })
    return books

all_books = []
for genre in GENRES:
    all_books.extend(fetch_books_for_genre(genre))

# Deduplicate by title + author
seen = set()
unique_books = []
for b in all_books:
    key = (b['title'], b['author'])
    if key not in seen:
        seen.add(key)
        unique_books.append(b)

with open('books.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.DictWriter(f, fieldnames=['title','author','description','cover_url','genres','year'])
    writer.writeheader()
    for book in unique_books[:200]:
        writer.writerow(book)