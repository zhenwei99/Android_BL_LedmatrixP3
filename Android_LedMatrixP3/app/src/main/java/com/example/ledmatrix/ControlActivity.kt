package com.example.ledmatrix

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.ledmatrix.databinding.ControlLayoutBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
class ControlActivity : AppCompatActivity() {
    private lateinit var binding: ControlLayoutBinding

    companion object {

        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var outputStream: OutputStream
        lateinit var inputStream: InputStream
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        binding = DataBindingUtil.setContentView(this, R.layout.control_layout)
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS).toString()
        ConnectToDevice(this).execute()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.mask_group)
        val scaled = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        //  val stream = ByteArrayOutputStream()
        //  bitmap.compress(CompressFormat.PNG, 50, stream)
        // val imageBytes = stream.toByteArray()
        val arr = convertToRGB24(scaled)
        Log.e("Tag------->", arr.toString())
        binding.controlLedOn.setOnClickListener {
            sendCommand(arr) }
        // binding.controlLedOff.setOnClickListener { sendCommand("Happy Birthday") }
        binding.controlLedDisconnect.setOnClickListener { disconnect() }

    }
    private fun convertToRGB24(img: Bitmap): ByteArray {
        val width = img.width
        val height = img.height
        val BufferRGB24 = ByteArray(width * height * 3)
        var val1: Int
        var R: Int
        var G: Int
        var B: Int
        for (i in 0 until height) {
            for (j in 0 until width) {
                val1 = img.getPixel(i, j)
                R = (val1 shr 16) and 0xff
                G = (val1 shr 8) and 0xff
                B = val1 and 0xff
                BufferRGB24[i * width + j] = R .toByte()
                BufferRGB24[(i * width)+64*64 + j] = G .toByte()
                BufferRGB24[(i * width) + ((64 * 64)*2) + j] = B .toByte()
            }
        }
        return BufferRGB24
    }
    private fun sendCommand(input: ByteArray){
        if(m_bluetoothSocket!=null){
            try {
                m_bluetoothSocket!!.outputStream.write(input)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }


    private fun disconnect(){
        if(m_bluetoothSocket != null){
           try{
               m_bluetoothSocket!!.close()
               m_bluetoothSocket = null
               m_isConnected = false
           }catch (e: IOException){
               e.printStackTrace()
           }
        }
        finish()
    }
    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>(){
        private var connectSuccess:Boolean = true
        private val context: Context
        init {
            this.context = c
        }
        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "Please wait")
        }

        override fun doInBackground(vararg params: Void?): String {
            try {
                if(m_bluetoothSocket == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            }catch (e: IOException){
                connectSuccess =false
                e.printStackTrace()
            }
            return null.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            }else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
}