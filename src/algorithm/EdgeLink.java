package algorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author workshop
 */
public class EdgeLink {

    /**
     * @param args the command line arguments
     */
    public String degreeInput = "0.2degree.txt";
    public String pairInput = "0.2degreePair.txt";
    HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDsc = new HashMap<>();
    int totalSize = 0;
    int curSize = 0;
    int osize = 0;
    int tottt = 0;
    public HashSet<ArrayList<Integer>> totalMathching = new HashSet<>();
    ArrayList<Integer> leftUnsettled = new ArrayList<>();
    ArrayList<Integer> rightUnsettled = new ArrayList<>();
    public HashMap<ArrayList<ArrayList<Integer>>, ArrayList<ArrayList<Integer>>> mappingDegree2IDs = new HashMap<>(); //the key is the degree of the mapping, and the key is the ids that have that id;

    public void run(HashMap<ArrayList<Integer>, Integer> degreeMap, HashMap<ArrayList<ArrayList<Integer>>, Integer> pairMap) throws FileNotFoundException {
        HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> idDegree = new HashMap<>();

        System.out.println("assign ID start");
        settleIDRelated(degreeMap, degreeIDs, idDegree);
        System.out.println("assign ID done");

        settleAlmostRegular(pairMap, degreeIDs);
        System.out.println("=============total: " + totalSize);
        System.out.println("=============oSize:" + osize);
        System.out.println("=============curSize:" + totalMathching.size());

    }

    private void settleIDRelated(HashMap<ArrayList<Integer>, Integer> degreeMap, HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs, HashMap<Integer, ArrayList<Integer>> idDegree) {
        int id = 0;
        for (Entry<ArrayList<Integer>, Integer> entry : degreeMap.entrySet()) {
            ArrayList<Integer> arr = entry.getKey();
            ArrayList<Integer> temp = new ArrayList<>();
            for (int i = 0; i < entry.getValue(); i++) {
                temp.add(id);
                idDegree.put(id, arr);
                id++;
            }
            degreeIDs.put(arr, temp);
        }
    }

    public List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortOnValue(HashMap<ArrayList<ArrayList<Integer>>, Integer> orders) {
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted = new ArrayList<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>>(orders.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>>() {
            public int compare(Map.Entry<ArrayList<ArrayList<Integer>>, Integer> o1, Map.Entry<ArrayList<ArrayList<Integer>>, Integer> o2) {
                return (o1.getValue() - o2.getValue());
            }
        });
        return sorted;
    }

    public List<Map.Entry<Integer, Integer>> sortOnValueI(HashMap<Integer, Integer> orders) {
        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<Map.Entry<Integer, Integer>>(orders.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        return sorted;
    }

    //this will produce the detailed mapping to ids
    private void settleAlmostRegular(HashMap<ArrayList<ArrayList<Integer>>, Integer> pairMap, HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs) {
        HashMap<ArrayList<Integer>, Queue<Integer>> leftQueue = new HashMap<>();
        HashMap<ArrayList<Integer>, Queue<Integer>> rightQueue = new HashMap<>();

        queueConstruction(leftQueue, rightQueue, degreeIDs);

        for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : pairMap.entrySet()) {
            ArrayList<Integer> leftDegree = entry.getKey().get(0);
            ArrayList<Integer> rightDegree = entry.getKey().get(1);
            int leftid = 0;
            int rightid = 0;
            int value = entry.getValue();
            while (value > 0) {
                leftid = leftQueue.get(leftDegree).poll();
                rightid = rightQueue.get(rightDegree).poll();
                ArrayList<Integer> arr = new ArrayList<Integer>();
                arr.add(leftid);
                arr.add(rightid);

                //avoid repeat
                if (leftid == rightid || totalMathching.contains(arr)) {
                    leftQueue.get(leftDegree).add(leftid);
                    leftid = leftQueue.get(leftDegree).poll();
                    arr.clear();
                    arr.add(leftid);
                    arr.add(rightid);
                }

                this.totalMathching.add(arr);
                value--;
                leftQueue.get(leftDegree).add(leftid);
                rightQueue.get(rightDegree).add(rightid);
                tottt++;
            }
        }
        System.out.println("total " + tottt);

    }

    private void queueConstruction(HashMap<ArrayList<Integer>, Queue<Integer>> leftQueue, HashMap<ArrayList<Integer>, Queue<Integer>> rightQueue, HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs) {
        for (Entry<ArrayList<Integer>, ArrayList<Integer>> entry : degreeIDs.entrySet()) {
            Queue<Integer> lq = new LinkedList<Integer>();
            Queue<Integer> rq = new LinkedList<Integer>();
            for (Integer in : entry.getValue()) {
                lq.add(in);
                rq.add(in);
            }
            leftQueue.put(entry.getKey(), lq);
            rightQueue.put(entry.getKey(), rq);
        }
    }

}
