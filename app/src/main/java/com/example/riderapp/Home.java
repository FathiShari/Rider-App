package com.example.riderapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.riderapp.Common.Common;
import com.example.riderapp.Helper.custominfowindow;
import com.example.riderapp.Model.FCMResponse;
import com.example.riderapp.Model.Notification;
import com.example.riderapp.Model.Rider;
import com.example.riderapp.Model.Sender;
import com.example.riderapp.Model.Token;
import com.example.riderapp.RetrofitController.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener  {

    SupportMapFragment mapFragment;
    //Location
    private GoogleMap mMap;
    private static final int my_permission_request_code = 7000;
    private static final int play_service_request_code = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mApiCLient;
    private Location mLastLocation;

    private int update_interval = 5000;
    private int fastest_interval = 3000;
    private int displacement = 10;

    DatabaseReference drivers;
    GeoFire mgeoFire;
    Marker mUserMarker;

    String mPlaceLocation,mPlaceDstination;

    //bottomsheet
    ImageView imageView;
    BottomSheetRiderFragment bottomSheetRiderFragment;
    Button btnPickUpRequest;

    //placesApi
    PlacesClient placesClient;
    AutocompleteSupportFragment location,destination;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG);

    //find near driver within 3 km
    boolean isDriverFound = false;
    String driverId = "";
    int radius = 1; //1km
    int distance = 1;
    int limit = 15;//3km

    //Presence System
    DatabaseReference driversAvailable;

    //Alert Driver
    IFCMService ifcmService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //map
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Alert driver
        ifcmService = Common.getfcmService();

        //Places Api
        initPlaces();

        //Init view
        imageView = (ImageView)findViewById(R.id.imageExpandable);
        location = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.place_location);
        destination = (AutocompleteSupportFragment)getSupportFragmentManager().findFragmentById(R.id.place_destination);
        btnPickUpRequest = (Button)findViewById(R.id.btnPickUpRequest);

        location.setPlaceFields(placeFields);
        destination.setPlaceFields(placeFields);

        location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mPlaceLocation = place.getAddress().toString();
                // Remove old Marker
                mMap.clear();
                //Add marker at new Location
                mUserMarker = mMap.addMarker(new MarkerOptions()
                        .title("Pickup Here")
                        .position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker()));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Home.this, "" + status.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mPlaceDstination = place.getAddress().toString();

                mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

                //show info in bottom
                bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance(mPlaceLocation,mPlaceDstination);
                bottomSheetRiderFragment.show(getSupportFragmentManager(),bottomSheetRiderFragment.getTag());
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        btnPickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDriverFound)
                    requestPickUpHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(driverId);
            }
        });

        setUpLocation();
        updateFirebaseToken();
    }

    private void updateFirebaseToken() {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_table);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(Common.token_table);

        db.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){

                    Token tokens = postSnapShot.getValue(Token.class);//get  object of a database with key

                    String json_lat_lang = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                    String riderToken = FirebaseInstanceId.getInstance().getToken();

                    Notification data = new Notification(riderToken,json_lat_lang);//send it to driver app and we deserialize it
                    Sender sender = new Sender(data,tokens.getToken());//sent it to token

                    ifcmService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success ==  1)
                                Toast.makeText(Home.this,"Message Sent",Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(Home.this,"Failed",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Toast.makeText(Home.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                            Log.d("Courier",t.getMessage());

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Home.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestPickUpHere(String uid) {

        DatabaseReference dbrequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_table);
        GeoFire mg = new GeoFire(dbrequest);
        mg.setLocation(uid,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        if(mUserMarker.isVisible())
            mUserMarker.remove();

        mUserMarker= mMap.addMarker(new MarkerOptions().title("Pickup Here")
            .snippet("")
            .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mUserMarker.showInfoWindow();
        btnPickUpRequest.setText("Getting your Driver.....");
        findDriver();
    }

    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.diver_location_table);
        GeoFire mg = new GeoFire(drivers);

        GeoQuery geoQuery = mg.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if found
                if(!isDriverFound){
                    isDriverFound = true;
                    driverId = key;
                    btnPickUpRequest.setText("Pickup Request");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if driver still not found,increase distnce
                if(!isDriverFound && radius < limit){
                    radius++;
                    findDriver();
                }
                else{
                    Toast.makeText(Home.this,"No available Drivers" ,Toast.LENGTH_SHORT).show();
                    btnPickUpRequest.setText("Pickup Request");
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){

            case my_permission_request_code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (checkPlayServices()){

                        buildGoogleApiClient();
                        createLocationequest();
                        displayLocation();
                    }
                }
        }
    }

    private void setUpLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {

            // Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            }, my_permission_request_code);
        }
        else{
            if (checkPlayServices()){

                buildGoogleApiClient();
                createLocationequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        mLastLocation = FusedLocationApi.getLastLocation(mApiCLient);
        if(mLastLocation != null){

            //Presence Stystem
            driversAvailable = FirebaseDatabase.getInstance().getReference(Common.diver_location_table);
            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // if we have any changes on driver location table,we load all available drivers
                    LoadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final double latitude = mLastLocation.getLatitude();
                final  double longtude = mLastLocation.getLongitude();

                        //Add a maker
                        if(mUserMarker != null)
                            mUserMarker.remove();
                        mUserMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude,longtude))
                                .title("Your Location"));
                        //Move camera to this position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longtude),15.0f));
                        LoadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

            Log.d("CourierDebug",String.format("your location was changed: %f %f",latitude,longtude));
            }else
            {
                Log.d("Error","Cannot get your Location");
            }

        }

    private void LoadAllAvailableDriver(final LatLng location) {
        //First we need to delete all markers on Map(include our location and drivers
        mMap.clear();

        //Next add our location
        mMap.addMarker(new MarkerOptions().position(location)
                .title("You"));

        //LoadAll available drivers
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.diver_location_table);
        GeoFire mg = new GeoFire(drivers);

        GeoQuery geoQuery = mg.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                // use key to get email from table driver
                FirebaseDatabase.getInstance().getReference(Common.driver_table_info).child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //rider amd user model same here
                                //therefore we use ridr model here

                                Rider rider = dataSnapshot.getValue(Rider.class);
                                //Add driver to map
                                mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude,location.longitude))
                                        .flat(true)
                                        .snippet(rider.getName())
                                        .title(rider.getPhoneNo())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carimage)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance <= limit){
                    distance++;
                    LoadAllAvailableDriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createLocationequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(update_interval);
        mLocationRequest.setFastestInterval(fastest_interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(displacement);
    }

    private void buildGoogleApiClient() {
        mApiCLient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mApiCLient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,play_service_request_code).show();
            else {
                Toast.makeText(this,"This Device is not Supported",Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void initPlaces() {
        Places.initialize(this,getString(R.string.google_maps_api));
        placesClient =  Places.createClient(this);
    }

@Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_trips) {

        } else if (id == R.id.nav_change_password) {
            changePassword();

        } else if (id == R.id.nav_sign_out) {
           signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changePassword() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Enter Password to change");

        LayoutInflater inflater = LayoutInflater.from(Home.this);
        View layout_change_password = inflater.inflate(R.layout.layout_change_password,null);

        final EditText currentPass = (EditText)layout_change_password.findViewById(R.id.etPassword);
        final EditText newPassword = (EditText)layout_change_password.findViewById(R.id.etNewPassword);
        final EditText confirmPass = (EditText)layout_change_password.findViewById(R.id.etConfirmPassword);

        alertDialog.setView(layout_change_password);

        alertDialog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(newPassword.getText().toString().equals(confirmPass.getText().toString()))
                {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    AuthCredential auth = EmailAuthProvider.getCredential(email,currentPass.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(auth).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()){
                               FirebaseAuth.getInstance().getCurrentUser()
                                       .updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful()){

                                           Map<String, Object> password = new HashMap<>();
                                           password.put("password",confirmPass.getText().toString());

                                           DatabaseReference driverInfo = FirebaseDatabase.getInstance().getReference(Common.driver_table_info);
                                           driverInfo.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                   .updateChildren(password)
                                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if (task.isSuccessful())
                                                               Toast.makeText(Home.this,"Password Updated",Toast.LENGTH_SHORT).show();
                                                            else
                                                               Toast.makeText(Home.this,"Password Changed Not Updated",Toast.LENGTH_SHORT).show();
                                                       }
                                                   });

                                       }
                                       else
                                       {
                                           Toast.makeText(Home.this,"Password Cannot Change",Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });
                           }
                        }
                    });


                }else{
                    Toast.makeText(Home.this, "Password Does not math", Toast.LENGTH_SHORT).show();

                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(Home.this, UserInterface.class));
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new custominfowindow(this));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != (PackageManager.PERMISSION_GRANTED)) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != (PackageManager.PERMISSION_GRANTED))) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiCLient, mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    mApiCLient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
            mLastLocation = location;
            displayLocation();
    }
}
