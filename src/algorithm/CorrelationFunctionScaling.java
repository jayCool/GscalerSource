package algorithm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/*
 * @author Zhang Jiangwei
 * Special Note: The nearest source Node/targetNode can be found using the mapping from node synthesis
 */
public class CorrelationFunctionScaling {

    private double s_n = 0.2;
    private double s_e = 0.0;

    private HashMap<ArrayList<Integer>, Integer> scaleJointDegreeDis = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> sourecDisMap = new HashMap<>();
    HashMap<Integer, ArrayList<ArrayList<Integer>>> targetDisMap = new HashMap<>();
    private final HashMap<ArrayList<Integer>, Integer> originalJointDegreeDis;

    int maxRange = 0;

    int iterationNumber = 0;

    CorrelationFunctionScaling(HashMap<ArrayList<Integer>, Integer> jointdegreeDis,
            HashMap<ArrayList<Integer>, Integer> jointdegreeDis0, double s_e, double s_n) {
        scaleJointDegreeDis = jointdegreeDis;
        originalJointDegreeDis = jointdegreeDis0;
        this.s_e = s_e;
        this.s_n = s_n;
    }

    HashMap<ArrayList<ArrayList<Integer>>, Integer> synthesize(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes) throws FileNotFoundException {
        iterationNumber = 0;
        HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale = new HashMap<>();

        synthesizeCorrelationFunction(scaleSourceNodes, scaleTargetNodes, correlatedOriginal, correlatedScale);

        doubleChecking(correlatedScale);

        return correlatedScale;
    }

    /**
     *
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param correlatedOriginal
     * @param correlatedScale
     * @throws FileNotFoundException
     */
    private void synthesizeCorrelationFunction(
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) throws FileNotFoundException {

        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortedOriginalCorrelationFunction = sortCorrelation(correlatedOriginal);

        int num = calMinLeftOverFrequencies(scaleSourceNodes, scaleTargetNodes);

        HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders = produceTraversalOrders();

        while (!scaleSourceNodes.keySet().isEmpty() && !scaleTargetNodes.keySet().isEmpty() && iterationNumber <= 1 && num > 1000) {
            processDistanceMap(scaleSourceNodes, sourecDisMap);
            processDistanceMap(scaleTargetNodes, targetDisMap);

            loopForCorrelationFunctionSynthesizing(traversalOrders, scaleSourceNodes, scaleTargetNodes, correlatedScale, sortedOriginalCorrelationFunction);

            num = calMinLeftOverFrequencies(scaleSourceNodes, scaleTargetNodes);
            iterationNumber += 1;
        }

        randomSynthesize(correlatedScale, scaleSourceNodes, scaleTargetNodes);

        adjustExistingDistribution(scaleSourceNodes, scaleTargetNodes, correlatedScale);

        num = calMinLeftOverFrequencies(scaleSourceNodes, scaleTargetNodes);
        if (num < 0) {
            System.err.println("exception");
        }
    }

    /**
     *
     *
     * Calculate the traversal order of the 2d join degree vector For faster
     * norm 1 calculation
     *
     * @return traversalOrders
     *
     *
     */
    HashMap<Integer, ArrayList<ArrayList<Integer>>> produceTraversalOrders() {
        HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders = new HashMap<>();
        for (int i = 0; i < 2 * Constant.CLEANING_THRESHOLD; i++) {
            ArrayList<ArrayList<Integer>> pairs = new ArrayList<>();
            for (int j = 0; j <= i; j++) {
                ArrayList<Integer> p1 = new ArrayList<>();
                p1.add(j);
                p1.add(i - j);
                pairs.add(p1);
            }
            traversalOrders.put(i, pairs);
        }
        return traversalOrders;
    }

