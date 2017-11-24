package com.mpho.instantweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class WeatherInfoActivity extends AppCompatActivity implements InternetVerifierBroadcastReceiver.ConnectivityReceiverListener{

    TextView txtv_location, txtv_temp, txtv_max_temp, txtv_min_temp, txtv_humidity;
    ImageView weather_icon_imgv;
    Double lat, lon;
    private String TAG = "WeatherInfoActivity: ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtv_location=(TextView) findViewById(R.id.txtv_location_value);
        txtv_temp=(TextView) findViewById(R.id.txtv_temp_value);
        txtv_max_temp=(TextView) findViewById(R.id.txtv_max_temp_value);
        txtv_min_temp=(TextView) findViewById(R.id.txtv_min_temp_value);
        txtv_humidity=(TextView) findViewById(R.id.txtv_humidity_value);
        weather_icon_imgv=(ImageView) findViewById(R.id.weather_icon_imageView);

        Bundle ext=getIntent().getExtras();
        if(ext != null){
            lat=ext.getDouble("lattitude");
            lon=ext.getDouble("longitude");
            Log.i(TAG,"creating Asynctask and calling JSONWeatherParse method");
            //create asynttask and pass the coordinates
            new JSONWeatherParse(lat, lon, weather_icon_imgv, txtv_location, txtv_temp, txtv_max_temp, txtv_min_temp, txtv_humidity).execute();

            Log.i(TAG,"JSONWeatherParse compeleted execution");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab == null) throw new AssertionError();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Please ensure that you are connected to the internet and you've turned your location settings on.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onResume(){
        Log.i(TAG,"application resumed");
        super.onResume();
        Log.i(TAG,"registering the connection status listener");
        WeatherApp.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(!isConnected) {

            //show a No Internet Alert or Dialog
            AlertDialog.Builder b=new AlertDialog.Builder(this);
            b.setMessage("Internet connection required to get location information");
            b.setCancelable(false);
            b.setTitle("Please switch connect to the internet");
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
