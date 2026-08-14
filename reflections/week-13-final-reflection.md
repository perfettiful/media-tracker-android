# Final Reflection, ICS 342

**Name:** Nathan Perfetti

**Date:** August 13, 2026

**Final pull request:** https://github.com/perfettiful/media-tracker-android/pull/14

---

## Part 1: Your App

**1a. Find the commit you are most proud of. Paste the URL and explain why you chose it. What did it take to get there? What was broken before and what worked after?**

> Your answer:

https://github.com/perfettiful/media-tracker-android/commit/7501b8d0497cbc7c8c773d92652d451938ba336a

Drag to reorder on the Priorities screen. I picked it because nobody told me what the answer looked like. The spec said drag to reorder, or grab a library if you don't want to write the gesture logic. I wrote it.

Before this the list was read only. You could set a priority but the order was whatever the server gave you.

The gesture wasn't the hard part, the bookkeeping was. You track how far the finger has moved, and when it passes a whole card you swap with the neighbor. What I kept getting wrong was the leftover distance after the swap. Got it wrong twice before it felt right.

Then it didn't work at all and I couldn't tell if my code was broken or if the emulator just can't fake a long press. I put a log in both callbacks. One run showed the drag starting fine and the move events never arriving, so it was never my code. That's really why I'd pick this commit.

Now you long press, drag, let go, and it saves. It renumbers the list and only sends rows that actually moved. Failed save puts the old order back with a snackbar.

---

**1b. Name one screen you think is genuinely well-built. Explain why: what decisions did you make, what did you refactor, and how does it differ from how you'd have done it in week 2?**

> Your answer:

Media Detail. It's the screen I rewrote the most and it shows.

State is a sealed class with Loading, Loaded, NotFound and Error, and the screen is a `when` over the four. NotFound is separate from Error because a 404 on `GET /media/{id}` and a dead network aren't the same problem. NotFound gets no Retry button, since retrying something that doesn't exist is pointless.

The load fires four calls at once instead of in a row: detail, reviews, library status, favorite check. Only detail can fail the screen, and if it does the other three get cancelled. That was a refactor. My first version ran them sequentially and you could feel it.

The buttons are optimistic. Tap the heart, it fills immediately, request goes out behind it, flips back with a snackbar if the server says no.

Week 2 me would have put all of it in the composable. One isLoading boolean, one error string, and a spinner that never goes away when something throws. I know because that's what my login screen looked like, and I ended up leaving that same comment on a pod mate's PR months later.

---

**1c. Name one screen or feature you're not satisfied with. What's wrong with it? With one more week, what would you change?**

> Your answer:

The Feed. First thing you see after logging in and it's completely fake. It reads from FakeMediaRepository, so you get Jordan Smith and Priya Patel doing things to media that has nothing to do with your account. The mock items carry mock ids too, so tapping one lands on a detail screen that 404s. I hit that during testing more than once and had to remind myself it wasn't a real bug.

There's a `GET /activity` endpoint I never wired, because Feed was never the week's assignment and I kept spending time on whatever was due.

One more week and I'd wire it with the cursor pagination I already built for Search, then delete the mock data instead of leaving it as a fallback. Profile needs the same treatment. Favorites come from the API but the recently tracked list under it is still fake, and half real is more confusing than either.

---

## Part 2: A Specific Bug

**2a. Describe the hardest bug you fixed this semester. What was the symptom? What did you think it was at first? What was it actually? How did you find it?**

> Your answer:

After logging in, tapping any bottom nav tab threw you back to the login screen.

This one cost me the most because I lived with it for weeks. Hit it while testing Library, assumed I'd mistapped or my session expired, logged back in, kept going.

My first theory was auth. You get sent to login, so the token must be bad. Then I thought my ON_RESUME refresh was firing on an expired token. Neither held up, because Logcat had no 401s anywhere. Nothing was failing. The app was navigating there on purpose.

It was navigation. The graph starts at `login`, and the login flow pops itself with `popUpTo(Routes.LOGIN) { inclusive = true }`. But the bottom bar was doing `popUpTo(navController.graph.startDestinationId)`, and the graph's start destination is still login. Every tab tap was unwinding to a screen that had already been removed.

I found it by dropping the theories and reading the two pieces of back stack code next to each other. `startDestinationId` doesn't say login anywhere in the name, which is why I skimmed past it for a month.

---

**2b. Paste the lines you changed. Explain in plain English what the fix does and why it works.**

> Your answer (include code):

In `navigation/BottomNavBar.kt`:

