package com.mapbox.mapboxsdk.plugins.lifecycle;

import android.arch.lifecycle.LifecycleObserver;
import android.content.ComponentCallbacks;
import android.os.Bundle;

public interface LifecycleCategory extends LifecycleObserver, ComponentCallbacks {
    void onSaveInstanceState(Bundle outState);
}
