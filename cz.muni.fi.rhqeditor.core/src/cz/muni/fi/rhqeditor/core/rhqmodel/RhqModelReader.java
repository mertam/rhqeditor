package cz.muni.fi.rhqeditor.core.rhqmodel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;

import cz.muni.fi.rhqeditor.core.utils.DocumentProvider;
import cz.muni.fi.rhqeditor.core.utils.RecipeReader;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;




/**
 * This class serves as an interface to access Rhq model
 * @author syche
 *
 */
public class RhqModelReader {

	//Document of editor which is being read
	private IDocument 				fDocument;
	
	private Map<String, RhqTask> 	fModelMap;
	private IProject				fProject;
	int 							version;
	
	
	/**
	 * create reader for selected project and version of RHQ
	 * @param proj
	 */
	public RhqModelReader(IProject proj, int version){
//		fProject = proj;
		fProject  = proj;
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
		if(taskName.startsWith(getRhqNamespacePrefix())){
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
    
    
    
    public static String getRhqNamespacePrefix(String content){
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
    
    /**
     * return namespace prefix. If fDocument is unset, reads from file in workspace.
     * @return
     */
    public String getRhqNamespacePrefix(){
    	return (fDocument == null ? 
    			getRhqNamespacePrefix(RecipeReader.readRecipe(fProject).toString()) : getRhqNamespacePrefix(fDocument.get()));
    }
    
	/**
	 * returns name of parent <rhq:archive name=".." tag in document. 
	 * @return archive name or empty String
	 */
    public String getParentArchiveFilename(IDocument document, int position){
		final String EMPTY_STRING = "";
		if(position < 0)
			return EMPTY_STRING;
		String toSearch = document.get().substring(0, position);
		
    	
		toSearch = toSearch.replaceAll("\\s","");
		String archiveElement = getRhqNamespacePrefix()+"archive";
    	//finds last occurence of tag <rhq:archive
    	int lastArchiveIndex = toSearch.lastIndexOf("<" + archiveElement);
    	
    	//there is no open <rhq:archive tag
    	if(lastArchiveIndex == -1){
    		return EMPTY_STRING;
    	}
    	toSearch =  toSearch.substring(lastArchiveIndex, toSearch.length());                	
    	//finds whether there is also closing tag
    	int closingIndex = toSearch.indexOf("</" +archiveElement);
//    	System.out.println("SUBSTR: " + toSearch.substring(lastArchiveIndex, toSearch.length()));
    	
    	if(closingIndex > -1){
    		//rhq:archvive is closed, normal file proposal
    		return EMPTY_STRING;
    	}else{
    		//cursor is inside of <rhq:archive tag, archvive file proposal
    		int nameBegin = toSearch.indexOf("name=\"");
    		if(nameBegin > -1){
    			toSearch = toSearch.substring(nameBegin + "name=\"".length() , toSearch.length());
    		}else{
    			return EMPTY_STRING;
    		}
    		int nameEnd = toSearch.indexOf("\"");
    		
    		if(nameEnd > -1){
    			String archiveName = toSearch.substring(0, nameEnd);
    			return archiveName;
    		}else{
    			return EMPTY_STRING;
    		}
    	}

	}
	
	
	
	
	
	
	
}
