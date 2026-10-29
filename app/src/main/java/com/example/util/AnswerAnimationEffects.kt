package com.example.util

data class AnswerAnimationSpec(
    val id: Int,
    val name: String,
    val description: String,
    val badge: String,
    val iconEmoji: String
)

object AnswerAnimationEffects {
    val ANIMATIONS = listOf(
        AnswerAnimationSpec(1, "Subtle Glow & Ripple", "Soft, elegant pulsing glow with a gentle ripple wave", "DEFAULT", "✨"),
        AnswerAnimationSpec(2, "Smooth Scale Pulse", "Smooth spring-backed scale expansion for correct answers", "POPULAR", "🎯"),
        AnswerAnimationSpec(3, "Radiant Golden Shimmer", "Brilliant radiant shimmer effect with gold highlights", "PREMIUM", "🌟"),
        AnswerAnimationSpec(4, "Emerald Success Burst", "Vibrant green success burst with floating particles", "SUCCESS", "🟢"),
        AnswerAnimationSpec(5, "Neon Cyber Spark", "Futuristic neon electric spark and glow effect", "CYBER", "⚡"),
        AnswerAnimationSpec(6, "Royal Violet Wave", "Luxurious royal purple ripple wave and crown glow", "ROYAL", "👑"),
        AnswerAnimationSpec(7, "Celestial Starry Flash", "Dazzling night sky starry sparkle and twinkle", "MAGIC", "🌠"),
        AnswerAnimationSpec(8, "Ruby Prism Ripple", "Dynamic red ruby prismatic reflection wave", "VIBRANT", "💎"),
        AnswerAnimationSpec(9, "Sapphire Glow Aura", "Deep blue sapphire glowing halo aura", "SERENE", "🌊"),
        AnswerAnimationSpec(10, "Amber Sunburst Pulse", "Warm golden sunburst radiating outward energy", "WARM", "☀️"),
        AnswerAnimationSpec(11, "Diamond Shimmer Sparkle", "Ultra-crisp diamond facet light flash", "LUXE", "✨"),
        AnswerAnimationSpec(12, "Neon Mint Flash", "Fresh mint green energetic pulse and glow", "FRESH", "🍃"),
        AnswerAnimationSpec(13, "Crimson Velvet Wave", "Deep velvet crimson elegant ripple wave", "ELEGANT", "🌹"),
        AnswerAnimationSpec(14, "Oceanic Blue Ripple", "Calm soothing ocean wave pulse", "OCEAN", "🐳"),
        AnswerAnimationSpec(15, "Amethyst Crystal Pulse", "Mystical purple crystal energy pulse", "MYSTIC", "🔮"),
        AnswerAnimationSpec(16, "Sunset Horizon Glow", "Warm orange-pink twilight horizon gradient flash", "SUNSET", "🌇"),
        AnswerAnimationSpec(17, "Electric Indigo Spark", "High-voltage indigo lightning spark flash", "ELECTRIC", "⚡"),
        AnswerAnimationSpec(18, "Velvet Rose Shimmer", "Soft pink rose petal shimmering glow", "ROSE", "🌸"),
        AnswerAnimationSpec(19, "Teal Aurora Wave", "Northern lights teal aurora borealis sweep", "AURORA", "🌌"),
        AnswerAnimationSpec(20, "Radiant Coral Pulse", "Energetic vibrant coral red pulse", "CORAL", "🪸"),
        AnswerAnimationSpec(21, "Cyber Lime Flash", "High-contrast lime green futuristic pulse", "MATRIX", "🟢"),
        AnswerAnimationSpec(22, "Platinum Star Burst", "Sleek metallic platinum star radiance", "SILVER", "⭐"),
        AnswerAnimationSpec(23, "Cosmic Nebula Glow", "Deep space stellar nebula cloud pulsation", "COSMIC", "🪐"),
        AnswerAnimationSpec(24, "Topaz Gold Ripple", "Warm yellow topaz crystalline ripple wave", "TOPAZ", "🟡"),
        AnswerAnimationSpec(25, "Garnet Red Sparkle", "Deep red gemstone fire sparkle flash", "GARNET", "🔴"),
        AnswerAnimationSpec(26, "Peridot Green Wave", "Vibrant lime-green peridot light wave", "PERIDOT", "🍏"),
        AnswerAnimationSpec(27, "Tanzanite Blue Pulse", "Deep blue-violet tanzanite glow pulse", "TANZANITE", "🔷"),
        AnswerAnimationSpec(28, "Onyx Sleek Shimmer", "Modern dark onyx metallic reflection shimmer", "ONYX", "⬛"),
        AnswerAnimationSpec(29, "Pearl White Halo", "Pristine white pearl iridescent halo glow", "PEARL", "⚪"),
        AnswerAnimationSpec(30, "Starlight Supernova", "Explosive magnificent starlight supernova flash", "ULTIMATE", "🌟")
    )

    fun getSpec(name: String): AnswerAnimationSpec {
        return ANIMATIONS.find { it.name.equals(name, ignoreCase = true) } ?: ANIMATIONS[0]
    }
}
