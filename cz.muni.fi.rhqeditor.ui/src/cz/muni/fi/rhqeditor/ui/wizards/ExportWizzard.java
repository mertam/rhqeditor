package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
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
			if (!checkNature())
				throw new CoreException(Status.CANCEL_STATUS);
		} catch (CoreException e) {
			fPage1.setInactive();
		}
		super.addPages();
	}

	@Override
	public boolean performFinish() {
		BundleExport export = new BundleExport(fProject, fPage1.getTargetFile());
		try {
			export.ExportBundle();
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
