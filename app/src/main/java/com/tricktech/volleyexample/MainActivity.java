package com.tricktech.volleyexample;

import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.tricktech.volleyexample.adapters.PostAdapter;
import com.tricktech.volleyexample.models.Post;
import com.tricktech.volleyexample.receiver.ConnectivityReceiver;
import com.tricktech.volleyexample.utils.AppController;
import com.tricktech.volleyexample.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    public CoordinatorLayout coordinatorLayout;
    public boolean isConnected;
    public static final String NA = "NA";
    public List<Post> postList;
    public RecyclerView recycler_post;
    public PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        recycler_post = (RecyclerView) findViewById(R.id.recycler_post);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_post.setLayoutManager(layoutManager);
        recycler_post.setItemAnimator(new DefaultItemAnimator());


        if (checkConnectivity()){
            try {
                getAllPosts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            showSnack();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppController.getInstance().setConnectivityReceiver(this);
    }

    public boolean checkConnectivity() {
        return ConnectivityReceiver.isConnected();
    }

    public void showSnack() {

        Snackbar.make(coordinatorLayout, getString(R.string.no_internet_connected), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).setActionTextColor(Color.RED)
                .show();
    }


    @Override
    public void onNetworkChange(boolean inConnected) {
        this.isConnected = inConnected;
    }

    public void getAllPosts() throws Exception{
        String TAG = "POSTS";
        String url = Constants.POSTS_URL;
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("response", response);
                parseJson(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.getMessage());
            }
        });

        AppController.getInstance().addToRequestQueue(jsonObjectRequest, TAG);
    }

    public void parseJson(String response){
        JSONArray postArr;
        postList = new ArrayList<>();
        try {
            postArr = new JSONArray(new String(response));
            for (int i = 0; i < postArr.length(); i++){
                JSONObject postObj =postArr.getJSONObject(i);
                Post post = new Post();
                if (contains(postObj, "userId")){
                    post.userId = postObj.getInt("userId");
                }else {
                    post.userId = 0;
                }
                if (contains(postObj, "id")){
                    post.id = postObj.getInt("id");
                }else {
                    post.id = 0;
                }
                if (contains(postObj, "title")){
                    post.title = postObj.getString("title");
                }else {
                    post.title = NA;
                }
                if (contains(postObj, "body")){
                    post.body = postObj.getString("body");
                }else {
                    post.body = NA;
                }
                postList.add(post);
            }

            adapter = new PostAdapter(MainActivity.this, postList);
            recycler_post.setAdapter(adapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean contains(JSONObject jsonObject, String key){
        return jsonObject != null && jsonObject.has(key) && !jsonObject.isNull(key) ? true : false;
    }

}
