# Test Plan — Bookmate

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 2026-11 | Project team | First version, 61 test cases |

## 1. Purpose and Scope
This plan describes how the team checks that Bookmate meets the SRS. It defines test levels, environment, data, roles, rules for bugs, and one test case for every requirement (TC-01 to TC-61, as reserved in the RTM).

**In scope:** all functional requirements (FR-01 to FR-47) and non-functional requirements (NFR-01 to NFR-14) on Android.

**Out of scope:** iOS, load testing with many users, penetration testing, a full accessibility audit, and automatic scoring of AI answer quality (AI answers are checked by sampling, section 8).

## 2. Test Levels

| Level | What is tested | Tool | Owner |
|---|---|---|---|
| Unit (U) | Use cases, ViewModels, TextSelector, PromptBuilder, recommendation checks, loan rules | JUnit and a mocking library | Shuvo, each developer for own code |
| Emulator (E) | Firestore security rules and the borrow and return transactions | Firebase Local Emulator Suite | Sudipta |
| UI (UI) | Key flows: login, search, borrow, Reader AI sheet | Compose UI tests (optional, if time allows) | Shuvo |
| Manual (M) | All screens and flows on a real phone and the emulator | Test cases in section 9 | Nafis, all members |
| Review (R) | Code, repository, and documents against the Project Standards | Checklist | Nafis, Shuvo |
| Acceptance | One full demo scenario before submission | Section 7 | Whole team |

Each developer also tests their own work before opening a pull request (Project Standards, section 6).

## 3. Test Environment
- At least one real Android phone with Android 8.0 or higher, and the Android emulator with Google Play services (needed for the barcode scanner).
- A test Firebase setup (emulator or a separate test project), never the real data.
- AI calls in tests use the real AI service in small numbers, to save the free tier (risk R-02). Unit tests use a fake `AiService`.
- Bugs and test progress are tracked in GitHub Issues.

## 4. Test Data

**Accounts**
| Account | Purpose |
|---|---|
| Admin | Admin tests |
| Member A (new) | Registration, onboarding, first borrow |
| Member B (3 active loans) | Loan limit (DA-01), renew |
| Member C (one overdue loan) | Borrow blocked by overdue loan |

**Books**
| Book | Purpose |
|---|---|
| 2 or more public-domain books with full text and checked stored summaries | Reader, summaries, recap |
| 1 book with full text and no stored summary | Live summary path |
| 2 or more books with description only | Description-based summary (FR-29) |
| 1 book with 0 available copies | Reserve, borrow blocked |
| 1 book with 1 available copy | Last copy, return flow |

## 5. Roles

| Person | Responsibility |
|---|---|
| Sadik Md Nafis | Owns this plan, runs manual and system tests, writes bug reports, updates the RTM results |
| Karmaker Sudipta | Emulator tests for rules and transactions, test data and seed script |
| Sarkar Shuvo | Unit tests for logic and AI parts, fixes critical bugs, approves the release |

## 6. Process Rules

**Bug report (GitHub Issue):** title, requirement ID and test case ID, steps, expected result, actual result, device and Android version, screenshot.


**Severity**
| Level | Meaning | Example |
|---|---|---|
| Critical | Crash, data loss, security problem, or AI sends spoiler text | App crashes on login; Member sees another user's loans |
| Major | A requirement does not work | Borrow does not reduce available copies |
| Minor | Small visual or text problem | Label cut off |

**Test rounds**
| Round | Content | When |
|---|---|---|
| Round 1 | All **Must** test cases | After the core features are merged |
| Round 2 | All **Should** test cases and the non-functional cases marked Should | After the Reader and AI features are merged |
| Round 3 | All **Could** test cases, then a full regression run of Round 1 | Last weeks before submission |

**Feature freeze:** one week before submission no new features are added; only bug fixes and tests.

**Entry criteria for a test:** the feature is merged into `main`, the app builds, and the test data is loaded.

**Exit criteria (ready to submit):** all Must test cases pass, there is no open Critical or Major bug, and every failed Should or Could test case has a written reason or a Minor bug.

## 7. Acceptance Scenario (final demo)
1. Register a new Member and pick genres.
2. Search for a book, open it, and borrow it.
3. Open a full-text book in the Reader and use the AI button for a recap.
4. Ask the AI a question and see the AI notice.
5. Ask for a recommendation.
6. Log in as Admin and record a return.

## 8. AI Test Method
- For each demo book, choose three reading positions (start, middle, near the end).
- Run each AI task at each position **three times** and compare with the book text.
- **Pass** only if all three answers: stay grounded in the text, contain no event after the position, and show the AI notice. A different wording each time is acceptable.
- Record the result per book and position in the test report.

## 9. Test Cases

