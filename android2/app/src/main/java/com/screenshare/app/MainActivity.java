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

        /* Top bar with hamburger */
        ".topbar{display:flex;align-items:center;justify-content:space-between;padding:10px 12px;background:#161B22;border-bottom:1px solid #1a508b;}" +
        ".hamburger{background:none;border:none;color:#00D4FF;font-size:22px;padding:4px 8px;cursor:pointer;}" +
        ".topbar-title{text-align:center;flex:1;}" +
        ".topbar-title h1{color:#00D4FF;font-size:18px;}" +
        ".topbar-title .sub{color:#888;font-size:10px;}" +
        ".topbar-title .author{color:#00D4FF;font-size:11px;}" +
        ".topbar-title .author a{color:#00D4FF;text-decoration:none;}" +

        /* Sidebar overlay */
        ".sidebar-overlay{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:998;display:none;}" +
        ".sidebar-overlay.active{display:block;}" +

        /* Sidebar menu */
        ".sidebar{position:fixed;top:0;left:-280px;width:280px;height:100%;background:#161B22;z-index:999;display:flex;flex-direction:column;transition:left 0.3s;box-shadow:4px 0 16px rgba(0,0,0,0.5);}" +
        ".sidebar.open{left:0;}" +
        ".sidebar-header{padding:20px 16px;border-bottom:1px solid #1a508b;}" +
        ".sidebar-header h2{color:#00D4FF;font-size:18px;}" +
        ".sidebar-header .author{color:#888;font-size:11px;margin-top:4px;}" +
        ".sidebar-header .author a{color:#00D4FF;text-decoration:none;}" +
        ".sidebar-item{display:flex;align-items:center;padding:14px 16px;border:none;background:none;color:#e0e0e0;font-size:14px;text-align:left;width:100%;border-bottom:1px solid #1a508b33;}" +
        ".sidebar-item:active{background:#0f3460;}" +
        ".sidebar-item .icon{width:24px;text-align:center;margin-right:12px;font-size:16px;}" +
        ".sidebar-footer{margin-top:auto;padding:12px 16px;border-top:1px solid #1a508b;color:#888;font-size:10px;text-align:center;}" +

        /* Main content */
        ".card{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:12px;margin:8px;}" +
        "input{width:100%;padding:10px;background:#0f3460;border:1px solid #1a508b;border-radius:6px;color:white;font-size:14px;margin-bottom:8px;}" +
        "input:focus{outline:none;border-color:#00D4FF;}" +
        "#status{text-align:center;font-size:12px;padding:6px;}" +
        ".online{color:#4ADE80;} .offline{color:#F87171;} .connecting{color:#FBBF24;}" +

        "#toggleBtn{width:100%;padding:14px;border:none;border-radius:8px;font-size:16px;font-weight:bold;}" +
        ".btn-connect{background:#00D4FF;color:#0D1117;}" +
        ".btn-disconnect{background:#F87171;color:white;}" +
        ".btn-connecting{background:#FBBF24;color:#0D1117;}" +

        "#screen{flex:1;background:#000;display:none;position:relative;overflow:hidden;}" +
        "#screen.active{display:flex;flex-direction:column;}" +
        "#screenImg{width:100%;flex:1;object-fit:contain;}" +
        ".touch-hint{text-align:center;color:#4ADE80;font-size:10px;padding:4px;}" +

        ".keyboard{background:#161B22;border-top:1px solid #1a508b;padding:6px;display:none;flex-wrap:wrap;gap:4px;justify-content:center;}" +
        ".keyboard.active{display:flex;}" +
        ".kbd{background:#0f3460;border:1px solid #1a508b;border-radius:4px;color:white;padding:6px 8px;font-size:11px;text-align:center;min-width:32px;}" +
        ".kbd:active{background:#00D4FF;color:#0D1117;}" +
        ".kbd.special{background:#1a508b;border-color:#00D4FF;}" +
        "#kbToggle{background:#1a508b;color:white;width:auto;padding:4px 10px;font-size:10px;border-radius:4px;position:absolute;right:8px;top:50%;transform:translateY(-50%);z-index:10;}" +

        ".info{font-size:10px;color:#888;line-height:1.4;padding:0 8px 8px 8px;}" +
        ".info b{color:#00D4FF;}" +

        ".page{position:fixed;top:0;left:0;width:100%;height:100%;background:#0D1117;z-index:2000;display:none;flex-direction:column;overflow-y:auto;}" +
        ".page.active{display:flex;}" +
        ".page-header{background:#161B22;padding:14px;border-bottom:1px solid #1a508b;display:flex;align-items:center;justify-content:space-between;}" +
        ".page-header h3{color:#00D4FF;font-size:16px;}" +
        ".page-back{background:none;border:none;color:#00D4FF;font-size:14px;}" +
        ".page-content{padding:16px;}" +
        ".page-content h4{color:#00D4FF;margin:16px 0 8px 0;}" +
        ".page-content p{color:#888;font-size:12px;line-height:1.6;}" +
        ".step{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:12px;margin:8px;}" +
        ".step-num{color:#00D4FF;font-size:20px;font-weight:bold;}" +
        ".step-title{color:white;font-size:13px;font-weight:bold;margin:4px 0;}" +
        ".step-desc{color:#888;font-size:11px;line-height:1.4;}" +
        ".step-key{display:inline-block;background:#0f3460;border:1px solid #1a508b;border-radius:4px;padding:2px 6px;color:#00D4FF;font-size:10px;margin:2px;}" +
        "</style></head><body>" +

        /* Top bar */
        "<div class='topbar'>" +
        "  <button class='hamburger' onclick='openSidebar()'>\u2630</button>" +
        "  <div class='topbar-title'>" +
        "    <h1>ScreenShare</h1>" +
        "    <div class='sub'>Wireless Display + Touch + Keyboard</div>" +
        "  </div>" +
        "  <div style='width:40px;'></div>" +
        "</div>" +

        /* Sidebar overlay */
        "<div class='sidebar-overlay' id='sidebarOverlay' onclick='closeSidebar()'></div>" +

        /* Sidebar menu */
        "<div class='sidebar' id='sidebar'>" +
        "  <div class='sidebar-header'>" +
        "    <h2>ScreenShare</h2>" +
        "    <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "  </div>" +
        "  <button class='sidebar-item' onclick='closeSidebar()'><span class='icon'>\u2302</span>Home</button>" +
        "  <button class='sidebar-item' onclick='showTutorial()'><span class='icon'>\u24D8</span>Tutorial</button>" +
        "  <button class='sidebar-item' onclick='showAbout()'><span class='icon'>\u2139</span>About</button>" +
        "  <button class='sidebar-item' onclick='closeSidebar()'><span class='icon'>\u2699</span>Settings</button>" +
        "  <div class='sidebar-footer'>ScreenShare v1.8.0<br>github.com/jalalmx32/screen-share</div>" +
        "</div>" +

        /* Connection card */
        "<div class='card' id='connectCard'>" +
        "  <input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>" +
        "  <button id='toggleBtn' class='btn-connect' onclick='toggleConnection()'>Connect</button>" +
        "</div>" +
        "<div id='status' class='offline'>Offline</div>" +

        /* Screen area */
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
        "    <div class='kbd' ontouchstart='sendKey(event,\"space\")'>Space</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"backspace\")'>BS</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"enter\")'>Enter</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"delete\")'>Del</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"up\")'>Up</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"down\")'>Down</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"left\")'>Left</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"right\")'>Right</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"alt_tab\")'>Alt+Tab</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"alt_f4\")'>Alt+F4</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_c\")'>Ctrl+C</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_v\")'>Ctrl+V</div>" +
        "    <div class='kbd' ontouchstart='sendKey(event,\"ctrl_z\")'>Ctrl+Z</div>" +
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

        /* About page */
        "<div class='page' id='aboutPage'>" +
        "  <div class='page-header'><h3>About</h3><button class='page-back' onclick='hideAbout()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>ScreenShare</h4>" +
        "    <p>Wireless Display + Touch Control for Android</p>" +
        "    <p>A free alternative to Spacedesk</p>" +
        "    <h4>Features</h4>" +
        "    <p>- Real-time screen streaming</p>" +
        "    <p>- Touch control (tap, drag, long press)</p>" +
        "    <p>- Virtual keyboard with special keys</p>" +
        "    <h4>Developer</h4>" +
        "    <p>Jalal | @x16_96</p>" +
        "    <p>GitHub: github.com/jalalmx32/screen-share</p>" +
        "    <h4>Version</h4>" +
        "    <p>v1.8.0</p>" +
        "  </div>" +
        "</div>" +

        /* Tutorial page */
        "<div class='page' id='tutorialPage'>" +
        "  <div class='page-header'><h3>Tutorial</h3><button class='page-back' onclick='hideTutorial()'>Back</button></div>" +
        "  <div class='step'><div class='step-num'>1</div><div class='step-title'>Enable Hotspot</div><div class='step-desc'>Turn on WiFi hotspot on your Android phone</div></div>" +
        "  <div class='step'><div class='step-num'>2</div><div class='step-title'>Connect PC</div><div class='step-desc'>Connect your Windows PC to the hotspot network</div></div>" +
        "  <div class='step'><div class='step-num'>3</div><div class='step-title'>Start Server</div><div class='step-desc'>Run ScreenShare on Windows and note the IP address</div></div>" +
        "  <div class='step'><div class='step-num'>4</div><div class='step-title'>Connect</div><div class='step-desc'>Enter the IP in this app and tap Connect</div></div>" +
        "  <div class='step'><div class='step-num'>5</div><div class='step-title'>Touch Control</div><div class='step-desc'>Tap the screen to click, drag to move mouse</div></div>" +
        "  <div class='step'><div class='step-num'>6</div><div class='step-title'>Virtual Keyboard</div><div class='step-desc'>Use special keys: <span class='step-key'>Win</span> <span class='step-key'>Alt</span> <span class='step-key'>Ctrl</span> <span class='step-key'>Alt+Tab</span> <span class='step-key'>Ctrl+C</span></div></div>" +
        "</div>" +

        "<script>" +
        "var ws=null;var isConnected=false;" +

        /* Sidebar */
        "function openSidebar(){" +
        "  document.getElementById('sidebar').classList.add('open');" +
        "  document.getElementById('sidebarOverlay').classList.add('active');" +
        "}" +
        "function closeSidebar(){" +
        "  document.getElementById('sidebar').classList.remove('open');" +
        "  document.getElementById('sidebarOverlay').classList.remove('active');" +
        "}" +

        /* Pages */
        "function showAbout(){closeSidebar();document.getElementById('aboutPage').classList.add('active');}" +
        "function hideAbout(){document.getElementById('aboutPage').classList.remove('active');}" +
        "function showTutorial(){closeSidebar();document.getElementById('tutorialPage').classList.add('active');}" +
        "function hideTutorial(){document.getElementById('tutorialPage').classList.remove('active');}" +

        /* Connection */
        "function toggleConnection(){if(isConnected)disconnect();else connect();}" +
        "function connect(){" +
        "  var ip=document.getElementById('ipInput').value.trim();" +
        "  if(!ip){alert('Enter IP');return;}" +
        "  if(!ip.startsWith('ws://'))ip='ws://'+ip;" +
        "  setStatus('Connecting...','connecting');setBtn('Connecting...','btn-connecting');" +
        "  try{" +
        "    ws=new WebSocket(ip);ws.binaryType='arraybuffer';" +
        "    ws.onopen=function(){" +
        "      isConnected=true;" +
        "      setStatus('Connected','online');setBtn('Disconnect','btn-disconnect');" +
        "      document.getElementById('connectCard').style.display='none';" +
        "      document.getElementById('infoCard').style.display='none';" +
        "      var sc=document.getElementById('screen');sc.classList.add('active');sc.style.display='flex';sc.style.flexDirection='column';sc.style.flex='1';" +
        "    };" +
        "    ws.onmessage=function(e){" +
        "      if(e.data instanceof ArrayBuffer){" +
        "        var blob=new Blob([e.data],{type:'image/jpeg'});" +
        "        var url=URL.createObjectURL(blob);" +
        "        var img=document.getElementById('screenImg');" +
        "        if(img.src)URL.revokeObjectURL(img.src);img.src=url;" +
        "      }" +
        "    };" +
        "    ws.onclose=function(){backToHome();};" +
        "    ws.onerror=function(){setStatus('Error','offline');backToHome();};" +
        "  }catch(e){setStatus('Error','offline');backToHome();}" +
        "}" +
        "function disconnect(){if(ws){ws.close();ws=null;}backToHome();}" +
        "function backToHome(){" +
        "  isConnected=false;setStatus('Offline','offline');setBtn('Connect','btn-connect');" +
        "  document.getElementById('connectCard').style.display='block';" +
        "  document.getElementById('infoCard').style.display='block';" +
        "  document.getElementById('screen').style.display='none';document.getElementById('screen').classList.remove('active');" +
        "  document.getElementById('keyboard').classList.remove('active');" +
        "}" +
        "function setBtn(t,c){var b=document.getElementById('toggleBtn');b.textContent=t;b.className=c;}" +
        "function setStatus(t,c){document.getElementById('status').textContent=t;document.getElementById('status').className=c;}" +

        /* Touch & Keyboard */
        "function sendTouchToServer(type,x,y){if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:type,x:x,y:y}));}" +
        "function sendKey(e,key){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'key',key:key}));}" +
        "function toggleKeyboard(){var kb=document.getElementById('keyboard');if(kb.classList.contains('active')){kb.classList.remove('active');document.getElementById('touchHint').style.display='block';}else{kb.classList.add('active');document.getElementById('touchHint').style.display='none';}}" +
        "</script></body></html>";
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
