package com.smartify_os.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class DoorsTileService : TileService() {

    private val eventListener: (String) -> Unit = { event ->
        if (event.startsWith("DEVICE_APPEARED:")) {
            val address = event.substringAfter(":")
            qsTile.label = "Loading..."
            qsTile.state = Tile.STATE_INACTIVE
            qsTile.subtitle = "Connecting ($address)"
            qsTile.updateTile()
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

    override fun onTileAdded() {
        super.onTileAdded()
        // Set the initial state of the tile
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.label = "Unknown"
        qsTile.subtitle = "Not connected"
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground) // Ensure this drawable exists
        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        qsTile.label = "State Unknown"
        qsTile.subtitle = "Not connected"
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        // Handle tile click
        Log.d("MyTileService", "Tile clicked!")

        // Example: Toggle the tile state
        qsTile.state = if (qsTile.state == Tile.STATE_ACTIVE) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        qsTile.updateTile()
    }

    companion object {
        private const val ACTION_UPDATE_TILE = "com.smartify_os.ACTION_UPDATE_TILE"
    }
}
