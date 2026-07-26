"""
Screen Capture Module
Captures screen and provides frames for streaming
"""

import io
import time
import threading
from collections import deque

try:
    import mss
    import mss.tools
    HAS_MSS = True
except ImportError:
    HAS_MSS = False

try:
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False


class ScreenCapture:
    """Captures screen frames and manages frame rate"""
    
    def __init__(self, resolution=(1280, 720), fps=30):
        self.resolution = resolution
        self.fps = fps
        self.frame_interval = 1.0 / fps
        self.running = False
        self.current_frame = None
        self.frame_lock = threading.Lock()
        self.capture_thread = None
        
        # Performance stats
        self.frame_count = 0
        self.last_fps_time = time.time()
        self.current_fps = 0
        
        # Frame buffer for multiple clients
        self.frame_buffer = deque(maxlen=2)
    
    def start(self):
        """Start screen capture"""
        if not HAS_MSS:
            raise RuntimeError("mss library not installed. Run: pip install mss")
        
        self.running = True
        self.capture_thread = threading.Thread(target=self._capture_loop, daemon=True)
        self.capture_thread.start()
    
    def stop(self):
        """Stop screen capture"""
        self.running = False
        if self.capture_thread:
            self.capture_thread.join(timeout=2)
    
    def _capture_loop(self):
        """Main capture loop"""
        with mss.mss() as sct:
            # Get primary monitor
            monitor = sct.monitors[1]  # Primary monitor
            
            while self.running:
                start_time = time.time()
                
                try:
                    # Capture screen
                    screenshot = sct.grab(monitor)
                    
                    # Convert to PIL Image - use raw bytes (BGRA format)
                    img = Image.frombytes("RGB", screenshot.size, bytes(screenshot), "raw", "BGRX")
                    
                    # Resize to target resolution
                    img = img.resize(self.resolution, Image.Resampling.LANCZOS)
                    
                    # Convert to JPEG bytes
                    buffer = io.BytesIO()
                    img.save(buffer, format="JPEG", quality=85, optimize=True)
                    frame_bytes = buffer.getvalue()
                    
                    # Update current frame
                    with self.frame_lock:
                        self.current_frame = frame_bytes
                        self.frame_buffer.append(frame_bytes)
                    
                    # Update FPS counter
                    self.frame_count += 1
                    current_time = time.time()
                    if current_time - self.last_fps_time >= 1.0:
                        self.current_fps = self.frame_count
                        self.frame_count = 0
                        self.last_fps_time = current_time
                    
                    # Maintain frame rate
                    elapsed = time.time() - start_time
                    sleep_time = self.frame_interval - elapsed
                    if sleep_time > 0:
                        time.sleep(sleep_time)
                        
                except Exception as e:
                    print(f"Capture error: {e}")
                    time.sleep(0.1)
    
    def get_frame(self):
        """Get current frame as JPEG bytes"""
        with self.frame_lock:
            return self.current_frame
    
    def get_fps(self):
        """Get current FPS"""
        return self.current_fps
    
    def set_resolution(self, resolution):
        """Update target resolution"""
        self.resolution = resolution
    
    def set_fps(self, fps):
        """Update target FPS"""
        self.fps = fps
        self.frame_interval = 1.0 / fps
