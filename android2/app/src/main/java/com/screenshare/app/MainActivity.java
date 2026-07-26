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

        // Apply custom layout with WebView
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webview);

        // Full screen setup
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

        // Configure WebView
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(0xFF0D1117);

        // Add JavaScript interface for Android communication
        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void setPage(String page) {
                runOnUiThread(() -> {
                    atHome = "home".equals(page);
                    reapplyFullscreen();
                });
            }

            @android.webkit.JavascriptInterface
            public void log(String m) {
                android.util.Log.d("ScreenShare", m);
            }
        }, "Android");

        // Load the HTML from assets instead of inline strings
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (atHome) {
            new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", (d, w) -> finish())
                .setNegativeButton("No", null)
                .show();
        } else {
            atHome = true;
            webView.evaluateJavascript("goHome()", null);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!atHome) {
            float vw = webView.getWidth();
            float vh = webView.getHeight();
            if (vw > 0 && vh > 0) {
                float nx = Math.max(0, Math.min(1, e.getX() / vw));
                float ny = Math.max(0, Math.min(1, e.getY() / vh));
                String t = "";
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        t = "touch_start";
                        break;
                    case MotionEvent.ACTION_MOVE:
                        t = "touch_move";
                        break;
                    case MotionEvent.ACTION_UP:
                        t = "touch_end";
                        break;
                }
                if (!t.isEmpty()) {
                    final String js = "sendTouchToServer('" + t + "'," + nx + "," + ny + ")";
                    mainHandler.post(() -> {
                        try {
                            webView.evaluateJavascript(js, null);
                        } catch (Exception ex) {
                            // WebView may be destroyed
                        }
                    });
                }
            }
        }
        return super.dispatchTouchEvent(e);
    }

    private void reapplyFullscreen() {
        runOnUiThread(() -> getWindow().getDecorView().setSystemUiVisibility(
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY));
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) reapplyFullscreen();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
