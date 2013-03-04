package cz.muni.fi.rhqeditor.core;

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

import utils.ExtractorProvider;
import utils.RhqPathExtractor;

public class BundleExport {


	public IProject fProject = null;
	public String fTargetFile;
		
	public BundleExport(IProject proj, String targetDir){
		fProject = proj;
		fTargetFile = targetDir;
	}
	
	
	
	
	public void ExportBundle() throws IOException{
		 
		ExtractorProvider provider = ExtractorProvider.getInstance();
		final RhqPathExtractor extractor = provider.getMap().get(fProject); 
		
		if(extractor.shouldBeListed())
			extractor.listFiles();
		System.out.println(fTargetFile);
	   
	    System.out.println(fProject.getLocation());
	    Job export = new Job("export bundle") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try(
					 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fTargetFile));
						) {
					 	   byte[] buf = new byte[1024];
					       IFile currentFile;
					       for (IPath file: extractor.getAllFiles() ) {
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
		
		
	    
	}
	
	
}
