package com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos;

import android.os.Parcel;

import com.example.examen1rtrimestremacia_denislopezsacher.Entrenamiento;
import com.example.examen1rtrimestremacia_denislopezsacher.R;

public class Fuerza extends Entrenamiento {

    public Fuerza() {
        super(
            "Fuerza",
            "Entrenamiento con pesas y resistencia para aumentar la masa muscular y la potencia.",
            R.drawable.ic_fuerza
        );
    }

    protected Fuerza(Parcel in) {
        super(in);
    }

    public static final Creator<Fuerza> CREATOR = new Creator<Fuerza>() {
        @Override
        public Fuerza createFromParcel(Parcel in) {
            return new Fuerza(in);
        }

        @Override
        public Fuerza[] newArray(int size) {
            return new Fuerza[size];
        }
    };
}

