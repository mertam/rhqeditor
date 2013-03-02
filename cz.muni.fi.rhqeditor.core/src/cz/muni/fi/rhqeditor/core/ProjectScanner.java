package cz.muni.fi.rhqeditor.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import utils.ExtractorProvider;
import utils.RhqConstants;
import utils.RhqPathExtractor;
import cz.muni.fi.rhqeditor.core.listeners.RecipeChangeListener;

public class ProjectScanner {
	
	public final String NATURE_ID = "cz.muni.fi.rhqeditor.natures.rhqeditornature";
	
	private RecipeChangeListener fListener = null;
	
	/**
	 * scans all projects in workspace, finds ones having RHQ nature, add listener to them and ...
	 */
	
	public ProjectScanner(){
	}
	
	/**
	 * calls init on all projects existing in workspace
	 */
	public void initAllProjects(){
		for(IProject proj: ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			initProject(proj);
		}
	}
	
	/**
	 * setup for RHQ project after opening. Sets up listener and adds it's extractor
	 * @param project
	 */
	public void initProject(IProject project){
		
		ExtractorProvider extProvider = ExtractorProvider.getInstance();
		
		if(fListener == null){
			RecipeChangeListener listener = new RecipeChangeListener();
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
			fListener = listener;
		}
		
		//terminate if project is already initialized
		if(extProvider.getMap().keySet().contains(project))
			return;
		
		try {
			if(project.isOpen() && project.hasNature(RhqConstants.RHQ_NATURE_ID)){
				RhqPathExtractor extractor = new RhqPathExtractor(project);
				extProvider.attachExtractorToProject(project, extractor);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
