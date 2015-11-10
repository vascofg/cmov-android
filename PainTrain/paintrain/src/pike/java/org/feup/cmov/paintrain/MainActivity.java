package org.feup.cmov.paintrain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int RC_SCAN = 0;

    private List<String> mTrips;
    private List<String> mStations;
    private JSONArray mTripsData;
    private Spinner current_trip, current_station;
    private Button mButton;

    private String mToken;

    private View mProgress;

    private PrivateKey privKey;

    public static PrivateKey getPrivateKey(Context mContext) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // reads the public key stored in a file
        InputStream is = mContext.getResources().openRawResource(R.raw.privkey);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] privKeyBytes = new byte[(int) is.available()];
        bis.read(privKeyBytes);
        bis.close();
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec ks = new PKCS8EncodedKeySpec(privKeyBytes);
        return keyFactory.generatePrivate(ks);
    }

    private static String Decrypt(String message, PrivateKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPadding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedText = cipher.doFinal(Base64.decode(message, Base64.DEFAULT));
        Log.d(TAG, "Decrypted " + decryptedText.length + " bytes");
        String result = new String(decryptedText, "UTF-8");
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current_trip = (Spinner) findViewById(R.id.current_trip);
        current_station = (Spinner) findViewById(R.id.current_station);

        mButton = (Button) findViewById(R.id.scan_qrcode_button);

        mProgress = findViewById(R.id.progress_bar);

        try {
            privKey = MainActivity.getPrivateKey(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

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
                        mProgress.setVisibility(View.GONE);
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

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, this);
        // Add the request to the RequestQueue.
        mProgress.setVisibility(View.VISIBLE);
        queue.add(jsonObjectRequest);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mToken = settings.getString(AuthActivity.TOKEN_KEY, null);

        if (mToken == null) {
            Intent authIntent = new Intent(this, AuthActivity.class);
            startActivityForResult(authIntent, AuthActivity.RC_AUTH);
        } else
            findViewById(R.id.main_linear_layout).setVisibility(View.VISIBLE);

        //TODO: send ticket status on end of trip
    }

    /* called after choosing account */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SCAN) {
            //QR CODE SCAN
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                if(validateTicket(contents)) {
                    Log.d(TAG, "Valid ticket!");
                    Toast.makeText(MainActivity.this, "Valid ticket!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG, "Invalid ticket!");
                    Toast.makeText(MainActivity.this, "Invalid ticket!", Toast.LENGTH_SHORT).show();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Scan canceled");
            }
        } else if (requestCode == AuthActivity.RC_AUTH) {
            if (resultCode == AuthActivity.RESULT_AUTH_SUCCESS) {
                Log.d(TAG, "Auth successful!");
                Toast.makeText(getBaseContext(), R.string.connected, Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Auth failed!");
                Toast.makeText(getBaseContext(), R.string.connection_error, Toast.LENGTH_SHORT).show();
            }
            findViewById(R.id.main_linear_layout).setVisibility(View.VISIBLE);
        }
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

    private boolean validateTicket(String ticketsStr) {
        Log.d(TAG, ticketsStr);
        boolean valid = true;
        try {
            JSONArray tickets = new JSONArray(ticketsStr);
            for(int i=0;i<tickets.length();i++) {
                String ticket = tickets.getString(i);
                Log.d(TAG, "Unencrypting ticket #" + i + ": " + ticket);
                String ticketDecrypted = MainActivity.Decrypt(ticket, privKey);
                Log.d(TAG, ticketDecrypted);
                String[] params = ticketDecrypted.split("--");
                for(String param:params)
                    Log.d(TAG, param);
                boolean paid = params[params.length-1].equals("paid");
                if(!paid)
                    valid=false;
            }
            return valid;
            //TODO: compare stations
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
    }
}
