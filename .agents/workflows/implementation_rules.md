---
description: Custom Implementation Rules & Preferences
---

# Implementation Rules

Below are the default rules, coding standards, and workflow preferences the AI agent will automatically learn and follow when modifying the **Brightness Controller** application. You can freely edit these rules to match your exact preferences!

## 0. Core Directives (New)
- **UI Framework**: Use **Jetpack Compose** exclusively for the UI implementation. Do not use legacy XML layouts.
- **Dependency Integration**: Use **Koin** for Dependency Injection.
- **Target API**: Target and compile for **API Level 37**.
- **Functional Parity**: The behavior and outcome of any refactored code must perfectly match the lagacy app.


## 1. Android Specific Guidelines
- **Lifecycle Awareness:** Never write initialization code (like `setContentView()` or `findViewById()`) inside `onResume()`. They must go inside `onCreate()`.
- **Memory Management:** Never store `Activity` or `View` contexts in `static` fields, as this causes catastrophic memory leaks. Let garbage collection do its job!
- **Exception Handling:** Stop using catch-all blocks `catch (Exception e)`. Always catch specific exceptions (like `ActivityNotFoundException` or `SecurityException`) so real crashes don't go unnoticed. Remove `finally` blocks whose only purpose is logging static text.
- **Modern APIs:** Keep APIs up to date and avoid using deprecated ones (e.g., `FLAG_DIM_BEHIND`).

## 2. Refactoring Preferences
- **Focus on readability:** Clean up messy nested `try-catch` structures. Modularize large methods into smaller, testable functions when possible.
- **Keep it safe:** Whenever making extensive changes, ensure the core Brightness Control logic remains completely intact and tested.

## 3. Workflow Behavior
- Always provide a brief explanation of *why* a particular piece of code was changed.
- Validate layout changes against `res/layout/activity_main.xml`.
