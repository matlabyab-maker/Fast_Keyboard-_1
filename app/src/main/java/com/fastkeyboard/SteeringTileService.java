package com.fastkeyboard;
import android.content.Intent;import android.service.quicksettings.Tile;import android.service.quicksettings.TileService;
public class SteeringTileService extends TileService{public void onClick(){super.onClick();Intent i=new Intent(this,SteeringOverlayService.class);if(android.os.Build.VERSION.SDK_INT>=26)startForegroundService(i);if(qsTile!=null){qsTile.state=Tile.STATE_ACTIVE;qsTile.updateTile();}}}
