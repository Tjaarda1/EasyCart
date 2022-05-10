package com.example.easycart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Donde el usuario realiza compras
public class ShoppingCartActivity extends AppCompatActivity {

    ListView listViewData;
    ArrayAdapter<String> adapter;
    List<String> optionsArray;
    private EasyCartDbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        dbAdapter = new EasyCartDbAdapter(this);
        dbAdapter.open();

        optionsArray = Arrays.asList(dbAdapter.cursorToArray(dbAdapter.fetchAllItems(),"name"));
        optionsArray = removeDuplicates(optionsArray);
        listViewData = findViewById(R.id.listItemsCart);
        dbAdapter.close();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,
                optionsArray);
        listViewData.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.cart_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        ArrayList<String> userCart = new ArrayList<>();
        if(id == R.id.item_done){
            for(int i=0;i<listViewData.getCount();i++) {
                if (listViewData.isItemChecked(i)) {
                    userCart.add(listViewData.getItemAtPosition(i).toString());
                }
            }
            dbAdapter.open();
            String[] superMarketIdsArray = dbAdapter.cursorToArray(dbAdapter.fetchAllSupermarkets(),"_id");
            double lowestPrice = Double.MAX_VALUE;
            String lowestMarketId = "";
            for(String superMarketId : superMarketIdsArray){
                double cartPrice = cartPrice(superMarketId,userCart);
                if(lowestPrice > cartPrice){
                    lowestPrice = cartPrice;
                    lowestMarketId = superMarketId;
                }

            }
            Cursor cheapestMarketCursor = dbAdapter.fetchSupermarket(Long.parseLong(lowestMarketId));
            cheapestMarketCursor.moveToFirst();
            String supermarketChain = cheapestMarketCursor.getString(cheapestMarketCursor.getColumnIndexOrThrow("chain"));
            String marketStreet = cheapestMarketCursor.getString(cheapestMarketCursor.getColumnIndexOrThrow("street"));
            Toast.makeText(this,"Lowest price is in " + supermarketChain +
                    " on " + marketStreet + " with a cost of " + Math.round(lowestPrice*100.0)/100.0,Toast.LENGTH_LONG).show();

            dbAdapter.close();
        }
        return super.onOptionsItemSelected(item);
    }

    // Funcion que dado un super y un carro de compra te da el precio total de la comrpra
    public double cartPrice(String supermarketId, ArrayList<String> userCart){
        double totalCost = 0.0;
        for(String item : userCart){
            double itemCost = dbAdapter.fetchItemPrice(item,supermarketId);
            System.out.println(itemCost);
            totalCost += itemCost;
        }
        System.out.println("Super: " + supermarketId + " Precio: "+ totalCost);
        return totalCost;
    }

    // funcion que quita duplicados en los array. Sacada de internet
    private List<String> removeDuplicates(List<String> listWithDuplicates){
        // Create a new ArrayList
        ArrayList<String> newList = new ArrayList<String>();

        // Traverse through the first list
        for (String element : listWithDuplicates) {
            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }
        return newList;
    }
}