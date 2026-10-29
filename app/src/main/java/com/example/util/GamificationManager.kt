package com.example.util

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.data.SampleData
import com.example.model.Question
import kotlinx.coroutines.delay
import kotlin.random.Random

data class LeaderboardCandidate(
    val rank: Int,
    val name: String,
    val district: String,
    val avatarColorHex: Long,
    val accuracy: Float,
    val mcqsSolved: Int,
    val avgSpeedSeconds: Int,
    val xpPoints: Int,
    val isCurrentUser: Boolean = false,
    val titleBadge: String? = null
)

data class DuelOpponent(
    val name: String,
    val district: String,
    val avatarColorHex: Long,
    val title: String,
    val accuracyBias: Float = 0.85f,
    val speedBiasSeconds: Int = 8
)

object GamificationManager {

    val currentUserXp = mutableStateOf(1850)
    val currentUserRankDaily = mutableStateOf(28)
    val currentUserRankWeekly = mutableStateOf(42)
    val currentUserAccuracy = mutableStateOf(86.5f)
    val currentUserDuelsWon = mutableStateOf(14)
    val currentUserDuelsLost = mutableStateOf(3)

    // Pre-generated realistic candidates across AP districts
    private val baseCandidates = listOf(
        LeaderboardCandidate(1, "K. Venkata Raman", "Visakhapatnam", 0xFF2563EB, 97.2f, 1420, 9, 5840, titleBadge = "🏆 State Topper"),
        LeaderboardCandidate(2, "S. Lavanya Devi", "Vijayawada", 0xFF9333EA, 95.8f, 1380, 11, 5520, titleBadge = "⚡ Speed Prodigy"),
        LeaderboardCandidate(3, "M. Sai Krishna", "Guntur", 0xFF059669, 94.4f, 1310, 10, 5290, titleBadge = "🎯 Accuracy Master"),
        LeaderboardCandidate(4, "P. Anusha Reddy", "Tirupati", 0xFFD97706, 93.0f, 1250, 12, 4980, titleBadge = "Top 1%"),
        LeaderboardCandidate(5, "R. Bhaskar Rao", "Kurnool", 0xFFE11D48, 92.5f, 1190, 13, 4710, titleBadge = "Top 1%"),
        LeaderboardCandidate(6, "T. Harshitha", "Nellore", 0xFF0D9488, 91.8f, 1140, 12, 4550),
        LeaderboardCandidate(7, "B. Kalyan Kumar", "Rajahmundry", 0xFF4F46E5, 90.6f, 1080, 14, 4320),
        LeaderboardCandidate(8, "V. Sneha Latha", "Kakinada", 0xFFBE185D, 89.9f, 1040, 13, 4180),
        LeaderboardCandidate(9, "Ch. Naveen Varma", "Anantapur", 0xFFB45309, 89.1f, 990, 15, 3990),
        LeaderboardCandidate(10, "G. Divya Prasanthi", "Kadapa", 0xFF047857, 88.7f, 960, 14, 3850),
        LeaderboardCandidate(11, "J. Suresh Naidu", "Srikakulam", 0xFF1D4ED8, 88.0f, 920, 16, 3710),
        LeaderboardCandidate(12, "D. Rajesh Patel", "Eluru", 0xFF6D28D9, 87.4f, 890, 15, 3590),
        LeaderboardCandidate(13, "N. Bhavani Shankar", "Machilipatnam", 0xFF0369A1, 87.1f, 860, 14, 3480)
    )

    fun getDailyLeaderboard(selectedDistrict: String = "All"): List<LeaderboardCandidate> {
        val userCandidate = LeaderboardCandidate(
            rank = currentUserRankDaily.value,
            name = SampleData.sampleUserProfile.name.ifBlank { "You (Candidate)" },
            district = "Visakhapatnam",
            avatarColorHex = 0xFFFF7A00,
            accuracy = currentUserAccuracy.value,
            mcqsSolved = 340,
            avgSpeedSeconds = 13,
            xpPoints = currentUserXp.value,
            isCurrentUser = true,
            titleBadge = "🔥 Rising Star"
        )

        val list = mutableListOf<LeaderboardCandidate>()
        list.addAll(baseCandidates)
        // Insert user in their current rank position
        list.add(userCandidate)
        val sorted = list.sortedBy { it.rank }

        return if (selectedDistrict == "All") sorted else sorted.filter { it.district.equals(selectedDistrict, ignoreCase = true) || it.isCurrentUser }
    }

    fun getWeeklyLeaderboard(): List<LeaderboardCandidate> {
        val userCandidate = LeaderboardCandidate(
            rank = currentUserRankWeekly.value,
            name = SampleData.sampleUserProfile.name.ifBlank { "You (Candidate)" },
            district = "Visakhapatnam",
            avatarColorHex = 0xFFFF7A00,
            accuracy = currentUserAccuracy.value,
            mcqsSolved = 840,
            avgSpeedSeconds = 12,
            xpPoints = currentUserXp.value + 400,
            isCurrentUser = true,
            titleBadge = "Top 5% State"
        )
        val list = mutableListOf<LeaderboardCandidate>()
        list.addAll(baseCandidates.mapIndexed { idx, c ->
            c.copy(rank = idx + 1, mcqsSolved = c.mcqsSolved * 3, xpPoints = c.xpPoints * 3)
        })
        list.add(userCandidate)
        return list.sortedBy { it.rank }
    }

    fun addXp(points: Int) {
        currentUserXp.value += points
        // Boost rank as XP increases
        if (currentUserRankDaily.value > 5) {
            currentUserRankDaily.value = maxOf(4, currentUserRankDaily.value - (points / 200).coerceAtLeast(1))
        }
    }

    fun getAvailableDuelBots(): List<DuelOpponent> {
        return listOf(
            DuelOpponent("Arjun Reddy (Ghost)", "Vijayawada", 0xFF2563EB, "APPSC Rank #12 aspirant", 0.85f, 9),
            DuelOpponent("Kavitha Rao (Ghost)", "Guntur", 0xFF9333EA, "DSC State Topper runner", 0.90f, 7),
            DuelOpponent("Chaitanya V. (Ghost)", "Tirupati", 0xFF059669, "Rapid Fire Champion", 0.80f, 10),
            DuelOpponent("Bhavya Sri (Ghost)", "Visakhapatnam", 0xFFE11D48, "History & Polity Wizard", 0.88f, 8)
        )
    }

    /**
     * Get 10 rapid-fire questions for a speed duel sprint
     */
    fun getDuelQuestions(): List<Question> {
        val pool = if (SampleData.questionsState.isNotEmpty()) {
            SampleData.questionsState
        } else {
            SampleData.getQuestionsForPractice("history", "", "", "")
        }
        return pool.shuffled().take(10)
    }
}
