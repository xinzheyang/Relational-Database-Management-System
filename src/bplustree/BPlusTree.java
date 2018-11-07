package bplustree;
import database.DBCatalog;
import database.TupleReader;
import physicaloperator.ClusteredIndexSortOperator;
import physicaloperator.InMemorySortOperator;
import physicaloperator.ScanOperator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BPlusTree {

	private String fileName;
	private int colIndex;
	private int order;
	private List<Node> leafNodes;
	private int counter;
	private TreeSerializer serializer;
	private boolean isClustered;
//	private BufferedWriter logger;

	/**
	 * @param tableIn the name of the file to process
	 * @param serializeLoc the path to put the serialized files
	 * @param isClustered if the index is clustered or not for the file
	 * @param order the order(d) of the B+ tree
	 * @throws IOException
	 */
	public BPlusTree(String tableIn, String serializeLoc, boolean isClustered, int order) throws IOException {
		fileName = DBCatalog.getTableLoc(tableIn);
		serializer = new TreeSerializer(serializeLoc);
		counter = 1; //counter initialized to 1
		this.isClustered = isClustered;
		//if clustered, sort and replace relation file, we use in memory sort for now.
		if (isClustered) {
			ScanOperator scan = new ScanOperator(tableIn, null);
			ClusteredIndexSortOperator sort = new ClusteredIndexSortOperator(scan, DBCatalog.getIndexKey(tableIn));
			sort.dump(fileName);
		}
		this.order = order;
		
		colIndex=Arrays.asList(DBCatalog.getTableColumns(tableIn)).indexOf(DBCatalog.getIndexKey(tableIn));
	}

	/**
	 * Reads each tuple from the file and fetch all the keys of in the 
	 * index column and records ids(page id, tuple id) corresponding to the keys
	 * @throws IOException
	 */
	public void scanAndConstructAll() throws IOException {
		List<List<int []>> rids = new LinkedList<List<int []>>();
		List<Integer> keyValues = new LinkedList<Integer>();

		if (isClustered) { //clustered, tuples already sorted, no need to sort again
			TupleReader reader = new TupleReader(fileName);
			int[] arr;
			while ((arr=reader.getColNext(colIndex)) != null) {
				int[] rid = {arr[1],arr[2]};
				if (keyValues.isEmpty() || keyValues.get(keyValues.size() - 1) != arr[0]) {
					keyValues.add(arr[0]);
					List<int []> newList = new LinkedList<>();
					newList.add(rid);
					rids.add(newList);
				}
				else {
					rids.get(rids.size() - 1).add(rid);
				}
			}
		}
		else { //unclustered, need to sort data entries
			HashMap<Integer, List<int []>> leafEntries = new HashMap<>();
			TupleReader reader = new TupleReader(fileName);
			int[] arr;
			while ((arr=reader.getColNext(colIndex)) != null) {
				int[] rid = {arr[1],arr[2]};
				if(leafEntries.containsKey(arr[0])) {
					leafEntries.get(arr[0]).add(new int[] {arr[1],arr[2]});
				}
				else {
					List<int []> newList = new LinkedList<>();
					newList.add(rid);
					leafEntries.put(arr[0], newList);
				}
			}

			rids = new ArrayList<List<int []>>();
			keyValues = new ArrayList<Integer>(leafEntries.keySet());
			Collections.sort(keyValues);
			for (Integer key : keyValues) {
				rids.add(leafEntries.get(key)); //don't need to sort rids with this key because they're added in order of occurrence
			}
		}
		serializer.serializeHeader(order);
		buildLeafNodes(keyValues, rids);
		buildIndexNodes();
		serializer.updateHeader(counter-1, leafNodes.size());
		serializer.close();
	}

	/**
	 * @param allKeys all the unique keys from the file
	 * @param allRids all the record ids corresponding to each key
	 * @throws IOException
	 */
	private void buildLeafNodes(List<Integer> allKeys, List<List<int []>> allRids) throws IOException {
		List<Node> lst = new ArrayList<>();
		int numOfEntries = allKeys.size();
		int numOfNodes = (int) Math.ceil(numOfEntries/(2.0*order)); //number of remaining nodes
		int curr=0;

		//address of node should be from 1 to leafTotalCount;
		while(counter <= numOfNodes) {
			int k = numOfEntries-curr;
			LeafNode leaf;
			if(numOfNodes - counter == 1 && k>2*order && k<3*order) {
				int n = (int) Math.floor((double) k / 2);
				leaf = new LeafNode(allKeys.subList(curr, curr+n), allRids.subList(curr, curr+n), counter);
				lst.add(leaf);
				curr += n;
			}
			else {
				int curEnd = Math.min(curr+2*order, numOfEntries);
				leaf = new LeafNode(allKeys.subList(curr, curEnd), allRids.subList(curr, curEnd), counter);
				lst.add(leaf);
				curr = curEnd;
			}
			this.serializer.serialize(leaf);
			counter++;
		}
		//counter equals numOfNodes + 1 when exiting the while loop, ready for counting the index nodes. 
		leafNodes = lst;
	}

	
	/**
	 * Build the index nodes of the tree layer by layer, from the
	 * very bottom pointing at the leaf nodes. Serialize each layer
	 * at the same time.
	 * @throws IOException
	 */
	private void buildIndexNodes() throws IOException {
		List<Node> curr = new ArrayList<>();
		List<Node> prev = leafNodes;

		if(leafNodes.size() == 1) { 
			IndexNode node= new IndexNode(leafNodes, counter);
			this.serializer.serialize(node);//serialize
		}

		while(prev.size() > 1) {

			int numOfChild = prev.size();//number of children from the previous lift
			int numOfNodes = (int) Math.ceil(numOfChild/(2.0*order+1)); //number of index nodes in the level
			int indexCurr=0; //the index of the current children
			//get the bottom layer of index nodes
			while(numOfNodes > 0) {
				int m = numOfChild-indexCurr;
				IndexNode node;
				//to see if it is the second last node
				if(numOfNodes == 2 && m>2*order+1 && m<3*order+2) {
					//get the page numbers
					//get the keys
					//get the minimum value
					int n = (int) Math.floor((double) m / 2);
					node= new IndexNode(prev.subList(indexCurr, indexCurr+n), counter);
					indexCurr += n;
				}
				else {
					int indexCurrEnd = Math.min(indexCurr+2*order+1, numOfChild);
					node= new IndexNode(prev.subList(indexCurr, Math.min(indexCurr+2*order+1, numOfChild)), counter);
					indexCurr = indexCurrEnd;
				}
				curr.add(node);
				serializer.serialize(node);
				//serialize this node
				counter++;
				numOfNodes--;	
			}
			//write the serializing code here
			prev=new ArrayList<>(curr);
			curr.clear();
		}	
	}

}
