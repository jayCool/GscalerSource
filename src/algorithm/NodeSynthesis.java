package algorithm;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jiangwei
 *
 */
public class NodeSynthesis extends PrintFunction {

    public int calUserSize = 1;
    public double domainRatio = 1;
    public double stime = 0.2;
    public int scaledVertexSize = 0;

    //The approach of the correlation is based on the original degree distribution.
    // 1. sort the degree in descending order. And settle the value from largest to the smallest
    // 2. Find the closest if the value is not found
    public NodeSynthesis() {
    }

    int loop = 1;

    //allocate those ids with enough credits, those leftovers deal with later.
    public HashMap<ArrayList<ArrayList<Integer>>, Integer> produceCorrel(HashMap<ArrayList<Integer>, Integer> scaleOutdegreeMap, HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap, HashMap<ArrayList<ArrayList<Integer>>, Integer> correlated) {
        int preV = 0;

        int sum_node = sum_nodes(scaleIndegreeMap);
        HashMap<ArrayList<ArrayList<Integer>>, Integer> result = new HashMap<>();

        synthesizing(correlated, scaleOutdegreeMap, scaleIndegreeMap, result);

        loop++;
        int num = checkBalance(scaleOutdegreeMap, scaleIndegreeMap);
        while (num != preV && !scaleOutdegreeMap.keySet().isEmpty() && !scaleIndegreeMap.keySet().isEmpty()) {
            preV = num;
            synthesizing(correlated, scaleOutdegreeMap, scaleIndegreeMap, result);
            num = checkBalance(scaleOutdegreeMap, scaleIndegreeMap);
        }

        if (!scaleOutdegreeMap.keySet().isEmpty() || !scaleIndegreeMap.keySet().isEmpty()) {
            while (!scaleIndegreeMap.keySet().isEmpty()) {
                clearZero(scaleIndegreeMap);
                clearZero(scaleOutdegreeMap);

                HashMap<ArrayList<ArrayList<Integer>>, Integer> add_result = new HashMap<>();

                for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : result.entrySet()) {
                    if (scaleIndegreeMap.keySet().isEmpty()) {
                        break;
                    }
                    if (entry.getValue() == 0) {
                        continue;
                    }
                    if (Math.random() < 1.0 * entry.getValue() / sum_node) {
                        loop_for_elements(scaleIndegreeMap, scaleOutdegreeMap, entry, add_result, result);
                    }
                }

                for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : add_result.entrySet()) {
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

    private ArrayList<Integer> manhattanClosest(ArrayList<Integer> pair, Set<ArrayList<Integer>> keySet) {
        if (keySet.contains(pair)) {
            return pair;
        }
        int thresh = 10;

        //strick manhattanClosest
        for (int i = 0; i < pair.size(); i++) {
            if (!pair.get(i).equals(0)) {
                for (int k = 1; k <= thresh; k++) {
                    ArrayList<Integer> result = new ArrayList<Integer>();
                    for (int m = -1; m <= 1; m = m + 2) {
                        int v = m * k;
                        for (int j = 0; j < pair.size(); j++) {
                            if (j == i) {
                                result.add(pair.get(j) - v);
                            } else {
                                result.add(pair.get(j));
                            }
                        }
                        if (keySet.contains(result)) {
                            return result;
                        }
                    }
                }
            }
        }

        //relax manhattanClosest
        HashMap<Integer, ArrayList<ArrayList<Integer>>> map = new HashMap<>();
        int sumde = 0;
        for (int i : pair) {
            sumde += i;
        }

        for (ArrayList<Integer> temp : keySet) {
            int sumt = 0;
            for (int i : temp) {
                sumt += i;
            }
            if (!map.containsKey(Math.abs(sumt - sumde))) {
                map.put(Math.abs(sumt - sumde), new ArrayList<ArrayList<Integer>>());
            }
            map.get(Math.abs(sumt - sumde)).add(temp);
        }

        int closeSum = Collections.min(map.keySet());
        return map.get(closeSum).get((int) Math.random() * (map.get(closeSum).size() - 1));

    }

    private HashMap<ArrayList<ArrayList<Integer>>, Integer> synthesizing(
            HashMap<ArrayList<ArrayList<Integer>>, Integer> original, HashMap<ArrayList<Integer>, Integer> scaleOutdegreeMap,
            HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap, HashMap<ArrayList<ArrayList<Integer>>, Integer> result) {
        System.out.println("Node Synthesizing");
        int value = 0;
        Sort so = new Sort();
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted = so.sortOnKeySum(original);

        for (int i = 0; i < sorted.size() && !scaleOutdegreeMap.keySet().isEmpty()
                && !scaleIndegreeMap.keySet().isEmpty() && sorted.size() > 0; i++) {

            Entry<ArrayList<ArrayList<Integer>>, Integer> entry = sorted.get(i);
            ArrayList<Integer> outDegree = entry.getKey().get(1);
            ArrayList<Integer> inDegree = entry.getKey().get(0);

            value = cal_value(entry);

            if (value == 0) {
                continue;
            }

            if (loop == 1) {
                if (!scaleOutdegreeMap.containsKey(outDegree) || !scaleIndegreeMap.containsKey(inDegree)) {
                    continue;
                }
            }

            ArrayList<Integer> calOutDegree = outDegree;
            ArrayList<Integer> calInDegree = inDegree;

            if (loop > 1) {
                calOutDegree = manhattanClosest(outDegree, scaleOutdegreeMap.keySet());
                calInDegree = manhattanClosest(inDegree, scaleIndegreeMap.keySet());
            }

            if (calInDegree.get(0) == 0 && calOutDegree.get(0) == 0) {
                value = 0;
                continue;
            }

            ArrayList<ArrayList<Integer>> calJointDegree = new ArrayList<>();
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
        System.out.println("End");
        return null;
    }

    private int checkBalance(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser) {
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

    double rationP = 0.005;

    private int cal_value(Entry<ArrayList<ArrayList<Integer>>, Integer> entry) {
        int value = (int) (entry.getValue() * stime);
        double difff = entry.getValue() * stime - value;

        double kl = Math.random();
        if (kl < difff) {
            value++;
        }
        return value;
    }

    private int sum_nodes(HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap) {
        int result = 0;
        for (int v : scaleIndegreeMap.values()) {
            result += v;
        }
        return result;
    }

    private void clearZero(HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap) {
        ArrayList<ArrayList<Integer>> zeroDegrees = new ArrayList<>();
        for (ArrayList<Integer> key : scaleIndegreeMap.keySet()) {
            if (scaleIndegreeMap.get(key) == 0) {
                zeroDegrees.add(key);
            }
        }
        for (ArrayList<Integer> key : zeroDegrees) {
            scaleIndegreeMap.remove(key);
        }
    }

    private void loop_for_elements(HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> scaleOutdegreeMap, Entry<ArrayList<ArrayList<Integer>>, Integer> entry, HashMap<ArrayList<ArrayList<Integer>>, Integer> add_result, HashMap<ArrayList<ArrayList<Integer>>, Integer> result) {
        boolean found = false;
        for (ArrayList<Integer> inDegree : scaleIndegreeMap.keySet()) {
            for (ArrayList<Integer> outDegree : scaleOutdegreeMap.keySet()) {
                if (entry.getKey().get(0).equals(inDegree) || entry.getKey().get(1).equals(outDegree)) {
                    continue;
                }
                ArrayList<ArrayList<Integer>> newPair1 = new ArrayList<>();
                newPair1.add(inDegree);
                newPair1.add(entry.getKey().get(1));

                ArrayList<ArrayList<Integer>> newPair2 = new ArrayList<>();
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
                found = true;
                break;
            }
            if (found) {
                break;
            }
        }
        if (found) {
            clearZero(scaleIndegreeMap);
            clearZero(scaleOutdegreeMap);
        }
    }

}
