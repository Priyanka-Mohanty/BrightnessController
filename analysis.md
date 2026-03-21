# Brightness Controller - Code Analysis

## Overview
This application is a simple utility tool that allows users to adjust their device's screen brightness. It requests permission to modify system settings and provides a `SeekBar` for users to manually slide and adjust the system brightness level. It also includes functional buttons for users to Rate, Share, and leave Feedback about the application.

## Code Structure Analysis
- **App Configuration**: The app extends `AppCompatActivity` and explicitly handles Android M (API 23+) Runtime Permissions using `Settings.ACTION_MANAGE_WRITE_SETTINGS` to safely modify system values.
- **Brightness Changes**: The core logic is inside the `SeekBar.OnSeekBarChangeListener`. As the user scrubs the seek bar, `System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness)` is called. If the seek bar value drops below `20` (out of `255`), it defaults to absolute minimal brightness `0`.
- **System Intents**:
  - **Rate**: Creates an intent to navigate to the Play Store via `market://details`.
  - **Feedback**: Starts an email intent pointing to `appfeedbackpriyanka@gmail.com`.
  - **Share**: Triggers the system's "Chooser" sharing modal with pre-filled promotional text linking to the app's Play Store URL.

## Potential Issues and Anti-Patterns

While the code is functional, there are several Android anti-patterns that negatively impact performance, maintainability, and code safety. Here are the most prominent issues:

### 1. View Initialization Inside `onResume()` (Performance Issue)
```java
@Override
protected void onResume() {
    super.onResume();
    try {
        setContentView(R.layout.activity_main);
        // ...
```
**Issue:** `setContentView` along with all `findViewById` assignments should be inside `onCreate()`. Calling this inside `onResume()` means that every time the user minimizes and re-opens the app, the entire view hierarchy is torn down, re-inflated, and listeners are re-attached. This leads to dropped frames and loss of variable state.

### 2. Static Context Memory Leak
```java
@SuppressLint("StaticFieldLeak")
private static Context context;
```
**Issue:** A `StaticFieldLeak` warning is suppressed, but the leak remains. Since `MainActivity.this` contains references to the entire view tree, saving it in a static variable blocks the Garbage Collector from freeing the Activity when it is destroyed.
**Fix:** You do not need this static `context` variable. Inside anonymous classes like `OnClickListener`, refer to `MainActivity.this` directly.

### 3. Overusing Broad `try-catch-finally` Blocks
**Issue:** Almost every block of code is wrapped in a `try-catch (Exception e)`, combined with a `finally` block that simply logs `"FINAL BLOCK EXECUTED"`.
**Fix:** Catching generic `Exception` limits the ability to see specific bugs (like `NullPointerException`) during development. Only catch specific exceptions such as `ActivityNotFoundException` (which is correctly handled inside the `buttonRate` and `buttonFeedBack` clicks), and remove empty logging `finally` blocks to improve formatting readability.

### 4. Deprecated Constants
```java
window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
```
**Issue:** As of API level 27, `FLAG_DIM_BEHIND` is considered deprecated in Android. Manually manipulating `FLAG_DIM_BEHIND` flags is generally unnecessary just to change the window brightness. Changing `layoutpars.screenBrightness` alongside `System.putInt` should be sufficient on its own.
