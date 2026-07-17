import SwiftUI

/// Central design constants.
/// Android analogue: `theme/Theme.kt` + `dimens`. SwiftUI leans on system
/// styling far more than Compose (fonts, dynamic type, dark mode come free),
/// so this stays small: shared spacing plus anything pulled from the asset
/// catalog. The app's brand color lives in Assets.xcassets as "AccentColor",
/// the counterpart of `colorScheme.primary` in a Material theme.
enum Theme {
    static let screenPadding: CGFloat = 16
    static let rowSpacing: CGFloat = 12
    static let cornerRadius: CGFloat = 12

    /// Semantic highlight colors, the hand-rolled version of a Material
    /// color scheme's named slots (primary / tertiary / error).
    static let doneHighlight = Color.mint
    static let activeHighlight = Color.orange
}
