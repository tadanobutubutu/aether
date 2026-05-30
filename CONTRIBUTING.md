# 🤝 Contributing to Aether

We are delighted you want to contribute to **Aether**! Whether you want to improve the physical orbit models, enrich the 3D Three.js constellations, or optimize the simulated COBOL logic, your input makes Aether better for personal knowledge explorers everywhere.

---

## 🛠️ Tech Stack & Directory Mapping

Before submitting adjustments, please familiarize yourself with the structural partitions:

```
app/
 └── src/main/
      ├── java/com/example/             # Native Android Core (Kotlin 1.9)
      │    ├── data/                    # Room Database, ThoughtEntity schema
      │    ├── ui/                      # Jetpack Compose Design Components
      │    │    ├── components/
      │    │    │    ├── AetherVisualizer.kt     # Jetpack Compose 2D Physics Canvas
      │    │    │    ├── RetroMainframeView.kt   # Retro COBOL & Dial Controls Emulation
      │    │    │    └── ThreeJsShowcaseView.kt  # WebView javascript bindings Bridge
      │    │    └── theme/              # Typography & customized colors
      │    └── MainActivity.kt          # UI state, gesture trackers & view routes
      └── assets/
           └── three_showcase.html      # Three.js 3D WebGL renderer & camera cruise
```

---

## 💻 Making Code Changes

### 1. Code Style Principles
- **Modern Jetpack Compose:** Always use Material Design 3 (M3) components. Follow Edge-to-edge guidelines utilizing correct `WindowInsets`.
- **State Flow & ViewModels:** Keep states flowing smoothly from `AetherViewModel` through declarative state-flows. Do not hardcode intermediate state counters.
- **Bijective Sync Integrity:** When modifying items on the 2D Physics Visualizer, log their changes seamlessly to the legacy mainframe log buffers using `viewModel.addMainframeLog(message)`.

### 2. Testing Your Changes
You can verify your changes quickly using Gradle's built-in target suites:

- **Verify App Compilation:**
  ```bash
  gradle assembleDebug
  ```
- **Run Unit & Sandbox Tests:**
  ```bash
  gradle :app:testDebugUnitTest
  ```
- **Review UI and Screenshot regressions:**
  Ensure updates do not break layout expectations:
  ```bash
  gradle verifyRoborazziDebug
  ```

---

## 🐛 Submitting Pull Requests

1. **Keep PRs Simple and Highly Focused:** Tackle single areas (e.g., "Add kinetic gravity line" or "Fix SOAP parse exception layout").
2. **Document Your Code:** Ensure any newly implemented gravity-control features are documented in `/README.md` under the appropriate section.
3. **No Mock APIs:** Aether is powered by direct, safe integration. Avoid placing dummy stub networks; let events flow naturally over local Room databases or verified network engines.