    /**
     * The method synthesize correlation function by combining elements from
     * scaledSourceNodes/scaledTargetNodes The rule is following the original
     * correlation function, The result are stored in scaledCorrelationFunction
     *
     * @param traversalOrders
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param scaledCorrelationFunction
     * @param sortedOriginalCorrelationFunction
     */
    private void loopForCorrelationFunctionSynthesizing(HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction, List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortedOriginalCorrelationFunction) {

        int scaledFrequency = 0;
        maxRange = (int) (Math.pow(10, iterationNumber * 2 - 1));

        for (int i = 0; i < sortedOriginalCorrelationFunction.size() && !scaleSourceNodes.keySet().isEmpty() && !scaleTargetNodes.keySet().isEmpty(); i++) {
            Map.Entry<ArrayList<ArrayList<Integer>>, Integer> originalCorrelationFunctionEntry = sortedOriginalCorrelationFunction.get(i);

            scaledFrequency = scaleFrequency(originalCorrelationFunctionEntry.getValue());
            if (scaledFrequency == 0) {
                continue;
            }

            ArrayList<Integer> targetDegree = originalCorrelationFunctionEntry.getKey().get(0);
            ArrayList<Integer> sourceDegree = originalCorrelationFunctionEntry.getKey().get(1);

            synthesizeIndividualCorrelationFunction(scaleSourceNodes, scaleTargetNodes, sourceDegree, targetDegree, traversalOrders, scaledFrequency, scaledCorrelationFunction);
        }

    }

