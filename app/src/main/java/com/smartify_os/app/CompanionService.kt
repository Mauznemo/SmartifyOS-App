package com.smartify_os.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.util.UUID

class CompanionService: CompanionDeviceService() {

    private lateinit var writeCharacteristic: BluetoothGattCharacteristic
    private lateinit var gatt: BluetoothGatt
    companion object {
        var connected: Boolean = false
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        Log.d("CompanionService", "onDeviceAppeared called")
        NotificationHelper.sendNotification(this@CompanionService, "system", "Device Appeared", "BLE device appeared.",
            2, R.drawable.ic_launcher_foreground)

        if(connected){
            return
        }
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

                    EventBus.post("DEVICE_CONNECTED:${device.address}")

                    connected = true

                    gatt.discoverServices()
                } else if (newState == BluetoothAdapter.STATE_DISCONNECTED) {
                    NotificationHelper.sendNotification(
                        this@CompanionService, "system", "Disconnected", "Disconnected from HM-10",
                        3, R.drawable.ic_launcher_foreground
                    )
                    connected = false
                    // Reconnect if necessary
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    this@CompanionService.gatt = gatt
                    // Handle the services discovered
                    NotificationHelper.sendNotification(
                        this@CompanionService, "system", "Ready", "HM-10 Ready to use",
                        3, R.drawable.ic_launcher_foreground
                    )

                    val serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
                    val characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
                    val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

                    val service = gatt.getService(serviceUUID)
                    writeCharacteristic = service?.getCharacteristic(characteristicUUID)!!


                    if (ActivityCompat.checkSelfPermission(
                            this@CompanionService,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }

                    gatt.setCharacteristicNotification(writeCharacteristic, true)

                    val descriptor = writeCharacteristic.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)
                    descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    // Enable notifications in the descriptor
                    gatt.writeDescriptor(descriptor)



                    //sendString("Hello World!\n")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val receivedData = characteristic.value
                val message = String(receivedData)
                // Process the received message
                Log.d("BLE", "Received message: $message")
            }
        })
    }

    fun sendString(message: String) {
        if (::writeCharacteristic.isInitialized) {
            Log.d("BLE", "Sent message: $message")
            val messageBytes = message.toByteArray()
            writeCharacteristic.value = messageBytes
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val success = gatt.writeCharacteristic(writeCharacteristic)

            if (success) {
                NotificationHelper.sendNotification(this@CompanionService, "system", "Data sent successfully", "HM-10 Data sent successfully",
                    4, R.drawable.ic_launcher_foreground)
            } else {
                NotificationHelper.sendNotification(this@CompanionService, "system", "Failed to send data",
                    "HM-10 Failed to send data ($success)",
                    4, R.drawable.ic_launcher_foreground)
            }
        }
    }

    private val eventListener: (String) -> Unit = { event ->
        if (event.startsWith("SEND_MESSAGE:")) {
            val message = event.substringAfter(":")
            if(connected)
            {
                sendString(message)
            }
        }

    }

    override fun onCreate() {
        super.onCreate()
        EventBus.subscribe(eventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.unsubscribe(eventListener)
    }

    /*
    private fun sendDataToCharacteristic(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, data: String) {
        // Convert your data to bytes
        val dataBytes = data.toByteArray()

        // Set the value for the characteristic
        //characteristic.value = dataBytes

        // Write the characteristic
        if (ActivityCompat.checkSelfPermission(
                this@CompanionService,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val success = gatt.writeCharacteristic(characteristic, dataBytes, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
        if (success == BluetoothStatusCodes.SUCCESS) {
            NotificationHelper.sendNotification(this@CompanionService, "system", "Data sent successfully", "HM-10 Data sent successfully",
                4, R.drawable.ic_launcher_foreground)
        } else {
            NotificationHelper.sendNotification(this@CompanionService, "system", "Failed to send data",
                "HM-10 Failed to send data ($success)",
                4, R.drawable.ic_launcher_foreground)
        }
    }*/
}

