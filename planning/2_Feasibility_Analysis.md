# Feasibility Analysis — Bookmate

## 1. Technical Feasibility

**Familiarity with the application**
The team is building a mobile library app. Library functions (search, borrow, reserve) are well understood and have clear, common patterns. The AI features (recommendation, book summary chatbot) are newer to the team.

**Familiarity with technology**
| Technology | Purpose | Assessment |
|---|---|---|
| Kotlin, Android Studio, Jetpack Compose | App development | Mature, well documented, widely used |
| Google Books API / Open Library API | Book information | Public APIs with documentation |
| Gemini API (Firebase AI Logic or direct API) | AI assistant and chatbot | Well documented; free tier exists but has usage limits |
| ML Kit Barcode Scanning | ISBN scan | On-device, simple integration |
| Project Gutenberg | Public-domain book text | Free and legal to use |

**Project size**
Medium for a student team. Main modules: user account, book catalog and search, borrowing, AI recommendation, AI summary chatbot, barcode scan. The size is controlled by limiting full-text summaries to a small set (about 5-10) of public-domain books.

**Compatibility**
The app targets Android only. It connects to external APIs over the internet. No integration with an existing university library system is planned.

**Technical risk areas**
- AI response quality and accuracy (summaries may be wrong).
- Splitting long books into chapters before sending them to the AI.
- API usage limits and internet dependency.

**Conclusion:** Technically feasible. The main risks can be reduced by limiting scope and showing an accuracy notice on AI output.

## 2. Economic Feasibility

**Development costs**
- Software and tools: Android Studio, Kotlin, ML Kit, GitHub (free).
- AI API: free tier is expected to be enough for development and demo. The team must confirm current free-tier limits before implementation.
- Book data: Google Books / Open Library APIs and Project Gutenberg (free).
- Hardware: team members' own computers and Android phones or emulator.
- Labor: student team time (no salary cost).

**Operational costs**
Not applicable for the semester project. If the app were deployed for real use, costs would include AI API usage fees, a backend or database hosting, and maintenance.

**Benefits**
- Tangible: no or very low spending; potential time saving for users in finding and understanding books (to be measured in testing).
- Intangible: practical learning in Android, AI integration, and software engineering process; a portfolio project.

**Conclusion:** Economically feasible. Cost is close to zero, while the learning and demonstration benefits are high.

## 3. Organizational Feasibility

**Stakeholders**
| Stakeholder | Interest |
|---|---|
| Course instructor (sponsor) | Project meets course requirements and SE process |
| Development team | Complete the project and learn |
| Library users (students) | Easy book search, borrowing, and summaries |
| Librarian / library staff | Fewer routine questions (benefit if deployed) |

**Fit with the course**
The project follows the required academic software engineering process and produces the required documents.

**User acceptance**
Students are used to mobile apps and AI chat tools, so acceptance risk is low. Users must be told clearly that AI summaries may be inaccurate.

**Conclusion:** Organizationally feasible.

## 4. Schedule Feasibility

- Project period: October 12, 2026 to around mid-December 2026 (about 9 weeks in total).
- Team size: 3 members (Sarkar Shuvo, Karmaker Sudipta, Sadik Md Nafis).
- Time remaining at the time of writing (October 19, 2026): about 8 weeks.

The plan is to finish all planning and analysis documents first, then design, then implement. To protect the schedule, features are ranked by priority. Library basics and one AI feature are the minimum; the barcode scan and extra AI features are added only if time remains.

**Conclusion:** Feasible if the scope stays controlled and tasks are divided early (see Staffing Plan).

## 5. Overall Conclusion

Bookmate is **feasible** technically, economically, organizationally, and in schedule, under these conditions:
1. Full-text AI summaries are limited to public-domain books.
2. AI output always carries an accuracy notice.
3. API keys are kept out of the public repository.
4. Features are prioritized so a working core is delivered first.

**Recommendation:** Proceed with the project.
