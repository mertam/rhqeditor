package cz.muni.fi.rhqeditor.core;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import cz.muni.fi.rhqeditor.core.listeners.RecipeChangeListener;

public class Activator implements BundleActivator {

	private static BundleContext context;

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
