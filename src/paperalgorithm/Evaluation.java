/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paperalgorithm;

//import edu.uci.ics.jung.graph.DirectedGraph;
//import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.MultiGraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import static org.apache.commons.collections15.CollectionUtils.intersection;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedSubgraph;

/**
 *
 * @author workshop
 */
public class Evaluation {

    /**
     * @param args the command line arguments
     */
    //   static String file = "Slashdot0811.txt";
    String file = "soc-Epinions1.txt";
    double scale = 0.22;
    long edgeExpected = 0;
    TreeMap<Double, Integer> realIn = new TreeMap<>();
    TreeMap<Double, Integer> realOut = new TreeMap<>();
    TreeMap<Double, Integer> realTwojoin = new TreeMap<>();
    TreeMap<Double, Integer> fakescc = new TreeMap<>();
    TreeMap<Double, Integer> fakewcc = new TreeMap<>();
    TreeMap<Double, Integer> fakeIn = new TreeMap<>();
    TreeMap<Double, Integer> fakeOut = new TreeMap<>();
    TreeMap<Double, Integer> fakeTwojoin = new TreeMap<>();
    TreeMap<Double, Integer> realwcc = new TreeMap<>();
    TreeMap<Double, Integer> realscc = new TreeMap<>();
    TreeMap<Double, Integer> realHop = new TreeMap<>();
    TreeMap<Double, Integer> fakeHop = new TreeMap<>();
    TreeMap<Double, Integer> fakeCoc = new TreeMap<>();
    TreeMap<Double, Integer> realCoc = new TreeMap<>();
    HashMap<Double, ArrayList<String>> fakeCoCVs = new HashMap<>();
    TreeMap<Integer, ArrayList<String>> totalDegreeVs = new TreeMap<>();
    HashMap<Integer, ArrayList<String>> indegreeVs = new HashMap<>();
    HashMap<Integer, ArrayList<String>> outdegreeVs = new HashMap<>();
    TreeMap<Integer, ArrayList<String>> intCocs = new TreeMap<>();
    // those node has been adjust, and the incoming degree is the key
    HashMap<Integer, Integer> adjustIndegreeNodes = new HashMap<>();
    HashMap<Integer, Integer> adjustOutdegreeNodes = new HashMap<>();
    ArrayList<String> noTouch = new ArrayList<>();
    int s = 0;

    private TreeMap<Double, Integer> outputscc(DirectedGraph results) throws FileNotFoundException {
        //   HashMap<Integer,Integer> countV= new HashMap<Integer,Integer>();
        StrongConnectivityInspector spi = new StrongConnectivityInspector(results);
        List<Set<String>> lists = spi.stronglyConnectedSets();

        HashMap<Double, Integer> countV = new HashMap<Double, Integer>();
        for (Set set : lists) {
            double indegree = set.size();
            if (!countV.containsKey(indegree)) {
                countV.put(indegree, 1);
            } else {
                countV.put(indegree, countV.get(indegree) + 1);
            }
        }

        TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>(countV);
        return treeMap;

    }

    private TreeMap<Double, Integer> outputwcc(DirectedGraph results) throws FileNotFoundException {
        //   HashMap<Integer,Integer> countV= new HashMap<Integer,Integer>();
        ConnectivityInspector spi = new ConnectivityInspector(results);
        List<Set<String>> lists = spi.connectedSets();

        HashMap<Double, Integer> countV = new HashMap<Double, Integer>();
        for (Set set : lists) {
            double indegree = set.size();
            if (!countV.containsKey(indegree)) {
                countV.put(indegree, 1);
            } else {
                countV.put(indegree, countV.get(indegree) + 1);
            }
        }

        TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>(countV);
        return treeMap;

    }

    private int calCoc(DirectedGraph results) {
        NeighborIndex ni = new NeighborIndex(results);
        List<String> uN;
        List<String> vN;
        HashSet<String> ss = new HashSet<>();
        for (Object de : results.edgeSet()) {
            String u = (String) results.getEdgeSource(de);
            String v = (String) results.getEdgeTarget(de);
            uN = ni.neighborListOf(u);
            vN = ni.neighborListOf(v);
            uN.retainAll(vN);
            ss.addAll(uN);
        }
        //    System.out.println(results.vertexSet().size() - ss.size());
        fakeCoCVs.put(0.0, new ArrayList<String>());
        ArrayList<String> arr = new ArrayList<>();
        for (Object s : results.vertexSet()) {
            String t = (String) s;
            if (!ss.contains(t)) {
                arr.add(t);
            }
            {
            }
        }
        fakeCoCVs.put(0.0, arr);
        fakeCoc.put(0.0, results.vertexSet().size() - ss.size());
        return 0;
    }

    private TreeMap<Double, Integer> outputCoC(DirectedGraph results) throws FileNotFoundException {
        Graph<String, String> graph = new SparseMultigraph<String, String>();
        //  Metrics.clusteringCoefficients(pw)
     /*   for (Object edge : results.edgeSet()) {
         // DefaultEdge kre =(DefaultEdge) edge;
         String startV = (String) results.getEdgeSource(edge);
         String endV = (String) results.getEdgeTarget(edge);
         graph.addVertex(endV);
         graph.addVertex(startV);
         graph.addEdge(startV + "_" + endV, startV, endV);
         }
         HashMap<Double, Integer> countV = new HashMap<Double, Integer>();
        
         Set<Map.Entry<String, Double>> entrys = Metrics.clusteringCoefficients(graph).entrySet();
         */
        HashMap<Double, Integer> countV = new HashMap<Double, Integer>();

        HashMap<String, Double> entrys = new HashMap<>();
        Set<String> ss = results.vertexSet();
        NeighborIndex ni = new NeighborIndex(results);
        for (String s : ss) {

            Set<String> vs = ni.neighborsOf(s);
            DirectedGraph sub = new DirectedSubgraph(results, vs, results.edgeSet());
            int size1 = (sub.vertexSet().size()) * (sub.vertexSet().size() - 1);
            if (size1 == 0) {
                entrys.put(s, 0.0);
            } else {
                entrys.put(s, 1.0 * sub.edgeSet().size() / size1);
            }

        }
        fakeCoCVs = new HashMap<>();
        for (Entry<String, Double> entry : entrys.entrySet()) {
            if (!fakeCoCVs.containsKey(entry.getValue())) {
                fakeCoCVs.put(entry.getValue(), new ArrayList<String>());
            }
            fakeCoCVs.get(entry.getValue()).add(entry.getKey());
        }

        for (Entry<String, Double> entry : entrys.entrySet()) {
            String v = entry.getKey();
            int size = results.edgesOf(v).size();
            int value = (int) (1 * size * (size - 1) * entry.getValue() + 0.4);
            if (!intCocs.containsKey(value)) {
                intCocs.put(value, new ArrayList<String>());
            }
            intCocs.get(value).add(v);

            int insize = results.inDegreeOf(v);
            int outsize = results.outDegreeOf(v);
            if (!indegreeVs.containsKey(insize)) {
                indegreeVs.put(insize, new ArrayList<String>());
            }
            indegreeVs.get(insize).add(v);

            if (!outdegreeVs.containsKey(outsize)) {
                outdegreeVs.put(outsize, new ArrayList<String>());
            }
            outdegreeVs.get(outsize).add(v);
        }

        for (Map.Entry<String, Double> entry : entrys.entrySet()) {
            double indegree = entry.getValue();
            if (!countV.containsKey(indegree)) {
                countV.put(indegree, 1);
            } else {
                countV.put(indegree, countV.get(indegree) + 1);
            }
        }

// List<Set<String>> lists= spi.connectedSets();
        TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>(countV);
        //   treeMap.

        return treeMap;

    }

    private void readInVertex(String file, DirectedGraph dg) throws FileNotFoundException {
        InputStream input = new FileInputStream(file);
        Scanner scanner = new Scanner(input);
        //   scanner.nextLine();
        HashMap<ArrayList<String>, Integer> te = new HashMap<>();
        int max = 0;
        while (scanner.hasNext()) {
            String[] temp = scanner.nextLine().split("\\s+");
            String from = temp[0];
            if (max < Integer.parseInt(from)) {
                max = Integer.parseInt(from);
            }
            String too = temp[1];
            if (max < Integer.parseInt(too)) {
                max = Integer.parseInt(too);
            }
            dg.addVertex(from);
            dg.addVertex(too);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(from);
            arr.add(too);
            if (!te.containsKey(arr) && !from.equals(too)) {
                dg.addEdge(from, too);
                te.put(arr, 0);
            }
        }
        System.out.println(dg.edgeSet().size());
        scanner.close();
        //      System.out.println(max);
    }

