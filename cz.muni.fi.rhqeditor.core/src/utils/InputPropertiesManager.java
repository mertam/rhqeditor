package utils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

public class InputPropertiesManager {

	private IProject fProject;
	
	public InputPropertiesManager(IProject proj){
		fProject = proj;
	}
	
	/**
	 * @return StringBuffer containing while content of recipe
	 */


	/**
	 * finds all names of input properties in recipe
	 * @return
	 */
	public ArrayList<String> getInputPropertiesFromRecipe(){
		//collection of intput properties that should be displayed
		ArrayList<String> propertyNames = new ArrayList<String>();
		
//		propertyNames.add(DEPLOY_DIR);
		//find all input properties in recipe
		StringBuilder sb = RecipeReader.readRecipe(fProject);
		
		//find properties
		Pattern pattern = Pattern.compile(RhqConstants.RHQ_TYPE_INPUT_PROPERTY + "\\s+[[\\w|\\s]*|=|\"|\\s]*name\\s*=\\s*\"");
        Matcher matcher = pattern.matcher(sb);

        //add default properties
        propertyNames.add(RhqConstants.RHQ_DEPLOY_NAME);
        propertyNames.add(RhqConstants.RHQ_DEPLOY_ID);
        propertyNames.add(RhqConstants.RHQ_DEPLOY_DIR);
        
        while (matcher.find()) {
        	//rhq:input-property....name="propname"...
        	String toAdd = sb.substring(matcher.start());
        	//name="propname"
        	toAdd = toAdd.substring(toAdd.indexOf("name"));
        	//propname
        	int startQuotes = toAdd.indexOf("\"");
        	int endQuotes = toAdd.indexOf("\"",startQuotes+1);
        	toAdd = toAdd.substring(startQuotes+1,endQuotes);
        	propertyNames.add(toAdd);
        }	
		return propertyNames;
	}
	
	
	
}
