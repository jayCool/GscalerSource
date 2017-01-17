package algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 *
 * @author workshop
 */
public class Gscaler {

    @Option(name = "-i", usage = "input of the graph file", metaVar = "INPUT")
    private String originfile = "";

    @Option(name = "-o", usage = "ouput of the file", metaVar = "OUTPUT")
    private String outputDir = "";

    @Option(name = "-d", usage = "Delimilter of the fields", metaVar = "MODE")
    private String delim = "\\s+";

    @Option(name = "-f", usage = "Ignore the first line", metaVar = "Thread")
    private String ignoreFirst = "0";

    @Option(name = "-sN", usage = "scaled node size", metaVar = "Thread")
    private int scaledNodeSize = 0;

    @Option(name = "-sE", usage = "scaled edge size", metaVar = "Thread")
    private int scaledEdgeSize = 0;

    double s = 1.0 / 15.0;
    public double se = 0.3;
    //int scaledVertexSize = 0;
    double ratioOfFixedP = 0.0003;
    int disp = 0;

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

        extractInformation(originfile);
     
        HashMap<ArrayList<Integer>, Integer> scaleIndegree_dis = new HashMap<ArrayList<Integer>, Integer>();
        HashMap<ArrayList<Integer>, Integer> scaleOutdegree_dis = new HashMap<ArrayList<Integer>, Integer>();
        scalInOutDegree(scaleIndegree_dis, scaleOutdegree_dis);
        
        NodeSynthesis nodeSyn = new NodeSynthesis();
        initCorrVetex(nodeSyn);

        HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledJointDegree_Dis = 
                nodeSyn.produceCorrel(scaleOutdegree_dis, scaleIndegree_dis, jointDegree_dis);
        
        
        //=====================Conversion============================//
        HashMap<ArrayList<Integer>, Integer> calCorrVertexSource = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> calCorrVertexTarget = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> degreeVertex = new HashMap<>();
        
        convertFormat(calCorrVertexTarget, calCorrVertexSource, degreeVertex, scaledJointDegree_Dis);
        
       // System.out.println(sumV + "SIZE");
        CorrelationFunctionScaling correlationFunctionScaling = new CorrelationFunctionScaling();
        HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorr = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> fuzzyTarget = convertFuzzy(calCorrVertexTarget, 0);
        HashMap<ArrayList<Integer>, Integer> fuzzySource = convertFuzzy(calCorrVertexSource, 1);

        settleInitialCP(correlationFunctionScaling, degreeVertex);

        scaledCorr = correlationFunctionScaling.run(this.correlation_function, fuzzyTarget, fuzzySource);
        
        EdgeLink edgelink = new EdgeLink();
        edgelink.run(degreeVertex, scaledCorr);

        finalCheck(edgelink);

