# Sequence Diagrams — Bookmate

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 2026-11 | Project team | First version |

## 1. Purpose
These diagrams show how the parts of Bookmate work together for the main use cases of the SRS. They follow the System Architecture (layers and components), the Database Design (transactions and collections), and the AI Integration Design (text selection, stored summaries, error handling). They are the guide for writing the code and the test cases.

GitHub shows the diagrams automatically. If a viewer does not, paste the diagram text into the Mermaid Live Editor (mermaid.live).

## 2. Diagram List

| Diagram | Use case | Requirements |
|---|---|---|
| 3.1 Register and onboarding | UC-01 | FR-01, FR-43 |
| 3.2 Search and open a book | UC-02 | FR-06, FR-07, FR-09, FR-10 |
| 3.3 Borrow a book | UC-03 | FR-11, FR-18 |
| 3.4 Reserve a book | UC-04 | FR-12 |
| 3.5 Record a return (Admin) | UC-09 | FR-33 |
| 3.6 AI help while reading (Recap so far) | UC-10 | FR-38 to FR-41, FR-28, FR-30 |
| 3.7 AI book summary | UC-05 | FR-24, FR-29 |
| 3.8 AI recommendation | UC-06 | FR-21 to FR-23 |
| 3.9 Scan an ISBN | UC-07 | FR-19, FR-20, FR-35 |

Not drawn: login and logout, saved books, settings, notifications, and Admin add or edit book (UC-08). They are simple one-step actions on one collection, described in the Database Design.

## 3. Diagrams

### 3.1 Register and onboarding (UC-01)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant S as Register screen
  participant VM as AuthViewModel
  participant AR as AuthRepository
  participant FA as Firebase Authentication
  participant FS as Firestore
  M->>S: Enter name, email, password
  S->>VM: register(name, email, password)
  VM->>AR: register(...)
  AR->>FA: Create account
  alt Account created
    FA-->>AR: User ID
    AR->>FS: Create users/uid with role member
    FS-->>AR: Saved
    AR-->>VM: Success
    VM-->>S: Go to Onboarding
    M->>S: Pick favorite genres
    S->>VM: saveGenres(list)
    VM->>FS: Update favoriteGenres and onboardingDone
    VM-->>S: Go to Home
  else Email used or weak password
    FA-->>AR: Error
    AR-->>VM: Error with reason
    VM-->>S: Show message
  end
```

### 3.2 Search and open a book (UC-02)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant S as Search screen
  participant VM as SearchViewModel
  participant BR as BookRepository
  participant FS as Firestore
  M->>S: Open Search tab
  S->>VM: load()
  VM->>BR: getCatalog()
  BR->>FS: Read books
  FS-->>BR: Book list
  BR-->>VM: Books
  M->>S: Type a query and set filters
  S->>VM: search(query, filters)
  VM->>VM: Filter on the device by title, author, genre, availability
  alt Results found
    VM-->>S: Show list
    M->>S: Tap a book
    S->>S: Open Book Detail
  else No result
    VM-->>S: Show empty-result message
  end
```

### 3.3 Borrow a book (UC-03)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant S as Book Detail screen
  participant VM as BookDetailViewModel
  participant UC as BorrowBook use case
  participant LR as LoanRepository
  participant FS as Firestore
  M->>S: Tap Borrow
  S->>VM: borrow(bookId)
  VM->>UC: execute(bookId)
  UC->>LR: hasOverdueLoan(userId)
  LR->>FS: Query active loans past due date
  FS-->>LR: Result
  alt Member has an overdue loan
    LR-->>UC: Yes
    UC-->>VM: Error overdue
    VM-->>S: Show message
  else No overdue loan
    UC->>LR: borrow(bookId)
    LR->>FS: Start transaction
    Note over LR,FS: Check availableCopies above 0 and activeLoanCount below 3
    alt Rules pass
      LR->>FS: Create loan, availableCopies minus 1, activeLoanCount plus 1
      FS-->>LR: Committed
      LR-->>UC: Loan with due date
      UC-->>VM: Success
      VM-->>S: Show due date
    else No copy or limit reached
      FS-->>LR: Rule failed
      LR-->>UC: Error
      UC-->>VM: Error
      VM-->>S: Show message, offer Reserve if no copy
    end
  end
```

### 3.4 Reserve a book (UC-04)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant S as Book Detail screen
  participant VM as BookDetailViewModel
  participant LR as LoanRepository
  participant FS as Firestore
  M->>S: Tap Reserve
  S->>VM: reserve(bookId)
  VM->>LR: reserve(bookId)
  LR->>FS: Read the book and check no active loan and no waiting reservation for it
  FS-->>LR: Result
  alt Book has no available copy
    LR->>FS: Create reservation with status waiting
    FS-->>LR: Saved
    LR-->>VM: Success
    VM-->>S: Show You are in the queue
  else Copy is available now
    LR-->>VM: Error use Borrow instead
    VM-->>S: Show message
  end
```

