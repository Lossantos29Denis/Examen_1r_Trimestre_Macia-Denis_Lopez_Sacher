package com.example.examen1rtrimestremacia_denislopezsacher;

import android.content.Context;                // Para acceder al contexto de la app
import android.content.SharedPreferences;      // Sistema de almacenamiento clave-valor

import com.google.gson.Gson;                   // Librería para convertir objetos ↔ JSON
import com.google.gson.reflect.TypeToken;      // Para obtener el tipo genérico List<Entrenamiento>

import java.lang.reflect.Type;    // Tipo de Java para reflexión
import java.util.ArrayList;       // Lista dinámica
import java.util.List;           // Interfaz de lista

/**
 * EntrenamientoStorage - Gestor de almacenamiento persistente de entrenamientos
 *
 * Esta clase encapsula toda la lógica de persistencia de datos usando:
 * - SharedPreferences: Sistema de almacenamiento clave-valor de Android
 * - Gson: Librería de Google para convertir objetos Java ↔ JSON
 *
 * ¿Por qué SharedPreferences?
 * - Es el sistema más simple para guardar datos en Android
 * - Los datos persisten al cerrar/abrir la app
 * - Se guarda en: /data/data/[paquete]/shared_prefs/[nombre].xml
 * - Perfecto para datos pequeños como nuestra lista de entrenamientos
 *
 * ¿Por qué Gson?
 * - SharedPreferences solo guarda tipos primitivos (String, int, boolean)
 * - Gson convierte List<Entrenamiento> → String JSON → SharedPreferences
 * - Al cargar: SharedPreferences → String JSON → Gson → List<Entrenamiento>
 *
 * Flujo de datos:
 * GUARDAR: List<Entrenamiento> → Gson.toJson() → String JSON → SharedPreferences
 * CARGAR: SharedPreferences → String JSON → Gson.fromJson() → List<Entrenamiento>
 */
public class EntrenamientoStorage {

    // ============= CONSTANTES =============

    /**
     * Nombre del archivo de preferencias
     * Se creará: /data/data/.../shared_prefs/EntrenamientosPrefs.xml
     */
    private static final String PREFS_NAME = "EntrenamientosPrefs";

    /**
     * Clave para guardar/recuperar la lista de entrenamientos
     * En el XML se verá como: <string name="entrenamientos_list">...</string>
     */
    private static final String KEY_ENTRENAMIENTOS = "entrenamientos_list";

    // ============= ATRIBUTOS =============

    /**
     * Instancia de SharedPreferences para acceder al almacenamiento
     * SharedPreferences funciona como un HashMap persistente
     */
    private final SharedPreferences preferences;

    /**
     * Instancia de Gson para serialización/deserialización JSON
     * Gson convierte automáticamente objetos Java ↔ JSON
     */
    private final Gson gson;

    // ============= CONSTRUCTOR =============

    /**
     * Constructor - Inicializa el sistema de almacenamiento
     *
     * @param context Contexto de la aplicación (necesario para SharedPreferences)
     */
    public EntrenamientoStorage(Context context) {
        // Obtener la instancia de SharedPreferences
        // MODE_PRIVATE = solo esta app puede acceder a estos datos
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Crear instancia de Gson (ligera, no consume muchos recursos)
        gson = new Gson();
    }

    // ============= MÉTODOS PÚBLICOS =============

    /**
     * guardarEntrenamientos - Guarda la lista de entrenamientos en SharedPreferences
     *
     * Proceso:
     * 1. Convierte List<Entrenamiento> a String JSON usando Gson
     * 2. Guarda el String JSON en SharedPreferences
     * 3. apply() guarda de forma asíncrona (no bloquea el hilo principal)
     *
     * Ejemplo de JSON generado:
     * [
     *   {"nombre":"Pilates","descripcion":"...","iconoResId":2131165279},
     *   {"nombre":"Running","descripcion":"...","iconoResId":2131165280}
     * ]
     *
     * @param entrenamientos Lista de entrenamientos a guardar
     */
    public void guardarEntrenamientos(List<Entrenamiento> entrenamientos) {
        // PASO 1: Convertir la lista de objetos Java a String JSON
        // Gson.toJson() inspecciona cada objeto y lo convierte automáticamente
        String json = gson.toJson(entrenamientos);

        // PASO 2: Guardar el String JSON en SharedPreferences
        // preferences.edit() → Inicia modo edición
        // putString(clave, valor) → Guarda el valor con la clave
        // apply() → Guarda de forma asíncrona (recomendado)
        //   Alternativa: commit() → Guarda de forma síncrona (bloquea el hilo)
        preferences.edit()
                .putString(KEY_ENTRENAMIENTOS, json)
                .apply();
    }

    /**
     * cargarEntrenamientos - Carga la lista de entrenamientos desde SharedPreferences
     *
     * Proceso:
     * 1. Lee el String JSON desde SharedPreferences
     * 2. Si existe, convierte el JSON a List<Entrenamiento> usando Gson
     * 3. Si no existe, retorna null
     *
     * TypeToken:
     * En Java, debido al "type erasure", no podemos hacer: new ArrayList<Entrenamiento>().getClass()
     * TypeToken es una técnica que usa clases anónimas para capturar el tipo genérico completo
     *
     * @return Lista de entrenamientos si existen, null si no hay datos guardados
     */
    public List<Entrenamiento> cargarEntrenamientos() {
        // PASO 1: Leer el String JSON desde SharedPreferences
        // getString(clave, valorPorDefecto) → Si la clave no existe, retorna el valor por defecto
        String json = preferences.getString(KEY_ENTRENAMIENTOS, null);

        // PASO 2: Verificar si hay datos guardados
        if (json != null) {
            // PASO 3: Obtener el tipo genérico completo: ArrayList<Entrenamiento>
            // Esta sintaxis extraña {} crea una clase anónima que captura el tipo genérico
            // getType() obtiene el tipo completo con generics
            Type type = new TypeToken<ArrayList<Entrenamiento>>(){}.getType();

            // PASO 4: Convertir el String JSON a List<Entrenamiento>
            // Gson.fromJson() parsea el JSON y crea los objetos automáticamente
            return gson.fromJson(json, type);
        }

        // PASO 5: Si no hay datos, retornar null
        return null;
    }

    /**
     * limpiarEntrenamientos - Elimina todos los entrenamientos guardados
     *
     * Este método borra la clave del almacenamiento, como si nunca
     * se hubiera guardado nada.
     *
     * Útil para:
     * - Resetear la app a estado inicial
     * - Debugging
     * - Funcionalidad de "borrar todos los datos"
     */
    public void limpiarEntrenamientos() {
        // preferences.edit() → Modo edición
        // remove(clave) → Elimina la clave y su valor
        // apply() → Aplica los cambios de forma asíncrona
        preferences.edit()
                .remove(KEY_ENTRENAMIENTOS)
                .apply();
    }

    /**
     * hayEntrenamientosGuardados - Verifica si existen entrenamientos guardados
     *
     * Este método es más eficiente que cargar todos los datos y verificar
     * si la lista está vacía.
     *
     * @return true si existe la clave en SharedPreferences, false si no
     */
    public boolean hayEntrenamientosGuardados() {
        // contains(clave) → Verifica si existe la clave (sin cargar el valor)
        // Retorna true si la clave existe, false si no
        return preferences.contains(KEY_ENTRENAMIENTOS);
    }
}

