package algorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 /*
 * @author workshop
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CorrelationFunctionScaling {

    private double s_n = 0.2;
    private double s_e = 0.0;

    private HashMap<ArrayList<Integer>, Integer> scaleJointDegreeDis = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> sourecDisMap = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> targetDisMap = new HashMap<>();
    private HashMap<ArrayList<Integer>, Integer> originalJointDegreeDis;

    int level = 0;
    boolean finalFlag = false;

    List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortedCorrelation;

    HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders = new HashMap<>();
    int loop = 1;

    CorrelationFunctionScaling(HashMap<ArrayList<Integer>, Integer> jointdegreeDis,
            HashMap<ArrayList<Integer>, Integer> jointdegreeDis0, double s_e, double s_n) {
        scaleJointDegreeDis = jointdegreeDis;
        originalJointDegreeDis = jointdegreeDis0;
        this.s_e = s_e;
        this.s_n = s_n;
    }

    //The approach of the correlation is based on the original degree distribution.
    // 1. sort the degree in descending order. And settle the value from largest to the smallest
    // 2. Find the closest if the value is not found
    HashMap<ArrayList<ArrayList<Integer>>, Integer> run(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, 
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes) throws FileNotFoundException {
        loop = 0;
        HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale = new HashMap<>();

        produceCorrel(scaleSourceNodes, scaleTargetNodes, correlatedOriginal, correlatedScale);

        doubleChecking(correlatedScale);

        return correlatedScale;
    }

    //allocate those ids with enough credits, those leftovers deal with later.
    private void produceCorrel(
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) throws FileNotFoundException {

        sort_correlation(correlatedOriginal);

        int num = cal_min_leftOver(scaleSourceNodes, scaleTargetNodes);

        produceTraversalOrders();

        while (!scaleSourceNodes.keySet().isEmpty() && !scaleTargetNodes.keySet().isEmpty() && loop <= 2 && num > 1000) {
            processDistanceMap(scaleSourceNodes, sourecDisMap);
            processDistanceMap(scaleTargetNodes, targetDisMap);

            mapping(scaleSourceNodes, scaleTargetNodes, correlatedScale);

            num = cal_min_leftOver(scaleSourceNodes, scaleTargetNodes);
            loop += 2;
        }

        finalFlag = true;
        randomMapping(correlatedScale, scaleSourceNodes, scaleTargetNodes);
        num = cal_min_leftOver(scaleSourceNodes, scaleTargetNodes);

        rewiring(scaleSourceNodes, scaleTargetNodes, correlatedScale);

        num = cal_min_leftOver(scaleSourceNodes, scaleTargetNodes);
        if (num < 0){
            System.err.println("exception");
        }
    }

    // produce the order of traversal
    void produceTraversalOrders() {
        for (int i = 0; i < 2*Constant.CLEANING_THRESHOLD; i++) {
            ArrayList<ArrayList<Integer>> pairs = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                ArrayList<Integer> p1 = new ArrayList<>();
                p1.add(j);
                p1.add(i - j);
                pairs.add(p1);
            }
            traversalOrders.put(i, pairs);
        }
    }

    private HashMap<ArrayList<ArrayList<Integer>>, Integer> mapping(
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {

        int value = 0;
        level = (int) (Math.pow(10, loop - 1));

        for (int i = 0; i < sortedCorrelation.size() && !scaleSourceNodes.keySet().isEmpty() && !scaleTargetNodes.keySet().isEmpty(); i++) {
            Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry = sortedCorrelation.get(i);

            value = cal_value(entry);
            if (value == 0) {
                continue;
            }

            ArrayList<Integer> targetDegree = entry.getKey().get(0);
            ArrayList<Integer> sourceDegree = entry.getKey().get(1);
            updateValue(scaleSourceNodes, scaleTargetNodes, sourceDegree, targetDegree, traversalOrders, value, correlatedScale);
        }

        return correlatedScale;
    }

    private int cal_min_leftOver(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes) {
        int sum1 = 0;
        for (int v : scaleSourceNodes.values()) {
            sum1 += v;
        }
        int sum = 0;
        for (int v : scaleTargetNodes.values()) {
            sum += v;
        }

        return Math.min(sum1, sum);
    }

    private void randomMapping(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes) {
        ArrayList<ArrayList<Integer>> calSourceDegrees = new ArrayList<>(scaleSourceNodes.keySet());
        ArrayList<ArrayList<Integer>> calTargetDegrees = new ArrayList<>(scaleTargetNodes.keySet());

        for (ArrayList<Integer> calSourceDegree : calSourceDegrees) {
            if (scaleSourceNodes.get(calSourceDegree) == 0) {
                continue;
            }
            for (ArrayList<Integer> calTargetDegree : calTargetDegrees) {
                if (scaleSourceNodes.keySet().isEmpty() || scaleTargetNodes.keySet().isEmpty()) {
                    return;
                }

                if (!scaleSourceNodes.containsKey(calSourceDegree) || !scaleTargetNodes.containsKey(calTargetDegree)) {
                    continue;
                }

                int value = scaleSourceNodes.get(calSourceDegree);
                if (CleaningMap.cleanHashMap(scaleSourceNodes, calSourceDegree)){
                    break;
                }
                value = Math.min(value, scaleTargetNodes.get(calTargetDegree));
                if (CleaningMap.cleanHashMap(scaleTargetNodes, calTargetDegree)){
                    continue;
                }

                ArrayList<ArrayList<Integer>> edge_correlation = new ArrayList<>();
                edge_correlation.add(calTargetDegree);
                edge_correlation.add(calSourceDegree);

                int oldV = 0;
                if (correlatedScale.containsKey(edge_correlation)) {
                    oldV = correlatedScale.get(edge_correlation);
                }

                value = (int) Math.min(value, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * this.scaleJointDegreeDis.get(calTargetDegree) - oldV);
                if (calSourceDegree.equals(calTargetDegree)) {
                    value = (int) Math.min(value, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * (this.scaleJointDegreeDis.get(calTargetDegree) - 1) - oldV);
                }

                if (value <= 0) {
                    continue;
                }

                scaleSourceNodes.put(calSourceDegree, scaleSourceNodes.get(calSourceDegree) - value);
                scaleTargetNodes.put(calTargetDegree, scaleTargetNodes.get(calTargetDegree) - value);

                correlatedScale.put(edge_correlation, value + oldV);
                
                CleaningMap.cleanHashMap(scaleTargetNodes, calTargetDegree);
                CleaningMap.cleanHashMap(scaleSourceNodes, calSourceDegree);
               
            }
        }
    }

    private void rewiring(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {

        HashMap<ArrayList<ArrayList<Integer>>, Integer> tempResult = new HashMap<>();
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry : correlatedScale.entrySet()) {
            tempResult.put(entry.getKey(), entry.getValue());
        }
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortedEntries
                = new Sort().sortOnKeySum(tempResult);

        int mapCount = 0;

        ArrayList<ArrayList<Integer>> sourceList = extractSourceList(scaleSourceNodes);

        for (ArrayList<Integer> sourceDegree : sourceList) {
            if (CleaningMap.cleanHashMap(scaleSourceNodes, sourceDegree)) {
                continue;
            }

            if (mapCount > Constant.CLEANING_THRESHOLD) {
                CleaningMap.removeZero(scaleTargetNodes);
                mapCount = 0;
            }

            for (ArrayList<Integer> targetDegree : scaleTargetNodes.keySet()) {
                if (CleaningMap.cleanHashMap(scaleSourceNodes, sourceDegree)) {
                    break;
                }
                if (scaleTargetNodes.get(targetDegree) <= 0) {
                    continue;
                }
                for (int j = 0; j < sortedEntries.size(); j++) {
                    ArrayList<ArrayList<Integer>> originalMapping = sortedEntries.get(j).getKey();
                    int capMin = correlatedScale.get(originalMapping);
                    if (capMin > 0) {
                        ArrayList<Integer> originalTarget = originalMapping.get(0);
                        ArrayList<Integer> originalSource = originalMapping.get(1);

                        ArrayList<ArrayList<Integer>> pair1 = paring(targetDegree, originalSource);
                        ArrayList<ArrayList<Integer>> pair2 = paring(originalTarget, sourceDegree);

                        int count1 = getCount(pair1, correlatedScale);
                        int count2 = getCount(pair2, correlatedScale);

                        int cap1 = this.scaleJointDegreeDis.get(targetDegree) * this.scaleJointDegreeDis.get(originalSource) - count1;
                        int cap2 = this.scaleJointDegreeDis.get(originalTarget) * this.scaleJointDegreeDis.get(sourceDegree) - count2;

                        capMin = getMinimum(cap1, cap2, capMin, scaleTargetNodes, targetDegree, scaleSourceNodes, sourceDegree);

                        if (!pair1.equals(pair2) && !originalTarget.equals(sourceDegree) && !targetDegree.equals(originalSource) && capMin > 0) {
                            int reduce = capMin;
                            correlatedScale.put(originalMapping, correlatedScale.get(originalMapping) - reduce);
                            correlatedScale.put(pair2, count2 + reduce);
                            correlatedScale.put(pair1, count1 + reduce);
                            scaleSourceNodes.put(sourceDegree, scaleSourceNodes.get(sourceDegree) - reduce);
                            scaleTargetNodes.put(targetDegree, scaleTargetNodes.get(targetDegree) - reduce);

                            if (scaleTargetNodes.get(targetDegree) <= 0) {
                                mapCount++;
                            }
                            if (scaleTargetNodes.get(targetDegree) <= 0 || scaleSourceNodes.get(sourceDegree) <= 0) {
                                break;
                            }
                        }
                    }
                }

            }
        }

    }

    private void processDistanceMap(HashMap<ArrayList<Integer>, Integer> calSource, HashMap<Integer, ArrayList<ArrayList<Integer>>> sourceDisMap) {
        sourceDisMap.clear();

        for (Map.Entry<ArrayList<Integer>, Integer> sourceEntry : calSource.entrySet()) {
            int sum = 0;
            if (sourceEntry.getValue() <= 0) {
                continue;
            }
            sum = sourceEntry.getKey().get(0) + sourceEntry.getKey().get(1);
            if (!sourceDisMap.containsKey(sum)) {
                sourceDisMap.put(sum, new ArrayList<ArrayList<Integer>>());
            }
            sourceDisMap.get(sum).add(sourceEntry.getKey());
        }
    }

    private void updateValue(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            ArrayList<Integer> sourceDegree, ArrayList<Integer> targetDegree,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders, int value,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {

        ArrayList<ArrayList<Integer>> sourcePools = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> targetPools = new ArrayList<ArrayList<Integer>>();

        int sourceSum = sourceDegree.get(0) + sourceDegree.get(1);
        int targetSum = targetDegree.get(0) + targetDegree.get(1);

        calCandidatePool(sourceSum, targetSum, scaleSourceNodes, scaleTargetNodes, sourcePools, targetPools);

        HashMap<Integer, ArrayList<ArrayList<Integer>>> calSourceOrdered = norm1Closest(sourceDegree, sourcePools);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> calTargetOrdered = norm1Closest(targetDegree, targetPools);

        traversal(traversalOrders, calSourceOrdered, calTargetOrdered, scaleSourceNodes, scaleTargetNodes, value, correlatedScale);

    }

    private HashMap<Integer, ArrayList<ArrayList<Integer>>> norm1Closest(
            ArrayList<Integer> sourceDegree, ArrayList<ArrayList<Integer>> sourcePools) {

        HashMap<Integer, ArrayList<ArrayList<Integer>>> ordered = new HashMap<>();
        int max = 0;

        for (ArrayList<Integer> sample : sourcePools) {
            int diff = Math.abs(sample.get(0) - sourceDegree.get(0)) + Math.abs(sample.get(1) - sourceDegree.get(1));
            max = Math.max(diff, max);

            if (!ordered.containsKey(sample)) {
                ordered.put(diff, new ArrayList<ArrayList<Integer>>());
            }
            ordered.get(diff).add(sample);
        }

        ordered.put(0, new ArrayList<ArrayList<Integer>>());
        ordered.get(0).add(sourceDegree);
        return ordered;
    }

    private void calCandidatePool(int sourceSum, int targetSum, HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            ArrayList<ArrayList<Integer>> sourcePools, ArrayList<ArrayList<Integer>> targetPools) {
        for (int i = -1 * level; i <= level; i++) {
            if (sourecDisMap.containsKey(i + sourceSum)) {
                for (ArrayList<Integer> r : sourecDisMap.get(i + sourceSum)) {
                    if (scaleSourceNodes.containsKey(r)) {
                        sourcePools.add(r);
                    }
                }
            }
            if (targetDisMap.containsKey(i + targetSum)) {
                for (ArrayList<Integer> r : targetDisMap.get(i + targetSum)) {
                    if (scaleTargetNodes.containsKey(r)) {
                        targetPools.add(r);
                    }
                }
            }

        }
    }

    private void traversal(HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> calSourceOrdered,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> calTargetOrdered,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, int value,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {
        int budget = value;

        for (int k = 0; k < 10 && k < traversalOrders.size(); k++) {
            for (ArrayList<Integer> pair : traversalOrders.get(k)) {
                if (calTargetOrdered.containsKey(pair.get(1)) && calSourceOrdered.containsKey(pair.get(0))) {
                    ArrayList<ArrayList<Integer>> calSourceDegrees = calSourceOrdered.get(pair.get(0));
                    ArrayList<ArrayList<Integer>> calTargetDegrees = calTargetOrdered.get(pair.get(1));
                    for (ArrayList<Integer> calTargetDegree : calTargetDegrees) {
                        for (ArrayList<Integer> calSourceDegree : calSourceDegrees) {

                            ArrayList<ArrayList<Integer>> edgeCorrelation = new ArrayList<>();
                            edgeCorrelation.add(calTargetDegree);
                            edgeCorrelation.add(calSourceDegree);

                            if (scaleSourceNodes.containsKey(calSourceDegree) && scaleTargetNodes.containsKey(calTargetDegree)) {
                                value = Math.min(value, scaleSourceNodes.get(calSourceDegree));
                                value = Math.min(value, scaleTargetNodes.get(calTargetDegree));
                                if (!correlatedScale.containsKey(edgeCorrelation)) {
                                    correlatedScale.put(edgeCorrelation, 0);
                                }
                                value = (int) Math.min(value, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * this.scaleJointDegreeDis.get(calTargetDegree) - correlatedScale.get(edgeCorrelation));
                                if (calSourceDegree.equals(calTargetDegree)) {
                                    value = (int) Math.min(value, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * (this.scaleJointDegreeDis.get(calTargetDegree) - 1) - correlatedScale.get(edgeCorrelation));
                                }

                                if (value <= 0) {
                                    value = 0;
                                }

                                scaleSourceNodes.put(calSourceDegree, scaleSourceNodes.get(calSourceDegree) - value);
                                scaleTargetNodes.put(calTargetDegree, scaleTargetNodes.get(calTargetDegree) - value);
                                correlatedScale.put(edgeCorrelation, value + correlatedScale.get(edgeCorrelation));
                                
                                CleaningMap.cleanHashMap(scaleSourceNodes, calSourceDegree);
                                CleaningMap.cleanHashMap(scaleTargetNodes, calTargetDegree);
                                
                               
                                budget = budget - value;
                                if (budget == 0) {
                                    return;
                                }
                                value = budget;
                            }
                        }
                    }
                }

            }
        }
    }

  

    private ArrayList<ArrayList<Integer>> paring(ArrayList<Integer> targetDegree ,ArrayList<Integer> sourceDegree) {
        ArrayList<ArrayList<Integer>> pair1 = new ArrayList<>();
        pair1.add(targetDegree);
        pair1.add(sourceDegree);
        return pair1;
    }

    private int getCount(ArrayList<ArrayList<Integer>> pair1, HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {
        int count = 0;
        if (correlatedScale.containsKey(pair1)) {
            count = correlatedScale.get(pair1);
        }
        return count;

    }

    private int getMinimum(int cap1, int cap2, int capMin, HashMap<ArrayList<Integer>, Integer> calUser, ArrayList<Integer> user, HashMap<ArrayList<Integer>, Integer> calTweet, ArrayList<Integer> tweet) {
        capMin = Math.min(cap1, capMin);
        capMin = Math.min(cap2, capMin);
        capMin = Math.min(capMin, calUser.get(user));
        capMin = Math.min(capMin, calTweet.get(tweet));
        return capMin;
    }

    private void doubleChecking(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale ) {
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry : correlatedScale.entrySet()) {
            int value = 0;
            if (entry.getKey().get(0).equals(entry.getKey().get(1))) {
                value = (int) (1.0 * this.scaleJointDegreeDis.get(entry.getKey().get(0)) * (this.scaleJointDegreeDis.get(entry.getKey().get(0)) - 1) - entry.getValue());
            } else {
                value = (int) (1.0 * this.scaleJointDegreeDis.get(entry.getKey().get(0)) * (this.scaleJointDegreeDis.get(entry.getKey().get(1)) - 0) - entry.getValue());
            }

            if (value < 0) {
                System.err.println("negative value in correlation function");
            }

            if (entry.getValue() > 0) {
                correlatedScale.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void sort_correlation(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal) {
        Sort so = new Sort();
        sortedCorrelation = so.sortOnAppearance(correlatedOriginal, originalJointDegreeDis);
    }

    private int cal_value(Entry<ArrayList<ArrayList<Integer>>, Integer> entry) {
        double floatValue = entry.getValue() * s_n * (1 + Math.max(0, s_e));
        int value = (int) (floatValue);
        double difff = floatValue - value;

        double kl = Math.random();
        if (kl < difff) {
            value++;
        }
        return value;
    }

    private ArrayList<ArrayList<Integer>> extractSourceList(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes) {
        ArrayList<ArrayList<Integer>> sourceSet = new ArrayList<>();
        for (ArrayList<Integer> tweet : scaleSourceNodes.keySet()) {
            sourceSet.add(tweet);
        }
        return sourceSet;
    }

}
