package com.example.webstore_android_client.events.eventTypes

sealed interface SessionEvent {
    object Expired : SessionEvent
    object Logout : SessionEvent
}