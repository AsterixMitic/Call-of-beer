package com.example.callofbeer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log


class UsbBroadcastReceiver(private val onPermissionGranted: (UsbDevice) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == "com.example.USB_PERMISSION") {
            val device: UsbDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            }

            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                Log.d("USB", "Permission granted for ${device.deviceName}")
                onPermissionGranted(device)
            } else {
                Log.e("USB", "Permission denied or device is null")
            }
        }
    }
}
