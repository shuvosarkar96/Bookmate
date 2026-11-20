package com.bookmate.app.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.repository.AdminRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.util.parsePlanPrice
import com.bookmate.app.util.planPriceLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlanPriceUiState(
    val isLoading: Boolean = true,
    val planName: String = "",
    val currentLabel: String = "",
    val input: String = "",
    val isSaving: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val savedLabel: String? = null
)

@HiltViewModel
class PlanPriceViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanPriceUiState())
    val uiState: StateFlow<PlanPriceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionRepository.getPlan()
                .onSuccess { plan ->
                    _uiState.update {
                        it.copy(isLoading = false, planName = plan?.name.orEmpty(), currentLabel = plan?.priceLabel.orEmpty())
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, errorRes = null, savedLabel = null) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving) return
        val price = parsePlanPrice(state.input)
        if (price == null) {
            _uiState.update { it.copy(errorRes = R.string.planprice_invalid, savedLabel = null) }
            return
        }
        val label = planPriceLabel(price)
        _uiState.update { it.copy(isSaving = true, errorRes = null, savedLabel = null) }
        viewModelScope.launch {
            adminRepository.updatePlanPrice(price, label)
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, currentLabel = label, input = "", savedLabel = label)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false, errorRes = R.string.error_unknown) }
                }
        }
    }
}