package com.nel.moringa.MyWorld;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.nel.moringa.MyWorld.model.Country;

public class MainActivity extends AppCompatActivity {

    private String RESULTS;
    private String TAG = MainActivity.class.getSimpleName();
    private Set<String> regions;
    private TreeMap<String, ArrayList<Country>> filteredCountries = new TreeMap<>();

    static final String NO_REGION = "No Region";
    static final String URL = "https://restcountries.eu/rest/v2/all";

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get data from endpoint
        createRequestQueue();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    getSupportActionBar().setTitle("Hello, " + user.getDisplayName() + "!");
                } else {

                }
            }
        };


    }




    /**
     * Creates a Volley RequestQueue to hit REST API endpoint.
     */
    private void createRequestQueue() {

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                RESULTS = response;

                // parse JSON
                parseCountries();

                // set list elements
                setRegionsList();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err) {
                Log.e(TAG, "Json parsing error: " + err.getMessage());
            }
        });

        // add request to queue
        queue.add(stringRequest);
    }

    /**
     * Parse countries from JSON.
     */
    private void parseCountries() {

        if (RESULTS != null) {

            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Country>>(){}.getType();
            List<Country> countries = gson.fromJson(RESULTS, listType);

            // parse regions
            for (Country c : countries) {

                if (!c.getRegion().isEmpty()) {

                    if (!filteredCountries.containsKey(c.getRegion())) {
                        ArrayList<Country> countriesList = new ArrayList<>();
                        countriesList.add(c);
                        filteredCountries.put(c.getRegion(), countriesList);

                    } else {
                        Objects.requireNonNull(filteredCountries.get(c.getRegion())).add(c);
                    }

                } else {

                    if (!filteredCountries.containsKey(NO_REGION)) {
                        ArrayList<Country> countriesList = new ArrayList<>();
                        countriesList.add(c);
                        filteredCountries.put(NO_REGION, countriesList);

                    } else {
                        Objects.requireNonNull(filteredCountries.get(NO_REGION)).add(c);
                    }
                }
            }

            // assign list of regions
            regions = filteredCountries.keySet();
        }
    }

    /**
     * Create regions ListView.
     */
    private void setRegionsList() {

       // ListView list = findViewById(R.id.regions_list);

        SwipeMenuListView list = (SwipeMenuListView) findViewById(R.id.regions_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getBaseContext(), R.layout.region_row, R.id.region, regions.toArray(new String[0]));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getBaseContext(), CountryListActivity.class);
                intent.putExtra("countries", filteredCountries.get(Objects.requireNonNull(filteredCountries.keySet().toArray())[i]));
                startActivity(intent);
            }
        });

        list.setAdapter(adapter);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
//                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(
//                        getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0x00,	0x33,	0x66)));
//                // set item width
//                openItem.setWidth(170);
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.airport);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        list.setMenuCreator(creator);

        list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        String data = String.format("geo:%s,%s", 1.2921, 36.8219);

                        intent.setData(Uri.parse(data));
                        startActivity(intent);
                        break;
                    case 1:
                        Log.d(TAG, "onMenuItemClick: clicked item " + index);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

    }




    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, ActivityLogin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
