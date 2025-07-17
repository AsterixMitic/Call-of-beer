package com.example.callofbeer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import android.widget.Toast

class UsbBroadcastReceiver(
    private val onPermissionGranted: (UsbDevice) -> Unit,
    private val onDeviceAttached: (UsbDevice) -> Unit,
    private val onDeviceDetached: () -> Unit,
    private val usbManager: UsbManager
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "com.example.USB_PERMISSION" -> {
                // Minimum API is TIRAMISU -> 33
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

            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                @Suppress("DEPRECATION")
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.let {
                    // Notify user about new device
                    Toast.makeText(context, "USB Device Attached", Toast.LENGTH_SHORT).show()
                    Log.d("USB", "Device attached: ${it.deviceName}")
                    // Check if device has permission
                    if(usbManager.hasPermission(device)){
                        onPermissionGranted(device)
                    } else {
                        onDeviceAttached(it)
                    }
                }
            }

            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                //Notify user about device removal
                Toast.makeText(context, "USB Device Removed", Toast.LENGTH_SHORT).show()
                Log.d("USB", "Device detached")
                onDeviceDetached()
            }

        }
    }
}
