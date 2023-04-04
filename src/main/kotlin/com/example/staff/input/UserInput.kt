package com.example.staff.input

class PermissionInput {
    val group: Int? = null
}

class UserContactInput {
    var contact: String? = null
    var value: String? = null
}

class UserInput {
    var id: Int? = null
    var login: String = ""
    var group: List<Int> = listOf()
    var contact: List<UserContactInput> = listOf()
    var permission: List<PermissionInput> = listOf()
}