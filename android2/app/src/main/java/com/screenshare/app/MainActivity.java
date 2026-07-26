package com.screenshare.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private WebView webView;
    private Handler mainHandler;
    private float lastX = 0;
    private float lastY = 0;
    
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
        settings.setAllowFileAccess(true);
        
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0xFF0D1117);
        
        webView.loadDataWithBaseURL(null, getHTML(), "text/html", "UTF-8", null);
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float viewWidth = webView.getWidth();
        float viewHeight = webView.getHeight();
        
        float normalizedX = Math.max(0, Math.min(1, x / viewWidth));
        float normalizedY = Math.max(0, Math.min(1, y / viewHeight));
        
        String type = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                type = "touch_start";
                break;
            case MotionEvent.ACTION_MOVE:
                type = "touch_move";
                break;
            case MotionEvent.ACTION_UP:
                type = "touch_end";
                break;
        }
        
        if (!type.isEmpty()) {
            final String js = "sendTouchToServer('" + type + "', " + normalizedX + ", " + normalizedY + ")";
            mainHandler.post(() -> {
                try {
                    webView.evaluateJavascript(js, null);
                } catch (Exception e) {}
            });
        }
        
        return super.dispatchTouchEvent(event);
    }
    
    private String getHTML() {
        return "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>\n" +
        "<style>\n" +
        "* { margin:0; padding:0; box-sizing:border-box; }\n" +
        "body { background:#0D1117; color:#e0e0e0; font-family:-apple-system,sans-serif; min-height:100vh; display:flex; flex-direction:column; padding:16px; }\n" +
        ".header { text-align:center; margin-bottom:20px; }\n" +
        "h1 { color:#00D4FF; font-size:22px; }\n" +
        ".sub { color:#888; font-size:11px; }\n" +
        ".author { color:#00D4FF; font-size:12px; margin-top:4px; }\n" +
        ".card { background:#161B22; border:1px solid #1a508b; border-radius:10px; padding:14px; margin-bottom:12px; }\n" +
        "input { width:100%; padding:12px; background:#0f3460; border:1px solid #1a508b; border-radius:8px; color:white; font-size:15px; margin-bottom:10px; }\n" +
        "input:focus { outline:none; border-color:#00D4FF; }\n" +
        "button { width:100%; padding:12px; border:none; border-radius:8px; font-size:15px; font-weight:bold; }\n" +
        "#connectBtn { background:#00D4FF; color:#0D1117; }\n" +
        "#disconnectBtn { background:#F87171; color:white; display:none; }\n" +
        "#status { text-align:center; font-size:13px; margin:12px 0; }\n" +
        ".online { color:#4ADE80; } .offline { color:#F87171; } .connecting { color:#FBBF24; }\n" +
        "#screen { flex:1; background:black; border-radius:8px; display:none; min-height:250px; }\n" +
        "#screen.active { display:flex; align-items:center; justify-content:center; }\n" +
        "#screenImg { width:100%; height:100%; object-fit:contain; }\n" +
        ".touch-hint { text-align:center; color:#4ADE80; font-size:11px; display:none; }\n" +
        ".touch-hint.active { display:block; }\n" +
        ".info { font-size:11px; color:#888; line-height:1.5; }\n" +
        ".info b { color:#00D4FF; }\n" +
        "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div class='header'>\n" +
        "  <h1>ScreenShare</h1>\n" +
        "  <div class='sub'>Wireless Display + Touch Control</div>\n" +
        "  <div class='author'>by Jalal | <a href='https://t.me/x16_96' style='color:#00D4FF;text-decoration:none;'>@x16_96</a></div>\n" +
        "</div>\n" +
        "<div class='card' id='connectCard'>\n" +
        "  <input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>\n" +
        "  <button id='connectBtn' onclick='connect()'>Connect</button>\n" +
        "  <button id='disconnectBtn' onclick='disconnect()'>Disconnect</button>\n" +
        "</div>\n" +
        "<div id='status' class='offline'>Offline</div>\n" +
        "<div id='screen'><img id='screenImg'></div>\n" +
        "<div id='touchHint' class='touch-hint'>Touch screen to control PC</div>\n" +
        "<div class='card' id='infoCard'>\n" +
        "  <div class='info'>\n" +
        "    <b>1.</b> Enable hotspot on Android<br>\n" +
        "    <b>2.</b> Connect PC to hotspot<br>\n" +
        "    <b>3.</b> Start ScreenShare on PC<br>\n" +
        "    <b>4.</b> Enter IP from PC screen<br>\n" +
        "    <b>5.</b> Touch to control PC!\n" +
        "  </div>\n" +
        "</div>\n" +
        "<script>\n" +
        "var ws = null;\n" +
        "function connect() {\n" +
        "  var ip = document.getElementById('ipInput').value.trim();\n" +
        "  if (!ip) { alert('Enter IP'); return; }\n" +
        "  if (!ip.startsWith('ws://')) ip = 'ws://' + ip;\n" +
        "  setStatus('Connecting...', 'connecting');\n" +
        "  document.getElementById('connectBtn').style.display = 'none';\n" +
        "  try {\n" +
        "    ws = new WebSocket(ip);\n" +
        "    ws.binaryType = 'arraybuffer';\n" +
        "    ws.onopen = function() {\n" +
        "      setStatus('Connected - Touch enabled', 'online');\n" +
        "      document.getElementById('disconnectBtn').style.display = 'block';\n" +
        "      document.getElementById('screen').classList.add('active');\n" +
        "      document.getElementById('touchHint').classList.add('active');\n" +
        "      document.getElementById('connectCard').style.display = 'none';\n" +
        "      document.getElementById('infoCard').style.display = 'none';\n" +
        "    };\n" +
        "    ws.onmessage = function(e) {\n" +
        "      if (e.data instanceof ArrayBuffer) {\n" +
        "        var blob = new Blob([e.data], {type:'image/jpeg'});\n" +
        "        var url = URL.createObjectURL(blob);\n" +
        "        var img = document.getElementById('screenImg');\n" +
        "        if (img.src) URL.revokeObjectURL(img.src);\n" +
        "        img.src = url;\n" +
        "      }\n" +
        "    };\n" +
        "    ws.onclose = function() { setStatus('Disconnected', 'offline'); resetUI(); };\n" +
        "    ws.onerror = function() { setStatus('Error', 'offline'); resetUI(); };\n" +
        "  } catch(e) { setStatus('Error', 'offline'); resetUI(); }\n" +
        "}\n" +
        "function disconnect() { if(ws){ws.close();ws=null;} resetUI(); setStatus('Offline', 'offline'); }\n" +
        "function setStatus(t,c) { var s=document.getElementById('status'); s.textContent=t; s.className=c; }\n" +
        "function resetUI() {\n" +
        "  document.getElementById('connectBtn').style.display='block';\n" +
        "  document.getElementById('disconnectBtn').style.display='none';\n" +
        "  document.getElementById('screen').classList.remove('active');\n" +
        "  document.getElementById('touchHint').classList.remove('active');\n" +
        "  document.getElementById('connectCard').style.display='block';\n" +
        "  document.getElementById('infoCard').style.display='block';\n" +
        "}\n" +
        "function sendTouchToServer(type, x, y) {\n" +
        "  if (ws && ws.readyState === WebSocket.OPEN) {\n" +
        "    ws.send(JSON.stringify({type:type, x:x, y:y}));\n" +
        "  }\n" +
        "}\n" +
        "</script>\n" +
        "</body>\n" +
        "</html>";
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
