# 🚀 FinPilot — AI-Powered Personal Finance Copilot

**FinPilot** is a modern, cross-platform personal finance and expense management application built using **Kotlin Multiplatform (KMP)**, **Compose Multiplatform**, and **Material 3**. 

It empowers users to track expenses, set category budgets, analyze monthly spending trends, and receive real-time, AI-driven financial insights and conversational guidance.

---

## 📱 App Vision & Purpose

Managing personal finances often feels tedious and complex. **FinPilot** simplifies financial management by blending **offline-first local database tracking** with **artificial intelligence**:

- **Natural Language Expense Logging**: Simply type *"Paid ₹450 at Zomato"* and let AI auto-extract the amount, category, and merchant.
- **Interactive AI Financial Copilot**: Ask natural questions like *"Why did my spending rise in March?"* or *"How much did I spend on food?"* and receive intelligent, actionable advice.
- **Smart Budgeting & Delta Tracking**: View month-over-month spending changes (`▲` increase, `▼` decrease, or neutral `No change vs last month`) and monitor budget usage percentages.
- **Offline-First Synchronization**: Log transactions anywhere without internet connectivity. Changes queue locally in a sync outbox and synchronize seamlessly when online.

---

## 🗺️ Application Navigation & User Flow

```
                     ┌──────────────────────┐
                     │    Splash Screen     │
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────┐
                     │  Onboarding Carousel │
                     └──────────┬───────────┘
                                │
                     ┌──────────▼───────────┐
                     │  Login / Sign Up     │
                     └──────────┬───────────┘
                                │
       ┌────────────────────────┼────────────────────────┐
       │                        │                        │
┌──────▼──────┐          ┌──────▼──────┐          ┌──────▼──────┐
│  Dashboard  │          │  Analytics  │          │ Add Expense │
│   (Home)    │          │   (Stats)   │          │ (Quick/Auto)│
└──────┬──────┘          └─────────────┘          └──────┬──────┘
       │                                                 │
       ├─────────────────┬─────────────────┐             │
┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼──────┐      │
│  AI Copilot │   │ Transactions│   │   Profile   │◄─────┘ (on save)
│    (Chat)   │   │    List     │   │ & Settings  │
└─────────────┘   └─────────────┘   └──────┬──────┘
                                           │
                        ┌──────────────────┼──────────────────┐
                 ┌──────▼──────┐    ┌──────▼──────┐    ┌──────▼──────┐
                 │ Edit Profile│    │   Budget    │    │ Sync Devices│
                 └─────────────┘    └─────────────┘    └─────────────┘
```

### Flow Breakdown

1. **Authentication & Onboarding**:
   - **Splash Screen**: Animated branding; automatically directs to Onboarding or Login.
   - **Onboarding Carousel**: Introduces key app capabilities. Clicking *Get Started* opens Login.
   - **Login / Create Account**: Authenticate or skip directly to the Dashboard.

2. **Main Tab Navigation (Shared Bottom Bar)**:
   - 🏠 **Home / Dashboard**: Monthly spending overview card, spent vs budget progress bar, dynamic month-over-month delta chip, AI insight preview, and 5 most recent transactions.
   - 📊 **Stats / Analytics**: Weekly, Monthly, and Yearly expense breakdowns with category distribution charts.
   - ➕ **Add Expense**: Natural language AI quick-entry bar (*"Paid ₹450 at Zomato"*) alongside manual input fields (Amount, Category, Payment Method, Date, Description).
   - 🤖 **AI Copilot**: Conversational AI assistant with smart prompt suggestions and transaction-based insights.
   - 👤 **Profile**: Profile summary with navigation to account management sub-screens.

3. **Profile Sub-Screens**:
   - **Edit Profile**: Modify user details, name, and email.
   - **Budget Management**: Set and track category spending limits.
   - **Sync & Devices**: View multi-device synchronization status and resolve outbox conflicts.
   - **Notifications**: Control notification alerts and preferences.
   - **Appearance**: Toggle theme mode (*System*, *Dark*, *Light*) and Material You accent colors (*Mint*, *Ocean*, *Violet*, *Emerald*).

