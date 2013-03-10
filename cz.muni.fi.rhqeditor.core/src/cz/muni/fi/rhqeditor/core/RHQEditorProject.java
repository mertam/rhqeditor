package cz.muni.fi.rhqeditor.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import utils.RhqConstants;



public class RHQEditorProject {
			
	/**
	 * Constructor creates project of given name in workspace
	 * 
	 * @param strProjectName - name of project
	 * @throws CoreException - if some error occures during creating
	 */
	public RHQEditorProject(String strProjectName) throws CoreException {
		
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = root.getProject(strProjectName);
		
		project.create(progressMonitor);
		project.open(progressMonitor);
		
	
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		
		System.out.println("point 1");

		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = RhqConstants.RHQ_NATURE_ID;
		
	
		
		IStatus status = workspace.validateNatureSet(newNatures);
		if(!status.isOK()){
			System.out.println(status.toString());
			throw new CoreException(status);
		}
	    description.setNatureIds(newNatures);
	    project.setDescription(description, null);
	    this.createDefaultRecipe(strProjectName);
	    
	    ProjectScanner scanner = new ProjectScanner();
	    scanner.initProject(project);

	    
	    RhqLaunchConfigurationDelegate.createNewLaunchConfiguration(strProjectName);
		
	}
	
	
	public RHQEditorProject(String strProjectName, IPath pathProjectPath) throws CoreException{
		this(strProjectName);
		//TODO dokoncit presun projektu
	}
	
	
	private void createDefaultRecipe(String projectName) throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		
		IFile recipe = project.getFile("deploy.xml");
		
		
	    String str = "<?xml version=\"1.0\"?>"+ System.getProperty("line.separator")+
	    "<project name=\"test-bundle\" default=\"main\" xmlns:rhq=\"antlib:org.rhq.bundle\">"+
	    System.getProperty("line.separator")+"</project>";
	    InputStream is = new ByteArrayInputStream(str.getBytes());
	    recipe.create(is, true, null);
	    

	}
	
	
    

    
	
	
	
	
}