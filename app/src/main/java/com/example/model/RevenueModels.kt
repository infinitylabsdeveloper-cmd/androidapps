package com.example.model

enum class PaymentStatus {
    SUCCESS, PENDING, FAILED, REFUNDED, CANCELLED
}

enum class PaymentMethod {
    UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING, WALLET
}

data class TransactionModel(
    val id: String,
    val appId: String = "appsc-group2",
    val userId: String,
    val userName: String,
    val planId: String,
    val planName: String,
    val amount: Double,
    val couponCode: String?,
    val discountAmount: Double,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    val timestamp: Long
)

data class RefundRequestModel(
    val id: String,
    val appId: String = "appsc-group2",
    val transactionId: String,
    val userId: String,
    val userName: String,
    val amount: Double,
    val reason: String,
    val status: String, // PENDING, APPROVED, REJECTED
    val requestedAt: Long
)
