package org.feup.cmov.paintrain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int RC_SCAN = 0;

    private List<String> mTrips;
    private List<String> mStations;
    private JSONArray mTripsData;
    private int[] mTripsIDs;
    private Spinner current_trip, current_station;
    private Button mButton;

    private String mToken;

    private List<String> mValidatedTickets;

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
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String payload = new String(message.getRecords()[0].getPayload());
            Log.d(TAG,"GOT NFC TICKET: " + payload);

            if(validateTicket(payload)) {
                Log.d(TAG, "Valid ticket!");
                Toast.makeText(MainActivity.this, "Valid ticket!", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d(TAG, "Invalid ticket!");
                Toast.makeText(MainActivity.this, "Invalid ticket!", Toast.LENGTH_SHORT).show();
            };
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        current_trip = (Spinner) findViewById(R.id.current_trip);
        current_station = (Spinner) findViewById(R.id.current_station);

        mButton = (Button) findViewById(R.id.scan_qrcode_button);

        mValidatedTickets = new LinkedList<String>();

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
                                mTripsData = jsonObject.getJSONArray("data");;
                                mTrips = new ArrayList<String>(mTripsData.length());
                                mTripsIDs = new int[mTripsData.length()];
                                for (int i = 0; i < mTripsData.length(); i++) {
                                    JSONObject trip = mTripsData.getJSONObject(i);
                                    JSONArray times = trip.getJSONArray("times");

                                    int tripID = trip.getInt("id");
                                    mTripsIDs[i] = tripID;

                                    JSONObject departureObj = times.getJSONObject(0);
                                    JSONObject arrivalObj = times.getJSONObject(times.length() - 1);

                                    String departureStation = departureObj.getString("station");
                                    String departureTime = departureObj.getString("time");

                                    String arrivalStation = arrivalObj.getString("station");
                                    String arrivalTime = arrivalObj.getString("time");

                                    mTrips.add("(" + tripID + ") " + departureTime + " " + departureStation + "->" + arrivalTime + " " + arrivalStation);
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
        try {
            JSONArray tickets = new JSONArray(ticketsStr);
            for(int i=0;i<tickets.length();i++) {
                String ticket = tickets.getString(i);
                Log.d(TAG, "Unencrypting ticket #" + i + ": " + ticket);
                String ticketDecrypted = MainActivity.Decrypt(ticket, privKey);
                Log.d(TAG, ticketDecrypted);
                String[] paramsStr = ticketDecrypted.split("--");
                JSONObject params = new JSONObject();
                for(String paramStr:paramsStr) {
                    String key, value;
                    int keyLenSeparatorPos = paramStr.indexOf(':');
                    if(keyLenSeparatorPos>0) {
                        key = paramStr.substring(0,keyLenSeparatorPos);
                        value = paramStr.substring(keyLenSeparatorPos+1,paramStr.length());
                    } else
                    {
                        key = paramStr;
                        value = "true";
                    }
                    params.put(key, value);
                }
                Log.d(TAG, params.toString());

                if(params.getBoolean("paid")) {
                    int currentTripID = mTripsIDs[current_trip.getSelectedItemPosition()];
                    if(params.getInt("id")==currentTripID) {
                        String firstStation = params.getString("firstStation");
                        String lastStation = params.getString("lastStation");

                        int firstStationIndex = mStations.indexOf(firstStation);
                        int lastStationIndex = mStations.indexOf(lastStation);
                        int currentStationIndex = current_station.getSelectedItemPosition();

                        if(firstStationIndex<=currentStationIndex && lastStationIndex>=currentStationIndex) {
                            //if at least one ticket is valid, it's OK
                            mValidatedTickets.add(ticket);
                            return true;
                        }
                    }

                }
            }
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

    public void sendTickets(View view) {
        try {
            if(mValidatedTickets.size()==0) {
                Log.d(TAG, "No tickets to send");
                Toast.makeText(this,"No tickets to send",Toast.LENGTH_LONG).show();
            }

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = getResources().getString(R.string.base_url) + "/ticketStatus";

            JSONObject jo = new JSONObject();
            JSONArray ja = new JSONArray(mValidatedTickets);
            jo.put("data", ja);


            // Request a JSON response from the provided URL.
            JsonObjectRequest jsonObjectRequest = new SecureJsonObjectRequest(Request.Method.POST, url, jo, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.d(TAG, "Sent " + mValidatedTickets.size() + " tickets");
                    Toast.makeText(MainActivity.this,"Sent " + mValidatedTickets.size() + " " + (mValidatedTickets.size()==1?"ticket":"tickets"), Toast.LENGTH_LONG).show();
                    mProgress.setVisibility(View.GONE);
                    mValidatedTickets = new LinkedList<String>();
                }
            },this);

            // Add the request to the RequestQueue.
            mProgress.setVisibility(View.VISIBLE);
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
