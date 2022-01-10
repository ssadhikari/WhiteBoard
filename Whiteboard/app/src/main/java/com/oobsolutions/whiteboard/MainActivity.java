package com.oobsolutions.whiteboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.snatik.storage.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import me.panavtec.drawableview.DrawableView;
import me.panavtec.drawableview.DrawableViewConfig;
import yuku.ambilwarna.AmbilWarnaDialog;


public class MainActivity extends AppCompatActivity {
    private static int REQUEST_CODE = 100;
    DrawableView drawableView;
    Button colour,undo,clearall,share,png;
    DrawableViewConfig config;
    Drawable colorBtn;
    SeekBar linewidth;
    Layout window;

    int DefaultColor;
    OutputStream outputStream;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        drawableView = (DrawableView) findViewById(R.id.paintView);


        colour = (Button) findViewById(R.id.color);
        undo = (Button) findViewById(R.id.undoButton);
        clearall = (Button)findViewById(R.id.clearAll);
        share = (Button)findViewById(R.id.share);
        png = (Button) findViewById(R.id.png);

        linewidth = (SeekBar) findViewById(R.id.seekbar);

        config = new DrawableViewConfig();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        linewidth.setMax(100);
        linewidth.setMin(1);
        config.setCanvasHeight(height-50);
        config.setCanvasWidth(width);
        config.setShowCanvasBounds(true);
        config.setMaxZoom(3.0f);
        config.setMinZoom(1.0f);
        config.setStrokeColor(getResources().getColor(android.R.color.black));
        config.setStrokeWidth(2.0f);


        drawableView.setConfig(config);
        drawableView.setBackgroundColor(getResources().getColor(android.R.color.white));
        DefaultColor = config.getStrokeColor();


        linewidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub

                config.setStrokeWidth(progress);



            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawableView.undo();
            }
        });
        colour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenColorPickerDialog(false);
            }
        });
        clearall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawableView.clear();

            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    screenshot();
                }
                else{
                    askPermission();
                }

            }
        });
        png.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    takePng();
                }
                else{
                    askPermission();
                }
            }
        });
    }

        private void OpenColorPickerDialog(boolean AlphaSupport) {

            AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(MainActivity.this, DefaultColor, AlphaSupport, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog ambilWarnaDialog, int color) {

                    DefaultColor = color;
                    config.setStrokeColor(color);
                    colour.setBackgroundColor(color);
            }

                @Override
                public void onCancel(AmbilWarnaDialog ambilWarnaDialog) {

                    Toast.makeText(MainActivity.this, "Color Picker Closed", Toast.LENGTH_SHORT).show();
                }
            });
            ambilWarnaDialog.show();

        }
    @SuppressLint("WrongConstant")


    private void shareImage(File file){
    Uri uri = Uri.fromFile(file);
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_SEND);
    intent.setType("image/*");

    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
    intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
    intent.putExtra(Intent.EXTRA_STREAM, uri);
    try {
        startActivity(Intent.createChooser(intent, "Share Screenshot"));
    } catch (ActivityNotFoundException e) {

    }

}
    private void askPermission(){
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
    }
    private void screenshot() {

        //File dir = new File(Environment.getStorageDirectory(),"Drawings");
        Storage storage = new Storage(getApplicationContext());
        String path = storage.getExternalStorageDirectory();
        String newDir = path + File.separator + "My Drawings";
        System.out.println(newDir);
        File dir = new File(newDir);
        if (!dir.exists()){

            storage.createDirectory(newDir);

        }

        //Bitmap bitmap = Bitmap.createBitmap(drawableView.obtainBitmap());
        Bitmap bitmap = Screenshot.takescreenshotOfRootView(drawableView);
        File file = new File(dir,System.currentTimeMillis()+".jpg");


        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(outputStream);
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        Toast.makeText(MainActivity.this,"Successfuly Saved",Toast.LENGTH_SHORT).show();

        try {
            outputStream.flush();
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void takePng() {

        //File dir = new File(Environment.getStorageDirectory(),"Drawings");
        Storage storage = new Storage(getApplicationContext());
        String path = storage.getExternalStorageDirectory();
        String newDir = path + File.separator + "My Drawings";
        System.out.println(newDir);
        File dir = new File(newDir);
        if (!dir.exists()){

            storage.createDirectory(newDir);

        }

        Bitmap bitmap = Bitmap.createBitmap(drawableView.obtainBitmap());
        //Bitmap bitmap = Screenshot.takescreenshotOfRootView(drawableView);
        File file = new File(dir,System.currentTimeMillis()+".png");


        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(outputStream);
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        Toast.makeText(MainActivity.this,"Successfuly Saved",Toast.LENGTH_SHORT).show();

        try {
            outputStream.flush();
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}
