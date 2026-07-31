# Week 11 Reflection — Bonus Feature Sprint (Week 1 of 2)

**Name:** Nathan Perfetti

**Date:** July 30, 2026

**My assigned bonus feature:** Priorities

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/13

---

## Code Review

**Reviewed:** Dustin

**Link to my review:**

https://github.com/dmarsh31/media-tracker-android/pull/14

### What I Looked At

Dustin's week-11 PR (#14), which is his bonus feature, Write Review. Focused on `WriteReviewViewModel` and the `createReview` method in `DefaultMediaRepository` since that is where the POST actually happens, then the new `StarRow` composable and the screen that uses it.

### What I Noticed

His repo layer is the strongest part. `createReview` returns a `Result<Review>` and pulls the 409 out into its own `ReviewAlreadyExistsException`, so "you already reviewed this" is a real case instead of a generic failure string. Three gaps though. The big one is that `postReview` does nothing visible on success, it just clears the error message, so you tap Post, the review actually posts, and the screen sits there. Tap again and you get "you already reviewed this item," which makes a working feature look broken. Second, `shareToFeed` defaults to false in `CreateReviewRequest` and `postReview` never passes it, so no review ever reaches the activity feed, and the checkbox the stub asked for is not built. Third, the Post button is always enabled and the missing-rating case gets caught with a snackbar after the tap, where the stub wanted it disabled until at least one star.

### Comments I Left

Four. Props on the Result plus 409 handling and the star row, then the no-feedback-on-success bug, the `shareToFeed` flag that is always false, and the Post button that should be disabled until a star is picked.

---

## Bonus Feature Progress

**What's working:**

Both endpoints are wired and running on real data. `GET /priorities` backs a new Priorities screen that you reach from the tune icon on My Library, sorted by `orderIndex`, with each card showing the cover, title, a priority badge, and the "Est. 6 hours . <note>" line from the wireframe. `PUT /priorities` is wired to a Set Priority dialog on the Library card overflow menu for any Want To item, where you pick High/Medium/Low, estimated hours, and an optional note. I watched the PUT go out in Logcat with the right body and come back 200, and the item shows up on the Priorities screen after. The All/High/Medium/Low filter chips work, filtering client side by level. Priorities hangs off the Library tab so the bottom nav stays lit on Library while you are in there.

**What's still stubbed, fake, or not started:**

Drag to reorder is not built yet, and neither is the 5 item cap in the UI. Both are next week's targets. I also deliberately left two things off the screen that the wireframe shows: the tune icon in the Priorities top bar and the "Drag to reorder" hint text. Neither can do anything until the drag lands, and last week I shipped a change status dialog whose handlers were still stubs, which read as broken rather than unfinished. I would rather have a smaller screen that works.

**What I'm blocked on, if anything:**

Not blocked on shipping, but I need an answer on removing a single item from the priority list before I build next week's UI around the cap. See below.

---

## One Thing I Understood More Deeply

I hit the API with curl before writing any Kotlin this time, and it changed the design. `PUT /priorities` replaces the whole row instead of merging into it. I sent `{"mediaId":450,"priority":3,"orderIndex":0}` to change just the level and the `estimatedTimeHours` and `notes` I had set a minute earlier both came back null. Nothing in the docs says that. If I had written the repo method the way I assumed it worked, sending only the fields the user touched, people would silently lose their notes every time they bumped a priority, and I would not have caught it until someone complained. So the repo method takes the whole object and always sends all five fields.

The related thing that clicked is that `priority` and `orderIndex` are two different numbers doing two different jobs. `priority` is how urgent it is (1 to 3), `orderIndex` is where it sits in the list. Two items can both be High and still have a defined order between them. I almost derived one from the other before realizing the list would stop being sortable the way the wireframe wants.

Building my own feature instead of following a handout is mostly this: the handout would have told me the request shape, and instead I had to go find out that the shape has a rule attached to it that is not written down anywhere.

---

## One Thing I'm Still Confused About

There is no way to remove one item from the priority list and I cannot tell if that is on purpose. `DELETE /priorities/{mediaId}` returns 405. Sending `priority: null` just resets it to 2. Sending `priority: 0` returns a 500. Taking the item out of my library does not clear its priority either. The feature doc says the intended move is to "PUT a new list without the removed item," but PUT only takes a single object and returns `MISSING_FIELDS` if you hand it an array.

That matters because the list is capped at 5 and the 6th add is a hard 400. Once you fill it, it is full forever. You can edit the five you have but you can never swap one out. Either I am missing an endpoint or the API genuinely does not support it, and next week I have to enforce that cap in the UI, so I would rather know which before I build a dead end.

Smaller one: I do not know whether a full replace PUT like this should be modeled differently on the client. Right now every caller has to remember to pass all five fields or it quietly wipes data, and "remember to do the right thing" is the kind of rule that gets broken three weeks later.

---

## Anything Else *(optional)*

The wireframe shows "Est. 2.5 hours" but `estimatedTimeHours` is an integer and a decimal comes back as a 500, so the hours input is digits only. Worth flagging since the mock implies something the API will not take.

One small bug I caught: my filter chips came out pink instead of purple. `FilterChip` defaults to `secondaryContainer` for the selected state and our secondary is the rose accent. The chips on Search and Library already override that to `primaryContainer`, I just had not copied the colors block over. Good argument for looking at how the existing screens do a thing before writing a new one.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Concrete progress report (what's wired, what's not) plus specific, honest "Understood More Deeply" and "Still Confused" sections. | Present but vague — "I worked on my feature" with no specifics on what's actually working. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match.
