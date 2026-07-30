# Lightning Exploration — Student Guide

Everything you need for Week 13: how the presentation night works, how it's graded, and what's expected of you both as a presenter and as an audience member.

---

## Presentation format (15 min per pod, 9 pods)

1. **Demo — 3–4 min.** Show the running screen(s) your pod built in Week 9. If it didn't survive the semester, describe what you attempted and why.
2. **Three standard questions — 4–5 min.** What was surprisingly easy? What was surprisingly hard? Would you actually build something in this platform?
3. **Platform-specific Q&A — 5–6 min.** Walk through each question from your platform's set below — you'll already have a slide per question from your handout. The room may jump in with a follow-up.

---

## Grading

### Your pod's grade — Lightning Exploration (60 pts total)

Split 60% code/project, 40% presentation:

| Component | Weight | Points |
|---|---|---|
| Code & project (Week 9 build) | 60% | 36 |
| Presentation (Week 13) | 40% | 24 |

**Code & project (36 pts).** Based on what your pod actually built in Week 9 and can still show or describe in Week 13:
- Reached a running screen in your assigned platform (16 pts)
- The screen does something real — actual data/state, not just a static layout (10 pts)
- Evidence your pod engaged with the platform's core mental model (state, layout, navigation), not just copy-pasted a tutorial (10 pts)

**Presentation (24 pts).** Assessed live in Week 13:
- Demo is clear and your pod can speak to what is and isn't working (8 pts)
- The three standard questions are answered substantively and specifically **when you say them out loud** — "it was hard" isn't enough, you need to explain *why*. This is about your spoken answer, not your slide text: your slide can just say "State management" per the Slide Guidelines below — the reasoning is what you say, not what's on screen (8 pts)
- Platform-specific questions are answered with real detail, showing your pod actually explored the platform rather than skimming a quickstart (8 pts)

### Your individual requirement — Presentation Response Worksheet

**This is required, not extra credit, and it's a hard cutoff.**

Every student — not just presenters — fills out a Presentation Response Worksheet during the other 8 pods' presentations (template below). Here's exactly how it affects your grade:

- **Turn in a complete worksheet** (all 8 rows, both columns, real and specific answers) → you receive your pod's Presentation score as your own.
- **Turn in an incomplete, missing, or generic worksheet** (a skipped row, or an answer that isn't actually specific to that pod's demo — "it was cool" doesn't count) → **your personal Presentation score is 0**, no matter how well your own pod presented.

There's no partial credit on this. Fill in all 8 rows with real answers, or your Presentation score is zero — full stop.

Due at the end of Week 13, collected before Course Closing.

---

## Presentation Response Worksheet (template)

One row per **other** pod (8 rows — skip your own pod).

| Pod | Platform | One specific thing you learned from their demo/discussion | In your own words, how did the pod answer one of their platform-specific questions? (pick one from their set below — this is what *they* said, not your own take) |
| --- | -------- | --------------------------------------------------------- | --------------------------------------------------------------------------------------- |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |
|     |          |                                                           |                                                                                         |

---

## Slide Guidelines — Don't Read Your Slides

Your slides are a visual aid for the audience, not a script for you. If a slide has full sentences on it, you'll end up reading them out loud — and after the fourth pod does that in a row, everyone checks out. Part of your presentation grade is the quality of the presentation itself, not just whether the content is correct.

- **6-word rule.** Roughly 6 words per line, 6 lines per slide, max. If your answer needs more than that, that's what you say out loud, not what you type.
- **Fragments, not sentences.** "State management — harder than expected" not "We found that managing state was surprisingly difficult because of X."
- **Show, don't describe.** A screenshot or a short code snippet beats three sentences explaining what the audience could just see.
- **Legible from a distance.** Good habit even on Zoom — if you'd have to shrink the font to fit your text, you have too much text. Cut it, don't shrink it.
- **Test: could you give this talk with the slides turned off?** If the answer is no because you'd forget what to say, that's fine — index cards exist. If the answer is no because the slide *is* the content, rework it.

---

## Platform-specific question sets

Every pod should be ready to answer their own platform's questions during their slot. When you're in the audience, draw from the presenting pod's set for your worksheet.

### iOS (native, Swift/SwiftUI)
- How closely did `@State`/`@Binding` map onto Compose's state-hoisting model? Where did the mental model diverge?
- SwiftUI only targets Apple platforms — what did you gain or give up by being tied to one vendor's tooling?
- How did the Xcode Previews iteration loop compare to Compose Previews?
- If you had to ship the same app on Android and iOS, what would you actually share vs. rebuild from scratch?

### React Native
- React Native renders through a bridge to real native components — did that show up anywhere, like a control that looked or behaved slightly off from what you expected?
- How did Flexbox-based layout compare to Compose's `Row`/`Column`/`Box` + `Modifier` system?
- JSX mixes markup and logic in one file. Compare that to Compose's function-based composables — which did you prefer, and why?

### Flutter
- Flutter draws every pixel itself instead of using native platform widgets. Did you notice? Did the app feel native, or did something feel slightly off?
- How did Flutter's hot reload compare to Compose's live preview / instant run for iteration speed?
- "Everything is a widget" vs. "everything is a Composable" — where did that analogy hold up, and where did it break down?

### .NET MAUI
- MAUI compiles to real native controls per platform, similar in spirit to React Native or KMP but in C#/XAML. How did the environment setup (Visual Studio, project structure) compare to Compose in Android Studio?
- XAML markup lives in a separate file from your C# code, unlike Compose where UI is inline Kotlin. How did that split change how you worked?
- MAUI targets more platforms than almost anything else explored tonight (Windows, macOS, Tizen, plus mobile). Does "write once, run everywhere" feel more real or less real after actually trying it?

### Ionic (Capacitor)
- Ionic apps are web apps wrapped in a native shell via Capacitor. Where did that show — did anything feel like "a website in an app," and where did it feel indistinguishable from native?
- How did state management in your web framework (Angular/React/Vue) compare to Compose's state hoisting?
- Capacitor plugins bridge web code to native device APIs like camera or GPS. How does that compare to how Kotlin/Compose apps call platform APIs directly?

### KMP (Kotlin Multiplatform)
- KMP's pitch is shared logic (or shared UI, if you tried Compose Multiplatform) with platform-specific rendering. What did you actually end up sharing in three hours, and what stayed platform-specific?
- This was flagged as the most complex setup of any platform this year — what specifically ate your time?
- If shared logic is the appeal, what's the catch? What did you end up having to write twice anyway?

### Progressive Web App (PWA)
- A PWA has no app-store install and no platform SDK build step. What did you gain by skipping that, and what did you lose?
- Compose has an explicit state/lifecycle model (`ViewModel`, `collectAsState`). What's the rough equivalent in a plain web app, and did it feel more or less structured?
- Would a user actually notice they were using a "web app" instead of a native one? What gave it away, if anything?

### Wear OS (Compose for Wear OS)
- You already knew Compose going in — what specifically changed about writing it for a small, round screen with limited attention?
- Wear OS has its own Material components (`WearComposeMaterial`) instead of reusing the mobile ones. What had to be different, and why do you think that split exists?
- What's a feature you'd genuinely want on a watch that you wouldn't want on a phone?

### Android TV (Compose for TV)
- There's no touch input at all — everything is D-pad/remote navigation. How did that change how you thought about focus and what's "clickable"?
- Compose for TV is still the same underlying Compose model. What carried over cleanly, and what had to be rethought for a "10-foot" experience?
- Pick a screen from Media Tracker (search, library, review). Would it work on a TV, or would it need to be rethought? Why?
