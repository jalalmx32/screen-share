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
        self.fps = max(fps, 1)  # Prevent division by zero
        self.frame_interval = 1.0 / self.fps
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

        # Monitor selection
        self.monitor_index = 1  # Primary monitor

    def start(self):
        """Start screen capture"""
        if not HAS_MSS:
            raise RuntimeError("mss library not installed. Run: pip install mss")
        if not HAS_PIL:
            raise RuntimeError("Pillow library not installed. Run: pip install Pillow")

        self.running = True
        self.capture_thread = threading.Thread(target=self._capture_loop, daemon=True)
        self.capture_thread.start()

    def stop(self):
        """Stop screen capture"""
        self.running = False
        if self.capture_thread:
            self.capture_thread.join(timeout=3)
            self.capture_thread = None

    def _capture_loop(self):
        """Main capture loop with automatic recovery on crash"""
        while self.running:
            try:
                self._do_capture()
            except Exception as e:
                print(f"[CAPTURE] Capture thread error: {e}")
                # Brief pause before retrying
                if self.running:
                    time.sleep(0.5)
            # If _do_capture returned normally (loop ended), we're done.
            # If it raised, we retry. Break out if no longer running.
            if not self.running:
                break

    def _do_capture(self):
        """Inner capture loop - runs until error or stopped"""
        with mss.mss() as sct:
            # Get primary monitor - safe bounds check
            monitors = sct.monitors
            if len(monitors) < 2:
                raise RuntimeError("No monitors found")
            monitor = monitors[self.monitor_index]

            while self.running:
                start_time = time.time()

                try:
                    # Capture screen
                    screenshot = sct.grab(monitor)

                    # Convert to PIL Image using RGB bytes
                    img = Image.frombytes("RGB", screenshot.size, screenshot.rgb)

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
            if self.current_frame is None:
                return None
            return self.current_frame

    def get_fps(self):
        """Get current FPS"""
        return self.current_fps

    def set_resolution(self, resolution):
        """Update target resolution"""
        self.resolution = resolution

    def is_running(self):
        """Check if capture is running"""
        return self.running and self.capture_thread is not None and self.capture_thread.is_alive()

    def set_fps(self, fps):
        """Update target FPS"""
        if fps < 1:
            fps = 1
        self.fps = fps
        self.frame_interval = 1.0 / fps

    def set_monitor(self, index):
        """Set which monitor to capture (0=all, 1=primary, 2=secondary, ...)"""
        self.monitor_index = index
