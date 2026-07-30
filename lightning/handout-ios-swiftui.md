# iOS (Swift/SwiftUI) — Lightning Exploration Handout

**Pod:** Pod I (all-Mac)

## Your goal tonight

Get one running screen on the iOS Simulator by the end of class. It doesn't need to be complete or polished — it needs to run. You have most of tonight's 3:45 class session; don't spend more than the first 30–45 minutes on setup.

## Getting started

- Quickstart: [developer.apple.com/tutorials/swiftui](https://developer.apple.com/tutorials/swiftui)
- Open Xcode, choose **File → New → Project → App**, select SwiftUI as the interface. Let Xcode finish indexing before you start editing — it's slow on a fresh project.
- Run on the iOS Simulator (not a physical device) — no signing/provisioning needed.

## Tips for making the most of tonight

- Start with a `List` and `ForEach` over a hardcoded array of a simple `struct` (mimic Media Tracker's shape — an id, a title, an image name). That's your fastest path to something real on screen.
- `@State` and `@Binding` are SwiftUI's version of Compose's state hoisting — if you know `remember { mutableStateOf(...) }`, the mental model transfers, but the syntax won't look familiar at first. Don't fight it, just try it.
- SF Symbols (`Image(systemName:)`) are a fast way to get icons without adding assets.
- If your pod has more than one Mac, don't all build the same thing in parallel — split into "screen builder" and "docs reader," and swap.

## If you finish early

Add a second screen (a detail view) and navigate to it with `NavigationStack`. Or replicate one of the actual Media Tracker screens from memory in SwiftUI.

## Slide Guidelines — Don't Read Your Slides

Your slides are a visual aid for the audience, not a script for you. If a slide has full sentences on it, you'll end up reading them out loud — and after the fourth pod does that in a row, everyone checks out. Part of your presentation grade is the quality of the presentation itself, not just whether the content is correct.

- **6-word rule.** Roughly 6 words per line, 6 lines per slide, max. If your answer needs more than that, that's what you say out loud, not what you type.
- **Fragments, not sentences.** "State management — harder than expected" not "We found that managing state was surprisingly difficult because of X."
- **Show, don't describe.** A screenshot or a short code snippet beats three sentences explaining what the audience could just see.
- **Legible from a distance.** Good habit even on Zoom — if you'd have to shrink the font to fit your text, you have too much text. Cut it, don't shrink it.
- **Test: could you give this talk with the slides turned off?** If the answer is no because you'd forget what to say, that's fine — index cards exist. If the answer is no because the slide *is* the content, rework it.

## Your slide deck (Week 13, 15-minute slot)

One slide per question — 10 slides total:

1. **Title** — pod members, iOS/SwiftUI.
2. **What we built** — screenshot or short screen recording of your running screen.
3. **What was surprisingly easy?**
4. **What was surprisingly hard?**
5. **Would you actually build something in SwiftUI?**
6. How closely did `@State`/`@Binding` map onto Compose's state-hoisting model? Where did the mental model diverge?
7. SwiftUI only targets Apple platforms — what did you gain or give up by being tied to one vendor's tooling?
8. How did the Xcode Previews iteration loop compare to Compose Previews?
9. If you had to ship the same app on Android and iOS, what would you actually share vs. rebuild from scratch?
10. **What we'd do with more time** — closing thought.

Full grading rubric and question sets: [lightning-exploration-student-guide.md](lightning-exploration-student-guide.md).
