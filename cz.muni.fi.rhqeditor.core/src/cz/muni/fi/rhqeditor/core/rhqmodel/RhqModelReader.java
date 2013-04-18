package cz.muni.fi.rhqeditor.core.rhqmodel;

import java.util.Collections;
import java.util.List;
import java.util.Map;




/**
 * This class serves as an interface to access Rhq model
 * @author syche
 *
 */
public class RhqModelReader {

	
	private Map<String, RhqTask> 	fModelMap;
	int 							version;
	
	
	/**
	 * create reader for selected project and version of RHQ
	 * @param proj
	 */
	public RhqModelReader(int version){
		fModelMap = RhqModel_4_6_0.getInstance().getModel(); 
		
	}
	
	public List<String> getReplacements(){
		return RhqModel_4_6_0.getInstance().getReplacements();
	}
	
	public Map<String, RhqTask> getMapOfTasks(){
		return Collections.unmodifiableMap(fModelMap);
	}
	
	/**
	 * return RhqTask of corresponding name, if exists. Method <b>doesn't</b> care about name space prefix.
	 * @return RhqTask or null
	 */
	public RhqTask getTask(String taskName){
		return(fModelMap.get(removeNamespacePrefix(taskName)));
	}
	
	/**
	 * return task name without name space prefix
	 * @param name
	 * @return
	 */
    public static String removeNamespacePrefix(String name){
    	int beginIndex = name.indexOf(":");
    	if(beginIndex < 0)
    		return name;
    	return name.substring(beginIndex+1);
    }
    
    
    

	
	
	
	
	
	
	
}