    private TreeMap<Double, Integer> outputInDegree(DirectedGraph results) throws FileNotFoundException {
        //   PrintWriter pw = new PrintWriter("indegree.txt");
        HashMap<Double, Integer> countV = new HashMap<Double, Integer>();
        for (Object v : results.vertexSet()) {
            double indegree = results.inDegreeOf(v);
            if (!countV.containsKey(indegree)) {
                countV.put(indegree, 1);
            } else {
                countV.put(indegree, countV.get(indegree) + 1);
            }
        }
        //countV.keySet()
        // Collections.sort(null, pw);
        TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>(countV);
        return treeMap;
    }

    private TreeMap<Double, Integer> outputOutDegree(DirectedGraph results) throws FileNotFoundException {
        HashMap<Double, Integer> countV = new HashMap<Double, Integer>();
        for (Object v : results.vertexSet()) {
            double indegree = results.outDegreeOf(v);
            if (!countV.containsKey(indegree)) {
                countV.put(indegree, 1);
            } else {
                countV.put(indegree, countV.get(indegree) + 1);
            }
        }
        //countV.keySet()
        // Collections.sort(null, pw);
        TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>(countV);
        return treeMap;
    }

    private HashMap<ArrayList<Integer>, Integer> outputTwoJoins(DirectedGraph<String, DefaultEdge> dg) {
        HashMap<ArrayList<Integer>, Integer> result = new HashMap<>();

        for (String v : dg.vertexSet()) {
            //    Pair p = new Pair(0.0 + dg.inDegreeOf(v), 0.0 + dg.outDegreeOf(v));
            //      double index = convert2Dto1D(p);
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(dg.inDegreeOf(v));
            arr.add(dg.outDegreeOf(v));
            if (result.containsKey(arr)) {
                result.put(arr, 1 + result.get(arr));
            } else {
                result.put(arr, 1);
            }
        }
        return result;
    }

    private double getDStatistics(TreeMap<Double, Integer> sample, TreeMap<Double, Integer> real) {
        double results = 0;
        TreeMap<Double, Double> pmf1 = new TreeMap<>();
        TreeMap<Double, Double> pmf2 = new TreeMap<>();

        //   sample.remove(0.0);
        //  real.remove(0.0);
        int sum1 = 0;
        for (Map.Entry<Double, Integer> entry : sample.entrySet()) {
            sum1 += entry.getValue();
        }
        int sum2 = 0;
        for (Map.Entry<Double, Integer> entry : real.entrySet()) {
            sum2 += entry.getValue();
        }

        for (Map.Entry<Double, Integer> entry : real.entrySet()) {
            //  Pair pair = new Pair(entry.getKey(),1.0*entry.getValue()/sum2);
            pmf2.put(entry.getKey(), 1.0 * entry.getValue() / sum2);
        }

        for (Map.Entry<Double, Integer> entry : sample.entrySet()) {
            //      Pair pair = new Pair(entry.getKey(),1.0*entry.getValue()/sum1);
            pmf1.put(entry.getKey(), 1.0 * entry.getValue() / sum1);
        }
//pmf1.
        for (Map.Entry<Double, Double> entry : pmf1.entrySet()) {
            if (!pmf2.containsKey(entry.getKey())) {
                pmf2.put(entry.getKey(), 0.0);
            }
        }

        for (Map.Entry<Double, Double> entry : pmf2.entrySet()) {
            // System.out.println(entry.getKey()+"ssss");
            if (!pmf1.containsKey(entry.getKey())) {
                pmf1.put(entry.getKey(), 0.0);
            }
        }

        ArrayList<Double> cmf1 = new ArrayList<>();
        ArrayList<Double> cmf2 = new ArrayList<>();
        int i = 0;
        for (Map.Entry<Double, Double> entry : pmf2.entrySet()) {
            if (i != 0) {
                cmf1.add(pmf1.get(entry.getKey()) + cmf1.get(i - 1));
                cmf2.add(entry.getValue() + cmf2.get(i - 1));

            } else {
                cmf1.add(pmf1.get(entry.getKey()));
                cmf2.add(entry.getValue());

            }
            i++;
        }
        double max = 0;
        // System.out.println(sample);
        //   System.out.println(cmf1);
        // System.out.println(cmf1.get(1)-cmf2.get(1)+ " === "+pmf2.firstKey());
        for (int t = 0; t < cmf1.size(); t++) {
            if (max < Math.abs(cmf1.get(t) - cmf2.get(t))) {
                max = Math.abs(cmf1.get(t) - cmf2.get(t));
            }
        }
        return max;
    }

    private void settleCoc(DirectedGraph<String, DefaultEdge> dg2, TreeMap<Double, Integer> realCoc, TreeMap<Double, Integer> fakeCoc, DirectedGraph<String, DefaultEdge> dg) {
        int real0Count = realCoc.get(0.0) * dg2.vertexSet().size() / dg.vertexSet().size();
        int fake0Count = fakeCoc.get(0.0);
        if (real0Count < fake0Count) {
            decrease(dg2, realCoc, fakeCoc, dg);
        } else {
            increase(dg2, realCoc, fakeCoc, dg);
        }
    }

