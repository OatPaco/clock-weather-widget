# Pilot Clock Weather Widget

A luxury Android home screen widget featuring a 3D metallic pilot-style analog clock with current weather information.

## Features

- **3D Pilot Clock** — Metallic bezel with radial gradient dome face, specular highlights, drop shadows, and lume-filled hands
- **Weather** — Current temperature and humidity via Open-Meteo API (free, no API key needed)
- **GPS Location** — Automatic city detection
- **5 Themes** — Deep Ocean, Sunset, Frosted Glass, AMOLED, Clean White
- **Auto-update** — Clock updates every minute, weather every 30 minutes

## Widget Size

2×2 (square) — optimized for maximum clock visibility

## Setup

### 1. Clone & generate Gradle wrapper

```bash
git clone https://github.com/OatPaco/clock-weather-widget.git
cd clock-weather-widget
gradle wrapper --gradle-version 8.7
```

### 2. Build locally

```bash
./gradlew assembleDebug
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### 3. Build via GitHub Actions

Push to `main` branch — the workflow will build both debug and release APKs automatically.

For signed release builds, add these repository secrets:
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`
- `SIGNING_STORE_PASSWORD`

And place your keystore at `app/release/release.jks`.

## Permissions

- `INTERNET` — Fetch weather data
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — GPS for weather location
- `RECEIVE_BOOT_COMPLETED` — Restart clock alarm after reboot
- `SCHEDULE_EXACT_ALARM` — Minute-accurate clock updates

## Tech Stack

- Kotlin
- Canvas API (bitmap-based 3D clock rendering)
- WorkManager (periodic weather updates)
- FusedLocationProviderClient (GPS)
- Open-Meteo API (free weather data)
- AlarmManager (clock ticking)

## Project Structure

```
app/src/main/java/com/oat/clockweather/
├── ClockWeatherWidget.kt    # AppWidgetProvider + AlarmManager
├── ClockDrawer.kt           # 3D clock Canvas rendering
├── ThemeManager.kt           # 5 theme color definitions
├── ConfigActivity.kt         # Theme picker on widget placement
├── WeatherService.kt         # Open-Meteo API client
├── WeatherStore.kt           # SharedPreferences weather cache
├── WeatherUpdateWorker.kt    # WorkManager periodic fetcher
├── LocationHelper.kt         # GPS + reverse geocoding
└── BootReceiver.kt           # Restart on device boot
```
