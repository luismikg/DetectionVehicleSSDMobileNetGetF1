package com.example.admin.proyectotesttesis;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.experimental.GpuDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class SSDMobileNet {

    public static boolean finishDetection = false;
    public static float[][][] boxesToProcessComplete;
    public static boolean[][][] boxHigherConfThanConfidenceThreshold;
    public static List<Box> boxes;

    //Converted using Tensorflow 1.12.0.
    //Model taken: https://github.com/tanakataiki/ssd_kerasV2
    //Tutorial: tensorflow to tensorflow Lite: https://heartbeat.fritz.ai/intro-to-machine-learning-on-android-how-to-convert-a-custom-model-to-tensorflow-lite-e07d2d9d50e3
    //File tensorflow lite model.
    private static final String MODEL_PATH = "MobileNetV1tf1.12.0.tflite";

    //Context
    private MainActivity mainActivity;

    //Interpreter.
    private Interpreter tflite;

    //GPU not work!
    //private static GpuDelegate delegate;

    /**
     * Classifications that model recognizes
     */
    private String [] classes = {"Aeroplane", "Bicycle", "Bird", "Boat", "Bottle", "Bus", "Car",
                         "Cat", "Chair", "Cow", "Diningtable", "Dog", "Horse",
                         "Motorbike", "Person", "Pottedplant", "Sheep", "Sofa", "Train", "Tvmonitor"};

    /**
     * Constructor.
     * @param context
     */
    public SSDMobileNet(Context context) {
        this.mainActivity = (MainActivity)context;
    }

    /**
     * Method to init detections.
     * @param img
     */
    public void startSSDMobileNet( Bitmap img ){
        this.loadModel();
        Detector carDetector = new Detector(this.mainActivity, this.tflite, img, this.classes);
    }

    /**
     * Load TensorFlow model.
     */
    private void loadModel() {
        try {
            //For executing on GPU:
//            SSDMobileNet.delegate = new GpuDelegate();
//            Interpreter.Options options = (new Interpreter.Options()).addDelegate(delegate);
//            this.tflite = new Interpreter( this.loadModelFile(), options );
            //For running on CPU:
            this.tflite = new Interpreter( this.loadModelFile() );
        }catch (Exception e){
            System.out.print(e.toString());
        }
    }

    /**
     * Load tensorflow lite file
     * @return
     * @throws IOException
     */
    private MappedByteBuffer loadModelFile () throws IOException {
        AssetFileDescriptor fileDescriptor = this.mainActivity.getAssets().openFd( SSDMobileNet.MODEL_PATH );
        FileInputStream inputStream = new FileInputStream( fileDescriptor.getFileDescriptor() );
        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        MappedByteBuffer m = fileChannel.map( FileChannel.MapMode.READ_ONLY, startOffset, declaredLength );
        return  m;
    }

    /**
     * Callback, the detections has been generated finished by "Detector.java" so it called this method to draw the bounding boxes results.
     * @param boxesToProcessComplete The Bounding boxes generated.
     * @param boxHigherConfThanConfidenceThreshold The Confidences for each bounding boxes send in "boxesToProcessComplete"
     */
    public static void callBack( float[][][] boxesToProcessComplete, boolean[][][] boxHigherConfThanConfidenceThreshold ){
        SSDMobileNet.boxesToProcessComplete =  boxesToProcessComplete;
        SSDMobileNet.boxHigherConfThanConfidenceThreshold = boxHigherConfThanConfidenceThreshold;
        SSDMobileNet.finishDetection = true;
    }

    public static void callBack( List<Box> boxes ){
        SSDMobileNet.boxes =  boxes;
        SSDMobileNet.finishDetection = true;
    }


    public static void cleanDetections(){
        SSDMobileNet.finishDetection = false;
        SSDMobileNet.boxesToProcessComplete = null;
        SSDMobileNet.boxHigherConfThanConfidenceThreshold = null;
        SSDMobileNet.boxes = null;
        //For executing on GPU:
//        SSDMobileNet.delegate.close();
    }
}
