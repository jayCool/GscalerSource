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
public class NodeSynthesis extends PrintFunction {

    public double s_n = 0;
    public int scaledVertexSize = 0;
    int loop = 1;

    
    //The approach of the correlation is based on the original degree distribution.
    // 1. sort the degree in descending order. And settle the value from largest to the smallest
    // 2. Find the closest if the value is not found
    public NodeSynthesis() {
    }

    
    //allocate those ids with enough credits, those leftovers deal with later.
    public HashMap<ArrayList<Integer>, Integer> produceCorrel(HashMap<Integer, Integer> scaleOutdegreeMap,
            HashMap<Integer, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> correlated) {
        int preV = 0;

        int sum_node = sum_nodes(scaleIndegreeMap);
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();

        clearZero(scaleIndegreeMap);
        clearZero(scaleOutdegreeMap);
        System.err.println("scaleIndegreeMap: " + scaleIndegreeMap);
        System.err.println("scaleoutdegreeMap: " + scaleOutdegreeMap);

        synthesizing(correlated, scaleOutdegreeMap, scaleIndegreeMap, result);

        loop++;
        int num = checkBalance(scaleOutdegreeMap, scaleIndegreeMap);
        clearZero(scaleIndegreeMap);
        clearZero(scaleOutdegreeMap);
        System.err.println("scaleIndegreeMap: " + scaleIndegreeMap);
        System.err.println("scaleoutdegreeMap: " + scaleOutdegreeMap);

        while (num != preV && !scaleOutdegreeMap.keySet().isEmpty() && !scaleIndegreeMap.keySet().isEmpty()) {
            preV = num;
            synthesizing(correlated, scaleOutdegreeMap, scaleIndegreeMap, result);
            num = checkBalance(scaleOutdegreeMap, scaleIndegreeMap);
            System.err.println("prev: " + preV);
            System.err.println("num: " + num);
        }

        if (!scaleOutdegreeMap.keySet().isEmpty() || !scaleIndegreeMap.keySet().isEmpty()) {
            System.err.println("non empty!");
            while (!scaleIndegreeMap.keySet().isEmpty()) {

                clearZero(scaleIndegreeMap);
                clearZero(scaleOutdegreeMap);
                System.err.println("scaleIndegreeMap: " + scaleIndegreeMap);
                System.err.println("scaleoutdegreeMap: " + scaleOutdegreeMap);

                HashMap<ArrayList<Integer>, Integer> add_result = new HashMap<>();

                for (Entry<ArrayList<Integer>, Integer> entry : result.entrySet()) {
                    if (scaleIndegreeMap.keySet().isEmpty()) {
                        break;
                    }
                    if (entry.getValue() == 0) {
                        continue;
                    }
                    if (Math.random() < 1.0 * entry.getValue() / sum_node * 10) {
                        loop_for_elements(scaleIndegreeMap, scaleOutdegreeMap, entry, add_result, result);
                    }
                }

                for (Entry<ArrayList<Integer>, Integer> entry : add_result.entrySet()) {
                    if (result.containsKey(entry.getKey())) {
                        result.put(entry.getKey(), entry.getValue() + result.get(entry.getKey()));
                    } else {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        return result;
    }

    private int manhattanClosest(int degree, Set<Integer> degreeSet) {
        if (degreeSet.contains(degree)) {
            return degree;
        }
        int thresh = Integer.MAX_VALUE-2;

        //strick manhattanClosest
        for (int k = 1; k <= thresh; k++) {
            for (int m = -1; m <= 1; m = m + 2) {
                int result = degree + m*k;
                if (degreeSet.contains(result)) {
                    return result;
                }
            }
        }
        
        return degreeSet.iterator().next();

    }

    private HashMap<ArrayList<Integer>, Integer> synthesizing(
            HashMap<ArrayList<Integer>, Integer> original, HashMap<Integer, Integer> scaleOutdegreeMap,
            HashMap<Integer, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> result) {
        //  System.out.println("Node Synthesizing");
        int value = 0;
        Sort so = new Sort();
        List<Map.Entry<ArrayList<Integer>, Integer>> sorted = so.sortOnKeySumS(original);

        for (int i = 0; i < sorted.size() && !scaleOutdegreeMap.keySet().isEmpty()
                && !scaleIndegreeMap.keySet().isEmpty() && sorted.size() > 0; i++) {

            Entry<ArrayList<Integer>, Integer> entry = sorted.get(i);
            int outDegree = entry.getKey().get(1);
            int inDegree = entry.getKey().get(0);

            value = cal_value(entry);

            if (value == 0) {
                continue;
            }

            if (loop == 1) {
                if (!scaleOutdegreeMap.containsKey(outDegree) || !scaleIndegreeMap.containsKey(inDegree)) {
                    continue;
                }
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

            if (!result.containsKey(calJointDegree)) {
                result.put(calJointDegree, 0);
            }
            result.put(calJointDegree, value + result.get(calJointDegree));
            if (scaleOutdegreeMap.get(calOutDegree) == 0) {
                scaleOutdegreeMap.remove(calOutDegree);
            }
            if (scaleIndegreeMap.get(calInDegree) == 0) {
                scaleIndegreeMap.remove(calInDegree);
            }
        }
        //System.out.println("End");
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

    private void clearZero(HashMap<Integer, Integer> scaleIndegreeMap) {
        ArrayList<Integer> zeroDegrees = new ArrayList<>();
        for (Integer key : scaleIndegreeMap.keySet()) {
            if (scaleIndegreeMap.get(key) == 0) {
                zeroDegrees.add(key);
            }
        }
        for (Integer key : zeroDegrees) {
            scaleIndegreeMap.remove(key);
        }
    }

    private void loop_for_elements(HashMap<Integer, Integer> scaleIndegreeMap,
            HashMap<Integer, Integer> scaleOutdegreeMap, 
            Entry<ArrayList<Integer>, Integer> entry, HashMap<ArrayList<Integer>, Integer> add_result, 
            HashMap<ArrayList<Integer>, Integer> result) {
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
                result.put(entry.getKey(), entry.getValue() - 1);
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
            scaleIndegreeMap.put(oldInDegree, v - 1);
            v = scaleOutdegreeMap.get(oldOutDegree);
            scaleOutdegreeMap.put(oldOutDegree, v - 1);
            System.err.println("here");
            clearZero(scaleIndegreeMap);
            clearZero(scaleOutdegreeMap);
        }
    }

}
