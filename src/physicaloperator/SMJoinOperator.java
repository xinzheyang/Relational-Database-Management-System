/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;

import database.Tuple;
import net.sf.jsqlparser.expression.Expression;
import visitor.EquiConjunctVisitor;

/** Partition-based approach to avoid cross-producting. Merging phase follows the 
 * algorithm in textbook page 460. All join conditions assumed to be conjunctions of
 * equality conditions.
 * @author sitianchen
 *
 */
public class SMJoinOperator extends JoinOperator {
	private int jumpBackToPartition; //index of previous right relation's start of partition to jump back to
	private int currRightPosition;
	private Tuple tLeft; //initialized to first tuple in left relation
	private Tuple tRight; //initialized to first tuple in right relation
	private Tuple startOfCurrPartition; //start of current partition, initialized to first tuple in right relation

	public SMJoinOperator(Operator left, Operator right, Expression condition) {
		super(left, right, condition);
		assert(left instanceof SortOperator);
		assert(right instanceof SortOperator);
		jumpBackToPartition = 0;
		currRightPosition = 0;
		tLeft = leftChild.getNextTuple();
		tRight = rightChild.getNextTuple();
		startOfCurrPartition = tRight;
	}

//	public SMJoinOperator(Operator left, Operator right) {
//		this(left, right, null);
//	}

	/** Compare two tuples by the order of the equity conjunct condition.
	 * @param tl left tuple to be compared
	 * @param tr right tuple to be compared
	 * @return 0 if tl == tr, -1 if tl < tr, 1 if tl > tr
	 */
	public int compareByCondition(Tuple tl, Tuple tr) {
		assert tl != null;
		assert tr != null;
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
		if (index >= size && l == r){ return 0; }
		return (l > r) ? 1 : -1;
	}

	/** Gets the next tuple of the operator by looking for the next matching 
	 * right tuple in the current partition. Resets right operator to the
	 * start of current partition before reading the next left tuple.
	 * This implementation follows the algorithm in textbook page 460.
	 * @see physicaloperator.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {

		if (tLeft != null && tRight == null) {
			rightChild.reset(jumpBackToPartition);
			tRight = rightChild.getNextTuple();
			currRightPosition = jumpBackToPartition;
			tLeft = leftChild.getNextTuple();
		}
		
		if (tLeft == null) {
			return null;
		}
		
		if (compareByCondition(tLeft, tRight) == 0 && 
				compareByCondition(tLeft, startOfCurrPartition) == 0) {
			while(tRight != null && tLeft != null && compareByCondition(tLeft, tRight) == 0) {
				Tuple merged = tLeft.merge(tRight);
				tRight = rightChild.getNextTuple();
				currRightPosition++;
				return merged;
			}
		}
		
		if (compareByCondition(tLeft, startOfCurrPartition) == 0 && 
				compareByCondition(tLeft, tRight) != 0) {
			tLeft = leftChild.getNextTuple();
			
			while(tLeft != null && compareByCondition(tLeft, startOfCurrPartition) == 0) {
				rightChild.reset(jumpBackToPartition);
				tRight = rightChild.getNextTuple();
				currRightPosition = jumpBackToPartition;
				while(tLeft != null && compareByCondition(tLeft, tRight) == 0) {
					Tuple merged = tLeft.merge(tRight);
					tRight = rightChild.getNextTuple();
					currRightPosition++;
					return merged;
				}
				tLeft = leftChild.getNextTuple();
			}
			startOfCurrPartition = tRight;
			jumpBackToPartition = currRightPosition;
			
		}

		while(tLeft != null && tRight != null) {
			while(tLeft != null && startOfCurrPartition != null && compareByCondition(tLeft, startOfCurrPartition) == -1) {
				tLeft = leftChild.getNextTuple();
			}

			while(startOfCurrPartition != null && tLeft != null && compareByCondition(tLeft, startOfCurrPartition) == 1) {
				startOfCurrPartition = rightChild.getNextTuple();
				jumpBackToPartition++;
			}
			tRight = startOfCurrPartition;
			while(tLeft != null && startOfCurrPartition != null && compareByCondition(tLeft, startOfCurrPartition) == 0) {
				rightChild.reset(jumpBackToPartition);
				tRight = rightChild.getNextTuple();
				currRightPosition = jumpBackToPartition;
				while(tRight != null && tLeft != null && compareByCondition(tLeft, tRight) == 0) {
					Tuple merged = tLeft.merge(tRight);
					tRight = rightChild.getNextTuple();
					currRightPosition++;
					return merged;
				}
				tLeft = leftChild.getNextTuple();
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see physicaloperator.Operator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		tLeft = leftChild.getNextTuple();
		tRight = rightChild.getNextTuple();
		startOfCurrPartition = tRight;
	}


}
