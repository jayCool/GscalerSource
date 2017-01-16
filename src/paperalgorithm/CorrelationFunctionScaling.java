/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paperalgorithm;


/*
 * @author workshop
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

public class CorrelationFunctionScaling {

    public int calUserSize = 2;
    public double domainRatio = 1;
    public double stime = 0.2;
    int couter = 0;
    HashMap<ArrayList<Integer>, Integer> target = new HashMap<>();
    HashMap<ArrayList<Integer>, Integer> source = new HashMap<>();
    HashMap<ArrayList<ArrayList<Integer>>, Integer> result = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> tweetDisMap = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> userDisMap = new HashMap<>();
    int indexcount = 0;
    int userindex = 0;
    int tweetindex = 0;
    HashMap<ArrayList<ArrayList<Integer>>, Integer> originalDegree;
    double se = 0.0;
    int level = 0;
    double thresh = 0;
    boolean finalFlag = false;

    List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted;
    HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>> avaUTs = new HashMap<>();
    boolean utflag = false;

    HashMap<Integer, ArrayList<ArrayList<Integer>>> transOrders = new HashMap<>();

    public int scaleEdgeSize = 0;
    int loop = 1;
    static String userMappingFile = "0.8userMap.txt";
    int threeHopThreshHold = 100;

    //The approach of the correlation is based on the original degree distribution.
    // 1. sort the degree in descending order. And settle the value from largest to the smallest
    // 2. Find the closest if the value is not found
    HashMap<ArrayList<ArrayList<Integer>>, Integer> run(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal, HashMap<ArrayList<Integer>, Integer> calUser, HashMap<ArrayList<Integer>, Integer> calTweet) throws FileNotFoundException {

        //caluser is the calculated user's degree
        HashMap<ArrayList<ArrayList<Integer>>, Integer> resultMapping = new HashMap<>();

        resultMapping = produceCorrel(calTweet, calUser, correlatedOriginal);

        int i = 0;

        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry : resultMapping.entrySet()) {
            /////Double Checking=======================////////////////
            if (entry.getKey().get(0).equals(entry.getKey().get(1))) {
                int value = (int) (1.0 * this.target.get(entry.getKey().get(0)) * (this.target.get(entry.getKey().get(0)) - 1) - entry.getValue());
                if (value < 0) {
                    System.err.println(value);
                }
            } else {
                int value = (int) (1.0 * this.target.get(entry.getKey().get(0)) * (this.target.get(entry.getKey().get(1)) - 0) - entry.getValue());
                if (value < 0) {
                    System.err.println(value);
                }
            }
            ////////////Ended Double Checking//////////////////////////

            if (entry.getValue() > 0) {
                i += entry.getValue();
                result.put(entry.getKey(), entry.getValue());
            }
        }
        System.out.println("total is " + i);
        return result;
    }

    //allocate those ids with enough credits, those leftovers deal with later.
    private HashMap<ArrayList<ArrayList<Integer>>, Integer> produceCorrel(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser, HashMap<ArrayList<ArrayList<Integer>>, Integer> correlated) throws FileNotFoundException {

        Sort so = new Sort();
        HashMap<ArrayList<Integer>, Integer> original = new HashMap<>();
        for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : this.originalDegree.entrySet()) {
            ArrayList<Integer> arr = new ArrayList<>();
            for (ArrayList<Integer> a : entry.getKey()) {
                for (int b : a) {
                    arr.add(b);
                }
            }
            original.put(arr, entry.getValue());
        }

        sorted = so.sortOnAppearance(correlated, original);
        int num = thirdRound(calTweet, calUser);

        loop = 0;
        this.produceTransOrders();
        while (!calTweet.keySet().isEmpty() && !calUser.keySet().isEmpty() && loop <= 2 && num > 1000) {
            tweetDisMap.clear();
            userDisMap.clear();
            processDistanceMap(calTweet, tweetDisMap);
            processDistanceMap(calUser, this.userDisMap);

            mapping_DSA(calTweet, calUser);
            num = thirdRound(calTweet, calUser);
            loop++;
            loop++;
        }

        finalFlag = true;
        randomMap(result, calTweet, calUser);
        System.out.println("Last Map");
        num = thirdRound(calTweet, calUser);

        rewiring(calTweet, calUser);

        num = thirdRound(calTweet, calUser);

        System.out.println(calTweet.size());
        System.out.println(calUser.size() + "==" + couter);
        System.out.println("*****" + couter);
        return result;
    }

    void produceTransOrders() {
        for (int i = 0; i < 20; i++) {
            ArrayList<ArrayList<Integer>> pairs = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                ArrayList<Integer> p1 = new ArrayList<>();
                p1.add(j);
                p1.add(i - j);
                pairs.add(p1);
                transOrders.put(i, pairs);
            }
        }
    }

    private HashMap<ArrayList<ArrayList<Integer>>, Integer> mapping_DSA(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser) {
        int value = 0;
        level = (int) (Math.pow(10, loop - 1));

        for (int i = 0; i < sorted.size() && !calTweet.keySet().isEmpty() && !calUser.keySet().isEmpty(); i++) {
            Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry = sorted.get(i);
            value = (int) (entry.getValue() * stime * (1 + Math.max(0, se)));
            double difff = entry.getValue() * stime * (1 + Math.max(0, se)) - value;

            double kl = Math.random();
            if (kl < difff) {
                value++;
            }
            if (value == 0) {
                continue;
            }
            ArrayList<Integer> tweetDegree = entry.getKey().get(1);
            ArrayList<Integer> userDegree = entry.getKey().get(0);
            updateValue(calTweet, calUser, tweetDegree, userDegree, transOrders, value);
        }

        return result;
    }

    private int thirdRound(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser) {
        int sum1 = 0;
        for (int v : calTweet.values()) {
            sum1 += v;
        }
        int sum = 0;
        for (int v : calUser.values()) {
            sum += v;
        }

        int sum2 = 0;
        for (int v : result.values()) {
            sum2 += v;
        }
        System.out.println("sum:" + sum + "  " + sum1 + "  " + sum2);
        return Math.min(sum1, sum);
    }

    private void randomMap(HashMap<ArrayList<ArrayList<Integer>>, Integer> result, HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser) {
        ArrayList<ArrayList<Integer>> calTweetDegrees = new ArrayList<>(calTweet.keySet());
        ArrayList<ArrayList<Integer>> calUserDegrees = new ArrayList<>(calUser.keySet());

        for (ArrayList<Integer> calTweetDegree : calTweetDegrees) {
            if (calTweet.get(calTweetDegree) == 0) {
                continue;
            }
            for (ArrayList<Integer> calUserDegree : calUserDegrees) {
                if (calTweet.keySet().isEmpty() || calUser.keySet().isEmpty()) {
                    return;
                }

                if (!calTweet.containsKey(calTweetDegree) || !calUser.containsKey(calUserDegree)) {
                    continue;
                }

                int value = calTweet.get(calTweetDegree);
                if (value == 0) {
                    calTweet.remove(calTweetDegree);
                    break;
                }

                value = Math.min(value, calUser.get(calUserDegree));
                if (value == 0) {
                    calUser.remove(calUserDegree);
                    continue;
                }

                ArrayList<ArrayList<Integer>> tempA = new ArrayList<>();
                tempA.add(calUserDegree);
                tempA.add(calTweetDegree);

                int oldV = 0;
                if (result.containsKey(tempA)) {
                    oldV = result.get(tempA);
                }

                value = (int) Math.min(value, 1.0 * this.target.get(calTweetDegree) * this.target.get(calUserDegree) - oldV);
                if (calTweetDegree.equals(calUserDegree)) {
                    value = (int) Math.min(value, 1.0 * this.target.get(calTweetDegree) * (this.target.get(calUserDegree) - 1) - oldV);
                }

                if (value <= 0) {
                    continue;
                }

                calTweet.put(calTweetDegree, calTweet.get(calTweetDegree) - value);
                calUser.put(calUserDegree, calUser.get(calUserDegree) - value);

                result.put(tempA, value + oldV);

                removeZero(calTweet, calTweetDegree);
                removeZero(calUser, calUserDegree);

            }
        }
    }

    private void rewiring(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser) {
        HashMap<ArrayList<ArrayList<Integer>>, Integer> tempResult = new HashMap<>();
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted;
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> originalM : result.entrySet()) {
            tempResult.put(originalM.getKey(), originalM.getValue());
        }
        sorted = new Sort().sortOnKeySum(tempResult);

        int mapCount = 0;
        int thresh = 10;

        ArrayList<ArrayList<Integer>> tweetSet = new ArrayList<>();
        for (ArrayList<Integer> tweet : calTweet.keySet()) {
            tweetSet.add(tweet);
        }

        for (ArrayList<Integer> tweet : tweetSet) {
            if (calTweet.get(tweet) <= 0) {
                calTweet.remove(tweet);
                continue;
            }

            if (mapCount > thresh) {
                cleanCalUser(calUser);
                mapCount = 0;
            }

            for (ArrayList<Integer> user : calUser.keySet()) {
                if (calTweet.get(tweet) <= 0) {
                    calTweet.remove(tweet);
                    break;
                }
                if (calUser.get(user) <= 0) {
                    continue;
                }
                for (int j = 0; j < sorted.size(); j++) {
                    ArrayList<ArrayList<Integer>> originalMapping = sorted.get(j).getKey();
                    int capMin = result.get(originalMapping);
                    if (capMin > 0) {
                        ArrayList<Integer> oriUser = originalMapping.get(0);
                        ArrayList<Integer> oriTweet = originalMapping.get(1);

                        ArrayList<ArrayList<Integer>> pair1 = paring(user, oriTweet);

                        ArrayList<ArrayList<Integer>> pair2 = paring(oriUser, tweet);

                        int r1 = getResult(pair1);
                        int r2 = getResult(pair2);

                        int cap1 = this.target.get(user) * this.target.get(oriTweet) - r1;
                        int cap2 = this.target.get(oriUser) * this.target.get(tweet) - r2;

                        capMin = getMinimum(cap1, cap2, capMin, calUser, user, calTweet, tweet);

                        if (!pair1.equals(pair2) && !oriUser.equals(tweet) && !user.equals(oriTweet) && capMin > 0) {
                            int reduce = capMin;
                            result.put(originalMapping, result.get(originalMapping) - reduce);
                            result.put(pair2, r2 + reduce);
                            result.put(pair1, r1 + reduce);
                            calTweet.put(tweet, calTweet.get(tweet) - reduce);
                            calUser.put(user, calUser.get(user) - reduce);
                           
                            if (calUser.get(user) <= 0) {
                                mapCount++;
                            }
                            if (calUser.get(user) <= 0 || calTweet.get(tweet) <= 0) {
                                break;
                            }
                        }
                    }
                }

            }
        }

    }

    private void processDistanceMap(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<Integer, ArrayList<ArrayList<Integer>>> tweetDisMap) {
        for (Map.Entry<ArrayList<Integer>, Integer> tweet : calTweet.entrySet()) {
            int sum = 0;
            if (tweet.getValue() <= 0) {
                continue;
            }
            sum = tweet.getKey().get(0) + tweet.getKey().get(1);
            if (!tweetDisMap.containsKey(sum)) {
                tweetDisMap.put(sum, new ArrayList<ArrayList<Integer>>());
            }
            tweetDisMap.get(sum).add(tweet.getKey());
        }
    }

    private void updateValue(HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser, ArrayList<Integer> tweetDegree, ArrayList<Integer> userDegree, HashMap<Integer, ArrayList<ArrayList<Integer>>> transOrders, int value) {
        ArrayList<ArrayList<Integer>> tempTweet = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> tempUser = new ArrayList<ArrayList<Integer>>();

        int tweetSum = tweetDegree.get(0) + tweetDegree.get(1);
        int userSum = userDegree.get(0) + userDegree.get(1);

        calCandidatePool(tweetSum, userSum, calTweet, calUser, tempTweet, tempUser);

        HashMap<Integer, ArrayList<ArrayList<Integer>>> calTweetOrdered = norm1Closest(tweetDegree, tempTweet);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> calUserOrdered = norm1Closest(userDegree, tempUser);
        int budget = value;

        travelsal(transOrders, calTweetOrdered, calUserOrdered, calTweet, calUser, value, budget);

    }

    private HashMap<Integer, ArrayList<ArrayList<Integer>>> norm1Closest(ArrayList<Integer> tweetDegree, ArrayList<ArrayList<Integer>> tempTweet) {

        HashMap<Integer, ArrayList<ArrayList<Integer>>> ordered = new HashMap<>();
        int max = 0;

        for (ArrayList<Integer> sample : tempTweet) {
            int diff = Math.abs(sample.get(0) - tweetDegree.get(0)) + Math.abs(sample.get(1) - tweetDegree.get(1));
            if (diff > max) {
                max = diff;
            }
            if (!ordered.containsKey(sample)) {
                ordered.put(diff, new ArrayList<ArrayList<Integer>>());
            }
            ordered.get(diff).add(sample);
        }

        ordered.put(0, new ArrayList<ArrayList<Integer>>());
        ordered.get(0).add(tweetDegree);
        return ordered;
    }

    private void calCandidatePool(int tweetSum, int userSum, HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser, ArrayList<ArrayList<Integer>> tempTweet, ArrayList<ArrayList<Integer>> tempUser) {
        for (int i = -1 * level; i <= level; i++) {
            if (this.tweetDisMap.containsKey(i + tweetSum)) {
                for (ArrayList<Integer> r : this.tweetDisMap.get(i + tweetSum)) {
                    if (calTweet.containsKey(r)) {
                        tempTweet.add(r);
                    }
                }
            }
            if (this.userDisMap.containsKey(i + userSum)) {
                for (ArrayList<Integer> r : this.userDisMap.get(i + userSum)) {
                    if (calUser.containsKey(r)) {
                        tempUser.add(r);
                    }
                }
            }

        }
    }

    private void travelsal(HashMap<Integer, ArrayList<ArrayList<Integer>>> transOrders, HashMap<Integer, ArrayList<ArrayList<Integer>>> calTweetOrdered, HashMap<Integer, ArrayList<ArrayList<Integer>>> calUserOrdered, HashMap<ArrayList<Integer>, Integer> calTweet, HashMap<ArrayList<Integer>, Integer> calUser, int value, int budget) {
        for (int k = 0; k < 10; k++) {
            if (k >= transOrders.size()) {
                break;
            }
            ArrayList<ArrayList<Integer>> pairs = transOrders.get(k);
            for (int j = 0; j < pairs.size(); j++) {
                ArrayList<Integer> pair = pairs.get(j);
                if (calUserOrdered.containsKey(pair.get(1)) && calTweetOrdered.containsKey(pair.get(0))) {
                    ArrayList<ArrayList<Integer>> calTweetDegrees = calTweetOrdered.get(pair.get(0));
                    ArrayList<ArrayList<Integer>> calUserDegrees = calUserOrdered.get(pair.get(1));
                    for (ArrayList<Integer> calUserDegree : calUserDegrees) {
                        for (ArrayList<Integer> calTweetDegree : calTweetDegrees) {
                            ArrayList<ArrayList<Integer>> tempA = new ArrayList<>();
                            tempA.add(calUserDegree);
                            tempA.add(calTweetDegree);
                            if (calTweet.containsKey(calTweetDegree) && calUser.containsKey(calUserDegree)) {
                                value = Math.min(value, calTweet.get(calTweetDegree));
                                value = Math.min(value, calUser.get(calUserDegree));
                                if (!result.containsKey(tempA)) {
                                    result.put(tempA, 0);
                                }
                                value = (int) Math.min(value, 1.0 * this.target.get(calTweetDegree) * this.target.get(calUserDegree) - result.get(tempA));
                                if (calTweetDegree.equals(calUserDegree)) {
                                    value = (int) Math.min(value, 1.0 * this.target.get(calTweetDegree) * (this.target.get(calUserDegree) - 1) - result.get(tempA));
                                }

                                if (value <= 0) {
                                    value = 0;
                                }

                                calTweet.put(calTweetDegree, calTweet.get(calTweetDegree) - value);
                                calUser.put(calUserDegree, calUser.get(calUserDegree) - value);
                                result.put(tempA, value + result.get(tempA));

                                if (calTweet.get(calTweetDegree) == 0) {
                                    calTweet.remove(calTweetDegree);
                                }

                                if (calUser.get(calUserDegree) == 0) {
                                    calUser.remove(calUserDegree);
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

    private void removeZero(HashMap<ArrayList<Integer>, Integer> calTweet, ArrayList<Integer> calTweetDegree) {
        if (calTweet.get(calTweetDegree) == 0) {
            calTweet.remove(calTweetDegree);
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

}
