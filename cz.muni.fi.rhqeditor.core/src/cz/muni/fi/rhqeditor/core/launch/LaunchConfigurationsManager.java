package cz.muni.fi.rhqeditor.core.launch;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import cz.muni.fi.rhqeditor.core.utils.RhqConstants;


/**
 * class provides basic operations for manipulating with Launch configurations
 * 
 * @author syche
 * 
 */
public class LaunchConfigurationsManager {

	/**
	 * returns array of all possible Launch configurations of project
	 * 
	 * @param proj
	 * @return
	 */
	public static ILaunchConfiguration[] getConfigurationsForProject(
			IProject proj) {
		ArrayList<ILaunchConfiguration> configs = new ArrayList<>();
		try {
			ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(RhqConstants.RHQ_LAUNCH_CONFIGURATION_ID);
			ILaunchConfiguration[] configurations = manager
					.getLaunchConfigurations(type);

			for (ILaunchConfiguration cnf : configurations) {
				if (cnf.getAttribute(RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
						RhqConstants.NOT_FOUND).equals(proj.getName())) {
					configs.add(cnf);
				}
			}
		} catch (CoreException e) {
			return configs.toArray(new ILaunchConfiguration[configs.size()]);
		}
System.out.println("");
		return configs.toArray(new ILaunchConfiguration[configs.size()]);
	}

	/**
	 * creates new ILaunchConfiguration of given name. Overwrite existing having
	 * same name. Configuration will use default deployer and deploy directory project/build
	 * 
	 * @param projectName
	 * @throws CoreException
	 */
	public static void createNewLaunchConfiguration(String projectName)
			throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager
				.getLaunchConfigurationType(RhqConstants.RHQ_LAUNCH_CONFIGURATION_ID);
		ILaunchConfiguration[] configurations = manager
				.getLaunchConfigurations();
		for (int i = 0; i < configurations.length; i++) {
			ILaunchConfiguration configuration = configurations[i];
			if (configuration.getName().equals(projectName)) {
				configuration.delete();
				break;
			}

		}

		ILaunchConfigurationWorkingCopy newConfig = type.newInstance(null,
				projectName);
		newConfig.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
				projectName);
		newConfig.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DIRECTORY, 
				true);
		newConfig.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DEPLOYER,
				true);
		newConfig.doSave();
	}

	/**
	 * removes all launch configurations attached to given project
	 * 
	 * @param proj
	 */
	public static void removeConfigurationsOfProject(IProject proj) {
		try {
			for (ILaunchConfiguration config : getConfigurationsForProject(proj)) {
				config.delete();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
