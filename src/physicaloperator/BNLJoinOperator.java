/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;
import java.util.HashMap;

import database.Tuple;
import visitor.EvaluateExpVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * The operator gets two child operators and join their tuples together
 * @author xinqi
 *
 */
public class BNLJoinOperator extends JoinOperator {
	private ArrayList<Tuple> buffer;
	private int buffer_size; //	the number of “pages” to devote to each block of the outer relation
	private Tuple rightTuple;
	private int index;
	/**
	 * Initializes the Join operator with condition given
	 * @param left the left operator to be joined
	 * @param right the right operator to be joined
	 * @param condition the Expression condition that filters the tuples
	 */
	public BNLJoinOperator(Operator left, Operator right, Expression condition, int size) {
//		leftChild = left;
//		rightChild = right;
//		
//		columnIndexMap = leftChild.getColumnIndexMap(); //same col index map as child operator
//		int leftSize = leftChild.getColumnIndexMap().size();
//		HashMap<String,Integer> rightMap = rightChild.getColumnIndexMap();
//		for(String s: rightMap.keySet()) {
//			columnIndexMap.put(s, rightMap.get(s)+leftSize);
//		}
//		joinCondition = condition;
//		leftTuple = leftChild.getNextTuple();
//		visitor = new EvaluateExpVisitor();
		super(left,right,condition);
		buffer_size = size;
		buffer = new ArrayList<>();
		index = 0;
		rightTuple = rightChild.getNextTuple();
	}
	
	/**
	 * Initializes the Join operator without condition given
	 * @param left the left operator to be joined
	 * @param right the right operator to be joined
	 */
	public BNLJoinOperator(Operator left, Operator right, int size) {
		this(left, right, null, size);
	}
	
	public void updateBuffer() {
		buffer.clear();
		int numTuples = 4096 * buffer_size / (4 * leftChild.getColumnIndexMap().size());
		int i=0;
		while(i< numTuples) {
			buffer.add(leftChild.getNextTuple());
			i++;
		}
	}
	

	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
//		while(leftTuple != null) {
//			Tuple rightTuple = rightChild.getNextTuple();
//			while (rightTuple != null) {
//				//combine the two tuples and check if they pass the result;
//				Tuple combinedTuple = leftTuple.merge(rightTuple);
//				if(joinCondition == null) return combinedTuple;
//				visitor.setCurrTuple(combinedTuple);
//				visitor.setOperator(this);
//				joinCondition.accept(visitor);
//				boolean result = visitor.getReturnBoolValue();
//				if(result) {
//					return combinedTuple;
//				}
//				rightTuple = rightChild.getNextTuple();
//			}
//			leftTuple = leftChild.getNextTuple();
//			rightChild.reset();
//		}
//		return null;
		
				
		if(buffer.isEmpty()) updateBuffer();
		//while there is another right tuple
		while(!buffer.isEmpty()) {
			while(rightTuple != null) {
				//while the end of the buffer is not reached
				while(index < buffer.size()) {
					//combine the two tuples and check if they pass the result;
					Tuple combinedTuple = buffer.get(index).merge(rightTuple);
					index++;
					if(joinCondition == null) return combinedTuple;
					visitor.setCurrTuple(combinedTuple);
					visitor.setOperator(this);
					joinCondition.accept(visitor);
					boolean result = visitor.getReturnBoolValue();
					if(result) {
						return combinedTuple;
					}
				}
				index = 0;
				//fetch the next right tuple
				rightTuple = rightChild.getNextTuple();
			}
			//finish one block, so get another, and starts from the first right child again
			updateBuffer();
			rightChild.reset();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		leftChild.reset();
		rightChild.reset();
		buffer.clear();
		rightTuple = rightChild.getNextTuple();
	}

	@Override
	public void reset(int index) {
		leftChild.reset();
		rightChild.reset();
		buffer.clear();
		rightTuple = rightChild.getNextTuple();
		
	}
}
