/**
 * 
 */
package visitor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
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
import operator.DupElimOperator;
import operator.JoinOperator;
import operator.Operator;
import operator.ProjectOperator;
import operator.ScanOperator;
import operator.SelectOperator;
import operator.SortOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author xinzheyang
 *
 */
public class DBSelectVisitor implements SelectVisitor {
	private Operator operator = null;
    private ScanOperator scanOperator;
    private SelectOperator selectOperator;
    private JoinOperator joinOperator;
    private ProjectOperator projectOperator;
    private SortOperator sortOperator;
    private DupElimOperator dupElimOperator;
    
    private ParseConjunctExpVisitor parseConjunctExpVisitor;
    private HashMap<String, Expression> selectMap;
	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	/**
	 * @param fromItem
	 * @return
	 */
	private ScanOperator buildScanFromItem(FromItem fromItem) {
		DBFromItemVisitor dbFromItemVisitor = new DBFromItemVisitor();
		fromItem.accept(dbFromItemVisitor);
		scanOperator = (ScanOperator) dbFromItemVisitor.getOperator();
		return scanOperator;
	}
	
	private SelectOperator buildSelectFromScan(ScanOperator scanOperator) {
		SelectOperator selectOp = null;
		if (selectMap != null && selectMap.size() > 0) {
			String tableName = scanOperator.getTableName();
			if (selectMap.containsKey(tableName)) {
				selectOp = new SelectOperator(scanOperator, selectMap.get(tableName));
			}
		}
		return selectOp;
	}
	
	private Operator buildScanSelectFromItem(FromItem fromItem) {
		ScanOperator scanOperator = buildScanFromItem(fromItem);
		SelectOperator selectOperator = buildSelectFromScan(scanOperator);
		if (selectOperator == null) {
			return scanOperator;
		} else {
			return selectOperator;
		}
	}
	
	
	private SortOperator buildSort(List<OrderByElement> orderByElements) {
		SortOperator sortOperator = null;
		String[] cols = new String[orderByElements.size()];
		for (int i=0; i<orderByElements.size(); i++) {
			cols[i] = orderByElements.get(i).toString();
		}
		if (projectOperator != null) {
			sortOperator = new SortOperator(projectOperator, cols);
		} else if (joinOperator != null) {
			sortOperator = new SortOperator(joinOperator, cols);
		} else if (selectOperator != null) {
			sortOperator = new SortOperator(selectOperator, cols);
		} else if (scanOperator != null) {
			sortOperator = new SortOperator(scanOperator, cols);
		}
		return sortOperator;
	}
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
			
			// SELECT * FROM A WHERE A.sid  A:A.sid = 1
			selectMap = parseConjunctExpVisitor.getSelectMap();
		}
		
		
		
		
		scanOperator = buildScanFromItem(fromItem);
		
		selectOperator = buildSelectFromScan(scanOperator);
		
		
		if (joins != null && joins.size() > 0) {
			JoinOperator left;
			FromItem firstRightItem = joins.get(0).getRightItem();
			ArrayList<FromItem> leftTable = new ArrayList<>();
			Operator initLeftOp = selectOperator == null ? scanOperator : selectOperator;
			
			
			if (parseConjunctExpVisitor != null && 
					parseConjunctExpVisitor.getJoinCondition(fromItem.toString(), firstRightItem.toString()) != null) {
				Expression condition = parseConjunctExpVisitor.getJoinCondition(fromItem.toString(), firstRightItem.toString());
				left = new JoinOperator(initLeftOp, buildScanSelectFromItem(firstRightItem), condition);
			} else {
				left = new JoinOperator(initLeftOp, buildScanSelectFromItem(firstRightItem));
			}
			
			leftTable.add(fromItem);
			leftTable.add(firstRightItem);
			
			for (int i=1; i<joins.size(); i++) {
				Expression condition = null;
				FromItem rightItem = joins.get(i).getRightItem();
				Operator newScanSelect = buildScanSelectFromItem(rightItem);
				for (FromItem table:leftTable) {
					if (parseConjunctExpVisitor != null && 
							parseConjunctExpVisitor.getJoinCondition(table.toString(), rightItem.toString()) != null) {
						Expression tempCondition = parseConjunctExpVisitor.getJoinCondition(table.toString(), rightItem.toString());
						condition = condition == null ? tempCondition: new AndExpression(condition, tempCondition);
						
					}
				}
				left = condition == null ? new JoinOperator(left, newScanSelect) 
						: new JoinOperator(initLeftOp, buildScanSelectFromItem(firstRightItem), condition);
			}
			joinOperator = left;
		}
		
		if (selectItems != null && selectItems.size() > 0) {
			String[] cols = new String[selectItems.size()];
			if (!(selectItems.size() == 1 && selectItems.get(0) instanceof AllColumns)) {
				for (int i=0; i<selectItems.size(); i++) {
					SelectExpressionItem selectItem = (SelectExpressionItem) selectItems.get(i);
					cols[i] = selectItem.getExpression().toString();
				}
			if (joinOperator == null) {
				Operator childOp = selectOperator == null ? scanOperator : selectOperator;
				projectOperator = new ProjectOperator(childOp, cols);
				
			} else {
				projectOperator = new ProjectOperator(joinOperator, cols);
			}
			}
			
		}
		
		
		if (orderByElements != null && orderByElements.size() > 0) {
			sortOperator = buildSort(orderByElements);
		}
		
		if (distinct != null) {
			if (sortOperator == null) {
				dupElimOperator = new DupElimOperator(buildSort(new ArrayList<>()));
			} else {
				dupElimOperator = new DupElimOperator(sortOperator);
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
