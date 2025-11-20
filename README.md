# Salsabil - Smart Hydration Assistant 💧

A production-ready Android hydration tracking app with smartwatch integration, activity monitoring, and privacy-preserving kidney support mode.

## 🌟 Features

### Core Features
- **Smart Hydration Tracking**: Dynamic daily goals based on weight, activity, and health conditions
- **Smartwatch Integration**: Connect to Balancs Edge (or any BLE smartwatch) for steps, heart rate, and battery monitoring
- **Activity Recognition**: Automatic step counting and GPS distance tracking
- **Intelligent Reminders**: Context-aware notifications based on activity and hydration status
- **Beautiful Analytics**: Animated charts showing weekly and monthly hydration trends

### Advanced Features
- **Kidney Support Mode**: Privacy-preserving BLE proximity detection for nearby Salsabil users
- **Tamper-Evident Ledger**: SHA-256 blockchain-style logging of all events
- **UUID Scanner**: Built-in tool to discover and configure smartwatch GATT characteristics
- **Multi-Language**: Full support for English, French, and Arabic (with RTL)
- **Material 3 Design**: Modern, fluid UI with light and dark themes
- **Offline-First**: All data stored locally with SQLCipher encryption

## 📋 Requirements

- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Kotlin**: 2.0.21
- **Gradle**: 8.13.1

## 🚀 Setup Instructions

### 1. Clone and Open Project
```bash
git clone <your-repo-url>
cd SalsabilApp
```

Open the project in Android Studio Otter (2025.2.1 Patch 1) or later.

### 2. Sync Gradle
The project will automatically download all dependencies. If not, click:
```
File → Sync Project with Gradle Files
```

### 3. Build the Project
```bash
./gradlew build
```

### 4. Run on Device/Emulator
- Connect your Android device or start an emulator
- Click Run (Shift + F10) or use:
```bash
./gradlew installDebug
```

## 🔧 Configuration

### Smartwatch Setup (Balancs Edge)

1. **Default Configuration**:
   - Device MAC: `78:02:B7:40:DB:87`
   - Device Version: V008614

2. **UUID Discovery**:
   - Open the app → Navigate to Smartwatch tab
   - Tap "Scan for Devices"
   - Select your device
   - Tap "UUID Scanner" to discover GATT services
   - Configure UUIDs for steps, heart rate, and battery

3. **Manual UUID Configuration**:
   Edit `BleDeviceConfig` in the database or use the in-app UUID editor.

### Hydration Formula

The app calculates daily hydration goals using:

```kotlin
base = weightKg * 35
activityExtra = (steps / 1000) * 150
gpsExtra = (distanceKm * 300)
finalGoal = clamp(base + activityExtra + gpsExtra, 1000, 6000)

// Kidney condition adds 20% increase
if (hasKidneyCondition) {
    finalGoal *= 1.2
}
```

Users can override this with a custom goal.

## 📱 Permissions

### Required Permissions

| Permission | Purpose | When Requested |
|------------|---------|----------------|
| `BLUETOOTH_SCAN` | Scan for smartwatch | First smartwatch connection |
| `BLUETOOTH_CONNECT` | Connect to smartwatch | First smartwatch connection |
| `BLUETOOTH_ADVERTISE` | Kidney Support Mode | Enabling proximity mode |
| `ACCESS_FINE_LOCATION` | GPS tracking & BLE (Android < 12) | First use |
| `ACTIVITY_RECOGNITION` | Step counting | First use |
| `POST_NOTIFICATIONS` | Hydration reminders | Android 13+ |
| `FOREGROUND_SERVICE` | Background tracking | Automatic |

### Permission Rationale

The app provides clear explanations for each permission:
- **Bluetooth**: Required for smartwatch connection and proximity detection
- **Location**: Required for GPS distance tracking and BLE scanning on older Android versions
- **Activity Recognition**: Required to track steps and adjust hydration goals
- **Notifications**: Required to send hydration reminders

## 🔋 Battery Optimization

### Disable Battery Optimization (Critical!)

For reliable background tracking, users **must** disable battery optimization:

#### Xiaomi/MIUI
1. Settings → Apps → Manage apps → Salsabil
2. Battery saver → No restrictions
3. Autostart → Enable
4. Battery → Unlock → Enable

#### Samsung/One UI
1. Settings → Apps → Salsabil
2. Battery → Optimize battery usage → All apps
3. Find Salsabil → Disable optimization

