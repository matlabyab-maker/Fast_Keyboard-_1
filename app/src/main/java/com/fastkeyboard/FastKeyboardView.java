package com.fastkeyboard;

import android.content.Context;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.inputmethod.InputConnection;
import android.widget.*;
import android.app.AlertDialog;
import java.util.*;

public class FastKeyboardView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final InputConnection ic;
    private final int blue=Color.rgb(18,86,205), cream=Color.rgb(255,253,245), edge=Color.rgb(220,218,210);
    private boolean drawer=false, hideTop=false;
    private float transparency=1f;
    private final ArrayList<String> history=new ArrayList<>();
    private RectF[][] rects;
    private String[][] labels;

    public FastKeyboardView(Context c, InputConnection connection){ super(c); ic=connection; setLayerType(View.LAYER_TYPE_SOFTWARE,null); }

    private void setup(){
        labels=new String[][]{
            {"Copy All","Copy","Paste","Esc","Undo","Redo","Copy Screen","Settings","12:30","Hide Top Rows"},
            {"~\n`","!\n1","@\n2","#\n3","$\n4","%\n5","^\n6","&\n7","*\n8","(\n9",")\n0","-\n_","=\n+","⌫"},
            {"Q","W","E","R","T","Y","U","I","O","P","{\n[","}\n]"},
            {"A","S","D","F","G","H","J","K","L",";\n:","\"\n'","Enter"},
            {"⇧","Z","X","C","V","B","N","M","<\n,",">\n.","?\n/","⇧"},
            {"!@#\n$%^&*","🌐","Emoji","Space","⚙","←","→","↓","↑"}
        };
    }
    @Override protected void onDraw(Canvas c){
        super.onDraw(c); setup(); c.drawColor(cream); float w=getWidth(), h=getHeight();
        int topRows=hideTop?0:2; float toolbar=h*.155f; float row2=h*.135f; float letter=(h-toolbar-row2)/4f;
        float y=0;
        if(!hideTop){ drawRow(c, labels[0], y, toolbar, 10, true); y+=toolbar; drawRow(c,labels[1],y,row2,14,false); y+=row2; }
        drawRow(c,labels[2],y,letter,12,false); y+=letter;
        drawRow(c,labels[3],y,letter,12,false); y+=letter;
        drawRow(c,labels[4],y,letter,12,false); y+=letter;
        drawBottom(c,y,h-y);
    }
    private void drawRow(Canvas c,String[] a,float y,float rh,int n,boolean toolbar){
        float cw=getWidth()/(float)n; for(int i=0;i<a.length;i++){
            float l=i*cw, r=(i+1)*cw-1; RectF q=new RectF(l+1,y+1,r,y+rh-1); p.setStyle(Paint.Style.FILL); p.setColor(cream); p.setShadowLayer(2,0,1,0x33000000); c.drawRoundRect(q,8,8,p); p.clearShadowLayer();
            drawText(c,a[i],(l+r)/2,y+rh/2, toolbar?15:20);
        }
    }
    private void drawBottom(Canvas c,float y,float rh){
        float w=getWidth(); float[] widths={.095f,.095f,.095f,.38f,.06f,.07f,.07f,.07f,.07f}; String[] a=labels[5]; float x=0;
        for(int i=0;i<a.length;i++){ float cw=widths[i]*w; RectF q=new RectF(x+1,y+1,x+cw-1,y+rh-1); p.setColor(cream);p.setStyle(Paint.Style.FILL);p.setShadowLayer(2,0,1,0x33000000);c.drawRoundRect(q,8,8,p);p.clearShadowLayer();drawText(c,a[i],x+cw/2,y+rh/2,(i==3)?22:19);x+=cw; }
    }
    private void drawText(Canvas c,String s,float x,float y,float size){ p.setColor(blue);p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));p.setTextSize(size*getResources().getDisplayMetrics().scaledDensity);p.setTextAlign(Paint.Align.CENTER);p.setAlpha((int)(255*transparency)); String[] ls=s.split("\\n"); float off=(ls.length-1)*p.getTextSize()*.32f; for(String line:ls){c.drawText(line,x,y-off,p);off-=p.getTextSize()*.64f;} p.setAlpha(255); }

    @Override public boolean onTouchEvent(MotionEvent e){ if(e.getAction()!=MotionEvent.ACTION_UP)return true; float x=e.getX(),y=e.getY(),h=getHeight(),w=getWidth();
        if(!hideTop && y<h*.155f){ int i=(int)(x/(w/10f)); if(i==0){copyAll();} else if(i==1){copy();} else if(i==2){paste();} else if(i==4){undo();} else if(i==5){redo();} else if(i==9){hideTop=!hideTop;invalidate();} else if(i==7){openDrawer();} return true; }
        float toolbar=hideTop?0:h*.155f,row2=hideTop?0:h*.135f, rowH=(h-toolbar-row2)/4f;
        if(!hideTop && y>=toolbar && y<toolbar+row2){ int i=(int)(x/(w/14f)); if(i>=1&&i<=12){ String[][] map={{"!","1"},{"@","2"},{"#","3"},{"$","4"},{"%","5"},{"^","6"},{"&","7"},{"*","8"},{"(","9"},{")","0"},{"-","_"},{"=","+"}}; int k=i-1; if(k<map.length) type(map[k][0]); } else if(i==13) backspace(); return true; }
        int row=(int)((y-toolbar-row2)/rowH); float pos=x/w;
        if(row==0){int i=(int)(pos*12); String s="QWERTYUIOP"; if(i<10) type(String.valueOf(s.charAt(i))); else type(i==10?"{":"}");}
        else if(row==1){int i=(int)(pos*12); if(i<9) type(String.valueOf("ASDFGHJKL".charAt(i))); else if(i==11) enter(); else type(i==9?";":"\"");}
        else if(row==2){int i=(int)(pos*12); if(i==0||i==11) type("SHIFT"); else if(i>=1&&i<=7) type(String.valueOf("ZXCVBNM".charAt(i-1))); else type(i==8?"<":i==9?">":"?");}
        else { handleBottom(x,w); }
        return true;
    }
    private void handleBottom(float x,float w){ float[] widths={.095f,.095f,.095f,.38f,.06f,.07f,.07f,.07f,.07f}; float s=0; for(int i=0;i<widths.length;i++){float n=s+widths[i]*w;if(x>=s&&x<n){ if(i==0){} else if(i==1){} else if(i==2){} else if(i==3)type(" "); else if(i==4)openDrawer(); else type(new String[]{"←","→","↓","↑"}[i-5]); return;} s=n;}}
    private void type(String s){ if(ic==null)return; if("SHIFT".equals(s))return; ic.commitText(s,1); }
    private void enter(){if(ic!=null)ic.sendKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_ENTER));}
    private void backspace(){if(ic!=null)ic.deleteSurroundingText(1,0);}
    private void copy(){ if(ic!=null){CharSequence s=ic.getSelectedText(0);if(s!=null){saveHistory(s.toString());}} }
    private void copyAll(){ if(ic!=null){ic.performContextMenuAction(android.R.id.selectAll); CharSequence s=ic.getSelectedText(0);if(s!=null)saveHistory(s.toString());}}
    private void paste(){ if(ic!=null){ClipboardManager cm=(ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);if(cm.hasPrimaryClip())ic.commitText(cm.getPrimaryClip().getItemAt(0).coerceToText(getContext()),1);}}
    private void undo(){if(ic!=null)ic.performContextMenuAction(android.R.id.undo);}
    private void redo(){if(ic!=null)ic.performContextMenuAction(android.R.id.redo);}
    private void saveHistory(String s){ if(s.length()==0)return; history.remove(s);history.add(0,s);while(history.size()>100)history.remove(100); ClipboardManager cm=(ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("Fast Keyboard",s)); }
    private void openDrawer(){ LinearLayout box=new LinearLayout(getContext());box.setOrientation(LinearLayout.VERTICAL);box.setPadding(18,12,18,12); box.setBackgroundColor(cream);
        TextView title=new TextView(getContext());title.setText("گزینه‌های کشویی");title.setTextSize(19);title.setTextColor(blue);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(title);
        Button tr=new Button(getContext());tr.setText("Transparency Roller");box.addView(tr); tr.setOnClickListener(v->showTransparency());
        Button hist=new Button(getContext());hist.setText("Copy History — 100 آخر");box.addView(hist);hist.setOnClickListener(v->showHistory());
        Button emo=new Button(getContext());emo.setText("Make Emoji");box.addView(emo);emo.setOnClickListener(v->showEmoji());
        new AlertDialog.Builder(getContext()).setView(box).setNegativeButton("بستن",null).show(); }
    private void showTransparency(){ final SeekBar sb=new SeekBar(getContext());sb.setMax(99);sb.setProgress((int)(transparency*99)); new AlertDialog.Builder(getContext()).setTitle("Transparency 1–100%").setView(sb).setPositiveButton("OK",(d,w)->{transparency=(sb.getProgress()+1)/100f;invalidate();}).show(); }
    private void showHistory(){String[] a=history.toArray(new String[0]); if(a.length==0)a=new String[]{"(خالی)"}; new AlertDialog.Builder(getContext()).setTitle("Copy History — 100 آخر").setItems(a,(d,which)->{if(which<history.size()&&ic!=null)ic.commitText(history.get(which),1);}).show();}
    private void showEmoji(){ new AlertDialog.Builder(getContext()).setTitle("Make Emoji").setMessage("در این نسخه پنجره آماده است؛ انتخاب تصویر/عکس در نسخه بعدی تکمیل می‌شود.").setPositiveButton("بستن",null).show(); }
}
