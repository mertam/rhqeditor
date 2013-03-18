package cz.muni.fi.rhqeditor.core.utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import cz.muni.fi.rhqeditor.core.rhqmodel.RhqModelReader;


public class InputPropertiesManager {

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
		Pattern pattern = Pattern.compile(reader.getRhqNamespacePrefix() + RhqConstants.RHQ_TYPE_INPUT_PROPERTY + "\\s+[[\\w|\\s]*|=|\"|\\s]*name\\s*=\\s*\"");
        Matcher matcher = pattern.matcher(sb);
        
        ArrayList<InputProperty>  result = new ArrayList<>();
        addDefaultProperties(result);
        while(matcher.find()){
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
        	property.setValue(getAttributeValue(toAdd, "default"));
        	result.add(property);
        	
        }
		return result;
	}
	
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
	
	private void addDefaultProperties(ArrayList<InputProperty> properties){
		InputProperty dir = new InputProperty();
		dir.setName(RhqConstants.RHQ_DEPLOY_DIR);
		InputProperty id = new InputProperty();
		id.setName(RhqConstants.RHQ_DEPLOY_ID);
		InputProperty name = new InputProperty();
		name.setName(RhqConstants.RHQ_DEPLOY_NAME);
		properties.add(dir);
		properties.add(id);
		properties.add(name);
		
		
	} 
	
	
}
