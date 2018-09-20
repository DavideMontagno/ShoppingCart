package com.example.davidemontagnob.myapplication;

import android.app.Activity;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class NFCActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {

    private Database_helper myDb;
    private NfcAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        myDb = new Database_helper(this);
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            finish();
            return;
        }

        if (!mAdapter.isEnabled()) {
            Toast.makeText(this, "NFC è disabilitato, per favore abilitalo!", Toast.LENGTH_LONG).show();
        }

        mAdapter.setNdefPushMessageCallback(this, this);

    }

    /**
     * Ndef Record that will be sent over via NFC
     * @param nfcEvent
     * @return
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {

        Cursor data = myDb.getList();
        String database="";
        if(data.getCount() != 0){
            while(data.moveToNext()){
               database+= data.getString(0).toString()+"\n";


            }
        }
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", database.getBytes());
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);

        return ndefMessage;
    }



}