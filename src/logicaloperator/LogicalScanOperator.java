/**
 * 
 */
package logicaloperator;

import java.io.FileNotFoundException;

import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 * An logical operator that stores minimal information
 * to be visited by PhysicalPlanBuilder to transform into a physical plan builder
 */
public class LogicalScanOperator extends LogicalOperator {
	private String tb;
	private String alias;
	/**
	 * 
	 */
	public LogicalScanOperator(String table) throws FileNotFoundException {
		this.tb = table;
		this.alias = "";
	}
	
	public LogicalScanOperator(String table, String alias) throws FileNotFoundException {
		this.tb = table;
		this.alias = alias;
	}
	/**
	 * @return the alias of the table
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @return the name of the table
	 */
	public String getTableName() {
		return tb;
	}
	/* (non-Javadoc)
	 * @see logicaloperator.LogicalOperator#accept(visitor.PhysicalPlanBuilder)
	 */
	@Override
	public void accept(PhysicalPlanBuilder pb) {
		pb.visit(this);
	}

}
