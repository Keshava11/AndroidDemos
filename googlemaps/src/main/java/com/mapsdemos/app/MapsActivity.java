package com.mapsdemos.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<MarkerOptions> mMarkerOptionsList = new ArrayList<>();
    private List<Marker> mMarkerList = new ArrayList<>();
    private int mUpdateCtr =0;
    private Paint mMarkerContainerPaint = new Paint();
    private Paint mMarkerTextPaint = new Paint();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Paint for Container
        mMarkerContainerPaint.setColor(Color.LTGRAY);
        mMarkerContainerPaint.setStrokeWidth(2.0f);

        // Paint for Text content
        mMarkerTextPaint.setColor(Color.GREEN);
        mMarkerTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mMarkerTextPaint.setStrokeWidth(3.0f);
        mMarkerTextPaint.setTextSize(30.0f);


        mMarkerOptionsList.add(new MarkerOptions().position(new LatLng(0, 0)).title("Marker 1").
                icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap("Marker 1"))));
        mMarkerOptionsList.add(new MarkerOptions().position(new LatLng(57, 0)).title("Hello")
        .icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap("Hello"))));
        mMarkerOptionsList.add(new MarkerOptions().position(new LatLng(76, 89)).title("56.3, 23.4 75%")
        .icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap("56.3, 23.4 75%"))));
        mMarkerOptionsList.add(new MarkerOptions().position(new LatLng(24, 23)).title("Marker 4")
        .icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap("Marker 4"))));
        mMarkerOptionsList.add(new MarkerOptions().position(new LatLng(34, 10)).title("76.3, 23.4 75%")
        .icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap("76.3, 23.4 75%"))));


        setUpMapIfNeeded();
    }

    public void updateMapContent(View iView) {
        mUpdateCtr++;
        for (int i = 0; i < mMarkerList.size(); i++) {
            Marker marker = mMarkerList.get(i);
            marker.setTitle("Marker " + (i + 1) + ". \n Updated " + mUpdateCtr + " time");
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap("Marker " + (i + 1) + ". \n" +
                    "  Updated " + mUpdateCtr + " time")));

            // updated window
            if (marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        for (MarkerOptions markerOptions : mMarkerOptionsList) {
            mMarkerList.add(mMap.addMarker(markerOptions));
        }
    }

    /**
     *
     * @param iMarkerText
     * @return
     */
    private Bitmap createMarkerBitmap(String iMarkerText) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(200, 100, conf);
        Canvas canvas = new Canvas(bmp);

        canvas.drawRect(0,75,200,175, mMarkerContainerPaint);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.purple_marker),100,25,mMarkerTextPaint);
        canvas.drawText(iMarkerText, 50, 100, mMarkerTextPaint);

        return bmp;
    }


}
