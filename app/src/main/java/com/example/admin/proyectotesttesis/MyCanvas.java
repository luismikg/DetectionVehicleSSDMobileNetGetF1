package com.example.admin.proyectotesttesis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.MIN_PRIORITY;
import static java.lang.Thread.sleep;

public class MyCanvas extends View {

    Paint paint;
    Bitmap image;

    MainActivity mainActivity;

    /**
     * bounding boxes to draw.
     */
    ArrayList<float[]> boxes = new ArrayList();
    List<Box> boxesList;

    /**
     * Constructor.
     * @param context
     */
    public MyCanvas(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        this.paint = new Paint();
    }

//    @Override
//    public void refreshDrawableState() {
//        super.refreshDrawableState();
//        this.invalidate();
//    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        // No car detections since no image background.
        if(this.image == null)
        { return;}

        this.paint.setARGB(0, 255, 255, 255);
        canvas.drawRect(0, 0,1794, 1080,paint);

        //Draw image.
        canvas.drawBitmap( image, 0 ,0,null );


        //Init: bounding boxes.
        Paint paintBox = new Paint();
        paintBox.setStyle(Paint.Style.STROKE);
        paintBox.setColor(Color.RED);
        paintBox.setStrokeWidth(5);

        //draw bounding boxes.
//        for(int i=0; i<MyCanvas.this.boxes.size(); i++) {
//            float[] box = MyCanvas.this.boxes.get(i);
//            canvas.drawRect(box[0], box[1], box[2], box[3], paintBox);
//        }

        if(this.boxesList != null) {
            for (Box box : this.boxesList) {
                if(box.getConfidence()>0.30) {
                    int width = image.getWidth();
                    int height = image.getHeight();

                    canvas.drawRect(box.getxMin() * width,
                            box.getyMin() * height,
                            box.getxMax() * width,
                            box.getyMax() * height, paintBox);

                    paint.setColor(Color.BLACK);
                    paint.setTextSize(50);
                    canvas.drawText("s: " + box.getConfidence(), box.getxMin() * width, box.getyMin() * height, paint);
                }
            }
        }

        //End to take time.
        MainActivity.FINISH = System.nanoTime();
        long time = ( MainActivity.FINISH - MainActivity.START )/1000000;
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
        canvas.drawText("Time: "+time+" ms", 10, 100, paint);
        canvas.drawText("Time: "+time/1000.0+" s", 10, 200, paint);

        //Clear bounding boxes
        boxes = new ArrayList();
        this.boxesList = null;
    }

    /**
     * Set image background to detect cars.
     * @param image
     */
    public void setImage(Bitmap image) {
        this.image = image;
    }

    /**
     * Add bounding boxes to draw.
     * @param box
     */
    public void addBox(float[] box) {
        int width = image.getWidth();
        int height = image.getHeight();

        float[] boxWH = new float[6];
        boxWH[0] = box[0]*width;
        boxWH[1] = box[1]*height;
        boxWH[2] = box[2]*width;
        boxWH[3] = box[3]*height;
        boxWH[4] = boxWH[2]-boxWH[0];
        boxWH[5] = boxWH[3]-boxWH[1];
        this.boxes.add( boxWH );
        //this.postInvalidate();
    }

    public void setBoxes(List<Box> boxes){
        this.boxesList = boxes;
    }
}
