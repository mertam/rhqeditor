package cz.muni.fi.rhqeditor.ui.rhqmodel;

import java.util.Comparator;

public class RhqAttributeNameComparator implements Comparator<String>{

	@Override
	public int compare(String o1, String o2) {
		if(o1.equalsIgnoreCase("name"))
			return -1;
		if(o2.equalsIgnoreCase("description"))
			return 1;
		return o1.compareTo(o2);
	}
}