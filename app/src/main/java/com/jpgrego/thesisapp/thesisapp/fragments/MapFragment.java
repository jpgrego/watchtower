package com.jpgrego.thesisapp.thesisapp.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jpgrego.thesisapp.thesisapp.R;
import com.jpgrego.thesisapp.thesisapp.services.MozillaLocationResponse;
import com.jpgrego.thesisapp.thesisapp.utils.Constants;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import java.util.List;


/**
 * Created by jpgrego on 28/11/16.
 */

public final class MapFragment extends Fragment {

    private static final int RADIUS_BORDER_COLOR = Color.argb(70, 72, 133, 237);
    private static final int RADIUS_FILL_COLOR = Color.argb(30, 72, 133, 237);
    private static Marker currentLocationMarker;
    private static Polygon currentLocationRadius;
    private MapView mapView;
    private IMapController mapController;

    private final BroadcastReceiver locationInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final MozillaLocationResponse location =
                    intent.getParcelableExtra(Constants.MAP_LOCATION_EXTRA_NAME);
            final float latitude = location.getLatitude();
            final float longitude = location.getLongitude();
            final float accuracy = location.getAccuracy();
            final GeoPoint point = new GeoPoint(latitude, longitude);

            if(mapController != null) {
                final List<Overlay> overlays = mapView.getOverlays();

                if(currentLocationMarker != null) {
                    overlays.remove(currentLocationMarker);
                }

                if(currentLocationRadius != null) {
                    overlays.remove(currentLocationRadius);
                }

                currentLocationMarker = new Marker(mapView);
                //noinspection deprecation
                currentLocationMarker.setIcon(getResources()
                        .getDrawable(R.drawable.marker_default_focused_base));
                currentLocationMarker.setPosition(point);
                currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                currentLocationMarker.setTitle(getResources().getString(R.string.my_location));
                overlays.add(currentLocationMarker);

                currentLocationRadius = new Polygon();
                currentLocationRadius.setPoints(Polygon.pointsAsCircle(point, accuracy));
                currentLocationRadius.setStrokeColor(RADIUS_BORDER_COLOR);
                currentLocationRadius.setFillColor(RADIUS_FILL_COLOR);
                overlays.add(currentLocationRadius);

                mapController.setCenter(point);
                mapController.setZoom(getZoomLevel(accuracy));
            }
        }

        private int getZoomLevel(final float radius) {
            final float scale = radius / 500;
            return (int) (17 - Math.log(scale) / Math.log(2));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View thisView = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) thisView.findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // default view point
        mapController = mapView.getController();
        final GeoPoint defaultPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setZoom(6);
        mapController.setCenter(defaultPoint);

        // add compass
        final CompassOverlay compassOverlay = new CompassOverlay(getContext(),
                new InternalCompassOrientationProvider(getContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        // add mapView scale
        final ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scaleBarOverlay.setScaleBarOffset(metrics.widthPixels / 2, 10);
        mapView.getOverlays().add(scaleBarOverlay);

        return thisView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(locationInfoReceiver,
                new IntentFilter(Constants.MAP_INTENT_FILTER_NAME));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(locationInfoReceiver);
    }

}