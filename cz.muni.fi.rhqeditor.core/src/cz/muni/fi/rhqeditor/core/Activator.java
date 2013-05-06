package cz.muni.fi.rhqeditor.core;

import java.net.URL;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static ILog fLogger;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
		fLogger = Platform.getLog(context.getBundle());
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	/**
	 * returns project url of given file or null
	 * @param file
	 * @return
	 */
	public static URL getFileURL(String file){
		return  context.getBundle().getEntry(file);
	}
	
	public static ILog getLog() {
		return fLogger;
	}

}
