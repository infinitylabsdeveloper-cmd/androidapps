package com.example.data

import com.example.model.AlternativeBillingConfig
import com.example.model.PaymentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Manages configuration for Google Play Alternative Billing / User Choice Billing.
 * Enables dynamic remote toggles for payment providers, region eligibility, and merchant info.
 */
object AlternativeBillingConfigRepository {
    private val _config = MutableStateFlow(AlternativeBillingConfig.DEFAULT)
    val config: StateFlow<AlternativeBillingConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: AlternativeBillingConfig) {
        _config.value = newConfig
    }

    fun updateAlternativeBillingEnabled(enabled: Boolean) {
        _config.update { it.copy(isAlternativeBillingEnabled = enabled) }
    }

    fun updateEnabledProviders(providers: List<String>) {
        _config.update {
            it.copy(
                paymentProviders = providers,
                enabledPaymentMethods = providers,
                enabledProviders = providers.mapNotNull { p ->
                    when (p.lowercase()) {
                        "google_play" -> PaymentProvider.GOOGLE_PLAY
                        "razorpay" -> PaymentProvider.RAZORPAY
                        else -> null
                    }
                }
            )
        }
    }

    fun updateEligibleCountries(countries: List<String>) {
        _config.update { it.copy(eligibleCountries = countries) }
    }

    fun isProviderEnabled(provider: String, countryCode: String = "IN"): Boolean {
        val current = _config.value
        if (provider == "google_play") return true
        if (provider == "razorpay") {
            return current.isAlternativeBillingEnabled &&
                    current.isEligibleForCountry(countryCode) &&
                    (current.paymentProviders.contains("razorpay") || current.enabledPaymentMethods.contains("razorpay"))
        }
        return false
    }

    fun resetToDefault() {
        _config.value = AlternativeBillingConfig.DEFAULT
    }
}
