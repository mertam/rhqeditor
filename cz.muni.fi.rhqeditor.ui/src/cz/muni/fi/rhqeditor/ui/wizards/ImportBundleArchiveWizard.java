package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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

public class ImportBundleArchiveWizard extends Wizard implements IWorkbenchWizard{

	
	private ImportBundleArchiveWizardPage1 page1;
	
	private IStructuredSelection selection;
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {		
		this.selection = selection;
	}

	@Override
	public void addPages() {
		page1 = new ImportBundleArchiveWizardPage1("Import bundle");
		page1.setSelection(selection);
		super.addPage(page1);
		super.addPages();
	}
	
	@Override
	public boolean performFinish() {
		page1.saveComboState();
		
		RhqBundleProject project = new RhqBundleProject();
		String archive = page1.getArchivePath();
		if(archive == null)
			return false;
		try{
			IPath path = new Path(archive);
			project.createProjectFromBundle(path);
			String name = path.removeFirstSegments(path.segmentCount()-1).removeFileExtension().toString();
			if(page1.getWorkingSets().length > 0) {
				 PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(ResourcesPlugin.getWorkspace().getRoot().getProject(name), page1.getWorkingSets());
			}
		} catch (CoreException e){
			ErrorDialog.openError(new Shell(), "Project creationg error", e.getMessage(),e.getStatus());
		} catch (IOException e) {
			ErrorDialog.openError(new Shell(), "Project importing error", e.getMessage(),new Status(IStatus.ERROR,
					RhqConstants.PLUGIN_UI_ID, "Import error occured"));
		}
		return true;
	}
	
	@Override
	public boolean needsProgressMonitor() {
		return true;
	}
}
