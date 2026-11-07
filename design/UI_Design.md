# UI Design and Style Guide — Bookmate

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 2026-11 | Project team | First version: 15 screens, style guide, requirement coverage |

## 1. Purpose
This document describes the user interface of Bookmate: the visual style, the reusable components, the screens, and which requirements each screen covers. It is based on the SRS and is the input for the Implementation phase (Jetpack Compose).

The screens are drawn in Figma at a phone size of 390 x 844. Book titles in the screens are public-domain books, and the user name "Alex Reader" is sample data. No copyrighted book covers are used; covers are shown as plain color blocks.

## 2. Design Principles
1. **Simple and calm:** few elements per screen, large touch targets, plenty of space.
2. **Reading first:** the Reader screen has no bottom navigation, so the text has more room.
3. **AI is helpful, not hidden:** AI actions are easy to find (AI tab, Summarize button, floating AI button in the Reader), and every AI answer carries the notice "AI-generated, may be inaccurate".
4. **Consistent:** one color profile, one font, and the same component shapes on every screen.
5. **Android conventions:** the look follows Material Design 3 (rounded shapes, bottom navigation, Roboto font).

The visual style was inspired by a book-tracking app screenshot chosen by the team: a cream-to-olive gradient, dark green accents, white rounded cards, and a floating pill navigation bar.

## 3. Color Profile

| Role | Use | Hex |
|---|---|---|
| Background gradient (top to bottom) | Screen background | `#FAF2DE`, `#EADCBB`, `#C0C0A7`, `#9CA793`, `#78866E` |
| Primary | Titles, main buttons, active chips | `#576B48` |
| On primary | Text and icons on primary | `#FAF2DE` |
| Dark green text | Card titles, large numbers | `#324B21` |
| Surface | Cards (shown at about 90% opacity) | `#FBF9F4` |
| Input background | Text fields | `#FFFFFF` |
| Outline | Field borders | `#DAD8CE` |
| Secondary container | Secondary buttons, inactive chips, dividers | `#E3E3D9` |
| Navigation container | Bottom navigation bar | `#C3D3BA` |
| Navigation active | Active tab background | `#ACBDA1` |
| Text | Main text and icons | `#0F1909` |
| Secondary text | Hints, captions | `#615E64` |
| Book cover placeholders | Plain blocks instead of real covers | `#D8CFB4`, `#B9C6AC`, `#A9B39B` |

Color roles map to the Material 3 theme in Compose (primary, onPrimary, surface, outline, secondaryContainer, onSurface, onSurfaceVariant). Text contrast on the gradient is checked during testing (NFR-01).

## 4. Typography
Font: **Roboto**.

| Style | Size | Weight | Used for |
|---|---|---|---|
| Screen title | 34 (30 or 28 on screens with a back button) | Bold | Page names, in primary color |
| Card title | 18 to 22 | Bold | Section names in cards |
| Body | 14 to 17 | Regular | Text, book titles (bold) |
| Button label | 16 to 17 | Bold | Buttons |
| Caption | 11 to 13 | Regular | Hints, notices, navigation labels |

## 5. Layout and Components

**Layout:** 390 x 844 screen, 24 px side margin, 12 to 16 px gap between cards.


| Component | Rule |
|---|---|
| Card | White-cream surface, corner radius 24 to 28 |
| Primary button | Height 52, fully rounded, primary color, bold light text |
| Secondary button | Height 52, fully rounded, `#E3E3D9` or an outlined primary border |
| Text field | Height 52, radius 16, white fill, outline border, hint in secondary text |
| Chip | Height 28 to 44, fully rounded; selected = primary color, not selected = surface |
| Book cover | Rounded corners (8 to 14), placeholder block |
| Bottom navigation | Floating pill, height 72, 5 tabs: Home, Search, AI, My Books, Profile; active tab has a darker round background and bold label |
| Floating AI button | Round, diameter 64, primary color, sparkle icon, soft halo; bottom right of the Reader |
| AI sheet | Bottom sheet with rounded top, drag handle, 4 option buttons, and the AI notice |
| Back button | Round surface button, top left |

