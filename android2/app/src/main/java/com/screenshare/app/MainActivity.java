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
        settings.setAllowFileAccess(true);
        
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
        "body{background:var(--bg);color:var(--fg);font-family:-apple-system,sans-serif;height:100vh;display:flex;flex-direction:column;overflow:hidden;transition:all .3s;}" +
        ":root{--bg:#0D1117;--card:#161B22;--border:#1a508b;--accent:#00D4FF;--text:#e0e0e0;--muted:#888;--input:#0f3460;--danger:#F87171;--success:#4ADE80;--warn:#FBBF24;}" +
        "body.light{--bg:#f5f5f5;--card:#ffffff;--border:#ddd;--accent:#0066CC;--text:#333;--muted:#666;--input:#e8e8e8;--danger:#dc3545;--success:#28a745;--warn:#ffc107;}" +

        /* Topbar */
        ".topbar{display:flex;align-items:center;padding:8px 12px;background:var(--card);border-bottom:1px solid var(--border);}" +
        ".hamburger{background:none;border:none;color:var(--accent);font-size:22px;padding:4px 8px;}" +
        ".topbar-title{flex:1;text-align:center;}" +
        ".topbar-title h1{color:var(--accent);font-size:16px;}" +
        ".topbar-title .sub{color:var(--muted);font-size:9px;}" +
        ".topbar-title .author{color:var(--accent);font-size:10px;}" +
        ".topbar-title .author a{color:var(--accent);text-decoration:none;}" +

        /* Sidebar */
        ".sidebar-overlay{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:998;display:none;}" +
        ".sidebar-overlay.active{display:block;}" +
        ".sidebar{position:fixed;top:0;left:-280px;width:280px;height:100%;background:var(--card);z-index:999;display:flex;flex-direction:column;transition:left .3s;box-shadow:4px 0 16px rgba(0,0,0,0.5);}" +
        ".sidebar.open{left:0;}" +
        ".sidebar-header{padding:20px 16px;border-bottom:1px solid var(--border);}" +
        ".sidebar-header h2{color:var(--accent);font-size:18px;}" +
        ".sidebar-header .author{color:var(--muted);font-size:11px;margin-top:4px;}" +
        ".sidebar-header .author a{color:var(--accent);text-decoration:none;}" +
        ".sidebar-item{display:flex;align-items:center;padding:14px 16px;border:none;background:none;color:var(--text);font-size:14px;text-align:left;width:100%;border-bottom:1px solid var(--border);opacity:0.8;}" +
        ".sidebar-item:active{background:var(--input);opacity:1;}" +
        ".sidebar-item .icon{width:24px;text-align:center;margin-right:12px;font-size:16px;}" +
        ".sidebar-footer{margin-top:auto;padding:12px 16px;border-top:1px solid var(--border);color:var(--muted);font-size:10px;text-align:center;}" +

        /* Cards & Inputs */
        ".card{background:var(--card);border:1px solid var(--border);border-radius:8px;padding:12px;margin:8px;}" +
        "input,select{width:100%;padding:10px;background:var(--input);border:1px solid var(--border);border-radius:6px;color:var(--text);font-size:14px;margin-bottom:8px;}" +
        "input:focus,select:focus{outline:none;border-color:var(--accent);}" +
        "#status{text-align:center;font-size:12px;padding:6px;}" +
        ".online{color:var(--success);} .offline{color:var(--danger);} .connecting{color:var(--warn);}" +

        /* Toggle button */
        "#toggleBtn{width:100%;padding:14px;border:none;border-radius:8px;font-size:16px;font-weight:bold;}" +
        ".btn-connect{background:var(--accent);color:#0D1117;}" +
        ".btn-disconnect{background:var(--danger);color:white;}" +
        ".btn-connecting{background:var(--warn);color:#0D1117;}" +

        /* Screen */
        "#screen{flex:1;background:#000;display:none;position:relative;overflow:hidden;}" +
        "#screen.active{display:flex;flex-direction:column;}" +
        "#screenImg{width:100%;flex:1;object-fit:contain;}" +
        ".touch-hint{text-align:center;color:var(--success);font-size:10px;padding:4px;}" +

        /* QWERTY Keyboard */
        ".qwerty{background:var(--card);border-top:1px solid var(--border);padding:4px;display:none;flex-direction:column;gap:3px;}" +
        ".qwerty.active{display:flex;}" +
        ".qwerty-row{display:flex;gap:3px;justify-content:center;}" +
        ".qkey{background:var(--input);border:1px solid var(--border);border-radius:4px;color:var(--text);padding:8px 4px;font-size:11px;text-align:center;min-width:28px;flex:1;max-width:36px;}" +
        ".qkey:active{background:var(--accent);color:#0D1117;}" +
        ".qkey.wide{min-width:44px;flex:1.5;}" +
        ".qkey.special{background:var(--border);font-size:10px;}" +
        ".qkey.space{flex:4;max-width:none;}" +
        ".qkey.num{background:var(--accent);color:#0D1117;font-weight:bold;}" +

        /* Special keys bar */
        ".special-bar{display:flex;gap:3px;justify-content:center;padding:2px;}" +
        ".skey{background:var(--border);border:1px solid var(--accent);border-radius:4px;color:var(--accent);padding:6px 8px;font-size:10px;min-width:40px;text-align:center;}" +
        ".skey:active{background:var(--accent);color:#0D1117;}" +

        /* Gamepad */
        ".gamepad{background:var(--card);border-top:1px solid var(--border);padding:12px;display:none;}" +
        ".gamepad.active{display:block;}" +
        ".dpad{display:flex;flex-direction:column;align-items:center;gap:4px;}" +
        ".dpad-row{display:flex;gap:4px;}" +
        ".dpad-btn{width:44px;height:44px;background:var(--input);border:1px solid var(--border);border-radius:8px;color:var(--text);font-size:18px;display:flex;align-items:center;justify-content:center;}" +
        ".dpad-btn:active{background:var(--accent);color:#0D1117;}" +
        ".ab-btns{display:flex;gap:12px;justify-content:center;margin-top:8px;}" +
        ".ab-btn{width:50px;height:50px;border-radius:25px;border:2px solid var(--accent);background:var(--input);color:var(--accent);font-size:16px;font-weight:bold;}" +
        ".ab-btn:active{background:var(--accent);color:#0D1117;}" +

        /* Pages */
        ".page{position:fixed;top:0;left:0;width:100%;height:100%;background:var(--bg);z-index:2000;display:none;flex-direction:column;overflow-y:auto;}" +
        ".page.active{display:flex;}" +
        ".page-header{background:var(--card);padding:14px;border-bottom:1px solid var(--border);display:flex;align-items:center;justify-content:space-between;}" +
        ".page-header h3{color:var(--accent);font-size:16px;}" +
        ".page-back{background:none;border:none;color:var(--accent);font-size:14px;}" +
        ".page-content{padding:16px;}" +
        ".page-content h4{color:var(--accent);margin:16px 0 8px 0;font-size:14px;}" +
        ".page-content p{color:var(--muted);font-size:12px;line-height:1.6;}" +

        /* Settings */
        ".setting-item{display:flex;justify-content:space-between;align-items:center;padding:12px 0;border-bottom:1px solid var(--border);}" +
        ".setting-label{color:var(--text);font-size:13px;}" +
        ".setting-desc{color:var(--muted);font-size:10px;margin-top:2px;}" +
        ".toggle{position:relative;width:44px;height:24px;cursor:pointer;}" +
        ".toggle input{opacity:0;width:0;height:0;}" +
        ".slider{position:absolute;top:0;left:0;right:0;bottom:0;background:var(--input);border-radius:12px;transition:.3s;}" +
        ".slider:before{content:'';position:absolute;width:18px;height:18px;left:3px;bottom:3px;background:white;border-radius:50%;transition:.3s;}" +
        ".toggle input:checked+.slider{background:var(--accent);}" +
        ".toggle input:checked+.slider:before{transform:translateX(20px);}" +

        /* History */
        ".history-item{display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--border);}" +
        ".history-ip{color:var(--accent);font-size:13px;font-family:monospace;}" +
        ".history-del{background:none;border:none;color:var(--danger);font-size:12px;}" +

        /* Clipboard */
        ".clip-area{width:100%;min-height:100px;background:var(--input);border:1px solid var(--border);border-radius:8px;color:var(--text);padding:10px;font-size:13px;resize:vertical;}" +
        ".clip-btn{padding:10px;border:none;border-radius:6px;font-size:13px;font-weight:bold;margin:4px;}" +
        ".clip-send{background:var(--accent);color:#0D1117;}" +
        ".clip-paste{background:var(--border);color:var(--text);}" +

        /* Info */
        ".info{font-size:10px;color:var(--muted);line-height:1.4;padding:0 8px 8px 8px;}" +
        ".info b{color:var(--accent);}" +

        /* Steps */
        ".step{background:var(--card);border:1px solid var(--border);border-radius:8px;padding:12px;margin:8px;}" +
        ".step-num{color:var(--accent);font-size:20px;font-weight:bold;}" +
        ".step-title{color:var(--text);font-size:13px;font-weight:bold;margin:4px 0;}" +
        ".step-desc{color:var(--muted);font-size:11px;line-height:1.4;}" +
        ".step-key{display:inline-block;background:var(--input);border:1px solid var(--border);border-radius:4px;padding:2px 6px;color:var(--accent);font-size:10px;margin:2px;}" +

        /* Theme toggle button */
        ".theme-btn{position:fixed;top:8px;right:8px;z-index:100;background:none;border:none;color:var(--accent);font-size:18px;padding:4px;}" +

        /* File transfer */
        ".file-list{max-height:200px;overflow-y:auto;}" +
        ".file-item{display:flex;justify-content:space-between;align-items:center;padding:8px;border-bottom:1px solid var(--border);}" +
        ".file-name{color:var(--text);font-size:12px;}" +
        ".file-size{color:var(--muted);font-size:10px;}" +
        ".file-dl{background:var(--accent);border:none;color:#0D1117;padding:4px 8px;border-radius:4px;font-size:10px;}" +
        "</style></head><body>" +

        /* Theme toggle */
        "<button class='theme-btn' id='themeBtn' onclick='toggleTheme()'>\u263E</button>" +

        /* Topbar */
        "<div class='topbar'>" +
        "  <button class='hamburger' onclick='openSidebar()'>\u2630</button>" +
        "  <div class='topbar-title'>" +
        "    <h1>ScreenShare</h1>" +
        "    <div class='sub'>Wireless Display + Touch + Keyboard</div>" +
        "    <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "  </div>" +
        "  <div style='width:40px;'></div>" +
        "</div>" +

        /* Sidebar */
        "<div class='sidebar-overlay' id='sidebarOverlay' onclick='closeSidebar()'></div>" +
        "<div class='sidebar' id='sidebar'>" +
        "  <div class='sidebar-header'>" +
        "    <h2>ScreenShare</h2>" +
        "    <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "  </div>" +
        "  <button class='sidebar-item' onclick='closeSidebar()'><span class='icon'>\u2302</span>Home</button>" +
        "  <button class='sidebar-item' onclick='showSettings()'><span class='icon'>\u2699</span>Settings</button>" +
        "  <button class='sidebar-item' onclick='showClipboard()'><span class='icon'>\u2398</span>Clipboard</button>" +
        "  <button class='sidebar-item' onclick='showFiles()'><span class='icon'>\u2630</span>Files</button>" +
        "  <button class='sidebar-item' onclick='showHistory()'><span class='icon'>\u21BB</span>History</button>" +
        "  <button class='sidebar-item' onclick='showTutorial()'><span class='icon'>\u24D8</span>Tutorial</button>" +
        "  <button class='sidebar-item' onclick='showAbout()'><span class='icon'>\u2139</span>About</button>" +
        "  <div class='sidebar-footer'>ScreenShare v2.0.0<br>github.com/jalalmx32/screen-share</div>" +
        "</div>" +

        /* Connection card */
        "<div class='card' id='connectCard'>" +
        "  <input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>" +
        "  <input type='password' id='passInput' placeholder='Password (optional)'>" +
        "  <button id='toggleBtn' class='btn-connect' onclick='toggleConnection()'>Connect</button>" +
        "</div>" +
        "<div id='status' class='offline'>Offline</div>" +

        /* Screen */
        "<div id='screen'>" +
        "  <img id='screenImg'>" +
        "  <div id='touchHint' class='touch-hint'>Touch to control</div>" +

        /* Special keys bar */
        "  <div class='special-bar' id='specialBar'>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"win\")'>Win</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"alt\")'>Alt</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl\")'>Ctrl</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"shift\")'>Shift</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"alt_tab\")'>Alt+Tab</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl_c\")'>C</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl_v\")'>V</div>" +
        "    <div class='skey' ontouchstart='sendKey(event,\"ctrl_z\")'>Z</div>" +
        "  </div>" +

        /* QWERTY Keyboard */
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
        "      <div class='qkey special' ontouchstart='sendChar(event,\",\")'>,</div>" +
        "      <div class='qkey space' ontouchstart='sendKey(event,\"space\")'>Space</div>" +
        "      <div class='qkey special' ontouchstart='sendChar(event,\".\")'>.</div>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"enter\")'>Enter</div>" +
        "    </div>" +
        "  </div>" +

        /* Gamepad */
        "  <div class='gamepad' id='gamepad'>" +
        "    <div style='display:flex;justify-content:space-between;align-items:center;'>" +
        "      <div class='dpad'>" +
        "        <div class='dpad-row'><div class='dpad-btn' ontouchstart='sendKey(event,\"up\")'>\u25B2</div></div>" +
        "        <div class='dpad-row'>" +
        "          <div class='dpad-btn' ontouchstart='sendKey(event,\"left\")'>\u25C0</div>" +
        "          <div class='dpad-btn' ontouchstart='sendChar(event,\"x\")' style='font-size:10px;'>X</div>" +
        "          <div class='dpad-btn' ontouchstart='sendKey(event,\"right\")'>\u25B6</div>" +
        "        </div>" +
        "        <div class='dpad-row'><div class='dpad-btn' ontouchstart='sendKey(event,\"down\")'>\u25BC</div></div>" +
        "      </div>" +
        "      <div class='ab-btns'>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"z\")'>A</div>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"x\")'>B</div>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"c\")'>Y</div>" +
        "        <div class='ab-btn' ontouchstart='sendChar(event,\"v\")'>X</div>" +
        "      </div>" +
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

        /* ===== PAGES ===== */

        /* Settings page */
        "<div class='page' id='settingsPage'>" +
        "  <div class='page-header'><h3>Settings</h3><button class='page-back' onclick='hideSettings()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>Display</h4>" +
        "    <div class='setting-item'><div><div class='setting-label'>Quality</div><div class='setting-desc'>Higher = more bandwidth</div></div>" +
        "      <select id='setQuality' onchange='saveSetting(\"quality\",this.value)' style='width:100px;'>" +
        "        <option value='1080p'>1080p</option><option value='720p' selected>720p</option><option value='480p'>480p</option><option value='360p'>360p</option>" +
        "      </select></div>" +
        "    <div class='setting-item'><div><div class='setting-label'>Frame Rate</div><div class='setting-desc'>FPS for streaming</div></div>" +
        "      <select id='setFPS' onchange='saveSetting(\"fps\",this.value)' style='width:100px;'>" +
        "        <option value='15'>15</option><option value='24'>24</option><option value='30' selected>30</option><option value='60'>60</option>" +
        "      </select></div>" +
        "    <div class='setting-item'><div><div class='setting-label'>Dark Mode</div><div class='setting-desc'>Toggle theme</div></div>" +
        "      <label class='toggle'><input type='checkbox' id='setDark' checked onchange='toggleThemeFromSetting(this.checked)'><span class='slider'></span></label></div>" +
        "    <div class='setting-item'><div><div class='setting-label'>Show Keyboard</div><div class='setting-desc'>Show QWERTY when connected</div></div>" +
        "      <label class='toggle'><input type='checkbox' id='setKB' checked onchange='saveSetting(\"showkb\",this.checked)'><span class='slider'></span></label></div>" +
        "    <div class='setting-item'><div><div class='setting-label'>Show Gamepad</div><div class='setting-desc'>Show game controller</div></div>" +
        "      <label class='toggle'><input type='checkbox' id='setGP' onchange='saveSetting(\"showgp\",this.checked)'><span class='slider'></span></label></div>" +
        "    <h4>Security</h4>" +
        "    <div class='setting-item'><div><div class='setting-label'>Password</div><div class='setting-desc'>Require password to connect</div></div></div>" +
        "    <input type='password' id='setPass' placeholder='Set password' onchange='saveSetting(\"password\",this.value)'>" +
        "    <h4>Auto Connect</h4>" +
        "    <div class='setting-item'><div><div class='setting-label'>Auto-connect</div><div class='setting-desc'>Connect on app start</div></div>" +
        "      <label class='toggle'><input type='checkbox' id='setAuto' onchange='saveSetting(\"autoconnect\",this.checked)'><span class='slider'></span></label></div>" +
        "  </div>" +
        "</div>" +

        /* Clipboard page */
        "<div class='page' id='clipboardPage'>" +
        "  <div class='page-header'><h3>Clipboard</h3><button class='page-back' onclick='hideClipboard()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <p style='margin-bottom:8px;'>Send text between devices</p>" +
        "    <textarea class='clip-area' id='clipText' placeholder='Type or paste text...'></textarea>" +
        "    <div style='margin-top:8px;'>" +
        "      <button class='clip-btn clip-send' onclick='sendClipboard()'>Send to PC</button>" +
        "      <button class='clip-btn clip-paste' onclick='getClipboard()'>Get from PC</button>" +
        "    </div>" +
        "    <div id='clipStatus' style='color:var(--success);font-size:11px;margin-top:8px;'></div>" +
        "  </div>" +
        "</div>" +

        /* Files page */
        "<div class='page' id='filesPage'>" +
        "  <div class='page-header'><h3>Files</h3><button class='page-back' onclick='hideFiles()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <button class='clip-btn clip-send' onclick='requestFileList()' style='width:100%;margin-bottom:12px;'>Refresh File List</button>" +
        "    <div class='file-list' id='fileList'>" +
        "      <p style='color:var(--muted);font-size:12px;'>Tap Refresh to see PC files</p>" +
        "    </div>" +
        "  </div>" +
        "</div>" +

        /* History page */
        "<div class='page' id='historyPage'>" +
        "  <div class='page-header'><h3>Connection History</h3><button class='page-back' onclick='hideHistory()'>Back</button></div>" +
        "  <div class='page-content' id='historyList'>" +
        "    <p style='color:var(--muted);font-size:12px;'>No connections yet</p>" +
        "  </div>" +
        "</div>" +

        /* Tutorial page */
        "<div class='page' id='tutorialPage'>" +
        "  <div class='page-header'><h3>Tutorial</h3><button class='page-back' onclick='hideTutorial()'>Back</button></div>" +
        "  <div class='step'><div class='step-num'>1</div><div class='step-title'>Enable Hotspot</div><div class='step-desc'>Turn on WiFi hotspot on your Android phone</div></div>" +
        "  <div class='step'><div class='step-num'>2</div><div class='step-title'>Connect PC</div><div class='step-desc'>Connect your Windows PC to the hotspot network</div></div>" +
        "  <div class='step'><div class='step-num'>3</div><div class='step-title'>Start Server</div><div class='step-desc'>Run ScreenShare on Windows and note the IP address</div></div>" +
        "  <div class='step'><div class='step-num'>4</div><div class='step-title'>Connect</div><div class='step-desc'>Enter the IP in this app and tap Connect</div></div>" +
        "  <div class='step'><div class='step-num'>5</div><div class='step-title'>Touch Control</div><div class='step-desc'>Tap to click, drag to move mouse, long press for right click</div></div>" +
        "  <div class='step'><div class='step-num'>6</div><div class='step-title'>Keyboard</div><div class='step-desc'>Use QWERTY or special keys: <span class='step-key'>Win</span> <span class='step-key'>Alt</span> <span class='step-key'>Ctrl</span> <span class='step-key'>Alt+Tab</span></div></div>" +
        "  <div class='step'><div class='step-num'>7</div><div class='step-title'>Clipboard</div><div class='step-desc'>Copy text between PC and phone via the Clipboard menu</div></div>" +
        "  <div class='step'><div class='step-num'>8</div><div class='step-title'>Gamepad</div><div class='step-desc'>Enable gamepad in Settings for gaming with D-pad and buttons</div></div>" +
        "  <div class='step'><div class='step-num'>9</div><div class='step-title'>Password</div><div class='step-desc'>Set a password in Settings for secure connections</div></div>" +
        "</div>" +

        /* About page */
        "<div class='page' id='aboutPage'>" +
        "  <div class='page-header'><h3>About</h3><button class='page-back' onclick='hideAbout()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>ScreenShare</h4>" +
        "    <p>Wireless Display + Touch Control + Keyboard</p>" +
        "    <p>A free alternative to Spacedesk</p>" +
        "    <h4>Features</h4>" +
        "    <p>- Real-time screen streaming</p>" +
        "    <p>- Touch control (tap, drag, long press)</p>" +
        "    <p>- Full QWERTY keyboard</p>" +
        "    <p>- Game controller (D-pad + buttons)</p>" +
        "    <p>- Clipboard sync between PC and phone</p>" +
        "    <p>- File browsing</p>" +
        "    <p>- Password protection</p>" +
        "    <p>- Dark/Light theme</p>" +
        "    <p>- Connection history</p>" +
        "    <h4>Developer</h4>" +
        "    <p>Jalal | @x16_96</p>" +
        "    <p>github.com/jalalmx32/screen-share</p>" +
        "    <h4>Version</h4>" +
        "    <p>v2.0.0</p>" +
        "  </div>" +
        "</div>" +

        "<script>" +
        "var ws=null;var isConnected=false;var isDark=true;" +

        /* Init from localStorage */
        "(function(){" +
        "  try{" +
        "    var s=JSON.parse(localStorage.getItem('ss_settings')||'{}');" +
        "    if(s.ip)document.getElementById('ipInput').value=s.ip;" +
        "    if(s.quality)document.getElementById('setQuality').value=s.quality;" +
        "    if(s.fps)document.getElementById('setFPS').value=s.fps;" +
        "    if(s.dark===false){isDark=false;document.body.classList.add('light');document.getElementById('setDark').checked=false;document.getElementById('themeBtn').textContent='\u2600';}" +
        "    if(s.showkb===false)document.getElementById('setKB').checked=false;" +
        "    if(s.showgp)document.getElementById('setGP').checked=true;" +
        "    if(s.password)document.getElementById('setPass').value=s.password;" +
        "    if(s.passreq)document.getElementById('passInput').value=s.password;" +
        "  }catch(e){}" +
        "  loadHistory();" +
        "})();" +

        "function saveSetting(k,v){" +
        "  var s=JSON.parse(localStorage.getItem('ss_settings')||'{}');" +
        "  s[k]=v;localStorage.setItem('ss_settings',JSON.stringify(s));" +
        "}" +

        /* Theme */
        "function toggleTheme(){" +
        "  isDark=!isDark;" +
        "  document.body.classList.toggle('light',!isDark);" +
        "  document.getElementById('themeBtn').textContent=isDark?'\u263E':'\u2600';" +
        "  document.getElementById('setDark').checked=isDark;" +
        "  saveSetting('dark',isDark);" +
        "}" +
        "function toggleThemeFromSetting(dark){" +
        "  isDark=dark;" +
        "  document.body.classList.toggle('light',!dark);" +
        "  document.getElementById('themeBtn').textContent=dark?'\u263E':'\u2600';" +
        "  saveSetting('dark',dark);" +
        "}" +

        /* Sidebar */
        "function openSidebar(){document.getElementById('sidebar').classList.add('open');document.getElementById('sidebarOverlay').classList.add('active');}" +
        "function closeSidebar(){document.getElementById('sidebar').classList.remove('open');document.getElementById('sidebarOverlay').classList.remove('active');}" +

        /* Pages */
        "function showSettings(){closeSidebar();loadSettings();document.getElementById('settingsPage').classList.add('active');}" +
        "function hideSettings(){document.getElementById('settingsPage').classList.remove('active');}" +
        "function showClipboard(){closeSidebar();document.getElementById('clipboardPage').classList.add('active');}" +
        "function hideClipboard(){document.getElementById('clipboardPage').classList.remove('active');}" +
        "function showFiles(){closeSidebar();document.getElementById('filesPage').classList.add('active');}" +
        "function hideFiles(){document.getElementById('filesPage').classList.remove('active');}" +
        "function showHistory(){closeSidebar();loadHistory();document.getElementById('historyPage').classList.add('active');}" +
        "function hideHistory(){document.getElementById('historyPage').classList.remove('active');}" +
        "function showTutorial(){closeSidebar();document.getElementById('tutorialPage').classList.add('active');}" +
        "function hideTutorial(){document.getElementById('tutorialPage').classList.remove('active');}" +
        "function showAbout(){closeSidebar();document.getElementById('aboutPage').classList.add('active');}" +
        "function hideAbout(){document.getElementById('aboutPage').classList.remove('active');}" +

        "function loadSettings(){" +
        "  var s=JSON.parse(localStorage.getItem('ss_settings')||'{}');" +
        "  if(s.quality)document.getElementById('setQuality').value=s.quality;" +
        "  if(s.fps)document.getElementById('setFPS').value=s.fps;" +
        "}" +

        /* Connection */
        "function toggleConnection(){if(isConnected)disconnect();else connect();}" +
        "function connect(){" +
        "  var ip=document.getElementById('ipInput').value.trim();" +
        "  if(!ip){alert('Enter IP');return;}" +
        "  if(!ip.startsWith('ws://'))ip='ws://'+ip;" +
        "  var pass=document.getElementById('passInput').value;" +
        "  saveSetting('ip',ip);" +
        "  setStatus('Connecting...','connecting');setBtn('Connecting...','btn-connecting');" +
        "  try{" +
        "    ws=new WebSocket(ip);ws.binaryType='arraybuffer';" +
        "    ws.onopen=function(){" +
        "      if(pass)ws.send(JSON.stringify({type:'auth',password:pass}));" +
        "      isConnected=true;" +
        "      setStatus('Connected','online');setBtn('Disconnect','btn-disconnect');" +
        "      document.getElementById('connectCard').style.display='none';" +
        "      document.getElementById('infoCard').style.display='none';" +
        "      var sc=document.getElementById('screen');sc.classList.add('active');sc.style.display='flex';sc.style.flexDirection='column';sc.style.flex='1';" +
        "      if(document.getElementById('setKB').checked)document.getElementById('qwerty').classList.add('active');" +
        "      if(document.getElementById('setGP').checked)document.getElementById('gamepad').classList.add('active');" +
        "      saveHistory(ip);" +
        "    };" +
        "    ws.onmessage=function(e){" +
        "      if(e.data instanceof ArrayBuffer){" +
        "        var blob=new Blob([e.data],{type:'image/jpeg'});" +
        "        var url=URL.createObjectURL(blob);" +
        "        var img=document.getElementById('screenImg');" +
        "        if(img.src)URL.revokeObjectURL(img.src);img.src=url;" +
        "      }else{" +
        "        try{" +
        "          var msg=JSON.parse(e.data);" +
        "          if(msg.type==='clipboard'){" +
        "            document.getElementById('clipText').value=msg.text;" +
        "            document.getElementById('clipStatus').textContent='Text received from PC';" +
        "          }else if(msg.type==='filelist'){" +
        "            renderFileList(msg.files);" +
        "          }else if(msg.type==='auth_fail'){" +
        "            alert('Wrong password!');disconnect();" +
        "          }" +
        "        }catch(ex){}" +
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
        "  document.getElementById('qwerty').classList.remove('active');" +
        "  document.getElementById('gamepad').classList.remove('active');" +
        "}" +
        "function setBtn(t,c){var b=document.getElementById('toggleBtn');b.textContent=t;b.className=c;}" +
        "function setStatus(t,c){document.getElementById('status').textContent=t;document.getElementById('status').className=c;}" +

        /* Touch & Keys */
        "function sendTouchToServer(type,x,y){if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:type,x:x,y:y}));}" +
        "function sendKey(e,key){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'key',key:key}));}" +
        "function sendChar(e,ch){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'char',char:ch}));}" +

        /* Clipboard */
        "function sendClipboard(){" +
        "  var text=document.getElementById('clipText').value;" +
        "  if(ws&&ws.readyState===WebSocket.OPEN){" +
        "    ws.send(JSON.stringify({type:'clipboard',text:text}));" +
        "    document.getElementById('clipStatus').textContent='Sent to PC';" +
        "  }else{document.getElementById('clipStatus').textContent='Not connected';}" +
        "}" +
        "function getClipboard(){" +
        "  if(ws&&ws.readyState===WebSocket.OPEN){" +
        "    ws.send(JSON.stringify({type:'get_clipboard'}));" +
        "    document.getElementById('clipStatus').textContent='Requesting...';" +
        "  }else{document.getElementById('clipStatus').textContent='Not connected';}" +
        "}" +

        /* Files */
        "function requestFileList(){" +
        "  if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'list_files'}));" +
        "}" +
        "function renderFileList(files){" +
        "  var el=document.getElementById('fileList');el.innerHTML='';" +
        "  if(!files||files.length===0){el.innerHTML='<p>No files</p>';return;}" +
        "  files.forEach(function(f){" +
        "    var d=document.createElement('div');d.className='file-item';" +
        "    d.innerHTML='<div><div class=file-name>'+f.name+'</div><div class=file-size>'+f.size+'</div></div><button class=file-dl onclick=\"downloadFile(\\x27'+f.name+'\\x27)\">Get</button>';" +
        "    el.appendChild(d);" +
        "  });" +
        "}" +
        "function downloadFile(name){" +
        "  if(ws&&ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'get_file',file:name}));" +
        "}" +

        /* History */
        "function saveHistory(ip){" +
        "  var h=JSON.parse(localStorage.getItem('ss_history')||'[]');" +
        "  h=h.filter(function(i){return i!==ip;});" +
        "  h.unshift(ip);" +
        "  if(h.length>10)h=h.slice(0,10);" +
        "  localStorage.setItem('ss_history',JSON.stringify(h));" +
        "}" +
        "function loadHistory(){" +
        "  var h=JSON.parse(localStorage.getItem('ss_history')||'[]');" +
        "  var el=document.getElementById('historyList');" +
        "  if(h.length===0){el.innerHTML='<p style=\"color:var(--muted)\">No connections yet</p>';return;}" +
        "  el.innerHTML='';" +
        "  h.forEach(function(ip){" +
        "    var d=document.createElement('div');d.className='history-item';" +
        "    var sp=document.createElement('span');sp.className='history-ip';sp.textContent=ip;" +
        "    sp.onclick=function(){document.getElementById('ipInput').value=ip;hideHistory();};" +
        "    var btn=document.createElement('button');btn.className='history-del';btn.textContent='X';" +
        "    btn.onclick=function(){delHistory(ip);};" +
        "    d.appendChild(sp);d.appendChild(btn);el.appendChild(d);" +
        "  });" +
        "}" +
        "function delHistory(ip){" +
        "  var h=JSON.parse(localStorage.getItem('ss_history')||'[]');" +
        "  h=h.filter(function(i){return i!==ip;});" +
        "  localStorage.setItem('ss_history',JSON.stringify(h));" +
        "  loadHistory();" +
        "}" +
        "</script></body></html>";
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
