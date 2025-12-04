# 🏋️ Aplicación de Gestión de Entrenamientos

## 📋 Descripción del Proyecto

Aplicación Android nativa desarrollada en **Java** que permite gestionar una lista de entrenamientos deportivos. La aplicación cuenta con una interfaz adaptativa que cambia según la orientación del dispositivo (portrait/landscape), persistencia de datos, y un sistema completo de gestión de entrenamientos.

---

## 🎯 Características Principales

### ✅ Funcionalidades Implementadas

1. **ListView Interactivo**
   - Lista de entrenamientos con icono y nombre
   - 4 entrenamientos predefinidos (Pilates, Voleibol, Fuerza, Running)
   - Scroll suave con indicador visible
   - Adapter personalizado con ViewHolder pattern

2. **Sistema de Fragments**
   - Fragment de detalle con información completa del entrenamiento
   - Vista adaptativa según orientación:
     - **Portrait**: Fragment a pantalla completa
     - **Landscape**: Vista dual (ListView + Fragment lado a lado)

3. **Añadir Entrenamientos**
   - DialogFragment con formulario completo
   - Campos: Nombre, Descripción
   - Selector visual de iconos (4 opciones)
   - Validación de campos obligatorios
   - **Sistema anti-duplicados** (ignora mayúsculas/minúsculas)

4. **Persistencia de Datos**
   - Los entrenamientos se guardan permanentemente
   - Usa SharedPreferences + Gson para serialización JSON
   - Los datos persisten al cerrar y abrir la app
   - Auto-guardado al agregar/modificar datos

5. **Diseño Responsive**
   - Layouts diferenciados para portrait y landscape
   - Toolbar compacto con Material Design
   - Diálogo optimizado para ambas orientaciones
   - Items del ListView adaptados al espacio disponible

6. **Organización del Código**
   - Clases específicas para cada tipo de entrenamiento
   - Carpeta `entrenamientos/` para organización
   - Patrón MVC (Model-View-Controller)
   - Código limpio y documentado

---

## 🏗️ Arquitectura del Proyecto

### 📁 Estructura de Carpetas

```
app/src/main/
├── java/com/example/examen1rtrimestremacia_denislopezsacher/
│   ├── MainActivity.java                          ← Activity principal
│   ├── Entrenamiento.java                         ← Modelo de datos (Parcelable)
│   ├── EntrenamientoAdapter.java                  ← Adapter del ListView
│   ├── EntrenamientoStorage.java                  ← Persistencia de datos
│   ├── FragmentDetalle.java                       ← Fragment de detalle
│   ├── AddEntrenamientoDialogFragment.java        ← Diálogo para añadir
│   └── entrenamientos/                            ← Clases específicas
│       ├── Pilates.java
│       ├── Voleibol.java
│       ├── Fuerza.java
│       └── Running.java
│
└── res/
    ├── layout/                                     ← Layouts portrait
    │   ├── activity_main.xml
    │   ├── item_entrenamiento.xml
    │   ├── fragment_detalle.xml
    │   └── dialog_add_entrenamiento.xml
    │
    ├── layout-land/                                ← Layouts landscape
    │   ├── activity_main.xml                       (Vista dual)
    │   ├── item_entrenamiento.xml                  (Compacto)
    │   └── dialog_add_entrenamiento.xml            (Optimizado)
    │
    ├── drawable/                                   ← Iconos vectoriales
    │   ├── ic_pilates.xml
    │   ├── ic_voleibol.xml
    │   ├── ic_fuerza.xml
    │   ├── ic_running.xml
    │   └── ic_add.xml
    │
    ├── menu/
    │   └── options_menu.xml                        ← Menú del toolbar
    │
    └── values/
        ├── colors.xml                              ← Colores morados
        ├── strings.xml                             ← Textos
        └── themes.xml                              ← Temas Material Design
```

---

## 🔧 Componentes Técnicos

### 1. **MainActivity.java**

