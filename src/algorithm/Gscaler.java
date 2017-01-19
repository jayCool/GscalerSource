package algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private String originfile = "C:\\Users\\workshop\\Desktop\\slashdot0811.txt";

    @Option(name = "-o", usage = "ouput of the file", metaVar = "OUTPUT")
    private String outputDir = "C:\\Users\\workshop\\Desktop\\OUTPUT.TXT";

    @Option(name = "-d", usage = "Delimilter of the fields", metaVar = "MODE")
    private String delim = "\\s+";

    @Option(name = "-f", usage = "Ignore the first line", metaVar = "Thread")
    private String ignoreFirst = "0";

    @Option(name = "-sN", usage = "scaled node size", metaVar = "Thread")
    private int scaledNodeSize = 4421;

    @Option(name = "-sE", usage = "scaled edge size", metaVar = "Thread")
    private int scaledEdgeSize = 32179;

    double s_n = 0;
    double s_e = 0;
   
    public Gscaler() {
    }

    public static void main(String[] args) throws IOException {
        Gscaler gscaler = new Gscaler();
        if (!gscaler.parseCmdLine(args)) {
            System.err.println("parameter wrong!!!");
            System.exit(-1);
        }
        gscaler.run();
    }

    public void run() throws FileNotFoundException, IOException {
        
        System.err.println("extract information");
        extractInformation(originfile);
        HashMap<Integer, Integer> scaleIndegree_dis = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> scaleOutdegree_dis = new HashMap<Integer, Integer>();
        
        System.err.println("scale degrees");
        scalInOutDegree(scaleIndegree_dis, scaleOutdegree_dis);
        
        System.err.println("synthesis nodes");
        NodeSynthesis nodeSyn = new NodeSynthesis();
        initCorrVetex(nodeSyn);
        HashMap<ArrayList<Integer>, Integer> scaledJointDegree_Dis = 
                nodeSyn.produceCorrel(scaleOutdegree_dis, scaleIndegree_dis, jointdegreeDis);
        
        
        System.err.println("format conversion");
        HashMap<ArrayList<Integer>, Integer> fuzzyTarget = convertFuzzy(scaledJointDegree_Dis, 0);
        HashMap<ArrayList<Integer>, Integer> fuzzySource = convertFuzzy(scaledJointDegree_Dis, 1);
        
        System.err.println("Edge correlation");
        CorrelationFunctionScaling correlationFunctionScaling = new CorrelationFunctionScaling();
        HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorr = new HashMap<>();
        settleInitialCP(correlationFunctionScaling, scaledJointDegree_Dis);
        scaledCorr = correlationFunctionScaling.run(correlation_function, fuzzyTarget, fuzzySource);
        
        System.err.println("Edge generation");
        EdgeLink edgelink = new EdgeLink();
        edgelink.run(scaledJointDegree_Dis, scaledCorr);
        
        if (edgelink.totalMathching.size() < this.scaledEdgeSize){
            System.err.println("missing: " + (scaledEdgeSize - edgelink.totalMathching.size()));
            finalCheck(edgelink);
        }
        
        PrintWriter pw = new PrintWriter(outputDir);
        for (ArrayList<Integer> pair : edgelink.totalMathching) {
            pw.println(pair.get(1) + " " + pair.get(0));
        }
        pw.close();
      }

    void printRunningTime(long runTime) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(this.outputDir + "_" + s_n + "_" + "0" + "_time.txt", true)));
            pw.println(runTime / 1000);
            pw.close();
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void scalInOutDegree(HashMap<Integer, Integer> scaleIndegreeMap, HashMap<Integer, Integer> scaleOutdegreeMap) throws FileNotFoundException {
        Scaling indegScale = new Scaling();
        indegScale.outputDir = this.outputDir;
        indegScale.s_n = s_n;
        indegScale.s_e = s_e;
        indegScale.scaledNodeSize = this.scaledNodeSize;
        indegScale.scaledEdgeSize = scaledEdgeSize;
        HashMap<Integer, Integer> result1 = new HashMap<>();
        result1 = indegScale.scale(indegreeDis);

        Scaling outdegScale = new Scaling();

        outdegScale.s_n = s_n;
        outdegScale.outputDir = this.outputDir;
        outdegScale.s_e = s_e;
        outdegScale.scaledNodeSize = scaledNodeSize;
        outdegScale.scaledEdgeSize = scaledEdgeSize;
        HashMap<Integer, Integer> result2 = new HashMap<>();
        result2 = outdegScale.scale(outdegreeDis);

        for (Entry<Integer, Integer> entry : result1.entrySet()) {
            scaleIndegreeMap.put(entry.getKey(), entry.getValue());
        }

        for (Entry<Integer, Integer> entry : result2.entrySet()) {
            scaleOutdegreeMap.put(entry.getKey(), entry.getValue());
        }
    }

    private void initCorrVetex(NodeSynthesis nodeSyn) {
        nodeSyn.scaledVertexSize = this.scaledNodeSize;
        nodeSyn.s_n = s_n;
    }

    private void settleInitialCP(CorrelationFunctionScaling edgeSynthesis, HashMap<ArrayList<Integer>, Integer> degreeVertex) {
        edgeSynthesis.scaleJoinDegree_Dis = degreeVertex;
        edgeSynthesis.original_joint_degree_dis = this.jointdegreeDis;
        edgeSynthesis.s_e = this.s_e;
        edgeSynthesis.s_n = this.s_n;
    }

    

    private HashMap<ArrayList<Integer>, Integer> convertFuzzy(HashMap<ArrayList<Integer>, Integer> calCorrVertexTarget, int i) {
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();
        for (Entry<ArrayList<Integer>, Integer> entry : calCorrVertexTarget.entrySet()) {
            if (entry.getValue() > 0) {
            }
            result.put(entry.getKey(), entry.getValue() * entry.getKey().get(i));
        }
        return result;
    }

    HashMap<Integer, Integer> indegreeDis = new HashMap<>();
    HashMap<Integer, Integer> outdegreeDis = new HashMap<>();
    HashMap<ArrayList<Integer>, Integer> jointdegreeDis = new HashMap<>();

    private void extractInformation(String originfile) {
        HashMap<String, Integer> idIndegreeCounts = new HashMap<String, Integer>();
        HashMap<String, Integer> idOutdegreeCounts = new HashMap<String, Integer>();

        count_in_out_degree(originfile, idIndegreeCounts, idOutdegreeCounts);

        HashMap<String, ArrayList<Integer>> idDegree = new HashMap<>();

        process_in_out_bi_frequency_counts(idDegree, idIndegreeCounts, idOutdegreeCounts);
     
        construct_correlation_function(originfile,idDegree);
    }

    HashMap<ArrayList<ArrayList<Integer>>, Integer> correlation_function = new HashMap<>();

    private void finalCheck(EdgeLink edgelink) {
        HashSet<Integer> nodes = new HashSet<>();

        for (ArrayList<Integer> edge : edgelink.totalMathching) {
            nodes.add(edge.get(0));
            nodes.add(edge.get(1));
        }

        ArrayList<Integer> shuffle = new ArrayList<>();
        for (int k : nodes) {
            shuffle.add(k);
        }

        while (edgelink.totalMathching.size() < this.scaledEdgeSize) {
            Collections.shuffle(shuffle);
            int from = shuffle.get(0);
            int to = shuffle.get(1);
            ArrayList<Integer> nPair = new ArrayList<>();
            nPair.add(to);
            nPair.add(from);
            if (!edgelink.totalMathching.contains(nPair)) {
                edgelink.totalMathching.add(nPair);
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

    private void count_in_out_degree(String originfile, HashMap<String, Integer> idIndegreeCounts, 
            HashMap<String, Integer> idOutdegreeCounts) {
        InputStream input = null;
        try {
            input = new FileInputStream(originfile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            int maxIndegree = 0, maxOutdegree = 0;
            int minMaxDegree = 0;
            while (line != null) {
                String[] temp = line.split("[^a-zA-Z0-9']+");
                String fid = temp[0];
                String uid = temp[1];
                if (idIndegreeCounts.containsKey(uid)) {
                    idIndegreeCounts.put(uid, idIndegreeCounts.get(uid) + 1);
                } else {
                    idIndegreeCounts.put(uid, 1);
                }
                if (!idIndegreeCounts.containsKey(fid)) {
                    idIndegreeCounts.put(fid, 0);
                }

                if (idOutdegreeCounts.containsKey(fid)) {
                    idOutdegreeCounts.put(fid, idOutdegreeCounts.get(fid) + 1);
                } else {
                    idOutdegreeCounts.put(fid, 1);
                }
                if (!idOutdegreeCounts.containsKey(uid)) {
                    idOutdegreeCounts.put(uid, 0);
                }
                maxIndegree = Math.max(maxIndegree, idIndegreeCounts.get(uid));
                maxOutdegree = Math.max(maxOutdegree, idOutdegreeCounts.get(fid));
                line = reader.readLine();
            }
            minMaxDegree = Math.min(maxIndegree, maxOutdegree);
            if (minMaxDegree < 1.0 * this.scaledEdgeSize / this.scaledNodeSize) {
                PrintWriter pw = new PrintWriter(new File(this.outputDir + "/" + "exception.txt"));
                pw.println("Scaled Average Degree Is Greater Than The Original Maximum Degree");
                pw.close();
                System.exit(-1);
            }
            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void process_in_out_bi_frequency_counts(HashMap<String, ArrayList<Integer>> idDegree, 
            HashMap<String, Integer> idIndegreeCounts, HashMap<String, Integer> idOutdegreeCounts) {
        for (Entry<String, Integer> entry : idIndegreeCounts.entrySet()) {
            if (indegreeDis.containsKey(entry.getValue())) {
                indegreeDis.put(entry.getValue(), indegreeDis.get(entry.getValue()) + 1);
            } else {
                indegreeDis.put(entry.getValue(), 1);
            }

            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getValue());
            arr.add(idOutdegreeCounts.get(entry.getKey()));
            idDegree.put(entry.getKey(), arr);

            if (jointdegreeDis.containsKey(arr)) {
                jointdegreeDis.put(arr, jointdegreeDis.get(arr) + 1);
            } else {
                jointdegreeDis.put(arr, 1);
            }
        }

        for (Entry<String, Integer> entry : idOutdegreeCounts.entrySet()) {
            if (outdegreeDis.containsKey(entry.getValue())) {
                outdegreeDis.put(entry.getValue(), outdegreeDis.get(entry.getValue()) + 1);
            } else {
                outdegreeDis.put(entry.getValue(), 1);
            }
        }
    }

    private void construct_correlation_function(String originfile, HashMap<String, ArrayList<Integer>> idDegree) {
        try {
            int nodesize = idDegree.size();
            int edgesize = 0;
            InputStream input = null;
            
            input = new FileInputStream(originfile);
            BufferedReader bb = new BufferedReader(new InputStreamReader(input));
            
            String line = bb.readLine();
            while (line != null) {
                String temp[] = line.split("[^a-zA-Z0-9']+");
                String u = temp[1];
                String f = temp[0];
                if (u != f) {
                    ArrayList<Integer> arr1 = idDegree.get(u);
                    ArrayList<Integer> arr2 = idDegree.get(f);
                    ArrayList<ArrayList<Integer>> arrs = new ArrayList<>(2);
                    arrs.add(arr1);
                    arrs.add(arr2);
                    if (!correlation_function.containsKey(arrs)) {
                        correlation_function.put(arrs, 1);
                    } else {
                        correlation_function.put(arrs, 1 + correlation_function.get(arrs));
                    }
                }
                edgesize ++;
                line = bb.readLine();
            
            }
            bb.close();

            this.s_n = 1.0 * this.scaledNodeSize / nodesize;
            this.s_e = 1.0 * this.scaledEdgeSize / edgesize / this.s_n - 1;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
}

}
