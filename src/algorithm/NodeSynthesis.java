package algorithm;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
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

    //allocate those ids with enough credits, those leftovers deal with later.
    public HashMap<ArrayList<Integer>, Integer> produceCorrel(HashMap<Integer, Integer> scaleOutdegreeMap,
            HashMap<Integer, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> jointDegreeMap) {

        int preV = -1, num = 0;
        int scaledNodeSize = sum_nodes(scaleIndegreeMap);
        CleaningMap.removeZeroDegreeMap(scaleIndegreeMap);
        CleaningMap.removeZeroDegreeMap(scaleOutdegreeMap);

        HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap = new HashMap<>();
        while (num != preV && !scaleOutdegreeMap.keySet().isEmpty() && !scaleIndegreeMap.keySet().isEmpty()) {
            preV = num;
            synthesizing(jointDegreeMap, scaleOutdegreeMap, scaleIndegreeMap, scaledJointDegreeMap);
            num = checkBalance(scaleOutdegreeMap, scaleIndegreeMap);
            CleaningMap.removeZeroDegreeMap(scaleIndegreeMap);
            CleaningMap.removeZeroDegreeMap(scaleOutdegreeMap);

        }

        if (!scaleOutdegreeMap.keySet().isEmpty() || !scaleIndegreeMap.keySet().isEmpty()) {
            while (!scaleIndegreeMap.keySet().isEmpty()) {

                CleaningMap.removeZeroDegreeMap(scaleIndegreeMap);
                CleaningMap.removeZeroDegreeMap(scaleOutdegreeMap);

                HashMap<ArrayList<Integer>, Integer> add_result = new HashMap<>();

                for (Entry<ArrayList<Integer>, Integer> entry : scaledJointDegreeMap.entrySet()) {
                    if (scaleIndegreeMap.keySet().isEmpty()) {
                        break;
                    }
                    if (entry.getValue() == 0) {
                        continue;
                    }
                    if (Math.random() < 1.0 * entry.getValue() / scaledNodeSize * Constant.CLEANING_THRESHOLD) {
                        loop_for_elements(scaleIndegreeMap, scaleOutdegreeMap, entry, add_result, scaledJointDegreeMap);
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

        return scaledJointDegreeMap;
    }

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

    private HashMap<ArrayList<Integer>, Integer> synthesizing(
            HashMap<ArrayList<Integer>, Integer> originalJointDegreeDis, HashMap<Integer, Integer> scaleOutdegreeMap,
            HashMap<Integer, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap) {

        int value = 0;

        Sort so = new Sort();
        List<Map.Entry<ArrayList<Integer>, Integer>> sorted = so.sortOnKeySumDescending(originalJointDegreeDis);

        for (int i = 0; i < sorted.size() && !scaleOutdegreeMap.keySet().isEmpty()
                && !scaleIndegreeMap.keySet().isEmpty() && sorted.size() > 0; i++) {

            Entry<ArrayList<Integer>, Integer> jointDegreeEntry = sorted.get(i);

            int outDegree = jointDegreeEntry.getKey().get(1);
            int inDegree = jointDegreeEntry.getKey().get(0);

            value = cal_value(jointDegreeEntry);

            if (value == 0 || (loop == 1 && (!scaleOutdegreeMap.containsKey(outDegree) || !scaleIndegreeMap.containsKey(inDegree)))) {
                continue;
            }

            int calOutDegree = outDegree;
            int calInDegree = inDegree;

            if (loop > 1) {
                calOutDegree = manhattanClosest(outDegree, scaleOutdegreeMap.keySet());
                calInDegree = manhattanClosest(inDegree, scaleIndegreeMap.keySet());
            }

            if (calInDegree == 0 && calOutDegree == 0) {
                value = 0;
                continue;
            }

            ArrayList<Integer> calJointDegree = new ArrayList<>(2);
            calJointDegree.add(calInDegree);
            calJointDegree.add(calOutDegree);

            value = Math.min(value, scaleOutdegreeMap.get(calOutDegree));
            value = Math.min(value, scaleIndegreeMap.get(calInDegree));

            scaleOutdegreeMap.put(calOutDegree, scaleOutdegreeMap.get(calOutDegree) - value);
            scaleIndegreeMap.put(calInDegree, scaleIndegreeMap.get(calInDegree) - value);

            if (!scaledJointDegreeMap.containsKey(calJointDegree)) {
                scaledJointDegreeMap.put(calJointDegree, 0);
            }
            scaledJointDegreeMap.put(calJointDegree, value + scaledJointDegreeMap.get(calJointDegree));
            
            CleaningMap.cleanHashMap(scaleOutdegreeMap, calOutDegree);
            CleaningMap.cleanHashMap(scaleIndegreeMap, calInDegree);
        }
        return null;
    }

    private int checkBalance(HashMap<Integer, Integer> calTweet, HashMap<Integer, Integer> calUser) {
        int sum1 = 0;
        for (int v : calTweet.values()) {
            sum1 += v;
        }
        int sum = 0;
        for (int v : calUser.values()) {
            sum += v;
        }
        return Math.min(sum1, sum);
    }

    private int cal_value(Entry<ArrayList<Integer>, Integer> entry) {
        int value = (int) (entry.getValue() * s_n);
        double difff = entry.getValue() * s_n - value;

        double kl = Math.random();
        if (kl < difff) {
            value++;
        }
        return value;
    }

    private int sum_nodes(HashMap<Integer, Integer> scaleIndegreeMap) {
        int result = 0;
        for (int v : scaleIndegreeMap.values()) {
            result += v;
        }
        return result;
    }
    
    private void loop_for_elements(HashMap<Integer, Integer> scaleIndegreeMap,
            HashMap<Integer, Integer> scaleOutdegreeMap,
            Entry<ArrayList<Integer>, Integer> entry, HashMap<ArrayList<Integer>, Integer> add_result,
            HashMap<ArrayList<Integer>, Integer> scaledJointDegreeMap) {

        boolean found = false;
        int oldInDegree = 0;
        int oldOutDegree = 0;

        for (int inDegree : scaleIndegreeMap.keySet()) {
            for (int outDegree : scaleOutdegreeMap.keySet()) {
                if (entry.getKey().get(0).equals(inDegree) || entry.getKey().get(1).equals(outDegree)) {
                    continue;
                }
                ArrayList<Integer> newPair1 = new ArrayList<>(2);
                newPair1.add(inDegree);
                newPair1.add(entry.getKey().get(1));

                ArrayList<Integer> newPair2 = new ArrayList<>(2);
                newPair2.add(entry.getKey().get(0));
                newPair2.add(outDegree);

                if (!add_result.containsKey(newPair1)) {
                    add_result.put(newPair1, 0);
                }
                add_result.put(newPair1, 1 + add_result.get(newPair1));

                if (!add_result.containsKey(newPair2)) {
                    add_result.put(newPair2, 0);
                }
                add_result.put(newPair2, 1 + add_result.get(newPair2));
                scaledJointDegreeMap.put(entry.getKey(), entry.getValue() - 1);

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
            int v = scaleIndegreeMap.get(oldInDegree);
            if (v == 1) {
                scaleIndegreeMap.remove(oldInDegree);
            } else {
                scaleIndegreeMap.put(oldInDegree, v - 1);
            }
            v = scaleOutdegreeMap.get(oldOutDegree);
            if (v == 1) {
                scaleOutdegreeMap.remove(oldOutDegree);
            } else {
                scaleOutdegreeMap.put(oldOutDegree, v - 1);
            }
        }

    }

}
