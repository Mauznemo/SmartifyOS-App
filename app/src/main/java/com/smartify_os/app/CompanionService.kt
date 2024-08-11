package com.smartify_os.app

import android.companion.AssociationInfo
import android.companion.CompanionDeviceService
import android.content.Context
import android.content.Intent
import android.util.Log

class CompanionService: CompanionDeviceService() {
    override fun onDeviceAppeared(associationInfo: AssociationInfo) {
        Log.d("CompanionService", "onDeviceAppeared called")
        NotificationHelper.sendNotification(this@CompanionService, "system", "Device Appeared", "BLE device appeared.",
            2, R.drawable.ic_launcher_foreground)

        EventBus.post("DEVICE_APPEARED:${associationInfo.deviceMacAddress}")
    }

    override fun onDeviceDisappeared(associationInfo: AssociationInfo) {
        Log.d("CompanionService", "onDeviceDisappeared called")
        NotificationHelper.sendNotification(this@CompanionService, "system", "Device Disappeared", "BLE device disappeared.",
            2, R.drawable.ic_launcher_foreground)
    }
}