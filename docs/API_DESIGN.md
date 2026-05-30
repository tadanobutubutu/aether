# 🧠 Aether Gemini AI Integration & API Design

This document covers the design of Aether's AI client layer, which crystallizes raw stream-of-consciousness text into actionable personal database entities.

---

## 🛰️ Gemini Model & Endpoint Specifications

Aether integrates with Google's **Gemini 3.5 Flash** model for quick cognitive parsing (< 2s latency).

* **Model Used:** `gemini-3.5-flash`
* **API Level:** `v1beta` REST API
* **Base Endpoint:** `https://generativelanguage.googleapis.com/`
* **Route Path:** `v1beta/models/gemini-3.5-flash:generateContent`
* **Authentication:** API keys are injected at runtime via `.env` parameter configurations (`BuildConfig.GEMINI_API_KEY`) using the secure AI Studio Secrets console.

---

## 📝 Request & Response JSON Contracts

The interface relies on Retrofit serialized by OkHttp and Moshi Parser converters.

### 1. JSON Payload Request Example
```json
{
  "contents": [
    {
      "parts": [
        {
          "text": "Study Kotlin, and build an online backup by next Monday since our team depends on it."
        }
      ]
    }
  ],
  "systemInstruction": {
    "parts": [
      {
        "text": "You are Aether Core, the superconscious AI... [System Instructions]"
      }
    ]
  },
  "generationConfig": {
    "responseMimeType": "application/json",
    "temperature": 0.5
  }
}
```

### 2. Structured JSON Response Format
The model is constrained using `responseMimeType = "application/json"` to generate valid JSON arrays matching our domain model:

```json
{
  "thoughts": [
    {
      "title": "Study Kotlin Core Specs",
      "content": "Deep dive into Kotlin Coroutines, Flow, and modern Android constructs.",
      "planetName": "Terra",
      "importance": 1.15
    },
    {
      "title": "Build Online Database Backup",
      "content": "Establish automated daily backup sync scripts in preparation for Monday's deployment.",
      "planetName": "Zenith",
      "importance": 1.35
    }
  ]
}
```

---

## 🔍 System Instructions (AI Prompts)

Our system instructions guide the LLM's classification logic across 4 domain planets:

* **`Zenith` (Purple / Orbit 85dp):** Core priorities, tasks with active deadlines, chores, critical work.
* **`Ignis` (Crimson / Orbit 145dp):** Creative ambitions, hobbies, personal coding passion projects, fitness/sports.
* **`Musa` (Teal / Orbit 205dp):** Transient musings, spontaneous ideas, journaling, poetry, philosophy, focus logs.
* **`Terra` (Cyan / Orbit 265dp):** Standard daily routines, personal development, academic readings, habit structures.

### Importance Mapping Range
* Minimum Score: `0.8` (low-urgency transient capture).
* Maximum Score: `1.4` (extreme cornerstone priority that alters the planetary gravitational field).

---

## 🔒 Off-line Fallbacks & Privacy Practices

### 1. Offline Mode (Local Sandboxing)
If network operations fail or the user lacks an internet connection:
* Text submissions skip server pipelines gracefully.
* Users can manually select their target orbit planet and launch thoughts locally using the floating action launch drawers provided in the interface.

### 2. Privacy Safeguards
* **Zero Cloud History:** Since files write entirely locally to Room SQLite, your daily activities and tasks are kept on the device.
* **Minimum Exposure:** Only the single string submitted in the "Speak into Aether" input is processed over the API. No other device files, databases, or indexes are queried or transmitted.
