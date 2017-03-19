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

   
    
   
    public  HashSet<ArrayList<Integer>> run(HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis, 
            HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction) throws FileNotFoundException {
        HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> idDegree = new HashMap<>();

        calDegreeIDs(scaledJointDegreeDis, degreeIDs, idDegree);
        HashSet<ArrayList<Integer>> edgeList = 
        edgeAssignment(scaledCorrelationFunction, degreeIDs);
        return edgeList;
    }

    private void calDegreeIDs(HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis, 
            HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs, HashMap<Integer, ArrayList<Integer>> idDegree) {
        int id = 0;
        for (Entry<ArrayList<Integer>, Integer> entry : scaledJointDegreeDis.entrySet()) {
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
    private HashSet<ArrayList<Integer>> edgeAssignment(HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction, 
            HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs) {
         HashSet<ArrayList<Integer>> edgeList = new HashSet<>();
       
        HashMap<ArrayList<Integer>, Queue<Integer>> leftQueue = new HashMap<>();
        HashMap<ArrayList<Integer>, Queue<Integer>> rightQueue = new HashMap<>();

        queueConstruction(leftQueue, rightQueue, degreeIDs);

        for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : scaledCorrelationFunction.entrySet()) {
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
                if (leftid == rightid || edgeList.contains(arr)) {
                    leftQueue.get(leftDegree).add(leftid);
                    leftid = leftQueue.get(leftDegree).poll();
                    arr.clear();
                    arr.add(leftid);
                    arr.add(rightid);
                }

                edgeList.add(arr);
                value--;
                leftQueue.get(leftDegree).add(leftid);
                rightQueue.get(rightDegree).add(rightid);
            }
        }
        return edgeList;
      
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
