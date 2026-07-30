import SwiftUI

// shared design constants, the Theme.kt equivalent. stays small because
// swiftui gives you fonts, dynamic type, and dark mode for free.
// the brand color lives in Assets.xcassets as AccentColor
enum Theme {
    static let screenPadding: CGFloat = 16
    static let rowSpacing: CGFloat = 12
    static let cornerRadius: CGFloat = 12

    // state highlight colors, our tiny version of Material's named slots
    static let doneHighlight = Color.mint
    static let activeHighlight = Color.orange
}
