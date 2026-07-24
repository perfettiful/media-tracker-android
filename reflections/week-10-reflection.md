# Week 10 Reflection

**Name:** Nathan Perfetti

**Date:** July 23, 2026

---

## Commits This Week

**Link:**

*(paste PR link after pushing week-10)*

---

## Code Review

**Reviewed:** *(pod mate's name)*

**Link to my review:**

### What I Looked At

*(fill in after doing the review)*

### What I Noticed

*(fill in after doing the review)*

### Comments I Left

*(fill in after doing the review)*

---

## One Thing I Understood More Deeply

Optimistic updates are mostly about flipping the order of two lines. All my earlier code went network first, state second, so every tap ate a full round trip before the UI moved. Part 2 is the same calls with the state write hoisted above the launch: stash a backup, update the list immediately, and only touch it again if the server says no. The part that actually made it click was writing the rollback test. I deleted the rollback lines and watched the test go red, put them back, green. First time a test caught the exact thing it claims to test in front of me instead of me just trusting it. Also learned a 409 must not roll back, the server saying "already there" means the optimistic state was right all along.

---

## One Thing I'm Still Confused About

The test dispatcher stuff is still half magic to me. `Dispatchers.setMain(UnconfinedTestDispatcher())` makes `viewModelScope.launch` run inline so my asserts see the final state, but I couldn't tell you when I'd want `StandardTestDispatcher` and `advanceUntilIdle()` instead, or what breaks if the coroutine has a real delay in it. Also not sure what happens if you spam the heart toggle fast, each tap fires its own add or remove and I think last one wins, but I don't know if that's guaranteed or just lucky.

---

## Anything Else *(optional)*

Two small bugs came out of getting real data flowing. Books with a broken coverUrl rendered as blank white boxes because my null check passed and AsyncImage silently showed nothing, fixed with SubcomposeAsyncImage falling back to the type tile. And my change status dialog sat there doing nothing for a bit because the handlers were stubs waiting for part 2, dead UI reads as broken UI. Best trick of the night: airplane mode is a perfect rollback test rig, remove an item offline and you get to watch it come back with the error snackbar. Funniest failure: the undo snackbar kept expiring before adb could tap it, the app was literally faster than my test tooling.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
