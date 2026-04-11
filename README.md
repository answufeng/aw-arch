# aw-arch

Android architecture foundation library. Provides MVVM/MVI base classes, navigation, event bus, Flow extensions, and state management.

## Installation

Add the dependency in your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.answufeng:aw-arch:1.0.0")
}
```

Make sure you have the JitPack repository in your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

## Features

- MVVM: BaseViewModel + MvvmActivity/Fragment/DialogFragment/BottomSheetDialogFragment
- MVI: MviViewModel + MviActivity/Fragment/DialogFragment/BottomSheetDialogFragment
- BrickNav: Pure-code Fragment navigation with animations, interceptors, and back stack
- FlowEventBus: SharedFlow-based event bus with sticky support
- Flow extensions: throttleFirst, debounceAction, select
- LoadState: Loading/Success/Error sealed class with retry support
- Lifecycle-aware collection helpers
- ViewBinding delegate for Activity/Fragment
- BrickTestRule: JUnit4 rule for coroutine testing

## Usage

```kotlin
// MVVM
class MainViewModel : BaseViewModel() {
    fun loadData() = launchIO { /* ... */ }
}
class MainActivity : MvvmActivity<ActivityMainBinding, MainViewModel>() {
    override fun render(uiEvent: BaseViewModel.UIEvent) { /* handle events */ }
}

// MVI
sealed class MainIntent : UiIntent { data object Load : MainIntent() }
class MainViewModel : MviViewModel<MainState, MainEvent, MainIntent>(MainState()) {
    override fun handleIntent(intent: MainIntent) { /* reduce state */ }
}

// Navigation
val nav = BrickNav.init(activity, R.id.container)
    .register<HomeFragment>("home")
    .register<ProfileFragment>("profile")
nav.navigate("profile")

// Event Bus
FlowEventBus.post(DemoEvent("hello"))
FlowEventBus.observe<DemoEvent>().collectOnLifecycle(this) { /* handle */ }
```

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.
