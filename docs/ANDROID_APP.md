# ScreenShare Android Client - Development Guide

## Overview

This document outlines the Android client app that will connect to the ScreenShare Windows server.

## Architecture

```
┌─────────────────────────────────────┐
│         Android App                 │
├─────────────────────────────────────┤
│  UI Layer (Jetpack Compose)         │
│  ├── Connection Screen              │
│  ├── Display Screen                 │
│  └── Settings Screen                │
├─────────────────────────────────────┤
│  Network Layer                      │
│  ├── WebSocket Client               │
│  ├── Frame Decoder                  │
│  └── Connection Manager             │
├─────────────────────────────────────┤
│  Display Layer                      │
│  ├── Frame Renderer (SurfaceView)   │
│  ├── Frame Buffer                   │
│  └── Touch Event Handler            │
└─────────────────────────────────────┘
```

## Connection Flow

1. User enters server IP (e.g., `192.168.43.1:8765`)
2. App connects via WebSocket
3. Server sends welcome message with resolution/FPS
4. Server streams JPEG frames
5. App decodes and displays frames

## Message Protocol

### Client → Server
```json
{
  "type": "input",
  "event": "touch",
  "x": 0.5,
  "y": 0.3,
  "action": "down|up|move"
}
```

### Server → Client
```json
{
  "type": "welcome",
  "client_id": "abc123",
  "resolution": [1920, 1080],
  "fps": 30
}

// Binary frame data (JPEG)

{
  "type": "stats",
  "fps": 28,
  "frame": 1234
}
```

## Dependencies (build.gradle)

```gradle
dependencies {
    // WebSocket
    implementation("org.java-websocket:Java-WebSocket:1.5.4")
    
    // Image decoding
    implementation("androidx.graphics:graphics-ktx:1.0.0")
    
    // UI
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.0")
    
    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}
```

## Permissions Required

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Screen Layout

### Connection Screen
```
┌─────────────────────────────┐
│      ScreenShare            │
│                             │
│   ┌─────────────────────┐   │
│   │ 192.168.43.1:8765   │   │
│   └─────────────────────┘   │
│                             │
│   [     Connect     ]       │
│                             │
│   ○ Saved Connections       │
│     • My PC (192.168.43.1)  │
└─────────────────────────────┘
```

### Display Screen
```
┌─────────────────────────────┐
│ ◄ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ► │
│                             │
│                             │
│    [PC Screen Content]      │
│                             │
│                             │
│ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ │
│  [Touch controls overlay]   │
└─────────────────────────────┘
```

## Performance Targets

- Latency: < 50ms (local network)
- FPS: 30fps stable
- CPU Usage: < 30%
- Memory: < 100MB

## Build Instructions

1. Open project in Android Studio
2. Sync Gradle
3. Build → Generate Signed APK
4. Install on Android device

## Future Features

- [ ] Touch input forwarding
- [ ] Keyboard input
- [ ] Audio streaming
- [ ] File transfer
- [ ] Multi-monitor support
- [ ] Landscape/Portrait modes
- [ ] Connection history
- [ ] Auto-discovery (mDNS)
