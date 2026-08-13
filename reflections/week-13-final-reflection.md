# Final Reflection — ICS 342

**Name:** Nathan Perfetti

**Date:** August 13, 2026

**Final pull request:** https://github.com/perfettiful/media-tracker-android/pull/14

---

## Part 1 — Your App

**1a. Open your final pull request on GitHub. Find the commit that you are most proud of — not the largest, not the last one, the one that meant the most to you. Paste the commit URL here and explain why you chose it. What did it take to get there? What was broken before and what worked after?**

> Your answer:

https://github.com/perfettiful/media-tracker-android/commit/7501b8d0497cbc7c8c773d92652d451938ba336a

Drag to reorder on the Priorities screen. I picked it because it's the only thing I built this semester where nobody told me what the answer looked like. The spec said drag to reorder, or use a library if you'd rather not build the gesture stuff yourself. I built it.

Before this the priority list was read only. You could set a priority and see the list but the order was just whatever the server had.

The drag part wasn't the hard part, the bookkeeping was. You have to know which card the finger grabbed, track how far it's moved, and when it goes past a whole card you swap it with its neighbor. The thing I kept messing up was what happens to that distance after the swap. Got it wrong twice.

Then it wouldn't work at all, and I couldn't tell if my code was broken or if the emulator just can't fake a long press. I stopped guessing and put a log in the two callbacks. One run and it was clear the drag was starting fine and the move events were never showing up, so it wasn't my code at all. That's really why this one is the commit I'd pick.

Now you long press, drag, let go, and it saves. It renumbers the list and only sends the rows that moved. If the save fails you get the old order back and a snackbar.

---

**1b. Name one screen in your app that you think is genuinely well-built. Not perfect — well-built. Explain specifically why: what design decisions did you make, what did you refactor, and how does it differ from how you would have approached it in week 2?**

> Your answer:

Media Detail. It's the screen I rewrote the most and you can tell.

The state is a sealed class with Loading, Loaded, NotFound and Error, and the screen is just a `when` over those four. The reason NotFound is its own thing instead of getting lumped in with Error is that a 404 on `GET /media/{id}` and a dead network are different problems. NotFound gets its own message and no Retry button, since retrying something that doesn't exist is pointless.

The load fires four calls at the same time instead of one after another: detail, reviews, library status, favorite check. Only the detail one can fail the screen, and if it does the other three get cancelled. That was a refactor. My first version ran them in a row and you could feel it.

The buttons are optimistic. Tap the heart, it fills right away, request goes out behind it, and if the server says no it flips back and you get a snackbar.

Week 2 me would have put all of this in the composable. One isLoading boolean, one error string, network calls kicked off from a LaunchedEffect, and a spinner that never goes away if something throws. I know because that's what my login screen looked like, and I ended up leaving that exact comment on a pod mate's PR months later.

---

**1c. Name one screen or feature that you are not satisfied with. What is wrong with it? If you had one more week, what specifically would you change?**

> Your answer:

The Feed. It's the first thing you see after logging in and it's still totally fake. Comes from FakeMediaRepository, so you get Jordan Smith and Priya Patel doing stuff to media that has nothing to do with your account. And the mock items have mock ids, so tapping one sends you to a detail screen that 404s. I hit that myself during testing more than once and had to stop and remember it wasn't a real bug.

There's a `GET /activity` endpoint sitting right there. I never wired it because Feed was never the assignment and I kept spending the time on whatever screen was due. Probably the wrong call. It's the front door of the app and it's the least real screen in it.

One more week and I'd wire `GET /activity` using the same cursor pagination I already built for Search, and delete the mock data instead of leaving it around as a fallback. I'd probably fix Profile too. It's half real right now, favorites come from the API but the recently tracked list under it is still fake, and that split is more confusing than either one being fully fake.

---

## Part 2 — A Specific Bug

**2a. Describe the hardest bug you fixed this semester. Not the most recent one — the one that took the longest or cost you the most confusion. What was the symptom? What did you think the problem was at first? What was it actually? How did you find it?**

> Your answer:

After logging in, tapping any bottom nav tab kicked you back to the login screen.

This is the one that cost me the most because I put up with it for weeks. First hit it while testing the Library stuff, figured I'd mistapped or my session died, logged back in, kept going. It kept happening and I kept working around it.

My first theory was auth, which makes sense, you get thrown to login so the token must be bad. Then I thought maybe my ON_RESUME refresh was firing on an expired token and blowing up the session. Neither one panned out because Logcat had no 401s anywhere. Nothing was failing. The app was going to login because I told it to.

It was navigation. My graph starts at `login`, and the login flow pops itself off the stack once you're in with `popUpTo(Routes.LOGIN) { inclusive = true }`. But the bottom bar was doing `popUpTo(navController.graph.startDestinationId)`, and the graph's start destination is still login. So every tab tap was saying unwind back to login, pointing at a screen that had already been removed.

I found it by giving up on theories and just reading the two bits of code that touch the back stack side by side. `startDestinationId` doesn't say login anywhere in the name, which is why I skimmed past it for a month. One line fix.

