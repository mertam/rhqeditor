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

import cz.muni.fi.rhqeditor.core.rhqmodel.RhqModelReader;


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
	public static StringBuilder readRecipe(IProject project) {	
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
			} catch (IOException | CoreException e){
				return new StringBuilder();
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
	public static HashSet<String> getReferencedFiles(IProject project){
		HashSet<String> result = new HashSet<>();
		String content = readRecipe(project).toString();
		String namespace = RhqModelReader.getRhqNamespacePrefix(content);
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
