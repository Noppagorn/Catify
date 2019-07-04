package com.nongmung.catify

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.security.Permissions
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent



class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?) = true//{
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        if (p0.equals(myMarker)) {
//            val intent = Intent(this@MarkerDemoActivity, AnotherActivity::class.java)
//            startActivity()
//        }
//    }
    //override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

//        Permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        Permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        permissionsToRequest = permissionsToRequest(permissions);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (permissionsToRequest.size() > 0) {
//                requestPermissions(permissionsToRequest.
//                    toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
//            }
//        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
//        googleMap.setOnMarkerClickListener(this);
//
//        myMarker = googleMap.addMarker(new MarkerOptions()
//            .position(latLng)
//            .title("My Spot")
//            .snippet("This is my spot!")
//            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val myPlace = LatLng(40.73, -73.99)  // this is New York
        map.addMarker(MarkerOptions().position(myPlace).title("My Favorite City"))
        map.moveCamera(CameraUpdateFactory.newLatLng(myPlace))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12.0f))

        map.getUiSettings().setZoomControlsEnabled(true)
        map.setOnMarkerClickListener(this)
        setUpMap()
    }
}
