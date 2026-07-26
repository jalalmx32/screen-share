package com.screenshare.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private WebView webView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
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
        
        // Load the HTML interface
        webView.loadDataWithBaseURL(
            null,
            getHTML(),
            "text/html",
            "UTF-8",
            null
        );
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
        "}\n" +
        "#screen.active { display: block; }\n" +
        ".instructions { font-size: 13px; color: #888; line-height: 1.6; }\n" +
        ".instructions b { color: #00D4FF; }\n" +
        "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div class='header'>\n" +
        "  <h1>📱 ScreenShare</h1>\n" +
        "  <div class='subtitle'>Wireless Display for Android</div>\n" +
        "</div>\n" +
        "<div class='card' id='connectCard'>\n" +
        "  <div class='card-title'>📡 Connect to PC</div>\n" +
        "  <input type='text' id='ipInput' placeholder='192.168.43.1:8765'>\n" +
        "  <button id='connectBtn' onclick='connect()'>▶ Connect</button>\n" +
        "  <button id='disconnectBtn' onclick='disconnect()'>⏹ Disconnect</button>\n" +
        "</div>\n" +
        "<div id='status' class='offline'>● Offline</div>\n" +
        "<div id='screen'><img id='screenImg' style='width:100%;height:100%;object-fit:contain;'></div>\n" +
        "<div class='card' id='instructionsCard'>\n" +
        "  <div class='card-title'>📖 How to connect:</div>\n" +
        "  <div class='instructions'>\n" +
        "    <b>1.</b> Enable hotspot on your Android<br>\n" +
        "    <b>2.</b> Connect PC to the hotspot<br>\n" +
        "    <b>3.</b> Start ScreenShare on PC<br>\n" +
        "    <b>4.</b> Enter the IP shown on PC screen\n" +
        "  </div>\n" +
        "</div>\n" +
        "<script>\n" +
        "var ws = null;\n" +
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
        "      setStatus('Connected', 'online');\n" +
        "      document.getElementById('disconnectBtn').style.display = 'block';\n" +
        "      document.getElementById('screen').classList.add('active');\n" +
        "      document.getElementById('connectCard').style.display = 'none';\n" +
        "      document.getElementById('instructionsCard').style.display = 'none';\n" +
        "    };\n" +
        "    \n" +
        "    ws.onmessage = function(e) {\n" +
        "      if (e.data instanceof ArrayBuffer) {\n" +
        "        var blob = new Blob([e.data], {type: 'image/jpeg'});\n" +
        "        var url = URL.createObjectURL(blob);\n" +
        "        document.getElementById('screenImg').src = url;\n" +
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
        "function disconnect() {\n" +
        "  if (ws) { ws.close(); ws = null; }\n" +
        "  resetUI();\n" +
        "  setStatus('Offline', 'offline');\n" +
        "}\n" +
        "function setStatus(text, cls) {\n" +
        "  var s = document.getElementById('status');\n" +
        "  s.textContent = '● ' + text;\n" +
        "  s.className = cls;\n" +
        "}\n" +
        "function resetUI() {\n" +
        "  document.getElementById('connectBtn').style.display = 'block';\n" +
        "  document.getElementById('disconnectBtn').style.display = 'none';\n" +
        "  document.getElementById('screen').classList.remove('active');\n" +
        "  document.getElementById('connectCard').style.display = 'block';\n" +
        "  document.getElementById('instructionsCard').style.display = 'block';\n" +
        "}\n" +
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
