# Week 5 Reflection

**Name:** Nathan Perfetti

**Date:** June 25, 2026

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/7

---

## Code Review

**Reviewed:** *(Dustin)*

**Link to my review:**

https://github.com/dmarsh31/media-tracker-android/pull/8



### What I Looked At

Dustin's week-06 PR (#8), his login API work. The diff was smaller so I focused on the two files in it, `AuthViewModel.kt` and `LoginScreen.kt`.


### What I Noticed

Dustin's been deep in the auth/login API, which is honestly one of the trickier parts of this whole thing since there are so many pieces that have to line up. He was upfront that he's still working through it and planning to keep at it after class to get caught up to Ben's milestone, so this is more of a checkpoint along the way, which makes total sense. The changes he did land are clean, pulling `import android.R` (a sneaky one that shadows your own `R`, nice find), an unused import, the empty `()` on the class, and tidying up the `Log.d` strings. He's also already logging the response, which is exactly the right move for figuring out what the API is doing.


### Comments I Left

Left some encouragement on the cleanups, especially the `android.R` catch. I also passed along something I spotted that might help him get unstuck on the 400 he mentioned, the `grantType` field looks like it wants the literal `"password"`. Figured a suggestion was more useful than nitpick like it was a final submission.


---

## One Thing I Understood More Deeply

The auth interceptor finally made sense once I realized it's just middleware. It's the same thing as middleware in an Express or Next app, it sits in the chain, catches the request on its way out, and can mess with it before it goes. Ours grabs the saved token and tacks the `Authorization` header on, so every call gets auth without me wiring it up each time, and the logging one lives right there too. Once it stopped being "some Android networking thing" and became "a wrapper that can change every request," the whole setup got way less intimidating.

---

## One Thing I'm Still Confused About

Still fuzzy on why the repo builds its own `MediaPage` instead of just passing back what the API gives us. We're already pulling `body()`, `X-Next-Cursor`, and `X-Has-More` off the Retrofit `Response`, so why repackage it into our own object? Why not just point our data model straight at the API response and skip the middleman? I think the answer is so the UI isn't tied to Retrofit types, but I honestly couldn't tell you when that's worth it and when it's just extra code to keep in sync.

---

## Anything Else *(optional)*

Honestly my biggest pain this week wasn't the code, it was the dev loop. Every rebuild relaunches the app and dumps the session, so I'm back at the login screen over and over while testing search. Kept wishing Studio could just swap the changed code and leave me logged in, or that there was an easy way to skip login for local testing. Gonna look for a faster way to iterate next week. Ben gave some tips and suggestions he uses for his workflow to allow fo smoother hot reloading, such a scriting and autofill/submit for the login screen but also just previewing 1 file at a time. Utlimately for final testing, you'd need to go thru the full signup/signin flow on-device for proper QA. 


---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
