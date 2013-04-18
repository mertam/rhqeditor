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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;



public class BundleExport {


	public IProject fProject = null;
	public String fTargetFile;
		
	public BundleExport(IProject proj, String targetDir){
		fProject = proj;
		fTargetFile = targetDir;
	}
	
	
	
	
	public int ExportBundle() throws IOException{

		final RhqPathExtractor extractor = ExtractorProvider.INSTANCE.getExtractor(fProject); 
		
		if(extractor.shouldBeListed())
			extractor.listFiles();
		System.out.println(fTargetFile);
	   
	    final File file = new File(fTargetFile);
	    if(file.exists()){
	    	// Message
	    	Shell shell = new Shell();
	    	shell.setSize(300, 100);
	        MessageBox messageDialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.CANCEL | SWT.NO | SWT.YES);
	        messageDialog.setText("Overwrite file");
	        messageDialog.setMessage("File "+ fTargetFile +" exists, overwrite?");
	        int returnCode = messageDialog.open();
	        if(returnCode == SWT.NO)
	        	return SWT.NO;
	        if(returnCode == SWT.CANCEL)
	        	return SWT.CANCEL;
	    }
		
		
	    System.out.println(fProject.getLocation());
	    Job export = new Job("export bundle") {
			
	    
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				try(
					 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
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
		return SWT.OK;
		
	    
	}
	
	
}
