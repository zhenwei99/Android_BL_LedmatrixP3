package com.example.ledmatrix

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.ledmatrix.databinding.SelectDeviceLayoutBinding
import org.jetbrains.anko.toast

class SelectDeviceActivity : AppCompatActivity() {
    private lateinit var binding : SelectDeviceLayoutBinding
    private var m_bluetoothAdapter : BluetoothAdapter? = null
    private lateinit var m_pairedDevices : Set<BluetoothDevice>
    private val REQUEST_ENABLE_BLUETOOTH = 1
    companion object{
        val EXTRA_ADDRESS : String = "Device_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_device_layout)
        binding = DataBindingUtil.setContentView(this, R.layout.select_device_layout)
        Log.i("device","")
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null){
            toast("this device doesn't support bluetooth")
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        binding.selectDeviceRefresh.setOnClickListener {
            pairedDeviceList()
        }
    }
    private fun pairedDeviceList(){
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()
        if(!m_pairedDevices.isEmpty()){
            for(device : BluetoothDevice in m_pairedDevices){
                list.add(device)
                Log.i("device",""+device.name)
            }
        }else{
            toast("No paired bluetooth devices found")
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,list)
        binding.selectDeviceList.adapter = adapter
        binding.selectDeviceList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device : BluetoothDevice = list[position]
            val address : String = device.address
            val intent = Intent(this,ControlActivity::class.java)
            intent.putExtra(EXTRA_ADDRESS,address)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH){
            if(resultCode == Activity.RESULT_OK){
                if(m_bluetoothAdapter!!.isEnabled){
                    toast("BlueTooth has been enabled")
                }else{
                    toast("Bluetooth has been disabled")
                }
            }else if(resultCode == Activity.RESULT_CANCELED){
                toast("Bluetooth enabling has been cancled")
            }
        }
    }
}