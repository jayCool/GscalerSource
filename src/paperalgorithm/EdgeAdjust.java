package paperalgorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

/**
 *
 * @author workshop
 */
class EdgeAdjust extends Sort {

    private double hardSmooth = 0.05;
    public int untouched = 10;
    double oneRatio = 1.0;
    double prevRatio = 1.0;
    int preVNum = 0;
    int breakIn = 0;
    double domainRatio = 1;
    int sourceAfter = 41500;
    public int calSourceAfter = 0;
    double stime = 0.1;
    long starttime = 0;
    String outputDir;

    EdgeAdjust(long currentTimeMillis) {
        this.starttime = currentTimeMillis;
    }
   
   
    private HashMap<Integer, ArrayList<Integer>> maximumRange(ArrayList<Integer> x, ArrayList<Integer> value) {
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        for (int i = 0; i < x.size(); i++) {
            if (value.get(i) > 0) {
                for (int j = i + 1; j < x.size(); j++) {
                    ArrayList<Integer> arr = new ArrayList<>();
                    arr.add(i);
                    arr.add(j);
                    result.put(x.get(j) - x.get(i), arr);
                }

                for (int j = 0; j < x.size(); j++) {
                    ArrayList<Integer> arr = new ArrayList<>();
                    arr.add(i);
                    arr.add(j);
                    result.put(x.get(j) - x.get(i), arr);

                }
            }
        }
        return result;
    }

    HashMap<Integer, Integer> smoothDstat_DBScale(HashMap<Integer, Integer> scaleDegree, int dependAfter, int sourceAfter) throws FileNotFoundException {
        ArrayList<Integer> x = new ArrayList<Integer>();
        int maxDegree = 0;
        maxDegree = Collections.max(scaleDegree.keySet());
        for (int i =0; i< maxDegree; i++){
            if (!scaleDegree.containsKey(i)){
                scaleDegree.put(i, 0);
            }
        }
        x.addAll(scaleDegree.keySet());
        Collections.sort(x);
        ArrayList<Integer> value = new ArrayList<>();
        for (int key : x) {
            value.add(scaleDegree.get(key));
        }
        
        System.out.println("befour: "+this.sumVector(value));
    
        int products = product(x, value);
        int diff = -products + dependAfter;
        int ender = value.size() - untouched - 1;
        int starter = 0;
        
        HashMap<Integer, ArrayList<Integer>> map = this.maximumRange(x, value);
        boolean maxflag=false;
        
        while (!map.containsKey(diff)) {
            if ((System.currentTimeMillis() - this.starttime)/1000 > 2000){
                PrintWriter pw = new PrintWriter(new File(this.outputDir+"/"+"exception.txt"));
                pw.println("Running Time Too Long(Greater Than 2000 Seconds)");
                pw.close();
                System.exit(-1);
            }
            
            if (diff < 0) {
                if (value.get(ender) > 0) {
                    value.set(starter, value.get(starter) + 1);
                    value.set(ender, value.get(ender) - 1);
                    if (value.get(ender)<=0) maxflag=true;
                    starter++;
                    ender--;
                } else {
                    ender--;
                }
            } else {
                if (value.get(starter) > 0) {
                    value.set(starter, value.get(starter) - 1);
                    value.set(ender, value.get(ender) + 1);
                    if (value.get(starter)<=0) maxflag=true;
                    starter++;
                    ender--;
                } else {
                    starter++;
                }
            }
            
            if (starter >= ender) {
                starter = 0;
                ender = value.size() - untouched - 1;
            }

            products = product(x, value);
            diff = dependAfter - products;
            
            if (maxflag){
                map = this.maximumRange(x, value);
                maxflag=false;
            }
        }
        
        if (diff != 0) {
            ArrayList<Integer> arr = map.get(diff);
            value.set(arr.get(0), value.get(arr.get(0)) - 1);
            value.set(arr.get(1), value.get(arr.get(1)) + 1);

        }
        products = product(x, value);
        HashMap<Integer, Integer> res = new HashMap<>();
        
        for (int i = 0; i < x.size(); i++) {
            res.put(x.get(i), value.get(i));
        }
        this.calSourceAfter = sumVector(value);
        System.out.println("source After:" + this.calSourceAfter);
        return res;
    }

    private int sumVector(ArrayList<Integer> x) {
        int sum = 0;
        for (int y : x) {
            sum += y;
        }
        return sum;
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

 
}