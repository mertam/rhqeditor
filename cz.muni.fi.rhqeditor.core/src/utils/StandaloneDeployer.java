package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.console.MessageConsoleStream;

import cz.muni.fi.rhqeditor.core.Activator;

public class StandaloneDeployer {

	//console stream to write output
	protected MessageConsoleStream fConsoleStream;
	//project directory
	private IProject fProject;
	protected IPath fRunningDir;
	private ArrayList<Path> fCreatedLinks;
	
	public StandaloneDeployer(){
		fCreatedLinks = new ArrayList<>();
	}
	
	public void setMessageConsoleStream(MessageConsoleStream mcs){
		fConsoleStream = mcs;
	}
	
	public void setProject(IProject proj){
		fProject = proj;
	}
	
	
	
	public void deploy(){
		
		fRunningDir = fProject.getLocation();
		InputPropertiesManager propManager = new InputPropertiesManager(fProject);
		
		System.out.println("standalone deployment");
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.UI_PLUGIN_ID);
		
		String pathToDeployer = prefs.get(RhqConstants.RHQ_DEPLOYER_PATH, RhqConstants.NOT_FOUND);
		if(pathToDeployer.equals(RhqConstants.NOT_FOUND))
		{
			fConsoleStream.print("Unset path to deployer.");
			return;
		}
		
		
		System.out.println(fRunningDir.lastSegment().toString());
		IScopeContext projectScope = new ProjectScope(fProject);
		IEclipsePreferences projNode = projectScope.getNode(RhqConstants.RHQ_PROPERTY_NODE);
	    		
		String deployDir = projNode.get(RhqConstants.RHQ_DEPLOY_DIR, RhqConstants.NOT_FOUND);
		if(deployDir.equals(RhqConstants.NOT_FOUND)){
			fConsoleStream.print("Unset rhq.deploy.dir");
			return;
		}
		
		StringBuilder deployCommand = new StringBuilder(pathToDeployer+" ");
		deployCommand.append("-D"+RhqConstants.RHQ_DEPLOY_DIR + "=" +deployDir+" ");
		
		
		String propertyValue;
		for(String property: propManager.getInputPropertiesFromRecipe()){
			propertyValue = projNode.get(RhqConstants.RHQ_PROPERTY_INPUT+property, RhqConstants.NOT_FOUND);
			if(!propertyValue.equals(RhqConstants.NOT_FOUND) && !propertyValue.isEmpty()){
				deployCommand.append("-D"+property+"="+propertyValue+" ");
			}
			
		}
	
	    final String cmd =deployCommand.toString();
	    final File dir = new File(fRunningDir.toString());
	    initializeStandaloneDeployment();
	       
	    Job deployment = new Job("deploy"){
	    	   
	    	@Override
			protected IStatus run(IProgressMonitor monitor) {
			      Process p;
			      fConsoleStream.println("running deployer with command: " + cmd);
			      try {
			    	  p = Runtime.getRuntime().exec(cmd,null,dir);
			    	  BufferedReader stdInput = new BufferedReader(new 
			                 InputStreamReader(p.getInputStream()));
			    	  String line;
			    	  while ((line = stdInput.readLine()) != null) {
						  fConsoleStream.println(line);
			    	  }
					} catch (IOException e) {
						deleteLinks();
						return Status.CANCEL_STATUS;
					}
			    deleteLinks();
				return Status.OK_STATUS;
			}
	       };
	
		deployment.schedule();  
	}
	
	
	/**
	 * method creates symbolic link to all files in project, which are linked into workspace
	 * @return
	 */
	private boolean initializeStandaloneDeployment(){
		ExtractorProvider provider = ExtractorProvider.getInstance();
		RhqPathExtractor extractor = provider.getMap().get(fProject);
		IFile currentFile;
		Path link, target;
		for(IPath pathToFile: extractor.getAllFiles()){
			currentFile = fProject.getFile(pathToFile.toString());
			if(currentFile.isLinked()){
				URI uri = currentFile.getLocationURI();
				target = Paths.get(uri);
				try {
					uri = new URI(fProject.getLocationURI().toString() + System.getProperty("file.separator")+ pathToFile.toString());
				} catch (URISyntaxException e) {
					e.printStackTrace();
					continue;
				}
				link = Paths.get(uri);
				try {
					Files.createLink(link, target);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(fProject.getLocationURI().toString() + System.getProperty("file.separator")+ pathToFile.toString());
				fCreatedLinks.add(link);
			}
			 
		}
		return true;
	}
	
	private void deleteLinks(){
		for(Path p: fCreatedLinks){
			try {
				Files.delete(p);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
