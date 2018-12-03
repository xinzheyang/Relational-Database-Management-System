/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import database.Tuple;
import visitor.EvaluateExpVisitor;
import visitor.PhysicalPlanWriter;
import net.sf.jsqlparser.expression.Expression;

/**
 * The operator gets two child operators and uses Block-Nested Loop Join to
 * join their tuples together
 * @author xinqi
 *
 */
public class BNLJoinOperator extends JoinOperator {
	private ArrayList<Tuple> buffer;
	private int buffer_size; //	the number of “pages” to devote to each block of the outer relation
	private Tuple rightTuple;
//	private int index;
	List<Tuple> matchedTuples = new LinkedList<>();
	
	/**
	 * @param left the left operator to be joined
	 * @param right the right operator to be joined
	 * @param condition the Expression condition that filters the tuples
	 * @param size the size of the buffer in pages
	 */
	public BNLJoinOperator(Operator left, Operator right, Expression condition, int size) {
		super(left,right,condition);
		buffer_size = size;
		buffer = new ArrayList<>();
//		index = 0;
		rightTuple = rightChild.getNextTuple();
	}
	

	/**
	 * Clear the previous elements in the buffer and fill it with new tuples from the 
	 * outer relation
	 */
	public void updateBuffer() {
		buffer.clear();
		int numTuples = 4096 * buffer_size / (4 * leftChild.getColumnIndexMap().size());
		int i=0;
		while(i< numTuples) {
			Tuple left = leftChild.getNextTuple();
			if(left == null) break;
			buffer.add(left);
			i++;
		}
//		System.out.println("number of columns is " + leftChild.getColumnIndexMap().size());
//		System.out.println("size is " + buffer.size());
	}
	
	private Tuple lambda(Tuple tup, Tuple right) {
		Tuple combinedTuple = tup.merge(right);
		if(joinCondition == null) return combinedTuple;
		visitor.setCurrTuple(combinedTuple);
		visitor.setOperator(this);
		joinCondition.accept(visitor);
		boolean result = visitor.getReturnBoolValue();
		if(result) {
			return combinedTuple;
		} else {
			return null;
		}
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		if(buffer.isEmpty()) updateBuffer();
		//while there is another right tuple
		while(!buffer.isEmpty()) {
			while(rightTuple != null) {
				if (matchedTuples.iterator().hasNext()) {
					return matchedTuples.iterator().next();
				} else {
					//fetch the next right tuple
					rightTuple = rightChild.getNextTuple();
					matchedTuples = buffer.parallelStream().map(t -> lambda(t, rightTuple)).collect(Collectors.toList());
				}
				
			}
			//finish one block, so get another, and starts from the first right child again
			updateBuffer();
			rightChild.reset();
			rightTuple = rightChild.getNextTuple();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		buffer.clear();
		rightTuple = rightChild.getNextTuple();
	}
	
	/* (non-Javadoc)
	 * @see physicaloperator.Operator#accept(visitor.PhysicalPlanWriter)
	 */
	public void accept(PhysicalPlanWriter write) {
		write.visit(this);
	}

}