**Responsabilidades:**
- Gestiona la Activity principal
- Inicializa el Toolbar con menú
- Carga y gestiona la lista de entrenamientos
- Maneja la persistencia de datos
- Detecta orientación y adapta la UI
- Implementa callbacks para el DialogFragment

**Métodos Clave:**
```java
onCreate()                          // Inicialización
initEntrenamientos()                // Carga entrenamientos iniciales
mostrarDetalle(Entrenamiento)       // Muestra fragment de detalle
onEntrenamientoAdded(Entrenamiento) // Callback al añadir entrenamiento
configurarBackNavigation()          // Gestiona botón atrás
onPause()                           // Guarda datos al cerrar
```

**Flujo de Datos:**
```
1. onCreate() → Carga datos de SharedPreferences (si existen)
2. Si no hay datos → initEntrenamientos() (4 por defecto)
3. Usuario añade entrenamiento → onEntrenamientoAdded()
4. Se guarda automáticamente en storage
5. onPause() → Guarda antes de cerrar app
```

---

### 2. **Entrenamiento.java** (Modelo)

**Características:**
- Clase modelo que representa un entrenamiento
- Implementa **Parcelable** para pasar entre componentes
- Atributos: `nombre`, `descripcion`, `iconoResId`

**¿Por qué Parcelable?**
- Permite pasar objetos complejos entre Activities/Fragments
- Más eficiente que Serializable en Android
- Necesario para guardar en `savedInstanceState`

```java
public class Entrenamiento implements Parcelable {
    private String nombre;
    private String descripcion;
    private int iconoResId;
    
    // Constructor, getters, setters
    // Métodos Parcelable: writeToParcel(), createFromParcel()
}
```

---

### 3. **EntrenamientoAdapter.java** (Adapter)

**Patrón ViewHolder:**
```java
public class EntrenamientoAdapter extends ArrayAdapter<Entrenamiento> {
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            // Inflar layout solo si es necesario
            convertView = inflater.inflate(R.layout.item_entrenamiento, ...);
            holder = new ViewHolder();
            holder.ivIcono = convertView.findViewById(R.id.iv_icono);
            holder.tvNombre = convertView.findViewById(R.id.tv_nombre);
            convertView.setTag(holder);
        } else {
            // Reutilizar vista existente
            holder = (ViewHolder) convertView.getTag();
        }
        
        // Actualizar datos
        Entrenamiento item = entrenamientos.get(position);
        holder.ivIcono.setImageResource(item.getIconoResId());
        holder.tvNombre.setText(item.getNombre());
        
        return convertView;
    }
    
    static class ViewHolder {
        ImageView ivIcono;
        TextView tvNombre;
    }
}
```

**Ventajas del ViewHolder:**
- Evita llamadas repetidas a `findViewById()`
- Mejora el rendimiento del scroll
- Reduce consumo de memoria

---

### 4. **EntrenamientoStorage.java** (Persistencia)

**Tecnología:** SharedPreferences + Gson

**¿Cómo funciona?**

```java
// GUARDAR
public void guardarEntrenamientos(List<Entrenamiento> entrenamientos) {
    String json = gson.toJson(entrenamientos);  // Lista → JSON
    preferences.edit()
        .putString(KEY_ENTRENAMIENTOS, json)
        .apply();  // Guardado asíncrono
}

// CARGAR
public List<Entrenamiento> cargarEntrenamientos() {
    String json = preferences.getString(KEY_ENTRENAMIENTOS, null);
    if (json != null) {
        Type type = new TypeToken<ArrayList<Entrenamiento>>(){}.getType();
        return gson.fromJson(json, type);  // JSON → Lista
    }
    return null;
}
```

**Flujo de Persistencia:**
```
Lista de Entrenamientos
        ↓
    Gson.toJson()
        ↓
  String JSON
        ↓
SharedPreferences.putString()
        ↓
Archivo XML en dispositivo
(/data/data/com.example.../shared_prefs/EntrenamientosPrefs.xml)

        ↓ (Al abrir app)

SharedPreferences.getString()
        ↓
  String JSON
        ↓
    Gson.fromJson()
        ↓
Lista de Entrenamientos
```

