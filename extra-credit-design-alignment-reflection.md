# Extra Credit Reflection — Design Alignment

**Name:** Nathan Perfetti
**Date:** July 1, 2026

---

## The Audit

*List at least five concrete differences you found:*

1. The bottom nav pill was amber (#FEF3C7) on every screen instead of the light indigo primary container (#E0E0FF). `NavigationBarItem` defaults its indicator to `secondaryContainer` and my theme had secondary defined as amber, so the amber was coming from the theme, not the nav code.
2. Same deal on Search and Library, the selected "All" chip was amber b/c `FilterChip` also defaults to `secondaryContainer`. The Want to / In Progress / Finished segmented control too.
3. Secondary and tertiary were just wrong vs the spec. Secondary was amber #D97706, spec wants pink #DB2777. Tertiary was teal #0D9488, spec wants amber. OnPrimaryContainer was #1E1B4B, spec says #3730A3.
4. The 6 status colors didn't exist in my theme at all. Library was showing status as a gray outlined `SuggestionChip` instead of the purple on lavender badge from the wireframes.
5. The Login title was rendering Regular weight. My `Type.kt` never defined `headlineMedium` so it fell back to the Material default (400) instead of bold. Also a bunch of screens were hardcoding `fontWeight = SemiBold` on individual Texts instead of the styles carrying it.
6. Buttons were full pills instead of 20dp radius, text fields were the stock 4dp corners instead of 8dp, and the library card was 1dp elevation on that dim default container color instead of 2dp on white.

---

## What You Changed

### Color System

Moved secondary to the pink pair (#DB2777 / #FCE7F3), moved amber over to tertiary where the spec wants it, fixed OnPrimaryContainer, added the 6 status colors. Also deleted a `MovieContainer` pair I hand-rolled a few weeks back, once secondary went pink it was the same color doing the same job so the movie tile just uses `secondaryContainer` now. Had to chase down the spots that wanted amber on purpose (rating stars on the feed, rating text on search cards) and move them from `colorScheme.secondary` to `colorScheme.tertiary` so they didn't silently go pink.

### Typography

`Type.kt` only had 5 styles defined, everything else was falling back to Material defaults, which is how the login title ended up Regular. Filled out the whole set w/ the spec weights (display/headlines bold, titles + labels semibold, body regular) then deleted all the per-Text `fontWeight` overrides across the screens. A few Texts that were `bodyMedium` plus a manual semibold really just wanted to be `titleSmall` or `labelLarge` anyway.

### Buttons

Log In, Sign Up, Follow/Following, Edit Profile all got `shape = RoundedCornerShape(20.dp)`. The outlined ones also got a primary `BorderStroke` since the stock outlined border is gray. Left the sign out button red, that ones supposed to look scary.

### Text Fields

Login and register fields got the 8dp shape and an explicit primary `focusedBorderColor`. Search bar already had its 28dp pill so no change there.

### Other Components

Chips got 8dp shape + primary container selected state, and Library reuses the same `MediaTypeFilterChips` from Search now instead of its own inline copy. Wrote a lil `StatusBadge` composable that maps each `LibraryStatus` to its container/on-container pair, swapped it in for the gray chip (still tappable to change status). Library card went white surface at 2dp and picked up the colored media type tile Search already had. Bottom nav got explicit colors, primary container pill, primary active, onSurfaceVariant inactive.

---

## What Was Hard

The amber pill in the bottom nav wasn't a bug in my screens at all, there was no amber anywhere in that code. I spent a while grepping for the hex before it clicked that M3 components default their selected states to `secondaryContainer`, so my amber secondary was leaking into components I never colored. And the fix was two sided, fix the palette so secondary matches spec, but then still set explicit colors on the nav and chips anyway b/c their defaults point at the wrong slot even with a correct palette (spec wants primary container there, not secondary container). Other tricky part was flipping secondary and tertiary without breaking the screens that wanted amber, the rating stars wouldve just quietly turned pink if I hadn't traced every `colorScheme.secondary` call site first.

---

## What You Understand Now

I used to think of `MaterialTheme` as basically a bag of constants you reference by hand. What I get now is the `colorScheme` slots are load bearing whether you reference them or not. Every M3 component ships with defaults wired to specific slots, `FilterChip` selected and the nav indicator both read `secondaryContainer`, focused field borders read `primary`, so defining a slot wrong restyles stuff you never touched. Same with typography, an undefined style isn't "no style", it's the Material default, which is how you get a Regular title while your `Type.kt` looks totally fine. If a pod mate hit this I'd tell em to open the component's `Defaults` object and see which theme slots it actually reads before assuming where a color is coming from.

---

## Self-Assessment

| Section | Possible | My Estimate |
|:---|:---:|:---:|
| Color System | 13 | 13 |
| Typography | 5 | 5 |
| Component Styling | 15 | 14 |
| Navigation & Cards | 5 | 5 |
| Reflection | 12 | 10 |
| **Total** | **50** | **47** |

*One thing I think I did well:* audited with the app and the wireframes literally side by side before touching code, and traced the amber leak to the theme instead of patching components one at a time.

*One thing I know I left incomplete or could have done better:* feed and profile still use gray emoji tiles for covers instead of the colored type tiles. Those are stub screens on fake data so I kept scope to the design system categories, but somebody holding them next to the wireframes would notice.