## 6. Navigation Structure

- **Main tabs (bottom navigation):** Home, Search, AI, My Books, Profile.
- **Without bottom navigation:** Login, Register, Onboarding, Book Detail, Reader, ISBN Scan, Settings, Admin Add Book.
- **Main flows:**
  1. Register or log in → Onboarding (first time) → Home.
  2. Home or Search → Book Detail → Borrow, Reserve, or Read.
  3. Book Detail → Read → Reader → floating AI button → AI sheet → AI Chat.
  4. AI tab → Book Chat (summary and questions) or Recommend.
  5. Search or Admin → ISBN Scan → book information.
  6. Profile → Saved, Finished, Notifications, Settings, Log out.
  7. Admin → Add a book (by hand or fill by ISBN).

## 7. Screen List and Requirement Coverage

| ID | Screen | User | Requirements covered |
|---|---|---|---|
| SCR-01 | Login | All | FR-02, FR-05 |
| SCR-02 | Register | All | FR-01 |
| SCR-03 | Onboarding (pick genres) | Member | FR-43 |
| SCR-04 | Home | Member | FR-08, FR-21, FR-37 |
| SCR-05 | Search | Member | FR-06, FR-07, FR-10, FR-12 |
| SCR-06 | Book Detail | Member | FR-09, FR-11, FR-12, FR-18, FR-24, FR-36, FR-44 |
| SCR-07 | Reader with floating AI button | Member | FR-36, FR-37, FR-38 |
| SCR-08 | Reader AI sheet | Member | FR-28, FR-39, FR-40, FR-41 |
| SCR-09 | AI Chat (Book Chat) | Member | FR-24, FR-25, FR-27, FR-28 |
| SCR-10 | My Books | Member | FR-12, FR-14, FR-15 |
| SCR-11 | Profile | Member | FR-03, FR-42 |
| SCR-12 | Finished / Saved | Member | FR-44, FR-45 |
| SCR-13 | Settings | Member | FR-46 |
| SCR-14 | ISBN Scan | Member, Admin | FR-19, FR-20 |
| SCR-15 | Admin: Add a book | Admin | FR-31, FR-35 |

Non-functional requirements shown in the design: NFR-01 (simple, Material Design 3 style) and NFR-09 (AI notice on SCR-08, SCR-09, and in Settings).

## 8. Screens Still To Design
These requirements are not yet fully covered by a screen. They will be added in version 1.1 in the same style.

| Screen or state | Requirements |
|---|---|
| Recommend tab (AI suggestions with reasons) | FR-21, FR-22, FR-23 |
| Chapter and page-range picker for summaries | FR-25, FR-26 |
| Notifications | FR-47 |
| Edit profile and Forgot password | FR-03, FR-04 |
| Loan history and reservation cancel | FR-13, FR-16 |
| Admin: book list (edit, remove), active loans, record return | FR-32, FR-33, FR-34 |
| States: loading, empty result, no internet, AI error | FR-10, FR-30, NFR-04 |

## 9. AI Interface Rules
- Every AI answer shows "AI-generated, may be inaccurate".
- The floating AI button appears only in the Reader, and only for books with full text (FR-38, UC-10).
- "Recap so far" is marked "(no spoilers)" and uses only the text up to the reader's position (FR-41).
- While the AI answers, a loading indicator is shown. If the AI fails, a clear message is shown and the Member can keep reading (FR-30).
- Quick question chips (for example "Who is Mr. Bingley?") help users who do not know what to ask.
- Settings has a "How Bookmate AI works" page that explains the limits of AI answers.


## 10. Assumptions
- The Reader works only for books that have full text (public-domain books).
- Loan period shown in the screens (14 days) follows assumption A-01 of the SRS.
- Sample data in the screens (titles, counts, user name) is only for design and is replaced by real data in the app.
