# Project Standards — Bookmate

## 1. Purpose
These standards keep documents, code, and team work consistent. All members, and any AI tool they use, must follow them.

## 2. Documentation Standards

**File naming**
- Planning documents: number prefix, words joined with underscores, e.g. `1_System_Request.md`, `4_Project_Standards.md`.
- Other documents: `SRS.md`, `RTM.md`, `Test_Plan.md`, `Design_UI.md`.
- Format: Markdown (`.md`), kept inside the correct SDLC folder (Planning, Analysis, Design, Implementation).

**Document structure**
- Every document starts with a title and a version table:

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | YYYY-MM-DD | Name | First version |

- Use numbered headings (1., 1.1) and tables for lists of items.
- Requirement IDs follow one pattern: `FR-01` (functional), `NFR-01` (non-functional), `UC-01` (use case), `TC-01` (test case). IDs are never reused or renumbered.

**Review**
- Every document is reviewed by at least one other member before it is final.
- Sadik Md Nafis checks format and consistency; Sarkar Shuvo checks technical content.

## 3. Git and GitHub Standards

**Branches**
- `main`: stable, always working. No direct commits.
- `feature/<short-name>`: new features, e.g. `feature/book-search`.
- `fix/<short-name>`: bug fixes, e.g. `fix/login-crash`.
- `docs/<short-name>`: documents, e.g. `docs/srs`.

**Commit messages**
Format: `type: short description` (present tense, under 70 characters).

| Type | Use for |
|---|---|
| feat | New feature |
| fix | Bug fix |
| docs | Document change |
| refactor | Code change without behavior change |
| test | Tests |
| chore | Setup, config, cleanup |

Example: `feat: add book search screen`

**Pull requests**
- Work is merged into `main` only through a pull request (PR).
- A PR needs at least one approval from another member.
- PR title follows the commit format; description says what changed and how it was tested.
- Keep PRs small (one feature or one document).

**Tasks**
- Every task is a GitHub Issue with an owner. Branches and PRs refer to the issue number.

**Repository hygiene**
- `.gitignore` must include: `local.properties`, `build/`, `.gradle/`, `.idea/`, `*.keystore`.
- No large or generated files, and no personal data, in the repository.

## 4. Coding Standards (Kotlin and Android)

**Naming**
| Item | Style | Example |
|---|---|---|
| Classes, objects, Composable functions | PascalCase | `BookCard`, `SearchScreen` |
| Functions, variables | camelCase | `loadBooks`, `bookList` |
| Constants | UPPER_SNAKE_CASE | `MAX_BORROW_DAYS` |
| Packages | lowercase | `com.bookmate.app.ui` |
| Resource files and IDs | snake_case | `ic_book_cover`, `strings.xml` |

**Structure**
- Package by layer inside the app: `ui` (screens, Compose), `data` (repository, API, database), `domain` (models, logic), `util`.
- Suggested architecture: MVVM (ViewModel + state) with Jetpack Compose. The final decision is recorded in the Design phase.

**Code quality**
- No hardcoded user-visible text; use `strings.xml`.
- No unused code, and no commented-out blocks left in `main`.
- Functions should do one thing and stay short and readable.
- Comments explain why, not what. Public classes have a short description.
- Handle errors and loading states (no internet, empty result, API failure) on every screen that loads data.
- Format code with Android Studio's default formatter (`Code > Reformat Code`) before each commit.

**UI**
- Follow Material Design 3. Use theme colors and typography, not scattered fixed values.
- Every AI answer shows the notice: "AI-generated, may be inaccurate".

## 5. Security Standards
- API keys and secrets live only in `local.properties` (not committed). The app reads them through `BuildConfig`.
- If a key is committed by mistake, tell the lead immediately and regenerate the key; deleting the commit is not enough.
- No real personal data of users in test data; use sample data.

## 6. Testing Standards
- Each requirement has at least one test case (see RTM).
- A feature is **done** only when: it works on a device or emulator, it has passed its test case, its PR is approved, and it is merged into `main`.
- Bugs are reported as GitHub Issues with steps to reproduce.

## 7. AI Tool Usage Standards
- Team members may use AI tools, but are responsible for all code and text they submit. Understand it, test it, and fix it before committing.
- Start each AI session with the shared project context prompt so all AI tools follow the same decisions.
- Do not paste API keys or private data into AI tools.
- AI-generated code must follow the standards in this document; reformat or correct it if not.
- Project decisions are made by the team, not by an AI tool. A new decision is shared with all members.

## 8. Communication Standards
- Weekly team meeting; short update in the team chat when a task starts, is blocked, or is finished.
- Decisions that change scope, features, or documents are written down (in the relevant document or an Issue) and announced to the team.
- Reply to messages about blocking issues within one day.