---

## 🛠️ Tech Stack & Architecture

### **Core Stack**
- **Kotlin Multiplatform (KMP)** `2.0.0`: Shared Kotlin business logic across Android and iOS.
- **Compose Multiplatform** `1.6.10`: Declarative, single-codebase UI for Android and iOS using Material 3 design tokens.
- **Koin** `4.0.0`: Dependency Injection (`koin-compose`, `koin-compose-viewmodel`).
- **SQLDelight** `2.0.2`: Type-safe, offline-first local SQLite database generation.
- **Ktor Client** `2.3.12`: Asynchronous HTTP network engine with auth interceptors and serialization.
- **`kotlinx-datetime`** `0.6.1`: Multiplatform date, time, and timezone utilities.
- **`kotlinx-coroutines` & StateFlow**: Reactive state management and asynchronous concurrency.

### **Architecture: Clean Architecture + MVI**
```
                     ┌───────────────────────────────────┐
                     │     UI / Presentation Layer       │
                     │ (Compose Screens & ViewModels)    │
                     └─────────────────┬─────────────────┘
                                       │
                     ┌─────────────────▼─────────────────┐
                     │          Domain Layer             │
                     │   (Use Cases & Domain Models)     │
                     └─────────────────┬─────────────────┘
                                       │
                     ┌─────────────────▼─────────────────┐
                     │           Data Layer              │
                     │(Repositories, SQLDelight, Ktor)   │
                     └───────────────────────────────────┘
```

---

## 📁 Repository Structure

```
FinPilot/
├── androidApp/                         # Android Application Target
│   └── src/main/
│       ├── AndroidManifest.xml          # Manifest with Adaptive Icons
│       ├── java/.../MainActivity.kt     # Android Entry Point
│       └── res/                         # Vector Drawables, Mipmaps, Strings
│
├── iosApp/                             # iOS Application Target
│   └── iosApp/
│       ├── Assets.xcassets/             # 1024x1024 AppIcon
│       └── Info.plist                   # iOS Configuration
│
└── shared/                             # Shared KMP Logic
    └── src/
        ├── commonMain/
        │   ├── composeResources/       # Custom Fonts (Sora, DM Sans) & Assets
        │   ├── sqldelight/             # SQLite Schemas & Queries (.sq)
        │   └── kotlin/.../
        │       ├── App.kt              # Root App Component & Scaffold
        │       ├── db/                 # SQLDelight Database Driver & Seed
        │       ├── di/                 # Koin Dependency Injection Modules
        │       ├── domain/             # Domain Models, Repositories, Use Cases
        │       ├── data/               # Data Repository Implementations
        │       ├── network/            # Ktor API Clients & DTOs
        │       ├── sync/               # Offline-First Sync Engine
        │       └── ui/                 # Compose UI Screens & Navigation
        ├── androidMain/                # Android Expect/Actual Implementations
        └── iosMain/                    # iOS Expect/Actual Implementations
```

---

## 🚀 Building & Running

### Prerequisites
- **JDK 17** or higher
- **Android Studio** (2024.1+ / Ladybug)
- **Xcode 15+** (for building iOS target on macOS)

### Android Build
```bash
# Build Debug APK
./gradlew :androidApp:assembleDebug

# Run Unit Tests
./gradlew :androidApp:testDebugUnitTest
```

### iOS Build
1. Open the Xcode workspace: `iosApp/iosApp.xcodeproj`
2. Select target device / simulator and press **Run (Cmd + R)**.

---

## 🎨 Design System Specs

- **Primary Color**: Mint `#2DD4A7`
- **Background Color**: Midnight `#0B1220`
- **Surface Color**: Dark Navy `#131C2E` / `#1B2740`
- **AI Accent Color**: Violet `#9B8CFF`
- **Headings & Amounts Font**: Sora Bold
- **Body Font**: DM Sans
