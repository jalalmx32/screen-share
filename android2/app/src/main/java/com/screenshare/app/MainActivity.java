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
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private WebView webView;
    private Handler mainHandler;
    private long lastTapTime = 0;
    private long pressStartTime = 0;
    private boolean isLongPress = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Create WebView
        webView = new WebView(this);
        setContentView(webView);
        
        // Configure WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0xFF0D1117);
        
        // Enable touch events on WebView
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(event);
            }
        });
        
        // Load the HTML interface
        webView.loadDataWithBaseURL(
            null,
            getHTML(),
            "text/html",
            "UTF-8",
            null
        );
    }
    
    private boolean handleTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float viewWidth = webView.getWidth();
        float viewHeight = webView.getHeight();
        
        // Normalize coordinates to 0-1
        float normalizedX = x / viewWidth;
        float normalizedY = y / viewHeight;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressStartTime = System.currentTimeMillis();
                isLongPress = false;
                
                // Check for double tap
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTapTime < 300) {
                    // Double tap
                    sendTouchEvent("double_tap", normalizedX, normalizedY);
                    lastTapTime = 0;
                } else {
                    lastTapTime = currentTime;
                    // Start long press detection
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!isLongPress && pressStartTime > 0) {
                                isLongPress = true;
                                sendTouchEvent("long_press", normalizedX, normalizedY);
                            }
                        }
                    }, 500); // 500ms for long press
                }
                
                // Send touch start
                sendTouchEvent("touch_start", normalizedX, normalizedY);
                return true;
                
            case MotionEvent.ACTION_MOVE:
                // Send touch move
                sendTouchEvent("touch_move", normalizedX, normalizedY);
                return true;
                
            case MotionEvent.ACTION_UP:
                pressStartTime = 0;
                
                // Send touch end
                sendTouchEvent("touch_end", normalizedX, normalizedY);
                return true;
        }
        
        return false;
    }
    
    private void sendTouchEvent(String type, float x, float y) {
        // Call JavaScript to send touch event via WebSocket
        String js = "javascript:sendTouchToServer('" + type + "', " + x + ", " + y + ")";
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    webView.evaluateJavascript(
                        "sendTouchToServer('" + type + "', " + x + ", " + y + ")",
                        null
                    );
                } catch (Exception e) {
                    // Ignore
                }
            }
        });
    }
    
    private String getHTML() {
        return "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>\n" +
        "<style>\n" +
        "* { margin: 0; padding: 0; box-sizing: border-box; }\n" +
        "body { \n" +
        "  font-family: -apple-system, sans-serif;\n" +
        "  background: #0D1117;\n" +
        "  color: #e0e0e0;\n" +
        "  min-height: 100vh;\n" +
        "  display: flex;\n" +
        "  flex-direction: column;\n" +
        "  padding: 20px;\n" +
        "  touch-action: none;\n" +
        "  -webkit-user-select: none;\n" +
        "  user-select: none;\n" +
        "}\n" +
        ".header { text-align: center; margin-bottom: 30px; }\n" +
        "h1 { color: #00D4FF; font-size: 24px; margin-bottom: 5px; }\n" +
        ".subtitle { color: #888; font-size: 12px; }\n" +
        ".card {\n" +
        "  background: #161B22;\n" +
        "  border: 1px solid #1a508b;\n" +
        "  border-radius: 12px;\n" +
        "  padding: 16px;\n" +
        "  margin-bottom: 16px;\n" +
        "}\n" +
        ".card-title { color: #888; font-size: 12px; margin-bottom: 10px; }\n" +
        "input {\n" +
        "  width: 100%;\n" +
        "  padding: 14px;\n" +
        "  background: #0f3460;\n" +
        "  border: 1px solid #1a508b;\n" +
        "  border-radius: 8px;\n" +
        "  color: white;\n" +
        "  font-size: 16px;\n" +
        "  margin-bottom: 12px;\n" +
        "}\n" +
        "input:focus { outline: none; border-color: #00D4FF; }\n" +
        "button {\n" +
        "  width: 100%;\n" +
        "  padding: 14px;\n" +
        "  border: none;\n" +
        "  border-radius: 8px;\n" +
        "  font-size: 16px;\n" +
        "  font-weight: bold;\n" +
        "  cursor: pointer;\n" +
        "}\n" +
        "#connectBtn { background: #00D4FF; color: #0D1117; }\n" +
        "#connectBtn:active { background: #00b8d4; }\n" +
        "#disconnectBtn { background: #F87171; color: white; display: none; }\n" +
        "#status { text-align: center; font-size: 14px; margin: 15px 0; }\n" +
        ".online { color: #4ADE80; }\n" +
        ".offline { color: #F87171; }\n" +
        ".connecting { color: #FBBF24; }\n" +
        "#screen {\n" +
        "  flex: 1;\n" +
        "  background: black;\n" +
        "  border-radius: 8px;\n" +
        "  display: none;\n" +
        "  min-height: 300px;\n" +
        "  overflow: hidden;\n" +
        "  touch-action: none;\n" +
        "}\n" +
        "#screen.active { display: block; }\n" +
        "#screenImg {\n" +
        "  width: 100%;\n" +
        "  height: 100%;\n" +
        "  object-fit: contain;\n" +
        "  pointer-events: none;\n" +
        "}\n" +
        ".instructions { font-size: 13px; color: #888; line-height: 1.6; }\n" +
        ".instructions b { color: #00D4FF; }\n" +
        ".touch-hint {\n" +
        "  text-align: center;\n" +
        "  color: #4ADE80;\n" +
        "  font-size: 11px;\n" +
        "  margin-top: 10px;\n" +
        "  display: none;\n" +
        "}\n" +
        ".touch-hint.active { display: block; }\n" +
        "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div class='header'>\n" +
        "  <h1>📱 ScreenShare</h1>\n" +
        "  <div class='subtitle'>Wireless Display + Touch Control</div>\n" +
        "</div>\n" +
        "<div class='card' id='connectCard'>\n" +
        "  <div class='card-title'>📡 Connect to PC</div>\n" +
        "  <input type='text' id='ipInput' placeholder='192.168.43.1:8765'>\n" +
        "  <button id='connectBtn' onclick='connect()'>▶ Connect</button>\n" +
        "  <button id='disconnectBtn' onclick='disconnect()'>⏹ Disconnect</button>\n" +
        "</div>\n" +
        "<div id='status' class='offline'>● Offline</div>\n" +
        "<div id='screen' ontouchstart='handleTouchStart(event)' ontouchmove='handleTouchMove(event)' ontouchend='handleTouchEnd(event)'>\n" +
        "  <img id='screenImg'>\n" +
        "</div>\n" +
        "<div id='touchHint' class='touch-hint'>🖐️ Touch to control PC</div>\n" +
        "<div class='card' id='instructionsCard'>\n" +
        "  <div class='card-title'>📖 How to connect:</div>\n" +
        "  <div class='instructions'>\n" +
        "    <b>1.</b> Enable hotspot on your Android<br>\n" +
        "    <b>2.</b> Connect PC to the hotspot<br>\n" +
        "    <b>3.</b> Start ScreenShare on PC<br>\n" +
        "    <b>4.</b> Enter the IP shown on PC screen<br>\n" +
        "    <b>5.</b> Touch screen to control PC!\n" +
        "  </div>\n" +
        "</div>\n" +
        "<script>\n" +
        "var ws = null;\n" +
        "var lastTapTime = 0;\n" +
        "\n" +
        "function connect() {\n" +
        "  var ip = document.getElementById('ipInput').value.trim();\n" +
        "  if (!ip) { alert('Enter IP address'); return; }\n" +
        "  if (!ip.startsWith('ws://')) ip = 'ws://' + ip;\n" +
        "  \n" +
        "  setStatus('Connecting...', 'connecting');\n" +
        "  document.getElementById('connectBtn').style.display = 'none';\n" +
        "  \n" +
        "  try {\n" +
        "    ws = new WebSocket(ip);\n" +
        "    ws.binaryType = 'arraybuffer';\n" +
        "    \n" +
        "    ws.onopen = function() {\n" +
        "      setStatus('Connected - Touch enabled', 'online');\n" +
        "      document.getElementById('disconnectBtn').style.display = 'block';\n" +
        "      document.getElementById('screen').classList.add('active');\n" +
        "      document.getElementById('touchHint').classList.add('active');\n" +
        "      document.getElementById('connectCard').style.display = 'none';\n" +
        "      document.getElementById('instructionsCard').style.display = 'none';\n" +
        "    };\n" +
        "    \n" +
        "    ws.onmessage = function(e) {\n" +
        "      if (e.data instanceof ArrayBuffer) {\n" +
        "        var blob = new Blob([e.data], {type: 'image/jpeg'});\n" +
        "        var url = URL.createObjectURL(blob);\n" +
        "        var img = document.getElementById('screenImg');\n" +
        "        if (img.src) URL.revokeObjectURL(img.src);\n" +
        "        img.src = url;\n" +
        "      }\n" +
        "    };\n" +
        "    \n" +
        "    ws.onclose = function() {\n" +
        "      setStatus('Disconnected', 'offline');\n" +
        "      resetUI();\n" +
        "    };\n" +
        "    \n" +
        "    ws.onerror = function(e) {\n" +
        "      setStatus('Error', 'offline');\n" +
        "      resetUI();\n" +
        "    };\n" +
        "  } catch(e) {\n" +
        "    setStatus('Error: ' + e.message, 'offline');\n" +
        "    resetUI();\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "function disconnect() {\n" +
        "  if (ws) { ws.close(); ws = null; }\n" +
        "  resetUI();\n" +
        "  setStatus('Offline', 'offline');\n" +
        "}\n" +
        "\n" +
        "function setStatus(text, cls) {\n" +
        "  var s = document.getElementById('status');\n" +
        "  s.textContent = '● ' + text;\n" +
        "  s.className = cls;\n" +
        "}\n" +
        "\n" +
        "function resetUI() {\n" +
        "  document.getElementById('connectBtn').style.display = 'block';\n" +
        "  document.getElementById('disconnectBtn').style.display = 'none';\n" +
        "  document.getElementById('screen').classList.remove('active');\n" +
        "  document.getElementById('touchHint').classList.remove('active');\n" +
        "  document.getElementById('connectCard').style.display = 'block';\n" +
        "  document.getElementById('instructionsCard').style.display = 'block';\n" +
        "}\n" +
        "\n" +
        "// Touch handlers for screen element\n" +
        "function handleTouchStart(e) {\n" +
        "  e.preventDefault();\n" +
        "  var touch = e.touches[0];\n" +
        "  var screen = document.getElementById('screen');\n" +
        "  var rect = screen.getBoundingClientRect();\n" +
        "  var x = (touch.clientX - rect.left) / rect.width;\n" +
        "  var y = (touch.clientY - rect.top) / rect.height;\n" +
        "  \n" +
        "  // Check for double tap\n" +
        "  var now = Date.now();\n" +
        "  if (now - lastTapTime < 300) {\n" +
        "    sendTouchToServer('double_tap', x, y);\n" +
        "    lastTapTime = 0;\n" +
        "  } else {\n" +
        "    lastTapTime = now;\n" +
        "  }\n" +
        "  \n" +
        "  sendTouchToServer('touch_start', x, y);\n" +
        "}\n" +
        "\n" +
        "function handleTouchMove(e) {\n" +
        "  e.preventDefault();\n" +
        "  var touch = e.touches[0];\n" +
        "  var screen = document.getElementById('screen');\n" +
        "  var rect = screen.getBoundingClientRect();\n" +
        "  var x = (touch.clientX - rect.left) / rect.width;\n" +
        "  var y = (touch.clientY - rect.top) / rect.height;\n" +
        "  \n" +
        "  sendTouchToServer('touch_move', x, y);\n" +
        "}\n" +
        "\n" +
        "function handleTouchEnd(e) {\n" +
        "  e.preventDefault();\n" +
        "  var touch = e.changedTouches[0];\n" +
        "  var screen = document.getElementById('screen');\n" +
        "  var rect = screen.getBoundingClientRect();\n" +
        "  var x = (touch.clientX - rect.left) / rect.width;\n" +
        "  var y = (touch.clientY - rect.top) / rect.height;\n" +
        "  \n" +
        "  sendTouchToServer('touch_end', x, y);\n" +
        "}\n" +
        "\n" +
        "// Send touch event to server\n" +
        "function sendTouchToServer(type, x, y) {\n" +
        "  if (ws && ws.readyState === WebSocket.OPEN) {\n" +
        "    ws.send(JSON.stringify({\n" +
        "      type: type,\n" +
        "      x: Math.max(0, Math.min(1, x)),\n" +
        "      y: Math.max(0, Math.min(1, y))\n" +
        "    }));\n" +
        "  }\n" +
        "}\n" +
        "\n" +
        "// Expose function for Android native calls\n" +
        "window.sendTouchToServer = sendTouchToServer;\n" +
        "</script>\n" +
        "</body>\n" +
        "</html>";
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
