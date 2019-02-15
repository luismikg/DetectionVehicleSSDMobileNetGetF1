package com.example.admin.proyectotesttesis;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

public class Detector {

    // Image mode coffe
    float[][][][] inputImage = null;
    // The other way to send the image to the CNN. In these case the image has the mode coffe too.
    private ByteBuffer inputBuffer = null;
    private float[][][] output = null;

    // Specify the input size like [1][300][300][3]
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 300;
    private static final int DIM_IMG_SIZE_Y = 300;
    private static final int DIM_PIXEL_SIZE = 3;

    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float
    private static final int BYTE_SIZE_OF_FLOAT = 4;

    // Context
    private MainActivity mainActivity;
    // Interpreter tensorflow
    private Interpreter tfLite;

    /**
     * Constructor.
     * @param context Main class
     * @param tfLite Tensorflow lite model
     * @param img Image to process
     * @param classes Classes to recognition
     */
    public Detector( Context context, Interpreter tfLite, Bitmap img, String [] classes ){
        this.mainActivity = (MainActivity)context;
        this.tfLite = tfLite;

        Bitmap image = img.copy(img.getConfig(), true);

        this.initInputAndOutput();
        this.preprocessImg( image );
        this.runInterface( classes );
    }

    /**
     * Init the buffer which will contend the image to process and init the output which will contends the detections from CNN.
     */
    private void initInputAndOutput(){
        this.inputBuffer = ByteBuffer.allocateDirect( Detector.BYTE_SIZE_OF_FLOAT * Detector.DIM_BATCH_SIZE * Detector.DIM_IMG_SIZE_X * Detector.DIM_IMG_SIZE_Y * Detector.DIM_PIXEL_SIZE  );
        this.inputBuffer.order( ByteOrder.nativeOrder() );
        this.output = new float[1][1692][33];
    }

    /**
     * Convert the image to process as caffe mode.
     * @param image
     */
    private void preprocessImg(Bitmap image) {
        Bitmap img =  Bitmap.createScaledBitmap(image, 300, 300, false );

        float[]mean = {103.939f, 116.779f, 123.68f};

        // To convert the input image to caffe mode.
        // 1st way for inputImage

//        int w = img.getWidth();
//        int h = img.getHeight();
//        this.inputImage = new float[1][w][h][3];
//
//        for (int i = 0; i < h; i++) {
//            for (int j = 0; j < w; j++) {
//                int pixel = img.getPixel(j, i);
//
//                int red = (pixel >> 16) & 0xff;
//                int green = (pixel >> 8) & 0xff;
//                int blue = (pixel) & 0xff;
//
//                this.inputImage [0][j][i][0] = (float) blue - mean[0];
//                this.inputImage [0][j][i][1] = (float) green - mean[1];
//                this.inputImage [0][j][i][2] = (float) red - mean[2];
//            }
//        }


        // 2th way for inputBuffer
        int[] pixels = new int[300*300];
        img.getPixels(pixels,0,300,0,0,300,300);
        for (int i = 0; i < pixels.length; ++i) {
            // Set 0 for white and 255 for black pixels
            int pixel = pixels[i];

            //GOOD for CPU and CAFFE image mode
            inputBuffer.putFloat(((pixel) & 0xFF)-mean[0]);
            inputBuffer.putFloat(((pixel >> 8) & 0xFF)-mean[1]);
            inputBuffer.putFloat(((pixel >> 16) & 0xFF)-mean[2]);
        }
    }

    /**
     * Run tensorflow model
     * @param classes Classes to recognize
     */
    protected void runInterface( String [] classes ){
        MainActivity.START  = System.nanoTime();
        //this.tfLite.run( this.inputImage, this.output );

        this.tfLite.run( this.inputBuffer, this.output );
        // Now this.output has all prediction
        this.getDetections( classes );
    }

    /**
     * This method decode call detections and make the non-Maximum-Suppression
     * @param classes Classes to recognized.
     */
    public void getDetections( String [] classes ) {
        //NonMaxSupression nonMaxSupression = new NonMaxSupression( classes );
        //nonMaxSupression.detectionsOut( this.output );

        Decode decodeBoundingBoxes = new Decode( classes, this.output );



        //boolean[][][] boxHigherConfThanConfidenceThreshold = decodeBoundingBoxes.boxHigherConfThanConfidenceThreshold;
        List<Box> boxes = decodeBoundingBoxes.boxes;
        //Call non-max-suppression .....


        SSDMobileNet.callBack( boxes );
    }
}
