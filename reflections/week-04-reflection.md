# Week 4 Reflection

**Name:** Nathan Perfetti

**Date:** June 11, 2026

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/5

---

## Code Review

**Reviewed:** *(Jama)*

**Link to my review:**

https://github.com/JamaRufai/media-tracker-android/pull/3/changes

### What I Looked At

On Jama's PR #3, I specifically looked at the most recent commits. I focused on `RegisterViewModel.kt` since he had deleted it earlier in the same PR and just restored it, plus the corresponding rewire in `RegisterScreen.kt` that points the form back at the VM. Mostly looking at the state model (the `RegisterUiState` sealed class), how the screen consumes it (`collectAsState`, `LaunchedEffect` on Success), and the validation flow inside `onRegisterClick()`.

### What I Noticed

- I noticed branch hygiene again, this pr still has commits stretching back to May 22 (week 1 reflection, week 2 bug fixes, etc.) on top of tonight's work. Same shape as last week. Next week he should branch off his local `main` after it catches up, otherwise every PR keeps growing in scope.
- The `RegisterUiState` sealed class nested in the VM is clean. Idle, Loading, Success, and `Error(val msgResId: Int)` makes the `when` on `_registerState.value` exhaustive. Same pattern Ben walked through.
- Validation is two rules right now (blank, password mismatch). No email format check, no password length minimum, both cheap to add.
- `delay(800)` is a fake network call but `UserRepository.kt` and `ApiService.kt` are still empty files from earlier in the same PR. The stub has nowhere to land.


### Comments I Left

- Left praise on `RegisterViewModel.kt` line 12 for the `RegisterUiState` shape. Nesting the sealed class inside the VM keeps it traveling with the class.
- Suggested adding an email format check and password length minimum to the validation `when` in `onRegisterClick()`. Server will catch those eventually but no reason not to surface them locally.


---

## One Thing I Understood More Deeply

Sealed classes made more sense and seem less obscure now after Ben explained their use in `RegisterUiState` with `Idle / Loading / Success / Error(val msgResId: Int`) inside `RegisterViewModel`. I'd recognized the pattern but thought of it as "enums but with extra steps,", but the `Error` variant carrying a payload while `Idle` carries nothing is the part that matters. A plain enum cannot do that. The other half is exhaustiveness: the `when` expression on `registerState.value` will not compile until every branch is handled, so adding a fifth state later forces the compiler to drag me through every place I read it. That is the fallback mechanism I did not fully understand until Ben broke it down.

---

## One Thing I'm Still Confused About

The thing I am still stuck on is why `viewModel()` is written as a generic. In `RegisterScreen.kt` the call is `viewModel: RegisterViewModel = viewModel()`, and the tooltip in Studio shows it as `<reified VM>`. I know `reified` means the runtime can see the type, but I do not get why this needs to be a generic function instead of just a regular one that returns my `RegisterViewModel` directly. What does keeping it generic actually buy me, and when would I notice the flexibility it gives?

---

## Anything Else *(optional)*


---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
