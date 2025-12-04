package com.example.examen1rtrimestremacia_denislopezsacher;

import android.app.Dialog;                    // Clase base de diálogos
import android.os.Bundle;                     // Para pasar datos
import android.view.LayoutInflater;           // Para inflar layouts
import android.view.View;                     // Clase base de vistas
import android.view.ViewGroup;                // Contenedor de vistas
import android.widget.Button;                 // Botones de acción
import android.widget.ImageView;              // Para mostrar iconos
import android.widget.Toast;                  // Mensajes emergentes

import androidx.annotation.NonNull;           // Anotación no nulo
import androidx.annotation.Nullable;          // Anotación puede ser null
import androidx.fragment.app.DialogFragment;  // Clase base de dialog fragments

import com.google.android.material.textfield.TextInputEditText; // Campo de texto Material

import java.util.List; // Interfaz de lista

/**
 * AddEntrenamientoDialogFragment - DialogFragment para añadir nuevos entrenamientos
 *
 * Este DialogFragment implementa un formulario completo para crear entrenamientos.
 *
 * Responsabilidades:
 * - Mostrar formulario con campos: Nombre, Descripción
 * - Permitir selección de icono (4 opciones)
 * - Validar campos obligatorios
 * - Validar duplicados (case-insensitive)
 * - Comunicar resultado mediante callback
 * - Adaptar tamaño según orientación
 *
 * ¿Qué es un DialogFragment?
 * - Fragment que se muestra como diálogo flotante
 * - Ventajas sobre Dialog tradicional:
 *   • Maneja correctamente rotaciones de pantalla
 *   • Se integra con el FragmentManager
 *   • Sigue el ciclo de vida de Fragments
 *   • Más flexible y robusto
 *
 * Patrón Callback:
 * - DialogFragment NO accede directamente a MainActivity
 * - Usa una interfaz (OnEntrenamientoAddedListener)
 * - MainActivity implementa la interfaz
 * - MainActivity se registra como listener
 * - Cuando se guarda, DialogFragment llama al callback
 * - MainActivity recibe los datos y los procesa
 *
 * Flujo de comunicación:
 * 1. MainActivity crea DialogFragment
 * 2. MainActivity se registra como listener
 * 3. Usuario llena formulario
 * 4. Usuario pulsa Guardar
 * 5. DialogFragment valida datos
 * 6. DialogFragment crea Entrenamiento
 * 7. DialogFragment llama a listener.onEntrenamientoAdded()
 * 8. MainActivity recibe el nuevo entrenamiento
 * 9. MainActivity actualiza la lista
 *
 * Validaciones implementadas:
 * - Campo nombre no vacío
 * - Campo descripción no vacío
 * - Nombre no duplicado (ignora mayúsculas/minúsculas)
 */
public class AddEntrenamientoDialogFragment extends DialogFragment {

    // ============= ATRIBUTOS DE LA UI =============

    /** Campo de texto para el nombre del entrenamiento */
    private TextInputEditText etNombre;

    /** Campo de texto para la descripción del entrenamiento */
    private TextInputEditText etDescripcion;

    /** ID del icono seleccionado (por defecto: Pilates) */
    private int selectedIconResId = R.drawable.ic_pilates;

    // ============= ATRIBUTOS PARA COMUNICACIÓN =============

    /** Listener para comunicar el resultado a MainActivity */
    private OnEntrenamientoAddedListener listener;

    /** Lista de entrenamientos existentes (para validar duplicados) */
    private List<Entrenamiento> entrenamientosExistentes;

    // ============= INTERFAZ CALLBACK =============

