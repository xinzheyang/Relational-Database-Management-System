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
	public BPlusTree() {
		// TODO Auto-generated constructor stub
	}
	
	public void scanAll() {
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

	}

}
