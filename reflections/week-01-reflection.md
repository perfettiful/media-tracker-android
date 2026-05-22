# Week 1 Reflection

**Name:** Nathan Perfetti
**Date:** May 21 2026

---

## Commits This Week

<!-- Paste a link to your commits for this week. The easiest way: go to your repo on GitHub,
     click "commits", and copy the URL after filtering by your name or branch. -->

**Link:**

https://github.com/benjamincassidymetro/media-tracker-android/pull/3/commits

---

## Code Review

<!-- Every week you leave a review on a pod mate's pull request. Fill in both parts below.
     Part 1 is the link — I will verify the review exists on GitHub.
     Part 2 is your written assessment — what you actually looked at and what you found. -->

**Reviewed:** *(Brian)*

**Link to my review:**
https://github.com/Sacchi-X/media-tracker-android/pull/1/changes#r3285410597

### What I Looked At

<!-- Walk through the code you reviewed. What was the PR trying to do? Which files or
     functions did you focus on? -->
     He updated some basic settings and imports for the theme layout.

### What I Noticed

<!-- Be specific. Did you spot a potential bug? A pattern that could cause problems? Something
     done well that you want to call out? "I looked at the ViewModel and everything seemed fine"
     is not specific enough. Name the thing you noticed and explain why it matters. -->
     I noticed a diff on the gradle properties, unless he was deliberately experiemnting, it seemed like the type of config change the IDE might make, go unnoticed, and that you wouldn't want to commit and only keep local.

### Comments I Left

<!-- Briefly summarize the comments you left on the PR. If you left a positive comment,
     say what it was. If you left a suggestion, say what you suggested and why. -->
     
     I left a question about diff a diff on this gradle properties as it may have been deliberate or accidental, in which he may revert the change.

---

## One Thing I Understood More Deeply

<!-- Be specific. Don't write "I learned about ViewModels." Write what specifically clicked —
     what was confusing before, what made it make sense, and how you'd explain it to someone else.
     There are no wrong answers here. -->

I was able to setup Android Studio / AVD on my M3 Macbook relatively painlessly. It was a contrasting DevEx compared to challenges I've had setting up on a Windows machine or even an Intel Mac. The ease of setup created mental intertia that lead me to want to tackle the substantive task at hand within the project itself.

---

## One Thing I'm Still Confused About

<!-- Be honest. This is the most useful part of the reflection for me — it tells me where to
     spend more time in class. You will not lose points for being confused. -->
     I'mm a bit confused why we forked from Benjamin's repo, to then direct PRs to that repo. I think it may just be the easiest for him to grade. But I usually associate PRs with actual changes that would get merged. 

---

## Anything Else *(optional)*

<!-- Did you help a pod mate work through something? Did you discover something cool or frustrating?
     Did something from a previous week finally click? This is a good place to put it. -->
Brian and Jama are chill and I look forward to working together and helping each other out.

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
