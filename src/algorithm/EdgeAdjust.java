package algorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class EdgeAdjust extends Sort {
    long starttime = 0;
   
    EdgeAdjust(long currentTimeMillis) {
        this.starttime = currentTimeMillis;
    }

    private HashMap<Integer, ArrayList<Integer>> maximumRange(ArrayList<Integer> degreeList, ArrayList<Integer> value) {
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        for (int i = 0; i < degreeList.size(); i++) {
            if (value.get(i) > 0) {
                for (int j = i + 1; j < degreeList.size(); j++) {
                    ArrayList<Integer> arr = new ArrayList<>();
                    arr.add(i);
                    arr.add(j);
                    result.put(degreeList.get(j) - degreeList.get(i), arr);
                }

                for (int j = 0; j < degreeList.size(); j++) {
                    ArrayList<Integer> arr = new ArrayList<>();
                    arr.add(i);
                    arr.add(j);
                    result.put(degreeList.get(j) - degreeList.get(i), arr);

                }
            }
        }
        return result;
    }

    HashMap<Integer, Integer> smoothDegree(HashMap<Integer, Integer> scaleDegree, int scaledEdgeSize, int scaledNodeSize) throws FileNotFoundException {
        ArrayList<Integer> degreeList = new ArrayList<>();
        ArrayList<Integer> value = new ArrayList<>();

        closing_degree_gap(scaleDegree, degreeList, value);

        int edgeDiff = -product(degreeList, value) + scaledEdgeSize;
        
        int ender = value.size() - 1;
        int starter = 0;

        HashMap<Integer, ArrayList<Integer>> map = maximumRange(degreeList, value);
        boolean maxflag = false;

        while (!map.containsKey(edgeDiff) && edgeDiff != 0) {
            RunningException.checkTooLongRunTime(starttime);
            
            if (edgeDiff < 0) {
                if (value.get(ender) > 0) {
                    value.set(starter, value.get(starter) + 1);
                    value.set(ender, value.get(ender) - 1);
                    if (value.get(ender) <= 0) {
                        maxflag = true;
                    }
                    starter++;
                    ender--;
                } else {
                    ender--;
                }
            } else if (value.get(starter) > 0) {
                value.set(starter, value.get(starter) - 1);
                value.set(ender, value.get(ender) + 1);
                if (value.get(starter) <= 0) {
                    maxflag = true;
                }
                starter++;
                ender--;
            } else {
                starter++;
            }

            if (starter >= ender) {
                starter = 0;
                ender = value.size() - 1;
            }

            edgeDiff = scaledEdgeSize - product(degreeList, value);

            if (maxflag) {
                map = this.maximumRange(degreeList, value);
                maxflag = false;
            }
        }

        if (edgeDiff != 0) {
            ArrayList<Integer> arr = map.get(edgeDiff);
            value.set(arr.get(0), value.get(arr.get(0)) - 1);
            value.set(arr.get(1), value.get(arr.get(1)) + 1);

        }
        HashMap<Integer, Integer> res = new HashMap<>();

        for (int i = 0; i < degreeList.size(); i++) {
            res.put(degreeList.get(i), value.get(i));
        }
        return res;
    }

    private int product(ArrayList<Integer> x, ArrayList<Integer> value) {
        int sum = 0;
        for (int i = 0; i < x.size(); i++) {
            if (x.get(i) > 0 && value.get(i) > 0) {
                sum += x.get(i) * value.get(i);
            }
        }
        return sum;
    }

    private void closing_degree_gap(HashMap<Integer, Integer> scaleDegree, ArrayList<Integer> x, ArrayList<Integer> value) {
        int maxDegree = Collections.max(scaleDegree.keySet());
        for (int i = 0; i < maxDegree; i++) {
            if (!scaleDegree.containsKey(i)) {
                scaleDegree.put(i, 0);
            }
            x.add(i);
            value.add(scaleDegree.get(i));
        }
    }

}
