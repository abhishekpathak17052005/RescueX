package com.rescuex.data.model

data class User(val id: String, val name: String, val email: String, val phone: String, val isResponder: Boolean = false)
