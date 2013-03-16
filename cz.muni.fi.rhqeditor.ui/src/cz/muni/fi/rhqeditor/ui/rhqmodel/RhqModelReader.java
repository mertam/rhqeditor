package cz.muni.fi.rhqeditor.ui.rhqmodel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;

import utils.DocumentProvider;
import utils.RhqConstants;



/**
 * This class serves as an interface to access Rhq model
 * @author syche
 *
 */
public class RhqModelReader {

	//Document of editor which is being read
	private IDocument 	fDocument;
	
	private Map<String, RhqTask> 	fModelMap;
//	private IProject				fProject;
	int 							version;
	
	
	/**
	 * create reader for selected project and version of RHQ
	 * @param proj
	 */
	public RhqModelReader(IProject proj, int version){
//		fProject = proj;
		fDocument = DocumentProvider.getInstance().getDocument(proj.getName());
		fModelMap = RhqModel.getInstance().getModel(); 
		
	}
	
	/**
	 * return all replacement variables from model
	 * @return list of replacement variables
	 */
	public List<String> getReplacements(){
		return RhqModel.getInstance().getReplacements();
	}
	public Map<String, RhqTask> getMapOfTasks(){
		return Collections.unmodifiableMap(fModelMap);
	}
	
	/**
	 * return RhqTask of corresponding name, if exists.
	 * @return RhqTask or null
	 */
	public RhqTask getTask(String taskName){
		if(taskName.startsWith(getRhqNamespacePrefix(fDocument))){
			return(fModelMap.get(removeNamespacePrefix(taskName)));
		}
		return null;
	}
	
	/**
	 * return task name without name space prefix
	 * @param name
	 * @return
	 */
    public String removeNamespacePrefix(String name){
    	int beginIndex = name.indexOf(":");
    	if(beginIndex < 0)
    		return name;
    	return name.substring(beginIndex+1);
    }
    
    /**
     * returns RhqPrefix from Document
     * @return
     */
    private String getRhqNamespacePrefix(IDocument doc){
    	String content = doc.get();
    	int endIndex = content.indexOf(RhqConstants.RHQ_NAMESPACE_URL);
    	if(endIndex == -1)
    		return null;
    	
    	int beginIndex = content.lastIndexOf("xmlns:");
    	if(beginIndex == -1)
    		return null;
    	beginIndex+="xmlns:".length();
    	content = content.substring(beginIndex,endIndex);
    	content.replaceAll("\\s", "");
    	return content.substring(0,content.indexOf("="))+":";
    	
    }
    
    public String getRhqNamespacePrefix(){
    	return getRhqNamespacePrefix(fDocument);
    }
    
    
	
	
	
	
	
	
	
	
}
