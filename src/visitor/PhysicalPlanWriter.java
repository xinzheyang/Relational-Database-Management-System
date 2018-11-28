/**
 * 
 */
package visitor;

import java.io.BufferedWriter;
import physicaloperator.*;

/**
 * @author sitianchen
 *
 */
public class PhysicalPlanWriter {

	public PhysicalPlanWriter(BufferedWriter write) {

	}

	public void visit(ScanOperator scan) {

	}
	
	public void visit(IndexScanOperator scan) {

	}

	public void visit(SelectOperator select) {

	}

	public void visit(TNLJoinOperator op) {

	}
	
	public void visit(SMJoinOperator op) {

	}
	
	public void visit(BNLJoinOperator op) {

	}

	public void visit(ExternalSortOperator op) {

	}
	
	public void visit(InMemorySortOperator op) {

	}
	
	public void visit(ProjectOperator op) {

	}
	
	public void visit(DupElimOperator op) {

	}

}