---

### 5. **FragmentDetalle.java**

**Función:**
- Muestra información detallada de un entrenamiento
- Recibe datos mediante Bundle en `newInstance()`

**Patrón Factory:**
```java
public static FragmentDetalle newInstance(String nombre, String desc, int icono) {
    FragmentDetalle fragment = new FragmentDetalle();
    Bundle args = new Bundle();
    args.putString(ARG_NOMBRE, nombre);
    args.putString(ARG_DESCRIPCION, desc);
    args.putInt(ARG_ICONO, icono);
    fragment.setArguments(args);
    return fragment;
}
```

**Ventaja:**
- Patrón recomendado por Google
- Evita constructores con parámetros en Fragments
- Los datos sobreviven a recreación del Fragment

---

### 6. **AddEntrenamientoDialogFragment.java**

**Características:**
- DialogFragment con formulario completo
- Validación de campos (nombre, descripción)
- Selector visual de iconos
- **Sistema anti-duplicados**

**Sistema Anti-Duplicados:**
```java
// Validación case-insensitive
for (Entrenamiento existente : entrenamientosExistentes) {
    if (existente.getNombre().equalsIgnoreCase(nombre)) {
        Toast.makeText(getContext(), 
            "Ya existe: " + existente.getNombre(),
            Toast.LENGTH_LONG).show();
        return;  // Bloquea guardado
    }
}
```

**Callback Pattern:**
```java
public interface OnEntrenamientoAddedListener {
    void onEntrenamientoAdded(Entrenamiento entrenamiento);
    List<Entrenamiento> getEntrenamientosExistentes();
}

// MainActivity implementa la interfaz
// DialogFragment llama al callback cuando se guarda
```

**Tamaño Adaptativo:**
```java
@Override
public void onStart() {
    if (orientation == ORIENTATION_LANDSCAPE) {
        // 65% del ancho en landscape
        int width = (int) (screenWidth * 0.65);
        dialog.getWindow().setLayout(width, WRAP_CONTENT);
    } else {
        // 100% en portrait
        dialog.getWindow().setLayout(MATCH_PARENT, WRAP_CONTENT);
    }
}
```

---

### 7. **Clases de Entrenamientos Específicas**

**Ubicación:** `entrenamientos/`

**Patrón de Herencia:**
```java
public class Pilates extends Entrenamiento {
    public Pilates() {
        super(
            "Pilates",
            "Ejercicio de bajo impacto que mejora la flexibilidad...",
            R.drawable.ic_pilates
        );
    }
    
    // Constructor Parcelable
    protected Pilates(Parcel in) {
        super(in);
    }
    
    // CREATOR para deserialización
    public static final Creator<Pilates> CREATOR = ...
}
```

**Ventajas:**
- Cada entrenamiento encapsula su propia información
- Fácil agregar métodos específicos (ej: `getDificultad()`)
- Código más organizado y mantenible
- Sigue principios OOP (Herencia, Encapsulación)

---

## 🎨 Diseño y UI/UX

### Material Design

**Colores Principales:**
```xml
<color name="purple_primary">#6200EA</color>
<color name="purple_primary_dark">#3700B3</color>
<color name="purple_accent">#9C27B0</color>
<color name="purple_light">#E1BEE7</color>
<color name="white">#FFFFFFFF</color>
```

**Componentes Material:**
- MaterialToolbar con elevación
- TextInputLayout con estilo OutlinedBox
- Botones con estilos Material
- Ripple effects en items clickeables

---

### Responsive Design

#### **Portrait:**
```
┌─────────────────────────┐
│ [Toolbar]        [+]    │
├─────────────────────────┤
│ [●] Pilates             │
│ [●] Voleibol            │
│ [●] Fuerza              │
│ [●] Running             │
│ [●] Yoga                │
│                         │
└─────────────────────────┘
```

