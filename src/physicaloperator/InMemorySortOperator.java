/**
 * 
 */
package physicaloperator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import database.Tuple;

/**
 * @author xinqi
 * InMemorySortOperator gets all the tuples from its child operator, put them in a list,
 * and sort them according to the order passed in as the column names list.
 *
 */
public class InMemorySortOperator extends SortOperator {

	private List<Tuple> allTuples=new ArrayList<>();
	/**
	 * Initializes the sort operator
	 * @param op The parent operator
	 * @param cols array containing the names of the columns to sort by
	 */
	public InMemorySortOperator(Operator op, String[] cols) {
		super(op, cols);
		Tuple tuple;
		while((tuple = childOp.getNextTuple()) != null) {
			allTuples.add(tuple);
		}
		Collections.sort(allTuples, com);
		index=0;
	}
	
	public int getIndex() {
		return index;
	}
	/* (non-Javadoc)
	 * @see database.Operator#getNextTuple()
	 */
	@Override
	public Tuple getNextTuple() {
		if (index < allTuples.size()) {
			Tuple cur = allTuples.get(index++);
			return cur;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see database.Operator#reset()
	 */
	@Override
	public void reset() {
		childOp.reset();
		index=0;
	}

	@Override
	public void reset(int index) {
		// TODO Auto-generated method stub
		
	}

}
