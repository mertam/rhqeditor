package cz.muni.fi.rhqeditor.core.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
   


public class RhqBundleEditorNature implements IProjectNature {

	  private IProject project;
	
	  @Override
	  public void configure() throws CoreException {
	  }
	
	  @Override
	  public IProject getProject() {
	     return project;
	  }
	  
	  @Override
	  public void setProject(IProject value) {
	     project = value;
	  }

	  @Override
	  public void deconfigure() throws CoreException {
	  }
   }