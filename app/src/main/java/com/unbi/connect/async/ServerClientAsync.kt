package com.unbi.connect.async

import android.os.AsyncTask
import android.util.Log
import com.unbi.connect.*
import com.unbi.connect.messaging.*
import java.net.Socket
import java.io.*
import java.net.InetAddress

class ServerAsync( val logger: Logger) : AsyncTask<Socket, Void, MyMessage>() {
    private val LOGTAG: String = "ServerClientAsync"
    override fun doInBackground(vararg params: Socket?): MyMessage? {
        val socket = params[0]
        var issocketclosed=false
        if (socket == null) {
            return null
        }
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
                communicator.listenMessage(str)
            }else{
                Log.d(LOGTAG,"Communicator is null")
            }
        } catch (e: IOException) {
            logger.show(LOG_TYPE_ERROR, e.message.toString())
            e.printStackTrace()
        } finally {
            if(!issocketclosed)
            socket.close();
        }
        return null
    }


}


class ClientAsync(val ipPort: IpPort, val toaster: Toaster?, val logger: Logger?) : AsyncTask<String, String,Unit>() {

    override fun doInBackground(vararg params: String) {
        val stringTosend =params[0]
        try {
            if (ipPort == null) {
                logger?.show(LOG_TYPE_ERROR, "Null ip and port")
                publishProgress("Null ip and port")
                Log.e(MyMessage::class.java.simpleName, "Null ip and port")
                return
            }
            val serverAddr = InetAddress.getByName(ipPort.ip)
            val socket = Socket(serverAddr, ipPort.port)

            //made connection, setup the read (in) and write (out)
            val out = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))

            try {
                //write a message to the server
                out.println(stringTosend)
                //read back a message from the server.
                val str = input.readLine()
                out.flush()
            } catch (e: Exception) {
                logger?.show(LOG_TYPE_ERROR, e.message.toString())
                publishProgress(e.message)
                Log.e(MyMessage::class.java.simpleName, e.message)

            } finally {
                input.close()
                out.close()
                socket.close()
            }

        } catch (e: Exception) {
            logger?.show(LOG_TYPE_ERROR, e.message.toString())
            publishProgress(e.message)
            Log.e(MyMessage::class.java.simpleName, e.message)
        }
        logger?.show(LOG_TYPE_ERROR, "Suucessfully sent to: "+ipPort.ip+":"+ipPort.port)
        publishProgress("success")
        return
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        if (toaster != null) {
            toaster.show(values[0])
        }
    }


}





















