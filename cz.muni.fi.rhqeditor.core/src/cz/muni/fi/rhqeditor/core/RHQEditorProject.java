package cz.muni.fi.rhqeditor.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IMarker;



public class RHQEditorProject {
	
	private static final String 	PROJECT_NATURE_ID = "cz.muni.fi.rhqeditor.natures.rhqeditornature";
	private IProject 				iprojProject = null;	
	
	//path to toot project dir, if it's not stored in workspace directory

	
		
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
		newNatures[natures.length] = PROJECT_NATURE_ID;
		
		IStatus status = workspace.validateNatureSet(newNatures);
		if(!status.isOK()){
			System.out.println(status.toString());
			throw new CoreException(status);
		}
	    description.setNatureIds(newNatures);
	    project.setDescription(description, null);
	    this.createDefaultRecipe(strProjectName);
	    
	    ProjectScanner scanner = new ProjectScanner();
//	    workspace.addResourceChangeListener(new RecipeChangeListener());
	    this.iprojProject = project;
		
	}
	
	
	public RHQEditorProject(String strProjectName, IPath pathProjectPath) throws CoreException{
		this(strProjectName);
		//TODO dokoncit presun projektu
	}
	
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		System.out.println("project finalize");
		super.finalize();
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
	    
//	    IMarker marker = recipe.createMarker(IMarker.PROBLEM);
//		marker.setAttribute(IMarker.MESSAGE, "This a a task");
//		marker.setAttribute(IMarker.LINE_NUMBER, 1);
//		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
//        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
//        
//        IMarker marker2 = recipe.createMarker(IMarker.PROBLEM);
//		marker2.setAttribute(IMarker.MESSAGE, "This a a task");
//		marker2.setAttribute(IMarker.LINE_NUMBER, 1);
//		marker2.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
//        marker2.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

	}
	
    

    
	
	
	
	
}