package com.example.davidemontagnob.myapplication;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Database_display_info extends AppCompatActivity implements OnItemClickListener, TextView.OnEditorActionListener{


    private Database_helper myDb;
    private EditText object_to_shop;
    private TextView txt_shop;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems=new ArrayList<>();
    private ListView db_list;
    private Cursor data;
    private ArrayList<String> selectedItem=new ArrayList<>(); //saved
    private ImageView add_object;
    private boolean select_all=false;
   android.support.v7.widget.Toolbar toolbar;

    private Button remove;
    /* PREFERENCE*/
    private SharedPreferences prefs;
    private int sex;






    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_database);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        txt_shop = findViewById(R.id.db_txt);
        toolbar.setTitle("Shopping Cart");
        remove = findViewById(R.id.action_remove_shop);
        setSupportActionBar(toolbar);
        myDb = new Database_helper(this);
        object_to_shop = findViewById(R.id.db_edit_text);
        db_list = findViewById(R.id.db_list);
        add_object = findViewById(R.id.ic_magnify);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

       data = myDb.getList();
       
        if(data.getCount() != 0){
            while(data.moveToNext()){
                listItems.add(data.getString(0).toString());


            }
        }
        db_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.db_row_layout, listItems);
        db_list.setAdapter(adapter);
        db_list.setOnItemClickListener(this);
        object_to_shop.setOnEditorActionListener(this);
        add_object.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
                object_to_shop.setText(null);
            }
        });


    }


    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this,"NFC intent received!", Toast.LENGTH_SHORT).show();

        super.onNewIntent(intent);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        String [] tmp2 = new String[selectedItem.size()];
        for(int j=0;j<selectedItem.size();j++){
            tmp2[j] = selectedItem.get(j).toString();
        }
        savedInstanceState.putStringArray("selectedItem",tmp2);
        savedInstanceState.putString("object_to_shop", object_to_shop.getText().toString());
        savedInstanceState.putBoolean("select_all",select_all);

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String [] tmp2 = savedInstanceState.getStringArray("selectedItem");

        for(int j=0;j<tmp2.length;j++){
            selectedItem.add(tmp2[j].toString());
        }
        object_to_shop.setText(savedInstanceState.getString("object_to_shop"));
        select_all = savedInstanceState.getBoolean("select_all");
    }


    @Override
    protected void onResume() {
        super.onResume();
        String sSetting = prefs.getString("sex","2").toString();
        if(sSetting!="")  sex =    Integer.parseInt(sSetting);

        switch(sex){
            case 0:
                txt_shop.setText("Sicuro di non aver dimenticato nulla?");
                break;
            case 1:
                txt_shop.setText("Sicura di non aver dimenticato nulla?");
                break;
            case 2:
                txt_shop.setText("Siamo sicuri che tu non abbia dimenticato nulla?");
                break;
        }
        if(adapter!=null) adapter.notifyDataSetChanged();
    }


    private Boolean insertData() {
        if(object_to_shop.getText().toString().equals("")){
            return false;
        }
        if(!listItems.contains(object_to_shop.getText().toString().toLowerCase())) {
            if (myDb.insertData(object_to_shop.getText().toString().toLowerCase()) == true) {
                listItems.add(object_to_shop.getText().toString().toLowerCase());


                Toast.makeText(Database_display_info.this, "Oggetto aggiunto correttamente", Toast.LENGTH_SHORT).show();
                object_to_shop.setHint("Inserisci l'oggetto da comprare");
                adapter.notifyDataSetChanged();
                return true;

            } else
                Toast.makeText(Database_display_info.this, "Abbiamo avuto un problema! Prova a reinserirlo!", Toast.LENGTH_SHORT).show();
        }
        else {

            object_to_shop.setHint("Inserisci l'oggetto da comprare");

            Toast.makeText(Database_display_info.this, "L'oggetto è già presente nel tuo carrello", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int res_id = item.getItemId();

        if(res_id ==R.id.action_send_nfc){
            Intent intent4 = new Intent(getApplicationContext(), NFCActivity.class);
            startActivity(intent4);

        }
        if(res_id == R.id.action_receive_nfc){
            Intent intent5 = new Intent(getApplicationContext(), NFCDisplayActivity.class);
            startActivity(intent5);
            finish();
        }
        if(res_id==R.id.action_remove_shop){
            if(selectedItem.isEmpty()){
                Toast.makeText(Database_display_info.this, "Nessun oggetto selezionato", Toast.LENGTH_SHORT).show();
                return true;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("TEST DMB");
            alert.setMessage("Sicuro di voler cancellare?");
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    for(String item2:selectedItem){
                        Integer deleted_rows = myDb.deleteData(item2.toLowerCase());
                        if(deleted_rows.intValue()!=0){

                            listItems.remove(item2);

                        }
                        else{
                            Toast.makeText(Database_display_info.this, "Problem delete: " + item2.toLowerCase(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    selectedItem.clear();
                    db_list.clearChoices();
                    Toast.makeText(Database_display_info.this, "Tutti gli oggetti selezionati sono stati rimossi correttamente dal carrello", Toast.LENGTH_LONG).show();

                    adapter.notifyDataSetChanged();

                }
            });
            alert.create().show();
        }


        if(res_id== R.id.action_select_all){
            if(listItems.isEmpty()){
                Toast.makeText(this,"Nessun elemento da selezionare", Toast.LENGTH_SHORT).show();
            }
            if (select_all == false) {
                for(int i=0;i<listItems.size();i++){

                    selectedItem.add(listItems.get(i).toString().toLowerCase());
                    db_list.setItemChecked(i,true);
                    select_all=true;
                    Toast.makeText(this,"Tutti gli oggetti sono stati selezionati " ,Toast.LENGTH_SHORT).show();
                    item.setIcon(R.drawable.deselect_all);
                }
            }
            else{
                selectedItem.clear();
                db_list.clearChoices();
                select_all=false;
                Toast.makeText(this,"Tutti gli oggetti sono stati deselezionati " ,Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.select_all);
            }


        }
        adapter.notifyDataSetChanged();
        return true;

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item_tap = ((TextView) view).getText().toString();
        if(selectedItem.contains(item_tap)){
            selectedItem.remove(item_tap);

        }
        else{
            selectedItem.add(item_tap);

        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                || event.getAction() == KeyEvent.KEYCODE_ENTER
                || event.getAction() == KeyEvent.ACTION_UP
                || event.getAction() == EditorInfo.IME_ACTION_GO){
            Boolean respons = insertData();
            object_to_shop.setText("");
            return respons;


        }

        return false;
    }





}
