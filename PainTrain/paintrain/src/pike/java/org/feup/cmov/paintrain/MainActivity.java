package org.feup.cmov.paintrain;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TOKEN_KEY = "TOKEN"; //name of token key in shared preferences
    private static final String TAG = "MainActivity";
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    private static final int RC_SCAN = 1;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private String mToken;
    private List<String> mTrips;
    private List<String> mStations;
    private JSONArray mTripsData;
    private Spinner current_trip, current_station;
    private Button mButton;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current_trip = (Spinner) findViewById(R.id.current_trip);
        current_station = (Spinner) findViewById(R.id.current_station);

        mButton = (Button) findViewById(R.id.scan_qrcode_button);

        current_trip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                //percorre times
                try {
                    JSONArray times = mTripsData.getJSONObject(i).getJSONArray("times");
                    mStations = new ArrayList<String>(times.length());

                    for (int j = 0; j < times.length(); j++) {
                        mStations.add(times.getJSONObject(j).getString("station"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mStations);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    current_station.setAdapter(adapter);

                    mButton.setEnabled(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mButton.setEnabled(false);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.base_url) + "/timetable";

        // Request a JSON response from the provided URL.
        JsonObjectRequest jsonObjectRequest = new InsecureJsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.d(TAG, jsonObject.toString());
                        try {
                            if (jsonObject != null) {
                                JSONArray data = jsonObject.getJSONArray("data");
                                mTripsData = data;
                                mTrips = new ArrayList<String>(data.length());
                                for (int i = 0; i < data.length(); i++) {
                                    JSONArray trips = data.getJSONObject(i).getJSONArray("times");
                                    JSONObject departureObj = trips.getJSONObject(0);
                                    JSONObject arrivalObj = trips.getJSONObject(trips.length() - 1);

                                    String departureStation = departureObj.getString("station");
                                    String departureTime = departureObj.getString("time");

                                    String arrivalStation = arrivalObj.getString("station");
                                    String arrivalTime = arrivalObj.getString("time");

                                    mTrips.add(departureTime + " " + departureStation + "->" + arrivalTime + " " + arrivalStation);
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mTrips);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                current_trip.setAdapter(adapter);

                                findViewById(R.id.progress_bar).setVisibility(View.INVISIBLE);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, this);
// Add the request to the RequestQueue.
        findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        queue.add(jsonObjectRequest);


        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        mToken = settings.getString(TOKEN_KEY, null);

        if (mToken == null) {
            connectAndAuth();
        } else
            findViewById(R.id.main_linear_layout).setVisibility(View.VISIBLE);

        //TODO: send ticket status on end of trip
    }

    public void readQRCode(View view) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            intent.putExtra("RESULT_DISPLAY_DURATION_MS", 0L);

            startActivityForResult(intent, RC_SCAN);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);

        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.

        String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);

        new GetIdTokenTask().execute(accountName);

        mShouldResolve = false;


        mProgress = ProgressDialog.show(this, "Connecting",
                "Authenticating with server", true);

        // Show the signed-in UI
        //showSignedInUI();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                connectionFailed("Error connecting to Google API");
            }
        } else {
            // Show the signed-out UI
            //showSignedOutUI();
        }
    }

    /* called after choosing account */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            connectAndAuth();
        } else if (requestCode == RC_SCAN) {
            //QR CODE SCAN
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                validateTicket(contents);
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Scan canceled");
            }
        }
    }

    private void validateTicket(String ticket) {
        Log.d(TAG, ticket);
        try {
            JSONObject ticketObj = new JSONObject(ticket);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void authWithServer(String token) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getResources().getString(R.string.base_url) + "/auth";
        JSONObject jo = new JSONObject();

        try {
            jo.put("googleCredentials", token);
            //jo.put("pike", getResources().getString(R.string.role).equals("Pike"));
            jo.put("pike", false);
            JsonObjectRequest jsonObjectRequest = new InsecureJsonObjectRequest(Request.Method.POST, url, jo, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    connectionSuccess();
                }
            }, this);
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            connectionFailed(e.toString());
        }
    }

    private void connectionSuccess() {
        Log.d(TAG, "Authed with server");
        Toast.makeText(getBaseContext(), R.string.connected, Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
        findViewById(R.id.main_linear_layout).setVisibility(View.VISIBLE);
    }

    private void connectionFailed(String error) {
        Log.e(TAG, error);
        Toast.makeText(getBaseContext(), R.string.connection_error, Toast.LENGTH_LONG).show();
        mProgress.dismiss();
    }

    public void connectAndAuth() {
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }

    private class GetIdTokenTask extends AsyncTask<String, Void, String> {

        private static final String SERVER_CLIENT_ID = "286336060185-fnqe7hokq83dbec4oh14vnvqama1aamn.apps.googleusercontent.com";
        private static final String TAG = "GetIdToken";

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String scopes = "audience:server:client_id:" + SERVER_CLIENT_ID; // Not the app's client ID.
            try {
                return GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
            } catch (IOException e) {
                connectionFailed("Error retrieving ID token.");
                return null;
            } catch (GoogleAuthException e) {
                connectionFailed("Error retrieving ID token.");
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "ID token: " + result);
            if (result != null) {
                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(TOKEN_KEY, result);
                editor.commit();

                authWithServer(result);
            } else {
                connectionFailed("Got null ID token result");
            }
        }
    }
}
