package com.example.admin.proyectotesttesis;

import android.util.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Decode {

    // Data from CNN-output
    private float[][][] boxLoc;
    private float[][][] variances;
    private float[][][] priorBox;
    private float[][][] boxConf;

    // Classes to recognized.
    private String[] classes;

    // Num classes to recognized.
    private int numClasses;

    //Arrays where all boxes detections are saved.
    public boolean[][][] boxHigherConfThanConfidenceThreshold;
    public float [][][] boxesToProcess;

    // Array where all final boxes are saved.
    public List<Box> boxes;

    /**
     * Constructor.
     * @param classes
     */
    public Decode( String [] classes ){

        this.classes = classes;

        //"+1" Because SSD return the confidence backgraound on 0 index so we have 21 confidences where
        //0 index is the confidence of background and
        //1 to 21, that is the 20 following indexes are the confidences by each class detected
        this.numClasses = this.classes.length+1;
    }

    /**
     * Constructor
     * @param classes Classes to recognized.
     * @param predictions predictions from tensorflow model
     */
    public Decode( String [] classes, float[][][] predictions ){

        this.classes = classes;

        //"+1" Because SSD return the confidence backgraound on 0 index so we have 21 confidences where
        //0 index is the confidence of background and
        //1 to 21, that is the 20 following indexes are the confidences by each class detected
        this.numClasses = this.classes.length+1;

        this.detectionsOut( predictions, 0.01 );
    }

    /**
     * This method return all bounding boxes which has a higher confidence than parameter "confidenceThreshold"
     * @param predictions all predictions from tensorflow model
     */
    public void detectionsOut(float[][][] predictions  ){
        detectionsOut( predictions, 0.01  );
    }
    private void detectionsOut(float[][][] predictions, double confidenceThreshold  ){

        //Divide all predictions to different arrays containers (boxLoc, variances, priorBox, boxConf)
        this.getSpecificData( predictions );
        this.boxes = new LinkedList<>();

        for( int i=0; i<this.boxLoc.length; i++ ){
            float[][] decodeBox = this.decodeBoxes( this.boxLoc[i], this.priorBox[i], this.variances[i] );

            this.boxHigherConfThanConfidenceThreshold = new boolean[this.boxConf.length][this.boxConf[0].length][this.boxConf[0][0].length];
            this.boxesToProcess = new float[this.boxLoc.length][this.boxLoc[0].length][this.boxLoc[0][0].length];

            for (int idxBox=0; idxBox<this.boxConf[i].length; idxBox++ ) {
                float[] cConfidenceData = this.boxConf[i][idxBox];
                int c=0;

                float cConfidenceCar = cConfidenceData[6]; //Car
                float cConfidenceBus = cConfidenceData[7]; //Bus
                if ( cConfidenceCar > confidenceThreshold || cConfidenceBus > confidenceThreshold ){

                    this.boxHigherConfThanConfidenceThreshold[i][idxBox][c] = true;
                    this.boxesToProcess[i][idxBox] = decodeBox[idxBox];

                    Box box = new Box();
                    box.setxMin( decodeBox[idxBox][0] );
                    box.setyMin( decodeBox[idxBox][1] );
                    box.setxMax( decodeBox[idxBox][2] );
                    box.setyMax( decodeBox[idxBox][3] );
                    box.setConfidence( Math.max(cConfidenceCar, cConfidenceBus) );
                    box.setArea( (box.getxMax() - box.getxMin() +1)*(box.getyMax() - box.getyMin()+1) );
                    this.boxes.add( box );

                    Log.d("MyApp","--- "+idxBox);
                }

                //Check all classes:
//                for (float cConfidence : cConfidenceData) {
//                    //Confidence of background: idx=0
//                    if(c==0){ c++; continue; }
//                    if(c==6||c==7) { //Only cars or buses
//                        if (cConfidence > confidenceThreshold) {
//                            this.boxHigherConfThanConfidenceThreshold[i][idxBox][c] = cConfidence > confidenceThreshold;
//                            this.boxesToProcessComplete[i][idxBox] = decodeBox[idxBox];
//                            Log.d("MyApp","--- "+idxBox);
//                        }
//                    }else{ c++; continue; }
//                  c++;
//                }
            }
        }

        NonMaximumSuppression nonMaximumSuppression = new NonMaximumSuppression();
        this.boxes = nonMaximumSuppression.nms(  boxes, 0.70f );
    }

    /**
     * //Divide all predictions to different arrays containers (boxLoc, variances, priorBox, boxConf)
     * @param predictions All results fron tensorflow model
     */
    private void getSpecificData( float[][][] predictions ){

        float[][][] boxData = new float[predictions.length][predictions[0].length][4];
        float[][][] variancesData = new float[predictions.length][predictions[0].length][4];
        float[][][] priorBoxData = new float[predictions.length][predictions[0].length][4];
        float[][][] boxConfData =  new float[predictions.length][predictions[0].length][(predictions[0].length-4)-8];
        int i = 0;
        for( float[][] prediction: predictions ){
            int j = 0;
            for( float[] predictionData: prediction ){

                boxData[i][j] = Arrays.copyOfRange(predictionData, 0, 4);
                variancesData[i][j] = Arrays.copyOfRange(predictionData, (predictionData.length-4), predictionData.length);
                priorBoxData[i][j] = Arrays.copyOfRange(predictionData, (predictionData.length-8), predictionData.length-4);
                boxConfData[i][j] = Arrays.copyOfRange(predictionData, 4, (predictionData.length-8));
                j++;

            }
            i++;
        }

        this.boxLoc = boxData;
        this.variances = variancesData;
        this.priorBox = priorBoxData;
        this.boxConf = boxConfData;
    }

    /**
     * Get coordinates from "boxLoc" array as a function of its variances and priorBoxes
     * @param boxesLoc
     * @param priorBoxes
     * @param variances
     * @return
     */
    private float[][] decodeBoxes( float[][] boxesLoc, float[][] priorBoxes, float[][] variances ){

        float[][] decodeBoxes = new float [priorBoxes.length][1];

        //Since: boxesLoc.length = variances.length = priorBoxes.length,
        //all operations are in the same loop
        for( int i=0; i<priorBoxes.length; i++ ){

            float[] priorBox = priorBoxes[i];
            float[] boxLoc = boxesLoc[i];
            float[] variance = variances[i];

            float priorWidth = priorBox[2] - priorBox[0] ;
            float priorHeight = priorBox[3] - priorBox[1];
            float priorCenterX = (float) (0.5f * ( priorBox[2] + priorBox[0] ));
            float priorCenterY = (float) (0.5f * ( priorBox[3] + priorBox[1] ));

            float decodeBoxCenterX = boxLoc[0] * priorWidth * variance[0];
            decodeBoxCenterX = decodeBoxCenterX + priorCenterX;

            float decodeBoxCenterY = boxLoc[1] * priorHeight * variance[1];//priorWidth;// * variance[1];//priorWidth * variance[1];
            decodeBoxCenterY = decodeBoxCenterY + priorCenterY;

            float decodeBoxWidth = (float) Math.exp( boxLoc[2]*variance[2] );
            decodeBoxWidth = decodeBoxWidth * priorWidth;

            float decodeBoxHeight = (float) Math.exp( boxLoc[3]*variance[3] );
            decodeBoxHeight = decodeBoxHeight * priorHeight;

            float decodeBoxXMin = (float) (decodeBoxCenterX - 0.5f * decodeBoxWidth);
            float decodeBoxYMin = (float) (decodeBoxCenterY - 0.5f * decodeBoxHeight);
            float decodeBoxXmax = (float) (decodeBoxCenterX + 0.5f * decodeBoxWidth);
            float decodeBoxYmax = (float) (decodeBoxCenterY + 0.5f * decodeBoxHeight);

            float[] decodeBox = {   (float)Math.min( Math.max(decodeBoxXMin, 0.0f), 1.0f),
                                    (float)Math.min( Math.max(decodeBoxYMin, 0.0f), 1.0f),
                                    (float)Math.min( Math.max(decodeBoxXmax, 0.0f), 1.0f),
                                    (float)Math.min( Math.max(decodeBoxYmax, 0.0f), 1.0f)
                                };

            decodeBoxes[i] = decodeBox;
        }

        return decodeBoxes;
    }
}