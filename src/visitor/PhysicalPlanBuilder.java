/**
 *
 */
package visitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import database.DBCatalog;
import datastructure.UnionElement;
import joinorderoptimizer.JoinOrderOptimizer;
import logicaloperator.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import physicaloperator.*;

/**
 * @author xinzheyang A physical plan builder that uses visitor pattern to
 *         transform a logical operator into a physical operator
 */
public class PhysicalPlanBuilder {
	private final String DASH="-";
	Operator operator;
	BufferedWriter logicalWriter; //the BufferWriter to write the logical plan
	int counter=0; //the counter to record the number of dashes
	int tmp; //temporary plcaeholder to keep track of counter
	
	public PhysicalPlanBuilder(BufferedWriter logicalPlan) throws IOException {
		//		logicalWriter = new BufferedWriter(new FileWriter("output" + File.separator + "query2" + "_logicalplan"));
		//		logicalWriter.write("fuck");
		logicalWriter=logicalPlan;
	}

	/**
	 * Constructs the sort operator according to the PPB configuration.
	 *
	 * @param child:
	 *            child operator to be sorted
	 * @param cols:
	 *            column order for sorting
	 * @return the right sort operator constructed
	 */
	private SortOperator getSortOperator(Operator child, String[] cols) {
		SortOperator sortOperator=new ExternalSortOperator(child, cols, 10); //hard code buffer size to 10
		//		if (DBCatalog.getSortMethod().equals("0")) {
		//			sortOperator = new InMemorySortOperator(child, cols);
		//		} else {
		//			assert DBCatalog.getSortMethod().equals("1");
		//			sortOperator = new ExternalSortOperator(child, cols, DBCatalog.getSortBufferSize());
		//		}

		return sortOperator;
	}

