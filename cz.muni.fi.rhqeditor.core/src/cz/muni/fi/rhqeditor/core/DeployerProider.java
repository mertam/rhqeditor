package cz.muni.fi.rhqeditor.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.RhqConstants;


/**
 * class represents singleton provider, which creates and holds information about Deployer in temp direcory
 * @author syche
 *
 */
public class DeployerProider {
	
    private static final DeployerProider instance = new DeployerProider();
    private String deployerDirPath = null;
    private Path deployerPath = null;
    private static String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private DeployerProider() {
    	
    }
 
    public static DeployerProider getInstance() {
        return instance;
    }
    
    public String getDirectory(){
    	return deployerDirPath;
    }
    
    public void setDirectory(String prefix) throws IOException{
    	deployerDirPath = Files.createTempDirectory(prefix).toString();
    }
    
    public Path getDeployerPath(){
    	return deployerPath;
    }
    
    public boolean isExexutable(){
    	if(deployerPath == null)
    		return false;
    	return Files.isExecutable(deployerPath);
    }
    
    public void initializeDeployer(URL zippedDeployer){
    	try {
    		//avoid multiple initialization
    		if(deployerPath != null)
    			return;
    		
    		setDirectory("deployer");
			InputStream in = zippedDeployer.openStream();													
			String pathToArchive = deployerDirPath.toString() + FILE_SEPARATOR + RhqConstants.RHQ_STANDALONE_DEPLOYER;
			FileOutputStream out = new FileOutputStream(new File(pathToArchive));
			copyInputStream(in, out);
			unzipFile(pathToArchive);
			
			out.close();
			in.close();
			//removes .zip
			String deployerDir = RhqConstants.RHQ_STANDALONE_DEPLOYER.toString().substring(0,RhqConstants.RHQ_STANDALONE_DEPLOYER.length() - 4);
			if(System.getProperty("os.name").equals("Windows"))
				deployerPath = FileSystems.getDefault().getPath(deployerDirPath +FILE_SEPARATOR+
						deployerDir+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"rhq-ant.bat");
			else
	    		deployerPath = FileSystems.getDefault().getPath(deployerDirPath +FILE_SEPARATOR+
	    				deployerDir+FILE_SEPARATOR+"bin"+FILE_SEPARATOR+"rhq-ant");
			
			setPermisions();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void unzipFile(String file){
    	try {
	      ZipFile zipFile = new ZipFile(file);

	      Enumeration<? extends ZipEntry> entries = zipFile.entries();

	      while(entries.hasMoreElements()) {
	        ZipEntry entry = (ZipEntry)entries.nextElement();

	        if(entry.isDirectory()) {
	          (new File(deployerDirPath + FILE_SEPARATOR + entry.getName())).mkdir();
	          continue;
	        }

	       
	        OutputStream out = new FileOutputStream(deployerDirPath + FILE_SEPARATOR + entry.getName());
	        copyInputStream(zipFile.getInputStream(entry),out);
	        out.close();
	      }

	      
	      zipFile.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	      return;
	    }
	  }
    	
    	
    private void copyInputStream(InputStream in, OutputStream out) throws IOException{
    	byte buf[] = new byte[1024];
		int read;	
    	
    	while((read = in.read(buf)) > -1){
			out.write(buf,0,read);
		}
    }
    
    private void setPermisions(){
    	if(!isExexutable()){
    		if(System.getProperty("os.name").equals("Windows")){
    		}else{
    			 try {
					Set<PosixFilePermission> perms = Files.getPosixFilePermissions(deployerPath);
					perms.add(PosixFilePermission.OWNER_EXECUTE);
					Files.setPosixFilePermissions(deployerPath, perms);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
    }
    
    

}
