package paperalgorithm;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author workshop
 */
public class PrintFunction extends Sort{
    public PrintFunction(){
    
    }
    
  public void  printListEntry(List<Map.Entry<Integer, Integer>> sortedSumDegree) {
        for (Map.Entry<Integer, Integer> temp : sortedSumDegree) {
            System.out.println(temp.getKey() + "  " + temp.getValue());
        }
    }
  
   public void printListPair(List<Pair> sortedSumDegree) {
        for (Pair temp : sortedSumDegree) {
            System.out.println(temp.toString());
        }
    }
   
   public void printHashMap(HashMap<Integer, Integer> downSizedSumDegree) throws FileNotFoundException {
       PrintWriter pw = new PrintWriter("0.8calfidDegree.txt");
       Iterator<Map.Entry<Integer, Integer>> iterator = downSizedSumDegree.entrySet().iterator();
        int sum=0;
        int countp=0;
        int size = downSizedSumDegree.size();
        int countDegree = 0;
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
             if (entry.getValue()!=0)
          countDegree+=entry.getKey()*entry.getValue();
            pw.println(entry.getKey() + "  " + entry.getValue());
            if (entry.getValue().equals(1)){
            countp++;
            }
            sum+=entry.getValue();
        }
        System.out.println(countp);
        pw.close();        
        System.out.println("UserSize: "+sum+"   OneRatio: "+(1.0*countp/size)+"   DegreeSize: "+ countDegree);
    }
   
   public void printHashMapPair(HashMap<Pair, Integer> downSizedSumDegree) {
        Iterator<Map.Entry<Pair, Integer>> iterator = downSizedSumDegree.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Pair, Integer> entry = iterator.next();
            if (entry.getValue()!=0)
            System.out.println(entry.getKey().toString()+"  " + entry.getValue());
                //  System.out.println( entry.getValue());
    
        }
    }
   
    public void printFunction(HashMap<Vector<Pair>, Integer> downSizeFunc) {
      Iterator<Map.Entry<Vector<Pair>, Integer>> iterator = downSizeFunc.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Vector<Pair>, Integer> entry = iterator.next();
            System.out.println(entry.getKey().get(0).toString()+"  "+entry.getKey().get(1).toString()+"  " + entry.getValue());
           }
    }

  
}
