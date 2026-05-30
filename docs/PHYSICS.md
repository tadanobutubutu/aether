# 🌌 Aether Physics Core & Mechanical Telemetry

This document describes the mathematical equations, parameters, and audio parameters governing the 2D Orbit engine and the retro terminal feedback loops in Aether.

---

## ☄️ 2D Orbital Mechanics (Compose Physics Engine)

Inside the 2D constellation viewport (`AetherVisualizer.kt`), all celestial nodes drift dynamically under standard orbital approximations.

### 1. Coordinate Systems & Center Point
The coordinate system is offset with the Central Wisdom Sun $(S_0)$ placed exactly at the mathematical center of the user's viewport context $[W/2, H/2]$.
All positions are calculated as 2D relative offsets $(x, y)$ from $S_0$.

### 2. Planetary Orbit Equation
Each category $C_i$ has a fixed Keplerian circular path with radius $R_i$ and orbital speed scalar $\omega_i$:

$$\theta_i(t) = \theta_i(0) + \omega_i \cdot t$$

$$P_i(t) = \begin{bmatrix} X_0 + R_i \cos(\theta_i(t)) \\ Y_0 + R_i \sin(\theta_i(t)) \end{bmatrix}$$

### 3. Satellites Coordinates (Thoughts)
An individual satellite $s_{ij}$ representing a personal markdown thought revolves around its parent category planet $P_i$.
* Let its secondary radius offset be $r = 25\text{ dp}$.
* Let its orbital velocity be $\omega_{sub} = 1.25 \times \omega_i$.
* Let the satellite position be calculated as:

$$S_{ij}(t) = \begin{bmatrix} P_{ix} + r \cos(\theta_{sub}) \\ P_{iy} + r \sin(\theta_{sub}) \end{bmatrix}$$

---

## ⚖️ Dynamic Mass & Particle Drag Calculations

When a user drags or orbits nodes manually, Aether applies kinetic vectors:

### 1. Drag & Kinematics
* Drag Coefficient: $\kappa = 0.94$ applied per frame step.
* Boundary Elastic Limit: If planetary boundary orbits stretch past original parameters, they experience hookian gravitational restoring vectors:

$$F_{restore} = -k_{spring} \cdot (R_{current} - R_{anchor})$$

### 2. Node Mass Scaling
The visual size ($d$) and gravitational hitbox radius $(H_{radius})$ of a satellite are dynamically adjusted based on its AI-analyzed interest importance score $(I_j)$:

$$d = 20\text{ dp} + (l_j \cdot 18\text{ dp})$$

$$Mass(s) = e^{I_j}$$

Where higher importance values generate strong visual trails and inertia responses on the canvas.

---

## 🔊 Mechanical Sound Synthesizer Specifications (Audio Beeps)

To avoid heavy asset bundles, Aether compiles physical mechanical warning sound waves using Android's native hardware `ToneGenerator` under standard frequencies:

### Audio Signal Tone Configurations

| Action Event | Tone Name / Sequence | Frequency (Hz) | Duration (ms) | Description |
|---|---|---|---|---|
| **Dial Adjustments** | `TONE_CDMA_PIP` | $480 \text{ Hz} \ \& \ 580 \text{ Hz}$ | $80 \text{ ms}$ | High-precision feedback click |
| **System Resets** | `TONE_CDMA_SIGNAL_FADE` | $800 \text{ Hz} \rightarrow 300 \text{ Hz}$ | $450 \text{ ms}$ | Deep sweeping flush sound |
| **Overflow Alarm** | `TONE_CDMA_ALERT_CALL_GUARD` | $1450 \text{ Hz}$ | $180 \text{ ms}$ (Pulse) | Double piercing warning beep |
| **Action Trigger** | `TONE_CDMA_PRESS_OK` | $941 \text{ Hz} \ \& \ 1209 \text{ Hz}$ | $120 \text{ ms}$ | Positive mechanics confirmation |
