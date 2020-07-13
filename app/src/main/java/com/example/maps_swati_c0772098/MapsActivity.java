package com.example.maps_swati_c0772098;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private List<Marker> mMarkerList = new ArrayList<>();
    private Marker mPrevMarker;
    private List<Polyline> mPolylineList = new ArrayList<>();
    private Polygon mPolygon;
    private static final int POLYGON_SIDES = 4;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(43.650383, -79.363892 ),10f));
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this);
        Address address = null;
        String title ="";
        String snippet = "";
        try {
            address = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1).get(0);
            title = address.getSubThoroughfare() + " "+ address.getThoroughfare()+" "+address.getPostalCode();
            snippet = address.getLocality()+" "+address.getAdminArea();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title).snippet(snippet).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        Marker marker = mMap.addMarker(markerOptions);
        mMarkerList.add(marker);
        if(mPrevMarker != null)
        {
            PolylineOptions polylineOptions = new PolylineOptions().color(Color.RED).clickable(true);
            polylineOptions.add(marker.getPosition(),mPrevMarker.getPosition());
            mPolylineList.add(mMap.addPolyline(polylineOptions));
        }
        mPrevMarker = marker;

        if(mMarkerList.size() == POLYGON_SIDES)
        {
            PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.argb(89,0,255,0)).strokeColor(Color.RED).clickable(true);
            for(Marker marker1: mMarkerList)
            {
                polygonOptions.add(marker1.getPosition());
            }
            mPolygon = mMap.addPolygon(polygonOptions);
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        for (Marker marker : mMarkerList)
        {
            if (Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05)
            {
                marker.remove();
                mMarkerList.remove(marker);
                removePolyline(marker);
                removePolygon();
                break;
            }
        }
    }

    private void removePolygon()
    {
        if (mPolygon != null)
        {
            mPolygon.remove();
        }
        mPolygon = null;
    }

    private void removePolyline(Marker marker)
    {
        for (Polyline polyline : mPolylineList)
        {
            if (polyline.getPoints().get(0).equals(marker.getPosition()) || polyline.getPoints().get(1).equals(marker.getPosition()))
            {
                polyline.remove();
                mPolylineList.remove(polyline);
                removePolyline(marker);
                break;
            }
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        double distance = 0;
        double lat = 0, lng = 0;
        LatLng prevLatLng = null;
        for(LatLng latLng: polygon.getPoints())
        {
            if(prevLatLng!=null)
            {
                distance += distance(latLng.latitude,latLng.longitude,prevLatLng.latitude,prevLatLng.longitude);
                lat = (latLng.latitude + prevLatLng.latitude)/2;
                lng = (latLng.longitude + prevLatLng.longitude)/2;
            }
            prevLatLng = latLng;
        }
        System.out.println(lat + " "+lng);
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat,lng)).title(distance+" Km");
        if(mMarker != null)
        {
            mMarker.remove();
        }
        mMarker = mMap.addMarker(markerOptions);
        mMarker.showInfoWindow();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        LatLng place1 = polyline.getPoints().get(0);
        LatLng place2 = polyline.getPoints().get(1);
        double lat = (place1.latitude + place2.latitude)/2;
        double lng = (place1.longitude + place2.longitude)/2;
        double distance = distance(place1.latitude,place1.longitude,place2.latitude,place2.longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat,lng)).title(distance+" Km");
        if(mMarker != null)
        {
            mMarker.remove();
        }
        mMarker = mMap.addMarker(markerOptions);
        mMarker.showInfoWindow();
    }

    public double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private static double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }
        @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        for(Polyline polyline: mPolylineList)
        {
            polyline.remove();
        }
        mPolylineList.clear();
        if(mPolygon!=null)
        {
            mPolygon.remove();
        }
        Marker marker1=null;
        for(Marker marker2: mMarkerList)
        {
            if(marker1!=null)
            {
                PolylineOptions polylineOptions = new PolylineOptions().add(marker1.getPosition(),marker2.getPosition()).color(Color.RED).clickable(true);
                mPolylineList.add(mMap.addPolyline(polylineOptions));
            }
            marker1 = marker2;
        }
        if(mMarkerList.size() == POLYGON_SIDES)
        {
            PolygonOptions polygonOptions = new PolygonOptions().fillColor(Color.argb(89,0,255,0)).clickable(true).strokeColor(Color.RED);
            for(Marker marker2: mMarkerList)
            {
                polygonOptions.add(marker2.getPosition());
            }
            mPolygon = mMap.addPolygon(polygonOptions);
        }
    }

}