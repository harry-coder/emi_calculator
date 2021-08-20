package com.think.emicalculator.locationutils

import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object SmsUtils {


    fun sendSms(lat:String,lon:String,fileno:String?) {

        GlobalScope.launch(Dispatchers.IO) {
             try {
                // Construct data
                val apiKey = "apikey=" + "NDQ2NDcxNTY3NDcwNjE2YzM4NDM1NjY3NDU2NjY0NTg="
                val message = "&message=" + "Please help! My location ${lat} ${lon}"
                val sender = "&sender=" + "File no: $fileno"
                // val numbers = "&numbers=" + "9870566717"
                val numbers = "&numbers=" + "918447937397"

                // Send data
                val conn: HttpURLConnection =
                    URL("https://api.txtlocal.com/send/?").openConnection() as HttpURLConnection
                val data = apiKey + numbers + message + sender


                conn.setDoOutput(true)
                conn.setRequestMethod("POST")
                conn.setRequestProperty("Content-Length", Integer.toString(data.length))
                conn.getOutputStream().write(data.toByteArray(charset("UTF-8")))
                val rd = BufferedReader(InputStreamReader(conn.getInputStream()))
                val stringBuffer = StringBuffer()
                var line: String?
                while (rd.readLine().also { line = it } != null) {
                    stringBuffer.append(line)
                }
                rd.close()
                stringBuffer.toString()
                 withContext(Dispatchers.Main){
                 }
            } catch (e: Exception) {
                println("Error SMS $e")
                "Error $e"
            }
        }
    }
}