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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (required ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof InputProperty))
			return false;
		InputProperty other = (InputProperty) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (required != other.required)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}
