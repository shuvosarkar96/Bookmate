# Requirements Traceability Matrix (RTM) — Bookmate

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 2026-11 | Project team | First version: requirements, use cases, design, test case IDs |

## 1. Purpose
This matrix links every requirement in the SRS to its source, use case, design screen, test case, code, and test result. It shows that nothing in the SRS is forgotten and nothing is built without a reason. It is updated in each phase.

## 2. How to Read It
- **Source:** SR-1 to SR-8 are the business requirements in the System Request. Admin requirements use SR-8 (the SRS calls it "SR value (library staff)").
- **Design:** SCR IDs are screens in the UI Design document. "Pending" means the screen is not drawn yet (UI Design version 1.1, section 8).
- **Test case:** IDs are reserved now (TC-01 to TC-47 for FR-01 to FR-47, TC-48 to TC-61 for NFR-01 to NFR-14). The Test Plan defines them.
- **Code** and **Result:** "-" means not started. They are filled in the Implementation phase (Code = pull request or module, Result = Pass or Fail).
- Use case "-" means the requirement has no use case yet; it is a simple screen or list.

## 3. Functional Requirements

| ID | Requirement | Priority | Source | Use case | Design | Test case | Code | Result |
|---|---|---|---|---|---|---|---|---|
| FR-01 | Register as a Member | Must | SR-1 | UC-01 | SCR-02 | TC-01 | - | - |
| FR-02 | Log in and log out | Must | SR-1 | UC-01 | SCR-01, SCR-13 | TC-02 | - | - |
| FR-03 | View and edit profile | Should | SR-1 | UC-01 | SCR-11; edit screen pending | TC-03 | - | - |
| FR-04 | Reset forgotten password | Could | SR-1 | UC-01 | Pending | TC-04 | - | - |
| FR-05 | Show screens by role (Member or Admin) | Must | SR-1 | UC-01 | SCR-01, SCR-15 | TC-05 | - | - |
| FR-06 | Search by title, author, ISBN | Must | SR-2 | UC-02 | SCR-05 | TC-06 | - | - |
| FR-07 | Filter and browse by genre and availability | Should | SR-2 | UC-02 | SCR-05 | TC-07 | - | - |
| FR-08 | Home list (new and popular books) | Should | SR-2 | UC-02 | SCR-04 | TC-08 | - | - |
| FR-09 | Show book details | Must | SR-3 | UC-02 | SCR-06 | TC-09 | - | - |
| FR-10 | Show a clear message for no result | Must | SR-2 | UC-02 | SCR-05; state screen pending | TC-10 | - | - |
| FR-11 | Borrow an available book | Must | SR-4 | UC-03 | SCR-06 | TC-11 | - | - |
| FR-12 | Reserve an unavailable book | Should | SR-4 | UC-04 | SCR-05, SCR-06, SCR-10 | TC-12 | - | - |
| FR-13 | Cancel own reservation | Should | SR-4 | UC-04 | Pending | TC-13 | - | - |
| FR-14 | Show current loans and reservations | Must | SR-4 | UC-03 | SCR-10 | TC-14 | - | - |
| FR-15 | Renew a loan | Should | SR-4 | UC-03 | SCR-10 | TC-15 | - | - |
| FR-16 | Show loan history | Could | SR-4 | UC-03 | Pending | TC-16 | - | - |
| FR-17 | Remind before due date | Could | SR-4 | UC-03 | SCR-13; reminder text pending | TC-17 | - | - |
| FR-18 | Prevent borrowing when no copy is available | Must | SR-4 | UC-03 | SCR-06 | TC-18 | - | - |
| FR-19 | Scan ISBN with the camera | Could | SR-5 | UC-07 | SCR-14 | TC-19 | - | - |
| FR-20 | Show matching or external book information | Could | SR-5 | UC-07 | SCR-14 | TC-20 | - | - |
| FR-21 | Recommend from genres and history | Should | SR-6 | UC-06 | SCR-04; Recommend tab pending | TC-21 | - | - |
| FR-22 | Recommend from natural-language request | Should | SR-6 | UC-06 | Pending | TC-22 | - | - |
| FR-23 | Reason for each recommendation, link to detail | Could | SR-6 | UC-06 | Pending | TC-23 | - | - |
| FR-24 | AI summary of a whole book | Must | SR-7 | UC-05 | SCR-06, SCR-09 | TC-24 | - | - |
| FR-25 | AI summary of a chapter | Should | SR-7 | UC-05 | SCR-09; picker pending | TC-25 | - | - |
| FR-26 | AI summary of a page range | Could | SR-7 | UC-05 | Pending | TC-26 | - | - |
| FR-27 | Follow-up questions in a chat | Should | SR-7 | UC-05 | SCR-09 | TC-27 | - | - |
| FR-28 | Show AI accuracy notice | Must | SR-6, SR-7 | UC-05, UC-06, UC-10 | SCR-08, SCR-09 | TC-28 | - | - |
| FR-29 | Chapter or page summaries only with full text; else description-based | Must | SR-7 | UC-05 | Pending | TC-29 | - | - |
| FR-30 | Loading state and AI error message | Must | SR-6, SR-7 | UC-05, UC-06, UC-10 | Pending | TC-30 | - | - |
| FR-31 | Admin adds a book | Must | SR-8 | UC-08 | SCR-15 | TC-31 | - | - |
| FR-32 | Admin edits or removes a book | Must | SR-8 | UC-08 | Pending | TC-32 | - | - |
| FR-33 | Admin records a return | Must | SR-8 | UC-09 | Pending | TC-33 | - | - |
| FR-34 | Admin views active loans and reservations | Should | SR-8 | UC-09 | Pending | TC-34 | - | - |
| FR-35 | Admin fills book data by ISBN lookup | Could | SR-8 | UC-07, UC-08 | SCR-14, SCR-15 | TC-35 | - | - |
| FR-36 | Read a book in the Reader | Should | SR-7 | UC-10 | SCR-06, SCR-07 | TC-36 | - | - |
| FR-37 | Save and continue reading position | Should | SR-7 | UC-10 | SCR-04, SCR-07 | TC-37 | - | - |
| FR-38 | Floating AI button in the Reader | Should | SR-7 | UC-10 | SCR-07 | TC-38 | - | - |
| FR-39 | AI options: summarize chapter, recap, explain, ask | Should | SR-7 | UC-10 | SCR-08 | TC-39 | - | - |
| FR-40 | Use reading position to choose AI text | Should | SR-7 | UC-10 | SCR-08 | TC-40 | - | - |
| FR-41 | Recap so far without spoilers | Could | SR-7 | UC-10 | SCR-08 | TC-41 | - | - |
| FR-42 | Profile shows current book and finished count | Could | SR-1 | - | SCR-11 | TC-42 | - | - |
| FR-43 | Onboarding: pick favorite genres | Should | SR-1, SR-6 | UC-01 | SCR-03 | TC-43 | - | - |
| FR-44 | Save a book to a Saved list | Should | SR-2 | - | SCR-06, SCR-12 | TC-44 | - | - |
| FR-45 | Mark finished and show Finished list | Could | SR-4 | - | SCR-12 | TC-45 | - | - |
| FR-46 | Settings screen | Should | SR-1 | - | SCR-13 | TC-46 | - | - |
| FR-47 | Notifications screen | Could | SR-4 | - | Pending | TC-47 | - | - |

