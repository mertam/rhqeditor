package cz.muni.fi.rhqeditor.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
	public boolean exportBundle(boolean overwrite) throws IOException{

		
		if(fExtractor.shouldBeListed())
			fExtractor.listFiles();
	   
	    final File file = new File(fTargetFile);
	    if(file.exists() && !overwrite){
	    	return false;
	    }
		
		
	    Job export = new Job("Export RHQ bundle") {
			
	    
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try(
					 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
						) {
					 	   byte[] buf = new byte[1024];
					       IFile currentFile;
					       for (IPath file: fExtractor.getAllFiles() ) {
					    	   if(file.toString().startsWith("."))
					    		   continue;
					           currentFile = fProject.getFile(file);
					    	   FileInputStream in = new FileInputStream(currentFile.getLocation().toString());
					    
					           
					           
					           // Add ZIP entry to output stream.
					           out.putNextEntry(new ZipEntry(file.toString()));
					    
					           // Transfer bytes from the file to the ZIP file
					           int len;
					           while ((len = in.read(buf)) > 0) {
					               out.write(buf, 0, len);
					           }
					           in.close();
					       }
					    
					        // Complete the ZIP file
					        out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
				return Status.OK_STATUS;	
				}	
				
		};
		export.schedule();
		return true;
		
	    
	}
	
	
}
