package com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos;

import android.os.Parcel;

import com.example.examen1rtrimestremacia_denislopezsacher.Entrenamiento;
import com.example.examen1rtrimestremacia_denislopezsacher.R;

public class Running extends Entrenamiento {

    public Running() {
        super(
            "Running",
            "Carrera que mejora la resistencia cardiovascular, quema calorías y fortalece las piernas.",
            R.drawable.ic_running
        );
    }

    protected Running(Parcel in) {
        super(in);
    }

    public static final Creator<Running> CREATOR = new Creator<Running>() {
        @Override
        public Running createFromParcel(Parcel in) {
            return new Running(in);
        }

        @Override
        public Running[] newArray(int size) {
            return new Running[size];
        }
    };
}

