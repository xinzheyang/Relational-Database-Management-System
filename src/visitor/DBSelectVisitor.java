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
import java.util.Collection;
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
	private HashMap<String, HashSet<Expression>> selectMap;
	private UnionFindVisitor unionFindVisitor;

	private HashMap<String, HashSet<Expression>> ufSelectMap;
	public LogicalOperator getOperator() {
		return operator;
	}

	public void setOperator(LogicalOperator operator) {
		this.operator = operator;
	}


	private Expression concatExp(Collection<Expression> expressions) {
		if (expressions == null || expressions.isEmpty()) {
			return null;
		} else {
			Expression temp = null;
			for (Expression e : expressions) {
				if (temp == null)
					temp = e;
				else
					temp = new AndExpression(e, temp);
			}
			return temp;
		}
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
				Expression exp = concatExp(selectMap.get(tableName));
				selectOp = new LogicalSelectOperator(scanOperator, exp);
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

	private LogicalSelectOperator ufBuildSelectFromScan(LogicalScanOperator scanOperator) {
		UnionFind uFind = unionFindVisitor.getUnionFind();
		LogicalSelectOperator selectOp = null;
		String tableReference = scanOperator.getAlias() != null ? scanOperator.getAlias() : scanOperator.getTableName();


		HashSet<Expression> set = new HashSet<>();
		HashMap<String, Expression> map = new HashMap<>();
		// process unused normal select
		if (ufSelectMap != null && ufSelectMap.size() > 0) {
			if (ufSelectMap.containsKey(tableReference)) {
				// set.addAll(ufSelectMap.get(tableReference));
        for (Expression ex : ufSelectMap.get(tableReference)) {
					map.put(ex.toString(), ex);
				}
			}
		}

		// process union-find
		List<Column> cols = unionFindVisitor.getEqJoinAttrByReference(tableReference); // the attributes of this table
		HashSet<Column> allEqColSet = new HashSet<>();
		HashSet<Column> eqColSet = new HashSet<>();
		if (cols != null) {
			for (Column col : cols) {
				UnionElement uElement = uFind.find(col);
				if (uElement.getEquality() != null) {
					Expression eq = new EqualsTo(col, new LongValue(uElement.getEquality()));
//					set.add(eq);
					map.put(eq.toString(), eq);
				}
				if (uElement.getLower() != null) {
					Expression geq = new GreaterThanEquals(col, new LongValue(uElement.getLower()));
//					set.add(geq);
					map.put(geq.toString(), geq);
				}
				if (uElement.getUpper() != null) {
					Expression leq = new MinorThanEquals(col, new LongValue(uElement.getUpper()));
//					set.add(leq);
					map.put(leq.toString(), leq);
				}
				if (!allEqColSet.contains(col)) {
					List<Column> attrsSameTable = uElement.getAttrByTable(tableReference);
					eqColSet = new HashSet<>(attrsSameTable);
					allEqColSet.addAll(attrsSameTable);

					if (eqColSet.size() > 1) {
						ArrayList<Column> lst = new ArrayList<>(eqColSet);
						for (int i=0; i<lst.size()-1; i++) { // not including the last one, generating n-1 equals
							EqualsTo equalsTo = new EqualsTo(lst.get(i), lst.get(i+1));
//							set.add(equalsTo);
							map.put(equalsTo.toString(), equalsTo);
						}
					}
				}
			}
		}

		System.out.println(map.values());
		Expression selectCondition = concatExp(map.values());
		if (selectCondition != null) {
			selectOp = new LogicalSelectOperator(scanOperator, selectCondition);
		}
		return selectOp;
	}


	/**
	 * @param fromItem
	 * @return builds a selectOperator from FromItem if possible, otherwise a scanOperator
	 */
	private LogicalOperator ufBuildScanSelectFromItem(FromItem fromItem) {
		LogicalScanOperator scanOperator = buildScanFromItem(fromItem);
		LogicalSelectOperator selectOperator = ufBuildSelectFromScan(scanOperator);
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
			ArrayList<LogicalOperator> joinChildren = new ArrayList<>();
			if (expression != null) {
				unionFindVisitor = new UnionFindVisitor();
				expression.accept(unionFindVisitor);
				ParseConjunctExpVisitor ufParseConjunctExpVisitor = new ParseConjunctExpVisitor();
				if (unionFindVisitor.getNormalSelect() != null) {
					unionFindVisitor.getNormalSelect().accept(ufParseConjunctExpVisitor);
					ufSelectMap = ufParseConjunctExpVisitor.getSelectMap();
				}
				LogicalOperator initOp = ufBuildScanSelectFromItem(fromItem);
				joinChildren.add(initOp);

				for (int i=0; i<joins.size(); i++) {
					FromItem rightItem = joins.get(i).getRightItem();
					LogicalOperator newScanSelect = ufBuildScanSelectFromItem(rightItem);
					joinChildren.add(newScanSelect);
				}
				UnionFind uFind = unionFindVisitor.getUnionFind();
				joinOperator = new LogicalJoinOperator(joinChildren, unionFindVisitor.getNormalJoin(), uFind.getRootElementMap(), parseConjunctExpVisitor);
			} else {
				LogicalOperator initOp = selectOperator == null ? scanOperator : selectOperator;
				joinChildren.add(initOp);
				for (int i=0; i<joins.size(); i++) {
					FromItem rightItem = joins.get(i).getRightItem();
					LogicalOperator newScanSelect = buildScanSelectFromItem(rightItem);
					joinChildren.add(newScanSelect);
				}
				joinOperator = new LogicalJoinOperator(joinChildren, expression, null, parseConjunctExpVisitor);
			}


//			if (joinOperator!= null) {
//				System.out.println(joinOperator.getJoinChildren());
//				for (LogicalOperator i : joinOperator.getJoinChildren()) {
//					if (i instanceof LogicalSelectOperator) {
//						LogicalSelectOperator sel = (LogicalSelectOperator) i;
//						System.out.println(sel.getEx());
//					} else {
//						LogicalScanOperator scan = (LogicalScanOperator) i;
//						System.out.println("scan");
//					}
//				}
//				System.out.println(joinOperator.getJoinCondition());
//				System.out.println(joinOperator.getUnionElements());
//				for (UnionElement unionElement :joinOperator.getUnionElements() ) {
//					System.err.println("upper:"+unionElement.getUpper());
//				}
//			}

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
