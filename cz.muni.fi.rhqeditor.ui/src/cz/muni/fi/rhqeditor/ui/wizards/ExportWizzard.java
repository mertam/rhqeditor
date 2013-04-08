package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import cz.muni.fi.rhqeditor.core.BundleExport;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

public class ExportWizzard extends Wizard implements IWorkbenchWizard {

	private IProject fProject = null;
	private ExportWizzardPage1 fPage1;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				fProject = (IProject) ((IAdaptable) firstElement)
						.getAdapter(IProject.class);
			}
		}
		// TODO Auto-generated method stub

	}

	@Override
	public void addPages() {
		fPage1 = new ExportWizzardPage1("exportPage", "Export RHQ bundle", null);
		addPage(fPage1);
		try {
			if(checkNature())
				fPage1.setProject(fProject.getName());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		String projectName = fPage1.getProject();
		
		BundleExport export = new BundleExport(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName), fPage1.getTargetFile());
		try {
			int result = export.ExportBundle();
			switch(result){
			case SWT.OK: return true;
			case SWT.NO: return true;
			case SWT.CANCEL: return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private boolean checkNature() throws CoreException {
		if (fProject == null || !fProject.hasNature(RhqConstants.RHQ_NATURE_ID))
			return false;
		return true;
	}
}
