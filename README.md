<!-- app-release:start -->
[**Descargar APK v3**](https://github.com/ricardoyf/TXTReader/raw/refs/tags/v3/release-artifacts/v3/TXTReader-v3.apk) · [SHA-256](https://github.com/ricardoyf/TXTReader/raw/refs/tags/v3/release-artifacts/v3/TXTReader-v3.apk.sha256)

`7cbbe356cf108d0c731af56076f131a9ac70882181996a04faccb513f86a0862`
<!-- app-release:end -->

# TXT Reader

App Android nativa en **Kotlin + Jetpack Compose** para leer archivos **.txt locales** en el móvil.

## Qué hace

- Selección de carpeta local con **Storage Access Framework (SAF)**
- Navegación simple por **carpetas, subcarpetas y archivos .txt**
- **Lector limpio** con tipografía cuidada
- **Swipe horizontal** izquierda/derecha para ir al archivo siguiente/anterior
- Guarda la **última carpeta**, el **último archivo** y el **tamaño de letra** con **DataStore**
- Todo funciona **en local**, sin red, sin backend y sin permisos peligrosos clásicos de almacenamiento

## Arquitectura

```text
com.ricardo.txtreader/
├── data/
│   ├── PreferencesRepository.kt
│   └── TxtRepository.kt
├── model/
│   └── ReaderModels.kt
├── navigation/
│   ├── Destinations.kt
│   └── TxtReaderApp.kt
├── ui/
│   ├── components/
│   │   └── EmptyStateCard.kt
│   ├── screens/
│   │   ├── LibraryScreen.kt
│   │   └── ReaderScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       ├── ReaderUiState.kt
│       ├── ReaderViewModel.kt
│       └── ReaderViewModelFactory.kt
└── MainActivity.kt
```

## Cómo abrir en Android Studio

1. Instala Android Studio Jellyfish o superior.
2. Ten disponible JDK 17 y Android SDK 34.
3. Abre la carpeta `TXT` como proyecto.
4. Espera la sincronización de Gradle.
5. Ejecuta en emulador o dispositivo físico.

## Compilar APK

Cuando el entorno tenga toolchain Android real:

```bash
./gradlew assembleDebug
```

APK esperado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Mejoras opcionales

- recordar posición de scroll por archivo
- modo sepia/noche específico de lectura
- búsqueda dentro del texto
- favoritos o historial reciente
- soporte para `.md` y `.log`
