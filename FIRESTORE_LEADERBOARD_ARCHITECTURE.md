# Cloud Firestore Leaderboard Collection & Indexing Architecture

This document specifies the enterprise Cloud Firestore schema, entity relationships, query patterns, and indexing strategies designed for the **APPSC Prep Competitive Leaderboard System**.

---

## 1. Architectural Overview & Collections

The leaderboard ecosystem is decoupled into four primary Firestore collections and two supporting audit collections to guarantee $O(1)$ lookups, prevent single-document write saturation, and deliver low-latency queries across thousands of active aspirants:

```
├── leaderboard_entries/       (Primary multi-period ranking documents)
│   └── {examId}_{periodType}_{periodKey}_{userId}
│
├── user_stats/                (Cumulative & lifetime candidate telemetry)
│   └── {examId}_{userId}
│
├── leagues/                   (Competitive tier configurations & standings)
│   └── {leagueId}             (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND)
│
├── achievements/              (Verified badge registry & criteria)
│   └── {badgeId}              (ACC_95, STREAK_30, QUESTIONS_1000, etc.)
│
├── user_achievements/         (Unlocked candidate badge audit records)
│   └── {userId}_{badgeId}
│
└── leaderboard_config/        (Administrative weights, XP rules & resets)
    └── default_config
```

---

## 2. Collection Schemas & Entity Relationships

### 2.1. `LeaderboardEntry` Collection (`leaderboard_entries`)
Represents an individual candidate's standing within a specific exam, time period, and geographic/subject slice.

- **Document ID Structure**: `${examId}_${periodType}_${periodKey}_${userId}`
  - Example Global: `appsc-group2_GLOBAL_all-time_candidate_01`
  - Example Weekly: `appsc-group2_WEEKLY_2026-W40_candidate_01`
  - Example Monthly: `appsc-group2_MONTHLY_2026-09_candidate_01`
- **Fields**:
  | Field | Type | Description |
  |---|---|---|
  | `entryId` | `String` | Document ID |
  | `userId` | `String` | **Foreign Key** to user identity / Firebase Auth UID |
  | `examId` | `String` | **Foreign Key** to exam/syllabus stream (`appsc-group2`) |
  | `targetExam` | `String` | Human-readable title (`APPSC Group-2`) |
  | `periodType` | `String` | `GLOBAL`, `WEEKLY`, `MONTHLY`, `SUBJECT` |
  | `periodKey` | `String` | `all-time`, `2026-W40`, `2026-09`, `subject_history` |
  | `district` | `String` | Andhra Pradesh district (`Visakhapatnam`, `Guntur`, etc.) |
  | `subjectId` | `String?` | Optional syllabus subject filter (`history`, `polity`) |
  | `displayName` | `String` | Candidate display name |
  | `avatarColorHex` | `Number (Long)` | Dynamic avatar color |
  | `avatarUrl` | `String?` | Custom candidate avatar path |
  | `privacyMode` | `String` | `PUBLIC`, `DISPLAY_NAME`, `ANONYMOUS` |
  | `xp` | `Number (Int)` | XP accumulated in this period |
  | `leaderboardScore` | `Number (Double)` | Fair competitive composite score |
  | `rank` | `Number (Int)` | Current verified rank |
  | `previousRank` | `Number (Int)` | Prior period rank for movement indicator |
  | `rankDelta` | `Number (Int)` | Position shift (`+8`, `-3`, `0`) |
  | `accuracyPercentage` | `Number (Float)` | Accuracy in attempted questions |
  | `testsCompleted` | `Number (Int)` | Verified tests completed |
  | `questionsAttempted` | `Number (Int)` | Total questions solved |
  | `streakDays` | `Number (Int)` | Current consecutive active study streak |
  | `league` | `String` | Tier (`BRONZE`, `SILVER`, `GOLD`, `PLATINUM`, `DIAMOND`) |
  | `badges` | `Array<String>` | Badge IDs unlocked |
  | `isVerified` | `Boolean` | True if verified via anti-cheat ledger |
  | `lastVerifiedTxId` | `String` | Idempotency transaction key |
  | `updatedAt` | `Number (Long)` | Millisecond timestamp of last score update |

---

### 2.2. `UserStats` Collection (`user_stats`)
Cumulative lifetime performance records linked 1:1 with candidate user IDs and active exams.

- **Document ID Structure**: `${examId}_${userId}`
  - Example: `appsc-group2_sobhadhanaraju0@gmail_com`
