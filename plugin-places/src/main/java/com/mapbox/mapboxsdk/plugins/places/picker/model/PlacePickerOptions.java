package com.mapbox.mapboxsdk.plugins.places.picker.model;

import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.mapbox.api.geocoding.v5.GeocodingCriteria.GeocodingTypeCriteria;
import com.mapbox.core.utils.TextUtils;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.plugins.places.common.model.BasePlaceOptions;

@AutoValue
public abstract class PlacePickerOptions implements BasePlaceOptions, Parcelable {

  @Override
  @Nullable
  public abstract String language();

  @Override
  @Nullable
  public abstract String geocodingTypes();

  @Nullable
  public abstract LatLngBounds startingBounds();

  @Nullable
  @Override
  public abstract Integer toolbarColor();

  @Nullable
  public abstract CameraPosition statingCameraPosition();

  public abstract boolean includeReverseGeocode();

  public abstract boolean includeSearch();

  public static Builder builder() {
    return new AutoValue_PlacePickerOptions.Builder()
        .includeReverseGeocode(true)
        .includeSearch(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder toolbarColor(@ColorInt Integer toolbarColor);

    public abstract Builder language(String language);

    public Builder geocodingTypes(@NonNull @GeocodingTypeCriteria String... geocodingTypes) {
      geocodingTypes(TextUtils.join(",", geocodingTypes));
      return this;
    }

    abstract Builder geocodingTypes(String geocodingTypes);

    public abstract Builder startingBounds(@NonNull LatLngBounds bounds);

    public abstract Builder statingCameraPosition(@NonNull CameraPosition cameraPosition);

    /**
     * @param includeReverseGeocode whether or not to make a reverse geocoding call to
     *                              retrieve and display information associated with
     *                              the picked location's coordinates. Defaults to true.
     *
     * @return this builder instance for chaining options together
     */
    public abstract Builder includeReverseGeocode(boolean includeReverseGeocode);

    /**
     * @param includeSearch whether or not to include autocomplete geocoding search
     *                      field with the Place Picker UI. Defaults to false.
     *
     * @return this builder instance for chaining options together
     */
    public abstract Builder includeSearch(boolean includeSearch);

    public abstract PlacePickerOptions build();
  }
}
