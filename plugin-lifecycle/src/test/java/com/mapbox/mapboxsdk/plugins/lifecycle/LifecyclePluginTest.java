package com.mapbox.mapboxsdk.plugins.lifecycle;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.os.Bundle;

import com.mapbox.mapboxsdk.maps.MapView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(RobolectricTestRunner.class)
public class LifecyclePluginTest {

  @Mock
  private MapView mapView;

  private Context context;

  private Bundle bundle;

  private Lifecycle lifecycle;

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    context = mock(Context.class, withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).extraInterfaces(LifecycleOwner.class));
    bundle = new Bundle();
    bundle.putInt("test", 0);
    lifecycle = mock(Lifecycle.class);
    when(((LifecycleOwner)context).getLifecycle()).thenReturn(lifecycle);
    Mockito.doNothing().when(context).registerComponentCallbacks(any(ComponentCallbacks.class));
    Mockito.doNothing().when(context).unregisterComponentCallbacks(any(ComponentCallbacks.class));
  }

  @Test(expected = NullPointerException.class)
  public void testNonNullAnnotatedArgs() {
    new LifecyclePlugin(null, null, null);
  }

  @Test(expected = RuntimeException.class)
  public void testIncompatibleContext() {
    Context testContext = mock(Context.class, Mockito.CALLS_REAL_METHODS);
    new LifecyclePlugin(mapView, bundle, testContext);
  }

  @Test
  public void testCompatibleContext() {
    new LifecyclePlugin(mapView, bundle, context);
  }

  @Test
  public void testOnLowMemory() {
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView,bundle, context);

    lifecyclePlugin.onLowMemory();
    Mockito.verify(mapView).onLowMemory();
  }

  @Test
  public void testOnSaveInstanceState() {
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView,bundle, context);
    Bundle bundle = new Bundle();
    bundle.putInt("ONE", 1);
    bundle.putInt("TWO", 2);
    lifecyclePlugin.onSaveInstanceState(bundle);
    Mockito.verify(mapView).onSaveInstanceState(bundle);
  }

  @Test
  public void libOnCreate() {
    LifecycleOwner owner = mock(LifecycleOwner.class);
    LifecycleRegistry lifecycle = new LifecycleRegistry(owner);
    Mockito.when(owner.getLifecycle()).thenReturn(lifecycle);

    // Instantiate your class to test
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView, bundle, context);
    lifecycle.addObserver(lifecyclePlugin);

    // Set lifecycle state
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

    // Verify that the event was handled properly
    Mockito.verify(mapView).onCreate(bundle);
  }

  @Test
  public void libOnStart() {
    LifecycleOwner owner = mock(LifecycleOwner.class);
    LifecycleRegistry lifecycle = new LifecycleRegistry(owner);
    Mockito.when(owner.getLifecycle()).thenReturn(lifecycle);

    // Instantiate your class to test
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView, bundle, context);
    lifecycle.addObserver(lifecyclePlugin);

    // Set lifecycle state
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);

    // Verify that the event was handled properly
    Mockito.verify(mapView).onStart();
  }

  @Test
  public void libOnStop() {
    // TODO Verify that the event was handled properly
  }

  @Test
  public void libOnResume() {
    LifecycleOwner owner = mock(LifecycleOwner.class);
    LifecycleRegistry lifecycle = new LifecycleRegistry(owner);
    Mockito.when(owner.getLifecycle()).thenReturn(lifecycle);

    // Instantiate your class to test
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView, bundle, context);
    lifecycle.addObserver(lifecyclePlugin);

    // Set lifecycle state
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);

    // Verify that the event was handled properly
    verify(mapView).onResume();
  }

  @Test
  public void libOnPause() {
    // TODO Verify that the event was handled properly
  }

  @Test
  public void libOnDestroy() {
    // TODO Verify that the event was handled properly
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView, bundle, context);
    lifecyclePlugin.cleanup();
    verify(mapView).onDestroy();
    verify(context).unregisterComponentCallbacks(any(ComponentCallbacks.class));
    verify(lifecycle).removeObserver(any(LifecycleObserver.class));
  }

  @Test
  public void onLowMemory() {
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView, bundle, context);
    lifecyclePlugin.onLowMemory();
    verify(mapView).onLowMemory();
  }

  @Test
  public void onSaveInstanceState() {
    LifecyclePlugin lifecyclePlugin = new LifecyclePlugin(mapView, bundle, context);
    lifecyclePlugin.onSaveInstanceState(bundle);
    verify(mapView).onSaveInstanceState(bundle);
  }
}
