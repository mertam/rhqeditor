package cz.muni.fi.rhqeditor.core.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.osgi.service.prefs.BackingStoreException;

import cz.muni.fi.rhqeditor.core.Activator;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;


public class RhqLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if(!configuration.supportsMode(mode))
			return;
		

		StandaloneDeployer sd = new StandaloneDeployer();
		sd.deploy(configuration, monitor);
		
		//store name of this configuration as last used
		String projectName = configuration.getAttribute(
		RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
		RhqConstants.NOT_FOUND);
		if (projectName.equals(RhqConstants.NOT_FOUND))
			return;

		IProject project = ResourcesPlugin.getWorkspace().getRoot()
		.getProject(projectName);
		//save last used configuration

		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences prefs = projectScope.getNode(RhqConstants.RHQ_PROPERTY_NODE);
		prefs.put(RhqConstants.RHQ_LAST_USED_CONFIGURATION, configuration.getName());
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			Activator.getLog().log(new Status(IStatus.WARNING,RhqConstants.PLUGIN_CORE_ID,"RhqLaunchConfigurationDelegate.launch " + e.getMessage()));
		}
		
	}
	
	
	
	
	
	

}
