package paperalgorithm;

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
 * This is most update correlation function for tweets and followee, and
 * follower
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
            System.out.println("NOT EMPTY");
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

    private HashMap<ArrayList<ArrayList<Integer>>, Integer> synthesizing(HashMap<ArrayList<ArrayList<Integer>>, Integer> original, HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser, HashMap<ArrayList<ArrayList<Integer>>, Integer> result) {
        System.out.println("Node Synthesizing");
        int value = 0;
        Sort so = new Sort();
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted = so.sortOnKeySum(original);

        for (int i = 0; i < sorted.size() && !calTweet.keySet().isEmpty() && !calUser.keySet().isEmpty() && sorted.size() > 0; i++) {
            Entry<ArrayList<ArrayList<Integer>>, Integer> entry = sorted.get(i);
            ArrayList<Integer> tweetDegree = entry.getKey().get(1);
            ArrayList<Integer> userDegree = entry.getKey().get(0);
            ArrayList<Integer> calTweetDegree = tweetDegree;
            ArrayList<Integer> calUserDegree = userDegree;
            value = (int) (entry.getValue() * stime);
            double difff = entry.getValue() * stime - value;

            double kl = Math.random();
            if (kl < difff) {
                value++;
            }

            if (value == 0) {
                continue;
            }
            if (loop == 1) {
                if (!calTweet.containsKey(tweetDegree) || !calUser.containsKey(userDegree)) {
                    continue;
                }
            }
            if (loop > 1) {
                calTweetDegree = manhattanClosest(tweetDegree, calTweet.keySet());
                calUserDegree = manhattanClosest(userDegree, calUser.keySet());
            }

            ArrayList<ArrayList<Integer>> tempA = new ArrayList<>();
            tempA.add(calUserDegree);
            tempA.add(calTweetDegree);

            if (calUserDegree.get(0) == 0 && calTweetDegree.get(0) == 0) {
                value = 0; continue;
                
            }
            value = Math.min(value, calTweet.get(calTweetDegree));
            value = Math.min(value, calUser.get(calUserDegree));

            calTweet.put(calTweetDegree, calTweet.get(calTweetDegree) - value);
            calUser.put(calUserDegree, calUser.get(calUserDegree) - value);
          
            if (!result.containsKey(tempA)) {
                result.put(tempA, 0);
            }
            result.put(tempA, value + result.get(tempA));
            if (calTweet.get(calTweetDegree) == 0) {
                calTweet.remove(calTweetDegree);
           
            }
            if (calUser.get(calUserDegree) == 0) {
                calUser.remove(calUserDegree);
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

    ////Not used................Updated at 2016
    private void lastRound(HashMap<ArrayList<ArrayList<Integer>>, Integer> result, HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser) {
        ArrayList<Integer> arr = new ArrayList<>();
        //       arr.add(0);
        while (calTweet.size() > 0 && calUser.size() > 0) {
            System.out.println("Last Run");
            arr = calTweet.keySet().iterator().next();
            ConcurrentHashMap<ArrayList<ArrayList<Integer>>, Integer> con = new ConcurrentHashMap<>();
            for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : result.entrySet()) {
                con.put(entry.getKey(), entry.getValue());
            }
            System.out.println(calTweet);
            System.out.println(calUser);

            for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : con.entrySet()) {
                if (entry.getKey().get(0).get(0) != 0 && entry.getKey().get(1).get(0) != 0 && calTweet.size() > 0 && calTweet.containsKey(arr) && entry.getValue() > 1 && calUser.containsKey(arr)) {

                    ArrayList<Integer> arr1 = new ArrayList<>();
                    arr1.add(entry.getKey().get(0).get(0));

                    ArrayList<Integer> arr2 = new ArrayList<>();
                    arr2.add(entry.getKey().get(1).get(0));

                    ArrayList<ArrayList<Integer>> arr3 = new ArrayList<>();
                    arr3.add(arr1);
                    arr3.add(arr);

                    ArrayList<ArrayList<Integer>> arr4 = new ArrayList<>();
                    arr4.add(arr);
                    arr4.add(arr2);
                    calTweet.put(arr, calTweet.get(arr) - 1);
                    calUser.put(arr, calUser.get(arr) - 1);
                    con.put(entry.getKey(), -1 + entry.getValue());
                    if (!con.containsKey(arr3)) {
                        con.put(arr3, 0);
                    }
                    con.put(arr3, 1 + con.get(arr3));

                    if (!con.containsKey(arr4)) {
                        con.put(arr4, 0);
                    }
                    con.put(arr4, 1 + con.get(arr4));

                    if (calTweet.get(arr) == 0) {
                        calTweet.remove(arr);
                        //        tweetRelated.remove(id);
                    }
                    if (calUser.get(arr) == 0) {
                        calUser.remove(arr);
                        //  calUser.remove(pair);
                        //       iterator1.remove();
                    }

                }
            }

            // result = new HashMap<>();
            for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : con.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
    }

}
