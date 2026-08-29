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
cd /home/n95/.openclaw/workspace/TXT
./gradlew assembleDebug
```

APK esperado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Bloqueo real de este entorno

No pude generar el APK aquí porque faltan herramientas base:

- `java` no está instalado
- `./gradlew` no existe todavía porque no se pudo generar wrapper sin Java/Gradle
- no se confirmó un Android SDK funcional

El proyecto queda listo para abrir en Android Studio y compilar allí.

## Mejoras opcionales

- recordar posición de scroll por archivo
- modo sepia/noche específico de lectura
- búsqueda dentro del texto
- favoritos o historial reciente
- soporte para `.md` y `.log`
