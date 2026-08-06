# movie-app-android-mvc

Study object for the Master's thesis *Quantifying the Architectural Tax: An
Empirical Study on Structural Overhead and Human Maintenance Effort in Mobile
Applications* (Bisma Saeed, Heilbronn University of Applied Sciences).

This repository holds the **standard MVC** implementation. Its counterpart,
`movie-app-android-clean-architecture`, implements the **identical feature set**
in MVVM + Clean Architecture. The two are compared on structural metrics,
runtime performance, and the maintenance effort and cognitive load of junior
developers performing standardised change tasks.

## Participants

If you are taking part in a session, read **[SETUP.md](SETUP.md)** before you
start Task 1.

## Controlled equivalence

The two study objects are held identical in everything except architecture, so
that any measured difference is attributable to architectural choice rather than
to incidental variation. Held constant:

- Feature set and UI
- Toolchain: AGP 8.7.3, Kotlin 2.0.21, KSP 2.0.21-1.0.27, Gradle 9.4.1, JDK 17
- Compose BOM 2024.09.00 and all shared library versions
- Test and debug tooling, so Compose Previews behave the same in both
- Lint configuration (`config/detekt/detekt.yml`, byte-identical in both repos)
- `compileSdk` 35, `minSdk` 26, `targetSdk` 35

Deliberate, architecture-driven differences are limited to:

- **Layering.** Controller-centred organisation, against the counterpart's
  UI / domain / data separation with an explicit Dependency Rule.
- **Dependency injection.** None; dependencies are constructed in place, whereas
  the Clean Architecture object uses Hilt for cross-layer wiring.
- **Networking.** This object uses Retrofit, which pulls Gson in transitively;
  the Clean Architecture object calls the API directly and parses with Gson.

Both are fenced and commented in `app/build.gradle.kts` and
`gradle/libs.versions.toml`.

## Baseline

The `baseline-v1` tag marks the frozen state used for all participant sessions
and for all static and runtime measurement. Do not move it.
