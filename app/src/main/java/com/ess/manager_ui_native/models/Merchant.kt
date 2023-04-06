package com.ess.manager_ui_native.models

data class Merchant (
     val id: Long,
     val username: String,
     val firstName: String,
     val lastName: String,
     val location: String,
     val roles: List<Role>,
     val isValid: Boolean
)