# Week 8 Reflection

**Name:** Nathan Perfetti

**Date:** July 9, 2026

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/10

---

## Code Review

**Reviewed:** Dustin 

https://github.com/dmarsh31/media-tracker-android/pull/12

**Link to my review:**

### What I Looked At

Dustin's week-08 PR (#12), mainly the media detail wiring since we debugged that screen together in class. Traced `MediaDetailViewModel` and `MediaApiService`, then his `SearchScreen` LazyColumn + paging changes.

### What I Noticed

The debug session paid off, the real mediaId flows through now and reviews got wired too (the stretch goal). Two things: his catch only logs, so a failed load leaves the spinner up forever, the /media/9 404 from tonight would do exactly that. And `getMediaDetails` returns bare `MediaDetail` so a non-2xx throws, while `getReviews` right below already uses `Response<>` correctly. The search paging with `derivedStateOf` is clean.

### Comments I Left

Four. Gave props on the recovery and the paging trigger, the forever-spinner catch (handout wants message + retry), the bare return type vs the `Response<>` he already uses, and a nit about the stale "not wired yet" comment.

---

## One Thing I Understood More Deeply

The same status code can mean opposite things depending on the endpoint. Tonight a 404 from `GET /library/{mediaId}` just means "not added yet," a normal answer, but a 404 from `GET /media/{id}` means the item doesn't exist, a real error. Write the Retrofit call the naive way and both throw, so "not added" renders as a full screen error. Using `Response<LibraryItem>` lets you read the code and decide what it means yourself. GitHub does the same thing, "did I star this repo" is answered with a 204 or 404.


---

## One Thing I'm Still Confused About

Hit a case where the list endpoint returned an item (id 9 from `GET /media?type=movie`) but `GET /media/9` 404'd. So the list and detail disagree about what exists. My error state handled it, but I don't know if that's bad seed data or something clients are supposed to defend against. Wanna ask in class. Also still not sure what the Save button is supposed to wire to, there's no favorites endpoint in the API.


---

## Anything Else *(optional)*

Helped Dustin debug his detail screen over screen share. Three failures in a row, none of them in his detail code: a 404 from the old NavGraph stub hardcoding mediaId = -1, a 401 b/c his stored token expired, then another 404 from mock ids being sent to the real API. All three diagnosed by reading the response body in Logcat instead of guessing. That logging interceptor from week 5 keeps earning its spot.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
