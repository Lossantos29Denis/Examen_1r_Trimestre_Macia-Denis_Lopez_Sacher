package com.example.examen1rtrimestremacia_denislopezsacher;

import android.content.Context;      // Contexto de la aplicación
import android.view.LayoutInflater;  // Para inflar layouts XML
import android.view.View;            // Clase base de vistas
import android.view.ViewGroup;       // Contenedor de vistas
import android.widget.ArrayAdapter;  // Adapter base para listas
import android.widget.ImageView;     // Para mostrar iconos
import android.widget.TextView;      // Para mostrar texto

import androidx.annotation.NonNull;  // Anotación para parámetros no nulos
import androidx.annotation.Nullable; // Anotación para parámetros que pueden ser null

import java.util.List;  // Interfaz de lista

/**
 * EntrenamientoAdapter - Adapter personalizado para el ListView
 *
 * Este adapter conecta los DATOS (List<Entrenamiento>) con la VISTA (ListView)
 * siguiendo el patrón ADAPTER del diseño de software.
 *
 * ¿Qué es un Adapter?
 * - Es el "puente" entre los datos y la vista
 * - Convierte cada objeto de datos en una vista visual
 * - Gestiona el reciclaje de vistas para optimizar rendimiento
 *
 * ¿Qué es el patrón ViewHolder?
 * - Técnica de optimización para ListView/RecyclerView
 * - Evita llamadas repetidas a findViewById()
 * - Guarda referencias a las vistas en un objeto holder
 * - Mejora significativamente el rendimiento del scroll
 *
 * Flujo de getView():
 * 1. Android pide una vista para la posición X
 * 2. Si hay vista reciclable (convertView != null) → reutilizar
 * 3. Si no hay vista → inflar nueva y crear ViewHolder
 * 4. Obtener datos de la posición
 * 5. Actualizar las vistas con los datos
 * 6. Retornar la vista configurada
 *
 * Optimización ViewHolder:
 * SIN ViewHolder: findViewById() se llama por cada scroll → LENTO
 * CON ViewHolder: findViewById() solo una vez por vista → RÁPIDO
 *
 * Ejemplo de mejora de rendimiento:
 * - Lista con 100 items, usuario hace scroll 10 veces
 * - Sin ViewHolder: ~1,000 llamadas a findViewById()
 * - Con ViewHolder: ~10 llamadas a findViewById()
 * - Mejora: ~100x más rápido
 */
public class EntrenamientoAdapter extends ArrayAdapter<Entrenamiento> {

    // ============= ATRIBUTOS =============

    /** Contexto de la aplicación (necesario para inflar layouts) */
    private final Context context;

    /** Lista de entrenamientos que se mostrarán en el ListView */
    private final List<Entrenamiento> entrenamientos;

    // ============= CONSTRUCTOR =============

    /**
     * Constructor - Inicializa el adapter con el contexto y la lista de datos
     *
     * @param context Contexto de la aplicación (normalmente la Activity)
     * @param entrenamientos Lista de entrenamientos a mostrar
     */
    public EntrenamientoAdapter(@NonNull Context context, @NonNull List<Entrenamiento> entrenamientos) {
        // Llamar al constructor del padre (ArrayAdapter)
        // Parámetros:
        // - context: Contexto
        // - 0: No usamos layout por defecto (usamos el nuestro)
        // - entrenamientos: Lista de datos
        super(context, 0, entrenamientos);

        // Guardar referencias locales para acceso rápido
        this.context = context;
        this.entrenamientos = entrenamientos;
    }

    // ============= MÉTODO PRINCIPAL DEL ADAPTER =============

