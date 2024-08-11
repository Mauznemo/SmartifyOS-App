package com.smartify_os.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

class CompanionService: CompanionDeviceService() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        Log.d("CompanionService", "onDeviceAppeared called")
        NotificationHelper.sendNotification(this@CompanionService, "system", "Device Appeared", "BLE device appeared.",
            2, R.drawable.ic_launcher_foreground)

        EventBus.post("DEVICE_APPEARED:${associationInfo.deviceMacAddress}")

        connectToDevice(associationInfo.associatedDevice?.bluetoothDevice)
    }

    override fun onDeviceDisappeared(associationInfo: AssociationInfo) {
        Log.d("CompanionService", "onDeviceDisappeared called")
        NotificationHelper.sendNotification(this@CompanionService, "system", "Device Disappeared", "BLE device disappeared.",
            2, R.drawable.ic_launcher_foreground)
    }

    private fun connectToDevice(device: BluetoothDevice?) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            Log.i("MyTag", "No BT connect perms")
            return
        }
        var bluetoothGatt = device?.connectGatt(this, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothAdapter.STATE_CONNECTED) {

                    if (ActivityCompat.checkSelfPermission(
                            this@CompanionService,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            this@CompanionService,
                            "No Bluetooth connect permission",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    NotificationHelper.sendNotification(
                        this@CompanionService, "system", "Connected", "Connected to HM-10",
                        3, R.drawable.ic_launcher_foreground
                    )

                    gatt.discoverServices()
                } else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                    NotificationHelper.sendNotification(
                        this@CompanionService, "system", "Disconnected", "Disconnected from HM-10",
                        3, R.drawable.ic_launcher_foreground
                    )
                    // Reconnect if necessary
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Handle the services discovered
                    NotificationHelper.sendNotification(
                        this@CompanionService, "system", "Ready", "HM-10 Ready to use",
                        3, R.drawable.ic_launcher_foreground
                    )

                    /*val serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
                    // Use the HM-10 Characteristic UUID
                    val characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

                    val service = gatt.getService(serviceUUID)
                    val characteristic = service?.getCharacteristic(characteristicUUID)

                    if (characteristic != null) {
                        // Write to the characteristic
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                            val handler = Handler(Looper.getMainLooper())
                            val delayMillis = 10000L // 1 second delay

                            val runnable = object : Runnable {
                                override fun run() {
                                    // Your repeated task here
                                    sendDataToCharacteristic(
                                        gatt,
                                        characteristic,
                                        "Your data to send\n"
                                    )

                                    // Repeat this runnable again after the delay
                                    handler.postDelayed(this, delayMillis)
                                }
                            }

                            // Start the loop
                            handler.post(runnable)

                        }
                    } else {
                        NotificationHelper.sendNotification(
                            this@MainActivity,
                            "system",
                            "Characteristic not found",
                            "HM-10 Characteristic not found",
                            4,
                            R.drawable.ic_launcher_foreground
                        )
                    }*/
                }
            }
        })
    }
}

