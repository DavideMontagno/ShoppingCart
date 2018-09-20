package com.example.davidemontagnob.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Davide Montagno B on 27/08/2018.
 */

public class Database_helper extends SQLiteOpenHelper {

    /* variabili per la gestione del database*/
    private static final String DATABASE_NAME = "test.db";
    //tabella del database - contiene n colonne => n variabili.
    private static final String TABLE_NAME = "table_test";
    private static final String id = "ID";
    private static final String COL_1 = "NAME";
    private static final String TABLE_NAME2 = "table_test2";
    private static final String id2 = "ID";
    private static final String COL_2 = "OBJECT";
    private static final String COL_3 = "SHOP";




    public Database_helper(Context context){
        super(context, DATABASE_NAME,null,1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creazione di una nuova tabella per il database.
        db.execSQL("create table " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT unique)");
        db.execSQL("create table " + TABLE_NAME2 + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, OBJECT TEXT, SHOP TEXT)" );



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //drop table se esiste.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME2);
        onCreate(db);
    }

    public boolean isIn(SQLiteDatabase db, String object){
        Cursor rows = db.rawQuery("SELECT NAME FROM "+TABLE_NAME+" WHERE NAME = ? ", new String[] {""+object});
        if(rows.getCount()>0) return true;
        else return false;
    }

    public boolean insertData(String object){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,object);
        if(db.insertOrThrow(TABLE_NAME,null,contentValues)!= -1){

            return true;
        }

        else {  return false;}



    }

    public boolean insertData2(String object, String shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,object);
        contentValues.put(COL_3,shop);
        if(db.insert(TABLE_NAME2,null,contentValues)!= -1){

            return true;
        }

        else {  return false;}

    }


    public void updateData() {
        deleteAllData();

        new DownloadFileFromURL().execute("https://api.myjson.com/bins/1ff3zs");

    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        StringBuilder response = new StringBuilder();
        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            Log.d("Download", "I'm starting");
            try {
                URL url = new URL(f_url[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.d("Download", "First line");
                InputStream inputStream = httpURLConnection.getInputStream();
                Log.d("Download", "Second line");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                Log.d("Download", "Third line");
                String line = "";
                String data = "";
                while(line != null){
                    line = bufferedReader.readLine();
                    data = data + line;
                }
                Log.d("Download", "Finish retreive data");
                JSONArray JA = new JSONArray(data);
                for(int i =0 ;i <JA.length(); i++){
                    JSONObject JO = (JSONObject) JA.get(i);


                   insertData2(JO.get("OBJECT").toString(),JO.getString("SHOP"));


                }
                Log.d("Download",  data);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }



            /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {

        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {

            Log.d("ok",response.toString());
        }
    }



    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_NAME2);
    }

    public Integer deleteData(String object){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "NAME = ?",new String[] {object.toLowerCase()});


    }

    public Cursor getList(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT NAME FROM " + TABLE_NAME, null);
        return data;
    }
    public Cursor getList2(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = db.rawQuery("SELECT "+COL_2+","+COL_3+" FROM " + TABLE_NAME2, null);
        return data;
    }



}
