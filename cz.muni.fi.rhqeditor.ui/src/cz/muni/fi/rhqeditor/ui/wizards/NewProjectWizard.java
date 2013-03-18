package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
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
				project = new RHQEditorProject(page1.getNewProjectName(),page1.getBundleName(),page1.getBundleVersion());
			}else{
				IPath path = Path.fromOSString(page1.getNewProjectPath());
				project = new RHQEditorProject(page1.getNewProjectName(),path, page1.getBundleName(), page1.getBundleVersion());
			}
		
		}catch(CoreException ex)
		{
			System.err.println(ex);
		}
		return true;
	}

}