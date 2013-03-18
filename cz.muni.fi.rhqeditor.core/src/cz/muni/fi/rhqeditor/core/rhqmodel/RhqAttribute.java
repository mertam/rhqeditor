package cz.muni.fi.rhqeditor.core.rhqmodel;


/**
 * class represents RHQ attribute. Used for Ant editor content proposals. 
 * Implements Comparable to show attribute "name" always on first place in editor
 * @author syche
 *
 */

public class RhqAttribute{
	
	private String name = "";
	private boolean required = false;
	private boolean visible = false;
	private String description = "";
	private RhqEntity entity = null;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		if(description != null)
			this.description = description.trim();
	}
	public RhqEntity getEntity() {
		return entity;
	}
	public void setEntity(RhqEntity entity) {
		this.entity = entity;
	}
	
}




