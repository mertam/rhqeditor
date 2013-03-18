package cz.muni.fi.rhqeditor.core.rhqmodel;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RhqTask {
	
	private String name;
	private Set<RhqAttribute> attributes = new HashSet<>();
	private Set<RhqTask> descendents = new HashSet<>();
	private Set<String> antParents = new HashSet<>();
	private Set<RhqTask> parents = new HashSet<>();
	private boolean paired;
	private String description = "";
	private List<String> antChildren = new ArrayList<>();
	
	
	public RhqTask(){}
	public RhqTask(String name){
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<RhqAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(Set<RhqAttribute> attributes) {
		this.attributes = attributes;
	}
	public Set<RhqTask> getDescendents() {
		return descendents;
	}
	public void setDescendents(Set<RhqTask> descendents) {
		this.descendents = descendents;
	}
	public Set<String> getAntParents() {
		return antParents;
	}
	public void setAntParents(Set<String> antParents) {
		this.antParents = antParents;
	}
	public Set<RhqTask> getParents() {
		return parents;
	}
	public void setParents(Set<RhqTask> parents) {
		this.parents = parents;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		if(description != null)
			this.description = description.trim();
	}
	
	public boolean isPaired() {
		return paired;
	}
	public void setPaired(boolean paired) {
		this.paired = paired;
	}
	
	public List<String> getAntChildren() {
		return antChildren;
	}
	public void setAntChildren(List<String> antChildren) {
		this.antChildren = antChildren;
	}
	public Set<String> getAllParentNames(){
		Set<String> prnts = new HashSet<>(antParents);
		for(RhqTask task: parents){
			prnts.add(task.getName());
		}
		return prnts;
		
	}
	
	public RhqAttribute getAttribute(String name){
		for(RhqAttribute attr: getAttributes()){
			if(attr.getName().equals(name))
				return attr;
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RhqTask other = (RhqTask) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	

}
