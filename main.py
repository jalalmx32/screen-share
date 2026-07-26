"""
ScreenShare - Wireless Display for Android
A free alternative to Spacedesk
"""

import sys
import os
import json
import socket
import threading
import time
from pathlib import Path

from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QComboBox, QSpinBox, QCheckBox, QFrame,
    QSystemTrayIcon, QMenu, QMessageBox, QSplashScreen
)
from PyQt6.QtCore import Qt, QTimer, pyqtSignal, QObject, QSize, QThread
from PyQt6.QtGui import (
    QIcon, QPixmap, QFont, QColor, QPalette, QAction,
    QPainter, QLinearGradient, QBrush, QPen
)

from src.screen_capture import ScreenCapture
from src.websocket_server import WebSocketServer


def create_app_icon(size=64):
    """Draw a clean monitor/broadcast icon programmatically (no external asset needed)."""
    pixmap = QPixmap(size, size)
    pixmap.fill(Qt.GlobalColor.transparent)

    painter = QPainter(pixmap)
    painter.setRenderHint(QPainter.RenderHint.Antialiasing)

    # Rounded gradient background
    gradient = QLinearGradient(0, 0, size, size)
    gradient.setColorAt(0, QColor("#00d4ff"))
    gradient.setColorAt(1, QColor("#0f3460"))
    painter.setBrush(QBrush(gradient))
    painter.setPen(Qt.PenStyle.NoPen)
    radius = size * 0.22
    painter.drawRoundedRect(0, 0, size, size, radius, radius)

    # Monitor body
    margin = size * 0.20
    screen_w = size - margin * 2
    screen_h = screen_w * 0.62
    screen_x = margin
    screen_y = margin * 0.85

    painter.setBrush(QBrush(QColor("#1a1a2e")))
    painter.setPen(QPen(QColor("#ffffff"), max(1.0, size * 0.02)))
    painter.drawRoundedRect(
        int(screen_x), int(screen_y), int(screen_w), int(screen_h),
        size * 0.06, size * 0.06
    )

    # Signal / broadcast wave inside the screen
    painter.setPen(QPen(QColor("#00d4ff"), max(1.5, size * 0.04), Qt.PenStyle.SolidLine,
                         Qt.PenCapStyle.RoundCap, Qt.PenJoinStyle.RoundJoin))
    wave_y = screen_y + screen_h * 0.6
    p1 = (screen_x + screen_w * 0.15, wave_y)
    p2 = (screen_x + screen_w * 0.40, wave_y - screen_h * 0.30)
    p3 = (screen_x + screen_w * 0.62, wave_y + screen_h * 0.12)
    p4 = (screen_x + screen_w * 0.85, wave_y - screen_h * 0.35)
    painter.drawLine(int(p1[0]), int(p1[1]), int(p2[0]), int(p2[1]))
    painter.drawLine(int(p2[0]), int(p2[1]), int(p3[0]), int(p3[1]))
    painter.drawLine(int(p3[0]), int(p3[1]), int(p4[0]), int(p4[1]))

    # Monitor stand
    stand_w = screen_w * 0.26
    stand_h = size * 0.09
    stand_x = size / 2 - stand_w / 2
    stand_y = screen_y + screen_h

    painter.setBrush(QBrush(QColor("#ffffff")))
    painter.setPen(Qt.PenStyle.NoPen)
    painter.drawRect(int(stand_x), int(stand_y), int(stand_w), int(stand_h * 0.5))

    base_w = screen_w * 0.55
    base_h = size * 0.055
    base_x = size / 2 - base_w / 2
    base_y = stand_y + stand_h * 0.5
    painter.drawRoundedRect(
        int(base_x), int(base_y), int(base_w), int(base_h),
        base_h * 0.4, base_h * 0.4
    )

    painter.end()
    return QIcon(pixmap)


class SignalEmitter(QObject):
    """Signals for thread communication"""
    client_connected = pyqtSignal(str)
    client_disconnected = pyqtSignal(str)
    status_update = pyqtSignal(str)
    fps_update = pyqtSignal(int)
    error_occurred = pyqtSignal(str)


