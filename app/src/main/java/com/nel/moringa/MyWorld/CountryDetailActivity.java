package com.nel.moringa.MyWorld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.ahmadrosid.svgloader.SvgLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.nel.moringa.MyWorld.model.Country;

public class CountryDetailActivity extends AppCompatActivity {

    private ShareActionProvider shareActionProvider;

    private String countryName;
    private String countryCapital;
    private String countryRegion;
    private String countryPopulation;
    private String countryArea;
    private String countryBorders;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private ImageView ivImage;
    private String userChoosenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_detail);
        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        btnSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);



        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Country c = (Country) Objects.requireNonNull(getIntent().getExtras()).getSerializable("country");

        // set flag
        ImageView flag = findViewById(R.id.c_flag);

        SvgLoader.pluck()
                .with(this)
                .setPlaceHolder(R.mipmap.ic_launcher, R.mipmap.ic_launcher)
                .load(Objects.requireNonNull(c).getFlag(), flag);

        // set name
        TextView name = findViewById(R.id.c_name);
        countryName = "Name: " + Objects.requireNonNull(c).getName();
        name.setText(countryName);

        // set capital
        TextView capital = findViewById(R.id.c_capital);
        countryCapital = "Capital: " + c.getCapital();
        capital.setText(countryCapital);

        // set region
        TextView region = findViewById(R.id.c_region);
        countryRegion = "Region: " + c.getRegion();
        region.setText(countryRegion);

        // set population
        TextView population = findViewById(R.id.c_population);
        countryPopulation = "Population: " + String.valueOf(c.getPopulation());
        population.setText(countryPopulation);

        // set area
        TextView area = findViewById(R.id.c_area);
        countryArea = "Area: " + String.valueOf(c.getArea());
        area.setText(countryArea);

        // set borders
        TextView borders = findViewById(R.id.c_borders);

        StringBuilder borderText = new StringBuilder("Borders: ");
        List<String> bordersList = c.getBorders();

        if (bordersList.size() > 0) {
            for (int i = 0; i < bordersList.size() - 2; ++i) {
                borderText.append(bordersList.get(i)).append(", ");
            }
            borderText.append(bordersList.get(bordersList.size() - 1));
        }

        countryBorders = borderText.toString();

        borders.setText(countryBorders);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(CountryDetailActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(CountryDetailActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivImage.setImageBitmap(thumbnail);
    }
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_get_country_details);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        setShareActionIntent(
                countryName + "\n"
                        + countryCapital + "\n"
                        + countryRegion + "\n"
                        + countryPopulation + "\n"
                        + countryArea + "\n"
                        + countryBorders);

        return super.onCreateOptionsMenu(menu);
    }

    private void setShareActionIntent(String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        shareActionProvider.setShareIntent(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String deviceData = "";
        deviceData += "Manufacturer: " + Build.MANUFACTURER + "\n";
        deviceData += "Model: " + Build.MODEL + "\n";
        deviceData += "Version: " + Build.VERSION.SDK_INT+ "\n";
        deviceData += "Version Release: " + Build.VERSION.RELEASE + "\n";
        deviceData += "Serial Number: " + Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Toast.makeText(getApplicationContext(), deviceData, Toast.LENGTH_LONG).show();
        return true;
    }
}
