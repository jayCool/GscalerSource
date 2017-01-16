/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package paperalgorithm;

/**
 *
 * @author workshop
 */
public class Pair {
    private Integer first;
    private Integer second;
    public Pair(Integer first, Integer second) {
    	super();
    	this.first = first;
    	this.second = second;
    }

    public Pair() {
        first =0;
        second = 0;
    }

    public int hashCode() {
    	int hashFirst = first != null ? first.hashCode() : 0;
    	int hashSecond = second != null ? second.hashCode() : 0;

    	return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    public boolean equals(Object other) {
    	if (other instanceof Pair) {
    		Pair otherPair = (Pair) other;
    		return 
    		((  this.first == otherPair.first ||
    			( this.first != null && otherPair.first != null &&
    			  this.first.equals(otherPair.first))) &&
    		 (	this.second == otherPair.second ||
    			( this.second != null && otherPair.second != null &&
    			  this.second.equals(otherPair.second))) );
    	}

    	return false;
    }

    public String toString()
    { 
           return "(" + first + ", " + second + ")"; 
    }

    public Integer getFirst() {
    	return first;
    }

    public void setFirst(Integer first) {
    	this.first = first;
    }

    public Integer getSecond() {
    	return second;
    }

    public void setSecond(Integer second) {
    	this.second = second;
    }
    
    public Integer getSum(){
        return first+second;
    }
    
    public Integer get(int k){
    if (k==0) return first;
    else return second;
    }
    public void set (int k, int value){
    if (k==0) first = value;
    else second = value;
    }
   
}
