package paperalgorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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
//import static paperalgorithm.Scaling.dependAfter;
//import static paperalgorithm.EdgeLink.totalMathching;

/**
 *
 * @author workshop
 */
public class Gscaler {

    double s = 1.0 / 15.0;
    public double se = 0.3;
    public String originfile = "C://Users//Anwesha Mal//Documents//slashdot.txt";
    public String outputDir = "C://Users//Anwesha Mal//Documents";
    int scaledVertexSize = 0;
    int scaledNodeSize = 75879 * 2;
    int scaledEdgeSize = 508837 * 2;
    double ratioOfFixedP = 0.0003;
    int disp = 0;
    
public Gscaler(){}

    public void run(String[] args) throws FileNotFoundException, IOException {
        System.out.println("Execution starts here ");
        if (args.length > 1) {
            this.originfile = args[0];
            this.outputDir = args[1];
           this.scaledNodeSize = Integer.parseInt(args[2]);
           this.scaledEdgeSize = Integer.parseInt(args[3]);
           System.out.println(this.scaledEdgeSize + ":edgeSize  "+this.scaledNodeSize);
           System.out.println(outputDir);
           System.out.println(originfile);
        }
        
        File theDir = new File(outputDir);
         
        File file = new File(outputDir+"out1.txt"); //Your file
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);
        
        
        try {
            extractInformation(originfile);
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("start");
//        this.outputDir = this.outputDir + "gsa" + this.se + '_';
        HashMap<ArrayList<Integer>, Integer> scaleIndegreeMap = new HashMap<ArrayList<Integer>, Integer>();
        HashMap<ArrayList<Integer>, Integer> scaleOutdegreeMap = new HashMap<ArrayList<Integer>, Integer>();
        long start = System.currentTimeMillis();
        
        scalInOutDegree(scaleIndegreeMap, scaleOutdegreeMap);
     
        NodeSynthesis nodeSyn = new NodeSynthesis();
        initCorrVetex(nodeSyn);
        
        HashMap<ArrayList<ArrayList<Integer>>, Integer> tempCorr = new HashMap<>();
        tempCorr = nodeSyn.produceCorrel(scaleOutdegreeMap, scaleIndegreeMap, keyDegree2);
        
        //=====================Conversion============================//
        HashMap<ArrayList<Integer>, Integer> calCorrVertexSource = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> calCorrVertexTarget = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> degreeVertex = new HashMap<>();
        int sumV = 0;
        convertFormat(calCorrVertexTarget, calCorrVertexSource, degreeVertex, tempCorr);
        for (Entry<ArrayList<Integer>, Integer> entry : degreeVertex.entrySet()) {
            sumV += entry.getValue();
        }
        System.out.println(sumV + "SIZE");
        CorrelationFunctionScaling correlationFunctionScaling = new CorrelationFunctionScaling();
        HashMap<ArrayList<ArrayList<Integer>>, Integer> scaledCorr = new HashMap<>();
        HashMap<ArrayList<Integer>, Integer> fuzzyTarget = convertFuzzy(calCorrVertexTarget, 0);
        HashMap<ArrayList<Integer>, Integer> fuzzySource = convertFuzzy(calCorrVertexSource, 1);
        
        settleInitialCP(correlationFunctionScaling,degreeVertex);
      
        
        scaledCorr = correlationFunctionScaling.run(this.keyDegreePair, fuzzyTarget, fuzzySource);
        int j = 0;
   
        EdgeLink edgelink = new EdgeLink();
        edgelink.run(degreeVertex, scaledCorr);
        
      

        //      cg.
        
        /*Evaluation eva = new Evaluation();
        eva.scale = this.s;
        eva.dir = this.s + this.outputDir;
        eva.edgeExpected = this.scaledEdgeSize;
        eva.file = this.originfile;
        eva.partialResult = this.outputDir + "" + this.s + eva.partialResult;
        eva.finalGraph = this.outputDir + this.s + '_' + this.disp + "_" + eva.finalGraph;
        eva.joinDegreeF = this.outputDir + this.s + eva.joinDegreeF;
        long end = System.currentTimeMillis();
        printRunningTime(end - start);
        System.out.println(edgelink.totalMathching.size());
        System.out.print(this.scaledEdgeSize);
        eva.run2(edgelink.totalMathching);
*/
        //*/
        
        finalCheck(edgelink);
        
        PrintWriter pw = new PrintWriter(outputDir+"scaled.txt");
        for (ArrayList<Integer> pair : edgelink.totalMathching){
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
        result1 = indegScale.scale(this.uidDegree);
        
        Scaling outdegScale = new Scaling();
        
        outdegScale.s = s;
        outdegScale.outputDir = this.outputDir;
        outdegScale.se = se;
        outdegScale.scaledNodeSize = scaledNodeSize;
        outdegScale.scaledEdgeSize = scaledEdgeSize;
        HashMap<Integer, Integer> result2 = new HashMap<>();
        result2 = outdegScale.scale(this.fidDegree);
        
        
        for (Entry<Integer, Integer> entry : result1.entrySet()) {
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getKey());
            this.scaledVertexSize += entry.getValue();
            scaleIndegreeMap.put(arr, entry.getValue());
        }

        for (Entry<Integer, Integer> entry : result2.entrySet()) {
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getKey());
            scaleOutdegreeMap.put(arr, entry.getValue());
        }
    }

    private void initCorrVetex(NodeSynthesis nodeSyn) {
        nodeSyn.scaledVertexSize = this.scaledVertexSize;
        System.out.println(this.scaledVertexSize + "\tvertexSize");
        nodeSyn.stime = s;
        nodeSyn.rationP = this.ratioOfFixedP;
    }

    private void settleInitialCP(CorrelationFunctionScaling edgeSynthesis, HashMap<ArrayList<Integer>, Integer> degreeVertex) {
        edgeSynthesis.target = degreeVertex;
        edgeSynthesis.originalDegree = this.keyDegree2;
        edgeSynthesis.se = this.se;
        edgeSynthesis.stime = this.s;
    }

    private void convertFormat(HashMap<ArrayList<Integer>, Integer> calCorrVertexTarget, HashMap<ArrayList<Integer>, Integer> calCorrVertexSource, HashMap<ArrayList<Integer>, Integer> degreeVertex, HashMap<ArrayList<ArrayList<Integer>>, Integer> tempCorr) {
        for (Entry<ArrayList<ArrayList<Integer>>, Integer> entry : tempCorr.entrySet()) {
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
        //       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();
        int sum = 0;
        for (Entry<ArrayList<Integer>, Integer> entry : calCorrVertexTarget.entrySet()) {
            if (entry.getValue() > 0) {
                sum += entry.getValue() * entry.getKey().get(i);
            }
            result.put(entry.getKey(), entry.getValue() * entry.getKey().get(i));
        }
        System.out.println(sum + "sum");
        return result;
    }

    HashMap<Integer, Integer> uidDegree = new HashMap<>();
    HashMap<Integer, Integer> fidDegree = new HashMap<>();
    HashMap<ArrayList<ArrayList<Integer>>, Integer> keyDegree2 = new HashMap<>();

    private void extractInformation(String originfile) throws FileNotFoundException, IOException {
        InputStream input = new FileInputStream(originfile);
        BufferedReader bi = new BufferedReader(new InputStreamReader(input));
        HashMap<Integer, Integer> uidCounts = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> fidCounts = new HashMap<Integer, Integer>();
        String line = bi.readLine();
        int nodesize = 77360; 
        int edgesize = 0;
        int maxIndegree=0, maxOutdegree=0;
        int minMaxDegree = 0;
        
        while (line != null) {
            edgesize++;
            String[] temp = line.split("[^a-zA-Z0-9']+");
            int fid = Integer.parseInt(temp[0]);
            int uid = Integer.parseInt(temp[1]);
           // System.out.println(fid+" "+uid);
          //  if (fid != uid) {
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
                
          //  }
            line = bi.readLine();
        }
        
        minMaxDegree = Math.min(maxIndegree, maxOutdegree);
        if (minMaxDegree < 1.0*this.scaledEdgeSize/this.scaledNodeSize){
            PrintWriter pw = new PrintWriter(new File(this.outputDir+"/"+"exception.txt"));
            pw.println("Scaled Average Degree Is Greater Than The Original Maximum Degree");
            pw.close();
            System.exit(-1);
        }
        bi.close();
        nodesize=fidCounts.size();
        
        HashMap<ArrayList<Integer>, Integer> keyDegree = new HashMap<>();

        HashMap<Integer, ArrayList<Integer>> idDegree = new HashMap<>();

        for (Entry<Integer, Integer> entry : uidCounts.entrySet()) {
            if (uidDegree.containsKey(entry.getValue())) {
                uidDegree.put(entry.getValue(), uidDegree.get(entry.getValue()) + 1);
            } else {
                uidDegree.put(entry.getValue(), 1);
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

            if (keyDegree2.containsKey(arrT)) {
                keyDegree2.put(arrT, keyDegree2.get(arrT) + 1);
            } else {
                keyDegree2.put(arrT, 1);
            }
            if (keyDegree.containsKey(arr)) {
                keyDegree.put(arr, keyDegree.get(arr) + 1);
            } else {
                keyDegree.put(arr, 1);
            }
        }
      
        for (Entry<Integer, Integer> entry : fidCounts.entrySet()) {
            if (fidDegree.containsKey(entry.getValue())) {
                fidDegree.put(entry.getValue(), fidDegree.get(entry.getValue()) + 1);
            } else {
                fidDegree.put(entry.getValue(), 1);
            }
        }

        int sun = 0;
        //  pw3.close();
        input = new FileInputStream(originfile);
        BufferedReader bb = new BufferedReader(new InputStreamReader(input));
        //   scanner1.nextLine();
        
        line = bb.readLine();
        while (line!=null) {
            String temp[] = line.split("[^a-zA-Z0-9']+");
            int u = Integer.parseInt(temp[1]);
            int f = Integer.parseInt(temp[0]);
            if (u != f) {
                ArrayList<Integer> arr1 = idDegree.get(u);
                ArrayList<Integer> arr2 = idDegree.get(f);
                ArrayList<ArrayList<Integer>> arrs = new ArrayList<>();
                arrs.add(arr1);
                arrs.add(arr2);
                if (!keyDegreePair.containsKey(arrs)) {
                    keyDegreePair.put(arrs, 1);
                } else {
                    keyDegreePair.put(arrs, 1 + keyDegreePair.get(arrs));
                }
            }
             line = bb.readLine();
        }
           
           
            this.s = 1.0*this.scaledNodeSize/nodesize;
            this.se = 1.0 * this.scaledEdgeSize/edgesize/this.s-1;
          //  this.scaledEdgeSize = (int) (edgesize * this.s * (1 + this.se));
          //  this.scaledNodeSize = (int) (nodesize * this.s);
            System.out.println(this.scaledEdgeSize);
            System.out.println(this.scaledNodeSize);
    }
    
    HashMap<ArrayList<ArrayList<Integer>>, Integer> keyDegreePair = new HashMap<>();

    private void finalCheck(EdgeLink edgelink) {
        HashSet<Integer> nodes = new HashSet<>();
        
        for (ArrayList<Integer> edge: edgelink.totalMathching){
            nodes.add(edge.get(0));
            nodes.add(edge.get(1));    
        }
        
        ArrayList<Integer> shuffle  = new ArrayList<>();
        for (int k : nodes){
            shuffle.add(k);
        }
        
        while (edgelink.totalMathching.size() < this.scaledEdgeSize){
            Collections.shuffle(shuffle);
            int from  = shuffle.get(0);
            int to = shuffle.get(1);
            ArrayList<Integer> nPair = new ArrayList<>();
            nPair.add(to);
            nPair.add(from);
            if (!edgelink.totalMathching.contains(nPair)) {
                edgelink.totalMathching.add(nPair);
            }
        }
    }

}