Type: **U** unit, **E** emulator, **UI** UI test, **M** manual, **R** review. Priority follows the requirement (Must, Should, Could).

### 9.1 Functional

| ID | Req | Priority | Test | Expected result | Type |
|---|---|---|---|---|---|
| TC-01 | FR-01 | Must | Register with a valid name, email, and password; then try an email already used and a short password. | Account created and Onboarding opens; the errors show a clear message. | M, E |
| TC-02 | FR-02 | Must | Log in with correct data; log in with a wrong password; log out. | Home opens; wrong password shows an error; after logout the Login screen shows. | M, UI |
| TC-03 | FR-03 | Should | Change name and favorite genres in the profile and restart the app. | New values are saved and shown. | M |
| TC-04 | FR-04 | Could | Use "Forgot password" with a registered and an unknown email. | A reset email is sent for the registered one; a clear message for the unknown one. | M |
| TC-05 | FR-05 | Must | Log in as Member, then as Admin. | Member sees no Admin screens; Admin sees Admin screens. | M, E |
| TC-06 | FR-06 | Must | Search by title, by author, and by ISBN. | Matching books are shown for each search. | M, U |
| TC-07 | FR-07 | Should | Filter by genre, and by "Available". | Only matching books are listed. | M, U |
| TC-08 | FR-08 | Should | Open Home. | New and popular books are shown. | M |
| TC-09 | FR-09 | Must | Open a book. | Cover or placeholder, title, author, genre, description, and availability are shown. | M |
| TC-10 | FR-10 | Must | Search for text that matches nothing. | A clear "no result" message; no crash. | M, U |
| TC-11 | FR-11 | Must | Borrow an available book as Member A. | A loan is created with due date 14 days later; available copies drop by 1; the book is in My Books. | M, E |
| TC-12 | FR-12 | Should | Reserve the book with 0 copies. | A reservation with status waiting is created; My Books shows "In the queue". | M, E |
| TC-13 | FR-13 | Should | Cancel the reservation. | Status becomes cancelled; it leaves the list. | M, E |
| TC-14 | FR-14 | Must | Open My Books as Member B. | Current loans with due dates and reservations are shown. | M |

| TC-15 | FR-15 | Should | Renew a loan; renew it a second time. | First renewal moves the due date by 14 days; the second is refused with a message. | M, E |
| TC-16 | FR-16 | Could | Open the loan history after a return. | The returned loan appears. | M |
| TC-17 | FR-17 | Could | Use a loan that is due soon (test dates). | A reminder notification appears; with reminders off, none appears. | M |
| TC-18 | FR-18 | Must | Borrow a book with 0 copies; as Member B (3 loans) borrow another book; as Member C (overdue loan) borrow. | Each attempt is refused with a clear message; for 0 copies Reserve is offered. | M, E, U |
| TC-19 | FR-19 | Could | Scan the barcode of a real book. | The ISBN is read. | M |
| TC-20 | FR-20 | Could | Scan an ISBN in the catalog; one only online; one unknown. | Book Detail opens; external information shows; "Book not found" shows. | M |
| TC-21 | FR-21 | Should | Open Home as a Member with favorite genres. | Recommended books match those genres. | M, U |
| TC-22 | FR-22 | Should | Ask for "a short mystery book". | Recommendations come only from the catalog. | M, U |
| TC-23 | FR-23 | Could | Check each recommendation. | Each has a short reason and opens its Book Detail. | M |
| TC-24 | FR-24 | Must | Ask for a summary of a full-text book. | A summary is shown with the AI notice. | M |
| TC-25 | FR-25 | Should | Choose a chapter for the summary. | The chapter summary is shown. | M |
| TC-26 | FR-26 | Could | Choose a page range. | The summary matches only that range. | M, U |
| TC-27 | FR-27 | Should | Ask a follow-up question; ask one whose answer is only in a later chapter. | The first is answered from the text; the second gets "I cannot find this in the part you have read." | M |
| TC-28 | FR-28 | Must | Use summary, recap, explain, question, and recommendation. | Every AI answer shows "AI-generated, may be inaccurate". | M |
| TC-29 | FR-29 | Must | Ask for a summary of a book without full text. | The summary starts with the description-only notice; chapter and page options are hidden. | M |
| TC-30 | FR-30 | Must | Watch an AI request; then turn the internet off and ask again. | A loading state shows; then a clear error message; the app stays usable. | M, U |
| TC-31 | FR-31 | Must | As Admin add a complete book; add one without a title; add a duplicate ISBN. | The first is added; the others show an error and are not saved. | M, E |
| TC-32 | FR-32 | Must | Edit a book, then remove another. | Members see the change. | M, E |
| TC-33 | FR-33 | Must | Record a return; record a return for a book with a waiting reservation. | Loan returned, copies +1, loan count −1; with a reservation it becomes ready and a notification is created. | M, E |
| TC-34 | FR-34 | Should | Open the Admin list of active loans and reservations. | All active loans and reservations are listed. | M |
| TC-35 | FR-35 | Could | Fill book data by ISBN; try an unknown ISBN. | Fields are filled; a message for the unknown ISBN. | M |
| TC-36 | FR-36 | Should | Open the Reader for a full-text book; change chapters. | Chapter text is shown and chapters can be changed. | M |
| TC-37 | FR-37 | Should | Read to chapter 3, close the app, open it again. | Reading continues at the saved position. | M, U |
| TC-38 | FR-38 | Should | Open the Reader for a full-text book; check a book without full text. | The floating AI button is visible; for the other book the Reader and button are not offered. | M |
| TC-39 | FR-39 | Should | Tap the AI button and use each option. | The four options show and each works. | M |
| TC-40 | FR-40 | Should | Ask the same question at two different positions. | The text sent matches each position (unit test) and the answers differ accordingly. | U, M |
| TC-41 | FR-41 | Could | Ask for "Recap so far" at three positions (AI test method, section 8). | No event after the position is mentioned; the unit test shows no later paragraph in the prompt. | U, M |
| TC-42 | FR-42 | Could | Open the profile after reading and finishing books. | Current book and finished count are correct. | M |