```kotlin
// before
onClick = {
    navController.navigate(item.route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
},

// after
onClick = {
    // the graph starts on login and login gets popped once youre in,
    // so popping to the start destination bounced you back to it.
    // feed is the real home of the tabs
    navController.navigate(item.route) {
        popUpTo(Routes.ACTIVITY_FEED) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
},
```

`popUpTo` clears the back stack down to the screen you name before going to the new one. A bottom nav needs that, otherwise the stack grows every tab switch and back walks you through all of them instead of leaving the app.

I just named the wrong screen. `graph.startDestinationId` sounds like home but it means the first destination declared in the graph, which is login. Naming `ACTIVITY_FEED` fixes it because that's where the tabs actually live. saveState and restoreState still work the same, each tab keeps its scroll position.

What bugs me is the symptom pointed at auth and the cause was in a file I hadn't opened in a month.

---

## Part 3: What You Actually Learned

**3a. Pick the concept that took longest to actually understand. What did you think it was, what changed, and how would you explain it now?**

> Your answer:

That status codes mean whatever the endpoint says they mean, not one universal thing.

What I thought: 2xx worked, 4xx is the user's fault, 5xx is the server's, anything not 2xx is a failure so catch it and show an error. I built weeks of the app that way.

What broke it was `GET /library/{mediaId}`. It returns 404 when the item isn't in your library, which isn't an error, it's the answer, and it's the answer most of the time. Under my old assumption, opening detail for anything you hadn't saved threw a full error screen. Meanwhile a 404 from `GET /media/{id}` means the item really doesn't exist, and that one is an error. Same code, opposite meaning, one endpoint apart.

After that the rest followed. A 409 when you favorite something means it's already favorited, which is what you wanted, so rolling back would be wrong. A 404 on delete means it's already gone.

Explaining it to week 2 me: stop asking whether the call failed, ask what that endpoint is telling you. In practice that means `Response<T>` in Retrofit so you can read `.code()` yourself instead of letting anything non-2xx throw before you see it.

---

**3b. Look back at your early reflections, weeks 1 through 4. Find something you were confused about. Are you still? If it clicked, when and how?**

> Your answer:

From week 4:

> The thing I am still stuck on is why `viewModel()` is written as a generic. In `RegisterScreen.kt` the call is `viewModel: RegisterViewModel = viewModel()`, and the tooltip in Studio shows it as `<reified VM>`. I know `reified` means the runtime can see the type, but I do not get why this needs to be a generic function instead of just a regular one that returns my `RegisterViewModel` directly.

Not stuck on it anymore.

What I was missing is that `viewModel()` usually isn't building anything, it's a lookup. Every nav destination has a ViewModelStore, basically a map, and the type is the key. So the call means give me the RegisterViewModel for this screen and make one if it isn't there. The generic is how it knows what to look under, and reified is what lets it read the type at runtime instead of me passing `RegisterViewModel::class.java`.

Rotation made it real. Library keeps the status filter in the ViewModel and the type chips in the composable with rememberSaveable. Rotate the phone and the ViewModel value is still there, because the store outlived the composable and `viewModel()` handed back the same object.

The flexibility half of my question got answered later. When I needed ViewModels with constructor arguments, ProfileViewModel and then SettingsViewModel, I used `viewModel(factory = ...)`. That only works because it's generic. Hardcoded to one type, it couldn't take a factory that builds a different one. So the thing I was annoyed about in week 4 is what let me do what I wanted in week 12.

---

**3c. Name one thing a pod mate said or showed you that changed how you approached something.**

> Your answer:

Dustin, reviewing my week 10 PR. He said I hadn't disabled the Save button on Media Detail so it could get spammed, and that could leave the UI and server out of sync.

He was right, and it bugged me because I'd been staring at that button all week. I was thinking about whether the request worked. He was thinking about someone tapping it four times fast, which people do and which I never do when testing my own feature.

It changed the question I ask when I finish a control. Now it's what happens if someone taps this twice, taps it with no signal, or taps it and backs out immediately. The Save button became an optimistic toggle with a guard. It also made my reviews of his PRs better, most of what I flagged after that was what happens when this fails instead of style stuff.

---

## Part 4: Your Bonus Feature

**4a. Describe your bonus feature in one paragraph as if you were explaining it to someone who has never used an app before.**

> Your answer:

