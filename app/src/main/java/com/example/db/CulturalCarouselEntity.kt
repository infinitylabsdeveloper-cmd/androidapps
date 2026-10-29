package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CulturalCarouselItem

@Entity(tableName = "cultural_carousel_items")
data class CulturalCarouselEntity(
    @PrimaryKey val id: String,
    val title: String,
    val teluguTitle: String,
    val subtitle: String,
    val badgeText: String,
    val tag: String,
    val imageUrl: String,
    val description: String,
    val orderIndex: Int,
    val isEnabled: Boolean,
    val fallbackGradientStart: Long,
    val fallbackGradientEnd: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomainModel(): CulturalCarouselItem = CulturalCarouselItem(
        id = id,
        title = title,
        teluguTitle = teluguTitle,
        subtitle = subtitle,
        badgeText = badgeText,
        tag = tag,
        imageUrl = imageUrl,
        description = description,
        orderIndex = orderIndex,
        isEnabled = isEnabled,
        fallbackGradientStart = fallbackGradientStart,
        fallbackGradientEnd = fallbackGradientEnd,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomainModel(item: CulturalCarouselItem): CulturalCarouselEntity = CulturalCarouselEntity(
            id = item.id,
            title = item.title,
            teluguTitle = item.teluguTitle,
            subtitle = item.subtitle,
            badgeText = item.badgeText,
            tag = item.tag,
            imageUrl = item.imageUrl,
            description = item.description,
            orderIndex = item.orderIndex,
            isEnabled = item.isEnabled,
            fallbackGradientStart = item.fallbackGradientStart,
            fallbackGradientEnd = item.fallbackGradientEnd,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }
}
