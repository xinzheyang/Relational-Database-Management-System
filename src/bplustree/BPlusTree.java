package bplustree;
import database.TupleReader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BPlusTree {

	private String fileName;
	private int colIndex;
	private int order;
	private List<LeafNode> leafNodes;
	private int counter;
	public BPlusTree() {
		// TODO Auto-generated constructor stub
	}
	
	public List<DataEntry> scanAll() {
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
		List<DataEntry> dataEntries = new ArrayList<>();
		for (Map.Entry<Integer, List<int []>> entry : leafEntries.entrySet()) {
		    int key = entry.getKey();
		    List<int []> value = entry.getValue();
		    DataEntry dataEntry = new DataEntry(key,value);
		    dataEntries.add(dataEntry);
		}
		dataEntries.sort(new Comparator<DataEntry>() {
			@Override
			public int compare(DataEntry a, DataEntry b) {
	        	if(a.getKey() < b.getKey()) return -1;
	        	else if(a.getKey() > b.getKey()) return 1;
		        return 0;
			}
		});
		
		return dataEntries;
	}
	
	public List<LeafNode> buildleafNodes(List<DataEntry> entries) {
		List<LeafNode> lst = new ArrayList<>();
		int numOfEntries = entries.size();
		int numOfNodes = (int) Math.ceil(numOfEntries/2.0*order);
		int curr=0;
		while(numOfNodes > 0) {
			int k = numOfEntries-curr;
			if(numOfNodes == 2 && k>2*order && k<3*order) {
				int n = (int) Math.ceil((double) k / 2);
				LeafNode leaf = new LeafNode(entries.subList(curr, curr+n));
				lst.add(leaf);
				curr += n;
			}
			else {
				LeafNode leaf = new LeafNode(entries.subList(curr, Math.min(curr+2*order, numOfEntries)));
				lst.add(leaf);
				curr += order;
//				numOfEntries -= order;
			}
			numOfNodes--;
		}
		counter = lst.size();
		return lst;
	}
	
	public void buildIndexNode(List<LeafNode> leafNodes) {
		//prev is null, construct the bottom layer: list of IndexNodes
		
		//prev not null 
		
		//BottomIndexNode
		//OtherIndexNode
		counter++;
		List<IndexNode> curr = null;
		List<IndexNode> prev = null;
		
		
		int numOfChild = prev.size();
		int numOfNodes = (int) Math.ceil(numOfEntries/2.0*order);
		int curr=0;
		//get the bottom layer of index nodes
		IndexNode d = new IndexNode();
		d.addPointer(leafNodes.get(0));
		for(int i=1;i<2*order+1;i++) {
			d.addKey(leafNodes.get(i).getMin());
			d.addPointer(leafNodes.get(i));
		}
		
		
	}

}
