package com.example.callofbeer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface

class MainActivity : ComponentActivity() {
    private lateinit var usbManager: UsbManager
    private var usbSerial: UsbSerialDevice? = null
    private var usbDevice: UsbDevice? = null
    private val connectedUsbDevice = mutableStateOf<UsbDevice?>(null)
    private lateinit var usbReceiver: UsbBroadcastReceiver
    private val valueToShow = mutableStateOf<Double?>(null)
    private val baudRate = 57600

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //TODO: Za splash screen -> pivo se polako prazni i ostaje logo!
        installSplashScreen()

        usbManager = getSystemService(USB_SERVICE) as UsbManager

        val filter = IntentFilter().apply {
            addAction("com.example.USB_PERMISSION")
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }

        // Setting up the UsbBroadcastReceiver
        usbReceiver = UsbBroadcastReceiver(
            onPermissionGranted = { device -> connectToDevice(device) },
            onDeviceAttached = { device -> requestUsbPermission(device) },
            onDeviceDetached = {
                usbSerial = null
                usbDevice = null
                connectedUsbDevice.value = null
                valueToShow.value = null
            },
            usbManager = usbManager
        )

        registerReceiver(usbReceiver, filter)

        // Checking the initial case where the Arduino device
        // is already connected...
        usbManager.deviceList.values.firstOrNull()?.let { device ->
            if (usbManager.hasPermission(device)) {
                // Device is already connected and permission is granted
                connectToDevice(device)
            } else {
                // Device is connected but doesnt have permission
                requestUsbPermission(device)
            }
        }

        setContent {
            USBSerialApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbReceiver)
    }

    // Function for permission request
    private fun requestUsbPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent("com.example.USB_PERMISSION"),
            PendingIntent.FLAG_MUTABLE
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    // Function to connect to device & read it's data
    private fun connectToDevice(device: UsbDevice) {
        val connection = usbManager.openDevice(device)
        if (connection == null) {
            Log.e("USB", "Failed to open USB connection")
            return
        }

        // Arduino specific parameters
        usbSerial = UsbSerialDevice.createUsbSerialDevice(device, connection)
        //TODO: Github & AOR resursi za objasnjenje...
        if (usbSerial != null && usbSerial!!.open()) {
            usbSerial!!.setBaudRate(baudRate)
            usbSerial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
            usbSerial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
            usbSerial!!.setParity(UsbSerialInterface.PARITY_NONE)

            usbSerial!!.read { bytes ->
                val receivedData = String(bytes)
                Log.d("USB", "Received: $receivedData")
                valueToShow.value = receivedData.toDoubleOrNull()
            }
        } else {
            Log.e("USB", "Failed to open USB serial")
        }
        usbDevice = device
        connectedUsbDevice.value = device
    }


    @Composable
    fun USBSerialApp() {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val connectedDevice = connectedUsbDevice.value
            if (connectedDevice == null) {
                Text("Waiting for device...", color = Color.Black)
            } else {
                Text("Connected to: ${connectedDevice.deviceName}", color = Color.Black)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    valueToShow.value?.let { "Weight: ${"%.2f".format(it)} g" } ?: "Connecting & Initializing...",
                    color = Color.Black,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}