#### **Landscape:**
```
┌──────────────────────────────────────┐
│ [Toolbar]                 [+]        │
├──────────┬───────────────────────────┤
│ [●] Pila │                           │
│ [●] Volei│   Fragment Detalle        │
│ [●] Fuerz│   - Icono grande          │
│ [●] Runni│   - Nombre                │
│          │   - Descripción           │
└──────────┴───────────────────────────┘
  ListView        Fragment (2/3)
   (1/3)
```

**Optimizaciones Landscape:**
- Toolbar: 56dp → 48dp (ahorra 8dp)
- Items: iconos 48dp → 40dp, texto 18sp → 16sp
- Diálogo: 100% → 65% del ancho
- Padding reducido en todos los elementos

---

## 🔄 Flujos de Usuario

### Flujo 1: Ver Detalle de Entrenamiento

```
Usuario abre app
    ↓
MainActivity carga datos de storage
    ↓
ListView muestra entrenamientos
    ↓
Usuario hace clic en un item
    ↓
[Portrait]                     [Landscape]
ListView se oculta             ListView permanece visible
Fragment ocupa pantalla        Fragment aparece a la derecha
    ↓                              ↓
Fragment muestra:              Fragment muestra:
- Icono grande                 - Icono grande
- Nombre en header             - Nombre en header
- Descripción                  - Descripción
    ↓                              ↓
Botón Atrás                    Click en otro item
→ Vuelve a ListView            → Cambia fragment
```

---

### Flujo 2: Añadir Entrenamiento

```
Usuario hace clic en botón [+]
    ↓
DialogFragment aparece
    ↓
Usuario ingresa:
- Nombre: "Yoga"
- Descripción: "Ejercicio de relajación..."
- Selecciona icono (ej: Pilates)
    ↓
Usuario pulsa [Guardar]
    ↓
Validaciones:
✓ ¿Nombre vacío? → Mostrar error
✓ ¿Descripción vacía? → Mostrar error
✓ ¿Nombre duplicado? → Mostrar error "Ya existe: Pilates"
    ↓
Si todo OK:
    ↓
Crear nuevo Entrenamiento
    ↓
MainActivity.onEntrenamientoAdded()
    ↓
entrenamientos.add(nuevo)
    ↓
storage.guardarEntrenamientos(lista)
    ↓
adapter.notifyDataSetChanged()
    ↓
ListView se actualiza
    ↓
Diálogo se cierra
```

---

### Flujo 3: Persistencia de Datos

```
[GUARDADO]
Usuario añade "Yoga"
    ↓
Lista actualizada en memoria
    ↓
storage.guardarEntrenamientos(lista)
    ↓
Gson convierte lista a JSON:
[{"nombre":"Pilates","descripcion":"...","iconoResId":2131165279}, ...]
    ↓
SharedPreferences guarda JSON
    ↓
Archivo XML creado en disco:
/data/data/.../shared_prefs/EntrenamientosPrefs.xml

[CIERRE Y APERTURA]
Usuario cierra app (onPause)
    ↓
storage.guardarEntrenamientos() (seguridad)
    ↓
App se cierra
    ↓
    ... tiempo ...
    ↓
Usuario abre app
    ↓
MainActivity.onCreate()
    ↓
storage.cargarEntrenamientos()
    ↓
SharedPreferences lee JSON
    ↓
Gson convierte JSON a lista
    ↓
Lista restaurada con todos los entrenamientos
    ↓
ListView muestra todo (incluyendo "Yoga")
```

---

## 🛡️ Gestión de Ciclo de Vida

### Activity Lifecycle

```java
onCreate() {
    // 1. Inicializar vistas
    // 2. Crear EntrenamientoStorage
    // 3. Cargar datos:
    //    - savedInstanceState (rotación)
    //    - SharedPreferences (apertura normal)
    //    - initEntrenamientos() (primera vez)
    // 4. Configurar adapter
}

onSaveInstanceState(Bundle) {
    // Guardar lista en Bundle (rotación)
    outState.putParcelableArrayList("entrenamientos", lista);
}

onPause() {
    // Guardar en SharedPreferences (cierre/background)
    storage.guardarEntrenamientos(entrenamientos);
}
```

**Escenarios:**

