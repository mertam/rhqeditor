package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


/**
 * class used for reading content of recipe "deploy.xml"
 * @author syche
 *
 */
public class RecipeReader {
	
	
	/**
	 * reads recipe and return stringBuilder containing it's contain
	 * @param project
	 * @return
	 */
	public static StringBuilder readRecipe(IProject project){	
		StringBuilder sb = new StringBuilder();
		IFile file = project.getFile(RhqConstants.RHQ_RECIPE_FILE);
		if(file != null){
			try {
				//read whole recipe
				InputStreamReader ir = new InputStreamReader(file.getContents());
				
				BufferedReader br = new BufferedReader(ir);
				
				String line;
				while((line = br.readLine()) != null){
					sb.append(line);
				}
			} catch (IOException | CoreException ex){
				return null;
			}
		}
		return sb;
		
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

}
