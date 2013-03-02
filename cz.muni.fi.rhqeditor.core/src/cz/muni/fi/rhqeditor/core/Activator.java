package cz.muni.fi.rhqeditor.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	public static final String UI_PLUGIN_ID = "cz.muni.fi.rhqeditor.ui";

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		//TODO experimental
//			IPath path = Path.fromOSString("pr47/deploy.xml");
//			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//			IWorkspace workspace = root.getWorkspace();
//			workspace.addResourceChangeListener(new RecipeChangeListener(path));
			System.out.println("Activator called");
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
