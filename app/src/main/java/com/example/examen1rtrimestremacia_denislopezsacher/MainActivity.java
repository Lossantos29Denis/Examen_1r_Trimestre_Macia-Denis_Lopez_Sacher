package com.example.examen1rtrimestremacia_denislopezsacher;

// Imports necesarios para la funcionalidad de la Activity
import android.content.res.Configuration;  // Para detectar orientación del dispositivo
import android.os.Bundle;                  // Para guardar/restaurar estado
import android.view.Menu;                  // Para inflar menú de opciones
import android.view.MenuItem;              // Para manejar clicks en menú
import android.view.View;                  // Para manejar visibilidad de vistas
import android.widget.FrameLayout;         // Contenedor para fragments
import android.widget.ListView;            // Para mostrar lista de entrenamientos

import androidx.activity.OnBackPressedCallback;  // API moderna para botón atrás
import androidx.annotation.NonNull;              // Anotación para valores no nulos
import androidx.appcompat.app.AppCompatActivity; // Activity base de AppCompat
import androidx.fragment.app.FragmentTransaction; // Para transacciones de fragments

// Imports de las clases de entrenamientos específicas
import com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos.Fuerza;
import com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos.Pilates;
import com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos.Running;
import com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos.Voleibol;
import com.google.android.material.appbar.MaterialToolbar;  // Toolbar Material Design

import java.util.ArrayList;  // Para crear listas dinámicas
import java.util.List;       // Interfaz para listas

/**
 * MainActivity - Activity principal de la aplicación de entrenamientos
 *
 * Responsabilidades:
 * - Gestionar el ciclo de vida de la aplicación
 * - Mostrar lista de entrenamientos en un ListView
 * - Manejar la vista dual en landscape (ListView + Fragment)
 * - Gestionar la persistencia de datos con SharedPreferences
 * - Comunicarse con DialogFragment para añadir entrenamientos
 *
 * Implementa OnEntrenamientoAddedListener para recibir callbacks del DialogFragment
 */
public class MainActivity extends AppCompatActivity implements AddEntrenamientoDialogFragment.OnEntrenamientoAddedListener {

    // ============= ATRIBUTOS DE LA CLASE =============

    /** Toolbar de Material Design en la parte superior */
    private MaterialToolbar toolbar;

    /** ListView que muestra la lista de entrenamientos */
    private ListView lvEntrenamientos;

    /** Contenedor donde se muestran los fragments de detalle */
    private FrameLayout fragmentContainer;

    /** Adapter personalizado que conecta los datos con el ListView */
    private EntrenamientoAdapter adapter;

    /** Lista que contiene todos los objetos Entrenamiento */
    private List<Entrenamiento> entrenamientos;

    /** Flag que indica si estamos en modo dual (landscape) o simple (portrait) */
    private boolean isDualPane;

    /** Gestor de almacenamiento persistente usando SharedPreferences */
    private EntrenamientoStorage storage;

