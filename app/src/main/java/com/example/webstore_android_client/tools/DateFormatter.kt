package com.example.webstore_android_client.tools

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatDate(instant: Instant?, format: String): String {
    val formatter: DateTimeFormatter?
    try {
        formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault())
    } catch (e: Exception) {
        println("Error creating date formatter: ${e.message}")
        return ""
    }
    return try {
        formatter.format(instant)
    } catch (e: Exception) {
        println("Error formatting date: ${e.message}")
        ""
    }
}

fun formatDate(dateTime: LocalDateTime?, format: String): String {
    val formatter: DateTimeFormatter?
    try {
        formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault())
    } catch (e: Exception) {
        println("Error creating date formatter: ${e.message}")
        return ""
    }
    return try {
        formatter.format(dateTime)
    } catch (e: Exception) {
        println("Error formatting date: ${e.message}")
        ""
    }
}
