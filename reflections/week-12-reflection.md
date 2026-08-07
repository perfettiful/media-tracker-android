# Week 12 Reflection — Bonus Feature Sprint (Week 2 of 2, Final)

**Name:** Nathan Perfetti

**Date:** August 6, 2026

**My assigned bonus feature:** Priorities

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/14

---

## Code Review

**Reviewed:** Dustin

**Link to my review:**

https://github.com/dmarsh31/media-tracker-android/pull/15

### What I Looked At

Dustin's week-12 PR (#15), the second half of his Write Review feature. He added edit and delete on top of what he had, so I spent most of my time in `WriteReviewViewModel` and `MediaDetailViewModel` where the new update and delete flows live, plus the star row test he added.

### What I Noticed

First thing I checked was whether he fixed the bug I flagged last week, where posting a review did nothing visible and you had to guess whether it worked. He did. There's a `reviewSaved` flag now and a `LaunchedEffect` that navigates back when it flips, which is the right shape. He also wrote an actual Compose UI test with a `testTag` on each star, which is more than I managed, my tests are all ViewModel level.

Three things I flagged. There are two identical `LaunchedEffect(errorMessage)` blocks in `WriteReviewScreen` now, one from last week and one he added, so any error snackbar fires twice. His `deleteReview` catches the exception and only logs it, so a failed delete closes the dialog and leaves the review sitting there with no explanation, which is the same silent failure he just fixed on the post side. And his `ReviewState` enum uses `null` to mean success, but `null` is also the starting value, so "haven't loaded yet" and "loaded fine" are indistinguishable, and the `Error` case never gets set at all.

One thing I checked before saying anything: he calls `getReviews` with a `userId` argument that I could not find anywhere in the diff. I pulled the file off his branch rather than guess, and it turned out the parameter was already there from earlier work. Glad I looked, that would have been an annoying comment to get wrong.

### Comments I Left

Four. Props on the navigate-back fix and the UI test, then the duplicated `LaunchedEffect`, the silent delete failure, and the null-as-success state.


---

## Bonus Feature — Final Status

**What works end-to-end, right now:**

The whole loop works on real data. Tap the tune icon on My Library and you get a Priorities screen off `GET /priorities`, sorted by `orderIndex`, cards showing the cover, title, priority badge, and the "Est. 6 hours . <note>" line from the wireframe. You set a priority from the overflow menu on any Want To item, pick High/Medium/Low plus hours and an optional note, and that goes out as `PUT /priorities`.

Drag to reorder was the big one this week and it works. Long press a card, drag, and it saves on drop. I hand rolled it with `pointerInput` and `detectDragGesturesAfterLongPress` instead of grabbing a library. It uses the list layout info to figure out which card you're holding, and once you've dragged past a whole card it swaps with that neighbour and keeps the leftover offset so the card stays under your finger. On drop it renumbers `orderIndex` 0 through 4 and only PUTs the rows that actually moved. If a save fails the old order comes back and you get a snackbar.

The 5 item cap is enforced in the UI instead of waiting on the server's 400. Once you have five, "Set priority" greys out and says "Priority list is full (5)." underneath. You can still edit the five you have, since the API allows that. Loading, empty, and error states are all in.

I checked the reorder on the emulator instead of assuming: dragged the top card down a slot, watched two PUTs go out with the new order indexes, then confirmed against `GET /priorities` that the server agreed.

**Tests written for this feature:**

Six, all MockK against a mocked repo.

Four in `PrioritiesViewModelTest`: moving an item lands it in the right spot, dropping renumbers `orderIndex` and only saves the rows that moved, a failed save restores the old order, and dropping something back where it started doesn't call the API at all.

Two in `LibraryViewModelTest`: a sixth priority never reaches the API, and editing one you already have still works when you're at five.

I also broke each guard on purpose to make sure the tests actually caught it. Delete the cap check and that test goes red, put it back and it passes. Felt worth the two minutes, a test that passes for the wrong reason is worse than not having one.

**Known gaps or rough edges going into demos:**

The big one isn't mine to fix. There's no way to remove a single item from the list, so once your five are full they're full for good. My UI at least tells you that instead of pretending, but "you can never change your mind" is a rough place to land for a feature that's supposed to be about deciding what to do next.

Reordering is off whenever a filter chip is active. Dragging inside a filtered view would write positions from a 2 item list onto a 5 item list and scramble the real order. I think that's the right call but it does mean the drag handles vanish when you filter, which probably looks broken until someone explains why.

The drag itself has no automated test. My tests hit `moveItem` and `saveOrder` directly, so the math and the saving are covered but nothing proves the gesture actually calls them.

Smaller stuff: you can only change an item's level or hours or notes from the Library menu, not from the Priorities screen, which is honestly where you'd expect to do it. And I never built the tune icon the wireframe has in the Priorities top bar since I don't have a second thing for it to do.

---

## One Thing I Understood More Deeply

Across both weeks, the thing that changed is that I plan around what the API won't let me do, not just what it will.

Week 1 I found out `PUT /priorities` overwrites the whole row instead of merging, so sending just `priority` wipes the hours and notes. Week 2 I found out there's no delete at all. Neither is in the docs. In a normal week the handout tells you the shape up front and you just build it.

The no delete thing is what actually drove my week 2 design, and I didn't expect a missing endpoint to decide that much. The cap had to keep letting you edit your existing five, otherwise a full list is frozen instead of just closed. The message had to say "full" instead of something like "remove one first," because you can't. And I ended up checking the cap in the ViewModel too, not just hiding the menu item, since if my count is stale the user eats a 400 they can't do anything about. One thing I couldn't change, three decisions.

That's the part that felt different from a handout week. The handout version is "wire these two endpoints." The real version was figure out what they do, figure out what they refuse to do, then decide what to tell the user about it.

---

## One Thing I'm Still Confused About

I don't know if my drag is the right shape. It works, but it's like 50 lines of gesture math sitting right in the composable with the dragged index and the offset as `remember` state next to the LazyColumn. That feels like a lot of logic to have in UI code. Is the normal move to pull it into a state holder, wrap it in a custom Modifier, or is this actually fine because it genuinely is a view concern?

Same area, I don't know how you'd test a gesture. My tests call `moveItem` and `saveOrder` straight, which covers the math but says nothing about whether dragging actually triggers them. Is there a real way to test that in Compose or is that where you stop and check by hand?

The removal thing from last week is still open too. I built the whole cap around not being able to delete, so if there's an endpoint I missed I'd want to go back and redo that part.

---

## Anything Else *(optional)*

The two week split worked well here. Week 1 was wiring plus a read only screen, week 2 was making it interactive, so week 2 had something real to build on instead of starting cold. No shot I get drag to reorder working if I'm also fighting the endpoints that same week.

Getting assigned Priorities instead of picking was fine by me. The writeup said lighter on API surface and heavier on UI and that was exactly right, two endpoints and a whole lot of Compose.

Favorite moment of the sprint was debugging the drag. First two tries it did nothing and I couldn't tell if my code was broken or if the emulator just can't fake a long press. Instead of guessing I threw a log in the drag start and drag callbacks. One run and it was obvious, drag start fired with the right index and the drag callback never ran at all. That's not a code bug, that's the input never showing up, so I changed how I was sending the touch events and it worked right away. I keep relearning that "it's broken" and "I can't tell if it's broken" are different problems, and logging is what turns the second into the first.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Honest final-status report — what works end-to-end, what's rough, what's tested — plus a specific, genuine "Understood More Deeply" that reflects on the sprint as a whole, not just this week. | Present but vague, or only reports on this week rather than the feature's overall state. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** same as every other week — I check the link before grading.