    /**
     *
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @return minimum sum of frequencies
     */
    private int calMinLeftOverFrequencies(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes) {
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

    /**
     * This method randomly synthesize left-over nodes
     *
     * @param scaledCorrelationFunction
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     */
    private void randomSynthesize(HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes) {
        ArrayList<ArrayList<Integer>> calSourceDegrees = new ArrayList<>(scaleSourceNodes.keySet());
        ArrayList<ArrayList<Integer>> calTargetDegrees = new ArrayList<>(scaleTargetNodes.keySet());

        for (ArrayList<Integer> calSourceDegree : calSourceDegrees) {
            if (scaleSourceNodes.get(calSourceDegree) == 0) {
                continue;
            }

            for (ArrayList<Integer> calTargetDegree : calTargetDegrees) {
                //checking early return or break or continue
                if (scaleSourceNodes.keySet().isEmpty() || scaleTargetNodes.keySet().isEmpty()) {
                    return;
                }

                if (CleaningMap.cleanHashMap(scaleSourceNodes, calSourceDegree)) {
                    break;
                }
                if (CleaningMap.cleanHashMap(scaleTargetNodes, calTargetDegree)) {
                    continue;
                }
                //finish checking early return or break or continue

                ArrayList<ArrayList<Integer>> edgeCorrelation = new ArrayList<>();
                edgeCorrelation.add(calTargetDegree);
                edgeCorrelation.add(calSourceDegree);

                int incrementedFrequency = calculateIncrementedFrequency(scaledCorrelationFunction, edgeCorrelation, calSourceDegree, scaleSourceNodes, calTargetDegree, scaleTargetNodes);
                if (incrementedFrequency <= 0) {
                    continue;
                }
                
                updateSourceTargetCorrelationFunction(scaleSourceNodes, scaleTargetNodes, calSourceDegree, calTargetDegree, incrementedFrequency, scaledCorrelationFunction, edgeCorrelation);

            }
        }
    }
    
    
    /**
     * Breaks the existing edge correlation
     * Forms two more edge correlation
     * 
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param scaledCorrelationFunction 
     */
    private void adjustExistingDistribution(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction) {

        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortedEntries = calculateSortedEntries(scaledCorrelationFunction);

        int mapCount = 0;

        ArrayList<ArrayList<Integer>> sourceList = extractSourceList(scaleSourceNodes);

        for (ArrayList<Integer> sourceDegree : sourceList) {
            //cleaning zero frequency entries
            if (CleaningMap.cleanHashMap(scaleSourceNodes, sourceDegree)) {
                continue;
            }

            if (mapCount > Constant.CLEANING_THRESHOLD) {
                CleaningMap.removeZero(scaleTargetNodes);
                mapCount = 0;
            }
            //finish cleaning

            for (ArrayList<Integer> targetDegree : scaleTargetNodes.keySet()) {
                //cleaning zer0 frequency entries
                if (CleaningMap.cleanHashMap(scaleSourceNodes, sourceDegree)) {
                    break;
                }
                if (scaleTargetNodes.get(targetDegree) <= 0) {
                    mapCount++;
                    continue;
                }
                //finish cleaning
                
                Random rand = new Random();
                int currentIndex = rand.nextInt(sortedEntries.size());
                for (int counter = 0; counter < sortedEntries.size(); counter++) {
                    currentIndex = (currentIndex + 1) % sortedEntries.size();
                    ArrayList<ArrayList<Integer>> existingEdgeCorrelation = sortedEntries.get(currentIndex).getKey();

                    int adjustableNumber = scaledCorrelationFunction.get(existingEdgeCorrelation);

                    if (adjustableNumber > 0) {
                        ArrayList<Integer> originalTarget = existingEdgeCorrelation.get(0);
                        ArrayList<Integer> originalSource = existingEdgeCorrelation.get(1);

                        ArrayList<ArrayList<Integer>> newPair1 = paring(targetDegree, originalSource);
                        ArrayList<ArrayList<Integer>> newPair2 = paring(originalTarget, sourceDegree);

                        if (newPair1.equals(newPair2) || originalTarget.equals(sourceDegree) || targetDegree.equals(originalSource)) {
                            continue;
                        }

                        adjustableNumber = calAdjustableNumber(scaleSourceNodes, scaleTargetNodes, newPair1, newPair2, scaledCorrelationFunction, targetDegree, sourceDegree, originalSource, originalTarget, adjustableNumber);

                        if (adjustableNumber > 0) {
                            updateTwoNewPairs(existingEdgeCorrelation, scaledCorrelationFunction, adjustableNumber, newPair1, newPair2, sourceDegree, targetDegree, scaleSourceNodes, scaleTargetNodes);

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

    /**
     * The method returns a map, where the key is the sum of the joint-degree,
     * and the value is the joint-degrees with that sum
     *
     * @param calSource
     * @param sourceDisMap
     */
    private void processDistanceMap(HashMap<ArrayList<Integer>, Integer> calSource, HashMap<Integer, ArrayList<ArrayList<Integer>>> sourceDisMap) {
        sourceDisMap.clear();

        for (Map.Entry<ArrayList<Integer>, Integer> sourceEntry : calSource.entrySet()) {
            if (sourceEntry.getValue() <= 0) {
                continue;
            }
            int sum = sourceEntry.getKey().get(0) + sourceEntry.getKey().get(1);
            if (!sourceDisMap.containsKey(sum)) {
                sourceDisMap.put(sum, new ArrayList<ArrayList<Integer>>());
            }
            sourceDisMap.get(sum).add(sourceEntry.getKey());
        }
    }
    
    
    /**
     * Given each individual correlation function entry,
     * it synthesizes the nodes.
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param sourceDegree
     * @param targetDegree
     * @param traversalOrders
     * @param scaledFrequency
     * @param correlatedScale 
     */
    private void synthesizeIndividualCorrelationFunction(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            ArrayList<Integer> sourceDegree, ArrayList<Integer> targetDegree,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders, int scaledFrequency,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {

        ArrayList<ArrayList<Integer>> sourceNodePools = new ArrayList<>();
        ArrayList<ArrayList<Integer>> targetNodePools = new ArrayList<>();

        int sourceSum = sourceDegree.get(0) + sourceDegree.get(1);
        int targetSum = targetDegree.get(0) + targetDegree.get(1);

        calculateCandidatePool(sourceSum, targetSum, scaleSourceNodes, scaleTargetNodes, sourceNodePools, targetNodePools);

        HashMap<Integer, ArrayList<ArrayList<Integer>>> calSourceOrdered = norm1ClosestJointDegree(sourceDegree, sourceNodePools);
        HashMap<Integer, ArrayList<ArrayList<Integer>>> calTargetOrdered = norm1ClosestJointDegree(targetDegree, targetNodePools);

        traversalOverTheCandidates(traversalOrders, calSourceOrdered, calTargetOrdered, scaleSourceNodes, scaleTargetNodes, scaledFrequency, correlatedScale);

    }

    /**
     *
     * @param sourceDegree
     * @param sourcePools
     * @return distance map of the candidates
     */
    private HashMap<Integer, ArrayList<ArrayList<Integer>>> norm1ClosestJointDegree(
            ArrayList<Integer> sourceDegree, ArrayList<ArrayList<Integer>> sourcePools) {

        HashMap<Integer, ArrayList<ArrayList<Integer>>> ordered = new HashMap<>();
        int max = 0;

        for (ArrayList<Integer> sample : sourcePools) {
            int diff = Math.abs(sample.get(0) - sourceDegree.get(0)) + Math.abs(sample.get(1) - sourceDegree.get(1));
            max = Math.max(diff, max);

            // type does not match. by swike
            if (!ordered.containsKey(diff)) {
                ordered.put(diff, new ArrayList<ArrayList<Integer>>());
            }
            ordered.get(diff).add(sample);
        }

        ordered.put(0, new ArrayList<ArrayList<Integer>>());
        ordered.get(0).add(sourceDegree);
        return ordered;
    }
    
    /**
     * This method calculates the candidates based on the sum distance.
     * @param sourceSum
     * @param targetSum
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param sourcePools
     * @param targetPools 
     */
    private void calculateCandidatePool(int sourceSum, int targetSum, HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes,
            ArrayList<ArrayList<Integer>> sourcePools, ArrayList<ArrayList<Integer>> targetPools) {
        for (int i = -1 * maxRange; i <= maxRange; i++) {
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
    
    
    /**
     * Traversal over the nodes based on the norm 1 distance between 2d vectors
     * @param traversalOrders
     * @param calSourceOrdered
     * @param calTargetOrdered
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param scaledFrequency
     * @param correlatedScale 
     */
    private void traversalOverTheCandidates(HashMap<Integer, ArrayList<ArrayList<Integer>>> traversalOrders,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> calSourceOrdered,
            HashMap<Integer, ArrayList<ArrayList<Integer>>> calTargetOrdered,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, int scaledFrequency,
            HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale) {
        int budget = scaledFrequency;

        for (int k = 0; k < traversalOrders.size(); k++) {
            for (ArrayList<Integer> pair : traversalOrders.get(k)) {
                if (calTargetOrdered.containsKey(pair.get(1)) && calSourceOrdered.containsKey(pair.get(0))) {
                    ArrayList<ArrayList<Integer>> calSourceDegrees = calSourceOrdered.get(pair.get(0));
                    ArrayList<ArrayList<Integer>> calTargetDegrees = calTargetOrdered.get(pair.get(1));

                    for (ArrayList<Integer> calTargetDegree : calTargetDegrees) {
                        for (ArrayList<Integer> calSourceDegree : calSourceDegrees) {
                            if (scaleSourceNodes.containsKey(calSourceDegree) && scaleTargetNodes.containsKey(calTargetDegree)) {

                                ArrayList<ArrayList<Integer>> edgeCorrelation = new ArrayList<>();
                                edgeCorrelation.add(calTargetDegree);
                                edgeCorrelation.add(calSourceDegree);

                                scaledFrequency = calFrequencyForGivenSourceTarget(scaleSourceNodes, scaleTargetNodes, calSourceDegree, calTargetDegree, scaledFrequency, correlatedScale, edgeCorrelation);

                                if (scaledFrequency <= 0) {
                                    scaledFrequency = budget;
                                    continue;
                                }

                                updateSourceTargetCorrelationFunction(scaleSourceNodes, scaleTargetNodes, calSourceDegree, calTargetDegree, scaledFrequency, correlatedScale, edgeCorrelation);

                                budget = budget - scaledFrequency;
                                if (budget == 0) {
                                    return;
                                }
                                scaledFrequency = budget;
                            }
                        }
                    }
                }

            }
        }
    }
    
    /**
     * 
     * @param targetDegree
     * @param sourceDegree
     * @return pairedEdge
     */
    private ArrayList<ArrayList<Integer>> paring(ArrayList<Integer> targetDegree, ArrayList<Integer> sourceDegree) {
        ArrayList<ArrayList<Integer>> pair1 = new ArrayList<>();
        pair1.add(targetDegree);
        pair1.add(sourceDegree);
        return pair1;
    }
    
    /**
     * 
     * @param pair
     * @param scaledCorrelationFunction
     * @return countsOfThePair
     */
    private int getCount(ArrayList<ArrayList<Integer>> pair, HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction) {
        int count = 0;
        if (scaledCorrelationFunction.containsKey(pair)) {
            count = scaledCorrelationFunction.get(pair);
        }
        return count;

    }

    private int getMinimum(int necessaryConditionAllowedNumber1, int necessaryConditionAllowedNumber2, int adjustableNumber,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, ArrayList<Integer> targetDegree,
            HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, ArrayList<Integer> sourceDegree) {
        adjustableNumber = Math.min(necessaryConditionAllowedNumber1, adjustableNumber);
        adjustableNumber = Math.min(necessaryConditionAllowedNumber2, adjustableNumber);
        adjustableNumber = Math.min(adjustableNumber, scaleTargetNodes.get(targetDegree));
        adjustableNumber = Math.min(adjustableNumber, scaleSourceNodes.get(sourceDegree));
        return adjustableNumber;
    }

    /**
     * This method checks if the scaled correlation function satisfies the
     * necessary condition.
     *
     * @param scaledCorrelationFcuntion
     */
    private void doubleChecking(HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFcuntion) {
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry : scaledCorrelationFcuntion.entrySet()) {
            int frequency = 0;
            if (entry.getKey().get(0).equals(entry.getKey().get(1))) {
                frequency = (int) (1.0 * this.scaleJointDegreeDis.get(entry.getKey().get(0)) * (this.scaleJointDegreeDis.get(entry.getKey().get(0)) - 1) - entry.getValue());
            } else {
                frequency = (int) (1.0 * this.scaleJointDegreeDis.get(entry.getKey().get(0)) * (this.scaleJointDegreeDis.get(entry.getKey().get(1)) - 0) - entry.getValue());
            }

            if (frequency < 0) {
                System.err.println("Error: Negative value in correlation function");
            }

        }
    }

    /**
     * The correlation function is sorted based on frequencies
     *
     * @param correlatedOriginal
     * @return sortedCorrelationFunction
     */
    private List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortCorrelation(HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedOriginal) {
        Sort so = new Sort();
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortedOriginalCorrelationFunction
                = so.sortOnAppearance(correlatedOriginal, originalJointDegreeDis);
        return sortedOriginalCorrelationFunction;
    }

    /**
     *
     * @param originalFrequency
     * @return scaledFrequency
     */
    private int scaleFrequency(int originalFrequency) {
        double floatValue = originalFrequency * s_n * (1 + Math.max(0, s_e));
        int value = (int) (floatValue);
        double difff = floatValue - value;

        double kl = Math.random();
        if (kl < difff) {
            value++;
        }
        return value;
    }
    
    
    /**
     * 
     * @param scaleSourceNodes
     * @return listOfSourceNodes
     */
    private ArrayList<ArrayList<Integer>> extractSourceList(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes) {
        ArrayList<ArrayList<Integer>> sourceNodeList = new ArrayList<>();
        for (ArrayList<Integer> sourceNode : scaleSourceNodes.keySet()) {
            sourceNodeList.add(sourceNode);
        }
        return sourceNodeList;
    }
    
    /**
     * 
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param calSourceDegree
     * @param calTargetDegree
     * @param scaledFrequency
     * @param correlatedScale
     * @param edgeCorrelation
     * @return incremantalFrequency
     */
    private int calFrequencyForGivenSourceTarget(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, ArrayList<Integer> calSourceDegree, ArrayList<Integer> calTargetDegree, int scaledFrequency, HashMap<ArrayList<ArrayList<Integer>>, Integer> correlatedScale, ArrayList<ArrayList<Integer>> edgeCorrelation) {
        scaledFrequency = Math.min(scaledFrequency, scaleSourceNodes.get(calSourceDegree));
        scaledFrequency = Math.min(scaledFrequency, scaleTargetNodes.get(calTargetDegree));

        if (!correlatedScale.containsKey(edgeCorrelation)) {
            correlatedScale.put(edgeCorrelation, 0);
        }

        scaledFrequency = (int) Math.min(scaledFrequency, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * this.scaleJointDegreeDis.get(calTargetDegree) - correlatedScale.get(edgeCorrelation));
        if (calSourceDegree.equals(calTargetDegree)) {
            scaledFrequency = (int) Math.min(scaledFrequency, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * (this.scaleJointDegreeDis.get(calTargetDegree) - 1) - correlatedScale.get(edgeCorrelation));
        }
        return scaledFrequency;
    }

    /**
     * Update the statistics for the random synthesis
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param calSourceDegree
     * @param calTargetDegree
     * @param scaledFrequency
     * @param scaledCorrelationFunction
     * @param edgeCorrelation 
     */
    private void updateSourceTargetCorrelationFunction(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, ArrayList<Integer> calSourceDegree, ArrayList<Integer> calTargetDegree, int scaledFrequency, HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction, ArrayList<ArrayList<Integer>> edgeCorrelation) {
        scaleSourceNodes.put(calSourceDegree, scaleSourceNodes.get(calSourceDegree) - scaledFrequency);
        scaleTargetNodes.put(calTargetDegree, scaleTargetNodes.get(calTargetDegree) - scaledFrequency);
        int oldFrequency = 0;
        if (scaledCorrelationFunction.containsKey(edgeCorrelation)) {
            oldFrequency = scaledCorrelationFunction.get(edgeCorrelation);
        }
        scaledCorrelationFunction.put(edgeCorrelation, scaledFrequency + oldFrequency);

        CleaningMap.cleanHashMap(scaleSourceNodes, calSourceDegree);
        CleaningMap.cleanHashMap(scaleTargetNodes, calTargetDegree);
    }

    /**
     *
     * @param calSourceDegree
     * @param scaleSourceNodes
     * @param calTargetDegree
     * @param scaleTargetNodes
     * @param oldFrequency
     * @return incrementedFrequency
     */
    private int calculateIncrementedFrequency(HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction,
            ArrayList<ArrayList<Integer>> edgeCorrelation,
            ArrayList<Integer> calSourceDegree, HashMap<ArrayList<Integer>, Integer> scaleSourceNodes,
            ArrayList<Integer> calTargetDegree, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes) {
        int oldFrequency = 0;
        if (scaledCorrelationFunction.containsKey(edgeCorrelation)) {
            oldFrequency = scaledCorrelationFunction.get(edgeCorrelation);
        }
        int incrementedFrequency = scaleSourceNodes.get(calSourceDegree);
        incrementedFrequency = Math.min(incrementedFrequency, scaleTargetNodes.get(calTargetDegree));

        incrementedFrequency = (int) Math.min(incrementedFrequency, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * this.scaleJointDegreeDis.get(calTargetDegree) - oldFrequency);
        if (calSourceDegree.equals(calTargetDegree)) {
            incrementedFrequency = (int) Math.min(incrementedFrequency, 1.0 * this.scaleJointDegreeDis.get(calSourceDegree) * (this.scaleJointDegreeDis.get(calTargetDegree) - 1) - oldFrequency);
        }
        return incrementedFrequency;
    }
    
    
    /**
     * 
     * @param scaledCorrelationFunction
     * @return sortedScaledCorrelationEntries
     */
    private List<Entry<ArrayList<ArrayList<Integer>>, Integer>> calculateSortedEntries(HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction) {
        HashMap<ArrayList<ArrayList<Integer>>, Integer> tempResult = new HashMap<>();
        for (Map.Entry<ArrayList<ArrayList<Integer>>, Integer> entry : scaledCorrelationFunction.entrySet()) {
            tempResult.put(entry.getKey(), entry.getValue());
        }
        return new Sort().sortOnKeySum(tempResult);
    }

    /**
     *
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     * @param newPair1
     * @param newPair2
     * @param scaledCorrelationFunction
     * @param targetDegree
     * @param sourceDegree
     * @param originalSource
     * @param originalTarget
     * @param adjustableNumber
     * @return adjustableNumber
     */
    private int calAdjustableNumber(HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes, ArrayList<ArrayList<Integer>> newPair1, ArrayList<ArrayList<Integer>> newPair2, HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction, ArrayList<Integer> targetDegree, ArrayList<Integer> sourceDegree, ArrayList<Integer> originalSource, ArrayList<Integer> originalTarget, int adjustableNumber) {

        int count1 = getCount(newPair1, scaledCorrelationFunction);
        int count2 = getCount(newPair2, scaledCorrelationFunction);

        int necessaryConditionAllowedNumber1 = this.scaleJointDegreeDis.get(targetDegree) * this.scaleJointDegreeDis.get(originalSource) - count1;
        int necessaryConditionAllowedNumber2 = this.scaleJointDegreeDis.get(originalTarget) * this.scaleJointDegreeDis.get(sourceDegree) - count2;

        adjustableNumber = getMinimum(necessaryConditionAllowedNumber1, necessaryConditionAllowedNumber2, adjustableNumber,
                scaleTargetNodes, targetDegree, scaleSourceNodes, sourceDegree);
        return adjustableNumber;
    }

    /**
     * This method updates the corresponding statistics
     *
     * @param existingEdgeCorrelation
     * @param scaledCorrelationFunction
     * @param reduce
     * @param newPair1
     * @param newPair2
     * @param sourceDegree
     * @param targetDegree
     * @param scaleSourceNodes
     * @param scaleTargetNodes
     */
    private void updateTwoNewPairs(ArrayList<ArrayList<Integer>> existingEdgeCorrelation, HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorrelationFunction, int reduce, ArrayList<ArrayList<Integer>> newPair1, ArrayList<ArrayList<Integer>> newPair2, ArrayList<Integer> sourceDegree, ArrayList<Integer> targetDegree, HashMap<ArrayList<Integer>, Integer> scaleSourceNodes, HashMap<ArrayList<Integer>, Integer> scaleTargetNodes) {
        int count1 = getCount(newPair1, scaledCorrelationFunction);
        int count2 = getCount(newPair2, scaledCorrelationFunction);
        scaledCorrelationFunction.put(existingEdgeCorrelation, scaledCorrelationFunction.get(existingEdgeCorrelation) - reduce);
        scaledCorrelationFunction.put(newPair2, count2 + reduce);
        scaledCorrelationFunction.put(newPair1, count1 + reduce);
        scaleSourceNodes.put(sourceDegree, scaleSourceNodes.get(sourceDegree) - reduce);
        scaleTargetNodes.put(targetDegree, scaleTargetNodes.get(targetDegree) - reduce);
    }

}
