package com.example.easycart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

// Adapter class for the db
public class EasyCartDbAdapter {
    private static final String TAG = "APPMOV: DbAdapter";

    //We define the database name and the two tables that are on it
    private static final String DATABASE_NAME = "easyCart";
    private static final String DATABASE_SUPERMARKET_TABLE = "supermarket";
    private static final String DATABASE_ITEM_TABLE = "item";
    private static final int DATABASE_VERSION = 2;

    //Supermarket table fields
    public static final String KEY_CHAIN = "chain";
    public static final String KEY_STREET = "street";
    public static final String KEY_POSTAL_CODE = "postalCode";
    public static final String KEY_CITY = "city";
    public static final String KEY_ROW_ID_1 = "_id";

    //Item table fields
    public static final String KEY_NAME = "name";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_PRICE = "price";
    public static final String KEY_FOREGIN_KEY = "supermarketId";
    public static final String KEY_ROW_ID_2 = "_id";

    // SQL Querys for both tables
    private static final String SUPERMARKET_TABLE_QUERY = "create table " + DATABASE_SUPERMARKET_TABLE + " (" +
            KEY_ROW_ID_1 +" integer primary key autoincrement, " +
            KEY_CHAIN +" text not null, " +
            KEY_POSTAL_CODE +" text not null, " +
            KEY_CITY +" text not null, " +
            KEY_STREET + " text not null);";

