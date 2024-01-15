# Android Automotive

A comprehensive guide to working with Android Automotive templates, highlighting their features,
limitations, and best practices.

Before diving into the project, it is strongly recommended to thoroughly review this
documentation [Gitbook](https://akshay-2.gitbook.io/untitled/)

## Table of Contents

- [Introduction](#introduction)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Templates Overview](automotive/src/main/java/com/akshay/weatherapp/HomeScreen.kt)
    - [Grid Template](automotive/src/main/java/com/akshay/weatherapp/templates/GridTemplateExample.kt)
    - [List Template](automotive/src/main/java/com/akshay/weatherapp/templates/ListTemplateExample.kt)
    - [Message Template](automotive/src/main/java/com/akshay/weatherapp/templates/MessageTemplateExample.kt)
    - [Long Message Template](automotive/src/main/java/com/akshay/weatherapp/templates/LongMessageTemplateExample.kt)
    - [Pane Template](automotive/src/main/java/com/akshay/weatherapp/templates/PaneTemplateExample.kt)
    - [Sign-In Template](automotive/src/main/java/com/akshay/weatherapp/templates/SignInTemplateExample.kt)
    - [Search Template](automotive/src/main/java/com/akshay/weatherapp/templates/SearchTemplateExample.kt)
    - [Map Template](automotive/src/main/java/com/akshay/weatherapp/templates/MapTemplateExample.kt)
    - [Navigation Template](automotive/src/main/java/com/akshay/weatherapp/templates/NavigationTemplateExample.kt)
- [Limitations](automotive/src/main/java/com/akshay/weatherapp/templates/GridTemplateExample.kt)

## Introduction

Welcome to the Android Automotive Templates project! This guide provides in-depth information on
utilizing Android Automotive templates, addressing their strengths and limitations.

## Getting Started

Follow these steps to set up the Android Virtual Device (AVD) for Polestar 2 System Image:

### Step 1: Add Polestar 2 System Image Repository

1. Open Android Studio.
2. Navigate to `Tools > SDK Manager`.
3. In the `SDK Platforms` tab, click on the `SDK Updates Sites` tab.
4. Click the '+' icon to add a new repository.
5. Give the repository a name, e.g., "Polestar 2 System Image."
6. Add the following URL: `https://developer.polestar.com/sdk/polestar2-sys-img.xml`.
7. Click 'OK' to save the repository.

### Step 2: Install Polestar 2 System Image

1. In the `SDK Platforms` tab, enable `Show Package Details`.
2. Inside the Android version you are working with (e.g., Android 10 or 9), find and enable "
   Polestar 2."
3. Click 'Apply' to initiate the download and installation of the Polestar 2 System Image.

### Step 3: Create Android Virtual Device (AVD)

1. Open the `Device Manager` by navigating to `Tools > AVD Manager`.
2. Click on `Create Virtual Device` and Select automotive.
3. Choose the hardware profile that matches your target Polestar 2 system image. If there is none,
   import a hardware profile.
4. To import a hardware profile,click on `Import Hardware Profiles`, then enter the system image
   URL (
   e.g., `C:\Users\your_user_name\AppData\Local\Android\Sdk\system-images\android-29\polestar_emulator\x86_64\devices.xml`)
   and press 'OK'.
5. Once the hardware profile is selected, press 'Next' and complete the AVD creation process.

Now you have set up the AVD with the Polestar 2 System Image. You can use this virtual device for
testing and development in your Android Automotive project.

## Prerequisites

Before getting started with this project, ensure that you have the following prerequisites:

- **Android Development Knowledge:** A basic understanding of Android app development concepts is
  essential.

- **Kotlin Functional Programming:** Familiarity with Kotlin programming language, especially its
  functional programming features.

- **Thread Handling:** Proficiency in managing threads and asynchronous programming in Android using
  features like coroutines.

- **Scope Functions:** Understanding of Kotlin scope functions (let, apply) and their appropriate
  usage in Android development.

- **Basic Knowledge of Templates:** A basic understanding of working with templates in Android
  development, including how to integrate and customize them for your project.

## Installation

**Clone the repository:**

```bash
git clone https://github.com/akshaySavanoor/AndroidAutomotive

cd AndroidAutomotive

run
```

## IMPORTANT: Setting up API Keys

To configure your application, create a directory named `app_secrets` and within it, add a file named `ApiKey` with the following content:

```kotlin
object ApiKey {
    const val API_KEY = "31xxxxxxxxxxxxxxxxxxxxxxb0"
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
}
```