        PrintWriter pw = new PrintWriter(outputDir + "scaled.txt");
        for (ArrayList<Integer> pair : edgelink.totalMathching) {
            pw.println(pair.get(1) + " " + pair.get(0));
        }
        pw.close();
        PrintWriter epw = new PrintWriter(outputDir + "/" + "exception.txt");
        epw.close();
    }

    void printRunningTime(long runTime) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(this.outputDir + "_" + s + '_' + this.disp + "_time.txt", true)));
            pw.println(runTime / 1000);
            pw.close();
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void scalInOutDegree(HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap, HashMap<ArrayList<Integer>, Integer> scaleOutdegreeMap) throws FileNotFoundException {
        Scaling indegScale = new Scaling();
        indegScale.outputDir = this.outputDir;
        indegScale.s = s;
        indegScale.se = se;
        indegScale.scaledNodeSize = this.scaledNodeSize;
        indegScale.scaledEdgeSize = scaledEdgeSize;
        HashMap<Integer, Integer> result1 = new HashMap<>();
        result1 = indegScale.scale(this.uidDegree_dis);

        Scaling outdegScale = new Scaling();

        outdegScale.s = s;
        outdegScale.outputDir = this.outputDir;
        outdegScale.se = se;
        outdegScale.scaledNodeSize = scaledNodeSize;
        outdegScale.scaledEdgeSize = scaledEdgeSize;
        HashMap<Integer, Integer> result2 = new HashMap<>();
        result2 = outdegScale.scale(this.fidDegree_dis);

        for (Entry<Integer, Integer> entry : result1.entrySet()) {
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getKey());
            scaleIndegreeMap.put(arr, entry.getValue());
        }

        for (Entry<Integer, Integer> entry : result2.entrySet()) {
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getKey());
            scaleOutdegreeMap.put(arr, entry.getValue());
        }
    }

    private void initCorrVetex(NodeSynthesis nodeSyn) {
        nodeSyn.scaledVertexSize = this.scaledNodeSize;
        System.out.println(this.scaledNodeSize + "\tvertexSize");
        nodeSyn.stime = s;
        nodeSyn.rationP = this.ratioOfFixedP;
    }

    private void settleInitialCP(CorrelationFunctionScaling edgeSynthesis, HashMap<ArrayList<Integer>, Integer> degreeVertex) {
        edgeSynthesis.scaleJoinDegree_Dis = degreeVertex;
        edgeSynthesis.original_joint_degree_dis = this.jointDegree_dis;
        edgeSynthesis.se = this.se;
        edgeSynthesis.stime = this.s;
    }

    private void convertFormat(HashMap<ArrayList<Integer>, Integer> calCorrVertexTarget, HashMap<ArrayList<Integer>, Integer> calCorrVertexSource, 
            HashMap<ArrayList<Integer>, Integer> degreeVertex, HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledJointDegree_Dis) {
        for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : scaledJointDegree_Dis.entrySet()) {
            ArrayList<Integer> indegree = new ArrayList<>();
            ArrayList<Integer> outDegree = new ArrayList<>();
            ArrayList<Integer> sameDegree = new ArrayList<>();
            for (ArrayList<Integer> temp : entry.getKey()) {
                for (int i : temp) {
                    indegree.add(i);
                    outDegree.add(i);
                    sameDegree.add(i);
                }
            }
            if (entry.getValue() > 0) {
                calCorrVertexTarget.put(indegree, entry.getValue());
                calCorrVertexSource.put(outDegree, entry.getValue());
                degreeVertex.put(sameDegree, entry.getValue());
            }
        }
    }

    private HashMap<ArrayList<Integer>, Integer> convertFuzzy(HashMap<ArrayList<Integer>, Integer> calCorrVertexTarget, int i) {
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();
        int sum = 0;
        for (Entry<ArrayList<Integer>, Integer> entry : calCorrVertexTarget.entrySet()) {
            if (entry.getValue() > 0) {
                sum += entry.getValue() * entry.getKey().get(i);
            }
            result.put(entry.getKey(), entry.getValue() * entry.getKey().get(i));
        }
        return result;
    }

    HashMap<Integer, Integer> uidDegree_dis = new HashMap<>();
    HashMap<Integer, Integer> fidDegree_dis = new HashMap<>();
    HashMap<ArrayList<ArrayList<Integer>>, Integer> jointDegree_dis = new HashMap<>();

    private void extractInformation(String originfile) {
        HashMap<String, Integer> uidCounts = new HashMap<String, Integer>();
        HashMap<String, Integer> fidCounts = new HashMap<String, Integer>();

        count_in_out_degree(originfile, uidCounts, fidCounts);

        HashMap<String, ArrayList<Integer>> idDegree = new HashMap<>();

        process_in_out_bi_frequency_counts(idDegree, uidCounts, fidCounts);
     
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

    private void count_in_out_degree(String originfile, HashMap<String, Integer> uidCounts, HashMap<String, Integer> fidCounts) {
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
                if (uidCounts.containsKey(uid)) {
                    uidCounts.put(uid, uidCounts.get(uid) + 1);
                } else {
                    uidCounts.put(uid, 1);
                }
                if (!uidCounts.containsKey(fid)) {
                    uidCounts.put(fid, 0);
                }

                if (fidCounts.containsKey(fid)) {
                    fidCounts.put(fid, fidCounts.get(fid) + 1);
                } else {
                    fidCounts.put(fid, 1);
                }
                if (!fidCounts.containsKey(uid)) {
                    fidCounts.put(uid, 0);
                }
                maxIndegree = Math.max(maxIndegree, uidCounts.get(uid));
                maxOutdegree = Math.max(maxOutdegree, fidCounts.get(fid));
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

    private void process_in_out_bi_frequency_counts(HashMap<String, ArrayList<Integer>> idDegree, HashMap<String, Integer> uidCounts, HashMap<String, Integer> fidCounts) {
        for (Entry<String, Integer> entry : uidCounts.entrySet()) {
            if (uidDegree_dis.containsKey(entry.getValue())) {
                uidDegree_dis.put(entry.getValue(), uidDegree_dis.get(entry.getValue()) + 1);
            } else {
                uidDegree_dis.put(entry.getValue(), 1);
            }

            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getValue());
            arr.add(fidCounts.get(entry.getKey()));
            ArrayList<Integer> arr1 = new ArrayList<>();
            ArrayList<Integer> arr2 = new ArrayList<>();
            arr1.add(entry.getValue());
            arr2.add(fidCounts.get(entry.getKey()));
            ArrayList<ArrayList<Integer>> arrT = new ArrayList<>();
            arrT.add(arr1);
            arrT.add(arr2);

            idDegree.put(entry.getKey(), arr);

            if (jointDegree_dis.containsKey(arrT)) {
                jointDegree_dis.put(arrT, jointDegree_dis.get(arrT) + 1);
            } else {
                jointDegree_dis.put(arrT, 1);
            }
        }

        for (Entry<String, Integer> entry : fidCounts.entrySet()) {
            if (fidDegree_dis.containsKey(entry.getValue())) {
                fidDegree_dis.put(entry.getValue(), fidDegree_dis.get(entry.getValue()) + 1);
            } else {
                fidDegree_dis.put(entry.getValue(), 1);
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
                int u = Integer.parseInt(temp[1]);
                int f = Integer.parseInt(temp[0]);
                if (u != f) {
                    ArrayList<Integer> arr1 = idDegree.get(u);
                    ArrayList<Integer> arr2 = idDegree.get(f);
                    ArrayList<ArrayList<Integer>> arrs = new ArrayList<>();
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

            this.s = 1.0 * this.scaledNodeSize / nodesize;
            this.se = 1.0 * this.scaledEdgeSize / edgesize / this.s - 1;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
}

}
