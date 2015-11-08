package org.feup.cmov.paintrain;

import android.app.Activity;
import android.content.Intent;
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
}
