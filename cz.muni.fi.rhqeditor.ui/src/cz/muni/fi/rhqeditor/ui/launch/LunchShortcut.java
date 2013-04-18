package cz.muni.fi.rhqeditor.ui.launch;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import cz.muni.fi.rhqeditor.core.launch.LaunchConfigurationsManager;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;
import cz.muni.fi.rhqeditor.ui.editor.RhqEditor;

/**
 * 
 * @author syche
 * 
 */
public class LunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {
		IProject project = getProjectFromSelection(selection);
		launch(project);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		if (editor instanceof RhqEditor) {
			IProject project = ((RhqEditor) editor).getProject();
			launch(project);
		}

	}

	private ILaunchConfiguration[] getLaunchConfigurations(IProject project) {
		return LaunchConfigurationsManager.getConfigurationsForProject(project);
	}

	private void launch(IProject proj) {
		ILaunchConfiguration configs[] = getLaunchConfigurations(proj);
		if (configs.length < 1) {
			ErrorDialog errorDialog = new ErrorDialog(new Shell(),
					"Launch error",
					"No launch configuration attached to project", new Status(
							IStatus.ERROR, RhqConstants.PLUGIN_UI_ID, 
							"No launch configuration attached to project "+proj.getName()),
					IStatus.ERROR);
			errorDialog.create();
			errorDialog.open();

		} else {
			try {
				configs[0].launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	}

	private IProject getProjectFromSelection(ISelection selection) {
		IProject project = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();

			if (firstElement instanceof File) {
				File file = (File) firstElement;
				project = file.getProject();
			} else if (firstElement instanceof IAdaptable) {
				project = (IProject) ((IAdaptable) firstElement)
						.getAdapter(IProject.class);
			}
		}
		return project;
	}
}
