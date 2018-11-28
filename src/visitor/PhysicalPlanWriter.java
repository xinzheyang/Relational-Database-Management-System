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
 * @author xinqilyu
 * A physical writer builder that uses visitor pattern to write the formatted physical plan
 * of the query into a file
 */
public class PhysicalPlanWriter {
	private final String DASH="-";
	BufferedWriter physicalWriter; //the BufferWriter 
	int counter=0;
//	int tmp;
	
	public PhysicalPlanWriter(BufferedWriter write) {
		physicalWriter = write;
	}
	
	public void visit(DupElimOperator op) {
		try {
			physicalWriter.write("DupElim\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		counter++;
		op.getChildOp().accept(this);
	}
	
	public void visit(ExternalSortOperator op) {
		try {
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH))
					+ "ExternalSort"+"["+String.join(", ", op.getSortedByCols())+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		op.getChildOp().accept(this);
	}
	
	public void visit(ProjectOperator op) {
		try {
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH))+
					"Project"+"["+String.join(", ", op.getColsInOrder())+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}

		op.getChildOp().accept(this);
	}
	
	public void visit(ScanOperator op) {
		//create logical plan
		try {
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH)) +
					"TableScan["+op.getTableName()+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void visit(IndexScanOperator op) {
		try {
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH))
					+ "IndexScan["+op.getTableName()+","+op.getIndexName()+","+op.getLowKey()+","+op.getHighKey()+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visit(SelectOperator op) {
		try {
			String ex = op.getEx() == null ? "" : op.getEx().toString();
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH))
					+ "Select["+ex+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		int tmp=counter;
		op.getChildOp().accept(this);
		counter=tmp;
	}

	public void visit(TNLJoinOperator op) {

	}
	
	public void visit(SMJoinOperator op) {
		String ex = op.getCondition() == null ? "" : op.getCondition().toString();
		try {
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH))+
					"SMJ"+"["+ex+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		int tmp=counter;
		System.out.println("SMJ before left" + counter);
		op.getLeftChild().accept(this);
		counter=tmp;
		op.getRightChild().accept(this);
		counter=tmp;
	}
	
	public void visit(BNLJoinOperator op) {
		String ex = op.getCondition() == null ? "" : op.getCondition().toString();
		try {
			physicalWriter.write(String.join("", Collections.nCopies(counter, DASH))+
					"BNLJ"+"["+ex+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		int tmp=counter;
		System.out.println(op.getLeftChild().toString());
		op.getLeftChild().accept(this);
		counter=tmp;
		op.getRightChild().accept(this);
		counter=tmp;
	}

	
	public void visit(InMemorySortOperator op) {

	}

}