    private void decrease(DirectedGraph<String, DefaultEdge> dg2, TreeMap<Double, Integer> realCoc, TreeMap<Double, Integer> fakeCoc, DirectedGraph<String, DefaultEdge> dg) {
        int real0Count = realCoc.get(0.0) * dg2.vertexSet().size() / dg.vertexSet().size();
        int fake0Count = fakeCoc.get(0.0);
        int rest = fake0Count - real0Count;
        //      System.out.println("target:=" + real0Count + "  ==" + rest);
//add aedge to those zeros, 

        //vs is those nodes with 0;
        Set<String> settled = new HashSet<String>();
        ArrayList<String> vs = (ArrayList<String>) fakeCoCVs.get(0.0);
        int i = 0;
        for (String v : vs) {
            if (settled.size() < (fake0Count - real0Count) && !settled.contains(v)) {

                Set<DefaultEdge> edgeSets = dg2.incomingEdgesOf(v);

                Set<DefaultEdge> edgeSets2 = dg2.outgoingEdgesOf(v);
                ArrayList<String> targetVs = new ArrayList<String>();
                int count = 0;
                for (DefaultEdge de : edgeSets) {
                    if (vs.contains(dg2.getEdgeSource(de)) && !settled.contains(dg2.getEdgeSource(de))) {
                        count++;
                        targetVs.add(dg2.getEdgeSource(de));
                        if (count == 2) {
                            break;
                        }
                    }
                }

                for (DefaultEdge de : edgeSets2) {
                    if (count == 2) {
                        break;
                    }

                    if (vs.contains(dg2.getEdgeTarget(de)) && !settled.contains(dg2.getEdgeSource(de)) && !targetVs.contains(dg2.getEdgeTarget(de))) {
                        count++;
                        targetVs.add(dg2.getEdgeTarget(de));
                    }
                }

                if (count == 2) {
                    String n1 = targetVs.get(0);
                    String n2 = targetVs.get(1);
                    if (n1 != n2) {
                        NeighborIndex ngb = new NeighborIndex(dg2);
                        Set<String> s1 = ngb.neighborsOf(n1);
                        Set<String> s2 = ngb.neighborsOf(n2);
                        Object[] inters = intersection(s1, s2).toArray();
                        int cout = 0;
                        for (Object kdi : inters) {
                            String ki = (String) kdi;
                            if (!settled.contains(ki) && vs.contains(ki)) {
                                cout++;
                                settled.add(ki);
                            }
                        }
                        settled.add(n1);
                        settled.add(n2);

                        rest = rest - 2 - cout;
                        dg2.addEdge(targetVs.get(0), targetVs.get(1));
                        int outDegree = dg2.outDegreeOf(targetVs.get(0));
                        int inDegree = dg2.inDegreeOf(targetVs.get(1));

                        if (!adjustOutdegreeNodes.containsKey(outDegree)) {
                            adjustOutdegreeNodes.put(outDegree, 0);
                        }
                        adjustOutdegreeNodes.put(outDegree, adjustOutdegreeNodes.get(outDegree) + 1);
                        noTouch.add(targetVs.get(0));
                        noTouch.add(targetVs.get(1));

                        if (!adjustIndegreeNodes.containsKey(inDegree)) {
                            adjustIndegreeNodes.put(inDegree, 0);
                        }
                        adjustIndegreeNodes.put(inDegree, adjustIndegreeNodes.get(inDegree) + 1);

                    }

                }

                if (count == 1 && dg2.edgesOf(v).size() > 1) {
                    Set<DefaultEdge> outSet = dg2.outgoingEdgesOf(v);
                    Set<DefaultEdge> inSet = dg2.incomingEdgesOf(v);

                    ArrayList<String> vertes = new ArrayList<>();
                    for (DefaultEdge de : outSet) {
                        vertes.add(dg2.getEdgeTarget(de));
                    }
                    for (DefaultEdge de : inSet) {
                        String tem = dg2.getEdgeSource(de);
                        if (!vertes.contains(tem)) {
                            vertes.add(tem);
                        }
                    }
                    if (vertes.size() >= 2) {
                        String v2 = vertes.get(1);
                        String v1 = vertes.get(0);
                        if (v1.equals(targetVs.get(0))) {
                            v1 = v2;
                        }
                        if (v1 != targetVs.get(0)) {
                            NeighborIndex ngb = new NeighborIndex(dg2);
                            Set<String> s1 = ngb.neighborsOf(v1);
                            Set<String> s2 = ngb.neighborsOf(targetVs.get(0));
                            Object[] inters = intersection(s1, s2).toArray();
                            int cout = 0;
                            for (Object kdi : inters) {
                                String ki = (String) kdi;
                                if (!settled.contains(ki) && vs.contains(ki)) {
                                    cout++;
                                    settled.add(ki);
                                }
                            }
                            settled.add(targetVs.get(0));
                            //  settled.add(v1);
                            rest = rest - 1 - cout;
                            dg2.addEdge(v1, targetVs.get(0));
                            int outDegree = dg2.outDegreeOf(v1);
                            int inDegree = dg2.inDegreeOf(targetVs.get(0));

                            if (!adjustOutdegreeNodes.containsKey(outDegree)) {
                                adjustOutdegreeNodes.put(outDegree, 0);
                            }
                            adjustOutdegreeNodes.put(outDegree, adjustOutdegreeNodes.get(outDegree) + 1);
                            noTouch.add(v1);
                            noTouch.add(targetVs.get(0));

                            if (!adjustIndegreeNodes.containsKey(inDegree)) {
                                adjustIndegreeNodes.put(inDegree, 0);
                            }
                            adjustIndegreeNodes.put(inDegree, adjustIndegreeNodes.get(inDegree) + 1);

                            //rest = rest - 2;
                        }

                    }
                }
                if (count == 0 && dg2.edgesOf(v).size() > 1) {
                    Set<DefaultEdge> outSet = dg2.outgoingEdgesOf(v);
                    Set<DefaultEdge> inSet = dg2.incomingEdgesOf(v);

                    ArrayList<String> vertes = new ArrayList<>();
                    for (DefaultEdge de : outSet) {
                        vertes.add(dg2.getEdgeTarget(de));
                    }
                    for (DefaultEdge de : inSet) {
                        String tem = dg2.getEdgeSource(de);
                        if (!vertes.contains(tem)) {
                            vertes.add(tem);
                        }
                    }
                    if (vertes.size() >= 2) {
                        String v2 = vertes.get(1);
                        String v1 = vertes.get(0);
                        if (v1 != v2) {
                            NeighborIndex ngb = new NeighborIndex(dg2);

                            Set<String> s1 = ngb.neighborsOf(v1);
                            Set<String> s2 = ngb.neighborsOf(v2);
                            Object[] inters = intersection(s1, s2).toArray();
                            int cout = 0;
                            for (Object kdi : inters) {
                                String ki = (String) kdi;
                                if (!settled.contains(ki) && vs.contains(ki)) {
                                    cout++;
                                    settled.add(ki);
                                }
                            }
                            rest = rest - cout;

                            dg2.addEdge(v1, v2);
                            int outDegree = dg2.outDegreeOf(v1);
                            int inDegree = dg2.inDegreeOf(v2);

                            if (!adjustOutdegreeNodes.containsKey(outDegree)) {
                                adjustOutdegreeNodes.put(outDegree, 0);
                            }
                            adjustOutdegreeNodes.put(outDegree, adjustOutdegreeNodes.get(outDegree) + 1);
                            noTouch.add(v1);
                            noTouch.add(v2);

                            if (!adjustIndegreeNodes.containsKey(inDegree)) {
                                adjustIndegreeNodes.put(inDegree, 0);
                            }
                            adjustIndegreeNodes.put(inDegree, adjustIndegreeNodes.get(inDegree) + 1);

                            //   rest = rest - 1;
                        }
                    }
                }
            }
        }
        //      System.out.println("=====");
        //    System.out.println(rest);
        noTouch.clear();
        for (String t : settled) {
            noTouch.add(t);
        }

        System.out.println(settled.size());
    }

    private void increase(DirectedGraph<String, DefaultEdge> dg2, TreeMap<Double, Integer> realCoc, TreeMap<Double, Integer> fakeCoc, DirectedGraph<String, DefaultEdge> dg) {
        int real0Count = realCoc.get(0.0) * dg2.vertexSet().size() / dg.vertexSet().size();
        int fake0Count = fakeCoc.get(0.0);
        int diff = real0Count - fake0Count;
        for (Entry<Integer, ArrayList<String>> entry : intCocs.entrySet()) {
            if (entry.getKey() > 0) {
                for (String v : entry.getValue()) {
                    if (diff < 0) {
                        break;
                    }
                    int pair = entry.getKey();
                    Set<DefaultEdge> edgeSet = dg2.incomingEdgesOf(v);
                    Set<DefaultEdge> edgeSet1 = dg2.outgoingEdgesOf(v);
                    HashSet<String> vSet = new HashSet<String>();
                    for (DefaultEdge de : edgeSet) {
                        vSet.add(dg2.getEdgeSource(de));
                    }
                    for (DefaultEdge de : edgeSet1) {
                        vSet.add(dg2.getEdgeSource(de));
                    }
                    Iterator<String> iter = vSet.iterator();
                    while (iter.hasNext()) {
                        String vs = iter.next();
                        Set<DefaultEdge> ds = dg2.incomingEdgesOf(vs);
                        Set<DefaultEdge> ds1 = dg2.outgoingEdgesOf(vs);
                        ArrayList<String> vSet1 = new ArrayList<String>();
                        for (DefaultEdge de : ds) {
                            vSet1.add(dg2.getEdgeSource(de));
                        }
                        ArrayList<String> vSet2 = new ArrayList<String>();

                        for (DefaultEdge de : ds1) {
                            vSet2.add(dg2.getEdgeTarget(de));
                        }

                        for (String vv : vSet1) {
                            if (vSet.contains(vv)) {
                                int startDegree = dg2.outDegreeOf(vv);
                                int endDegree = dg2.inDegreeOf(vs);
                                dg2.removeEdge(vv, vs);
                                if (!adjustOutdegreeNodes.containsKey(startDegree)) {
                                    adjustOutdegreeNodes.put(startDegree, 0);
                                }
                                adjustOutdegreeNodes.put(startDegree, adjustOutdegreeNodes.get(startDegree) - 1);
                                noTouch.add(vv);
                                noTouch.add(vs);

                                if (!adjustIndegreeNodes.containsKey(endDegree)) {
                                    adjustIndegreeNodes.put(endDegree, 0);
                                }
                                adjustIndegreeNodes.put(endDegree, adjustIndegreeNodes.get(endDegree) - 1);

                                diff--;
                                //   diff--;
                            }
                        }

                        for (String vv : vSet2) {
                            if (vSet.contains(vv)) {
                                int startDegree = dg2.inDegreeOf(vs);
                                int endDegree = dg2.outDegreeOf(vv);
                                if (!adjustOutdegreeNodes.containsKey(startDegree)) {
                                    adjustOutdegreeNodes.put(startDegree, 0);
                                }
                                adjustOutdegreeNodes.put(startDegree, adjustOutdegreeNodes.get(startDegree) - 1);
                                noTouch.add(vv);
                                noTouch.add(vs);

                                if (!adjustIndegreeNodes.containsKey(endDegree)) {
                                    adjustIndegreeNodes.put(endDegree, 0);
                                }
                                adjustIndegreeNodes.put(endDegree, adjustIndegreeNodes.get(endDegree) - 1);

                                dg2.removeEdge(vs, vv);
                                diff--;
                            }
                        }
                    }
                }
            }
        }
    }

