package com.example.easycart;

import android.annotation.SuppressLint;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easycart.EasyCartDbAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


// Actividad para editar un supermercado y añadir items
// además de emplear servicios camara y ml de google
public class EditActivity extends AppCompatActivity {

    // Supermarket fields initialization
    private EditText mChainText;
    private EditText mStreetText;
    private EditText mPostalCodeText;
    private EditText mCityText;
    private Long mRowId;
    private EasyCartDbAdapter dbAdapter;
    private ListView m_listview;

     // código de acción para lanzar un Intent que solicite una captura
     private static final int CODIGO_HACER_FOTO = 100;

     // ubicación de la imagen tomada
     private File ubicacion = null;

    // Clave para no perder la ruta en caso de destrucción de la activity
    private final static String CLAVE_RUTA_IMAGEN = "CLAVE_RUTA_IMAGEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_edit);

         // Si targetSdkVersion > 23 hay que poner lo siguiente
        // https://stackoverflow.com/questions/42251634/android-os-fileuriexposedexception-file-jpg-exposed-beyond-app-through-clipdata
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // Reference to the layout views
        mChainText = (EditText) findViewById(R.id.chain);
        mStreetText = (EditText) findViewById(R.id.street);
        mPostalCodeText = (EditText) findViewById(R.id.postalCode);
        mCityText = (EditText) findViewById(R.id.city);
        Button confirmButton = (Button) findViewById(R.id.confirm);

        // new Easy Cart DB Adapter instance
        dbAdapter = new EasyCartDbAdapter(this);
        dbAdapter.open();

