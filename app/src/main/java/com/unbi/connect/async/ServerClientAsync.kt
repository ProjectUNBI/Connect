package com.unbi.connect.async

import android.os.AsyncTask
import android.util.Log
import com.unbi.connect.*
import com.unbi.connect.messaging.*
import java.net.Socket
import java.io.*
import java.net.InetAddress

class ServerAsync(toaster: Toaster?, logger: Logger?,trig:TriggerTask) :  AssyncViewUpdater(toaster,logger,trig) {
    private val LOGTAG: String = "ServerClientAsync"
    override fun doInBackground(vararg params: Any?): MyMessage? {
        val socket = params[0] as Socket
        var issocketclosed=false
        try {

            val stream = socket.getInputStream()
//            val out = PrintWriter(// for response
//                socket.getOutputStream(),
//                true
//            )
            //out.println("Welcome to \""+Server_Name+"\" Server");//it is the reply
            // actually i dont want to do any response to the cleint.. we will send directly to the  cleint server ipPort
            val br = BufferedReader(
                InputStreamReader(stream)
            )
            val str=br.readLine()// read alll the line
            socket.close();
            issocketclosed=true
            val communicator=ApplicationInstance.instance.communicator
            if (communicator != null) {
                communicator.listenMessage(str,this)
            }else{
                Log.d(LOGTAG,"Communicator is null")
            }
        } catch (e: IOException) {
            mypublish(LOGTYPE,LOG_TYPE_ERROR, e.message.toString())
            e.printStackTrace()
        } finally {
            if(!issocketclosed)
            socket.close();
        }
        return null
    }




}


class ClientAsync(private val ipPort: IpPort, toaster: Toaster?, logger: Logger?) :  AssyncViewUpdater(
    toaster,
    logger
) {

    override fun doInBackground(vararg params: Any) {
        val stringTosend =params[0] as String
        try {
            val serverAddr = InetAddress.getByName(ipPort.ip)
            val socket = Socket(serverAddr, ipPort.port)

            //made connection, setup the read (in) and write (out)
            val out = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))

            try {
                //write a message to the server
                out.println(stringTosend)
                //read back a message from the server.
                /***
                 * Dont ever read from the server back Because as the server dont know the length
                 */
                //val str = input.readLine()
                out.flush()
            } catch (e: Exception) {
                mypublish(LOGTYPE,LOG_TYPE_ERROR, e.message.toString())
                mypublish(TOASTYPE,e.message)
                Log.e(MyMessage::class.java.simpleName, e.message)

            } finally {
                input.close()
                out.close()
                socket.close()
            }

        } catch (e: Exception) {
            mypublish(LOGTYPE,LOG_TYPE_ERROR, e.message.toString())
            mypublish(TOASTYPE,e.message)
            Log.e(MyMessage::class.java.simpleName, e.message)
        }
        mypublish(LOGTYPE, LOG_TYPE_SUCCESS, "Sucessfully sent to: "+ipPort.ip+":"+ipPort.port)
//        mypublish(TOASTYPE,"success")
        return
    }

}


abstract class  AssyncViewUpdater(
    val toaster: Toaster?,
    val logger: Logger?,
    val trig: TriggerTask?=null
) : AsyncTask<Any,Any,Any>(){
    companion object {
        val TOASTYPE=1
        val LOGTYPE=2
        val CAPTURE_TYPE=3

    }
    fun mypublish(vararg values: Any?){
        publishProgress(*values)
    }
    override fun onProgressUpdate(vararg values: Any) {
        super.onProgressUpdate(*values)
        when(values[0] as Int){
            TOASTYPE->{
                if (toaster != null) {
                    toaster.show(values[1] as String)
                }
            }
            LOGTYPE->{
                logger?.show(values[1] as Int, values[2] as String)
            }
            CAPTURE_TYPE->{
                trig?.triggered(values[1] as MyMessage,this)
            }
        }

    }
}