## 4. Non-Functional Requirements

| ID | Category | Requirement | Source | Design / where handled | Test case | Code | Result |
|---|---|---|---|---|---|---|---|
| NFR-01 | Usability | Simple to use, Material Design 3 style | SR value (user experience) | All screens (UI Design) | TC-48 | - | - |
| NFR-02 | Performance | Search results within 3 s (target) | SR value (less time to find books) | Architecture | TC-49 | - | - |
| NFR-03 | Performance | AI loading indicator at once, answer within 15 s (target) | SR value (quicker understanding) | SCR-08, SCR-09; Architecture | TC-50 | - | - |
| NFR-04 | Reliability | No crash offline, empty result or API failure | SR constraint (AI service and internet) | State screens (pending) | TC-51 | - | - |
| NFR-05 | Security | No plain-text passwords | SR constraint (security) | Architecture | TC-52 | - | - |
| NFR-06 | Security | API keys not in the public repository | SR constraint (security) | Project Standards; Architecture | TC-53 | - | - |
| NFR-07 | Security | Members cannot use Admin functions or see others' data | SR constraint (security) | Architecture | TC-54 | - | - |
| NFR-08 | Privacy | Collect only needed data; sample data in tests | SR constraint (security) | Architecture | TC-55 | - | - |
| NFR-09 | Transparency | AI output marked as AI-generated | SR constraint (AI accuracy) | SCR-08, SCR-09, SCR-13 | TC-56 | - | - |
| NFR-10 | Legal | Full text only for public-domain books | SR constraint (book text) | Architecture | TC-57 | - | - |
| NFR-11 | Compatibility | Runs on the minimum Android version set in Design | SR constraint (platform) | Architecture | TC-58 | - | - |
| NFR-12 | Maintainability | Follow Project Standards; AI provider in one module | Project Standards | Architecture | TC-59 | - | - |
| NFR-13 | Availability | App works when external services are down | SR constraint (AI service limits) | Architecture | TC-60 | - | - |
| NFR-14 | Testability | Every requirement has a test case | Project Standards | Test Plan | TC-61 | - | - |