| Acción | Método Llamado | Fuente de Datos |
|--------|----------------|-----------------|
| Abrir app primera vez | onCreate() | initEntrenamientos() |
| Rotar pantalla | onCreate() → onSaveInstanceState() | savedInstanceState (Bundle) |
| Cerrar y abrir app | onCreate() → onPause() | SharedPreferences |
| App en background | onPause() | (guarda en SharedPreferences) |

---

## 🧩 Patrones de Diseño Utilizados

### 1. **MVC (Model-View-Controller)**
- **Model:** `Entrenamiento.java`, `EntrenamientoStorage.java`
- **View:** XML layouts
- **Controller:** `MainActivity.java`, Adapter, Fragments

### 2. **Adapter Pattern**
- `EntrenamientoAdapter` adapta la lista de datos al ListView

### 3. **ViewHolder Pattern**
- Optimiza el rendimiento del ListView
- Evita llamadas repetidas a `findViewById()`

### 4. **Factory Pattern**
- `FragmentDetalle.newInstance()` - Creación controlada de fragments

### 5. **Callback Pattern**
- `OnEntrenamientoAddedListener` - Comunicación DialogFragment → MainActivity

### 6. **Singleton Pattern (implícito)**
- SharedPreferences es singleton por naturaleza

### 7. **Observer Pattern**
- `adapter.notifyDataSetChanged()` - Notifica cambios al ListView

---

## 📱 Características de Accesibilidad

### Material Design Guidelines

✅ **Áreas Táctiles Mínimas:**
- Botones: mínimo 48dp × 48dp
- Items clickeables: altura suficiente para toque cómodo

