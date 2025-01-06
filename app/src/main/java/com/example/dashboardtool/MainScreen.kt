package com.example.dashboardtool

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import android.app.Service

@Composable
fun MainScreen(context: Context, bluetoothAdapter: BluetoothAdapter?, usbManager: UsbManager) {
    var connectionType by remember { mutableStateOf("Bluetooth") } // Auswahl für Verbindungstyp
    var rpm by remember { mutableStateOf(0) }
    var speed by remember { mutableStateOf(0) }
    var coolantTemp by remember { mutableStateOf(90) }
    var fuelLevel by remember { mutableStateOf(50) }
    var oilTemp by remember { mutableStateOf(70) }
    var gear by remember { mutableStateOf(1) }
    var isServiceRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "Verbindungstyp: $connectionType", fontSize = 16.sp)

        Row {
            Button(onClick = { connectionType = "Bluetooth" }, enabled = !isServiceRunning) {
                Text("Bluetooth wählen")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { connectionType = "USB" }, enabled = !isServiceRunning) {
                Text("USB wählen")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Motordrehzahl: $rpm", fontSize = 20.sp)
        Button(onClick = { rpm += 200 }) { Text("Erhöhe RPM") }
        Button(onClick = { rpm -= 200 }) { Text("Reduziere RPM") }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Geschwindigkeit: $speed", fontSize = 20.sp)
        Button(onClick = { speed += 10 }) { Text("Erhöhe Geschwindigkeit") }
        Button(onClick = { speed -= 10 }) { Text("Reduziere Geschwindigkeit") }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Kühlmitteltemperatur: $coolantTemp", fontSize = 20.sp)
        Button(onClick = { coolantTemp += 10 }) { Text("Erhöhe Temperatur") }
        Button(onClick = { coolantTemp -= 10 }) { Text("Reduziere Temperatur") }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Tank: $fuelLevel", fontSize = 20.sp)
        Button(onClick = { fuelLevel += 10 }) { Text("Erhöhe Tankinhalt") }
        Button(onClick = { fuelLevel -= 10 }) { Text("Reduziere Tankinhalt") }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Öltemperatur: $oilTemp", fontSize = 20.sp)
        Button(onClick = { oilTemp += 10 }) { Text("Erhöhe Öltemperatur") }
        Button(onClick = { oilTemp -= 10 }) { Text("Reduziere Öltemperatur") }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    Intent(context, RunningService::class.java).also {
                        it.action = RunningService.Actions.START.toString()
                        context.startService(it)
                    }
                    /*coroutineScope.launch {
                        sendDataToDevice(context, connectionType, bluetoothAdapter, usbManager, rpm, speed, coolantTemp, fuelLevel, oilTemp, gear)
                    }*/
                },
                enabled = !isServiceRunning
            ) { Text("Daten senden") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    Intent(context, RunningService::class.java).also {
                        it.action = RunningService.Actions.STOP.toString()
                        context.startService(it)
                    }
                    /*coroutineScope.launch {
                        sendDataToDevice(context, connectionType, bluetoothAdapter, usbManager, rpm, speed, coolantTemp, fuelLevel, oilTemp, gear)
                    }*/
                },
                enabled = isServiceRunning
            ) { Text("Daten nicht senden") }
        }
    }
}