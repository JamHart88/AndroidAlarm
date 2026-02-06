# Android Alarm

A simple yet customizable alarm clock application for Android, built with modern Android development practices using Jetpack Compose.

## 🌟 Features

*   **Digital Clock Display**: Shows the current time on a clean, dark-themed interface.
*   **Alarm Functionality**: Easily set, change, and toggle alarms.
*   **Customizable Brightness**: Adjust screen brightness separately for daytime and nighttime. You can set a preferred brightness level for both day and night modes, and the app will automatically switch between them.
*   **Sound Selection**: Choose from a variety of built-in alarm sounds.
*   **Smart Alarm Setting**: The app warns you if you're setting an alarm for a weekend or a federal holiday, giving you the option to confirm. This helps prevent accidental alarms on your days off.
*   **Modern UI**: A beautiful and intuitive user interface built entirely with Jetpack Compose.

## 🚀 How to Use

1.  The main screen displays the current time.
2.  Use the top-bar icons to navigate:
    *   **Sound Settings**: Tap the leftmost icon to open a list of available alarm sounds.
    *   **Set Alarm**: Tap the middle icon to enter alarm setting mode. Use the `+` and `-` buttons to adjust the hour and minute, and the `Set` button to confirm each stage.
    *   **Brightness**: Tap the rightmost icon to adjust the screen brightness.
3.  The controls at the bottom change based on the current mode (setting alarm, adjusting brightness, or default).
4.  The main alarm toggle button at the bottom allows you to activate or deactivate the alarm.

## 🎯 Target Device

This application was designed and tested for a **Samsung Galaxy Tab A (2016)** running **LineageOS**. The screen resolution of this device is **800 x 1280 pixels**.

*   **Minimum Android Version**: Android 7.1 (API Level 25)
*   **Target Android Version**: Android 15 (API Level 36)

## 🛠️ Tech Stack

*   [Kotlin](https://kotlinlang.org/): The programming language used.
*   [Jetpack Compose](https://developer.android.com/jetpack/compose): Android's modern toolkit for building native UI.
*   [Android Studio](https://developer.android.com/studio): The official IDE for Android app development.

## ⚙️ Setup

To build and run this project:

1.  Clone the repository:
    ```bash
    git clone <repository-url>
    ```
2.  Open the project in Android Studio.
3.  Let Gradle sync the project dependencies.
4.  Run the application on an Android emulator or a physical device.
