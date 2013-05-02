package cz.muni.fi.rhqeditor.ui.wizards;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import cz.muni.fi.rhqeditor.core.utils.BundleExport;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

public class ExportBundleWizard extends Wizard implements IWorkbenchWizard {

	private IProject fProject = null;
	private ExportBundleWizardPage1 fPage1;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {

		Object firstElement = selection.getFirstElement();
		if(firstElement instanceof IResource) {
			fProject = ((IResource) firstElement).getProject();
			return;
		}
		if (firstElement instanceof IAdaptable) {
			fProject = (IProject) ((IAdaptable) firstElement)
					.getAdapter(IProject.class);
		}
		
	}
	
	
	@Override
	public void addPages() {
		fPage1 = new ExportBundleWizardPage1("exportPage", "Export RHQ bundle project into bundle", null);
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
			if(!export.exportBundle(false)) {
				Shell shell = new Shell();
		    	shell.setSize(300, 100);
		        MessageBox messageDialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.NO | SWT.YES);
		        messageDialog.setText("Overwrite file");
		        messageDialog.setMessage("File "+ fPage1.getTargetFile() +" exists, overwrite?");
		        
		        int result = messageDialog.open();
		        
				switch(result){
				case SWT.YES: 
					export.exportBundle(true);
					return true;
				case SWT.NO: return true;
				case SWT.CANCEL: return false;
			}
			
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
