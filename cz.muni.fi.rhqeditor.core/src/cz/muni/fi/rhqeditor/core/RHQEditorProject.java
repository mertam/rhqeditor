package cz.muni.fi.rhqeditor.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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

import cz.muni.fi.rhqeditor.core.launch.LaunchConfigurationsManager;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;




public class RHQEditorProject {
			
	/**
	 * Constructor creates project of given name in workspace
	 * 
	 * @param strProjectName - name of project
	 * @throws CoreException - if some error occures during creating
	 */
	public RHQEditorProject(String strProjectName, String bundleName, String bundleVersion) throws CoreException {
		
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = root.getProject(strProjectName);
		
		project.create(progressMonitor);
		project.open(progressMonitor);
		
	
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		
		boolean addRhqNature = true;
		
		//checks whether project already has rhq nature. 
		//(In case of creating project which was deleted from workspace, not from file system)
		for(String natrue: natures){
			if(natrue.equals(RhqConstants.RHQ_NATURE_ID))
				addRhqNature = false;
		}
		if(addRhqNature){
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = RhqConstants.RHQ_NATURE_ID;
		
			IStatus status = workspace.validateNatureSet(newNatures);
			if(!status.isOK()){
				System.out.println(status.toString());
				throw new CoreException(status);
			}
	    	description.setNatureIds(newNatures);
		}
	    project.setDescription(description, null);
	    this.createDefaultRecipe(strProjectName, bundleName, bundleVersion);
	    
	    ProjectScanner scanner = new ProjectScanner();
	    scanner.initProject(project);

	    
	    LaunchConfigurationsManager.createNewLaunchConfiguration(strProjectName);
		
	}
	
	
	public RHQEditorProject(String strProjectName, IPath pathProjectPath,String bundleName, String bundleVersion) throws CoreException{
		this(strProjectName, bundleName, bundleVersion);
		//TODO dokoncit presun projektu
	}
	
	
	private void createDefaultRecipe(String projectName, String bundleName, String bundleVersion) throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		
		String name = (bundleName == null || bundleName.equals("") ? "bundle" : bundleName);
		String version = (bundleVersion == null || bundleVersion.equals("") ? "1.0.0" : bundleVersion);
		
		IFile recipe = project.getFile("deploy.xml");
		if(!recipe.exists()){
		
			String str = "<?xml version=\"1.0\"?>"+ System.getProperty("line.separator")+
					"<project name=\""+projectName+"\" default=\"main\" xmlns:rhq=\"antlib:org.rhq.bundle\">"+
					System.getProperty("line.separator")+"\t<target name=\"main\"/>"+  System.getProperty("line.separator")+
					"\t<rhq:bundle name=\""+name+"\" version=\""+version+"\">"+System.getProperty("line.separator") +
					"\t</rhq:bundle>" + System.getProperty("line.separator")+
					"</project>";
			InputStream is = new ByteArrayInputStream(str.getBytes());
			recipe.create(is, true, null);
		}
	    //create default deploy dir 
	    IFolder folder = project.getFolder(".bin");
	    if(!folder.exists())
	    	folder.create(true, true, null);
	    
	    

	}
	
	
    

    
	
	
	
	
}