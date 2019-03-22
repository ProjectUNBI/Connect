package com.unbi.connect.async

import android.os.AsyncTask
import android.util.Log
import com.unbi.connect.*
import com.unbi.connect.messaging.*
import java.net.Socket
import java.io.*
import java.net.InetAddress

class ServerAsync(val listener: Listener, val logger: Logger, val toaster: Toaster?) : AsyncTask<Socket, Void, MyMessage>() {
    private val LOGTAG: String = "ServerClientAsync"
    override fun doInBackground(vararg params: Socket?): MyMessage? {
        val socket = params[0]
        var issocketclosed=false
        if (socket == null) {
            return null
        }
        try {

            val stream = socket.getInputStream()
            val out = PrintWriter(
                socket.getOutputStream(),
                true
            )

            //out.println("Welcome to \""+Server_Name+"\" Server");//it is the reply

            val br = BufferedReader(
                InputStreamReader(stream)
            )
            val str=br.readLine()
            socket.close();
            issocketclosed=true
            val msgType=MessageProcessor(str,logger).messageTaskType
            if (msgType.message == null) {
                return null
            }
            if(msgType.type== TO_NOT_TODO){
                return null
            }
            if(msgType.type== TO_REDIRECT){
                msgType.message.send(toaster,logger)
            }
            if(msgType.type== TO_TRIGGER){
                return msgType.message
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


    override fun onPostExecute(message: MyMessage?) {
        super.onPostExecute(message)
        if (message == null) {
            return
        }
        //from this return if salt to check is null. we dont want to parse such message
        if (message.saltToCheck == null) {
            logger.show(LOG_TYPE_ERROR, "Salt is null and message is not of init type")
            return
        }
        val valid = ApplicationInstance.instance.SaltDataArray.isValid(message.saltToCheck as TimeBaseObject)

        if (!ApplicationInstance.instance.SaltDataArray.isValid(message.saltToCheck)) {
            logger.show(LOG_TYPE_ERROR, "Invalid Salt and message is not of init type")
            Log.d(LOGTAG, "salt is not valid")
            return
        }

        //now we have got a valid message .... do something
        //todo perform some interesting Task
        listener.ActionComplete(message)
    }

}


class ClientAsync(val toaster: Toaster?, val logger: Logger) : AsyncTask<MyMessage, String,Unit>() {

    override fun doInBackground(vararg params: MyMessage) {
        val message = params[0]

        val stringTosend = message.getEncryptedMsg()

        try {
            val serverAddr = InetAddress.getByName(message.sender.ip)
            val socket = Socket(serverAddr, message.sender.port)

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
                logger.show(LOG_TYPE_ERROR, e.message.toString())
                publishProgress(e.message)
                Log.e(MyMessage::class.java.simpleName, e.message)

            } finally {
                input.close()
                out.close()
                socket.close()
            }

        } catch (e: Exception) {
            logger.show(LOG_TYPE_ERROR, e.message.toString())
            publishProgress(e.message)
            Log.e(MyMessage::class.java.simpleName, e.message)
        }
        logger.show(LOG_TYPE_ERROR, "Suucessfully sent to: "+message.sender.ip+":"+message.sender.port)
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