- **Fields**:
  | Field | Type | Description |
  |---|---|---|
  | `statsId` | `String` | Document ID |
  | `userId` | `String` | **Foreign Key** to User |
  | `examId` | `String` | **Foreign Key** to Exam |
  | `targetExam` | `String` | `APPSC Group-2` |
  | `district` | `String` | State District |
  | `totalXp` | `Number (Int)` | Lifetime XP accumulated |
  | `weeklyXp` | `Number (Int)` | XP earned during current calendar week |
  | `monthlyXp` | `Number (Int)` | XP earned during current calendar month |
  | `currentWeekKey` | `String` | ISO week token (`2026-W40`) |
  | `currentMonthKey` | `String` | ISO month token (`2026-09`) |
  | `questionsAttempted`| `Number (Int)` | Total questions answered |
  | `correctAnswers` | `Number (Int)` | Total correct answers |
  | `overallAccuracy` | `Number (Float)` | Lifetime accuracy percentage |
  | `testsCompleted` | `Number (Int)` | Total tests (mock + sets) |
  | `mockTestsCompleted` | `Number (Int)`| Full-length mock tests completed |
  | `practiceSetsCompleted`| `Number (Int)`| Topic & subtopic practice sets |
  | `currentStreak` | `Number (Int)` | Consecutive active days |
  | `longestStreak` | `Number (Int)` | Lifetime personal best streak |
  | `lastActiveDate` | `String` | Server date `YYYY-MM-DD` |
  | `currentLeague` | `String` | League tier (`BRONZE` through `DIAMOND`) |
  | `leagueProgress` | `Number (Float)` | Progress fraction to next tier (0.0 to 1.0) |
  | `subjectXp` | `Map<String, Int>` | XP breakdown per syllabus subject |
  | `subjectAccuracy` | `Map<String, Float>`| Accuracy breakdown per syllabus subject |
  | `unlockedBadgeIds` | `Array<String>` | List of unlocked achievement badges |
  | `antiCheatAuditFlags` | `Number (Int)` | Audit flag counter (0 = clean) |
  | `privacyMode` | `String` | `PUBLIC`, `DISPLAY_NAME`, `ANONYMOUS` |
  | `updatedAt` | `Number (Long)` | Last update timestamp |

---

### 2.3. `League` Collection (`leagues`)
Defines the competitive tiers, XP thresholds, promotion/demotion quotas, and benefits.

- **Document ID Structure**: Tier name (`BRONZE`, `SILVER`, `GOLD`, `PLATINUM`, `DIAMOND`)
- **Fields**:
  | Field | Type | Value Example |
  |---|---|---|
  | `leagueId` | `String` | `DIAMOND` |
  | `tier` | `String` | `DIAMOND` |
  | `title` | `String` | `Diamond League` |
  | `iconEmoji` | `String` | `💎` |
  | `minXp` | `Number (Int)` | `10000` |
  | `maxXp` | `Number?` | `null` (uncapped) |
  | `colorHex` | `Number (Long)` | `0xFF38BDF8` |
  | `accentHex` | `Number (Long)` | `0xFF0284C7` |
  | `orderIndex` | `Number (Int)` | `5` |
  | `promotionThresholdPercentile` | `Number (Float)`| `0.00f` (Top tier) |
  | `demotionThresholdPercentile` | `Number (Float)` | `0.10f` (Bottom 10% drop to Platinum) |
  | `totalCandidatesCount` | `Number (Int)` | Real-time candidate headcount |
  | `description` | `String` | Pinnacle competitive division |
  | `perks` | `Array<String>` | `["Top 1% State Recognition", "Hall of Fame Listing"]` |
  | `isActive` | `Boolean` | `true` |

---

### 2.4. `Achievement` Collection (`achievements`)
Official verified milestone catalog.

- **Document ID Structure**: Milestone Badge ID (`ACC_95`, `STREAK_30`, etc.)
- **Fields**:
  | Field | Type | Description |
  |---|---|---|
  | `achievementId` | `String` | Unique badge code |
  | `badgeId` | `String` | Canonical ID |
  | `title` | `String` | `🎯 95% Accuracy Master` |
  | `description` | `String` | Maintain 95%+ accuracy over 50+ questions |
  | `icon` | `String` | `🎯` |
  | `category` | `String` | `ACCURACY`, `STREAK`, `QUESTIONS`, `MOCK_TEST`, `LEAGUE` |
  | `criteriaType` | `String` | `ACCURACY_PERCENTAGE`, `STREAK_DAYS`, `QUESTIONS_COUNT`, `LEAGUE_TIER`, `TESTS_COUNT` |
  | `thresholdValue` | `Number (Double)` | Target value to trigger verification |
  | `xpReward` | `Number (Int)` | Bonus XP awarded to user |
  | `rarity` | `String` | `COMMON`, `RARE`, `EPIC`, `LEGENDARY` |
  | `totalUnlockedCount` | `Number (Int)` | Total candidate unlocks |
  | `isLive` | `Boolean` | `true` |
  | `orderIndex` | `Number (Int)` | Sorting sequence in UI |

