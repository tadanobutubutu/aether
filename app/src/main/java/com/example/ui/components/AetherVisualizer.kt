package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherViewModel
import com.example.ui.viewmodel.PlanetState
import com.example.ui.viewmodel.SatelliteState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AetherVisualizer(
    viewModel: AetherViewModel,
    modifier: Modifier = Modifier,
    onThoughtSelected: (SatelliteState) -> Unit
) {
    val planets by viewModel.planets.collectAsState()
    val satellites by viewModel.satellites.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var canvasWidth by remember { mutableStateOf(1f) }
    var canvasHeight by remember { mutableStateOf(1f) }

    val density = LocalDensity.current
    val touchThreshold = with(density) { 32.dp.toPx() }

    // Active drag tracking
    var draggedPlanetId by remember { mutableStateOf<Int?>(null) }
    var draggedSatelliteId by remember { mutableStateOf<Long?>(null) }
    var isActuallyDragged by remember { mutableStateOf(false) }

    // Centered Infinite breathing transitions for the central Aether Core sun
    val infiniteTransition = rememberInfiniteTransition(label = "core_pulse")
    val corePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val coreAuraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )
    val coreRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Physics Engine Clock tick loop running natively in Compose
    LaunchedEffect(Unit) {
        while (isActive) {
            if (canvasWidth > 1f && canvasHeight > 1f) {
                viewModel.updatePhysics(canvasWidth, canvasHeight)
            }
            delay(32) // ~30fps stable updates (Delta time maintains natural speed)
        }
    }

    // Stable pointer references preventing 60Hz gesture detector tear and recreation
    val currentPlanets by rememberUpdatedState(planets)
    val currentSatellites by rememberUpdatedState(satellites)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(canvasWidth, canvasHeight) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isActuallyDragged = false
                        // Check if touching any satellite node first (high priority target)
                        var matchedSat: SatelliteState? = null
                        var minSatDist = touchThreshold
                        currentSatellites.forEach { s ->
                            val dx = offset.x - s.x
                            val dy = offset.y - s.y
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            if (dist < minSatDist) {
                                minSatDist = dist
                                matchedSat = s
                            }
                        }

                        if (matchedSat != null) {
                            draggedSatelliteId = matchedSat!!.id
                        } else {
                            // Check if touching any Planet node
                            var matchedPlanet: PlanetState? = null
                            var minPlanetDist = touchThreshold * 1.5f
                            currentPlanets.forEach { p ->
                                val dx = offset.x - p.x
                                val dy = offset.y - p.y
                                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                                if (dist < minPlanetDist) {
                                    minPlanetDist = dist
                                    matchedPlanet = p
                                }
                            }
                            if (matchedPlanet != null) {
                                draggedPlanetId = matchedPlanet!!.id
                                viewModel.onPlanetDragged(matchedPlanet!!.id, offset.x, offset.y, canvasWidth, canvasHeight)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        isActuallyDragged = true
                        val currentDragPos = change.position
                        draggedSatelliteId?.let { satId ->
                            viewModel.onSatelliteDragged(satId, currentDragPos.x, currentDragPos.y)
                        } ?: draggedPlanetId?.let { planetId ->
                            viewModel.onPlanetDragged(planetId, currentDragPos.x, currentDragPos.y, canvasWidth, canvasHeight)
                        }
                    },
                    onDragEnd = {
                        draggedSatelliteId?.let { satId ->
                            if (!isActuallyDragged) {
                                // TAP: Open thoughts detail
                                val sat = currentSatellites.find { it.id == satId }
                                if (sat != null) {
                                    onThoughtSelected(sat)
                                }
                            } else {
                                // DRAG ENDED: Gravity check / incinerate
                                viewModel.onSatelliteDragEnded(satId, canvasWidth, canvasHeight)
                            }
                            draggedSatelliteId = null
                        } ?: draggedPlanetId?.let { planetId ->
                            viewModel.onPlanetDragEnded(planetId)
                            draggedPlanetId = null
                        }
                    },
                    onDragCancel = {
                        draggedSatelliteId?.let { satId ->
                            viewModel.onSatelliteDragEnded(satId, canvasWidth, canvasHeight)
                            draggedSatelliteId = null
                        } ?: draggedPlanetId?.let { planetId ->
                            viewModel.onPlanetDragEnded(planetId)
                            draggedPlanetId = null
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            canvasWidth = size.width
            canvasHeight = size.height

            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // 1. Draw Space background grid
            val gridSpacing = 80f
            val gridColor = CosmicBorder.copy(alpha = 0.25f)
            val strokeWidth = 1f

            // Vertical grid lines
            var currentGridX = 0f
            while (currentGridX < size.width) {
                drawLine(gridColor, Offset(currentGridX, 0f), Offset(currentGridX, size.height), strokeWidth)
                currentGridX += gridSpacing
            }
            // Horizontal grid lines
            var currentGridY = 0f
            while (currentGridY < size.height) {
                drawLine(gridColor, Offset(0f, currentGridY), Offset(size.width, currentGridY), strokeWidth)
                currentGridY += gridSpacing
            }

            // 2. Draw Incinerator garbage gravity well at the bottom (shows only when satellite is dragged)
            val isAnySatDragged = satellites.any { it.isDragged }
            if (isAnySatDragged) {
                val incineratorX = centerX
                val incineratorY = size.height - 130f
                val baseRadius = 85f
                
                // Pulsing glowing red incinerator well
                val alphaPulse = coreAuraAlpha * 0.7f + 0.1f
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = baseRadius + (corePulseScale * 18f),
                    center = Offset(incineratorX, incineratorY),
                    alpha = alphaPulse * 0.4f,
                    style = Stroke(width = 3f)
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444).copy(alpha = alphaPulse * 0.5f), Color.Transparent),
                        center = Offset(incineratorX, incineratorY),
                        radius = baseRadius * 1.5f
                    ),
                    center = Offset(incineratorX, incineratorY),
                    radius = baseRadius * 1.5f
                )

                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 35f,
                    center = Offset(incineratorX, incineratorY),
                    alpha = 0.7f
                )
            }

            // 3. Draw Orbit Track lines for planets
            planets.forEach { planet ->
                val scaleOrbitRadius = planet.orbitRadius * 2.5f
                drawCircle(
                    color = CosmicBorder.copy(alpha = 0.35f),
                    radius = scaleOrbitRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 15f), 0f)
                    )
                )
            }

            // 4. Draw Elastic vector lines connecting dragged satellites to their origin planets (Bijective mapping)
            satellites.forEach { sat ->
                if (sat.isDragged) {
                    val host = planets.find { p -> p.id == sat.planetId }
                    if (host != null) {
                        val hostColor = Color.parse(host.colorHex)
                        // Gradient connection representing gravity pull
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(hostColor.copy(alpha = 0.8f), CosmicPrimary.copy(alpha = 0.4f)),
                                start = Offset(host.x, host.y),
                                end = Offset(sat.x, sat.y)
                            ),
                            start = Offset(host.x, host.y),
                            end = Offset(sat.x, sat.y),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        
                        // Dynamic gravity ring enclosing host planet
                        drawCircle(
                            color = hostColor,
                            radius = host.size * 2.5f + 15f,
                            center = Offset(host.x, host.y),
                            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)),
                            alpha = 0.5f
                        )
                    }
                }
            }

            // 5. Draw Planet Orbs (Cognitive Gravity anchors)
            planets.forEach { planet ->
                val planetColor = Color.parse(planet.colorHex)
                val dipSize = planet.size * 2f

                // Glow aura around the planet
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(planetColor.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(planet.x, planet.y),
                        radius = dipSize * 2.1f
                    ),
                    center = Offset(planet.x, planet.y),
                    radius = dipSize * 2.1f
                )

                // Main planet circle
                drawCircle(
                    color = planetColor,
                    radius = dipSize,
                    center = Offset(planet.x, planet.y)
                )

                // Shiny specular highlights (3D volumetric spheres feel)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                        center = Offset(planet.x - dipSize * 0.3f, planet.y - dipSize * 0.3f),
                        radius = dipSize * 0.7f
                    ),
                    center = Offset(planet.x - dipSize * 0.3f, planet.y - dipSize * 0.3f),
                    radius = dipSize * 0.7f
                )

                // Optional name caption under planet (sleek visual cue)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                        alpha = 180
                    }
                    drawText(planet.name, planet.x, planet.y + dipSize + 40f, paint)
                }
            }

            // 6. Draw Satellites (glowing items orbiting planets)
            satellites.forEach { sat ->
                val host = planets.find { p -> p.id == sat.planetId }
                val satColor = host?.let { Color.parse(it.colorHex) } ?: CosmicPrimary
                val baseRadius = 8f * sat.importance

                // A. Render particle trail (highly visual, satisfying fluid displacement with zero GC allocations)
                val count = sat.trailCount
                val head = sat.trailHead
                if (count > 1) {
                    val startIdx = if (count < 10) 0 else head
                    for (i in 0 until count) {
                        val idx = (startIdx + i) % 10
                        val tX = sat.trailX[idx]
                        val tY = sat.trailY[idx]
                        val progress = i.toFloat() / count
                        val trailRadius = baseRadius * 0.75f * progress
                        drawCircle(
                            color = satColor,
                            radius = trailRadius,
                            center = Offset(tX, tY),
                            alpha = progress * 0.35f
                        )
                    }
                }

                // B. Render the glowing node
                // Pulsate node mildly
                val pulseSatSize = baseRadius * (1f + (corePulseScale - 1f) * 0.15f)

                // Glow ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(satColor.copy(alpha = 0.7f), Color.Transparent),
                        center = Offset(sat.x, sat.y),
                        radius = pulseSatSize * 3.3f
                    ),
                    center = Offset(sat.x, sat.y),
                    radius = pulseSatSize * 3.3f
                )

                // Satellite core
                drawCircle(
                    color = if (sat.status == "Completed") Color.White else satColor,
                    radius = pulseSatSize,
                    center = Offset(sat.x, sat.y)
                )

                // Inner white hot core star
                drawCircle(
                    color = Color.White,
                    radius = pulseSatSize * 0.4f,
                    center = Offset(sat.x, sat.y)
                )

                // Status Indicator
                if (sat.status == "Completed") {
                    drawCircle(
                        color = CosmicSecondary,
                        radius = pulseSatSize + 5f,
                        center = Offset(sat.x, sat.y),
                        style = Stroke(width = 2f)
                    )
                }

                // Node Title Label (Floating)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                        alpha = 210
                    }
                    drawText(sat.title, sat.x, sat.y - baseRadius - 16f, paint)
                }
            }

            // 7. Draw Central Aether Core Sun (Representing the server-side Gemini AI engine)
            val sunRadius = 40f
            val baseSunAura = 110f

            // Pulsing auras represent AI thinking/activity
            val activeAuraMultiplier = if (isAnalyzing) 2.2f else 1.0f
            val auraColor = if (isAnalyzing) GoldAcc else CosmicPrimary

            // Outer energy nebula waves
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = coreAuraAlpha * 0.6f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = baseSunAura * corePulseScale * activeAuraMultiplier
                ),
                center = Offset(centerX, centerY),
                radius = baseSunAura * corePulseScale * activeAuraMultiplier
            )

            // Inner hot plasma sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, auraColor.copy(alpha = 0.8f), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = sunRadius * 1.8f
                ),
                center = Offset(centerX, centerY),
                radius = sunRadius * 1.8f
            )

            // Solar flares rotation visualization when analyzing
            if (isAnalyzing) {
                val numFlares = 8
                for (i in 0 until numFlares) {
                    val angleOffset = (coreRotate + (i * (360f / numFlares))) * 0.0174533f // degrees to radians
                    val startDist = sunRadius * 1.1f
                    val endDist = sunRadius * 2.3f * corePulseScale
                    val startX = centerX + startDist * cos(angleOffset)
                    val startY = centerY + startDist * sin(angleOffset)
                    val endX = centerX + endDist * cos(angleOffset)
                    val endY = centerY + endDist * sin(angleOffset)

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, GoldAcc.copy(alpha = 0f)),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

// Color parsing extension to support hex codes securely
fun Color.Companion.parse(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.White
    }
}
