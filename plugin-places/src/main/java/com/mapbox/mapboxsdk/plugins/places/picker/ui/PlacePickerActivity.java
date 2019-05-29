package com.mapbox.mapboxsdk.plugins.places.picker.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.places.R;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mapbox.mapboxsdk.plugins.places.common.PlaceConstants;
import com.mapbox.mapboxsdk.plugins.places.common.utils.ColorUtils;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker.IntentBuilder;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.viewmodel.PlacePickerViewModel;

import java.util.Locale;

import timber.log.Timber;

import static android.support.design.widget.Snackbar.LENGTH_LONG;

/**
 * Do not use this class directly, instead create an intent using the {@link IntentBuilder} inside
 * the {@link PlacePicker} class.
 *
 * @since 0.2.0
 */
public class PlacePickerActivity extends AppCompatActivity implements OnMapReadyCallback,
  MapboxMap.OnCameraMoveStartedListener, MapboxMap.OnCameraIdleListener, Observer<CarmenFeature> {

  CurrentPlaceSelectionBottomSheet bottomSheet;
  CarmenFeature carmenFeature;
  private PlacePickerViewModel viewModel;
  private PlacePickerOptions options;
  private ImageView markerImage;
  private MapboxMap mapboxMap;
  private String accessToken;
  private MapView mapView;
  private CarmenFeature selectedSearchUiCarmenFeatureForBottomSheet;
  private CardView searchCardView;
  public Boolean resultsCardViewListIsCollapsed = true;
  private final int heightToMatchToolbarHeight = 147;
  private boolean includeReverseGeocode;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Hide any toolbar an apps theme might automatically place in activities. Typically creating an
    // activity style would cover this issue but this seems to prevent us from getting the users
    // application colorPrimary color.
    getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.hide();
    }
    setContentView(R.layout.mapbox_activity_place_picker);

    if (savedInstanceState == null) {
      accessToken = getIntent().getStringExtra(PlaceConstants.ACCESS_TOKEN);
      options = getIntent().getParcelableExtra(PlaceConstants.PLACE_OPTIONS);
      includeReverseGeocode = options.includeReverseGeocode();
    }

    // Initialize the view model.
    viewModel = ViewModelProviders.of(this).get(PlacePickerViewModel.class);
    viewModel.getResults().observe(this, this);

    bindViews();
    if (options.includeSearch()) {
      AppBarLayout appBarLayout = findViewById(R.id.place_picker_app_bar_layout);
      appBarLayout.setVisibility(View.GONE);
    } else {
      addBackButtonListener();
      customizeViews();
    }
    addPlaceSelectedButton();
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  private void addBackButtonListener() {
    ImageView backButton = findViewById(R.id.mapbox_place_picker_toolbar_back_button);
    backButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
      }
    });
  }

  private void bindViews() {
    mapView = findViewById(R.id.map_view);
    bottomSheet = findViewById(R.id.mapbox_plugins_picker_bottom_sheet);
    markerImage = findViewById(R.id.mapbox_plugins_image_view_marker);
  }

  private void customizeViews() {
    ConstraintLayout toolbar = findViewById(R.id.place_picker_toolbar);
    if (options != null && options.toolbarColor() != null) {
      toolbar.setBackgroundColor(options.toolbarColor());
    } else {
      int color = ColorUtils.getMaterialColor(this, R.attr.colorPrimary);
      toolbar.setBackgroundColor(color);
    }
  }

  @Override
  public void onMapReady(final MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
      @Override
      public void onStyleLoaded(@NonNull Style style) {
        if (options != null) {
          if (options.startingBounds() != null) {
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(options.startingBounds(), 0));
          } else if (options.statingCameraPosition() != null) {
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(options.statingCameraPosition()));
          }

          if (options.includeSearch()) {
            searchCardView = findViewById(R.id.optional_search_autocomplete_cardview);
            searchCardView.setVisibility(View.VISIBLE);
            PlaceAutocompleteFragment autocompleteFragment = new PlaceAutocompleteFragment();
            PlaceOptions placeOptions = PlaceOptions.builder()
                .toolbarColor(getThemePrimaryColor(PlacePickerActivity.this))
//                .hint(getString(R.string.mapbox_plugins_autocomplete_search_hint))
                .build();

            autocompleteFragment = PlaceAutocompleteFragment.newInstance(
                Mapbox.getAccessToken(), placeOptions);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.optional_search_autocomplete_cardview_fragment_container,
                autocompleteFragment, PlaceAutocompleteFragment.TAG);
            transaction.commit();

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
              @Override
              public void onPlaceSelected(CarmenFeature carmenFeature) {

                PlacePickerActivity.this.selectedSearchUiCarmenFeatureForBottomSheet = carmenFeature;

                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(carmenFeature.center().latitude(),
                        carmenFeature.center().longitude()), mapboxMap.getCameraPosition().zoom));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                  adjustResultsCardViewHeight(resultsCardViewListIsCollapsed,
                      searchCardView.getMeasuredHeight());
                  resultsCardViewListIsCollapsed = !resultsCardViewListIsCollapsed;
                } else {
                  searchCardView.setLayoutParams(new FrameLayout.LayoutParams(
                      searchCardView.getMeasuredWidth(),147));
                }
              }

              @Override
              public void onCancel() {
                finish();
              }
            });

          }

          if (includeReverseGeocode) {
            // Initialize with the markers current location information.
            makeReverseGeocodingSearch();
          }
        }
        PlacePickerActivity.this.mapboxMap.addOnCameraMoveStartedListener(PlacePickerActivity.this);
        PlacePickerActivity.this.mapboxMap.addOnCameraIdleListener(PlacePickerActivity.this);
      }
    });
  }

  public void adjustResultsCardViewHeight(boolean expandCard,
                                           int expandedHeight) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      TransitionManager.beginDelayedTransition(searchCardView, new TransitionSet()
          .addTransition(new ChangeBounds()));
      ViewGroup.LayoutParams params = searchCardView.getLayoutParams();
      params.height = expandCard ? expandedHeight : heightToMatchToolbarHeight;
      searchCardView.setLayoutParams(params);
    }
  }

  private static int getThemePrimaryColor(Context context) {
    int colorAttr;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      colorAttr = android.R.attr.colorPrimary;
    } else {
      //Get colorAccent defined for AppCompat
      colorAttr = context.getResources().getIdentifier("colorPrimary", "attr", context.getPackageName());
    }
    TypedValue outValue = new TypedValue();
    context.getTheme().resolveAttribute(colorAttr, outValue, true);
    return outValue.data;
  }

  @Override
  public void onCameraMoveStarted(int reason) {
    Timber.v("Map camera has begun moving.");
    if (markerImage.getTranslationY() == 0) {
      markerImage.animate().translationY(-75)
        .setInterpolator(new OvershootInterpolator()).setDuration(250).start();
      if (includeReverseGeocode) {
        if (bottomSheet.isShowing()) {
          bottomSheet.dismissPlaceDetails();
        }
      }
    }
  }

  @Override
  public void onCameraIdle() {
    Timber.v("Map camera is now idling.");
    markerImage.animate().translationY(0)
      .setInterpolator(new OvershootInterpolator()).setDuration(250).start();
    if (includeReverseGeocode) {
      if (options.includeSearch()) {
        if (selectedSearchUiCarmenFeatureForBottomSheet != null) {
          bottomSheet.setPlaceDetails(selectedSearchUiCarmenFeatureForBottomSheet);
        }
      }
      bottomSheet.setPlaceDetails(null);
      // Initialize with the markers current location information.
      makeReverseGeocodingSearch();
    }
  }

  @Override
  public void onChanged(@Nullable CarmenFeature carmenFeature) {
    if (carmenFeature == null) {
      carmenFeature = CarmenFeature.builder().placeName(
        String.format(Locale.US, "[%f, %f]",
          mapboxMap.getCameraPosition().target.getLatitude(),
          mapboxMap.getCameraPosition().target.getLongitude())
      ).text("No address found").properties(new JsonObject()).build();
    }
    this.carmenFeature = carmenFeature;
    bottomSheet.setPlaceDetails(carmenFeature);
  }

  private void makeReverseGeocodingSearch() {
    LatLng latLng = mapboxMap.getCameraPosition().target;
    if (latLng != null) {
      viewModel.reverseGeocode(
          Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()),
          accessToken, options
      );
    }
  }

  private void addPlaceSelectedButton() {
    FloatingActionButton placeSelectedButton = findViewById(R.id.place_chosen_button);
    placeSelectedButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (carmenFeature == null && includeReverseGeocode) {
          Snackbar.make(bottomSheet,
            getString(R.string.mapbox_plugins_place_picker_not_valid_selection),
            LENGTH_LONG).show();
          return;
        }
        placeSelected();
      }
    });
  }

  void placeSelected() {
    Intent returningIntent = new Intent();
    if (includeReverseGeocode) {
      String json = carmenFeature.toJson();
      returningIntent.putExtra(PlaceConstants.RETURNING_CARMEN_FEATURE, json);
    }
    returningIntent.putExtra(PlaceConstants.MAP_CAMERA_POSITION, mapboxMap.getCameraPosition());
    setResult(AppCompatActivity.RESULT_OK, returningIntent);
    finish();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }
}