---

## 3. High-Performance Indexing Strategy

To support high concurrency with sub-100ms response times and zero client-side sorting overhead, the following **Composite Indexes** are deployed:

### 3.1. Global Leaderboard Query
- **Query**:
  ```kotlin
  firestore.collection("leaderboard_entries")
      .whereEqualTo("examId", "appsc-group2")
      .whereEqualTo("periodType", "GLOBAL")
      .orderBy("leaderboardScore", Query.Direction.DESCENDING)
      .orderBy("xp", Query.Direction.DESCENDING)
      .limit(50)
  ```
- **Index**:
  - `examId`: ASCENDING
  - `periodType`: ASCENDING
  - `leaderboardScore`: DESCENDING
  - `xp`: DESCENDING

### 3.2. Weekly Leaderboard Query
- **Query**:
  ```kotlin
  firestore.collection("leaderboard_entries")
      .whereEqualTo("examId", "appsc-group2")
      .whereEqualTo("periodType", "WEEKLY")
      .whereEqualTo("periodKey", currentWeekKey) // e.g. "2026-W40"
      .orderBy("leaderboardScore", Query.Direction.DESCENDING)
      .orderBy("xp", Query.Direction.DESCENDING)
      .limit(50)
  ```
- **Index**:
  - `examId`: ASCENDING
  - `periodType`: ASCENDING
  - `periodKey`: ASCENDING
  - `leaderboardScore`: DESCENDING
  - `xp`: DESCENDING

### 3.3. Monthly Leaderboard Query
- **Query**:
  ```kotlin
  firestore.collection("leaderboard_entries")
      .whereEqualTo("examId", "appsc-group2")
      .whereEqualTo("periodType", "MONTHLY")
      .whereEqualTo("periodKey", currentMonthKey) // e.g. "2026-09"
      .orderBy("leaderboardScore", Query.Direction.DESCENDING)
      .orderBy("xp", Query.Direction.DESCENDING)
      .limit(50)
  ```
- **Index**:
  - `examId`: ASCENDING
  - `periodType`: ASCENDING
  - `periodKey`: ASCENDING
  - `leaderboardScore`: DESCENDING
  - `xp`: DESCENDING

### 3.4. District / Exam-Wise Leaderboard Query
- **Query**:
  ```kotlin
  firestore.collection("leaderboard_entries")
      .whereEqualTo("examId", "appsc-group2")
      .whereEqualTo("district", selectedDistrict)
      .whereEqualTo("periodType", "GLOBAL")
      .orderBy("leaderboardScore", Query.Direction.DESCENDING)
      .limit(50)
  ```
- **Index**:
  - `examId`: ASCENDING
  - `district`: ASCENDING
  - `periodType`: ASCENDING
  - `leaderboardScore`: DESCENDING

### 3.5. Subject-Wise Leaderboard Query
- **Query**:
  ```kotlin
  firestore.collection("leaderboard_entries")
      .whereEqualTo("examId", "appsc-group2")
      .whereEqualTo("subjectId", selectedSubjectId)
      .whereEqualTo("periodType", "SUBJECT")
      .orderBy("leaderboardScore", Query.Direction.DESCENDING)
      .limit(50)
  ```
- **Index**:
  - `examId`: ASCENDING
  - `subjectId`: ASCENDING
  - `periodType`: ASCENDING
  - `leaderboardScore`: DESCENDING

### 3.6. User Lookup & History
- **Query**:
  ```kotlin
  firestore.collection("leaderboard_entries")
      .whereEqualTo("userId", currentUserId)
      .whereEqualTo("periodType", periodType)
      .orderBy("updatedAt", Query.Direction.DESCENDING)
  ```
- **Index**:
  - `userId`: ASCENDING
  - `periodType`: ASCENDING
  - `updatedAt`: DESCENDING

---

## 4. Anti-Cheat & Fair Scoring Formula

Leaderboard score is computed using the centralized, configurable formula to prevent spamming:

$$\text{LeaderboardScore} = \text{XP} + (\text{Accuracy} \times 30) + (\text{TestsCompleted} \times 40) + (\text{StreakDays} \times 20)$$

All XP allocations verify idempotency using `XPTransactionEntity` records (`mock_${testId}_${totalQuestions}_${correctAnswers}`) before syncing to Firestore.
