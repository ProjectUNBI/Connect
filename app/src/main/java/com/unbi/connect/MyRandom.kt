package com.unbi.connect

import com.unbi.connect.util_classes.PasswordGenration
import java.util.concurrent.atomic.AtomicInteger



fun generatePassword():String {
    return PasswordGenration.password()
}


//generating notification ID
private val c = AtomicInteger(0)
fun getID(): Int {
    return c.incrementAndGet()
}