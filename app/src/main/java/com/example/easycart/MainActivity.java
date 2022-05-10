package com.example.easycart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /* Los 3 botones de la aplicación principal con sus respectivos intens */

        Button boton3 = findViewById(R.id.boton3);
        boton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shoppingCartActivity = new Intent(MainActivity.this, ShoppingCartActivity.class);
                startActivity(shoppingCartActivity);
            }
        });
        ImageButton foto = findViewById(R.id.addMarket);
        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fotoActivity = new Intent(MainActivity.this, CreateSupermarketActivity.class);
                startActivity(fotoActivity);
            }
        });

    }

    /**Metodos para crear el menu y que aparezca por pantalla*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Se recrea el menu que aparece en ActionBar de la actividad.
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
        if (id == R.id.action_settings) {
            // Creamos el Intent que va a lanzar la segunda activity (SecondActivity)
            Intent settings = new Intent(this, SettingsActivity.class);
            // Iniciamos la nueva actividad
            startActivity(settings);

            return true;
        }

        if (id == R.id.action_about) {
            System.out.println("APPMOV: About action...");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


