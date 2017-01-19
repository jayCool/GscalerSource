package algorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

/**
 *
 * @author workshop
 */
public class EdgeLink {

   
    public HashSet<ArrayList<Integer>> totalMathching = new HashSet<>();
   
    public void run(HashMap<ArrayList<Integer>, Integer> degreeMap, HashMap<ArrayList<ArrayList<Integer>>, Integer> nodePairMap) throws FileNotFoundException {
        HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> idDegree = new HashMap<>();

        settleIDRelated(degreeMap, degreeIDs, idDegree);
        settleAlmostRegular(nodePairMap, degreeIDs);
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
            }
        }
      
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