    /**
     * OnEntrenamientoAddedListener - Interfaz para comunicación con MainActivity
     *
     * Este es el PATRÓN CALLBACK que permite la comunicación entre componentes
     * de forma desacoplada (DialogFragment no conoce a MainActivity directamente).
     *
     * ¿Por qué usar una interfaz?
     * - Desacoplamiento: DialogFragment no depende de MainActivity
     * - Reutilización: El diálogo puede usarse con cualquier Activity
     * - Testabilidad: Fácil crear mocks para testing
     * - Flexibilidad: Varios listeners pueden implementar la interfaz
     *
     * Métodos:
     * - onEntrenamientoAdded(): Callback llamado cuando se añade un entrenamiento
     * - getEntrenamientosExistentes(): Obtiene la lista para validar duplicados
     */
    public interface OnEntrenamientoAddedListener {
        /**
         * Callback invocado cuando se añade un nuevo entrenamiento
         * @param entrenamiento El entrenamiento creado por el usuario
         */
        void onEntrenamientoAdded(Entrenamiento entrenamiento);

        /**
         * Obtiene la lista de entrenamientos existentes para validar duplicados
         * @return Lista actual de entrenamientos
         */
        List<Entrenamiento> getEntrenamientosExistentes();
    }

    // ============= CONFIGURACIÓN DEL LISTENER =============

    /**
     * setOnEntrenamientoAddedListener - Registra el listener para callbacks
     *
     * MainActivity llama a este método para registrarse como listener.
     *
     * Flujo:
     * 1. MainActivity crea el DialogFragment
     * 2. MainActivity llama a setOnEntrenamientoAddedListener(this)
     * 3. DialogFragment guarda la referencia
     * 4. DialogFragment obtiene la lista de entrenamientos existentes
     *
     * @param listener Objeto que implementa OnEntrenamientoAddedListener (MainActivity)
     */
    public void setOnEntrenamientoAddedListener(OnEntrenamientoAddedListener listener) {
        // Guardar referencia al listener (MainActivity)
        this.listener = listener;

        // Si el listener no es null, obtener la lista de entrenamientos
        if (listener != null) {
            // Llamar al método de la interfaz para obtener la lista
            // Esto se usa para validar duplicados antes de guardar
            this.entrenamientosExistentes = listener.getEntrenamientosExistentes();
        }
    }

    // ============= MÉTODOS DEL CICLO DE VIDA =============

