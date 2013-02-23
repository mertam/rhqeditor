package cz.muni.fi.rhqeditor.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * 
 * @author Michal Merta
 * 
 * 
 *
 */

public class RhqCompletionProposal {
	/**
	 * Returns attributes 
	 */
	
	private Set<String> tasks = new HashSet<String>();
	private String namespacePrefix = "rhq:";
	
	public RhqCompletionProposal(){
		loadTask();
	}
	
	public void setNamespacePrefix(String prefix){
		if(prefix != null) namespacePrefix = prefix;
	}
	
	public String getPrefix(){
		return namespacePrefix;
	}
	
	public Set<String> getTasksStrings(){
		return tasks;
	}
	
//	public ICompletionProposal[] addRhqTasks(ICompletionProposal[] fTasks, String sPrefix){
//		//find all tasks matching to prefix
//		for(String task : tasks){
//			if(task.startsWith(sPrefix))
//				
//		}
//		return null;
//	}
	
	
	public ICompletionProposal[] addAttributeNames(String sRhqTaskName, String sPrefix){
		return null;
		
	}
	
	public boolean isKnownRhqTask(String task){
		return tasks.contains(task);
	}
	
	private void loadTask(){
		
		tasks.add(namespacePrefix + "bundle");
		tasks.add(namespacePrefix + "input-property");
		tasks.add(namespacePrefix + "deployment-unit");
	}
}
