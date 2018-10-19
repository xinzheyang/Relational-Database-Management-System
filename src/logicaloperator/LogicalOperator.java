/**
 * 
 */
package logicaloperator;

import visitor.PhysicalPlanBuilder;

/**
 * @author xinzheyang
 *
 */
public abstract class LogicalOperator {
	public abstract void accept(PhysicalPlanBuilder pb);
}