## 5. Use Case to Requirement

| Use case | Requirements |
|---|---|
| UC-01 | FR-01, FR-02, FR-03, FR-04, FR-05, FR-43 |
| UC-02 | FR-06, FR-07, FR-08, FR-09, FR-10 |
| UC-03 | FR-11, FR-14, FR-15, FR-16, FR-17, FR-18 |
| UC-04 | FR-12, FR-13 |
| UC-05 | FR-24 to FR-30 |
| UC-06 | FR-21, FR-22, FR-23, FR-28, FR-30 |
| UC-07 | FR-19, FR-20, FR-35 |
| UC-08 | FR-31, FR-32, FR-35 |
| UC-09 | FR-33, FR-34 |
| UC-10 | FR-36 to FR-41, FR-28, FR-30 |

FR-42, FR-44, FR-45, FR-46, and FR-47 have no use case yet (see section 2).

## 6. Risk to Requirement

| Risk | What reduces it |
|---|---|
| R-01 Wrong AI summaries | FR-28, FR-29, FR-41, NFR-09 |
| R-02 AI free tier changes | FR-30, NFR-12, NFR-13 |
| R-03 Not enough time | Priorities (Must, Should, Could) in the SRS |
| R-04 API key leak | NFR-06 |
| R-08 Copyright | FR-29, NFR-10 |
| R-09 Poor book data | FR-10, FR-35 |
| R-10 Chapter splitting | FR-25, FR-37, FR-40 |
| R-12 Needs internet | FR-30, NFR-04, NFR-13 |
| R-16 Late testing | NFR-14, test case IDs TC-01 to TC-61 |

## 7. Status Summary

| Item | Count |
|---|---|
| Functional requirements | 47 (Must 16, Should 19, Could 12) |
| Non-functional requirements | 14 |
| Functional requirements with at least one screen | 35 |
| Functional requirements with no screen yet | 12 (FR-04, FR-13, FR-16, FR-22, FR-23, FR-26, FR-29, FR-30, FR-32, FR-33, FR-34, FR-47) |
| Of the covered ones, partly covered | 5 (FR-03, FR-10, FR-17, FR-21, FR-25) |
| Requirements with code | 0 |
| Requirements tested | 0 |
