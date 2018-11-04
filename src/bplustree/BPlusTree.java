package bplustree;
import database.TupleReader;

import java.io.FileNotFoundException;
import java.io.IOException;
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
	private TreeSerializer serializer;
	public BPlusTree() throws FileNotFoundException {
		String fileout = "//indexes//bla"; //the mighty place holder
		serializer = new TreeSerializer(fileout);
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
		int numOfNodes = (int) Math.ceil(numOfEntries/(2.0*order));
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
				curr += 2*order;
//				numOfEntries -= order;
			}
			numOfNodes--;
		}
		counter = lst.size()+1; //the 
		return lst;
	}
	
	public void buildIndexNode(List<Node> leafNodes) throws IOException {
		//prev is null, construct the bottom layer: list of IndexNodes
		
		//prev not null 
		
		//BottomIndexNode
		//OtherIndexNode
//		counter++;
		List<Node> curr = new ArrayList<>();
		List<Node> prev = leafNodes;

		if(leafNodes.size() == 1) {
			Node node= new IndexNode(leafNodes, counter);
			//serialize
		}
		
		while(prev.size() != 1) {
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
					int n = (int) Math.ceil((double) m / 2);
					node= new IndexNode(prev.subList(indexCurr, indexCurr+n), counter);
//					curr.add(node);
					indexCurr += n;
//					counter += n;
				}
				else {
					node= new IndexNode(prev.subList(indexCurr, Math.min(indexCurr+2*order+1, numOfChild)), counter);
//					curr.add(node);
					indexCurr += 2*order+1;
				}
				curr.add(node);
				serializer.serialize(node);
				//serialize this node
				counter++;
				numOfNodes--;	
			}
			//write the serializing code here
			prev=curr;
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
