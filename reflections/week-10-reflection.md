# Week 10 Reflection

**Name:** Nathan Perfetti

**Date:** July 23, 2026

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/12

---

## Code Review

**Reviewed:** *(Dustin)*

**Link to my review:**

https://github.com/dmarsh31/media-tracker-android/pull/13

### What I Looked At



### What I Noticed



### Comments I Left



---

## One Thing I Understood More Deeply

Optimistic updates are mostly about flipping the order of two lines. All my earlier code went network first, state second, so every tap ate a full round trip before the UI moved. Part 2 is the same calls with the state write hoisted above the launch: stash a backup, update the list immediately, and only touch it again if the server says no. The lecture framing that made it stick is that HTTP verbs are promises: PUT and DELETE are idempotent (already gone = still gone), which is exactly why the remove and status change were safe to make optimistic, while POST is the one that can create the same thing twice, which is why a 409 on add must be treated as success and never roll back. The other thing that clicked was writing the rollback test. I deleted the rollback lines and watched the test go red, put them back, green. The slide said it straight, tapping it in the emulator tests one path, the network working on a good day, once. The rollback path needs its own test because nobody rechecks it by hand.

---

## One Thing I'm Still Confused About

The thing from lecture that bugged me: Retrofit's `isSuccessful` is just "was it 2xx", and that binary feels janky once you've seen status codes as a per endpoint contract. A 404 from `/library/{id}` is a normal answer, a 409 on a double save is basically success, but Retrofit lumps them all into "failure" and my repo has to keep patching over it with stuff like `isSuccessful || code() == 409`. It works, but every endpoint ends up re-deciding what failure even means, and I don't know what the grown up version of this looks like, a custom CallAdapter, some sealed response type, something else. Feels like everyone must hit this and I don't know the standard fix.

Also the test dispatcher stuff is still half magic. `Dispatchers.setMain(UnconfinedTestDispatcher())` makes `viewModelScope.launch` run inline so my asserts see the final state, but I couldn't tell you when I'd want `StandardTestDispatcher` and `advanceUntilIdle()` instead. And I'm not sure spamming the heart toggle fast is actually safe, each tap fires its own add or remove and I think last one wins, but that might just be lucky.

---

## Anything Else *(optional)*

Two small bugs came out of getting real data flowing. Books with a broken coverUrl rendered as blank white boxes because my null check passed and AsyncImage silently showed nothing, fixed with SubcomposeAsyncImage falling back to the type tile. And my change status dialog sat there doing nothing for a bit because the handlers were stubs waiting for part 2, dead UI reads as broken UI. Best trick of the night: airplane mode is a perfect rollback test rig, remove an item offline and you get to watch it come back with the error snackbar. "The network failing is not an edge case, it's Tuesday" is a good line and airplane mode makes it Tuesday on demand. Funniest failure: the undo snackbar kept expiring before adb could tap it, the app was literally faster than my test tooling. Also kind of satisfying that the lecture's cheat sheet was basically a checklist of things already in the app, the interceptor from week 5, cursor pagination from week 6, the 404 contract from week 8.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
