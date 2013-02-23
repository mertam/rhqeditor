package cz.muni.fi.rhqeditor.core.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
   


public class RHQEditorNature implements IProjectNature {

	  private IProject project;
	
	  @Override
	  public void configure() throws CoreException {
		  System.err.println("config called");
	
	  }
	
	  @Override
	  public IProject getProject() {
		 System.err.println("get called");
	     return project;
	  }
	  
	  @Override
	  public void setProject(IProject value) {
		  System.err.println("set called");
	     project = value;
	  }

	  @Override
	  public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub
			
	  }
   }