package com.sebasira.mapquestdragablemarker;


import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mapquest.android.maps.GeoPoint;
import com.mapquest.android.maps.ItemizedOverlay;
import com.mapquest.android.maps.MapView;
import com.mapquest.android.maps.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
* MapFragment
*/
public class MapFragment extends Fragment {
    // TAG
    private static final String TAG = "MapFragment";

    // Mapquest Map View
    private MapView map;

    // Default location
    private GeoPoint defaultPoint = new GeoPoint(-32.9476917, -60.6304694);

    // ImageView that would act as marker been dragged
    private ImageView dragImage = null;

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

        // Adding draggable overlay
        Drawable icon = getResources().getDrawable(R.drawable.location_marker);
        dragImage = (ImageView) rootView.findViewById(R.id.drag);
        map.getOverlays().add(new DraggableOverlay(icon));

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


/************************************************************************************/
/** DRAGGABLE OVERLAY **/

    public class DraggableOverlay extends ItemizedOverlay<OverlayItem> {
        private List<OverlayItem> items = new ArrayList<OverlayItem>();
        private Drawable marker = null;
        private OverlayItem inDrag = null;

        /** DragImageOffset are the values halfWidth and fullHeight used to position the image.
         * When positioning an image (setting margins) the easiest way is to set a LEFT and TOP margin.
         * But the coordinates we are going to use to position it are relatives to BOTTOM and CENTER,
         * because the marker is boundCenterBottom.
         * So we need to adjust this margin in order to correctly position the ImageView where the
         * marker was. Therefore there's an offset on the image's margins. Those offsets are:
         *      - for Left Margin: half width to the left (to be in the middle) so this value should be subtracted
         *      - for Top Margin: full height to the top (to be in the bottom) so this value should be subtracted
         */
        private int xDragImageOffset = 0;
        private int yDragImageOffset = 0;

        private int xDragTouchOffset = 0;
        private int yDragTouchOffset = 0;

        /* CONSTRUCTOR */
        /* *********** */
        public DraggableOverlay(Drawable marker) {
            super(marker);
            this.marker = marker;


            xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth()/2;
            yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

            items.add(new OverlayItem(defaultPoint,"Title", "Snippet"));

            // Populates ItemizedOverlay's internal list. Subclass must provide number of items that
            // must be populate by implementing size(). Each item in the list populated by calling createItem(int).
            populate();
        }


        /* CREATE ITEM */
        /* *********** */
        /** Required method when extending ItemizedOverlay. Returns the item at the given index.
         *
         * @param i index
         * @return item at given index
         */
        @Override
        protected OverlayItem createItem(int i) {
            return(items.get(i));
        }


        /* DRAW */
        /* **** */
        /** Required method when extending ItemizedOverlay. If not present, markers won't be
         * drawn int the map
         */
        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, shadow);

            boundCenterBottom(marker);
        }


        /* SIZE */
        /* **** */
        /** Required method when extending ItemizedOverlay. Returns the number of items in the overlay.
         *
         * @return number of items in the overlay
         */
        @Override
        public int size() {
            return(items.size());
        }


        /* ON TOUCH EVENT */
        /* ************** */
        /** Handles the touch events for this overlay. This is how we're gone make it draggable
         *
         * @param event type of touch event (MotionEvent) DOWN, MOVE, UP, etc
         * @param mapView mapview where the event ocurr
         * @return TRUE to stop propagation of the event or FALSE to pass the event handler
         */
        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
            final int action  = event.getAction();      // Type of Action
            final int x = (int)event.getX();            // X coordinate where the event happened
            final int y = (int)event.getY();            // Y coordinate where the event happened

            // DOWN
            if (action == MotionEvent.ACTION_DOWN) {
                // Sweep all items on the overlay list to test if touch event happened over one
                // of them
                for (OverlayItem item : items) {
                    Point p = new Point();                              // Creates an Android screen point
                    map.getProjection().toPixels(item.getPoint(), p);   // Set point coordinates to be item's coordinates on screen

                    // Check if event (DOWN) happened over an item (marker)
                    if (hitTest(item, marker, x - p.x, y - p.y)) {
                        inDrag = item;                                  // Item being dragged
                        items.remove(inDrag);                           // Remove this dragged item from item list
                        populate();                                     // Update ItemizedOverlay internal list

                        // As the Touch Event may have happened around the point but not exactly
                        // on it we need to consider this little offset.
                        // Depending on your needs the may not be necessary
                        // This offset is an initial value that won't be changed
                        xDragTouchOffset = x - p.x;
                        yDragTouchOffset = y - p.y;

                        // Now the marker was removed from the list, so it won't exist and won't be shown.
                        // So what we do is to show an ImageView with the same picture as the location
                        // marker, starting from the exact same place,  to make user believe he
                        // is dragging that marker. But that's only an illusion
                        setDragImagePosition(x, y);                     // Set ImageView in the same place the marker was
                        dragImage.setVisibility(View.VISIBLE);          // Show this ImageView

                        break;                                          // Exit FOR, don't keep sweeping
                    }

                    // If dragging and item, the stop progation of tocuh event => return true
                    if (inDrag != null) {
                        return true;
                    }
                }

            // MOVE
            }else if (action == MotionEvent.ACTION_MOVE) {
                // Only move the ImageView if an item is being dragged
                if (inDrag != null) {
                    // Change position of imageView accordly to movement
                    setDragImagePosition(x, y);

                    // If dragging and item, the stop progation of tocuh event => return true
                    return true;
                }

            // UP
            }else if (action == MotionEvent.ACTION_UP) {
                // Only process UP event if an item is being dragged
                if (inDrag != null) {
                    dragImage.setVisibility(View.GONE);     // Hide the ImageView

                    // Now create a GeoPoint from where the UP occurs, create an OverlayItem from
                    // it and add it to the item list so it will be drawn on the map
                    GeoPoint pt = map.getProjection().fromPixels(x - xDragTouchOffset,y - yDragTouchOffset);
                    OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),inDrag.getSnippet());
                    items.add(toDrop);
                    populate();
                    map.postInvalidate();                   // Re-draw the map

                    inDrag = null;                          // There's not an item been dragged anymore

                    // Stop progation of tocuh event => return true
                    return true;
                }
            }

            // Return false to pass the handler of the Event
            return false;
        }

        /* SET DRAG IMAGE POSITION */
        /* *********************** */
        /** Sets the ImageView position on screen. This ImageView is the one that creates the illusion
         * of dragging the marker.
         *
         * @param x X coordinate on screen
         * @param y Y coordinate on screen
         */
        private void setDragImagePosition(int x, int y) {
            RelativeLayout.LayoutParams lp= (RelativeLayout.LayoutParams) dragImage.getLayoutParams();

            // To positioning a View (an ImageView ins this case) on the screen we need to set its margins.
            // We are going to set LEFT and TOP margins, and to correctly positioning the image
            // some offsets need to be taken in account.
            lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y - yDragImageOffset - yDragTouchOffset, 0, 0);
            dragImage.setLayoutParams(lp);
        }
    }
}