    private void printFile(TreeMap<Double, Integer> realCoc) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter("0.4fakeHopValue.txt");
        for (Entry<Double, Integer> entry : realCoc.entrySet()) {
            pw.println(entry.getKey() + " " + entry.getValue());
        }
        pw.close();

    }

    private TreeMap<Double, Integer> readIn() throws FileNotFoundException {
        TreeMap<Double, Integer> result = new TreeMap<>();
        InputStream input = new FileInputStream("realCocValue.txt");
        Scanner scanner = new Scanner(input);
        while (scanner.hasNext()) {
            String[] temp = scanner.nextLine().split("\\s+");
            Double key = Double.parseDouble(temp[0]);
            Integer value = Integer.parseInt(temp[1]);
            result.put(key, value);
        }
        scanner.close();
        return result;

    }

    private void settleCocTail(DirectedGraph<String, DefaultEdge> dg2, TreeMap<Double, Integer> realCoc, TreeMap<Double, Integer> fakeCoc, DirectedGraph<String, DefaultEdge> dg) {
        int real1Count = realCoc.get(1.0) * dg2.vertexSet().size() / dg.vertexSet().size();
        int fake1Count = fakeCoc.get(1.0);
        if (real1Count < fake1Count) {
            decreaseTail(dg2, realCoc, fakeCoc, dg);
        } else {
            increaseTail(dg2, realCoc, fakeCoc, dg);
        }
    }

    private void decreaseTail(DirectedGraph<String, DefaultEdge> dg2, TreeMap<Double, Integer> realCoc, TreeMap<Double, Integer> fakeCoc, DirectedGraph<String, DefaultEdge> dg) {
        int real0Count = realCoc.get(1.0) * dg2.vertexSet().size() / dg.vertexSet().size();
        int fake0Count = fakeCoc.get(1.0);
        int diff = real0Count - fake0Count;
        ArrayList<String> settled = new ArrayList<>();
        ArrayList<String> vS = new ArrayList<>(0);
        vS = fakeCoCVs.get(1.0);
        for (String s : settled) {
            NeighborIndex ni = new NeighborIndex(dg2);
            List<String> list = ni.neighborListOf(s);
            String v1 = list.get(0);
            String v2 = list.get(1);
            int outDegree = dg2.outDegreeOf(v1);
            int inDegree = dg2.inDegreeOf(v2);

            if (!adjustOutdegreeNodes.containsKey(outDegree)) {
                adjustOutdegreeNodes.put(outDegree, 0);
            }
            adjustOutdegreeNodes.put(outDegree, adjustOutdegreeNodes.get(outDegree) - 1);
            noTouch.add(v1);
            noTouch.add(v2);

            if (!adjustIndegreeNodes.containsKey(inDegree)) {
                adjustIndegreeNodes.put(inDegree, 0);
            }
            adjustIndegreeNodes.put(inDegree, adjustIndegreeNodes.get(inDegree) - 1);

        }

    }
    String[] args;

    private void increaseTail(DirectedGraph<String, DefaultEdge> dg2, TreeMap<Double, Integer> realCoc, TreeMap<Double, Integer> fakeCoc, DirectedGraph<String, DefaultEdge> dg) {
        int real0Count = realCoc.get(1.0) * dg2.vertexSet().size() / dg.vertexSet().size();
        int fake0Count = fakeCoc.get(1.0);
        int diff = real0Count - fake0Count;
        Set<String> use = new HashSet<>();
        ArrayList<String> ttemp = fakeCoCVs.get(0.0);
        for (String s : ttemp) {
            if (!noTouch.contains(s)) {
                use.add(s);
            }
        }
        ArrayList<String> settled = new ArrayList<>();
        for (Entry<Integer, ArrayList<String>> entry : totalDegreeVs.entrySet()) {
            if (entry.getKey() > 0) {
                for (String s : entry.getValue()) {
                    if (diff > 0 && !use.contains(s)) {
                        //    System.out.println(diff);
                        NeighborIndex ni = new NeighborIndex(dg2);
                        Set<String> sNeig = ni.neighborsOf(s);
                        DirectedGraph sub = new DirectedSubgraph(dg2, sNeig, dg2.edgeSet());
                        //make the complete subgraph
                        boolean added = false;
                        boolean okF = false;
                        for (String t : sNeig) {
                            if (use.contains(t)) {
                                okF = true;
                            }
                        }
                        if (!okF) {
                            for (String u : sNeig) {

                                for (String v : sNeig) {
                                    if (!u.equals(v) && !sub.containsEdge(u, v)) {
                                        dg2.addEdge(u, v);
                                        int outDegree = dg2.outDegreeOf(u);
                                        int inDegree = dg2.inDegreeOf(v);

                                        if (!adjustOutdegreeNodes.containsKey(outDegree)) {
                                            adjustOutdegreeNodes.put(outDegree, 0);
                                        }
                                        adjustOutdegreeNodes.put(outDegree, adjustOutdegreeNodes.get(outDegree) + 1);
                                        noTouch.add(u);
                                        noTouch.add(v);

                                        if (!adjustIndegreeNodes.containsKey(inDegree)) {
                                            adjustIndegreeNodes.put(inDegree, 0);
                                        }
                                        adjustIndegreeNodes.put(inDegree, adjustIndegreeNodes.get(inDegree) + 1);

                                        added = true;
                                    }
                                }
                            }
                        }
                        if (added) {
                            diff--;
                        }
                        //   }
                    }
                }
            }
        }
        System.out.println(diff);
    }

    private void produceDegree(DirectedGraph<String, DefaultEdge> dg2) {

        for (String s : dg2.vertexSet()) {
            int size = dg2.edgesOf(s).size();
            if (!totalDegreeVs.containsKey(size)) {
                totalDegreeVs.put(size, new ArrayList<String>());
            }
            totalDegreeVs.get(size).add(s);
        }
    }

    private void settleNodes(DirectedGraph<String, DefaultEdge> dg2) {
        HashMap<ArrayList<Integer>, ArrayList<ArrayList<String>>> mapping = new HashMap<>();
        HashMap<Integer, HashSet<String>> indegreeIDs = new HashMap<>();
        HashMap<Integer, HashSet<String>> outdegreeIDs = new HashMap<>();
        for (DefaultEdge de : dg2.edgeSet()) {
            String v1 = dg2.getEdgeSource(de);
            String v2 = dg2.getEdgeTarget(de);
            int outDegree = dg2.outDegreeOf(v1);
            int inDegree = dg2.inDegreeOf(v2);
            if (!indegreeIDs.containsKey(inDegree)) {
                indegreeIDs.put(inDegree, new HashSet<String>());
            }
            indegreeIDs.get(inDegree).add(v1);

            if (!outdegreeIDs.containsKey(outDegree)) {
                outdegreeIDs.put(outDegree, new HashSet<String>());
            }
            outdegreeIDs.get(outDegree).add(v2);
            ArrayList<Integer> arr = new ArrayList<>();
            arr.add(outDegree);
            arr.add(inDegree);
            ArrayList<String> vArr = new ArrayList<>();
            vArr.add(v1);
            vArr.add(v2);
            if (!mapping.containsKey(arr)) {
                mapping.put(arr, new ArrayList<ArrayList<String>>());
            }
            mapping.get(arr).add(vArr);

        }
        Set<Integer> set1 = adjustOutdegreeNodes.keySet();
        Set<Integer> set2 = adjustIndegreeNodes.keySet();
        for (int i : set1) {
            for (int j : set2) {
                ArrayList<Integer> p = new ArrayList<>();
                p.add(i);
                p.add(j);
                int min = Math.min(adjustOutdegreeNodes.get(i), adjustIndegreeNodes.get(j));
                if (adjustOutdegreeNodes.get(i) > 0 && adjustIndegreeNodes.get(j) > 0) {
                    if (mapping.containsKey(p)) {
                        ArrayList<ArrayList<String>> values = mapping.get(p);
                        for (ArrayList<String> pai : values) {
                            //     for (int m=0;m<pai.size();m++)
                            //       for(int n=0;n<pai.)
                            String va = pai.get(0);
                            String vb = pai.get(1);
                            if (!noTouch.contains(va) && !noTouch.contains(vb) && min > 0) {
                                dg2.removeEdge(va, vb);

                                noTouch.add(vb);
                                noTouch.add(va);
                                adjustOutdegreeNodes.put(i, adjustOutdegreeNodes.get(i) - 1);
                                adjustIndegreeNodes.put(j, adjustIndegreeNodes.get(j) - 1);
                                min--;
                            }

                        }

                    }
                }
            }
        }
        /* 
         NeighborIndex ni = new NeighborIndex(dg2);
         for (int i : set1) {
         for (int j : set2) {
         ArrayList<String> vs = new ArrayList<>(outdegreeIDs.get(i));
         ArrayList<String> us = new ArrayList<>(indegreeIDs.get(j));
         int min = Math.min(adjustOutdegreeNodes.get(i), adjustIndegreeNodes.get(j));
         for (String v:vs)
         for(String u: us)        
         if (!noTouch.contains(u) && !noTouch.contains(v)){
         Set<DefaultEdge> des= dg2.outgoingEdgesOf(v);
         for (DefaultEdge de:des){
         String t=dg2.getEdgeTarget(de);
         if (!noTouch.contains(u)){
                                   
                     
         if (adjustOutdegreeNodes.get(i) > 0 && adjustIndegreeNodes.get(j) > 0) {
                    
         }}}}
         }
         }*/
    }

    private TreeMap<Double, Integer> outputHopPlot(DirectedGraph<String, DefaultEdge> results) {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //  Hypergraph result = new Hypergraph();
        TreeMap<Double, Integer> result = new TreeMap<Double, Integer>();
        Graph<String, String> dg = new SparseMultigraph<String, String>();
        for (DefaultEdge edge : results.edgeSet()) {
            // DefaultEdge kre =(DefaultEdge) edge;
            String startV = (String) results.getEdgeSource(edge);
            String endV = (String) results.getEdgeTarget(edge);
            dg.addVertex(endV);
            dg.addVertex(startV);
            dg.addEdge(startV + "_" + endV, startV, endV);
        }
        DijkstraDistance djs = new DijkstraDistance(dg);
        //  FloydWarshallShortestPaths path = new FloydWarshallShortestPaths(dg);
        for (String s : dg.getVertices()) {
            System.out.println(s);
            //  List<GraphPath<String,DefaultEdge>> ps= path.getShortestPaths(s);
            Map<String, Double> maps = djs.getDistanceMap(s);

            for (Entry<String, Double> en : maps.entrySet()) {
                double size = 0.0 + en.getValue();
                if (result.containsKey(size)) {
                    result.put(size, result.get(size) + 1);
                } else {
                    result.put(size, 1);
                }
            }
        }
        return result;
    }

    private void outputgraph(DirectedGraph<String, DefaultEdge> dg2) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(finalGraph);
        for (DefaultEdge de : dg2.edgeSet()) {
            pw.println(dg2.getEdgeSource(de) + " " + dg2.getEdgeTarget(de));
        }
        pw.close();

    }
    HashMap<Integer, HashSet<String>> totalMaps = new HashMap<>();

    public void run2old(HashSet<ArrayList<Integer>> totalMathching) throws FileNotFoundException {
        DirectedGraph<String, DefaultEdge> dg = new DefaultDirectedGraph(DefaultEdge.class);
        readInVertex(file, dg);
        DirectedGraph<String, DefaultEdge> dg2 = new DefaultDirectedGraph(DefaultEdge.class);
        convertGraph(totalMathching, dg2);
        //   adjustStars(dg2, dg);
        //  readInVertex(files2, dg2);
        realIn = outputInDegree(dg);
        //          System.out.println(realIn.get(0.0));
        realOut = outputOutDegree(dg);
        //    realTwojoin = outputTwoJoin(dg);
        //realscc = outputscc(dg);
        // realwcc = outputwcc(dg);
        fakeIn = outputInDegree(dg2);
        //          System.out.println(realIn.get(0.0));
        fakeOut = outputOutDegree(dg2);
        //     fakeTwojoin = outputTwoJoin(dg2);
        //System.out.println(getDStatistics(fakeIn, realIn));
        //System.out.println(getDStatistics(fakeOut, realOut));
        //    System.out.println(getDStatistics(fakeTwojoin, realTwojoin));

        //   System.out.println(realIn);
        //   System.out.println(fakeOut);
//        realCoc = readIn();
        //  fakeCoc = outputCoC(dg2);//
        //    calCoc(dg2);
        //   produceDegree(dg2);
        //   settleCoc(dg2, realCoc, fakeCoc, dg);
        //   fakeCoc = outputCoC(dg2);
        //    settleNodes(dg2);
        int idd = 0;
        int count = 0;
        //    HashSet<ArrayList<String>, Integer> deleted = new HashSet<>();
        HashMap<String, Integer> deletedIn = new HashMap<>();
        HashMap<String, Integer> deletedOut = new HashMap<>();
        HashMap<ArrayList<String>, Integer> threeHop = new HashMap<>();
        HashMap<ArrayList<String>, Double> fourHop = new HashMap<>();

        //System.out.println(idd);
        System.out.println("Settled");
   //   produceLayer(dg2);
        //  produceLayer(dg);

        //   outputgraph(dg2);
        // printFile(realCoc);
        //   printFile(fakeCoc);
        //fakeIn = outputInDegree(dg2);
        //          System.out.println(realIn.get(0.0));
        //fakeOut = outputOutDegree(dg2);
        //HashMap<ArrayList<Integer>, Integer> fakeTwojoins = outputTwoJoins(dg2);
        //fakescc = outputscc(dg2);
        //  fakewcc = outputwcc(dg2);
        //   fakeCoc = outputCoC(dg2);
        System.out.println("calculating");
        ///   fakeHop = outputHopPlot(dg2);
        System.out.println("Vertex size:    " + dg2.vertexSet().size());
        System.out.println("Edge size:    " + dg2.edgeSet().size());

        System.out.println("Indegree: " + getDStatistics(fakeIn, realIn));
        System.out.println("Outdegree: " + getDStatistics(fakeOut, realOut));
        System.out.println("Degree: " + this.getDStatistics(this.outputDegree(dg), this.outputDegree(dg2)));
        //  System.out.println("Joindegree: " + getDStatistics(fakeTwojoin, realTwojoin));
        //    System.out.println("scc: " + getDStatistics(fakescc, realscc));
        // System.out.println(getDStatistics(fakewcc, realwcc));
        //       System.out.println("coc:" + getDStatistics(fakeCoc, realCoc));

        PrintWriter pw = new PrintWriter(partialResult);
        pw.println("Vertex size:    " + dg2.vertexSet().size());
        pw.println("Edge size:    " + dg2.edgeSet().size());

        pw.println("Indegree: " + getDStatistics(fakeIn, realIn));
        pw.println("Outdegree: " + getDStatistics(fakeOut, realOut));
        //  System.out.println("Joindegree: " + getDStatistics(fakeTwojoin, realTwojoin));
        //    pw.println("scc: " + getDStatistics(fakescc, realscc));
        // System.out.println(getDStatistics(fakewcc, realwcc));
        //    pw.println("coc:" + getDStatistics(fakeCoc, realCoc));
        pw.close();
       // outputgraph(dg2);
        // outputTwoJoins(fakeTwojoins);

        //   System.out.println(getDStatistics(fakeHop,realHop));
        //   printFile(fakeHop);
   /*     System.out.println(fakeCoc);
         System.out.println(realCoc);
         System.out.println(1.0 * fakeCoc.pollFirstEntry().getValue() / dg2.vertexSet().size());
         System.out.println(1.0 * realCoc.pollFirstEntry().getValue() / dg.vertexSet().size());
         System.out.println(1.0 * fakeCoc.get(1.0) / dg2.vertexSet().size() + "    " + fakeCoc.pollLastEntry());
         System.out.println(1.0 * realCoc.get(1.0) / dg.vertexSet().size() + "   " + realCoc.pollLastEntry());
        
         //    System.out.println(fakeCoCVs.get(1.5));
         //  System.out.println(dg2.edgesOf("10232")); */    }

    public void run2(HashSet<ArrayList<Integer>> totalMathching) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(finalGraph);
        for (ArrayList<Integer> arr : totalMathching) {
            pw.println(arr.get(1) + " " + arr.get(0));
        }
        pw.close();
        /*
         DirectedGraph<String, DefaultEdge> dg2 = new DefaultDirectedGraph(DefaultEdge.class);
         convertGraph(totalMathching, dg2);
         System.out.println("printing");
         outputgraph(dg2);
         */

    }

    String finalGraph = "finalGraph.txt";
    String joinDegreeF = "joinDegree.txt";
    String partialResult = "partialResult.txt";
    String dir = "";

    private TreeMap<Double, Integer> outputDegree(DirectedGraph<String, DefaultEdge> dg) {
        TreeMap<Double, Integer> countV = new TreeMap<Double, Integer>();
        NeighborIndex ni = new NeighborIndex(dg);
        for (Object v : dg.vertexSet()) {
            String v1 = (String) v;
            double indegree = ni.neighborsOf(v1).size();
            if (!countV.containsKey(indegree)) {
                countV.put(indegree, 1);
            } else {
                countV.put(indegree, countV.get(indegree) + 1);
            }
        }
        //countV.keySet()
        // Collections.sort(null, pw);
        TreeMap<Double, Integer> treeMap = new TreeMap<Double, Integer>(countV);
        return treeMap;
    }

    private void outputTwoJoins(HashMap<ArrayList<Integer>, Integer> fakeTwojoins) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(joinDegreeF);
        for (Entry<ArrayList<Integer>, Integer> entry : fakeTwojoins.entrySet()) {
            pw.println(entry.getKey().get(0) + " " + entry.getKey().get(1) + " " + entry.getValue());
        }
        pw.close();
    }

    void run2() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    double coeff = 0.02;

    private void convertGraph(HashSet<ArrayList<Integer>> totalMathching, DirectedGraph<String, DefaultEdge> dg2) {
        //    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        HashMap<ArrayList<String>, Integer> te = new HashMap<>();
        int max = 0;
        for (ArrayList<Integer> temp : totalMathching) {
            //String[] temp = scanner.nextLine().split("\\s+");
            String from = "" + temp.get(1);

            if (max < Integer.parseInt(from)) {
                max = Integer.parseInt(from);
            }
            String too = "" + temp.get(0);
            if (max < Integer.parseInt(too)) {
                max = Integer.parseInt(too);
            }
            dg2.addVertex(from);
            dg2.addVertex(too);
            ArrayList<String> arr = new ArrayList<>();
            arr.add(from);
            arr.add(too);
            if (!te.containsKey(arr) && !from.equals(too)) {
                dg2.addEdge(from, too);
                //      te.put(arr, 0);
            }
        }

    }

    private HashMap<Integer, HashSet<String>> produceLayer(DirectedGraph<String, DefaultEdge> dg2) {
        HashMap<Integer, HashSet<String>> maps = new HashMap<>();
        HashSet<String> arr = new HashSet<>();
        HashSet<String> arrTotal = new HashSet<>();
        for (String s : dg2.vertexSet()) {
            if (dg2.inDegreeOf(s) == 0 || dg2.outDegreeOf(s) == 0) {
                arr.add(s);
                arrTotal.add(s);
            }
        }
        maps.put(0, arr);
        int id = 1;

        NeighborIndex ni = new NeighborIndex(dg2);

        HashSet<String> arr1 = new HashSet<>();
        arr1 = arr;
        while (!arr1.isEmpty()) {
            HashSet<String> newArr = new HashSet<>();
            for (String s : arr1) {
                List<String> lists = ni.neighborListOf(s);
                for (String t : lists) {
                    if (!arrTotal.contains(t)) {
                        newArr.add(t);
                        arrTotal.add(t);
                    }
                }
            }
            maps.put(id, newArr);
            id++;
            arr1 = newArr;
        }
        for (int i = 0; i < maps.keySet().size(); i++) {
            System.out.println(i + " " + maps.get(i).size());
        }
        while (id < 150) {
            HashSet<String> newArr = new HashSet<>();
            maps.put(id, newArr);
            id++;
        }
        //    HashSet<String> newArr1 = new HashSet<>();
        //    maps.put(id, newArr1);

        System.out.println();
        System.out.println(id);
        return maps;
    }

    private void settleDiameter(HashMap<Integer, HashSet<String>> totalMaps, HashMap<Integer, HashSet<String>> currentMaps, DirectedGraph<String, DefaultEdge> dg2, DirectedGraph<String, DefaultEdge> dg) {
        //      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        HashMap<String, Integer> counts = new HashMap<>();
        for (Entry<Integer, HashSet<String>> entry : currentMaps.entrySet()) {
            if (entry.getKey() == 1 && totalMaps.get(entry.getKey()).size() > 100) {
                level1Increase(counts, entry, currentMaps, totalMaps, dg2);

            }
        }
        // finisheReduc(currentMaps,dg2,counts);
        currentMaps = produceLayer(dg2);
        countEdges(currentMaps, dg2, 1);
        currentMaps = produceLayer(dg);
        countEdges(currentMaps, dg, 1);
        currentMaps = produceLayer(dg2);
        removeEdges(1, 5000, currentMaps, dg2);
        counts = new HashMap<>();
        for (int i = 2; i <= 14; i++) {
            currentMaps = produceLayer(dg2);
            for (Entry<Integer, HashSet<String>> entry : currentMaps.entrySet()) {
                if (entry.getKey() == i) {

                    level2Decrease(counts, entry, currentMaps, totalMaps, dg2);
                    currentMaps = produceLayer(dg2);
                    countEdges(currentMaps, dg2, i);
                    // if (i==2)

                    currentMaps = produceLayer(dg);
                    countEdges(currentMaps, dg, i);

                }
            }

        }

    }

    private void level1Increase(HashMap<String, Integer> counts, Entry<Integer, HashSet<String>> entry, HashMap<Integer, HashSet<String>> currentMaps, HashMap<Integer, HashSet<String>> totalMaps, DirectedGraph<String, DefaultEdge> dg2) {
        NeighborIndex ni = new NeighborIndex(dg2);
        HashMap<String, ArrayList<String>> avaVecs = new HashMap<>();
        //    HashMap<String, Integer> counts = new HashMap<>();

        int expected = (int) (totalMaps.get(entry.getKey()).size() * this.scale);
        System.out.println(expected);
        int layer = entry.getKey();
        if (expected > entry.getValue().size()) {
            HashMap<String, ArrayList<String>> outerVes = new HashMap<>();
            for (String s : entry.getValue()) {
                List<String> neigs = ni.neighborListOf(s);
                ArrayList<String> temp = new ArrayList<>();
                ArrayList<String> tempout = new ArrayList<>();
                for (String p : neigs) {
                    if (currentMaps.get(layer + 1).contains(p)) {
                        tempout.add(p);
                    }
                    if (currentMaps.get(layer - 1).contains(p)) {
                        temp.add(p);
                    }
                }
                if (temp.size() > 1) {
                    avaVecs.put(s, temp);
                }
                outerVes.put(s, tempout);
            }

            //
            ArrayList<String> keys = new ArrayList<>();
            keys.addAll(avaVecs.keySet());
            int start = 0;
            HashSet<String> finished = new HashSet<>();
            while (expected > entry.getValue().size() && start < keys.size()) {
                String v1 = keys.get(start);
                int flag = 0;
                String in1 = "";
                String out = "";
                System.out.println(expected + " " + start);
                for (String out1 : outerVes.get(v1)) {
                    if (!finished.contains(out1)) {
                        if (dg2.containsEdge(v1, out1)) {
                            for (String s : avaVecs.get(v1)) {
                                if (dg2.containsEdge(s, v1) && avaVecs.size() > 1) {
                                    in1 = s;
                                    flag = 1;
                                    break;
                                }
                            }
                        } else {
                            for (String s : avaVecs.get(v1)) {
                                if (dg2.containsEdge(v1, s) && avaVecs.size() > 1) {
                                    in1 = s;
                                    flag = 2;
                                    break;
                                }
                            }
                        }
                        if (flag != 0) {
                            out = out1;
                            break;
                        }

                    }
                }
                if (flag == 1) {
                    dg2.removeEdge(v1, out);
                    dg2.removeEdge(in1, v1);
                    dg2.addEdge(in1, out);
                    outerVes.get(v1).remove(out);
                    avaVecs.get(v1).remove(in1);
                    if (!counts.containsKey(v1)) {
                        counts.put(v1, 1);
                    } else {
                        counts.put(v1, counts.get(v1) + 1);
                    }
                    finished.add(out);
                    expected--;
                } else if (flag == 2) {
                    dg2.removeEdge(out, v1);
                    dg2.removeEdge(v1, in1);
                    dg2.addEdge(out, in1);
                    outerVes.get(v1).remove(out);
                    avaVecs.get(v1).remove(in1);
                    if (!counts.containsKey(v1)) {
                        counts.put(v1, 1);
                    } else {
                        counts.put(v1, counts.get(v1) + 1);
                    }
                    finished.add(out);
                    expected--;

                } else {
                    start++;
                }
            }
        }

    }

    private void finisheReduc(HashMap<Integer, HashSet<String>> currentMaps, DirectedGraph<String, DefaultEdge> dg2, HashMap<String, Integer> counts) {
        ConcurrentHashMap<String, Integer> incount = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Integer> outcount = new ConcurrentHashMap<>();
        for (Entry<String, Integer> entry : counts.entrySet()) {
            incount.put(entry.getKey(), entry.getValue());
            outcount.put(entry.getKey(), entry.getValue());
        }

        for (Entry<String, Integer> entry : incount.entrySet()) {
            ArrayList<String> arrs = new ArrayList<>();
            arrs.addAll(outcount.keySet());
            int start = 0;
            String in1 = entry.getKey();
            while (entry.getValue() > 0 && start < arrs.size()) {
                //     for (int i=0;i<arrs.size();i++){}
                String out1 = arrs.get(start);
                if (outcount.get(out1) > 0 && !in1.equals(out1)) {
                    if (!dg2.containsEdge(out1, in1)) {
                        dg2.addEdge(out1, in1);
                        outcount.put(out1, outcount.get(out1) - 1);
                        incount.put(in1, incount.get(in1) - 1);

                    }

                }
                start++;

            }
        }
        HashSet<String> arr = currentMaps.get(1);

    }

    private void level2Decrease(HashMap<String, Integer> counts, Entry<Integer, HashSet<String>> entry, HashMap<Integer, HashSet<String>> currentMaps, HashMap<Integer, HashSet<String>> totalMaps, DirectedGraph<String, DefaultEdge> dg2) {
        int expected = (int) (totalMaps.get(entry.getKey()).size() * this.scale) + (10 - entry.getKey());
        NeighborIndex ni = new NeighborIndex(dg2);
        HashMap<String, ArrayList<String>> maps = new HashMap<>();
        HashMap<String, Integer> mapsValue = new HashMap<>();
        HashSet<String> per = currentMaps.get(entry.getKey() - 1);
        System.out.println(entry.getKey() + "==" + entry.getValue().size());
        for (String s : entry.getValue()) {
            ArrayList<String> temp = new ArrayList<>();
            for (Object tt : ni.neighborsOf(s)) {
                String st = (String) tt;
                if (per.contains(st)) {
                    temp.add(st);
                }
            }
            if (temp.size() > 0) {
                maps.put(s, temp);
                mapsValue.put(s, temp.size());
            }
        }
        Sort sot = new Sort();
        List<Entry<String, Integer>> sorted = sot.sortOnValueIntegerS(mapsValue);
        if (entry.getKey() == 2) {
            expected = expected - 200;
        }
        if (entry.getKey() > 2) {
            expected = (int) (expected * 1.1);
        }
        System.out.println(entry.getValue().size() + "    " + expected + "    " + sorted.size());
        ArrayList<String> ks = new ArrayList<>();
        ks.addAll(entry.getValue());
        while (entry.getValue().size() > expected && sorted.size() > 0) {
            Entry<String, Integer> entr = sorted.remove(0);
            String s1 = entr.getKey();
            int a = 0;
            int b = 0;
            for (String s2 : maps.get(s1)) {
                if (dg2.containsEdge(s2, s1)) {
                    dg2.removeEdge(s2, s1);
                    a++;
                }
                if (dg2.containsEdge(s1, s2)) {
                    dg2.removeEdge(s1, s2);
                    b++;
                }

            }
            int index = (int) ((ks.size() - 1) * Math.random() + 0.4);
            String s2 = ks.get(index);
            while (a > 0) {
                while (s2.equals(s1)) {
                    index = (int) ((ks.size() - 1) * Math.random() + 0.4);
                    s2 = ks.get(index);
                }
                dg2.addEdge(s2, s1);
                a--;
            }
            index = (int) ((ks.size() - 1) * Math.random() + 0.4);
            s2 = ks.get(index);
            while (b > 0) {
                while (s2.equals(s1)) {
                    index = (int) ((ks.size() - 1) * Math.random() + 0.4);
                    s2 = ks.get(index);
                }
                dg2.addEdge(s1, s2);
                b--;
            }
            // if (index>ks.size()/2) dg2.addEdge(s2, s1);
            // else dg2.addEdge(s1, s2);
            expected++;

        }

        currentMaps = produceLayer(dg2);
    }

    private void countEdges(HashMap<Integer, HashSet<String>> currentMaps, DirectedGraph<String, DefaultEdge> dg2, int i) {
        //      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int total = 0;
        NeighborIndex ni = new NeighborIndex(dg2);
        for (String s : currentMaps.get(i)) {
            for (Object t : ni.neighborsOf(s)) {
                String ts = (String) t;
                if (currentMaps.get(i).contains(ts)) {
                    total++;
                }
            }
        }
        System.out.println("total::" + total);
    }

    private void removeEdges(int i, int i0, HashMap<Integer, HashSet<String>> currentMaps, DirectedGraph<String, DefaultEdge> dg2) {
        //      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        NeighborIndex ni = new NeighborIndex(dg2);
        i0 = -1;
        HashMap<String, ArrayList<String>> maps = new HashMap<>();
        for (String s : currentMaps.get(i)) {
            ArrayList<String> arr = new ArrayList<>();
            for (Object t : ni.neighborsOf(s)) {
                String ts = (String) t;
                if (currentMaps.get(2).contains(ts)) {
                    arr.add(ts);
                }

            }
            maps.put(s, arr);
        }

        while (i0 > 0) {
            System.out.println(i0);
            for (Entry<String, ArrayList<String>> entry : maps.entrySet()) {
                if (i0 > 0 && entry.getValue().size() > 0) {
                    String s = entry.getKey();
                    String t = entry.getValue().remove((int) (Math.random() * (entry.getValue().size() - 1) + 0.4));
                    if (dg2.containsEdge(t, s)) {
                        dg2.removeEdge(t, s);
                        i0--;
                    }
                    if (dg2.containsEdge(s, t)) {
                        dg2.removeEdge(s, t);
                        i0--;
                    }
                }
            }
        }
    }

    private void settleFourHops(DirectedGraph<String, DefaultEdge> dg, DirectedGraph<String, DefaultEdge> dg2) {
        System.out.println(this.scale);
        double totalsum = 0;
        System.out.println("start");
        /*      for (String s:dg.vertexSet()){
         NeighborIndex ni = new NeighborIndex(dg);
         for (Object t:ni.neighborsOf(s)){
         for (Object r:ni.neighborsOf(s)){
         if (!t.equals(r)){
         totalsum+=(dg.edgesOf((String) t).size()-1)*(dg.edgesOf((String) r).size()-1);
         }
         }
         }
         }
         */
        System.out.println("totalSum: " + totalsum);
        totalsum = 0;
        /*           for (DefaultEdge de:dg.edgeSet()){
         String s = dg.getEdgeSource(de);
         String t = dg.getEdgeTarget(de);
         double sum1 = dg.edgesOf(s).size()-1;
         NeighborIndex ni = new NeighborIndex(dg);
         double sum2=0;
         //   if (sum1>this.fourthHopThreshold){
         for (Object obneig: ni.neighborsOf(t)){
         String neig=(String) obneig;
         sum2+=dg.edgesOf(neig).size()-1;
         }
         sum2=sum2-sum1;
         totalsum+=sum2*sum1;
         //}
         }*/
        System.out.println("totalSum: " + totalsum);
        totalsum = 4180000000000.0;
        double ratio = 1.0 / (this.scale * dg.edgeSet().size() / dg2.edgeSet().size());
        ratio = ratio * ratio;
        System.out.println(ratio);
        //  for ()
        double expectedSum = totalsum * dg2.edgeSet().size() * dg2.edgeSet().size() / dg.edgeSet().size() / dg.edgeSet().size() * ratio * ratio * ratio * ratio * ratio * Math.sqrt(ratio);

        System.out.println(expectedSum + "expected");
        double currentSum = 0;
        HashMap<String, Double> vertexMaps = new HashMap<>();
        HashMap<DefaultEdge, Double> maps = new HashMap<>();
        HashMap<String, Double> vertexsDergrees = new HashMap<>();
        for (DefaultEdge de : dg2.edgeSet()) {
            String s = dg2.getEdgeSource(de);
            vertexsDergrees.put(s, 0.0 + dg2.edgesOf(s).size());

            String t = dg2.getEdgeTarget(de);
            vertexsDergrees.put(t, 0.0 + dg2.edgesOf(t).size());

            double sum1 = dg2.edgesOf(s).size() - 1;
            NeighborIndex ni = new NeighborIndex(dg2);
            double sum2 = 0;
            for (Object obneig : ni.neighborsOf(t)) {
                String neig = (String) obneig;
                sum2 += dg2.edgesOf(neig).size() - 1;
            }
            //    sum2=sum2-dg2.e;
            vertexMaps.put(t, sum2);
            sum2 = sum2 - sum1;
            currentSum += sum2 * sum1;
            maps.put(de, sum2 * sum1);
        }
        System.out.println(currentSum + "eeeee" + expectedSum);
        //  expectedSum=currentSum/1.3;
        HashMap<String, Integer> deletedIn = new HashMap<>();
        HashMap<String, Integer> deletedOut = new HashMap<>();

        if (currentSum > expectedSum) {
            List<Entry<DefaultEdge, Double>> sorted = new Sort().sortOnValueDefaultEdge(maps);

            int indicator = 0;
            int vindicator = 0;
            while (currentSum > expectedSum && indicator < sorted.size()) {
                DefaultEdge de = sorted.get(indicator).getKey();
                double valCount = sorted.get(indicator).getValue();
                System.out.println(valCount);

                String source = dg2.getEdgeSource(de);
                String tar = dg2.getEdgeTarget(de);
                //     if (valCount>=3){
                if (dg2.edgesOf(tar).size() < this.fourthHopThreshold) {
                    if (deletedIn.containsKey(tar)) {
                        deletedIn.put(tar, deletedIn.get(tar) + 1);
                    } else {
                        deletedIn.put(tar, 1);
                    }
                }

                if (dg2.edgesOf(source).size() < this.fourthHopThreshold) {
                    if (deletedOut.containsKey(source)) {
                        deletedOut.put(source, deletedOut.get(source) + 1);
                    } else {
                        deletedOut.put(source, 1);
                    }
                }
                dg2.removeEdge(de);
                currentSum -= valCount;
                indicator++;
                for (Entry<String, Integer> entry : deletedOut.entrySet()) {
                    for (Entry<String, Integer> entry2 : deletedIn.entrySet()) {
                        if (entry.getValue() > 0 && entry2.getValue() > 0 && !dg2.containsEdge(entry.getKey(), entry2.getKey()) && entry.getKey() != entry2.getKey()) {
                            dg2.addEdge(entry.getKey(), entry2.getKey());
                            String s = entry.getKey();
                            String t = entry2.getKey();
                            double sum1 = dg2.edgesOf(s).size() - 1;
                            NeighborIndex ni = new NeighborIndex(dg2);
                            double sum2 = 0;
                            for (Object obneig : ni.neighborsOf(t)) {
                                String neig = (String) obneig;
                                sum2 += dg2.edgesOf(neig).size() - 1;
                            }
                            //    sum2=sum2-dg2.e;
                            sum2 = sum2 - sum1;
                            double currentSum1 = sum2 * sum1;
                            currentSum = currentSum + currentSum1;
                            //  count--;
                            deletedOut.put(entry.getKey(), deletedOut.get(entry.getKey()) - 1);

                            deletedIn.put(entry2.getKey(), deletedIn.get(entry2.getKey()) - 1);
                        }
                    }
                }

                System.out.println(currentSum + " " + expectedSum);
            }
        } else {
            List<Entry<DefaultEdge, Double>> sorted = new Sort().sortOnValueDefaultEdge(maps);
            List<Entry<String, Double>> vsorted = new Sort().sortOnValueString(vertexMaps);
            List<Entry<String, Double>> degreesorted = new Sort().sortOnValueString(vertexsDergrees);

            int indicator = sorted.size() - 1;
            int vindicator = 0;
            int count = 0;
            while (currentSum < expectedSum && indicator >= 0 && count < dg2.edgeSet().size() * 0.01) {
                DefaultEdge de = sorted.get(indicator).getKey();
                double valCount = sorted.get(indicator).getValue();
                System.out.println(valCount);
                if (valCount > 10) {
                    String source = dg2.getEdgeSource(de);
                    String tar = dg2.getEdgeTarget(de);
                    if (dg2.edgesOf(tar).size() < this.fourthHopThreshold) {
                        if (deletedIn.containsKey(tar)) {
                            deletedIn.put(tar, deletedIn.get(tar) + 1);
                        } else {
                            deletedIn.put(tar, 1);
                        }
                    }

                    if (dg2.edgesOf(source).size() < this.fourthHopThreshold) {
                        if (deletedOut.containsKey(source)) {
                            deletedOut.put(source, deletedOut.get(source) + 1);
                        } else {
                            deletedOut.put(source, 1);
                        }
                    }
                    dg2.removeEdge(de);
                    count++;
                    currentSum -= valCount;
                }
                indicator--;
                System.out.println("size" + degreesorted.size());
                System.out.println(vsorted.size());
                //     for (Entry<String, Integer> entry : deletedOut.entrySet()) {

                for (Entry<String, Double> entry2 : degreesorted) {
                    if (vindicator >= vsorted.size()) {
                        vindicator = 0;
                    }
                    if (entry2.getValue() > 0 && vsorted.size() > 0 && currentSum < expectedSum && count < dg2.edgeSet().size() * 0.01) {

                        String temp = vsorted.get(vindicator).getKey();
                        while (dg2.containsEdge(temp, entry2.getKey()) && vindicator < vsorted.size() - 1) {
                            vindicator++;
                            temp = vsorted.get(vindicator).getKey();
                        }
                        System.out.println("vindi" + vindicator);
                        if (vindicator < vsorted.size()) {
                            dg2.addEdge(temp, entry2.getKey());
                            //   String s = entry.getKey();
                            String t = entry2.getKey();
                            double sum1 = dg2.edgesOf(temp).size() - 1;
                            NeighborIndex ni = new NeighborIndex(dg2);
                            double sum2 = 0;
                            for (Object obneig : ni.neighborsOf(t)) {
                                String neig = (String) obneig;
                                sum2 += dg2.edgesOf(neig).size() - 1;
                            }
                            sum2 = sum2 - sum1;
                            double currentSum1 = sum2 * sum1;
                            currentSum = currentSum + currentSum1;
                            System.out.println("here " + currentSum);
                         //   deletedOut.put(entry.getKey(), deletedOut.get(entry.getKey()) - 1);

                            //      deletedIn.put(entry2.getKey(), deletedIn.get(entry2.getKey()) - 1);
                            vsorted.remove(vindicator);
                            break;
                        }
                        vindicator = 0;
                    }
                    //     }
                }

                /*  
                 for (Entry<String, Integer> entry2 : deletedOut.entrySet()) {
                 if ( entry2.getValue() > 0 && vsorted.size()>0) {
                 System.out.println(vindicator);
                 String temp=   vsorted.get(vindicator).getKey();
                 while (dg2.containsEdge(entry2.getKey(),temp) && vindicator<vsorted.size()){
                 vindicator++;
                 temp=   vsorted.get(vindicator).getKey();
                 }
                 if (vindicator<vsorted.size()){
                 dg2.addEdge(entry2.getKey(),temp);
                 //   String s = entry.getKey();
                 String t = entry2.getKey();
                 double sum1 = dg2.edgesOf(t).size()-1;
                 //   NeighborIndex ni = new NeighborIndex(dg2);
                 double sum2 = vsorted.get(vindicator).getValue();
                       
                 //   sum2=sum2-sum1;
                 double currentSum1 = sum2 * sum1;
                 currentSum = currentSum + currentSum1;
                 System.out.println("here");
                 //   deletedOut.put(entry.getKey(), deletedOut.get(entry.getKey()) - 1);

                 deletedOut.put(entry2.getKey(), deletedOut.get(entry2.getKey()) - 1);
                 vsorted.remove(vindicator);
                 }
                 vindicator=0;}
                 //     }
                 }*/
            }

        }
        System.out.println(currentSum + " " + expectedSum);
        int total = 0;
        for (String s : dg2.vertexSet()) {
            int sum1 = dg2.edgesOf(s).size();
            total += sum1 * (sum1 - 1);
        }
        //     System.out.println(total);
    }
    int fourthHopThreshold = 100;

    private void adjustStars(DirectedGraph<String, DefaultEdge> dg2, DirectedGraph<String, DefaultEdge> dg) {
        //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        NeighborIndex ni = new NeighborIndex(dg);
        DirectedSubgraph<String, DefaultEdge> sub;
        int count = 0;
        HashSet<String> ss = new HashSet<>();
        /*      for (DefaultEdge dh : dg.edgeSet()) {
         String s1 = dg.getEdgeSource(dh);
         String s2 = dg.getEdgeTarget(dh);
         Set<String> v1 = ni.neighborsOf(s1);
         Set<String> v2 = ni.neighborsOf(s2);
         for (String s : v1) {
         if (v2.contains(s)) {
         ss.add(s);
         }

         }
         for (String s : v2) {
         if (v1.contains(s)) {
         ss.add(s);
         }

         }
         }
         int count2 = 0;
         for (String s : dg.vertexSet()) {
         if (!ss.contains(s)) {
         count2++;
         Set<String> vs = ni.neighborsOf(s);
         sub = new DirectedSubgraph(dg, vs, dg.edgeSet());
         if (sub.edgeSet().isEmpty()) {
         count++;
         }
         }
         }
         System.out.println(count2 + " " + count);
         double ratio = 1.0 * count / dg.vertexSet().size();
         System.out.println("ratio1 " + ratio);
         */
        double ratio1 = 0.08293290105945696;
        double ratio;
        count = 0;
        ni = new NeighborIndex(dg2);
        ArrayList<String> stars = new ArrayList<>();

        for (String s : dg2.vertexSet()) {
            Set<String> vs = ni.neighborsOf(s);
            sub = new DirectedSubgraph(dg2, vs, dg2.edgeSet());
            if (sub.edgeSet().size() == 0) {
                stars.add(s);
                count++;
            }
        }

        ratio = 1.0 * count / dg2.vertexSet().size();
        System.out.println("ratio2 " + ratio);

        if (ratio > ratio1) {
            reduceStar(stars, (int) ((ratio - ratio1) * dg2.vertexSet().size()), dg2);
        }
    }

    private void reduceStar(ArrayList<String> stars, int d, DirectedGraph<String, DefaultEdge> dg2) {
        NeighborIndex ni = new NeighborIndex(dg2);
        ArrayList<String> arrs = new ArrayList<>();

        for (String s : stars) {
            if (ni.neighborsOf(s).size() <= 1) {
                arrs.add(s);
            }
        }
        for (String s : arrs) {
            stars.remove(s);
        }

        while (d > 0 && !stars.isEmpty()) {
            int k = (int) (Math.random() * (stars.size() - 1));
            Set<String> sets = ni.neighborsOf(stars.get(k));
            if (sets.size() > 1) {
                Iterator<String> it = sets.iterator();
                String s = it.next();
                String v = it.next();
                dg2.addEdge(s, v);
                List<String> ss1 = ni.neighborListOf(s);
                List<String> ss2 = ni.neighborListOf(v);
                HashSet<String> inters = new HashSet<>();
                for (String e : ss1) {
                    if (ss2.contains(e)) {
                        inters.add(e);
                    }
                }
                for (String e : ss2) {
                    if (ss1.contains(e)) {
                        inters.add(e);
                    }
                }
                d = d - inters.size();
            }
            stars.remove(k);
        }
    }
}
