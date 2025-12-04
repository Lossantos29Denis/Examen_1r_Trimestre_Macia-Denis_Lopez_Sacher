package com.example.examen1rtrimestremacia_denislopezsacher;

import android.os.Parcel;       // Clase para serializar/deserializar objetos
import android.os.Parcelable;   // Interfaz para objetos que pueden ser serializados

import androidx.annotation.NonNull;  // Anotación para parámetros no nulos

/**
 * Entrenamiento - Clase modelo que representa un entrenamiento deportivo
 *
 * Esta clase es el MODELO en el patrón MVC (Model-View-Controller).
 *
 * Implementa Parcelable para permitir:
 * - Pasar objetos entre Activities/Fragments mediante Intents o Bundles
 * - Guardar objetos en savedInstanceState durante rotaciones
 * - Serialización eficiente (más rápida que Serializable)
 *
 * ¿Por qué Parcelable y no Serializable?
 * - Parcelable es ~10x más rápido en Android
 * - Usa menos memoria
 * - Está optimizado para IPC (Inter-Process Communication)
 * - Es el estándar recomendado por Google para Android
 *
 * Atributos:
 * - nombre: Nombre del entrenamiento (ej: "Pilates")
 * - descripcion: Descripción detallada del entrenamiento
 * - iconoResId: ID del recurso drawable del icono (ej: R.drawable.ic_pilates)
 */
public class Entrenamiento implements Parcelable {

    // ============= ATRIBUTOS =============

    /** Nombre del entrenamiento (ej: "Pilates", "Running") */
    private String nombre;

    /** Descripción detallada del entrenamiento */
    private String descripcion;

    /** ID del recurso drawable del icono (ej: R.drawable.ic_pilates = 2131165279) */
    private int iconoResId;

    // ============= CONSTRUCTORES =============

    /**
     * Constructor principal - Crea un nuevo entrenamiento con todos sus datos
     *
     * Este constructor se usa cuando creamos entrenamientos manualmente
     * (tanto los predefinidos como los añadidos por el usuario)
     *
     * @param nombre Nombre del entrenamiento
     * @param descripcion Descripción del entrenamiento
     * @param iconoResId ID del recurso del icono (R.drawable.ic_xxx)
     */
    public Entrenamiento(String nombre, String descripcion, int iconoResId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.iconoResId = iconoResId;
    }

    /**
     * Constructor Parcelable - Lee un entrenamiento desde un Parcel
     *
     * Este constructor se usa automáticamente cuando Android necesita
     * deserializar (reconstruir) un objeto Entrenamiento desde un Parcel.
     *
     * IMPORTANTE: El orden de lectura DEBE ser el mismo que el orden de escritura
     * en writeToParcel(). Si cambias el orden aquí, debes cambiarlo allí también.
     *
     * @param in Parcel del que leer los datos
     */
    protected Entrenamiento(Parcel in) {
        // Leer los datos en el MISMO orden que fueron escritos
        nombre = in.readString();        // 1. Leer nombre
        descripcion = in.readString();   // 2. Leer descripción
        iconoResId = in.readInt();       // 3. Leer iconoResId
    }

    // ============= PARCELABLE CREATOR =============

    /**
     * CREATOR - Objeto estático obligatorio para implementar Parcelable
     *
     * Android usa este CREATOR para crear instancias de Entrenamiento
     * cuando deserializa objetos desde un Parcel.
     *
     * Debe ser:
     * - public static final
     * - llamarse exactamente "CREATOR"
     * - implementar Parcelable.Creator<T>
     */
    public static final Creator<Entrenamiento> CREATOR = new Creator<Entrenamiento>() {
        /**
         * createFromParcel - Crea un objeto Entrenamiento desde un Parcel
         *
         * Android llama a este método cuando necesita deserializar un objeto.
         *
         * @param in Parcel que contiene los datos serializados
         * @return Nueva instancia de Entrenamiento con los datos del Parcel
         */
        @Override
        public Entrenamiento createFromParcel(Parcel in) {
            // Llamar al constructor que lee desde Parcel
            return new Entrenamiento(in);
        }

        /**
         * newArray - Crea un array de Entrenamientos
         *
         * Android usa este método cuando necesita deserializar un array de objetos.
         *
         * @param size Tamaño del array a crear
         * @return Nuevo array de Entrenamiento del tamaño especificado
         */
        @Override
        public Entrenamiento[] newArray(int size) {
            return new Entrenamiento[size];
        }
    };

    // ============= GETTERS Y SETTERS =============

    /**
     * getNombre - Obtiene el nombre del entrenamiento
     * @return Nombre del entrenamiento
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * setNombre - Establece el nombre del entrenamiento
     * @param nombre Nuevo nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * getDescripcion - Obtiene la descripción del entrenamiento
     * @return Descripción del entrenamiento
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * setDescripcion - Establece la descripción del entrenamiento
     * @param descripcion Nueva descripción
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * getIconoResId - Obtiene el ID del recurso del icono
     * @return ID del recurso (ej: 2131165279 = R.drawable.ic_pilates)
     */
    public int getIconoResId() {
        return iconoResId;
    }

    /**
     * setIconoResId - Establece el ID del recurso del icono
     * @param iconoResId Nuevo ID del recurso
     */
    public void setIconoResId(int iconoResId) {
        this.iconoResId = iconoResId;
    }

    // ============= MÉTODOS PARCELABLE =============

    /**
     * describeContents - Describe el contenido del Parcel
     *
     * Este método es obligatorio de la interfaz Parcelable.
     * Se usa para indicar si el Parcel contiene File Descriptors especiales.
     *
     * @return 0 en la mayoría de casos (no hay descriptores especiales)
     */
    @Override
    public int describeContents() {
        return 0;  // 0 = no hay file descriptors especiales
    }

    /**
     * writeToParcel - Escribe los datos del objeto en un Parcel
     *
     * Este método serializa (convierte a bytes) el objeto para que pueda
     * ser enviado entre componentes o guardado temporalmente.
     *
     * IMPORTANTE: El orden de escritura DEBE coincidir con el orden de lectura
     * en el constructor Entrenamiento(Parcel in).
     *
     * Orden de serialización:
     * 1. nombre (String)
     * 2. descripcion (String)
     * 3. iconoResId (int)
     *
     * @param dest Parcel destino donde escribir los datos
     * @param flags Flags adicionales (usualmente 0)
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // Escribir los datos en el MISMO orden que se leen en el constructor
        dest.writeString(nombre);        // 1. Escribir nombre
        dest.writeString(descripcion);   // 2. Escribir descripción
        dest.writeInt(iconoResId);       // 3. Escribir iconoResId
    }
}