    private static final String ITEM_TABLE_QUERY = "create table " + DATABASE_ITEM_TABLE + " (" +
            KEY_ROW_ID_2 +" integer primary key autoincrement, " +
            KEY_NAME +" text not null, " +
            KEY_FULL_NAME +" text, " +
            KEY_PRICE +" integer, " +
            KEY_FOREGIN_KEY + " integer," +
            "FOREIGN KEY(" + KEY_FOREGIN_KEY +") references " +  DATABASE_SUPERMARKET_TABLE + "("+ KEY_FOREGIN_KEY +"));";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SUPERMARKET_TABLE_QUERY);
            db.execSQL(ITEM_TABLE_QUERY);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_SUPERMARKET_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_ITEM_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */

    public EasyCartDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the easy cart database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public EasyCartDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new supermarket using the parameters provided. If the supermarket is
     * successfully created return the new rowId for that supermarket, otherwise return
     * a -1 to indicate failure.
     *
     * @param chain chain of the supermarket
     * @param street the street where the supermarket is
     * @param postalCode the postalCode of the supermarket
     * @param city the city where the supermarket is
     * @return rowId or -1 if failed
     */
    public long createSupermarket(String chain, String street, String postalCode, String city) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CHAIN, chain);
        initialValues.put(KEY_STREET, street);
        initialValues.put(KEY_POSTAL_CODE, postalCode);
        initialValues.put(KEY_CITY, city);

        return mDb.insert(DATABASE_SUPERMARKET_TABLE, null, initialValues);
    }

    /**
     * Delete the supermarket with the given rowId
     *
     * @param rowId id of supermarket to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteSupermarket(long rowId) {

        return mDb.delete(DATABASE_SUPERMARKET_TABLE, KEY_ROW_ID_1 + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all supermarkets in the database
     *
     * @return Cursor over all supermarkets
     */
    public Cursor fetchAllSupermarkets() {

        return mDb.query(DATABASE_SUPERMARKET_TABLE, new String[] {KEY_ROW_ID_1, KEY_CHAIN,
                        KEY_STREET, KEY_POSTAL_CODE, KEY_CITY}, null, null,
                null, null, null);
    }

    /**
     * Return a Cursor positioned at the supermarket that matches the given rowId
     *
     * @param rowId id of supermarlet to retrieve
     * @return Cursor positioned to matching supermarket, if found
     * @throws SQLException if supermarket could not be found/retrieved
     */
    public Cursor fetchSupermarket(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_SUPERMARKET_TABLE, new String[] {KEY_ROW_ID_1, KEY_CHAIN,
                                KEY_STREET, KEY_POSTAL_CODE, KEY_CITY}, KEY_ROW_ID_1 + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public boolean supermarketAlreadyInDatabase(String street){
        Cursor mCursor = mDb.rawQuery("SELECT chain FROM supermarket WHERE street LIKE ? ", new String[] {"%" + street + "%"});
        return mCursor.getCount() > 0;
    }

    /**
     * Update the supermarket using the details provided. The supermarket to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of supermarket to update
     * @param chain chain of the supermarket
     * @param street the street where the supermarket is
     * @param postalCode the postalCode of the supermarket
     * @param city the city where the supermarket is
     * @return true if the supermarket was successfully updated, false otherwise
     */
    public boolean updateSupermarket(long rowId, String chain, String street, String postalCode, String city) {
        ContentValues args = new ContentValues();
        args.put(KEY_CHAIN, chain);
        args.put(KEY_STREET, street);
        args.put(KEY_POSTAL_CODE, postalCode);
        args.put(KEY_CITY, city);

        return mDb.update(DATABASE_SUPERMARKET_TABLE, args, KEY_ROW_ID_1 + "=" + rowId, null) > 0;
    }


    // ----------------- SUPERMARKET ITEM MANAGEMENT ------------------------------------------------
    /**
     * Create a new item using the parameters provided. If the item is
     * successfully created return the new rowId for that item, otherwise return
     * a -1 to indicate failure.
     *
     * @param name general item name (example: tomatoes)
     * @param fullName specific name of the item (example: green tomatoes)
     * @param price item's price
     * @param supermarketId id of the supermarket where the item is located
     * @return rowId or -1 if failed
     */
    public long createItem(String name, String fullName, String price, Long supermarketId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_FULL_NAME, fullName);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_FOREGIN_KEY, supermarketId);

        return mDb.insert(DATABASE_ITEM_TABLE, null, initialValues);
    }


    /**
     * Delete the item with the given rowId
     *
     * @param rowId id of item to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteItem(long rowId) {

        return mDb.delete(DATABASE_ITEM_TABLE, KEY_ROW_ID_2 + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all supermarket items in the database
     *
     * @return Cursor over all items
     */
    public Cursor fetchAllItems() {

        return mDb.query(DATABASE_ITEM_TABLE, new String[] {KEY_ROW_ID_2, KEY_NAME,
                        KEY_FULL_NAME, KEY_PRICE, KEY_FOREGIN_KEY}, null, null,
                null, null, null);
    }

    public Cursor fetchAllItemNames(){
        return mDb.rawQuery("SELECT ? FROM item", new String[] {"name"});
    }

    /**
     * Return a Cursor over the list of all supermarket items in the database
     *
     * @return Cursor over all items
     */
    public Cursor fetchAllSupermarketItems(String supermarket_id) {
        String whereClause = KEY_FOREGIN_KEY + " = " + supermarket_id;
        return mDb.query(DATABASE_ITEM_TABLE, new String[] {KEY_ROW_ID_2, KEY_NAME,
                        KEY_FULL_NAME, KEY_PRICE}, whereClause, null,
                null, null, null);
    }

    /**
     * Return a Cursor positioned at the item that matches the given rowId
     *
     * @param rowId id of item to retrieve
     * @return Cursor positioned to matching item, if found
     * @throws SQLException if item could not be found/retrieved
     */
    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_ITEM_TABLE, new String[] {KEY_ROW_ID_2, KEY_NAME,
                                KEY_FULL_NAME, KEY_PRICE, KEY_FOREGIN_KEY},KEY_ROW_ID_2 + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    // funcion que dado un item y un super devuelve el precio de ese item en el super.
    // Si no tiene ese super el item, se devuelve una version mas cara del item.
    public double fetchItemPrice(String item, String supermarketId){
        Cursor mCursor = mDb.query(DATABASE_ITEM_TABLE, new String[] {KEY_PRICE},"name =? AND superMarketId=?",
                new String[] {item, supermarketId}, null, null, null);
        if(mCursor.getCount() == 0 ){
            return getAveragePrice(item) + 0.99;
        }
        mCursor.moveToFirst();
        return Double.parseDouble(mCursor.getString(0));
    }

    /**
     * Update the item using the details provided. The item to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of item to update
     * @param name general item name (example: tomatoes)
     * @param fullName specific name of the item (example: green tomatoes)
     * @param price item's price
     * @param supermarketId id of the supermarket where the item is located
     * @return true if the supermarket was successfully updated, false otherwise
     */
    public boolean updateItem(long rowId, String name, String fullName, String price, Integer supermarketId) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_FULL_NAME, fullName);
        args.put(KEY_PRICE, price);
        args.put(KEY_FOREGIN_KEY, supermarketId);

        return mDb.update(DATABASE_ITEM_TABLE, args, KEY_ROW_ID_2 + "=" + rowId, null) > 0;
    }

    // Funcion sacada de internet. Pasa un objeto cursor a un array de strings
    public String[] cursorToArray(Cursor cursor, String columnName) {

        ArrayList<String> arrayList = new ArrayList<>();
        int columnIndex = cursor.getColumnIndexOrThrow(columnName);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
               arrayList.add(cursor.getString(columnIndex));
        }
            if(arrayList.isEmpty())
                return new String[]{};
            return arrayList.toArray(new String[0]);
    }

    // Da precio medio de un item
    private double getAveragePrice(String item){
        double sumPrice = 0.0;
        Cursor mCursor = mDb.query(DATABASE_ITEM_TABLE, new String[] {KEY_PRICE},"name =?",
                new String[] {item}, null, null, null);
        String[] priceList = cursorToArray(mCursor, "price");
        for(String price : priceList){
            sumPrice += Double.parseDouble(price);
        }
        return sumPrice/mCursor.getCount();
    }

}
