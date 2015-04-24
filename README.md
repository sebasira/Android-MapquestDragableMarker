# Android-MapquestDragableMarker

This is a sample code showing how to drag a marker over the mapView using Mapquest Android SDK v1.0.5

This is accomplished by creating a CustomOverlay placing the markers on it, and using its onTouchEvent. Actually what is being dragged is not the marker itself but an ImageView acting as the marker.
When the user release this ImageView a marker is placed, making this illusion happen.

This is not fully my creation, the idea came from !(CommonsWare)[https://github.com/commonsguy/cw-advandroid/blob/master/Maps/NooYawkTouch/src/com/commonsware/android/maptouch/NooYawk.java]  