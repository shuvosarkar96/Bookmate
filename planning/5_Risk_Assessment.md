# Risk Assessment — Bookmate

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 2026-10 | Project team | First version |

## 1. Purpose
This document lists the main risks of the Bookmate project, estimates how serious each one is, and defines how the team will prevent or handle it. It will be reviewed during the project and updated when new risks appear.

## 2. Rating Method
Probability and impact are the team's estimates, rated Low, Medium, or High.

| Probability \ Impact | Low | Medium | High |
|---|---|---|---|
| **High** | Medium | High | High |
| **Medium** | Low | Medium | High |
| **Low** | Low | Low | Medium |

Risk level = result of the table above.

## 3. Risk Register

| ID | Risk | Category | Probability | Impact | Level | Owner |
|---|---|---|---|---|---|---|
| R-01 | AI gives wrong or invented summaries (hallucination) | Technical | High | Medium | High | Shuvo |
| R-02 | Free tier of the AI API has limits, changes, or stops working | Technical / Cost | Medium | High | High | Shuvo |
| R-03 | Not enough time to finish all features (about 9 weeks) | Schedule | High | High | High | Shuvo |
| R-04 | API key is accidentally pushed to GitHub | Security | Medium | High | High | All members |
| R-05 | Lead is overloaded (management, development, AI) | Staffing | High | Medium | High | Shuvo |
| R-06 | A member is unavailable (exams, other courses, illness) | Staffing | Medium | Medium | Medium | Shuvo |
| R-07 | Scope creep: team keeps adding features | Scope | Medium | High | High | Shuvo |
| R-08 | Copyright problem with book text | Legal | Low | High | Medium | Sudipta |
| R-09 | Book data from public APIs is missing, wrong, or incomplete | Data | Medium | Medium | Medium | Sudipta |
| R-10 | Splitting long books into chapters is harder than expected | Technical | Medium | Medium | Medium | Sudipta |
| R-11 | Team is new to Jetpack Compose or AI integration (learning curve) | Skill | Medium | Medium | Medium | Shuvo |
| R-12 | App needs internet; AI or book data fails when offline | Technical | Medium | Medium | Medium | Shuvo |
| R-13 | Code quality problems from unchecked AI-generated code | Quality | Medium | Medium | Medium | Shuvo |
| R-14 | Git conflicts or lost work from poor branch handling | Process | Medium | Medium | Medium | Shuvo |
| R-15 | Documents become inconsistent with each other or with the code | Quality | Medium | Medium | Medium | Nafis |
| R-16 | Bugs found too late because testing starts at the end | Quality | Medium | High | High | Nafis |

## 4. Risk Response Plan

| ID | Prevention (before it happens) | Contingency (if it happens) |
|---|---|---|
| R-01 | Show the notice "AI-generated, may be inaccurate" on every AI answer. Ground summaries in the real book text. Ask the AI to use only the given text. | Limit the chatbot to public-domain books where the text can be checked. Test summaries manually. |
| R-02 | Read the current free-tier limits before implementation. Keep AI calls few (short prompts, chapter-based). Keep the AI code in one place so the provider can be changed. | Switch to another free or low-cost AI provider, or use pre-generated summaries for the demo books. |
| R-03 | Rank features by priority. The minimum is library basics and one AI feature. Set weekly goals and track them in GitHub Issues. | Drop low-priority features first (barcode scan, extra AI features). |
| R-04 | Keep keys only in `local.properties` (ignored by Git). Check each pull request for secrets. | Regenerate the key immediately and tell the lead. Deleting the commit alone is not enough. |
| R-05 | Split tasks into small issues early. Sudipta co-develops app features. Nafis takes testing and documents. | Move tasks to other members; reduce scope. |
| R-06 | Every area has a backup person. Everything is kept in the shared repository. | Reassign tasks at the next meeting; move deadlines inside the team schedule. |
| R-07 | Requirements are fixed in the SRS. New features need team agreement and a written change. | Reject or postpone the change to a "future work" list. |
| R-08 | Use only public-domain books for full text. Use metadata only for other books. | Remove the affected book and use another public-domain one. |
| R-09 | Use two sources (Google Books and Open Library). Show a clear message when data is missing. | Add a small manual dataset of books for the demo. |
| R-10 | Start with a few books of simple chapter structure. Test the chaptering early. | Split by fixed text size instead of real chapters. |
| R-11 | Start with small tasks, pair work, and short knowledge sharing. Use official documentation and sample code. | Simplify the UI; use simpler AI integration (direct API call). |
| R-12 | Handle loading, error, and offline states on every screen. | Show a clear error message; keep catalog data cached if possible. |
| R-13 | Every member must understand and test AI-generated code. Pull requests need one approval. | Rewrite or remove the problem code; add tests for it. |
| R-14 | Follow the Git rules in the Project Standards (branches, small PRs). | Restore work from branch history; fix conflicts with the lead. |
| R-15 | Use requirement IDs and the RTM. Review documents before they are final. | Hold a consistency review of all documents before submission. |
| R-16 | Write test cases during the SRS and Design phases. Test each feature when it is merged. | Reserve time before the deadline for a full test round and bug fixing. |

## 5. Monitoring
- The risk register is reviewed at the weekly team meeting.
- New risks are added with the next free ID (R-17, R-18, ...). IDs are never reused.
- When a risk becomes real, the owner reports it to the lead and starts the contingency plan.
