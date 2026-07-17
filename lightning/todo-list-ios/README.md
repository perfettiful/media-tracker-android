# TodoList (iOS / SwiftUI)

A deliberately small todo app built to compare SwiftUI patterns against the
Jetpack Compose patterns used in `media-tracker-android`. Same architecture,
different platform: unidirectional data flow, a screen-level state holder,
and a repository abstraction over storage.

**Features:** add todos, check them off, swipe to delete, filter (All /
Active / Done), clear completed, and persistence across launches.

## Requirements

- macOS with Xcode 15 or newer (built and tested with Xcode 26)
- No Apple developer account needed — it runs in the iOS Simulator unsigned

## How to run

1. Open the project:
   ```
   open TodoList.xcodeproj
   ```
   (or double-click `TodoList.xcodeproj` in Finder)
2. In the toolbar at the top of Xcode, pick a simulator from the device menu
   (e.g. **iPhone 16 Pro**). This menu is the equivalent of Android Studio's
   device dropdown.
3. Press **⌘R** (or the ▶ button) — Xcode builds the app, boots the
   simulator, and launches it. Same flow as Run ▶ in Android Studio.

To run on a physical iPhone you'd need to sign in with an Apple ID under
Xcode ▸ Settings ▸ Accounts and pick a development team — not needed for
this project.

Command-line build (equivalent of `./gradlew assembleDebug`):

```
xcodebuild -project TodoList.xcodeproj -scheme TodoList \
  -destination 'generic/platform=iOS Simulator' build
```

## Project structure

The folder layout intentionally mirrors `media-tracker-android`:

```
TodoList/
├── TodoListApp.swift          # entry point (≈ MainActivity + Application)
├── Model/
│   └── TodoItem.swift         # value type (≈ Kotlin data class)
├── Data/
│   └── TodoRepository.swift   # protocol + UserDefaults & in-memory impls (≈ data/)
├── UI/
│   ├── TodoList/
│   │   ├── TodoListView.swift       # screen (≈ TodoListScreen.kt)
│   │   ├── TodoListViewModel.swift  # state holder (≈ TodoListViewModel.kt)
│   │   └── TodoRowView.swift        # stateless row composable-equivalent
│   └── AddTodo/
│       └── AddTodoView.swift        # input sheet (≈ dialog destination)
├── Theme/
│   └── Theme.swift            # design constants (≈ theme/Theme.kt)
└── Assets.xcassets/           # accent color + app icon (≈ res/values, mipmap)
```

Xcode 16+ keeps this folder tree synced with the project automatically
(folder-synchronized groups), so adding a file on disk adds it to the build —
similar to how Gradle picks up anything under `src/main/java`.

## Jetpack ↔ SwiftUI translation table

| Concept | Jetpack (Android) | SwiftUI (iOS) |
|---|---|---|
| UI toolkit | Compose `@Composable` functions | `View` structs with a `body` |
| Screen state holder | `ViewModel` | `ObservableObject` class (`@MainActor`) |
| Observable state | `StateFlow` / `MutableStateFlow` | `@Published` properties |
| Collecting state in UI | `collectAsStateWithLifecycle()` | `@ObservedObject` / `@StateObject` |
| VM survives recomposition | `viewModel()` / `hiltViewModel()` | `@StateObject` (created once, kept alive) |
| Local UI state | `remember { mutableStateOf(...) }` | `@State` |
| Two-way binding | callback + state hoisting | `$binding` (`Binding<T>`) |
| Dependency injection | Hilt `@Inject` constructor | plain constructor injection at the composition root |
| Repository interface | Kotlin `interface` | Swift `protocol` |
| Simple persistence | DataStore / SharedPreferences | `UserDefaults` |
| Bigger persistence | Room | SwiftData / Core Data |
| Serialization | kotlinx.serialization / Moshi | `Codable` |
| Lazy list | `LazyColumn` + `items(key = ...)` | `List` + `ForEach` (`Identifiable`) |
| Navigation container | `NavHost` + `NavController` | `NavigationStack` |
| Dialog / bottom sheet | dialog destination, `ModalBottomSheet` | `.sheet(isPresented:)` |
| Back/dismiss | `navController.popBackStack()` | `@Environment(\.dismiss)` |
| App bars | `Scaffold` + `TopAppBar`/`BottomAppBar` | `.toolbar { ToolbarItem(...) }` |
| Theming | `MaterialTheme` (Color.kt, Type.kt) | system styles + asset-catalog `AccentColor` |
| Design-time preview | `@Preview` composable | `#Preview` block |
| String/image resources | `res/values`, `res/drawable` | `Assets.xcassets`, SF Symbols (`Image(systemName:)`) |
| Main-thread confinement | `Dispatchers.Main` / `viewModelScope` | `@MainActor` |

