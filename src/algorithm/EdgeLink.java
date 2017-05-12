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
 * @author Zhang Jiangwei
 */
public class EdgeLink {
   
    public  HashSet<ArrayList<Integer>> generateLinks(HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis, 
            HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction) throws FileNotFoundException {
        HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> idDegree = new HashMap<>();

        calDegreeIDs(scaledJointDegreeDis, degreeIDs, idDegree);
        HashSet<ArrayList<Integer>> edgeList = edgeAssignment(scaledCorrelationFunction, degreeIDs);
        return edgeList;
    }
    
    /**
     * This method calculates the id to degrees and degree to IDs
     * @param scaledJointDegreeDis
     * @param degreeIDs
     * @param idDegree 
     */
    private void calDegreeIDs(HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis, 
            HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs, HashMap<Integer, ArrayList<Integer>> idDegree) {
        int id = 0;
        for (Entry<ArrayList<Integer>, Integer> entry : scaledJointDegreeDis.entrySet()) {
            ArrayList<Integer> jointDegree = entry.getKey();
            ArrayList<Integer> ids = new ArrayList<>();
            for (int i = 0; i < entry.getValue(); i++) {
                ids.add(id);
                idDegree.put(id, jointDegree);
                id++;
            }
            degreeIDs.put(jointDegree, ids);
        }
    }
    
    /**
     * 
     * @param scaledCorrelationFunction
     * @param degreeIDs
     * @return edgeList
     */
    private HashSet<ArrayList<Integer>> edgeAssignment(HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction, 
            HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs) {
        HashSet<ArrayList<Integer>> edgeList = new HashSet<>();
       
        HashMap<ArrayList<Integer>, Queue<Integer>> targetNodeQueue = new HashMap<>();
        HashMap<ArrayList<Integer>, Queue<Integer>> sourceNodeQueue = new HashMap<>();

        queueConstruction(targetNodeQueue, sourceNodeQueue, degreeIDs);

        for (Entry<ArrayList<ArrayList<Integer>>, Integer> edgeCorrelationEntry : scaledCorrelationFunction.entrySet()) {
            ArrayList<Integer> targetNodeDegree = edgeCorrelationEntry.getKey().get(0);
            ArrayList<Integer> sourceNodeDegree = edgeCorrelationEntry.getKey().get(1);
            int frequencies = edgeCorrelationEntry.getValue();
            
            while (frequencies > 0) {
                int targetNodeID = targetNodeQueue.get(targetNodeDegree).poll();
                int sourceNodeID = sourceNodeQueue.get(sourceNodeDegree).poll();
                ArrayList<Integer> formedEdge = new ArrayList<>();
                formedEdge.add(targetNodeID);
                formedEdge.add(sourceNodeID);

                //avoid repeating
                if (targetNodeID == sourceNodeID || edgeList.contains(formedEdge)) {
                    targetNodeQueue.get(targetNodeDegree).add(targetNodeID);
                    targetNodeID = targetNodeQueue.get(targetNodeDegree).poll();
                    formedEdge.clear();
                    formedEdge.add(targetNodeID);
                    formedEdge.add(sourceNodeID);
                }

                edgeList.add(formedEdge);
                frequencies--;
                targetNodeQueue.get(targetNodeDegree).add(targetNodeID);
                sourceNodeQueue.get(sourceNodeDegree).add(sourceNodeID);
            }
        }
        return edgeList;
    }
    
    
    /**
     * Construct the node queue
     * @param targetNodeQueue
     * @param sourceNodeQueue
     * @param degreeIDs 
     */
    private void queueConstruction(HashMap<ArrayList<Integer>, Queue<Integer>> targetNodeQueue, 
            HashMap<ArrayList<Integer>, Queue<Integer>> sourceNodeQueue, 
            HashMap<ArrayList<Integer>, ArrayList<Integer>> degreeIDs) {
        for (Entry<ArrayList<Integer>, ArrayList<Integer>> entry : degreeIDs.entrySet()) {
            Queue<Integer> lq = new LinkedList<>();
            Queue<Integer> rq = new LinkedList<>();
            for (Integer in : entry.getValue()) {
                lq.add(in);
                rq.add(in);
            }
            targetNodeQueue.put(entry.getKey(), lq);
            sourceNodeQueue.put(entry.getKey(), rq);
        }
    }

}
