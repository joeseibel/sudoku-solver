package sudokusolver;

import java.util.ArrayList;
import java.util.HashMap;

public class Util {
	public static void addToValueList(HashMap<SudokuNumber, ArrayList<Cell>> map, SudokuNumber key, Cell value) {
		ArrayList<Cell> valueList = map.get(key);
		if (valueList == null) {
			valueList = new ArrayList<>();
			map.put(key, valueList);
		}
		valueList.add(value);
	}
}