    /**
     * onCreate - Método del ciclo de vida llamado cuando se crea la Activity
     *
     * Este método se ejecuta cuando:
     * - La app se abre por primera vez
     * - Se rota el dispositivo (la Activity se destruye y recrea)
     * - Se vuelve a la app después de estar en background
     *
     * @param savedInstanceState Bundle que contiene el estado guardado (null si es primera vez)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Llamar al método padre obligatoriamente
        setContentView(R.layout.activity_main);  // Inflar el layout correspondiente (portrait o landscape)

        // ========== PASO 1: CONFIGURAR TOOLBAR ==========
        toolbar = findViewById(R.id.toolbar);  // Buscar el toolbar en el layout
        setSupportActionBar(toolbar);  // Establecerlo como ActionBar de la Activity

        // Configurar listener para clicks en items del menú del toolbar
        // Lambda que se ejecuta cuando se hace click en un item del menú
        toolbar.setOnMenuItemClickListener(item -> {
            // Verificar si el item clickeado es el botón de añadir
            if (item.getItemId() == R.id.action_add) {
                mostrarDialogAgregarEntrenamiento();  // Mostrar el diálogo
                return true;  // Indicar que el evento fue consumido
            }
            return false;  // El evento no fue manejado
        });

        // ========== PASO 2: INICIALIZAR VISTAS ==========
        // Buscar las vistas en el layout por su ID
        lvEntrenamientos = findViewById(R.id.lv_entrenamientos);     // ListView de entrenamientos
        fragmentContainer = findViewById(R.id.fragment_container);   // Contenedor de fragments

        // ========== PASO 3: CONFIGURAR SISTEMA DE ALMACENAMIENTO ==========
        // Crear instancia del gestor de almacenamiento pasando el contexto
        storage = new EntrenamientoStorage(this);

        // ========== PASO 4: DETECTAR ORIENTACIÓN ==========
        // Obtener la orientación actual del dispositivo desde la configuración
        // Si es LANDSCAPE → isDualPane = true (vista dual)
        // Si es PORTRAIT → isDualPane = false (vista simple)
        isDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        // ========== PASO 5: CARGAR DATOS (PRIORIDAD: Bundle > SharedPreferences > Default) ==========

        // PRIORIDAD 1: Restaurar desde savedInstanceState (rotación de pantalla)
        // Cuando se rota, Android guarda el estado en un Bundle
        if (savedInstanceState != null && savedInstanceState.containsKey("entrenamientos")) {
            // getParcelableArrayList deserializa los objetos Parcelable guardados
            entrenamientos = savedInstanceState.getParcelableArrayList("entrenamientos");
        }
        // PRIORIDAD 2: Cargar desde SharedPreferences (app cerrada y vuelta a abrir)
        else if (storage.hayEntrenamientosGuardados()) {
            // Cargar la lista desde el almacenamiento persistente (JSON → List)
            entrenamientos = storage.cargarEntrenamientos();
        }
        // PRIORIDAD 3: Primera vez - Crear entrenamientos por defecto
        else {
            initEntrenamientos();  // Crear los 4 entrenamientos iniciales
            storage.guardarEntrenamientos(entrenamientos);  // Guardarlos inmediatamente
        }

        // ========== PASO 6: CONFIGURAR ADAPTER Y LISTVIEW ==========
        // Crear el adapter que conecta los datos (entrenamientos) con la vista (ListView)
        adapter = new EntrenamientoAdapter(this, entrenamientos);
        // Asignar el adapter al ListView para que se muestren los datos
        lvEntrenamientos.setAdapter(adapter);

        // ========== PASO 7: CONFIGURAR LISTENER DE CLICKS EN EL LISTVIEW ==========
        // Lambda que se ejecuta cuando el usuario hace click en un item del ListView
        lvEntrenamientos.setOnItemClickListener((parent, view, position, id) -> {
            // Obtener el entrenamiento correspondiente a la posición clickeada
            Entrenamiento entrenamiento = entrenamientos.get(position);
            // Mostrar el fragment de detalle con la información del entrenamiento
            mostrarDetalle(entrenamiento);
        });

        // ========== PASO 8: CONFIGURACIÓN ESPECIAL PARA LANDSCAPE ==========
        // En landscape (vista dual), mostrar el primer entrenamiento automáticamente
        // Esto llena el espacio del fragment que está visible permanentemente
        if (isDualPane && !entrenamientos.isEmpty()) {
            mostrarDetalle(entrenamientos.get(0));  // Mostrar el primer entrenamiento
        }

        // ========== PASO 9: CONFIGURAR NAVEGACIÓN DEL BOTÓN ATRÁS ==========
        configurarBackNavigation();  // Método que gestiona el comportamiento del botón atrás
    }

    /**
     * configurarBackNavigation - Configura el comportamiento del botón atrás
     *
     * Usa OnBackPressedCallback (API moderna, no deprecada) para gestionar
     * el botón de navegación atrás del sistema.
     *
     * Comportamiento:
     * - Portrait con fragment visible: Vuelve al ListView
     * - Otros casos: Comportamiento por defecto (cierra app)
     */
    private void configurarBackNavigation() {
        // Obtener el dispatcher de eventos del botón atrás
        // addCallback registra un nuevo manejador del botón atrás
        // Parámetros: (LifecycleOwner, Callback habilitado por defecto)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            /**
             * handleOnBackPressed - Se ejecuta cuando el usuario presiona el botón atrás
             */
            @Override
            public void handleOnBackPressed() {
                // Verificar si estamos en portrait Y el fragment está visible
                if (!isDualPane && fragmentContainer.getVisibility() == View.VISIBLE) {
                    // CASO 1: Portrait con fragment visible → Volver al ListView

                    // Remover el fragment del backstack (destruirlo)
                    getSupportFragmentManager().popBackStack();

                    // Hacer visible el ListView nuevamente
                    lvEntrenamientos.setVisibility(View.VISIBLE);

                    // Ocultar el contenedor del fragment
                    fragmentContainer.setVisibility(View.GONE);
                } else {
                    // CASO 2: Landscape O ListView visible → Cerrar app

                    // Deshabilitar este callback para permitir el comportamiento por defecto
                    setEnabled(false);

                    // Ejecutar el comportamiento por defecto (cerrar app)
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * initEntrenamientos - Inicializa la lista con los 4 entrenamientos por defecto
     *
     * Este método solo se llama la primera vez que se abre la app,
     * cuando no hay datos guardados en SharedPreferences.
     *
     * Crea instancias de las clases específicas de entrenamientos:
     * - Pilates, Voleibol, Fuerza, Running
     */
    private void initEntrenamientos() {
        // Crear una nueva lista vacía
        entrenamientos = new ArrayList<>();

        // Añadir las 4 instancias de entrenamientos predefinidos
        // Cada clase (Pilates, Voleibol, etc.) ya tiene su nombre, descripción e icono
        entrenamientos.add(new Pilates());    // Añade entrenamiento de Pilates
        entrenamientos.add(new Voleibol());   // Añade entrenamiento de Voleibol
        entrenamientos.add(new Fuerza());     // Añade entrenamiento de Fuerza
        entrenamientos.add(new Running());    // Añade entrenamiento de Running
    }

    /**
     * mostrarDetalle - Muestra el fragment de detalle con la información del entrenamiento
     *
     * Este método maneja la transición entre el ListView y el fragment de detalle.
     * Comportamiento diferente según la orientación:
     * - Portrait: Oculta ListView, muestra fragment pantalla completa
     * - Landscape: Ambos visibles (vista dual)
     *
     * @param entrenamiento El entrenamiento cuyo detalle se va a mostrar
     */
    private void mostrarDetalle(Entrenamiento entrenamiento) {
        // Crear una nueva instancia del fragment usando el patrón Factory
        // newInstance() crea el fragment y le pasa los datos mediante Bundle
        FragmentDetalle fragment = FragmentDetalle.newInstance(
                entrenamiento.getNombre(),        // Nombre del entrenamiento
                entrenamiento.getDescripcion(),   // Descripción del entrenamiento
                entrenamiento.getIconoResId()     // ID del recurso del icono
        );

        // Iniciar una transacción de fragments (permite realizar múltiples operaciones)
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Reemplazar el contenido del contenedor con el nuevo fragment
        // Si ya había un fragment, se destruye y se reemplaza con este
        transaction.replace(R.id.fragment_container, fragment);

        // Diferenciar comportamiento según orientación
        if (!isDualPane) {
            // ===== MODO PORTRAIT =====
            // Ocultar el ListView (solo se ve el fragment)
            lvEntrenamientos.setVisibility(View.GONE);

            // Hacer visible el contenedor del fragment
            fragmentContainer.setVisibility(View.VISIBLE);

            // Añadir esta transacción al backstack
            // Esto permite volver atrás con el botón de navegación
            transaction.addToBackStack(null);
        } else {
            // ===== MODO LANDSCAPE =====
            // Solo hacer visible el contenedor (el ListView ya está visible)
            // No se añade al backstack porque la vista dual es permanente
            fragmentContainer.setVisibility(View.VISIBLE);
        }

        // Ejecutar la transacción (aplicar todos los cambios)
        transaction.commit();
    }

    /**
     * onCreateOptionsMenu - Infla el menú de opciones en el Toolbar
     *
     * Este método del ciclo de vida se llama una vez cuando se crea el menú.
     *
     * @param menu El menú donde se inflarán los items
     * @return true para mostrar el menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el archivo XML del menú en el objeto Menu
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;  // Retornar true para que se muestre el menú
    }

    /**
     * onOptionsItemSelected - Maneja los clicks en los items del menú
     *
     * Este método alternativo se mantiene para compatibilidad,
     * aunque el click también se maneja en el listener del toolbar.
     *
     * @param item El item del menú que fue seleccionado
     * @return true si el evento fue manejado
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Verificar si el item seleccionado es el botón de añadir
        if (item.getItemId() == R.id.action_add) {
            mostrarDialogAgregarEntrenamiento();  // Mostrar el diálogo
            return true;  // Evento consumido
        }
        // Si no fue el botón de añadir, delegar al método padre
        return super.onOptionsItemSelected(item);
    }

    /**
     * mostrarDialogAgregarEntrenamiento - Muestra el DialogFragment para añadir un entrenamiento
     *
     * Crea una instancia del diálogo, registra esta Activity como listener
     * y muestra el diálogo en pantalla.
     */
    private void mostrarDialogAgregarEntrenamiento() {
        // Crear una nueva instancia del DialogFragment
        AddEntrenamientoDialogFragment dialog = new AddEntrenamientoDialogFragment();

        // Registrar esta Activity como listener para recibir el callback
        // cuando se añada un entrenamiento
        dialog.setOnEntrenamientoAddedListener(this);

        // Mostrar el diálogo
        // Parámetros: (FragmentManager, tag identificador)
        dialog.show(getSupportFragmentManager(), "AddEntrenamientoDialog");
    }

    /**
     * onEntrenamientoAdded - Callback que se ejecuta cuando se añade un entrenamiento
     *
     * Este método implementa la interfaz OnEntrenamientoAddedListener.
     * Es llamado por el DialogFragment cuando el usuario guarda un nuevo entrenamiento.
     *
     * Flujo:
     * 1. Añadir a la lista
     * 2. Guardar en almacenamiento persistente
     * 3. Notificar al adapter para actualizar la vista
     *
     * @param entrenamiento El nuevo entrenamiento añadido por el usuario
     */
    @Override
    public void onEntrenamientoAdded(Entrenamiento entrenamiento) {
        // PASO 1: Agregar el nuevo entrenamiento a la lista en memoria
        entrenamientos.add(entrenamiento);

        // PASO 2: Guardar la lista actualizada en SharedPreferences
        // Esto asegura que el nuevo entrenamiento persista al cerrar la app
        storage.guardarEntrenamientos(entrenamientos);

        // PASO 3: Notificar al adapter que los datos han cambiado
        // Esto hace que el ListView se refresque y muestre el nuevo item
        adapter.notifyDataSetChanged();
    }

    /**
     * getEntrenamientosExistentes - Retorna la lista actual de entrenamientos
     *
     * Este método implementa la interfaz OnEntrenamientoAddedListener.
     * Es usado por el DialogFragment para validar duplicados.
     *
     * @return La lista completa de entrenamientos actuales
     */
    @Override
    public List<Entrenamiento> getEntrenamientosExistentes() {
        return entrenamientos;  // Retornar la referencia a la lista
    }

    /**
     * onSaveInstanceState - Guarda el estado antes de destruir la Activity
     *
     * Este método del ciclo de vida se llama antes de que la Activity se destruya,
     * típicamente cuando se rota el dispositivo.
     *
     * Los datos guardados aquí se pasan al método onCreate() en el parámetro
     * savedInstanceState cuando la Activity se recrea.
     *
     * @param outState Bundle donde se guardan los datos del estado
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);  // Llamar al método padre

        // Guardar la lista de entrenamientos en el Bundle
        // putParcelableArrayList serializa los objetos Parcelable
        // Se crea una nueva ArrayList para evitar problemas de mutabilidad
        outState.putParcelableArrayList("entrenamientos", new ArrayList<>(entrenamientos));
    }

    /**
     * onPause - Se ejecuta cuando la Activity pasa a segundo plano
     *
     * Este método del ciclo de vida se llama cuando:
     * - El usuario sale de la app
     * - Se abre otra app encima
     * - Se bloquea el dispositivo
     * - La app está a punto de cerrarse
     *
     * Es el momento ideal para guardar datos importantes.
     */
    @Override
    protected void onPause() {
        super.onPause();  // Llamar al método padre obligatoriamente

        // Medida de seguridad: Guardar entrenamientos en SharedPreferences
        // Verificar que las variables no sean null antes de guardar
        if (entrenamientos != null && storage != null) {
            storage.guardarEntrenamientos(entrenamientos);
        }
        // Esto asegura que los datos se guarden incluso si la app se cierra inesperadamente
    }
}