    /**
     * onCreateView - Crea y configura la vista del diálogo
     *
     * Este método es el CORAZÓN del DialogFragment.
     * Aquí se configura toda la UI y la lógica de validación.
     *
     * Responsabilidades:
     * 1. Inflar el layout del diálogo
     * 2. Buscar todas las vistas (campos, botones, iconos)
     * 3. Configurar listeners para selección de iconos
     * 4. Configurar listener del botón Cancelar
     * 5. Configurar listener del botón Guardar (con validaciones)
     *
     * Validaciones en el botón Guardar:
     * - Campo nombre no vacío → Toast de error
     * - Campo descripción no vacío → Toast de error
     * - Nombre no duplicado (case-insensitive) → Toast de error
     * - Si todo OK → crear Entrenamiento → llamar callback → cerrar diálogo
     *
     * @param inflater Para inflar el layout
     * @param container Contenedor padre
     * @param savedInstanceState Estado guardado
     * @return Vista del diálogo configurada
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // PASO 1: Inflar el layout del diálogo
        // Convierte el XML en objetos View de Java
        View view = inflater.inflate(R.layout.dialog_add_entrenamiento, container, false);

        // PASO 2: Buscar los campos de texto del formulario
        etNombre = view.findViewById(R.id.et_nombre);           // Campo de nombre
        etDescripcion = view.findViewById(R.id.et_descripcion); // Campo de descripción

        // PASO 3: Buscar los iconos seleccionables
        ImageView ivPilates = view.findViewById(R.id.iv_icon_pilates);     // Icono Pilates
        ImageView ivVoleibol = view.findViewById(R.id.iv_icon_voleibol);   // Icono Voleibol
        ImageView ivFuerza = view.findViewById(R.id.iv_icon_fuerza);       // Icono Fuerza
        ImageView ivRunning = view.findViewById(R.id.iv_icon_running);     // Icono Running

        // PASO 4: Buscar los botones de acción
        Button btnCancel = view.findViewById(R.id.btn_cancel); // Botón Cancelar
        Button btnSave = view.findViewById(R.id.btn_save);     // Botón Guardar

        // ========== PASO 5: CONFIGURAR SELECCIÓN DE ICONOS ==========

        /**
         * iconClickListener - Listener único compartido por todos los iconos
         *
         * Este listener implementa el patrón de "single listener" donde
         * un solo listener maneja múltiples vistas.
         *
         * Ventajas:
         * - Menos código duplicado
         * - Más mantenible (lógica centralizada)
         * - Más eficiente en memoria
         *
         * Funcionamiento:
         * 1. Usuario hace click en un icono
         * 2. Resetear fondos de todos los iconos (transparente)
         * 3. Cambiar fondo del icono clickeado (morado claro)
         * 4. Identificar qué icono fue clickeado por su ID
         * 5. Guardar el ID del recurso correspondiente
         *
         * Feedback visual:
         * - Solo el icono seleccionado tiene fondo morado
         * - Los demás iconos tienen fondo transparente
         */
        View.OnClickListener iconClickListener = v -> {
            // PASO 1: Resetear fondos de todos los iconos a transparente
            // Esto asegura que solo uno tenga fondo coloreado
            resetIconBackgrounds(ivPilates, ivVoleibol, ivFuerza, ivRunning);

            // PASO 2: Marcar el icono seleccionado con fondo morado claro
            // Esto da feedback visual al usuario de su selección
            v.setBackgroundColor(getResources().getColor(R.color.purple_light, null));

            // PASO 3: Identificar qué icono fue clickeado y guardar su recurso
            // Comparamos el ID de la vista clickeada con los IDs de los iconos
            if (v.getId() == R.id.iv_icon_pilates) {
                // Usuario seleccionó Pilates
                selectedIconResId = R.drawable.ic_pilates;
            } else if (v.getId() == R.id.iv_icon_voleibol) {
                // Usuario seleccionó Voleibol
                selectedIconResId = R.drawable.ic_voleibol;
            } else if (v.getId() == R.id.iv_icon_fuerza) {
                // Usuario seleccionó Fuerza
                selectedIconResId = R.drawable.ic_fuerza;
            } else if (v.getId() == R.id.iv_icon_running) {
                // Usuario seleccionó Running
                selectedIconResId = R.drawable.ic_running;
            }
            // selectedIconResId se usa al crear el Entrenamiento en btnSave
        };

        // Asignar el mismo listener a todos los iconos
        // Todos comparten la misma lógica de selección
        ivPilates.setOnClickListener(iconClickListener);   // Click en Pilates
        ivVoleibol.setOnClickListener(iconClickListener);  // Click en Voleibol
        ivFuerza.setOnClickListener(iconClickListener);    // Click en Fuerza
        ivRunning.setOnClickListener(iconClickListener);   // Click en Running

        // Marcar el primer icono (Pilates) como seleccionado por defecto
        // Esto muestra al usuario que Pilates es la opción por defecto
        ivPilates.setBackgroundColor(getResources().getColor(R.color.purple_light, null));

        // ========== PASO 6: CONFIGURAR BOTÓN CANCELAR ==========

        /**
         * Listener del botón Cancelar
         *
         * Simplemente cierra el diálogo sin hacer nada.
         * dismiss() destruye el DialogFragment y lo quita de pantalla.
         *
         * No se llama al callback porque no se creó ningún entrenamiento.
         */
        btnCancel.setOnClickListener(v -> dismiss());

        // ========== PASO 7: CONFIGURAR BOTÓN GUARDAR (CON VALIDACIONES) ==========

