package com.les.chat.models

class UserState(val typing : Boolean, val seen: Boolean) {
    constructor(): this(false, false)
}