| TC-43 | FR-43 | Should | Register a new Member; also try Skip. | Onboarding shows; chosen genres are saved; Skip works. | M |
| TC-44 | FR-44 | Should | Save a book, then remove it from Saved. | It appears and disappears in the Saved list. | M |
| TC-45 | FR-45 | Could | Mark a book finished. | It appears in Finished and the count updates. | M |
| TC-46 | FR-46 | Should | In Settings change text size, switch reminders, open "How Bookmate AI works", log out. | Reader text size changes; the other actions work. | M |
| TC-47 | FR-47 | Could | Open Notifications after a reminder and a "reservation ready". | Both appear and can be marked read. | M |

### 9.2 Non-Functional

| ID | Req | Priority | Test | Expected result | Type |
|---|---|---|---|---|---|
| TC-48 | NFR-01 | Should | Let three people who did not build the app search for a book, borrow it, and ask the AI, without help. Note where they get stuck. | All three finish; problems are listed as Minor or Major bugs. | M |
| TC-49 | NFR-02 | Should | Run a search five times on a normal connection. | Results appear within 3 seconds (target). | M |
| TC-50 | NFR-03 | Should | Send five AI requests. | The loading state shows at once; a typical answer arrives within 15 seconds (target). | M |
| TC-51 | NFR-04 | Must | Go through every screen with the internet off, with an empty result, and with a failed request. | No crash; a clear message each time. | M |
| TC-52 | NFR-05 | Must | Check the Firestore data and the app logs. | No password in plain text. | R |
| TC-53 | NFR-06 | Must | Search the repository and the code for keys; check `.gitignore`. | No AI or API key in the repository; `local.properties` and `google-services.json` are ignored. | R |
| TC-54 | NFR-07 | Must | Run the rule tests: a Member tries Admin actions and reads another user's loans. | All are denied. | E |
| TC-55 | NFR-08 | Should | Compare stored fields with the Database Design; check that AI chat is not stored. | Only the listed fields exist; no chat is stored; test accounts use sample data. | R |
| TC-56 | NFR-09 | Must | Review the code and use every AI feature. | All AI answers use the one shared answer component with the notice. | R, M |
| TC-57 | NFR-10 | Must | Compare the books with full text to the list of public-domain books. | Every full-text book is public domain and shows its source. | R |
| TC-58 | NFR-11 | Should | Install and run on Android 8.0 and on the newest version. | The app runs on both. | M |
| TC-59 | NFR-12 | Should | Review the code with the Project Standards checklist. | Packages, naming, formatting, and the single AI access point (`AiService`) follow the rules. | R |
| TC-60 | NFR-13 | Should | Block the AI and book APIs; then use Home, My Books, and the Reader. | Those parts still work. | M |
| TC-61 | NFR-14 | Should | Check the RTM before submission. | Every requirement has a test case with a result. | R |

## 10. Reporting
- Each result (Pass or Fail) is written in the **Result** column of the RTM, and the code reference in the **Code** column.
- A Test Report in the Implementation phase lists rounds, results, open bugs, and the AI sampling table.

## 11. Risks to Testing

| Risk | Handling |
|---|---|
| AI answers differ each time | Run each AI test three times and use the pass rule in section 8. |
| Free AI limit is used up by testing | Fake `AiService` in unit tests; few live runs; stored summaries. |
| Few real devices | Use the emulator with Play services, and test on at least one real phone. |
| Late testing (R-16) | Rounds start after the core features, and each developer tests before each pull request. |
