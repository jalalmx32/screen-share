"""
WebSocket Server Module
Full-featured: screen streaming, touch, keyboard, clipboard, files, auth, UDP discovery
"""

import asyncio
import threading
import time
import json
import uuid
import os
import socket
import pyautogui

try:
    import websockets
    HAS_WEBSOCKETS = True
except ImportError:
    HAS_WEBSOCKETS = False

KEY_MAP = {
    "win": "win", "alt": "altleft", "ctrl": "ctrlleft", "shift": "shiftleft",
    "esc": "escape", "tab": "tab", "capslock": "capslock", "space": "space",
    "backspace": "backspace", "enter": "enter", "delete": "delete",
    "insert": "insert", "home": "home", "end": "end",
    "pageup": "pageup", "pagedown": "pagedown",
    "up": "up", "down": "down", "left": "left", "right": "right",
    "f4": "f4", "f5": "f5", "f11": "f11", "printscreen": "printscreen",
}

COMBO_MAP = {
    "alt_tab": ["altleft", "tab"], "alt_f4": ["altleft", "f4"],
    "ctrl_c": ["ctrlleft", "c"], "ctrl_v": ["ctrlleft", "v"],
    "ctrl_z": ["ctrlleft", "z"], "ctrl_a": ["ctrlleft", "a"],
    "ctrl_s": ["ctrlleft", "s"], "ctrl_x": ["ctrlleft", "x"],
    "ctrl_y": ["ctrlleft", "y"], "ctrl_p": ["ctrlleft", "p"],
}

DESKTOP_PATH = os.path.join(os.path.expanduser("~"), "Desktop")


