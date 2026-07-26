"""
WebSocket Server Module
Handles client connections, streams screen frames, and receives touch input
"""

import asyncio
import threading
import time
import json
import uuid
import pyautogui

try:
    import websockets
    HAS_WEBSOCKETS = True
except ImportError:
    HAS_WEBSOCKETS = False


class WebSocketServer:
    """WebSocket server for streaming screen to Android clients"""
    
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
        
        # Screen dimensions for touch mapping
        self.screen_width = 1920
        self.screen_height = 1080
        
        # Configure pyautogui
        pyautogui.FAILSAFE = True
        pyautogui.PAUSE = 0.01
    
    def start(self):
        """Start the WebSocket server"""
        if not HAS_WEBSOCKETS:
            raise RuntimeError("websockets library not installed. Run: pip install websockets")
        
        # Get actual screen size
        try:
            self.screen_width, self.screen_height = pyautogui.size()
        except Exception:
            pass
        
        self.running = True
        self.thread = threading.Thread(target=self._run_server, daemon=True)
        self.thread.start()
    
    def stop(self):
        """Stop the WebSocket server"""
        self.running = False
        if self.loop:
            self.loop.call_soon_threadsafe(self.loop.stop)
        if self.thread:
            self.thread.join(timeout=3)
        self.clients.clear()
    
    def _run_server(self):
        """Run the server in a separate thread"""
        self.loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.loop)
        
        self.loop.run_until_complete(self._serve())
    
    async def _serve(self):
        """Main server coroutine"""
        async with websockets.serve(
            self._handle_client,
            self.host,
            self.port,
            ping_interval=20,
            ping_timeout=10,
            max_size=2**20,  # 1MB max message size
            compression=None
        ) as server:
            self.server = server
            if self.signals:
                self.signals.status_update.emit(f"Server started on {self.host}:{self.port}")
            
            # Keep running until stopped
            while self.running:
                await asyncio.sleep(1)
    
    async def _handle_client(self, websocket, path=None):
        """Handle a new client connection"""
        client_id = str(uuid.uuid4())[:8]
        client_addr = websocket.remote_address
        
        self.clients[client_id] = {
            "websocket": websocket,
            "address": client_addr,
            "connected_at": time.time()
        }
        
        if self.signals:
            self.signals.client_connected.emit(f"{client_addr[0]}:{client_addr[1]}")
        
        try:
            # Send welcome message with screen dimensions
            welcome = {
                "type": "welcome",
                "client_id": client_id,
                "resolution": self.screen_capture.resolution if self.screen_capture else (1280, 720),
                "fps": self.screen_capture.fps if self.screen_capture else 30,
                "screen_size": [self.screen_width, self.screen_height]
            }
            await websocket.send(json.dumps(welcome))
            
            # Handle messages from client (touch events)
            await self._handle_messages(websocket, client_id)
            
        except websockets.exceptions.ConnectionClosed:
            pass
        except Exception as e:
            print(f"Client error: {e}")
        finally:
            # Remove client
            if client_id in self.clients:
                del self.clients[client_id]
            
            if self.signals:
                addr = client_addr if client_addr else ("unknown", 0)
                self.signals.client_disconnected.emit(f"{addr[0]}:{addr[1]}")
    
    async def _handle_messages(self, websocket, client_id):
        """Handle incoming messages from client (touch events)"""
        # Start streaming frames
        frame_task = asyncio.create_task(self._stream_frames(websocket, client_id))
        
        try:
            async for message in websocket:
                # Handle touch events
                if isinstance(message, str):
                    try:
                        data = json.loads(message)
                        self._process_touch_event(data)
                    except json.JSONDecodeError:
                        pass
        except websockets.exceptions.ConnectionClosed:
            pass
        finally:
            frame_task.cancel()
    
    def _process_touch_event(self, data):
        """Process touch event from Android client"""
        try:
            event_type = data.get("type", "")
            x = data.get("x", 0)
            y = data.get("y", 0)
            
            # Convert normalized coordinates (0-1) to screen coordinates
            screen_x = int(x * self.screen_width)
            screen_y = int(y * self.screen_height)
            
            if event_type == "touch_start":
                # Move mouse and click
                pyautogui.moveTo(screen_x, screen_y, duration=0.05)
                pyautogui.mouseDown()
                
            elif event_type == "touch_move":
                # Move mouse
                pyautogui.moveTo(screen_x, screen_y, duration=0.02)
                
            elif event_type == "touch_end":
                # Release click
                pyautogui.mouseUp()
                
            elif event_type == "double_tap":
                # Double click
                pyautogui.doubleClick(screen_x, screen_y)
                
            elif event_type == "long_press":
                # Right click
                pyautogui.rightClick(screen_x, screen_y)
                
        except Exception as e:
            print(f"Touch error: {e}")
    
    async def _stream_frames(self, websocket, client_id):
        """Stream screen frames to client"""
        frame_count = 0
        
        while self.running and client_id in self.clients:
            try:
                frame = self.screen_capture.get_frame()
                
                if frame:
                    # Send frame as binary
                    await websocket.send(frame)
                    frame_count += 1
                    
                    # Send stats every 30 frames
                    if frame_count % 30 == 0:
                        stats = {
                            "type": "stats",
                            "fps": self.screen_capture.get_fps(),
                            "frame": frame_count
                        }
                        await websocket.send(json.dumps(stats))
                
                # Maintain frame rate
                await asyncio.sleep(1.0 / self.screen_capture.fps)
                
            except websockets.exceptions.ConnectionClosed:
                break
            except Exception as e:
                print(f"Stream error: {e}")
                break
    
    def get_client_count(self):
        """Get number of connected clients"""
        return len(self.clients)
    
    def get_clients(self):
        """Get list of connected clients"""
        return [
            {
                "id": cid,
                "address": info["address"],
                "connected_at": info["connected_at"]
            }
            for cid, info in self.clients.items()
        ]
