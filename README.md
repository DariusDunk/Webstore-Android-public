# Online Shop — Android Client

Native Android client for a full-stack e-commerce platform developed as a master's thesis project.

The application provides a mobile interface for browsing products, searching the catalogue, managing shopping carts and favourites, placing purchases, managing user accounts, and interacting with the online shop.

## Features

* Product browsing and search
* Product suggestions and filtering
* Category and manufacturer browsing
* Product details and image galleries
* AI-powered product search using uploaded images or by capturing images with the device’s camera
* Shopping cart management
* Guest and authenticated shopping sessions
* Favourites
* Product reviews
* Purchase and order management
* User profile management
* Responsive native Android interface

## Architecture

The Android application communicates with a dedicated **Android Backend-for-Frontend (BFF)**, which is part of the server-side repository.

```
┌─────────────────┐
│  Android Client │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Android BFF   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Spring Boot API │
└─────────────────┘
```

The BFF provides a client-specific server-side interface while the central backend contains the shared business logic and data management.

## Technologies

* Kotlin
* Android
* Jetpack Compose
* MVVM
* Kotlin Coroutines
* Retrofit
* Hilt
* Gradle

## Running the Project

### Requirements

* Android Studio
* Android SDK 36
* A physical Android device or emulator

Clone the repository and open it in Android Studio. After Gradle synchronization completes, the application can be run on a connected Android device or emulator.

The application requires the Android BFF to be available and properly configured.

## Project Context

This repository contains the native Android client of a multi-client e-commerce system. The complete system consists of this Android application, a React web client, and a server-side repository containing the Spring Boot backend and the client-specific BFF services.
