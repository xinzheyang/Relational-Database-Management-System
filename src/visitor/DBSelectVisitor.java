/**
 * 
 */
package visitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.Union;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import datastructure.UnionElement;
import datastructure.UnionFind;
import logicaloperator.*;

/**
 * @author xinzheyang
 * An important visitor that builds the query plan for the database.
 * It uses JSqlParser to extract all the elements and then builds the
 * query plan in a bottom-up manner.
 */
public class DBSelectVisitor implements SelectVisitor {
	private LogicalOperator operator = null;
	private LogicalScanOperator scanOperator;
	private LogicalSelectOperator selectOperator;
	private LogicalJoinOperator joinOperator;
	private LogicalProjectOperator projectOperator;
	private LogicalSortOperator sortOperator;
	private LogicalDupElimOperator dupElimOperator;

	private ParseConjunctExpVisitor parseConjunctExpVisitor;
	private HashMap<String, Expression> selectMap;
	public LogicalOperator getOperator() {
		return operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}

	/**
	 * @param fromItem
	 * @return an scanOperator that builds from an FromItem
	 */
	private LogicalScanOperator buildScanFromItem(FromItem fromItem) {
		DBFromItemVisitor dbFromItemVisitor = new DBFromItemVisitor();
		fromItem.accept(dbFromItemVisitor);
		scanOperator = (LogicalScanOperator) dbFromItemVisitor.getOperator();
		return scanOperator;
	}

	/**
	 * @param scanOperator
	 * @return an selectOperator that builds from a scanOperator
	 */
	private LogicalSelectOperator buildSelectFromScan(LogicalScanOperator scanOperator) {
		LogicalSelectOperator selectOp = null;
		if (selectMap != null && selectMap.size() > 0) {
			String tableName = scanOperator.getTableName();
			if (scanOperator.getAlias() != null) {
				tableName = scanOperator.getAlias();
			}
			if (selectMap.containsKey(tableName)) {
				selectOp = new LogicalSelectOperator(scanOperator, selectMap.get(tableName));
			}
		}
		return selectOp;
	}
	

	/**
	 * @param fromItem
	 * @return builds a selectOperator from FromItem if possible, otherwise a scanOperator
	 */
	private LogicalOperator buildScanSelectFromItem(FromItem fromItem) {
		LogicalScanOperator scanOperator = buildScanFromItem(fromItem);
		LogicalSelectOperator selectOperator = buildSelectFromScan(scanOperator);
		if (selectOperator == null) {
			return scanOperator;
		} else {
			return selectOperator;
		}
	}


	/**
	 * @param orderByElements
	 * @return a sortOperator built from a list of OrderByElements, which follows the query plan structure
	 */
	private LogicalSortOperator buildSort(List<OrderByElement> orderByElements) {
		LogicalSortOperator sortOperator = null;
		String[] cols = new String[orderByElements.size()];
		for (int i=0; i<orderByElements.size(); i++) {
			cols[i] = orderByElements.get(i).toString();
		}
		if (projectOperator != null) {
			sortOperator = new LogicalSortOperator(projectOperator, cols);
		} else if (joinOperator != null) {
			sortOperator = new LogicalSortOperator(joinOperator, cols);
		} else if (selectOperator != null) {
			sortOperator = new LogicalSortOperator(selectOperator, cols);
		} else if (scanOperator != null) {
			sortOperator = new LogicalSortOperator(scanOperator, cols);
		}
		return sortOperator;
	}


