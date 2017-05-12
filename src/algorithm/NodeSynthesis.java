package algorithm;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author jiangwei
 *
 */
public class NodeSynthesis {

    public double s_n = 0;
    int loop = 1;

    //The approach of the correlation is based on the original degree distribution.
    // 1. sort the degree in descending order. And settle the value from largest to the smallest
    // 2. Find the closest if the value is not found
    public NodeSynthesis(double s_n) {
        this.s_n = s_n;
    }

    /**
     * This method synthesize the nodes from
     * scaledIndegreeMap/scaledOutdegreeMap based on the original jointdegree
     * Map.
     *
     * @param scaleOutdegreeMap
     * @param scaleIndegreeMap
     * @param originalJointDegreeMap
     * @return scaledJointDegreeMap
     */
    public HashMap<ArrayList<Integer>, Integer> synthesizeNode(HashMap<Integer, Integer> scaleOutdegreeMap,
            HashMap<Integer, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> originalJointDegreeMap) {

        CleaningMap.removeZeroFrequencyFromMap(scaleIndegreeMap);
        CleaningMap.removeZeroFrequencyFromMap(scaleOutdegreeMap);

        HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap = new HashMap<>();

        loopingForSynthesizing(originalJointDegreeMap, scaleIndegreeMap, scaleOutdegreeMap, scaledJointDegreeMap);

        synthesizeLeftOverNodes(scaleIndegreeMap, scaleOutdegreeMap, scaledJointDegreeMap);

        return scaledJointDegreeMap;
    }

    /**
     *
     * @param degree
     * @param degreeSet
     * @return nearest degree
     */
    private int manhattanClosest(int degree, Set<Integer> degreeSet) {
        if (degreeSet.contains(degree)) {
            return degree;
        }
        int thresh = Integer.MAX_VALUE - 2;

        //strick manhattanClosest
        for (int k = 1; k <= thresh; k++) {
            for (int m = -1; m <= 1; m = m + 2) {
                int result = degree + m * k;
                if (degreeSet.contains(result)) {
                    return result;
                }
            }
        }

        return degreeSet.iterator().next();

    }

    /**
     * Loop through the originalJointDegree Distribution to synthesis nodes for
     * scaledJointDegreeMap
     *
     * @param originalJointDegreeDis
     * @param scaleOutdegreeMap
     * @param scaleIndegreeMap
     * @param scaledJointDegreeMap
     */
    private void synthesizingForOneLoop(
            HashMap<ArrayList<Integer>, Integer> originalJointDegreeDis, HashMap<Integer, Integer> scaleOutdegreeMap,
            HashMap<Integer, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap) {

        Sort so = new Sort();
        List<Map.Entry<ArrayList<Integer>, Integer>> sorted = so.sortOnKeySumDescending(originalJointDegreeDis);

        for (int i = 0; i < sorted.size() && !scaleOutdegreeMap.keySet().isEmpty()
                && !scaleIndegreeMap.keySet().isEmpty(); i++) {

            Entry<ArrayList<Integer>, Integer> originalJointDegreeEntry = sorted.get(i);
            int[] scaledDegreeAndFrequency = calScaledDegreeAndFrequency(originalJointDegreeEntry, scaleIndegreeMap, scaleOutdegreeMap);
            int scaledInDegree = scaledDegreeAndFrequency[0];
            int scaledOutDegree = scaledDegreeAndFrequency[1];
            int scaledFrequency = scaledDegreeAndFrequency[2];
            if (scaledFrequency == 0) {
                continue;
            }

            updateInOutJoinDegreeMap(scaleIndegreeMap, scaleOutdegreeMap, scaledJointDegreeMap, scaledInDegree, scaledOutDegree, scaledFrequency);

            CleaningMap.cleanHashMap(scaleOutdegreeMap, scaledOutDegree);
            CleaningMap.cleanHashMap(scaleIndegreeMap, scaledInDegree);
        }
    }

    /**
     *
     * @param scaledOutdegreeMap
     * @param scaledIndegreeMap
     * @return minSun of scaledIn/Out-degreeMap
     */
    private int checkBalance(HashMap<Integer, Integer> scaledOutdegreeMap, HashMap<Integer, Integer> scaledIndegreeMap) {
        int sum1 = sum_nodes(scaledIndegreeMap);
        int sum2 = sum_nodes(scaledOutdegreeMap);
        return Math.min(sum1, sum2);
    }

    /**
     *
     * @param joindegreeEntry
     * @return scaledFrequency
     */
    private int calScaledFrequency(Entry<ArrayList<Integer>, Integer> joindegreeEntry) {
        int value = (int) (joindegreeEntry.getValue() * s_n);
        double diff = joindegreeEntry.getValue() * s_n - value;

        double kl = Math.random();
        if (kl < diff) {
            value++;
        }
        return value;
    }

