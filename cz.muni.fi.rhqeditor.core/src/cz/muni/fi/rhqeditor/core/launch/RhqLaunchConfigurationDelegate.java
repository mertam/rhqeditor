package cz.muni.fi.rhqeditor.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;


public class RhqLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
//		configuration.getWorkingCopy().remo
		StandaloneDeployer sd = new StandaloneDeployer();
		sd.deploy(configuration);
		
	}
	
	
	
	
	
	

}