## Reading the code

Two differences worth noticing while reading the code:

- **SwiftUI has first-class two-way bindings** (`$viewModel.filter` in
  `TodoListView`). Compose would express the same thing as a value parameter
  plus an `onFilterChange` callback. Where a binding would hide the data flow
  (the add sheet), this app still hoists state with a callback, Compose-style.
- **`ObservableObject` vs `@Observable`:** iOS 17 introduced the
  `@Observable` macro, which tracks property reads the way Compose's snapshot
  system does. This app uses the older `ObservableObject`/`@Published`
  pattern because it maps one-to-one onto `ViewModel` + `StateFlow`.

## Week 13 — Lightning Exploration notes

Working notes for presentation night (15 min per pod).

### Demo script (3–4 min)

Run on the iPhone simulator, then walk this sequence — it hits every feature
in under two minutes and leaves time for a code peek:

1. **Empty state** — delete everything first so the screen opens on
   "No Todos Yet."
2. **Add** — tap +, note the sheet slides up with the keyboard already
   focused and the Add button disabled until you type. Add two or three.
3. **Complete** — tap a checkmark (animated symbol swap) and point at the
   progress bar updating. Also swipe **right** on a row to complete it.
4. **Delete** — swipe left.
5. **Filter** — flip through All / Active / Done, including the "Nothing
   Left To Do" empty state.
6. **Persistence** — swipe the app away in the app switcher, relaunch,
   todos are still there.
7. **Code peek** — put `TodoListViewModel.swift` next to any ViewModel from
   `media-tracker-android`. The shape is identical: private mutable state,
   public read-only state, event methods, injected repository.

### The three standard questions (4–5 min)

**Surprisingly easy:** the UI itself. The whole app is ~300 lines of Swift
across seven files. Dark mode, dynamic type, swipe gestures, and list
animations came free from the platform — none of them were written by hand.
Coming from Compose, nothing had to be relearned, only renamed:
`@Composable` → `View`, `LazyColumn` → `List`, `StateFlow` → `@Published`.

**Surprisingly hard:** everything *around* the code. Xcode's default run
destination was "My Mac (Designed for iPhone)," which fails with a signing
error until you notice the device menu — the simulator needs no signing at
all. Stray simulator windows from older iOS versions restore themselves and
sit on top of the one actually running the app. And the project file
(`project.pbxproj`) is a generated artifact you mostly hope never to merge —
Gradle's text-based build scripts are genuinely nicer to live with.

**Would we build something real in this platform?** Yes for a product that
is iPhone-first: the tooling friction is front-loaded, and once past it the
framework is as productive as Compose with better system integration. But
there's no story for shipping to Android from here — for a two-platform
product this pod would reach for a cross-platform layer or budget for two
codebases.

### Platform-specific Q&A (5–6 min)

> Paste the iOS/SwiftUI question set from the pod handout here — one slide
> per question. Source material for most answers already exists in this
> repo: the translation table above, the Jetpack-analogy comments in every
> source file, and the architecture notes in "Reading the code."
