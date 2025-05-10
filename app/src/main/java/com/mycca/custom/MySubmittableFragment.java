package com.mycca.custom;


import android.support.v4.app.Fragment;

import com.mycca.enums.State;

public abstract class MySubmittableFragment extends Fragment {
    public State state = State.INIT;
    public abstract void updateState(State state);
}
