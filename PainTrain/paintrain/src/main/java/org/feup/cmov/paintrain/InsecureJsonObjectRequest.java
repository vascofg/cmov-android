package org.feup.cmov.paintrain;

import android.app.Activity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

/**
 * Created by vascofg on 09-11-2015.
 */
public class InsecureJsonObjectRequest extends JsonObjectRequest {

    private Activity activity;
    private final DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(5000,2,1.0f);

    public InsecureJsonObjectRequest(int method,
                                     String url,
                                     JSONObject jsonRequest,
                                     Response.Listener<JSONObject> listener,
                                     Activity activity) {
        super(method, url, jsonRequest, listener, new CustomErrorListener(activity));
        this.activity = activity;
        this.setRetryPolicy(retryPolicy);
    }

    public InsecureJsonObjectRequest(int method,
                                     String url,
                                     JSONObject jsonRequest,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener,
                                     Activity activity) {
        super(method, url, jsonRequest, listener, errorListener);
        this.activity = activity;
        this.setRetryPolicy(retryPolicy);
    }

    public InsecureJsonObjectRequest(int method,
                                     String url,
                                     Response.Listener<JSONObject> listener,
                                     Activity activity) {
        super(method, url, listener, new CustomErrorListener(activity));
        this.activity = activity;
        this.setRetryPolicy(retryPolicy);
    }

    public InsecureJsonObjectRequest(int method,
                                     String url,
                                     Response.Listener<JSONObject> listener,
                                     Response.ErrorListener errorListener,
                                     Activity activity) {
        super(method, url, listener, errorListener);
        this.activity = activity;
        this.setRetryPolicy(retryPolicy);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        //if response empty, make it into the empty json object "{}"
        if (response.data.length == 0) {
            byte[] responseData = "{}".getBytes();
            response = new NetworkResponse(response.statusCode, responseData, response.headers, response.notModified);
        }
        return super.parseNetworkResponse(response);
    }
}