---

**2b. Copy and paste the specific lines of code you changed to fix it. (This can be a before/after comparison, a diff, or just the relevant snippet.) Explain in plain English what the fix does and why it works.**

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

`popUpTo` means clear the back stack down to this screen before going to the new one. In a bottom nav bar you want that so the stack doesn't grow every time you switch tabs, otherwise back walks you through every tab you've ever touched instead of leaving the app.

My problem was which screen I named. `graph.startDestinationId` reads like it means home, but it just means the first destination declared in the graph, and mine is login. So I was pointing at a screen the login flow had already thrown away.

Naming `ACTIVITY_FEED` fixes it because that's where the tabs actually live. saveState and restoreState still do their thing so each tab keeps its scroll position.

What bugs me about this one is the symptom pointed at auth and the cause was in a file I hadn't opened in a month.

---

## Part 3 — What You Actually Learned

**3a. Pick the concept from this semester that took the longest to actually understand — not just to implement, but to understand. Describe what you thought it was before you understood it, what changed, and how you would explain it now to a student who was exactly where you were at the start of the semester.**

> Your answer:

That status codes mean whatever the endpoint you're calling says they mean, not one universal thing.

What I thought before: 2xx worked, 4xx is the user's fault, 5xx is the server's fault, anything not 2xx is a failure so catch it and show an error. Built weeks of the app that way and nothing pushed back on it.

What broke it was `GET /library/{mediaId}`. It gives you a 404 when the item isn't in your library, which isn't an error, it's the answer, and it's the answer most of the time. Under my old assumption, opening the detail screen for anything you hadn't saved threw up a full error screen. Meanwhile a 404 from `GET /media/{id}` means the item really doesn't exist and that one is an error. Same code, opposite meaning, one endpoint apart.

After that a bunch of other stuff made sense. A 409 when you favorite something means it's already favorited, which is the state you were going for, so rolling the UI back would be wrong. A 404 on a delete means it's already gone, which is fine.

If I were explaining it to me in week 2: stop asking whether the call failed and start asking what that specific endpoint is telling you. Practically that means using `Response<T>` in Retrofit so you can read `.code()` and decide yourself, instead of letting anything non-2xx throw before you get a look at it.

---

**3b. Your weekly reflections had a "Still Confused" section. Look back at your early reflections — weeks 1 through 4. Find something you wrote that you were confused about then. Are you still confused about it? If not, when and how did it click? If you still are, say so honestly and describe what the sticking point is.**

> Your answer:

From week 4:

> The thing I am still stuck on is why `viewModel()` is written as a generic. In `RegisterScreen.kt` the call is `viewModel: RegisterViewModel = viewModel()`, and the tooltip in Studio shows it as `<reified VM>`. I know `reified` means the runtime can see the type, but I do not get why this needs to be a generic function instead of just a regular one that returns my `RegisterViewModel` directly.

Not stuck on it anymore.

The thing I was missing is that `viewModel()` mostly isn't building anything, it's looking something up. Every nav destination has a ViewModelStore, which is basically a map, and the type is the key. So the call means give me the RegisterViewModel for this screen and make one if it's not there yet. The generic is how it knows what to look under, and reified is what lets it read the type at runtime so I don't have to pass `RegisterViewModel::class.java` myself like the older API made you do.

What made it real was rotation. Library holds the status filter in the ViewModel and the type chips in the composable with rememberSaveable. Rotate the phone and the ViewModel value is just sitting there, because the store outlived the composable and `viewModel()` handed back the same object. Watching that happen is different from reading that it happens.

The flexibility part of my question got answered later than I expected. When I needed ViewModels with constructor arguments, ProfileViewModel and then SettingsViewModel this week, I used `viewModel(factory = ...)`. That only works because the function is generic. If it were hardcoded to return one type it couldn't take a factory that builds a different one. So the generic thing I was annoyed about in week 4 is what let me do what I wanted in week 12.

---

**3c. Name one thing a pod mate said, asked, or showed you during a code review or work session that changed how you approached something. It doesn't have to be a big thing. What was it, and what did it change?**

> Your answer:

Dustin reviewing my week 10 PR. He said I hadn't disabled the Save button on Media Detail so it could get spammed, and that could leave the UI and the server out of sync.

He was right and it bugged me a little because I'd been looking at that button all week. I was thinking about whether the request worked. He was thinking about somebody tapping it four times fast, which people do and which I never do when I'm testing my own thing because I already know what it's supposed to do.

What changed is I stopped calling something done when the happy path works. The Save button became an optimistic toggle with a guard so a second tap while one is in flight doesn't fire again, and a real failure rolls it back. But mostly it changed the question I ask when I finish a control, which is now what happens if someone taps this twice, or taps it with no signal, or taps it and immediately backs out of the screen.

It also made my reviews of his stuff better. Most of what I flagged on his PRs after that was what happens when this fails rather than style stuff, because that's the kind of comment that had helped me.

---

