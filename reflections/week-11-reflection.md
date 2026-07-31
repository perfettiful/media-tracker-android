# Week 11 Reflection

**Name:** Nathan Perfetti

**Date:** July 30, 2026

---

## Commits This Week

**Link:**

https://github.com/perfettiful/media-tracker-android/pull/13

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

I started this one by hitting the API with curl before writing a single line of Kotlin, and that turned out to matter more than usual. `PUT /priorities` replaces the whole row instead of merging into it. I sent `{"mediaId":450,"priority":3,"orderIndex":0}` to change just the level, and the `estimatedTimeHours` and `notes` I had set a minute earlier both came back null. Nothing in the docs says that. If I had written the repo method the way I assumed it worked, sending only the fields the user touched, people would have silently lost their notes every time they bumped a priority, and I probably would not have noticed until someone complained.

So the repo method takes the whole object and always sends all five fields. It also changed how the reorder has to work, since moving one card means re-sending every row that shifted, with its hours and notes riding along.

The related thing that clicked is that `priority` and `orderIndex` are two different numbers doing two different jobs. `priority` is how urgent it is (1 to 3), `orderIndex` is where it sits in the list. Two items can both be High and still have a defined order between them. I almost wrote one field and derived the other before I realized the list would stop being sortable the way the wireframe wants.

---

## One Thing I'm Still Confused About

There is no way to remove one item from the priority list and I cannot tell if that is on purpose. `DELETE /priorities/{mediaId}` returns 405. Sending `priority: null` just resets it to 2. Sending `priority: 0` returns a 500. Taking the item out of my library does not remove its priority either. The feature doc says the intended move is to "PUT a new list without the removed item," but PUT only takes a single object and returns `MISSING_FIELDS` if you hand it an array.

That matters because the list is capped at 5 and the 6th add is a hard 400. So once you fill it, it is full forever. You can edit the five you have but you can never swap one out. Either I am missing an endpoint or the API genuinely does not support it, and I would like to know which before I build the UI around it next week.

Smaller one: I do not know whether a full replace PUT like this should be modeled differently on the client. Right now every caller has to remember to pass all five fields or it quietly wipes data, and "remember to do the right thing" is exactly the kind of rule that gets broken later.

---

## Anything Else *(optional)*

The wireframe shows "Est. 2.5 hours" but `estimatedTimeHours` is an integer and a decimal comes back as a 500, so the hours input is digits only. Worth flagging since the mock implies something the API will not take.

I left two things off the screen on purpose. The wireframe has a tune icon in the Priorities top bar and a "Drag to reorder" hint, and neither can do anything until the drag lands next week. After last week, where my change status dialog sat there doing nothing because the handlers were still stubs, I would rather ship a smaller screen that works than a complete looking one with dead controls in it.

One small bug I caught: my filter chips came out pink instead of purple. `FilterChip` defaults to `secondaryContainer` for the selected state, and our secondary is the rose accent. The chips on Search and Library already override that to `primaryContainer`, I just had not copied the colors block over. Good argument for looking at how the existing screens do a thing before writing a new one.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
