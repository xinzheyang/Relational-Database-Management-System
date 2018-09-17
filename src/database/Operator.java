/**
 * 
 */
package database;

/**
 * @author xinzheyang
 *
 */
public abstract class Operator {
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
