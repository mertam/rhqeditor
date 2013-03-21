package cz.muni.fi.rhqeditor.core.utils;

import java.util.Arrays;
import java.util.HashSet;


/**
 * class carry information about one input property
 * @author syche
 *
 */
public class InputProperty {

	private String name = null;
	private String defaultValue = null;
	private String type = null;
	private String description = null;
	private boolean required = true;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return defaultValue;
	}

	public void setValue(String value) {
		this.defaultValue = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public static HashSet<String> InputPropertyType = new HashSet<>(Arrays.asList(new String[] { 
			"string","longString","boolean","integer","long","float","double","password","file","directory"}));
}
