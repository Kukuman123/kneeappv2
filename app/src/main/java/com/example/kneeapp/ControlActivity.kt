package com.example.kneeapp

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.control_layout.*
import java.io.IOException
import java.util.*


class ControlActivity: AppCompatActivity(){

    companion object {
        var m_myUUID: UUID = UUID.fromString("86a71cfa-47db-11eb-b378-0242ac130002")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        val liveData: MutableLiveData<String> by lazy {
            MutableLiveData<String>()
        }

    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS)
        val angleTxt = findViewById<TextView>(R.id.angle)
        val accelTxt = findViewById<TextView>(R.id.acceleration)
        val bothTxt = findViewById<TextView>(R.id.both)

        ConnectToDevice(this).execute()
        control_led_on.setOnClickListener {
            sendCommand("a")
            //accelTxt.text = ""
            //bothTxt.text = ""

        }
        control_led_off.setOnClickListener { sendCommand("b")}
        control_led_disconnect.setOnClickListener { disconnect()}
    }

    private fun sendCommand(input: String){
        if(m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            }
            catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
    private fun readCommand(bluetoothSocket: BluetoothSocket) {
        Log.i("data", Thread.currentThread().name)
        val bluetoothSocketInputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int
        //Loop to listen for received bluetooth messages
        while (true) {
            try {
                bytes = bluetoothSocketInputStream.read(buffer)
                val readMessage = String(buffer, 0, bytes)
                liveData.postValue(readMessage)
            } catch (e: IOException) {
                e.printStackTrace()
                break
            }
        }
    }

    // display or don't star image
    private fun View.showOrHideImage(imageShow: Boolean) {
        visibility = if (imageShow) View.VISIBLE else View.GONE
    }

    private fun disconnect(){
        if(m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            }
            catch (e: IOException){
                e.printStackTrace()
            }

        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")

        }

        override fun doInBackground(vararg p0: Void?): String {
            try{
                if(m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            }
            catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data","couldn't connect")
            }
            else{
                m_isConnected = true
            }
            m_progress.dismiss()

        }




    }





}