    /**
     *
     * @param degreeMap
     * @return sum of the values
     */
    private int sum_nodes(HashMap<Integer, Integer> degreeMap) {
        int result = 0;
        for (int v : degreeMap.values()) {
            result += v;
        }
        return result;
    }
    
    
    /**
     * Detailed Forming refers to synthesizeTwoNewNodes
     * @param scaleIndegreeMap
     * @param scaleOutdegreeMap
     * @param oldNodeEntry
     * @param newlyFormedNodes
     * @param scaledJointDegreeMap 
     */
    private void loopForNewlyFormedElements(HashMap<Integer, Integer> scaleIndegreeMap,
            HashMap<Integer, Integer> scaleOutdegreeMap,
            Entry<ArrayList<Integer>, Integer> oldNodeEntry, HashMap<ArrayList<Integer>, Integer> newlyFormedNodes,
            HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap) {

        boolean found = false;
        int oldInDegree = 0;
        int oldOutDegree = 0;

        for (int inDegree : scaleIndegreeMap.keySet()) {
            for (int outDegree : scaleOutdegreeMap.keySet()) {
                if (oldNodeEntry.getKey().get(0).equals(inDegree) || oldNodeEntry.getKey().get(1).equals(outDegree) || (inDegree == 0 && oldNodeEntry.getKey().get(1) == 0) || (outDegree == 0 && oldNodeEntry.getKey().get(0) == 0)) {
                    continue;
                }

                synthesizeTwoNewNodes(inDegree, oldNodeEntry.getKey(), outDegree, newlyFormedNodes);

                scaledJointDegreeMap.put(oldNodeEntry.getKey(), oldNodeEntry.getValue() - 1);

                oldInDegree = inDegree;
                oldOutDegree = outDegree;
                found = true;
                break;
            }
            if (found) {
                break;
            }
        }

        if (found) {
            decreaseFrequency(oldInDegree, scaleIndegreeMap);
            decreaseFrequency(oldOutDegree, scaleOutdegreeMap);
        }

    }

    /**
     * Looping until the synthesis can't be done Or the
     * scaledIndegreeMap/scaledOutdegreeMap is empty.
     *
     * @param originalJointDegreeMap
     * @param scaleIndegreeMap
     * @param scaleOutdegreeMap
     * @param scaledJointDegreeMap
     */
    private void loopingForSynthesizing(HashMap<ArrayList<Integer>, Integer> originalJointDegreeMap, HashMap<Integer, Integer> scaleIndegreeMap, HashMap<Integer, Integer> scaleOutdegreeMap, HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap) {
        int preV = -1, num = 0;
        while (num != preV && !scaleOutdegreeMap.keySet().isEmpty() && !scaleIndegreeMap.keySet().isEmpty()) {
            preV = num;
            synthesizingForOneLoop(originalJointDegreeMap, scaleOutdegreeMap, scaleIndegreeMap, scaledJointDegreeMap);
            num = checkBalance(scaleOutdegreeMap, scaleIndegreeMap);
            CleaningMap.removeZeroFrequencyFromMap(scaleIndegreeMap);
            CleaningMap.removeZeroFrequencyFromMap(scaleOutdegreeMap);

        }
    }

    /**
     *
     * @param degree
     * @param scaledDegreeMap
     * @return scaledDegree
     */
    private int calScaledDegree(int degree, HashMap<Integer, Integer> scaledDegreeMap) {
        int scaledOutDegree = degree;

        if (loop > 1) {
            scaledOutDegree = manhattanClosest(degree, scaledDegreeMap.keySet());
        }
        return scaledOutDegree;
    }

    /**
     * This method returns the scaled Indegree/Outdegree: (1) First time: only
     * the original degree (2) Second time onwards: The nearest degree The
     * frequency is scaled proportionally.
     *
     * @param originalJointDegreeEntry
     * @param scaleIndegreeMap
     * @param scaleOutdegreeMap
     * @return scaledIndegree/scaledOutdegree/scaledFrequency
     */
    private int[] calScaledDegreeAndFrequency(Entry<ArrayList<Integer>, Integer> originalJointDegreeEntry, HashMap<Integer, Integer> scaleIndegreeMap, HashMap<Integer, Integer> scaleOutdegreeMap) {
        int inDegree = originalJointDegreeEntry.getKey().get(0);
        int outDegree = originalJointDegreeEntry.getKey().get(1);
        int scaledFrequency = calScaledFrequency(originalJointDegreeEntry);
        if (scaledFrequency == 0 || (loop == 1 && (!scaleOutdegreeMap.containsKey(outDegree) || !scaleIndegreeMap.containsKey(inDegree)))) {
            return new int[3];
        }

        int scaledOutDegree = calScaledDegree(outDegree, scaleOutdegreeMap);
        int scaledInDegree = calScaledDegree(inDegree, scaleIndegreeMap);

        if (scaledInDegree == 0 && scaledOutDegree == 0) {
            return new int[3];
        }
        int[] result = {scaledInDegree, scaledOutDegree, scaledFrequency};
        return result;
    }

