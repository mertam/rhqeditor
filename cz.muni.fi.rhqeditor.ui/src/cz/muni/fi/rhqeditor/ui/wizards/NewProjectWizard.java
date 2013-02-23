package cz.muni.fi.rhqeditor.ui.wizards;

import cz.muni.fi.rhqeditor.core.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addPages(){
		page1 = new NewProjectWizardPage1("page 1");
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