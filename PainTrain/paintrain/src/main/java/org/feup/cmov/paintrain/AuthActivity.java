package org.feup.cmov.paintrain;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

/**
 * Created by vascofg on 10-11-2015.
 */
public class AuthActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TOKEN_KEY = "TOKEN"; //name of token key in shared preferences
    public static final int RC_AUTH = 2;
    public static final int RESULT_AUTH_SUCCESS = 0;
    public static final int RESULT_AUTH_FAILURE = 1;
    private static final String TAG = "AuthActivity";
    private static final String SERVER_CLIENT_ID = "286336060185-fnqe7hokq83dbec4oh14vnvqama1aamn.apps.googleusercontent.com";
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();

        connectAndAuth();
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

    private void authWithServer(String token) {
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
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    connectionFailed(volleyError.toString());
                }
            },this);
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            connectionFailed(e.toString());
        }
    }

    private void connectionSuccess() {
        mProgress.dismiss();
        setResult(RESULT_AUTH_SUCCESS);
        finish();
    }

    private void connectionFailed(String error) {
        Log.e(TAG, error);
        mProgress.dismiss();
        setResult(RESULT_AUTH_FAILURE);
        finish();
    }

    private void connectAndAuth() {
        mShouldResolve = true;
        mGoogleApiClient.connect();
    }

    private class GetIdTokenTask extends AsyncTask<String, Void, String> {

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
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
