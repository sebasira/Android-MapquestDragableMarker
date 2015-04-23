package com.sebasira.mapquestdragablemarker;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapquest.android.maps.DefaultItemizedOverlay;
import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.Overlay;
import com.mapquest.android.maps.OverlayItem;

import java.util.Locale;

/**
* MapFragment
*/
public class MapFragment extends Fragment {
    // TAG
    private static final String TAG = "MapFragment";

    // Mapquest Map View
    private MapView map;

    // Default locations
    private GeoPoint defaultPoint = new GeoPoint(-32.9476917, -60.6304694);

/************************************************************************************/
/** LIFE CYLCE **/

	/* ON CREATE */
	/* ********* */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /* ON CREATE VIEW */
	/* ************** */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        // Setup the map
        map = (MapView) rootView.findViewById(R.id.map);
        map.getController().setZoom(17);
        map.getController().setCenter(defaultPoint);
        map.setBuiltInZoomControls(true);

        // Adding default Markers Overlays
        Drawable icon = getResources().getDrawable(R.drawable.location_marker);
        DefaultItemizedOverlay marker_overlay = new DefaultItemizedOverlay(icon);

        OverlayItem defaultItem = new OverlayItem(defaultPoint,"Tittle","Snippet");
        marker_overlay.addItem(defaultItem);

        map.getOverlays().add(marker_overlay);

        map.invalidate();                           // Re-Draw the map

        return rootView;
    }


    /* ON RESUME */
    /* ********* */
    @Override
    public void onResume() {
        super.onResume();
    }


    /* ON PAUSE */
    /* ******** */
    @Override
    public void onPause() {
        super.onPause();
    }

    /* ON DESTROY */
	/* ********** */
    @Override
    public void onDestroy() {
        // Possible solution to some leaks
        // http://developer.mapquest.com/web/products/forums/-/message_boards/view_message/744551
        if (null != map){
            map.destroy();
        }

        super.onDestroy();
    }
}