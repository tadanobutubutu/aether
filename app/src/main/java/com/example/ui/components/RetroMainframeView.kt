package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.viewmodel.PlanetState
import com.example.ui.viewmodel.SatelliteState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Lightweight Audio Synthesizer to simulate mechanical mainframe ticks
object RetroBeeper {
    private var toneGen: ToneGenerator? = null
    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_SYSTEM, 45)
        } catch (e: Exception) {
            // Fallback safe ignore
        }
    }
    fun playPunch() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
        } catch (e: Exception) {}
    }
    fun playReset() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 120)
        } catch (e: Exception) {}
    }
    fun playCritical() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (e: Exception) {}
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RetroMainframeView(
    viewModel: AetherViewModel,
    modifier: Modifier = Modifier
) {
    val planets by viewModel.planets.collectAsState()
    val satellites by viewModel.satellites.collectAsState()
    val isLegacyRoute by viewModel.isLegacyRouteEnabled.collectAsState()
    val cgiBufferOverflow by viewModel.cgiBufferOverflow.collectAsState()
    val mainframeLogs by viewModel.mainframeLogs.collectAsState()

    var rawTextToPunch by remember { mutableStateOf("ZENITH") }
    var crtScanlinesEnabled by remember { mutableStateOf(true) }
    var handshakeStatus by remember { mutableStateOf("MEMEX PORT COM3 - MODEM LOCKED") }

    // Selectable bijective telemetry control target
    var selectedTargetIsPlanet by remember { mutableStateOf(true) }
    var selectedTargetId by remember { mutableStateOf<Long>(1L) }

    val scrollState = rememberScrollState()
    val logScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Blinking visual animation for warning lights
    val infiniteTransition = rememberInfiniteTransition(label = "warningBlink")
    val alertAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertAlpha"
    )

    // Automatically follow log updates
    LaunchedEffect(mainframeLogs.size) {
        scope.launch {
            logScrollState.animateScrollTo(logScrollState.maxValue)
        }
    }

    // Trigger critical beep sound alert when CGI cache collapses
    LaunchedEffect(cgiBufferOverflow) {
        if (cgiBufferOverflow) {
            RetroBeeper.playCritical()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030803))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // CRT Green Fluorine ASCII Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF33FF33), RoundedCornerShape(8.dp))
                .background(Color(0xFF071107))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "    ___   _________________  ___________ \n" +
                           "   /   | / ____/_  __/ / / / / ___/ ___/ \n" +
                           "  / /| |/ __/   / / / /_/ /  \\__ \\\\_  \\  \n" +
                           " / ___ / /___  / / / __  /  ___/ /__/ /  \n" +
                           "/_/  |_/_____/ /_/ /_/ /_/  /____/____/  \n" +
                           "    >> COBOL-80 SYSTEM COGNITIVE DOCK TERMINAL <<",
                    color = Color(0xFF33FF33),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (cgiBufferOverflow) Color.Red else Color(0xFF33FF33))
                        )
                        Text(
                            text = if (cgiBufferOverflow) "BUFFER CRITICAL OVERFLOW (SOAP DRIFT)" else "CORE TELEMETRY: REALTIME BIJECTIVE SYNCED",
                            color = if (cgiBufferOverflow) Color.Red else Color(0xFF33FF33),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "SYS TIME: METRIC 2000-05-30",
                        color = Color(0xFF33FF33).copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Interactive Route Toggler & System Flush Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091409)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B4F1B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "1. NETWORK PIPELINE SINK CONFIGURATION",
                    color = Color(0xFF33FF33),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Bijective Experience: Route live ideas through old SOAP or modern JSON highroads. Legacy perl-cgi path creates organic bugs!",
                    color = Color(0xFF88CC88),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLegacyRoute) Color(0xFF112911) else Color(0xFF060E06))
                            .border(1.dp, if (isLegacyRoute) Color(0xFF33FF33) else Color(0xFF1B4F1B), RoundedCornerShape(8.dp))
                            .clickable {
                                RetroBeeper.playPunch()
                                viewModel.setLegacyRouteEnabled(true)
                            }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = if (isLegacyRoute) Color(0xFF33FF33) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("PERL SOAP PORT 80", color = if (isLegacyRoute) Color.White else Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("Active (With 25% Drift Faults)", color = Color(0xFF33FF33).copy(alpha = 0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isLegacyRoute) Color(0xFF112911) else Color(0xFF060E06))
                            .border(1.dp, if (!isLegacyRoute) Color(0xFF33FF33) else Color(0xFF1B4F1B), RoundedCornerShape(8.dp))
                            .clickable {
                                RetroBeeper.playPunch()
                                viewModel.setLegacyRouteEnabled(false)
                            }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (!isLegacyRoute) Color(0xFF33FF33) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("MODERN JSON HIGHWAY", color = if (!isLegacyRoute) Color.White else Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("Secure SSL (Direct REST)", color = Color(0xFF33FF33).copy(alpha = 0.7f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Blinking Critical warning panel when SOAP buffer cache breaks
                if (cgiBufferOverflow) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF4C0808).copy(alpha = alertAlpha))
                            .border(1.5.dp, Color.Red, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.PriorityHigh, contentDescription = "Error", tint = Color.Red, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "SOAP DRIVING DISPLACEMENT BUFFER EXPLODED!",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "COBOL Memory Register Drift has reached limit value (99.2%). Cognitive stream processing is locked offline until cache is flushed.",
                                color = Color(0xFFFF9999),
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    RetroBeeper.playReset()
                                    viewModel.clearCgiOverflow()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFAA1111)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("PATCH DRIFT & FLUSH CGI BUFFER", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. BIJECTIVE TELEMETRY HARNESS: Live Node lists & calibrations dials
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091409)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B4F1B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "2. BIJECTIVE REALTIME TELEMETRY DIALS",
                    color = Color(0xFF33FF33),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "Live physical coordinates on the Compose Explorer: select any constellation node below to recalibrate its angle and orbit radius dynamically!",
                    color = Color(0xFF88CC88),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render dynamic table list of currently existing physical states on the Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF1B4F1B))
                ) {
                    val listScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(listScroll)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "=== HIGH-DIMENSIONAL COGNITIVE NODES ===",
                            color = Color(0xFF33FF33).copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        // 1. Show planets (Constellation Orbits)
                        planets.forEach { p ->
                            val isSelected = selectedTargetIsPlanet && selectedTargetId == p.id.toLong()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFF183818) else Color.Transparent)
                                    .clickable {
                                        RetroBeeper.playPunch()
                                        selectedTargetIsPlanet = true
                                        selectedTargetId = p.id.toLong()
                                        rawTextToPunch = p.name.uppercase()
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🪐 [ORBIT] ID-${p.id}: ${p.name.uppercase()}",
                                    color = if (isSelected) Color(0xFF33FF33) else Color(0xFFBBFFBB),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "R:${p.orbitRadius.toInt()}dp | θ:${String.format(java.util.Locale.US, "%.1f", p.angle)}°",
                                    color = Color(0xFF33FF33).copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // 2. Show satellites (Active thoughts)
                        satellites.forEach { s ->
                            val isSelected = !selectedTargetIsPlanet && selectedTargetId == s.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFF183818) else Color.Transparent)
                                    .clickable {
                                        RetroBeeper.playPunch()
                                        selectedTargetIsPlanet = false
                                        selectedTargetId = s.id
                                        rawTextToPunch = s.title.uppercase().take(12)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "☄️ [SAT] ID-${s.id}: ${s.title.uppercase().take(14)}",
                                    color = if (isSelected) Color(0xFF33FF33) else Color(0xFFBBFFBB),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "D:${s.relativeDistance.toInt()}dp | θ:${String.format(java.util.Locale.US, "%.1f", s.angle)}°",
                                    color = Color(0xFF33FF33).copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (planets.isEmpty() && satellites.isEmpty()) {
                            Text(
                                "NO PHYSICS COGNITIVE NODES AVAILABLE.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calibration dials for selected target
                val currentTargetName = if (selectedTargetIsPlanet) {
                    planets.find { it.id == selectedTargetId.toInt() }?.name ?: "None"
                } else {
                    satellites.find { it.id == selectedTargetId }?.title ?: "None"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF225522), RoundedCornerShape(8.dp))
                        .background(Color(0xFF050E05))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "⚡ CALIBRATION INTERFACE: [ $currentTargetName ]".uppercase(),
                            color = Color(0xFF33FF33),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Dial Section 1: Angle rotation
                            Column(modifier = Modifier.weight(1f)) {
                                Text("MATH ANGULAR ROTATION (θ)", color = Color(0xFF88CC88), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            RetroBeeper.playPunch()
                                            if (selectedTargetIsPlanet) {
                                                viewModel.updatePlanetOrbitFromMainframe(selectedTargetId.toInt(), 0f, -0.26f) // -15 degrees
                                            } else {
                                                viewModel.updateSatelliteOrbitFromMainframe(selectedTargetId, -0.26f, 0f)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3D1B)),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("ROT -15°", color = Color(0xFF33FF33), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Button(
                                        onClick = {
                                            RetroBeeper.playPunch()
                                            if (selectedTargetIsPlanet) {
                                                viewModel.updatePlanetOrbitFromMainframe(selectedTargetId.toInt(), 0f, 0.26f) // +15 degrees
                                            } else {
                                                viewModel.updateSatelliteOrbitFromMainframe(selectedTargetId, 0.26f, 0f)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3D1B)),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("ROT +15°", color = Color(0xFF33FF33), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            // Dial Section 2: Distance/Radius expansion
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ORBIT RADIUS SPAN (R)", color = Color(0xFF88CC88), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            RetroBeeper.playPunch()
                                            if (selectedTargetIsPlanet) {
                                                viewModel.updatePlanetOrbitFromMainframe(selectedTargetId.toInt(), -15f, 0f)
                                            } else {
                                                viewModel.updateSatelliteOrbitFromMainframe(selectedTargetId, 0f, -12f)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3D1B)),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PAN -15dp", color = Color(0xFF33FF33), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Button(
                                        onClick = {
                                            RetroBeeper.playPunch()
                                            if (selectedTargetIsPlanet) {
                                                viewModel.updatePlanetOrbitFromMainframe(selectedTargetId.toInt(), 15f, 0f)
                                            } else {
                                                viewModel.updateSatelliteOrbitFromMainframe(selectedTargetId, 0f, 12f)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3D1B)),
                                        contentPadding = PaddingValues(horizontal = 8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PAN +15dp", color = Color(0xFF33FF33), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. HOLLERITH PUNCHCARD SIMULATION ENCODER CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091409)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B4F1B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "[ COBOL / HOLLERITH 80-COL PUNCHCARD PHYSICAL CORRESPONDENCE ]",
                    color = Color(0xFF33FF33),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                Text(
                    text = "Bijections principle: Character coordinates compute physical puncture holes under classic COBOL-80 hardware protocols.",
                    color = Color(0xFF88CC88),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = rawTextToPunch,
                    onValueChange = {
                        val uppercaseVal = it.uppercase()
                        if (uppercaseVal.length <= 12) {
                            rawTextToPunch = uppercaseVal
                            RetroBeeper.playPunch()
                        }
                    },
                    label = { Text("PUNCH FIELD COMPLIANCE (MAX 12 CHR)", color = Color(0xFF44AA44), fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF55FF55),
                        unfocusedTextColor = Color(0xFF33DD33),
                        focusedContainerColor = Color.Black,
                        unfocusedContainerColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF33FF33),
                        unfocusedIndicatorColor = Color(0xFF1B4F1B)
                    ),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real retro cardboard punch design
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEADBCE)) // Creamy cardboard retro color
                        .border(2.dp, Color(0xFF908070))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("AETHER BIJECTIVE COBOL-80 CARD", color = Color(0xFF605040), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("PUNCH SERIAL: 420-79A", color = Color(0xFF605040), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Main physical grid loop. Each column renders based on ascii characters
                        for (row in 0..4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                if (rawTextToPunch.isEmpty()) {
                                    for (dot in 0..11) {
                                        Text("·", color = Color(0xFF807060), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    }
                                } else {
                                    rawTextToPunch.forEachIndexed { colIdx, char ->
                                        val punches = (char.code + row + colIdx) % 3 == 0
                                        Text(
                                            text = if (punches) "▇" else "·",
                                            color = if (punches) Color(0xFFBB1111) else Color(0xFF908070),
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "HOLLERITH: [ ${if (rawTextToPunch.isEmpty()) "EMPTY" else rawTextToPunch} ]",
                                color = Color(0xFF302010),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Traceback logger output window of legacy simulated executions
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF091409)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B4F1B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "[ RETROTRACE SINK LOG STREAM ]",
                        color = Color(0xFF33FF33),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "PORT: 80 / COM3",
                        color = Color(0xFF33FF33).copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF1B4F1B))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(logScrollState)
                    ) {
                        mainframeLogs.forEach { log ->
                            Text(
                                text = log,
                                color = when {
                                    log.contains("CRITICAL") || log.contains("WARNING") -> Color.Red
                                    log.contains("BIJECTIVE") || log.contains("CALIBRATE") -> Color(0xFF00FFCC)
                                    log.contains("POST") || log.contains("SOAP") -> Color(0xFFFFCC33)
                                    else -> Color(0xFF33FF33)
                                },
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
}
