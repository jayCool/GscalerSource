/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zhang Jiangwei
 */
public class FeatureExtraction {
    int minMaxDegree = 0;
   
    
    /**
     * This is the method for extracting informations, indegree, outdegree, joindegree(bidegree) distributions, and correlation distribution
     * @param originfile (The file for the graph)
     * @return DistributionFeature 
     */
    public DistributionFeature extractInformation(String originfile) {
        DistributionFeature disFeature = new DistributionFeature();
        
        HashMap<String, Integer> idIndegreeCounts = new HashMap<>();
        HashMap<String, Integer> idOutdegreeCounts = new HashMap<>();
        
        countInOutDegreeForAllNodes(originfile, idIndegreeCounts, idOutdegreeCounts);
        
        HashMap<String, ArrayList<Integer>> idJointDegree = new HashMap<>();
        
        processInOutBiFrequency(idJointDegree, idIndegreeCounts, idOutdegreeCounts, disFeature);

        constructCorrelationFunction(originfile, idJointDegree, disFeature);
        return disFeature;
    }
    
    
    /**
     * 
     * @param originfile (The file for the graph)
     * @param idIndegreeCounts 
     * @param idOutdegreeCounts 
     * 
     * 
     */
    private void countInOutDegreeForAllNodes(String originfile, HashMap<String, Integer> idIndegreeCounts,
            HashMap<String, Integer> idOutdegreeCounts) {
        InputStream input = null;
        try {
            input = new FileInputStream(originfile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();
            int maxIndegree = 0, maxOutdegree = 0;
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
            this.minMaxDegree = Math.min(maxIndegree, maxOutdegree);
      
            reader.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CommandParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CommandParser.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * 
     * @param idJointDegree  
     * @param idIndegreeCounts
     * @param idOutdegreeCounts
     * @param disFeature  (results are updated in disFeature)
     */
    private void processInOutBiFrequency(HashMap<String, ArrayList<Integer>> idJointDegree,
            HashMap<String, Integer> idIndegreeCounts, HashMap<String, Integer> idOutdegreeCounts, DistributionFeature disFeature) {
        for (Map.Entry<String, Integer> entry : idIndegreeCounts.entrySet()) {
            if (disFeature.indegreeDis.containsKey(entry.getValue())) {
                disFeature.indegreeDis.put(entry.getValue(), disFeature.indegreeDis.get(entry.getValue()) + 1);
            } else {
                disFeature.indegreeDis.put(entry.getValue(), 1);
            }

            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(entry.getValue());
            arr.add(idOutdegreeCounts.get(entry.getKey()));
            idJointDegree.put(entry.getKey(), arr);

            if (disFeature.jointdegreeDis.containsKey(arr)) {
                disFeature.jointdegreeDis.put(arr, disFeature.jointdegreeDis.get(arr) + 1);
            } else {
                disFeature.jointdegreeDis.put(arr, 1);
            }
        }

        for (Map.Entry<String, Integer> entry : idOutdegreeCounts.entrySet()) {
            if (disFeature.outdegreeDis.containsKey(entry.getValue())) {
                disFeature.outdegreeDis.put(entry.getValue(), disFeature.outdegreeDis.get(entry.getValue()) + 1);
            } else {
                disFeature.outdegreeDis.put(entry.getValue(), 1);
            }
        }
    }

    
    /**
     * 
     * @param originfile
     * @param idJointDegree
     * @param disFeature  (Results are updated in disFeature)
     */
    private void constructCorrelationFunction(String originfile, HashMap<String, ArrayList<Integer>> idJointDegree, DistributionFeature disFeature) {
        try (BufferedReader bb = new BufferedReader(new InputStreamReader(new FileInputStream(originfile)))) {
            int edgesize = 0;

            String line = bb.readLine();
            while (line != null) {
                String temp[] = line.split("[^a-zA-Z0-9']+");
                String u = temp[1];
                String f = temp[0];
                if (!u.equals(f)) {
                    ArrayList<Integer> arr1 = idJointDegree.get(u);
                    ArrayList<Integer> arr2 = idJointDegree.get(f);
                    ArrayList<ArrayList<Integer>> arrs = new ArrayList<>(2);
                    arrs.add(arr1);
                    arrs.add(arr2);
                    if (!disFeature.correlationFunction.containsKey(arrs)) {
                        disFeature.correlationFunction.put(arrs, 1);
                    } else {
                        disFeature.correlationFunction.put(arrs, 1 + disFeature.correlationFunction.get(arrs));
                    }
                }
                edgesize++;
                line = bb.readLine();

            }
            bb.close();
            disFeature.nodeSize = idJointDegree.size();
            disFeature.edgeSize = edgesize;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CommandParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CommandParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
