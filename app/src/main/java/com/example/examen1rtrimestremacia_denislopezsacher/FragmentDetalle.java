package com.example.examen1rtrimestremacia_denislopezsacher;

import android.os.Bundle;              // Para pasar datos entre componentes
import android.view.LayoutInflater;    // Para inflar layouts XML
import android.view.View;              // Clase base de vistas
import android.view.ViewGroup;         // Contenedor de vistas
import android.widget.ImageView;       // Para mostrar el icono
import android.widget.TextView;        // Para mostrar texto

import androidx.annotation.NonNull;    // Anotación para parámetros no nulos
import androidx.annotation.Nullable;   // Anotación para parámetros que pueden ser null
import androidx.fragment.app.Fragment; // Clase base de fragments

/**
 * FragmentDetalle - Fragment que muestra los detalles de un entrenamiento
 *
 * Este fragment es la VISTA de detalle en el patrón Master-Detail.
 *
 * Responsabilidades:
 * - Mostrar información detallada de un entrenamiento
 * - Recibir datos mediante Bundle (patrón recomendado por Google)
 * - Adaptarse a portrait (pantalla completa) y landscape (lado derecho)
 *
 * Ciclo de vida del Fragment:
 * 1. newInstance() → Crea el fragment y le pasa datos en Bundle
 * 2. onCreate() → Lee los datos del Bundle
 * 3. onCreateView() → Infla el layout y muestra los datos
 *
 * ¿Por qué usar newInstance() en lugar de constructor?
 * - Google recomienda NO usar constructores con parámetros en Fragments
 * - Si Android recrea el Fragment, solo llama al constructor vacío
 * - Los datos en Bundle sí se restauran automáticamente
 * - newInstance() es el patrón Factory recomendado
 *
 * ¿Por qué Bundle y no parámetros directos?
 * - Bundle sobrevive a la recreación del Fragment
 * - Android puede destruir/recrear Fragments (rotación, memoria baja)
 * - Los datos en Bundle se guardan automáticamente
 */
public class FragmentDetalle extends Fragment {

    // ============= CONSTANTES PARA LAS CLAVES DEL BUNDLE =============

    /**
     * Clave para guardar/recuperar el nombre del Bundle
     * Se usa como: bundle.putString(ARG_NOMBRE, "Pilates")
     */
    private static final String ARG_NOMBRE = "nombre";

    /**
     * Clave para guardar/recuperar la descripción del Bundle
     */
    private static final String ARG_DESCRIPCION = "descripcion";

    /**
     * Clave para guardar/recuperar el ID del icono del Bundle
     */
    private static final String ARG_ICONO = "icono";

    // ============= ATRIBUTOS DEL FRAGMENT =============

    /** Nombre del entrenamiento a mostrar */
    private String nombre;

    /** Descripción detallada del entrenamiento */
    private String descripcion;

    /** ID del recurso del icono (ej: R.drawable.ic_pilates) */
    private int iconoResId;

    // ============= MÉTODO FACTORY (PATRÓN RECOMENDADO) =============

    /**
     * newInstance - Método factory para crear una instancia del Fragment
     *
     * Este es el PATRÓN FACTORY recomendado por Google para crear Fragments.
     *
     * ¿Por qué usar este patrón?
     * - Evita constructores con parámetros (problemáticos en Fragments)
     * - Encapsula la lógica de creación del Bundle
     * - Los datos sobreviven a la recreación del Fragment
     * - Más claro y mantenible que pasar datos manualmente
     *
     * Flujo:
     * 1. Crear nuevo Fragment vacío (constructor sin parámetros)
     * 2. Crear Bundle para guardar los argumentos
     * 3. Poner los datos en el Bundle con claves específicas
     * 4. Asignar el Bundle al Fragment
     * 5. Retornar el Fragment configurado
     *
     * Ejemplo de uso desde MainActivity:
     * FragmentDetalle fragment = FragmentDetalle.newInstance("Pilates", "Descripción...", R.drawable.ic_pilates);
     *
     * @param nombre Nombre del entrenamiento
     * @param descripcion Descripción del entrenamiento
     * @param iconoResId ID del recurso del icono
     * @return Nueva instancia de FragmentDetalle con los datos configurados
     */
    public static FragmentDetalle newInstance(String nombre, String descripcion, int iconoResId) {
        // PASO 1: Crear una nueva instancia del Fragment
        // Usa el constructor vacío (único que Android garantiza que existe)
        FragmentDetalle fragment = new FragmentDetalle();

        // PASO 2: Crear un Bundle para almacenar los argumentos
        // Bundle es como un HashMap que puede guardar tipos primitivos y Parcelables
        Bundle args = new Bundle();

        // PASO 3: Guardar cada dato en el Bundle con su clave correspondiente
        args.putString(ARG_NOMBRE, nombre);           // Guardar nombre
        args.putString(ARG_DESCRIPCION, descripcion); // Guardar descripción
        args.putInt(ARG_ICONO, iconoResId);           // Guardar ID del icono

        // PASO 4: Asignar el Bundle al Fragment
        // setArguments() guarda el Bundle en el Fragment
        // Android automáticamente preserva este Bundle si recrea el Fragment
        fragment.setArguments(args);

        // PASO 5: Retornar el Fragment configurado
        return fragment;
    }