## Part 4 — Your Bonus Feature

**4a. Describe your bonus feature in one paragraph as if you were explaining it to someone who has never used an app before. What does it do? Why would a user want it?**

> Your answer:

The app keeps a list of books, movies and shows you want to get to eventually, and that list gets long fast. My feature lets you pull five of them out into a short list of what's actually next. Each one gets marked high, medium or low, and you can put down roughly how many hours it'll take plus a note about why it's there, like a friend recommended it or you're saving it for movie night. Then you drag them into whatever order you want and it stays. The reason you'd want it is that picking what to watch or read is its own little chore, and this lets you do that thinking at some calm moment instead of when you're tired. Later when you've actually got a free evening you're looking at five things instead of forty, and you can see one is two hours and another is six, so you just take whichever one fits.

---

**4b. What was the technically hardest part of building it? Name a specific function, flow, or data structure that gave you trouble, and explain what the problem was.**

> Your answer:

The `onDrag` callback, specifically the running offset.

I track which index the finger grabbed and how far it's moved down or up. Once that passes the height of a card, the item should swap with its neighbor. What I kept getting wrong was what to do with that distance afterward.

First version just did the swap and left the offset alone. The offset is what actually shifts the card on screen, so after swapping, the card was still drawn a full card away from the finger, which meant the code immediately thought it had passed another threshold and swapped again, and again. One drag would send the item straight to the bottom of the list.

The fix is to subtract one card height back out after a swap and keep the leftover. The leftover is how far past the line you actually went, which is also how far the finger is from the card's new home. Keep that and the card stays under your finger and the next swap happens when it should.

Sounds obvious written down. It wasn't, because the swap changes what the offset is even measuring. I was thinking of it as how far the finger moved when it's really how far the card is from where it currently belongs. Those are the same number right up until the first swap.

Second hardest was `PUT /priorities` replacing the whole row instead of merging. Sending just the priority level quietly wiped the hours and the note. That one cost me less only because I found it poking at the API with curl before I'd built anything on top of it.

---

**4c. Your bonus feature has tests. Open the test file and paste the test you think is most valuable — the one that would catch the most important failure. Explain what it proves and what it does not prove.**

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

This is the one I'd keep if I could only keep one, because the failure it catches is invisible. The reorder is optimistic so the list moves before the server has agreed to anything. If a save fails and there's no rollback, you're looking at an order the server never took, and nothing on screen looks wrong. You'd find out next time you opened the app and the list wasn't what you left, and you'd probably assume you did it. This test says when the saves fail, the list goes back to 1, 2, 3 and the user gets told about it.

What it proves: the rollback runs, it restores the exact order from before, and it sets an error. I also checked it was testing the right thing by deleting the rollback and watching it fail, then putting it back.

What it doesn't prove is a decent list. The repo is mocked so it says nothing about whether `PUT /priorities` actually works, only about what my ViewModel does when something tells it no. It's all-or-nothing failure, not the partial case where two rows save and the third doesn't, which is honestly the more likely one since a reorder is several separate calls. And it never touches the drag, it calls `moveItem` and `saveOrder` straight, so if the gesture stopped calling them tomorrow this test would still be green.

---

## Part 5 — Looking Forward

**5a. If you were going to continue developing this app after the semester ends, what would you build next and why?**

> Your answer:

Wire the Feed to `GET /activity` and delete the mock data. It's the first screen after login and the only fully fake one left. It'd also go fast since it's a paginated list and I already built cursor pagination for Search.

Then the partial failure thing I mentioned in 4c. Right now if the third of five rows fails to save I roll the whole list back, including the two the server already accepted, so now we disagree in the other direction. The real fix is re-reading the list from the server after a failed save instead of trusting my local copy.

The one I actually want is the one I can't do, which is removing a single item from the priority list. There's no endpoint. DELETE isn't allowed, a null priority just resets it to medium, and taking the item out of your library doesn't clear it. So five items is permanent. My UI at least tells you that instead of pretending, but it's a weird place for a feature that's supposed to be about changing your mind.

---

**5b. A friend tells you they want to learn Android development. Based specifically on your experience this semester — not what you've read, what you lived — what is the one thing you would tell them to understand before they write a single line of code?**

> Your answer:

The network is going to fail and your screen needs an answer for that before it needs to look nice.

I'd say it that way because of how many times I learned it late. Every screen I built quickly had two states, loading and content, and every one of them I had to go back into later and add the third. Two states isn't done, it's done for the demo where the wifi works.

The specific version so it's not just a saying: any screen that hits a server needs loading, content, and error, and error means an actual message plus something to press. Not a spinner that spins forever. I shipped a spinner that spun forever, and then flagged the same thing on a pod mate's PR later, so I'm fairly sure it's the default mistake and not just me.

The part that's harder to get across is that you can't see the third state on your own machine. Your emulator's on wifi, the server's up, you tap the thing and it works, so you move on. You have to break it on purpose. Airplane mode and try again. That found more real bugs in my app than rereading my own code ever did.