class DarkTheme:
    """Dark theme stylesheet"""
    STYLESHEET = """
    /* Main Window */
    QMainWindow {
        background-color: #1a1a2e;
    }
    
    QWidget {
        background-color: #1a1a2e;
        color: #e0e0e0;
        font-family: 'Segoe UI', Arial, sans-serif;
        font-size: 13px;
    }
    
    /* Labels */
    QLabel {
        color: #e0e0e0;
        border: none;
    }
    
    QLabel#title {
        font-size: 20px;
        font-weight: bold;
        color: #00d4ff;
    }
    
    QLabel#subtitle {
        font-size: 11px;
        color: #888888;
    }
    
    QLabel#status {
        font-size: 13px;
        color: #4ade80;
        padding: 6px 12px;
        background-color: #16213e;
        border-radius: 6px;
    }
    
    QLabel#status.offline {
        color: #f87171;
    }
    
    QLabel#ip-label {
        font-size: 14px;
        color: #00d4ff;
        padding: 10px 16px;
        background-color: #16213e;
        border-radius: 6px;
        font-family: 'Consolas', monospace;
    }
    
    /* Buttons */
    QPushButton {
        background-color: #0f3460;
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 6px;
        font-size: 13px;
        font-weight: bold;
    }
    
    QPushButton:hover {
        background-color: #1a508b;
    }
    
    QPushButton:pressed {
        background-color: #0a2647;
    }
    
    QPushButton#start-btn {
        background-color: #00d4ff;
        color: #1a1a2e;
        font-size: 14px;
        padding: 12px 24px;
    }
    
    QPushButton#start-btn:hover {
        background-color: #00b8d4;
    }
    
    QPushButton#stop-btn {
        background-color: #f87171;
        color: white;
    }
    
    QPushButton#stop-btn:hover {
        background-color: #ef4444;
    }
    
    /* Frame */
    QFrame {
        background-color: #16213e;
        border-radius: 8px;
        padding: 10px;
    }
    
    QFrame#card {
        background-color: #16213e;
        border: 1px solid #1a508b;
    }
    
    /* ComboBox */
    QComboBox {
        background-color: #0f3460;
        color: white;
        border: 1px solid #1a508b;
        padding: 6px 10px;
        border-radius: 5px;
        min-width: 120px;
        font-size: 12px;
    }
    
    QComboBox:hover {
        border-color: #00d4ff;
    }
    
    QComboBox::drop-down {
        border: none;
        width: 24px;
    }
    
    QComboBox QAbstractItemView {
        background-color: #0f3460;
        color: white;
        selection-background-color: #1a508b;
        border: 1px solid #1a508b;
    }
    
    /* SpinBox */
    QSpinBox {
        background-color: #0f3460;
        color: white;
        border: 1px solid #1a508b;
        padding: 6px;
        border-radius: 5px;
        font-size: 12px;
    }
    
    QSpinBox:hover {
        border-color: #00d4ff;
    }
    
    /* CheckBox */
    QCheckBox {
        spacing: 6px;
        color: #e0e0e0;
        font-size: 12px;
    }
    
    QCheckBox::indicator {
        width: 16px;
        height: 16px;
        border-radius: 3px;
        border: 2px solid #1a508b;
        background-color: #0f3460;
    }
    
    QCheckBox::indicator:checked {
        background-color: #00d4ff;
        border-color: #00d4ff;
    }
    
    QCheckBox::indicator:hover {
        border-color: #00d4ff;
    }
    
    /* Menu Bar */
    QMenuBar {
        background-color: #16213e;
        color: #e0e0e0;
        border-bottom: 1px solid #1a508b;
        padding: 2px;
        font-size: 12px;
    }
    
    QMenuBar::item {
        padding: 6px 12px;
        border-radius: 4px;
    }
    
    QMenuBar::item:selected {
        background-color: #0f3460;
    }
    
    QMenu {
        background-color: #16213e;
        color: #e0e0e0;
        border: 1px solid #1a508b;
        border-radius: 6px;
        padding: 6px;
    }
    
    QMenu::item {
        padding: 8px 24px;
        border-radius: 4px;
        font-size: 12px;
    }
    
    QMenu::item:selected {
        background-color: #0f3460;
    }
    
    QMenu::separator {
        height: 1px;
        background-color: #1a508b;
        margin: 4px 8px;
    }
    
    /* StatusBar */
    QStatusBar {
        background-color: #16213e;
        color: #888888;
        border-top: 1px solid #1a508b;
        font-size: 11px;
    }
    
    /* ScrollBar */
    QScrollBar:vertical {
        background-color: #1a1a2e;
        width: 8px;
        border-radius: 4px;
    }
    
    QScrollBar::handle:vertical {
        background-color: #1a508b;
        border-radius: 4px;
        min-height: 25px;
    }
    
    QScrollBar::handle:vertical:hover {
        background-color: #00d4ff;
    }
    
    QScrollBar::add-line:vertical, QScrollBar::sub-line:vertical {
        height: 0;
    }
    
    /* ToolTip */
    QToolTip {
        background-color: #0f3460;
        color: white;
        border: 1px solid #00d4ff;
        border-radius: 4px;
        padding: 6px;
        font-size: 11px;
    }
    """


