/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author workshop
 */
public class FeatureExtraction {

    int minMaxDegree = 0;
   
    public DistributionFeature extractInformation(String originfile) {
        DistributionFeature disFeature = new DistributionFeature();
        
        HashMap<String, Integer> idIndegreeCounts = new HashMap<String, Integer>();
        HashMap<String, Integer> idOutdegreeCounts = new HashMap<String, Integer>();
        
        count_in_out_degree(originfile, idIndegreeCounts, idOutdegreeCounts);
        
        HashMap<String, ArrayList<Integer>> idDegree = new HashMap<>();

        process_in_out_bi_frequency_counts(idDegree, idIndegreeCounts, idOutdegreeCounts, disFeature);

        construct_correlation_function(originfile, idDegree, disFeature);
        return disFeature;
    }

    private void count_in_out_degree(String originfile, HashMap<String, Integer> idIndegreeCounts,
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
            /*   if (minMaxDegree < 1.0 * gscaler.scaledEdgeSize / gscaler.scaledNodeSize) {
                PrintWriter pw = new PrintWriter(new File(gscaler.outputDir + "/" + "exception.txt"));
                pw.println("Scaled Average Degree Is Greater Than The Original Maximum Degree");
                pw.close();
                System.exit(-1);
            }
             */
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
            idDegree.put(entry.getKey(), arr);

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

    private void construct_correlation_function(String originfile, HashMap<String, ArrayList<Integer>> idDegree, DistributionFeature disFeature) {
        try {
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
                    if (!disFeature.correlation_function.containsKey(arrs)) {
                        disFeature.correlation_function.put(arrs, 1);
                    } else {
                        disFeature.correlation_function.put(arrs, 1 + disFeature.correlation_function.get(arrs));
                    }
                }
                edgesize++;
                line = bb.readLine();

            }
            bb.close();
            disFeature.nodeSize = idDegree.size();
            disFeature.edgeSize = edgesize;
            //    this.s_n = 1.0 * this.scaledNodeSize / nodesize;
            //    this.s_e = 1.0 * this.scaledEdgeSize / edgesize / this.s_n - 1;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gscaler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
