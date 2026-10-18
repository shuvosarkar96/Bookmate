# System Request — Bookmate

| | |
|---|---|
| **Project name** | Bookmate: Smart Library App with AI Integration |
| **Project sponsor** | Course Instructor |
| **Project team** | Sarkar Shuvo (Lead), Karmaker Sudipta, Sadik Md Nafis |
| **Project period** | October 12, 2026 to around mid-December 2026 |
| **Date** | October 2026 |

## 1. Business Need
Students and library users often struggle to find suitable books quickly, and reading a whole book just to decide whether it is useful takes a lot of time. Traditional library systems offer only basic search and borrowing. They do not guide users toward books they may like, and they do not help users understand book content quickly.

Bookmate is a mobile library application that combines normal library services with AI support. It helps users find books, manage borrowing, and get AI-generated summaries of books, so that they can decide and learn faster.

## 2. Business Requirements
The system should allow users to:
1. Register, log in, and manage a profile.
2. Search and browse books by title, author, and genre.
3. View book details and availability.
4. Borrow and reserve books, and view their borrowed books with due dates.
5. Scan a book's ISBN barcode to find the book.
6. Get book recommendations from an AI assistant.
7. Ask an AI chatbot for a summary of a whole book, a chapter, or a page range, and ask follow-up questions about it.

## 3. Business Value

**Tangible value (expected, to be measured in testing)**
- Less time to find a suitable book compared with manual searching.
- Fewer routine questions to library staff (availability, due dates), because users can check them in the app.
- Low development cost, using free tools and free-tier services (Android Studio, Kotlin, ML Kit, public APIs).

**Intangible value**
- Better user experience and higher interest in reading and library use.
- Quicker understanding of books through AI summaries.
- Team gains practical experience in Android development, AI integration, and professional software engineering documentation.

## 4. Special Issues or Constraints
- **Platform:** Android only, built with Kotlin in Android Studio.
- **Book text:** Full-text summaries are limited to public-domain books (Project Gutenberg) because of copyright. Other books use metadata and descriptions only.
- **AI accuracy:** AI summaries can be wrong, so every summary shows the notice "AI-generated, may be inaccurate".
- **AI service limits:** The AI API has free-tier usage limits and needs an internet connection.
- **Security:** API keys must not be stored in the public repository.
- **Time and team:** The project must be completed by a three-member student team within the semester, by around mid-December 2026.
