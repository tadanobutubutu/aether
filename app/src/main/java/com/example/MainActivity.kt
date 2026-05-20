package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.components.AetherVisualizer
import com.example.ui.components.parse
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.viewmodel.PlanetState
import com.example.ui.viewmodel.SatelliteState

class MainActivity : ComponentActivity() {
    private val viewModel: AetherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge immersive display
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    AetherAppScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AetherAppScreen(
    viewModel: AetherViewModel,
    modifier: Modifier = Modifier
) {
    var rawInputText by remember { mutableStateOf("") }
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    
    val selectedThought by viewModel.selectedThought.collectAsState()
    val planets by viewModel.planets.collectAsState()
    val thoughts by viewModel.thoughts.collectAsState()

    var showManualDialog by remember { mutableStateOf(false) }
    var showGuide by remember { mutableStateOf(true) }

    var activeTab by remember { mutableStateOf("Explorer") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackground)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeTab == "Explorer") {
                // 1. The Immersive Custom physics visualizer canvas (60fps, elastic mapping, space incinerator)
        AetherVisualizer(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
            onThoughtSelected = { satState ->
                viewModel.selectThought(satState)
            }
        )

        // 2. Headings Header Panel with Sophisticated Dark Styling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Title Section (Material 3 Style matches HTML Bijective.v1 header)
                Column {
                    Text(
                        text = "SYSTEM CORE",
                        color = CosmicPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Cosmic icon",
                            tint = CosmicSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Bijective.v1",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right Utility & Pulse Control Center
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showManualDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicSurface)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                            .testTag("add_manual_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add manually",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { showGuide = !showGuide },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicSurface)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                            .testTag("info_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Toggle Guide",
                            tint = if (showGuide) CosmicSecondary else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Floating State Pulse Well Dot
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CosmicSurface)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val breathingAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1420, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "breathingPulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CosmicSecondary.copy(alpha = breathingAlpha))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Stats Grid Panel (Two column layout matching HTML CSS grid)
            val activeSynapses = thoughts.count { it.status != "Completed" }
            val crystallizedCores = thoughts.count { it.status == "Completed" }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active synapses column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CosmicSurface.copy(alpha = 0.85f))
                        .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "ACTIVE THREADS",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (activeSynapses < 10) "0$activeSynapses" else "$activeSynapses",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Crystallized cores column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CosmicSurface.copy(alpha = 0.85f))
                        .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "CRYSTALLIZED CORES",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (crystallizedCores < 10) "0$crystallizedCores" else "$crystallizedCores",
                        color = CosmicSecondary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current state / sub-status bar
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CosmicSurface.copy(alpha = 0.7f))
                    .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = CosmicSecondary,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = statusText,
                        color = if (isAnalyzing) CosmicSecondary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

        } // Close the header Column cleanly here to prevent scope receiver conflicts

        // Beautiful overlapping guide dropdown card
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 185.dp, end = 20.dp)
        ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showGuide,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicSurface.copy(alpha = 0.94f))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CompassCalibration,
                                    contentDescription = null,
                                    tint = CosmicSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "System Instruction Core",
                                    color = TextBright,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "✍️ Speak stream-of-thought into the core below to auto-transcribe.\n\n" +
                                "🪐 Orbits: Drag domains to balance gravity paths.\n\n" +
                                "🌌 Satellites: Long press to transpose orbits, single tap to inspect data metrics, or swipe down to dissolve/incinerate.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

        // 4. API Key Unconfigured Warning notice
        errorMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF7F1D1D).copy(alpha = 0.85f))
                    .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(20.dp))
                    .padding(24.dp)
                    .widthIn(max = 450.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Core Disruption",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To ignite the stream analysis, configure your GEMINI_API_KEY inside the secure Secrets Panel in AI Studio.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error detail: $msg",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 5. Glassmorphic Slider Drawer (Details Card - slide in from lower bottom)
        androidx.compose.animation.AnimatedVisibility(
            visible = selectedThought != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedThought?.let { thought ->
                val planet = planets.find { p -> p.id == thought.categoryId }
                val planetColor = planet?.let { Color.parse(it.colorHex) } ?: CosmicPrimary

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CosmicSurface.copy(alpha = 0.92f))
                        .border(1.5.dp, planetColor.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                        .testTag("thought_detail_drawer")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(planetColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (planet?.name ?: "Musa").uppercase(),
                                color = planetColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            
                            IconButton(onClick = { viewModel.selectThought(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close detailed view", tint = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title
                        Text(
                            text = thought.title,
                            color = TextBright,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Content description
                        Text(
                            text = thought.content,
                            color = TextMuted,
                            fontSize = 14.sp,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Actions row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Secondary dissolve
                            TextButton(
                                onClick = { viewModel.deleteSelectedThought() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dissolve")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Primary complete status
                            if (thought.status != "Completed") {
                                Button(
                                    onClick = { viewModel.completeThought(thought) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CosmicBackground, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Crystallized", color = CosmicBackground, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.reopenThought(thought) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextBright, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Re-Orbit", color = TextBright, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Sticky bottom-pinned Stream-of-consciousness quick input block
        androidx.compose.animation.AnimatedVisibility(
            visible = selectedThought == null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CosmicSurface.copy(alpha = 0.85f))
                    .border(1.dp, CosmicBorder, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        placeholder = {
                            Text(
                                "Speak into the Aether...",
                                color = TextMuted.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextBright,
                            unfocusedTextColor = TextBright
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("aether_speech_input"),
                        singleLine = false,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Crystalize button triggering Gemini API integration
                    Button(
                        onClick = {
                            viewModel.speakIntoAether(rawInputText)
                            rawInputText = ""
                        },
                        enabled = rawInputText.isNotBlank() && !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CosmicPrimary,
                            disabledContainerColor = CosmicSurfaceLighter
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("crystallize_button")
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = TextBright, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Crystallize")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Crystallize", fontWeight = FontWeight.Bold, color = TextBright)
                            }
                        }
                    }
                }
            }
        }

        // 7. Dialog for Manual satellite launchers creation
        if (showManualDialog) {
            var mTitle by remember { mutableStateOf("") }
            var mContent by remember { mutableStateOf("") }
            var mPlanetId by remember { mutableStateOf(1) } // Zenith default

            Dialog(onDismissRequest = { showManualDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CosmicSurface)
                        .border(1.5.dp, CosmicBorder, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Launch Satellite",
                            color = TextBright,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = mTitle,
                            onValueChange = { mTitle = it },
                            label = { Text("Title", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextBright,
                                unfocusedTextColor = TextBright,
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = mContent,
                            onValueChange = { mContent = it },
                            label = { Text("Details", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextBright,
                                unfocusedTextColor = TextBright,
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicBorder
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Selector Choice Buttons
                        Text("Cognitive Orbits Domain:", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            planets.forEach { p ->
                                val planetColor = Color.parse(p.colorHex)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (mPlanetId == p.id) planetColor.copy(alpha = 0.35f) else CosmicSurfaceLighter)
                                        .border(
                                            width = if (mPlanetId == p.id) 2.dp else 1.dp,
                                            color = if (mPlanetId == p.id) planetColor else CosmicBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { mPlanetId = p.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(p.name, color = if (mPlanetId == p.id) TextBright else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showManualDialog = false }) {
                                Text("Cancel", color = TextMuted)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    if (mTitle.isNotBlank()) {
                                        viewModel.createThoughtManually(mTitle, mContent, mPlanetId)
                                        showManualDialog = false
                                    }
                                },
                                enabled = mTitle.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary)
                            ) {
                                Text("Launch Orbit", color = TextBright)
                            }
                        }
                    }
                }
            }
        }

            } else {
                ThreeJsShowcaseView(modifier = Modifier.fillMaxSize())
            }
        }

        // Bottom Navigation Bar with Sophisticated Dark Aesthetics
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CosmicSurface)
                .padding(vertical = 12.dp)
        ) {
            // Draw clean top separation boundary line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CosmicBorder)
                    .align(Alignment.TopCenter)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Explorer Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { activeTab = "Explorer" }
                        .padding(horizontal = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Explorer",
                        tint = if (activeTab == "Explorer") CosmicSecondary else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explorer",
                        color = if (activeTab == "Explorer") CosmicSecondary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Compute Tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { activeTab = "Compute" }
                        .padding(horizontal = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Compute",
                        tint = if (activeTab == "Compute") CosmicSecondary else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compute 3D",
                        color = if (activeTab == "Compute") CosmicSecondary else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ThreeJsShowcaseView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                webViewClient = WebViewClient()
                loadUrl("file:///android_asset/three_showcase.html")
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
