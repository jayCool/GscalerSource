package algorithm;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class DegreeScaling {

    public HashMap<Integer, Integer> scale(HashMap<Integer, Integer> originalDegreeDis, int scaledEdgeSize, int scaledNodeSize, double s_n) throws FileNotFoundException {
        HashMap<Integer, Integer> scaleDegree = saticScale(originalDegreeDis, s_n);
        NodeAdjustment nodeAdjustment = new NodeAdjustment();
        nodeAdjustment.adjustment(scaleDegree, scaledNodeSize);
        
        EdgeAdjust edgeAdjust = new EdgeAdjust(System.currentTimeMillis());
        HashMap<Integer, Integer> smoothDegree = edgeAdjust.smoothDstat_DBScale(scaleDegree, scaledEdgeSize, scaledNodeSize);

        return smoothDegree;
    }

    private int calExpectation(double val) {
        int base = (int) val;
        if ((val - base) > Math.random()) {
            base++;
        }
        return base;
    }

    private HashMap<Integer, Integer> saticScale(HashMap<Integer, Integer> orders, double s_n) {
        HashMap<Integer, Integer> results = new HashMap<>();
        ArrayList<Integer> x = new ArrayList<>();

        for (Entry<Integer, Integer> entry : orders.entrySet()) {
            x.add(entry.getKey());
            int val = calExpectation(s_n * entry.getValue());
            results.put(entry.getKey(), val);
        }

        return results;
    }

   

  
}
