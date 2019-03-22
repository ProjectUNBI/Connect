package com.unbi.connect

import com.unbi.connect.messaging.MyMessage

interface Listener{
    fun ActionComplete(message: MyMessage)
}

interface Logger{
    fun show(int: Int,msg:String)
}

interface Toaster{
    fun show(msg:String)
}