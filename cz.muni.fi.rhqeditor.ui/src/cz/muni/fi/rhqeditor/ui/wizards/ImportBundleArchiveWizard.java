package cz.muni.fi.rhqeditor.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import cz.muni.fi.rhqeditor.core.RHQBundleProject;

public class ImportBundleArchiveWizard extends Wizard implements IWorkbenchWizard{

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
		
	}

	@Override
	public void addPages() {
		ImportBundleArchiveWizardPage1 page1 = new ImportBundleArchiveWizardPage1("Import RHQ bundle");
		super.addPage(page1);
		super.addPages();
	}
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		RHQBundleProject proj = new RHQBundleProject();
		return false;
	}

}
