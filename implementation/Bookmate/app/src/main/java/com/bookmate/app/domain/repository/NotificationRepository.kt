package com.bookmate.app.domain.repository

import com.bookmate.app.domain.model.AppNotification
import com.bookmate.app.util.NewNotification

interface NotificationRepository {
    /** The signed-in user's notifications, newest first. */
    suspend fun getNotifications(): Result<List<AppNotification>>

    /** Creates a new notification. The id must not exist yet (the rules do not allow rewriting). */
    suspend fun create(notification: NewNotification): Result<Unit>

    suspend fun markRead(id: String): Result<Unit>

    suspend fun delete(id: String): Result<Unit>
}