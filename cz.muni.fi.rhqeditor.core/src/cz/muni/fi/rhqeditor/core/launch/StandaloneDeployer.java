package cz.muni.fi.rhqeditor.core.launch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import cz.muni.fi.rhqeditor.core.Activator;
import cz.muni.fi.rhqeditor.core.utils.InputPropertiesManager;
import cz.muni.fi.rhqeditor.core.utils.InputProperty;
import cz.muni.fi.rhqeditor.core.utils.RhqConstants;

public class StandaloneDeployer {

	// console stream to write output
	protected MessageConsoleStream fConsoleStream;
	protected MessageConsole fConsole;
	// project directory
	private IProject fProject;
	protected IPath fRunningDir;

	private final String EMPTY_VALUE = "";

	public StandaloneDeployer() {
	}

	public void setMessageConsoleStream(MessageConsoleStream mcs) {
		fConsoleStream = mcs;
	}

	public void setProject(IProject proj) {
		fProject = proj;
	}

	public void deploy(ILaunchConfiguration configuration,
			IProgressMonitor monitor) {

		StringBuilder deployCommand = new StringBuilder();

		try {
			String projectName = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_PROJECT,
					RhqConstants.NOT_FOUND);
			if (projectName.equals(RhqConstants.NOT_FOUND))
				return;

			fProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);

			if (fProject == null)
				return;

			initializeDeployment();
			if (monitor.isCanceled())
				return;

			boolean useDefaultDeployer = configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DEPLOYER, true);

			String pathToDeployer = null;
			// initialize standalone deployer
			if (useDefaultDeployer) {
				pathToDeployer = initializeLocalDeployer();
			} else {
				pathToDeployer = configuration.getAttribute(
						RhqConstants.RHQ_LAUNCH_ATTR_LOCAL_DEPLOYER,
						RhqConstants.NOT_FOUND);
			}

			if (pathToDeployer.equals(RhqConstants.NOT_FOUND))
				return;

			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				pathToDeployer = pathToDeployer + ".bat";
			}

			fConsole = findConsole(fProject.getName()
					+ "[RHQ Standalone deployment]" + pathToDeployer);
			fConsole.clearConsole();
			fConsole.activate();
			deployCommand.append(pathToDeployer + " ");

			// deploy dir
			String dir;
			if (configuration.getAttribute(
					RhqConstants.RHQ_LAUNCH_ATTR_USE_DEFAULT_DIRECTORY, true) == true) {
				dir = getDefaltDeployDirectory();

			} else {
				dir = configuration.getAttribute(
						RhqConstants.RHQ_LAUNCH_ATTR_LOCAL_DIRECTORY,
						RhqConstants.NOT_FOUND);
				if (dir.equals(RhqConstants.NOT_FOUND)) {
					fConsoleStream
							.println("Select deploy directory or use project default.");
					return;
				}
			}
			deployCommand.append("-Drhq.deploy.dir=" + dir + " ");

			InputPropertiesManager propManager = new InputPropertiesManager(
					fProject.getName());

			fConsoleStream = fConsole.newMessageStream();
			boolean continueDeploying = true;

			String inputPropertyValue;
			for (InputProperty property : propManager
					.getInputPropertiesFromRecipe(false)) {
				inputPropertyValue = configuration.getAttribute(
						RhqConstants.RHQ_LAUNCH_ATTR_INPUT_PROPERTY + "."
								+ property.getName(), EMPTY_VALUE);

				if (inputPropertyValue.equals(EMPTY_VALUE)) {
					// has default value?
					if (property.getValue() != null
							&& !property.getValue().isEmpty()) {
						deployCommand.append("-D" + property.getName() + "="
								+ property.getValue() + " ");
						// print error if property is required, filter
						// rhq.deploy.dir
					} else if (property.isRequired()
							&& !property.getName().equals(
									RhqConstants.RHQ_DEPLOY_DIR)) {
						continueDeploying = false;
						fConsoleStream.println("Input property \""
								+ property.getName()
								+ "\" is neither set neither inicialized");
					}
				} else {
					// add value from user
					deployCommand.append("-D" + property.getName() + "="
							+ inputPropertyValue + " ");
				}

			}

			// undefined value of required property
			if (!continueDeploying)
				return;

			System.out.println(deployCommand);

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (monitor.isCanceled()) {
			return;
		}

		final String cmd = deployCommand.toString();
		final File deployDir = new File(fProject
				.getFolder(RhqConstants.RHQ_DEFAULT_BUILD_DIR).getLocation()
				.toString());

		Job deployment = new Job("Standalone deployment") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Process deploymentProcess;
				fConsoleStream.println("running deployer with command: " + cmd);
				try {
					deploymentProcess = Runtime.getRuntime().exec(cmd, null,
							deployDir);
					BufferedReader stdInput = new BufferedReader(
							new InputStreamReader(
									deploymentProcess.getInputStream()));
					String line;
					while ((line = stdInput.readLine()) != null) {
						fConsoleStream.println(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
					try {
						fConsoleStream.println(e.toString());
						fProject.refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e1) {
						fConsoleStream.println("Refresh of resources failed");
					}
				}
				try {
					fProject.refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};

		deployment.schedule();
	}

	/**
	 * initialize local deployer and returns path to it or NOT_FOUND
	 * 
	 * @return
	 */
	private String initializeLocalDeployer() {
		DeployerProvider provider = DeployerProvider.INSTANCE;
		try {
			provider.initializeDeployer(Activator
					.getFileURL(RhqConstants.RHQ_STANDALONE_DEPLOYER_URL));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Path path = provider.getDeployerPath();
		if (path == null || !provider.isExexutable()) {
			return RhqConstants.NOT_FOUND;
		}
		return path.toString();
	}

	private void initializeDeployment() {
		try {

			// remove previous content of build
			IFolder folder = fProject
					.getFolder(RhqConstants.RHQ_DEFAULT_DEPLOY_DIR);
			if (folder.exists()) {
				folder.delete(true, null);
			}

			folder = fProject.getFolder(RhqConstants.RHQ_DEFAULT_BUILD_DIR);
			// delete content of previous deployment
			if (folder.exists()) {
				folder.delete(true, null);
			}

			folder = fProject.getFolder(RhqConstants.RHQ_DEFAULT_BUILD_DIR);
			folder.create(true, true, null);

			for (IResource res : fProject.members()) {
				if (res.getName().toString().startsWith("."))
					continue;
				res.copy(
						new org.eclipse.core.runtime.Path(
								RhqConstants.RHQ_DEFAULT_BUILD_DIR
										+ System.getProperty("file.separator")
										+ res.getName()), true, null);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);

		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	/**
	 * Return path to default deploy directory for project (project/build).
	 * Creates this this in case it doesn't exist.
	 * 
	 * @return
	 * @throws CoreException
	 */
	private String getDefaltDeployDirectory() throws CoreException {
		IFolder folder = fProject
				.getFolder(RhqConstants.RHQ_DEFAULT_DEPLOY_DIR);
		if (!folder.exists())
			folder.create(true, true, null);

		return folder.getLocation().toString();
	}

}
