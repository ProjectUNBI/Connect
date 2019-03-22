package com.unbi.connect.async

import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.unbi.connect.*
import com.unbi.connect.service.TCPservice
import java.net.Socket
import com.unbi.connect.util_classes.AES_Util
import java.io.*
import java.net.InetAddress

class ServerAsync(val listener: Listener, val logger: Logger) : AsyncTask<Socket, Void, MyMessage>() {
    private val LOGTAG: String = "ServerClientAsync"
    override fun doInBackground(vararg params: Socket?): MyMessage? {
        val socket = params[0]
        var msg: MyMessage? = null
        var decrypt: String? = null
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
            decrypt = AES_Util().decrypt(str)

        } catch (e: IOException) {
            logger.show(LOG_TYPE_ERROR, e.message.toString())
            e.printStackTrace()
        } finally {
            socket.close();
        }

        if (decrypt == null) {
            logger.show(LOG_TYPE_ERROR, "Decrypted Message is null/password missmatch")
            return null
        }
        msg = Gson().fromJson(decrypt, MyMessage::class.java)
        //now we are checking about message
        if (msg == null) {
            logger.show(LOG_TYPE_ERROR, "Message is null/not valid format")
            return null
        }
        if (msg.type == TYPE_INIT && msg.saltToCheck == null) {
            logger.show(LOG_TYPE_ERROR, "Sender salt is null,sending a valid salt")


            //todo send back generated a new salt
            return null
        }


        if (msg.type == TYPE_INIT && msg.saltToCheck != null) {
            val valid = ApplicationInstance.instance.SaltDataArray.isValid(msg.saltToCheck as TimeBaseObject)
            if (!valid) {
                logger.show(LOG_TYPE_ERROR, "Invalid Salt")
                return null
            }
            logger.show(LOG_TYPE_ERROR, "Valid salt...sending actual meaasge")
            val pendingmessage = ApplicationInstance.instance
                .PendingMessageDataArray
                .getAndPopTimeBaseObject(msg.uuidToCheck.uuid, PendingMessage::class.java)


            //todo send back the message
            //todo tack out the Pending messasge and send the message back
            return null
        }

        return msg
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


class ClientAsync(val toaster: Toaster, val logger: Logger) : AsyncTask<MyMessage, String,Unit>() {

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
        toaster.show(values[0])
    }


}


