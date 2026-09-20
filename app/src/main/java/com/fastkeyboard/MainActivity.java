package com.fastkeyboard;
import android.app.*;import android.content.*;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.view.*;import android.widget.*;
public class MainActivity extends Activity{
 @Override public void onCreate(Bundle b){super.onCreate(b); LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(28,28,28,28); TextView t=new TextView(this);t.setText("Fast Keyboard\nReset 13");t.setTextSize(24);box.addView(t); Button ime=new Button(this);ime.setText("فعال‌سازی کیبورد");ime.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));box.addView(ime); Button ov=new Button(this);ov.setText("اجازه نمایش روی برنامه‌ها");ov.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))));box.addView(ov);setContentView(box);}
}
