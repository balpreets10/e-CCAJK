package com.mycca.custom;

import android.support.v7.app.AppCompatActivity;

import com.mycca.enums.State;

public abstract class MySubmittableAppCompatActivity extends AppCompatActivity{

    public State state = State.INIT;
    public abstract void updateState(State state);
}
