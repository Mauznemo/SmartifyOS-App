package com.smartify_os.app

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.companion.DeviceFilter
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.MacAddress
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.smartify_os.app.ui.theme.SmartifyOSAppTheme
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import java.util.UUID
import java.util.concurrent.Executor
import java.util.regex.Pattern

//TODO: Scan only while in app, save mac, try to connect to saved mac every sec or use some
//  system event
class MainActivity : ComponentActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var sharedPreferences: SharedPreferences
    //private lateinit var deviceManager: CompanionDeviceManager

    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }

    private val executor: Executor =  Executor { it.run() }

    private var bluetoothGatt: BluetoothGatt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartifyOSAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Initialize Bluetooth
        //val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        //bluetoothAdapter = bluetoothManager.adapter

        Log.i("MyTag", "Requesting permissions")
        // Request necessary permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            ),
            1001
        )
        Log.i("MyTag", "Staring Scan")
        // Start scanning for BLE devices
        //startScanning()
        /*
        try {

            val serviceIntent = Intent(this, BLEService::class.java)
            this.startForegroundService(serviceIntent)
            Log.i("MyTag", "this.startForegroundService(serviceIntent)")
        }
        catch (e: Exception)
        {
            Log.e("MyTag", e.message.toString())
        }*/
        if (!isDeviceAssociated()) {
            associateBle()
        } else {
            // Device is already associated, handle accordingly
        }

    }

    private fun associateBle()
    {
        NotificationHelper.createNotificationChannel(this@MainActivity, "system", "System Notifications",
            "System Notifications", NotificationManager.IMPORTANCE_LOW)
        NotificationHelper.sendNotification(this@MainActivity, "system", "Started association", "Started scanning for BLE devices...",
            1, R.drawable.ic_launcher_foreground)

        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            // Match only Bluetooth devices whose name matches the pattern.
            .setNamePattern(Pattern.compile("DSD TECH"))
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            // Find only devices that match this request filter.
            .addDeviceFilter(deviceFilter)
            // Stop scanning as soon as one device matching the filter is found.
            .setSingleDevice(true)
            .build()

        deviceManager.associate(pairingRequest,
            executor,
            object : CompanionDeviceManager.Callback() {
                // Called when a device is found. Launch the IntentSender so the user
                // can select the device they want to pair with.
                override fun onAssociationPending(intentSender: IntentSender) {
                    NotificationHelper.sendNotification(this@MainActivity, "system", "Association Pending", "Association Pending...",
                        1, R.drawable.ic_launcher_foreground)

                    startIntentSenderForResult(intentSender, 0, null, 0, 0, 0)
                }

                override fun onAssociationCreated(associationInfo: AssociationInfo) {
                    // An association is created.
                    val associationId: Int = associationInfo.id
                    val macAddress: MacAddress? = associationInfo.deviceMacAddress
                    saveAssociationInfo(macAddress)
                    NotificationHelper.sendNotification(this@MainActivity, "system", "Successfully associated ($associationId)", "Successfully associated with $macAddress",
                        1, R.drawable.ic_launcher_foreground)
                }

                override fun onFailure(errorMessage: CharSequence?) {
                    // To handle the failure.
                    NotificationHelper.sendNotification(this@MainActivity, "system", "Association failed", "Failed ($errorMessage)",
                        1, R.drawable.ic_launcher_foreground)
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> when(resultCode) {
                Activity.RESULT_OK -> {
                    // The user chose to pair the app with a Bluetooth device.
                    val deviceToPair: BluetoothDevice? =
                        data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                    deviceToPair?.let { device ->
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        //connectToDevice(device)
                        deviceManager.startObservingDevicePresence(device.address);
                        // Maintain continuous interaction with a paired device.
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun isDeviceAssociated(): Boolean {
        return sharedPreferences.getBoolean("device_associated", false)
    }

    private fun saveAssociationInfo(macAddress: MacAddress?) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("device_associated", true)
        editor.putString("device_mac_address", macAddress.toString())
        editor.apply()
    }



    private fun startScanning() {
        val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device: BluetoothDevice = result.device

                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    Log.i("MyTag", "No BT connect perms")
                    return
                }

                if (device.name != null && device.name == "HM-10") {
                    // Device found, connect to it
                    //connectToDevice(device)
                }
            }
        }
        bluetoothLeScanner.startScan(leScanCallback)
    }




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartifyOSAppTheme {
        Greeting("Android")
    }
}
}
