# ProGuard rules for ScreenShare
# Add project specific ProGuard rules here.

# Keep WebSocket classes
-keep class org.java_websocket.** { *; }
-keep class com.screenshare.app.network.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
