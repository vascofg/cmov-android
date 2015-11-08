package org.feup.cmov.paintrain;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;


public class NFCReceiveActivity extends Activity {

    TextView mTextView;

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_receive_activity);
        mTextView = (TextView) findViewById(R.id.nfc_text);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            mTextView.setText(new String(message.getRecords()[0].getPayload()));

        } else
            mTextView.setText("Waiting for NDEF Message");
    }
}