        // We get the id of the supermarket selected
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(EasyCartDbAdapter.KEY_ROW_ID_1);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(EasyCartDbAdapter.KEY_ROW_ID_1) : null;
        }

        // If id is not null we fill the layout views
        if (mRowId != null) {
            Cursor supermarket = dbAdapter.fetchSupermarket(mRowId);
            mChainText.setText(supermarket.getString(
                    supermarket.getColumnIndexOrThrow(EasyCartDbAdapter.KEY_CHAIN)));
            mStreetText.setText(supermarket.getString(
                    supermarket.getColumnIndexOrThrow(EasyCartDbAdapter.KEY_STREET)));
            mPostalCodeText.setText(supermarket.getString(
                    supermarket.getColumnIndexOrThrow(EasyCartDbAdapter.KEY_POSTAL_CODE)));
            mCityText.setText(supermarket.getString(
                    supermarket.getColumnIndexOrThrow(EasyCartDbAdapter.KEY_CITY)));
        }

        // List with all the items of this selected supermarket
        m_listview = (ListView) findViewById(R.id.supermarket_list_view_id);
        fillData();

        Button addItemsButton = (Button) findViewById(R.id.AddItemBtn);

        addItemsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /**
                 * Query Example :
                 * INSERT INTO item (name, fullName,price, supermarketId)
                 * VALUES ('Leche','Leche entera', '1', '1');
                 */


                // Set up layout for the dialog box
                LayoutInflater myLayout = LayoutInflater.from(v.getContext());
                final View dialogView = myLayout.inflate(R.layout.add_item_dialog, null);

                // Build dialog box
                AlertDialog.Builder ad = new AlertDialog.Builder(v.getContext());
                ad.setTitle(R.string.title_add_items_dialog);
                ad.setView(dialogView);
                ad.setPositiveButton(R.string.confirmButton,
                        new DialogInterface.OnClickListener() {

                            @SuppressLint("ResourceType")
                            public void onClick(DialogInterface dialog, int arg1) {
/**
                                System.out.println(dialogView.getContext().getText(R.id.name));

                                saveItem(dialog);

*/
                                EditText fullName = dialogView.findViewById(R.id.fullName);
                                String fullNameValue = fullName.getText().toString();
                                EditText name = dialogView.findViewById(R.id.name);
                                String nameValue = name.getText().toString();
                                EditText price = dialogView.findViewById(R.id.price);
                                String priceValue = price.getText().toString();

                                saveItem(fullNameValue,nameValue,priceValue);
                                finish();
                                getIntent().putExtra(EasyCartDbAdapter.KEY_ROW_ID_1, mRowId);
                                startActivity(getIntent());

                            }
                        });
                ad.show();

               /** Button confirmItemBtn = (Button) findViewById(R.id.confirmItemBtn);
                confirmItemBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        saveItem(v);
                    }
                });*/


            }
        });

    }

    private void fillData() {
        if(mRowId != null){
            Cursor itemsCursor = dbAdapter.fetchAllSupermarketItems(mRowId.toString());

            // Array with the item name and price to show on the list of items in supermarket
            String[] from = new String[]{EasyCartDbAdapter.KEY_NAME, EasyCartDbAdapter.KEY_PRICE};

            // we link the texts with the text views
            int[] to = new int[]{R.id.text2, R.id.text3};

            SimpleCursorAdapter items =
                    new SimpleCursorAdapter(this, R.layout.items_row, itemsCursor, from, to, 0);
            m_listview.setAdapter(items);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            if (mRowId != null) {
                dbAdapter.deleteSupermarket(mRowId);
            }
            setResult(RESULT_OK);
            dbAdapter.close();
            finish();
        }
        if (id == R.id.action_photo) {
            // solicitamos específicamente la captura de una imagen
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // creamos una ruta en la que guardar la imagen
            // y damos esta ubicación a la app que se encargue de hacerla

            ubicacion = obtenerUbicacionImagen(); // objeto File

            // nuestro método obtenerUbicacionImagen() puede devolver null si el
            // sistema de almacenamiento externo no está disponible, por lo que
            // dependemos de que no sea así para poder iniciar la captura
            if (ubicacion != null) {

                Uri photoURI = FileProvider.getUriForFile(this, "com.example.easycart.provider", ubicacion);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, CODIGO_HACER_FOTO);

            } else {
                Toast.makeText(this, "No se puede acceder al sistema de archivos",
                        Toast.LENGTH_LONG).show();
            }

        }

        if (id == R.id.action_about) {
            System.out.println("APPMOV: About action...");
        }

        return super.onOptionsItemSelected(item);
    }


    public void saveSupermarket(View view) {
        String chain = mChainText.getText().toString();
        String street = mStreetText.getText().toString();
        String postalCode = mPostalCodeText.getText().toString();
        String city = mCityText.getText().toString();
        if (mRowId == null) {
            long id = dbAdapter.createSupermarket(chain, street, postalCode, city);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            dbAdapter.updateSupermarket(mRowId, chain, street, postalCode, city);
        }
        setResult(RESULT_OK);
        dbAdapter.close();
        finish();
    }

    public void saveItem(String name, String fullName, String price) {
        if(mRowId == null)
            mRowId = dbAdapter.createSupermarket(mChainText.getText().toString(),
                    mStreetText.getText().toString(), mPostalCodeText.getText().toString(),
                    mCityText.getText().toString());

        long supermarketId = mRowId;


        long id = dbAdapter.createItem(name, fullName, price, supermarketId);

        setResult(RESULT_OK);
        dbAdapter.close();
    }

     /**Metodos para la cámara*/
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         if (ubicacion != null) {
             savedInstanceState.putString(CLAVE_RUTA_IMAGEN, ubicacion.getPath());
         }
         super.onSaveInstanceState(savedInstanceState);
     }
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         if (savedInstanceState.containsKey(CLAVE_RUTA_IMAGEN)) {
             ubicacion = new File(savedInstanceState.getString(CLAVE_RUTA_IMAGEN));
         }
         super.onRestoreInstanceState(savedInstanceState);
     }
     /** Creamos un objeto File (que no es otra cosa que una ruta) para guardar ahí la foto */
    private File obtenerUbicacionImagen() {

        // ====>>> Se recomienda utilizar almacenamiento EXTERNO
        //         siempre que se guarden archivos MULTIMEDIA

        File directorioExterno;


        // IMPORTANTE: Primero comprobamos que el almacenamiento externo está disponible para escritura.
        // Ver posibles estados en:
        // http://developer.android.com/reference/android/os/Environment.html#getExternalStorageState()
        if (! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        // ====>>> Puedes consultar las distintas opciones de acceso a ficheros a través de la
        //         clase Environment:
        //         http://developer.android.com/reference/android/os/Environment.html

        // Usaremos el directorio PRIVADO de la aplicación ubicado en almacenamiento externo,
        // que se borra al desinstalarla

        directorioExterno = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Si aún no existe el directorio, lo creamos
        if (! directorioExterno.exists()){
            if (! directorioExterno.mkdirs()){
                Log.d("HelloCamera", "no se puede crear el directorio");
                return null;
            }
        }

        // Creamos un nombre de fichero dentro de ese directorio
        // (es buena idea utilizar un timestamp para evitar sobreescribir imágenes)
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Devolvemos el objeto File
        return new File(directorioExterno.getPath() + File.separator +
                "IMG_"+ timestamp + ".jpg");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("onActivityResult", "requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + (data == null ? "null" : "not null"));
        if (requestCode == CODIGO_HACER_FOTO) {
            if (resultCode != RESULT_CANCELED) {
                // La imagen ha sido capturada y grabada en la ubicación
                // que se señaló en el Intent
                TextView miTexto = findViewById(R.id.texto);
                ImageView miImagen = findViewById(R.id.imagen);
                Bitmap miBitmap = BitmapFactory.decodeFile(ubicacion.getPath());

                miImagen.setImageBitmap(miBitmap);

                // Initialize the text recognizer
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                //Take the bitmap as input image
                InputImage image = InputImage.fromBitmap(miBitmap, 0);

                Task<Text> result =
                        recognizer.process(image)
                                .addOnSuccessListener(new OnSuccessListener<Text>() {
                                    @Override
                                    public void onSuccess(Text visionText) {
                                        List<Text.TextBlock> textBlocks =  visionText.getTextBlocks();
                                        int index = 0;
                                        dbAdapter.open();
                                        while(textBlocks.size() > index){
                                            System.out.println(textBlocks.get(index));
                                            String[] marketItem = textBlocks.get(index).getText().split(" ");
                                            dbAdapter.createItem(marketItem[0],marketItem[0],marketItem[1],mRowId);
                                            index++;
                                        }
                                        dbAdapter.close();
                                        finish();
                                        getIntent().putExtra(EasyCartDbAdapter.KEY_ROW_ID_1, mRowId);
                                        startActivity(getIntent());

                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                System.err.println("vNklT error message: " +e.getMessage());
                                            }
                                        });
            }
        }
    }

}