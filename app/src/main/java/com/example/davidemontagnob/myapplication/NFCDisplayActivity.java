package com.example.davidemontagnob.myapplication;



import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

/**
 * Created by mshrestha on 7/23/2014.
 */
public class NFCDisplayActivity extends Activity {

    TextView text;
    private Database_helper myDb;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcdisplay);
        myDb = new Database_helper(this);
        text = findViewById(R.id.nfc_txt_receive);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String received = new String(message.getRecords()[0].getPayload());

            String[] separated = received.split("\n");
            SQLiteDatabase db = myDb.getReadableDatabase();
            for(int i=0; i<separated.length;i++){
                if(!myDb.isIn(db,separated[i]))
                myDb.insertData(separated[i]);
            }
          text.setText("I dati sono arrivati correttamente! Torna indietro per visualizzarli");
        }


    }



}