package com.example.staff.input

class UserPermissionInput {
    var group: Int? = null
    val method: String? = null
}

class UserContactInput {
    var contact: String? = null
    var value: String? = null
}

class UserPropertyInput {
    var property: String? = null
    var value: String? = null
    var lang: String? = null
}

class UserInput {
    var id: Int? = null
    var login: String? = null
    var group: List<Int> = listOf()
    var contact: List<UserContactInput> = listOf()
    var permission: List<UserPermissionInput> = listOf()
    var property: List<UserPropertyInput> = listOf()
}