        /**
         * Listener del botón Guardar
         *
         * Este es el método MÁS IMPORTANTE del diálogo.
         * Realiza todas las validaciones antes de crear el entrenamiento.
         *
         * Flujo de validación:
         * 1. Obtener texto de los campos (trim elimina espacios)
         * 2. Validación 1: Nombre no vacío
         * 3. Validación 2: Descripción no vacía
         * 4. Validación 3: Nombre no duplicado (case-insensitive)
         * 5. Si todas las validaciones pasan:
         *    a. Crear nuevo objeto Entrenamiento
         *    b. Llamar al callback del listener
         *    c. Cerrar el diálogo
         *
         * Cada validación que falla:
         * - Muestra un Toast con el error
         * - Hace return (no continúa con el resto)
         * - NO cierra el diálogo (usuario puede corregir)
         */
        btnSave.setOnClickListener(v -> {
            // ========== OBTENER Y LIMPIAR DATOS DE LOS CAMPOS ==========

            // Obtener el texto del campo nombre
            // getText() puede retornar null, por eso verificamos primero
            // toString() convierte Editable → String
            // trim() elimina espacios al inicio y final
            // Si es null, usar string vacío por defecto
            String nombre = etNombre.getText() != null ? etNombre.getText().toString().trim() : "";

            // Mismo proceso para la descripción
            String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";

            // ========== VALIDACIÓN 1: NOMBRE NO VACÍO ==========

            /**
             * Verificar que el nombre no esté vacío
             *
             * isEmpty() retorna true si el string tiene longitud 0
             * (después del trim, espacios en blanco cuentan como vacío)
             */
            if (nombre.isEmpty()) {
                // Mostrar mensaje de error al usuario
                // Toast.LENGTH_SHORT = 2 segundos
                Toast.makeText(getContext(), "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show();
                // return detiene la ejecución, no se continúa con las demás validaciones
                return;
            }

            // ========== VALIDACIÓN 2: DESCRIPCIÓN NO VACÍA ==========

            /**
             * Verificar que la descripción no esté vacía
             */
            if (descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Por favor ingresa una descripción", Toast.LENGTH_SHORT).show();
                return;
            }

            // ========== VALIDACIÓN 3: NOMBRE NO DUPLICADO ==========

            /**
             * Verificar que no exista un entrenamiento con el mismo nombre
             *
             * Validación case-insensitive:
             * - "Pilates" = "pilates" = "PILATES" = "PiLaTeS"
             *
             * Método:
             * 1. Recorrer la lista de entrenamientos existentes
             * 2. Comparar cada nombre con equalsIgnoreCase()
             * 3. Si encuentra coincidencia → mostrar error con el nombre original
             * 4. return → no permite guardar
             *
             * ¿Por qué equalsIgnoreCase()?
             * - Evita duplicados con diferente capitalización
             * - Mejora la experiencia del usuario
             * - Mantiene la base de datos limpia
             */
            if (entrenamientosExistentes != null) {
                // Iterar por cada entrenamiento existente
                for (Entrenamiento existente : entrenamientosExistentes) {
                    // Comparar nombres ignorando mayúsculas/minúsculas
                    if (existente.getNombre().equalsIgnoreCase(nombre)) {
                        // ¡Duplicado encontrado!
                        // Mostrar el nombre ORIGINAL del existente (no el ingresado)
                        // Esto ayuda al usuario a ver la diferencia de capitalización
                        Toast.makeText(getContext(),
                            "Ya existe un entrenamiento con ese nombre: " + existente.getNombre(),
                            Toast.LENGTH_LONG).show(); // LENGTH_LONG = 3.5 segundos
                        return; // No permitir guardar
                    }
                }
            }

            // ========== TODAS LAS VALIDACIONES PASARON ==========

            // PASO 1: Crear el nuevo objeto Entrenamiento
            // Constructor: Entrenamiento(nombre, descripcion, iconoResId)
            // selectedIconResId contiene el icono seleccionado por el usuario
            Entrenamiento nuevoEntrenamiento = new Entrenamiento(nombre, descripcion, selectedIconResId);

            // PASO 2: Notificar a MainActivity mediante el callback
            // Verificar que el listener no sea null (buena práctica)
            if (listener != null) {
                // Llamar al método de la interfaz
                // MainActivity recibirá el nuevo entrenamiento y lo procesará
                listener.onEntrenamientoAdded(nuevoEntrenamiento);
            }

            // PASO 3: Cerrar el diálogo
            // dismiss() destruye el DialogFragment y lo quita de pantalla
            // El usuario vuelve a MainActivity con el nuevo entrenamiento añadido
            dismiss();
        });

        // ========== PASO 8: RETORNAR LA VISTA CONFIGURADA ==========

        // Retornar la vista con todos los listeners configurados
        // Android mostrará esta vista como un diálogo flotante
        return view;
    }

