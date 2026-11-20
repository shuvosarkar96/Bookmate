package com.bookmate.app.ui.notifications

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.R
import com.bookmate.app.domain.model.AppNotification
import com.bookmate.app.domain.repository.NotificationRepository
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.util.neededNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val items: List<AppNotification> = emptyList(),
    @StringRes val errorRes: Int? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.items.isEmpty(), errorRes = null) }
            var list = notificationRepository.getNotifications().getOrNull()
            if (list == null) {
                _uiState.update { it.copy(isLoading = false, errorRes = R.string.error_unknown) }
                return@launch
            }
            val subscription = subscriptionRepository.getMySubscription().getOrNull()
            val needed = neededNotifications(subscription, list.map { it.id }.toSet(), System.currentTimeMillis())
            var created = false
            for (notice in needed) {
                if (notificationRepository.create(notice).isSuccess) created = true
            }
            if (created) list = notificationRepository.getNotifications().getOrNull() ?: list
            _uiState.update { it.copy(isLoading = false, items = known(list)) }
        }
    }

    fun onOpen(item: AppNotification) {
        if (item.read) return
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id == item.id) it.copy(read = true) else it })
        }
        viewModelScope.launch {
            notificationRepository.markRead(item.id).onFailure { load() }
        }
    }

    fun onRemove(item: AppNotification) {
        _uiState.update { state -> state.copy(items = state.items.filter { it.id != item.id }) }
        viewModelScope.launch {
            notificationRepository.delete(item.id).onFailure { load() }
        }
    }

    private fun known(list: List<AppNotification>): List<AppNotification> =
        list.filter { it.type == AppNotification.TYPE_ENDING || it.type == AppNotification.TYPE_ENDED }
}