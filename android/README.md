# ScreenShare Android Client

Android app for receiving screen share from Windows PC.

## Features

- 📱 **Wireless Display** - View PC screen on Android
- 🔗 **Easy Connection** - Just enter IP address
- 📊 **Live Stats** - FPS and frame count
- 🎨 **Dark Mode** - Easy on the eyes
- 📐 **Pinch to Zoom** - Zoom and pan the screen
- 🔒 **Local Only** - No internet required

## Requirements

- Android 7.0+ (API 24)
- WiFi connection (same network as PC)
- ScreenShare Windows app running

## Installation

### Option 1: Build with Android Studio

1. Open Android Studio
2. File → Open → Select `android/` folder
3. Wait for Gradle sync
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. Install APK on your device

### Option 2: Command Line Build

```bash
cd android
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Usage

### Step 1: Setup PC

1. Enable hotspot on Android
2. Connect PC to the hotspot
3. Launch ScreenShare on PC
4. Note the IP address (e.g., `192.168.43.1:8765`)

### Step 2: Connect from Android

1. Open ScreenShare app
2. Enter the IP address from PC
3. Tap **Connect**
4. Your PC screen appears!

### Controls

- **Tap** - Show/hide controls
- **Pinch** - Zoom in/out
- **Drag** - Pan around (when zoomed)
- **Red button** - Disconnect

## Architecture

```
┌─────────────────────────────────────┐
│         Android App                 │
├─────────────────────────────────────┤
│  UI Layer (Jetpack Compose)         │
│  ├── ConnectionScreen               │
│  ├── DisplayScreen                  │
│  └── FrameRenderer                  │
├─────────────────────────────────────┤
│  Network Layer                      │
│  ├── ScreenShareClient (WebSocket)  │
│  └── FrameDecoder                   │
├─────────────────────────────────────┤
│  Display Layer                      │
│  ├── Canvas Renderer                │
│  ├── Zoom/Pan Controller            │
│  └── Frame Buffer                   │
└─────────────────────────────────────┘
```

## Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Troubleshooting

### "Connection Refused"
- Ensure both devices are on the same network
- Check if PC firewall is blocking port 8765
- Verify PC ScreenShare app is running

### Low FPS
- Reduce quality on PC app
- Move devices closer together
- Ensure good WiFi signal

### High Latency
- Use 5GHz WiFi if available
- Close other apps using bandwidth
- Reduce quality setting

## Future Features

- [ ] Touch input forwarding
- [ ] Keyboard input
- [ ] File transfer
- [ ] Auto-discovery (mDNS)
- [ ] Connection history
- [ ] Landscape mode
