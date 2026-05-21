package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.db.AetherDatabase
import com.example.data.db.CategoryEntity
import com.example.data.db.ThoughtEntity
import com.example.data.repository.AetherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AetherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AetherRepository
    val categories: StateFlow<List<CategoryEntity>>
    val thoughts: StateFlow<List<ThoughtEntity>>

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _statusText = MutableStateFlow("Cosmic peace. Speak into the Aether...")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    // Interactive canvas nodes
    private val _planets = MutableStateFlow<List<PlanetState>>(emptyList())
    val planets: StateFlow<List<PlanetState>> = _planets.asStateFlow()

    private val _satellites = MutableStateFlow<List<SatelliteState>>(emptyList())
    val satellites: StateFlow<List<SatelliteState>> = _satellites.asStateFlow()

    // Detailed view state
    private val _selectedThought = MutableStateFlow<ThoughtEntity?>(null)
    val selectedThought: StateFlow<ThoughtEntity?> = _selectedThought.asStateFlow()

    val selectedThoughtUiState: StateFlow<SelectedThoughtUiState?> = combine(
        _selectedThought,
        _planets
    ) { thought, currentPlanets ->
        if (thought == null) null else {
            val planet = currentPlanets.find { it.id == thought.categoryId }
            SelectedThoughtUiState(
                thought = thought,
                planetName = planet?.name ?: "Unknown",
                planetColorHex = planet?.colorHex ?: "#FFFFFF"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private var nextSatelliteId = 1L // For birth animations

    init {
        val database = AetherDatabase.getDatabase(application)
        repository = AetherRepository(database.aetherDao())

        categories = repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        thoughts = repository.allThoughts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            // First time initialization
            repository.checkAndPrepopulate()
            
            // Sync DB states to visual states when DB loads loaded items
            launch {
                categories.collect { dbCategories ->
                    syncPlanets(dbCategories)
                }
            }
            launch {
                thoughts.collect { dbThoughts ->
                    syncSatellites(dbThoughts)
                }
            }
        }
    }

    private fun syncPlanets(dbCategories: List<CategoryEntity>) {
        val currentPlanets = _planets.value.associateBy { it.id }
        _planets.value = dbCategories.map { db ->
            val existing = currentPlanets[db.id]
            PlanetState(
                id = db.id,
                name = db.name,
                colorHex = db.colorHex,
                relativeSize = db.relativeSize,
                orbitRadius = db.orbitRadius,
                orbitSpeed = db.orbitSpeed,
                description = db.description,
                // keep current position angle if it already exists to avoid jumps
                angle = existing?.angle ?: (Random.nextFloat() * 6.28f),
                x = existing?.x ?: 0f,
                y = existing?.y ?: 0f,
                isDragged = existing?.isDragged ?: false
            )
        }
    }

    private fun syncSatellites(dbThoughts: List<ThoughtEntity>) {
        val currentSatellites = _satellites.value.associateBy { it.id }
        _satellites.value = dbThoughts.map { db ->
            val existing = currentSatellites[db.id.toLong()]
            SatelliteState(
                id = db.id.toLong(),
                title = db.title,
                content = db.content,
                planetId = db.categoryId,
                importance = db.importance,
                status = db.status,
                angle = existing?.angle ?: (Random.nextFloat() * 6.28f),
                relativeDistance = existing?.relativeDistance ?: (15f + db.importance * 12f + Random.nextFloat() * 8f),
                orbitSpeed = existing?.orbitSpeed ?: ((0.015f + (1.5f - db.importance) * 0.015f) * (if (Random.nextBoolean()) 1 else -1)),
                x = existing?.x ?: 0f,
                y = existing?.y ?: 0f,
                isDragged = existing?.isDragged ?: false,
                trailX = existing?.trailX ?: FloatArray(10),
                trailY = existing?.trailY ?: FloatArray(10),
                trailCount = existing?.trailCount ?: 0,
                trailHead = existing?.trailHead ?: 0,
                birthAnimationProgress = existing?.birthAnimationProgress ?: 1.0f
            )
        }
    }

    private var lastUpdateTime = 0L

    fun updatePhysics(width: Float, height: Float) {
        val now = System.currentTimeMillis()
        if (lastUpdateTime == 0L) {
            lastUpdateTime = now
            return
        }
        val elapsed = now - lastUpdateTime
        lastUpdateTime = now

        // Target: 16.67ms per frame (60fps). Safe limits to prevent teleportation on background resume.
        val dt = (elapsed / 16.67f).coerceIn(0.1f, 4.0f)

        val currentPlanets = _planets.value
        val currentSatellites = _satellites.value

        // GC optimization: Perform physical updates in-place via engine (Zero Objects Allocation!)
        AetherPhysicsEngine.update(currentPlanets, currentSatellites, width, height, dt)

        // Emitting shallow copy lists to satisfy StateFlow and trigger recomposition / canvas invalidation with zero GC overhead
        _planets.value = ArrayList(currentPlanets)
        _satellites.value = ArrayList(currentSatellites)
    }

    fun onPlanetDragged(id: Int, newX: Float, newY: Float, width: Float, height: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        _planets.value = _planets.value.map { p ->
            if (p.id == id) {
                val dx = newX - centerX
                val dy = newY - centerY
                val radius = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat() / 2.5f
                val angle = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                p.copy(
                    x = newX,
                    y = newY,
                    orbitRadius = radius.coerceIn(40f, 320f),
                    angle = angle,
                    isDragged = true
                )
            } else {
                p
            }
        }
    }

    fun onPlanetDragEnded(id: Int) {
        _planets.value = _planets.value.map { p ->
            if (p.id == id) {
                // Persist the custom orbit radius/speed changes to local Room db!
                viewModelScope.launch {
                    val dbCat = categories.value.find { it.id == id }
                    if (dbCat != null) {
                        repository.updateCategory(dbCat.copy(orbitRadius = p.orbitRadius))
                    }
                }
                p.copy(isDragged = false)
            } else {
                p
            }
        }
    }

    fun onSatelliteDragged(id: Long, newX: Float, newY: Float) {
        _satellites.value = _satellites.value.map { s ->
            if (s.id == id) {
                s.copy(x = newX, y = newY, isDragged = true)
            } else {
                s
            }
        }
    }

    fun onSatelliteDragEnded(id: Long, width: Float, height: Float) {
        val sat = _satellites.value.find { it.id == id } ?: return
        val centerX = width / 2f
        val centerY = height / 2f

        // Check if dragged to incinerator bottom well (Y > height - 120dp and X near center)
        val isTrashZone = sat.y > (height - 250f) && Math.abs(sat.x - centerX) < 180f

        if (isTrashZone) {
            // Incinerate / Delete
            viewModelScope.launch {
                repository.deleteThoughtById(id.toInt())
                _statusText.value = "Incinerated: '${sat.title}' from Aether"
            }
            _satellites.value = _satellites.value.filter { it.id != id }
            return
        }

        // Check nearest planet gravity capture
        var nearestPlanet: PlanetState? = null
        var minDistance = Float.MAX_VALUE

        _planets.value.forEach { planet ->
            val dx = sat.x - planet.x
            val dy = sat.y - planet.y
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist < minDistance) {
                minDistance = dist
                nearestPlanet = planet
            }
        }

        val destinationPlanet = nearestPlanet
        if (destinationPlanet != null && destinationPlanet.id != sat.planetId && minDistance < 250f) {
            // Captured by another planet's gravity! Re-map category immediately in Room!
            viewModelScope.launch {
                val dbThought = thoughts.value.find { it.id == id.toInt() }
                if (dbThought != null) {
                    repository.updateThought(dbThought.copy(categoryId = destinationPlanet.id))
                    _statusText.value = "'${sat.title}' captured by ${destinationPlanet.name}'s gravity!"
                }
            }
            _satellites.value = _satellites.value.map { s ->
                if (s.id == id) {
                    s.copy(
                        planetId = destinationPlanet.id,
                        isDragged = false,
                        angle = Math.atan2((s.y - destinationPlanet.y).toDouble(), (s.x - destinationPlanet.x).toDouble()).toFloat()
                    )
                } else {
                    s
                }
            }
        } else {
            // Restore orbiting its current planet
            _satellites.value = _satellites.value.map { s ->
                if (s.id == id) {
                    val host = _planets.value.find { p -> p.id == s.planetId }
                    val angle = if (host != null) {
                        Math.atan2((s.y - host.y).toDouble(), (s.x - host.x).toDouble()).toFloat()
                    } else {
                        s.angle
                    }
                    s.copy(isDragged = false, angle = angle)
                } else {
                    s
                }
            }
        }
    }

    fun speakIntoAether(rawText: String) {
        if (rawText.isBlank()) return

        _isAnalyzing.value = true
        _statusText.value = "Aether Core is aligning cognitive vectors..."
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Call Gemini API to extract and parse tasks response
                val structuredSatellites = GeminiClient.analyzeStreamOfConsciousness(rawText)

                withContext(Dispatchers.Main) {
                    if (structuredSatellites.isEmpty()) {
                        _statusText.value = "The Core found nothing to crystallize. Try saying more."
                    } else {
                        _statusText.value = "Crystallized ${structuredSatellites.size} satellites!"
                    }
                }

                // Map planets to ID for easy DB saving
                val planetNameToId = categories.value.associateBy { it.name.lowercase() }

                structuredSatellites.forEach { sat ->
                    val resolvedId = planetNameToId[sat.planetName.lowercase()]?.id ?: 3 // default to Musa pink
                    val thought = ThoughtEntity(
                        categoryId = resolvedId,
                        title = sat.title,
                        content = sat.content,
                        importance = sat.importance.coerceIn(0.8f, 1.4f)
                    )

                    // Insert locally in Room
                    val newRowId = repository.insertThought(thought)

                    // Animate newly born satellites expanding outward from the sun
                    withContext(Dispatchers.Main) {
                        _satellites.value = _satellites.value + SatelliteState(
                            id = newRowId,
                            title = thought.title,
                            content = thought.content,
                            planetId = thought.categoryId,
                            importance = thought.importance,
                            status = "Active",
                            angle = Random.nextFloat() * 6.28f,
                            relativeDistance = 15f + thought.importance * 12f + Random.nextFloat() * 8f,
                            orbitSpeed = (0.015f + (1.5f - thought.importance) * 0.015f) * (if (Random.nextBoolean()) 1 else -1),
                            birthAnimationProgress = 0f // Start dynamic birth flight animation from sun center
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _errorMessage.value = e.message ?: "Cosmic error during crystallization."
                    _statusText.value = "The Aether was disrupted. Check your connection or API key."
                    e.printStackTrace()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isAnalyzing.value = false
                }
            }
        }
    }

    fun createThoughtManually(title: String, content: String, planetId: Int) {
        viewModelScope.launch {
            val thought = ThoughtEntity(
                categoryId = planetId,
                title = title,
                content = content,
                importance = 1.0f
            )
            val newRowId = repository.insertThought(thought)

            _satellites.value = _satellites.value + SatelliteState(
                id = newRowId,
                title = thought.title,
                content = thought.content,
                planetId = thought.categoryId,
                importance = thought.importance,
                status = "Active",
                angle = Random.nextFloat() * 6.28f,
                relativeDistance = 15f + thought.importance * 12f + Random.nextFloat() * 8f,
                orbitSpeed = (0.015f + (1.5f - thought.importance) * 0.015f) * (if (Random.nextBoolean()) 1 else -1),
                birthAnimationProgress = 0f // Fly out beautifully!
            )
            _statusText.value = "Direct orbit launched: '$title'"
        }
    }

    fun selectThought(satState: SatelliteState?) {
        if (satState == null) {
            _selectedThought.value = null
        } else {
            _selectedThought.value = thoughts.value.find { it.id == satState.id.toInt() }
        }
    }

    fun completeThought(thought: ThoughtEntity) {
        viewModelScope.launch {
            val updated = thought.copy(status = "Completed")
            repository.updateThought(updated)
            _selectedThought.value = updated
            _statusText.value = "Crystallization stabilized: '${thought.title}' completed"
        }
    }

    fun reopenThought(thought: ThoughtEntity) {
        viewModelScope.launch {
            val updated = thought.copy(status = "Active")
            repository.updateThought(updated)
            _selectedThought.value = updated
            _statusText.value = "Re-orbiting satellite: '${thought.title}' active"
        }
    }

    fun deleteSelectedThought() {
        val current = _selectedThought.value ?: return
        viewModelScope.launch {
            repository.deleteThought(current)
            _selectedThought.value = null
            _satellites.value = _satellites.value.filter { it.id != current.id.toLong() }
            _statusText.value = "Dissolved satellite: '${current.title}'"
        }
    }
}

