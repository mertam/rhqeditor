package cz.muni.fi.rhqeditor.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import utils.RhqConstants;
import utils.StandaloneDeployer;


public class RhqLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
//		configuration.getWorkingCopy().remo
		StandaloneDeployer sd = new StandaloneDeployer();
		sd.deploy(configuration);
		
	}
	
	
	
	/**
	 * creates new ILaunchConfiguration of given name. Removes existing having same name
	 * @param projectName
	 * @throws CoreException
	 */
	public static void createNewLaunchConfiguration(String projectName) throws CoreException{
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		   ILaunchConfigurationType type =
		      manager.getLaunchConfigurationType(RhqConstants.RHQ_LAUNCH_CONFIGURATION_ID);
		ILaunchConfiguration[] configurations =
		      manager.getLaunchConfigurations();
		   for (int i = 0; i < configurations.length; i++) {
		      ILaunchConfiguration configuration = configurations[i];
		      if (configuration.getName().equals(projectName)) {
		          configuration.delete();
		          break;
		       }

		   }
		   
		ILaunchConfigurationWorkingCopy newConfig = type.newInstance(null, projectName);
		newConfig.setAttribute(RhqConstants.RHQ_LAUNCH_ATTR_PROJECT, projectName);
		System.out.println("saving with "+projectName+"."+RhqConstants.RHQ_LAUNCH_ATTR_PROJECT);
		newConfig.doSave();
	}
	
	

}
