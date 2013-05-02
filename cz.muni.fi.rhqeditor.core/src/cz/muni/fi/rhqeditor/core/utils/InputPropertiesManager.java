package cz.muni.fi.rhqeditor.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;


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
	
	/**
	 * map to store ant property values
	 * stored values has format ${property} = value
	 */
	private HashMap<String,String> fPropertyValues;
	
	
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
		String content;
		RecipeReader reader = new RecipeReader(fProject);
		content = reader.readRecipe(forceSearchOnSavedFile);
		String namespacePrefix = reader.getRhqNamespacePrefix();
		
		//find properties
		Pattern pattern = 
				Pattern.compile("("+ namespacePrefix +
				RhqConstants.RHQ_TYPE_INPUT_PROPERTY + ")"+     				//rhq:input-property
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" +			//some attribute*
				"\\s+name\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]+\"" +    			//name="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				 "\\s*/?\\s*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);		// /> or >
        Matcher matcher = pattern.matcher(content);
        
        ArrayList<InputProperty>  result = new ArrayList<>();
        addDefaultProperties(result);
        int commentStartPosition, commentEndPosition;
        while(matcher.find()){
        	if ((commentEndPosition = content.substring(matcher.start(),content.length()).indexOf("-->")) > -1) {
				commentStartPosition = content.substring(matcher.start(),content.length()).indexOf("<!--");
				if(true && commentStartPosition > commentEndPosition){
					// commented property
					continue;
				}
        	}	
        	
        
        	
        	InputProperty property = new InputProperty();
        	String toAdd = content.substring(matcher.start());
        	String name = getAttributeValue(toAdd, "name");
        	
        	//check if name is propertized
        	if(name.matches(".*\\$\\{.*\\}.*")) {
        		name = resolveNameWithProperty(name);
        	}
        	
        	//set name
        	property.setName(name == null ? getAttributeValue(toAdd, "name") : name);
        	
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
	 * adds default properties (rhq.deploy.dir, rhq.deploy.name, rhq.deploy.id) into given list of InputProperties
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
	
	
	
	/**
	 * returns givenName vith replaces value of ${property}, if there is this property defined in recipe.
	 * @param givenName
	 * @return
	 */
	public String resolveNameWithProperty( String givenName) {
		if (!givenName.matches(".*\\$\\{.*\\}.*"))
			return givenName;
		
		if(fPropertyValues == null)
			initalizeAntPropertyMap();
		
		//replace all possible ${...} with known properties
		String workingCopy = givenName;
		String replacement;
		for(String property: fPropertyValues.keySet()) {
			replacement = fPropertyValues.get(property);
			workingCopy = workingCopy.replaceAll(Pattern.quote(property), replacement);
		}
		
		return workingCopy;
	}
	
	
	/**
	 * initializes map of given ant properties
	 */
	public void initalizeAntPropertyMap() {
		
		fPropertyValues = new HashMap<>();
		String content;
		RecipeReader reader = new RecipeReader(fProject);
		content = reader.readRecipe(false);
		
		//TODO make this readable...
		
		//find ant properties, must contain property name="..." value="..."
		Pattern pattern = 
				Pattern.compile("(<\\s*property)"+     							//property, [^-] filters out rhq:input-property
				"(" +															// (
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				"(\\s+\\value\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" +        //value="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				"\\s+name\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]+\"" +    			//name="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				")|("+															// ) OR (
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				"(\\s+\\name\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" +         //name="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				"\\s+value\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]+\"" +    			//value="..."
				"(\\s+\\w+\\s*=\"[\\w\\d\\s\\p{Punct}&&[^\"]]*\")*" + 			//some attribute*
				")"+															// ) 
				"\\s*/?\\s*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);		// /> or >
        Matcher matcher = pattern.matcher(content);
		
        int commentStartPosition, commentEndPosition;
        while( matcher.find()) {
        	if ((commentEndPosition = content.substring(matcher.start(),content.length()).indexOf("-->")) > -1) {
				commentStartPosition = content.substring(matcher.start(),content.length()).indexOf("<!--");
				if(true && commentStartPosition > commentEndPosition){
					// commented property
					continue;
				}
        	}	
        	
        	String toAdd = content.substring(matcher.start());
        	String name = getAttributeValue(toAdd, "name");
        	String value = getAttributeValue(toAdd, "value");
        	//if value wasn't found in attribute
        	if ( value == null) {
        		Pattern insertedPattern = Pattern.compile("<\\s*/\\s*property\\s*>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        		Matcher insertedMather = insertedPattern.matcher(toAdd);
        		if (insertedMather.find()) {
        			value = toAdd.substring(0, insertedMather.start());
        			//remove <property name=... >
        			value = value.substring(value.indexOf(">")+1);
        		}
        	}
        	
        	if (value != null)
        		fPropertyValues.put("${" + name + "}", value.trim());
        }
	}
	
	
	
	public HashMap<String, String > getPropertyMap() {
		return fPropertyValues;
	}
	
	public LinkedList<String> getPropertiesFromString (String from) {
		LinkedList<String> result = new LinkedList<String>();
		int endIndex;
		for(String s: from.split("\\$\\{") ) {
			endIndex = s.indexOf("}");
			if(!s.isEmpty() && endIndex > -1) {
				result.add(s.substring(0,endIndex));
			}
		}
		return result;
	}
	
	
}
