/**
 * 
 */
package logicaloperator;

import java.util.HashMap;

import database.DBCatalog;
import net.sf.jsqlparser.expression.Expression;
import visitor.DivideSelectVisitor;
import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 * An logical operator that stores minimal information
 * to be visited by PhysicalPlanBuilder to transform into a physical plan builder
 */
public class LogicalSelectOperator extends LogicalOperator {
	private LogicalOperator childOp;
	private int totalReductionFactor; //reduction factor calculated from select condition
	private Expression ex;
	private HashMap<String, int[]> attribBounds;
	private HashMap<String, Integer> reductionFactorMap;
	
	public LogicalOperator getChildOp() {
		return childOp;
	}
	
	public String getReference() {
		return ((LogicalScanOperator) childOp).getReference();
	}
	
	public String getBaseTableName() {
		return ((LogicalScanOperator) childOp).getTableName();
	}

	public Expression getEx() {
		return ex;
	}
	
	/**
	 * 
	 */
	public LogicalSelectOperator(LogicalOperator child, Expression exp) {
		childOp = child;
		ex = exp;
		attribBounds = new HashMap<String, int[]>(((LogicalScanOperator) child).getAttribBounds());
		//builds the reduction factor map and computes the total reduction factor for relation size calculation
		reductionFactorMap = new HashMap<String, Integer>();
		totalReductionFactor = 1;
		for (String attrib : DBCatalog.getTableColumns(getBaseTableName())) {
			int curReductionFactor = computeReductionFactor(attrib);
			totalReductionFactor *= curReductionFactor;
			reductionFactorMap.put(attrib, curReductionFactor);
		}
		
	}
	
	/** Computes the reduction factor of this attribute.
	 * @param attrib
	 * @return
	 */
	public int computeReductionFactor(String attrib) {
		DivideSelectVisitor visitor = new DivideSelectVisitor(attrib);
		ex.accept(visitor);
		int[] bounds = attribBounds.get(attrib);
		int selectLow = Math.max(visitor.getLowKey(), bounds[0]);
		int selectHigh = Math.min(visitor.getHighKey(), bounds[1]);
		int r = (selectHigh-selectLow+1)/(bounds[1]-bounds[0]+1);
		return r;
	}
	
	public int getReductionFactor(String attrib) {
		assert reductionFactorMap.containsKey(attrib);
		return reductionFactorMap.get(attrib);
	}
	
	/** Sets the reduction factor of this instance. Used for testing only.
	 * @param reductionFactor
	 */
	public void setReductionFactor(int reductionFactor) {
		this.totalReductionFactor = reductionFactor;
	}
	
	/** Gets the relation size, in tuples, of this select operator.
	 * @return
	 * @throws Exception 
	 */
	public int getRelationSize() {
		return ((LogicalScanOperator) childOp).getRelationSize() * totalReductionFactor;
	}
	
	/** Setter just for testing, not allowed to be called for other purposes.
	 * @param newFactor
	 */
	public void setTotalReductionFactor(int newFactor) {
		totalReductionFactor = newFactor;
	}
	
	/** Computes and returns the V-value of this selection on the given attribute
	 * from its base table's V-value on the attribute multiplied by the reduction factor
	 * of this attribute, adding an upper bound of the selection's output relation size.
	 * @param attrib
	 * @return
	 * @throws Exception 
	 */
	public int getVValue(String attrib) {
		//TODO: implement this
		int reductionFactor = getReductionFactor(attrib);
		return Math.min(getRelationSize(),  ((LogicalScanOperator) childOp).getVValue(attrib) * reductionFactor);
	}

	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
