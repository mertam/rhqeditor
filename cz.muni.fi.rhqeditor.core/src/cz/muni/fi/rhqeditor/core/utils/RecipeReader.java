package cz.muni.fi.rhqeditor.core.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;


/**
 * class used for reading content of recipe "deploy.xml"
 * @author syche
 *
 */
public class RecipeReader {
	
	
	private IDocument fDocument;
	private IProject  fProject;
	
	public RecipeReader(IProject project) {
		fDocument = DocumentProvider.getInstance().getDocument(project);
	}
	
	
    public String getRhqNamespacePrefix(){
    	return getRhqNamespacePrefix(readRecipe(false));
    }
    
    /**
     * return namespace prefix from given string
     * @param content
     * @return
     */
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
	
	
	
	
	
	/**
	 * reads recipe and return stringBuilder containing it's contain
	 * @param project
	 * @return
	 */
	public String readRecipe(boolean forceReadOnSavedFile) {	
		StringBuilder sb = new StringBuilder();
		//if there is no document, read file
		if(forceReadOnSavedFile || fDocument == null) {
			IFile file = fProject.getFile(RhqConstants.RHQ_RECIPE_FILE);
			if(file != null){
				try {
					//read whole recipe
					InputStreamReader ir = new InputStreamReader(file.getContents());
					
					BufferedReader br = new BufferedReader(ir);
					
					String line;
					while((line = br.readLine()) != null){
						sb.append(line);
					}
				} catch (IOException | CoreException e){
					return new String();
				}
			}
			return sb.toString();
		} else {
			return fDocument.get();
		}
	}
	
	
	
	public static void setRecipeContent(IProject project,String source){
		IFile file = project.getFile(RhqConstants.RHQ_RECIPE_FILE);
		
		try{
		ByteArrayInputStream bais = new ByteArrayInputStream(source.getBytes());
		file.setContents(bais,
                true,
                true,
                null);
		} catch(CoreException ex){
			ex.printStackTrace();
		}    
	}
	
	/**
	 * returns true if project contains file deploy.xml (must be in direct child of project folder)
	 * @param projectName
	 * @return
	 */
	public static boolean hasRrecipe(String projectName){
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IFile file = proj.getFile(new Path(RhqConstants.RHQ_RECIPE_FILE));
		return file.exists();
		
	}
	
	/**
	 *	reads recipe of given project and finds all files, that are referenced in it (rhq:file name="file.xml", rhq:archive name="arch.jar") 
	 *  @param project IProject to be searched
	 *  @return HashSet containing names of referenced files
	 */
	public HashSet<String> getReferencedFiles(){
		HashSet<String> result = new HashSet<>();
		String content = readRecipe(false);
		String namespace = getRhqNamespacePrefix();
		Pattern pattern = 
				Pattern.compile("("+namespace + RhqConstants.RHQ_TYPE_ARCHIVE +	//rhq:archive
				"|" + namespace + RhqConstants.RHQ_TYPE_FILE + ")"+     		// or rhq:file
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" +			//some attribute*
				"\\s+name\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]+\"" +    			//name="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				 "\\s*/?\\s*>", Pattern.DOTALL);								// /> or >
		
	    Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
        	int endIndex;
        	String currentString = content.substring(matcher.start());
        	currentString = currentString.substring(currentString.indexOf("name"));
        	currentString = currentString.substring(currentString.indexOf("\"")+1);
        	endIndex = currentString.indexOf("\"");
        	result.add(currentString.substring(0,endIndex));
        }
        
		return result;
	}
	
	
	

}
