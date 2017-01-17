package paperalgorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Vector;

/**
 *
 * @author workshop
 */
public class Scaling extends PrintFunction {

    public int scaledNodeSize = 60704;
    public int secondPoint = 0;
    private double randomProb = 0.5;
    public double oneRatio = 1;
    public int domaingap = 0;
    public int breakIn = 0;
    public double s = 0.8;
    static int scaledEdgeSize = 411958;
    double se;

    /**
     * @param args the command line arguments
     */
    int untouched = 0;
    long starttime = 0;
    String outputDir;
    public HashMap<Integer, Integer> scale(HashMap<Integer, Integer> orders) throws FileNotFoundException {
    HashMap<Integer, Integer> scaleDegree = saticScale(orders);
        EdgeAdjust smoothing = new EdgeAdjust(System.currentTimeMillis());
       // this.starttime = System.currentTimeMillis();
        //smoothing.stattime = this.starttime;
        smoothing.breakIn = breakIn;
        smoothing.stime = this.s;
        smoothing.untouched = this.untouched;
        smoothing.outputDir = this.outputDir;
        HashMap<Integer, Integer> smoothDegree = smoothing.smoothDstat_DBScale(scaleDegree, scaledEdgeSize, this.scaledNodeSize);
        
        return smoothDegree;
        //  }
        //   return downsizeDegree;
    }

    private HashMap<Integer, Integer> saticScale(HashMap<Integer, Integer> orders) {
        HashMap<Integer, Integer> results = new HashMap<>();
        ArrayList<Integer> x = new ArrayList<>();
        double accu = 0;
        int total = 0;
        int used = 0;

        for (Entry<Integer, Integer> entry : orders.entrySet()) {
            total += entry.getValue();
            if (entry.getValue() * this.s < 1) {
                x.add(entry.getKey());
                double prob = Math.random();
                if (1.0 * entry.getValue() * this.s > prob) {
                    used++;
                    results.put(entry.getKey(), 1);
                }
                accu += 1.0 * entry.getValue() * this.s;
            } else {

                x.add(entry.getKey());

                double prob = Math.random();
                double gap = entry.getValue() * this.s - (int) (entry.getValue() * this.s);
                if (gap > prob) {
                    used += (int) (entry.getValue() * this.s) + 1;
                    results.put(entry.getKey(), (int) (entry.getValue() * this.s) + 1);
                } else {
                    results.put(entry.getKey(), (int) (entry.getValue() * this.s));
                    used += (int) (entry.getValue() * this.s);
                }
            }
        }

        Collections.sort(x);

        //===================Node Adjustment=================================//
        if (this.s < 1) {
            double interval = (1.0 * (x.size() - 2) / (total * this.s - used));
            for (int i = 0; i < total * this.s - used; i++) {
                int temp = x.get((int) (i * interval));
                if (results.containsKey(temp)) {
                    results.put(temp, results.get(temp) + 1);
                } else {
                    results.put(temp, 1);
                }
            }
        }

        ArrayList<Integer> value = new ArrayList<>();
        untouched = 0;
        System.out.println("untouched:" + untouched);
        for (int key : x) {
            if (!results.containsKey(key)) {
                value.add(0);
            } else {
                value.add(results.get(key));
            }
        }
        if (x.size() == 1) {
            for (int i = 0; i < x.get(x.size() - 1); i++) {
                x.add(i, i);
                value.add(i, 0);
            }
            for (int i = 0; i < 5; i++) {
                x.add(x.size());
                value.add(0);
            }
        }

        int vtex = this.sumVector(value);
        int diffs = this.scaledNodeSize - vtex;
        int threshold = 0;
        for (int i = 0; i <= threshold && i < x.size(); i++) {
            double rati = (int) 1.0 * value.get(i) / vtex;
            value.set(i, Math.max(0, (int) (rati * diffs) + value.get(i)));
            if (value.get(i) < 0) {
                int k = -value.get(i);
                for (int j = 0; j < value.size() && k > 0; j++) {
                    if (value.get(j) > 0) {
                        value.set(i, value.get(i) + 1);
                        k--;
                        value.set(j, value.get(j) - 1);
                    }
                }
            }
        }

        vtex = this.sumVector(value);
        diffs = scaledNodeSize - vtex;
        int kk = 0;

        while (diffs != 0) {
            if (kk == value.size() - 1) {
                kk = 0;
            }
            value.set(kk, Math.max(0, value.get(kk) + Math.abs(diffs) / diffs));
            kk++;
            vtex = this.sumVector(value);
            diffs = scaledNodeSize - vtex;
        }
          results.clear();
        for (int i=0;i<x.size();i++){
          
            results.put(x.get(i), value.get(i));
        }
        
        System.out.println(diffs);
        //====Node Adjustment done================================//
        return results;
    }

    private int sumVector(ArrayList<Integer> x) {
        int sum = 0;
        for (int y : x) {
            
            sum += y;
        }
        return sum;
    }

}