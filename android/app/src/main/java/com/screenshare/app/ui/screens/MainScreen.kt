package com.screenshare.app.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.screenshare.app.network.ScreenShareClient
import com.screenshare.app.ui.FrameRenderer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    
    // State
    var ipAddress by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("8765") }
    var currentFrame by remember { mutableStateOf<Bitmap?>(null) }
    var connectionState by remember { mutableStateOf<ScreenShareClient.ConnectionState>(
        ScreenShareClient.ConnectionState.Disconnected
    ) }
    var showSettings by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf(ScreenShareClient.Stats()) }
    
    // WebSocket client
    val client = remember {
        ScreenShareClient(
            onFrameReceived = { bitmap ->
                currentFrame = bitmap
            },
            onConnectionChanged = { state ->
                connectionState = state
            }
        )
    }
    
    // Collect state
    LaunchedEffect(Unit) {
        while (true) {
            connectionState = client.getConnectionState()
            stats = client.stats.value
            delay(100)
        }
    }
    
    // Full screen display mode
    val isDisplaying = connectionState is ScreenShareClient.ConnectionState.Connected
    
    if (isDisplaying) {
        // Full screen display
        DisplayScreen(
            frame = currentFrame,
            stats = stats,
            onDisconnect = { client.disconnect() },
            onToggleSettings = { showSettings = !showSettings }
        )
    } else {
        // Connection screen
        ConnectionScreen(
            ipAddress = ipAddress,
            port = port,
            connectionState = connectionState,
            onIpAddressChange = { ipAddress = it },
            onPortChange = { port = it },
            onConnect = {
                if (ipAddress.isNotBlank()) {
                    client.connect(ipAddress, port.toIntOrNull() ?: 8765)
                }
            },
            onDisconnect = { client.disconnect() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    ipAddress: String,
    port: String,
    connectionState: ScreenShareClient.ConnectionState,
    onIpAddressChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isConnecting = connectionState is ScreenShareClient.ConnectionState.Connecting
    val isError = connectionState is ScreenShareClient.ConnectionState.Error
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ScreenShare",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Icon
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Connect to PC",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Enter your PC's IP address to connect",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Connection Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // IP Address Field
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = onIpAddressChange,
                        label = { Text("IP Address") },
                        placeholder = { Text("192.168.43.1") },
                        leadingIcon = {
                            Icon(Icons.Default.Router, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConnecting
                    )
                    
                    // Port Field
                    OutlinedTextField(
                        value = port,
                        onValueChange = onPortChange,
                        label = { Text("Port") },
                        placeholder = { Text("8765") },
                        leadingIcon = {
                            Icon(Icons.Default.SettingsEthernet, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConnecting
                    )
                }
            }
            
            // Error Message
            AnimatedVisibility(visible = isError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = (connectionState as? ScreenShareClient.ConnectionState.Error)?.message
                                ?: "Connection failed",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Connect Button
            Button(
                onClick = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isConnecting && ipAddress.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect", fontSize = 18.sp)
                }
            }
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📖 How to connect:",
                        fontWeight = FontWeight.Bold
                    )
                    
                    InstructionItem(
                        icon = Icons.Default.Wifi,
                        text = "Enable hotspot on your Android"
                    )
                    
                    InstructionItem(
                        icon = Icons.Default.Computer,
                        text = "Connect PC to the hotspot"
                    )
                    
                    InstructionItem(
                        icon = Icons.Default.PlayArrow,
                        text = "Start ScreenShare on PC"
                    )
                    
                    InstructionItem(
                        icon = Icons.Default.PhoneAndroid,
                        text = "Enter the IP shown on PC screen"
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text)
    }
}

@Composable
fun DisplayScreen(
    frame: Bitmap?,
    stats: ScreenShareClient.Stats,
    onDisconnect: () -> Unit,
    onToggleSettings: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    
    // Auto-hide controls
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            showControls = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Frame Renderer
        FrameRenderer(
            frame = frame,
            modifier = Modifier.fillMaxSize()
        )
        
        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stats
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.7f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "FPS: ${stats.fps}",
                                color = Color.Green,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Frames: ${stats.frameCount}",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Disconnect button
                    IconButton(
                        onClick = onDisconnect,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.8f))
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "Disconnect",
                            tint = Color.White
                        )
                    }
                }
                
                // Bottom hint
                Text(
                    text = "Tap to show controls • Pinch to zoom",
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        
        // Touch to show controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
        )
    }
}

// Add missing import
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
