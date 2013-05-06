package cz.muni.fi.rhqeditor.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * Singleton class, can be accessible via method getInstance
 * @author syche
 *
 */
public enum ExtractorProvider {

		INSTANCE;
	    private Map<IProject,RhqPathExtractor> map = new HashMap<>();
	    
	    private ExtractorProvider() {}
	 
	    /**
	     * returns RhaPathExtractor for given IProject
	     * @param project
	     * @return null if there is no extractor for this project
	     */
	    public RhqPathExtractor getExtractor(IProject project) {
	    	return map.get(project);
	    }
	    
	    
	    /**
	     * puts into extractor into cache for given project. Repleces existing one.
	     * @param project
	     * @param extractor
	     */
	    public void attachExtractorToProject(IProject project, RhqPathExtractor extractor){
	    	
	    	map.put(project,extractor);
	    }
	    	    
	    public void deleteExtractorOfProject(IProject project) {
	    	map.remove(project);
	    }
	    /**
	     * return array of all listed projects
	     * @return
	     */
		public String[] listProjects(){
			ArrayList<String> projects = new ArrayList<>();
			for(IProject proj: map.keySet()){
				projects.add(proj.getName());
			}
			
			Collections.sort(projects);
			return projects.toArray(new String[projects.size()]);
		}
}