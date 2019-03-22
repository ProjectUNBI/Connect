package com.unbi.connect

interface Listener{
    fun ActionComplete(message:MyMessage)
}

interface Logger{
    fun show(int: Int,msg:String)
}

interface Toaster{
    fun show(msg:String)
}