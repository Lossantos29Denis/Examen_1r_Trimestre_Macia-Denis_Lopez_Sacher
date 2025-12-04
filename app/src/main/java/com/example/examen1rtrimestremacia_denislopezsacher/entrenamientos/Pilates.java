package com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos;

import android.os.Parcel;

import com.example.examen1rtrimestremacia_denislopezsacher.Entrenamiento;
import com.example.examen1rtrimestremacia_denislopezsacher.R;

public class Pilates extends Entrenamiento {

    public Pilates() {
        super(
            "Pilates",
            "Ejercicio de bajo impacto que mejora la flexibilidad, fuerza muscular y postura corporal.",
            R.drawable.ic_pilates
        );
    }

    protected Pilates(Parcel in) {
        super(in);
    }

    public static final Creator<Pilates> CREATOR = new Creator<Pilates>() {
        @Override
        public Pilates createFromParcel(Parcel in) {
            return new Pilates(in);
        }

        @Override
        public Pilates[] newArray(int size) {
            return new Pilates[size];
        }
    };
}

