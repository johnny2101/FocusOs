# FocusOS 🎯

> **A minimal, intentional Android home screen launcher and digital wellbeing system designed to curb mindless scrolling and smartphone anxiety.**

FocusOS transforms your Android device into a low-friction, text-focused environment. It categorizes apps into distinct functional tiers, monitors usage velocity, introduces intentional friction before launching distracting apps, and automatically enforces brief lockdown periods when doomscrolling patterns are detected.

---

## 🚀 Key Features

* **Minimal Text-Based Launcher**: Replaces visually overwhelming icon grids with a clean, searchable list of installed applications.
* **App Categorization & Tiers**: Classifies apps into `UTILITY`, `DISTRACTION`, and `NEUTRAL` tiers to prioritize productivity tools over addictive algorithms.
* **Intentional Friction Interventions**: Prompts users with a pause or confirmation dialog before opening distracting applications.
* **Automated Anxiety & Lockdown Detection**: Analyzes screen unlock frequency and app-switching velocity in real time. Triggers an automated lockdown overlay when high-stress usage spikes occur.
* **Git-Style Weekly Analytics**: Generates a weekly "commit log" style usage summary (delta tracking) comparing current usage against previous week baselines.
* **Emergency Override Protocol**: Provides a fail-safe mechanism allowing users to break lockdown during true emergencies while recording an override audit event.

---

## 🏗️ Architecture & How It Works

FocusOS adheres to **Clean Architecture** principles and the modern **MVVM (Model-View-ViewModel)** UI pattern.

```mermaid
flowchart TD
    subgraph Presentation ["Presentation Layer (Jetpack Compose)"]
        UI[HomeScreen / LockdownOverlay / AnalyticsScreen]
        VM[HomeViewModel / AnalyticsViewModel / OnboardingViewModel]
    end

    subgraph Domain ["Domain Layer (Business Logic)"]
        UC[AnalyzeAnxietyUseCase / LaunchAppUseCase / CalculateWeeklyDiffUseCase]
        REPO_INT[Repository Interfaces]
    end

    subgraph Data ["Data Layer (Persistence & System)"]
        REPO_IMPL[UsageRepositoryImpl / ConfigRepositoryImpl / SystemSettingRepositoryImpl]
        DB[(Room Database: AppDatabase)]
        PREFS[(SharedPreferences: focusos_prefs)]
    end

    subgraph Services ["Background & System Services"]
        ACCESSIBILITY[FocusAccessibilityService]
        MONITOR[BackgroundMonitorService]
        WORKER[WeeklyReportWorker]
    end

    UI --> VM
    VM --> UC
    UC --> REPO_INT
    REPO_IMPL ..|> REPO_INT
    REPO_IMPL --> DB
    REPO_IMPL --> PREFS
    ACCESSIBILITY --> UC
    MONITOR --> UC
    WORKER --> UC
```

---

## 🛠️ Tech Stack

* **Language**: [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
* **Dependency Injection**: [Hilt (Dagger)](https://dagger.dev/hilt/)
* **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) & SharedPreferences
* **Background Processing**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) & Foreground Services
* **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
* **Navigation**: Jetpack Navigation Compose

---

## 📁 Project Structure

```text
FocusOs/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/focusos/
│   │   │   │   ├── data/            # Room DAOs, Entities, Repository Implementations
│   │   │   │   ├── di/              # Hilt Modules (Database, Repository, App)
│   │   │   │   ├── domain/          # Domain Models, Repository Contracts, Use Cases
│   │   │   │   ├── model/           # Utility Data Structures & DTOs
│   │   │   │   ├── presentation/   # Compose Screens, ViewModels, UI Components
│   │   │   │   ├── service/         # Accessibility Service, Foreground Service, Workers
│   │   │   │   └── util/            # Helper utilities
│   │   │   ├── res/                 # Resources, XML configs, strings, themes
│   │   │   └── AndroidManifest.xml  # Application Manifest
│   │   └── test/                    # Automated Unit Tests
│   └── build.gradle.kts             # App Module Dependencies & Configuration
├── gradle/                          # Version Catalogs & Gradle Wrapper
├── local.properties.example         # Template for local environment configuration
├── build.gradle.kts                 # Root Gradle Configuration
└── README.md                        # Documentation
```

---

## ⚙️ Requirements & Installation

### Requirements
* **Android Studio**: Ladybug / 2024.2.1 or newer
* **JDK**: Java 17 or JDK 21 (bundled with Android Studio JBR)
* **Target SDK**: Android API Level 35 (Android 15)
* **Min SDK**: Android API Level 28 (Android 9.0)

### Setup Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/FocusOs.git
   cd FocusOs
   ```

2. **Configure Environment**:
   Copy `local.properties.example` to `local.properties` and specify your local Android SDK directory:
   ```bash
   cp local.properties.example local.properties
   ```
   Edit `local.properties`:
   ```properties
   sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
   ```

3. **Build the Project**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```

5. **Run Lint Analysis**:
   ```bash
   ./gradlew lint
   ```

---

## 📱 Permission Setup

FocusOS requires specific system permissions to function as a launcher and usage monitor:

1. **Default Home App**: Set FocusOS as your default launcher when prompted during onboarding.
2. **Usage Access Permission**: Required to query foreground application events and calculate anxiety scores (`PACKAGE_USAGE_STATS`).
3. **Accessibility Service (`FocusOS Monitor`)**: Optional but recommended for instant app-switch detection and lockdown enforcement.

---

## 🔒 Security & Privacy

* **Zero External Data Transmission**: All usage statistics, event logs, and analytics reports remain 100% local on your device inside a secure Room database.
* **No Telemetry / No Tracking**: FocusOS collects no analytics or remote logging.

---

## ⚠️ Known Limitations

* **System Apps & System Settings**: Essential system apps (Settings, Phone dialer) are classified under `UTILITY` to ensure emergency access is never compromised.
* **WorkManager Schedule**: Weekly Git-style reports are generated every 7 days on devices with non-low battery states.

---

## 📜 License

This project is maintained for personal wellbeing and open-source demonstration.
