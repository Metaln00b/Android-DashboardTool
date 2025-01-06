package com.example.dashboardtool

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.example.dashboardtool.ui.theme.DashboardToolTheme

class MainActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var usbManager: UsbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE),
            0
        )
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        requestBluetoothPermission()

        setContent {
            DashboardToolTheme {
                MainScreen(applicationContext, bluetoothAdapter, usbManager)
            }
        }
    }

    private fun requestBluetoothPermission() {
        val requestBluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Bluetooth permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    /*private fun requestUsbPermission() {
        val requestUsbPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "USB permission is required.", Toast.LENGTH_SHORT).show()
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.DEVICE) != PackageManager.PERMISSION_GRANTED) {
            requestUsbPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }*/
}