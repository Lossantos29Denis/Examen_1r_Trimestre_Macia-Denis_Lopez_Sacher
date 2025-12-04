package com.example.examen1rtrimestremacia_denislopezsacher.entrenamientos;

import android.os.Parcel;

import com.example.examen1rtrimestremacia_denislopezsacher.Entrenamiento;
import com.example.examen1rtrimestremacia_denislopezsacher.R;

public class Voleibol extends Entrenamiento {

    public Voleibol() {
        super(
            "Voleibol",
            "Deporte de equipo que mejora la coordinación, agilidad y trabajo en equipo.",
            R.drawable.ic_voleibol
        );
    }

    protected Voleibol(Parcel in) {
        super(in);
    }

    public static final Creator<Voleibol> CREATOR = new Creator<Voleibol>() {
        @Override
        public Voleibol createFromParcel(Parcel in) {
            return new Voleibol(in);
        }

        @Override
        public Voleibol[] newArray(int size) {
            return new Voleibol[size];
        }
    };
}