    // ============= MÉTODOS DEL CICLO DE VIDA =============

    /**
     * onCreate - Primer método del ciclo de vida del Fragment
     *
     * Este método se llama cuando:
     * - El Fragment se crea por primera vez
     * - El Fragment se recrea después de ser destruido (rotación, memoria)
     *
     * Aquí leemos los datos del Bundle que fueron pasados en newInstance().
     *
     * Ciclo de vida de Fragment:
     * onCreate() → onCreateView() → onViewCreated() → onStart() → onResume()
     *
     * @param savedInstanceState Bundle con el estado guardado (puede ser null)
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Llamar al método padre obligatoriamente

        // Verificar si el Fragment tiene argumentos (Bundle) asignados
        if (getArguments() != null) {
            // LEER los datos del Bundle usando las mismas claves que en newInstance()
            // El orden de lectura no importa (a diferencia de Parcelable)
            nombre = getArguments().getString(ARG_NOMBRE);           // Leer nombre
            descripcion = getArguments().getString(ARG_DESCRIPCION); // Leer descripción
            iconoResId = getArguments().getInt(ARG_ICONO);           // Leer ID del icono
        }
        // Nota: Si getArguments() es null, las variables quedan con sus valores por defecto
    }

    /**
     * onCreateView - Crea y retorna la vista del Fragment
     *
     * Este método es responsable de:
     * 1. Inflar el layout XML (convertir XML → objetos View)
     * 2. Buscar las vistas por sus IDs
     * 3. Asignar los datos a las vistas
     * 4. Retornar la vista configurada
     *
     * Este método se llama cada vez que el Fragment necesita mostrar su UI.
     *
     * IMPORTANTE:
     * - NO guardar referencias a las vistas como atributos del Fragment
     * - Las vistas pueden ser destruidas y recreadas sin destruir el Fragment
     * - Usar View Binding o findViewById en cada llamada a onCreateView
     *
     * @param inflater Objeto para inflar layouts XML
     * @param container Contenedor padre donde se añadirá la vista
     * @param savedInstanceState Estado guardado (puede ser null)
     * @return La vista raíz del Fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // PASO 1: Inflar el layout XML del Fragment
        // inflate() convierte el XML en objetos View de Java
        // Parámetros:
        // - R.layout.fragment_detalle: Layout a inflar
        // - container: Contenedor padre (FrameLayout en MainActivity)
        // - false: NO adjuntar aún al padre (Android lo hace automáticamente)
        View view = inflater.inflate(R.layout.fragment_detalle, container, false);

        // PASO 2: Buscar las vistas dentro del layout inflado
        // findViewById busca en la jerarquía de vistas a partir de 'view'
        ImageView ivIcono = view.findViewById(R.id.iv_detalle_icono);           // Icono del entrenamiento
        TextView tvNombre = view.findViewById(R.id.tv_detalle_nombre);          // Nombre del entrenamiento
        TextView tvDescripcion = view.findViewById(R.id.tv_detalle_descripcion); // Descripción del entrenamiento

        // PASO 3: Asignar los datos leídos del Bundle a las vistas
        // Estos datos fueron leídos en onCreate() desde el Bundle
        ivIcono.setImageResource(iconoResId);  // Mostrar el icono
        tvNombre.setText(nombre);              // Mostrar el nombre
        tvDescripcion.setText(descripcion);    // Mostrar la descripción

        // PASO 4: Retornar la vista configurada
        // Android la añadirá automáticamente al contenedor (fragment_container)
        return view;
    }
}

