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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private WebView webView;
    private Handler mainHandler;
    private boolean showMenu = true;
    
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
    public void onBackPressed() {
        // Always handle back button in the app
        if (!showMenu) {
            // Go back to menu
            showMenu = true;
            webView.evaluateJavascript("goHome()", null);
        } else {
            // Show exit confirmation
            new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        }
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

        ".topbar{display:flex;align-items:center;padding:8px 12px;background:#161B22;border-bottom:1px solid #1a508b;}" +
        ".hamburger{background:none;border:none;color:#00D4FF;font-size:22px;padding:4px 8px;}" +
        ".topbar-title{flex:1;text-align:center;}" +
        ".topbar-title h1{color:#00D4FF;font-size:16px;}" +
        ".topbar-title .sub{color:#888;font-size:9px;}" +
        ".topbar-title .author{color:#00D4FF;font-size:10px;}" +
        ".topbar-title .author a{color:#00D4FF;text-decoration:none;}" +

        ".sidebar-overlay{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:998;display:none;}" +
        ".sidebar-overlay.active{display:block;}" +
        ".sidebar{position:fixed;top:0;left:-280px;width:280px;height:100%;background:#161B22;z-index:999;display:flex;flex-direction:column;transition:left .3s;box-shadow:4px 0 16px rgba(0,0,0,0.5);}" +
        ".sidebar.open{left:0;}" +
        ".sidebar-header{padding:20px 16px;border-bottom:1px solid #1a508b;}" +
        ".sidebar-header h2{color:#00D4FF;font-size:18px;}" +
        ".sidebar-header .author{color:#888;font-size:11px;margin-top:4px;}" +
        ".sidebar-header .author a{color:#00D4FF;text-decoration:none;}" +
        ".sidebar-item{display:flex;align-items:center;padding:14px 16px;border:none;background:none;color:#e0e0e0;font-size:14px;text-align:left;width:100%;border-bottom:1px solid #1a508b33;}" +
        ".sidebar-item:active{background:#0f3460;}" +
        ".sidebar-item .icon{width:24px;text-align:center;margin-right:12px;font-size:16px;}" +
        ".sidebar-footer{margin-top:auto;padding:12px 16px;border-top:1px solid #1a508b;color:#888;font-size:10px;text-align:center;}" +

        ".card{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:12px;margin:8px;}" +
        "input,select{width:100%;padding:10px;background:#0f3460;border:1px solid #1a508b;border-radius:6px;color:white;font-size:14px;margin-bottom:8px;}" +
        "input:focus,select:focus{outline:none;border-color:#00D4FF;}" +
        "#status{text-align:center;font-size:12px;padding:6px;}" +
        ".online{color:#4ADE80;} .offline{color:#F87171;} .connecting{color:#FBBF24;}" +

        "#toggleBtn{width:100%;padding:14px;border:none;border-radius:8px;font-size:16px;font-weight:bold;cursor:pointer;}" +
        ".btn-connect{background:#00D4FF;color:#0D1117;}" +
        ".btn-disconnect{background:#F87171;color:white;}" +
        ".btn-connecting{background:#FBBF24;color:#0D1117;}" +

        "#screen{flex:1;background:#000;display:none;position:relative;overflow:hidden;}" +
        "#screen.active{display:flex;flex-direction:column;}" +
        "#screenImg{width:100%;flex:1;object-fit:contain;}" +
        ".touch-hint{text-align:center;color:#4ADE80;font-size:10px;padding:4px;}" +

        ".qwerty{background:#161B22;border-top:1px solid #1a508b;padding:4px;display:none;flex-direction:column;gap:3px;}" +
        ".qwerty.active{display:flex;}" +
        ".qwerty-row{display:flex;gap:3px;justify-content:center;}" +
        ".qkey{background:#0f3460;border:1px solid #1a508b;border-radius:4px;color:white;padding:8px 4px;font-size:11px;text-align:center;min-width:28px;flex:1;max-width:36px;}" +
        ".qkey:active{background:#00D4FF;color:#0D1117;}" +
        ".qkey.wide{min-width:44px;flex:1.5;}" +
        ".qkey.special{background:#1a508b;font-size:10px;}" +
        ".qkey.space{flex:4;max-width:none;}" +
        ".qkey.num{background:#00D4FF;color:#0D1117;font-weight:bold;}" +

        ".special-bar{display:flex;gap:3px;justify-content:center;padding:2px;}" +
        ".skey{background:#1a508b;border:1px solid #00D4FF;border-radius:4px;color:#00D4FF;padding:6px 8px;font-size:10px;min-width:40px;text-align:center;}" +
        ".skey:active{background:#00D4FF;color:#0D1117;}" +

        ".gamepad{background:#161B22;border-top:1px solid #1a508b;padding:12px;display:none;}" +
        ".gamepad.active{display:block;}" +
        ".dpad-btn{width:44px;height:44px;background:#0f3460;border:1px solid #1a508b;border-radius:8px;color:white;font-size:18px;display:inline-flex;align-items:center;justify-content:center;margin:2px;}" +
        ".dpad-btn:active{background:#00D4FF;color:#0D1117;}" +
        ".ab-btn{width:50px;height:50px;border-radius:25px;border:2px solid #00D4FF;background:#0f3460;color:#00D4FF;font-size:16px;font-weight:bold;display:inline-flex;align-items:center;justify-content:center;margin:4px;}" +
        ".ab-btn:active{background:#00D4FF;color:#0D1117;}" +

        ".page{position:fixed;top:0;left:0;width:100%;height:100%;background:#0D1117;z-index:2000;display:none;flex-direction:column;overflow-y:auto;}" +
        ".page.active{display:flex;}" +
        ".page-header{background:#161B22;padding:14px;border-bottom:1px solid #1a508b;display:flex;align-items:center;justify-content:space-between;}" +
        ".page-header h3{color:#00D4FF;font-size:16px;}" +
        ".page-back{background:none;border:none;color:#00D4FF;font-size:14px;cursor:pointer;}" +
        ".page-content{padding:16px;}" +
        ".page-content h4{color:#00D4FF;margin:16px 0 8px 0;font-size:14px;}" +
        ".page-content p{color:#888;font-size:12px;line-height:1.6;}" +

        ".setting-item{display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid #1a508b33;}" +
        ".setting-label{color:white;font-size:13px;}" +
        ".setting-desc{color:#888;font-size:10px;margin-top:2px;}" +
        ".toggle{position:relative;width:44px;height:24px;cursor:pointer;}" +
        ".toggle input{opacity:0;width:0;height:0;}" +
        ".slider{position:absolute;top:0;left:0;right:0;bottom:0;background:#0f3460;border-radius:12px;transition:.3s;}" +
        ".slider:before{content:'';position:absolute;width:18px;height:18px;left:3px;bottom:3px;background:white;border-radius:50%;transition:.3s;}" +
        ".toggle input:checked+.slider{background:#00D4FF;}" +
        ".toggle input:checked+.slider:before{transform:translateX(20px);}" +

        ".clip-area{width:100%;min-height:100px;background:#0f3460;border:1px solid #1a508b;border-radius:8px;color:white;padding:10px;font-size:13px;resize:vertical;}" +
        ".clip-btn{padding:10px;border:none;border-radius:6px;font-size:13px;font-weight:bold;margin:4px;cursor:pointer;}" +
        ".clip-send{background:#00D4FF;color:#0D1117;}" +
        ".clip-paste{background:#1a508b;color:white;}" +

        ".history-item{display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #1a508b33;}" +
        ".history-ip{color:#00D4FF;font-size:13px;font-family:monospace;cursor:pointer;}" +
        ".history-del{background:none;border:none;color:#F87171;font-size:12px;cursor:pointer;}" +

        ".file-list{max-height:200px;overflow-y:auto;}" +
        ".file-item{display:flex;justify-content:space-between;align-items:center;padding:8px;border-bottom:1px solid #1a508b33;}" +
        ".file-name{color:white;font-size:12px;}" +
        ".file-size{color:#888;font-size:10px;}" +
        ".file-dl{background:#00D4FF;border:none;color:#0D1117;padding:4px 8px;border-radius:4px;font-size:10px;cursor:pointer;}" +

        ".step{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:12px;margin:8px;}" +
        ".step-num{color:#00D4FF;font-size:20px;font-weight:bold;}" +
        ".step-title{color:white;font-size:13px;font-weight:bold;margin:4px 0;}" +
        ".step-desc{color:#888;font-size:11px;line-height:1.4;}" +
        ".step-key{display:inline-block;background:#0f3460;border:1px solid #1a508b;border-radius:4px;padding:2px 6px;color:#00D4FF;font-size:10px;margin:2px;}" +

        ".info{font-size:10px;color:#888;line-height:1.4;padding:0 8px 8px 8px;}" +
        ".info b{color:#00D4FF;}" +
        "</style></head><body>" +

        "<div class='topbar'>" +
        "  <button class='hamburger' onclick='openSidebar()'>\u2630</button>" +
        "  <div class='topbar-title'>" +
        "    <h1>ScreenShare</h1>" +
        "    <div class='sub'>Wireless Display + Touch + Keyboard</div>" +
        "    <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "  </div>" +
        "  <div style='width:40px;'></div>" +
        "</div>" +

        "<div class='sidebar-overlay' id='sidebarOverlay' onclick='closeSidebar()'></div>" +
        "<div class='sidebar' id='sidebar'>" +
        "  <div class='sidebar-header'>" +
        "    <h2>ScreenShare</h2>" +
        "    <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "  </div>" +
        "  <button class='sidebar-item' onclick='closeSidebar()'>Home</button>" +
        "  <button class='sidebar-item' onclick='showSettings()'>Settings</button>" +
        "  <button class='sidebar-item' onclick='showClipboard()'>Clipboard</button>" +
        "  <button class='sidebar-item' onclick='showHistory()'>History</button>" +
        "  <button class='sidebar-item' onclick='showTutorial()'>Tutorial</button>" +
        "  <button class='sidebar-item' onclick='showAbout()'>About</button>" +
        "  <div class='sidebar-footer'>ScreenShare v2.1.0<br>github.com/jalalmx32/screen-share</div>" +
        "</div>" +

        "<div class='card' id='connectCard'>" +
        "  <input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>" +
        "  <input type='password' id='passInput' placeholder='Password (optional)'>" +
        "  <button id='toggleBtn' class='btn-connect' onclick='toggleConnection()'>Connect</button>" +
        "</div>" +
        "<div id='status' class='offline'>Offline</div>" +

        "<div id='screen'>" +
        "  <img id='screenImg'>" +
        "  <div id='touchHint' class='touch-hint'>Touch to control</div>" +
        "  <div class='special-bar' id='specialBar'>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"win\")'>Win</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"alt\")'>Alt</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl\")'>Ctrl</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"shift\")'>Shift</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"alt_tab\")'>Alt+Tab</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl_c\")'>C</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl_v\")'>V</div>" +
        "  </div>" +
        "  <div class='qwerty' id='qwerty'>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"1\")'>1</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"2\")'>2</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"3\")'>3</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"4\")'>4</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"5\")'>5</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"6\")'>6</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"7\")'>7</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"8\")'>8</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"9\")'>9</div>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"0\")'>0</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"q\")'>Q</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"w\")'>W</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"e\")'>E</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"r\")'>R</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"t\")'>T</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"y\")'>Y</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"u\")'>U</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"i\")'>I</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"o\")'>O</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"p\")'>P</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"a\")'>A</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"s\")'>S</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"d\")'>D</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"f\")'>F</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"g\")'>G</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"h\")'>H</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"j\")'>J</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"k\")'>K</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"l\")'>L</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"shift\")'>Shift</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"z\")'>Z</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"x\")'>X</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"c\")'>C</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"v\")'>V</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"b\")'>B</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"n\")'>N</div>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"m\")'>M</div>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"backspace\")'>BS</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"tab\")'>Tab</div>" +
        "      <div class='qkey space' ontouchstart='sendKey(event,\"space\")'>Space</div>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"enter\")'>Enter</div>" +
        "    </div>" +
        "  </div>" +
        "  <div class='gamepad' id='gamepad'>" +
        "    <div style='display:flex;justify-content:space-between;align-items:center;'>" +
        "      <div><div class='dpad-btn' ontouchstart='sendKey(event,\"up\")'>\u25B2</div>" +
        "        <div class='dpad-btn' ontouchstart='sendKey(event,\"left\")'>\u25C0</div>" +
        "        <div class='dpad-btn' ontouchstart='sendKey(event,\"right\")'>\u25B6</div>" +
        "        <div class='dpad-btn' ontouchstart='sendKey(event,\"down\")'>\u25BC</div></div>" +
        "      <div><div class='ab-btn' ontouchstart='sendChar(event,\"z\")'>A</div>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"x\")'>B</div>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"c\")'>Y</div>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"v\")'>X</div></div>" +
        "    </div>" +
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

        "<div class='page' id='settingsPage'>" +
        "  <div class='page-header'><h3>Settings</h3><button class='page-back' onclick='hidePage(\"settingsPage\")'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>Display</h4>" +
        "    <div class='setting-item'><div><div class='setting-label'>Quality</div></div>" +
        "      <select id='setQuality' onchange='saveSetting(\"quality\",this.value)' style='width:100px;'>" +
        "        <option value='1080p'>1080p</option><option value='720p' selected>720p</option><option value='480p'>480p</option><option value='360p'>360p</option>" +
        "      </select></div>" +
        "    <div class='setting-item'><div><div class='setting-label'>Frame Rate</div></div>" +
        "      <select id='setFPS' onchange='saveSetting(\"fps\",this.value)' style='width:100px;'>" +
        "        <option value='15'>15</option><option value='24'>24</option><option value='30' selected>30</option><option value='60'>60</option>" +
        "      </select></div>" +
        "    <h4>Input</h4>" +
        "    <div class='setting-item'><div><div class='setting-label'>Show Keyboard</div></div>" +
        "      <label class='toggle'><input type='checkbox' id='setKB' checked onchange='saveSetting(\"showkb\",this.checked)'><span class='slider'></span></label></div>" +
        "    <div class='setting-item'><div><div class='setting-label'>Show Gamepad</div></div>" +
        "      <label class='toggle'><input type='checkbox' id='setGP' onchange='saveSetting(\"showgp\",this.checked)'><span class='slider'></span></label></div>" +
        "    <h4>Security</h4>" +
        "    <input type='password' id='setPass' placeholder='Set password' onchange='saveSetting(\"password\",this.value)'>" +
        "  </div>" +
        "</div>" +

        "<div class='page' id='clipboardPage'>" +
        "  <div class='page-header'><h3>Clipboard</h3><button class='page-back' onclick='hidePage(\"clipboardPage\")'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <textarea class='clip-area' id='clipText' placeholder='Type or paste text...'></textarea>" +
        "    <div style='margin-top:8px;'>" +
        "      <button class='clip-btn clip-send' onclick='sendClipboard()'>Send to PC</button>" +
        "      <button class='clip-btn clip-paste' onclick='getClipboard()'>Get from PC</button>" +
        "    </div>" +
        "    <div id='clipStatus' style='color:#4ADE80;font-size:11px;margin-top:8px;'></div>" +
        "  </div>" +
        "</div>" +

        "<div class='page' id='historyPage'>" +
        "  <div class='page-header'><h3>History</h3><button class='page-back' onclick='hidePage(\"historyPage\")'>Back</button></div>" +
        "  <div class='page-content' id='historyList'><p style='color:#888'>No connections yet</p></div>" +
        "</div>" +

        "<div class='page' id='tutorialPage'>" +
        "  <div class='page-header'><h3>Tutorial</h3><button class='page-back' onclick='hidePage(\"tutorialPage\")'>Back</button></div>" +
        "  <div class='step'><div class='step-num'>1</div><div class='step-title'>Enable Hotspot</div><div class='step-desc'>Turn on WiFi hotspot</div></div>" +
        "  <div class='step'><div class='step-num'>2</div><div class='step-title'>Connect PC</div><div class='step-desc'>Connect PC to hotspot</div></div>" +
        "  <div class='step'><div class='step-num'>3</div><div class='step-title'>Start Server</div><div class='step-desc'>Run ScreenShare on Windows</div></div>" +
        "  <div class='step'><div class='step-num'>4</div><div class='step-title'>Connect</div><div class='step-desc'>Enter IP and tap Connect</div></div>" +
        "  <div class='step'><div class='step-num'>5</div><div class='step-title'>Touch</div><div class='step-desc'>Tap to click, drag to move</div></div>" +
        "  <div class='step'><div class='step-num'>6</div><div class='step-title'>Keyboard</div><div class='step-desc'>Use QWERTY or special keys</div></div>" +
        "  <div class='step'><div class='step-num'>7</div><div class='step-title'>Clipboard</div><div class='step-desc'>Sync text between PC and phone</div></div>" +
        "  <div class='step'><div class='step-num'>8</div><div class='step-title'>Gamepad</div><div class='step-desc'>Enable in Settings for gaming</div></div>" +
        "</div>" +

        "<div class='page' id='aboutPage'>" +
        "  <div class='page-header'><h3>About</h3><button class='page-back' onclick='hidePage(\"aboutPage\")'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>ScreenShare v2.1.0</h4>" +
        "    <p>Wireless Display + Touch Control</p>" +
        "    <p>A free alternative to Spacedesk</p>" +
        "    <h4>Features</h4>" +
        "    <p>- Real-time screen streaming</p>" +
        "    <p>- Touch control</p>" +
        "    <p>- Full QWERTY keyboard</p>" +
        "    <p>- Game controller</p>" +
        "    <p>- Clipboard sync</p>" +
        "    <p>- Password protection</p>" +
        "    <h4>Developer</h4>" +
        "    <p>Jalal | @x16_96</p>" +
        "    <p>github.com/jalalmx32/screen-share</p>" +
        "  </div>" +
        "</div>" +

        "<script>" +
        "var ws=null;var isConnected=false;var currentPage='';" +

        "(function(){" +
        "  try{" +
        "    var s=JSON.parse(localStorage.getItem('ss_settings')||'{}');" +
        "    if(s.ip)document.getElementById('ipInput').value=s.ip;" +
        "    if(s.quality)document.getElementById('setQuality').value=s.quality;" +
        "    if(s.fps)document.getElementById('setFPS').value=s.fps;" +
        "    if(s.showkb===false)document.getElementById('setKB').checked=false;" +
        "    if(s.showgp)document.getElementById('setGP').checked=true;" +
        "    if(s.password)document.getElementById('setPass').value=s.password;" +
        "  }catch(e){}" +
        "  loadHistory();" +
        "})();" +

        "function saveSetting(k,v){" +
        "  var s=JSON.parse(localStorage.getItem('ss_settings')||'{}');" +
        "  s[k]=v;localStorage.setItem('ss_settings',JSON.stringify(s));" +
        "}" +

        "function openSidebar(){document.getElementById('sidebar').classList.add('open');document.getElementById('sidebarOverlay').classList.add('active');}" +
        "function closeSidebar(){document.getElementById('sidebar').classList.remove('open');document.getElementById('sidebarOverlay').classList.remove('active');}" +

        "function showPage(id){closeSidebar();document.getElementById(id).classList.add('active');currentPage=id;}" +
        "function hidePage(id){document.getElementById(id).classList.remove('active');currentPage='';}" +
        "function showSettings(){showPage('settingsPage');}" +
        "function showClipboard(){showPage('clipboardPage');}" +
        "function showHistory(){loadHistory();showPage('historyPage');}" +
        "function showTutorial(){showPage('tutorialPage');}" +
        "function showAbout(){showPage('aboutPage');}" +

        "function goHome(){" +
        "  if(currentPage){hidePage(currentPage);return;}" +
        "  if(isConnected){" +
        "    if(ws){ws.close();ws=null;}" +
        "    isConnected=false;" +
        "    document.getElementById('connectCard').style.display='block';" +
        "    document.getElementById('infoCard').style.display='block';" +
        "    document.getElementById('screen').style.display='none';" +
        "    document.getElementById('screen').classList.remove('active');" +
        "    document.getElementById('qwerty').classList.remove('active');" +
        "    document.getElementById('gamepad').classList.remove('active');" +
        "    setBtn('Connect','btn-connect');setStatus('Offline','offline');" +
        "  }" +
        "}" +

        "function toggleConnection(){" +
        "  var btn=document.getElementById('toggleBtn');" +
        "  if(isConnected){" +
        "    if(ws)ws.close();" +
        "    ws=null;isConnected=false;" +
        "    document.getElementById('connectCard').style.display='block';" +
        "    document.getElementById('infoCard').style.display='block';" +
        "    document.getElementById('screen').style.display='none';" +
        "    document.getElementById('screen').classList.remove('active');" +
        "    document.getElementById('qwerty').classList.remove('active');" +
        "    document.getElementById('gamepad').classList.remove('active');" +
        "    setBtn('Connect','btn-connect');setStatus('Offline','offline');" +
        "  }else{" +
        "    var ip=document.getElementById('ipInput').value.trim();" +
        "    if(!ip){alert('Enter IP address');return;}" +
        "    if(ip.indexOf(':')===-1)ip+=':8765';" +
        "    var url='ws://'+ip;" +
        "    var pass=document.getElementById('passInput').value;" +
        "    saveSetting('ip',ip);" +
        "    setStatus('Connecting...','connecting');setBtn('Connecting...','btn-connecting');" +
        "    try{" +
        "      ws=new WebSocket(url);ws.binaryType='arraybuffer';" +
        "      ws.onopen=function(){" +
        "        if(pass)ws.send(JSON.stringify({type:'auth',password:pass}));" +
        "        isConnected=true;" +
        "        setStatus('Connected','online');setBtn('Disconnect','btn-disconnect');" +
        "        document.getElementById('connectCard').style.display='none';" +
        "        document.getElementById('infoCard').style.display='none';" +
        "        var sc=document.getElementById('screen');sc.classList.add('active');sc.style.display='flex';sc.style.flexDirection='column';sc.style.flex='1';" +
        "        if(document.getElementById('setKB').checked)document.getElementById('qwerty').classList.add('active');" +
        "        if(document.getElementById('setGP').checked)document.getElementById('gamepad').classList.add('active');" +
        "        saveHistory(ip);" +
        "      };" +
        "      ws.onmessage=function(e){" +
        "        if(e.data instanceof ArrayBuffer){" +
        "          var blob=new Blob([e.data],{type:'image/jpeg'});" +
        "          var url2=URL.createObjectURL(blob);" +
        "          var img=document.getElementById('screenImg');" +
        "          if(img.src)URL.revokeObjectURL(img.src);img.src=url2;" +
        "        }else{" +
        "          try{var m=JSON.parse(e.data);if(m.type==='clipboard')document.getElementById('clipText').value=m.text;}catch(ex){}" +
        "        }" +
        "      };" +
        "      ws.onclose=function(){" +
        "        isConnected=false;setBtn('Connect','btn-connect');setStatus('Offline','offline');" +
        "        document.getElementById('connectCard').style.display='block';" +
        "        document.getElementById('infoCard').style.display='block';" +
        "        document.getElementById('screen').style.display='none';" +
        "        document.getElementById('screen').classList.remove('active');" +
        "        document.getElementById('qwerty').classList.remove('active');" +
        "        document.getElementById('gamepad').classList.remove('active');" +
        "      };" +
        "      ws.onerror=function(){setBtn('Connect','btn-connect');setStatus('Error','offline');};" +
        "    }catch(ex){setBtn('Connect','btn-connect');setStatus('Error','offline');}" +
        "  }" +
        "}" +

        "function setBtn(t,c){var b=document.getElementById('toggleBtn');b.textContent=t;b.className=c;}" +
        "function setStatus(t,c){document.getElementById('status').textContent=t;document.getElementById('status').className=c;}" +

        "function sendTouchToServer(type,x,y){if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:type,x:x,y:y}));}" +
        "function sendKey(e,key){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'key',key:key}));}" +
        "function sendChar(e,ch){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'char',char:ch}));}" +

        "function sendClipboard(){" +
        "  var t=document.getElementById('clipText').value;" +
        "  if(ws&&ws.readyState===WebSocket.OPEN){ws.send(JSON.stringify({type:'clipboard',text:t}));document.getElementById('clipStatus').textContent='Sent!';}" +
        "  else document.getElementById('clipStatus').textContent='Not connected';" +
        "}" +
        "function getClipboard(){" +
        "  if(ws&&ws.readyState===WebSocket.OPEN){ws.send(JSON.stringify({type:'get_clipboard'}));document.getElementById('clipStatus').textContent='Requesting...';}" +
        "  else document.getElementById('clipStatus').textContent='Not connected';" +
        "}" +

        "function loadHistory(){" +
        "  var h=JSON.parse(localStorage.getItem('ss_history')||'[]');" +
        "  var el=document.getElementById('historyList');" +
        "  if(h.length===0){el.innerHTML='<p style=color:#888>No connections yet</p>';return;}" +
        "  el.innerHTML='';" +
        "  h.forEach(function(ip){" +
        "    var d=document.createElement('div');d.className='history-item';" +
        "    var sp=document.createElement('span');sp.className='history-ip';sp.textContent=ip;" +
        "    sp.onclick=function(){document.getElementById('ipInput').value=ip;hidePage('historyPage');};" +
        "    var btn=document.createElement('button');btn.className='history-del';btn.textContent='X';" +
        "    btn.onclick=function(){delHistory(ip);};" +
        "    d.appendChild(sp);d.appendChild(btn);el.appendChild(d);" +
        "  });" +
        "}" +
        "function saveHistory(ip){" +
        "  var h=JSON.parse(localStorage.getItem('ss_history')||'[]');" +
        "  h=h.filter(function(i){return i!==ip;});h.unshift(ip);" +
        "  if(h.length>10)h=h.slice(0,10);" +
        "  localStorage.setItem('ss_history',JSON.stringify(h));" +
        "}" +
        "function delHistory(ip){" +
        "  var h=JSON.parse(localStorage.getItem('ss_history')||'[]');" +
        "  h=h.filter(function(i){return i!==ip;});" +
        "  localStorage.setItem('ss_history',JSON.stringify(h));loadHistory();" +
        "}" +
        "</script></body></html>";
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
