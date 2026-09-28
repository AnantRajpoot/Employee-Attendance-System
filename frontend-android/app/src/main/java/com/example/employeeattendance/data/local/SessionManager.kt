package com.example.employeeattendance.data.local

import android.content.Context

class SessionManager(context: Context) {

    private val preferences =
        context.getSharedPreferences("employee_session", Context.MODE_PRIVATE)

    fun saveSession(
        token: String,
        employeeId: Long,
        name: String,
        role: String
    ) {
        preferences.edit()
            .putString("token", token)
            .putLong("employeeId", employeeId)
            .putString("name", name)
            .putString("role", role)
            .apply()
    }

    fun getToken(): String? {
        return preferences.getString("token", null)
    }

    fun getEmployeeId(): Long {
        return preferences.getLong("employeeId", -1)
    }

    fun getName(): String? {
        return preferences.getString("name", null)
    }

    fun getRole(): String? {
        return preferences.getString("role", null)
    }

    fun saveReadNotificationIds(ids: Set<String>) {
        preferences.edit().putStringSet("read_notifications", ids).apply()
    }

    fun getReadNotificationIds(): Set<String> {
        return preferences.getStringSet("read_notifications", emptySet()) ?: emptySet()
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }
}
