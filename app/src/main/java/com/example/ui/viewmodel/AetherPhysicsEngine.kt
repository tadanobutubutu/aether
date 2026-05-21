package com.example.ui.viewmodel

import kotlin.math.cos
import kotlin.math.sin

object AetherPhysicsEngine {

    fun update(
        planets: List<PlanetState>,
        satellites: List<SatelliteState>,
        width: Float,
        height: Float,
        dt: Float
    ) {
        val centerX = width / 2f
        val centerY = height / 2f

        // 1. Update Planets in-place without copying
        for (i in planets.indices) {
            val planet = planets[i]
            if (!planet.isDragged) {
                // Update orbit angle scaled by Delta Time
                val speedCoef = 0.005f * planet.orbitSpeed * dt
                val newAngle = (planet.angle + speedCoef) % 6.2831855f
                // Compute standard pixel screen coordinates
                val rad = planet.orbitRadius * 2.5f // Scale factor for screen DPI
                planet.angle = newAngle
                planet.x = centerX + rad * cos(newAngle)
                planet.y = centerY + rad * sin(newAngle)
            }
        }

        // 2. Update Satellites orbiting their planet hosts in-place without copying
        for (i in satellites.indices) {
            val sat = satellites[i]
            if (sat.isDragged) continue

            // Find host planet with zero allocation lookup loop
            var host: PlanetState? = null
            for (pIdx in planets.indices) {
                if (planets[pIdx].id == sat.planetId) {
                    host = planets[pIdx]
                    break
                }
            }

            if (host != null) {
                if (sat.birthAnimationProgress >= 1f) {
                    val newAngle = (sat.angle + sat.orbitSpeed * dt) % 6.2831855f
                    // satellite orbits the host planet coordinates
                    val distanceFactor = sat.relativeDistance * 1.5f
                    val satX = host.x + distanceFactor * cos(newAngle)
                    val satY = host.y + distanceFactor * sin(newAngle)

                    sat.angle = newAngle
                    sat.x = satX
                    sat.y = satY
                    sat.addTrailPoint(satX, satY)
                } else {
                    // Newly born satellite flying from central sun to its destination
                    val progress = sat.birthAnimationProgress + 0.03f * dt
                    // Destination is host planet position plus initial orbit position
                    val destAngle = sat.angle
                    val distanceFactor = sat.relativeDistance * 1.5f
                    val destX = host.x + distanceFactor * cos(destAngle)
                    val destY = host.y + distanceFactor * sin(destAngle)

                    // Linear interpolation from central sun (centerX, centerY) to dest
                    val currentX = centerX + (destX - centerX) * progress
                    val currentY = centerY + (destY - centerY) * progress

                    sat.birthAnimationProgress = progress
                    sat.x = currentX
                    sat.y = currentY
                    sat.addTrailPoint(currentX, currentY)
                }
            }
        }
    }
}