    /**
     * getView - Crea o reutiliza una vista para un item en la posición especificada
     *
     * Este es el método MÁS IMPORTANTE del Adapter.
     * Android llama a este método por cada item visible en pantalla.
     *
     * ¿Cuándo se llama?
     * - Al mostrar el ListView por primera vez
     * - Al hacer scroll (nuevos items entran en pantalla)
     * - Después de notifyDataSetChanged()
     *
     * PATRÓN VIEWHOLDER implementado aquí:
     *
     * CASO 1: convertView == null (NO hay vista reciclable)
     * - Inflar nuevo layout XML
     * - Crear nuevo ViewHolder
     * - Buscar vistas con findViewById() (SOLO UNA VEZ)
     * - Guardar ViewHolder en el tag de la vista
     *
     * CASO 2: convertView != null (SÍ hay vista reciclable)
     * - Reutilizar la vista existente
     * - Obtener ViewHolder del tag
     * - NO llamar a findViewById() (¡optimización!)
     *
     * Flujo visual del scroll:
     *
     * [Item 1] ← Visible
     * [Item 2] ← Visible
     * [Item 3] ← Visible
     * [Item 4] ← Visible (sale de pantalla al hacer scroll)
     *    ↓
     * Esta vista se recicla y se usa para:
     *    ↓
     * [Item 8] ← Nueva posición visible
     *
     * @param position Posición del item en la lista (0, 1, 2, ...)
     * @param convertView Vista reciclable (null si no hay vista disponible)
     * @param parent ListView padre que contiene este item
     * @return Vista configurada con los datos del item
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Declarar variable para el ViewHolder
        ViewHolder holder;

        // ========== PASO 1: OBTENER O CREAR LA VISTA ==========

        // Verificar si hay una vista reciclable disponible
        if (convertView == null) {
            // CASO 1: NO hay vista reciclable → CREAR NUEVA

            // Inflar el layout XML del item
            // LayoutInflater convierte XML → objetos View
            // Parámetros:
            // - R.layout.item_entrenamiento: Layout del item
            // - parent: ListView padre
            // - false: NO adjuntar aún al padre (ListView lo hace después)
            convertView = LayoutInflater.from(context).inflate(R.layout.item_entrenamiento, parent, false);

            // Crear un nuevo ViewHolder (contenedor de referencias a vistas)
            holder = new ViewHolder();

            // Buscar las vistas y guardar referencias en el ViewHolder
            // findViewById() es una operación COSTOSA (recorre el árbol de vistas)
            // Por eso solo lo hacemos UNA VEZ y guardamos las referencias
            holder.ivIcono = convertView.findViewById(R.id.iv_icono);   // ImageView del icono
            holder.tvNombre = convertView.findViewById(R.id.tv_nombre);  // TextView del nombre

            // Guardar el ViewHolder en el tag de la vista
            // setTag() permite asociar cualquier objeto con una vista
            // Recuperaremos este ViewHolder en el CASO 2
            convertView.setTag(holder);

        } else {
            // CASO 2: SÍ hay vista reciclable → REUTILIZAR

            // Recuperar el ViewHolder que guardamos en el tag
            // getTag() retorna Object, por eso hacemos cast a ViewHolder
            holder = (ViewHolder) convertView.getTag();

            // ¡NO llamamos a findViewById()! → OPTIMIZACIÓN
            // Ya tenemos las referencias guardadas en holder
        }

        // ========== PASO 2: OBTENER LOS DATOS ==========

        // Obtener el objeto Entrenamiento de la posición actual
        // position = 0 → primer item, position = 1 → segundo item, etc.
        Entrenamiento entrenamiento = entrenamientos.get(position);

        // ========== PASO 3: ACTUALIZAR LAS VISTAS CON LOS DATOS ==========

        // Usar las referencias del ViewHolder (rápido, sin findViewById)
        // Asignar el icono del entrenamiento al ImageView
        holder.ivIcono.setImageResource(entrenamiento.getIconoResId());

        // Asignar el nombre del entrenamiento al TextView
        holder.tvNombre.setText(entrenamiento.getNombre());

        // ========== PASO 4: RETORNAR LA VISTA CONFIGURADA ==========

        // Retornar la vista lista para ser mostrada en el ListView
        return convertView;
    }

    // ============= CLASE VIEWHOLDER =============

    /**
     * ViewHolder - Contenedor de referencias a las vistas de un item
     *
     * Este patrón es FUNDAMENTAL para la optimización de ListViews.
     *
     * ¿Qué problema resuelve?
     * - findViewById() es una operación costosa (O(n) en profundidad del árbol)
     * - Sin ViewHolder: se llama findViewById() cada vez que se hace scroll
     * - Con ViewHolder: se llama findViewById() solo al crear la vista
     *
     * ¿Cómo funciona?
     * 1. Al inflar una vista nueva → crear ViewHolder → buscar vistas → guardar referencias
     * 2. Guardar ViewHolder en el tag de la vista
     * 3. Al reciclar la vista → recuperar ViewHolder del tag → usar referencias guardadas
     *
     * Mejora de rendimiento:
     * - Lista con 50 items visibles, usuario hace scroll por todos
     * - Sin ViewHolder: ~500 llamadas a findViewById() → Scroll entrecortado
     * - Con ViewHolder: ~10 llamadas a findViewById() → Scroll suave
     *
     * ¿Por qué static?
     * - No necesita acceso a la instancia del Adapter
     * - Más eficiente en memoria
     * - Evita referencias innecesarias
     *
     * Nota: En RecyclerView esto es obligatorio, en ListView es opcional pero MUY recomendado
     */
    static class ViewHolder {
        /** Referencia al ImageView que muestra el icono del entrenamiento */
        ImageView ivIcono;

        /** Referencia al TextView que muestra el nombre del entrenamiento */
        TextView tvNombre;

        // Solo guardamos referencias a las vistas que necesitamos actualizar
        // No guardamos vistas estáticas o que no cambian
    }
}

