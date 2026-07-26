package com.screenshare.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private WebView webView;
    private Handler mainHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        mainHandler = new Handler(Looper.getMainLooper());
        webView = new WebView(this);
        setContentView(webView);
        
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0xFF0D1117);
        
        webView.loadDataWithBaseURL(null, getHTML(), "text/html", "UTF-8", null);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float vw = webView.getWidth();
        float vh = webView.getHeight();
        
        float nx = Math.max(0, Math.min(1, x / vw));
        float ny = Math.max(0, Math.min(1, y / vh));
        
        String type = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: type = "touch_start"; break;
            case MotionEvent.ACTION_MOVE: type = "touch_move"; break;
            case MotionEvent.ACTION_UP: type = "touch_end"; break;
        }
        
        if (!type.isEmpty()) {
            final String js = "sendTouchToServer('" + type + "'," + nx + "," + ny + ")";
            mainHandler.post(() -> {
                try { webView.evaluateJavascript(js, null); } catch (Exception e) {}
            });
        }
        
        return super.dispatchTouchEvent(event);
    }
    
    private String getHTML() {
        return "<!DOCTYPE html><html><head>" +
        "<meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=no'>" +
        "<style>" +
        "*{margin:0;padding:0;box-sizing:border-box;}" +
        "body{background:#0D1117;color:#e0e0e0;font-family:-apple-system,sans-serif;height:100vh;display:flex;flex-direction:column;overflow:hidden;}" +
        ".header{text-align:center;padding:10px;background:#161B22;border-bottom:1px solid #1a508b;}" +
        "h1{color:#00D4FF;font-size:18px;}" +
        ".sub{color:#888;font-size:10px;}" +
        ".author{color:#00D4FF;font-size:11px;margin-top:2px;}" +
        ".author a{color:#00D4FF;text-decoration:none;}" +
        ".card{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:12px;margin:8px;}" +
        "input{width:100%;padding:10px;background:#0f3460;border:1px solid #1a508b;border-radius:6px;color:white;font-size:14px;margin-bottom:8px;}" +
        "input:focus{outline:none;border-color:#00D4FF;}" +
        "button{width:100%;padding:10px;border:none;border-radius:6px;font-size:14px;font-weight:bold;}" +
        "#connectBtn{background:#00D4FF;color:#0D1117;}" +
        "#disconnectBtn{background:#F87171;color:white;margin-top:6px;}" +
        "#status{text-align:center;font-size:12px;padding:6px;}" +
        ".online{color:#4ADE80;} .offline{color:#F87171;} .connecting{color:#FBBF24;}" +
        "#screen{flex:1;background:#000;display:none;position:relative;overflow:hidden;}" +
        "#screen.active{display:flex;flex-direction:column;}" +
        "#screenImg{width:100%;flex:1;object-fit:contain;}" +
        ".touch-hint{text-align:center;color:#4ADE80;font-size:10px;padding:4px;}" +
        ".keyboard{background:#161B22;border-top:1px solid #1a508b;padding:6px;display:none;flex-wrap:wrap;gap:4px;justify-content:center;}" +
        ".keyboard.active{display:flex;}" +
        ".kbd{background:#0f3460;border:1px solid #1a508b;border-radius:4px;color:white;padding:6px 8px;font-size:11px;text-align:center;min-width:32px;}" +
        ".kbd:active{background:#00D4FF;color:#0D1117;}" +
        ".kbd.wide{min-width:50px;font-size:10px;}" +
        ".kbd.special{background:#1a508b;border-color:#00D4FF;}" +
        ".kbd.row2{min-width:28px;}" +
        ".info{font-size:10px;color:#888;line-height:1.4;padding:0 8px 8px 8px;}" +
        ".info b{color:#00D4FF;}" +
        "#kbToggle{background:#1a508b;color:white;width:auto;padding:4px 10px;font-size:10px;border-radius:4px;position:absolute;right:8px;top:50%;transform:translateY(-50%);z-index:10;}" +
        "</style></head><body>" +
        "<div class='header'>" +
        "  <h1>ScreenShare</h1>" +
        "  <div class='sub'>Wireless Display + Touch + Keyboard</div>" +
        "  <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "</div>" +
        "<div class='card' id='connectCard'>" +
        "  <input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>" +
        "  <button id='connectBtn' onclick='connect()'>Connect</button>" +
        "</div>" +
        "<div id='status' class='offline'>Offline</div>" +
        "<div id='screen'>" +
        "  <div style='position:relative;'>" +
        "    <button id='kbToggle' onclick='toggleKeyboard()'>Keyboard</button>" +
        "  </div>" +
        "  <img id='screenImg'>" +
        "  <div id='touchHint' class='touch-hint'>Touch screen to control PC</div>" +
        "  <div class='keyboard' id='keyboard'>" +
        "    <div class='kbd special' ontouchstart='sendKey(event,\"win\")'>Win</div>" +
        "    <div class='kbd special' ontouchstart='sendKey(event,\"alt\")'>Alt</div>" +
        "    <div class='kbd special' ontouchstart='sendKey(event,\"ctrl\")'>Ctrl</div>" +
        "    <div class='kbd special' ontouchstart='sendKey(event,\"shift\")'>Shift</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"esc\")'>Esc</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"tab\")'>Tab</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"capslock\")'>Caps</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"space\")'>Space</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"backspace\")'>BS</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"enter\")'>Enter</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"delete\")'>Del</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"insert\")'>Ins</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"home\")'>Home</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"end\")'>End</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"pageup\")'>PgUp</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"pagedown\")'>PgDn</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"up\")'>Up</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"down\")'>Down</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"left\")'>Left</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"right\")'>Right</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"f4\")'>F4</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"f5\")'>F5</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"f11\")'>F11</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"printscreen\")'>PrtSc</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"alt_tab\")'>Alt+Tab</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"alt_f4\")'>Alt+F4</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_c\")'>Ctrl+C</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_v\")'>Ctrl+V</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_z\")'>Ctrl+Z</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_a\")'>Ctrl+A</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_s\")'>Ctrl+S</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_x\")'>Ctrl+X</div>" +
        "  </div>" +
        "</div>" +
        "<div class='card' id='infoCard'>" +
        "  <div class='info'>" +
        "    <b>1.</b> Enable hotspot on Android<br>" +
        "    <b>2.</b> Connect PC to hotspot<br>" +
        "    <b>3.</b> Start ScreenShare on PC<br>" +
        "    <b>4.</b> Enter IP from PC screen<br>" +
        "    <b>5.</b> Touch to control PC!" +
        "  </div>" +
        "</div>" +
        "<script>" +
        "var ws=null;var kbVisible=false;" +
        "function connect(){" +
        "  var ip=document.getElementById('ipInput').value.trim();" +
        "  if(!ip){alert('Enter IP');return;}" +
        "  if(!ip.startsWith('ws://'))ip='ws://'+ip;" +
        "  setStatus('Connecting...','connecting');" +
        "  document.getElementById('connectBtn').style.display='none';" +
        "  try{" +
        "    ws=new WebSocket(ip);ws.binaryType='arraybuffer';" +
        "    ws.onopen=function(){" +
        "      setStatus('Connected','online');" +
        "      document.getElementById('connectCard').style.display='none';" +
        "      document.getElementById('infoCard').style.display='none';" +
        "      document.getElementById('screen').classList.add('active');" +
        "      document.getElementById('touchHint').style.display='block';" +
        "      document.getElementById('keyboard').classList.add('active');" +
        "      document.getElementById('kbToggle').style.display='block';" +
        "    };" +
        "    ws.onmessage=function(e){" +
        "      if(e.data instanceof ArrayBuffer){" +
        "        var blob=new Blob([e.data],{type:'image/jpeg'});" +
        "        var url=URL.createObjectURL(blob);" +
        "        var img=document.getElementById('screenImg');" +
        "        if(img.src)URL.revokeObjectURL(img.src);" +
        "        img.src=url;" +
        "      }" +
        "    };" +
        "    ws.onclose=function(){setStatus('Disconnected','offline');resetUI();};" +
        "    ws.onerror=function(){setStatus('Error','offline');resetUI();};" +
        "  }catch(e){setStatus('Error','offline');resetUI();}" +
        "}" +
        "function disconnect(){" +
        "  if(ws){ws.close();ws=null;}" +
        "  resetUI();setStatus('Offline','offline');" +
        "}" +
        "function resetUI(){" +
        "  document.getElementById('connectCard').style.display='block';" +
        "  document.getElementById('infoCard').style.display='block';" +
        "  document.getElementById('screen').classList.remove('active');" +
        "  document.getElementById('keyboard').classList.remove('active');" +
        "  document.getElementById('kbToggle').style.display='none';" +
        "  document.getElementById('connectBtn').style.display='block';" +
        "}" +
        "function setStatus(t,c){document.getElementById('status').textContent=t;document.getElementById('status').className=c;}" +
        "function sendTouchToServer(type,x,y){" +
        "  if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:type,x:x,y:y}));" +
        "}" +
        "function sendKey(e,key){" +
        "  e.preventDefault();e.stopPropagation();" +
        "  if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'key',key:key}));" +
        "}" +
        "function toggleKeyboard(){" +
        "  var kb=document.getElementById('keyboard');" +
        "  kbVisible=!kbVisible;" +
        "  kb.style.display=kbVisible?'flex':'none';" +
        "  document.getElementById('touchHint').style.display=kbVisible?'none':'block';" +
        "}" +
        "function toggleDisconnect(){" +
        "  if(confirm('Disconnect?'))disconnect();" +
        "}" +
        "</script></body></html>";
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
