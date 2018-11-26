/**
 * 
 */
package datastructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;



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
	public UnionElement find(String attr) {
		if (idMap.containsKey(attr)) {
			return rootElementMap.get(root(idMap.get(attr)));
		} else {
			int newId = idArray.size();
			idMap.put(attr, newId);
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
		
		assert success && (q.getAttributes().size() == oldsize + p.getAttributes().size());
		
	}
	
	public void setConstraint(UnionElement e, Integer lower, Integer upper, Integer eq) {
		e.setLower(lower);
		e.setUpper(upper);
		e.setEquality(eq);
	}
	
	private void debug() {
		System.out.println(idArray);
		System.out.println(idMap);
		System.out.println(rootElementMap);
		System.out.println("=========");
	}
	public static void main(String[] args) {
		UnionFind uFind = new UnionFind();
		uFind.find("RA");
		uFind.debug();
		
		UnionElement ra = uFind.find("RA");
		UnionElement rb = uFind.find("RB");
		System.out.println(ra.getAttributes());
		System.out.println(rb.getAttributes());
		uFind.debug();
		uFind.unite(ra, rb);
		uFind.debug();
		rb = uFind.find("RB");
		System.out.println(rb.getAttributes());
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


