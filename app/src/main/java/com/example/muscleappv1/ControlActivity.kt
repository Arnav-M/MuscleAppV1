package com.example.muscleappv1

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.util.*

class ControlActivity: AppCompatActivity() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("f0a14e4a-621f-11ed-9b6a-0242ac120002")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address:String
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calibration)
        m_address = intent.getStringExtra(BluetoothActivity.EXTRA_ADDRESS)!!

        ConnectToDevice( this).execute()
        val calibrate = findViewById<Button>(R.id.Calibrate)
        val next = findViewById<Button>(R.id.Next)
        val back = findViewById<Button>(R.id.Back)

        // Calibrate the microcontroller when it receives command a
        calibrate.setOnClickListener{ sendCommand("a") }
        // Start sending muscle values
        next.setOnClickListener{sendCommand("b")}
        // Disconnects from connection
        back.setOnClickListener{disconnect()}
    }

    private fun sendCommand(input:String) {
        if(m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private val mmInStream: InputStream = m_bluetoothSocket!!.inputStream
    private val mmBuffer: ByteArray = ByteArray(1024)

    // Receives data from the arduino
    private fun receiveData() {
        var numBytes: Int
        if(m_bluetoothSocket != null) {
            while(true) {
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch(e: IOException) {
                    Log.i("data", "couldnt connect", e)
                    break
                }
            }
        }
    }

    private fun disconnect() {
        if(m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {

        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "Please wait")
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if(m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch(e:IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess) {
                Log.i("data", "couldn't connect")

            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }

}