package com.mpho.instantweather;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, InternetVerifierBroadcastReceiver.ConnectivityReceiverListener {

    Button view_weather, exit;
    TextView txtv_lat, txtv_lon;
    private GoogleApiClient google_api_client;
    private Location user_location;
    private String TAG = "MainActivity: ";
    private LocationRequest location_request;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private int PERMISSION_REQUEST_LOCATION = 200;
    private final int REQUEST_CHECK_SETTINGS = 100;
    private boolean requesting_location_updates=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        view_weather = (Button) findViewById(R.id.btn_view_weather);
        exit = (Button) findViewById(R.id.btn_exit);
        txtv_lat = (TextView) findViewById(R.id.txtv_lat_value);
        txtv_lon = (TextView) findViewById(R.id.txtv_lon_value);
        txtv_lon.setVisibility(View.INVISIBLE);
        txtv_lat.setVisibility(View.INVISIBLE);

        view_weather.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                if(user_location!=null) {
                    String lattiude_txt = "lattitude";
                    String longitude_txt="longitude";
                    Intent weather_details_intent = new Intent(MainActivity.this, WeatherInfoActivity.class);
                    weather_details_intent.putExtra(lattiude_txt, user_location.getLatitude());
                    weather_details_intent.putExtra(longitude_txt,user_location.getLatitude());
                    startActivity(weather_details_intent);
                }
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Please ensure that you are connected to the internet and you've turned your location settings on.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }
        Log.i(TAG, "calling updateValuesFromBundle method");
        updateValuesFromBundle(savedInstanceState);

    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {

        Log.i(TAG, "UpdateValuesFromBundle method");
        if (savedInstanceState != null) {

            //Update the value of requesting_location_updates with value from previous app state
            String LOCATION_UPDATES_ENABLED = "requesting_location_updates";
            if (savedInstanceState.keySet().contains(LOCATION_UPDATES_ENABLED)) {
                requesting_location_updates = savedInstanceState.getBoolean(LOCATION_UPDATES_ENABLED);
                if (requesting_location_updates) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            Log.i(TAG, "savedinstancestate not null, initialising with saved state");
            String LOCATION_KEY = "current_location";
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, this indicates that
                // mCurrentLocationis not null.
                user_location = savedInstanceState.getParcelable(LOCATION_KEY);
                handleNewLocation(user_location);
            }
        } else {
            Log.i(TAG, "creating location request");
            //set interval and fastest rate at which the location updates will be received in milliseconds
            location_request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000).setFastestInterval(5000);
            if (google_api_client == null) {
                Log.i(TAG, "initialising google api client");
                google_api_client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API).build();
            }

            changeLocationSettings();
            getLocation();
        }
    }

    private void changeLocationSettings() {
        //create object to get the location settings of a user's device
        LocationSettingsRequest.Builder location_s_builder = new LocationSettingsRequest.Builder().addLocationRequest(location_request);

        //Check if the user device meets the required application settings
        PendingResult<LocationSettingsResult> settings_result = LocationServices.SettingsApi.checkLocationSettings(google_api_client, location_s_builder.build());

        //check the results object to determine if the user device are have enabled the location settings
        settings_result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status locationSettingsRes_status = locationSettingsResult.getStatus();
                //final LocationSettingsStates locationSettingsRes_states = locationSettingsResult.getLocationSettingsStates();
                switch (locationSettingsRes_status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied.
                        getLocation();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //location settings are not satisfied but user can fix, show user dialog to prompt them to fix settings
                        try {
                            //show dialogue by calling startResolutionForResult()
                            locationSettingsRes_status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //settings are not satisfied but there is no way to fix them
                        Toast.makeText(getApplicationContext(), "Settings not available on the device", Toast.LENGTH_LONG).show();

                        break;

                }
            }
        });
    }

    private void handleNewLocation(Location new_user_location) {
        Log.i(TAG,"handleNewLocation method, set locationn to the new location object if any");
        user_location=new_user_location;
        Log.i(TAG,"get the coordinates info and date info");
        Double lattitude = user_location.getLatitude();
        Double longitude = user_location.getLongitude();
        Log.i(TAG,"display the values of the new location cordinates on the TextViews");
        txtv_lat.setText(String.valueOf(lattitude));
        txtv_lon.setText(String.valueOf(longitude));
        txtv_lon.setVisibility(View.VISIBLE);
        txtv_lat.setVisibility(View.VISIBLE);
    }

    private void stopLocationUpdates() {

        Log.i(TAG, "removing location updates, disconnecting google api client");
        LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client, this);
    }

    private void startLocationUpdates() {
        Log.i(TAG, "requesting location updates");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            /*
            TODO: Consider calling
            ActivityCompat#requestPermissions
            here to request the missing permissions, and then overriding
            public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults)
            to handle the case where the user grants the permission. See the documentation
            for ActivityCompat#requestPermissions for more details.
            */
            Log.i(TAG, "permissions denied, requesting permissions");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(google_api_client, location_request, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //populate the location objct and the UI
            Log.i(TAG, "location settings permission granted, launches getLocation method");
            getLocation();
        } else {
            Log.i(TAG, "location settings permission denied");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.i(TAG, "explain why it is important for the user to turn on the location settings");
                    //add alert dialog
                    AlertDialog.Builder b=new AlertDialog.Builder(this);
                    b.setMessage("To receive relevant location info for the app, you have to allow app to access your location. Please turn your location settings on");
                    b.setTitle("Location Settings");
                    b.setPositiveButton("OK", new DialogInterface.OnClickListener(

                    ) {
                        /**
                         * This method will be invoked when a button in the dialog is clicked.
                         *
                         * @param dialog The dialog that received the click.
                         * @param which  The button that was clicked (e.g.
                         *               {@link DialogInterface#BUTTON1}) or the position
                         */
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                        }
                    });
                    b.setNegativeButton("EXIT",new DialogInterface.OnClickListener(

                    ) {
                        /**
                         * This method will be invoked when a button in the dialog is clicked.
                         *
                         * @param dialog The dialog that received the click.
                         * @param which  The button that was clicked (e.g.
                         *               {@link DialogInterface#BUTTON1}) or the position
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    } );
                    b.show();
                }
            } else {
                Log.i(TAG, "requests settings permission again");
                Toast.makeText(getApplicationContext(), "Settings not available on the device, Application exits", Toast.LENGTH_LONG).show();
                System.exit(0);
            }
        }
    }

    private void getLocation() {
        Log.i(TAG, "getLocation method");
        Log.i(TAG, "permissions granted, checking the last location recorded on user device");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            }
            return;
        }
        user_location = FusedLocationApi.getLastLocation(google_api_client);
        if (user_location != null) {
            Log.i(TAG,"last location found, calling handleNewLocation");
            handleNewLocation(user_location);
        } else {
            Log.i(TAG,"last location not available, requesting location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(google_api_client, location_request, this);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG,"starting app");

        google_api_client.connect();
    }
    @Override
    public void onStop() {
        Log.i(TAG,"application stopping");
        super.onStop();
        google_api_client.disconnect();
    }
    @Override
    protected void onResume(){
        Log.i(TAG,"application resumed");
        super.onResume();
        Log.i(TAG,"registering the connection status listener");
        WeatherApp.getInstance().setConnectivityListener(this);
        Log.i(TAG,"starting location updates");
        if(google_api_client.isConnected() && !requesting_location_updates){
            startLocationUpdates();
            requesting_location_updates=true;
        }
    }
    @Override
    protected void onPause(){
        Log.i(TAG,"application paused");
        super.onPause();
        if(google_api_client.isConnected()){
            stopLocationUpdates();
            requesting_location_updates=false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG,"Connection established, lanches getLocationmethod");
        if(requesting_location_updates){
            startLocationUpdates();
        }
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG,"Connection Suspended");
        Toast.makeText(getApplicationContext(), "Connection Suspended", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG,"Entering the OnConnectionFailed method");

        if(connectionResult.hasResolution()){
            try{
                Log.i(TAG,"launches startResolutionForResult method");
                connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }catch (IntentSender.SendIntentException e){
                Log.e(TAG,e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(!isConnected) {

            //show a No Internet Alert or Dialog
            AlertDialog.Builder b=new AlertDialog.Builder(this);
            b.setMessage("Internet connection required to get location information");
            b.setTitle("Please switch connect to the internet");
            b.setCancelable(false);
            b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                /**
                 * This method will be invoked when a button in the dialog is clicked.
                 *
                 * @param dialog The dialog that received the click.
                 * @param which  The button that was clicked (e.g.
                 *               {@link DialogInterface#BUTTON1}) or the position
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setClassName("com.android.phone", "com.android.phone.NetworkSetting");
                    startActivity(intent);
                }
            });
            b.setNegativeButton("EXIT",new DialogInterface.OnClickListener(

            ) {
                /**
                 * This method will be invoked when a button in the dialog is clicked.
                 *
                 * @param dialog The dialog that received the click.
                 * @param which  The button that was clicked (e.g.
                 *               {@link DialogInterface#BUTTON1}) or the position
                 */
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            } );
            b.show();

        }else{
            // dismiss the dialog or refresh the activity
            return;
        }
    }
}
