# ScreenShare - Wireless Display for Android

A free, open-source alternative to Spacedesk. Share your Windows PC screen with your Android device over WiFi hotspot.

## Features

- 🖥️ **Wireless Display** - Use your Android as a second monitor
- 🌙 **Dark Mode** - Easy on the eyes
- 📱 **Android Only** - Simple, focused experience
- 🔧 **Customizable** - Adjust quality, FPS, and display settings
- 📡 **Hotspot Support** - Works without a router
- 🔒 **Local Network** - No internet required, fully local

## Requirements

### PC (Windows)
- Windows 10/11
- Python 3.10+ (for running from source)
- WiFi adapter (for hotspot)

### Android
- Android 7.0+
- ScreenShare Android app (coming soon)

## Installation

### Option 1: Run from Source

1. Install Python 3.10+ from [python.org](https://www.python.org/downloads/)
2. Clone or download this repository
3. Run the build script:
   ```
   build.bat
   ```
4. Or install manually:
   ```
   pip install -r requirements.txt
   pip install PyQt6
   python main.py
   ```

### Option 2: Build Executable

1. Run `build.bat`
2. Find `ScreenShare.exe` in `dist/` folder
3. Distribute to other Windows PCs

## Usage

### Setup

1. **Enable Hotspot** on your Android phone
2. **Connect PC** to the Android hotspot
3. **Launch ScreenShare** on PC
4. **Note the IP address** shown in the app (e.g., `192.168.43.1:8765`)

### Connect from Android

1. Open ScreenShare Android app
2. Enter the IP address from PC
3. Tap Connect
4. Your PC screen will appear on Android!

### Settings

- **Display**: Choose which monitor to share
- **Quality**: High (1080p), Medium (720p), or Low (480p)
- **FPS**: 15-60 frames per second
- **Auto-start**: Start sharing on app launch
- **Minimize to tray**: Keep running in background

## Network Diagram

```
┌─────────────────┐         ┌─────────────────┐
│  Windows PC     │         │  Android Phone  │
│  (Server)       │         │  (Client)       │
│                 │  WiFi   │                 │
│  ScreenShare ◄──┼────────►│  ScreenShare    │
│  Port: 8765     │ Hotspot │  App            │
└─────────────────┘         └─────────────────┘
```

## Troubleshooting

### "Connection Refused"
- Ensure both devices are on the same network
- Check if firewall is blocking port 8765
- Try disabling Windows Firewall temporarily

### Low FPS
- Reduce quality setting
- Lower FPS setting
- Ensure good WiFi signal
- Close other bandwidth-heavy apps

### High Latency
- Use 5GHz WiFi if available
- Move devices closer together
- Reduce quality setting

## Technical Details

- **Protocol**: WebSocket (binary frames)
- **Frame Format**: JPEG
- **Default Port**: 8765
- **Resolution**: Configurable (default 1280x720)
- **Frame Rate**: 15-60 FPS configurable

## Building Android App

The Android client app is planned for future development. It will:

- Connect to the Windows server via WebSocket
- Display received frames in real-time
- Support touch input (future feature)
- Available on Google Play Store

## License

MIT License - Free to use, modify, and distribute.

## Contributing

Contributions welcome! Areas that need help:

- Android app development
- Touch input support
- Audio streaming
- Performance optimization
- UI/UX improvements

## Credits

- Inspired by [Spacedesk](https://www.spacedesk.net/)
- Built with Python, PyQt6, and WebSocket
