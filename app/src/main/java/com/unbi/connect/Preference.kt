package com.unbi.connect

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.R.id.edit
import android.content.SharedPreferences


object Userdata {

    val MY_PREFS_NAME: String = "com.unbi.connect.preferenece"
    var port: Int = 8080
    var global_password = ""
    var iscustomactivity = false
    var islogview_enable = false

    init {
        global_password = generatePassword()

    }
}