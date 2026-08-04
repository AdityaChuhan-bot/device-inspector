# 📱 Device Inspector

<p align="center">
  <img src="docs/banner.png" alt="Device Inspector Banner" width="100%">
</p>

<p align="center">

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-success)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)
![License](https://img.shields.io/badge/License-MIT-green)

</p>

---

## Overview

**Device Inspector** is a modern Android application built with **Kotlin** and **Jetpack Compose** that provides comprehensive hardware and software information about an Android device through a clean, responsive dashboard.

The goal of the project is to make device diagnostics, hardware inspection, and system information easy to access without requiring root access.

---

## Features

- 📱 Device information
- ⚙ Android version details
- 💾 RAM information
- 📦 Storage usage
- 🔋 Battery statistics
- 🧠 CPU information
- 📺 Display information
- 📷 Camera information
- 🌐 Network details
- 📡 Sensor information
- 🔍 System diagnostics
- 🎨 Modern Material 3 UI
- ⚡ Built with Jetpack Compose
- 📱 Responsive layouts

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM |
| Design | Material 3 |
| Dependency Injection | KSP |
| Build System | Gradle Kotlin DSL |
| Minimum Android | API 24 |
| Target Android | API 36 |

---

# Screenshots

> Screenshots coming soon.

```
docs/
 ├── home.png
 ├── hardware.png
 ├── battery.png
 ├── storage.png
 └── about.png
```

---

# Project Structure

```
Device-Inspector
│
├── app
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   ├── res
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest
│   │   └── test
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

# Installation

Clone the repository

```bash
git clone https://github.com/AdityaChuhan-bot/device-inspector.git
```

Open using Android Studio.

Build the project.

Run on an Android device running Android 7.0 (API 24) or above.

---

# Requirements

- Android Studio
- JDK 17+
- Android SDK 36
- Gradle (included)

---

# Architecture

The application follows the **MVVM** architecture.

```
UI
 │
 ▼
ViewModel
 │
 ▼
Device Modules
 │
 ▼
Android Framework APIs
```

This architecture keeps the UI reactive while separating business logic from platform-specific implementations.

---

# Roadmap

- [ ] Export device report as PDF
- [ ] Share diagnostics
- [ ] Dark / Light themes
- [ ] Benchmark improvements
- [ ] Storage analyzer
- [ ] Network speed test
- [ ] GPU information
- [ ] Thermal monitoring
- [ ] Sensor live dashboard
- [ ] Wear OS companion support

---

# Privacy

Device Inspector is designed with privacy in mind.

- No root access required.
- No personal data collection.
- No tracking.
- No advertising.
- All device information is processed locally unless cloud-based features are explicitly enabled.

---

# Contributing

Contributions are welcome.

If you have ideas for improvements, bug fixes, or new features, feel free to:

- Fork the repository
- Create a feature branch
- Commit your changes
- Open a Pull Request

---

# Credits

## Project Owner

**Aditya Chauhan**

GitHub:
https://github.com/AdityaChuhan-bot

---

## Development

Project architecture, implementation, and ongoing development by **Aditya Chauhan**.

---

## AI Assistance

This project was developed with assistance from AI development tools, including:

- Google AI Studio
- OpenAI ChatGPT

AI tools were used to accelerate development, generate ideas, assist with code, documentation, debugging, and UI improvements. Final implementation, review, testing, and project decisions remain the responsibility of the project owner.

---

## Open Source Libraries

This project uses several excellent open-source technologies, including:

- Kotlin
- Android Jetpack Compose
- AndroidX
- Material 3
- Gradle
- KSP

Special thanks to the Android open-source community for maintaining these projects.

---

# License

This project is licensed under the MIT License.

See the LICENSE file for details.

---

# Support

If you find this project useful, consider giving it a ⭐ on GitHub.

Feedback, feature requests, and pull requests are always welcome.

---

<p align="center">
Made with ❤️ using Kotlin, Jetpack Compose, and Android.
</p>
