# Contributing to HydroTracker

**Please read this document carefully before making any contributions to make the process smoother for both you and me.**

First of all, thank you for thinking about making a contribution to this project. Let's make awesome things together!

---

## General Rules

Here are some general rules and suggestions before making a Pull Request:

1. Read the latest [Material Design Guidelines](https://m3.material.io/) at least once. This app fully aligns with Material Design and is also open to using the latest APIs to keep it fresh, so regularly checking the documentation is advisable.
2. The app is built with [Jetpack Compose](https://developer.android.com/compose). It is recommended to bookmark its [documentation](https://developer.android.com/develop/ui/compose/documentation).
3. Even though you can work on a particular feature that you like, I highly suggest looking at the [Issues](https://github.com/Econ01/HydroTracker/issues) page first to see what bugs and enhancements are already tracked. More importantly, check whether the thing you want to implement or fix is already listed to save both of us time.
4. You **must** [create an issue](https://github.com/Econ01/HydroTracker/issues/new/choose) first before you start working. Otherwise tracking everything becomes very challenging.
5. After creating your issue, please wait for discussion. I am usually busy so it can take some time, but I will definitely respond. This is not to prevent your work but to make the PR smoother.
6. I am not against using AI tools, **but** at least read **every** line your AI tool is writing. If you cannot even read your PR once, imagine how hard it is to review it.

---

## Development Environment

### Prerequisites
- **JDK 17** or later
- **Android Studio** (latest stable version recommended)
- **Android SDK** with API 37 installed

### Building the Project

The release signing configuration is not included in the repository for security reasons. The build script is already set up to fall back to the default debug keystore automatically, so **most contributors can build immediately** after cloning.

If you do not have `ANDROID_HOME` set globally, create a `local.properties` file in the project root and point it to your Android SDK:

```bash
# macOS / Linux
sdk.dir=/path/to/your/Android/Sdk

# Windows
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

Then build and run:

```bash
./gradlew assembleDebug
```

Or simply run via Android Studio.

---

## Structural Rules

Follow these steps so that your PR can be considered valid:

1. Clone the repository to your working environment.
2. Create a new branch. The name of the branch should be `HT-Issue#` where `Issue#` is the issue number assigned by GitHub. For example, for [Issue #3](https://github.com/Econ01/HydroTracker/issues/3), your branch should be named `HT-3`. You may add a descriptive title after the tracking number if you wish (e.g. `HT-3: Quick Select Custom Entry`).

3. If you have any questions or are unsure about something, ask in the corresponding issue page.
4. Keep your PR focused. One feature or one bug fix per branch. Large refactoring PRs are hard to review and likely to be rejected.
5. When you are done with the implementation, you **must rebase** onto the current `main` (or `HEAD`). You can open a PR before rebasing, but you must rebase before merging.
6. After rebasing, you **must** run the app again and make sure everything works as intended and nothing was broken.
7. Open a Pull Request and fill out the description properly. See the PR Guidelines below.

---

## Code Style

We follow the **Kotlin official code style** with a few project-specific conventions:

- **Indentation:** 4 spaces
- **Braces:** Egyptian (K&R) style — opening brace on the same line
- **Naming:**
  - Regular functions and variables: `camelCase`
  - `@Composable` functions: `PascalCase`
  - Constants and enum values: `SCREAMING_SNAKE_CASE`
- **Imports:** Android/framework → Compose → Project → Kotlin stdlib. Wildcards (`.*`) are fine for Compose packages.
- **Compose:**
  - `Modifier` should be the first optional parameter when present
  - Use `@OptIn` for experimental APIs
  - Add a `@Preview` for new UI components when feasible
- **Documentation:** Use KDoc (`/** */`) for public functions and classes. Inline `//` comments are fine for implementation details.
- **Logging:** Emoji markers in logs are encouraged for quick visual scanning (e.g. ✅, ❌, 📝).

---

## PR Guidelines

A good Pull Request makes review fast and merging easy:

- **Describe what you changed and why.** Reference the issue number (e.g. `Closes #3`).
- **UI changes:** Attach **before/after screenshots** in the PR description.
- **Animations or transitions:** Attach a **screen recording** (MP4 or GIF) showing the new behavior.
- **Keep the diff minimal.** Do not reformat unrelated code or change indentation in files you did not touch.
- **Test your changes.** At minimum, verify the changed screen works and the app launches without crashes.
- **Update documentation** if your change affects user-facing behavior, permissions, or build setup.

---

## License

By contributing to HydroTracker, you agree that your contributions will be licensed under the **GNU General Public License v3.0**. Derivative works must also remain under GPL v3.0.

---

Thank you for your hard work. Welcome to the project!
