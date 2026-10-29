# Implementation Plan: Luxurious Redesign of Bookmarked Questions Screen

Transform the Bookmarked Questions screen (`SavedQuestionsScreen.kt`) into a luxurious, high-end experience featuring **Soft Champagne Pearl**, **Warm Gold**, **Emerald**, and **Amber** accents. Completely remove all dark themes, slate/charcoal surfaces, and cold grey tones, replacing them with warm, elegant materials and jewel-toned badges.

---

## User Preferences & Requirements

- **Color Palette**: Soft Champagne Pearl base with Emerald and Amber accents.
- **Card & Header Style**: Pure white cards with refined warm gold borders and jewel badges.
- **Strict Constraint**: Zero dark mode surfaces, zero charcoal/slate backgrounds, zero cold greys (`Color.Gray`, `#64748B`, `#94A3B8`, `#1E293B`, `#0F172A`).
- **Functionality**: Preserve all existing features (hierarchy and flat view modes, subject/topic filters, search query, single/batch practice navigation, share, and delete dialog).

---

## Proposed Changes

### 1. Palette Architecture & Token Definition (`SavedQuestionsScreen.kt`)

Introduce dedicated luxury color tokens for the Bookmarked Questions screen:
- **Screen Canvas**: Soft Champagne Pearl (`#FAF7F2` / `#FDFBF7`) — warm, radiant, and clean.
- **Card Surfaces**: Pure Crisp White (`#FFFFFF`).
- **Borders & Dividers**: Warm Gold / Champagne outlines (`#E8D8B8` / `#DFCEAB` / `#D4AF37` at 30% alpha).
- **Primary Ink**: Deep Warm Espresso / Mahogany (`#2A1E17`) for crisp, readable, high-contrast headings without harsh black.
- **Secondary Ink**: Warm Muted Bronze / Taupe (`#7A6A5D`) for meta labels and subtitles instead of cold grey.
- **Accents**:
  - Imperial Emerald (`#047857` / `#059669`) for primary practice actions, active topic chips, and correct answer badges.
  - Royal Amber / Warm Gold (`#D97706` / `#B45309`) for bookmark star accents, revision badges, and highlights.
  - Jewel Subject Accents (Polity Sapphire, History Terracotta Gold, Economy Violet, Science Teal, Current Affairs Rose) on warm pearl tints with gold borders.

### 2. Top App Bar & Bottom Bar Redesign

- **Top App Bar**:
  - Background: Soft Champagne Pearl matching the screen.
  - Back & Home navigation icons in deep espresso and rich emerald.
  - "Premium Revision Hub" header with radiant amber bookmark star and warm espresso typography.
  - View Switcher toggle ("Hierarchy" vs "Flat List") styled as an elegant gold/emerald pill.
- **Bottom Bar (Custom Practice Selection)**:
  - Container in pure white with a warm gold top border (`#E8D8B8`) and subtle elevation.
  - Counter text in warm espresso ink.
  - Primary "Practice Selected" CTA button in Imperial Emerald (`#047857`) with warm gold play icon.

### 3. Header Banner, Search Bar, and Filter Chips

- **Hero Banner ("MCQ REVISION HUB")**:
  - Gradient background transitioning from Soft Champagne Cream (`#FBF6EE`) to Pale Amber Silk (`#FDF1DC`) with a fine gold border (`#E5D3B3`).
  - Bookmark count badge in Imperial Emerald pill with gold highlights.
- **Search Bar**:
  - Pure white container with warm gold outline (`#DDC8A4` unfocused, `#B45309` focused).
  - Amber search icon and warm taupe placeholder text.
- **Subject & Topic Filter Chips**:
  - Selected chip: Rich Emerald (`#047857`) or Radiant Amber (`#D97706`) with pure white text and gold glow.
  - Unselected chip: Soft pearl cream (`#F4EFE6`) with warm bronze text (`#6E5844`) and subtle champagne border.

### 4. Subject & Topic Hierarchy Cards

- **Subject Accordion Cards**:
  - Pure white cards with warm gold borders (`#DFCEAB`).
  - Subject header row with soft jewel-tinted gradient, crisp circular subject icon badge, and warm espresso subject title.
  - Subject practice button in tailored emerald/amber outline.
- **Topic Section**:
  - Refined pearl container (`#FDFBF7`) with jewel badge counts and expandable caret.

### 5. Bookmarked Question Cards (`SavedQuestionCard`)

- **Card Body**:
  - Pure white card with rounded 16.dp corners, warm gold border (`#E8D8B8`), and delicate shadow.
  - Subtopic / Subject badge as a jewel-toned chip with gold border.
  - Question text rendered in deep espresso (`#2A1E17`).
- **Options List**:
  - Option container: Crisp white with warm champagne border (`#EFE4D2`).
  - Option key circle ("A", "B", "C", "D") in warm champagne-gold with deep bronze lettering.
  - Correct Answer state: Rich Emerald container tint (`#ECFDF5`), bold emerald border (`#059669`), and checkmark icon.
- **Explanation Box**:
  - Soft Emerald Pearl container (`#F0FDF4`) with mint-emerald border (`#A7F3D0`), golden lightbulb icon, and deep forest green text (`#064E3B`).
- **Action Buttons**:
  - "View Solution" button in warm amber/emerald.
  - "Practice" button in Imperial Emerald (`#047857`) with white text.
  - Delete dialog styled in crisp white with warm mahogany headers and amber cancel button.

### 6. Empty States

- When no questions exist or filters return 0 results:
  - Elegant amber/gold bookmark emblem with soft circular pearl background.
  - Warm espresso heading and warm bronze guidance text.

---

## Verification Plan

### Automated Compilation Check
- Run `compile_applet` to ensure zero compilation or syntax errors across `SavedQuestionsScreen.kt`.

### Visual & Interactive Review
- Verify that no dark mode surfaces (`#1E293B`, `#0F172A`, `#334155`) or cold grey colors (`Color.Gray`, `#64748B`, `#94A3B8`, `#CBD5E1`, `#E2E8F0`) appear on the screen.
- Verify that all bookmark actions (view solution, open practice, practice all, delete, filter, and switch view mode) remain fully responsive and functional.
