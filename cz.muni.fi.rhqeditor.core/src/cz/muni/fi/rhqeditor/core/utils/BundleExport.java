package cz.muni.fi.rhqeditor.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import cz.muni.fi.rhqeditor.core.ProjectInitializer;


/**
 * class used for exporting RHQ bundle
 * @author syche
 *
 */
public class BundleExport {

   
	private IProject fProject = null;
	private String fTargetFile;
	private RhqPathExtractor fExtractor;
		
	public BundleExport(IProject proj, String targetFile){
		if(proj == null)
			throw new IllegalArgumentException("Project not found");
		if(targetFile == null)
			throw new IllegalArgumentException("Wrong target file");
		
		fProject = proj;
		fTargetFile = targetFile;
		
		fExtractor = ExtractorProvider.INSTANCE.getExtractor(fProject); 
		if(fExtractor == null) {
			//init project if it hasn't been inited yet. Probably should not happen.
			new ProjectInitializer().initProject(proj);
		}
			
	}
	
	
	
	/**
	 * exports bundle into file given in constructor. Overwrites existing file if 'overwrite' is true
	 * @param overwrite
	 * @return
	 * @throws IOException
	 */
	public boolean exportBundle(boolean overwrite) {

		
		if(fExtractor.shouldBeListed())
			fExtractor.listFiles();
	   
	    final File file = new File(fTargetFile);
	    if(file.exists() && !overwrite){
	    	return false;
	    }
		
		final HashSet<IPath> foldersToCreate = new HashSet<>();
		for	(IPath path: fExtractor.getAllFiles()) {
			 for ( int i = 0; i < path.segmentCount(); i++ ) {
	    		   
	    		   for (int j = 0; j < i; j++) {
	    			   path = path.removeLastSegments(i);
	    			   foldersToCreate.add(path);
	    		   }
	    		  
			 }		
		}
	    Job export = new Job("Export RHQ bundle") {
			
	    
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try(
					 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
						) {
						   int alreadyWorked = 0;
					 	   byte[] buf = new byte[1024];
					       IFile currentFile;
					       FileInputStream in;
					       
					       //crate folders
					       for	(IPath path: foldersToCreate ) {
					    	   out.putNextEntry(new ZipEntry(path.toString() + "/"));
					           out.closeEntry();
					       }
					       
					       for (IPath file: fExtractor.getAllFiles() ) {
					    	   
					    	   if(monitor.isCanceled()) {
					    		   return Status.CANCEL_STATUS;
					    	   }
					    	   if(alreadyWorked < 95) {
					    		   monitor.worked(alreadyWorked++);
					    	   }
					    	   monitor.setTaskName("Exporting "+file.toString());
					    	   
					    	   if(file == null || file.segmentCount() < 1 || file.toString().startsWith("."))
					    		   continue;
					    	   

					           currentFile = fProject.getFile(file);
					    	   in = new FileInputStream(currentFile.getLocation().toString());
					           
					           
					           out.putNextEntry(new ZipEntry(file.toString()));
					    
					           int len;
					           while ((len = in.read(buf)) > 0) {
					               out.write(buf, 0, len);
					               
					           }
					           in.close();
					       }
					    
					        out.close();
						} catch (IOException e) {							
							return new Status(IStatus.ERROR, RhqConstants.PLUGIN_CORE_ID, e.getMessage(), e);
						} 
				return Status.OK_STATUS;	
				}	
				
		};
		export.setUser(true);
		export.schedule();
		

		return true;		
	    
	}
}
