package com.smartify_os.app

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class TrunkTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()
        // Set the initial state of the tile
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.label = "Open Trunk"
        qsTile.subtitle = "Not connected"
        qsTile.icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground) // Ensure this drawable exists
        qsTile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()

        qsTile.subtitle = "Not connected"
        qsTile.updateTile()
        // Update the tile state if necessary
    }

    override fun onStopListening() {
        super.onStopListening()
        // Clean up if needed
    }

    override fun onClick() {
        super.onClick()
        // Handle tile click
        Log.d("MyTileService", "Tile clicked!")

        // Example: Toggle the tile state
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}