class WebSocketServer:
    def __init__(self, host="0.0.0.0", port=8765, screen_capture=None, signals=None):
        self.host = host
        self.port = port
        self.screen_capture = screen_capture
        self.signals = signals
        self.clients = {}
        self.running = False
        self.server = None
        self.thread = None
        self.loop = None
        self.screen_width = 1920
        self.screen_height = 1080
        self.password = None
        self.udp_thread = None

        pyautogui.FAILSAFE = True
        pyautogui.PAUSE = 0.01

    def start(self):
        if not HAS_WEBSOCKETS:
            raise RuntimeError("websockets library not installed. Run: pip install websockets")

        # Check if port is already in use before starting
        if self._is_port_in_use():
            raise RuntimeError(
                f"Port {self.port} is already in use. "
                "Another instance may be running, or the port hasn't been released yet."
            )

        try:
            self.screen_width, self.screen_height = pyautogui.size()
        except Exception:
            pass
        self.running = True
        self.thread = threading.Thread(target=self._run_server, daemon=True)
        self.thread.start()
        self.udp_thread = threading.Thread(target=self._run_udp_discovery, daemon=True)
        self.udp_thread.start()

    def _is_port_in_use(self):
        """Check if the port is already in use"""
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 0)
                s.bind((self.host, self.port))
                return False
        except OSError:
            return True

    def stop(self):
        self.running = False
        if self.loop and self.loop.is_running():
            # Schedule server close and loop stop
            try:
                self.loop.call_soon_threadsafe(self._close_server_and_stop)
            except RuntimeError:
                # Loop already stopping or closed
                pass
        if self.thread:
            self.thread.join(timeout=5)
        self.clients.clear()

    def _close_server_and_stop(self):
        """Close the WebSocket server gracefully, then stop the loop"""
        if self.server:
            try:
                self.server.close()
            except Exception:
                pass
        # Stop the loop - don't create tasks after scheduling stop
        self.loop.stop()

    def _run_udp_discovery(self):
        """Listen for UDP discovery packets from Android"""
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            sock.bind(("", 8766))
            sock.settimeout(1)

            while self.running:
                try:
                    data, addr = sock.recvfrom(1024)
                    msg = data.decode("utf-8")
                    if msg == "SCREENSHARE_DISCOVER":
                        response = json.dumps({
                            "name": "ScreenShare",
                            "port": self.port,
                            "clients": self.get_client_count()
                        })
                        sock.sendto(response.encode("utf-8"), addr)
                        print(f"Discovery response sent to {addr[0]}")
                except socket.timeout:
                    continue
                except Exception as e:
                    if self.running:
                        print(f"UDP error: {e}")
            sock.close()
        except Exception as e:
            print(f"UDP discovery failed: {e}")

    def _run_server(self):
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        try:
            self.loop.run_until_complete(self._serve())
        except Exception as e:
            if self.running:
                print(f"Server error: {e}")
        finally:
            try:
                self.loop.close()
            except Exception:
                pass

    async def _serve(self):
        try:
            async with websockets.serve(
                self._handle_client, self.host, self.port,
                ping_interval=20, ping_timeout=10, max_size=2**22, compression=None
            ) as server:
                self.server = server
                if self.signals:
                    self.signals.status_update.emit(f"Server on {self.host}:{self.port}")
                while self.running:
                    await asyncio.sleep(1)
        except OSError as e:
            if self.signals:
                self.signals.error_occurred.emit(f"Server bind error: {e}")
            print(f"Server bind error: {e}")
        except Exception as e:
            if self.signals:
                self.signals.error_occurred.emit(f"Server error: {e}")
            print(f"Server error: {e}")

    async def _handle_client(self, websocket):
        client_id = str(uuid.uuid4())[:8]
        client_addr = websocket.remote_address
        self.clients[client_id] = {"websocket": websocket, "address": client_addr, "connected_at": time.time()}
        authenticated = not self.password

        if self.signals:
            self.signals.client_connected.emit(f"{client_addr[0]}:{client_addr[1]}")

        try:
            # Determine resolution and fps from screen_capture, with safe defaults
            sc = self.screen_capture
            resolution = sc.resolution if sc else (1280, 720)
            fps = sc.fps if sc else 30

            welcome = {
                "type": "welcome", "client_id": client_id,
                "resolution": resolution,
                "fps": fps,
                "screen_size": [self.screen_width, self.screen_height],
                "requires_auth": bool(self.password)
            }
            await websocket.send(json.dumps(welcome))

            # Start streaming frames in background
            stream_task = asyncio.ensure_future(self._stream_frames(websocket, client_id))

            try:
                async for message in websocket:
                    if isinstance(message, str):
                        try:
                            data = json.loads(message)
                            msg_type = data.get("type", "")

                            if msg_type == "auth":
                                if data.get("password") == self.password:
                                    authenticated = True
                                    await websocket.send(json.dumps({"type": "auth_ok"}))
                                else:
                                    await websocket.send(json.dumps({"type": "auth_fail"}))
                                continue

                            if not authenticated:
                                await websocket.send(json.dumps({"type": "auth_fail"}))
                                continue

                            if msg_type in ("touch_start", "touch_move", "touch_end", "double_tap", "long_press"):
                                self._process_touch(data)
                            elif msg_type == "key":
                                self._process_key(data)
                            elif msg_type == "char":
                                self._process_char(data)
                            elif msg_type == "clipboard":
                                self._process_clipboard(data)
                            elif msg_type == "get_clipboard":
                                await self._send_clipboard(websocket)
                            elif msg_type == "list_files":
                                await self._send_file_list(websocket)
                        except json.JSONDecodeError:
                            pass
            finally:
                stream_task.cancel()
                try:
                    await stream_task
                except asyncio.CancelledError:
                    pass
        except websockets.exceptions.ConnectionClosed:
            pass
        except Exception as e:
            print(f"Client error: {e}")
        finally:
            if client_id in self.clients:
                del self.clients[client_id]
            if self.signals:
                addr = client_addr if client_addr else ("unknown", 0)
                self.signals.client_disconnected.emit(f"{addr[0]}:{addr[1]}")

    def _process_touch(self, data):
        try:
            et = data.get("type", "")
            x = int(data.get("x", 0) * self.screen_width)
            y = int(data.get("y", 0) * self.screen_height)
            if et == "touch_start":
                pyautogui.moveTo(x, y, duration=0.05)
                pyautogui.mouseDown()
            elif et == "touch_move":
                pyautogui.moveTo(x, y, duration=0.02)
            elif et == "touch_end":
                pyautogui.mouseUp()
            elif et == "double_tap":
                pyautogui.doubleClick(x, y)
            elif et == "long_press":
                pyautogui.rightClick(x, y)
        except Exception as e:
            print(f"Touch error: {e}")

    def _process_key(self, data):
        try:
            key = data.get("key", "")
            if key in COMBO_MAP:
                combo = COMBO_MAP[key]
                for k in combo:
                    pyautogui.keyDown(k)
                for k in reversed(combo):
                    pyautogui.keyUp(k)
            elif key in KEY_MAP:
                pyautogui.press(KEY_MAP[key])
        except Exception as e:
            print(f"Key error: {e}")

    def _process_char(self, data):
        try:
            ch = data.get("char", "")
            if ch:
                pyautogui.press(ch)
        except Exception as e:
            print(f"Char error: {e}")

    def _process_clipboard(self, data):
        try:
            text = data.get("text", "")
            import subprocess
            subprocess.run(["clip"], input=text.encode("utf-16le"), check=True)
        except Exception as e:
            print(f"Clipboard error: {e}")

    async def _send_clipboard(self, websocket):
        try:
            import subprocess
            result = subprocess.run(
                ["powershell", "-command", "Get-Clipboard"],
                capture_output=True, text=True, timeout=5
            )
            text = result.stdout.strip()
            await websocket.send(json.dumps({"type": "clipboard", "text": text}))
        except Exception as e:
            print(f"Clipboard read error: {e}")

    async def _send_file_list(self, websocket):
        try:
            files = []
            for item in os.listdir(DESKTOP_PATH):
                full = os.path.join(DESKTOP_PATH, item)
                size = os.path.getsize(full) if os.path.isfile(full) else 0
                if os.path.isfile(full):
                    files.append({"name": item, "size": f"{size/1024:.1f} KB"})
            files.sort(key=lambda f: f["name"])
            await websocket.send(json.dumps({"type": "filelist", "files": files}))
        except Exception as e:
            print(f"File list error: {e}")

    async def _stream_frames(self, websocket, client_id):
        frame_count = 0
        consecutive_errors = 0
        max_consecutive_errors = 5
        print(f"[STREAM] Starting frame stream for client {client_id}")
        while self.running and client_id in self.clients:
            try:
                sc = self.screen_capture
                if sc is None or not sc.is_running():
                    await asyncio.sleep(0.5)
                    continue

                frame = sc.get_frame()
                if frame:
                    await websocket.send(frame)
                    frame_count += 1
                    consecutive_errors = 0  # Reset error counter on success
                    if frame_count % 30 == 0:
                        print(f"[STREAM] Sent {frame_count} frames to {client_id}")
                else:
                    if frame_count == 0:
                        print("[STREAM] No frames available yet, waiting...")
                    await asyncio.sleep(0.1)  # Brief wait if no frame yet
                    continue  # Don't count sleep if no frame

                fps = sc.fps if sc.fps > 0 else 30
                await asyncio.sleep(1.0 / fps)
            except websockets.exceptions.ConnectionClosed:
                print(f"[STREAM] Client {client_id} disconnected")
                break
            except asyncio.CancelledError:
                break
            except Exception as e:
                consecutive_errors += 1
                print(f"[STREAM] Error ({consecutive_errors}/{max_consecutive_errors}): {e}")
                if consecutive_errors >= max_consecutive_errors:
                    print(f"[STREAM] Too many consecutive errors, stopping stream to {client_id}")
                    break
                await asyncio.sleep(0.5)  # Brief pause before retry
        print(f"[STREAM] Stopped. Sent {frame_count} frames total.")

    def get_client_count(self):
        return len(self.clients)

    def get_clients(self):
        return [{"id": cid, "address": info["address"], "connected_at": info["connected_at"]}
                for cid, info in self.clients.items()]

    def set_password(self, password):
        self.password = password if password else None
