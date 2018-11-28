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
	Operator operator;
	BufferedWriter logicalWriter;

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
		SortOperator sortOperator=new ExternalSortOperator(child, cols, DBCatalog.getSortBufferSize());
//		if (DBCatalog.getSortMethod().equals("0")) {
//			sortOperator = new InMemorySortOperator(child, cols);
//		} else {
//			assert DBCatalog.getSortMethod().equals("1");
//			sortOperator = new ExternalSortOperator(child, cols, DBCatalog.getSortBufferSize());
//		}

		return sortOperator;
	}

	public void visit(LogicalDupElimOperator op) {
		op.getChildOp().accept(this);
		DupElimOperator dupElimOperator = new DupElimOperator(operator);
		operator = dupElimOperator;
		//create logical plan
		try {
			logicalWriter.write("DupElim");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visit(LogicalSortOperator op) {
		op.getChildOp().accept(this);
		SortOperator sortOperator = getSortOperator(operator, op.getCols());
		operator = sortOperator;
		
		//create logical plan
		try {
			logicalWriter.write("Sort"+"["+String.join(", ", op.getCols())+"]");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void visit(LogicalProjectOperator op) {
		op.getChildOp().accept(this);
		ProjectOperator projectOperator = new ProjectOperator(operator, op.getCols());
		operator = projectOperator;
		
		//create logical plan
		try {
			logicalWriter.write("Project"+"["+String.join(", ", op.getCols())+"]");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	/** Creates a SMJ operator from the left and right operator along with the join condition.
	 * @param left
	 * @param right
	 * @param cond
	 * @return
	 */
	private SMJoinOperator createSMJ(Operator left, Operator right, Expression cond) {
		EquiConjunctVisitor equiVisit = new EquiConjunctVisitor();
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

	/**
	 * @param op
	 *            the logicalJoinOperator it consults the config and determines
	 *            which physical Join operators to construct
	 */
	public void visit(LogicalJoinOperator op) {
		// refactor PPB to left deep join tree
		JoinOrderOptimizer opt = new JoinOrderOptimizer(op, op.getVisitor());
		ParseConjunctExpVisitor visitor = op.getVisitor();
		opt.dpChooseBestPlan();
		List<LogicalOperator> optOrder = opt.getBestOrder(); //best join order
		//use parse conjunct visitor to build left deep join tree
		assert optOrder.size() > 1;
		JoinOperator left;
		optOrder.get(0).accept(this);
		Operator firstLeft = operator;
		optOrder.get(1).accept(this);
		Operator secondLeft = operator;
		
		List<String> leftTableRefs = new LinkedList<String>();
		leftTableRefs.add(getScanOrSelectRef(firstLeft));
		leftTableRefs.add(getScanOrSelectRef(secondLeft));
		
		Expression firstCond = visitor == null ? null : visitor.getJoinCondition(leftTableRefs.get(0), leftTableRefs.get(1));
		CheckAllEquityExpVisitor checkEquity = new CheckAllEquityExpVisitor();

		left = createBestJoin(firstLeft, secondLeft, firstCond, checkEquity);
		
		for (int i = 2; i < optOrder.size(); i++) {
			Expression condition = null;
			LogicalOperator currRight = optOrder.get(i);
			currRight.accept(this);
			String currRef = getScanOrSelectRef(operator);
			for (String leftRef: leftTableRefs) {
				Expression tempCondition = visitor != null ? visitor.getJoinCondition(leftRef, currRef) : null;
				if (tempCondition != null)
					condition = condition == null ? tempCondition: new AndExpression(condition, tempCondition);
			}
			leftTableRefs.add(currRef);
			left = createBestJoin(left, operator, condition, checkEquity);
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
		
		//create logical plan
		try {
			logicalWriter.write("Join"+"["+String.join(", ", op.getJoinCondition().toString())+"]");
			for(UnionElement elt: op.getUnionElements()) {
				logicalWriter.write("["+String.join(", ", elt.getAttributeStrings())+"], equals "+
						elt.getEquality()+", min "+elt.getLower()+", max "+elt.getUpper());
			}
			for (LogicalOperator child : op.getJoinChildren()) {
				child.accept(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			logicalWriter.write("Select["+op.getEx().toString()+"]\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogicalOperator child = op.getChildOp();
		LogicalScanOperator scanChild = (LogicalScanOperator) child;
		String tableName = scanChild.getTableName();
		DivideSelectVisitor visitor = null;
		
		//the number of pages in the relation
		int p = DBCatalog.getRelationSize(tableName)*(DBCatalog.getTableColumns(tableName).length * 4)/4096;
		int t = DBCatalog.getRelationSize(tableName); //the number of tuples
		int minIndexCost=Integer.MAX_VALUE;
		String minIndex=null;
		int isMinClustered=0;
		DivideSelectVisitor minVisitor=null;
		List<String[]> infos = DBCatalog.getIndexInfo(tableName);
		for(String[] info: infos) {
			//compute the index scan cost for each possible index
			String index= info[0];//A
//			visitor = new DivideSelectVisitor(index);
//			op.getEx().accept(visitor);
//			int[] bounds = DBCatalog.getAttribBounds(tableName).get(index);
//			int indexLow = Math.max(visitor.getLowKey(), bounds[0]);
//			int indexHigh = Math.min(visitor.getHighKey(), bounds[1]);
//			int r = (indexHigh-indexLow+1)/(bounds[1]-bounds[0]+1); //the reduction factor
			int r = op.getReductionFactor(index); //the reduction factor of this index
			int l = DBCatalog.getNumOfLeaves(tableName+"."+index); //the number of leaves in the index
			int costTraversal = DBCatalog.getTraversalCost(tableName+"."+index);
			int cost;
			if(info[1] == "0") { //unclustered
				cost = costTraversal + l*r + t*r;
			}
			else {
				cost = costTraversal + p*r;
			}
			if(cost < minIndexCost) {
				minIndexCost = cost;
				minIndex = index;
				isMinClustered = Integer.parseInt(info[1]);
				minVisitor = visitor;
			}
		}
		if(minIndex != null && minIndexCost<p) {
//			visitor = new DivideSelectVisitor(minIndex);
//			op.getEx().accept(visitor);
			ScanOperator indexScanOp;
			try {
				indexScanOp = new IndexScanOperator(tableName, scanChild.getAlias(),
				DBCatalog.getIndexFileLoc(tableName, minIndex), minIndex, isMinClustered, 
				minVisitor.getLowKey(), minVisitor.getHighKey());
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
			logicalWriter.write("Leaf["+op.getTableName()+"]\n");
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
