/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Zhang Jiangwei
 * Special Note: Degree scaling is purposely written in sequential algorithm.
 * Users can easily modify it to parallel which improves the efficiency by ~30% by using multi-threading.
 */
public class Gscaler {
    int scaledEdgeSize;
    int scaledNodeSize;
    String originfile = "";
    String outputDir = ""; 
     
    Gscaler(int scaledEdgeSize, int scaledNodeSize, String outputDir, String originfile) {
        this.scaledEdgeSize = scaledEdgeSize;
        this.scaledNodeSize = scaledNodeSize;
        this.originfile = originfile;
        this.outputDir = outputDir;
    }

    public void run() throws FileNotFoundException, IOException {

        System.out.println("extract information");
        FeatureExtraction featureExtraction = new FeatureExtraction();
        DistributionFeature originalFeature = featureExtraction.extractInformation(originfile);
        DistributionFeature scaledFeature = new DistributionFeature();
        scaledFeature.nodeSize = scaledNodeSize;
        scaledFeature.edgeSize = scaledEdgeSize;

        System.out.println("scale degrees");
        DegreeScaling indegScale = new DegreeScaling();
        scaledFeature.indegreeDis = indegScale.scale(originalFeature.indegreeDis, scaledFeature.edgeSize, scaledFeature.nodeSize,
                1.0 * scaledFeature.nodeSize / originalFeature.nodeSize);

        DegreeScaling outdegScale = new DegreeScaling();
        scaledFeature.outdegreeDis = outdegScale.scale(originalFeature.outdegreeDis, scaledFeature.edgeSize, scaledFeature.nodeSize, 1.0 * scaledFeature.nodeSize / originalFeature.nodeSize);
        
        
        System.out.println("synthesis nodes");
        NodeSynthesis nodeSyn = new NodeSynthesis(1.0 * scaledFeature.nodeSize / originalFeature.nodeSize);
        scaledFeature.jointdegreeDis
                = nodeSyn.synthesizeNode(scaledFeature.outdegreeDis, scaledFeature.indegreeDis, originalFeature.jointdegreeDis);

        System.out.println("format conversion");
        HashMap<ArrayList<Integer>, Integer> scaleTargetNodes = calNodeAppearances( scaledFeature.jointdegreeDis, 0);
        HashMap<ArrayList<Integer>, Integer> scaleSourceNodes = calNodeAppearances( scaledFeature.jointdegreeDis, 1);

        System.out.println("Edge correlation");
        double s_n = 1.0 * scaledFeature.nodeSize / originalFeature.nodeSize;
        double s_e = 1.0 * scaledFeature.edgeSize / originalFeature.edgeSize / s_n - 1;

        
        CorrelationFunctionScaling correlationFunctionScaling = new CorrelationFunctionScaling(scaledFeature.jointdegreeDis, originalFeature.jointdegreeDis, s_e, s_n);
        scaledFeature.correlationFunction = correlationFunctionScaling.synthesize(originalFeature.correlationFunction, scaleTargetNodes, scaleSourceNodes);

        System.out.println("Edge generation");
        EdgeLink edgelink = new EdgeLink();
        HashSet<ArrayList<Integer>> edgeList = edgelink.generateLinks(scaledFeature.jointdegreeDis, scaledFeature.correlationFunction);
        
        if (edgeList.size() < this.scaledEdgeSize) {
            System.out.println("missing: " + (scaledEdgeSize - edgeList.size()));
            finalCheck(edgeList);
        }

        System.out.println("Output edges");
        try (PrintWriter pw = new PrintWriter(outputDir)) {
            for (ArrayList<Integer> pair : edgeList) {
                pw.println(pair.get(1) + " " + pair.get(0));
            }
            pw.close();
        }
    }

    
    /**
     * This method calculates the appearance of the nodes
     * @param scaledJointDegreeDis
     * @param i (indegree/outdegree)
     * @return The appearance of the node sets
     */
    private HashMap<ArrayList<Integer>, Integer> calNodeAppearances(HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis, int i) {
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();
        for (Map.Entry<ArrayList<Integer>, Integer> entry : scaledJointDegreeDis.entrySet()) {
            result.put(entry.getKey(), entry.getValue() * entry.getKey().get(i));
        }
        return result;
    }
    
    /**
     * Checks if the edgelist satisfy the scaledEdgeSize
     * @param edgeList 
     */
    private void finalCheck(HashSet<ArrayList<Integer>> edgeList) {
        HashSet<Integer> nodes = new HashSet<>();

        for (ArrayList<Integer> edge : edgeList) {
            nodes.add(edge.get(0));
            nodes.add(edge.get(1));
        }

        ArrayList<Integer> shuffle = new ArrayList<>();
        for (int k : nodes) {
            shuffle.add(k);
        }

        while (edgeList.size() < this.scaledEdgeSize) {
            Collections.shuffle(shuffle);
            int from = shuffle.get(0);
            int to = shuffle.get(1);
            ArrayList<Integer> nPair = new ArrayList<>();
            nPair.add(to);
            nPair.add(from);
            if (!edgeList.contains(nPair)) {
                edgeList.add(nPair);
            }
        }
    }    
}
