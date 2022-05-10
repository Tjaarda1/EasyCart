//settings 2
package com.example.easycart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

// Guardar preferencias del usuario
public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new MyPreferenceFragment())
                .commit();

    }
    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings_layout, rootKey);

        }
    }
    public void savePref(View view) {

        // Creamos colecciÃ³n de preferencias
        String sharedPrefFile = "com.example.easycart.preferencesappmovprefs";
        SharedPreferences mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Obtenemos un editor de preferencias
        SharedPreferences.Editor editor = mPreferences.edit();

        // Obtenemos referencias a los elementos del interfaz grafico
        EditText RadioText = (EditText) findViewById(R.id.edit_message);

        // Guardamos el valor de la preferencia (podria ser commit en  versiones antiguas)
        editor.putString("RADIO", RadioText.getText().toString());
        editor.apply();


    }
}