	/* (non-Javadoc)
	 * This is the visit method that overrides SelectVisitor for PlainSelect type
	 * It basically parses all the SQL elements and builds the query from bottom up
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void visit(PlainSelect plainSelect) {
		Distinct distinct = plainSelect.getDistinct();
		FromItem fromItem = plainSelect.getFromItem();
		List<SelectItem> selectItems = plainSelect.getSelectItems();
		List<Join> joins = plainSelect.getJoins();
		List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
		Expression expression = plainSelect.getWhere();

		if (expression != null) {
			parseConjunctExpVisitor = new ParseConjunctExpVisitor();
			expression.accept(parseConjunctExpVisitor);
			if (parseConjunctExpVisitor.isAlwaysFalse()) { //there exists at least one constant conjunction
				//that always evaluates to false, and we know select or join wouldn't output any tuples
				return; //simply return -> null on this.operator
			}
			// SELECT * FROM A WHERE A.sid  A:A.sid = 1
			selectMap = parseConjunctExpVisitor.getSelectMap();
		}


		scanOperator = buildScanFromItem(fromItem);

		selectOperator = buildSelectFromScan(scanOperator);


		if (joins != null && joins.size() > 0) {
//			LogicalJoinOperator left;
//			FromItem firstRightItem = joins.get(0).getRightItem();
//			ArrayList<FromItem> leftTable = new ArrayList<>();
//			LogicalOperator initLeftOp = selectOperator == null ? scanOperator : selectOperator;
//
//			String fromItemReference = fromItem.getAlias() != null ? fromItem.getAlias() : fromItem.toString();
//			String fromRightItemReference = firstRightItem.getAlias() != null ? firstRightItem.getAlias() : firstRightItem.toString();
//			
//			Expression conditionFirst = parseConjunctExpVisitor != null ? 
//					parseConjunctExpVisitor.getJoinCondition(fromItemReference, fromRightItemReference) : null;
//			left = new LogicalJoinOperator(initLeftOp, buildScanSelectFromItem(firstRightItem), conditionFirst);
//
//			leftTable.add(fromItem);
//			leftTable.add(firstRightItem);
//
//			for (int i=1; i<joins.size(); i++) {
//				Expression condition = null;
//				FromItem rightItem = joins.get(i).getRightItem();
//				LogicalOperator newScanSelect = buildScanSelectFromItem(rightItem);
//				for (FromItem table:leftTable) {
//					String tableItemReference = table.getAlias() != null ? table.getAlias() : table.toString();
//					String rightItemReference = rightItem.getAlias() != null ? rightItem.getAlias() : rightItem.toString();
//					Expression tempCondition = parseConjunctExpVisitor != null ? 
//							parseConjunctExpVisitor.getJoinCondition(tableItemReference, rightItemReference) : null;
//					if (tempCondition != null)
//						condition = condition == null ? tempCondition: new AndExpression(condition, tempCondition);
//				}
//				leftTable.add(rightItem);
//				left = new LogicalJoinOperator(left, newScanSelect, condition);
//			}
//			joinOperator = left;
//			
			
			UnionFindVisitor unionFindVisitor = new UnionFindVisitor();
			if (expression != null) {
				expression.accept(unionFindVisitor);
			}
			
			
			UnionFind uFind = unionFindVisitor.getUnionFind();
			Expression normalSelect = unionFindVisitor.getNormalSelect();
			Expression normalJoin = unionFindVisitor.getNormalJoin();
			
//			private LogicalSelectOperator buildSelectFromScan(LogicalScanOperator scanOperator) {
//				LogicalSelectOperator selectOp = null;
//				if (selectMap != null && selectMap.size() > 0) {
//					String tableName = scanOperator.getTableName();
//					if (scanOperator.getAlias() != null) {
//						tableName = scanOperator.getAlias();
//					}
//					if (selectMap.containsKey(tableName)) {
//						selectOp = new LogicalSelectOperator(scanOperator, selectMap.get(tableName));
//					}
//				}
//				return selectOp;
//			}
			
			
			
			LogicalSelectOperator ufBuildSelectFromScan(LogicalScanOperator scanOperator) {
				LogicalSelectOperator selectOp = null;
				String tableReference = scanOperator.getAlias() != null ? scanOperator.getAlias() : scanOperator.getTableName();
				
				
//				HashMap<String, Expression> ufSelectMap;
//				if (normalSelect != null) {
//					ParseConjunctExpVisitor ufPCEVisitor = new ParseConjunctExpVisitor();
//					normalSelect.accept(ufPCEVisitor);
//					if (ufPCEVisitor.isAlwaysFalse()) { 
//						return; //simply return -> null on this.operator
//					}
//					ufSelectMap = ufPCEVisitor.getSelectMap();
//				}
				
				// process unused normal select
				AndExpression tempCondition = null;
				if (selectMap != null && selectMap.size() > 0) {
					if (selectMap.containsKey(tableReference)) {
						tempCondition = new AndExpression(selectMap.get(tableReference), tempCondition);
					}
				}
				
				// process union-find
				HashSet<Expression> set = new HashSet<>();
				set.add(tempCondition);
				List<Column> cols = unionFindVisitor.getEqJoinAttrByReference(tableReference); // the attributes of this table
				for (Column col : cols) {
					UnionElement uElement = uFind.find(col.getWholeColumnName());
//					Column attrColumn = new Col
					if (uElement.getEquality() != null) {
						Expression eq = new EqualsTo(col, new LongValue(uElement.getEquality()));
						tempCondition = new AndExpression(eq, tempCondition);
					}
					if (uElement.getLower() != null) {
						Expression geq = new GreaterThanEquals(col, new LongValue(uElement.getLower()));
						tempCondition = new AndExpression(geq, tempCondition);
					}
					if (uElement.getUpper() != null) {
						Expression leq = new MinorThanEquals(col, new LongValue(uElement.getUpper()));
						tempCondition = new AndExpression(leq, tempCondition);
					}
					
				}
			}
			
//			/**
//			 * @param fromItem
//			 * @return builds a selectOperator from FromItem if possible, otherwise a scanOperator
//			 */
//			private LogicalOperator buildScanSelectFromItem(FromItem fromItem) {
//				LogicalScanOperator scanOperator = buildScanFromItem(fromItem);
//				LogicalSelectOperator selectOperator = buildSelectFromScan(scanOperator);
//				if (selectOperator == null) {
//					return scanOperator;
//				} else {
//					return selectOperator;
//				}
//			}
			ArrayList<LogicalOperator> joinChildren = new ArrayList<>();
			LogicalOperator initOp = selectOperator == null ? scanOperator : selectOperator;
			joinChildren.add(initOp);

			
			for (int i=0; i<joins.size(); i++) {
				FromItem rightItem = joins.get(i).getRightItem();
				LogicalOperator newScanSelect = buildScanSelectFromItem(rightItem);
				joinChildren.add(newScanSelect);
			}
			joinOperator = new LogicalJoinOperator(joinChildren, expression);
		}

		
		// Projection
		if (selectItems != null && selectItems.size() > 0) {
			String[] cols = new String[selectItems.size()];
			if (!(selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns)) {
				for (int i=0; i<selectItems.size(); i++) {
					SelectExpressionItem selectItem = (SelectExpressionItem) selectItems.get(i);
					cols[i] = selectItem.getExpression().toString();
				}
				if (joinOperator == null) {
					LogicalOperator childOp = selectOperator == null ? scanOperator : selectOperator;
					projectOperator = new LogicalProjectOperator(childOp, cols);

				} else {
					projectOperator = new LogicalProjectOperator(joinOperator, cols);
				}
			}

		}


		if (orderByElements != null && orderByElements.size() > 0) {
			sortOperator = buildSort(orderByElements);
		}

		if (distinct != null) {
			if (sortOperator == null) {
				dupElimOperator = new LogicalDupElimOperator(buildSort(new ArrayList<>()));
			} else {
				dupElimOperator = new LogicalDupElimOperator(sortOperator);
			}
		}

		if (dupElimOperator != null) {
			operator = dupElimOperator;
		} else if (sortOperator != null) {
			operator = sortOperator;
		} else if (projectOperator != null) {
			operator = projectOperator;
		} else if (joinOperator != null) {
			operator = joinOperator;
		} else if (selectOperator != null) {
			operator = selectOperator;
		} else if (scanOperator != null) {
			operator = scanOperator;
		}
	}

	@Override
	public void visit(Union union) {
		throw new UnsupportedOperationException("not supported");
	}

}