    // ============= MÉTODOS AUXILIARES =============

    /**
     * resetIconBackgrounds - Resetea los fondos de todos los iconos a transparente
     *
     * Este método auxiliar limpia la selección visual de todos los iconos.
     *
     * ¿Por qué varargs (ImageView...)?
     * - Permite pasar número variable de parámetros
     * - resetIconBackgrounds(iv1, iv2, iv3, iv4) es válido
     * - Internamente se convierte en array
     * - Más limpio que pasar un array explícito
     *
     * Uso:
     * resetIconBackgrounds(ivPilates, ivVoleibol, ivFuerza, ivRunning);
     *
     * @param imageViews Número variable de ImageViews a resetear
     */
    private void resetIconBackgrounds(ImageView... imageViews) {
        // Iterar por cada ImageView pasado como parámetro
        for (ImageView iv : imageViews) {
            // Cambiar el fondo a transparente
            // android.R.color.transparent = color transparente del sistema
            // null = no hay tema específico
            iv.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        }
        // Resultado: Todos los iconos quedan sin fondo coloreado
    }

    // ============= MÉTODOS DEL CICLO DE VIDA =============

    /**
     * onStart - Se ejecuta cuando el diálogo está a punto de mostrarse
     *
     * Este método del ciclo de vida es el momento ideal para:
     * - Configurar el tamaño del diálogo
     * - Ajustar propiedades de la ventana
     * - Detectar orientación y adaptar el tamaño
     *
     * Ciclo de vida de DialogFragment:
     * onCreate() → onCreateView() → onStart() → onResume()
     *
     * ¿Por qué en onStart() y no en onCreateView()?
     * - En onStart() el diálogo ya está creado y tiene Window
     * - Podemos acceder a getDialog() y getWindow() con seguridad
     * - Es el momento recomendado por Google para configurar tamaño
     *
     * Estrategia de tamaño:
     * - Portrait: Ancho completo (MATCH_PARENT)
     * - Landscape: 65% del ancho (más compacto, mejor legibilidad)
     *
     * ¿Por qué diferente tamaño según orientación?
     * - En portrait hay poco ancho → aprovechar todo el espacio
     * - En landscape hay mucho ancho → 100% sería excesivo
     * - 65% en landscape es el sweet spot (no muy grande, no muy pequeño)
     */
    @Override
    public void onStart() {
        super.onStart();  // Llamar al método padre obligatoriamente

        // Obtener referencia al Dialog
        Dialog dialog = getDialog();

        // Verificar que el dialog y su window no sean null (buena práctica)
        // Puede ser null en casos raros (dialog aún no completamente inicializado)
        if (dialog != null && dialog.getWindow() != null) {
            // ========== DETECTAR ORIENTACIÓN ==========

            // Obtener la orientación actual del dispositivo
            // getResources().getConfiguration() = configuración actual
            // orientation = ORIENTATION_PORTRAIT o ORIENTATION_LANDSCAPE
            int orientation = getResources().getConfiguration().orientation;

            // ========== CONFIGURAR TAMAÑO SEGÚN ORIENTACIÓN ==========

            if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                // ===== LANDSCAPE =====

                // Calcular el 65% del ancho de la pantalla
                // getDisplayMetrics().widthPixels = ancho total en píxeles
                // * 0.65 = 65% del ancho
                // (int) = convertir double a int
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.65);

                // Establecer el tamaño del diálogo
                // Parámetros: (ancho, alto)
                // - width: 65% del ancho de pantalla (calculado arriba)
                // - WRAP_CONTENT: altura ajustada al contenido
                dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

            } else {
                // ===== PORTRAIT =====

                // Usar ancho completo en portrait
                // MATCH_PARENT = ocupar todo el ancho disponible
                // WRAP_CONTENT = altura ajustada al contenido
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }

            // Resultado:
            // - Portrait: Diálogo ocupa todo el ancho
            // - Landscape: Diálogo ocupa 65% del ancho (más compacto y legible)
        }
    }
}

