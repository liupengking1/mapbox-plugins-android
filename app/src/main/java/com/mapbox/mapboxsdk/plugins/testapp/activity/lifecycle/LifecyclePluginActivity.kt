package com.mapbox.mapboxsdk.plugins.testapp.activity.lifecycle

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.testapp.R
import com.mapbox.mapboxsdk.plugins.testapp.Utils
import com.mapbox.mapboxsdk.plugins.lifecycle.LifecyclePlugin
import kotlinx.android.synthetic.main.activity_traffic.*

/**
 * Activity showcasing lifecycle plugin integration
 */
class LifecyclePluginActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapboxMap: MapboxMap? = null
    private var lifecyclePlugin: LifecyclePlugin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annotation)
        lifecyclePlugin = LifecyclePlugin(mapView, savedInstanceState, this)

        mapView.getMapAsync(this)

        fabStyles.setOnClickListener {
            mapboxMap?.setStyle(Style.Builder().fromUrl(Utils.nextStyle))
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        lifecyclePlugin?.onSaveInstanceState(outState)
    }
}