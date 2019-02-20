package com.example.admin.proyectotesttesis;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.view.WindowManager;

import com.example.admin.proyectotesttesis.JsonDataDetectedSaved.BoxDetected;
import com.example.admin.proyectotesttesis.JsonDataDetectedSaved.JsonDetection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.*;

public class MainActivity extends Activity {

    private MyCanvas myCamvas;
    private Bitmap imageOriginal;
    private Bitmap imageToDraw;

    public static long START = (long)0.0;
    public static long FINISH = (long) 0.0;

    private int MY_REQUEST_PERMISSION_CODE = 205300;
    private String PATH_IMGS = "/ImgTesis/GetFramesV3/Frames/Dia/";
    private String NAME_JSON_BOUNDIG_BOXES = "jsonDetectedFromMobileConf_0.30.json";
    public static double CONFIDENCE = 0.30;
    List<BoxDetected> boxDetectedList = null;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Full immersive mode
        this.hideSystemUI();
        this.keepScreenOn();

        //Set camvas
        this.myCamvas = new MyCanvas(this);
        this.myCamvas.setBackgroundColor(Color.WHITE);
        this.setContentView(myCamvas);

        this.checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if( requestCode == this.MY_REQUEST_PERMISSION_CODE ){
            if( grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED ){
                this.allPermissionOk();
            }
        }
    }

    private void checkPermissions(){

        if ( (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ){
            if ( (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ){
                this.allPermissionOk();
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    this.MY_REQUEST_PERMISSION_CODE);
        }


//        if ( (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//             && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)   ) {
//
//            // Should we show an explanation?
//            if( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        this.MY_REQUEST_PERMISSION_CODE);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }else{
//            this.allPermissionOk();
//        }
    }

    /**
     * Set full screen
     */
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Set screen on always
     */
    private void keepScreenOn(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void allPermissionOk(){
        //Detection
        this.startDetection();
    }

    private Bitmap[] loadImage( File file ) {
        Bitmap[] bitmap = new Bitmap[2];
        try {
            Bitmap originalImg = BitmapFactory.decodeStream(new FileInputStream(file));

            //scale img
            DisplayMetrics displayMetrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            Bitmap scaledImg = Bitmap.createScaledBitmap( originalImg, width, height, false );
            bitmap[0] = originalImg;
            bitmap[1] = scaledImg;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Process to start car and bus detection from image loaded
     */
    private void startDetection() {

        this.boxDetectedList = new LinkedList<>();

        //Thread to set a cycle to load and show different images while
        // neural convolutional network makes inferences detections from the image loaded and showed
        Thread threadMain = new Thread(new Runnable() {
            @Override
            public void run() {

                File path = new File(Environment.getExternalStorageDirectory(), MainActivity.this.PATH_IMGS);
                File[] imgFiles = path.listFiles();

                int i=0;
                //Get
                for (File imgFile : imgFiles){ //Prevenir leer el json

                    String [] nameList = imgFile.getName().split("\\.");
                    if( nameList[nameList.length-1].equalsIgnoreCase("PNG")||nameList[nameList.length-1].equalsIgnoreCase("JPG")||
                            nameList[nameList.length-1].equalsIgnoreCase("JPEG")||nameList[nameList.length-1].equalsIgnoreCase("BMP") ){
//                        if(i==5) break;
                        //Get image
                        Bitmap[] bitmaps = MainActivity.this.loadImage( imgFile );
                        MainActivity.this.imageOriginal = bitmaps[0];
                        MainActivity.this.imageToDraw = bitmaps[1];

                        //Set image and repaint
                        MainActivity.this.myCamvas.setImage(imageToDraw);
                        MainActivity.this.myCamvas.postInvalidate();

                        //Start SSDMobileNet network
                        SSDMobileNet ssdMobileNet = new SSDMobileNet(MainActivity.this);
                        ssdMobileNet.startSSDMobileNet(MainActivity.this.imageOriginal);

                        //wait to SSDMobileNet finished
                        while (!SSDMobileNet.finishDetection) {}

                        //SSDMobileNet had finished and the boxes detections have been saved so they need to be painted
                        List<Box> boxes = new LinkedList<Box>( SSDMobileNet.boxes );
                        SSDMobileNet.cleanDetections();

                        MainActivity.this.drawDetections( boxes );
                        MainActivity.this.saveDetections( boxes, imgFile.getName(), true );

//                        try {
//                            //Wait
//                            sleep((long) (0.5 * 1000));
//                        }catch (Exception e){}
                    }
                    i++;
                }
                JsonDetection jsonDetection = new JsonDetection( MainActivity.this.boxDetectedList );
                jsonDetection.makeJsonDetections();
                String jsonDetectionSaved = jsonDetection.toString();
                try {
                    String state = Environment.getExternalStorageState();
                    if( Environment.MEDIA_MOUNTED.equals(state) ) {
                        File fileJson = new File(Environment.getExternalStorageDirectory() , MainActivity.this.PATH_IMGS+ MainActivity.this.NAME_JSON_BOUNDIG_BOXES);
                        if (fileJson.exists())
                            fileJson.delete();
                        if (!fileJson.exists())
                            fileJson.createNewFile();
                        File[] f = fileJson.getParentFile().listFiles();
                        FileOutputStream fos = new FileOutputStream(fileJson);
                        OutputStreamWriter out = new OutputStreamWriter( fos );
                        out.write(jsonDetectionSaved);
                        out.flush();
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("My app", "Adios");
            }
        });

        threadMain.setPriority(MIN_PRIORITY);
        threadMain.start();
    }

    /**
     * Draw all detections saved
     */
    private void drawDetections( List<Box> boxes ){
        this.myCamvas.setImage( this.imageToDraw );
        myCamvas.setBoxes( boxes );
        this.myCamvas.postInvalidate();
    }

    /**
     * Draw all detections saved
     */
    private void saveDetections( List<Box> boxes, String name, boolean isRealScale ){
        BoxDetected boxDetected = new BoxDetected();
        boxDetected.setNameImg( name );
        for( Box box: boxes ) {
            if( box.getConfidence() > MainActivity.CONFIDENCE ) {
                if (isRealScale) {
                    int width = this.imageOriginal.getWidth();
                    int height = this.imageOriginal.getHeight();

                    boxDetected.addRegion(box.getxMin() * width, box.getyMin() * height, box.getxMax() * width, box.getyMax() * height);
                } else {
                    boxDetected.addRegionFromBox(box);
                }
            }

        }

        this.boxDetectedList.add( boxDetected );
    }
}
