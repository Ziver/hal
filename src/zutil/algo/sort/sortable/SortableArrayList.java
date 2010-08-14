package zutil.algo.sort.sortable;

import java.util.ArrayList;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SortableArrayList<T> implements SortableDataList<T>{
	private ArrayList<T> list;
	
	public SortableArrayList(ArrayList<T> list){
		this.list = list;
	}

	public T get(int i) {
		return list.get(i);
	}
	
	public void set(int i, T o){
		list.set(i, o);
	}

	public int size() {
		return list.size();
	}

	public void swap(int a, int b) {
		T temp = list.get(a);
		list.set(a, list.get(b));		
		list.set(b, temp);
	}
	
	public int compare(int a, int b) {
		Comparable aa = (Comparable)list.get(a);
		Comparable bb = (Comparable)list.get(b);
		return aa.compareTo(bb);
	}

	public int compare(int a, T b) {
		Comparable aa = (Comparable)list.get(a);
		Comparable bb = (Comparable)b;
		return aa.compareTo(bb);
	}
	
	

}
