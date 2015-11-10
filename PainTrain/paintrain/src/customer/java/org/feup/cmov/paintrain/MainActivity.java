package org.feup.cmov.paintrain;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TOKEN_KEY = "TOKEN"; //name of token key in shared preferences
    private static final String TAG = "MainActivity";
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private String mToken;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

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
            findViewById(R.id.drawer_layout).setVisibility(View.VISIBLE);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        DrawerViewFragment f = null;
        switch (position) {
            default:
            case 0:
                f = BuyTicketsFragment.newInstance(BuyTicketsFragment.class, position + 1);
                break;
            case 1:
                f = TimetableFragment.newInstance(TimetableFragment.class, position + 1);
                break;
            case 2:
                f = MyTicketsFragment.newInstance(MyTicketsFragment.class, position + 1);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }

    public void onSectionAttached(int number) {
        mTitle = getResources().getStringArray(R.array.titles)[number - 1];
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        findViewById(R.id.drawer_layout).setVisibility(View.VISIBLE);
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
