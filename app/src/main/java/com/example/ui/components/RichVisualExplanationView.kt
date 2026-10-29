package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HighlightRange

@Composable
fun RichVisualExplanationView(
    explanationText: String,
    highlights: List<HighlightRange> = emptyList(),
    fontSize: TextUnit = 14.sp,
    fontFamily: FontFamily = FontFamily.Default,
    textColor: Color = Color(0xFF1E293B),
    modifier: Modifier = Modifier
) {
    val lowerText = remember(explanationText) { explanationText.lowercase() }

    val hasTriangle = lowerText.contains("equilateral triangle") || lowerText.contains("right triangle") || lowerText.contains("hypotenuse") || (lowerText.contains("triangle") && (lowerText.contains("altitude") || lowerText.contains("base") || lowerText.contains("perimeter")))
    val hasCircle = (lowerText.contains("circle") && (lowerText.contains("radius") || lowerText.contains("diameter") || lowerText.contains("circumference") || lowerText.contains("chord")))
    val hasRectangle = (lowerText.contains("rectangle") && (lowerText.contains("breadth") || lowerText.contains("perimeter") || lowerText.contains("diagonal")))

    val isNumberSeries = lowerText.contains("number series") || lowerText.contains("missing number") || lowerText.contains("2, 3, 6, 11") || (lowerText.contains("series") && lowerText.contains("difference between consecutive"))
    val isDirection = lowerText.contains("direction sense") || lowerText.contains("walks north") || lowerText.contains("walks south") || lowerText.contains("walks east") || lowerText.contains("walks west") || lowerText.contains("pythagoras theorem")
    val isBloodRelation = lowerText.contains("blood relation") || lowerText.contains("family tree") || lowerText.contains("paternal") || lowerText.contains("maternal")
    val isCoding = lowerText.contains("coding-decoding") || lowerText.contains("coded as") || lowerText.contains("in a certain code")

    val stepHighlights = remember(highlights) {
        highlights.map { it.copy(startIndex = -1, endIndex = -1) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isNumberSeries) {
            NumberSeriesZigZagDiagramView()
        } else if (hasTriangle) {
            GeometryTriangleFigureView()
        } else if (hasCircle) {
            GeometryCircleFigureView()
        } else if (hasRectangle) {
            GeometryRectangleFigureView()
        } else if (isDirection) {
            DirectionCompassDiagramView()
        } else if (isBloodRelation) {
            BloodRelationDiagramView()
        } else if (isCoding) {
            CodingDecodingDiagramView()
        }

        MathAndHighlightText(
            text = explanationText,
            highlights = highlights,
            fontSize = fontSize,
            fontFamily = fontFamily,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF3B82F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "IN SIMPLE WORDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D4ED8),
                        letterSpacing = 0.5.sp
                    )
                }

                val steps = remember(explanationText) {
                    extractSimpleSteps(explanationText)
                }

                steps.forEach { stepText ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            MathAndHighlightText(
                                text = stepText,
                                highlights = stepHighlights,
                                fontSize = (fontSize.value * 0.95f).sp,
                                fontFamily = fontFamily,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberSeriesZigZagDiagramView() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(340.dp, 160.dp)) {
                val points = listOf(
                    Offset(30f, 30f),
                    Offset(80f, 75f),
                    Offset(130f, 30f),
                    Offset(180f, 75f),
                    Offset(230f, 30f),
                    Offset(280f, 75f),
                    Offset(320f, 30f)
                )

                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF94A3B8),
                    style = Stroke(width = 2f)
                )

                val diffPoints = listOf(
                    Offset(55f, 105f),
                    Offset(105f, 140f),
                    Offset(155f, 105f),
                    Offset(205f, 140f),
                    Offset(255f, 105f),
                    Offset(300f, 140f)
                )

                val diffPath = Path().apply {
                    moveTo(diffPoints[0].x, diffPoints[0].y)
                    for (i in 1 until diffPoints.size) {
                        lineTo(diffPoints[i].x, diffPoints[i].y)
                    }
                }

                drawPath(
                    path = diffPath,
                    color = Color(0xFFCBD5E1),
                    style = Stroke(width = 1.5f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("2", "3", "6", "11", "18", "27", "?").forEach { num ->
                        Text(
                            text = num,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("+1", "+3", "+5", "+7", "+9", "+11").forEach { diff ->
                            Text(
                                text = diff,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 44.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("+2", "+2", "+2", "+2", "+2").forEach { diff2 ->
                            Text(
                                text = diff2,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeometryTriangleFigureView() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(220.dp, 110.dp)) {
                val w = size.width
                val h = size.height

                val apex = Offset(w / 2f, 10f)
                val bottomLeft = Offset(40f, h - 15f)
                val bottomRight = Offset(w - 40f, h - 15f)

                val trianglePath = Path().apply {
                    moveTo(apex.x, apex.y)
                    lineTo(bottomLeft.x, bottomLeft.y)
                    lineTo(bottomRight.x, bottomRight.y)
                    close()
                }

                drawPath(
                    path = trianglePath,
                    color = Color(0xFF2563EB),
                    style = Stroke(width = 2.5f)
                )

                val midBase = Offset(w / 2f, h - 15f)
                val paintColor = android.graphics.Color.parseColor("#F97316")
                val strokePaint = android.graphics.Paint().apply {
                    color = paintColor
                    strokeWidth = 3f
                    style = android.graphics.Paint.Style.STROKE
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 6f), 0f)
                }

                drawContext.canvas.nativeCanvas.drawLine(
                    apex.x, apex.y, midBase.x, midBase.y, strokePaint
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("2√3 cm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                }
                Text("4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text("4 cm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }
    }
}

@Composable
fun GeometryCircleFigureView() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = Color(0xFF2563EB), radius = size.minDimension / 2 - 4f, style = Stroke(width = 2.5f))
                drawCircle(color = Color(0xFFF97316), radius = 3.dp.toPx(), center = center)
                drawLine(color = Color(0xFFF97316), start = center, end = Offset(center.x + size.width / 2 - 4f, center.y), strokeWidth = 2f)
            }
        }
    }
}

@Composable
fun GeometryRectangleFigureView() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp, 80.dp)) {
                drawRect(color = Color(0xFF2563EB), size = size, style = Stroke(width = 2.5f))
            }
        }
    }
}

