package algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class AdjacencyMat {

    public static void main(String[] args) throws FileNotFoundException {
        
    }
    public void adjmat(String args[]) throws FileNotFoundException{
         System.out.println(args.length);
        String inputFile = args[0];
        String outputFile = args[1];
        Scanner scanner = new Scanner(new File(inputFile));
        int row = 0;
        PrintWriter pw = new PrintWriter(new File(outputFile));
        while (scanner.hasNext()){
            String[] splits = scanner.nextLine().trim().split("[^a-zA-Z0-9']+");
            for (int col = 0; col < splits.length; col ++){
                if (splits[col].equals("1")){
                    pw.println(row + "\t" + col);
                }
            }
            row ++;
        }
        scanner.close();
        pw.close();
    
    }
    
}