    /**
     * This method updateds
     * scaledIndegreeMap/scaledOutdegreeMap/scaledJointDegreeMap
     *
     * @param scaleIndegreeMap
     * @param scaleOutdegreeMap
     * @param scaledJointDegreeMap
     * @param scaledInDegree
     * @param scaledOutDegree
     * @param scaledFrequency
     */
    private void updateInOutJoinDegreeMap(HashMap<Integer, Integer> scaleIndegreeMap, HashMap<Integer, Integer> scaleOutdegreeMap, HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap, int scaledInDegree, int scaledOutDegree, int scaledFrequency) {

        ArrayList<Integer> scaledJointDegree = new ArrayList<>(2);
        scaledJointDegree.add(scaledInDegree);
        scaledJointDegree.add(scaledOutDegree);

        scaledFrequency = Math.min(scaledFrequency, scaleOutdegreeMap.get(scaledOutDegree));
        scaledFrequency = Math.min(scaledFrequency, scaleIndegreeMap.get(scaledInDegree));

        scaleOutdegreeMap.put(scaledOutDegree, scaleOutdegreeMap.get(scaledOutDegree) - scaledFrequency);
        scaleIndegreeMap.put(scaledInDegree, scaleIndegreeMap.get(scaledInDegree) - scaledFrequency);

        if (!scaledJointDegreeMap.containsKey(scaledJointDegree)) {
            scaledJointDegreeMap.put(scaledJointDegree, 0);
        }
        scaledJointDegreeMap.put(scaledJointDegree, scaledFrequency + scaledJointDegreeMap.get(scaledJointDegree));
    }

    /**
     * Synthesize leftover elements in scaleIndegreeMap/scaleOutdegreeMap
     *
     * @param scaleIndegreeMap
     * @param scaleOutdegreeMap
     * @param scaledJointDegreeMap
     */
    private void synthesizeLeftOverNodes(HashMap<Integer, Integer> scaleIndegreeMap, HashMap<Integer, Integer> scaleOutdegreeMap, HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap) {
        while (!scaleIndegreeMap.keySet().isEmpty()) {
            int scaledNodeSize = sum_nodes(scaleIndegreeMap);

            CleaningMap.removeZeroFrequencyFromMap(scaleIndegreeMap);
            CleaningMap.removeZeroFrequencyFromMap(scaleOutdegreeMap);

            HashMap<ArrayList<Integer>, Integer> add_result = new HashMap<>();

            for (Entry<ArrayList<Integer>, Integer> entry : scaledJointDegreeMap.entrySet()) {
                if (scaleIndegreeMap.keySet().isEmpty()) {
                    break;
                }
                if (entry.getValue() == 0) {
                    continue;
                }
                if (Math.random() < 1.0 * entry.getValue() / scaledNodeSize * Constant.CLEANING_THRESHOLD) {
                    loopForNewlyFormedElements(scaleIndegreeMap, scaleOutdegreeMap, entry, add_result, scaledJointDegreeMap);
                }
            }

            for (Entry<ArrayList<Integer>, Integer> entry : add_result.entrySet()) {
                if (scaledJointDegreeMap.containsKey(entry.getKey())) {
                    scaledJointDegreeMap.put(entry.getKey(), entry.getValue() + scaledJointDegreeMap.get(entry.getKey()));
                } else {
                    scaledJointDegreeMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }
    
    
    /**
     * This method breaks oldPair and form 2 new pairs, by doing the following:
     * oldPair.inDegree:outDegree
     * inDegree:oldPair.outDegree
     * @param inDegree
     * @param oldPair
     * @param outDegree
     * @param newlyFormedNodes 
     */
    private void synthesizeTwoNewNodes(int inDegree, ArrayList<Integer> oldPair, int outDegree, HashMap<ArrayList<Integer>, Integer> newlyFormedNodes) {
        ArrayList<Integer> newPair1 = new ArrayList<>(2);
        newPair1.add(inDegree);
        newPair1.add(oldPair.get(1));

        ArrayList<Integer> newPair2 = new ArrayList<>(2);
        newPair2.add(oldPair.get(0));
        newPair2.add(outDegree);

        if (!newlyFormedNodes.containsKey(newPair1)) {
            newlyFormedNodes.put(newPair1, 0);
        }
        newlyFormedNodes.put(newPair1, 1 + newlyFormedNodes.get(newPair1));

        if (!newlyFormedNodes.containsKey(newPair2)) {
            newlyFormedNodes.put(newPair2, 0);
        }
        newlyFormedNodes.put(newPair2, 1 + newlyFormedNodes.get(newPair2));
    }

    /**
     * Decrease the frequency of the corresponding degree
     * @param oldInDegree
     * @param scaleIndegreeMap 
     */
    private void decreaseFrequency(int oldInDegree, HashMap<Integer, Integer> scaleIndegreeMap) {
    int v = scaleIndegreeMap.get(oldInDegree);
            if (v == 1) {
                scaleIndegreeMap.remove(oldInDegree);
            } else {
                scaleIndegreeMap.put(oldInDegree, v - 1);
            }
    }

}