The app keeps a list of books, movies and shows you want to get to eventually, and that list gets long fast. My feature pulls five of them out into a short list of what's actually next. Each one gets marked high, medium or low, plus roughly how many hours it'll take and a note about why it's there, like a friend recommended it or you're saving it for movie night. Then you drag them into whatever order you want and it stays that way. You'd want it because picking what to watch is its own chore, and this lets you do that thinking when you're not tired. Later when you've got a free evening you're looking at five things instead of forty, and you can see one is two hours and another is six, so you take whichever fits.

---

**4b. What was the technically hardest part? Name a specific function, flow or data structure and explain the problem.**

> Your answer:

The `onDrag` callback, specifically the running offset.

I track which index the finger grabbed and how far it's moved. Once that passes the height of a card, the item swaps with its neighbor. What I kept getting wrong was what to do with the distance afterward.

First version swapped and left the offset alone. The offset is what visually shifts the card, so after swapping the card was still drawn a full card away from the finger, so the code immediately decided it had passed another threshold and swapped again. One drag sent the item to the bottom of the list.

The fix is subtracting one card height after a swap and keeping the leftover. The leftover is how far past the line you went, which is also how far the finger sits from the card's new home. Keep it and the card stays under your finger.

Sounds obvious written down. It wasn't, because the swap changes what the offset is measuring. I was thinking of it as how far the finger moved when it's really how far the card is from where it belongs now. Same number until the first swap.

Runner up was `PUT /priorities` replacing the whole row instead of merging. Sending just the priority level quietly wiped the hours and the note. Cost me less only because I found it with curl before building on it.

---

**4c. Paste the test you think is most valuable. Explain what it proves and what it does not.**

> Your answer (include code):

From `PrioritiesViewModelTest.kt`:

```kotlin
@Test
fun `a failed save puts the old order back`() {
    coEvery { repo.getPriorities() } returns threeItems
    coEvery { repo.setPriority(any()) } returns false
    val viewModel = PrioritiesViewModel(repo)

    viewModel.beginDrag()
    viewModel.moveItem(0, 2)
    viewModel.saveOrder()

    assertEquals(listOf(1, 2, 3), viewModel.priorities.value.map { it.mediaId })
    assertNotNull(viewModel.actionError.value)
}
```

I'd keep this one because the failure it catches is invisible. The reorder is optimistic, so the list moves before the server agrees. No rollback and you're looking at an order the server never took, with nothing on screen looking wrong. You'd find out next time you opened the app and the list wasn't what you left, and you'd probably blame yourself. This says when the saves fail, the list goes back and the user gets told.

What it proves: rollback runs, restores the exact previous order, sets an error. I checked it was testing the right thing by deleting the rollback and watching it go red.

What it doesn't prove is a decent list. The repo is mocked, so it says nothing about whether `PUT /priorities` works, only what my ViewModel does when told no. It's all or nothing failure, not the partial case where two rows save and the third doesn't, which is the likelier one since a reorder is several calls. And it never touches the drag, it calls `moveItem` and `saveOrder` directly, so if the gesture stopped calling them tomorrow this would still be green.

---

## Part 5: Looking Forward

**5a. If you kept developing this app after the semester, what would you build next and why?**

> Your answer:

Wire the Feed to `GET /activity` and delete the mock data. It's the first screen after login and the only fully fake one left, and it'd go fast since I already built cursor pagination for Search.

Then the partial failure problem from 4c. Right now if the third of five rows fails I roll the whole list back, including the two the server already took, so we disagree in the other direction. The fix is re-reading the list from the server after a failed save instead of trusting my local copy.

The one I actually want is the one I can't do. There's no way to remove a single item from the priority list. No DELETE, a null priority resets to medium, and pulling the item out of your library doesn't clear it. So five items is permanent. My UI says so instead of pretending, but that's a weird place to land for a feature about changing your mind.

---

**5b. A friend wants to learn Android development. Based on your experience this semester, what's the one thing you'd tell them to understand before writing any code?**

> Your answer:

The network is going to fail and your screen needs an answer for that before it needs to look nice.

I say it that way because of how many times I learned it late. Every screen I built fast had two states, loading and content, and every one I had to reopen later to add the third. Two states isn't done, it's done for the demo where the wifi works.

The specific version: any screen that hits a server needs loading, content and error, and error means a real message plus something to press. Not a spinner that never stops. I shipped one of those, then flagged the same thing on a pod mate's PR later, so it's the default mistake and not just me.

The harder part to explain is that you can't see the third state on your own machine. Your emulator's on wifi, the server's up, you tap the thing and it works. You have to break it on purpose. Airplane mode, try again. That found more real bugs in my app than rereading my own code ever did.
