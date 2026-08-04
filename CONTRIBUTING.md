# Contributing to Device Inspector

First off, thank you for considering contributing to **Device Inspector**! It is contributions like yours that make Device Inspector a great tool for the Android community.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check existing issues to ensure the problem hasn't already been reported. When creating a bug report, please include as many details as possible:

* Use a clear and descriptive title.
* Describe the exact steps to reproduce the problem.
* Provide your device model, Android OS version, and app version.
* Include screenshots or logs if available.

### Requesting Features

Feature requests are welcome! Please submit an issue using the **Feature Request** template:

* Explain why this feature would be useful to users.
* Describe how you envision the feature working or looking.

### Submitting Pull Requests

1. **Fork the Repository**: Create your own fork of `device-inspector`.
2. **Create a Branch**: Create a feature branch off of `main` (`git checkout -b feature/my-cool-feature`).
3. **Write Code**: Follow modern Android development guidelines with Kotlin and Jetpack Compose.
4. **Test Your Changes**: Verify that the project builds cleanly (`./gradlew assembleDebug` or `compile_applet`).
5. **Commit cleanly**: Write clear, descriptive commit messages.
6. **Open a PR**: Submit a Pull Request targeting the `main` branch with a thorough summary of changes.

## Development Setup

- **IDE**: Android Studio Ladybug (or newer recommended)
- **JDK**: Java 17+ (JDK 21 recommended)
- **Android SDK**: API 36 (Min SDK 24)
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

## Code Style & Conventions

- Use **Kotlin** for all application logic.
- Use **Jetpack Compose** for UI components.
- Follow **Material 3** design guidelines.
- Ensure all composables are responsive and handle screen configuration changes cleanly.
- Keep codebase modular and maintain high standards of code readability.

Thank you for helping improve Device Inspector!
