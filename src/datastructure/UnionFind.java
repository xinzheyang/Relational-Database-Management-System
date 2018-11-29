/**
 * 
 */
package datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Union;



/**
 * @author xinzheyang
 *	reference of Union-Find algorithm: https://www.cs.princeton.edu/~rs/AlgsDS07/01UnionFind.pdf
 */
public class UnionFind {
	
//	private List<UnionElement> elements;
	private List<Integer> idArray = new ArrayList<>();
	private HashMap<String, Integer> idMap = new HashMap<>();
	
	private HashMap<Integer, UnionElement> rootElementMap = new HashMap<>();
	
	/**
	 * 
	 */
	public UnionFind() {
	}
	
	private int root(int i) {
		while (i != idArray.get(i)) {
			i = idArray.get(i);
		}
		return i;
	}
	
	
	/**
	 * @return the values of rootElementMap, i.e. all UnionElements
	 */
	public Collection<UnionElement> getRootElementMap() {
		return rootElementMap.values();
	}

	/**
	 * @param attr
	 * @return
	 */
	public UnionElement find(Column attr) {
		if (idMap.containsKey(attr.getWholeColumnName())) {
			return rootElementMap.get(root(idMap.get(attr.getWholeColumnName())));
		} else {
			int newId = idArray.size();
			idMap.put(attr.getWholeColumnName(), newId);
			idArray.add(newId);
			UnionElement newElement = new UnionElement(newId);
			newElement.addAttribute(attr);
			rootElementMap.put(newId, newElement);
			return newElement;
		}
	}
	
	public void unite(UnionElement p, UnionElement q) {
		assert rootElementMap.containsValue(p) && rootElementMap.containsValue(q);
		
		int i = p.getRootId();
		int j = q.getRootId();
		idArray.set(i, j);
		rootElementMap.remove(i);
		
		// for assert 
		int oldsize = q.getAttributes().size();
		
		boolean success = q.getAttributes().addAll(p.getAttributes());
		if (q.getEquality() == null)
			q.setEquality(p.getEquality());
		
		if (q.getLower() == null) {
			q.setLower(p.getLower());
		} else if (p.getLower() != null){
			q.setLower(Math.max(p.getLower(), q.getLower()));
		}
		
		if (q.getUpper() == null) {
			q.setUpper(p.getUpper());
		} else if (p.getUpper() != null){
			q.setUpper(Math.min(p.getUpper(), q.getUpper()));
		}
		assert success && (q.getAttributes().size() == oldsize + p.getAttributes().size());
		
	}
	
	public void setUpper(UnionElement e, Integer upper) {
		int rid = e.getRootId();
		assert rootElementMap.containsKey(rid);
		rootElementMap.get(rid).setUpper(upper);
	}
	
	public void setLower(UnionElement e, Integer lower) {
		int rid = e.getRootId();
		assert rootElementMap.containsKey(rid);
		rootElementMap.get(rid).setLower(lower);
	}
	
	public void setEquality(UnionElement e, Integer eq) {
		int rid = e.getRootId();
		assert rootElementMap.containsKey(rid);
		rootElementMap.get(rid).setEquality(eq);
	}

	private void debug() {
		System.out.println("==========");
		System.out.println(idArray);
		System.out.println(idMap);
		System.out.println(rootElementMap);
		System.out.println("=========");
	}
	public static void main(String[] args) {
		UnionFind uFind = new UnionFind();
		Column raColumn = new Column(new Table(null, "R"), "A");
		uFind.find(raColumn);
		uFind.debug();
		AndExpression andExpression = new AndExpression(raColumn, null);
		System.out.println(andExpression);
		AndExpression newand = new AndExpression(raColumn, andExpression);
		System.out.println(newand.getLeftExpression());
//		UnionElement ra = uFind.find("RA");
//		UnionElement rb = uFind.find("RB");
//		System.out.println(ra.getAttributes());
//		System.out.println(rb.getAttributes());
//		uFind.debug();
//		uFind.unite(ra, rb);
//		uFind.debug();
//		rb = uFind.find("RB");
//		System.out.println(rb.getAttributes());
//		UnionElement rc = uFind.find("RC");
//		uFind.unite(rb, rc);
//		uFind.debug();
//		
//		
//		UnionElement sd = uFind.find("SD");
//		UnionElement se = uFind.find("SE");
//		uFind.unite(sd, se);
//		UnionElement sf = uFind.find("SF");
//		uFind.unite(se, sf);
//		uFind.debug();
//		ra = uFind.find("RA");
//		sd = uFind.find("SD");
//		uFind.unite(ra, sd);
//		uFind.debug();
	}
}


