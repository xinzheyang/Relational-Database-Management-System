/**
 * 
 */
package visitor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;

import logicaloperator.LogicalOperator;
import physicaloperator.*;

/**
 * @author sitianchen
 *
 */
public class PhysicalPlanWriter {
	private final String DASH="-";
	BufferedWriter physicalWriter;
	
	public PhysicalPlanWriter(BufferedWriter write) {
		physicalWriter = write;
	}

	public void visit(ScanOperator op) {
		//create logical plan
		try {
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH)) +
					"TableScan["+op.getTableName()+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void visit(IndexScanOperator op) {
		try {
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH))
					+ "IndexScan["+op.getTableName()+","+op.getIndexName()+","+op.getLowKey()+","+op.getHighKey()+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visit(SelectOperator op) {
		try {
			String ex = op.getEx() == null ? "" : op.getEx().toString();
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH))
					+ "Select["+ex+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getChildOp().accept(this);
	}

	public void visit(TNLJoinOperator op) {

	}
	
	public void visit(SMJoinOperator op) {
		String ex = op.getCondition() == null ? "" : op.getCondition().toString();
		try {
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH))+
					"SMJ"+"["+ex+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getLeftChild().accept(this);
		op.getRightChild().accept(this);
	}
	
	public void visit(BNLJoinOperator op) {
		String ex = op.getCondition() == null ? "" : op.getCondition().toString();
		try {
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH))+
					"BNLJ"+"["+ex+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getLeftChild().accept(this);
		op.getRightChild().accept(this);
	}

	public void visit(ExternalSortOperator op) {
		try {
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH))
					+ "ExternalSort"+"["+String.join(", ", op.getColumnIndexMap().keySet())+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getChildOp().accept(this);
	}
	
	public void visit(InMemorySortOperator op) {

	}
	
	public void visit(ProjectOperator op) {
		try {
			physicalWriter.write(String.join("", Collections.nCopies(1, DASH))+
					"Project"+"["+String.join(", ", op.getColumnIndexMap().keySet())+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getChildOp().accept(this);
	}
	
	public void visit(DupElimOperator op) {
		try {
			physicalWriter.write("DupElim\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getChildOp().accept(this);
	}

}