#### Huawei/EMUI
1. Settings → Apps → Salsabil
2. Battery → App launch → Manual
3. Enable all three options (Auto-launch, Secondary launch, Run in background)

#### Stock Android
1. Settings → Apps → Salsabil
2. Battery → Battery optimization
3. Select "All apps" → Find Salsabil → Don't optimize

#### OnePlus/OxygenOS
1. Settings → Apps → Salsabil
2. Battery → Battery optimization → Don't optimize

### Foreground Service Notification

The app runs a persistent foreground service for:
- BLE smartwatch connection
- GPS tracking
- Proximity scanning

**This notification cannot be dismissed** and is required for Android to keep the service alive.

## 🏗️ Architecture

### MVVM + Clean Architecture

```
app/
├── data/
│   ├── local/
│   │   ├── entity/          # Room entities
│   │   ├── dao/             # Data Access Objects
│   │   ├── SalsabilDatabase # Encrypted Room database
│   │   └── LedgerManager    # Tamper-evident ledger
│   └── repository/          # Repository layer
├── domain/
│   ├── HydrationEngine      # Business logic
│   └── ble/                 # BLE managers
├── presentation/
│   ├── home/                # Home screen + ViewModel
│   ├── profile/             # Profile screen + ViewModel
│   ├── analytics/           # Analytics screen + ViewModel
│   ├── smartwatch/          # Smartwatch screen + ViewModel
│   └── proximity/           # Proximity screen + ViewModel
├── service/
│   ├── SalsabilForegroundService  # Main foreground service
│   └── ActivityTrackingService    # Activity tracking
├── worker/
│   └── HydrationReminderWorker    # Periodic reminders
└── di/                      # Hilt dependency injection
```

### Key Technologies

- **Hilt**: Dependency injection
- **Room**: Local database (encrypted with SQLCipher)
- **Coroutines + Flow**: Asynchronous programming
- **Navigation Component**: Fragment navigation
- **WorkManager**: Background tasks
- **DataStore**: Preferences storage
- **Lottie**: Animations
- **MPAndroidChart**: Charts and graphs

## 🔐 Security & Privacy

### Data Encryption
- All local data encrypted with SQLCipher
- Passphrase: `salsabil_secure_key_2024` (change in production!)

### Tamper-Evident Ledger
Every significant event is logged with SHA-256 hashing:
```kotlin
currentHash = SHA256(previousHash + timestamp + payloadJson)
```

Verify integrity:
```kotlin
ledgerManager.verifyLedgerIntegrity()
```

### Kidney Support Mode Privacy
- Uses rotating ephemeral UUIDs (changes every 15 minutes)
- No personal data transmitted
- Only proximity detection, no identity exchange

## 🧪 Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

## 🌍 Localization

Supported languages:
- **English** (`values/`)
- **French** (`values-fr/`)
- **Arabic** (`values-ar/`) with full RTL support

Add new languages by creating `values-{lang}/strings.xml`.

## 📦 Building Release APK

### 1. Configure Signing
Create `keystore.properties` in project root:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=path/to/keystore.jks
```

### 2. Build Release
```bash
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release.apk`

### 3. ProGuard
ProGuard is enabled for release builds. Rules are in `app/proguard-rules.pro`.

## 🐛 Troubleshooting

### App Killed in Background
- Disable battery optimization (see Battery Optimization section)
- Ensure foreground service notification is visible
- Check manufacturer-specific settings

### Bluetooth Not Working
- Enable Bluetooth in system settings
- Grant all Bluetooth permissions
- For Android 12+, ensure `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` are granted

### Smartwatch Not Connecting
- Ensure device is in range
- Check device MAC address matches
- Use UUID Scanner to discover correct GATT characteristics
- Verify device is not connected to another app

### Notifications Not Showing
- Grant `POST_NOTIFICATIONS` permission (Android 13+)
- Check notification channels are enabled in system settings
- Ensure app is not in battery saver mode

### GPS Not Tracking
- Enable Location in system settings
- Grant `ACCESS_FINE_LOCATION` permission
- Ensure app has background location access

## 📄 License

[Add your license here]

## 👥 Contributors

[Add contributors here]

## 📞 Support

For issues and questions:
- GitHub Issues: [your-repo-url]/issues
- Email: [your-email]

## 🙏 Acknowledgments

- Material Design 3 by Google
- Lottie by Airbnb
- MPAndroidChart by PhilJay
- SQLCipher by Zetetic

---

**Built with ❤️ for better hydration and health**
