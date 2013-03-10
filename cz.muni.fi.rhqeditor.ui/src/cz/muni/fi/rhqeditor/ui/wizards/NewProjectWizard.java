package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.tools.ant.taskdefs.Sync.MyCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import cz.muni.fi.rhqeditor.core.RHQEditorProject;
import cz.muni.fi.rhqeditor.ui.UiActivator;

public class NewProjectWizard extends Wizard implements IWorkbenchWizard{
	/**
	 * @uml.property  name="page1"
	 * @uml.associationEnd  
	 */
	private NewProjectWizardPage1 				page1;

	
	public NewProjectWizard()
	{
		super();
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
	@Override
	public void addPages(){
		
		page1 = new NewProjectWizardPage1("page 1","RHQ title",UiActivator.getImageDescriptor("icons/rhq.gif"));
		addPage(page1);
	}

	@Override
	public boolean performFinish() {
		try{
			RHQEditorProject project;
			
			if(page1.getNewProjectPath() == null){
				project = new RHQEditorProject(page1.getNewProjectName());
			}else{
				IPath path = Path.fromOSString(page1.getNewProjectPath());
				project = new RHQEditorProject(page1.getNewProjectName(),path);
			}
		
		}catch(CoreException ex)
		{
			System.err.println(ex);
		}
		return true;
	}

}