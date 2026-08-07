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

**Reviewed:** *()*

**Link to my review:**


### What I Looked At



### What I Noticed



### Comments I Left



---

## Bonus Feature — Final Status

**What works end-to-end, right now:**

The whole loop works on real data. From My Library you tap the tune icon and land on a Priorities screen backed by `GET /priorities`, sorted by `orderIndex`, each card showing the cover, title, priority badge, and the "Est. 6 hours . <note>" line from the wireframe. You set a priority from the Library card overflow menu on any Want To item, picking High/Medium/Low, estimated hours, and an optional note, which goes out as `PUT /priorities`.

Drag to reorder is the big one this week and it is working. Long press a card, drag it, and on drop the new order saves. I hand rolled it with `pointerInput` and `detectDragGesturesAfterLongPress` instead of pulling in a library. It reads `listState.layoutInfo.visibleItemsInfo` to figure out which card you grabbed, and once you drag past one card height plus the 8dp gap it swaps with that neighbour and keeps the leftover offset so the card stays under your finger. On drop it renumbers `orderIndex` 0..n and only PUTs the rows that actually moved. A failed save puts the old order back and shows a snackbar.

The 5 item cap is enforced in the UI, not by waiting for the server's 400. Once you have five, "Set priority" greys out with "Priority list is full (5)." underneath. Editing one you already set still works at the cap, because the API allows that. Loading, empty, and error states are all there.

I verified the reorder on the emulator rather than trusting it: dragged the top card down one slot, watched exactly two PUTs go out (`411 -> orderIndex 0`, `450 -> orderIndex 1`), and confirmed the resulting order against `GET /priorities`.

**Tests written for this feature:**

Six, all MockK against a mocked repository.

Four in `PrioritiesViewModelTest`: moving an item puts it in the new spot, dropping renumbers `orderIndex` top to bottom and only saves the rows that moved, a failed save restores the old order and sets the error, and dropping an item back where it started makes no API call at all.

Two in `LibraryViewModelTest`: a sixth priority never reaches the API, and editing one that is already on the list still works at the cap.

I mutation checked both guards. Break the cap check or the rollback and the matching test goes red, put it back and it passes. Worth doing because a test that passes for the wrong reason is worse than no test.

**Known gaps or rough edges going into demos:**

The big one is not mine to fix: there is no way to remove a single item from the priority list. Once your five are full they are full permanently. My UI is honest about it (the action greys out and says why) but "you can never change your mind" is a bad end state for a feature about deciding what to do next.

Reordering is disabled whenever a filter chip is active. Dragging inside a filtered view would write positions computed from a 2 item list onto a 5 item list, so I only allow it on All. Defensible, but it means the drag handles disappear when you filter, which looks like the feature broke until you realize why.

The drag has no automated test. My tests cover the ViewModel's `moveItem` and `saveOrder`, not the gesture itself.

Smaller stuff: you can only change an item's level, hours, or notes from the Library card menu, not from the Priorities screen itself, which is where you would expect to. And the wireframe's tune icon in the Priorities top bar is not built, since I do not have a second thing for it to do yet.

---

## One Thing I Understood More Deeply

Looking at both weeks together, the shift is that I now design around what the API cannot do, not just what it can.

Week 1 I found out `PUT /priorities` replaces the whole row instead of merging, so sending just `priority` nulls out the hours and notes. Week 2 I found out there is no delete at all. Neither of those is in the docs, and neither is something a handout week would have made me deal with, because a handout tells you the shape up front.

The second one actually drove the design. The cap had to allow editing at five, or a full list becomes completely frozen instead of just closed. The message had to say "full" rather than something hopeful like "remove one first," because there is no way to remove one. And it pushed the cap check into the ViewModel as well as the menu, since being wrong about the count means the user eats a 400 they can do nothing about. A constraint I could not change ended up shaping three separate decisions.

That is the part that feels different from following a handout. The handout version of this feature is "wire these two endpoints." The real version was: find out what the endpoints actually do, find out what they refuse to do, then decide what the UI should say about it.

---

## One Thing I'm Still Confused About

I do not know if my drag implementation is the right shape. It works, but it is about 50 lines of gesture math living directly in the composable, with the dragged index and the accumulated offset as `remember` state right next to the LazyColumn. That feels like a lot of logic to have sitting in UI code, and I do not know whether the normal move is to pull it into a state holder object, wrap it in a custom Modifier, or just leave it because it is genuinely view concern.

Related, I do not know how you would test a gesture. My tests call `moveItem` and `saveOrder` directly, which proves the reordering math and the save behavior but says nothing about whether the drag actually triggers them. Is there a real way to test that in Compose, or is that the line where you stop and verify by hand?

And the removal question from last week is still open. I built the cap around not being able to delete, so if there is an endpoint I missed, I would want to redo that part.

---

## Anything Else *(optional)*

The two week split worked well for this feature. Week 1 was wiring and a read only screen, week 2 was making it interactive, and that meant week 2 had something real to build on instead of starting cold. I do not think I would have gotten drag to reorder working if I had also been fighting the endpoints that week.

Being assigned Priorities rather than picking was fine. The description said it was lighter on API surface and heavier on UI, which turned out to be exactly right, two endpoints and a lot of Compose.

Best debugging moment of the sprint: my drag did nothing on the first two tries and I could not tell whether my gesture code was broken or the emulator just could not fake a long press. Instead of guessing, I dropped a log into `onDragStart` and `onDrag`. One run answered it: `onDragStart` fired with the correct index, `onDrag` never ran at all. That is not a code bug, that is the input not arriving, so I switched from `adb input draganddrop` to manual `motionevent` DOWN, hold, slow MOVEs, UP, and it worked immediately. The lesson I keep relearning is that "it does not work" and "I cannot tell if it works" are different problems, and logging is how you turn the second into the first.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Honest final-status report — what works end-to-end, what's rough, what's tested — plus a specific, genuine "Understood More Deeply" that reflects on the sprint as a whole, not just this week. | Present but vague, or only reports on this week rather than the feature's overall state. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** same as every other week — I check the link before grading.
