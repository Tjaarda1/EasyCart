package com.example.easycart;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class CreateSupermarketActivity extends AppCompatActivity {

    // indicar API KEY para el API de tipo "browser" de Google Places
    final String GOOGLE_KEY = "AIzaSyCzCTW12esVTLiQLRPeByB0H831o_WXuoI";

    // Radio de búsqueda
    // Recuperamos la informacion salvada en la preferencia
    /**String sharedPrefFile = "com.uc3m.it.preferencesappmovprefs";
    SharedPreferences mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
    String name = mPreferences.getString("RADIO", "No name defined yet!");

    final String radius = name;
*/
    String radius="2000";

    // Tipo de establecimiento (ver API Google Places)
    final String type = "supermarket";

    // para gestionar localizacion del usuario
    protected LocationManager locationManager;
    private EasyCartLocationListener locationListener;

    // gestionar base de datos
    private EasyCartDbAdapter dbAdapter;
    private ListView m_listview;

    // New supermarket or edit existing supermarket
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_supermarket);

        //Vemos donde esta el usuario
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new EasyCartLocationListener();
        
        long minTimeMs = 0; //milisegundos
        //Distancia mínima entre escuchas de nueva posición
        float minDistance = 0; //metros
        System.out.println(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION));

        //Abrimos una instancia que busca donde el usuario esta en cada momento
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistance, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeMs, minDistance, locationListener);


        // Si el usuario tiene permisos quitados, se le avisa
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(CreateSupermarketActivity.this, "Not Enough Permission", Toast.LENGTH_SHORT).show();

            return;
        }

        // New db adapter instance
        dbAdapter = new EasyCartDbAdapter(this);
        dbAdapter.open();

        // list view with all supermarkets. If you click on one you can edit its info
        m_listview = (ListView) findViewById(R.id.id_list_view);
        m_listview.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view, int position, long id)
                    {
                        Intent i = new Intent(view.getContext(), EditActivity.class);
                        i.putExtra(EasyCartDbAdapter.KEY_ROW_ID_1, id);
                        startActivityForResult(i, ACTIVITY_EDIT);
                    }
                }
        );

        // Boton que ejecuta la busqueda de supermercados cercanos
        Button supermarketButton = (Button) findViewById(R.id.LocateSupermarketsBtn);

        supermarketButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new GooglePlaces().execute();

            }
        });

        // fill the list view with all supermarkets
        fillData();
    }

    // funcion que llena la lista de supermercados mirando en la base de datos
    private void fillData() {
        Cursor supermarketsCursor = dbAdapter.fetchAllSupermarkets();

        // Array with the columns of out list
        String[] from = new String[]{EasyCartDbAdapter.KEY_CHAIN,EasyCartDbAdapter.KEY_STREET};

        // for the moment we will only show the supermarket's chain
        int[] to = new int[]{R.id.text1};

        // Show it with SimpleCursorAdapter
        SimpleCursorAdapter supermarkets =
                new SimpleCursorAdapter(this, R.layout.supermarkets_row, supermarketsCursor, from, to, 0);
        m_listview.setAdapter(supermarkets);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_supermarket_db, menu);
        return true;
    }

    // Menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // crear supermercado manualmente
        if (id == R.id.action_insert) {
            createSupermarket();
            return true;
        }

        if (id == R.id.action_about) {
            System.out.println("APPMOV: About action...");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // funcion que nos lleva a crear supermercado manualmente
    private void createSupermarket() {
        Intent i = new Intent(this, EditActivity.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }

    // clase que gestiona la busqueda de supermercados basado en localizacion y preferencias del usuario
    private class GooglePlaces extends AsyncTask<View, Void, ArrayList<GooglePlace>> {

        @Override
        protected ArrayList<GooglePlace> doInBackground(View... urls) {
            ArrayList<GooglePlace> temp;
            //print the call in the console
            Location userLocation = locationListener.getLocation();
            // make Call to the url
            temp = makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + userLocation.getLatitude() + "," + userLocation.getLongitude() + "&radius=" + radius + "&type=" + type + "&sensor=true&key=" + GOOGLE_KEY);

            return temp;
        }


        // Cuando el get nos haya llegado y tengamos ya el response, creamos los supermercados para meter
        // los en la db
        @Override
        protected void onPostExecute(ArrayList<GooglePlace> result) {

            for (GooglePlace place : result) {
                System.out.println(place);
                // make a list of the venus that are loaded in the list.
                // show the name, the category and the city
                if(!dbAdapter.supermarketAlreadyInDatabase(place.getStreet()))
                    dbAdapter.createSupermarket(place.getName(),place.getStreet(),"",place.getCity());
            }
            finish();
            startActivity(getIntent());
    }
    }

    public static ArrayList<GooglePlace> makeCall(String stringURL) {

        URL url = null;
        BufferedInputStream is = null;
        JsonReader jsonReader;
        ArrayList<GooglePlace> temp = new ArrayList<GooglePlace>();

        try {
            url = new URL(stringURL);
        } catch (Exception ex) {
            System.out.println("Malformed URL");
        }

        try {
            if (url != null) {
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                is = new BufferedInputStream(urlConnection.getInputStream());
            }
        } catch (IOException ioe) {
            System.out.println("IOException");
        }

        if (is != null) {
            try {
                jsonReader = new JsonReader(new InputStreamReader(is, "UTF-8"));
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    String name = jsonReader.nextName();
                    // Busca la cadena "results"
                    if (name.equals("results")) {
                        // comienza un array de objetos
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            GooglePlace poi = new GooglePlace();
                            jsonReader.beginObject();
                            // comienza un objeto
                            while (jsonReader.hasNext()) {
                                name = jsonReader.nextName();
                                if (name.equals("name")) {
                                    // si clave "name" guarda el valor
                                    poi.setName(jsonReader.nextString());
                                    System.out.println("PLACE NAME:" + poi.getName());
                                } else if (name.equals("geometry")) {
                                    // Si clave "geometry" empieza un objeto
                                    jsonReader.beginObject();
                                    while (jsonReader.hasNext()) {
                                        name = jsonReader.nextName();
                                        if (name.equals("location")) {
                                            // dentro de "geometry", si clave "location" empieza un objeto
                                            jsonReader.beginObject();
                                            while (jsonReader.hasNext()) {
                                                name = jsonReader.nextName();
                                                // se queda con los valores de "lat" y "long" de ese objeto
                                                if (name.equals("lat")) {
                                                    poi.setLatitude(jsonReader.nextString());
                                                } else if (name.equals("lng")) {
                                                    poi.setLongitude(jsonReader.nextString());
                                                } else {
                                                    jsonReader.skipValue();
                                                }
                                            }
                                            jsonReader.endObject();
                                        } else {
                                            jsonReader.skipValue();
                                        }
                                    }
                                    jsonReader.endObject();
                                }else if (name.equals("vicinity")) {
                                    // We take the supermarkets street
                                    int index = 0;
                                    String[] vicinity = jsonReader.nextString().split(",");
                                    if(vicinity.length == 4 )
                                        index = 1;
                                    if(vicinity.length == 2)
                                        poi.setStreet(vicinity[index--]);
                                    else
                                        poi.setStreet(vicinity[index] + ", " + vicinity[index + 1 ]);
                                    poi.setCity(vicinity[index + 2]);
                                }else{
                                    jsonReader.skipValue();
                                }
                            }
                            jsonReader.endObject();
                            temp.add(poi);
                        }
                        jsonReader.endArray();
                    } else {
                        jsonReader.skipValue();
                    }
                }
                jsonReader.endObject();
            } catch (Exception e) {
                System.out.println("Exception");
                return new ArrayList<GooglePlace>();
            }
        }

        return temp;}

}