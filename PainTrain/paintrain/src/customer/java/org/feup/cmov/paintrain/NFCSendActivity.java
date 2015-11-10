package org.feup.cmov.paintrain;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;

/**
 * Created by vascofg on 08-11-2015.
 */
public class NFCSendActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    public static final int RC_NFC = 4;

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String message = getIntent().getStringExtra("NFC_DATA");
        byte[] mimeType = getResources().getString(R.string.nfc_mimeType).getBytes();
        NdefRecord ndefRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                mimeType, null, message.getBytes());
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[]{ndefRecord});
        return ndefMessage;
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_send_activity);

        NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mAdapter==null || !mAdapter.isEnabled()) {
            finish();
        } else {
            mAdapter.setNdefPushMessageCallback(this, this);
        }

    }

}