@Composable
fun DirectionCompassDiagramView() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(90.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                drawLine(color = Color(0xFF2563EB), start = Offset(center.x, 10f), end = Offset(center.x, size.height - 10f), strokeWidth = 2f)
                drawLine(color = Color(0xFF2563EB), start = Offset(10f, center.y), end = Offset(size.width - 10f, center.y), strokeWidth = 2f)
            }
            Row(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("N", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    Text("S", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
                Text("E", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
        }
    }
}

@Composable
fun BloodRelationDiagramView() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEFF6FF), border = BorderStroke(1.dp, Color(0xFF93C5FD))) {
                    Text("Generation 1: Parents / Grandparents", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Text("↓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0FDF4), border = BorderStroke(1.dp, Color(0xFF86EFAC))) {
                    Text("Generation 2: Self / Siblings / Children", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun CodingDecodingDiagramView() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Original Word", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("A B C D", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                }
                Text("⇒", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Coded Pattern", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text("C D E F", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
        }
    }
}

private fun extractSimpleSteps(text: String): List<String> {
    if (text.isBlank()) return listOf("Analyze the given pattern and apply step-by-step logic.")
    val dotPlaceholder = "\u0000"
    val protectedText = text
        .replace("క్రీ.శ.", "క్రీ${dotPlaceholder}శ${dotPlaceholder}")
        .replace("క్రీ.పూ.", "క్రీ${dotPlaceholder}పూ${dotPlaceholder}")
        .replace("డా.", "డా${dotPlaceholder}")
        .replace("శ్రీ.", "శ్రీ${dotPlaceholder}")
        .replace("బి.ఆర్.", "బి${dotPlaceholder}ఆర్${dotPlaceholder}")
        .replace(Regex("\\b(Dr|Mr|Mrs|Ms|St|Art|No|Vol|Fig|vs|etc)\\."), "$1$dotPlaceholder")
        .replace(Regex("(?<=\\b[A-Za-z])\\."), dotPlaceholder)

    val rawSentences = protectedText.split(Regex("(?:\\r?\\n)+|;\\s*|\\.(?=\\s+|$)"))
        .map { it.replace(dotPlaceholder, ".").trim() }
        .filter { it.isNotBlank() && it.length > 5 }

    if (rawSentences.isEmpty()) {
        return listOf(text)
    }

    return rawSentences.take(4)
}
