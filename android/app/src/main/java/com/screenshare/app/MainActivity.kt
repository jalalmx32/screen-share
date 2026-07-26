package com.screenshare.app

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

class MainActivity : Activity() {

    private lateinit var ipInput: EditText
    private lateinit var connectBtn: Button
    private lateinit var disconnectBtn: Button
    private lateinit var statusText: TextView
    private lateinit var fpsText: TextView
    private lateinit var screenView: ImageView

    private var webSocket: WebSocketClient? = null
    private var frameCount = 0L
    private var lastFpsTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ipInput = findViewById(R.id.ipInput)
        connectBtn = findViewById(R.id.connectBtn)
        disconnectBtn = findViewById(R.id.disconnectBtn)
        statusText = findViewById(R.id.statusText)
        fpsText = findViewById(R.id.fpsText)
        screenView = findViewById(R.id.screenView)

        connectBtn.setOnClickListener { connect() }
        disconnectBtn.setOnClickListener { disconnect() }
    }

    private fun connect() {
        val address = ipInput.text.toString().trim()
        if (address.isEmpty()) {
            statusText.text = "⚠️ Enter IP address"
            statusText.setTextColor(0xFFFFB800.toInt())
            return
        }

        try {
            val uri = URI("ws://$address")
            
            webSocket = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    runOnUiThread {
                        statusText.text = "● Connected"
                        statusText.setTextColor(0xFF4ADE80.toInt())
                        connectBtn.visibility = View.GONE
                        disconnectBtn.visibility = View.VISIBLE
                        screenView.visibility = View.VISIBLE
                        ipInput.isEnabled = false
                    }
                }

                override fun onMessage(message: ByteBuffer?) {
                    message?.let {
                        val bytes = ByteArray(it.remaining())
                        it.get(bytes)
                        
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bitmap != null) {
                            frameCount++
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastFpsTime >= 1000) {
                                val fps = ((frameCount) * 1000 / (currentTime - lastFpsTime)).toInt()
                                runOnUiThread {
                                    fpsText.text = "FPS: $fps"
                                }
                                frameCount = 0
                                lastFpsTime = currentTime
                            }
                            
                            runOnUiThread {
                                screenView.setImageBitmap(bitmap)
                            }
                        }
                    }
                }

                override fun onMessage(message: String?) {}

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    runOnUiThread {
                        statusText.text = "● Disconnected"
                        statusText.setTextColor(0xFFF87171.toInt())
                        resetUI()
                    }
                }

                override fun onError(ex: Exception?) {
                    runOnUiThread {
                        statusText.text = "❌ Error: ${ex?.message}"
                        statusText.setTextColor(0xFFF87171.toInt())
                        resetUI()
                    }
                }
            }

            statusText.text = "⏳ Connecting..."
            statusText.setTextColor(0xFFFFB800.toInt())
            webSocket?.connect()

        } catch (e: Exception) {
            statusText.text = "❌ ${e.message}"
            statusText.setTextColor(0xFFF87171.toInt())
        }
    }

    private fun disconnect() {
        webSocket?.close()
        webSocket = null
        resetUI()
        statusText.text = "● Offline"
        statusText.setTextColor(0xFFF87171.toInt())
    }

    private fun resetUI() {
        connectBtn.visibility = View.VISIBLE
        disconnectBtn.visibility = View.GONE
        screenView.visibility = View.GONE
        ipInput.isEnabled = true
        fpsText.text = "FPS: 0"
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocket?.close()
    }
}
