# PROJECT_CONTEXT.md — Bookmate

> For AI assistants: read this whole file before helping. It is the single source of truth
> for the project. Follow the rules in section 9. Do not contradict decisions in section 6
> unless the user explicitly asks to change them.

## 1. Project summary
- **Name:** Bookmate
- **What:** A smart library Android app with AI integration.
- **Context:** University semester final project, done by a team. Must follow the
  academic software engineering (SDLC) approach and produce formal documents.
- **Platform / language:** Android, Kotlin, Android Studio.

## 2. Process and document order (SDLC)
Repo folders:

```
Bookmate/
├── PROJECT_CONTEXT.md
├── Planning/
│   ├── System Request          (Chapter 1: Project Initiation)
│   ├── Feasibility Analysis    (Chapter 1: Project Initiation)
│   ├── Staffing Plan           (Chapter 2: Project Management)
│   ├── Project Standards       (Chapter 2: Project Management)
│   └── Risk Assessment         (Chapter 2: Project Management)
├── Analysis/                   (SRS goes here)
├── Design/                     (UI, architecture, database)
└── Implementation/             (code)
```

Order of work: System Request -> Feasibility -> Staffing Plan -> Project Standards
-> Risk Assessment -> SRS -> Design -> Implementation.

## 3. Core features
1. **Library basics:** register/login, search and browse books (title, author, genre),
   book detail (availability), borrow/reserve, my borrowed books (due date, renew).
2. **AI recommendation assistant:** suggests books to the user.
3. **AI book chatbot:** the user picks a whole book, a chapter, or a page range and asks
   for a summary, then can ask follow-up questions about that part.
4. **ISBN barcode scan** (ML Kit, on-device) to look up a book.

## 4. Book text source (decided)
- Metadata and descriptions: Google Books API and/or Open Library API.
- Full text for summaries: public-domain books only (Project Gutenberg). A small set
  of about 5-10 books bundled or stored for the demo.
- No librarian upload feature (decided not to include).
- Long books must be split by chapter before being sent to the AI.
- Every AI summary shows: "AI-generated, may be inaccurate".

## 5. Tech choices
- UI: Jetpack Compose, Material Design 3, bottom navigation (5 tabs: Home, Search, AI,
  My Books, Profile).
- AI: Gemini API for the assistant and chatbot (Firebase AI Logic SDK or direct API;
  verify the current official docs before coding).
- ML Kit Barcode Scanning for ISBN.
- Simple rule-based recommendation (genre/tag matching) is acceptable.
- Version control: GitHub.

## 6. Decisions log (do not change without asking the team)
| Decision | Detail |
|---|---|
| App name | Bookmate |
| Language/IDE | Kotlin, Android Studio |
| AI features | Recommendation assistant + book summary chatbot |
| Book text | Metadata API + public-domain books only |
| UI plan | DRAFT only; final screens come from the SRS, stored in Design/ |

## 7. Draft screen list (to be finalized after SRS)
Splash, Login/Register, Home, Search/Browse, Book Detail, Reader/Chapter Select,
AI tab (Recommend + Book Chat), My Books (borrowed/reserved), Barcode Scan, Profile.

## 8. Current status
- [x] Repo and folders created
- [x] Idea, features, AI approach decided
- [ ] System Request
- [ ] Feasibility Analysis
- [ ] Staffing Plan
- [ ] Project Standards
- [ ] Risk Assessment
- [ ] SRS
- [ ] Design
- [ ] Implementation

## 9. Rules for the AI assistant
1. Read this file first. Stay consistent with it.
2. Never put API keys in code that gets committed. Keys go in `local.properties`
   (gitignored). Remind the user if they paste a key.
3. Use academic software engineering terms and structure for documents.
4. Do not invent facts about the team, deadline, or grading. Ask the user, or write
   "TODO".
5. If the user makes a new decision, tell them to update sections 6 and 8 of this file
   and commit it, so teammates' AIs stay in sync.
6. Explain things simply. Give small steps, not a huge dump.

## 10. Team (fill in)
| Name | Role | GitHub |
|---|---|---|
| TODO | TODO | TODO |

Final submission date: TODO
Course / instructor: TODO
