package com.example.admin.proyectotesttesis;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class NonMaximumSuppression {

    public List<Box> nms( List<Box> boxesToProcess ) {
        return nms( boxesToProcess, 0.45f );
    }
    public List<Box> nms( final List<Box> boxesToProcess, float iouThresh ) {

        List<Integer> idxs = new LinkedList<>();
        List<Box> boxes = new LinkedList<>();

        if (boxesToProcess.size() == 0) {
            return boxes;
        }

        for (int i = 0; i < boxesToProcess.size(); i++) {
            idxs.add(new Integer(i));
        }

        Collections.sort(idxs, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {

                Box box1 = boxesToProcess.get(o1.intValue());
                Box box2 = boxesToProcess.get(o2.intValue());

                int returned = 0;
                if (box1.getConfidence() < box2.getConfidence()) {
                    returned = -1;
                }
                if (box1.getConfidence() > box2.getConfidence()) {
                    returned = 1;
                }

                return returned;
            }
        });

        //LinkedList pick = new LinkedList();
        while (!idxs.isEmpty()) {

            int last = idxs.size() - 1;
            int i = idxs.get(last);
            //pick.add(i);
            boxes.add(boxesToProcess.get(i));
            // List of boxes we want to ignore
            List<Integer> suppress = new LinkedList<>();
            suppress.add(last);

            for (int pos = 0; pos < last; pos++) {
                int j = idxs.get(pos);

                Box boxOne = boxesToProcess.get(i);
                Box boxTwo = boxesToProcess.get(j);
                float iou = this.getIntersectionOverUnion(boxOne, boxTwo);
                // if iou's box(j) is high with box(i), just get rid, because it probably correspond to the same object.
                if (iou > iouThresh) {
                    suppress.add(pos);
                }
            }

            Collections.sort(suppress, Collections.<Integer>reverseOrder());
            for (int idxToRemove : suppress) {
                idxs.remove(idxToRemove);
            }
        }
        return boxes;
    }

    private float getIntersectionOverUnion(Box boxOne, Box boxTwo) {
        float iou = 0.0f;

        //Area of overlap
        float xMin = Math.max(boxOne.getxMin(), boxTwo.getxMin());
        float yMin = Math.max(boxOne.getyMin(), boxTwo.getyMin());
        float xMax = Math.min(boxOne.getxMax(), boxTwo.getxMax());
        float yMax = Math.min(boxOne.getyMax(), boxTwo.getyMax());

        float a = (float) Math.max(0.0, (xMax - xMin));
        float b = (float) Math.max(0.0, (yMax - yMin));
        if(a > 0){a=a+1;}
        if(b > 0){b=b+1;}

        float areaOver = a * b;

        //float areaOver = Math.max(0, (xMax - xMin + 1)) * Math.max(0, (yMax - yMin + 1));

        //Get iou
        iou = areaOver / (boxOne.getArea() + boxTwo.getArea() - areaOver);
        return iou;
    }
}
