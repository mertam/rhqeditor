package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class InputPropertiesManager {

	private IProject fProject;
	
	public InputPropertiesManager(IProject proj){
		fProject = proj;
	}
	
	/**
	 * @return StringBuffer containing while content of recipe
	 */
	private StringBuilder readRecipe(){	
		StringBuilder sb = new StringBuilder();
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
			} catch (Exception e){}
		}
		return sb;
		
	}

	/**
	 * finds all names of input properties in recipe
	 * @return
	 */
	public ArrayList<String> getInputPropertiesFromRecipe(){
		//collection of intput properties that should be displayed
		ArrayList<String> propertyNames = new ArrayList<String>();
//		propertyNames.add(DEPLOY_DIR);
		//find all input properties in recipe
		StringBuilder sb = readRecipe();
		
		//find properties
		Pattern pattern = Pattern.compile(RhqConstants.RHQ_TYPE_INPUT_PROPERTY + "\\s+[[\\w|\\s]*|=|\"|\\s]*name\\s*=\\s*\"");
        System.out.println(pattern.toString());
        Matcher matcher = pattern.matcher(sb);

        while (matcher.find()) {
        	//rhq:input-property....name="propname"...
        	String toAdd = sb.substring(matcher.start());
        	//name="propname"
        	toAdd = toAdd.substring(toAdd.indexOf("name"));
        	//propname
        	int startQuotes = toAdd.indexOf("\"");
        	int endQuotes = toAdd.indexOf("\"",startQuotes+1);
        	toAdd = toAdd.substring(startQuotes+1,endQuotes);
        	
        	System.out.println(toAdd);
        	propertyNames.add(toAdd);
        	System.out.println(matcher.start());
        	System.out.println("found");
        
        }	
		return propertyNames;
	}
	
	
	
}
