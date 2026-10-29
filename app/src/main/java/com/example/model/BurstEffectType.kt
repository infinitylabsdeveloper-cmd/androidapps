package com.example.model

/**
 * 10 authentic answer burst feedback effects from the Practice & Appearance reference:
 * 1. Confetti Pop (Default for all sections)
 * 2. Star Burst
 * 3. Ripple Rings
 * 4. Fireworks
 * 5. Emoji Burst
 * 6. Sparkle Shine
 * 7. Hearts Rise
 * 8. Coin Shower
 * 9. Big Check Pop
 * 10. Shatter Flash
 */
enum class BurstEffectType(
    val effectId: String,
    val displayName: String,
    val description: String,
    val emoji: String,
    val isDefault: Boolean = false
) {
    CONFETTI_POP(
        effectId = "confetti",
        displayName = "Confetti Pop",
        description = "Vibrant tumbling confetti particles with gravity and 3D spin",
        emoji = "🎉",
        isDefault = true
    ),
    STAR_BURST(
        effectId = "star",
        displayName = "Star Burst",
        description = "Radiating golden stars flying outward with drag and rotation",
        emoji = "⭐"
    ),
    RIPPLE_RINGS(
        effectId = "ripple",
        displayName = "Ripple Rings",
        description = "Concentric expanding emerald shockwave rings with fading stroke",
        emoji = "💫"
    ),
    FIREWORKS(
        effectId = "fireworks",
        displayName = "Fireworks",
        description = "Staged multi-burst aerial fireworks arching with spark tails",
        emoji = "🎆"
    ),
    EMOJI_BURST(
        effectId = "emoji",
        displayName = "Emoji Burst",
        description = "Celebration emojis bursting radially outward with gentle gravity",
        emoji = "🥳"
    ),
    SPARKLE_SHINE(
        effectId = "sparkle",
        displayName = "Sparkle Shine",
        description = "Diamond sparkles twinkling and pulsing across the option area",
        emoji = "✨"
    ),
    HEARTS_RISE(
        effectId = "hearts",
        displayName = "Hearts Rise",
        description = "Floating colorful hearts rising upward with gentle sine wobble",
        emoji = "💖"
    ),
    COIN_SHOWER(
        effectId = "coins",
        displayName = "Coin Shower",
        description = "Gleaming 3D gold coins showering down like a winning jackpot",
        emoji = "🪙"
    ),
    BIG_CHECK_POP(
        effectId = "check",
        displayName = "Big Check Pop",
        description = "Centered green badge checkmark pop with expanding shockwave rings",
        emoji = "✅"
    ),
    SHATTER_FLASH(
        effectId = "shatter",
        displayName = "Shatter Flash",
        description = "Luminous emerald screen flash with glowing crystal shards",
        emoji = "💥"
    );

    companion object {
        val DEFAULT = CONFETTI_POP

        fun fromDisplayName(name: String?): BurstEffectType {
            if (name.isNullOrBlank()) return DEFAULT
            return entries.find {
                it.displayName.equals(name, ignoreCase = true) ||
                it.effectId.equals(name, ignoreCase = true) ||
                it.name.equals(name, ignoreCase = true)
            } ?: DEFAULT
        }
    }
}
