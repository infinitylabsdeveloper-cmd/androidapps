package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import java.util.*
import kotlin.math.*

enum class CalcTab { CALCULATOR, CONVERTER, GRAPH }

@Composable
fun ScientificCalculatorDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    var selectedTab by remember { mutableStateOf(CalcTab.CALCULATOR) }
    var isFullScreen by remember { mutableStateOf(false) }
    var isLightTheme by remember { mutableStateOf(false) } // Theme state
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var history by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var isDegree by remember { mutableStateOf(true) }
    var showHistory by remember { mutableStateOf(false) }
    var showConstants by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf("") }
    var memoryValue by remember { mutableStateOf(0.0) }

    val calculate = {
        if (expression.isNotEmpty()) {
            try {
                // Pre-process expression for degree/radian
                var processedExpr = expression
                    .replace("×", "*")
                    .replace("÷", "/")
                    .replace("π", "PI")
                    .replace("e", "E")
                    .replace("log\\(".toRegex(), "log10(")
                    .replace("ln\\(".toRegex(), "log(")

                // Handle trig functions for DEG
                if (isDegree) {
                    processedExpr = processedExpr
                        .replace("sin\\(".toRegex(), "sin(pi/180*")
                        .replace("cos\\(".toRegex(), "cos(pi/180*")
                        .replace("tan\\(".toRegex(), "tan(pi/180*")
                        .replace("asin\\(".toRegex(), "180/pi*asin(")
                        .replace("acos\\(".toRegex(), "180/pi*acos(")
                        .replace("atan\\(".toRegex(), "180/pi*atan(")
                }

                // Custom Factorial Function for exp4j
                val factFunc = object : Function("fact", 1) {
                    override fun apply(vararg args: Double): Double {
                        val n = args[0]
                        if (n < 0) return Double.NaN
                        var res = 1.0
                        for (i in 2..n.toInt()) res *= i
                        return res
                    }
                }

                val e = ExpressionBuilder(processedExpr)
                    .function(factFunc)
                    .build()
                val eval = e.evaluate()
                
                if (eval.isInfinite() || eval.isNaN()) {
                    result = "Error: Division by Zero"
                } else {
                    val finalResult = if (eval % 1 == 0.0) eval.toLong().toString() else "%.8f".format(java.util.Locale.US, eval).trimEnd('0').trimEnd('.')
                    result = finalResult
                    history = (listOf(expression to finalResult) + history).take(50)
                    lastResult = finalResult
                }
            } catch (e: ArithmeticException) {
                result = "Error: Math Error"
            } catch (e: Exception) {
                result = "Error: Invalid Input"
            }
        }
    }

    val onAction = { action: String ->
        when (action) {
            "AC" -> {
                expression = ""
                result = ""
            }
            "C" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
            "MC" -> memoryValue = 0.0
            "MR" -> {
                val memStr = if (memoryValue % 1 == 0.0) memoryValue.toLong().toString() else memoryValue.toString()
                expression += memStr
            }
            "MS" -> {
                val value = result.toDoubleOrNull() ?: lastResult.toDoubleOrNull() ?: 0.0
                memoryValue = value
            }
            "M+" -> {
                val value = result.toDoubleOrNull() ?: lastResult.toDoubleOrNull() ?: 0.0
                memoryValue += value
            }
            "=" -> calculate()
            "Ans" -> expression += lastResult
            "sin", "cos", "tan", "log", "ln", "sqrt", "asin", "acos", "atan", "abs" -> expression += "$action("
            "fact" -> expression += "fact("
            "^" -> expression += "^"
            "π" -> expression += "π"
            "e" -> expression += "e"
            "(" -> expression += "("
            ")" -> expression += ")"
            else -> expression += action
        }
    }

    val copyToClipboard = { text: String ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator Result", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val themeBg = if (isLightTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
        val themeSurface = if (isLightTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
        val themeAccent = if (isLightTheme) Color(0xFF2563EB) else Color(0xFFFFD700)
        val themeTextPrimary = if (isLightTheme) Color(0xFF0F172A) else Color.White
        val themeTextSecondary = if (isLightTheme) Color(0xFF64748B) else Color(0xFF94A3B8)
        val themeButtonSecondary = if (isLightTheme) Color(0xFFE2E8F0) else Color(0xFF334155)

        Surface(
            modifier = if (isFullScreen) Modifier.fillMaxSize() else Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp),
            color = themeBg,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = themeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Scientific Calculator",
                            color = themeTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isLightTheme = !isLightTheme }) {
                            Icon(
                                imageVector = if (isLightTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Theme",
                                tint = themeTextPrimary
                            )
                        }
                        IconButton(onClick = { showHistory = !showHistory }) {
                            Icon(Icons.Default.History, "History", tint = if (showHistory) themeAccent else themeTextPrimary)
                        }
                        IconButton(onClick = { isFullScreen = !isFullScreen }) {
                            Icon(
                                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Toggle Fullscreen",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close", tint = Color.White)
                        }
                    }
                }

                // Tab Selector
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFFFFD700),
                    indicator = { tabPositions ->
                        if (selectedTab.ordinal < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = Color(0xFFFFD700)
                            )
                        }
                    },
                    divider = {}
                ) {
                    CalcTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab; showHistory = false },
                            text = { 
                                Text(
                                    tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
                
                if (showHistory) {
                    HistoryPanel(history = history, onSelect = { expr, res -> 
                        expression = expr
                        result = res
                        showHistory = false
                    }, onClear = { history = emptyList() })
                } else if (showConstants) {
                    ConstantsPanel(onSelect = { value ->
                        expression += value
                        showConstants = false
                    })
                } else if (selectedTab == CalcTab.CONVERTER) {
                    UnitConverterView(isFullScreen)
                } else if (selectedTab == CalcTab.GRAPH) {
                    GraphingView(isFullScreen)
                } else {
                    // Display Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                            .padding(16.dp)
                            .then(
                                if (result.startsWith("Error")) {
                                    Modifier.border(1.dp, Color(0xFFF87171).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                } else Modifier
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = expression,
                            color = Color(0xFF94A3B8),
                            fontSize = if (isFullScreen) 28.sp else 22.sp,
                            textAlign = TextAlign.End,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (result.isNotEmpty() && !result.startsWith("Error")) {
                                IconButton(
                                    onClick = { copyToClipboard(result) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, "Copy", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = result.ifEmpty { "0" },
                                color = if (result.startsWith("Error")) Color(0xFFF87171) else Color.White,
                                fontSize = if (isFullScreen) if (result.startsWith("Error")) 28.sp else 48.sp else if (result.startsWith("Error")) 20.sp else 36.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Keypad
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f)
                            .background(Color(0xFF1E293B))
                            .padding(8.dp)
                    ) {
                        // Scientific Mode & Memory Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF334155))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "RAD",
                                    color = if (!isDegree) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (!isDegree) FontWeight.Bold else FontWeight.Normal
                                )
                                Switch(
                                    checked = isDegree,
                                    onCheckedChange = { isDegree = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFFFFD700),
                                        checkedTrackColor = Color(0xFF334155),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFF0F172A),
                                        checkedBorderColor = Color(0xFFFFD700),
                                        uncheckedBorderColor = Color(0xFF94A3B8)
                                    ),
                                    modifier = Modifier.scale(0.7f)
                                )
                                Text(
                                    "DEG",
                                    color = if (isDegree) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (isDegree) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            listOf("MC", "MR", "MS", "M+", "CONST").forEach { label ->
                                ModeButton(
                                    label = label, 
                                    isSelected = if (label == "CONST") showConstants else false
                                ) {
                                    if (label == "CONST") {
                                        showConstants = !showConstants
                                        showHistory = false
                                    } else {
                                        onAction(label)
                                    }
                                }
                            }
                        }

                        val buttons = when {
                            isLandscape -> listOf(
                                listOf("sin", "cos", "tan", "asin", "acos", "atan", "AC", "C", "%", "÷"),
                                listOf("log", "ln", "sqrt", "fact", "abs", "^", "7", "8", "9", "×"),
                                listOf("(", ")", "π", "e", "Ans", ".", "4", "5", "6", "-"),
                                listOf("MC", "MR", "MS", "M+", "CONST", " ", "1", "2", "3", "+"),
                                listOf(" ", " ", " ", " ", " ", " ", "0", " ", " ", "=")
                            )
                            isFullScreen -> listOf(
                                listOf("sin", "cos", "tan", "sqrt", "^"),
                                listOf("log", "ln", "(", ")", "π"),
                                listOf("AC", "C", "%", "÷"),
                                listOf("7", "8", "9", "×"),
                                listOf("4", "5", "6", "-"),
                                listOf("1", "2", "3", "+"),
                                listOf("0", ".", "Ans", "=")
                            )
                            else -> listOf(
                                listOf("sin", "cos", "tan", "sqrt"),
                                listOf("AC", "C", "%", "÷"),
                                listOf("7", "8", "9", "×"),
                                listOf("4", "5", "6", "-"),
                                listOf("1", "2", "3", "+"),
                                listOf("0", ".", "Ans", "=")
                            )
                        }

                        buttons.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { label ->
                                    CalcButton(
                                        label = label,
                                        modifier = Modifier.weight(if (label == "0" && !isFullScreen) 1f else 1f),
                                        onClick = { onAction(label) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, label = "scale")

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF475569)),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .scale(scale)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFFFFD700) else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CalcButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    val containerColor = when {
        label == "=" -> Color(0xFF2563EB)
        label == "AC" || label == "C" -> Color(0xFFEF4444)
        label in listOf("×", "÷", "-", "+", "%") -> Color(0xFF334155)
        label in listOf("sin", "cos", "tan", "sqrt", "log", "ln", "(", ")", "π", "e", "^", "Ans", "asin", "acos", "atan", "fact", "abs") -> Color(0xFF1E293B).copy(alpha = 0.8f)
        else -> Color(0xFF334155).copy(alpha = 0.5f)
    }
    
    val textColor = when {
        label == "=" || label == "AC" || label == "C" -> Color.White
        label in listOf("×", "÷", "-", "+", "%") -> Color(0xFFFFD700)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length > 2) 14.sp else 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

enum class UnitCategory { LENGTH, WEIGHT, TEMPERATURE }

@Composable
fun GraphingView(isFullScreen: Boolean) {
    var functionInput by remember { mutableStateOf("sin(x)") }
    var expressionError by remember { mutableStateOf(false) }
    var zoomLevel by remember { mutableFloatStateOf(10f) } // Default 10 units across
    var showGrid by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Function Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = functionInput,
                onValueChange = { 
                    functionInput = it
                    expressionError = false
                },
                label = { Text("y =", color = Color(0xFFFFD700)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF475569),
                    focusedBorderColor = Color(0xFFFFD700),
                    cursorColor = Color(0xFFFFD700),
                    focusedLabelColor = Color(0xFFFFD700),
                    unfocusedLabelColor = Color(0xFF94A3B8)
                ),
                isError = expressionError,
                trailingIcon = {
                    if (expressionError) {
                        Icon(Icons.Default.Error, "Error", tint = Color(0xFFEF4444))
                    }
                }
            )

            // Clear Button
            IconButton(
                onClick = { functionInput = "" },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF334155), RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Clear, "Clear", tint = Color.White)
            }
        }

        if (expressionError) {
            Text("Invalid expression. Try 'sin(x)', 'x^2', etc.", color = Color(0xFFEF4444), fontSize = 12.sp)
        }

        // Quick Function Presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("sin(x)", "cos(x)", "x^2", "abs(x)", "log10(x)").forEach { preset ->
                Surface(
                    onClick = { functionInput = preset },
                    color = if (functionInput == preset) Color(0xFFFFD700).copy(alpha = 0.2f) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (functionInput == preset) Color(0xFFFFD700) else Color(0xFF475569)),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        preset,
                        color = if (functionInput == preset) Color(0xFFFFD700) else Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Zoom & Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.ZoomIn, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            Slider(
                value = zoomLevel,
                onValueChange = { zoomLevel = it },
                valueRange = 1f..50f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFD700),
                    activeTrackColor = Color(0xFFFFD700),
                    inactiveTrackColor = Color(0xFF334155)
                )
            )
            Text(
                "Range: ${zoomLevel.toInt()}",
                color = Color.White,
                fontSize = 12.sp
            )
        }

        // Graph Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF020617)) // Deeper black for better visibility
                .border(2.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2f
                val centerY = height / 2f
                val scaleFactor = width / zoomLevel 

                // 1. Draw Grid
                if (showGrid) {
                    val step = scaleFactor
                    var currentX = centerX % step
                    while (currentX < width) {
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(currentX, 0f),
                            end = Offset(currentX, height),
                            strokeWidth = 1f
                        )
                        currentX += step
                    }
                    var currentY = centerY % step
                    while (currentY < height) {
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, currentY),
                            end = Offset(width, currentY),
                            strokeWidth = 1f
                        )
                        currentY += step
                    }
                }

                // 2. Draw Axes
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, height),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(0f, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = 2f
                )

                // 3. Draw Axis Labels (Numeric)
                // We'll draw a few labels for context
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                
                drawContext.canvas.nativeCanvas.apply {
                    drawText("0", centerX - 20f, centerY + 30f, textPaint)
                    drawText("x", width - 20f, centerY - 10f, textPaint)
                    drawText("y", centerX + 20f, 30f, textPaint)
                }

                // 4. Plot Function
                try {
                    val points = mutableListOf<Offset>()
                    val stepSize = zoomLevel / 200f // 200 points across the view
                    val builder = ExpressionBuilder(functionInput).variable("x")
                    val exp = builder.build()

                    // Calculate range based on zoom
                    val halfRange = zoomLevel / 2f
                    
                    for (i in 0..400) { // High density plot
                        val xVal = -halfRange + (i * (zoomLevel / 400f))
                        exp.setVariable("x", xVal.toDouble())
                        val yVal = exp.evaluate()

                        val canvasX = centerX + (xVal * scaleFactor)
                        val canvasY = centerY - (yVal.toFloat() * scaleFactor) // Invert Y

                        // Only add if within or near visible area to allow lines to connect correctly
                        if (canvasY >= -height && canvasY <= height * 2) {
                            points.add(Offset(canvasX, canvasY))
                        } else {
                            // If we hit a jump/asymptote, draw what we have and start a new list
                            if (points.size > 1) {
                                for (j in 0 until points.size - 1) {
                                    drawLine(
                                        color = Color(0xFF3B82F6), // Professional blue plot line
                                        start = points[j],
                                        end = points[j + 1],
                                        strokeWidth = 4f
                                    )
                                }
                            }
                            points.clear()
                        }
                    }

                    // Draw final segment
                    if (points.size > 1) {
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = Color(0xFF3B82F6),
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 4f
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Silently handle parser errors during typing
                }
            }
        }
        
        Text(
            "Use 'x' as variable. Adjust slider to zoom.",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UnitConverterView(isFullScreen: Boolean) {
    var category by remember { mutableStateOf(UnitCategory.LENGTH) }
    var inputValue by remember { mutableStateOf("") }
    var outputValue by remember { mutableStateOf("") }
    
    val unitsMap = mapOf(
        UnitCategory.LENGTH to listOf("Kilometer", "Meter", "Centimeter", "Mile", "Foot"),
        UnitCategory.WEIGHT to listOf("Kilogram", "Gram", "Pound", "Ounce"),
        UnitCategory.TEMPERATURE to listOf("Celsius", "Fahrenheit", "Kelvin")
    )
    
    var fromUnit by remember(category) { mutableStateOf(unitsMap[category]!![0]) }
    var toUnit by remember(category) { mutableStateOf(unitsMap[category]!![1]) }

    fun performConversion() {
        val input = inputValue.toDoubleOrNull() ?: return
        
        val result = when (category) {
            UnitCategory.LENGTH -> convertLength(input, fromUnit, toUnit)
            UnitCategory.WEIGHT -> convertWeight(input, fromUnit, toUnit)
            UnitCategory.TEMPERATURE -> convertTemperature(input, fromUnit, toUnit)
        }
        
        outputValue = if (result % 1 == 0.0) result.toLong().toString() else "%.4f".format(java.util.Locale.US, result).trimEnd('0').trimEnd('.')
    }

    LaunchedEffect(inputValue, fromUnit, toUnit, category) {
        if (inputValue.isEmpty()) {
            outputValue = ""
        } else {
            performConversion()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitCategory.values().forEach { cat ->
                ModeButton(
                    label = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                    isSelected = category == cat,
                    onClick = { category = cat }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input Area
        Column(modifier = Modifier.weight(if (isFullScreen) 1f else 0.8f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ConversionBox(
                label = "From",
                value = inputValue,
                onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null || it == "-") inputValue = it },
                selectedUnit = fromUnit,
                units = unitsMap[category]!!,
                onUnitChange = { fromUnit = it }
            )
            
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(onClick = {
                    val temp = fromUnit
                    fromUnit = toUnit
                    toUnit = temp
                }) {
                    Icon(Icons.Default.SwapVert, "Swap", tint = Color(0xFFFFD700))
                }
            }
            
            ConversionBox(
                label = "To",
                value = outputValue,
                onValueChange = {},
                readOnly = true,
                selectedUnit = toUnit,
                units = unitsMap[category]!!,
                onUnitChange = { toUnit = it }
            )
        }
        
        // Numeric Keypad for input
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isFullScreen) 0.6f else 1f)
                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            val buttons = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf(".", "0", "C")
            )
            
            buttons.forEach { row ->
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { char ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .scale(scale)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (char == "C") Color(0xFFEF4444) else Color(0xFF334155))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(color = Color.White),
                                    onClick = {
                                        when (char) {
                                            "C" -> inputValue = ""
                                            else -> {
                                                if (char == "." && inputValue.contains(".")) return@clickable
                                                inputValue += char
                                            }
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(char, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    selectedUnit: String,
    units: List<String>,
    onUnitChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value.ifEmpty { "0" },
                color = if (readOnly) Color(0xFFFFD700) else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Box {
                Surface(
                    onClick = { expanded = true },
                    color = Color(0xFF334155),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedUnit, color = Color.White, fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                    }
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF334155))
                ) {
                    units.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit, color = Color.White) },
                            onClick = {
                                onUnitChange(unit)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

fun convertLength(value: Double, from: String, to: String): Double {
    val meters = when (from) {
        "Kilometer" -> value * 1000.0
        "Meter" -> value
        "Centimeter" -> value / 100.0
        "Mile" -> value * 1609.34
        "Foot" -> value * 0.3048
        else -> value
    }
    return when (to) {
        "Kilometer" -> meters / 1000.0
        "Meter" -> meters
        "Centimeter" -> meters * 100.0
        "Mile" -> meters / 1609.34
        "Foot" -> meters / 0.3048
        else -> meters
    }
}

fun convertWeight(value: Double, from: String, to: String): Double {
    val grams = when (from) {
        "Kilogram" -> value * 1000.0
        "Gram" -> value
        "Pound" -> value * 453.592
        "Ounce" -> value * 28.3495
        else -> value
    }
    return when (to) {
        "Kilogram" -> grams / 1000.0
        "Gram" -> grams
        "Pound" -> grams / 453.592
        "Ounce" -> grams / 28.3495
        else -> grams
    }
}

fun convertTemperature(value: Double, from: String, to: String): Double {
    val celsius = when (from) {
        "Celsius" -> value
        "Fahrenheit" -> (value - 32.0) * 5.0 / 9.0
        "Kelvin" -> value - 273.15
        else -> value
    }
    return when (to) {
        "Celsius" -> celsius
        "Fahrenheit" -> (celsius * 9.0 / 5.0) + 32.0
        "Kelvin" -> celsius + 273.15
        else -> celsius
    }
}

@Composable
fun ConstantsPanel(onSelect: (String) -> Unit) {
    val constants = listOf(
        Triple("π", Math.PI.toString(), "Pi"),
        Triple("e", Math.E.toString(), "Euler's Number"),
        Triple("c", "299792458", "Speed of Light (m/s)"),
        Triple("G", "6.67430e-11", "Gravitational Constant"),
        Triple("h", "6.62607015e-34", "Planck's Constant"),
        Triple("Na", "6.02214076e23", "Avogadro's Number"),
        Triple("R", "8.314462618", "Gas Constant"),
        Triple("k", "1.380649e-23", "Boltzmann Constant"),
        Triple("me", "9.1093837e-31", "Electron Mass (kg)"),
        Triple("φ", "1.6180339887", "Golden Ratio")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Text(
            "Scientific Constants",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(constants) { (symbol, value, description) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(value) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            symbol,
                            color = Color(0xFFFFD700),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            description,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        Button(
            onClick = { /* Handled by showConstants state in parent */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
        ) {
            Text("Back to Calculator")
        }
    }
}

@Composable
fun HistoryPanel(history: List<Pair<String, String>>, onSelect: (String, String) -> Unit, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calculation History", color = Color.White, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) {
                Text("Clear History", color = Color(0xFFEF4444))
            }
        }
        
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No history yet", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history) { (expr, res) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(expr, res) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(expr, color = Color(0xFF94A3B8), fontSize = 14.sp)
                            Text("= $res", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
