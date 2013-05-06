package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

import cz.muni.fi.rhqeditor.core.RhqBundleProject;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import cz.muni.fi.rhqeditor.ui.UiActivator;

public class NewProjectWizard extends Wizard implements IWorkbenchWizard{

	private NewProjectWizardPage1	page1;
	private IStructuredSelection 	selection;

	
	public NewProjectWizard() {
		super();
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
	
	@Override
	public void addPages() {
		
		page1  = new NewProjectWizardPage1("ProjectWizardPage1");
		page1.setSelection(selection);
		page1.setMessage("Create new RHQ bundle");
		addPage(page1);
	}

	@Override
	public boolean performFinish() {
		try {
			RhqBundleProject project = new RhqBundleProject();
			IPath path = page1.getLocationPath();
			if (path == null || path.equals(ResourcesPlugin.getWorkspace().getRoot().getLocation())) {
				project.createProject(page1.getProjectName(), null);
				project.createDefaultRecipe(page1.getProjectName(),page1.getBundleName(),page1.getBundleVersion());
			} else {
				project.createProject(page1.getProjectName(), path);
				project.createDefaultRecipe(page1.getProjectName(),page1.getBundleName(),page1.getBundleVersion());
			}
			PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(page1.getProjectHandle(), page1.getWorkingSets());
		
		} catch (CoreException e) {
			ErrorDialog.openError(new Shell(), "Project creationg error", e.getMessage(),e.getStatus());
			UiActivator.getLogger().log(new Status(IStatus.WARNING,RhqConstants.PLUGIN_UI_ID,"NewProjectWizard.performFinish " + e.getMessage()));
		}
		return true;
	}

}