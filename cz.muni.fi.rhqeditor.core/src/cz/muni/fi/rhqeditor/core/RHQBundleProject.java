package cz.muni.fi.rhqeditor.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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




public class RHQBundleProject {
			
	
	/**
	 * creates RHQ bundle project with given name in given location
	 * @param projectName project name
	 * @param bundleName bundle name
	 * @param bundleVersion bundle version
	 * @param location location for creating project. Workspace is used if null.
	 * @throws CoreException if some error occures
	 */
	public void createProject(String projectName, IPath location) throws CoreException{

		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = root.getProject(projectName);
		IStatus status;
		
		
		IProjectDescription description;
		if(location != null){
			description = new ProjectDescription();
			description.setLocation(location);
			description.setName(projectName);
			String[] natures = new String[1];
			natures[0] = RhqConstants.RHQ_NATURE_ID;
			description.setNatureIds(natures);
			project.create(description, progressMonitor);
			
		} else {
			project.create(progressMonitor);
		}
		project.open(progressMonitor);
		description = project.getDescription();
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
		
			status = workspace.validateNatureSet(newNatures);
			if(!status.isOK()){
				System.out.println(status.toString());
				throw new CoreException(status);
			}
	    	description.setNatureIds(newNatures);
		}
	    project.setDescription(description, null);

	    ProjectScanner scanner = new ProjectScanner();
	    scanner.initProject(project);

	    
	    LaunchConfigurationsManager.createNewLaunchConfiguration(projectName);
	}
	
	public void createProjectFromArchive(IPath pathToArchive) throws CoreException, IOException{
		String name = pathToArchive.removeFirstSegments(pathToArchive.segmentCount()-1).removeFileExtension().toString();
		createProject(name, null);
		IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		ArchiveReader.unzipFile(pathToArchive.toString(), newProject.getLocation().toString());
		newProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		
	}
	
	
	
	public void createDefaultRecipe(String projectName, String bundleName, String bundleVersion) throws CoreException{
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		
		String name = (bundleName == null || bundleName.equals("") ? "bundle" : bundleName);
		String version = (bundleVersion == null || bundleVersion.equals("") ? "1.0.0" : bundleVersion);
		
		IFile recipe = project.getFile(RhqConstants.RHQ_RECIPE_FILE);
		if(!recipe.exists()){
			String separator =  System.getProperty("line.separator");
			String str = "<?xml version=\"1.0\"?>"+separator+
					"<project name=\""+projectName+"\" default=\"main\" xmlns:rhq=\"antlib:org.rhq.bundle\">"+
					System.getProperty("line.separator")+"\t<target name=\"main\"/>"+ separator+
					"\t<rhq:bundle name=\""+name+"\" version=\""+version+"\">"+System.getProperty("line.separator") + 
					"\t\t<rhq:deployment-unit name=\"unit\">"+separator + separator + "\t\t</rhq:deployment-unit>" + separator+
					"\t</rhq:bundle>" +separator+
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