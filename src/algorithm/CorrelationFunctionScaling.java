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

    public double stime = 0.2;
    HashMap<ArrayList<Integer>, Integer> scaleJoinDegree_Dis = new HashMap<>();
    HashMap<ArrayList<ArrayList<Integer>>, Integer> result = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> sourecDisMap = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> targetDisMap = new HashMap<>();
    HashMap<ArrayList<ArrayList<Integer>>, Integer> original_joint_degree_dis;
    double se = 0.0;
    int level = 0;
    boolean finalFlag = false;

    List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted_correlation;

    HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders = new HashMap<>();
    int loop = 1;

    //The approach of the correlation is based on the original degree distribution.
    // 1. sort the degree in descending order. And settle the value from largest to the smallest
    // 2. Find the closest if the value is not found
    HashMap<ArrayList<ArrayList<Integer>>, Integer> run(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal,
            HashMap<ArrayList<Integer>, Integer> calTarget, HashMap<ArrayList<Integer>, Integer> calSource) throws FileNotFoundException {
        loop = 0;

        HashMap<ArrayList<ArrayList<Integer>>, Integer> resultMapping
                = produceCorrel(calSource, calTarget, correlatedOriginal);

        double_checking(resultMapping);

        return result;
    }

    //allocate those ids with enough credits, those leftovers deal with later.
    private HashMap<ArrayList<ArrayList<Integer>>, Integer> produceCorrel(
            HashMap<ArrayList<Integer>, Integer> calSource, HashMap<ArrayList<Integer>, Integer> calTarget,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal) throws FileNotFoundException {

        sort_correlation(correlatedOriginal);

        int num = cal_min_leftOver(calSource, calTarget);

        produceTraversalOrders();

        while (!calSource.keySet().isEmpty() && !calTarget.keySet().isEmpty() && loop <= 2 && num > 1000) {
            sourecDisMap.clear();
            targetDisMap.clear();
            processDistanceMap(calSource, sourecDisMap);
            processDistanceMap(calTarget, targetDisMap);

            mapping_DSA(calSource, calTarget);
            num = cal_min_leftOver(calSource, calTarget);
            loop++;
            loop++;
        }

        finalFlag = true;
        randomMap(result, calSource, calTarget);
        num = cal_min_leftOver(calSource, calTarget);

        rewiring(calSource, calTarget);

        num = cal_min_leftOver(calSource, calTarget);
        if (num !=0){
            System.err.println("something wrong! edge is not full");
        }
        return result;
    }

    // produce the order of traversal
    void produceTraversalOrders() {
        for (int i = 0; i < 20; i++) {
            ArrayList<ArrayList<Integer>> pairs = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                ArrayList<Integer> p1 = new ArrayList<>();
                p1.add(j);
                p1.add(i - j);
                pairs.add(p1);
                traversalOrders.put(i, pairs);
            }
        }
    }

    private HashMap<ArrayList<ArrayList<Integer>>, Integer> mapping_DSA(
            HashMap<ArrayList<Integer>, Integer> calSource, HashMap<ArrayList<Integer>, Integer> calTarget) {
        int value = 0;
        level = (int) (Math.pow(10, loop - 1));

        for (int i = 0; i < sorted_correlation.size() && !calSource.keySet().isEmpty() && !calTarget.keySet().isEmpty(); i++) {
            Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry = sorted_correlation.get(i);

            value = cal_value(entry);
            if (value == 0) {
                continue;
            }

            ArrayList<Integer> targetDegree = entry.getKey().get(0);
            ArrayList<Integer> sourceDegree = entry.getKey().get(1);

            updateValue(calSource, calTarget, sourceDegree, targetDegree, traversalOrders, value);
        }

        return result;
    }

    private int cal_min_leftOver(HashMap<ArrayList<Integer>, Integer> calSource, HashMap<ArrayList<Integer>, Integer> calTarget) {
        int sum1 = 0;
        for (int v : calSource.values()) {
            sum1 += v;
        }
        int sum = 0;
        for (int v : calTarget.values()) {
            sum += v;
        }

        return Math.min(sum1, sum);
    }

    private void randomMap(HashMap<ArrayList<ArrayList<Integer>>, Integer> result, 
            HashMap<ArrayList<Integer>, Integer> calSource, HashMap<ArrayList<Integer>, Integer> calTarget) {
        ArrayList<ArrayList<Integer>> calSourceDegrees = new ArrayList<>(calSource.keySet());
        ArrayList<ArrayList<Integer>> calTargetDegrees = new ArrayList<>(calTarget.keySet());

        for (ArrayList<Integer> calSourceDegree : calSourceDegrees) {
            if (calSource.get(calSourceDegree) == 0) {
                continue;
            }
            for (ArrayList<Integer> calTargetDegree : calTargetDegrees) {
                if (calSource.keySet().isEmpty() || calTarget.keySet().isEmpty()) {
                    return;
                }

                if (!calSource.containsKey(calSourceDegree) || !calTarget.containsKey(calTargetDegree)) {
                    continue;
                }

                int value = calSource.get(calSourceDegree);
                if (value == 0) {
                    calSource.remove(calSourceDegree);
                    break;
                }

                value = Math.min(value, calTarget.get(calTargetDegree));
                if (value == 0) {
                    calTarget.remove(calTargetDegree);
                    continue;
                }

                ArrayList<ArrayList<Integer>> edge_correlation = new ArrayList<>();
                edge_correlation.add(calTargetDegree);
                edge_correlation.add(calSourceDegree);

                int oldV = 0;
                if (result.containsKey(edge_correlation)) {
                    oldV = result.get(edge_correlation);
                }

                value = (int) Math.min(value, 1.0 * this.scaleJoinDegree_Dis.get(calSourceDegree) * this.scaleJoinDegree_Dis.get(calTargetDegree) - oldV);
                if (calSourceDegree.equals(calTargetDegree)) {
                    value = (int) Math.min(value, 1.0 * this.scaleJoinDegree_Dis.get(calSourceDegree) * (this.scaleJoinDegree_Dis.get(calTargetDegree) - 1) - oldV);
                }

                if (value <= 0) {
                    continue;
                }

                calSource.put(calSourceDegree, calSource.get(calSourceDegree) - value);
                calTarget.put(calTargetDegree, calTarget.get(calTargetDegree) - value);

                result.put(edge_correlation, value + oldV);

                removeZero(calSource, calSourceDegree);
                removeZero(calTarget, calTargetDegree);

            }
        }
    }

    private void rewiring(HashMap<ArrayList<Integer>, Integer> calSource, HashMap<ArrayList<Integer>, Integer> calTarget) {

        HashMap<ArrayList<ArrayList<Integer>>, Integer> tempResult = new HashMap<>();
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted;
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> originalM : result.entrySet()) {
            tempResult.put(originalM.getKey(), originalM.getValue());
        }
        sorted = new Sort().sortOnKeySum(tempResult);

        int mapCount = 0;
        int thresh = 10;

        ArrayList<ArrayList<Integer>> sourceSet = cal_source_set(calSource);
      
        for (ArrayList<Integer> sourceDegree : sourceSet) {
            if (calSource.get(sourceDegree) <= 0) {
                calSource.remove(sourceDegree);
                continue;
            }

            if (mapCount > thresh) {
                cleanCalUser(calTarget);
                mapCount = 0;
            }

            for (ArrayList<Integer> targetDegree : calTarget.keySet()) {
                if (calSource.get(sourceDegree) <= 0) {
                    calSource.remove(sourceDegree);
                    break;
                }
                if (calTarget.get(targetDegree) <= 0) {
                    continue;
                }
                for (int j = 0; j < sorted.size(); j++) {
                    ArrayList<ArrayList<Integer>> originalMapping = sorted.get(j).getKey();
                    int capMin = result.get(originalMapping);
                    if (capMin > 0) {
                        ArrayList<Integer> oriUser = originalMapping.get(0);
                        ArrayList<Integer> oriTweet = originalMapping.get(1);

                        ArrayList<ArrayList<Integer>> pair1 = paring(targetDegree, oriTweet);

                        ArrayList<ArrayList<Integer>> pair2 = paring(oriUser, sourceDegree);

                        int r1 = getResult(pair1);
                        int r2 = getResult(pair2);

                        int cap1 = this.scaleJoinDegree_Dis.get(targetDegree) * this.scaleJoinDegree_Dis.get(oriTweet) - r1;
                        int cap2 = this.scaleJoinDegree_Dis.get(oriUser) * this.scaleJoinDegree_Dis.get(sourceDegree) - r2;

                        capMin = getMinimum(cap1, cap2, capMin, calTarget, targetDegree, calSource, sourceDegree);

                        if (!pair1.equals(pair2) && !oriUser.equals(sourceDegree) && !targetDegree.equals(oriTweet) && capMin > 0) {
                            int reduce = capMin;
                            result.put(originalMapping, result.get(originalMapping) - reduce);
                            result.put(pair2, r2 + reduce);
                            result.put(pair1, r1 + reduce);
                            calSource.put(sourceDegree, calSource.get(sourceDegree) - reduce);
                            calTarget.put(targetDegree, calTarget.get(targetDegree) - reduce);

                            if (calTarget.get(targetDegree) <= 0) {
                                mapCount++;
                            }
                            if (calTarget.get(targetDegree) <= 0 || calSource.get(sourceDegree) <= 0) {
                                break;
                            }
                        }
                    }
                }

            }
        }

    }

    private void processDistanceMap(HashMap<ArrayList<Integer>, Integer> calSource, HashMap<Integer, ArrayList<ArrayList<Integer>>> sourceDisMap) {
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

    private void updateValue(HashMap<ArrayList<Integer>, Integer> calSource, HashMap<ArrayList<Integer>, Integer> calTarget,
            ArrayList<Integer> sourceDegree, ArrayList<Integer> targetDegree,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> transOrders, int value) {
        ArrayList<ArrayList<Integer>> tempSource = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> tempTarget = new ArrayList<ArrayList<Integer>>();

        int sourceSum = sourceDegree.get(0) + sourceDegree.get(1);
        int targetSum = targetDegree.get(0) + targetDegree.get(1);

        calCandidatePool(sourceSum, targetSum, calSource, calTarget, tempSource, tempTarget);

        HashMap<Integer, ArrayList<ArrayList<Integer>>> calSourceOrdered = norm1Closest(sourceDegree, tempSource);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> calTargetOrdered = norm1Closest(targetDegree, tempTarget);
        int budget = value;

        travelsal(transOrders, calSourceOrdered, calTargetOrdered, calSource, calTarget, value, budget);

    }

    private HashMap<Integer, ArrayList<ArrayList<Integer>>> norm1Closest(
            ArrayList<Integer> sourceDegree, ArrayList<ArrayList<Integer>> tempSource) {

        HashMap<Integer, ArrayList<ArrayList<Integer>>> ordered = new HashMap<>();
        int max = 0;

        for (ArrayList<Integer> sample : tempSource) {
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

    private void calCandidatePool(int sourceSum, int targetSum, HashMap<ArrayList<Integer>, Integer> calSource,
            HashMap<ArrayList<Integer>, Integer> calTarget,
            ArrayList<ArrayList<Integer>> tempSource, ArrayList<ArrayList<Integer>> tempTarget) {
        for (int i = -1 * level; i <= level; i++) {
            if (sourecDisMap.containsKey(i + sourceSum)) {
                for (ArrayList<Integer> r : sourecDisMap.get(i + sourceSum)) {
                    if (calSource.containsKey(r)) {
                        tempSource.add(r);
                    }
                }
            }
            if (targetDisMap.containsKey(i + targetSum)) {
                for (ArrayList<Integer> r : targetDisMap.get(i + targetSum)) {
                    if (calTarget.containsKey(r)) {
                        tempTarget.add(r);
                    }
                }
            }

        }
    }

    private void travelsal(HashMap<Integer, ArrayList<ArrayList<Integer>>> transOrders, 
            HashMap<Integer, ArrayList<ArrayList<Integer>>> calSourceOrdered, 
            HashMap<Integer, ArrayList<ArrayList<Integer>>> calTargetOrdered, 
            HashMap<ArrayList<Integer>, Integer> calSource, 
            HashMap<ArrayList<Integer>, Integer> calTarget, int value, int budget) {
        
        for (int k = 0; k < 10 && k < transOrders.size(); k++) {
            for (ArrayList<Integer> pair: transOrders.get(k)){    
                if (calTargetOrdered.containsKey(pair.get(1)) && calSourceOrdered.containsKey(pair.get(0))) {
                    ArrayList<ArrayList<Integer>> calSourceDegrees = calSourceOrdered.get(pair.get(0));
                    ArrayList<ArrayList<Integer>> calTargetDegrees = calTargetOrdered.get(pair.get(1));
                    for (ArrayList<Integer> calTargetDegree : calTargetDegrees) {
                        for (ArrayList<Integer> calSourceDegree : calSourceDegrees) {
                            
                            ArrayList<ArrayList<Integer>> edgeCorrelation = new ArrayList<>();
                            edgeCorrelation.add(calTargetDegree);
                            edgeCorrelation.add(calSourceDegree);
                            
                            if (calSource.containsKey(calSourceDegree) && calTarget.containsKey(calTargetDegree)) {
                                value = Math.min(value, calSource.get(calSourceDegree));
                                value = Math.min(value, calTarget.get(calTargetDegree));
                                if (!result.containsKey(edgeCorrelation)) {
                                    result.put(edgeCorrelation, 0);
                                }
                                value = (int) Math.min(value, 1.0 * this.scaleJoinDegree_Dis.get(calSourceDegree) * this.scaleJoinDegree_Dis.get(calTargetDegree) - result.get(edgeCorrelation));
                                if (calSourceDegree.equals(calTargetDegree)) {
                                    value = (int) Math.min(value, 1.0 * this.scaleJoinDegree_Dis.get(calSourceDegree) * (this.scaleJoinDegree_Dis.get(calTargetDegree) - 1) - result.get(edgeCorrelation));
                                }

                                if (value <= 0) {
                                    value = 0;
                                }

                                calSource.put(calSourceDegree, calSource.get(calSourceDegree) - value);
                                calTarget.put(calTargetDegree, calTarget.get(calTargetDegree) - value);
                                result.put(edgeCorrelation, value + result.get(edgeCorrelation));

                                if (calSource.get(calSourceDegree) == 0) {
                                    calSource.remove(calSourceDegree);
                                }

                                if (calTarget.get(calTargetDegree) == 0) {
                                    calTarget.remove(calTargetDegree);
                                }
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

    private void removeZero(HashMap<ArrayList<Integer>, Integer> calSource, ArrayList<Integer> calSourceDegree) {
        if (calSource.get(calSourceDegree) == 0) {
            calSource.remove(calSourceDegree);
        }
    }

    private void cleanCalUser(HashMap<ArrayList<Integer>, Integer> calUser) {
        Set<ArrayList<Integer>> userSets = new HashSet<>();
        for (ArrayList<Integer> temp : calUser.keySet()) {
            userSets.add(temp);
        }
        for (ArrayList<Integer> temp : userSets) {
            if (calUser.get(temp) <= 0) {
                calUser.remove(temp);
            }
        }
    }

    private ArrayList<ArrayList<Integer>> paring(ArrayList<Integer> user, ArrayList<Integer> oriTweet) {
        ArrayList<ArrayList<Integer>> pair1 = new ArrayList<>();
        pair1.add(user);
        pair1.add(oriTweet);
        return pair1;
    }

    private int getResult(ArrayList<ArrayList<Integer>> pair1) {
        int cap = 0;
        if (result.containsKey(pair1)) {
            cap = result.get(pair1);
        }
        return cap;

    }

    private int getMinimum(int cap1, int cap2, int capMin, HashMap<ArrayList<Integer>, Integer> calUser, ArrayList<Integer> user, HashMap<ArrayList<Integer>, Integer> calTweet, ArrayList<Integer> tweet) {
        capMin = Math.min(cap1, capMin);
        capMin = Math.min(cap2, capMin);
        capMin = Math.min(capMin, calUser.get(user));
        capMin = Math.min(capMin, calTweet.get(tweet));
        return capMin;
    }

    private void double_checking(HashMap<ArrayList<ArrayList<Integer>>, Integer> resultMapping) {
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry : resultMapping.entrySet()) {
            int value = 0;
            if (entry.getKey().get(0).equals(entry.getKey().get(1))) {
                value = (int) (1.0 * this.scaleJoinDegree_Dis.get(entry.getKey().get(0)) * (this.scaleJoinDegree_Dis.get(entry.getKey().get(0)) - 1) - entry.getValue());
            } else {
                value = (int) (1.0 * this.scaleJoinDegree_Dis.get(entry.getKey().get(0)) * (this.scaleJoinDegree_Dis.get(entry.getKey().get(1)) - 0) - entry.getValue());
            }

            if (value < 0) {
                System.err.println("negative value in correlation function");
                System.exit(-1);
            }

            if (entry.getValue() > 0) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void sort_correlation(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal) {
        Sort so = new Sort();
        HashMap<ArrayList<Integer>, Integer> original = new HashMap<>();
        for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : this.original_joint_degree_dis.entrySet()) {
            ArrayList<Integer> arr = new ArrayList<>();
            for (ArrayList<Integer> a : entry.getKey()) {
                for (int b : a) {
                    arr.add(b);
                }
            }
            original.put(arr, entry.getValue());
        }

        sorted_correlation = so.sortOnAppearance(correlatedOriginal, original);
    }

    private int cal_value(Entry<ArrayList<ArrayList<Integer>>, Integer> entry) {
        int value = (int) (entry.getValue() * stime * (1 + Math.max(0, se)));
        double difff = entry.getValue() * stime * (1 + Math.max(0, se)) - value;

        double kl = Math.random();
        if (kl < difff) {
            value++;
        }
        return value;
    }

    private ArrayList<ArrayList<Integer>> cal_source_set(HashMap<ArrayList<Integer>, Integer> calSource) {
           ArrayList<ArrayList<Integer>> sourceSet =    new ArrayList<>();
        for (ArrayList<Integer> tweet : calSource.keySet()) {
            sourceSet.add(tweet);
        }
        return sourceSet;
    }

}
