package algorithm;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;


public class Gscaler {

    @Option(name = "-i", usage = "input of the graph file", metaVar = "INPUT")
    private String originfile = "";

    @Option(name = "-o", usage = "ouput of the file", metaVar = "OUTPUT")
    private String outputDir = "";

    @Option(name = "-d", usage = "Delimilter of the fields", metaVar = "DELIMILTER")
    private String delim = "\\s+";

    @Option(name = "-f", usage = "Ignore the first line", metaVar = "FIRSTLINE")
    private String ignoreFirst = "0";

    @Option(name = "-sN", usage = "scaled node size", metaVar = "NODE SIZE")
    public int scaledNodeSize = 0;

    @Option(name = "-sE", usage = "scaled edge size", metaVar = "EDGE SIZE")
    public int scaledEdgeSize = 0;

    
   
    public Gscaler() {
    }

    public static void main(String[] args) throws IOException {
        Gscaler gscaler = new Gscaler();
        if (!gscaler.parseCmdLine(args)) {
            System.exit(-1);
        }
        gscaler.run();
    }

    public void run() throws FileNotFoundException, IOException {
        
        System.err.println("extract information");
        FeatureExtraction featureExtraction = new FeatureExtraction();
        DistributionFeature originalFeature = featureExtraction.extractInformation(originfile);
        DistributionFeature scaledFeature = new DistributionFeature();
        scaledFeature.nodeSize = scaledNodeSize;
        scaledFeature.edgeSize = scaledEdgeSize;
       
        System.err.println("scale degrees");
       
        DegreeScaling indegScale = new DegreeScaling();
        scaledFeature.indegreeDis = indegScale.scale(originalFeature.indegreeDis, scaledFeature.edgeSize, scaledFeature.nodeSize, 
                1.0*scaledFeature.nodeSize/originalFeature.nodeSize);
        
        DegreeScaling outdegScale = new DegreeScaling();
        scaledFeature.outdegreeDis = outdegScale.scale(originalFeature.outdegreeDis, scaledFeature.edgeSize, scaledFeature.nodeSize, 1.0*scaledFeature.nodeSize/originalFeature.nodeSize);
        
        System.err.println("synthesis nodes");
        NodeSynthesis nodeSyn = new NodeSynthesis(1.0*scaledFeature.nodeSize/originalFeature.nodeSize);
        HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis = 
                nodeSyn.produceCorrel(scaledFeature.outdegreeDis, scaledFeature.indegreeDis, originalFeature.jointdegreeDis);
        
        
        System.err.println("format conversion");
        HashMap<ArrayList<Integer>, Integer> scaleTargetNodes = calNodeSets(scaledJointDegreeDis, 0);
        HashMap<ArrayList<Integer>, Integer> scaleSourceNodes = calNodeSets(scaledJointDegreeDis, 1);
        
        System.err.println("Edge correlation");
        double s_n = 1.0 * scaledFeature.nodeSize / originalFeature.nodeSize;
        double s_e = 1.0 * scaledFeature.edgeSize / originalFeature.edgeSize / s_n - 1;
        
        CorrelationFunctionScaling correlationFunctionScaling = new CorrelationFunctionScaling(scaledFeature.jointdegreeDis, originalFeature.jointdegreeDis,s_e, s_n);
        scaledFeature.correlationFunction = correlationFunctionScaling.run(originalFeature.correlationFunction, scaleTargetNodes, scaleSourceNodes);
        
        System.err.println("Edge generation");
        EdgeLink edgelink = new EdgeLink();
        HashSet<ArrayList<Integer>> edgeList = edgelink.run(scaledJointDegreeDis, scaledFeature.correlationFunction);
        
        if (edgeList.size() < this.scaledEdgeSize){
            System.err.println("missing: " + (scaledEdgeSize - edgeList.size()));
            finalCheck(edgeList);
        }
        
        
        System.out.println("Output edges");
        PrintWriter pw = new PrintWriter(outputDir);
        for (ArrayList<Integer> pair : edgeList) {
            pw.println(pair.get(1) + " " + pair.get(0));
        }
        pw.close();
      }



 

    

    private HashMap<ArrayList<Integer>, Integer> calNodeSets(HashMap<ArrayList<Integer>, Integer> scaledJointDegreeDis, int i) {
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();
        for (Entry<ArrayList<Integer>, Integer> entry : scaledJointDegreeDis.entrySet()) {
            result.put(entry.getKey(), entry.getValue() * entry.getKey().get(i));
        }
        return result;
    }

   
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

    private boolean parseCmdLine(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
            if (this.originfile.isEmpty() || this.outputDir.isEmpty() || this.scaledNodeSize == 0 || this.scaledEdgeSize == 0) {
                return false;
            }
        } catch (CmdLineException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

}
