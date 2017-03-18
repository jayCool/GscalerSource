package algorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author workshop
 */
public class Sort {
    public Sort(){
    }
    
    public List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortOnKeySum(HashMap<ArrayList<ArrayList<Integer>>, Integer> orders) {
        List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted = new ArrayList<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>>(orders.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>>() {
            public int compare(Map.Entry<ArrayList<ArrayList<Integer>>, Integer> o1, Map.Entry<ArrayList<ArrayList<Integer>>, Integer> o2) {
                ArrayList<ArrayList<Integer>> arr1 = new ArrayList<>();
                ArrayList<ArrayList<Integer>> arr2= new ArrayList<>();
                arr1=o1.getKey();
                arr2=o2.getKey();
                int sum1=0;
                int sum2=0;
                for (ArrayList<Integer> psum:arr1){
                 for (int p:psum){
                     sum1+=p;
                 }
                }
                 for (ArrayList<Integer> psum:arr2){
                 for (int p:psum){
                     sum2+=p;
                 }
                }
                return -(sum2 - sum1);
            }
        });
        return sorted;
    }
      
    public List<Map.Entry<ArrayList<Integer>, Integer>> sortOnKeySumDescending(HashMap<ArrayList<Integer>, Integer> orders) {
        List<Map.Entry<ArrayList<Integer>, Integer>> sorted = new ArrayList<Map.Entry<ArrayList<Integer>, Integer>>(orders.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<ArrayList<Integer>, Integer>>(){
            public int compare(Map.Entry<ArrayList<Integer>, Integer> o1, Map.Entry<ArrayList<Integer>, Integer> o2) {
                int sum1=0;
                int sum2=0;
                 for (int p:o1.getKey()){
                     sum1+=p;
                 }
                for (int p:o2.getKey()){
                     sum2+=p;
                 }
              
                return -(sum2 - sum1);
            }
        });
        return sorted;
    }
    
      List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sortOnAppearance(HashMap<ArrayList<ArrayList<Integer>>, Integer> orders,  final HashMap<ArrayList<Integer>, Integer> target) {
    List<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>> sorted = new ArrayList<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>>(orders.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<ArrayList<ArrayList<Integer>>, Integer>>() {
            public int compare(Map.Entry<ArrayList<ArrayList<Integer>>, Integer> o1, Map.Entry<ArrayList<ArrayList<Integer>>, Integer> o2) {
                ArrayList<ArrayList<Integer>> arr1 = new ArrayList<>();
                ArrayList<ArrayList<Integer>> arr2= new ArrayList<>();
                arr1=o1.getKey();
                arr2=o2.getKey();
                int sum1=0;
                int sum2=0;
                for (ArrayList<Integer> psum:arr1){
                sum1+=target.get(psum);
                }
                 for (ArrayList<Integer> psum:arr2){
                sum2+=target.get(psum);
                }
                return -(sum2 - sum1);
            }
        });
        return sorted;  }
  
}