class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.signals = SignalEmitter()
        self.screen_capture = None
        self.ws_server = None
        self.is_running = False
        self.current_ip = "127.0.0.1"
        
        # Connect signals
        self.signals.client_connected.connect(self.on_client_connected)
        self.signals.client_disconnected.connect(self.on_client_disconnected)
        self.signals.status_update.connect(self.on_status_update)
        self.signals.fps_update.connect(self.on_fps_update)
        self.signals.error_occurred.connect(self.on_error)
        
        self.init_ui()
        self.init_system_tray()
        
        # Defer IP lookup so it never blocks the initial UI render
        QTimer.singleShot(100, self.update_ip_address)
        
        # Timer for updating connection info
        self.update_timer = QTimer()
        self.update_timer.timeout.connect(self.update_connection_info)
        self.update_timer.start(1000)
    
    def init_ui(self):
        self.setWindowTitle("ScreenShare - Wireless Display")
        self.setMinimumSize(380, 480)
        self.resize(420, 550)
        
        # Apply dark theme
        self.setStyleSheet(DarkTheme.STYLESHEET)
        
        # Status bar (used throughout the class via self.status_bar)
        self.status_bar = self.statusBar()
        
        # Central widget
        central = QWidget()
        self.setCentralWidget(central)
        layout = QVBoxLayout(central)
        layout.setSpacing(10)
        layout.setContentsMargins(16, 10, 16, 10)
        
        # === Header ===
        header = QVBoxLayout()
        header.setSpacing(4)
        
        title = QLabel("ScreenShare")
        title.setObjectName("title")
        title.setAlignment(Qt.AlignmentFlag.AlignCenter)
        header.addWidget(title)
        
        subtitle = QLabel("Wireless Display for Android")
        subtitle.setObjectName("subtitle")
        subtitle.setAlignment(Qt.AlignmentFlag.AlignCenter)
        header.addWidget(subtitle)

        author = QLabel("by Jalal | @x16_96")
        author.setObjectName("subtitle")
        author.setAlignment(Qt.AlignmentFlag.AlignCenter)
        header.addWidget(author)
        
        layout.addLayout(header)
        
        # === Status Card ===
        status_frame = QFrame()
        status_frame.setObjectName("card")
        status_layout = QVBoxLayout(status_frame)
        
        self.status_label = QLabel("● Offline")
        self.status_label.setObjectName("status offline")
        self.status_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        status_layout.addWidget(self.status_label)
        
        self.clients_label = QLabel("No devices connected")
        self.clients_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.clients_label.setStyleSheet("color: #888888; font-size: 12px;")
        status_layout.addWidget(self.clients_label)
        
        layout.addWidget(status_frame)
        
        # === IP Address Card ===
        ip_frame = QFrame()
        ip_frame.setObjectName("card")
        ip_layout = QVBoxLayout(ip_frame)
        
        ip_title = QLabel("📡 Connection Address")
        ip_title.setStyleSheet("color: #888888; font-size: 12px;")
        ip_layout.addWidget(ip_title)
        
        self.ip_label = QLabel("Loading...")
        self.ip_label.setObjectName("ip-label")
        self.ip_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        ip_layout.addWidget(self.ip_label)
        
        copy_btn = QPushButton("📋 Copy Address")
        copy_btn.clicked.connect(self.copy_ip_address)
        ip_layout.addWidget(copy_btn)
        
        layout.addWidget(ip_frame)
        
        # === Settings Card ===
        settings_frame = QFrame()
        settings_frame.setObjectName("card")
        settings_layout = QVBoxLayout(settings_frame)
        
        settings_title = QLabel("⚙️ Settings")
        settings_title.setStyleSheet("color: #888888; font-size: 12px;")
        settings_layout.addWidget(settings_title)
        
        # Display selection
        display_layout = QHBoxLayout()
        display_label = QLabel("Display:")
        display_label.setFixedWidth(80)
        self.display_combo = QComboBox()
        self.display_combo.addItem("Primary Display")
        self.display_combo.currentIndexChanged.connect(self.on_display_changed)
        display_layout.addWidget(display_label)
        display_layout.addWidget(self.display_combo)
        settings_layout.addLayout(display_layout)
        
        # Quality settings
        quality_layout = QHBoxLayout()
        quality_label = QLabel("Quality:")
        quality_label.setFixedWidth(80)
        self.quality_combo = QComboBox()
        self.quality_combo.addItems(["High (1080p)", "Medium (720p)", "Low (480p)"])
        quality_layout.addWidget(quality_label)
        quality_layout.addWidget(self.quality_combo)
        settings_layout.addLayout(quality_layout)
        
        # FPS settings
        fps_layout = QHBoxLayout()
        fps_label = QLabel("FPS:")
        fps_label.setFixedWidth(80)
        self.fps_spin = QSpinBox()
        self.fps_spin.setRange(15, 60)
        self.fps_spin.setValue(30)
        fps_layout.addWidget(fps_label)
        fps_layout.addWidget(self.fps_spin)
        settings_layout.addLayout(fps_layout)
        
        # Auto-start checkbox
        self.auto_start_cb = QCheckBox("Auto-start on launch")
        settings_layout.addWidget(self.auto_start_cb)
        
        # Minimize to tray checkbox
        self.minimize_tray_cb = QCheckBox("Minimize to system tray")
        self.minimize_tray_cb.setChecked(True)
        settings_layout.addWidget(self.minimize_tray_cb)
        
        layout.addWidget(settings_frame)
        
        # === Start/Stop Button ===
        self.start_btn = QPushButton("▶️  Start Sharing")
        self.start_btn.setObjectName("start-btn")
        self.start_btn.clicked.connect(self.toggle_sharing)
        layout.addWidget(self.start_btn)
        
        # === Footer ===
        footer = QLabel("ScreenShare v1.0 | Android Only")
        footer.setObjectName("subtitle")
        footer.setAlignment(Qt.AlignmentFlag.AlignCenter)
        layout.addWidget(footer)
        
        layout.addStretch()
    
    def init_system_tray(self):
        """Initialize system tray icon"""
        self.tray_icon = QSystemTrayIcon(self)
        self.tray_icon.setIcon(create_app_icon(32))
        
        # Tray menu
        tray_menu = QMenu()
        
        show_action = QAction("Show Window", self)
        show_action.triggered.connect(self.show_window)
        tray_menu.addAction(show_action)
        
        tray_menu.addSeparator()
        
        self.tray_start_action = QAction("Start Sharing", self)
        self.tray_start_action.triggered.connect(self.toggle_sharing)
        tray_menu.addAction(self.tray_start_action)
        
        tray_menu.addSeparator()
        
        quit_action = QAction("Quit", self)
        quit_action.triggered.connect(self.quit_app)
        tray_menu.addAction(quit_action)
        
        self.tray_icon.setContextMenu(tray_menu)
        self.tray_icon.activated.connect(self.tray_icon_clicked)
        self.tray_icon.show()
    
    def get_local_ip(self):
        """Get the local IP address"""
        try:
            # Connect to a remote server to find local IP
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.settimeout(1)
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
            s.close()
            return ip
        except Exception:
            return "127.0.0.1"
    
    def update_ip_address(self):
        """Update displayed IP address"""
        ip = self.get_local_ip()
        self.ip_label.setText(f"{ip}:8765")
        self.current_ip = ip
    
    def copy_ip_address(self):
        """Copy IP address to clipboard"""
        clipboard = QApplication.clipboard()
        clipboard.setText(f"{self.current_ip}:8765")
        self.status_bar.showMessage("Address copied to clipboard!", 2000)
    
    def on_display_changed(self, index):
        """Called when the user selects a different display from the dropdown"""
        selected = self.display_combo.currentText()
        self.status_bar.showMessage(f"Display selected: {selected}", 2000)
        
        # If sharing is already in progress, restart it so the new display takes effect
        if self.is_running and self.screen_capture:
            self.stop_sharing()
            self.start_sharing()
    
    def toggle_sharing(self):
        """Start or stop screen sharing"""
        if self.is_running:
            self.stop_sharing()
        else:
            self.start_sharing()
    
    def start_sharing(self):
        """Start screen sharing"""
        try:
            # Get settings
            quality_map = {
                "High (1080p)": (1920, 1080),
                "Medium (720p)": (1280, 720),
                "Low (480p)": (640, 480)
            }
            quality = self.quality_combo.currentText()
            resolution = quality_map.get(quality, (1280, 720))
            fps = self.fps_spin.value()
            
            # Start screen capture
            self.screen_capture = ScreenCapture(
                resolution=resolution,
                fps=fps
            )
            
            # Start WebSocket server
            self.ws_server = WebSocketServer(
                host="0.0.0.0",
                port=8765,
                screen_capture=self.screen_capture,
                signals=self.signals
            )
            
            self.ws_server.start()

            # Wait briefly for server thread to be ready before starting capture
            time.sleep(0.1)

            self.screen_capture.start()

            self.is_running = True
            self.update_ui_state()

            self.status_bar.showMessage("Screen sharing started!", 3000)

        except RuntimeError as e:
            # Port conflict or other startup error
            self.signals.error_occurred.emit(str(e))
        except Exception as e:
            self.signals.error_occurred.emit(str(e))
    
    def auto_start_sharing(self):
        """Auto-start sharing on launch if the checkbox is checked"""
        if self.auto_start_cb.isChecked():
            # Wait a moment for GUI to load, then start
            QTimer.singleShot(1000, self.start_sharing)
    
    def stop_sharing(self):
        """Stop screen sharing"""
        try:
            if self.ws_server:
                self.ws_server.stop()
                self.ws_server = None
            
            if self.screen_capture:
                self.screen_capture.stop()
                self.screen_capture = None
            
            self.is_running = False
            self.update_ui_state()
            self.clients_label.setText("No devices connected")
            
            self.status_bar.showMessage("Screen sharing stopped", 3000)
            
        except Exception as e:
            self.signals.error_occurred.emit(str(e))
    
    def update_ui_state(self):
        """Update UI based on sharing state"""
        if self.is_running:
            self.start_btn.setText("⏹️  Stop Sharing")
            self.start_btn.setObjectName("stop-btn")
            self.status_label.setText("● Online")
            self.status_label.setObjectName("status")
            self.tray_start_action.setText("Stop Sharing")
        else:
            self.start_btn.setText("▶️  Start Sharing")
            self.start_btn.setObjectName("start-btn")
            self.status_label.setText("● Offline")
            self.status_label.setObjectName("status offline")
            self.tray_start_action.setText("Start Sharing")
        
        # Re-apply styles
        self.start_btn.setStyleSheet(self.start_btn.styleSheet())
        self.status_label.setStyleSheet(self.status_label.styleSheet())
    
    def update_connection_info(self):
        """Periodically update connection info"""
        if self.ws_server:
            client_count = self.ws_server.get_client_count()
            if client_count > 0:
                self.clients_label.setText(f"📱 {client_count} device(s) connected")
                self.clients_label.setStyleSheet("color: #4ade80; font-size: 12px;")
            else:
                self.clients_label.setText("Waiting for device...")
                self.clients_label.setStyleSheet("color: #fbbf24; font-size: 12px;")
    
    # === Signal Handlers ===
    def on_client_connected(self, client_id):
        self.status_bar.showMessage(f"Device connected: {client_id}", 3000)
    
    def on_client_disconnected(self, client_id):
        self.status_bar.showMessage(f"Device disconnected: {client_id}", 3000)
    
    def on_status_update(self, status):
        self.status_bar.showMessage(status, 3000)
    
    def on_fps_update(self, fps):
        pass  # Could update FPS display
    
    def on_error(self, error):
        QMessageBox.critical(self, "Error", str(error))
        self.stop_sharing()
    
    def show_window(self):
        self.showNormal()
        self.activateWindow()
    
    def tray_icon_clicked(self, reason):
        if reason == QSystemTrayIcon.ActivationReason.DoubleClick:
            self.show_window()
    
    def closeEvent(self, event):
        if self.minimize_tray_cb.isChecked() and self.is_running:
            event.ignore()
            self.hide()
            self.tray_icon.showMessage(
                "ScreenShare",
                "Running in background. Double-click tray to show.",
                QSystemTrayIcon.MessageIcon.Information,
                2000
            )
        else:
            self.quit_app()
    
    def quit_app(self):
        self.stop_sharing()
        self.tray_icon.hide()
        QApplication.quit()


def main():
    app = QApplication(sys.argv)
    app.setApplicationName("ScreenShare")
    app.setOrganizationName("ScreenShare")
    
    # Set app icon
    app.setWindowIcon(create_app_icon(64))
    
    window = MainWindow()
    window.show()
    
    # Auto-start sharing on launch
    window.auto_start_sharing()
    
    sys.exit(app.exec())


if __name__ == "__main__":
    main()
