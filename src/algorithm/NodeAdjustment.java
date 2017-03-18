/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class NodeAdjustment {

    private int sumVector(ArrayList<Integer> x) {
        int sum = 0;
        for (int y : x) {

            sum += y;
        }
        return sum;
    }

    private void extending_x_value(ArrayList<Integer> degreeList, ArrayList<Integer> value) {
        for (int i = 0; i < degreeList.get(degreeList.size() - 1); i++) {
            if (!degreeList.contains(i)) {
                degreeList.add(i, i);
                value.add(i, 0);
            }
        }
        for (int i = 0; i < Math.min(5, degreeList.size() / 5); i++) {
            degreeList.add(degreeList.size());
            value.add(0);
        }
    }

    public void adjustment(HashMap<Integer, Integer> degreeDis, int scaledNodeSize) {
        ArrayList<Integer> degreeList = new ArrayList<>();
        for (int degree : degreeDis.keySet()) {
            degreeList.add(degree);
        }
        Collections.sort(degreeList);

        ArrayList<Integer> value = new ArrayList<>();
        for (int degree : degreeList) {
            if (!degreeDis.containsKey(degree)) {
                value.add(0);
            } else {
                value.add(degreeDis.get(degree));
            }
        }

        extending_x_value(degreeList, value);

        int vtex_number = sumVector(value);
        int diffs = scaledNodeSize - vtex_number;
        for (int i = 0; i < degreeList.size(); i++) {
            double ratio = (int) 1.0 * value.get(i) / vtex_number;
            value.set(i, Math.max(0, (int) (ratio * diffs) + value.get(i)));
        }
        
        vtex_number = sumVector(value);
        diffs = scaledNodeSize - vtex_number;
        int adjustingIndex = 0;

        while (diffs != 0) {
            
            value.set(adjustingIndex, Math.max(0, value.get(adjustingIndex) + Math.abs(diffs) / diffs));
            adjustingIndex = (adjustingIndex + 1) % (value.size()-1);
            vtex_number = sumVector(value);
            diffs = scaledNodeSize - vtex_number;
        }
        degreeDis.clear();
       
        for (int i = 0; i < degreeList.size(); i++) {
            degreeDis.put(degreeList.get(i), value.get(i));
        }

    }

}