✅ **Contraste de Colores:**
- Texto blanco sobre morado oscuro (#6200EA)
- Cumple ratio de contraste WCAG

✅ **ContentDescriptions:**
- Todos los ImageView tienen descripción para screen readers

✅ **Feedback Visual:**
- Ripple effects en elementos clickeables
- Cambio de color en iconos seleccionados

---

## 🔐 Validaciones Implementadas

### 1. **Campos Obligatorios**
```java
if (nombre.isEmpty()) {
    Toast.makeText("Por favor ingresa un nombre");
    return;
}
```

### 2. **Anti-Duplicados (Case-Insensitive)**
```java
if (existente.getNombre().equalsIgnoreCase(nombre)) {
    Toast.makeText("Ya existe: " + existente.getNombre());
    return;
}
```

**Ejemplos:**
- "Pilates" vs "pilates" → **Duplicado** ❌
- "VOLEIBOL" vs "Voleibol" → **Duplicado** ❌
- "Yoga" vs "Pilates" → **Permitido** ✅

### 3. **Validación de Null**
- Verificaciones en `onCreate()` antes de usar datos
- Comprobación de `savedInstanceState != null`
- Validación de `storage.hayEntrenamientosGuardados()`

---

## 📦 Dependencias Externas

### build.gradle.kts

```kotlin
dependencies {
    implementation("androidx.appcompat:appcompat:1.x.x")
    implementation("com.google.android.material:material:1.x.x")
    implementation("androidx.activity:activity:1.x.x")
    implementation("androidx.constraintlayout:constraintlayout:2.x.x")
    
    // Gson para serialización JSON
    implementation("com.google.code.gson:gson:2.10.1")
}
```

**¿Por qué Gson?**
- Convierte objetos Java ↔ JSON fácilmente
- Ligero y eficiente
- Ampliamente usado en Android
- Soporta tipos genéricos (List<Entrenamiento>)

---

## 🚀 Cómo Funciona la App (Resumen)

### 1. **Primera Apertura**
```
App abre → No hay datos guardados → Crea 4 entrenamientos por defecto
→ Los guarda en SharedPreferences → Usuario los ve en ListView
```

### 2. **Usuario Añade Entrenamiento**
```
Click [+] → Formulario → Ingresa datos → Valida → Guarda
→ Actualiza lista → Refresca ListView → Guarda en disco
```

### 3. **Cierre y Apertura**
```
Cierra app → onPause() guarda datos → App se cierra
→ Usuario abre app → onCreate() carga datos → Todo restaurado
```

### 4. **Rotación de Pantalla**
```
Portrait → Rotar → onSaveInstanceState() guarda en Bundle
→ Activity se destruye y recrea → onCreate() restaura desde Bundle
→ Layout landscape se carga → Vista dual aparece
```

---

## 🎓 Conceptos Android Aplicados

### Fundamentales
- ✅ Activities y su ciclo de vida
- ✅ Fragments y FragmentManager
- ✅ Layouts XML y ViewBinding
- ✅ ListView y Adapters personalizados
- ✅ DialogFragments
- ✅ Menús de opciones

### Intermedios
- ✅ SharedPreferences para persistencia
- ✅ Parcelable para paso de datos
- ✅ savedInstanceState para rotación
- ✅ Layouts alternativos (layout-land)
- ✅ Configuration changes

### Avanzados
- ✅ Serialización JSON con Gson
- ✅ ViewHolder pattern
- ✅ Callback interfaces
- ✅ Material Design components
- ✅ Responsive design
- ✅ OnBackPressedCallback (API moderna)

---

## 🐛 Problemas Resueltos Durante el Desarrollo

### Problema 1: Lista se pierde al rotar
**Solución:** Implementar Parcelable + savedInstanceState

### Problema 2: Datos desaparecen al cerrar app
**Solución:** SharedPreferences + Gson para persistencia permanente

### Problema 3: Pantalla en blanco al presionar atrás
**Solución:** Usar OnBackPressedCallback con orden correcto de operaciones

### Problema 4: Botón ACTION no visible en landscape
**Solución:** Toolbar compacto (48dp) + items optimizados

### Problema 5: Diálogo muy grande en landscape
**Solución:** Layout landscape específico + ancho dinámico (65%)

### Problema 6: Duplicados de entrenamientos
**Solución:** Validación case-insensitive con equalsIgnoreCase()

---

## 📚 Estructura de Datos

### JSON Guardado (ejemplo)

```json
[
  {
    "nombre": "Pilates",
    "descripcion": "Ejercicio de bajo impacto que mejora la flexibilidad, fuerza muscular y postura corporal.",
    "iconoResId": 2131165279
  },
  {
    "nombre": "Voleibol",
    "descripcion": "Deporte de equipo que mejora la coordinación, agilidad y trabajo en equipo.",
    "iconoResId": 2131165280
  },
  {
    "nombre": "Yoga",
    "descripcion": "Ejercicio de relajación y flexibilidad",
    "iconoResId": 2131165279
  }
]
```

**Ubicación:**
```
/data/data/com.example.examen1rtrimestremacia_denislopezsacher/
    shared_prefs/EntrenamientosPrefs.xml
```

---

## 🎯 Preguntas Frecuentes para el Profesor

### 1. **¿Por qué usas Parcelable en lugar de Serializable?**
**Respuesta:** Parcelable es más eficiente en Android porque está optimizado para IPC (Inter-Process Communication). Serializable usa reflexión Java que es más lenta y consume más memoria. Para paso de datos entre Activities/Fragments, Parcelable es la opción recomendada por Google.

### 2. **¿Cómo funciona el ViewHolder pattern?**
**Respuesta:** Cuando el ListView hace scroll, reutiliza las vistas que salen de pantalla. El ViewHolder guarda las referencias a los componentes (ImageView, TextView) en el tag de la vista, evitando llamar a `findViewById()` repetidamente. Esto mejora el rendimiento del scroll significativamente.

### 3. **¿Por qué guardas datos en onPause() si ya guardas al añadir?**
**Respuesta:** Es una medida de seguridad. Si el usuario modifica algo y cierra la app inmediatamente, `onPause()` asegura que los datos se guarden. Es como un "auto-save" adicional.

### 4. **¿Qué pasa con los iconos cuando guardas en JSON?**
**Respuesta:** Los iconos se guardan como enteros (Resource IDs). Por ejemplo, `R.drawable.ic_pilates` es un número único. Al cargar, Android resuelve ese número de vuelta al recurso correcto.

### 5. **¿Por qué cada entrenamiento tiene su propia clase?**
**Respuesta:** Es una decisión de diseño orientado a objetos. Aunque ahora solo encapsulan datos, en el futuro podríamos añadir métodos específicos como `getDificultad()`, `getDuracionRecomendada()`, etc. Además, el código está más organizado y es más fácil de mantener.

### 6. **¿Cómo detectas la orientación?**
**Respuesta:** 
```java
int orientation = getResources().getConfiguration().orientation;
if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
    // Código para landscape
}
```
Android tiene carpetas especiales como `layout-land/` que se cargan automáticamente en landscape.

### 7. **¿Qué es Gson y por qué lo usas?**
**Respuesta:** Gson es una librería de Google que convierte objetos Java a JSON y viceversa. Es necesaria porque SharedPreferences solo guarda tipos primitivos (String, int, boolean). Con Gson, convierto `List<Entrenamiento>` → JSON String → guardo. Al cargar: cargo String → Gson lo convierte → `List<Entrenamiento>`.

### 8. **¿Cómo se comunica el DialogFragment con MainActivity?**
**Respuesta:** Uso el patrón Callback:
1. Defino una interfaz `OnEntrenamientoAddedListener`
2. MainActivity implementa la interfaz
3. MainActivity se registra como listener del DialogFragment
4. Cuando el usuario guarda, el DialogFragment llama al método de la interfaz
5. MainActivity recibe el nuevo entrenamiento y actualiza la lista

---

## 🏆 Puntos Destacables del Proyecto

### Aspectos Técnicos Avanzados
✅ Arquitectura MVC bien definida
✅ Persistencia de datos completa
✅ Diseño responsive profesional
✅ Patrones de diseño modernos
✅ Código limpio y documentado
✅ Validaciones robustas
✅ Material Design 3

### Buenas Prácticas
✅ Uso de OnBackPressedCallback (API moderna, no deprecada)
✅ ViewHolder para optimización
✅ Layouts alternativos para orientación
✅ Separación de responsabilidades
✅ Nombres de variables descriptivos
✅ Comentarios explicativos
✅ Organización en carpetas

### Experiencia de Usuario
✅ Interfaz intuitiva y limpia
✅ Feedback visual en todas las acciones
✅ Validaciones claras con mensajes útiles
✅ Transiciones suaves entre vistas
✅ Responsive en todas las orientaciones
✅ Scroll fluido con indicador visible

---

## 📖 Referencias y Documentación

### Documentación Oficial Android
- [Activities](https://developer.android.com/guide/components/activities/intro-activities)
- [Fragments](https://developer.android.com/guide/fragments)
- [ListView](https://developer.android.com/reference/android/widget/ListView)
- [SharedPreferences](https://developer.android.com/training/data-storage/shared-preferences)
- [Parcelable](https://developer.android.com/reference/android/os/Parcelable)
- [Material Design](https://material.io/develop/android)

### Librerías Externas
- [Gson](https://github.com/google/gson)

---

## 👨‍💻 Autor

**Denis López Sacher**  
Examen 1er Trimestre - Programación Multimedia y Dispositivos Móviles

---

## 📝 Licencia

Este proyecto es de uso educativo para el curso de Desarrollo de Aplicaciones Multiplataforma.

---

## 🎉 Conclusión

Este proyecto implementa una aplicación completa de gestión de entrenamientos con:
- ✅ Persistencia permanente de datos
- ✅ Diseño responsive y adaptativo
- ✅ Arquitectura limpia y mantenible
- ✅ Buenas prácticas de Android
- ✅ Material Design moderno

La aplicación demuestra conocimientos sólidos de desarrollo Android nativo con Java, incluyendo conceptos fundamentales (Activities, Fragments, Layouts) y avanzados (Persistencia, Parcelable, Patrones de diseño).

---

**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Plataforma:** Android  
**Lenguaje:** Java  
**Min SDK:** API 36  
**Target SDK:** API 36

