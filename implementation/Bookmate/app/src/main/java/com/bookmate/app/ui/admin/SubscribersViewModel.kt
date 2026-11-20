package com.bookmate.app.ui.admin

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.MemberSubscription
import com.bookmate.app.domain.model.Subscription
import com.bookmate.app.domain.repository.AdminRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.util.activateSubscription
import com.bookmate.app.util.endSubscription
import com.bookmate.app.util.extendSubscription
import com.bookmate.app.util.filterMembers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_PERIOD_DAYS = 30

data class SubscribersUiState(
    val isLoading: Boolean = true,
    /** Members that match the search text. */
    val members: List<MemberSubscription> = emptyList(),
    val total: Int = 0,
    val query: String = "",
    val periodDays: Int = DEFAULT_PERIOD_DAYS,
    @StringRes val errorRes: Int? = null,
    /** The Member whose action is being saved. Other actions wait. */
    val busyUserId: String? = null,
    /** The Member waiting for the Admin to confirm "End". */
    val confirmEnd: MemberSubscription? = null,
    @StringRes val noticeRes: Int? = null,
    val noticeName: String = ""
)

@HiltViewModel
class SubscribersViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private var allMembers: List<MemberSubscription> = emptyList()

    private val _uiState = MutableStateFlow(SubscribersUiState())
    val uiState: StateFlow<SubscribersUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown. */
    fun load() {
        _uiState.update { it.copy(isLoading = allMembers.isEmpty(), errorRes = null) }
        viewModelScope.launch {
            val days = subscriptionRepository.getPlan().getOrNull()?.periodDays ?: DEFAULT_PERIOD_DAYS
            adminRepository.getMembers()
                .onSuccess { members ->
                    allMembers = members
                    _uiState.update { refreshed(it).copy(isLoading = false, periodDays = days) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                }
        }
    }

    fun onQueryChange(value: String) {
        _uiState.update { refreshed(it.copy(query = value)) }
    }

    fun onActivate(member: MemberSubscription) {
        val days = _uiState.value.periodDays
        save(member, activateSubscription(member.userId, days), R.string.subscribers_done_activate)
    }

    fun onExtend(member: MemberSubscription) {
        val days = _uiState.value.periodDays
        save(member, extendSubscription(member.userId, member.subscription, days), R.string.subscribers_done_extend)
    }

    fun onEndClick(member: MemberSubscription) {
        _uiState.update { it.copy(confirmEnd = member, noticeRes = null) }
    }

    fun onDismissEnd() {
        _uiState.update { it.copy(confirmEnd = null) }
    }

    fun onConfirmEnd() {
        val member = _uiState.value.confirmEnd ?: return
        _uiState.update { it.copy(confirmEnd = null) }
        val current = member.subscription ?: return
        save(member, endSubscription(current), R.string.subscribers_done_end)
    }

    private fun save(member: MemberSubscription, updated: Subscription, @StringRes noticeRes: Int) {
        if (_uiState.value.busyUserId != null) return
        _uiState.update { it.copy(busyUserId = member.userId, errorRes = null, noticeRes = null) }
        viewModelScope.launch {
            adminRepository.saveSubscription(updated)
                .onSuccess {
                    allMembers = allMembers.map {
                        if (it.userId == member.userId) it.copy(subscription = updated) else it
                    }
                    _uiState.update {
                        refreshed(it).copy(
                            busyUserId = null,
                            noticeRes = noticeRes,
                            noticeName = member.name.ifBlank { member.email }
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(busyUserId = null, errorRes = R.string.error_unknown) }
                }
        }
    }

    private fun refreshed(state: SubscribersUiState): SubscribersUiState =
        state.copy(members = filterMembers(allMembers, state.query), total = allMembers.size)
}