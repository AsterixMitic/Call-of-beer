package com.example.callofbeer

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var usbManager: UsbManager
    private var usbSerial: UsbSerialDevice? = null
    private var usbDevice: UsbDevice? = null
    private lateinit var usbReceiver: UsbBroadcastReceiver
    private var value : Double? = -1.0
    private val valueToShow = mutableStateOf(value)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usbManager = getSystemService(USB_SERVICE) as UsbManager


        usbReceiver = UsbBroadcastReceiver { device -> connectToDevice(device) }
        registerReceiver(usbReceiver, IntentFilter("com.example.USB_PERMISSION"))

        setContent {
            USBSerialApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent("com.example.USB_PERMISSION"), PendingIntent.FLAG_MUTABLE)
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun connectToDevice(device: UsbDevice) {
        val connection = usbManager.openDevice(device)
        if (connection == null) {
            Log.e("USB", "Failed to open USB connection")
            return
        }

        usbSerial = UsbSerialDevice.createUsbSerialDevice(device, connection)
        if (usbSerial != null && usbSerial!!.open()) {
            usbSerial!!.setBaudRate(57600)
            usbSerial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
            usbSerial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
            usbSerial!!.setParity(UsbSerialInterface.PARITY_NONE)

            usbSerial!!.read { bytes ->
                val receivedData = String(bytes)
                Log.d("USB", "Received: $receivedData")
                value = receivedData.toDoubleOrNull()
                valueToShow.value = value
            }
        } else {
            Log.e("USB", "Failed to open USB serial")
        }
    }

    @Composable
    fun USBSerialApp() {
        val devices = usbManager.deviceList.values.toList()
        var connectedDevice by remember { mutableStateOf<UsbDevice?>(null) }
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Available USB Devices:", color = Color.Black)

            devices.forEach { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            scope.launch(Dispatchers.IO) {
                                requestUsbPermission(device)
                                connectedDevice = device
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Device: ${device.deviceName}", color = Color.White)
                        Text("Vendor ID: ${device.vendorId}", color = Color.White)
                        Text("Product ID: ${device.productId}", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Connected Device: ${connectedDevice?.deviceName ?: "None"}", color = Color.Black)
            Text("${valueToShow.value ?: "Setting up please wait..."}",
                color = Color.Black,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}