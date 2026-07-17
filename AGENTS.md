# AGENTS.md


**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them instead of picking silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop, name what is confusing, and ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- Do not add features beyond what was asked.
- Do not create abstractions for single-use code.
- Do not add flexibility or configurability that was not requested.
- Do not add error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:

- Do not improve adjacent code, comments, or formatting unless required.
- Do not refactor things that are not broken.
- Match existing style, even if you would do it differently.
- If you notice unrelated dead code, mention it instead of deleting it.

When your changes create orphans:

- Remove imports, variables, functions, and files that your changes made unused.
- Do not remove pre-existing dead code unless asked.

The test: every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:

- "Add validation" -> "Write tests for invalid inputs, then make them pass."
- "Fix the bug" -> "Write a test that reproduces it, then make it pass."
- "Refactor X" -> "Ensure tests pass before and after."
- When writing new production code, add or update unit tests for the behavior in the same change unless the code is purely declarative UI with no logic; if skipping tests, state why.
- When writing a new UI feature or materially changing an existing UI feature, add or update Compose UI tests for the visible state and user callbacks.
- When changing production code, maintain all affected tests in the same change; do not leave stale assertions, mocks, fixtures, or documentation behind.

For multi-step tasks, state a brief plan:

```text
1. [Step] -> verify: [check]
2. [Step] -> verify: [check]
3. [Step] -> verify: [check]
```

## 5. Android Development

**Prefer platform conventions and lifecycle-safe code.**

- Keep Android components small and focused on their platform responsibility.
- Respect lifecycle boundaries for activities, fragments, services, and composables.
- Do not keep long-running work in UI components; move it to appropriate lifecycle-aware layers.
- Use resource files for user-facing strings, dimensions, colors, and themes when appropriate.
- Avoid hardcoded values in UI when a resource or theme token already exists.
- Prefer explicit permissions, manifest entries, and intent handling over hidden assumptions.
- Keep build, Gradle, and manifest changes minimal and directly tied to the task.
- Whenever adding or updating libraries, run a Gradle sync-equivalent check before final verification.

## 6. Kotlin Development

**Write clear, idiomatic Kotlin before clever Kotlin.**

- Prefer immutable values with `val` unless mutation is required.
- Use nullable types intentionally; avoid unnecessary `!!`.
- Prefer sealed classes or sealed interfaces for closed state and event hierarchies.
- Use data classes for simple value holders.
- Keep extension functions close to the domain they support and avoid broad utility dumping grounds.
- Prefer standard library functions when they improve clarity, but avoid dense chains that obscure behavior.
- Keep coroutine work structured; avoid launching unmanaged coroutines from arbitrary scopes.
- Make suspend functions do suspend work; do not hide blocking calls inside them.
- Avoid duplicate passthrough overloads or wrapper functions that only delegate or construct a DTO without adding behavior; expose one clear API instead.

## 7. Compose UI

**Compose functions should be predictable, stateless when practical, and cheap to recompose.**

- Prefer state hoisting: pass state down and events up.
- Keep business logic out of composables; route it through state holders or ViewModels.
- Use `remember` for values that should survive recomposition, not as a general cache.
- Use `rememberSaveable` only for state that should survive configuration changes and can be saved safely.
- Avoid side effects directly in composable bodies; use appropriate effect APIs such as `LaunchedEffect`, `DisposableEffect`, or `SideEffect`.
- Provide stable keys for dynamic lists when item identity matters.
- Keep previews useful and lightweight; do not require network, database, or dependency injection setup in previews.
- Prefer existing design system components, typography, spacing, and color tokens before adding new UI patterns.
- Ensure composables remain accessible with meaningful text, labels, content descriptions, and touch targets where applicable.

## 8. Android Architecture

**Separate UI, state, domain logic, and data access by responsibility.**

- Keep UI layers focused on rendering state and forwarding user actions.
- Keep ViewModels focused on UI state management, event handling, and coordination.
- Do not pass Android UI objects into lower layers unless the dependency is explicitly platform-specific.
- Prefer unidirectional data flow: events move up, state flows down.
- Model UI state explicitly with immutable state objects.
- Expose observable state from ViewModels and avoid exposing mutable state directly.
- Keep repositories responsible for data coordination, not UI decisions.
- In repositories, assign `safeApiCall` results to a local `val result` before branching; do not inline it inside `when` conditions.
- Keep domain logic independent from Android framework APIs when practical.
- Prefer dependency injection patterns already used by the project instead of introducing a new framework or style.
- For MVI screens, define `State`, `Intent`, and `Effect` in a feature contract file, such as `LoginContract.kt`.
- Add tests at the layer where behavior lives: unit tests for logic, UI tests for UI behavior, and integration tests for boundaries when needed.
- Keep UI tests focused on screen contracts: render explicit state, perform user actions, and assert visible output or emitted callbacks.
- Prefer focused JVM unit tests for repositories, API boundaries, interceptors, and ViewModels before adding heavier instrumentation tests.

## 9. API DTO Boundaries

**Treat backend JSON as untrusted input.**

- For response DTOs parsed from network JSON, prefer nullable fields or safe defaults at the API boundary.
- Use `kotlinx.serialization` for network JSON DTOs and Retrofit conversion.
- Do not rely on non-null DTO fields to enforce backend contracts; malformed or missing fields must not crash the app.
- Validate response DTOs in the repository or mapper layer before creating non-null domain or UI models.
- Convert invalid required fields into controlled `ApiResult.Error` values or drop malformed list rows when that is the safer behavior.
- Do not create extra UI/domain wrapper classes for backend response data unless there is a concrete transformation need; pass the existing response DTOs through feature state when the UI displays the backend data directly.
- Keep request DTOs as strict as the app-owned outgoing contract allows.

## 10. Feature Documentation

**Document implemented features as part of the change.**

- Keep feature documentation in the `docs/` folder.
- Add or update documentation whenever implementing a new feature or materially changing an existing feature.
- Split documentation by implementation milestone when changes are naturally grouped that way.
- Keep docs focused on what was implemented, how it works, important behavior, and verification.
- Do not include external links unless explicitly requested.