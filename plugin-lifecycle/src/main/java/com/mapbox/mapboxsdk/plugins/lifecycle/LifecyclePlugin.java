package com.mapbox.mapboxsdk.plugins.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.mapbox.mapboxsdk.maps.MapView;

import org.jetbrains.annotations.NotNull;


/**
 * The Lifecycle Plugin helps to easily manage the lifecycle of mapview.
 * <p>
 * Initialise this plugin in the onCreate() and provide
 * a valid instance of {@link MapView}, savedInstanceState and activity/fragmentation context.
 * </p>
 */
@UiThread
public final class LifecyclePlugin implements LifecycleCategory {

  private final MapView mapView;
  private final Context context;
  private final Bundle savedInstanceState;

  /**
   * Create a lifecycle plugin.
   *
   * @param mapView   the MapView to apply the lifecycle plugin to
   * @param savedInstanceState   Bundle of your saved state.
   * @param context   the Activity or Fragment to apply the lifecycle plugin to
   */
  public LifecyclePlugin(@NonNull MapView mapView, @Nullable Bundle savedInstanceState, @NotNull Context context) {
    if ( mapView == null || context == null ) {
      throw new NullPointerException();
    }
    if( !(context instanceof LifecycleOwner) ) {
      throw new RuntimeException("Could not register lifecycle plugin, your activity/fragment didn't implement lifecycle owner interface.");
    }
    this.mapView = mapView;
    this.savedInstanceState = savedInstanceState;
    this.context = context;

    ((LifecycleOwner) this.context).getLifecycle().addObserver(this);
    this.context.registerComponentCallbacks(this);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  void LibOnCreate() {
    this.mapView.onCreate(savedInstanceState);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_START)
  void LibOnStart() {
    this.mapView.onStart();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  void LibOnStop() {
    this.mapView.onStop();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  void LibOnResume() {
    this.mapView.onResume();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  void LibOnPause() {
    this.mapView.onPause();
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  void cleanup() {
    this.mapView.onDestroy();
    ((LifecycleOwner) this.context).getLifecycle().removeObserver(this);
    this.context.unregisterComponentCallbacks(this);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
  }

  @Override
  public void onLowMemory() {
    this.mapView.onLowMemory();
  }

  /**
   * Should be called on Activity's onSaveInstanceState(Bundle)
   * @param outState
   */
  public void onSaveInstanceState(Bundle outState) {
    this.mapView.onSaveInstanceState(outState);
  }
}
