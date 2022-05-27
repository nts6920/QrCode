package com.nhnextsoft.qrcode.ui.screen.scancode

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nhnextsoft.qrcode.ui.theme.ColorBackground
import com.nhnextsoft.qrcode.ui.theme.ColorButton
import com.nhnextsoft.qrcode.ui.theme.ColorOrange


@Preview(showBackground = true, backgroundColor = 0x00000000)
@Composable
fun ScannerFrameComposable(modifier: Modifier = Modifier) {

    val infiniteTransition = rememberInfiniteTransition()
    val animLineScanHeight by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 1F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val colorLine = ColorBackground.copy(red = 1f)
    val colorCorner = Color.White
    val colorLineScan = ColorOrange.copy(red = 1f, blue = 1f)
    val sizeStretch = 50f
    val sizeTriangleWidth = 10f

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize(0.8f)
            .padding(10.dp),
        onDraw = {
            translate {

                drawRoundRect(
                    color = colorLine,
                    style = Stroke(width = 1f, cap = StrokeCap.Square)
                )
                val trianglePathTopLeft = Path().apply {
                    // Moves to top center position
                    moveTo(0f, sizeStretch)
                    lineTo(0f, sizeStretch)
                    lineTo(0f, 0f)
                    lineTo(sizeStretch, 0f)
                }
                val trianglePathTopRight = Path().apply {
                    moveTo(size.width - sizeStretch, 0f)
                    lineTo(size.width - sizeStretch, 0f)
                    // Add line to bottom right corner
                    lineTo(size.width, 0f)
                    lineTo(size.width, sizeStretch)
                    // Add line to bottom left corner
                }
                val trianglePathBottomLeft = Path().apply {
                    // Moves to top center position
                    moveTo(0f, size.height - sizeStretch)
                    lineTo(0f, size.height - sizeStretch)
                    // Add line to bottom right corner
                    lineTo(0f, size.height)
                    lineTo(sizeStretch, size.height)
                    // Add line to bottom left corner
                }
                val trianglePathBottomRight = Path().apply {
                    // Moves to top center position
                    moveTo(size.width - sizeStretch, size.height)
                    lineTo(size.width - sizeStretch, size.height)
                    // Add line to bottom right corner
                    lineTo(size.width, size.height)
                    lineTo(size.width, size.height - sizeStretch)
                    // Add line to bottom left corner
                }

                val cornerPathEffect = PathEffect.cornerPathEffect(sizeTriangleWidth / 2)
                drawPath(trianglePathTopLeft,
                    brush = SolidColor(colorCorner),
                    style = Stroke(width = sizeTriangleWidth,
                        cap = StrokeCap.Square,
                        pathEffect = cornerPathEffect))

                drawPath(trianglePathTopRight,
                    brush = SolidColor(colorCorner),
                    style = Stroke(width = sizeTriangleWidth,
                        cap = StrokeCap.Square,
                        pathEffect = cornerPathEffect))

                drawPath(trianglePathBottomLeft,
                    brush = SolidColor(colorCorner),
                    style = Stroke(width = sizeTriangleWidth,
                        cap = StrokeCap.Square,
                        pathEffect = cornerPathEffect))

                drawPath(trianglePathBottomRight,
                    brush = SolidColor(colorCorner),
                    style = Stroke(width = sizeTriangleWidth,
                        cap = StrokeCap.Square,
                        pathEffect = cornerPathEffect))

                drawLine(colorLineScan,
                    start = Offset(2f, size.height * animLineScanHeight),
                    end = Offset(size.width - 2f, size.height * animLineScanHeight),
                    strokeWidth = 3f)
            }
        }
    )
}