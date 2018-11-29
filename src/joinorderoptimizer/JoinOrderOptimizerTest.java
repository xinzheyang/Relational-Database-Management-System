/**
 * 
 */
package joinorderoptimizer;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import logicaloperator.LogicalJoinOperator;
import logicaloperator.LogicalOperator;
import logicaloperator.LogicalScanOperator;

/** Test for join order optimizer.
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
		scanC.setRelationSize(7000);
		List<LogicalOperator> listAB = new LinkedList<LogicalOperator>();
		listAB.add(scanA);
		listAB.add(scanB);
		LogicalJoinOperator joinAB = new LogicalJoinOperator(listAB, null, null, null);
		JoinOrderOptimizer optAB = new JoinOrderOptimizer(joinAB, null);
		optAB.dpChooseBestPlan();
		List<LogicalOperator> bestAB = optAB.getBestOrder();
		assert bestAB.get(0).equals(scanA);
		assert bestAB.get(1).equals(scanB);
		
		List<LogicalOperator> listCB = new LinkedList<LogicalOperator>();
		listCB.add(scanC);
		listCB.add(scanB);
		
		LogicalJoinOperator joinCB = new LogicalJoinOperator(listCB, null, null, null);
		JoinOrderOptimizer optCB = new JoinOrderOptimizer(joinCB, null);
		optCB.dpChooseBestPlan();
		List<LogicalOperator> bestCB = optCB.getBestOrder();
		assert bestCB.get(0).equals(scanB);
		assert bestCB.get(1).equals(scanC);
		
		List<LogicalOperator> listABC = new LinkedList<LogicalOperator>();
		listABC.add(scanA);
		listABC.add(scanB);
		listABC.add(scanC);
		
		LogicalJoinOperator joinABC = new LogicalJoinOperator(listABC, null, null, null);
		JoinOrderOptimizer optABC = new JoinOrderOptimizer(joinABC, null);
		optABC.dpChooseBestPlan();
		List<LogicalOperator> bestABC = optABC.getBestOrder();
		assert bestABC.get(0).equals(scanA);
		assert bestABC.get(1).equals(scanB);
		assert bestABC.get(2).equals(scanC);
	}

}
