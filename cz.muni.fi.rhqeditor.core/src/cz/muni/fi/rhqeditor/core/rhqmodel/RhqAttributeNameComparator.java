package cz.muni.fi.rhqeditor.core.rhqmodel;

import java.util.Comparator;

public class RhqAttributeNameComparator implements Comparator<String>{

	@Override
	public int compare(String o1, String o2) {
		if(o1.equalsIgnoreCase("name"))
			return Integer.MAX_VALUE * (-1);
		if(o2.equalsIgnoreCase("description"))
			return Integer.MAX_VALUE;
		return o1.compareTo(o2);
	}
}