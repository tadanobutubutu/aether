# 🛠️ Aether Local Development & Testing Guide

This document describes how to configure the local workspace, set up secrets, run diagnostics, and run unit/UI regression tests on the Aether project.

---

## 🔑 Environment Configuration & Secrets

Aether requires a Gemini API key for crystallizing text entries.

1. **Secrets Location:** Define keys inside active runtime variables. In human development, create a copy of `.env.example` named `.env` in the root directory.
2. **AI Studio Setup:** In the Google AI Studio platform, enter your `GEMINI_API_KEY` directly inside the **Secrets Panel** in the sidebar. This key is securely loaded at build time:
   ```env
   GEMINI_API_KEY=AIzaSyA1...
   ```
3. **Internal Key Injection:** During compilation, the Secrets Gradle Plugin extracts variables, creating readable fields inside `com.example.BuildConfig.GEMINI_API_KEY`.

---

## 🏗️ Building & Running

### 1. Requirements
* **Android Studio Koala** (or newer)
* **SDK Tools:** Platform-Tools 34+, Build-Tools 34+
* **JDK Version:** Java 17 (Required by Gradle 8.2+)

### 2. Compilation Commands
Always run Gradle using the standard command line syntax (never append wrapper executables):

* **Clean the Workspace (As a last resort for caching glitches):**
  ```bash
  gradle clean
  ```
* **Assemble Debug APK:**
  ```bash
  gradle assembleDebug
  ```

---

## 🧪 Testing Strategies & Quality Assurance

Aether is backed by robust local testing frameworks that verify business logic and visual presentations with zero reliance on slow Android Emulators.

### 1. Local Business Logic Tests (Unit & Robolectric)
Tests are placed in `/app/src/test/java/`. These verify coroutine emitters, Room DB transitions, database operations, and COBOL mainframe state calibrations.

* **Execute All Unit Tests:**
  ```bash
  gradle :app:testDebugUnitTest
  ```

### 2. Visual Regression Testing (Roborazzi Screenshots)
To detect unintended visual bugs on complex layouts (2D canvas lines, mainframe grids, dialogue boxes), Aether employs **Roborazzi**:

* **Running Verification Tests:**
  Compares active screens against reference baseline states:
  ```bash
  gradle verifyRoborazziDebug
  ```
* **Recording New Screenshot baselines:**
  If you intentionally modified Compose themes (e.g., changing buttons, titles, or colors), run this command to update baseline screenshots:
  ```bash
  gradle recordRoborazziDebug
  ```
  *Visual baseline storage:* Reference images are preserved in `/app/src/test/screenshots` or standard build folders.

---

## 🐞 Diagnostics & Debug Triggers

* **Moshi Payload Checks:** Ensure all custom fields parsing incoming LLM JSON are logged explicitly.
* **Tones & Sounds:** Touch events leverage `ToneGenerator` in background thread runners. Verify device volume is enabled during visual tests.
