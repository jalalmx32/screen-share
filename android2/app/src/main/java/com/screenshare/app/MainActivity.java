package com.screenshare.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;
    private Handler mainHandler;
    private boolean atHome = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        mainHandler = new Handler(Looper.getMainLooper());
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0xFF0D1117);

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void setPage(String page) {
                runOnUiThread(() -> { atHome = "home".equals(page); reapplyFullscreen(); });
            }
            @android.webkit.JavascriptInterface
            public void log(String m) { android.util.Log.d("SS", m); }
        }, "Android");

        webView.loadDataWithBaseURL(null, H(), "text/html", "UTF-8", null);
    }

    @Override
    public void onBackPressed() {
        if (atHome) {
            new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you want to exit?")
                .setPositiveButton("Yes", (d, w) -> finish()).setNegativeButton("No", null).show();
        } else {
            atHome = true;
            webView.evaluateJavascript("goHome()", null);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!atHome) {
            float vw = webView.getWidth(), vh = webView.getHeight();
            if (vw > 0 && vh > 0) {
                float nx = Math.max(0, Math.min(1, e.getX() / vw));
                float ny = Math.max(0, Math.min(1, e.getY() / vh));
                String t = "";
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN: t = "touch_start"; break;
                    case MotionEvent.ACTION_MOVE: t = "touch_move"; break;
                    case MotionEvent.ACTION_UP: t = "touch_end"; break;
                }
                if (!t.isEmpty()) {
                    final String js = "sendTouchToServer('" + t + "'," + nx + "," + ny + ")";
                    mainHandler.post(() -> { try { webView.evaluateJavascript(js, null); } catch (Exception ex) {} });
                }
            }
        }
        return super.dispatchTouchEvent(e);
    }

    private void reapplyFullscreen() {
        runOnUiThread(() -> getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
    }

    @Override
    public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) reapplyFullscreen(); }

    @Override
    protected void onDestroy() { if (webView != null) webView.destroy(); super.onDestroy(); }

    private String H() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
        + "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>"
        + "<style>"
        + "*{margin:0;padding:0;box-sizing:border-box;}"
        + "html,body{background:#0D1117;color:#e0e0e0;font-family:sans-serif;height:100%;overflow:hidden;}"
        + ".topbar{display:flex;align-items:center;padding:6px 10px;background:#161B22;border-bottom:1px solid #1a508b;height:40px;flex-shrink:0;}"
        + ".hamburger{background:none;border:none;color:#00D4FF;font-size:20px;padding:4px 6px;}"
        + ".topbar-title{flex:1;text-align:center;}.topbar-title h1{color:#00D4FF;font-size:14px;}"
        + ".topbar-title .sub{color:#888;font-size:8px;}.topbar-title .auth{color:#00D4FF;font-size:9px;}"
        + ".sidebar-overlay{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:998;display:none;}"
        + ".sidebar-overlay.active{display:block;}"
        + ".sidebar{position:fixed;top:0;left:-260px;width:260px;height:100%;background:#161B22;z-index:999;display:flex;flex-direction:column;transition:left .25s;box-shadow:4px 0 12px rgba(0,0,0,0.5);}"
        + ".sidebar.open{left:0;}"
        + ".sidebar-header{padding:16px;border-bottom:1px solid #1a508b;}.sidebar-header h2{color:#00D4FF;font-size:16px;}"
        + ".sidebar-header .auth{color:#888;font-size:10px;margin-top:3px;}.sidebar-header .auth a{color:#00D4FF;text-decoration:none;}"
        + ".sidebar-item{display:flex;align-items:center;padding:12px 16px;border:none;background:none;color:#e0e0e0;font-size:13px;text-align:left;width:100%;border-bottom:1px solid #1a508b33;}"
        + ".sidebar-item:active{background:#0f3460;}"
        + ".sidebar-footer{margin-top:auto;padding:10px 16px;border-top:1px solid #1a508b;color:#888;font-size:9px;text-align:center;}"
        + ".home{display:flex;flex-direction:column;height:100%;overflow-y:auto;}"
        + ".card{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:10px;margin:8px;}"
        + "input{width:100%;padding:10px;background:#0f3460;border:1px solid #1a508b;border-radius:6px;color:white;font-size:14px;margin-bottom:8px;}"
        + "input:focus{outline:none;border-color:#00D4FF;}"
        + "#status{text-align:center;font-size:12px;padding:6px;}"
        + ".online{color:#4ADE80;}.offline{color:#F87171;}.connecting{color:#FBBF24;}"
        + "#toggleBtn{width:100%;padding:14px;border:none;border-radius:8px;font-size:16px;font-weight:bold;}"
        + ".btn-connect{background:#00D4FF;color:#0D1117;}.btn-disconnect{background:#F87171;color:white;}.btn-connecting{background:#FBBF24;color:#0D1117;}"
        + ".scan-title{color:#888;font-size:10px;margin:8px 8px 4px 8px;}"
        + ".scan-item{background:#161B22;border:1px solid #1a508b;border-radius:6px;padding:8px;margin:0 8px 4px 8px;display:flex;justify-content:space-between;align-items:center;}"
        + ".scan-ip{color:#00D4FF;font-family:monospace;font-size:12px;}"
        + ".scan-connect{background:#00D4FF;border:none;color:#0D1117;padding:5px 10px;border-radius:4px;font-size:10px;font-weight:bold;}"
        + ".scan-refresh{background:none;border:1px solid #1a508b;color:#00D4FF;padding:4px 10px;border-radius:4px;font-size:10px;margin:4px 8px;}"
        + ".scan-status{color:#FBBF24;font-size:10px;margin:0 8px;}"
        + ".info{font-size:9px;color:#888;line-height:1.3;}.info b{color:#00D4FF;}"
        + ".footer{text-align:center;padding:12px;margin-top:auto;}.footer .ver{color:#888;font-size:10px;}"
        + ".footer .auth{margin-top:6px;display:flex;align-items:center;justify-content:center;gap:6px;}"
        + ".footer .nm{color:#e0e0e0;font-size:12px;}.footer .nm b{color:#00D4FF;}"
        + "#screenView{display:none;flex-direction:column;overflow:hidden;position:fixed;top:40px;left:0;right:0;bottom:0;background:#000;z-index:100;}"
        + "#screenContainer{flex:1;overflow:hidden;position:relative;touch-action:none;min-height:0;}"
        + "#screenCanvas{width:100%;height:100%;display:block;background:#000;touch-action:none;}"
        + ".controls-bar{background:#161B22;border-top:1px solid #1a508b;padding:4px 6px;display:flex;flex-wrap:wrap;gap:3px;justify-content:center;flex-shrink:0;}"
        + ".ctrl-key{background:#0f3460;border:1px solid #1a508b;border-radius:3px;color:white;padding:5px 6px;font-size:9px;text-align:center;min-width:30px;}"
        + ".ctrl-key:active{background:#00D4FF;color:#0D1117;}"
        + ".ctrl-key.special{background:#1a508b;border-color:#00D4FF;color:#00D4FF;}"
        + ".qwerty{background:#161B22;border-top:1px solid #1a508b;padding:3px;display:flex;flex-direction:column;gap:2px;flex-shrink:0;}"
        + ".qwerty-row{display:flex;gap:2px;justify-content:center;}"
        + ".qkey{background:#0f3460;border:1px solid #1a508b;border-radius:3px;color:white;padding:6px 2px;font-size:10px;text-align:center;min-width:24px;flex:1;max-width:32px;}"
        + ".qkey:active{background:#00D4FF;color:#0D1117;}"
        + ".qkey.wide{min-width:36px;flex:1.5;}.qkey.special{background:#1a508b;font-size:9px;}"
        + ".qkey.space{flex:4;max-width:none;}.qkey.num{background:#00D4FF;color:#0D1117;font-weight:bold;}"
        + ".page{position:fixed;top:0;left:0;width:100%;height:100%;background:#0D1117;z-index:2000;display:none;flex-direction:column;overflow-y:auto;}"
        + ".page.active{display:flex;}"
        + ".page-header{background:#161B22;padding:12px;border-bottom:1px solid #1a508b;display:flex;align-items:center;justify-content:space-between;}"
        + ".page-header h3{color:#00D4FF;font-size:14px;}.page-back{background:none;border:none;color:#00D4FF;font-size:13px;}"
        + ".page-content{padding:12px;}.page-content h4{color:#00D4FF;margin:12px 0 6px 0;font-size:13px;}.page-content p{color:#888;font-size:11px;line-height:1.5;}"
        + ".step{background:#161B22;border:1px solid #1a508b;border-radius:6px;padding:10px;margin:6px;}"
        + ".step-num{color:#00D4FF;font-size:18px;font-weight:bold;}.step-title{color:white;font-size:12px;font-weight:bold;margin:3px 0;}"
        + ".step-desc{color:#888;font-size:10px;line-height:1.3;}"
        + "#debugLog{position:fixed;bottom:0;left:0;right:0;background:rgba(0,0,0,0.8);color:#4ADE80;font-size:7px;padding:2px 4px;z-index:9999;font-family:monospace;max-height:40px;overflow-y:auto;}"
        + "</style></head><body>"

        // TOPBAR
        + "<div class='topbar'>"
        + "<button class='hamburger' onclick='openSidebar()'>\u2630</button>"
        + "<div class='topbar-title'><h1>ScreenShare</h1>"
        + "<div class='sub'>Wireless Display + Touch + Keyboard</div>"
        + "<div class='auth'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div></div>"
        + "<div style='width:32px;'></div></div>"

        // SIDEBAR
        + "<div class='sidebar-overlay' id='sidebarOverlay' onclick='closeSidebar()'></div>"
        + "<div class='sidebar' id='sidebar'>"
        + "<div class='sidebar-header'><h2>ScreenShare</h2><div class='auth'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div></div>"
        + "<button class='sidebar-item' onclick='goHome()'>Home</button>"
        + "<button class='sidebar-item' onclick='showPage(\"settingsPage\")'>Settings</button>"
        + "<button class='sidebar-item' onclick='showPage(\"clipboardPage\")'>Clipboard</button>"
        + "<button class='sidebar-item' onclick='showPage(\"historyPage\")'>History</button>"
        + "<button class='sidebar-item' onclick='showPage(\"tutorialPage\")'>Tutorial</button>"
        + "<button class='sidebar-item' onclick='showPage(\"aboutPage\")'>About</button>"
        + "<div class='sidebar-footer'>v3.0.0 | github.com/jalalmx32/screen-share</div></div>"

        // HOME
        + "<div id='homeView' class='home'>"
        + "<div class='card'>"
        + "<input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>"
        + "<input type='password' id='passInput' placeholder='Password (optional)'>"
        + "<button id='toggleBtn' class='btn-connect' ontouchstart='toggleConnection(event)' onclick='toggleConnection(event)'>Connect</button></div>"
        + "<div id='status' class='offline'>Offline</div>"
        + "<div class='scan-title'>Scanning for servers...</div>"
        + "<div id='scanList'></div>"
        + "<div class='scan-status' id='scanStatus'></div>"
        + "<button class='scan-refresh' ontouchstart='startScan(event)' onclick='startScan(event)'>Refresh</button>"
        + "<div class='card'><div class='info'>"
        + "<b>1.</b> Enable hotspot on Android<br><b>2.</b> Connect PC to hotspot<br>"
        + "<b>3.</b> Start ScreenShare on PC<br><b>4.</b> Enter IP or tap server<br>"
        + "<b>5.</b> Touch to control PC!</div></div>"
        + "<div class='footer'><div class='ver'>ScreenShare v3.0.0</div>"
        + "<div class='auth'><span class='nm'>by <b>Jalal</b></span>"
        + "<a class='tg' href='https://t.me/x16_96'>"
        + "<svg width='16' height='16' viewBox='0 0 24 24' fill='#00D4FF'><path d='M12 0C5.37 0 0 5.37 0 12s5.37 12 12 12 12-5.37 12-12S18.63 0 12 0zm5.95 7.47l-1.97 9.28c-.15.67-.54.83-1.09.52l-3.02-2.22-1.46 1.4c-.16.16-.3.3-.61.3l.22-3.05 5.55-5.01c.24-.22-.05-.33-.37-.14L8.68 13.3l-2.96-.92c-.64-.2-.66-.64.13-.95l11.53-4.45c.53-.19 1 .13.83.95l-.23.09z'/></svg>"
        + "<span style='color:#00D4FF;font-size:11px;'>@x16_96</span></a></div></div></div>"

        // SCREEN
        + "<div id='screenView'>"
        + "<div id='screenContainer'><canvas id='screenCanvas'></canvas></div>"
        + "<div class='controls-bar'>"
        + "<div class='ctrl-key special' ontouchstart='sendKey(event,\"win\")'>Win</div>"
        + "<div class='ctrl-key special' ontouchstart='sendKey(event,\"alt\")'>Alt</div>"
        + "<div class='ctrl-key special' ontouchstart='sendKey(event,\"ctrl\")'>Ctrl</div>"
        + "<div class='ctrl-key special' ontouchstart='sendKey(event,\"shift\")'>Shift</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"alt_tab\")'>Alt+Tab</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"ctrl_c\")'>C</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"ctrl_v\")'>V</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"up\")'>\u2191</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"down\")'>\u2193</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"left\")'>\u2190</div>"
        + "<div class='ctrl-key' ontouchstart='sendKey(event,\"right\")'>\u2192</div></div>"
        + "<div class='qwerty'>"
        + "<div class='qwerty-row'>"
        + "<div class='qkey num' ontouchstart='sendChar(event,\"1\")'>1</div><div class='qkey num' ontouchstart='sendChar(event,\"2\")'>2</div><div class='qkey num' ontouchstart='sendChar(event,\"3\")'>3</div><div class='qkey num' ontouchstart='sendChar(event,\"4\")'>4</div><div class='qkey num' ontouchstart='sendChar(event,\"5\")'>5</div><div class='qkey num' ontouchstart='sendChar(event,\"6\")'>6</div><div class='qkey num' ontouchstart='sendChar(event,\"7\")'>7</div><div class='qkey num' ontouchstart='sendChar(event,\"8\")'>8</div><div class='qkey num' ontouchstart='sendChar(event,\"9\")'>9</div><div class='qkey num' ontouchstart='sendChar(event,\"0\")'>0</div></div>"
        + "<div class='qwerty-row'>"
        + "<div class='qkey' ontouchstart='sendChar(event,\"q\")'>Q</div><div class='qkey' ontouchstart='sendChar(event,\"w\")'>W</div><div class='qkey' ontouchstart='sendChar(event,\"e\")'>E</div><div class='qkey' ontouchstart='sendChar(event,\"r\")'>R</div><div class='qkey' ontouchstart='sendChar(event,\"t\")'>T</div><div class='qkey' ontouchstart='sendChar(event,\"y\")'>Y</div><div class='qkey' ontouchstart='sendChar(event,\"u\")'>U</div><div class='qkey' ontouchstart='sendChar(event,\"i\")'>I</div><div class='qkey' ontouchstart='sendChar(event,\"o\")'>O</div><div class='qkey' ontouchstart='sendChar(event,\"p\")'>P</div></div>"
        + "<div class='qwerty-row'>"
        + "<div class='qkey' ontouchstart='sendChar(event,\"a\")'>A</div><div class='qkey' ontouchstart='sendChar(event,\"s\")'>S</div><div class='qkey' ontouchstart='sendChar(event,\"d\")'>D</div><div class='qkey' ontouchstart='sendChar(event,\"f\")'>F</div><div class='qkey' ontouchstart='sendChar(event,\"g\")'>G</div><div class='qkey' ontouchstart='sendChar(event,\"h\")'>H</div><div class='qkey' ontouchstart='sendChar(event,\"j\")'>J</div><div class='qkey' ontouchstart='sendChar(event,\"k\")'>K</div><div class='qkey' ontouchstart='sendChar(event,\"l\")'>L</div></div>"
        + "<div class='qwerty-row'>"
        + "<div class='qkey wide special' ontouchstart='sendKey(event,\"shift\")'>Shift</div><div class='qkey' ontouchstart='sendChar(event,\"z\")'>Z</div><div class='qkey' ontouchstart='sendChar(event,\"x\")'>X</div><div class='qkey' ontouchstart='sendChar(event,\"c\")'>C</div><div class='qkey' ontouchstart='sendChar(event,\"v\")'>V</div><div class='qkey' ontouchstart='sendChar(event,\"b\")'>B</div><div class='qkey' ontouchstart='sendChar(event,\"n\")'>N</div><div class='qkey' ontouchstart='sendChar(event,\"m\")'>M</div><div class='qkey wide special' ontouchstart='sendKey(event,\"backspace\")'>BS</div></div>"
        + "<div class='qwerty-row'>"
        + "<div class='qkey wide special' ontouchstart='sendKey(event,\"tab\")'>Tab</div><div class='qkey space' ontouchstart='sendKey(event,\"space\")'>Space</div><div class='qkey wide special' ontouchstart='sendKey(event,\"enter\")'>Enter</div></div></div></div>"

        // PAGES
        + "<div class='page' id='settingsPage'><div class='page-header'><h3>Settings</h3><button class='page-back' onclick='goHome()'>Back</button></div><div class='page-content'><h4>Display</h4><p>Quality: 720p | FPS: 30</p></div></div>"
        + "<div class='page' id='clipboardPage'><div class='page-header'><h3>Clipboard</h3><button class='page-back' onclick='goHome()'>Back</button></div><div class='page-content'><textarea id='clipText' style='width:100%;min-height:80px;background:#0f3460;border:1px solid #1a508b;color:white;padding:8px;border-radius:6px;' placeholder='Type or paste text...'></textarea><div style='margin-top:6px;'><button onclick='sendClipboard()' style='background:#00D4FF;border:none;color:#0D1117;padding:8px 12px;border-radius:5px;font-weight:bold;margin:3px;'>Send to PC</button><button onclick='getClipboard()' style='background:#1a508b;border:none;color:white;padding:8px 12px;border-radius:5px;margin:3px;'>Get from PC</button></div><div id='clipStatus' style='color:#4ADE80;font-size:10px;margin-top:6px;'></div></div></div>"
        + "<div class='page' id='historyPage'><div class='page-header'><h3>History</h3><button class='page-back' onclick='goHome()'>Back</button></div><div class='page-content' id='historyList'><p style='color:#888'>No connections yet</p></div></div>"
        + "<div class='page' id='tutorialPage'><div class='page-header'><h3>Tutorial</h3><button class='page-back' onclick='goHome()'>Back</button></div>"
        + "<div class='step'><div class='step-num'>1</div><div class='step-title'>Enable Hotspot</div><div class='step-desc'>Turn on WiFi hotspot on Android</div></div>"
        + "<div class='step'><div class='step-num'>2</div><div class='step-title'>Connect PC</div><div class='step-desc'>Connect PC to your hotspot</div></div>"
        + "<div class='step'><div class='step-num'>3</div><div class='step-title'>Start Server</div><div class='step-desc'>Run ScreenShare on Windows</div></div>"
        + "<div class='step'><div class='step-num'>4</div><div class='step-title'>Auto Detect</div><div class='step-desc'>App auto-detects servers</div></div>"
        + "<div class='step'><div class='step-num'>5</div><div class='step-title'>Touch & Pinch</div><div class='step-desc'>Tap to click, pinch to zoom, drag to pan</div></div>"
        + "<div class='step'><div class='step-num'>6</div><div class='step-title'>Keyboard</div><div class='step-desc'>QWERTY + special keys + gamepad</div></div></div>"
        + "<div class='page' id='aboutPage'><div class='page-header'><h3>About</h3><button class='page-back' onclick='goHome()'>Back</button></div><div class='page-content'><h4>ScreenShare v3.0.0</h4><p>Wireless Display + Touch Control</p><p>A free alternative to Spacedesk</p><h4>Developer</h4><p>Jalal | @x16_96</p><p>github.com/jalalmx32/screen-share</p></div></div>"
        + "<div id='debugLog'></div>"

        // JAVASCRIPT
        + "<script>"
        + "var ws=null,isConnected=false,atHome=true,canvasEl=null,ctx=null;"
        + "try{var s=JSON.parse(localStorage.getItem('ss')||'{}');if(s.ip)document.getElementById('ipInput').value=s.ip;if(s.pass)document.getElementById('passInput').value=s.pass;}catch(e){}"
        + "function save(k,v){try{var s=JSON.parse(localStorage.getItem('ss')||'{}');s[k]=v;localStorage.setItem('ss',JSON.stringify(s));}catch(e){}}"
        + "function dbg(m){var d=document.getElementById('debugLog');if(d){d.innerHTML=m+'<br>';d.scrollTop=d.scrollHeight;}}"

        // SIDEBAR
        + "function openSidebar(){document.getElementById('sidebar').classList.add('open');document.getElementById('sidebarOverlay').classList.add('active');}"
        + "function closeSidebar(){document.getElementById('sidebar').classList.remove('open');document.getElementById('sidebarOverlay').classList.remove('active');}"

        // GO HOME
        + "function goHome(){closeSidebar();document.querySelectorAll('.page').forEach(function(p){p.classList.remove('active');});document.getElementById('homeView').style.display='flex';document.getElementById('screenView').style.display='none';try{Android.setPage('home');}catch(e){}if(isConnected){isConnected=false;if(ws){try{ws.close();}catch(x){}ws=null;}setBtn('Connect','btn-connect');setStatus('Offline','offline');}atHome=true;}"
        + "function showPage(id){closeSidebar();document.getElementById(id).classList.add('active');try{Android.setPage('page');}catch(e){}atHome=false;}"

        // PINCH ZOOM
        + "var zoom=1,panX=0,panY=0;"
        + "(function(){var lc=document.getElementById('screenCanvas');if(!lc)return;var lastDist=0,lastX=0,lastY=0,pinch=false,drag=false;"
        + "lc.addEventListener('touchstart',function(e){if(e.touches.length===2){pinch=true;drag=false;var dx=e.touches[0].clientX-e.touches[1].clientX;var dy=e.touches[0].clientY-e.touches[1].clientY;lastDist=Math.sqrt(dx*dx+dy*dy);e.preventDefault();}else if(e.touches.length===1){drag=true;lastX=e.touches[0].clientX;lastY=e.touches[0].clientY;}},{passive:false});"
        + "lc.addEventListener('touchmove',function(e){if(pinch&&e.touches.length===2){var dx=e.touches[0].clientX-e.touches[1].clientX;var dy=e.touches[0].clientY-e.touches[1].clientY;var d=Math.sqrt(dx*dx+dy*dy);zoom=Math.max(0.5,Math.min(5,zoom*(d/lastDist)));lastDist=d;updateTransform();e.preventDefault();}else if(drag&&e.touches.length===1&&!pinch){panX+=e.touches[0].clientX-lastX;panY+=e.touches[0].clientY-lastY;lastX=e.touches[0].clientX;lastY=e.touches[0].clientY;updateTransform();e.preventDefault();}},{passive:false});"
        + "lc.addEventListener('touchend',function(e){if(e.touches.length<2)pinch=false;if(e.touches.length===0)drag=false;});})();"
        + "function updateTransform(){if(canvasEl)canvasEl.style.transform='translate('+panX+'px,'+panY+'px) scale('+zoom+')';}"
        + "function resetView(){zoom=1;panX=0;panY=0;updateTransform();}"

        // SCAN
        + "var scannedHosts=[];"
        + "function startScan(e){if(e)e.preventDefault();document.getElementById('scanStatus').textContent='Scanning...';document.getElementById('scanList').innerHTML='';var base='192.168.';var ip=document.getElementById('ipInput').value.trim();if(ip){var p=ip.split('.');if(p.length>=3)base=p[0]+'.'+p[1]+'.';}var hosts=[];for(var i=1;i<=15;i++)hosts.push(base+'1.'+i);hosts.push(base+'43.1');hosts.push(base+'0.1');var found=0,checked=0;"
        + "hosts.forEach(function(h){try{var t=new WebSocket('ws://'+h+':8765');var host=h;var timer=setTimeout(function(){t.close();checked++;if(checked>=hosts.length)scanDone(found);},1200);"
        + "t.onopen=function(){clearTimeout(timer);found++;checked++;addScanItem(host);t.close();if(checked>=hosts.length)scanDone(found);};"
        + "t.onerror=function(){clearTimeout(timer);checked++;if(checked>=hosts.length)scanDone(found);};}catch(x){checked++;}});}"
        + "function scanDone(n){document.getElementById('scanStatus').textContent=n>0?'Found '+n+' server(s)':'No servers found';}"
        + "function addScanItem(ip){var el=document.getElementById('scanList');var d=document.createElement('div');d.className='scan-item';d.innerHTML='<div><span class=scan-ip>'+ip+'</span></div>';var btn=document.createElement('button');btn.className='scan-connect';btn.textContent='Connect';btn.ontouchstart=function(e){e.preventDefault();document.getElementById('ipInput').value=ip+':8765';toggleConnection(e);};btn.onclick=function(e){e.preventDefault();document.getElementById('ipInput').value=ip+':8765';toggleConnection(e);};d.appendChild(btn);el.appendChild(d);}"

        // CONNECTION - THE KEY FIX
        + "function toggleConnection(e){if(e)e.preventDefault();dbg('toggle');"
        + "if(isConnected){if(ws){try{ws.close();}catch(x){}ws=null;}isConnected=false;atHome=true;document.getElementById('homeView').style.display='flex';document.getElementById('screenView').style.display='none';setBtn('Connect','btn-connect');setStatus('Offline','offline');resetView();try{Android.setPage('home');}catch(e){}}"
        + "else{var ip='';try{ip=document.getElementById('ipInput').value.trim();}catch(x){}dbg('IP=['+ip+']');if(!ip){dbg('No IP');return;}"
        + "if(ip.indexOf(':')===-1)ip+=':8765';var pass='';try{pass=document.getElementById('passInput').value;}catch(x){}"
        + "try{save('ip',ip);save('pass',pass);}catch(x){}"
        + "setStatus('Connecting...','connecting');setBtn('Connecting...','btn-connecting');dbg('Connecting ws://'+ip);"
        + "try{ws=new WebSocket('ws://'+ip);ws.binaryType='arraybuffer';dbg('WS created');"
        + "ws.onopen=function(){dbg('CONNECTED');if(pass)ws.send(JSON.stringify({type:'auth',password:pass}));isConnected=true;atHome=false;setStatus('Connected!','online');setBtn('Disconnect','btn-disconnect');document.getElementById('homeView').style.display='none';document.getElementById('screenView').style.display='flex';try{Android.setPage('screen');}catch(e){}saveHistory(ip);canvasEl=document.getElementById('screenCanvas');if(canvasEl)ctx=canvasEl.getContext('2d');dbg('Screen ready');};"

        // IMAGE HANDLER - USE CANVAS
        + "ws.onmessage=function(e){if(e.data instanceof ArrayBuffer){try{var b=new Blob([e.data],{type:'image/jpeg'});var u=URL.createObjectURL(b);var tmp=new Image();tmp.onload=function(){if(canvasEl&&ctx){canvasEl.width=tmp.width;canvasEl.height=tmp.height;ctx.drawImage(tmp,0,0);dbg('Frame '+tmp.width+'x'+tmp.height);}URL.revokeObjectURL(u);};tmp.onerror=function(){dbg('Img load err');URL.revokeObjectURL(u);};tmp.src=u;}catch(ex){dbg('ERR:'+ex.message);}}else{dbg('TXT:'+e.data.substring(0,60));}};"

        + "ws.onclose=function(e){dbg('Closed:'+e.code);goHome();};"
        + "ws.onerror=function(){dbg('WS Error');goHome();};"
        + "}catch(ex){dbg('Exc:'+ex.message);goHome();}}}"

        + "function setBtn(t,c){var b=document.getElementById('toggleBtn');b.textContent=t;b.className=c;}"
        + "function setStatus(t,c){var s=document.getElementById('status');s.textContent=t;s.className=c;}"

        // TOUCH & KEYS
        + "function sendTouchToServer(t,x,y){if(ws&&ws.readyState===1)ws.send(JSON.stringify({type:t,x:x,y:y}));}"
        + "function sendKey(e,k){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===1)ws.send(JSON.stringify({type:'key',key:k}));}"
        + "function sendChar(e,c){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===1)ws.send(JSON.stringify({type:'char',char:c}));}"

        // CLIPBOARD
        + "function sendClipboard(){var t=document.getElementById('clipText').value;if(ws&&ws.readyState===1){ws.send(JSON.stringify({type:'clipboard',text:t}));document.getElementById('clipStatus').textContent='Sent!';}}"
        + "function getClipboard(){if(ws&&ws.readyState===1){ws.send(JSON.stringify({type:'get_clipboard'}));document.getElementById('clipStatus').textContent='Requesting...';}}"

        // HISTORY
        + "function saveHistory(ip){var h=JSON.parse(localStorage.getItem('hist')||'[]');h=h.filter(function(i){return i!==ip;});h.unshift(ip);if(h.length>10)h=h.slice(0,10);localStorage.setItem('hist',JSON.stringify(h));}"
        + "function loadHistory(){var h=JSON.parse(localStorage.getItem('hist')||'[]');var el=document.getElementById('historyList');if(h.length===0){el.innerHTML='<p style=color:#888>No connections yet</p>';return;}el.innerHTML='';h.forEach(function(ip){var d=document.createElement('div');d.style.cssText='display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #1a508b33;';var sp=document.createElement('span');sp.style.cssText='color:#00D4FF;font-size:12px;font-family:monospace;cursor:pointer;';sp.textContent=ip;sp.onclick=function(){document.getElementById('ipInput').value=ip;goHome();};var btn=document.createElement('button');btn.style.cssText='background:none;border:none;color:#F87171;';btn.textContent='X';btn.onclick=function(){var h2=JSON.parse(localStorage.getItem('hist')||'[]');h2=h2.filter(function(i){return i!==ip;});localStorage.setItem('hist',JSON.stringify(h2));loadHistory();};d.appendChild(sp);d.appendChild(btn);el.appendChild(d);});}"
        + "</script></body></html>";
    }
}
