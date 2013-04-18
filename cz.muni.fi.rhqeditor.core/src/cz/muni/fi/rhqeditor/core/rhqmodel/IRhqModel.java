package cz.muni.fi.rhqeditor.core.rhqmodel;

import java.util.List;
import java.util.Map;


/**
 * interface representing model of RHQ tasks
 * @author syche
 *
 */
interface IRhqModel {

	
	/**
	 * returns map of tasks - String task name is key for RhqTask value
	 * @return
	 */
	public abstract Map<String, RhqTask> getModel();

	
	/**
	 * return all replacement variables from model
	 * @return list of replacement variables
	 */
	public abstract List<String> getReplacements();
	
	

}