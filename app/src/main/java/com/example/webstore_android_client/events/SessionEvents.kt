package com.example.webstore_android_client.events

import com.example.webstore_android_client.events.eventTypes.SessionEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object SessionEvents {

    private val _events = Channel<SessionEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

     fun sessionExpired() {
        _events.trySend(SessionEvent.Expired)
    }

    fun logout() {
        _events.trySend(SessionEvent.Logout)
    }
}