// --- Visual Canvas States ---

data class SelectedThoughtUiState(
    val thought: ThoughtEntity,
    val planetName: String,
    val planetColorHex: String
)

data class PlanetState(
    val id: Int,
    val name: String,
    val colorHex: String,
    val relativeSize: Float,
    val orbitRadius: Float,
    val orbitSpeed: Float,
    val description: String,
    var angle: Float,
    var x: Float = 0f,
    var y: Float = 0f,
    var isDragged: Boolean = false
) {
    val size: Float
        get() = 18f * relativeSize // radius in dp
}

data class SatelliteState(
    val id: Long,
    val title: String,
    val content: String,
    val planetId: Int,
    val importance: Float,
    val status: String,
    var angle: Float,
    val relativeDistance: Float,
    val orbitSpeed: Float,
    var x: Float = 0f,
    var y: Float = 0f,
    val trailX: FloatArray = FloatArray(10),
    val trailY: FloatArray = FloatArray(10),
    var trailCount: Int = 0,
    var trailHead: Int = 0,
    var isDragged: Boolean = false,
    var birthAnimationProgress: Float = 1.0f // 1.0 means fully loaded in native orbit
) {
    fun addTrailPoint(px: Float, py: Float) {
        trailX[trailHead] = px
        trailY[trailHead] = py
        trailHead = (trailHead + 1) % 10
        if (trailCount < 10) {
            trailCount++
        }
    }

    fun getTrailPoints(): List<Offset> {
        val points = ArrayList<Offset>(trailCount)
        val startIdx = if (trailCount < 10) 0 else trailHead
        for (i in 0 until trailCount) {
            val idx = (startIdx + i) % 10
            points.add(Offset(trailX[idx], trailY[idx]))
        }
        return points
    }
}
