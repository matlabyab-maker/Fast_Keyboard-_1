package com.fastkeyboard;
import android.content.Intent;import android.service.quicksettings.Tile;import android.service.quicksettings.TileService;import android.provider.Settings;
public class KeyboardTileService extends TileService{
 @Override public void onClick(){super.onClick(); Intent i=new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivityAndCollapse(i);if(qsTile!=null){qsTile.state=Tile.STATE_ACTIVE;qsTile.updateTile();}}
}
