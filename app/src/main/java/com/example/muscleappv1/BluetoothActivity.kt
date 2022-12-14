package com.example.muscleappv1

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class BluetoothActivity : AppCompatActivity() {

    var m_bluetoothAdapter: BluetoothAdapter? = null
    lateinit var m_pairedDevices:Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluetooth)


        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null) {
            toast("This device does not support bluetooth")
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }
        var mListView = findViewById<ListView>(R.id.select_device_list)
        mListView.setOnClickListener{ pairedDeviceList()}
    }

    private fun pairedDeviceList() {
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()

        if(!m_pairedDevices.isEmpty()) {
            for(device: BluetoothDevice in m_pairedDevices) {
                list.add(device)
                Log.i("device", "" + device)
            }
        }
        else {
            toast("no paired Bluetooth devices found")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        var mListView = findViewById<ListView>(R.id.select_device_list)
        mListView.adapter = adapter
        mListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address
            val intent = Intent(this, ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS, address)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK) {
                if(m_bluetoothAdapter!!.isEnabled) {
                    toast("Bluetooth has been enabled")
                }
                else {
                    toast("Bluetooth has been disabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                toast("Bluetooth enabling has been cancelled")
            }
        }
    }

}