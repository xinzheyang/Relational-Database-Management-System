/**
 * 
 */
package joinorderoptimizer;

import static org.junit.Assert.*;

import org.junit.Test;

import logicaloperator.LogicalOperator;
import logicaloperator.LogicalScanOperator;

/**
 * @author sitianchen
 *
 */
public class JoinOrderOptimizerTest {

	@Test
	public void test() {
		LogicalScanOperator scanA = new LogicalScanOperator("Sailors", "S");
		LogicalScanOperator scanB = new LogicalScanOperator("Reserves", "R");
		LogicalScanOperator scanC = new LogicalScanOperator("Boats", "B");
		scanA.setRelationSize(1000);
		scanB.setRelationSize(5000);
		fail("Not yet implemented");
	}

}
