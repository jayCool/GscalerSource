/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.io.IOException;

/**
 *
 * @author Anwesha94
 */
public class Executeexternal {
    public static void main(String args[]) throws IOException{
        Process p = Runtime.getRuntime().exec("cmd /K (cd C:\\u & wgnuplot.exe C:\\u\\p1.plt)");
        
        //cmd /C "cd C:\\u && wgnuplot.exe C:\\u\\p1.plt"
    }
}
