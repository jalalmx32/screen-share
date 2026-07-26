package com.screenshare.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    
    private static final String TAG = "ScreenShare";
    private WebView webView;
    private Handler mainHandler;
    private boolean atHome = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        // True fullscreen
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
        
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Enable console logging
        webView.setWebChromeClient(new WebChromeClient());
        
        webView.setWebViewClient(new WebViewClient());
        
        webView.setBackgroundColor(0xFF0D1117);
        
        // JS interface for page tracking
        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void setPage(String page) {
                runOnUiThread(() -> {
                    atHome = "home".equals(page);
                    reapplyFullscreen();
                });
            }
            @android.webkit.JavascriptInterface
            public void log(String msg) {
                Log.d(TAG, "App: " + msg);
            }
        }, "Android");
        
        // Load the HTML directly
        webView.loadDataWithBaseURL(null, getHTML(), "text/html", "UTF-8", null);
        
        Log.d(TAG, "App started, HTML loaded");
    }
    
    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed, atHome=" + atHome);
        if (atHome) {
            new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
        } else {
            atHome = true;
            webView.evaluateJavascript("goHome()", null);
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Forward touch to WebView for button clicks, etc.
        // Only send server touch when on screen view (not at home)
        
        if (!atHome) {
            float x = event.getX();
            float y = event.getY();
            float vw = webView.getWidth();
            float vh = webView.getHeight();
            
            if (vw > 0 && vh > 0) {
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
                    final float finalNx = nx;
                    final float finalNy = ny;
                    mainHandler.post(() -> {
                        try {
                            webView.evaluateJavascript(js, null);
                        } catch (Exception e) {
                            Log.e(TAG, "Touch error: " + e.getMessage());
                        }
                    });
                }
            }
        }
        
        return super.dispatchTouchEvent(event);
    }
    
    private void reapplyFullscreen() {
        runOnUiThread(() -> {
            getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        });
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) reapplyFullscreen();
    }
    
    private String getHTML() {
        return "<!DOCTYPE html><html><head>" +
        "<meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>" +
        "<style>" +
        "*{margin:0;padding:0;box-sizing:border-box;}" +
        "html,body{background:#0D1117;color:#e0e0e0;font-family:-apple-system,sans-serif;height:100%;overflow:hidden;}" +

        /* TOPBAR */
        ".topbar{display:flex;align-items:center;padding:6px 10px;background:#161B22;border-bottom:1px solid #1a508b;height:44px;flex-shrink:0;}" +
        ".hamburger{background:none;border:none;color:#00D4FF;font-size:20px;padding:4px 6px;cursor:pointer;-webkit-tap-highlight-color:transparent;}" +
        ".topbar-title{flex:1;text-align:center;}" +
        ".topbar-title h1{color:#00D4FF;font-size:14px;}" +
        ".topbar-title .sub{color:#888;font-size:8px;}" +
        ".topbar-title .author{color:#00D4FF;font-size:9px;}" +
        ".topbar-title .author a{color:#00D4FF;text-decoration:none;}" +

        /* SIDEBAR */
        ".sidebar-overlay{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.5);z-index:998;display:none;}" +
        ".sidebar-overlay.active{display:block;}" +
        ".sidebar{position:fixed;top:0;left:-260px;width:260px;height:100%;background:#161B22;z-index:999;display:flex;flex-direction:column;transition:left .25s ease;box-shadow:4px 0 12px rgba(0,0,0,0.5);}" +
        ".sidebar.open{left:0;}" +
        ".sidebar-header{padding:16px;border-bottom:1px solid #1a508b;}" +
        ".sidebar-header h2{color:#00D4FF;font-size:16px;}" +
        ".sidebar-header .author{color:#888;font-size:10px;margin-top:3px;}" +
        ".sidebar-header .author a{color:#00D4FF;text-decoration:none;}" +
        ".sidebar-item{display:flex;align-items:center;padding:12px 16px;border:none;background:none;color:#e0e0e0;font-size:13px;text-align:left;width:100%;border-bottom:1px solid #1a508b33;cursor:pointer;-webkit-tap-highlight-color:transparent;}" +
        ".sidebar-item:active{background:#0f3460;}" +
        ".sidebar-footer{margin-top:auto;padding:10px 16px;border-top:1px solid #1a508b;color:#888;font-size:9px;text-align:center;}" +

        /* HOME */
        ".home{display:flex;flex-direction:column;height:100%;overflow-y:auto;-webkit-overflow-scrolling:touch;}" +
        ".card{background:#161B22;border:1px solid #1a508b;border-radius:8px;padding:10px;margin:8px;}" +
        "input,select{width:100%;padding:10px;background:#0f3460;border:1px solid #1a508b;border-radius:6px;color:white;font-size:14px;margin-bottom:8px;-webkit-appearance:none;}" +
        "input:focus{outline:none;border-color:#00D4FF;}" +
        "#status{text-align:center;font-size:12px;padding:6px;}" +
        ".online{color:#4ADE80;} .offline{color:#F87171;} .connecting{color:#FBBF24;}" +

        /* BIG CONNECT BUTTON */
        "#toggleBtn{width:100%;padding:16px;border:none;border-radius:10px;font-size:18px;font-weight:bold;cursor:pointer;-webkit-tap-highlight-color:transparent;transition:all .2s;}" +
        ".btn-connect{background:#00D4FF;color:#0D1117;}" +
        ".btn-disconnect{background:#F87171;color:white;}" +
        ".btn-connecting{background:#FBBF24;color:#0D1117;}" +

        /* SCAN */
        ".scan-section{margin:8px;}" +
        ".scan-title{color:#888;font-size:10px;margin-bottom:4px;}" +
        ".scan-item{background:#161B22;border:1px solid #1a508b;border-radius:6px;padding:8px;margin-bottom:4px;display:flex;justify-content:space-between;align-items:center;}" +
        ".scan-ip{color:#00D4FF;font-family:monospace;font-size:12px;}" +
        ".scan-name{color:#888;font-size:9px;}" +
        ".scan-connect{background:#00D4FF;border:none;color:#0D1117;padding:6px 12px;border-radius:4px;font-size:11px;font-weight:bold;cursor:pointer;}" +
        ".scan-refresh{background:none;border:1px solid #1a508b;color:#00D4FF;padding:5px 12px;border-radius:4px;font-size:10px;cursor:pointer;margin-top:4px;}" +
        ".scan-status{color:#FBBF24;font-size:10px;}" +

        ".info{font-size:9px;color:#888;line-height:1.3;}" +
        ".info b{color:#00D4FF;}" +

        /* FOOTER */
        ".footer{text-align:center;padding:12px;margin-top:auto;}" +
        ".footer .ver{color:#888;font-size:10px;}" +
        ".footer .author{margin-top:6px;display:flex;align-items:center;justify-content:center;gap:6px;}" +
        ".footer .name{color:#e0e0e0;font-size:12px;}" +
        ".footer .name b{color:#00D4FF;}" +
        ".footer .tg{display:inline-flex;align-items:center;gap:3px;text-decoration:none;}" +

        /* SCREEN */
        "#screenView{display:none;flex:1;flex-direction:column;overflow:hidden;position:relative;background:#000;}" +
        "#screenContainer{flex:1;overflow:hidden;position:relative;touch-action:none;}" +
        "#screenImg{position:absolute;top:0;left:0;transform-origin:0 0;will-change:transform;touch-action:none;user-select:none;-webkit-user-drag:none;max-width:none;}" +

        /* CONTROLS BAR */
        ".controls-bar{background:#161B22;border-top:1px solid #1a508b;padding:4px 6px;display:none;flex-wrap:wrap;gap:3px;justify-content:center;flex-shrink:0;}" +
        ".controls-bar.active{display:flex;}" +
        ".ctrl-key{background:#0f3460;border:1px solid #1a508b;border-radius:3px;color:white;padding:5px 6px;font-size:9px;text-align:center;min-width:30px;cursor:pointer;user-select:none;}" +
        ".ctrl-key:active{background:#00D4FF;color:#0D1117;}" +
        ".ctrl-key.special{background:#1a508b;border-color:#00D4FF;color:#00D4FF;}" +

        /* QWERTY */
        ".qwerty{background:#161B22;border-top:1px solid #1a508b;padding:3px;display:none;flex-direction:column;gap:2px;flex-shrink:0;}" +
        ".qwerty.active{display:flex;}" +
        ".qwerty-row{display:flex;gap:2px;justify-content:center;}" +
        ".qkey{background:#0f3460;border:1px solid #1a508b;border-radius:3px;color:white;padding:6px 2px;font-size:10px;text-align:center;min-width:24px;flex:1;max-width:32px;cursor:pointer;user-select:none;}" +
        ".qkey:active{background:#00D4FF;color:#0D1117;}" +
        ".qkey.wide{min-width:36px;flex:1.5;}" +
        ".qkey.special{background:#1a508b;font-size:9px;}" +
        ".qkey.space{flex:4;max-width:none;}" +
        ".qkey.num{background:#00D4FF;color:#0D1117;font-weight:bold;}" +

        /* PAGES */
        ".page{position:fixed;top:0;left:0;width:100%;height:100%;background:#0D1117;z-index:2000;display:none;flex-direction:column;overflow-y:auto;}" +
        ".page.active{display:flex;}" +
        ".page-header{background:#161B22;padding:12px;border-bottom:1px solid #1a508b;display:flex;align-items:center;justify-content:space-between;}" +
        ".page-header h3{color:#00D4FF;font-size:14px;}" +
        ".page-back{background:none;border:none;color:#00D4FF;font-size:13px;cursor:pointer;}" +
        ".page-content{padding:12px;}" +
        ".page-content h4{color:#00D4FF;margin:12px 0 6px 0;font-size:13px;}" +
        ".page-content p{color:#888;font-size:11px;line-height:1.5;}" +

        ".setting-item{display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #1a508b33;}" +
        ".setting-label{color:white;font-size:12px;}" +

        ".clip-area{width:100%;min-height:80px;background:#0f3460;border:1px solid #1a508b;border-radius:6px;color:white;padding:8px;font-size:12px;}" +
        ".clip-btn{padding:8px;border:none;border-radius:5px;font-size:12px;font-weight:bold;margin:3px;cursor:pointer;}" +
        ".clip-send{background:#00D4FF;color:#0D1117;}" +
        ".clip-paste{background:#1a508b;color:white;}" +

        ".history-item{display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid #1a508b33;}" +
        ".history-ip{color:#00D4FF;font-size:12px;font-family:monospace;cursor:pointer;}" +
        ".history-del{background:none;border:none;color:#F87171;font-size:11px;cursor:pointer;}" +

        ".step{background:#161B22;border:1px solid #1a508b;border-radius:6px;padding:10px;margin:6px;}" +
        ".step-num{color:#00D4FF;font-size:18px;font-weight:bold;}" +
        ".step-title{color:white;font-size:12px;font-weight:bold;margin:3px 0;}" +
        ".step-desc{color:#888;font-size:10px;line-height:1.3;}" +

        /* DEBUG LOG */
        "#debugLog{position:fixed;bottom:0;left:0;right:0;background:rgba(0,0,0,0.8);color:#4ADE80;font-size:8px;padding:4px;z-index:9999;display:none;font-family:monospace;max-height:60px;overflow-y:auto;}" +
        "</style></head><body>" +

        /* TOPBAR */
        "<div class='topbar'>" +
        "  <button class='hamburger' onclick='openSidebar()'>\u2630</button>" +
        "  <div class='topbar-title'>" +
        "    <h1>ScreenShare</h1>" +
        "    <div class='sub'>Wireless Display + Touch + Keyboard</div>" +
        "    <div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div>" +
        "  </div>" +
        "  <div style='width:32px;'></div>" +
        "</div>" +

        /* SIDEBAR */
        "<div class='sidebar-overlay' id='sidebarOverlay' onclick='closeSidebar()'></div>" +
        "<div class='sidebar' id='sidebar'>" +
        "  <div class='sidebar-header'><h2>ScreenShare</h2><div class='author'>by Jalal | <a href='https://t.me/x16_96'>@x16_96</a></div></div>" +
        "  <button class='sidebar-item' onclick='goHome()'>Home</button>" +
        "  <button class='sidebar-item' onclick='showPage(\"settingsPage\")'>Settings</button>" +
        "  <button class='sidebar-item' onclick='showPage(\"clipboardPage\")'>Clipboard</button>" +
        "  <button class='sidebar-item' onclick='showPage(\"historyPage\")'>History</button>" +
        "  <button class='sidebar-item' onclick='showPage(\"tutorialPage\")'>Tutorial</button>" +
        "  <button class='sidebar-item' onclick='showPage(\"aboutPage\")'>About</button>" +
        "  <div class='sidebar-footer'>v2.8.0 | github.com/jalalmx32/screen-share</div>" +
        "</div>" +

        /* HOME */
        "<div id='homeView' class='home'>" +
        "  <div class='card'>" +
        "    <input type='text' id='ipInput' placeholder='PC IP: 192.168.43.1:8765'>" +
        "    <input type='password' id='passInput' placeholder='Password (optional)'>" +
        "    <button id='toggleBtn' class='btn-connect' ontouchstart='toggleConnection(event)' onclick='toggleConnection(event)'>Connect</button>" +
        "  </div>" +
        "  <div id='status' class='offline'>Offline</div>" +
        "  <div class='scan-section' id='scanSection'>" +
        "    <div class='scan-title'>Scanning for servers...</div>" +
        "    <div id='scanList'></div>" +
        "    <div class='scan-status' id='scanStatus'></div>" +
        "    <button class='scan-refresh' ontouchstart='startScan(event)' onclick='startScan(event)'>Refresh</button>" +
        "  </div>" +
        "  <div class='card'><div class='info'>" +
        "    <b>1.</b> Enable hotspot on Android<br>" +
        "    <b>2.</b> Connect PC to hotspot<br>" +
        "    <b>3.</b> Start ScreenShare on PC<br>" +
        "    <b>4.</b> Enter IP or tap detected server<br>" +
        "    <b>5.</b> Touch to control PC!" +
        "  </div></div>" +

        /* AUTHOR FOOTER */
        "  <div class='footer'>" +
        "    <div class='ver'>ScreenShare v2.8.0</div>" +
        "    <div class='author'>" +
        "      <span class='name'>by <b>Jalal</b></span>" +
        "      <a class='tg' href='https://t.me/x16_96'>" +
        "        <svg width='16' height='16' viewBox='0 0 24 24' fill='#00D4FF'><path d='M12 0C5.37 0 0 5.37 0 12s5.37 12 12 12 12-5.37 12-12S18.63 0 12 0zm5.95 7.47l-1.97 9.28c-.15.67-.54.83-1.09.52l-3.02-2.22-1.46 1.4c-.16.16-.3.3-.61.3l.22-3.05 5.55-5.01c.24-.22-.05-.33-.37-.14L8.68 13.3l-2.96-.92c-.64-.2-.66-.64.13-.95l11.53-4.45c.53-.19 1 .13.83.95l-.23.09z'/></svg>" +
        "        <span style='color:#00D4FF;font-size:11px;'>@x16_96</span>" +
        "      </a>" +
        "    </div>" +
        "  </div>" +
        "</div>" +

        /* SCREEN */
        "<div id='screenView'>" +
        "  <div id='screenContainer'>" +
        "    <img id='screenImg' draggable='false'>" +
        "  </div>" +
        "  <div class='controls-bar active' id='ctrlBar'>" +
        "    <div class='ctrl-key special' ontouchstart='sendKey(event,\"win\")'>Win</div>" +
        "    <div class='ctrl-key special' ontouchstart='sendKey(event,\"alt\")'>Alt</div>" +
        "    <div class='ctrl-key special' ontouchstart='sendKey(event,\"ctrl\")'>Ctrl</div>" +
        "    <div class='ctrl-key special' ontouchstart='sendKey(event,\"shift\")'>Shift</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"alt_tab\")'>Alt+Tab</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"ctrl_c\")'>C</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"ctrl_v\")'>V</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"up\")'>\u2191</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"down\")'>\u2193</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"left\")'>\u2190</div>" +
        "    <div class='ctrl-key' ontouchstart='sendKey(event,\"right\")'>\u2192</div>" +
        "  </div>" +
        "  <div class='qwerty' id='qwerty'>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey num' ontouchstart='sendChar(event,\"1\")'>1</div><div class='qkey num' ontouchstart='sendChar(event,\"2\")'>2</div><div class='qkey num' ontouchstart='sendChar(event,\"3\")'>3</div><div class='qkey num' ontouchstart='sendChar(event,\"4\")'>4</div><div class='qkey num' ontouchstart='sendChar(event,\"5\")'>5</div><div class='qkey num' ontouchstart='sendChar(event,\"6\")'>6</div><div class='qkey num' ontouchstart='sendChar(event,\"7\")'>7</div><div class='qkey num' ontouchstart='sendChar(event,\"8\")'>8</div><div class='qkey num' ontouchstart='sendChar(event,\"9\")'>9</div><div class='qkey num' ontouchstart='sendChar(event,\"0\")'>0</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"q\")'>Q</div><div class='qkey' ontouchstart='sendChar(event,\"w\")'>W</div><div class='qkey' ontouchstart='sendChar(event,\"e\")'>E</div><div class='qkey' ontouchstart='sendChar(event,\"r\")'>R</div><div class='qkey' ontouchstart='sendChar(event,\"t\")'>T</div><div class='qkey' ontouchstart='sendChar(event,\"y\")'>Y</div><div class='qkey' ontouchstart='sendChar(event,\"u\")'>U</div><div class='qkey' ontouchstart='sendChar(event,\"i\")'>I</div><div class='qkey' ontouchstart='sendChar(event,\"o\")'>O</div><div class='qkey' ontouchstart='sendChar(event,\"p\")'>P</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey' ontouchstart='sendChar(event,\"a\")'>A</div><div class='qkey' ontouchstart='sendChar(event,\"s\")'>S</div><div class='qkey' ontouchstart='sendChar(event,\"d\")'>D</div><div class='qkey' ontouchstart='sendChar(event,\"f\")'>F</div><div class='qkey' ontouchstart='sendChar(event,\"g\")'>G</div><div class='qkey' ontouchstart='sendChar(event,\"h\")'>H</div><div class='qkey' ontouchstart='sendChar(event,\"j\")'>J</div><div class='qkey' ontouchstart='sendChar(event,\"k\")'>K</div><div class='qkey' ontouchstart='sendChar(event,\"l\")'>L</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"shift\")'>Shift</div><div class='qkey' ontouchstart='sendChar(event,\"z\")'>Z</div><div class='qkey' ontouchstart='sendChar(event,\"x\")'>X</div><div class='qkey' ontouchstart='sendChar(event,\"c\")'>C</div><div class='qkey' ontouchstart='sendChar(event,\"v\")'>V</div><div class='qkey' ontouchstart='sendChar(event,\"b\")'>B</div><div class='qkey' ontouchstart='sendChar(event,\"n\")'>N</div><div class='qkey' ontouchstart='sendChar(event,\"m\")'>M</div><div class='qkey wide special' ontouchstart='sendKey(event,\"backspace\")'>BS</div>" +
        "    </div>" +
        "    <div class='qwerty-row'>" +
        "      <div class='qkey wide special' ontouchstart='sendKey(event,\"tab\")'>Tab</div><div class='qkey space' ontouchstart='sendKey(event,\"space\")'>Space</div><div class='qkey wide special' ontouchstart='sendKey(event,\"enter\")'>Enter</div>" +
        "    </div>" +
        "  </div>" +
        "</div>" +

        /* PAGES */
        "<div class='page' id='settingsPage'>" +
        "  <div class='page-header'><h3>Settings</h3><button class='page-back' onclick='goHome()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>Display</h4>" +
        "    <div class='setting-item'><div class='setting-label'>Quality</div>" +
        "      <select id='setQuality' style='width:90px;'><option value='1080p'>1080p</option><option value='720p' selected>720p</option><option value='480p'>480p</option></select></div>" +
        "    <div class='setting-item'><div class='setting-label'>Frame Rate</div>" +
        "      <select id='setFPS' style='width:90px;'><option value='15'>15</option><option value='30' selected>30</option><option value='60'>60</option></select></div>" +
        "  </div>" +
        "</div>" +
        "<div class='page' id='clipboardPage'>" +
        "  <div class='page-header'><h3>Clipboard</h3><button class='page-back' onclick='goHome()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <textarea class='clip-area' id='clipText' placeholder='Type or paste text...'></textarea>" +
        "    <div style='margin-top:6px;'>" +
        "      <button class='clip-btn clip-send' onclick='sendClipboard()'>Send to PC</button>" +
        "      <button class='clip-btn clip-paste' onclick='getClipboard()'>Get from PC</button>" +
        "    </div>" +
        "    <div id='clipStatus' style='color:#4ADE80;font-size:10px;margin-top:6px;'></div>" +
        "  </div>" +
        "</div>" +
        "<div class='page' id='historyPage'>" +
        "  <div class='page-header'><h3>History</h3><button class='page-back' onclick='goHome()'>Back</button></div>" +
        "  <div class='page-content' id='historyList'><p style='color:#888'>No connections yet</p></div>" +
        "</div>" +
        "<div class='page' id='tutorialPage'>" +
        "  <div class='page-header'><h3>Tutorial</h3><button class='page-back' onclick='goHome()'>Back</button></div>" +
        "  <div class='step'><div class='step-num'>1</div><div class='step-title'>Enable Hotspot</div><div class='step-desc'>Turn on WiFi hotspot</div></div>" +
        "  <div class='step'><div class='step-num'>2</div><div class='step-title'>Connect PC</div><div class='step-desc'>Connect PC to hotspot</div></div>" +
        "  <div class='step'><div class='step-num'>3</div><div class='step-title'>Start Server</div><div class='step-desc'>Run ScreenShare on Windows</div></div>" +
        "  <div class='step'><div class='step-num'>4</div><div class='step-title'>Auto Detect</div><div class='step-desc'>App auto-detects servers</div></div>" +
        "  <div class='step'><div class='step-num'>5</div><div class='step-title'>Touch & Pinch</div><div class='step-desc'>Tap to click, pinch to zoom, drag to pan</div></div>" +
        "  <div class='step'><div class='step-num'>6</div><div class='step-title'>Keyboard</div><div class='step-desc'>QWERTY + special keys</div></div>" +
        "</div>" +
        "<div class='page' id='aboutPage'>" +
        "  <div class='page-header'><h3>About</h3><button class='page-back' onclick='goHome()'>Back</button></div>" +
        "  <div class='page-content'>" +
        "    <h4>ScreenShare v2.8.0</h4><p>Wireless Display + Touch Control</p><p>A free alternative to Spacedesk</p>" +
        "    <h4>Developer</h4><p>Jalal | @x16_96</p><p>github.com/jalalmx32/screen-share</p>" +
        "  </div>" +
        "</div>" +

        /* DEBUG */
        "<div id='debugLog'></div>" +

        "<script>" +
        "var ws=null,isConnected=false,atHome=true;" +
        "function dbg(m){var d=document.getElementById('debugLog');d.style.display='block';d.innerHTML=m+'<br>';d.scrollTop=d.scrollHeight;console.log(m);}" +

        /* INIT */
        "try{" +
        "  var s=JSON.parse(localStorage.getItem('ss')||'{}');" +
        "  if(s.ip)document.getElementById('ipInput').value=s.ip;" +
        "  if(s.pass)document.getElementById('passInput').value=s.pass;" +
        "  dbg('Ready. IP: '+(s.ip||'none'));" +
        "}catch(e){dbg('Init error: '+e.message);}" +
        "function save(k,v){var s=JSON.parse(localStorage.getItem('ss')||'{}');s[k]=v;localStorage.setItem('ss',JSON.stringify(s));}" +

        /* SIDEBAR */
        "function openSidebar(){document.getElementById('sidebar').classList.add('open');document.getElementById('sidebarOverlay').classList.add('active');}" +
        "function closeSidebar(){document.getElementById('sidebar').classList.remove('open');document.getElementById('sidebarOverlay').classList.remove('active');}" +

        /* GO HOME */
        "function goHome(){" +
        "  closeSidebar();" +
        "  document.querySelectorAll('.page').forEach(function(p){p.classList.remove('active');});" +
        "  document.getElementById('homeView').style.display='flex';" +
        "  document.getElementById('screenView').style.display='none';" +
        "  resetView();" +
        "  try{Android.setPage('home');}catch(e){}" +
        "  if(isConnected){if(ws){ws.close();ws=null;}isConnected=false;setBtn('Connect','btn-connect');setStatus('Offline','offline');}" +
        "  atHome=true;" +
        "  dbg('Home');}" +

        "function showPage(id){closeSidebar();document.getElementById(id).classList.add('active');try{Android.setPage('page');}catch(e){}atHome=false;dbg('Page: '+id);}" +

        /* PINCH ZOOM */
        "var zoom=1,panX=0,panY=0;" +
        "var img=null,container=null;" +
        "(function(){" +
        "  img=document.getElementById('screenImg');" +
        "  container=document.getElementById('screenContainer');" +
        "  if(!img||!container)return;" +
        "  var lastDist=0,lastX=0,lastY=0,pinch=false,drag=false;" +
        "  container.addEventListener('touchstart',function(e){" +
        "    if(e.touches.length===2){" +
        "      pinch=true;drag=false;" +
        "      var dx=e.touches[0].clientX-e.touches[1].clientX;" +
        "      var dy=e.touches[0].clientY-e.touches[1].clientY;" +
        "      lastDist=Math.sqrt(dx*dx+dy*dy);e.preventDefault();" +
        "    }else if(e.touches.length===1){" +
        "      drag=true;lastX=e.touches[0].clientX;lastY=e.touches[0].clientY;" +
        "    }" +
        "  },{passive:false});" +
        "  container.addEventListener('touchmove',function(e){" +
        "    if(pinch&&e.touches.length===2){" +
        "      var dx=e.touches[0].clientX-e.touches[1].clientX;" +
        "      var dy=e.touches[0].clientY-e.touches[1].clientY;" +
        "      var d=Math.sqrt(dx*dx+dy*dy);" +
        "      zoom=Math.max(0.5,Math.min(5,zoom*(d/lastDist)));lastDist=d;" +
        "      updateTransform();e.preventDefault();" +
        "    }else if(drag&&e.touches.length===1&&!pinch){" +
        "      panX+=e.touches[0].clientX-lastX;panY+=e.touches[0].clientY-lastY;" +
        "      lastX=e.touches[0].clientX;lastY=e.touches[0].clientY;" +
        "      updateTransform();e.preventDefault();" +
        "    }" +
        "  },{passive:false});" +
        "  container.addEventListener('touchend',function(e){" +
        "    if(e.touches.length<2)pinch=false;" +
        "    if(e.touches.length===0)drag=false;" +
        "  });" +
        "})();" +
        "function updateTransform(){if(img)img.style.transform='translate('+panX+'px,'+panY+'px) scale('+zoom+')';}" +
        "function resetView(){zoom=1;panX=0;panY=0;updateTransform();}" +

        /* SCAN */
        "function startScan(e){" +
        "  if(e)e.preventDefault();" +
        "  document.getElementById('scanStatus').textContent='Scanning...';document.getElementById('scanList').innerHTML='';" +
        "  var base='192.168.';var ip=document.getElementById('ipInput').value.trim();" +
        "  if(ip){var p=ip.split('.');if(p.length>=3)base=p[0]+'.'+p[1]+'.';}" +
        "  var hosts=[];for(var i=1;i<=15;i++)hosts.push(base+'1.'+i);hosts.push(base+'43.1');hosts.push(base+'0.1');" +
        "  var found=0,checked=0;" +
        "  dbg('Scanning '+hosts.length+' hosts...');" +
        "  hosts.forEach(function(h){" +
        "    try{" +
        "      var t=new WebSocket('ws://'+h+':8765');var host=h;" +
        "      var timer=setTimeout(function(){t.close();checked++;if(checked>=hosts.length){scanDone(found);dbg('Scan done: '+found+' found');}},1200);" +
        "      t.onopen=function(){clearTimeout(timer);found++;checked++;addScanItem(host);t.close();if(checked>=hosts.length){scanDone(found);dbg('Scan done: '+found+' found');}};" +
        "      t.onerror=function(){clearTimeout(timer);checked++;if(checked>=hosts.length){scanDone(found);dbg('Scan done: '+found+' found');}};" +
        "    }catch(e){checked++;}" +
        "  });" +
        "}" +
        "function scanDone(n){document.getElementById('scanStatus').textContent=n>0?'Found '+n+' server(s)':'No servers found';}" +
        "function addScanItem(ip){" +
        "  var el=document.getElementById('scanList');var d=document.createElement('div');d.className='scan-item';" +
        "  d.innerHTML='<div><div class=scan-ip>'+ip+'</div><div class=scan-name>ScreenShare Server</div></div>';" +
        "  var btn=document.createElement('button');btn.className='scan-connect';btn.textContent='Connect';" +
        "  btn.ontouchstart=function(e){e.preventDefault();document.getElementById('ipInput').value=ip+':8765';toggleConnection(e);};" +
        "  btn.onclick=function(e){e.preventDefault();document.getElementById('ipInput').value=ip+':8765';toggleConnection(e);};" +
        "  d.appendChild(btn);el.appendChild(d);" +
        "}" +

        /* CONNECTION - THE KEY FIX */
        "function toggleConnection(e){" +
        "  if(e)e.preventDefault();" +
        "  dbg('toggleConnection called');" +
        "  if(isConnected){" +
        "    dbg('Disconnecting...');" +
        "    if(ws){try{ws.close();}catch(x){}ws=null;}" +
        "    isConnected=false;atHome=true;" +
        "    document.getElementById('homeView').style.display='flex';" +
        "    document.getElementById('screenView').style.display='none';" +
        "    setBtn('Connect','btn-connect');setStatus('Offline','offline');" +
        "    resetView();try{Android.setPage('home');}catch(e){}" +
        "  }else{" +
        "    var ip='';" +
        "    try{ip=document.getElementById('ipInput').value.trim();}catch(x){dbg('ERR read input: '+x);}" +
        "    dbg('IP=['+ip+']');" +
        "    if(!ip){dbg('ERROR: No IP entered');return;}" +
        "    if(ip.indexOf(':')===-1)ip+=':8765';" +
        "    var pass='';" +
        "    try{pass=document.getElementById('passInput').value;}catch(x){}" +
        "    try{save('ip',ip);save('pass',pass);}catch(x){dbg('Save err: '+x);}" +
        "    setStatus('Connecting...','connecting');setBtn('Connecting...','btn-connecting');" +
        "    dbg('Connecting to ws://'+ip+'...');" +
        "    try{" +
        "      ws=new WebSocket('ws://'+ip);" +
        "      ws.binaryType='arraybuffer';" +
        "      dbg('WebSocket created, waiting...');" +
        "      ws.onopen=function(){" +
        "        dbg('WebSocket CONNECTED!');" +
        "        if(pass)ws.send(JSON.stringify({type:'auth',password:pass}));" +
        "        isConnected=true;atHome=false;" +
        "        setStatus('Connected!','online');setBtn('Disconnect','btn-disconnect');" +
        "        document.getElementById('homeView').style.display='none';" +
        "        document.getElementById('screenView').style.display='flex';" +
        "        resetView();" +
        "        try{Android.setPage('screen');}catch(e){}" +
        "        saveHistory(ip);dbg('Screen view shown');" +
        "      };" +
        "      ws.onmessage=function(e){" +
        "        if(e.data instanceof ArrayBuffer){" +
        "          var b=new Blob([e.data],{type:'image/jpeg'});" +
        "          var u=URL.createObjectURL(b);" +
        "          if(img.src)URL.revokeObjectURL(img.src);img.src=u;" +
        "        }else{" +
        "          dbg('MSG: '+e.data);" +
        "        }" +
        "      };" +
        "      ws.onclose=function(e){" +
        "        dbg('WebSocket CLOSED: '+e.code);" +
        "        goHome();" +
        "      };" +
        "      ws.onerror=function(e){" +
        "        dbg('WebSocket ERROR!');goHome();" +
        "      };" +
        "    }catch(ex){" +
        "      dbg('EXCEPTION: '+ex.message);goHome();" +
        "    }" +
        "  }" +
        "}" +
        "function setBtn(t,c){var b=document.getElementById('toggleBtn');b.textContent=t;b.className=c;dbg('Button: '+t);}" +
        "function setStatus(t,c){document.getElementById('status').textContent=t;document.getElementById('status').className=c;}" +

        /* TOUCH */
        "function sendTouchToServer(t,x,y){if(ws&&ws.readyState===1)ws.send(JSON.stringify({type:t,x:x,y:y}));}" +
        "function sendKey(e,k){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===1)ws.send(JSON.stringify({type:'key',key:k}));}" +
        "function sendChar(e,c){e.preventDefault();e.stopPropagation();if(ws&&ws.readyState===1)ws.send(JSON.stringify({type:'char',char:c}));}" +

        /* CLIPBOARD */
        "function sendClipboard(){var t=document.getElementById('clipText').value;if(ws&&ws.readyState===1){ws.send(JSON.stringify({type:'clipboard',text:t}));document.getElementById('clipStatus').textContent='Sent!';}}" +
        "function getClipboard(){if(ws&&ws.readyState===1){ws.send(JSON.stringify({type:'get_clipboard'}));document.getElementById('clipStatus').textContent='Requesting...';}}" +

        /* HISTORY */
        "function saveHistory(ip){var h=JSON.parse(localStorage.getItem('hist')||'[]');h=h.filter(function(i){return i!==ip;});h.unshift(ip);if(h.length>10)h=h.slice(0,10);localStorage.setItem('hist',JSON.stringify(h));}" +
        "function loadHistory(){" +
        "  var h=JSON.parse(localStorage.getItem('hist')||'[]');var el=document.getElementById('historyList');" +
        "  if(h.length===0){el.innerHTML='<p style=color:#888>No connections yet</p>';return;}" +
        "  el.innerHTML='';" +
        "  h.forEach(function(ip){" +
        "    var d=document.createElement('div');d.className='history-item';" +
        "    var sp=document.createElement('span');sp.className='history-ip';sp.textContent=ip;" +
        "    sp.onclick=function(){document.getElementById('ipInput').value=ip;goHome();};" +
        "    var btn=document.createElement('button');btn.className='history-del';btn.textContent='X';" +
        "    btn.onclick=function(){var h2=JSON.parse(localStorage.getItem('hist')||'[]');h2=h2.filter(function(i){return i!==ip;});localStorage.setItem('hist',JSON.stringify(h2));loadHistory();};" +
        "    d.appendChild(sp);d.appendChild(btn);el.appendChild(d);" +
        "  });" +
        "}" +
        "</script></body></html>";
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
