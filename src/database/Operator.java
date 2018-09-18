/**
 * 
 */
package database;

import java.util.*;

/**
 * @author xinzheyang
 *
 */
public abstract class Operator {
	
	public ArrayList<Tuple> relation;
	public int ptr;
	/**
	 * @return
	 */
	public abstract Tuple getNextTuple();
	/**
	 * 
	 */
	public abstract void reset();
	
	public abstract void dump();
}
