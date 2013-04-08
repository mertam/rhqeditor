package cz.muni.fi.rhqeditor.core.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import cz.muni.fi.rhqeditor.core.rhqmodel.RhqModelReader;


/**
 * class used for getting input properties from recipe
 * @author syche
 *
 */
public class InputPropertiesManager {

	/**
	 * project associated to this manager, is't recipe is being searched
	 */
	private IProject fProject;
	
	
	public InputPropertiesManager(String projectName){
		fProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}
	
	
	/**
	 * finds all input properties which have filled name. Implicitly searches in file stored in workspace, optionally can search 
	 * in the IDocument of editor page if forceSearchOnSavedFile is true
	 * 	 
	 * @return List of all input properties from recipe
	 * @throws CoreException when error during reading recipe occurs
	 */
	public ArrayList<InputProperty> getInputPropertiesFromRecipe(boolean forceSearchOnSavedFile) throws CoreException{
		
		//init searched object
		StringBuilder sb;
		if(!forceSearchOnSavedFile){
			DocumentProvider provider = DocumentProvider.getInstance();
			IDocument document = provider.getDocument(fProject.getName());
			String searchedContext = (document == null ? null : document.get());
			if(searchedContext == null)
				sb = RecipeReader.readRecipe(fProject);
			else
				sb = new StringBuilder(searchedContext);
		}else{
			sb = RecipeReader.readRecipe(fProject);
		}
		
		
		//find properties
		RhqModelReader reader = new RhqModelReader(fProject,0);
		Pattern pattern = 
				Pattern.compile("("+ reader.getRhqNamespacePrefix() +
				RhqConstants.RHQ_TYPE_INPUT_PROPERTY + ")"+     				//rhq:input-property
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" +			//some attribute*
				"\\s+name\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]+\"" +    			//name="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				 "\\s*/?\\s*>", Pattern.DOTALL);								// /> or >
        Matcher matcher = pattern.matcher(sb);
        
        ArrayList<InputProperty>  result = new ArrayList<>();
        addDefaultProperties(result);
        int commentStartPosition, commentEndPosition;
        while(matcher.find()){
        	if ((commentEndPosition = sb.substring(matcher.start(),sb.length()).indexOf("-->")) > -1) {
				commentStartPosition = sb.substring(matcher.start(),sb.length()).indexOf("<!--");
				if(true && commentStartPosition > commentEndPosition){
					// commented property
					continue;
				}
        	}	
        	
        	
        	InputProperty property = new InputProperty();
        	String toAdd = sb.substring(matcher.start());
        	property.setName(getAttributeValue(toAdd, "name"));
        	property.setType(getAttributeValue(toAdd, "type"));
        	property.setDescription(getAttributeValue(toAdd, "description"));
        	String required = getAttributeValue(toAdd, "required");

        	if(required == null)
        		property.setRequired(false);
        	else if(required.equalsIgnoreCase("true"))
        			property.setRequired(true);
        		else
        			property.setRequired(false);
        	property.setValue(getAttributeValue(toAdd, "defaultValue"));
        	result.add(property);
        	
        }
		return result;
	}
	
	/**
	 * returns value of given attribute from string readFrom
	 * @param readFrom string with part of recipe content
	 * @param name name of atribute
	 * @return
	 */
	private String getAttributeValue(String readFrom, String name){
		int endIndex = readFrom.indexOf('>');
		if(endIndex < 0)
			return null;
		
		String property = readFrom.substring(0,endIndex);
		Pattern pattern = Pattern.compile(name+"\\s*=\\s*\"");
        Matcher matcher = pattern.matcher(property);
        if (matcher.find()) {
        	property = property.substring(matcher.start());
        	int startQuotes = property.indexOf("\"");
        	int endQuotes = property.indexOf("\"",startQuotes+1);
        	return property.substring(startQuotes+1,endQuotes);
        }
        return null;
	}
	
	
	/**
	 * addes default properties (rhq.deploy.dir, rhq.deploy.name, rhq.deploy.id) into given list of InputProperties
	 * @param properties
	 */
	private void addDefaultProperties(ArrayList<InputProperty> properties){
		InputProperty dir = new InputProperty();
		dir.setName(RhqConstants.RHQ_DEPLOY_DIR);
		InputProperty id = new InputProperty();
		id.setName(RhqConstants.RHQ_DEPLOY_ID);
		id.setRequired(false);
		InputProperty name = new InputProperty();
		name.setName(RhqConstants.RHQ_DEPLOY_NAME);
		name.setRequired(false);
		properties.add(dir);
		properties.add(id);
		properties.add(name);
		
		
	} 
	
	
}
