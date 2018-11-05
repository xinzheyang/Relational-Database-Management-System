package bplustree;
import database.DBCatalog;
import database.TupleReader;
import physicaloperator.ClusteredIndexSortOperator;
import physicaloperator.InMemorySortOperator;
import physicaloperator.ScanOperator;

import java.io.FileNotFoundException;
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

	public BPlusTree(String tableIn, String serializeLoc, boolean isClustered, int order) throws IOException {
		fileName = DBCatalog.getTableLoc(tableIn);
		serializer = new TreeSerializer(serializeLoc);
		counter = 1; //counter initialized to 1
		this.isClustered = isClustered;
		//if clustered, sort and replace relation file, we use in memory sort for now.
		if (isClustered) {
			ScanOperator scan = new ScanOperator(tableIn, "");
			ClusteredIndexSortOperator sort = new ClusteredIndexSortOperator(scan, DBCatalog.getIndexKey(tableIn));
			sort.dump(fileName);
		}
		this.order = order;
		
		colIndex=Arrays.asList(DBCatalog.getTableColumns(tableIn)).indexOf(DBCatalog.getIndexKey(tableIn));
	}

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
			//		List<DataEntry> dataEntries = new ArrayList<>();
			//		for (Map.Entry<Integer, List<int []>> entry : leafEntries.entrySet()) {
			//		    int key = entry.getKey();
			//		    List<int []> value = entry.getValue();
			//		    DataEntry dataEntry = new DataEntry(key,value);
			//		    dataEntries.add(dataEntry);
			//		}
			//		dataEntries.sort(new Comparator<DataEntry>() {
			//			@Override
			//			public int compare(DataEntry a, DataEntry b) {
			//	        	if(a.getKey() < b.getKey()) return -1;
			//	        	else if(a.getKey() > b.getKey()) return 1;
			//		        return 0;
			//			}
			//		});
		}

		buildLeafNodes(keyValues, rids);
//		System.out.println("finished leaf nodes");
		buildIndexNodes();
		serializer.serializeHeader(counter, keyValues.size(), order);
		serializer.close();
		//		return rids;
	}

	/** Builds and serializes all leaf nodes of this BPlusTree.
	 * @param entries
	 * @return
	 * @throws IOException
	 */
	private void buildLeafNodes(List<Integer> allKeys, List<List<int []>> allRids) throws IOException {
		List<Node> lst = new ArrayList<>();
		int numOfEntries = allKeys.size();
		int numOfNodes = (int) Math.ceil(numOfEntries/(2.0*order)); //number of remaining nodes
		int curr=0;
		System.out.println(numOfNodes);

		//		int leafTotalCount = numOfNodes; 
		//address of node should be from 1 to leafTotalCount;
		while(counter <= numOfNodes) {
			int k = numOfEntries-curr;
			LeafNode leaf;
			if(numOfNodes == 2 && k>2*order && k<3*order) {
				int n = (int) Math.ceil((double) k / 2);
				leaf = new LeafNode(allKeys.subList(curr, curr+n), allRids.subList(curr, curr+n), counter);
				//				leaf = new LeafNode(entries.subList(curr, curr+n));
				lst.add(leaf);
				curr += n;
			}
			else {
				int curEnd = Math.min(curr+2*order, numOfEntries);
				leaf = new LeafNode(allKeys.subList(curr, curEnd), allRids.subList(curr, curEnd), counter);
				//				leaf = new LeafNode(entries.subList(curr, Math.min(curr+2*order, numOfEntries)));
				lst.add(leaf);
				curr += 2*order;
				//				numOfEntries -= order;
			}
			this.serializer.serialize(leaf);
			//			numOfNodes--;
			counter++;
		}
		//counter equals numOfNodes + 1 when exiting the while loop, ready for counting the index nodes. 
		//		counter = lst.size()+1; //the 
		leafNodes = lst;
		//		return lst;
	}

	/** Builds and serializes all index node of this tree.
	 * @param leafNodes
	 * @throws IOException
	 */
	private void buildIndexNodes() throws IOException {
		//prev is null, construct the bottom layer: list of IndexNodes

		//prev not null 

		//BottomIndexNode
		//OtherIndexNode
		//		counter++;
		List<Node> curr = new ArrayList<>();
		List<Node> prev = leafNodes;
		System.out.println(prev.size());
		System.out.println(order);

		if(leafNodes.size() == 1) { 
			IndexNode node= new IndexNode(leafNodes, counter);
			this.serializer.serialize(node);//serialize
		}

		while(prev.size() > 1) {
			System.out.println("begin the inner while loop");

			int numOfChild = prev.size();//number of children from the previous lift
			System.out.println("number of children" + numOfChild);
			int numOfNodes = (int) Math.ceil(numOfChild/(2.0*order+1)); //number of index nodes in the level
			System.out.println("number of nodes" + numOfNodes);
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
					int n = (int) Math.ceil((double) m / 2);
					node= new IndexNode(prev.subList(indexCurr, indexCurr+n), counter);
					//					curr.add(node);
					indexCurr += n;
					System.out.println("is the second last node");
				}
				else {
					node= new IndexNode(prev.subList(indexCurr, Math.min(indexCurr+2*order+1, numOfChild)), counter);
					//					curr.add(node);
					indexCurr += 2*order+1;
				}
				curr.add(node);
				System.out.println("curr size" + curr.size());
				System.out.println("indexCurr" + indexCurr);
				serializer.serialize(node);
				//serialize this node
				counter++;
				numOfNodes--;	
				System.out.println("number of nodes" + numOfNodes);
			}
			//write the serializing code here
//			prev=curr;
			prev=new ArrayList<>(curr);
			System.out.println("end the inner while loop"+prev.size());
			curr.clear();
		}
		//		IndexNode d = new IndexNode();
		//		d.addPointer(leafNodes.get(0));
		//		for(int i=1;i<2*order+1;i++) {
		//			d.addKey(leafNodes.get(i).getMin());
		//			d.addPointer(leafNodes.get(i));
		//		}
		//		
		//		
	}
	//<Node>children,address,keys,min
	//	public Node buildIndexNode(lst,start,end) {
	//		List<Integer> tmp = new ArrayList<>();
	//		address=counter;
	//		min=prev.get(0).get
	//		tmp.add(e)(leafNodes.get(0));
	//		for(int i=curr;i<n;i++) {
	//			
	//		}	
	//	}

}
