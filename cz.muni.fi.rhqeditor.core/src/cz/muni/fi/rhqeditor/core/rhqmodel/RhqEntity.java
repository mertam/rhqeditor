package cz.muni.fi.rhqeditor.core.rhqmodel;

import java.util.HashSet;
import java.util.Set;

/**
 * class hold information about one entity
 * @author syche
 *
 */
public class RhqEntity {

	private String name;
	private Set<String> values = new HashSet<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<String> getValues() {
		return values;
	}
	public void setValues(Set<String> values) {
		this.values = values;
	}
	
	
}
