/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author workshop
 */
public class DistributionFeature {

    public HashMap<Integer, Integer> indegreeDis = new HashMap<>();
    public HashMap<Integer, Integer> outdegreeDis = new HashMap<>();
    public HashMap<ArrayList<Integer>, Integer> jointdegreeDis = new HashMap<>();
    public HashMap<ArrayList<ArrayList<Integer>>, Integer> correlationFunction = new HashMap<>();
    int nodeSize;
    int edgeSize;

}
