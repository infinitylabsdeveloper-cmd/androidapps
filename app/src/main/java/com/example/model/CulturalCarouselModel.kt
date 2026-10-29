package com.example.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.UUID

/**
 * Domain and Firestore model for Andhra Pradesh Cultural Carousel slides.
 * Dynamic and customizable by Admins in real-time.
 */
@IgnoreExtraProperties
data class CulturalCarouselItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val teluguTitle: String = "",
    val subtitle: String = "",
    val badgeText: String = "ANDHRA HERITAGE",
    val tag: String = "Culture",
    val imageUrl: String = "",
    val description: String = "",
    val orderIndex: Int = 0,
    val isEnabled: Boolean = true,
    val fallbackGradientStart: Long = 0xFF0F172A,
    val fallbackGradientEnd: Long = 0xFF1E293B,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefaultCulturalItems(): List<CulturalCarouselItem> = listOf(
            CulturalCarouselItem(
                id = "araku-valley-01",
                title = "Araku Valley & Coffee Plantations",
                teluguTitle = "అరకు లోయ & కాఫీ తోటలు",
                subtitle = "Lush Terraced Hills • Eastern Ghats, Visakhapatnam",
                badgeText = "NATURAL WONDER",
                tag = "Landscape",
                imageUrl = "https://images.unsplash.com/photo-1627483262769-04d0a1401487?q=80&w=1200&auto=format&fit=crop",
                description = "Famous for breathtaking emerald valleys, organic coffee estates, and gentle mist-wrapped mountain ranges.",
                orderIndex = 1,
                isEnabled = true,
                fallbackGradientStart = 0xFF064E3B,
                fallbackGradientEnd = 0xFF0F172A
            ),
            CulturalCarouselItem(
                id = "dhimsa-dance-02",
                title = "Dhimsa (Dhemsa) Tribal Dance",
                teluguTitle = "ధింసా గిరిజన సాంప్రదాయ నృత్యం",
                subtitle = "Living Heritage • Alluri Sitharama Raju District",
                badgeText = "TRIBAL CULTURE",
                tag = "Dance & Traditions",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?q=80&w=1200&auto=format&fit=crop",
                description = "Ancient community folk dance of Araku valley tribes, celebrated with Mori and Tudumu rhythmic beats in traditional yellow-red sarees.",
                orderIndex = 2,
                isEnabled = true,
                fallbackGradientStart = 0xFF78350F,
                fallbackGradientEnd = 0xFF1E1B4B
            ),
            CulturalCarouselItem(
                id = "eastern-ghats-03",
                title = "Misty Peaks of Eastern Ghats",
                teluguTitle = "తూర్పు కనుమల కొండ శ్రేణులు",
                subtitle = "Sunrise Over Lambasinghi & Paderu Highlands",
                badgeText = "SCENIC HIGHLANDS",
                tag = "Mountains",
                imageUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1200&auto=format&fit=crop",
                description = "Often called the Kashmir of Andhra Pradesh, where winter mornings touch zero degrees amidst thick rolling clouds.",
                orderIndex = 3,
                isEnabled = true,
                fallbackGradientStart = 0xFF1E3A8A,
                fallbackGradientEnd = 0xFF0F172A
            ),
            CulturalCarouselItem(
                id = "lepakshi-heritage-04",
                title = "Lepakshi Temple & Hanging Pillar",
                teluguTitle = "లేపాక్షి వీరభద్ర ఆలయం & వేలాడే స్తంభం",
                subtitle = "Monolithic Vijayanagara Marvel • Sri Sathya Sai District",
                badgeText = "ARCHITECTURAL MARVEL",
                tag = "Temples & History",
                imageUrl = "https://images.unsplash.com/photo-1599707367072-cd6ada2bc375?q=80&w=1200&auto=format&fit=crop",
                description = "Spectacular 16th-century temple featuring intricate stone carvings, the colossal monolithic Nandi, and the world-famous Hanging Pillar.",
                orderIndex = 4,
                isEnabled = true,
                fallbackGradientStart = 0xFF831843,
                fallbackGradientEnd = 0xFF18181B
            ),
            CulturalCarouselItem(
                id = "borra-caves-05",
                title = "Borra Caves & Gosthani River",
                teluguTitle = "బొర్రా గుహలు • అనంతగిరి కొండలు",
                subtitle = "Million-Year-Old Speleothems • Ananthagiri Hills",
                badgeText = "GEOLOGICAL WONDER",
                tag = "Natural Wonder",
                imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1200&auto=format&fit=crop",
                description = "One of India's deepest limestone cave systems, carved naturally by the subterranean Gosthani River with breathtaking stalactite formations.",
                orderIndex = 5,
                isEnabled = true,
                fallbackGradientStart = 0xFF14532D,
                fallbackGradientEnd = 0xFF020617
            ),
            CulturalCarouselItem(
                id = "seshachalam-hills-06",
                title = "Tirumala Seshachalam Biosphere",
                teluguTitle = "తిరుమల శేషాచలం కొండలు",
                subtitle = "Sacred Seven Hills & Red Sanders Forest • Tirupati",
                badgeText = "SACRED BIOSPHERE",
                tag = "Spiritual & Ecology",
                imageUrl = "https://images.unsplash.com/photo-1610123598147-f632aa18b875?q=80&w=1200&auto=format&fit=crop",
                description = "Part of the Eastern Ghats biosphere reserve, famous for spiritual devotion, ancient flora, and the sacred Sri Venkateswara shrine.",
                orderIndex = 6,
                isEnabled = true,
                fallbackGradientStart = 0xFF581C87,
                fallbackGradientEnd = 0xFF0F172A
            ),
            CulturalCarouselItem(
                id = "prakasam-barrage-07",
                title = "Prakasam Barrage at Twilight",
                teluguTitle = "ప్రకాశం బ్యారేజ్ • విజయవాడ కృష్ణా నది",
                subtitle = "Historic Gateway Across Krishna River • Vijayawada",
                badgeText = "HISTORIC LANDMARK",
                tag = "Riverfront & Heritage",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1200&auto=format&fit=crop",
                description = "Iconic engineering bridge connecting Krishna and Guntur districts, illuminated vividly at sunset over the grand Krishna River.",
                orderIndex = 7,
                isEnabled = true,
                fallbackGradientStart = 0xFF0369A1,
                fallbackGradientEnd = 0xFF0F172A
            ),
            CulturalCarouselItem(
                id = "kalamkari-art-08",
                title = "Kalamkari Art & Handloom Heritage",
                teluguTitle = "శ్రీకాళహస్తి కలంకారీ కళ & చేనేత సంస్కృతి",
                subtitle = "Ancient Organic Dye Craft • Srikalahasti Tradition",
                badgeText = "ARTISAN HERITAGE",
                tag = "Handicrafts & Art",
                imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?q=80&w=1200&auto=format&fit=crop",
                description = "Centuries-old freehand pen art using organic bamboo reeds and natural vegetable dyes depicting epics and flora.",
                orderIndex = 8,
                isEnabled = true,
                fallbackGradientStart = 0xFF7C2D12,
                fallbackGradientEnd = 0xFF18181B
            )
        )
    }
}
