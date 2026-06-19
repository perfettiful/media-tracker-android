# Week 5 Reflection

**Name:** Nathan Perfetti

**Date:** June 18, 2026

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/6

---

## Code Review

**Reviewed:** *(Jama)*

**Link to my review:**

https://github.com/JamaRufai/media-tracker-android/pull/4

### What I Looked At

On Jama's PR #4, I spent most of my time in the new `data/network/` package since that's where the real work is, tracing the register call from the screen down to the API and back. That meant reading `DefaultUserRepository`, `RetrofitInstance`, `UserApiService`, and the `UserRepository` interface, then following it up into `RegisterViewModel` to see how the result becomes UI state. Also skimmed his `build.gradle.kts` to see how the client creds get pulled in.


### What I Noticed

First thing, he branched off `main` this week, so the diff is just this week's register work instead of his whole history stacked up like last time. Made it a lot easier to follow. The code lines up with the reference pattern closely: one shared `RetrofitInstance` with the logging interceptor, a `UserRepository` interface backed by a sealed `RegisterResult` (Success, Conflict, NetworkError, UnknownError), and `createUser` returning `Response<Unit>` so there's no body to deserialize. `DefaultUserRepository` maps the 201/409 codes to results itself, which keeps the http details out of the viewmodel, and the creds come from `BuildConfig` through `ApiConstants` instead of being hardcoded. One small thing, `UserApiService` still imports `Headers` and `Query` that aren't used.


### Comments I Left

I mostly confirmed the parts that had to be put together, inluding the status-code mapping in `DefaultUserRepository` as a clean way to keep networking in the data layer, the `Response<Unit>` choice for register since there's nothing to read back, and `RegisterViewModel` depending on the `UserRepository` interface instead of the concrete class. Also left a quick cleanup note on the unused `Headers` and `Query` imports so it's not all praise.


---

## One Thing I Understood More Deeply

LaunchedEffect made more sense to me this week. It's a coroutine builder tied to a composable, and it only re-runs when its key changes, otherwise it runs once. It works a lot like React's useEffect with a dependency array, which is what helped me grasp it. In the register screen we key it on the register state, so when the state becomes Success the navigation fires one time instead of on every recomposition. That keyed behavior is the reason to use it over just calling the code directly, since it's how you run a one-time side effect from inside a composable without it repeating.

---

## One Thing I'm Still Confused About

I don't fully understand the RetrofitInstance singleton. I can see it builds the OkHttp client and the Retrofit object and exposes the api service, but I'm not clear on why it's a standalone object instead of living inside the repository or the service. The broader version of that question is the singleton plus dependency injection pattern itself, why we build one shared instance and pass it in rather than creating things where they're used. The code reads fine line by line, but I couldn't rebuild it from scratch or explain why it's set up that way.

---

## Anything Else *(optional)*

The live walkthroughs of the syntax and patterns are useful, and so is the format of trying it ourselves and then syncing with the working example at each milestone. One thing that would help me is covering the high level architecture first, the general client-server pattern we're building by looking at digrams, as well as what the gradle packages actually give us over doing it by hand, before we start implementing. I tend to be wiring pieces together before I have the reason for them. Separate but related, I think the implementation and the packages would stick better if we practiced on a smaller stripped-down app focused on the one concept for the week. I realize that's not nothing to get setup up, setting up a whole environment for a throwaway app is its own work, so I get why we stay in the real project.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
