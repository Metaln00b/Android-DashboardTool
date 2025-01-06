package com.example.dashboardtool

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.*

private const val ACTION = "com.my.app.USB_PERMISSION"

@SuppressLint("MissingPermission")
class BluetoothAndUsbManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?,
    private val usbManager: UsbManager
) {
    private var bluetoothSocket: BluetoothSocket? = null
    private var usbSerialPort: UsbSerialPort? = null
    private var outputStream: OutputStream? = null
    private var isConnected = false

    // Funktion zum Verbinden mit einem Bluetooth-Gerät
    suspend fun connectToBluetoothDevice(device: BluetoothDevice) {
        if (isConnected) return
        withContext(Dispatchers.IO) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"))
                bluetoothSocket?.connect()
                outputStream = bluetoothSocket?.outputStream
                isConnected = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Funktion zum Verbinden mit einem USB-Gerät (CH340)
    suspend fun connectToUsbDevice(device: UsbDevice) {
        if (isConnected) return
        withContext(Dispatchers.IO) {
            if (!usbManager.hasPermission(device))
            {
                usbManager.requestPermission(device,
                    PendingIntent.getBroadcast(context, 0, Intent(ACTION),
                        PendingIntent.FLAG_IMMUTABLE))
            }
            val driver = UsbSerialProber.getDefaultProber().probeDevice(device)

            if (driver != null && usbManager.hasPermission(device)) {
                val connection = usbManager.openDevice(device)
                usbSerialPort = driver.ports[0] // Zugriff auf den ersten Port des Geräts

                usbSerialPort?.apply {
                    open(connection)
                    setParameters(
                        115200, // Baudrate
                        8,      // Datenbits
                        UsbSerialPort.STOPBITS_1,
                        UsbSerialPort.PARITY_NONE
                    )
                }
                isConnected = true
            }
        }
    }

    // Funktion zum Senden von Daten an das verbundene Gerät
    suspend fun sendData(data: String) {
        withContext(Dispatchers.IO) {
            if (!isConnected) return@withContext
            try {
                if (outputStream != null) {
                    // Sende über Bluetooth
                    outputStream?.write(data.toByteArray())
                } else if (usbSerialPort != null) {
                    // Sende über USB
                    usbSerialPort?.write(data.toByteArray(), 1000) // Timeout von 1000 ms
                } else {

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Schließen der Verbindung und Freigabe von Ressourcen
    fun closeConnection() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
            usbSerialPort?.close()
            isConnected = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

// Utility-Funktion, um Daten an das ausgewählte Gerät zu senden
@SuppressLint("MissingPermission")
suspend fun sendDataToDevice(
    context: Context,
    connectionType: String,
    bluetoothAdapter: BluetoothAdapter?,
    usbManager: UsbManager,
    rpm: Int,
    speed: Int,
    coolantTemp: Int,
    fuelLevel: Int,
    oilTemp: Int,
    gear: Int
) {
    val manager = BluetoothAndUsbManager(context, bluetoothAdapter, usbManager)
    val data = "{$rpm&$speed&$fuelLevel&$coolantTemp&0&0&0&$oilTemp}"

    if (connectionType == "Bluetooth") {
        val pairedDevice: BluetoothDevice? = bluetoothAdapter?.bondedDevices?.firstOrNull { it.name.startsWith("BT-DASH") }
        pairedDevice?.let {
            manager.connectToBluetoothDevice(it)
            manager.sendData(data)
        }
    } else if (connectionType == "USB") {
        val usbDevice: UsbDevice? = usbManager.deviceList.values.firstOrNull { it.vendorId == 6790 }
        usbDevice?.let {
            manager.connectToUsbDevice(it)
            manager.sendData(data)
        }
    }

    manager.closeConnection()
}