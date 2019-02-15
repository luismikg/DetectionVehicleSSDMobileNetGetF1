package com.example.admin.proyectotesttesis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.view.WindowManager;

import com.example.admin.proyectotesttesis.JsonDataDetectedSaved.BoxDetected;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.*;

public class MainActivity extends Activity {

    private MyCanvas myCamvas;
    private Bitmap imageOriginal;
    private Bitmap image;

    public static long START = (long)0.0;
    public static long FINISH = (long) 0.0;

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

        //Load first image
        this.loadImage( 0 );

        //Detection
        this.startDetection();
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

    /**
     * Load and set an image depending idxImg (0 to 8)
     * @param idxImg
     */
    private void loadImage( int idxImg ) {

        Log.d("MyApp","IMG: --- "+idxImg);

        switch ( idxImg ) {
            case 0: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m1_f25_tf25);
                break;
            case 1: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m12_f102_ft441);
                break;
            case 2: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m1_f86_tf86);
                break;
            case 3: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m12_f94_tf433);
                break;
            case 4: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m7_f8_tf172);
                break;
            case 5: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m7_f9_tf173);
                break;
            case 6: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m7_f18_tf182);
                break;
            case 7: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m7_f25_tf189);
                break;
            case 8: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m9_f8_tf203);
                break;
            default: this.imageOriginal = BitmapFactory.decodeResource(this.getResources(), R.mipmap.m9_f35_tf230);
        }

        //scale img
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        this.image = Bitmap.createScaledBitmap(imageOriginal, width, height, false );

        //Set image and repaint
        this.myCamvas.setImage(image);
        this.myCamvas.postInvalidate();
    }

    /**
     * Process to start car and bus detection from image loaded
     */
    private void startDetection() {

        //Thread to set a cycle to load and show different images while
        // neural convolutional network makes inferences detections from the image loaded and showed
        Thread threadMain = new Thread(new Runnable() {
            @Override
            public void run() {

                //index image to load
                int idxImg = 0;

                while(true) {
                    if (idxImg>9){idxImg = 0;}

                    try {
                        //Start SSDMobileNet network
                        SSDMobileNet ssdMobileNet = new SSDMobileNet(MainActivity.this);
                        ssdMobileNet.startSSDMobileNet(MainActivity.this.imageOriginal);

                        //wait to SSDMobileNet finished
                        while (!SSDMobileNet.finishDetection) {}
                        SSDMobileNet.cleanDetections();

                        //SSDMobileNet had finished and the boxes detections have been saved so they need to be painted
                        List<Box> boxes = new LinkedList<Box>( SSDMobileNet.boxes );

                        MainActivity.this.drawDetections( boxes );
                        MainActivity.this.saveDetections(boxes);

                        //Wait 5 sec.
                        sleep(5 * 1000);

                        //Load next image
                        MainActivity.this.loadImage( idxImg );

                    } catch (Exception e) {
                        System.out.print(e.toString());
                    }
                    idxImg++;
                }
            }
        });

        threadMain.setPriority(MIN_PRIORITY);
        threadMain.start();
    }

    /**
     * Draw all detections saved
     */
    private void drawDetections( List<Box> boxes ){
        this.myCamvas.setImage(image);
        myCamvas.setBoxes( boxes );

//        for ( int i=0; i<SSDMobileNet.boxHigherConfThanConfidenceThreshold.length; i++ ) {
//            for ( int idxBox=0; idxBox<SSDMobileNet.boxHigherConfThanConfidenceThreshold[i].length; idxBox++) {
//                boolean[] boxHigherConfThanConfidenceThresholdData = SSDMobileNet.boxHigherConfThanConfidenceThreshold[i][idxBox];
//
//                for (boolean boxHigherConfThanConfidenceThreshold : boxHigherConfThanConfidenceThresholdData) {
//                    if (boxHigherConfThanConfidenceThreshold) {
//                        float[] box = SSDMobileNet.boxesToProcessComplete[i][idxBox];
//                        myCamvas.addBox( box );
//                    }
//                }
//            }
//        }
        this.myCamvas.postInvalidate();
    }

    /**
     * Draw all detections saved
     */
    private void saveDetections( List<Box> boxes ){
//        BoxDetected boxDetected = new BoxDetected();
//        boxDetected.setNameImg( this.);
//        for ( Box boxe: boxes ) {
//
//        }
    }
}
