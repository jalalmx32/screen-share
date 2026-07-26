package com.screenshare.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color

/**
 * Composable that renders screen frames
 */
@Composable
fun FrameRenderer(
    frame: Bitmap?,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offset = Offset(
                        x = offset.x + pan.x,
                        y = offset.y + pan.y
                    )
                }
            }
    ) {
        if (frame != null) {
            val imageBitmap = frame.asImageBitmap()
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawScreenFrame(
                    bitmap = imageBitmap,
                    scale = scale,
                    offset = offset
                )
            }
        } else {
            // No frame - show placeholder
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Black,
                    size = size
                )
            }
        }
    }
}

/**
 * Draw the screen frame with zoom and pan
 */
private fun DrawScope.drawScreenFrame(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    scale: Float,
    offset: Offset
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    
    val bitmapWidth = bitmap.width.toFloat()
    val bitmapHeight = bitmap.height.toFloat()
    
    // Calculate scale to fit screen while maintaining aspect ratio
    val scaleX = canvasWidth / bitmapWidth
    val scaleY = canvasHeight / bitmapHeight
    val baseScale = minOf(scaleX, scaleY)
    
    // Apply user zoom
    val finalScale = baseScale * scale
    
    // Calculate position to center the image
    val scaledWidth = bitmapWidth * finalScale
    val scaledHeight = bitmapHeight * finalScale
    
    val offsetX = (canvasWidth - scaledWidth) / 2 + offset.x
    val offsetY = (canvasHeight - scaledHeight) / 2 + offset.y
    
    // Draw with translation and scale
    translate(offsetX, offsetY) {
        scale(finalScale, finalScale) {
            drawImage(bitmap)
        }
    }
}

/**
 * Alternative frame renderer with pinch-to-zoom
 */
@Composable
fun FrameRendererWithControls(
    frame: Bitmap?,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotationChange ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offset = Offset(
                        x = offset.x + pan.x,
                        y = offset.y + pan.y
                    )
                    rotation += rotationChange
                }
            }
    ) {
        if (frame != null) {
            val imageBitmap = frame.asImageBitmap()
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                val bitmapWidth = imageBitmap.width.toFloat()
                val bitmapHeight = imageBitmap.height.toFloat()
                
                // Calculate scale to fit screen
                val scaleX = canvasWidth / bitmapWidth
                val scaleY = canvasHeight / bitmapHeight
                val baseScale = minOf(scaleX, scaleY)
                val finalScale = baseScale * scale
                
                // Center the image
                val scaledWidth = bitmapWidth * finalScale
                val scaledHeight = bitmapHeight * finalScale
                val offsetX = (canvasWidth - scaledWidth) / 2 + offset.x
                val offsetY = (canvasHeight - scaledHeight) / 2 + offset.y
                
                // Draw with rotation
                translate(offsetX + scaledWidth / 2, offsetY + scaledHeight / 2) {
                    rotate(rotation) {
                        translate(-scaledWidth / 2, -scaledHeight / 2) {
                            scale(finalScale, finalScale) {
                                drawImage(imageBitmap)
                            }
                        }
                    }
                }
            }
        }
    }
}
