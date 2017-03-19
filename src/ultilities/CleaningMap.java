/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CleaningMap {

    public static boolean cleanHashMap(HashMap<ArrayList<Integer>, Integer> map, ArrayList<Integer> key) {
        if (map.get(key) <= 0) {
            map.remove(key);
            return true;
        }
        return false;
    }
    
     public static boolean cleanHashMap(HashMap<Integer, Integer> map, Integer key) {
        if (map.get(key) <= 0) {
            map.remove(key);
            return true;
        }
        return false;
    }

    public static void removeZero(HashMap<ArrayList<Integer>, Integer> map) {
        ArrayList<ArrayList<Integer>> keys = new ArrayList<>();
        for (ArrayList<Integer> temp : map.keySet()) {
            keys.add(temp);
        }
        for (ArrayList<Integer> temp : keys) {
            if (map.get(temp) <= 0) {
                map.remove(temp);
            }
        }
    }
    
    public static void removeZeroDegreeMap(HashMap<Integer, Integer> scaleIndegreeMap) {
        ArrayList<Integer> zeroDegrees = new ArrayList<>();
        for (Integer key : scaleIndegreeMap.keySet()) {
            if (scaleIndegreeMap.get(key) == 0) {
                zeroDegrees.add(key);
            }
        }
        for (Integer key : zeroDegrees) {
            scaleIndegreeMap.remove(key);
        }
    }

}