	public void visit(LogicalDupElimOperator op) {
		//create logical plan
		try {
			logicalWriter.write("DupElim\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		counter++;

		op.getChildOp().accept(this);
		DupElimOperator dupElimOperator = new DupElimOperator(operator);
		operator = dupElimOperator;
	}

	public void visit(LogicalSortOperator op) {
		//create logical plan
		try {
			logicalWriter.write(String.join("", Collections.nCopies(counter, DASH))
					+ "Sort"+"["+String.join(", ", op.getCols())+"]\n");

			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}

		op.getChildOp().accept(this);
		SortOperator sortOperator = getSortOperator(operator, op.getCols());
		operator = sortOperator;
	}

	public void visit(LogicalProjectOperator op) {
		//create logical plan
		try {
			logicalWriter.write(String.join("", Collections.nCopies(counter, DASH))+
					"Project"+"["+String.join(", ", op.getCols())+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}

		op.getChildOp().accept(this);
		ProjectOperator projectOperator = new ProjectOperator(operator, op.getCols());
		operator = projectOperator;
	}

	private String getScanOrSelectRef(Operator op) {
		String ref;
		if (op instanceof ScanOperator) {
			ref = ((ScanOperator) op).getReference();
		} else {
			ref = ((SelectOperator) op).getReference();
		}
		return ref;
	}
	
	private String getLogicalScanOrSelectRef(LogicalOperator op) {
		String ref;
		if (op instanceof LogicalScanOperator) {
			ref = ((LogicalScanOperator) op).getReference();
		} else {
			ref = ((LogicalSelectOperator) op).getReference();
		}
		return ref;
	}

	/** Creates a SMJ operator from the left and right operator along with the join condition.
	 * @param left
	 * @param right
	 * @param cond
	 * @return
	 */
	private SMJoinOperator createSMJ(Operator left, Operator right, Expression cond) {
		EquiConjunctVisitor equiVisit = new EquiConjunctVisitor(left, right);
		cond.accept(equiVisit);

		Object[] sortLeftObj = equiVisit.getLeftCompareCols().toArray();
		Object[] sortRightObj = equiVisit.getRightCompareCols().toArray();
		String[] sortOrderLeft = Arrays.copyOf(sortLeftObj, sortLeftObj.length, String[].class);
		String[] sortOrderRight = Arrays.copyOf(sortRightObj, sortRightObj.length, String[].class);
		// push down left and right sort operator to sort relations before merging
		SortOperator leftSort = getSortOperator(left, sortOrderLeft);
		SortOperator rightSort = getSortOperator(right, sortOrderRight);
		return new SMJoinOperator(leftSort, rightSort, cond);
	}

	/** Creates a BNLJ operator from the left and right operator along with the join condition.
	 * @param left
	 * @param right
	 * @param cond
	 * @return
	 */
	private BNLJoinOperator createBNLJ(Operator left, Operator right, Expression cond) {
		String ex = cond == null ? "" : cond.toString();
		return new BNLJoinOperator(left, right, cond, 10); //use 10 as join size
	}

	/** Creates a join operator by choosing the better join by following the principle of always using SMJ for
	 * joins with all equality conditions and BNLJ otherwise.
	 * @param left
	 * @param right
	 * @param cond
	 * @param checkEquity
	 * @return
	 */
	private JoinOperator createBestJoin(Operator left, Operator right, Expression cond, CheckAllEquityExpVisitor checkEquity) {
		JoinOperator op;
		if (cond != null) {
			cond.accept(checkEquity);
			if (checkEquity.isAllEquity()) {
				op = createSMJ(left, right, cond);
			}
			else {
				op = createBNLJ(left, right, cond);
			}
		} else { //condition null, default to use BNLJ
			op = createBNLJ(left, right, cond);
		}
		return op;
	}
	
	public Operator findPhysOperator(LogicalOperator op, List<Operator> lst) {
		String opRef = getLogicalScanOrSelectRef(op);
		for (Operator phys : lst) {
			if (this.getScanOrSelectRef(phys).equals(opRef))
				return phys;
		}
		return null; //not found
	}

	/**
	 * @param op
	 *            the logicalJoinOperator it consults the config and determines
	 *            which physical Join operators to construct
	 */
	public void visit(LogicalJoinOperator op) {
		//create logical plan
		try {
			String joinCond = op.getJoinCondition() == null ? "" : op.getJoinCondition().toString();
			logicalWriter.write(String.join("", Collections.nCopies(counter, DASH))+"Join"
					+"["+joinCond+"]\n");
			for(UnionElement elt: op.getUnionElements()) {
				logicalWriter.write("[["+String.join(", ", elt.getAttributeStrings())+"], equals "+
						elt.getEquality()+", min "+elt.getLower()+", max "+elt.getUpper()+"]\n");
			}
			counter++;
			//			beforeSelect=counter;
			//			for (LogicalOperator child : op.getJoinChildren()) {
			//				child.accept(this);
			//			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		// refactor PPB to left deep join tree
		JoinOrderOptimizer opt = new JoinOrderOptimizer(op, op.getVisitor());
		ParseConjunctExpVisitor visitor = op.getVisitor();
		opt.dpChooseBestPlan();
		List<LogicalOperator> optOrder = opt.getBestOrder(); //best join order
		List<Operator> physChildren = new LinkedList<Operator>(); //physical children
		for (LogicalOperator child : op.getJoinChildren()) { //serialize logical children in order by accepting
			tmp=counter; 
			child.accept(this);
			counter=tmp;
			Operator childPhys = operator;
			physChildren.add(childPhys);
		}
		List<Operator> physOptOrder = new LinkedList<Operator>();
		for (LogicalOperator logChild : optOrder) { //build physical operator order from logical operator order
			physOptOrder.add(findPhysOperator(logChild, physChildren));
		}
		
		//use parse conjunct visitor to build left deep join tree
		assert physOptOrder.size() > 1;
		JoinOperator left;
//		tmp=counter; 
//		optOrder.get(0).accept(this);
//		counter=tmp;
//		Operator firstLeft = operator;
//		optOrder.get(1).accept(this);
//		counter=tmp;
//		Operator secondLeft = operator;
		Operator firstLeft = physOptOrder.get(0);
		Operator secondLeft = physOptOrder.get(1);

		List<String> leftTableRefs = new LinkedList<String>();
		leftTableRefs.add(getScanOrSelectRef(firstLeft));
		leftTableRefs.add(getScanOrSelectRef(secondLeft));

		Expression firstCond = visitor == null ? null : visitor.getJoinCondition(leftTableRefs.get(0), leftTableRefs.get(1));
		CheckAllEquityExpVisitor checkEquity = new CheckAllEquityExpVisitor();

		left = createBestJoin(firstLeft, secondLeft, firstCond, checkEquity);

		for (int i = 2; i < physOptOrder.size(); i++) {
			CheckAllEquityExpVisitor checkEquityIn = new CheckAllEquityExpVisitor();
			Expression condition = null;
			Operator currRight = physOptOrder.get(i);
//			LogicalOperator currRight = optOrder.get(i);
//			currRight.accept(this);
//			counter=tmp;
			String currRef = getScanOrSelectRef(currRight);
			for (String leftRef: leftTableRefs) {
				Expression tempCondition = visitor != null ? visitor.getJoinCondition(leftRef, currRef) : null;
				if (tempCondition != null)
					condition = condition == null ? tempCondition: new AndExpression(condition, tempCondition);
			}
			leftTableRefs.add(currRef);
			left = createBestJoin(left, currRight, condition, checkEquityIn);
		}
		//		op.getLeftChild().accept(this);
		//		Operator left = operator;
		//		op.getRightChild().accept(this);
		//		Operator right = operator;
		//		JoinOperator joinOperator;
		//		if (DBCatalog.getJoinMethod().equals("0")) { // TNLJ
		//			joinOperator = new TNLJoinOperator(left, right, op.getJoinCondition());
		//		} else {
		//			if (DBCatalog.getJoinMethod().equals("1")) { // BNLJ
		//				int bufferSize = DBCatalog.getJoinBufferSize();
		//				joinOperator = new BNLJoinOperator(left, right, op.getJoinCondition(), bufferSize);
		//			} else if (DBCatalog.getJoinMethod().equals("2")) { // SMJ
		//				Expression smjCondition = op.getJoinCondition();
		//				EquiConjunctVisitor equiVisit = new EquiConjunctVisitor();
		//				/*
		//				 * by accepting, the visitor processes the join condition and extract left and
		//				 * right column names in the sorting order.
		//				 */
		//				smjCondition.accept(equiVisit);
		//				Object[] sortLeftObj = equiVisit.getLeftCompareCols().toArray();
		//				Object[] sortRightObj = equiVisit.getRightCompareCols().toArray();
		//
		//				String[] sortOrderLeft = Arrays.copyOf(sortLeftObj, sortLeftObj.length, String[].class);
		//				String[] sortOrderRight = Arrays.copyOf(sortRightObj, sortRightObj.length, String[].class);
		//				// push down left and right sort operator to sort relations before merging
		//				SortOperator leftSort = getSortOperator(left, sortOrderLeft);
		//				SortOperator rightSort = getSortOperator(right, sortOrderRight);
		//				joinOperator = new SMJoinOperator(leftSort, rightSort, smjCondition);
		//			} else { // invalid input, default to TNLJ
		//				joinOperator = new TNLJoinOperator(left, right, op.getJoinCondition());
		//			}
		//		}
		operator = left;
	}

	/**
	 * @param op
	 *            the logicaiSelectOperator. the method uses a DivideSelectVisitor
	 *            to determine whether to use index scan operator and it's lower and
	 *            upper bounds, if any.
	 */
	public void visit(LogicalSelectOperator op) {

		//create logical plan
		try {
			String ex = op.getEx() == null ? "" : op.getEx().toString();
			logicalWriter.write(String.join("", Collections.nCopies(counter, DASH))
					+ "Select["+ex+"]\n");
			counter++;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		LogicalOperator child = op.getChildOp();
		LogicalScanOperator scanChild = (LogicalScanOperator) child;
		String tableName = scanChild.getTableName();
		DivideSelectVisitor visitor = null;

		//calculate the cost of using scan and different indices, 
		//and choose the best choice for selection
		
		//the number of pages in the relation
		int p = DBCatalog.getRelationSize(tableName)*(DBCatalog.getTableColumns(tableName).length * 4)/4096;
		int t = DBCatalog.getRelationSize(tableName); //the number of tuples
		int minIndexCost=Integer.MAX_VALUE;
		String minIndex=null;
		int isMinClustered=0;
		int[] range = new int[2];
		DivideSelectVisitor minVisitor=null;
		List<String[]> infos = DBCatalog.getIndexInfo(tableName);
		if (infos != null) {
			for(String[] info: infos) {
				//compute the index scan cost for each possible index
				String index= info[0];//A
				visitor = new DivideSelectVisitor(index);
				op.getEx().accept(visitor);
				int[] bounds = DBCatalog.getAttribBounds(tableName).get(index);
				int indexLow = Math.max(visitor.getLowKey(), bounds[0]);
				int indexHigh = Math.min(visitor.getHighKey(), bounds[1]);
				//			int r = (indexHigh-indexLow+1)/(bounds[1]-bounds[0]+1); //the reduction factor
				double r = op.getReductionFactor(index); //the reduction factor of this index
				int l = DBCatalog.getNumOfLeaves(tableName+"."+index); //the number of leaves in the index
				int costTraversal = DBCatalog.getTraversalCost(tableName+"."+index);
				int cost;
				if(info[1] == "0") { //unclustered
					cost = (int)Math.ceil(costTraversal + l*r + t*r);
				}
				else {
					cost = (int)Math.ceil(costTraversal + p*r);
				}
				if(cost < minIndexCost) {
					minIndexCost = cost;
					minIndex = index;
					isMinClustered = Integer.parseInt(info[1]);
					minVisitor = visitor;
					range[0]=indexLow;
					range[1]=indexHigh;
				}
			}
		}
		if(minIndex != null && minIndexCost<p) {
			//			visitor = new DivideSelectVisitor(minIndex);
			//			op.getEx().accept(visitor);
			ScanOperator indexScanOp;
			try {
				indexScanOp = new IndexScanOperator(tableName, scanChild.getAlias(),
						DBCatalog.getIndexFileLoc(tableName, minIndex), minIndex, isMinClustered,
						range[0], range[1]);
				if (minVisitor.getNormalSelect() == null) {
					operator = indexScanOp;
				} else {
					SelectOperator selectOperator = new SelectOperator(indexScanOp, minVisitor.getNormalSelect());
					operator = selectOperator;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}


		}
		else {
			child.accept(this);
			SelectOperator selectOperator = new SelectOperator(operator, op.getEx());
			operator = selectOperator;
		}

		//		if (DBCatalog.useIndex()) {
		//			visitor = new DivideSelectVisitor(DBCatalog.getIndexKey(tableName));
		//			op.getEx().accept(visitor);
		//		}
		//
		//		if (child instanceof LogicalScanOperator && DBCatalog.useIndex() && visitor != null
		//				&& visitor.needIndexScan()) {
		//			try {
		//				ScanOperator indexScanOp = new IndexScanOperator(tableName, scanChild.getAlias(),
		//						DBCatalog.getIndexFileLoc(tableName), DBCatalog.getIndexKey(tableName),
		//						DBCatalog.hasClusteredIndex(tableName), visitor.getLowKey(), visitor.getHighKey());
		//
		//				if (visitor.getNormalSelect() == null) {
		//					operator = indexScanOp;
		//				} else {
		//					SelectOperator selectOperator = new SelectOperator(indexScanOp, visitor.getNormalSelect());
		//					operator = selectOperator;
		//				}
		//			} catch (FileNotFoundException e) {
		//				System.err.println("error occurred during construcuting IndexScanOp");
		//				e.printStackTrace();
		//			}
		//
		//		} else {
		//			child.accept(this);
		//			SelectOperator selectOperator = new SelectOperator(operator, op.getEx());
		//			operator = selectOperator;
		//		}
	}

	public void visit(LogicalScanOperator op) {

		//create logical plan
		try {
			logicalWriter.write(String.join("", Collections.nCopies(counter, DASH)) +
					"Leaf["+op.getTableName()+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

		ScanOperator scanOperator;
		try {
			scanOperator = new ScanOperator(op.getTableName(), op.getAlias());
			operator = scanOperator;
		} catch (FileNotFoundException e) {
			System.err.println("error occurred during building scan operator, file not found");
			e.printStackTrace();
		}


	}

	/**
	 * @return operator
	 */
	public Operator getOperator() {
		return operator;
	}
}
