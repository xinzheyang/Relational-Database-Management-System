/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;

import database.Tuple;
import net.sf.jsqlparser.expression.Expression;
import visitor.EquiConjunctVisitor;

/** Partition-based approach to avoid cross-producting. Merging phase follows the 
 * algorithm in textbook page 460.
 * @author sitianchen
 *
 */
public class SMJoinOperator extends JoinOperator {
	private int jumpBackToPartition; //index of previous right relation's start of partition to jump back to
	private int tupleIndex;
	private ArrayList<Tuple> allTuples;
	
	public SMJoinOperator(Operator left, Operator right, Expression condition) {
		super(left, right, condition);
		assert(left instanceof SortOperator);
		assert(right instanceof SortOperator);
		tupleIndex = 0;
		jumpBackToPartition = 0;
		allTuples = new ArrayList<Tuple>();
	}
	
	/** Compare two tuples by the order of the equity conjunct condition.
	 * @param tl left tuple to be compared
	 * @param tr right tuple to be compared
	 * @return 0 if tl == tr, -1 if tl < tr, 1 if tl > tr
	 */
	public int compareByCondition(Tuple tl, Tuple tr) {
		EquiConjunctVisitor equiVisit = new EquiConjunctVisitor(tl, tr, leftChild, rightChild);
		this.joinCondition.accept(equiVisit);
		ArrayList<Integer> leftVals = equiVisit.getLeftCompareVals();
		ArrayList<Integer> rightVals = equiVisit.getRightCompareVals();
		int index = 0;
		int size = leftVals.size();
		int l = leftVals.get(index);
		int r = rightVals.get(index);
		while(l == r && index < size) {
			l = leftVals.get(index);
			r = rightVals.get(index++);
		}
		if (index >= size){ return 0; }
		return (l > r) ? 1 : -1;
	}
	
	/** Gets all tuples with the joinCondition.
	 * @return all tuples
	 */
	public void getAllTuples() {
		
//		ArrayList<Tuple> allTuples = new ArrayList<Tuple>();
		Tuple tLeft = leftChild.getNextTuple(); //initialized to first tuple in left relation
		Tuple tRight = rightChild.getNextTuple(); //initialized to first tuple in right relation
		Tuple startOfCurrPartition = tRight; //start of current partition, initialized to first tuple in right relation
		while(tLeft != null && tRight != null) {
			while(compareByCondition(tLeft, startOfCurrPartition) == -1) {
				tLeft = leftChild.getNextTuple();
			}
			
			while(compareByCondition(tLeft, startOfCurrPartition) == 1) {
				startOfCurrPartition = rightChild.getNextTuple();
				jumpBackToPartition++;
			}
			tRight = startOfCurrPartition;
			while(compareByCondition(tLeft, startOfCurrPartition) == 0) {
//				tRight = startOfCurrPartition;
				rightChild.reset(jumpBackToPartition);
				while(compareByCondition(tRight, tLeft) == 0) {
					allTuples.add(tLeft.merge(tRight));
					tRight = rightChild.getNextTuple();
				}
				tLeft = leftChild.getNextTuple();
			}
			startOfCurrPartition = tRight;
		}
		
//		return allTuples;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
//		Tuple tLeft = leftChild.getNextTuple(); //initialized to first tuple in left relation
//		Tuple tRight = rightChild.getNextTuple(); //initialized to first tuple in right relation
//		Tuple startOfCurrPartition = tRight; //start of current partition, initialized to first tuple in right relation
//		while(tLeft != null && tRight != null) {
//			while(compareByCondition(tLeft, startOfCurrPartition) == -1) {
//				tLeft = leftChild.getNextTuple();
//			}
//			
//			while(compareByCondition(tLeft, startOfCurrPartition) == 1) {
//				startOfCurrPartition = rightChild.getNextTuple();
//				jumpBackToPartition++;
//			}
//			tRight = startOfCurrPartition;
//			while(compareByCondition(tLeft, startOfCurrPartition) == 0) {
//				tRight = startOfCurrPartition;
//				while(compareByCondition(tRight, tLeft) == 0) {
//					
//				}
//			}
//		}
		if (tupleIndex < allTuples.size()) return allTuples.get(tupleIndex++);
		return null;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#reset()
	 */
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

}
