package com.bookmate.app.ui.subscription

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.Plan
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.util.formatDate
import com.bookmate.app.util.nextEndDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val plan: Plan? = null,
    val newEndText: String = "",
    val isSubmitting: Boolean = false,
    val completed: Boolean = false,
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorRes = null) }
        viewModelScope.launch {
            val planResult = repository.getPlan()
            val subResult = repository.getMySubscription()
            val plan = planResult.getOrNull()
            if (planResult.isFailure || subResult.isFailure || plan == null) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                return@launch
            }
            val newEnd = nextEndDate(subResult.getOrNull(), plan.periodDays)
            _uiState.update {
                it.copy(isLoading = false, plan = plan, newEndText = formatDate(newEnd))
            }
        }
    }

    fun confirm() {
        val state = _uiState.value
        val plan = state.plan ?: return
        if (state.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, errorRes = null) }
        viewModelScope.launch {
            repository.subscribe(plan.periodDays)
                .onSuccess {
                    _uiState.update { s -> s.copy(isSubmitting = false, completed = true) }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(isSubmitting = false, errorRes = R.string.checkout_failed)
                    }
                }
        }
    }
}