/**
 * 
 */
package joinorderoptimizer;

import logicaloperator.LogicalOperator;
import net.sf.jsqlparser.expression.Expression;

/** Computes the intermediate join size of two relations.
 * @author sitianchen
 *
 */
public class JoinSizeCompute {
	
	private LogicalOperator leftRelation;
	private LogicalOperator rightRelation;
	
	public JoinSizeCompute(LogicalOperator left, LogicalOperator right, Expression joinCondition) {
		//TODO: placeholder
		leftRelation = left;
		rightRelation = right;
	}
	
	/** Computes the intermediate join size of this j
	 * @return
	 */
	public int computeJoinSize() {
		//TODO:placeholder
		return 0;
	}
	
	/**
	 * @return
	 */
	private int computeVValue(String relation, String attribute) {
		//TODO:placeholder
		return 0;
	}

}
