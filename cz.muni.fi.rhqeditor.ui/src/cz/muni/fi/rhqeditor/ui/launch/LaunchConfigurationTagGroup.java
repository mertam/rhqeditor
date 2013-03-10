package cz.muni.fi.rhqeditor.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;


public class LaunchConfigurationTagGroup extends AbstractLaunchConfigurationTabGroup{

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		LaunchMainTab mainTab	 			= new LaunchMainTab();
		LaunchPropertiesTab propertiesTab 	= new LaunchPropertiesTab();
		
		ILaunchConfigurationTab tabs[] = new ILaunchConfigurationTab[2];
		tabs[0]=mainTab;
		tabs[1]=propertiesTab;
		setTabs(tabs);
	}
}
