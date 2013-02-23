package cz.muni.fi.rhqeditor.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import cz.muni.fi.rhqeditor.core.listeners.RecipeChangeListener;

public class ProjectScanner {
	
	public final String NATURE_ID = "cz.muni.fi.rhqeditor.natures.rhqeditornature";
	
	/**
	 * scans all projects in workspace, finds ones having RHQ nature, add listener to them and ...
	 */
	public ProjectScanner(){
		
		ExtractorProvider extProvider = ExtractorProvider.getInstance();
		
		for(IProject proj: ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			try {
				//skip listed projects
				if(extProvider.getMap().keySet().contains(proj))
					continue;
				
				//sets extractor and listener to all projects with RHQ nature, which havn't been set before
				if(proj.hasNature(NATURE_ID)){
					RecipeChangeListener listener = new RecipeChangeListener();
					ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
					listener.setProject(proj);
					RhqPathExtractor extractor = new RhqPathExtractor(proj);
					
					listener.setExtractor(extractor);
					extProvider.attachExtractorToProject(proj, extractor);
					
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
