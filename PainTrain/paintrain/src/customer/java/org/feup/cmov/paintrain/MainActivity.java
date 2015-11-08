package org.feup.cmov.paintrain;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * Created by vascofg on 31-10-2015.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    public void launchAuth(View view) {
        startActivity(new Intent(this, AuthActivity.class));
    }

    public void launchBuyTickets(View view) {
        startActivity(new Intent(this, BuyTicketsActivity.class));
    }

    public void launchTimetable(View view) {
        startActivity(new Intent(this, TimetableActivity.class));
    }

    public void launchNFC(View view) {
        startActivity(new Intent(this, NFCSendActivity.class));
    }

    public void showQRCode(View view) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
            intent.putExtra("ENCODE_DATA", "The Pain Train!");
            intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
            intent.putExtra("ENCODE_SHOW_CONTENTS", false);

            startActivityForResult(intent, 0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);

        }
    }
}
