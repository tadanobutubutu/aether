# 🏗️ Aether System Architecture

This specialized document provides a highly structured developer-focused deep dive into Aether's underlying subsystems, communication schemas, database schemas, and interface boundaries.

---

## 🗺️ High-Level System Partitioning

Aether operates via three primary structural layers that interact in standard unidirectional or bijective synchronizations:

```
                  ┌─────────────────────────────────────────┐
                  │        Stream of Consciousness          │
                  │             (User Input)                │
                  └────────────────────┬────────────────────┘
                                       │ Raw Text
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │       Gemini AI Engine (Rest API)       │ (Online)
                  └────────────────────┬────────────────────┘
                                       │ Extraction / Categorization json
                                       ▼
                  ┌─────────────────────────────────────────┐
                  │       Aether Core State Machine         │  ◀───────┐
                  │             (ViewModel)                 │          │
                  └──────────┬───────────────────┬──────────┘          │
                             │                   │                     │
      SQLite State Sync      │                   │ Bijective logs      │ User Edits
                             ▼                   ▼                     │ (Drag & Pan)
           ┌───────────────────┐       ┌───────────────────┐           │
           │   Room Database   │       │ Retro Mainframe   │           │
           │  (Local Storage)  │       │  (COBOL Sim & Log)│           │
           └───────────────────┘       └───────────────────┘           │
                     ▲                                                 │
                     └─────────────────  Jetpack Compose Physics  ─────┘
```

---

## 🗄️ Database Schemas (SQLite via Room)

Persistence is driven entirely using **Room Database** containing two pre-configured relational layouts:

### 1. `categories` Table (`CategoryEntity`)
This entity maintains the parent "planets" governing planetary orbit centers.

| Field | SQLite Type | Description |
|---|---|---|
| `id` | `INTEGER` (PK, Auto) | Unique Category identifier |
| `name` | `TEXT` | Visual category name |
| `colorHex` | `TEXT` | Hex string defining planet surface and orbit trail colors |
| `relativeSize` | `REAL` | Scale modifier for viewport renders |
| `orbitRadius` | `REAL` | Radial center offset distance in dp |
| `orbitSpeed` | `REAL` | Orbital speed scaler (negative means Retrograde orbit) |
| `description` | `TEXT` | Descriptive text describing core domain |

### 2. `thoughts` Table (`ThoughtEntity`)
This entity stores individual ideas acting as "satellites".

| Field | SQLite Type | Description |
|---|---|---|
| `id` | `INTEGER` (PK, Auto) | Unique Thought identifier |
| `categoryId` | `INTEGER` (FK-equivalent) | Links satellite directly to its parent coordinate center |
| `title` | `TEXT` | Structured heading summarized by AI |
| `content` | `TEXT` | Full body text or original user input |
| `status` | `TEXT` | Thought stage: `"Active"`, `"Completed"`, or `"Archived"` |
| `importance` | `REAL` | Calculated significance determining mass/size |
| `createdAt` | `INTEGER` | Time-stamp in milliseconds |

---

## 🌐 Android WebView & ThreeJS Bridge Protocol

To render the 3D space constellation, Aether loads `/app/src/main/assets/three_showcase.html` locally into a standard Compose WebView container.

### JS Sandbox Specifications
* **Rendering Sub-Layer:** Three.js (r128) WebGL framework.
* **Physics Coprocessors:** Run locally compiled Rust and Zig WebAssembly bin payloads on the WebView JavaScript thread to dynamically generate deterministic chaotic orbit perturbations at real-time speeds (60 FPS).
* **Navigation UI:** Features interactive Zoom, Orbit Rotate, and Touch-Raycasting.

### Current Bridging Architecture (MVP Status)
1. **Unidirectional UI Loading:** The Android native host launches and displays the local HTML5 asset with sandbox configurations.
2. **Virtual Data Layers:** In the MVP framework, the WebGL orbit scene presents high-fidelity, interactive, custom simulated data points. This is done to ensure completely offline performance and instantaneous loading metrics while maintaining visually responsive sliders mapping straight onto the global mesh.
3. **Bridge Roadmap (Multi-Process Sync):** In upcoming builds, `WebView.postWebMessage()` will synchronize live database queries securely from SQLite to the Three.js WebGL model structure.

---

## 📟 Retro Mainframe Simulation Mechanics

The Retro Console emulator operates as a pure **Kotlin-written virtual machine**, completely removing external system toolchain requirements (COBOL compiler, Perl engines, etc.).

### System State Flags
* **`CGI Buffer Status`**: Maintains tracking of simulated active system payloads. High-frequency network synchronizations can trigger a `CGI Buffer Overflow` state.
* **Bijective Dial Routing:** Modifying orbital angles (`ROT`) or radial widths (`PAN`) inside the controller triggers discrete state dispatches to the active Kotlin StateFlow, which is then serialized directly into a historical log stream using a standard `SOAP v1.1 Envelope` XML wrap:

```xml
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <m:CalibrateOrbit xmlns:m="urn:aether-cognitive-space">
      <m:TargetId>WORK</m:TargetId>
      <m:DeltaRadius>15</m:DeltaRadius>
      <m:DeltaAngle>-0.26</m:DeltaAngle>
    </m:CalibrateOrbit>
  </soap:Body>
</soap:Envelope>
```
