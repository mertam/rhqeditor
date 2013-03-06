package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;


public class ExtractorProvider {


	    private static final ExtractorProvider instance = new ExtractorProvider();
	    private Map<IProject,RhqPathExtractor> map;
	    
	    private ExtractorProvider() {
	    	map = new HashMap<IProject, RhqPathExtractor>();
	    }
	 
	    public static ExtractorProvider getInstance() {
	        return instance;
	    }
	    
	    public void attachExtractorToProject(IProject project, RhqPathExtractor extractor){
	    	map.put(project,extractor);
	    }
	    
	    public Map<IProject,RhqPathExtractor> getMap(){
	    	return Collections.unmodifiableMap(map);
	    }	    
	
}