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
 * The operator gets two child operators and uses Block-Nested Loop Join to join
 * their tuples together
 * 
 * @author xinqi
 *
 */
public class BNLJoinOperator extends JoinOperator {
	private ArrayList<Tuple> buffer;
	private int buffer_size; // the number of “pages” to devote to each block of the outer relation
	private Tuple rightTuple;
	// private int index;
	// Iterator<Tuple> matchedTuples = new LinkedList<Tuple>().iterator();
	LinkedList<Tuple> matchedTuples = new LinkedList<Tuple>();

	/**
	 * @param left
	 *            the left operator to be joined
	 * @param right
	 *            the right operator to be joined
	 * @param condition
	 *            the Expression condition that filters the tuples
	 * @param size
	 *            the size of the buffer in pages
	 */
	public BNLJoinOperator(Operator left, Operator right, Expression condition, int size) {
		super(left, right, condition);
		buffer_size = size;
		buffer = new ArrayList<>();
		// index = 0;
		rightTuple = rightChild.getNextTuple();
	}

	/**
	 * Clear the previous elements in the buffer and fill it with new tuples from
	 * the outer relation
	 */
	public void updateBuffer() {
		buffer.clear();
		int numTuples = 4096 * buffer_size / (4 * leftChild.getColumnIndexMap().size());
		int i = 0;
		while (i < numTuples) {
			Tuple left = leftChild.getNextTuple();
			if (left == null)
				break;
			buffer.add(left);
			i++;
		}
	}

	/**
	 * this is a stateless lambda expression
	 * 
	 * @param tup
	 *            the left tuple to check condition
	 * @param right
	 *            the right tuple to check condition
	 * @return combinedTuple if two tuples matched, null otherwise
	 */
	private Tuple lambda(Tuple tup, Tuple right) {
		Tuple combinedTuple = tup.merge(right);
		if (joinCondition == null)
			return combinedTuple;
		EvaluateExpVisitor evaluateExpVisitor = new EvaluateExpVisitor();
		evaluateExpVisitor.setCurrTuple(combinedTuple);
		evaluateExpVisitor.setOperator(this);
		joinCondition.accept(evaluateExpVisitor);
		boolean result = evaluateExpVisitor.getReturnBoolValue();
		if (result) {
			return combinedTuple;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		if (buffer.isEmpty())
			updateBuffer();
		// while there is another right tuple
		if (!matchedTuples.isEmpty()) {
			return matchedTuples.pop();
		}
		while (!buffer.isEmpty()) {
			while (rightTuple != null) {
				if (!matchedTuples.isEmpty()) {
					return matchedTuples.pop();
				} else {

					matchedTuples = buffer.parallelStream()
							.map(t -> lambda(t, rightTuple))
							.filter(t -> t != null)
							.collect(Collectors.toCollection(LinkedList::new));
					// fetch the next right tuple
					rightTuple = rightChild.getNextTuple();
					if (!matchedTuples.isEmpty()) {
						return matchedTuples.pop();
					}
				}
			}
			// finish one block, so get another, and starts from the first right child again
			updateBuffer();
			rightChild.reset();
			rightTuple = rightChild.getNextTuple();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		matchedTuples.clear();
		buffer.clear();
		rightTuple = rightChild.getNextTuple();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see physicaloperator.Operator#accept(visitor.PhysicalPlanWriter)
	 */
	public void accept(PhysicalPlanWriter write) {
		write.visit(this);
	}
}
