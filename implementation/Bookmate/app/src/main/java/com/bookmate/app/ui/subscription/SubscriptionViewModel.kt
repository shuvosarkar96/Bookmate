package com.bookmate.app.ui.subscription

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.Plan
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.util.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SubscriptionStatus { NONE, ACTIVE, EXPIRED }

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val plan: Plan? = null,
    val status: SubscriptionStatus = SubscriptionStatus.NONE,
    val endDateText: String = "",
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown, so the status is always fresh. */
    fun load() {
        _uiState.update { it.copy(isLoading = it.plan == null, errorRes = null) }
        viewModelScope.launch {
            val planResult = repository.getPlan()
            val subResult = repository.getMySubscription()
            if (planResult.isFailure || subResult.isFailure) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                return@launch
            }
            val subscription = subResult.getOrNull()
            val status = when {
                subscription == null -> SubscriptionStatus.NONE
                subscription.isActive() -> SubscriptionStatus.ACTIVE
                else -> SubscriptionStatus.EXPIRED
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    plan = planResult.getOrNull(),
                    status = status,
                    endDateText = subscription?.let { s -> formatDate(s.endDate) }.orEmpty(),
                    errorRes = if (planResult.getOrNull() == null) R.string.error_unknown else null
                )
            }
        }
    }
}