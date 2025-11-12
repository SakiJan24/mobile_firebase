# Taller Final - Aplicación de Rastreo de Ubicaciones

Aplicación Android que permite el registro y autenticación de usuarios con rastreo de ubicación en tiempo real usando Firebase.

## Características Implementadas

### ✅ Autenticación y Registro
- Registro de usuarios con:
  - Nombre
  - Email
  - Password
  - Número de teléfono
- Login y Logout
- Firebase Authentication para email/password
- Firebase Realtime Database para datos secundarios

### ✅ Gestión de Perfil
- Visualización de datos del usuario
- Edición de nombre y teléfono (Realtime Database)
- Cambio de contraseña (Firebase Auth)
- Menú con opciones de perfil y cerrar sesión

### ✅ Mapa y Ubicación en Tiempo Real
- Mapa de Google Maps integrado
- Switch para activar/desactivar compartir ubicación
- Marcador azul para la ubicación del usuario actual
- Marcadores rojos para otros usuarios en línea
- Polylines (rutas) para visualizar el recorrido:
  - Azul para el usuario actual
  - Rojo para otros usuarios
- Actualización en tiempo real de posiciones
- Limpieza automática de rutas al desconectarse
- Contador de usuarios en línea

### ✅ Permisos
- Solicitud de permisos de ubicación (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
- Manejo de permisos con Accompanist Permissions

### ✅ Fotos de Perfil (BONO - 0.5pts)
- Cámara y galería para seleccionar foto de perfil
- Firebase Storage para almacenar fotos
- Marcadores personalizados en el mapa con fotos de usuarios
- Actualización automática de marcadores al cambiar foto
- Diseño circular con borde de color (azul para usuario actual, rojo para otros)

## Estructura del Proyecto

```
app/src/main/java/com/example/tallerfinal/
├── MainActivity.kt                    # Actividad principal
├── TallerFinalApp.kt                 # Inicialización de Firebase
├── data/
│   └── User.kt                       # Modelo de datos de usuario
├── navigation/
│   └── AppNavigation.kt              # Navegación con Compose
├── services/
│   └── LocationHandler.kt            # Manejo de ubicación y actualización en Firebase
└── ui/
    ├── screens/
    │   ├── auth/
    │   │   ├── AuthViewModel.kt      # ViewModel de autenticación
    │   │   ├── LoginScreen.kt        # Pantalla de login
    │   │   └── RegisterScreen.kt     # Pantalla de registro
    │   └── main/
    │       ├── HomeScreen.kt          # Pantalla principal con mapa
    │       ├── MapViewModel.kt        # ViewModel para gestionar usuarios en línea
    │       ├── ProfileScreen.kt       # Pantalla de perfil
    │       └── ProfileViewModel.kt    # ViewModel de perfil
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Configuración del Proyecto

### 1. Firebase Setup

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto o usa uno existente
3. Añade una aplicación Android con el package name: `com.example.tallerfinal`
4. Descarga el archivo `google-services.json` y colócalo en `app/`

### 2. Firebase Realtime Database

1. En Firebase Console, ve a Realtime Database
2. Crea una base de datos
3. Copia la URL de la base de datos (ej: `https://tu-proyecto.firebaseio.com/`)
4. Actualiza la URL en los siguientes archivos:
   - `AuthViewModel.kt` (línea ~18)
   - `ProfileViewModel.kt` (línea ~18)
   - `HomeScreen.kt` (línea ~39)

### 3. Firebase Authentication

1. En Firebase Console, ve a Authentication
2. Habilita el método de autenticación "Email/Password"

### 4. Reglas de Seguridad de Firebase Realtime Database

Configura las siguientes reglas en Firebase Console:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": true,
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

### 5. Google Maps API Key

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un proyecto o selecciona uno existente
3. Habilita "Maps SDK for Android"
4. Crea una API Key
5. Restringe la API Key (opcional pero recomendado):
   - Tipo: Aplicaciones Android
   - Agrega el package name y el SHA-1 de tu keystore
6. Copia la API Key y pégala en `AndroidManifest.xml` (línea ~20):
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="TU_API_KEY_AQUI" />
   ```

### 6. Obtener el SHA-1 (para Google Maps)

Ejecuta en terminal (desde la raíz del proyecto):

**En Windows (cmd):**
```cmd
gradlew signingReport
```

**En Linux/Mac:**
```bash
./gradlew signingReport
```

Copia el SHA-1 del debug keystore.

## Dependencias Principales

- **Firebase BOM 33.0.0**
  - Firebase Authentication
  - Firebase Realtime Database
- **Jetpack Compose** (Material 3)
- **Navigation Compose**
- **Google Maps Compose 4.3.3**
- **Play Services Location 21.3.0**
- **Accompanist Permissions 0.34.0**

## Cómo Ejecutar

1. Clona el repositorio
2. Abre el proyecto en Android Studio
3. Completa la configuración de Firebase y Google Maps (pasos anteriores)
4. Sincroniza el proyecto con Gradle
5. Ejecuta la aplicación en un emulador o dispositivo físico

**Nota:** Para probar la funcionalidad de ubicación en tiempo real, se recomienda usar un dispositivo físico o configurar la ubicación en el emulador.

## Funcionalidades del Mapa

### Usuario Actual
- **Marcador azul circular:** Muestra tu posición actual con tu foto de perfil
- **Línea azul:** Muestra tu ruta recorrida mientras estás en línea
- **Switch:** Activa/desactiva el compartir ubicación

### Otros Usuarios
- **Marcadores rojos circulares:** Muestra la posición de otros usuarios en línea con sus fotos
- **Líneas rojas:** Muestra las rutas de otros usuarios
- **Actualización automática:** Los marcadores y rutas se actualizan en tiempo real
- **Limpieza automática:** Al desconectarse, los marcadores y rutas se eliminan

## Funcionalidades de Foto de Perfil (BONO)

- Desde la pantalla de perfil, toca la foto circular
- Selecciona "Tomar foto" (requiere permiso de cámara) o "Seleccionar de galería"
- La foto se sube automáticamente a Firebase Storage
- El marcador en el mapa se actualiza con la nueva foto
- Otros usuarios ven tu foto en tu marcador en tiempo real

## Notas Importantes

- La aplicación requiere permisos de ubicación para funcionar
- Los datos de ubicación solo se comparten cuando el switch está activado
- Al cerrar sesión, la ubicación se desactiva automáticamente
- Las rutas se limpian cuando el usuario se desconecta
- No hay límite en el número de usuarios que se pueden visualizar en el mapa
- Las fotos de perfil se almacenan en Firebase Storage y se cargan automáticamente en los marcadores

## Configuración de Firebase Storage (BONO)

1. En Firebase Console, ve a Storage
2. Haz clic en "Comenzar"
3. Selecciona el modo de producción (o prueba para desarrollo)
4. Elige la ubicación de almacenamiento
5. Configura las reglas de seguridad (ver FIREBASE_RULES.md)

## Autor

Proyecto desarrollado para el Taller Final del curso.