### 3.5 Record a return, by Admin (UC-09)
```mermaid
sequenceDiagram
  autonumber
  actor A as Admin
  participant S as Admin loans screen
  participant VM as AdminViewModel
  participant LR as LoanRepository
  participant FS as Firestore
  A->>S: Choose a loan and tap Record return
  S->>VM: recordReturn(loanId)
  VM->>LR: recordReturn(loanId)
  LR->>FS: Find oldest waiting reservation for the book
  FS-->>LR: Reservation or none
  LR->>FS: Start transaction
  LR->>FS: Loan returned with returnDate, availableCopies plus 1, activeLoanCount minus 1
  opt A waiting reservation exists
    LR->>FS: Reservation ready, notification reservation_ready for that user
  end
  FS-->>LR: Committed
  LR-->>VM: Success
  VM-->>S: Show updated list
```

### 3.6 AI help while reading: Recap so far (UC-10)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant R as Reader screen
  participant VM as AiChatViewModel
  participant UC as GetRecap use case
  participant TS as TextSelector
  participant CR as ChapterRepository
  participant PB as PromptBuilder
  participant AI as AiRepository and AiService
  M->>R: Tap floating AI button
  R-->>M: Show AI sheet
  M->>R: Choose Recap so far
  R->>VM: recap(bookId, chapter, paragraph)
  VM->>UC: execute(position)
  UC->>CR: Load stored summaries of earlier chapters
  CR-->>UC: Summaries
  UC->>CR: Load current chapter paragraphs
  CR-->>UC: Paragraphs
  UC->>TS: Keep paragraphs up to the position only
  TS-->>UC: Selected text
  UC->>PB: Build recap prompt
  PB-->>UC: Prompt
  UC->>AI: generateStream(prompt)
  VM-->>R: Show loading state
  alt Answer arrives
    AI-->>VM: Text chunks
    VM-->>R: Show text with notice AI-generated, may be inaccurate
  else Error, timeout or limit
    AI-->>VM: Error
    VM-->>R: Show error message, Member keeps reading
  end
```

### 3.7 AI book summary (UC-05)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant S as Book Detail or AI Chat screen
  participant VM as AiChatViewModel
  participant UC as GetBookSummary use case
  participant BR as BookRepository
  participant AI as AiRepository and AiService
  M->>S: Tap Summarize with AI
  S->>VM: summarize(bookId)
  VM->>UC: execute(bookId)
  UC->>BR: Get book
  BR-->>UC: Book
  alt Book has a stored bookSummary
    UC-->>VM: Stored summary
    VM-->>S: Show summary with AI notice
  else Book has full text but no stored summary
    UC->>AI: Summarize chapter by chapter, then merge
    AI-->>UC: Summary
    UC-->>VM: Summary
    VM-->>S: Show summary with AI notice
  else Book has no full text
    UC->>AI: Summarize the description only
    AI-->>UC: Summary starting with the description-only notice
    UC-->>VM: Summary
    VM-->>S: Show summary, hide chapter options
  end
```

### 3.8 AI recommendation (UC-06)
```mermaid
sequenceDiagram
  autonumber
  actor M as Member
  participant S as AI tab, Recommend
  participant VM as RecommendViewModel
  participant UC as GetRecommendations use case
  participant BR as BookRepository
  participant AI as AiRepository and AiService
  M->>S: Open Recommend, type a request or use default
  S->>VM: recommend(request)
  VM->>UC: execute(request)
  UC->>BR: Get catalog, favorite genres, recent history
  BR-->>UC: Data
  UC->>AI: Prompt with catalog list and reader profile
  alt Valid JSON answer
    AI-->>UC: Book IDs with reasons
    UC->>UC: Keep only IDs that exist in the catalog, remove duplicates
    UC-->>VM: Recommendations
  else Bad answer or error
    UC->>UC: Use rule-based list by genre match
    UC-->>VM: Rule-based recommendations
  end
  VM-->>S: Show books with reasons and AI notice
  M->>S: Tap a recommended book
  S->>S: Open Book Detail
```

### 3.9 Scan an ISBN (UC-07)
```mermaid
sequenceDiagram
  autonumber
  actor U as Member or Admin
  participant S as Scan screen
  participant VM as ScanViewModel
  participant SR as ScanRepository
  participant BR as BookRepository
  participant WEB as Google Books and Open Library
  U->>S: Open Scan
  S->>SR: Start ML Kit scanner
  SR-->>VM: ISBN
  VM->>BR: findByIsbn(isbn)
  BR->>BR: Search catalog in Firestore
  alt Found in catalog
    BR-->>VM: Book
    VM-->>S: Open Book Detail
  else Not in catalog
    BR->>WEB: Look up ISBN
    alt Found online
      WEB-->>BR: Book information
      BR-->>VM: External book
      VM-->>S: Show information, Admin can add to catalog
    else Not found
      VM-->>S: Show Book not found, offer typing the ISBN
    end
  end
```

## 4. Notes for Implementation
- Every ViewModel call ends in a `UiState` of Success or Error, so each diagram's error branch becomes an error message on screen (FR-30, NFR-04).
- Diagram 3.3 and 3.5 are the two Firestore transactions of the project; their test cases must cover the failure branch.
- Diagram 3.6 is the most important AI test: the text sent must never include paragraphs after the reader's position (FR-41).
